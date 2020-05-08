 package models;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import api.responses.CreatePollResponse;
 import api.entities.ChoiceJSON;
 import api.entities.Jsonable;
 import api.entities.PollJSON;
 import api.entities.VoteJSON;
 import api.helpers.GsonSkip;
 
 
 import play.db.jpa.Model;
 
 /**
  * Model class for a possible answer linked to a question.
  * @author OpenARS Server API team
  */
 @Entity
 public class Choice extends Model implements Jsonable {
 	private static final long serialVersionUID = 7558864274526935981L;
 	/**
 	 * The poll that this is a choice for.
 	 */
 	@ManyToOne
     @GsonSkip(classes = {Poll.class, CreatePollResponse.class}, applications={"OpenARMS 1.1 Service"})
 	public Poll poll;
 	/**
 	 * The human understandable text describing the choice.
 	 */
 	public String text;
 	
 	/**
 	 * Is this the correct choice for the poll?
 	 */
 	public Boolean correct;
 	
 	/**
 	 * The votes that has used this choice when voting for an instance of a poll.
 	 */
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "choice")
 	public List<Vote> votes;
 
 	public Choice(Poll poll, String text) {
 		this.poll = poll;
 		this.text = text;
 		this.votes = new LinkedList<Vote>();
 	}
 
 	/**
 	 * Used to determine whether this answer was voted for in the last/current voting round
 	 * @return boolean true when it was voted for already or false otherwise
 	 */
 	public boolean inLatestPollInstance() {
 		PollInstance pi = poll.getLatestInstance();
 		List<Vote> latestVotes = pi.votes;
 		for (Vote vote : latestVotes) {
 			if (vote.choice.equals(this)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns list of votes that are associated with the last/current voting round.
 	 * @return List<Vote> list of latest votes
 	 */
 	public List<Vote> latestVotes() {
 		PollInstance lastRound = poll.getLatestInstance();
 		List<Vote> latestVotes = new ArrayList<Vote>();
 
 		if (!votes.isEmpty()) {
 			for (Vote vote : votes) {
 				if (vote.pollInstance.equals(lastRound)) {
 					latestVotes.add(vote);
 				}
 			}
 		}
 		return latestVotes;
 	}
 	
 
 
     /**
      * Turn this Choice into a ChoiceJSON
      * @return PollJSON A PollJSON object that represents this poll.
      */
     public ChoiceJSON toJson() {
     	return toJson(this);
     }
     
     /**
      * Turn a Choice into a ChoiceJSON
      * @return PollJSON A PollJSON object that represents this poll.
      */
     public static ChoiceJSON toJson(Choice c) {
     	ChoiceJSON result = new ChoiceJSON();
     	result.id = c.id;
     	result.text = c.text;
     	if(c.poll != null) {
         	result.poll_id = c.poll.id;
         }
     	result.votes = new LinkedList<VoteJSON>();
 		for(Vote v: c.votes) {
 			result.votes.add(v.toJson());
 		}
 		return result;
     }
     
     /**
      * Turn a ChoiceJSON into a Choice
      * @param json
      * @return Choice the choice object.
      */
     public static Choice fromJson(ChoiceJSON json) {
     	Poll poll = Poll.find("byID", json.poll_id).first();
     	Choice choice = new Choice(poll, json.text);
     	choice.correct = json.correct;
     	choice.votes = new LinkedList<Vote>();
    	for (VoteJSON v : json.votes) {
    		choice.votes.add(Vote.fromJson(v));
     	}
 		return choice;
     }
 }
