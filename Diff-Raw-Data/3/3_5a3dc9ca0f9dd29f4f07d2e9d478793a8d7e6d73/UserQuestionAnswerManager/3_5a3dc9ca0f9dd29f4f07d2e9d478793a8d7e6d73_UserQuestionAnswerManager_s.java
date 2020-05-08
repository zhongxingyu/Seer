 package models;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 /**
  * The Class UserQuestionAnswerManager delivers functionality to coordinate the
  * different entities of the application.
  */
 public class UserQuestionAnswerManager {
 
 	/** All questions stored. */
 	public static ArrayList<Question> questions;
 
 	/** All answers stored. */
 	public static ArrayList<Answer> answers;
 
 	/** All comments stored. */
 	public static ArrayList<Comment> comments;
 
 	/** All registered users. */
 	public static ArrayList<User> users;
 
 	/** All tags that have been used so far. */
 	public static ArrayList<String> tags;
 
 	private static final UserQuestionAnswerManager INSTANCE = new UserQuestionAnswerManager();
 
 	/**
 	 * Delivers the only instance of this class.
 	 * 
 	 * @return - single instance of UserQuestionAnswerManager
 	 */
 	public static UserQuestionAnswerManager getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Instantiates a new UserQuestionAnswerManager.
 	 */
 	private UserQuestionAnswerManager() {
 		questions = new ArrayList<Question>();
 		answers = new ArrayList<Answer>();
 		comments = new ArrayList<Comment>();
 		users = new ArrayList<User>();
 		tags = new ArrayList<String>();
 	}
 
 	/**
 	 * Checks if a username is already occupied.
 	 * 
 	 * @param name
 	 *            - the username you want to check
 	 * @return - true, if the username is already occupied or false otherwise.
 	 */
 	public boolean checkUserNameIsOccupied(String name) {
 		if (users.size() != 0) {
 			for (int i = 0; i < users.size(); i++) {
 				if (name.equals(users.get(i).getName())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Check if there's already a question with a certain content.
 	 * 
 	 * @param - content to be checked
 	 * @return - true if there already exists a question with this content and
 	 *         false otherwise.
 	 */
 	public boolean checkQuestionDuplication(String content) {
 		if (questions.size() != 0) {
 			for (int i = 0; i < questions.size(); i++) {
 				if (content.equals(questions.get(i).getContent())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public ArrayList<Comment> getComments() {
 		return comments;
 	}
 
 	/**
 	 * Gets a user by his name.
 	 * 
 	 * @param name
 	 *            - the name of the user you are looking for. the name
 	 * @return - the user with the username 'name'.
 	 */
 	public User getUserByName(String name) {
 		for (User user : users) {
 			if (user.getName().equals(name))
 				return user;
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the all answers to the question with a certain id.
 	 * 
 	 * @param id
 	 *            - the id of the question you want to get the answers of.
 	 * @return - all answers to the question with the id 'id'.
 	 */
 	public ArrayList<Answer> getAllAnswersByQuestionId(int id) {
 		ArrayList<Answer> questionAnswers = new ArrayList<Answer>();
 		for (Answer answer : answers) {
 			if (answer.getQuestionId() == id) {
 				questionAnswers.add(answer);
 			}
 		}
 		return questionAnswers;
 	}
 
 	/**
 	 * Gets the question with a certain id.
 	 * 
 	 * @param id
 	 *            - the id of the question you are looking for.
 	 * @return - the question with the id #id.
 	 */
 	public Question getQuestionById(int id) {
 		for (Question question : questions)
 			if (question.getId() == id)
 				return question;
 		return null;
 	}
 
 	/**
 	 * Gets the answer with a certain id.
 	 * 
 	 * @param id
 	 *            - the id of the answer you are looking for.
 	 * @return - the answer with the id #id.
 	 */
 	public Answer getAnswerById(int id) {
 		for (Answer answer : answers)
 			if (answer.getId() == id)
 				return answer;
 		return null;
 	}
 
 	/**
 	 * Gets all questions sorted by score.
 	 * 
 	 * @return - the questions sorted by score
 	 */
 	public ArrayList<Question> getQuestionsSortedByScore() {
 		ArrayList<Question> sortedQuestions = questions;
 
 		Collections.sort(sortedQuestions, new VotableComparator());
 
 		return sortedQuestions;
 	}
 
 	/**
 	 * Gets the answers to a certain question sorted by their score.
 	 * 
 	 * @param id
 	 *            - the id of the question you are looking for the answers to.
 	 * @return - all answers sorted by their score.
 	 */
 	public ArrayList<Answer> getAnswersSortedByScore(int id) {
 		ArrayList<Answer> sortedAnswers = this.getAllAnswersByQuestionId(id);
 
 		Collections.sort(sortedAnswers, new VotableComparator());
 
 		return sortedAnswers;
 	}
 
 	/**
 	 * Gets all questions sorted by date.
 	 * 
 	 * @return - all questions sorted by date.
 	 */
 	public ArrayList<Question> getQuestionsSortedByDate() {
 		ArrayList<Question> sortedQuestions = this.getQuestions();
 
 		Collections.sort(sortedQuestions, new DateComparator());
 
 		return sortedQuestions;
 	}
 	
 	/**
 	 * Gets all comments to a certain question sorted by date.
 	 * 
 	 * @param id
 	 *            - the id of the question
 	 *            
 	 * @return - comments sorted by date
 	 */
 	public ArrayList<Comment> getAllCommentsByQuestionIdSortedByDate(int questionId) {
 		ArrayList<Comment> sortedComments = new ArrayList<Comment>();
 		Question currentQuestion = this.getQuestionById(questionId);
 		for (Comment currentComment : this.getComments()) {
 			if (currentComment.getCommentedVotable().equals(currentQuestion))
 				sortedComments.add(currentComment);
 		}
 		return sortedComments;
 	}
 	
 	/**
 	 * Gets all comments to a certain answer sorted by date.
 	 * 
 	 * @param id
 	 *            - the id of the answer
 	 *            
 	 * @return - comments sorted by date
 	 */
 	public ArrayList<Comment> getAllCommentsByAnswerIdSortedByDate(int answerId) {
 		ArrayList<Comment> sortedComments = new ArrayList<Comment>();
 		Answer currentAnswer = this.getAnswerById(answerId);
 		for (Comment currentComment : this.getComments()) {
 			if (currentComment.getCommentedVotable().equals(currentAnswer))
 				sortedComments.add(currentComment);
 		}
 		return sortedComments;
 	}
 
 	/**
 	 * Gets the user log of a certain user by his/her username.
 	 * 
 	 * @param username
 	 *            - the username of the user you are looking for the log of.
 	 * @return - the user log
 	 */
 	public ArrayList<String> getUserLog(String username) {
 		return this.getUserByName(username).getActivities();
 	}
 
 	/**
 	 * Gets all Object of the type votable a certain user created.
 	 * 
 	 * @param userId
 	 *            - the id of the user
 	 * @return - all votables the user with the id #userId created.
 	 */
 	public ArrayList<Votable> getVotablesByUserId(int userId) {
 		ArrayList<Votable> usersVotables = new ArrayList<Votable>();
 		for (Question currentQuestion : questions) {
 			if (currentQuestion.getOwner().getId() == userId) {
 				usersVotables.add(currentQuestion);
 			}
 		}
 		for (Answer currentAnswer : answers) {
 			if (currentAnswer.getOwner().getId() == userId) {
 				usersVotables.add(currentAnswer);
 			}
 		}
 		return usersVotables;
 	}
 
 	/**
 	 * Adds a tag to the list of all tags that have been used.
 	 * 
 	 * @param singleTag
 	 *            - the tag that has to be added.
 	 */
 	public void addTag(String singleTag) {
 		if (!this.tags.contains(singleTag))
 			this.tags.add(singleTag);
 	}
 
 	/**
 	 * Gets the #count newest questions in the knowledgeBase.
 	 * 
 	 * @param count
 	 *            - the number of questions.
 	 * @return - the newest questions in the KB. The size of the array equals
 	 *         'count'.
 	 */
 	public ArrayList<Question> getRecentQuestionsByNumber(int count){
 		ArrayList allQuestions = getQuestionsSortedByDate();
 		ArrayList recentQuestions= new ArrayList<String>();
 		int size = allQuestions.size();
 
 		// Pick last '#count' questions out of the list sorted by date.
 		for (int i = size - 1; i >= size - count && i >= 0; i--) {
 			recentQuestions.add((Question) allQuestions.get(i));
 		}
 		return recentQuestions;
 	}
 
 	/*
 	 * Getter methods
 	 */
 	public ArrayList<Question> getQuestions() {
 		return questions;
 	}
 
 	public ArrayList<Answer> getAnswers() {
 		return answers;
 	}
 
 	public ArrayList<User> getUsers() {
 		return users;
 	}
 	
 	public ArrayList<String> getTagList(){
 		return this.tags;
 	}
 }
