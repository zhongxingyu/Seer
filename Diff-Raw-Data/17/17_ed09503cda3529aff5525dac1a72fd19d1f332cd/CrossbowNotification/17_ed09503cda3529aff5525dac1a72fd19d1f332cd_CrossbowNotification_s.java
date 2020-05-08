 package org.jims.modules.crossbow.infrastructure.progress;
 
import java.util.List;
 
 import javax.management.ListenerNotFoundException;
 import javax.management.MBeanNotificationInfo;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.management.Notification;
 import javax.management.NotificationFilter;
 import javax.management.NotificationListener;
 
 import org.jims.modules.crossbow.infrastructure.progress.notification.ProgressNotification;
 import org.jims.modules.crossbow.infrastructure.progress.notification.TaskCompletedNotification;
 import org.jims.modules.crossbow.infrastructure.JimsMBeanServer;
 
 import org.apache.log4j.Logger;
 
 
 public class CrossbowNotification implements CrossbowNotificationMBean {
 
 	private int totalTasks;
 
 	private int index;
 
 	private int sequenceNumber = 0;
 
 	private Logger log = Logger.getLogger( CrossbowNotification.class );
 
 	public CrossbowNotification(int totalTasks) {
 		this.index = 0;
 		this.totalTasks = totalTasks;
 
 		registerNotificationListener();
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4845007778991652486L;
 
	private List<NotificationListener> listeners;
 
 	//@todo dopisac rejestrowanie we wszystkich WorkerProgressMBEan'ach - narazie jest tylko jeden
 	private void registerNotificationListener() {
 
 		log.debug("Registering worker progress listener");
 
 		MBeanServer server = JimsMBeanServer.findJimsMBeanServer();
 		if(server != null) {
 			try{
 				server.addNotificationListener(new ObjectName( "Crossbow:type=WorkerProgress" ), this,
 					null, null);
 				log.debug("Worker progress listener successfully registered");
 			}catch(Exception e) {
 				log.error("Couldn't register WorkerProgress Listener");
 			}
 
 		}
 
 	}
 
 	@Override
 	public void addNotificationListener(NotificationListener listener,
 			NotificationFilter arg1, Object arg2)
 			throws IllegalArgumentException {
 		listeners.add(listener);
 	}
 
 	@Override
 	public MBeanNotificationInfo[] getNotificationInfo() {
 		MBeanNotificationInfo info = new MBeanNotificationInfo(null,
 				"Notification about deployment process", null);
 		return new MBeanNotificationInfo[] { info };
 	}
 
 	@Override
 	public void removeNotificationListener(NotificationListener listener)
 			throws ListenerNotFoundException {
 		listeners.remove(listener);
 
 	}
 
 	/**
 	 * Jesli otrzymana notyfikacja jest o stanie procesu deploymentu to
 	 * aktualizuje licznik i przekazuje notyfikacje do wszystkich listenerow
 	 * postepu procesu deploymentu
 	 */
 	@Override
 	public void handleNotification(Notification notification, Object handback) {
 
 		Object userData = notification.getUserData();
 
 		if (notification.getUserData() != null
 				&& notification.getUserData() instanceof TaskCompletedNotification) {
 			userData = new ProgressNotification(++index, totalTasks,
 					((TaskCompletedNotification) notification.getUserData())
 							.getNodeIpAddress());
 		}
 
 		Notification notification2 = new Notification("ProgressNotification",
 				"Supervisor", ++sequenceNumber);
 		notification2.setUserData(userData);
 
 		for (NotificationListener notificationListener : listeners) {
 			notificationListener.handleNotification(notification2, null);
 		}
 	}
 }
