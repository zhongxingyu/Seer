 package ch.bfh.bti7081.s2013.yellow.service.notification;
 
 import ch.bfh.bti7081.s2013.yellow.model.notification.Notification;
 import ch.bfh.bti7081.s2013.yellow.service.generic.GenericService;
 
 import java.util.List;
 
 /**
  * Interface for the NotificationService
  * @author fabianhutzli
  *
  */
 public interface NotificationService extends GenericService<Notification> {
 
     /**
      * find new notifications to send
      * @param 
      */
 	List<Notification> findNewNotificationsToSend();
     
 	
     /**
      * sends the Notification
      * @param notification
      */
     void send(Notification notification);
 
     
     /**
      * All notifications which wasn't confirmed by the patient must be resend after timePassed
      * @param 
      */
     void sendNotifications();
     
     /**
      * Notifications within time Range will be sent to receiver
     * @param timerange in seconds
      */
     void resendNotifications(Integer timePassed);
 
     /**
      * Find affected Notifications
      * @return all affected Notifications
      */
     List<Notification> findSentNotificationsToResend(Integer timePassed);
 
     /**
      * searches the Notification with the given uuid and confirms the intake of it
      * @param uuid
      */
     void confirmIntake(String uuid);
 }
 
 
