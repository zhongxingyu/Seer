 package com.forum.web.controller;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.Map;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class QuestionControllerTest {
 
     private QuestionController questionController;
 
     @Before
     public void setup(){
         this.questionController = new QuestionController(null);
     }
 
     @Test
     public void shouldShowPostQuestionPage(){
         ModelAndView questionPageModelAndView = questionController.postQuestion();
 
         assertThat(questionPageModelAndView.getViewName() ,is("postQuestion"));
     }
 
 }
