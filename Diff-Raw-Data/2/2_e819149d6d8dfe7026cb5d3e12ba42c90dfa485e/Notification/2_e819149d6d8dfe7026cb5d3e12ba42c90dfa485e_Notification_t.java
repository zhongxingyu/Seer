 package db;
 
 public class Notification {
 	
 	private int userId;
 	private int appointmentId;
 	private NotificationType notificationType;
 	private String message;
 	
 
	public Notification(int userId, int appointmentId, NotificationType notificationType){
 		this.userId = userId;
 		this.appointmentId = appointmentId;
 		this.notificationType = notificationType;	
 	}
 	
 	public String getMeassage() {
 		return message;
 	}
 
 	public void setMeassage(String meassage) {
 		this.message = meassage;
 	}
 
 	public int getUserId() {
 		return userId;
 	}
 
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 
 	public int getAppointmentId() {
 		return appointmentId;
 	}
 
 	public void setAppointmentId(int appointmentId) {
 		this.appointmentId = appointmentId;
 	}
 
 	public NotificationType getNotificationType() {
 		return notificationType;
 	}
 
 	public void setNotificationType(NotificationType notificationType) {
 		this.notificationType = notificationType;
 	}
 	@Override
 	public String toString(){
 		return message;
 	}
 	
 }
