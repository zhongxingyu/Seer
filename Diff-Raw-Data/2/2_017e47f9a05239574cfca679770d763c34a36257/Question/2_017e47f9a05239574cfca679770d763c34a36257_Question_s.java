 package models;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 
 import annotations.Testing;
 
 /**
  * The Class Question delivers all functionality of the questions that other
  * votables don't have (those would be located in the class @see Votable.java.
  */
 public class Question extends Votable {
 
 	/** All users that already voted for the question. */
 	private ArrayList<User> userVotedForQuestion = new ArrayList<User>();
 
 	/** The question id. */
 	private static int question_id = 0;
 
 	/** The best answer to this question. */
 	private Answer bestAnswer;
 
 	/** The time when the best answer has been defined. */
 	private Date bestAnswerSetTime;
 
 	/** The tags of this question. */
 	private ArrayList<String> tags;
 
 	/**
 	 * Instantiates a new question.
 	 * 
 	 * @param content
 	 *            - The content of the question.
 	 * @param questionOwner
 	 *            - The user who asked the question.
 	 */
 	public Question(String content, User questionOwner) {
 		this.owner = questionOwner;
 		this.content = content;
 		currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
 		this.id = question_id;
 		this.score = 0;
 		tags = new ArrayList<String>();
 		userQuestionAnswerManager.getQuestions().add(this);
 		questionOwner.addActivity("Asked question <" + content + ">");
 		question_id++;
 	}
 	
 	@Testing
 	public Question(String content, User questionOwner, int questionId) {
 		this.owner = questionOwner;
 		this.content = content;
 		currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
 		this.id = questionId;
 		this.question_id = questionId;
 		this.score = 0;
 		tags = new ArrayList<String>();
 		userQuestionAnswerManager.getQuestions().add(this);
 		questionOwner.addActivity("Asked question <" + content + ">");
 		question_id++;
 	}
 
 	/**
 	 * Checks if a certain user already voted for this question.
 	 * 
 	 * @param user
 	 *            - The user you want to check if he has alrady voted for this
 	 *            question.
 	 * @return - true if the user already voted for this question or false if he
 	 *         didn't.
 	 */
 	public boolean checkUserVotedForQuestion(User user) {
 		for (int i = 0; i < userVotedForQuestion.size(); i++) {
 			if (user.getName().equals(userVotedForQuestion.get(i).getName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Adds a user to the list of all users who already voted for this question.
 	 * 
 	 * @param user
 	 *            - that voted for the question.
 	 */
 	public void userVotedForQuestion(User user) {
 		userVotedForQuestion.add(user);
 	}
 
 	/**
 	 * Set an answer as the best answer to this question.
 	 * 
 	 * @param answer
 	 *            - the answer that is best answering this question.
 	 */
 	public void setBestAnswer(Answer answer) {
 		if (bestAnswerChangeable() && answer.belongsToQuestion(id)) {
 			if (hasBestAnswer()) {
 				bestAnswer.markAsBestAnswer(false);
 			}
 
 			answer.markAsBestAnswer(true);
 			bestAnswerSetTime = new Date();
 			bestAnswer = answer;
 		}
 	}
 
 	/**
 	 * Checks if an answer has been selected as the best answer.
 	 * 
 	 * @return - true if the best answer has been selected of false if no answer
 	 *         is set as best one.
 	 */
 	public boolean hasBestAnswer() {
 		return bestAnswer != null;
 	}
 
 	/**
 	 * Checks if you can still change the choice of the best answer. This is
 	 * possible in a timespan of 30 min after the first choice.
 	 * 
 	 * @return - true if best answer was set less than 30 min ago or no best
 	 *         answer is set yet, false otherwise.
 	 */
 	public boolean bestAnswerChangeable() {
 		long now = new Date().getTime();
 		long then = hasBestAnswer() ? bestAnswerSetTime.getTime() : now;
 		long diff = now - then; // time difference in ms
 
 		return (diff / (1000 * 60)) < 30;
 	}
 
 	/**
 	 * Adds tags to the question separated by spaces and also adds new Tags to
 	 * the tag-List in the manager.
 	 * 
 	 * @param tags
 	 *            - A string containing all tags separated by spaces
 	 */
 	public void addTags(String tags) {
 		String delimiter = "[ ]+";
 		tags=tags.toLowerCase();
 		String existingTags = new String();
 		for (String newTag : tags.split(delimiter)) {
 			this.tags.add(newTag);
 			if (!userQuestionAnswerManager.getTagList().contains(newTag))
 				userQuestionAnswerManager.addTag(newTag.toLowerCase());
 		}
 	}
 
 	public static String checkTags(String tags) {
 		String delimiter = "[ ]+";
 		// The minimum Levenshtein distance two strings need to have.
 		int maxDistance = 2;
 		tags=tags.toLowerCase();
 		String existingTags = new String();
 		for (String newTag : tags.split(delimiter)) {
 			for (String existingTag : userQuestionAnswerManager.getTagList()) {
 				if (models.algorithms.Levenshtein.getLevenshteinDistance(newTag
						.toLowerCase(), existingTag) < maxDistance
 						&& !userQuestionAnswerManager.getTagList().contains(
 								newTag)) {
 					existingTags = existingTags + "#" + existingTag + " ";
 				}
 			}
 		}
 		return existingTags;
 	}
 
 	/*
 	 * Getter methods
 	 */
 	public Answer getBestAnswer() {
 		return bestAnswer;
 	}
 
 	public ArrayList<String> getTags() {
 		return this.tags;
 	}
 
 }
