 package com.thoughtworks.twu.controller;
 
 import com.sun.security.auth.UserPrincipal;
 import com.thoughtworks.twu.domain.Feedback;
 import com.thoughtworks.twu.service.FeedbackService;
 import org.hamcrest.CoreMatchers;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.ArrayList;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 
 public class FeedbackControllerTest {
     private FeedbackController feedbackController;
     private FeedbackService feedbackService;
     private MockHttpServletRequest request;
 
     @Before
     public void setUp() throws Exception {
         feedbackService = mock(FeedbackService.class);
         feedbackController = new FeedbackController(feedbackService);
         UserPrincipal userPrincipal = new UserPrincipal("test.twu");
         request = new MockHttpServletRequest();
         request.setUserPrincipal(userPrincipal);
     }
 
     @Test
     public void shouldLoadAddFeedbackWhenClickedATalk() {
         assertThat(feedbackController.enterFeedback(request, 0, "").getViewName(), is("add_feedback"));
     }
 
     @Test
     public void shouldEnterAFeedback() {
         int talkId = 9;
         ArrayList<Feedback> feedbackArrayList = new ArrayList<Feedback>();
         when(feedbackService.retrieveFeedbackByTalkId(talkId)).thenReturn(feedbackArrayList);
 
         ModelAndView result = feedbackController.enterFeedback(request, talkId, "Feedback comment");
 
         verify(feedbackService).enterFeedback(talkId, "Feedback comment", "test.twu", "test.twu@thoughtworks.com");
         verify(feedbackService).retrieveFeedbackByTalkId(talkId);
         assertThat((ArrayList<Feedback>) result.getModel().get("retrieved_feedback_list"), CoreMatchers.is(feedbackArrayList));
     }
 
     @Test
     public void shouldShowListOfPreviousFeedbackByTalkIdOrderedByMostRecent() throws Exception {
         //Given
         int talkId = 1;
         Feedback feedback = new Feedback(talkId, "comment1", "Vegeta", "vegeta@dragon.ball", new DateTime(2008, DateTimeZone.UTC));
         Feedback feedback2 = new Feedback(talkId, "comment2", "Vegeta", "vegeta@dragon.ball", new DateTime(2009, DateTimeZone.UTC));
         Feedback feedback3 = new Feedback(talkId, "comment3", "Vegeta", "vegeta@dragon.ball", new DateTime(2010, DateTimeZone.UTC));
         Feedback feedback4 = new Feedback(talkId, "comment4", "Vegeta", "vegeta@dragon.ball", new DateTime(2011, DateTimeZone.UTC));
         ArrayList<Feedback> feedbackArrayList = new ArrayList<Feedback>();
         feedbackArrayList.add(feedback);
         feedbackArrayList.add(feedback2);
         feedbackArrayList.add(feedback3);
         feedbackArrayList.add(feedback4);
         when(feedbackService.retrieveFeedbackByTalkId(talkId)).thenReturn(feedbackArrayList);
         //When
         ModelAndView result = feedbackController.getListOfPastFeedback(talkId);
         //Then
         assertThat(result.getViewName(), CoreMatchers.is("add_feedback"));
         assertThat((ArrayList<Feedback>) result.getModel().get("retrieved_feedback_list"), CoreMatchers.is(feedbackArrayList));
         verify(feedbackService).retrieveFeedbackByTalkId(talkId);
 
     }
 
     @Test
     public void shouldReturnListOfFeedbackForATalk() throws Exception {
         int talkId=1;
        ArrayList<Feedback> feedbackArrayList = new ArrayList<>();
         when(feedbackService.retrieveFeedbackByTalkId(talkId)).thenReturn(feedbackArrayList);
         ModelAndView result =  feedbackController.getUpdatedListOfFeedbackForTalk(talkId);
         assertThat(result.getViewName(),is("feedback_list"));
         assertThat((ArrayList<Feedback>)result.getModel().get("updated_feedback_list"),is(feedbackArrayList));
         verify(feedbackService).retrieveFeedbackByTalkId(talkId);
 
     }
 }
