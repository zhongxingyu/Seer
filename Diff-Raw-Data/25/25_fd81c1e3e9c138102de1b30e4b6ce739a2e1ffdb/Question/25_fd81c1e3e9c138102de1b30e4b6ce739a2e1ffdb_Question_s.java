 package models;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 
 public class Question extends Votable {
 
 	private ArrayList<User> userVotedForQuestion = new ArrayList<User>();
 	private static int question_id = 0;
 	
 	private Answer bestAnswer;
 	private Date bestAnswerSetTime;
 
 	public Question(String content, User questionOwner) {
 		this.owner = questionOwner;
 		this.content = content;
 		currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
 		this.id = question_id;
 		this.score = 0;
 		userQuestionAnswerManager.getQuestions().add(this);
 		question_id++;
 	}
 
 	public boolean checkUserVotedForQuestion(User user) {
 		for (int i = 0; i < userVotedForQuestion.size(); i++) {
 			if (user.getName().equals(userVotedForQuestion.get(i).getName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void userVotedForQuestion(User user) {
 		userVotedForQuestion.add(user);
 	}
 	
 	/**
 	 * 
 	 * @param answer
 	 */
 	public void setBestAnswer(Answer answer) {
 		if ( bestAnswerChangeable() && answer.belongsToQuestion(id) ) {
 			if ( hasBestAnswer() ) {
 				bestAnswer.markAsBestAnswer(false);
 			}
 			
 			answer.markAsBestAnswer(true);
 			bestAnswerSetTime = new Date();
 			bestAnswer = answer;
 		}
 	}
 	
 	public Answer getBestAnswer() {
 		return bestAnswer;
 	}
 	
 	public boolean hasBestAnswer() {
 		return bestAnswer != null;
 	}
 	
 	/**
	 * Only to be used in a template
	 * @param uid numeric user id as string
	 * @return
	 */
	public boolean isOwner(String uid) {
		return Integer.parseInt(uid) == owner.getId();
	}
	
	/**
 	 * 
 	 * @return true if best answer was set less than 30 min ago or
 	 * 		   no best answer is set yet, false otherwise.
 	 */
 	public boolean bestAnswerChangeable() {
 		long now = new Date().getTime();
 		long then = hasBestAnswer() ? bestAnswerSetTime.getTime() : now;
 		long diff = now - then; // time difference in ms
 		
 		return (diff / (1000*60)) < 30;
 	}
 }
