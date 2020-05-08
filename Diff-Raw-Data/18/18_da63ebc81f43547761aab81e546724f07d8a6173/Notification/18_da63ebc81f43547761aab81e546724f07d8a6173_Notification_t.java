 package ch.bfh.bti7081.s2013.yellow.model.notification;
 
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.validation.constraints.NotNull;
 
 import ch.bfh.bti7081.s2013.yellow.model.generic.YellowEntity;
 import ch.bfh.bti7081.s2013.yellow.model.person.User;
 import ch.bfh.bti7081.s2013.yellow.util.stateMachine.NotificationState;
 import ch.bfh.bti7081.s2013.yellow.util.stateMachine.NotificationStateNew;
 
 /**
  * This class represents a notification
  */
 @Entity
 public class Notification extends YellowEntity<Notification> {
 
     public NotificationType getNotificationType;
     @ManyToOne(optional = false)
 	@NotNull
     private User receiver;
     private String message;
    
    //private NotificationState state;
     //private NotificationStateMachine stateMachine;
 
     public Notification(User receiver, String message) {
         super(Notification.class);
     	this.receiver = receiver;
         this.message = message;
        //this.state = new NotificationStateNew();
     }
     
     public void send()
     {
    	//state = state.send();
     }
     
     
 
 
     public NotificationType getGetNotificationType() {
         return getNotificationType;
     }
 
     public void setGetNotificationType(NotificationType getNotificationType) {
         this.getNotificationType = getNotificationType;
     }
 
     public User getReceiver() {
         return receiver;
     }
 
     public void setReceiver(User receiver) {
         this.receiver = receiver;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 }
