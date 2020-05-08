 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Feedback;
 import com.thoughtworks.twu.persistence.FeedbackMapper;
 import com.thoughtworks.twu.utils.TestClock;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 public class FeedbackServiceTest {
     @Test
     public void shouldInsertFeedback() {
         //Given
        FeedbackMapper mockFeedback = mock(FeedbackMapper.class);
         TestClock testClock = new TestClock();
        FeedbackService feedbackService = new FeedbackService(mockFeedback, testClock);
        Feedback feedback = new Feedback();
 
         //When
        feedbackService.enterFeedback(feedback);
 
         //Then
        verify(mockFeedback).insertFeedback(feedback);
     }
 
     @Test
     public void shouldRetrieveFeedbackListByTalkId() {
         //Given
         FeedbackMapper mockFeedbackMapper = mock(FeedbackMapper.class);
         TestClock testClock = new TestClock();
         FeedbackService feedbackService = new FeedbackService(mockFeedbackMapper, testClock);
         int talk_id = 1;
         Feedback feedback1 = new Feedback(talk_id, "feedbackComment1", "attendee1", "attendeeMail 1",new DateTime(DateTimeZone.UTC));
         Feedback feedback2 = new Feedback(talk_id, "feedbackComment2", "attendee2", "attendeeMail 2",new DateTime(DateTimeZone.UTC));
         Feedback feedback3 = new Feedback(talk_id, "feedbackComment3", "attendee3", "attendeeMail 3",new DateTime(DateTimeZone.UTC));
         Feedback feedback = new Feedback(2, "feedbackComment1", "attendee1", "attendeeMail 1",new DateTime(DateTimeZone.UTC));
        feedbackService.enterFeedback(feedback1);
        feedbackService.enterFeedback(feedback2);
        feedbackService.enterFeedback(feedback3);
        feedbackService.enterFeedback(feedback);
         ArrayList<Feedback> feedbackList = new ArrayList<Feedback>();
         feedbackList.add(feedback3);
         feedbackList.add(feedback2);
         feedbackList.add(feedback1);
         //When
         List<Feedback> result = feedbackService.retrieveFeedbackByTalkId(talk_id);
         //Then
         verify(mockFeedbackMapper).getFeedbackByTalkId(talk_id);
 
     }
 }
