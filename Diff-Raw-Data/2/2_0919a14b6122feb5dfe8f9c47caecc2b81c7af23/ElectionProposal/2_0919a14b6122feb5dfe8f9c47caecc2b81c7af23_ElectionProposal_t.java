 import java.util.Iterator;
 import java.util.Set;
 
 public class ElectionProposal extends ProposalValue {
   Set<Integer> liveNodes;
   int designatedProposer;
   int electionRoundNumber;
 
   public ElectionProposal(Set<Integer> liveNodes,
                           int designatedProposer,
                          int electionRoundNumber) {
     this.liveNodes = liveNodes;
     this.designatedProposer = designatedProposer;
     this.electionRoundNumber = electionRoundNumber;
   }
 
   public Set<Integer> getLiveNodes() {
     return liveNodes;
   }
 
   public int getDesignatedProposer() {
     return designatedProposer;
   }
 
   public int getElectionRoundNumber() {
     return electionRoundNumber;
   }
 
   @Override
   public String toString() {
     String live = "<";
     if (liveNodes.size() > 0) {
       Iterator<Integer> it = liveNodes.iterator();
       live += it.next();
 
       while (it.hasNext())
         live += ", " + it.next();
     }
     live += ">";
 
     return "Election Results [leader=" + designatedProposer + ", round=" + electionRoundNumber + ", live=" + live + "]";
   }
 }
