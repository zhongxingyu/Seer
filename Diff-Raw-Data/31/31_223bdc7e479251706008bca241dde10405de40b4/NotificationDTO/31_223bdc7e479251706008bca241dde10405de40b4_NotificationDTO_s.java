 package at.ac.tuwien.dse.fairsurgeries.dto;
 
 import java.io.Serializable;
 
 public class NotificationDTO implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private int notificationId;
 	
 	public NotificationDTO(int notificationId){
 		this.setNotificationId(notificationId);
 	}
 
	private int getNotificationId() {
 		return notificationId;
 	}
 
	private void setNotificationId(int notificationId) {
 		this.notificationId = notificationId;
 	}
 
 }
