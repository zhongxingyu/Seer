 package se.diversify.webroti.data;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * A meeting
  */
 public class Meeting {
 
    private String id;
     private List<Vote> votes = new ArrayList<Vote>();
 
     /**
      * Create a new meeting
      */
     public Meeting() {
         id = UUID.randomUUID().toString();
     }
 
     /**
      * Get the list of votes for this meeting
      * @return the list of votes for this meeting
      */
     public List<Vote> getVotes() {
        return new ArrayList<Vote>(votes);
     }
 
     /**
      * Get the meeting id
      * @return the meeting id
      */
     public String getId() {
         return id;
     }
 
     /**
      * Add a vote for this meeting
      * @param vote the new vote to add
      */
     public void addVote(Vote vote) {
        votes.add(vote);
     }
 
 }
