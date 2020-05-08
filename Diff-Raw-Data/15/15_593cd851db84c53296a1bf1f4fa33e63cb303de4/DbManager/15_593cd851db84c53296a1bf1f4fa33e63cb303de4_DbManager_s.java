 package models;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.NoSuchElementException;
 
 import comparators.ChangedDateComparator;
 import comparators.DateComparator;
 import comparators.ScoreComparator;
 
 /**
  * The Class UserQuestionAnswerManager delivers functionality to coordinate the
  * different entities of the application.
  */
 public class DbManager {
 
 	/** All questions stored. */
 	private static ArrayList<Question> questions;
 
 	/** All answers stored. */
 	private static ArrayList<Answer> answers;
 
 	/** All comments stored. */
 	private static ArrayList<Comment> comments;
 
 	/** All registered users. */
 	private static ArrayList<User> users;
 
 	/** All tags that have been used so far. */
 	private static ArrayList<String> tags;
 	
 	private static ArrayList[] reputations;
 
 	/** 4 Counters for the Id's */
 	private int userCounterIdCounter;
 	private int questionIdCounter;
 	private int answerIdCounter;
 	private int commentIdCounter;
 
 	private static final DbManager INSTANCE = new DbManager();
 
 	/**
 	 * Delivers the only instance of this class.
 	 * 
 	 * @return - single instance of UserQuestionAnswerManager
 	 */
 	public static DbManager getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Instantiates a new UserQuestionAnswerManager.
 	 */
 	private DbManager() {
 		questions = new ArrayList<Question>();
 		answers = new ArrayList<Answer>();
 		comments = new ArrayList<Comment>();
 		users = new ArrayList<User>();
 		tags = new ArrayList<String>();
 		this.userCounterIdCounter = 0;
 		this.questionIdCounter = 0;
 		this.answerIdCounter = 0;
 		this.commentIdCounter = 0;
 		reputations = new ArrayList[3];
 		reputations[0] = new ArrayList<User>();
 		reputations[1] = new ArrayList<Date>();
 		reputations[2] = new ArrayList<Integer>();
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
 
 	/**
 	 * Deletes a user and all entries he or she wrote.
 	 * 
 	 * @param - The username of the user to be deleted as a string object.
 	 */
 	public void deleteUser(String username){
 		if(!DbManager.users.contains(this.getUserByName(username)))
 			throw new NoSuchElementException();
 		
 		User deleteUser= getUserByName(username);
 		
 		ArrayList<Question> updatedQuestions= new ArrayList<Question>();
 		ArrayList<Answer> updatedAnswers= new ArrayList<Answer>();
 		ArrayList<Comment> updatedComments = new ArrayList<Comment>();
 		users.remove(deleteUser);
 		
 		// Delete all questions a user added
 		for (Question q : DbManager.questions) {
 			if (!q.getOwner().equals(deleteUser))
 				updatedQuestions.add(q);
 		}
 		DbManager.questions.clear();
 		DbManager.questions.addAll(updatedQuestions);
 
 		// Delete all answers a user added
 		for (Answer a : DbManager.answers) {
 			if (!a.getOwner().equals(deleteUser))
 				updatedAnswers.add(a);
 		}
 		DbManager.answers.clear();
 		DbManager.answers.addAll(updatedAnswers);
 
 		// Delete all comments a user added
 		for (Comment c : DbManager.comments) {
 			if (!c.getOwner().equals(deleteUser))
 				updatedComments.add(c);
 		}
 		DbManager.comments.clear();
 		DbManager.comments.addAll(updatedComments);
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
 
 		Collections.sort(sortedQuestions, Collections.reverseOrder(new ScoreComparator()));
 		
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
 
 		Collections.sort(sortedAnswers, Collections.reverseOrder(new ScoreComparator()));
 
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
 	 * Gets all question sorted by the date of their latest change (means adding
 	 * of answers, comments, votes)
 	 */
 	public ArrayList<Question> getQuestionsSortedByLastChangedDate() {
 		ArrayList<Question> sortedQuestions = this.getQuestions();
 
 		Collections.sort(sortedQuestions, new ChangedDateComparator());
 
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
 	public ArrayList<Comment> getAllCommentsByQuestionIdSortedByDate(
 			int questionId) {
 		ArrayList<Comment> sortedComments = new ArrayList<Comment>();
 		Question currentQuestion = this.getQuestionById(questionId);
 		for (Comment currentComment : this.getComments()) {
 			if (currentComment.getCommentedVotable().equals(currentQuestion))
 				sortedComments.add(currentComment);
 		}
 		Collections.sort(sortedComments, new DateComparator());
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
 		Collections.sort(sortedComments, new DateComparator());
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
 	public ArrayList<Post> getVotablesByUserId(int userId) {
 		ArrayList<Post> usersVotables = new ArrayList<Post>();
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
 	public ArrayList<Question> getRecentQuestionsByNumber(int count) {
 		// The list of which the newest questions are picked out.
 		ArrayList allQuestions = getQuestionsSortedByLastChangedDate();
 		ArrayList recentQuestions = new ArrayList<String>();
 		int size = allQuestions.size();
 
 		// Pick last '#count' questions out of the list sorted by date.
 		for (int i = size - 1; i >= size - count && i >= 0; i--) {
 			recentQuestions.add((Question) allQuestions.get(i));
 		}
 		return recentQuestions;
 	}
 
 	/**
 	 * Ads a new created user to user ArrayList, sets the id in the user and
 	 * increments the userIdCounter.
 	 * 
 	 * @param user
 	 */
 	public void addUser(User user) {
 		user.setId(userCounterIdCounter);
 		users.add(user);
 		userCounterIdCounter++;
 	}
 
 	/**
 	 * Ads a new created question to question ArrayList, sets the id in the
 	 * question and increments the questionIdCounter.
 	 * 
 	 * @param question
 	 */
 	public void addQuestion(Question question) {
 		question.setId(questionIdCounter);
 		questions.add(question);
 		questionIdCounter++;
 	}
 
 	/**
 	 * Ads a new created answer to answer ArrayList, sets the id in the answer
 	 * and increments the answerIdCounter.
 	 * 
 	 * @param answer
 	 */
 	public void addAnswer(Answer answer) {
 		answer.setId(answerIdCounter);
 		answers.add(answer);
 		answerIdCounter++;
 	}
 
 	/**
 	 * Ads a new created comment to comment ArrayList, sets the id in the
 	 * comment and increments the commentIdCounter.
 	 * 
 	 * @param comment
 	 */
 	public void addComment(Comment comment) {
 		comment.setId(commentIdCounter);
 		comments.add(comment);
 		commentIdCounter++;
 	}
 
 	/**
 	 * Resets all counters to 0
 	 */
 	public void resetAllIdCounts() {
 		this.userCounterIdCounter = 0;
 		this.questionIdCounter = 0;
 		this.answerIdCounter = 0;
 		this.commentIdCounter = 0;
 	}
 	
 	/**
 	 * Saves a reputation from a specific user and day
 	 */
 	public void addReputation(User user, Date time, int reputation) {
 		reputations[0].add(user);
 		reputations[1].add(time);
 		reputations[2].add(reputation);
 	}
 	
	public int getReputationsByUserAndDate(User user, Date date) {
 		int result = 0;
 		User currentUser;
 		Date currentDate;
 		for (int i = 0; i < reputations[0].size(); i++) {
 			currentUser = (User) reputations[0].get(i);
 			currentDate = (Date) reputations[1].get(i);
			if (user.equals(currentUser ) && date.equals(currentDate)) {
 				result = (Integer) reputations[2].get(i);
 			}
 		}
 		return result;
 	}
 	
 	public ArrayList<Integer> getReputations(User user, int days) {
 		ArrayList<Integer> currentReputations = new ArrayList<Integer>();
 		Date currentDay = new Date();
		for (; days > 0; days--) {
			currentReputations.add(this.getReputationsByUserAndDate(user, currentDay));
 			currentDay.setTime(currentDay.getTime() - 86400000);
 		}
 		return currentReputations;
 	}
 
 	/*
 	 * Getter methods
 	 */
 	public ArrayList<User> getUsers() {
 		return users;
 	}
 
 	public ArrayList<Question> getQuestions() {
 		return questions;
 	}
 
 	public ArrayList<Answer> getAnswers() {
 		return answers;
 	}
 
 	public ArrayList<Comment> getComments() {
 		return comments;
 	}
 
 	public static ArrayList<String> getTagList() {
 		return tags;
 	}
 
 	/*
 	 * Count methods
 	 */
 	public int countOfUsers() {
 		return this.getUsers().size();
 	}
 
 	public int countOfQuestions() {
 		return this.getQuestions().size();
 	}
 
 	public int countOfAnswers() {
 		return this.getAnswers().size();
 	}
 
 	public int countOfComments() {
 		return this.getComments().size();
 	}
 
 	public int countOfTags() {
 		return this.getTagList().size();
 	}
 }
