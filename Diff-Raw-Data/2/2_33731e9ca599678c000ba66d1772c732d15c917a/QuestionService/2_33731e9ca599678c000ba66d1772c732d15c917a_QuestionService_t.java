 /**
  * A class to provide services to the front end for getting questions,
  * getting and posting solutions, etc. 
  * 
  * @author Dan Sanders, Bennett Ng, 5/2/2013
  *
  */
 
 package com.huskysoft.interviewannihilator.service;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 
 import org.json.JSONException;
 
 import com.huskysoft.interviewannihilator.model.Category;
 import com.huskysoft.interviewannihilator.model.Difficulty;
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.util.PaginatedQuestions;
 import com.huskysoft.interviewannihilator.util.PaginatedSolutions;
 
 public class QuestionService {
 	
 	private static final String RESULTS_KEY = "results";
 	private static QuestionService instance;
 	private NetworkService networkService;
	private ObjectMapper mapper;
 
 	private QuestionService() {
 		this(NetworkService.getInstance());
 	}
 	
 	protected QuestionService(NetworkService networkService) {
 		this.networkService = networkService;
 		mapper = new ObjectMapper();
 		mapper.configure(DeserializationConfig.
 		Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 	}
 		
 	/**
 	 * Get the singleton QuestionService
 	 */
 	public static QuestionService getInstance() {
 		if (instance == null) {
 			instance = new QuestionService();
 		}
 		return instance;
 	}
 
 	/**
 	 * The method the front-end of the app calls to retrieve questions from
 	 * the database
 	 * 
 	 * @param categories a list of categories that they want to search for. Can
 	 * be null, which means everything
 	 * @param difficulty the difficulty of questions to get. Can be null, which
 	 * means everything
 	 * @param limit the max number of questions they want returned to them
 	 * @param offset the starting offset of the questions in the database to
 	 * get
 	 * @return A PaginatedQuestions object holding up to limit questions
 	 * @throws NetworkException
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	public PaginatedQuestions getQuestions(List<Category> categories,
 	Difficulty difficulty, int limit, int offset) 
 	throws NetworkException, JSONException, IOException {
 		if (limit < 0 || offset < 0) {
 			throw new IOException("Invalid limit or offset parameter");
 		}
 		String json = networkService.getQuestions(difficulty, categories,
 		limit, offset);
 		PaginatedQuestions databaseQuestions;
 		try {
 			// deserialize "flat parameters"
 			databaseQuestions = mapper.readValue
 			(json, PaginatedQuestions.class);
 			JsonNode node = mapper.readTree(json);
 			
 			// deserialize nested Questions
 			String questionsJson = node.get(RESULTS_KEY).asText();
 			JavaType jtype = TypeFactory.defaultInstance().
 			constructParametricType(List.class, Question.class);
 			List<Question> questions = mapper.readValue(questionsJson, jtype);
 			databaseQuestions.setQuestions(questions);
 		} catch (Exception e) {
 			throw new JSONException("Failed to deserialize JSON :" + json);
 		}
 		return databaseQuestions;
 	}
 	
 	/**
 	 * The method the front-end calls to receive solutions for a given question
 	 * from the database
 	 * 
 	 * @param questionId the id of the question of which solutions will be
 	 * returned
 	 * @param limit the maximum number of solutions to be retrieved
 	 * @param offset the offset of the solutions to retrieve as they are stored
 	 * in the database
 	 * @return A PaginatedSolutions object holding up to limit solutions
 	 * @throws NetworkException
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	public PaginatedSolutions getSolutions(int questionId, int limit,
 	int offset)
 	throws NetworkException, JSONException, IOException {
 		if (limit < 0 || offset < 0) {
 			throw new IOException("Invalid limit or offset parameter");
 		}
 		String json = networkService.getSolutions(questionId, limit, offset);
 		PaginatedSolutions databaseSolutions;
 		try {
 			// deserialize "flat parameters"
 			databaseSolutions = mapper.readValue(json, 
 			PaginatedSolutions.class);
 			JsonNode node = mapper.readTree(json);
 			
 			// deserialize nested Questions
 			String solutionsJson = node.get(RESULTS_KEY).asText();
 			JavaType jtype = TypeFactory.defaultInstance().
 			constructParametricType(List.class, Solution.class);
 			List<Solution> solutions = mapper.readValue(solutionsJson, jtype);
 			databaseSolutions.setSolutions(solutions);
 		} catch (Exception e) {
 			throw new JSONException("Failed to deserialize JSON :" + json);
 		}
 		return databaseSolutions;
 	}
 
 	public int postQuestion(Question toPost) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int postSolution(Solution toPost) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public boolean upvoteSolution(int solutionId) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean downvoteSolution(int solutionId) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public PaginatedQuestions getFavorites(int limit, int offset) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
