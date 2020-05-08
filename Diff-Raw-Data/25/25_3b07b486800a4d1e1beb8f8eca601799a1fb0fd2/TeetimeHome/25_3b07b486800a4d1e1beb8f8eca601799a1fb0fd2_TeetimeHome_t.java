 package com.basinc.golfminus.view.teetime;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.enterprise.context.ConversationScoped;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 
 import org.jboss.seam.faces.context.conversation.Begin;
 import org.jboss.seam.faces.context.conversation.End;
 import org.jboss.seam.transaction.Transactional;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.basinc.golfminus.domain.Score;
 import com.basinc.golfminus.domain.TeeTime;
 import com.basinc.golfminus.domain.TeeTimeParticipant;
 import com.basinc.golfminus.security.Identity;
 import com.basinc.golfminus.view.scores.ScoreUpdated;
 
 @Transactional
 @ConversationScoped
 @Named
 /**
  * Replicate Seam2 EntityHome concept where this would be a backing bean for a page that displays a Single Club
  * 
  * @author Scott
  *
  */
 public class TeetimeHome implements Serializable {
 	private static final long serialVersionUID = 4327132844325203346L;
 
 	private static Logger log = LoggerFactory.getLogger(TeetimeHome.class);
 
 	@Inject
 	EntityManager entityManager;
 
     @Inject Identity identity;
 
     @Inject
     @TeeTimeAdded
     private Event<TeeTime> teeTimeAddedEventSrc;
 
     @Inject
     @ScoreUpdated
     private Event<Score> scoreUpdatedEventSrc;
 
     private TeeTime selection;
     
     private List<EnteredScore> scores = new ArrayList<EnteredScore>();
     
     @Begin
     public void selectTeetime(final Integer id) {
         // NOTE get a fresh reference that's managed by the extended persistence context
     	selection = entityManager.find(TeeTime.class, id.intValue());
     }
 
     @Produces
     @RequestScoped
     @Named("teetime")
 	public TeeTime getSelection() {
 		return selection;
 	}
 
     @End
     public void save() {
     	entityManager.persist(selection);
     	entityManager.flush();
 		teeTimeAddedEventSrc.fire(selection);
     }
 
     @End
     public void saveScores() {
     	//move scores to the teetime participants
     	for (EnteredScore score : scores) {
    		if (score.getUser()!=null && score.getGrossScore()!=null&&score.getAdjustedScore()!=null&&score.getGrossScore()>0&&score.getAdjustedScore()>0) {
     			for (TeeTimeParticipant participant : selection.getParticipants()) {
     				if (score.getUser().equals(participant.getUser())) {
    					participant.addScore(score.getGrossScore(), score.getAdjustedScore());
     					//    		scoreUpdatedEventSrc.fire(participant.getScore());
     					break;
     				}
     			}
     		}
     	}
     	entityManager.joinTransaction();
     	entityManager.persist(selection);
     	entityManager.flush();
     }
     
     @End
     public void cancel() {
     	selection = null;
     	log.info("Cancel updates to Tee Time");
     }
 
     public void createScores() {
     	for (TeeTimeParticipant participant : selection.getParticipants()) {
     		EnteredScore score = new EnteredScore();
     		log.info("Checking to see if {} has a score.",participant.getUser().getName());
     		if (participant.getScore() != null) {
     			score.setUser(participant.getUser());
     			score.setGrossScore(participant.getScore().getGrossScore());
    			score.setAdjustedScore(participant.getScore().getAdjustedScore());
         		log.info("Player {} already has a score.",participant.getUser().getName());
     		} else {
     			score.setUser(participant.getUser());
     		}
        		scores.add(score);
     	}
     	entityManager.flush();
     }
 
 	public List<EnteredScore> getScores() {
 		return scores;
 	}
 
 	public void setScores(List<EnteredScore> scores) {
 		this.scores = scores;
 	}
 	
 	public void newTeetime() {
 		selection = new TeeTime();
 		selection.setOrganizer(identity.getCurrentUser());
 		selection.setDate(Calendar.getInstance().getTime());
 	}
 
 	public void setSelection(TeeTime selection) {
 		this.selection = selection;
 	}
 
 	@Begin
 	public void setSelectionAndPopulateScores(TeeTime selection) {
 		log.info("Setting selection and populating scores");
 		scores.clear();
 		this.selection = selection;
 		createScores();
 	}
 
 	public void getSelectionAndPopulateScores() {
 		log.info("Getting selection and populating scores");
 	}
 
 }
