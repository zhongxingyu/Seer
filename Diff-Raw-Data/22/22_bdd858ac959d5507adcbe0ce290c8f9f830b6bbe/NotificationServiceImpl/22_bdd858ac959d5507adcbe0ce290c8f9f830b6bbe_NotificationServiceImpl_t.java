 package ch.bfh.bti7081.s2013.yellow.service.notification;
 
 import ch.bfh.bti7081.s2013.yellow.dao.notification.NotificationDAO;
 import ch.bfh.bti7081.s2013.yellow.model.notification.Notification;
 import ch.bfh.bti7081.s2013.yellow.model.notification.NotificationType;
 import ch.bfh.bti7081.s2013.yellow.service.generic.GenericServiceImpl;
 import ch.bfh.bti7081.s2013.yellow.service.mail.MailService;
 import ch.bfh.bti7081.s2013.yellow.service.notification.strategy.NotificationContext;
 import ch.bfh.bti7081.s2013.yellow.service.notification.strategy.SendAlarmNotifaction;
 import ch.bfh.bti7081.s2013.yellow.service.notification.strategy.SendReminderNotification;
 import ch.bfh.bti7081.s2013.yellow.ui.medication.IntakeConfirmView;
 import ch.bfh.bti7081.s2013.yellow.util.stateMachine.NotificationState;
 import ch.bfh.bti7081.s2013.yellow.util.stateMachine.NotificationStateMachine;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.PostConstruct;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Transactional
 @Service("notificationService")
 public class NotificationServiceImpl extends GenericServiceImpl<Notification> implements NotificationService {
 
 	@Autowired
 	NotificationDAO notificationDAO;
 	@Autowired
 	private SendAlarmNotifaction sendAlarmNotifaction;
 	@Autowired
 	private SendReminderNotification sendReminderNotification;
 	@Autowired
 	NotificationStateMachine notificationStateMachine;
 
 	@PostConstruct
 	public void init() {
 		setDAO(notificationDAO);
 	}
 
 	public static final int TIME_PASSED_DEFAULT = 1800;
 
 	@Override
 	public void send(Notification notification) {
 		NotificationContext context = new NotificationContext();
 		switch (notification.getNotificationType()) {
 			case REMINDER:
 				context.setSendStrategy(sendReminderNotification);
 				break;
 			case ALARM:
 				context.setSendStrategy(sendAlarmNotifaction);
 				break;
 			default:
 				context.setSendStrategy(sendReminderNotification);
 				break;
 		}
 		context.send(notification);
 	}
 
 	@Override
 	public void sendNotifications() {
 		for (Notification n : findNewNotificationsToSend()) {
 			send(n);
			setNotificationState(n, NotificationState.SENT);
			save(n);
 		}
 	}
 
 	@Override
 	public void resendNotifications(Integer timePassed) {
 
 		//If timePassed not set, set it to Default Value
 		if (timePassed == null)
 			timePassed = TIME_PASSED_DEFAULT;
 		//Get every unconfirmed Notifications greater than a specific time passed
 		List<Notification> unconfirmedNotifications = findSentNotificationsToResend(timePassed);
 
 		//Set State of those unconfirmed Notifications to deleted
 		for (Notification n : unconfirmedNotifications) {
 			Notification newNotification = new Notification(n.getReceiver(), n.getMessage(), n.getSendDate());
 			newNotification.setParentNotification(n);
			setNotificationState(newNotification, NotificationState.NEW);
 			newNotification.setNotificationType(NotificationType.REMINDER);
 			save(newNotification);
 
 			setNotificationState(n, NotificationState.DELETED);
 			save(n);
 
 			send(newNotification);
 		}
 	}
 
 
 	//Get a list of unconfirmed Notification with State SENT and older than the current time - timePassed (Default 1800s)
 	public List<Notification> findSentNotificationsToResend(Integer timePassed) {
 		//Current Time - timepassed
 		Date cmpDate = new Date();
 		cmpDate.setTime(cmpDate.getTime() - timePassed * 1000);
 		//Return list of Notifications
 		return notificationDAO.findByCriteria(Restrictions.and(
 				Restrictions.eq("state", NotificationState.SENT), Restrictions.lt("sendDate", cmpDate)));
 	}
 
 	@Override
 	public boolean confirmIntake(String uuid) {
 		try {
 			if (isValidUUID(uuid)) {
 				Notification n = findByCriteria(Restrictions.eq("uuid", uuid)).get(0);
 				setNotificationState(n, NotificationState.CONFIRMED);
 				save(n);
 				return true;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 
 
 	private boolean isValidUUID(String param) {
 		if (param == null)
 			return false;
 		int uuidLength = 36;
 		return uuidLength == param.length();
 	}
 
 	@Override
 	public List<Notification> findNewNotificationsToSend() {
 		//CurrentTime
 		Date cmpDate = new Date();
 		//Return list of Notifications
 		return notificationDAO.findByCriteria(Restrictions.and(
 				Restrictions.eq("state", NotificationState.NEW), Restrictions.lt("sendDate", cmpDate)));
 	}
 
 	@Override
 	public String getIntakeConfirmationLink(Notification notification) {
		return "http://147.87.46.38:8080/#!" + IntakeConfirmView.NAME + "/" + notification.getUuid();
 	}
 
 	@Override
 	public boolean setNotificationState(Notification notification, NotificationState notificationState) {
 		if (notificationStateMachine.validChangeOver(notification.getState(), notificationState)) {
 			notification.setState(notificationState);
 			return true;
 		}
 		return false;
 	}
 }
