 package com.forum.web.controller;
 
 
 import com.forum.domain.Question;
 import com.forum.domain.User;
 import com.forum.service.QuestionService;
 import org.junit.Test;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class QuestionControllerTest {
 
     private QuestionController questionController;
 
     @Test
     public void shouldShowPostQuestionPage() {
         this.questionController = new QuestionController(null);
         String questionView = questionController.postQuestion(new HashMap());
         assertThat(questionView, is("postQuestion"));
     }
 
     @Test
     public void shouldReturnPostedQuestion() {
         QuestionService mockedQuestionService = mock(QuestionService.class);
         Question question = new Question(1, "Question Title", "Question Description", new User(), new Date());
         List<Question> questionList = new LinkedList<Question>();
         questionList.add(question);
         when(mockedQuestionService.latestQuestion("1", "1")).thenReturn(questionList);
         when(mockedQuestionService.getById(1)).thenReturn(question);
 
         mockedQuestionService.createQuestion(question);
         this.questionController = new QuestionController(mockedQuestionService);
 
         BindingResult result = mock(BindingResult.class);
         when(result.hasErrors()).thenReturn(false);
 
         String questionView = questionController.showPostedQuestion(question, result, new HashMap());
 
         assertThat(questionView, is("redirect:/question/view/" + question.getId()));
     }
 
     @Test
     public void shouldReturnToPostQuestionWhenInvalid() {
         QuestionService mockedQuestionService = mock(QuestionService.class);
         Date createdAt = new Date();
         Question question = new Question(1, "Question Title", "Question Description", new User(), createdAt);
 
         this.questionController = new QuestionController(mockedQuestionService);
 
         BindingResult result = mock(BindingResult.class);
         when(result.hasErrors()).thenReturn(true);
 
         String questionView = questionController.showPostedQuestion(question, result, new HashMap());
 
         assertThat(questionView, is("postQuestion"));
     }
 
     @Test
     public void shouldReturnDetailedViewOfQuestion() {
         QuestionService questionService = mock(QuestionService.class);
         ModelAndView modelAndView;
         User user = new User();
         user.setName("Dummy User");
         Date createdAt = new Date();
         Question question = new Question(42, "model question title", "model question description", user, createdAt);
         when(questionService.getById(42)).thenReturn(question);
         this.questionController = new QuestionController(questionService);
 
         modelAndView = questionController.viewQuestionDetail(42);
         String questionTitle = (String) modelAndView.getModel().get("questionTitle");
         String questionDescription = (String) modelAndView.getModel().get("questionDescription");
         String questionDate = (String) modelAndView.getModel().get("dateCreatedAt");
         String questionTime = (String) modelAndView.getModel().get("timeCreatedAt");
         String questionUserName = (String) modelAndView.getModel().get("username");
 
         assertThat(questionTitle, is("model question title"));
         assertThat(questionDescription, is("model question description"));
        assertThat(questionDate, is(new SimpleDateFormat("MMMM dd,yyyy").format(createdAt)));
         assertThat(questionTime, is(new SimpleDateFormat("hh:mm:ss a").format(createdAt)));
         assertThat(questionUserName, is(user.getName()));
 
     }
 
     @Test
     public void shouldReturnDetailedViewOfQuestionWithLikesDisLikesAndFlags() {
         QuestionService questionService = mock(QuestionService.class);
         ModelAndView modelAndView;
         User user = new User();
         user.setName("Dummy User");
         Date createdAt = new Date();
         Question question = new Question(100, "model question title", "model question description", user, createdAt, 10, 10, 10);
         when(questionService.getById(100)).thenReturn(question);
         this.questionController = new QuestionController(questionService);
 
         modelAndView = questionController.viewQuestionDetail(100);
         Integer questionLikes = (Integer) modelAndView.getModel().get("likes");
         Integer questionDisLikes = (Integer) modelAndView.getModel().get("dislikes");
         Integer questionFlags = (Integer) modelAndView.getModel().get("flags");
 
         assertThat(questionLikes, is(question.getLikes()));
         assertThat(questionDisLikes, is(question.getDislikes()));
         assertThat(questionFlags, is(question.getFlags()));
 
     }
 
     @Test
     public void shouldLikeQuestion() {
         int likes = 1;
         prepareQuestionController(likes, 2, 3);
 
         Map<String, Integer> params = new HashMap<String, Integer>();
         params.put("likes", likes);
 
         String result = questionController.likeQuestion(24, params);
         assertThat(result, is("(" + (likes + 1) + ") Likes"));
     }
 
     @Test
     public void shouldDisLikeQuestion() {
         int dislikes = 1;
         prepareQuestionController(1, dislikes, 3);
 
         Map<String, Integer> params = new HashMap<String, Integer>();
         params.put("dislikes", dislikes);
 
         String result = questionController.dislikeQuestion(24, params);
         assertThat(result, is("(" + (dislikes + 1) + ") Dislikes"));
     }
 
     @Test
     public void shouldFlagQuestion() {
         int flags = 1;
         prepareQuestionController(1, 2, flags);
 
         Map<String, Integer> params = new HashMap<String, Integer>();
         params.put("flags", flags);
 
         String result = questionController.flagQuestion(24, params);
         assertThat(result, is("(" + (flags + 1) + ") Flags"));
     }
 
     private void prepareQuestionController(int likes, int dislikes, int flags) {
         QuestionService questionService = mock(QuestionService.class);
         Question question = new Question(
                 24,
                 "model question title",
                 "model question description",
                 new User(),
                 new Date(),
                 likes + 1,
                 dislikes + 1,
                 flags + 1);
         when(questionService.getById(24)).thenReturn(question);
         this.questionController = new QuestionController(questionService);
     }
 
 }
