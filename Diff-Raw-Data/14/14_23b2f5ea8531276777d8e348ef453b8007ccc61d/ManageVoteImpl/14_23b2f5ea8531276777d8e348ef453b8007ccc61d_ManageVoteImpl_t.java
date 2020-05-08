 package net.mademocratie.gae.server.services.impl;
 
 import com.google.inject.Inject;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Result;
 import com.googlecode.objectify.cmd.Query;
 import net.mademocratie.gae.server.entities.VoteList;
 import net.mademocratie.gae.server.entities.v1.*;
 import net.mademocratie.gae.server.exception.MaDemocratieException;
 import net.mademocratie.gae.server.services.IManageProposal;
 import net.mademocratie.gae.server.services.IManageVote;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 public class ManageVoteImpl implements IManageVote {
 
     private final static Logger LOGGER = Logger.getLogger(ManageVoteImpl.class.getName());
 
     @Inject
     private IManageProposal manageProposal;
 
     public List<Vote> getProposalVotesOfACitizen(Citizen citizen, Long proposalId) {
         return findProposalVotesByAuthor(citizen, proposalId);
     }
 
     private List<Vote> findProposalVotesByAuthor(Citizen author, Long proposalId) {
         List<Vote> votes = ofy().load().type(Vote.class)
                 .filter("author", author)
                 .list();
         List<Vote> proposalVotes = new ArrayList<Vote>();
         for(Vote vote : votes) {
             if (proposalId.equals(vote.getProposalId())) {
                 proposalVotes.add(vote);
             }
         }
         LOGGER.info("findProposalVotesByAuthor result " + (proposalVotes != null ? proposalVotes.size() : "(none)"));
         return proposalVotes;
     }
 
     public Vote vote(Citizen author, Long proposalId, VoteKind kind) throws MaDemocratieException {
         Proposal proposal = manageProposal.getById(proposalId);
         if (proposal == null) {
             throw new MaDemocratieException("unable to vote : proposal no more exits");
         }
         return vote(author, proposal, kind);
     }
 
     public Vote vote(Citizen author, Proposal proposal, VoteKind kind) throws MaDemocratieException {
         List<Vote> existingVotes = findProposalVotesByAuthor(author, proposal.getContributionId());
         if (existingVotes.size() > 0) {
             removeVotes(existingVotes);
         }
         Vote vote = new Vote(author, proposal, kind);
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
 
     public VoteList getProposalVotes(Long proposalId) {
         List<Vote> votes= ofy().load().type(Vote.class)
                 .filter("proposal", Key.create(Proposal.class, proposalId))
                 .list();
         return new VoteList(votes);
     }
 
     public void removeProposalVotes(Long proposalId) {
         // votesQueries.removeProposalVotes(proposalId);
     }
 
     public List<Vote> latest() {
         return latest(0);
     }
     public List<Vote> latest(int max) {
         Query<Vote> orderedVotes = ofy().load().type(Vote.class).order("-date");
         if (max > 0) {
             orderedVotes = orderedVotes.limit(max);
         }
         List<Vote> latestVotes = orderedVotes.list();
         int resultCount = latestVotes != null ? latestVotes.size() : 0;
         LOGGER.info("* latest votes asked " + (max > 0 ? max : "unlimited") + " result " +resultCount);
         return latestVotes;
     }
 
     public VoteList latestAsList(int max) {
         return new VoteList(latest(max));
     }
 
     public List<VoteOnProposal> fetchProposalsVotes(List<Vote> votes) {
         int votesCount = votes.size();
         List<Long> proposalIds = new ArrayList<Long>(votesCount);
         for (Vote v : votes) {
             proposalIds.add(v.getProposalId());
         }
         Map<Long, Proposal> proposalMap = ofy().load().type(Proposal.class).ids(proposalIds);
         List<VoteOnProposal> votesOnProposals = new ArrayList<VoteOnProposal>(votesCount);
         for(Vote v: votes){
             Proposal voteProposalContent = proposalMap.get(v.getProposalId());
             VoteOnProposal voteOnProposal = new VoteOnProposal(v, voteProposalContent);
             votesOnProposals.add(voteOnProposal);
         }
         return votesOnProposals;
     }
 
     public void removeAll() {
         int limit = 100;
         List<Vote> votes = ofy().load().type(Vote.class).limit(limit).list();
         if (votes.size() > 0) {
            LOGGER.info("will remove " + votes.size() + " vote(s) :" + votes.toString());
             ofy().delete().entities(votes).now();
         }
         LOGGER.info(votes.size() + " vote(s) removed");
         if (votes.size() == limit) {
             removeAll();
         }
     }
 }
