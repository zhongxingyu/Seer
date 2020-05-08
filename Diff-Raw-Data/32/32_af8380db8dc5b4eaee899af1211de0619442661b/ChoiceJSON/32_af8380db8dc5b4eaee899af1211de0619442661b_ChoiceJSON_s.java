 package api.entities;
 
 import java.util.List;
 
 public class ChoiceJSON extends BaseModelJSON {
 	public Long poll_id;
 	public Long id;
 	public String text;
 	public Boolean correct;
	
 	public List<VoteJSON> votes;
 	
 	/*
 	public ChoiceJSON(Choice c) {
 		this.id = c.id;
 		this.text = c.text;
 		this.poll_id = c.poll.id;
 		for(Vote v: c.votes) {
 			votes.add(new VoteJSON(v));
 		}
 	}
 
 	
 	public Choice toChoice() {
 		Poll poll = Poll.find("byPollID", this.poll_id).first();
 		return new Choice(poll, this.text);
 	}
 	*/
 }
