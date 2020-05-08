 package de.flower.rmt.service.mail;
 
 import de.flower.rmt.model.User;
 import de.flower.rmt.model.event.Event;
 import de.flower.rmt.model.event.EventType;
 import de.flower.rmt.model.event.Match;
 import de.flower.rmt.model.type.Notification;
import de.flower.rmt.test.AbstractIntegrationTests;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.*;
 
 /**
  * @author flowerrrr
  */
public class NotificationServiceTest extends AbstractIntegrationTests {
 
     @Test
     public void testSendResetPasswordMail() {
         User user = testData.createUser();
         notificationService.sendResetPasswordMail(user);
     }
 
     @Test
     public void testSendInvitationNewUserMail() {
         User user = testData.createUser();
         notificationService.sendInvitationNewUser(user, securityService.getUser());
     }
 
     @Test
     public void testNewEventNotification() {
         Event event = testData.createEvent();
         // load from db to get version with uninitialized associations
         event = eventManager.loadById(event.getId());
         String eventLink = "http://flower.de/event/4";
         Notification notification = notificationService.newEventNotification(event, eventLink);
         log.info(notification.getSubject());
         log.info(notification.getBody());
         assertFalse(notification.getBody().contains("Gegner"));
 
         // no venue selected yet
         testData.setEventType(EventType.Match);
         Match match = (Match) testData.createEvent();
         match.setVenue(null);
         match.getSurfaceList().clear();
         eventManager.save(match);
         notification = notificationService.newEventNotification(match, eventLink);
         log.info(notification.getSubject());
         log.info(notification.getBody());
         assertTrue(notification.getBody().contains("Ort: <noch nicht festgelegt>"));
         assertTrue(notification.getBody().contains("Untergrund: <unbekannt>"));
         assertTrue(notification.getBody().contains("Gegner"));
     }
 }
