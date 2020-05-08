 package smartpool.service;
 
 import org.joda.time.LocalTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import smartpool.builder.BuddyBuilder;
 import smartpool.domain.Buddy;
 import smartpool.domain.CarpoolBuddy;
 import smartpool.domain.JoinRequest;
 import smartpool.persistence.dao.CarpoolBuddyDao;
 import smartpool.persistence.dao.JoinRequestDao;
 
 import java.util.ArrayList;
 
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class JoinRequestServiceTest {
 
     private JoinRequestService joinRequestService;
     @Mock
     private JoinRequestDao joinRequestDao;
     @Mock
     private CarpoolBuddyDao carpoolBuddyDao;
     @Mock
     private MailService mailService;
 
     private Buddy buddy;
     private String carpoolName;
 
     @Before
     public void setUp() {
         initMocks(this);
         buddy = BuddyBuilder.buddy_1;
         carpoolName = "carpool-1";
         joinRequestService = new JoinRequestService(joinRequestDao, carpoolBuddyDao, mailService);
     }
 
     @Test
     public void buddyRequestAlreadySentToJoinACarpool() {
         when(joinRequestDao.isRequestSent(buddy, carpoolName)).thenReturn(true);
 
         boolean actualResult = joinRequestService.isRequestSent(buddy, carpoolName);
         assertTrue(actualResult);
     }
 
 
     @Test
     public void shouldGetEmailsOfBuddies() throws Exception {
         when(carpoolBuddyDao.getCarpoolBuddiesByCarpoolName("carpool-2")).thenReturn(new ArrayList<CarpoolBuddy>());
        joinRequestService.getCarpoolBuddies("carpool-2");
         verify(carpoolBuddyDao).getCarpoolBuddiesByCarpoolName("carpool-2");
     }
 
     @Test
     public void shouldSendEmailToList() throws Exception {
         carpoolName = "carpool-2";
        JoinRequest joinRequest = new JoinRequest("suganthk", carpoolName, "address", "pickupPoint", new LocalTime(10, 0));
         when(carpoolBuddyDao.getCarpoolBuddiesByCarpoolName(carpoolName)).thenReturn(new ArrayList<CarpoolBuddy>());
         ArrayList<String> buddyEmailList = joinRequestService.getCarpoolBuddies(carpoolName);
         String subject = "New Buddy Request To Join Your Carpool";
         String message = "Hi\nA new buddy wants to join your carpool. Approve/Reject his request\n" +
                 "Below is the buddy details:\n" +
                 "Buddy Name  :" + buddy.getName() +
                 joinRequest;
 
         joinRequestService.sendEmailToList(joinRequest, buddy);
         verify(mailService).sendMailToList(buddyEmailList, subject, message);
     }
 }
