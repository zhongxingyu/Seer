 package fi.solita.dev.goosdemo.domain;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.BDDMockito.given;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class SlowlyWarmingGreeterTest {
 
     @Mock
     private MeetingHistory meetingHistory;
 
     @InjectMocks
     private SlowlyWarmingGreeter greeter;
 
     @Test
     public void shouldGreetPeopleRudelyTheFirstTime() {
         // Given
         given(meetingHistory.timesMet("John")).willReturn(0);
 
         // When
         String greeting = greeter.greet("John");
 
         // Then
         assertEquals("What do you want? Beat it!", greeting);
     }
 
     @Test
     public void shouldGreetPeopleNeutrallyAfterTheFirstTime() {
         // Given
         given(meetingHistory.timesMet("John")).willReturn(1);
 
         // When
         String greeting = greeter.greet("John");
 
         // Then
         assertEquals("Hello, John!", greeting);
     }
 
     @Test
    public void shouldGreetPeopleHappilyAfterFiveTimes() {
         // Given
         given(meetingHistory.timesMet("John")).willReturn(5);
 
         // When
         String greeting = greeter.greet("John");
 
         // Then
         assertEquals("Hey, John! Great to see you!", greeting);
     }
 
 }
