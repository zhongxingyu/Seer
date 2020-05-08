 package com.forum.service;
 
 
 import com.forum.domain.Question;
 import com.forum.domain.User;
 import com.forum.repository.QuestionRepository;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.*;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class QuestionServiceTest {
 
     private QuestionService questionService;
     private QuestionRepository questionRepository;
     @Before
     public void setup(){
         questionRepository = mock(QuestionRepository.class);
         questionService = new QuestionService(questionRepository);
     }
 
 
     @Test
     public void shouldSaveQuestionToRepository(){
 
         Question question = new Question("Question Title", "Question Description", null, null);
         QuestionRepository mockQuestionRepository = mock(QuestionRepository.class);
         when(mockQuestionRepository.createQuestion(question)).thenReturn(1);
         questionService = new QuestionService(mockQuestionRepository);
 
 
         int questionUpdatedNumber = questionService.createQuestion(question);
 
         assertThat(questionUpdatedNumber, is(1));
     }
 
     @Test
     public void shouldReturnQuestionBasedOnId(){
         Question question = new Question(42,"mock question title","mock question description",new User(),new Date());
         when(questionRepository.getById(42)).thenReturn(question);
 
         Question expectedQuestion = questionService.getById(42);
 
         assertThat(expectedQuestion, is(question));
     }
 
     @Test
     public void shouldReturnLatestQuestions(){
         List<Question> questions = new ArrayList<Question>();
         when(questionRepository.latestQuestion(1,10)).thenReturn(questions);
 
         List<Question> returnedList = questionService.latestQuestion("1","10");
 
         assertThat(returnedList, is(questions));
     }
     @Test
     public void shouldUpdateLikeOfAQuestion(){
         Question question = new Question(100,"Question Title", "Question Description", null, null,0,0,0);
         QuestionRepository mockQuestionRepository = mock(QuestionRepository.class);
         when(mockQuestionRepository.addLikesById(100)).thenReturn(1);
         questionService = new QuestionService(mockQuestionRepository);
 
         int numberOfRowsEffected = questionService.addLikesById(question.getId());
 
         assertThat(numberOfRowsEffected, is(1));
     }
     @Test
     public void shouldUpdateDisLikeOfAQuestion(){
         Question question = new Question(100,"Question Title", "Question Description", null, null,0,0,0);
         QuestionRepository mockQuestionRepository = mock(QuestionRepository.class);
         when(mockQuestionRepository.addDisLikesById(100)).thenReturn(1);
         questionService = new QuestionService(mockQuestionRepository);
 
         int numberOfRowsEffected = questionService.addDisLikesById(question.getId());
 
         assertThat(numberOfRowsEffected, is(1));
     }
     @Test
     public void shouldUpdateFlagsOfAQuestion(){
         Question question = new Question(100,"Question Title", "Question Description", null, null,0,0,0);
         QuestionRepository mockQuestionRepository = mock(QuestionRepository.class);
         when(mockQuestionRepository.addFlagsById(100)).thenReturn(1);
         questionService = new QuestionService(mockQuestionRepository);
 
         int numberOfRowsAffected = questionService.addFlagsByID(question.getId());
 
         assertThat(numberOfRowsAffected, is(1));
     }
 
 }
