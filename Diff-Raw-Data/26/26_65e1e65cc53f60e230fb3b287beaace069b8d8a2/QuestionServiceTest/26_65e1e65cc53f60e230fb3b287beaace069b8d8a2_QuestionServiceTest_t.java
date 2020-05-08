 package com.huskysoft.interviewannihilator.service;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
import java.io.IOException;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 import org.json.JSONException;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.huskysoft.interviewannihilator.model.Difficulty;
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.service.NetworkService;
 import com.huskysoft.interviewannihilator.service.QuestionService;
 import com.huskysoft.interviewannihilator.util.PaginatedQuestions;
 
 public class QuestionServiceTest extends TestCase {
 
 	private static final String GET_QUESTIONS_RESPONSE = "{\"results\":\"[{\\\"questionId\\\":\\\"2\\\",\\\"authorId\\\":\\\"2\\\",\\\"qtext\\\":\\\"test question text\\\",\\\"title\\\":\\\"testy question\\\",\\\"likes\\\":\\\"0\\\",\\\"dislikes\\\":\\\"0\\\",\\\"difficulty\\\":\\\"EASY\\\",\\\"category\\\":\\\"BUSINESS\\\",\\\"dateCreated\\\":\\\"10514\\\"},{\\\"questionId\\\":\\\"5\\\",\\\"authorId\\\":\\\"2\\\",\\\"qtext\\\":\\\"second test question\\\",\\\"title\\\":\\\"testy question 2\\\",\\\"likes\\\":\\\"0\\\",\\\"dislikes\\\":\\\"0\\\",\\\"difficulty\\\":\\\"MEDIUM\\\",\\\"category\\\":\\\"LOGIC\\\",\\\"dateCreated\\\":\\\"10524\\\"}]\",\"totalNumberOfResults\":\"2\",\"limit\":\"10\",\"offset\":\"0\"}";
 	private NetworkService mockNetworkService;
 	private QuestionService questionService;
 	
 	@SuppressWarnings("unchecked")
 	@BeforeClass
 	public void setUp() throws NetworkException {
 		mockNetworkService = mock(NetworkService.class);
 		when(mockNetworkService.getQuestions(any(Difficulty.class), 
 				any(Collection.class), anyInt(), anyInt())).
 				thenReturn(GET_QUESTIONS_RESPONSE);
 		questionService = new QuestionService(mockNetworkService);
 		System.out.println(GET_QUESTIONS_RESPONSE);
 	}
 	
 	@Test
	public void testGetAllQuestions() throws NetworkException, JSONException, IOException {
 		PaginatedQuestions questions = questionService.getQuestions(null, null, 10, 0);
 		// System.out.println(questions);
 	}
 
 }
