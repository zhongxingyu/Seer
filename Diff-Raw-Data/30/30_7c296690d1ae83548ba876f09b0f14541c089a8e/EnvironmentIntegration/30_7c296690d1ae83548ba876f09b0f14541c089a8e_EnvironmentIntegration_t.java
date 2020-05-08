 package net.mms_projects.copy_it;
 
 import java.util.ArrayList;
 import java.util.List;
 
 abstract public class EnvironmentIntegration {
 
 	private NotificationManager notificationManager;
 	private AutostartManager autostartManager;
 	private List<EnvironmentIntegration> integrationProviders = new ArrayList<EnvironmentIntegration>();
 
 	/**
 	 * This will set the current notification manager to the one provided
 	 * 
 	 * @param notificationManager
 	 *            The notification manager to set
 	 */
 	public void setNotificationManager(NotificationManager notificationManager) {
 		this.notificationManager = notificationManager;
 	}
 
 	/**
 	 * This returns the current notification manager
 	 * 
 	 * @return The current notification manager
 	 */
 	public NotificationManager getNotificationManager() {
 		return this.notificationManager;
 	}
 
 	/**
 	 * This method sets the auto start manager that should be used for this
 	 * environment
 	 * 
 	 * @param autostartManager
 	 *            The auto start manager to use
 	 */
 	public void setAutostartManager(AutostartManager autostartManager) {
 		this.autostartManager = autostartManager;
 	}
 
 	/**
 	 * This method checks if there is a auto start manager available
 	 * 
 	 * @return true if there is a auto start manager available
 	 */
 	public boolean hasAutostartManager() {
 		return this.autostartManager != null;
 	}
 
 	/**
 	 * This method returns the auto start manager used in this environment
 	 * 
 	 * @return The actual auto start manager
 	 */
 	public AutostartManager getAutostartManager() {
 		return this.autostartManager;
 	}
 
 	/**
 	 * Adds some sub integration that will get setup after the parent has been
 	 * setup.
 	 * 
 	 * @param integrationProvider
 	 *            The actual integration provider to add
 	 */
 	public void addIntegration(EnvironmentIntegration integrationProvider) {
 		this.integrationProviders.add(integrationProvider);
 	}
 
 	/**
 	 * This method initializes the setup of the integration.
 	 */
 	final public void setup() {
 		this.standaloneSetup();
 
 		for (EnvironmentIntegration integrationProvider : this.integrationProviders) {
 			integrationProvider.setup();
 		}
 	}
 
 	/**
 	 * This method handles the internal setup of the integration. Do not execute
 	 * this directly use {@link EnvironmentIntegration#setup
 	 * EnvironmentIntegration.setup} instead!
 	 */
 	abstract public void standaloneSetup();
 
 	/**
 	 * A interface that describes a basic notification manager with one method
 	 * to send a notification.
 	 */
 	static public interface NotificationManager {
 		enum NotificationUrgency {
 			LOW, NORMAL, CRITICAL
 		}
 
 		/**
 		 * This sends a notification to the user
 		 * 
 		 * @param id
 		 *            The optional notification ID that this notification
 		 *            replaces. The server must atomically (ie with no flicker
 		 *            or other visual cues) replace the given notification with
 		 *            this one. This allows clients to effectively modify the
 		 *            notification while it's active. A value of value of 0
 		 *            means that this notification won't replace any existing
 		 *            notifications.
 		 * @param urgency
 		 *            The urgency the notification should have.
 		 * @param icon
 		 *            The optional program icon of the calling application. See
 		 *            Icons and Images. Can be an empty string, indicating no
 		 *            icon.
 		 * @param summary
 		 *            The summary text briefly describing the notification.
 		 * @param body
 		 *            The optional detailed body text. Can be empty.
 		 */
 		public void notify(int id, NotificationUrgency urgency, String icon,
 				String summary, String body);
 
 	}
 
 	/**
 	 * A interface that describes a basic auto start manager
 	 */
 	static public interface AutostartManager {
		public static class AutoStartSetupException extends Exception {

			private static final long serialVersionUID = -2363782709319312249L;

			public AutoStartSetupException() {
				super();
			}

			public AutoStartSetupException(String message) {
				super(message);
			}

			public AutoStartSetupException(String message, Throwable cause) {
				super(message, cause);
			}

			public AutoStartSetupException(Throwable cause) {
				super(cause);
			}

		}

 		/**
 		 * This method sets up the auto start the way it should in the
 		 * environment it belongs to.
		 * 
		 * @throws AutoStartSetupException
		 *             This gets thrown when something bad happens while setting
		 *             up the auto start
 		 */
		public void setupAutostartup() throws AutoStartSetupException;
 	}
 
 }
