 package net.chrissearle.flickrvote.web.admin;
 
 import com.opensymphony.xwork2.ActionSupport;
 import net.chrissearle.flickrvote.service.ChallengeService;
 import net.chrissearle.flickrvote.service.model.ChallengeInfo;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class CreateChallengeAction extends ActionSupport {
     private ChallengeInfo challenge;
 
     @Autowired
     private ChallengeService challengeService;
 
     private static final int START_VOTE_TIME = 18;
     private static final int START_CHALLENGE_TIME = 18;
     private static final int END_CHALLENGE_TIME = 21;
 
     @Override
     public String execute() throws Exception {
         DateTime start = new DateTime(challenge.getStartDate()).plusHours(START_CHALLENGE_TIME);
         DateTime vote = new DateTime(challenge.getVoteDate()).plusHours(START_VOTE_TIME);
         DateTime end = new DateTime(challenge.getEndDate()).plusHours(END_CHALLENGE_TIME);
 
         challengeService.addChallenge(challenge.getTitle(), challenge.getTag(),
                start.toDate(), vote.toDate(), end.toDate());
 
         return SUCCESS;
     }
 
     @Override
     public void validate() {
         if (challenge.getTitle().length() == 0) {
             addFieldError("challenge.title", "Title must be filled out");
         }
         if (challenge.getTag().length() == 0) {
             addFieldError("challenge.tag", "Tag must be filled out");
         }
     }
 
     public ChallengeInfo getChallenge() {
         return challenge;
     }
 
     public void setChallenge(ChallengeInfo challenge) {
         this.challenge = challenge;
     }
 }
