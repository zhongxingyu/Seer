 package smartpool.domain;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class CarpoolTest {
 
     private Carpool carpool;
 
     @Before
     public void setUp() throws Exception {
         carpool = new Carpool("name");
     }
 
     @Test
     public void shouldStartItIfInNotStartedStateAndHaveMoreThanOneBuddy() throws Exception {
         List<CarpoolBuddy> carpoolBuddies = Arrays.asList(new CarpoolBuddy(), new CarpoolBuddy());
         carpool = new Carpool("can start", null, null, 0, null, null, Status.NOT_STARTED, carpoolBuddies, 4, null);
 
         assertThat(carpool.canStart(), is(true));
     }
 
     @Test
     public void shouldNotStartItIfHaveOnlyOneBuddy() throws Exception {
         List<CarpoolBuddy> carpoolBuddies = Arrays.asList(new CarpoolBuddy());
         carpool = new Carpool("can start", null, null, 0, null, null, Status.NOT_STARTED, carpoolBuddies, 4, null);
 
         assertThat(carpool.canStart(), is(false));
     }
 
     @Test
     public void shouldNotStartItIfInActiveState() throws Exception {
         List<CarpoolBuddy> carpoolBuddies = Arrays.asList(new CarpoolBuddy(), new CarpoolBuddy());
         carpool = new Carpool("can start", null, null, 0, null, null, Status.ACTIVE, carpoolBuddies, 4, null);
 
         assertThat(carpool.canStart(), is(false));
     }
 
     @Test
     public void shouldReturnStartItIfRequestNotSent() throws Exception {
         carpool.setRequestSent(false);
 
         assertThat(carpool.getStartLinkText(), is("Start It"));
     }
 
     @Test
     public void shouldReturnRequestSentIfRequestIsSent() throws Exception {
         carpool.setRequestSent(true);
 
        assertThat(carpool.getStartLinkText(), is("Request Sent"));
     }
 
     @Test
     public void shouldReturnSendRequestLinkIfRequestNotSent() throws Exception {
         carpool.setRequestSent(false);
 
         assertThat(carpool.getStartLink(), is("/carpool/"+ carpool.getName() +"/start"));
     }
 
     @Test
     public void shouldReturnEmptyLinkIfRequestSent() throws Exception {
         carpool.setRequestSent(true);
 
         assertThat(carpool.getStartLink(), is(""));
     }
 }
