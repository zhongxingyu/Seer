 package com.huskysoft.interviewannihilator.service;
 
 import junit.framework.TestCase;
 
 import org.json.JSONException;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.service.QuestionService;
 import com.huskysoft.interviewannihilator.util.PaginatedQuestions;
 import com.huskysoft.interviewannihilator.util.PaginatedSolutions;
 
 public class QuestionServiceIntegrationTest extends TestCase {
 
 	private QuestionService questionService;
 	
 	@BeforeClass
 	public void setUp() throws NetworkException {
 		questionService = QuestionService.getInstance();
 	}
 	
 	@Test
	public void testGetAllQuestions() throws NetworkException, JSONException {
 		PaginatedQuestions questions = questionService.getQuestions(null, null, 10, 0);
 		System.out.println(questions);
 	}
 	
 	@Test
	public void testGetSolutions() throws NetworkException, JSONException {
		PaginatedSolutions solutions = questionService.getSolutions(6, 10, 0);
 		System.out.println(solutions);
 	}
 
 }
