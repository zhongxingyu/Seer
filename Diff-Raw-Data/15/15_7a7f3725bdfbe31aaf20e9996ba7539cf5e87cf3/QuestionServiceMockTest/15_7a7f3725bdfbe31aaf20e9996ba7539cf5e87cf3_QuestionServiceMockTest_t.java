 /**
  * This is a class for testing QuestionService using EasyMock.
  * 
  * @author Kevin Loh, 5/28/2013
  */
 
 package com.huskysoft.interviewannihilator.service;
 
 import java.io.IOException;
 import java.util.Date;
 
import junit.framework.TestCase;

 import android.util.Log;
 
 import com.huskysoft.interviewannihilator.util.TestHelpers;
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.model.UserInfo;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import static org.easymock.EasyMock.createNiceMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 
public class QuestionServiceMockTest extends TestCase {
 	
 	private static final String TAG = "QuestionServiceMockTest";
 
 	private QuestionService questionService;
 	private NetworkServiceInterface mockNetworkService;
 	private UserInfo userInfo;
 
 	/**
 	 * Construct new test instance
 	 * 
 	 * @param name
 	 *            the test name
 	 */
	public QuestionServiceMockTest(String name) {
		super(name);
 	}
 
 	/**
 	 * Perform pre-test initialization
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		mockNetworkService = createNiceMock(NetworkServiceInterface.class);
 		questionService = QuestionService.getInstance();
 		userInfo = TestHelpers.createDummyUserInfo();
 		questionService.setUserInfo(userInfo);
 	}
 
 	/**
 	 * Tests postQuestion in QuestionService returns the correct value from
 	 * NetworkService.
 	 * 
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 * @throws NetworkException
 	 */
 	public void testPostQuestionCorrectValue() throws JsonGenerationException,
 			JsonMappingException, IOException, NetworkException {
 		// Create the question and its json string
 		Question question = TestHelpers.createDummyQuestion(1);
 		Date date = new Date();
 		question.setAuthorId(userInfo.getUserId());
 		question.setDateCreated(date);
 		ObjectMapper mapper = new ObjectMapper();
 		String questionStr = mapper.writeValueAsString(question);
 
 		// Create and activate the mock networkService object
 		expect(mockNetworkService.postQuestion(questionStr)).andReturn("1");
 		replay(mockNetworkService);
 		questionService.setNetworkService(mockNetworkService);
 
 		// Check the result
 		int result = questionService.postQuestion(question, date);
 		assertEquals(1, result);
 		verify(mockNetworkService);
 	}
 
 	/**
 	 * Tests postQuestion in QuestionService throws a NetworkException
 	 * propagated from NetworkService.
 	 * 
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 * @throws NetworkException
 	 */
 	public void testPostQuestionThrowsException()
 			throws JsonGenerationException, JsonMappingException, IOException,
 			NetworkException {
 		// Create the question and its json string
 		Question question = TestHelpers.createDummyQuestion(1);
 		Date date = new Date();
 		question.setAuthorId(userInfo.getUserId());
 		question.setDateCreated(date);
 		ObjectMapper mapper = new ObjectMapper();
 		String questionStr = mapper.writeValueAsString(question);
 
 		// Create and activate the mock networkService object
 		NetworkException ne = new NetworkException();
 		expect(mockNetworkService.postQuestion(questionStr)).andThrow(ne);
 		replay(mockNetworkService);
 		questionService.setNetworkService(mockNetworkService);
 
 		// Check the result
 		boolean thrown = false;
 		try {
 			questionService.postQuestion(question, date);
 		} catch (NetworkException e) {
 			Log.w(TAG, "False positive: test expected " + e.toString());
 			thrown = true;
 		}
 		assertTrue(thrown);
 		verify(mockNetworkService);
 	}
 
 	/**
 	 * Tests postSolution in QuestionService returns the correct value from
 	 * NetworkService.
 	 * 
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 * @throws NetworkException
 	 */
 	public void testPostSolutionCorrectValue() throws JsonGenerationException,
 			JsonMappingException, IOException, NetworkException {
 		// Create the solution and its json string
 		Solution solution = TestHelpers.createDummySolution(1);
 		Date date = new Date();
 		solution.setAuthorId(userInfo.getUserId());
 		solution.setDateCreated(date);
 		ObjectMapper mapper = new ObjectMapper();
 		String solutionStr = mapper.writeValueAsString(solution);
 
 		// Create and activate the mock networkService object
 		expect(mockNetworkService.postSolution(solutionStr)).andReturn("1");
 		replay(mockNetworkService);
 		questionService.setNetworkService(mockNetworkService);
 
 		// Check the result
 		int result = questionService.postSolution(solution, date);
 		assertEquals(1, result);
 		verify(mockNetworkService);
 	}
 
 	/**
 	 * Tests postSolution in QuestionService throws a NetworkException
 	 * propagated from NetworkService.
 	 * 
 	 * @throws JsonGenerationException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 * @throws NetworkException
 	 */
 	public void testPostSolutionThrowsException()
 			throws JsonGenerationException, JsonMappingException, IOException,
 			NetworkException {
 		// Create the solution and its json string
 		Solution solution = TestHelpers.createDummySolution(1);
 		Date date = new Date();
 		solution.setAuthorId(userInfo.getUserId());
 		solution.setDateCreated(date);
 		ObjectMapper mapper = new ObjectMapper();
 		String solutionStr = mapper.writeValueAsString(solution);
 
 		// Create and activate the mock networkService object
 		NetworkException ne = new NetworkException();
 		expect(mockNetworkService.postSolution(solutionStr)).andThrow(ne);
 		replay(mockNetworkService);
 		questionService.setNetworkService(mockNetworkService);
 
 		// Check the result
 		boolean thrown = false;
 		try {
 			questionService.postSolution(solution, date);
 		} catch (NetworkException e) {
 			Log.w(TAG, "False positive: test expected " + e.toString());
 			thrown = true;
 		}
 		assertTrue(thrown);
 		verify(mockNetworkService);
 	}
 }
