 package net.mademocratie.gae.server.services.impl;
 
 import com.google.appengine.api.datastore.Email;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Result;
 import net.mademocratie.gae.server.entities.Proposal;
 import net.mademocratie.gae.server.entities.ProposalVotes;
 import net.mademocratie.gae.server.entities.Vote;
 import net.mademocratie.gae.server.entities.VoteKind;
 import net.mademocratie.gae.server.services.IManageVote;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 /**
  * ManageVoteImpl
  * <p/>
  * Last update  : $LastChangedDate$
  * Last author  : $Author$
  *
  * @version : $Revision$
  */
 public class ManageVoteImpl implements IManageVote {
     private final static Logger LOGGER = Logger.getLogger(ManageVoteImpl.class.getName());
 
     public List<Vote> getProposalVotesOfACitizen(String citizenEmail, Long proposalId) {
         return findProposalVotesByUserEmail(citizenEmail, proposalId);
     }
 
     private List<Vote> findProposalVotesByUserEmail(String citizenEmail, Long proposalId) {
         Email citizenEmailVal = new Email(citizenEmail);
         List<Vote> votes = ofy().load().type(Vote.class)
                 .filter("citizenEmail", citizenEmailVal)
                 .list();
        LOGGER.info("findProposalVotesByUserEmail result " + (votes != null ? votes.size() : "(none)"));
        return votes;
     }
 
     public Vote vote(String citizenEmail, Long proposalId, VoteKind kind) {
         List<Vote> existingVotes = findProposalVotesByUserEmail(citizenEmail, proposalId);
         if (existingVotes.size() > 0) {
             removeVotes(existingVotes);
         }
         Vote vote = new Vote(citizenEmail, proposalId, kind);
         addVote(vote);
         LOGGER.info("* Vote ADDED : " + vote);
         return vote;
     }
 
     private void removeVotes(List<Vote> existingVotes) {
         if (existingVotes == null
          || existingVotes.size() == 0) return;
         Result<Void> entitiesToDelete = ofy().delete().entities(existingVotes);
         entitiesToDelete.now();
         LOGGER.info("vote removed :" + existingVotes.size());
 
     }
 
     private Vote addVote(Vote vote) {
         ofy().save().entity(vote).now();
         LOGGER.fine("addVote result " + vote.toString());
         return vote;
     }
 
     private void removeVote(Vote voteToDelete) {
         ofy().delete().entity(voteToDelete).now();
         LOGGER.info("vote removed :" + voteToDelete.toString());
     }
 
     public ProposalVotes getProposalVotes(Long proposalId) {
         List<Vote> votes= ofy().load().type(Vote.class)
                 .filter("proposal", Key.create(Proposal.class, proposalId))
                 .list();
         return new ProposalVotes(votes);
     }
 
     public void removeProposalVotes(Long proposalId) {
         // votesQueries.removeProposalVotes(proposalId);
     }
 
     public List<Vote> latest(int max) {
         List<Vote> latestVotes = ofy().load().type(Vote.class).limit(max).list();
         // TODO : add ".order("-date")" : desc order seems not working !?
         LOGGER.info("* latest votes asked " + max + " result " + latestVotes.size());
         return latestVotes;
     }
 }
