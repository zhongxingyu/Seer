 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
import java.util.ArrayList;
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.List;
import java.util.Map;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 
 /**
  * Proposal log abstraction. Capabilities:
  *   Proposals can be logged and subsequently marked as having been accepted.
  *   Proposals can be retrieved as an ArrayList of ProposalLogEntrys.
  *   A client can be brought up-to-date in terms of the log via `restore`.
  */
 public class ProposalLog {
   public static final String kProposalLogFilename = ".proposallog";
   public static final String kProposalAcceptMarker = "accept";
   RIONode owner;
 
   public class ProposalLogEntry {
     Proposal proposal;
     ProposalStatus status;
 
     public ProposalLogEntry(Proposal p) {
       this.proposal = p;
       status = ProposalStatus.Pending;
     }
   }
 
 
   public ProposalLog(RIONode owner) {
     this.owner = owner;
   }
 
 
   /**
    * Mark as accepted the entry in the argument proposal list specified by the
    * target proposal number.
    */
   private void markProposalAsAccepted(List<ProposalLogEntry> proposals,
                                       int target) {
     // TODO: Better way to find proposal than linear search.
     for (ProposalLogEntry entry : proposals) {
       if (entry.proposal.getId() == target) {
         entry.status = ProposalStatus.Accepted;
         break;
       }
     }
   }
 
 
   /**
    * Parse the log file and generate a list of ProposalLogEntrys.
    *
    * @return A chronological list of ProposalLogEntry objects.
    */
   private List<ProposalLogEntry> loadProposalHistory() {
     List<ProposalLogEntry> proposals = new ArrayList<ProposalLogEntry>();
     try {
       PersistentStorageReader reader = owner.getReader(kProposalLogFilename);
       if (!reader.ready())
         return null;
 
       while (reader.ready()) {
         String line = reader.readLine();
         if (line.startsWith(kProposalAcceptMarker)) {
           // The line is denoting the finalization of an existing proposal.
           String[] parts = line.split(" ");
           markProposalAsAccepted(proposals, Integer.parseInt(parts[1]));
         } else {
           ByteArrayInputStream baIn = null;
           ObjectInputStream objIn = null;
           try {
             baIn = new ByteArrayInputStream(line.getBytes());
             objIn = new ObjectInputStream(baIn);
             Proposal p = (Proposal) objIn.readObject();
             proposals.add(new ProposalLogEntry(p));
           } catch (IOException e) {
             return null;
           } catch (ClassNotFoundException e) {
             return null;
           } finally {
             try {
               if (baIn != null)
                 baIn.close();
               if (objIn != null)
                 objIn.close();
             } catch (IOException e) {
               return null;
             }
           }
         }
       }
     } catch (IOException e) {
       return null;
     }
 
     return proposals;
   }
 
 
   /**
    * Get a subset of the proposal history as specified in the log.
    * Note: The argument ints specify indices in terms of log chronology, not
    *       in terms of proposal numbers.
    *
    * @param lo
    *            The index of the first proposal to include from the log.
    *
    * @param hi
    *            The index of the upper bound of the subset (exclusive).
    */
   public List<ProposalLogEntry> getProposalRange(int lo, int hi) {
     return loadProposalHistory().subList(lo, hi);
   }
 
 
   /**
    * Returns true if the log contains a proposal with the argument proposal
    * number.
    */
   public boolean containsProposal(int targetId) {
     Iterator<ProposalLogEntry> it = loadProposalHistory().iterator();
     while (it.hasNext()) {
      if (it.next().proposal.getId() == targetId) {
         return true;
       }
     }
     return false;
   }
 
 
   /**
    * Returns a list of proposals that were issued but not accepted.
    */
   public List<Proposal> restore() {
     List<Proposal> danglingProposals = new ArrayList<Proposal>();
     for (ProposalLogEntry entry : loadProposalHistory()) {
       if (entry.status == ProposalStatus.NotSent ||
           entry.status == ProposalStatus.Pending) {
         danglingProposals.add(entry.proposal);
       }
     }
     return danglingProposals;
   }
 
 
   /**
    * Log a proposal.
    *
    * @param p
    *            The proposal to log.
    */
   public void write(Proposal p) {
     ByteArrayOutputStream baOut = null;
     ObjectOutputStream objOut = null;
     try {
       baOut = new ByteArrayOutputStream();
       objOut = new ObjectOutputStream(baOut);
       PersistentStorageWriter writer = owner.getWriter(kProposalLogFilename, true);
       objOut.writeObject(p);
       writer.write(baOut.toString() + "\n");
     } catch (IOException e) {
       throw new IllegalStateException("Error writing proposal log");
     } finally {
       try {
         if (baOut != null)
           baOut.close();
         if (objOut != null)
           objOut.close();
       } catch (IOException e) {
         throw new IllegalStateException("Error writing proposal log");
       }
     }
   }
 
 
   /**
    * Mark a logged proposal as having been accepted.
    *
    * For example, transactions that have been completed should be marked as
    * accepted.
    *
    * @param targetPropNum
    *            The proposal number of the proposal to mark as accepted.
    */
   public void accept(int targetPropNum) {
     try {
       PersistentStorageWriter writer = owner.getWriter(kProposalLogFilename, true);
       writer.write(kProposalAcceptMarker + " " + targetPropNum + "\n");
     } catch (IOException e) {
       throw new IllegalStateException("Error writing proposal log");
     }
   }
 }
