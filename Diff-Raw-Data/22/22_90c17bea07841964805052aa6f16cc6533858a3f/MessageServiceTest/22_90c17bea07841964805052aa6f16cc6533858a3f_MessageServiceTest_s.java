 package be.kdg.groepi.service;
 
 import be.kdg.groepi.model.*;
 import be.kdg.groepi.utils.DateUtil;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static be.kdg.groepi.utils.DateUtil.dateToLong;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tim
  * Date: 1/03/13
  * Time: 11:57
  * To change this template use File | Settings | File Templates.
  */
 public class MessageServiceTest {
     User user;
     Trip trip;
     //    Stop stop;
 //    Answer answer;
 //    StopInstance stopInstance;
     TripInstance tripInstance;
     //    AnswerInstance answerInstance;
     Message message;
 
     @Before
     public void beforeEachTest() {
         user = new User("Tim", "tim@junittest.com", "tim", dateToLong(4, 5, 2011, 15, 32, 0));
         UserService.createUser(user);
         trip = new Trip("Onze eerste trip", "Hopelijk is deze niet te saai!", true, true, user);// trip aanmaken
         TripService.createTrip(trip);
 
         tripInstance = new TripInstance("Tripje 1", "Eerste uitvoering van de 'Onze eerste trip'-trip", true,
                 DateUtil.dateToLong(2, 3, 2013, 12, 0, 0), DateUtil.dateToLong(2, 3, 2013, 16, 0, 0), user, trip);
         TripInstanceService.createTripInstance(tripInstance);
 
 
         message = new Message("Voorbeeldbericht over voorbeelden en berichten waarin deze kunnen zitten",
                 DateUtil.dateToLong(2, 3, 2013, 17, 16, 0), tripInstance, user);
         MessageService.createMessage(message);
     }
 
     @After
     public void afterEachTest() {
 
     }
 
     @Test
     public void createMessage() {
         assertTrue("createMessage: message was not created",
                 message.equals(MessageService.getMessageById(message.getId())));
     }
 
     @Test
     public void updateMessage() {
         Message originalMessage = new Message(message.getContent(), message.getDate(), message.getTripInstance(), message.getUser());
 
         message.setContent("Ofnee, we gaan het hebben over eenhoorns");
         message.setDate(DateUtil.dateToLong(2, 3, 2013, 17, 19, 0));
 
         MessageService.createMessage(message);
 
         assertFalse("updateMessage: message was not updated", originalMessage.equals(MessageService.getMessageById(message.getId())));
     }
 
     @Test
     public void deleteCosts() {
         while (!MessageService.getAllMessages().isEmpty()) {
             Message firstMessage = MessageService.getAllMessages().get(0);
             MessageService.deleteMessage(firstMessage);
         }
        assertTrue(CostService.getAllCosts().isEmpty());
     }
 
 }
