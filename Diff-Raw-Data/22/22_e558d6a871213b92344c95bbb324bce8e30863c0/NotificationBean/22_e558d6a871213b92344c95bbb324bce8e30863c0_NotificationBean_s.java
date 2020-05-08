 package server;
 
 import java.sql.Timestamp;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import data.Notification;
 import data.User;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import ctrl.DBField;
 import ctrl.DBNotification;
 import ctrl.DBUser;
 
 @ManagedBean(name = "NotificationBean")
 @RequestScoped
 public class NotificationBean {
 
 	private String recipientEmail;
 	private String senderEmail;
 	private Timestamp timeStamp;
 	private String message;
 	private String action;
 	private String status;
 	private List<Notification> notificationList;
 	private Notification selectedNotification;
 	private Notification selectedMessage;
 	private String strTimeStamp;
 
 	@PostConstruct
 	void init() {
 		notificationList = new LinkedList<Notification>();
 		notificationList = DBNotification.loadNotification();
 	}
 	
 	/**
 	 * decline selected notification
 	 */
 	public void declineSelectedNotification(ActionEvent e) {
 		System.out.println("decline");
 		
 		if (selectedNotification != null) {
			if(DBNotification.declineNotification(getSelectedNotification()))
 				System.out.println(selectedNotification.getMessage() + " was declined");
			else System.out.println("nothing to decline");
 		} else
 			System.out.println("null");
 		loadNotifications();
 	}
 
 	/**
 	 * accepts selected notification
 	 */
 	public void acceptSelectedNotification(ActionEvent e) {
 		System.out.println("accept");
 		
 		if (selectedNotification != null) {
			if(DBNotification.acceptNotification(getSelectedNotification()))
 				System.out.println(selectedNotification.getMessage() + " was accepted");
			else System.out.println("nothing to accept");
 		} else
 			System.out.println("null");
 		loadNotifications();
 	}
 	// Currently unused method
 	// START********************************************************************
 	/**
 	 * loads the selected notification
 	 * 
 	 */
 	public void loadSelectedNotification(ActionEvent e) {
 		System.out.println("selected");
 
 		if (selectedNotification != null) {
 			System.out.println(selectedNotification.getMessage());
 			selectedMessage = DBNotification.loadNotification(
 					selectedNotification.getRecipientEmail(),
 					selectedNotification.getSenderEmail(),
 					selectedNotification.getTimeStamp());
 		} else
 			System.out.println("null");
 	}
 
 	/**
 	 * loads the selected notification
 	 */
 	public void selectNotification() {
 		FacesContext fc = FacesContext.getCurrentInstance();
 		this.recipientEmail = fc.getExternalContext().getRequestParameterMap()
 				.get("recipientEmail");
 		this.senderEmail = fc.getExternalContext().getRequestParameterMap()
 				.get("senderEmail");
 		this.strTimeStamp = fc.getExternalContext().getRequestParameterMap()
 				.get("timeStamp");
 		Timestamp ts = Timestamp.valueOf(strTimeStamp);
 		System.out.println("notification from: " + senderEmail + " to "
 				+ recipientEmail + " at: " + ts);
 		setSelectedNotification(DBNotification.loadNotification(recipientEmail,
 				senderEmail, ts));
 	}
 
 	// Currently unused method
 	// END********************************************************************
 	/**
 	 * Loads all Notifications from the database
 	 * 
 	 */
 	public void loadNotifications() {
 		setNotificationList(DBNotification.loadNotification());
 	}
 
 	/**
 	 * @return the recipientEmail
 	 */
 	public String getRecipientEmail() {
 		return recipientEmail;
 	}
 
 	/**
 	 * @param recipientEmail
 	 *            the recipientEmail to set
 	 */
 	public void setRecipientEmail(String recipientEmail) {
 		this.recipientEmail = recipientEmail;
 	}
 
 	/**
 	 * @return the senderEmail
 	 */
 	public String getSenderEmail() {
 		return senderEmail;
 	}
 
 	/**
 	 * @param senderEmail
 	 *            the senderEmail to set
 	 */
 	public void setSenderEmail(String senderEmail) {
 		this.senderEmail = senderEmail;
 	}
 
 	/**
 	 * @return the timeStamp
 	 */
 	public Timestamp getTimeStamp() {
 		return timeStamp;
 	}
 
 	/**
 	 * @param timeStamp
 	 *            the timeStamp to set
 	 */
 	public void setTimeStamp(Timestamp timeStamp) {
 		this.timeStamp = timeStamp;
 	}
 
 	/**
 	 * @return the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @param message
 	 *            the message to set
 	 */
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	/**
 	 * @return the action
 	 */
 	public String getAction() {
 		return action;
 	}
 
 	/**
 	 * @param action
 	 *            the action to set
 	 */
 	public void setAction(String action) {
 		this.action = action;
 	}
 
 	/**
 	 * @return the status
 	 */
 	public String getStatus() {
 		return status;
 	}
 
 	/**
 	 * @param status
 	 *            the status to set
 	 */
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	/**
 	 * @return the notificationList
 	 */
 	public List<Notification> getNotificationList() {
 		return notificationList;
 	}
 
 	/**
 	 * @param notificationList
 	 *            the notificationList to set
 	 */
 	public void setNotificationList(List<Notification> notificationList) {
 		this.notificationList = notificationList;
 	}
 
 	/**
 	 * @return the selectedNotification
 	 */
 	public Notification getSelectedNotification() {
 		return selectedNotification;
 	}
 
 	/**
 	 * @param selectedNotification
 	 *            the selectedNotification to set
 	 */
 	public void setSelectedNotification(Notification selectedNotification) {
 		this.selectedNotification = selectedNotification;
 	}
 
 	/**
 	 * @return the selectedMessage
 	 */
 	public Notification getSelectedMessage() {
 		return selectedMessage;
 	}
 
 	/**
 	 * @param selectedMessage the selectedMessage to set
 	 */
 	public void setSelectedMessage(Notification selectedMessage) {
 		this.selectedMessage = selectedMessage;
 	}
 
 	/**
 	 * @return the strTimeStamp
 	 */
 	public String getStrTimeStamp() {
 		return strTimeStamp;
 	}
 
 	/**
 	 * @param strTimeStamp the strTimeStamp to set
 	 */
 	public void setStrTimeStamp(String strTimeStamp) {
 		this.strTimeStamp = strTimeStamp;
 	}
 	
 
 }
