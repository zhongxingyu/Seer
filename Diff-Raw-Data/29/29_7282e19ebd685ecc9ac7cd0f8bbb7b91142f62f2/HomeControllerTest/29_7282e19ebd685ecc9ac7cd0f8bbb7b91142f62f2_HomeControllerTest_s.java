 package com.forum.web.controller;
 
 import com.forum.domain.Question;
 
 import com.forum.domain.User;
 import com.forum.service.QuestionService;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class HomeControllerTest {
 
     private QuestionService questionService;
     private HomeController homeController;
     @Before
     public void setup(){
         this.questionService = mock(QuestionService.class);
         this.homeController = new HomeController(questionService);
     }
 
     @Test
     public void shouldDefaultToHomePage(){
         ModelAndView activityModelAndView = homeController.activityView();
         assertThat(activityModelAndView.getViewName(), is("home"));
     }
 
     @Test
     public void shouldLoadMoreQuestionsOnBrowser(){
         List<Question> questions = new ArrayList<Question>();
         Date date = mock(Date.class);
         Question question = new Question(1,"dummy title","dummy description for a dummy question with a dummy title by a dummy developer",new User(),date);
         questions.add(question);
         when(questionService.latestQuestion("1","1")).thenReturn(questions);
 
         String result = homeController.loadMoreQuestions("1","1");
        String expected = "[{\"id\":1,\"title\":\"dummy title\",\"createdAt\":\"Jan 1, 1970 5:30:00 AM\",\"description\":\"dummy description for a dummy question with a dummy title by a dummy developer\",\"user\":{\"encrypter\":{},\"interests\":[],\"knowledge\":[]}}]";
 
         assertThat(result, is(expected) );
     }
 }
