 package models;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
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
 
 	private static ArrayList<Like> likes;
 
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
		likes = new ArrayList<Like>();
 		this.userCounterIdCounter = 0;
 		this.questionIdCounter = 0;
 		this.answerIdCounter = 0;
 		this.commentIdCounter = 0;
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
 	 * Deletes a user and all entries he or she wrote. Posts which the user
 	 * edited are anonymized.
 	 * 
 	 * @param - The username of the user to be deleted as a string object.
 	 */
 	public void deleteUser(String username) {
 		if (!DbManager.users.contains(this.getUserByName(username)))
 			throw new NoSuchElementException();
 
 		User deleteUser = getUserByName(username);
 		if (!checkUserNameIsOccupied("anonymous"))
 			new User("anonymous", "a@nonymous", "anonymous");
 
 		ArrayList<Question> updatedQuestions = new ArrayList<Question>();
 		ArrayList<Answer> updatedAnswers = new ArrayList<Answer>();
 		ArrayList<Comment> updatedComments = new ArrayList<Comment>();
 		users.remove(deleteUser);
 
 		// Delete all questions a user added
 		int i = 0;
 		while (i < DbManager.questions.size()) {
 			if (questions.get(i).getOwner().equals(deleteUser))
 				deleteQuestion(questions.get(i));
 			else
 				i++;
 		}
 
 		// Anonymize all questions a user edited
 		for (Question p : DbManager.questions) {
 			if (p.getEditor() != p.getOwner()
 					|| p.getEditor().equals(deleteUser))
 				p.setEditor("anonymous");
 		}
 
 		// Delete all answers a user added
 		i = 0;
 		while (i < DbManager.answers.size()) {
 			if (answers.get(i).getOwner().equals(deleteUser))
 				deleteAnswer(answers.get(i));
 			else
 				i++;
 		}
 
 		// Anonymize all answers a user edited
 		for (Post p : DbManager.answers) {
 			if (p.getEditor() != p.getOwner()
 					&& p.getEditor().equals(deleteUser))
 				p.setEditor("anonymous");
 		}
 
 		// Delete all comments a user added
 		for (Comment c : DbManager.comments) {
 			if (!c.getOwner().equals(deleteUser))
 				updatedComments.add(c);
 		}
 		DbManager.comments.clear();
 		DbManager.comments.addAll(updatedComments);
 	}
 
 	/**
 	 * Deletes a certain question and all its answers
 	 * 
 	 * @param question
 	 */
 	public void deleteQuestion(Question question) {
 		for (int i = 0; i <= question.getComments().size() - 1; i++) {
 			if (question.getComments().contains(this.comments.get(i))) {
 				this.comments.remove(this.comments.get(i));
 			}
 		}
 
 		for (Answer a : this.answers) {
 			if (question.getAnswers().contains(a)) {
 				for (int i = 0; i <= a.getComments().size() - 1; i++) {
 					this.comments.remove(i);
 				}
 			}
 		}
 
 		for (Answer a : question.getAnswers()) {
 			deleteAnswer(a);
 		}
 		DbManager.questions.remove(question);
 	}
 
 	/**
 	 * Deletes a certain answer
 	 * 
 	 * @param answer
 	 */
 	public void deleteAnswer(Answer answer) {
 		for (int i = 0; i <= comments.size() - 1; i++) {
 			if (this.getAllCommentsByAnswerIdSortedByDate(answer.getId())
 					.contains(comments.get(i)))
 				this.comments.remove(comments.get(i));
 		}
 
 		DbManager.answers.remove(answer);
 	}
 
 	/**
 	 * Deletes a certain comment
 	 * 
 	 * @param comment
 	 */
 	public void deleteComment(Comment comment) {
 		DbManager.comments.remove(comment);
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
 	 * Gets a user by his id.
 	 * 
 	 * @param uid
 	 *            - the id of the user you are looking for.
 	 * @return - the user with the user id 'uid'.
 	 */
 	public User getUserById(int uid) {
 		for (User user : users) {
 			if (user.getId() == uid)
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
 	 * Get the comment with a certain id.
 	 * 
 	 * @param id
 	 *            - the id of the comment you are looking for
 	 * @return - the comment with the id #id
 	 */
 	public Comment getCommentById(int id) {
 		for (Comment c_comment : comments)
 			if (c_comment.getId() == id)
 				return c_comment;
 		return null;
 	}
 
 	/**
 	 * Gets all questions sorted by score.
 	 * 
 	 * @return - the questions sorted by score
 	 */
 	public ArrayList<Question> getQuestionsSortedByScore() {
 		ArrayList<Question> sortedQuestions = questions;
 
 		Collections.sort(sortedQuestions, Collections
 				.reverseOrder(new ScoreComparator()));
 
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
 
 		Collections.sort(sortedAnswers, Collections
 				.reverseOrder(new ScoreComparator()));
 
 		return sortedAnswers;
 	}
 
 	/**
 	 * Gets all answers sorted by date.
 	 * 
 	 * @return - all answers sorted by date.
 	 */
 	public ArrayList<Answer> getAnswersSortedByDate() {
 		ArrayList<Answer> sortedAnswers = this.getAnswers();
 
 		Collections.sort(sortedAnswers, new DateComparator());
 
 		return sortedAnswers;
 	}
 
 	/**
 	 * Gets all answers from a specific user sorted by Date
 	 * 
 	 * @param userId
 	 *            - the users id
 	 * 
 	 * @return - sorted Answers
 	 */
 	public ArrayList<Answer> getAnswersByUserIdSortedByDate(int userId) {
 		ArrayList<Answer> usersAnswers = new ArrayList<Answer>();
 		ArrayList<Answer> sortedAnswers = this.getAnswersSortedByDate();
 		for (Answer currentAnswer : sortedAnswers) {
 			if (currentAnswer.ownerIs(userId))
 				usersAnswers.add(currentAnswer);
 		}
 		return usersAnswers;
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
 	 * Gets all questions from a specific user sorted by Date
 	 * 
 	 * @param userId
 	 *            - the id of the user, the questions are from
 	 * 
 	 * @return - sorted Questions
 	 */
 	public ArrayList<Question> getQuestionsByUserIdSortedByDate(int userId) {
 		ArrayList<Question> usersQuestions = new ArrayList<Question>();
 		ArrayList<Question> sortedQuestions = this.getQuestionsSortedByDate();
 		for (Question currentQuestion : sortedQuestions) {
 			if (currentQuestion.ownerIs(userId))
 				usersQuestions.add(currentQuestion);
 		}
 		return usersQuestions;
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
 		Post currentQuestion = this.getQuestionById(questionId);
 		for (Comment currentComment : this.getComments()) {
 			if (currentComment.getCommentedPost().equals(currentQuestion))
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
 			if (currentComment.getCommentedPost().equals(currentAnswer))
 				sortedComments.add(currentComment);
 		}
 		Collections.sort(sortedComments, new DateComparator());
 		return sortedComments;
 	}
 
 	public void likeComment(User user, int commentId) {
 		Comment comment = getCommentById(commentId);
 		if (!userCanLikeComment(user, commentId))
 			this.likes.add(new Like(user, comment));
 	}
 
 	public Like getLike(User user, int commentId) {
 
 		Comment comment = getCommentById(commentId);
 
 		return getLike(user, comment);
 	}
 
 	public Like getLike(User user, Comment comment) {
 		for (Like l : likes) {
 			if (l.getUser().equals(user) && l.getComment().equals(comment))
 				return l;
 		}
 		return null;
 	}
 
 	public boolean userCanLikeComment(User user, int commentId) {
 
 		Comment comment = getCommentById(commentId);
 
 		if (getLike(user, comment) != null)
 			return true;
 
 		return false;
 	}
 
 	public void unlikeComment(User user, int commentId) {
 
 		Comment comment = getCommentById(commentId);
 		Like like = getLike(user, comment);
 		this.likes.remove(like);
 	}
 
 	public List<Like> getLikes(int commentId) {
 
 		List<Like> likeList = new ArrayList<Like>();
 		Comment comment = getCommentById(commentId);
 		for (Like l : likes) {
 			if (l.getComment().equals(comment)) {
 				likeList.add(l);
 			}
 		}
 
 		return likeList;
 	}
 
 	public int numberOfLike(int commentId) {
 
 		return getLikes(commentId).size();
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
 		for (Post currentQuestion : questions) {
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
 			recentQuestions.add((Post) allQuestions.get(i));
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
 
 	// ------------------------------------------------------------------------------------------------------
 	public void addQuestion(Question question, int id) {
 		question.setId(id);
 		questions.add(question);
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
 		this.getQuestionById(answer.getQuestionId()).notifyChange();
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
 	public void addReputation(User user, int reputation) {
 		user.addReputation(reputation);
 	}
 
 	/**
 	 * Gets the reputation from a user to a specific date.
 	 * 
 	 * @param user
 	 *            - the specific user.
 	 * @param date
 	 *            - the specific date.
 	 * 
 	 * @return - the reputation as an integer.
 	 */
 	@SuppressWarnings("deprecation")
 	public int getReputationByUserAndDate(User user, Date date) {
 		ArrayList<Integer> allReputations = user.getReputations();
 		int result = 0;
 		int counter = 1;
 		Date currentDate = new Date();
 		while ((counter <= allReputations.size())
 				&& ((date.getYear() != currentDate.getYear())
 						|| (date.getMonth() != currentDate.getMonth()) || (date
 						.getDate() != currentDate.getDate()))) {
 			currentDate = new Date(currentDate.getTime() - 86400000);
 			counter++;
 		}
 		if (counter <= allReputations.size())
 			result = allReputations.get(counter - 1);
 		return result;
 	}
 
 	/**
 	 * Gets all reputations from a user in a specific time range.
 	 * 
 	 * @param user
 	 *            - the specific user.
 	 * @param days
 	 *            - the number of day the reputations are from.
 	 * 
 	 * @return - a list of reputations as integers sorted by date
 	 */
 	public ArrayList<Integer> getReputations(User user, int days) {
 		ArrayList<Integer> reputations = user.getReputations();
 		while (reputations.size() < days) {
 			reputations.add(0);
 		}
 		while (reputations.size() > days) {
 			reputations.remove(reputations.size() - 1);
 		}
 		return reputations;
 	}
 
 	/**
 	 * Updates the reputations of an user
 	 * 
 	 * @param reputatedUser
 	 */
 
 	public void updateReputation(User reputatedUser) {
 		reputatedUser.updateReputation();
 	}
 
 	/** Getters */
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
 
 	private ArrayList<Like> getLikeList() {
 		return likes;
 	}
 
 	/** Count Methods */
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
 
 	public int countOfLikes() {
 		return this.getLikeList().size();
 	}
 
 }
