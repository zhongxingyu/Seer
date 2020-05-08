 /**
  * A class to provide services to the front end for getting questions,
  * getting and posting solutions, etc. 
  * 
  * @author Kevin Loh, Dan Sanders, Bennett Ng, 5/2/2013
  *
  */
 
 package com.huskysoft.interviewannihilator.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 import org.codehaus.jackson.type.TypeReference;
 
 import org.json.JSONException;
 
 import android.util.Log;
 
 import com.huskysoft.interviewannihilator.model.Category;
 import com.huskysoft.interviewannihilator.model.Difficulty;
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.model.UserInfo;
 import com.huskysoft.interviewannihilator.util.PaginatedQuestions;
 import com.huskysoft.interviewannihilator.util.PaginatedSolutions;
 import com.huskysoft.interviewannihilator.util.Utility;
 
 public class QuestionService {
 
 	private static final String RESULTS_KEY = "results";
 	private static final String TAG = "QUESTION_SERVICE";
 	private static QuestionService instance;
 	private NetworkServiceInterface networkService;
 	private ObjectMapper mapper;
 	private UserInfo userInfo;
 	private File baseDir;
 
 	private QuestionService() {
 		this.networkService = NetworkService.getInstance();
 		mapper = new ObjectMapper();
 		mapper.configure(DeserializationConfig.
 				Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 	}
 
 	/**
 	 * Get the singleton QuestionService.
 	 */
 	public static QuestionService getInstance() {
 		if (instance == null) {
 			instance = new QuestionService();
 		}
 		return instance;
 	}
 
 	/**
 	 * Load the local UserInfo, or create it if not found.
 	 * 
 	 * @param baseDir
 	 *            the app's private file directory, from Context
 	 * @param userEmail
 	 *            the user's Email address
 	 * @throws IOException
 	 * @throws NetworkException
 	 */
 	public void initializeUserInfo(File baseDir, String userEmail)
 			throws NetworkException {
 		// open file
 		this.baseDir = baseDir;
 		File file = new File(baseDir, Utility.USER_INFO_FILENAME);
 		try {
 			// parse JSON
 			String json = Utility.readStringFromFile(file);
 			userInfo = mapper.readValue(json, UserInfo.class);
 		} catch (IOException e) {
 			// failed to read userInfo; create a new one
 			Log.w(TAG, e.getMessage());
 			userInfo = new UserInfo();
 		}
 		if (!userEmail.equals(userInfo.getUserEmail())) {
 			// new or non-matching UserInfo; clear history
 			userInfo.setUserEmail(userEmail);
 			userInfo.setUserId(getUserId(userEmail));
 			userInfo.clear();
 		}
 	}
 
 	/**
 	 * Write the local UserInfo to storage. Will use the same File directory
 	 * from which the UserInfo was initialized.
 	 * 
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 */
 	public void writeUserInfo() throws JsonGenerationException,
 			JsonMappingException, IOException {
 		// open file
 		if (baseDir == null || userInfo == null) {
 			throw new IllegalStateException(
 					"UserInfo has not yet been initialized!");
 		}
 		File file = new File(baseDir, Utility.USER_INFO_FILENAME);
 
 		// write UserInfo to file as JSON
 		String json = mapper.writeValueAsString(userInfo);
 		Utility.writeStringToFile(file, json);
 	}
 
 	/**
 	 * Get a specific list of Questions from the remote server. NetworkException
 	 * if one or more QuestionID does not exist.
 	 * 
 	 * @param questionIds
 	 * @return
 	 * @throws IOException
 	 * @throws JSONException
 	 * @throws NetworkException
 	 */
 	public List<Question> getQuestionsById(List<Integer> questionIds)
 			throws IOException, NetworkException {
 		if (questionIds == null || questionIds.size() == 0) {
 			throw new IllegalArgumentException(
 					"Must specify at least one Question ID!");
 		}
 		String json = networkService.getQuestionsById(questionIds);
 		TypeReference<List<Question>> tr = new TypeReference<List<Question>>() {
 		};
 		List<Question> questions = mapper.readValue(json, tr);
 		return questions;
 	}
 
 	/**
 	 * The method the front-end of the app calls to retrieve questions from the
 	 * database. Retrieves most recent questions first.
 	 * 
 	 * @param categories
 	 *            a list of categories that they want to search for. Can be
 	 *            null, which means everything
 	 * @param difficulty
 	 *            the difficulty of questions to get. Can be null, which means
 	 *            everything
 	 * @param limit
 	 *            the max number of questions they want returned to them
 	 * @param offset
 	 *            the starting offset of the questions in the database to get
 	 * @param random
 	 *            a boolean representing whether the questions to be fetched
 	 *            should be random or not
 	 * @return A PaginatedQuestions object holding up to limit questions
 	 * @throws NetworkException
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	public PaginatedQuestions getQuestions(List<Category> categories,
 			Difficulty difficulty, int limit, int offset, boolean random)
 			throws NetworkException, IOException {
 		return getQuestions(categories, difficulty, limit, offset, random,
 							null);
 	}
 
 	private PaginatedQuestions getQuestions(List<Category> categories,
 			Difficulty difficulty, int limit, int offset, boolean random,
 			Integer authorId) throws NetworkException, IOException {
 		if (limit < 0 || offset < 0) {
 			throw new IllegalArgumentException(
 					"Invalid limit or offset parameter");
 		}
 		String json = networkService.getQuestions(difficulty, categories,
 				limit, offset, random, authorId);
 		PaginatedQuestions databaseQuestions;
 
 		// deserialize "flat parameters"
 		databaseQuestions = mapper.readValue(json, PaginatedQuestions.class);
 		JsonNode node = mapper.readTree(json);
 
 		// deserialize nested Questions
 		String questionsJson = node.get(RESULTS_KEY).asText();
 		JavaType jtype = TypeFactory.defaultInstance().constructParametricType(
 				List.class, Question.class);
 		List<Question> questions = mapper.readValue(questionsJson, jtype);
 		databaseQuestions.setQuestions(questions);
 
 		return databaseQuestions;
 	}
 
 	/**
 	 * Retrieves questions the user created
 	 * 
 	 * @param categories
 	 *            list of the categories to get
 	 * @param difficulty
 	 *            the difficulty of the questions to get
 	 * @param limit
 	 *            max number of questions
 	 * @param offset
 	 *            starting offset of questions
 	 * @param random
 	 *            whether the questions that are fetched should be random
 	 * @return questions that the user created
 	 * @throws NetworkException
 	 * @throws IOException
 	 */
 	public PaginatedQuestions getMyQuestions(List<Category> categories,
 			Difficulty difficulty, int limit, int offset, boolean random)
 			throws NetworkException, IOException {
 		requireUserInfo();
 		return getQuestions(categories, difficulty, limit, offset, random,
 				userInfo.getUserId());
 	}
 
 	/**
 	 * Get questions the user has favorited
 	 * 
 	 * @param limit
 	 *            the max number of questions to get
 	 * @param offset
 	 *            the offset of the questions retrieved
 	 * @return the questions the user has favorited
 	 * @throws NetworkException
 	 * @throws IOException
 	 */
 	public PaginatedQuestions getFavoriteQuestions(int limit, int offset)
 			throws NetworkException, IOException {
 		if (limit < 0 || offset < 0) {
 			throw new IllegalArgumentException(
 					"Invalid limit or offset parameter");
 		}
 		requireUserInfo();
 		List<Integer> favoriteList = new ArrayList<Integer>(userInfo
 				.getFavoriteQuestions().keySet());
 		List<Integer> favoritesToFetch = new ArrayList<Integer>();
 		int totalFavorited = favoriteList.size();
 		int toFetch = Math.min(limit, totalFavorited);
 		for (int i = offset; i < toFetch + offset; i++) {
 			favoritesToFetch.add(favoriteList.get(i));
 		}
 		List<Question> favoriteQs = getQuestionsById(favoritesToFetch);
 		return new PaginatedQuestions(favoriteQs, totalFavorited,
 				favoriteQs.size(), offset);
 	}
 
 	/**
 	 * Posts a question to the server.
 	 * 
 	 * @param toPost
 	 *            the Question object that represents the question
 	 * @return the id of the question being posted; -1 if a parsing error has
	 *         occured
 	 * @throws NetworkException
 	 * @throws JSONException
 	 * @throws IOException
 	 * @throws IllegalArgumentException
 	 *             if toPost is null or its fields are set incorrectly
 	 */
 	public int postQuestion(Question toPost) throws NetworkException,
 			IOException {
 		// Check parameter
 		if (toPost == null) {
 			throw new IllegalArgumentException("Invalid Question: null");
 		}
 		if (toPost.getText() == null || toPost.getTitle() == null) {
 			throw new IllegalArgumentException("Null text/title in question");
 		}
 		if (toPost.getText().isEmpty() || toPost.getTitle().isEmpty()) {
 			throw new IllegalArgumentException("Empty text/title in question");
 		}
 		if (toPost.getCategory() == null) {
 			throw new IllegalArgumentException("Null category in question");
 		}
 		if (toPost.getDifficulty() == null) {
 			throw new IllegalArgumentException("Null difficulty in question");
 		}
 
 		// Populate authorId and dateCreated (others are filled in)
 		toPost.setAuthorId(userInfo.getUserId());
 		toPost.setDateCreated(new Date());
 
 		// Post the question and return result
 		String questionStr = mapper.writeValueAsString(toPost);
 		String result = networkService.postQuestion(questionStr);
 		if (result != null) {
 			return Integer.parseInt(result);
 		}
 		return -1;
 	}
 
 	/**
 	 * Posts a question to the server.
 	 * 
 	 * @param toPost
 	 *            the Question object that represents the question
 	 * @param date
 	 *            the date for the posting
	 * @return the id of the question being posted
 	 * @throws NetworkException
 	 * @throws JSONException
 	 * @throws IOException
 	 * @throws IllegalArgumentException
 	 *             if toPost is null or its fields are set incorrectly
 	 */
 	public int postQuestion(Question toPost, Date date)
 			throws NetworkException, IOException {
 		// Check parameter
 		if (toPost == null) {
 			throw new IllegalArgumentException("Invalid Question: null");
 		}
 		if (toPost.getText() == null || toPost.getTitle() == null) {
 			throw new IllegalArgumentException("Null text/title in question");
 		}
 		if (toPost.getText().isEmpty() || toPost.getTitle().isEmpty()) {
 			throw new IllegalArgumentException("Empty text/title in question");
 		}
 		if (toPost.getCategory() == null) {
 			throw new IllegalArgumentException("Null category in question");
 		}
 		if (toPost.getDifficulty() == null) {
 			throw new IllegalArgumentException("Null difficulty in question");
 		}
 
 		// Populate authorId and dateCreated (others are filled in)
 		toPost.setAuthorId(userInfo.getUserId());
 		toPost.setDateCreated(date);
 
 		// Post the question and return result
 		String questionStr = mapper.writeValueAsString(toPost);
 		String result = networkService.postQuestion(questionStr);
 		if (result != null) {
 			return Integer.parseInt(result);
 		}
 		return -1;
 	}
 
 	/**
 	 * Delete a Question. The user must be the author of the Question. Returns
 	 * true on success.
 	 * 
 	 * @param questionId
 	 * @return whether the deletion succeeded
 	 * @throws NetworkException
 	 */
 	public boolean deleteQuestion(int questionId) throws NetworkException {
 		requireUserInfo();
 		boolean success = networkService.deleteQuestion(questionId,
 				userInfo.getUserEmail());
 		if (success) {
 			userInfo.clearFavoriteQuestion(questionId);
 			userInfo.novoteQuestion(questionId);
 		}
 		return success;
 	}
 
 	/**
 	 * Upvote a given Question. Returns true if upvote was received by the
 	 * server, otherwise false.
 	 * 
 	 * @param questionId
 	 *            the id to be voted on
 	 * @return bool indicating the success of the vote
 	 * @throws NetworkException
 	 */
 	public boolean upvoteQuestion(int questionId) throws NetworkException {
 		return voteQuestion(questionId, UserInfo.UPVOTE);
 	}
 
 	/**
 	 * Downvote a given Question. Returns true if downvote was received by the
 	 * server, false otherwise.
 	 * 
 	 * @param questionId
 	 *            the id of the question to be voted on
 	 * @return bool indicating the success of the vote
 	 * @throws NetworkException
 	 */
 	public boolean downvoteQuestion(int questionId) throws NetworkException {
 		return voteQuestion(questionId, UserInfo.DOWNVOTE);
 
 	}
 
 	/**
 	 * Vote on a question
 	 * 
 	 * @param questionId
 	 *            the id of the question to be voted on
 	 * @param upvote
 	 *            whether the question is getting upvoted
 	 * @return whether the vote succeeded
 	 * @throws NetworkException
 	 */
 	private boolean voteQuestion(int questionId, boolean upvote)
 			throws NetworkException {
 		requireUserInfo();
 		Boolean previousVote = userInfo.getQuestionVote(questionId);
 		// If they have already voted on the question, don't allow them
 		// to vote again
 		if (previousVote == null) {
 			boolean res = networkService.voteQuestion(questionId, upvote,
 					userInfo.getUserEmail());
 			if (res) {
 				userInfo.downvoteQuestion(questionId);
 			}
 			return res;
 		}
 		return false;
 	}
 
 	/**
 	 * The method the front-end calls to receive solutions for a given question
 	 * from the database
 	 * 
 	 * @param questionId
 	 *            the id of the question of which solutions will be returned
 	 * @param limit
 	 *            the maximum number of solutions to be retrieved
 	 * @param offset
 	 *            the offset of the solutions to retrieve as they are stored in
 	 *            the database
 	 * @return A PaginatedSolutions object holding up to limit solutions
 	 * @throws NetworkException
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	public PaginatedSolutions getSolutions(int questionId, int limit,
 			int offset)	throws NetworkException, IOException {
 		if (limit < 0 || offset < 0) {
 			throw new IllegalArgumentException(
 					"Invalid limit or offset parameter");
 		}
 		String json = networkService.getSolutions(questionId, limit, offset);
 		PaginatedSolutions databaseSolutions;
 
 		// deserialize "flat parameters"
 		databaseSolutions = mapper.readValue(json, PaginatedSolutions.class);
 		JsonNode node = mapper.readTree(json);
 
 		// deserialize nested Solutions
 		String solutionsJson = node.get(RESULTS_KEY).asText();
 		JavaType jtype = TypeFactory.defaultInstance().constructParametricType(
 				List.class, Solution.class);
 		List<Solution> solutions = mapper.readValue(solutionsJson, jtype);
 		databaseSolutions.setSolutions(solutions);
 
 		if (userInfo != null) {
 			userInfo.markViewedQuestion(questionId);
 		}
 		return databaseSolutions;
 	}
 
 	/**
 	 * Posts a solution to the server.
 	 * 
 	 * @param toPost
 	 *            the Solution object that represents the solution
	 * @return the id of the solution being posted
 	 * @throws NetworkException
 	 * @throws JSONException
 	 * @throws IOException
 	 * @throws IllegalArgumentException
 	 *             if toPost is null or its fields are set incorrectly
 	 */
 	public int postSolution(Solution toPost) throws NetworkException,
 			IOException {
 		// Check parameter
 		if (toPost == null) {
 			throw new IllegalArgumentException("Invalid Solution: null");
 		}
 		if (toPost.getText() == null || toPost.getText().isEmpty()) {
 			throw new IllegalArgumentException("Null/Empty text in solution");
 		}
 
 		// Populate authorId and dateCreated (others are filled in)
 		toPost.setAuthorId(userInfo.getUserId());
 		toPost.setDateCreated(new Date());
 
 		// Post the solution and return result
 		String solutionStr = mapper.writeValueAsString(toPost);
 		String result = networkService.postSolution(solutionStr);
		return Integer.parseInt(result);
 	}
 	
 	/**
 	 * Posts a solution to the server.
 	 * 
 	 * @param toPost
 	 *            the Solution object that represents the solution
 	 * @param date
 	 *            the date for the posting
	 * @return the id of the solution being posted
 	 * @throws NetworkException
 	 * @throws IOException
 	 */
 	public int postSolution(Solution toPost, Date date) throws NetworkException,
 	IOException {
 		// Check parameter
 		if (toPost == null) {
 			throw new IllegalArgumentException("Invalid Solution: null");
 		}
 		if (toPost.getText() == null || toPost.getText().isEmpty()) {
 			throw new IllegalArgumentException("Null/Empty text in solution");
 		}
 
 		// Populate authorId and dateCreated (others are filled in)
 		toPost.setAuthorId(userInfo.getUserId());
 		toPost.setDateCreated(date);
 
 		// Post the solution and return result
 		String solutionStr = mapper.writeValueAsString(toPost);
 		String result = networkService.postSolution(solutionStr);
		return Integer.parseInt(result);
 	}
 
 	/**
 	 * Delete a Solution. The user must be the author of the Solution.
 	 * 
 	 * @param solutionId
 	 * @param userEmail
 	 * @return bool indicating whether the deletion succeeded
 	 * @throws NetworkException
 	 */
 	public boolean deleteSolution(int solutionId) throws NetworkException {
 		requireUserInfo();
 		return networkService.deleteSolution(solutionId,
 				userInfo.getUserEmail());
 	}
 
 	/**
 	 * Upvote a given Solution. Returns true if upvote was received by the
 	 * server, otherwise false.
 	 * 
 	 * @param solutionId
 	 *            the id of the solution to be voted on
 	 * @return bool indicating the result of the vote
 	 * @throws NetworkException
 	 */
 	public boolean upvoteSolution(int solutionId) throws NetworkException {
 		return voteSolution(solutionId, UserInfo.UPVOTE);
 	}
 
 	/**
 	 * Downvote a given Solution. Returns true if downvote was received by the
 	 * server, otherwise false.
 	 * 
 	 * @param solutionId
 	 *            the id of the solution to be voted on
 	 * @return bool indicating the result of the vote
 	 * @throws NetworkException
 	 */
 	public boolean downvoteSolution(int solutionId) throws NetworkException {
 		return voteSolution(solutionId, UserInfo.DOWNVOTE);
 	}
 
 	/**
 	 * Votes on a given solution
 	 * 
 	 * @param solutionId
 	 *            the id of the solution to be voted on
 	 * @param upvote
 	 *            whether the vote is a like
 	 * @return whether the vote succeeded
 	 * @throws NetworkException
 	 * @throws IOException
 	 */
 	private boolean voteSolution(int solutionId, boolean upvote)
 			throws NetworkException {
 		requireUserInfo();
 		Boolean previousVote = userInfo.getSolutionVote(solutionId);
 		// If they have already voted on the solution, don't allow them
 		// to vote again
 		if (previousVote == null) {
 			boolean res = networkService.voteSolution(solutionId, upvote,
 					userInfo.getUserEmail());
 			if (res) {
 				userInfo.downvoteSolution(solutionId);
 			}
 			return res;
 		}
 		return false;
 	}
 
 	/**
 	 * Clears the favorited questions for the user
 	 */
 	public void clearAllFavorites() {
 		userInfo.getFavoriteQuestions().clear();
 	}
 
 	private void requireUserInfo() {
 		if (userInfo == null) {
 			throw new IllegalStateException(
 					"UserInfo has not been initialized!");
 		}
 	}
 
 	/**
 	 * Gets the userId associated with a given email in the database
 	 * 
 	 * @param userEmail
 	 *            the email whose id we are getting
 	 * @return the id associated with the email. Creates a new entry in the
 	 *         database and returns the id of the new entry if the email doesn't
 	 *         exist in the database yet
 	 * @throws NetworkException
 	 * @throws IOException
 	 */
 	protected int getUserId(String userEmail) throws NetworkException {
 		if (userEmail == null) {
 			throw new IllegalArgumentException("userEmail cannot be null");
 		}
 		return networkService.getUserId(userEmail);
 	}
 
 	/**
 	 * Set the NetworkService, for testing.
 	 * 
 	 * @param networkService
 	 */
 	protected void setNetworkService(NetworkServiceInterface networkService) {
 		this.networkService = networkService;
 	}
 
 	/**
 	 * Set the UserInfo, for testing.
 	 * 
 	 * @param userInfo
 	 */
 	protected void setUserInfo(UserInfo userInfo) {
 		this.userInfo = userInfo;
 	}
 }
