 /*
  * Copyright 2007 Sebastien Brunot (sbrunot@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.sourceforge.buildmonitor;
 
 import java.awt.CheckboxMenuItem;
 import java.awt.Desktop;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Menu;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.TrayIcon.MessageType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.net.URI;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import net.sourceforge.buildmonitor.monitors.Monitor;
 
 import org.joda.time.LocalDateTime;
 import org.joda.time.Period;
 
 /**
  * The main class of the application.
  * @author sbrunot
  *
  */
 public class BuildMonitorImpl implements Runnable, BuildMonitor
 {
 	//////////////////////////////
 	// Constants
 	//////////////////////////////
 	
 	private static final String MESSAGES_BASE_NAME = "messages/GUIStrings";
 
 	private static final String IMAGE_MONITORING_EXCEPTION = "images/network-offline.png";
 
 	private static final String IMAGE_INITIAL_ICON = "images/utilities-system-monitor.png";
 
 	private static final String IMAGE_ABOUT_ICON = "images/about.png";
 
 	private static final String IMAGE_BUILD_SUCCESS = "images/green_up.png";
 
 	private static final String IMAGE_BUILD_FAILURE = "images/red_down.png";
 
 	// TODO: use relative font if possible
 	private static final Font SUCCESSFULL_BUILD_MENUITEM_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
 
 	// TODO: use relative font if possible
 	private static final Font FAILED_BUILD_MENUITEM_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
 	
 	private static final int TOOLTIP_MAX_LENGTH = 127;
 
 	private static final String OPTIONS_RELATED_MESSAGES_SUFFIX = " Double click here to edit Options.";
 	
 	private static final String TRUNCATED_MESSAGE_SUFFIX = " [...]";
 
 	private static final int SORT_BY_NAME = 1;
 	
 	private static final int SORT_BY_AGE = 2;
 	
 	///////////////////////////////////
 	// Nested classes
 	///////////////////////////////////
 
 	/**
 	 * An ActionListener that opens an URI in a browser when the action is performed
 	 */
 	private class OpenURIInBrowserActionListener implements ActionListener
 	{
 		////////////////////////////
 		// Instance attributes
 		////////////////////////////
 
 		private URI uri = null;
 		
 		////////////////////////////
 		// Constructor
 		////////////////////////////
 
 		/**
 		 * Create a new instance of the ActionListener.
 		 * @param theURI the URI to open in a web browser when the action is performed
 		 */
 		public OpenURIInBrowserActionListener(URI theURI)
 		{
 			this.uri = theURI;
 		}
 		
 		////////////////////////////
 		// ActionListener implementation
 		////////////////////////////
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		public void actionPerformed(ActionEvent e)
 		{
 			if (Desktop.isDesktopSupported())
 			{
 				try
 				{
 					Desktop.getDesktop().browse(this.uri);
 				}
 				catch (IOException err)
 				{
 					// Nothing can be done here...
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * The thread to be registered as a shutdown hook for the application
 	 */
 	private class ShutdownThread extends Thread
 	{
 		public void run()
 		{
 			// Stop the monitoring thread if necessary
 			if (monitor != null)
 			{
 				monitor.stop();
 			}
 		}
 	}
 
 	/**
 	 * A Runnable that can be launched with {@link javax.swing.SwingUtilities#invokeLater(Runnable)}
 	 * to update the tray icon of the application.
 	 * 
 	 * @author sbrunot
 	 *
 	 */
 	private class TrayIconUpdater implements Runnable
 	{
 		private Image newIcon;
 		
 		private String iconTooltip;
 		
 		private String messageToDisplay;
 		
 		private String messageCaption;
 		
 		private MessageType messageType;
 		
 		private ActionListener trayIconNewActionListener;
 		
 		public TrayIconUpdater(Image theNewIcon, String theNewIconTooltip, String theMessageCaption, String theMessageToDisplay, MessageType theMessageType, ActionListener theTrayIconNewActionListener)
 		{
 			this.newIcon = theNewIcon;
 			this.iconTooltip = theNewIconTooltip;
 			this.messageToDisplay = theMessageToDisplay;
 			this.messageCaption = theMessageCaption;
 			this.messageType = theMessageType;
 			this.trayIconNewActionListener = theTrayIconNewActionListener;
 		}
 		
 		public void run()
 		{
 			if (this.iconTooltip != null)
 			{
 				trayIcon.setToolTip(this.iconTooltip);
 			}
 			if (this.newIcon != null)
 			{
 				trayIcon.setImage(this.newIcon);
 			}
 			if (this.messageToDisplay != null)
 			{
 				trayIcon.displayMessage(this.messageCaption, this.messageToDisplay, this.messageType);
 			}
 			if (this.trayIconNewActionListener != null)
 			{
 				ActionListener[] listeners = trayIcon.getActionListeners();
 				for (ActionListener listener : listeners)
 				{
 					trayIcon.removeActionListener(listener);
 				}
 				trayIcon.addActionListener(this.trayIconNewActionListener);
 			}
 		}
 		
 	}
 
 	/**
 	 * A Runnable that can be launched with {@link javax.swing.SwingUtilities#invokeLater(Runnable)}
 	 * to update the build status in the system tray icon of the application.
 	 * It updates the tray icon and tooltip and the popup menu.
 	 * 
 	 * @author sbrunot
 	 *
 	 */
 	private class BuildStatusUpdater implements Runnable
 	{
 		//////////////////////////
 		// Instance attributes
 		//////////////////////////
 		
 		List<BuildReport> listOfBuildReportsOrderedByName = null;
 		int numberOfFailedBuilds = 0;
 		
 		//////////////////////////
 		// Constuctor
 		//////////////////////////
 		
 		/**
 		 * Create a new instance of the updater
 		 * @param theListOfBuildReportsOrderedByName the list of build reports to use
 		 * to update the system tray icon. It MUST be ordered by names (as when sorted
 		 * using the BuildReport.NameComparator comparator).
 		 */
 		public BuildStatusUpdater(List<BuildReport> theListOfBuildReportsOrderedByName)
 		{
 			this.listOfBuildReportsOrderedByName = theListOfBuildReportsOrderedByName;
 		}
 		
 		//////////////////////////
 		// Runnable implementation
 		//////////////////////////
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		public void run()
 		{
 			PopupMenu trayIconPopupMenu = trayIcon.getPopupMenu();
 			
 			// If the build results menu entries exists, delete them all
 			while (trayIconPopupMenu.getItemCount() > numberOfItemInEmptyTrayMenu)
 			{
 				trayIconPopupMenu.remove(indexOfTheFirstBuildResultMenuItem);
 			}
 			
 			// Create the build results menu entries
 			int newMenuItemIndex = indexOfTheFirstBuildResultMenuItem;
 			for (BuildReport buildReport : this.listOfBuildReportsOrderedByName)
 			{
 				// Create a MenuItem for this build report
 				MenuItem newMenuItem = createNewMenuItemForBuildReport(buildReport, FAILED_BUILD_MENUITEM_FONT, SUCCESSFULL_BUILD_MENUITEM_FONT);
 
 				// Insert the MenuItem into the popup menu
 				trayIconPopupMenu.insert(newMenuItem, newMenuItemIndex);
 				newMenuItemIndex++;
 			}
 			// Add the separator at the end
 			trayIconPopupMenu.insertSeparator(newMenuItemIndex);
 
 			// update action listener (that might have been changed when previously reporting a monitoring exception)
 			ActionListener[] listeners = trayIcon.getActionListeners();
 			for (ActionListener listener : listeners)
 			{
 				trayIcon.removeActionListener(listener);
 			}
 			trayIcon.addActionListener(openBuildServerHomePageActionListener);
 			
 			// update icon and tooltip
 			if (this.numberOfFailedBuilds > 0)
 			{
 				trayIcon.setImage(buildFailureIcon);
 			}
 			else
 			{
 				trayIcon.setImage(buildSuccessIcon);
 			}
 			SimpleDateFormat timeFormat = new SimpleDateFormat("HH'h'mm");
 			trayIcon.setToolTip(monitor.getSystemTrayIconTooltipHeader() + "\nLast update at " + timeFormat.format(new Date()) + "\n" + this.numberOfFailedBuilds + " failed builds out of " + this.listOfBuildReportsOrderedByName.size());
 		}
 
 		/////////////////////////////////
 		// Private methods
 		/////////////////////////////////
 
 		/**
 		 * Create a new menu item for a build report
 		 * @param theBuildReport the build report to create a menu item for
 		 * @param theBuildFailedFont the Font to use for a menu item related to a failed build
		 * @param theBuildSucessFont the Font to use for a menu ite related to a successfull build
 		 * @return
 		 */
 		private MenuItem createNewMenuItemForBuildReport(BuildReport theBuildReport, Font theBuildFailedFont, Font theBuildSucessFont)
 		{
 			MenuItem newMenuItem = new MenuItem(getMenuItemLabelForBuildReport(theBuildReport));
 			if (theBuildReport.hasFailed())
 			{
 				newMenuItem.setFont(theBuildFailedFont);
 				this.numberOfFailedBuilds++;
 			}
 			else
 			{
 				newMenuItem.setFont(theBuildSucessFont);
 			}
 			newMenuItem.setActionCommand(theBuildReport.getId());
 			newMenuItem.setName(theBuildReport.getName());
 			ActionListener newMenuItemActionListener = new ActionListener() {
 				public void actionPerformed(ActionEvent e)
 				{
 					if (Desktop.isDesktopSupported())
 					{
 						try
 						{
 							Desktop.getDesktop().browse(monitor.getBuildURI(e.getActionCommand()));
 						}
 						catch (IOException err)
 						{
 							// Nothing can be done here...
 						}
 					}
 				}
 			};
 			newMenuItem.addActionListener(newMenuItemActionListener);		
 			return newMenuItem;
 		}
 		
 		/**
 		 * Build a MenuItem label for a build report.
 		 * @param theBuildReport the build report
 		 * @return a MenuItem label for theBuildReport
 		 */
 		private String getMenuItemLabelForBuildReport(BuildReport theBuildReport)
 		{
 			String howLongAgo = null;
 			Period ageOfTheBuild = new Period(new LocalDateTime(theBuildReport.getDate()), new LocalDateTime());
 			// is it more than one year ago ?
 			if (ageOfTheBuild.getYears() > 0)
 			{
 				if (ageOfTheBuild.getYears() > 1)
 				{
 					howLongAgo = ageOfTheBuild.getYears() + " years ago";					
 				}
 				else
 				{
 					howLongAgo = "1 year ago";					
 				}
 			}
 			else if (ageOfTheBuild.getMonths() > 0)
 			{
 				if (ageOfTheBuild.getMonths() > 1)
 				{
 					howLongAgo = ageOfTheBuild.getMonths() + " months ago";											
 				}
 				else
 				{
 					howLongAgo = "1 month ago";											
 				}
 			}
 			else if (ageOfTheBuild.getWeeks() > 0)
 			{
 				if (ageOfTheBuild.getWeeks() > 1)
 				{
 					howLongAgo = ageOfTheBuild.getWeeks() + " weeks ago";
 				}
 				else
 				{
 					howLongAgo = "1 week ago";
 				}
 			}
 			else if (ageOfTheBuild.getDays() > 0)
 			{
 				if (ageOfTheBuild.getDays() > 1)
 				{
 					howLongAgo = ageOfTheBuild.getDays() + " days ago";
 				}
 				else
 				{
 					howLongAgo = "yesterday";
 				}
 			}
 			else if (ageOfTheBuild.getHours() > 0)
 			{
 				if (ageOfTheBuild.getHours() > 1)
 				{
 					howLongAgo = ageOfTheBuild.getHours() + " hours ago";
 				}
 				else
 				{
 					howLongAgo = "one hour ago";
 				}
 			}
 			else if (ageOfTheBuild.getMinutes() > 5)
 			{
 				howLongAgo = ageOfTheBuild.getMinutes() + " minutes ago";
 			}
 			else if (ageOfTheBuild.getMinutes() > 1)
 			{
 				howLongAgo = "a few minutes ago";
 			}
 			else
 			{
 				howLongAgo = "a few seconds ago";
 			}
 			
 			String failedIndicator = "";
 			if(theBuildReport.hasFailed())
          {
 			   failedIndicator = "- ";
          }
          else
          {
             failedIndicator = "+ ";
          }
 			
 			return failedIndicator+ theBuildReport.getName() + "  (" + howLongAgo + ")";
 		}
 	}
 
 	//////////////////////////////
 	// Instance attributes
 	//////////////////////////////
 	
 	private ResourceBundle messages = null;
 	
 	private Monitor monitor = null;
 	
 	private Thread monitorThread = null;
 	
 	private TrayIcon trayIcon = null;
 	
 	private Image initialIcon = null;
 
 	private Image buildSuccessIcon = null;
 	
 	private Image buildFailureIcon = null;
 
 	private Image monitoringExceptionIcon = null;
 
 	private ImageIcon aboutIcon = null;
 	
 	private String currentlyReportedMonitoringException = null;
 	
 	private ActionListener openBuildServerHomePageActionListener = null;
 	
 	private ActionListener openOptionsDialogActionListener = null;
 	
 	private int currentSortOrder = SORT_BY_NAME;
 
 	private CheckboxMenuItem sortByNameMenuItem = null;
 
 	private CheckboxMenuItem sortByAgeMenuItem = null;
 
 	/**
 	 * The number of menu items on the tray icon popup menu when it does not contains
 	 * build results (this value is calculated when the menu is build the first time).
 	 */
 	private int numberOfItemInEmptyTrayMenu = -1;
 	
 	/**
 	 * Index of the first build result menu item in the build menu. It is a constant,
 	 * defined when the menu is build the first time.
 	 */
 	private int indexOfTheFirstBuildResultMenuItem = -1;
 	
 	/**
 	 * The previous build reports (that we use to detect if the situation have changed)
 	 */
 	private List<BuildReport> previousBuildReports = new ArrayList<BuildReport>();
 	
 	//////////////////////////////
 	// Constructor
 	//////////////////////////////
 
 	/**
 	 * Create a new instance of BuildMonitorImpl
 	 * @param theMonitorImplementationClass the implementation of Monitor to use
 	 */
 	public BuildMonitorImpl(Class<? extends Monitor> theMonitorImplementationClass)
 	{
 		super();
 
 		// load messages resource file
 		this.messages = ResourceBundle.getBundle(MESSAGES_BASE_NAME, Locale.getDefault(), this.getClass().getClassLoader());
 
 		try
 		{
 			Constructor<?> monitorConstructor = theMonitorImplementationClass.getConstructor(new Class[] {BuildMonitor.class});
 			this.monitor = (Monitor) monitorConstructor.newInstance(new Object[] {this});
 		}
 		catch(Exception e)
 		{
 			// TODO: use a nice formatted message for the end user ?
 			panic(e);
 		}
 	}
 
 	//////////////////////////////
 	// Runnable implementation
 	//////////////////////////////
 	
 	/**
 	 * TODO: DOCUMENTS ME !
 	 */
 	public void run()
 	{
 		try
 		{
 			// load image resources
 			this.initialIcon = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(IMAGE_INITIAL_ICON));
 			this.monitoringExceptionIcon = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(IMAGE_MONITORING_EXCEPTION));
 			this.buildSuccessIcon = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(IMAGE_BUILD_SUCCESS));
 			this.buildFailureIcon = Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(IMAGE_BUILD_FAILURE));
 			this.aboutIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource(IMAGE_ABOUT_ICON)));
 
 			// create the system tray icon
 			if (SystemTray.isSupported())
 			{
 				// create the monitor instance (TODO: PROMPT THE USER / USE A PROPERTY TO DEFINE THE KIND OF MONITOR TO CREATE ?
 				// TODO: WHAT WILL BE THE LOOK & FEEL ???
 				// this.monitor = new BambooMonitor(this);
 				this.openBuildServerHomePageActionListener = new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						if (Desktop.isDesktopSupported())
 						{
 							try
 							{
 								Desktop.getDesktop().browse(monitor.getMainPageURI());
 							}
 							catch (IOException err)
 							{
 								// Nothing can be done here...
 							}
 						}
 					}
 				};
 
 //				this.monitor = new CruiseControlRssMonitor(this, new URL("http://leo:9080/cruisecontrol/rss"), new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"), 30);
 
 				// create the tray icon
 				SystemTray tray = SystemTray.getSystemTray();
 
 				// Popup menu for the tray icon
 				PopupMenu trayMenu = new PopupMenu();
 				
 				// Build server home page menu item
 				MenuItem buildServerHomePageMenuItem = new MenuItem(this.monitor.getMonitoredBuildSystemName() + " " + getMessage(MESSAGEKEY_TRAYICON_MENUITEM_BUILD_SERVER_HOME_PAGE_SUFFIX));
 				buildServerHomePageMenuItem.addActionListener(this.openBuildServerHomePageActionListener);
 				trayMenu.add(buildServerHomePageMenuItem);
 				
 				// Update status now menu item
 				MenuItem updateStatusNowMenuItem = new MenuItem(getMessage(MESSAGEKEY_TRAYICON_MENUITEM_UPDATE_STATUS_NOW));
 				ActionListener updateStatusNowMenuItemActionListener = new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						monitorThread.interrupt();
 					}
 				};
 				updateStatusNowMenuItem.addActionListener(updateStatusNowMenuItemActionListener);
 				trayMenu.add(updateStatusNowMenuItem);
 				
 				// Sort sub menu
 				Menu sortMenu = new Menu(getMessage(MESSAGEKEY_TRAYICON_MENU_SORT));
 				this.sortByAgeMenuItem = new CheckboxMenuItem(getMessage(MESSAGEKEY_TRAYICON_MENUITEM_SORT_BY_AGE), true);
 				this.currentSortOrder = SORT_BY_AGE;
 				sortMenu.add(this.sortByAgeMenuItem);
 				this.sortByNameMenuItem = new CheckboxMenuItem(getMessage(MESSAGEKEY_TRAYICON_MENUITEM_SORT_BY_NAME), false);
 				sortMenu.add(this.sortByNameMenuItem);
 				ItemListener sortByNameMenuItemActionListener = new ItemListener()
 				{
 					public void itemStateChanged(ItemEvent e)
 					{
 						if (e.getStateChange() == ItemEvent.SELECTED)
 						{
 							sortByNameMenuItem.setState(true);
 							sortByAgeMenuItem.setState(false);
 							currentSortOrder = SORT_BY_NAME;
 							reportConfigurationUpdatedToBeTakenIntoAccountImmediately();
 						}
 						else
 						{
 							sortByNameMenuItem.setState(true);
 							reportConfigurationUpdatedToBeTakenIntoAccountImmediately();
 						}
 						
 					}
 				};
 				this.sortByNameMenuItem.addItemListener(sortByNameMenuItemActionListener);
 				ItemListener sortByAgeMenuItemActionListener = new ItemListener()
 				{
 					public void itemStateChanged(ItemEvent e)
 					{
 						if (e.getStateChange() == ItemEvent.SELECTED)
 						{
 							sortByNameMenuItem.setState(false);
 							sortByAgeMenuItem.setState(true);
 							currentSortOrder = SORT_BY_AGE;
 							reportConfigurationUpdatedToBeTakenIntoAccountImmediately();
 						}
 						else
 						{
 							sortByAgeMenuItem.setState(true);
 							reportConfigurationUpdatedToBeTakenIntoAccountImmediately();
 						}
 						
 					}
 				};
 				this.sortByAgeMenuItem.addItemListener(sortByAgeMenuItemActionListener);
 				trayMenu.add(sortMenu);
 
 				// Options menu item
 				MenuItem optionsMenuItem = new MenuItem(getMessage(MESSAGEKEY_TRAYICON_MENUITEM_OPTIONS));
 				this.openOptionsDialogActionListener = new ActionListener() {
 					public void actionPerformed(ActionEvent e)
 					{
 						try
 						{
 							monitor.displayOptionsDialog();
 						}
 						catch (Exception exc)
 						{
 							panic(exc);
 						}
 					}
 				};
 				optionsMenuItem.addActionListener(this.openOptionsDialogActionListener);
 				trayMenu.add(optionsMenuItem);
 				
 				// Separator
 				trayMenu.addSeparator();
 
 				// Here will be added the build results menu items by the monitor
 				this.indexOfTheFirstBuildResultMenuItem = trayMenu.getItemCount();
 
 				// About menu item
 				MenuItem aboutMenuItem = new MenuItem(getMessage(MESSAGEKEY_TRAYICON_MENUITEM_ABOUT));
 				ActionListener aboutMenuItemActionListener = new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						// FIXME: THE ABOUT MESSAGE SHOULD BE DYNAMICALY GENERATED AT BUILD TIME
 						JOptionPane.showMessageDialog(null, "This is the preview version of build monitor, by sbrunot@gmail.com.\nBuild Revision: unknown\nCurrent monitor is the Bamboo monitor.\n\n", "About...", JOptionPane.INFORMATION_MESSAGE, aboutIcon);
 					}
 				};
 				aboutMenuItem.addActionListener(aboutMenuItemActionListener);
 				trayMenu.add(aboutMenuItem);
 				
 				// Exit sub menu
 				Menu exitMenu = new Menu(getMessage(MESSAGEKEY_TRAYICON_MENU_EXIT));
 				MenuItem exitMenuItem = new MenuItem(getMessage(MESSAGEKEY_TRAYICON_MENUITEM_EXIT));
 				ActionListener exitMenuItemActionListener = new ActionListener() {
 					public void actionPerformed(ActionEvent e)
 					{
 						System.exit(0);
 					}
 				};
 				exitMenuItem.addActionListener(exitMenuItemActionListener);
 				exitMenu.add(exitMenuItem);
 				trayMenu.add(exitMenu);
 				
 				this.numberOfItemInEmptyTrayMenu = trayMenu.getItemCount();
 				
 				this.trayIcon = new TrayIcon(this.initialIcon, getMessage(MESSAGEKEY_TRAYICON_INITIAL_TOOLTIP), trayMenu);
 				tray.add(trayIcon);
 			}
 			else
 			{
 				panic(getMessage(MESSAGEKEY_ERROR_SYSTEMTRAY_NOT_SUPPORTED));
 			}
 			
 			// Register a shutdown hook thread to stop monitoring thread on exit
 			Runtime.getRuntime().addShutdownHook(new ShutdownThread());
 			
 			// Start the monitor
 			this.monitorThread = new Thread(this.monitor, "Bamboo monitor thread");
 			this.monitorThread.start();
 
 			this.trayIcon.addActionListener(openBuildServerHomePageActionListener);
 			
 		}
 		catch(Throwable t)
 		{
 			panic(t);
 		}
 	}
 
 	//////////////////////////////
 	// BuildMonitor implementation
 	//////////////////////////////
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void panic(String theErrorMessage)
 	{
 		showErrorMessage(theErrorMessage);
 		// TODO: OPEN NEW EMAIL WITH ERROR MESSAGE ?
 		System.exit(1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void panic(Throwable theUnexpectedError)
 	{
 		MessageFormat errorMessage = new MessageFormat(getMessage(MESSAGEKEY_UNEXPECTED_ERROR_MESSAGE));
 		panic(errorMessage.format(new Object[] {theUnexpectedError.getMessage(), getStackTrace(theUnexpectedError)}));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getMessage(String theMessageKey)
 	{
 		return this.messages.getString(theMessageKey);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Image getDialogsDefaultIcon()
 	{
 		return this.initialIcon;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void reportMonitoringException(MonitoringException theMonitoringException)
 	{
 		// We only display the message if it is a new one (not the one currently displayed)
 		if ((this.currentlyReportedMonitoringException == null) || (!this.currentlyReportedMonitoringException.equals(theMonitoringException.getMessage())))
 		{
 			// We have two messages: the one to display in the alert bubble, and the one to display in the tray icon tooltip
 			String messageToDisplayInAlertBubble = theMonitoringException.getMessage();
 			String tooltipMessage = theMonitoringException.getMessage();
 			// The default action listener to use for the tray icon when the error is displayed
 			ActionListener trayIconNewActionListener = this.openBuildServerHomePageActionListener;
 
 			if (theMonitoringException.isOptionsRelated())
 			{
 				// The error is related to the options set by the end user: we inform the user in the displayed messages that he can double click the alert
 				// bubble or the tray icon to open the options dialog
 				messageToDisplayInAlertBubble += OPTIONS_RELATED_MESSAGES_SUFFIX;
 				// There is a maximum length for a tooltip message: truncate it if necessary
 				if (tooltipMessage != null && (tooltipMessage.length() + OPTIONS_RELATED_MESSAGES_SUFFIX.length()) > TOOLTIP_MAX_LENGTH)
 				{
 					tooltipMessage = tooltipMessage.substring(0, TOOLTIP_MAX_LENGTH - TRUNCATED_MESSAGE_SUFFIX.length() - OPTIONS_RELATED_MESSAGES_SUFFIX.length() - 1) + TRUNCATED_MESSAGE_SUFFIX + OPTIONS_RELATED_MESSAGES_SUFFIX;
 				}
 				else if (tooltipMessage != null)
 				{
 					tooltipMessage += OPTIONS_RELATED_MESSAGES_SUFFIX;
 				}
 				// Setup the tray icon action listener so that it opens the options dialog
 				trayIconNewActionListener = this.openOptionsDialogActionListener;
 			}
 			else
 			{
 				// There is a maximum length for a tooltip message: truncate it if necessary
 				if (tooltipMessage != null && tooltipMessage.length() > TOOLTIP_MAX_LENGTH)
 				{
 					tooltipMessage = tooltipMessage.substring(0, TOOLTIP_MAX_LENGTH - TRUNCATED_MESSAGE_SUFFIX.length() - 1) + TRUNCATED_MESSAGE_SUFFIX;
 				}
 				// If there is a related URI for the Exception, setup the tray icon action listener so that it opens it in a web browser
 				if (theMonitoringException.getRelatedURI() != null)
 				{
 					trayIconNewActionListener = new OpenURIInBrowserActionListener(theMonitoringException.getRelatedURI());
 				}
 			}
 			javax.swing.SwingUtilities.invokeLater(new TrayIconUpdater(this.monitoringExceptionIcon, tooltipMessage, "Build Monitor need your attention", messageToDisplayInAlertBubble, MessageType.ERROR, trayIconNewActionListener));
 			this.currentlyReportedMonitoringException = theMonitoringException.getMessage();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void reportConfigurationUpdatedToBeTakenIntoAccountImmediately()
 	{
 		// Interrupt the monitor thread so that it takes into account its new configuration
 		if (this.monitorThread != null)
 		{
 			this.monitorThread.interrupt();
 		}
 	}
 
 	/**
 	 * TODO: TRES TRES CHAUD... A TESTER DE MANIERE AUTOMATISEE !!!!
 	 * {@inheritDoc}
 	 */
 	public void updateBuildStatus(List<BuildReport> theBuildsStatus)
 	{
 		// 1) Sort the list of build reports according to user preferences (as set using the sort menu)
 		// TODO: POUVOIR AUSSI TRIER LA LISTE PAR AGE, EN FONCTION DE L'OPTION CHOISIE PAR L'UTILISATEUR
 		List<BuildReport> buildsStatus = new ArrayList<BuildReport>(theBuildsStatus);
 		if (this.currentSortOrder == SORT_BY_NAME)
 		{
 			Collections.sort(buildsStatus, new BuildReport.NameComparator());
 		}
 		else if (this.currentSortOrder == SORT_BY_AGE)
 		{
 			Collections.sort(buildsStatus, new BuildReport.AgeComparator());
 		}
 		
 		// 2) Update the tray menu
 		BuildStatusUpdater updater = new BuildStatusUpdater(buildsStatus);
 		javax.swing.SwingUtilities.invokeLater(updater);
 		
 		// 3) Status has been updated, so there is no current monitoring exception...
 		this.currentlyReportedMonitoringException = null;
 		
 		// 4) If situation have changed, notify the end user !
 		StringBuffer newFailingBuilds = new StringBuffer();
 		StringBuffer fixedBuilds = new StringBuffer();
 		for (BuildReport currentBuildReport : theBuildsStatus)
 		{
 			for (BuildReport previousBuildReport : this.previousBuildReports)
 			{
 				if (previousBuildReport.getId().equals(currentBuildReport.getId()))
 				{
 					if (previousBuildReport.getStatus() != currentBuildReport.getStatus())
 					{
 						if (currentBuildReport.getStatus() == BuildReport.Status.OK)
 						{
 							fixedBuilds.append(currentBuildReport.getName() + " is fixed.\n");
 						}
 						else
 						{
 							newFailingBuilds.append(currentBuildReport.getName() + " is failing.\n");
 						}
 					}
 				}
 			}
 		}
 		if ((newFailingBuilds.length() > 0) || (fixedBuilds.length() > 0))
 		{
 			MessageType messageType = MessageType.INFO;
 			if (newFailingBuilds.length() > 0)
 			{
 				messageType = MessageType.WARNING;
 			}
 			javax.swing.SwingUtilities.invokeLater(new TrayIconUpdater(null, null, "Build situation have changed !", newFailingBuilds.toString() + fixedBuilds.toString() + "Right click the tray icon to display the detailed build status.", messageType, null));
 		}
 		this.previousBuildReports = buildsStatus;
 	}
 
 
 	/**
 	 * displays an error message in a dialog.
 	 * @param theErrorMessage the error message to display
 	 */
 	protected void showErrorMessage(String theErrorMessage)
 	{
 		if (this.messages != null)
 		{
 			JOptionPane.showMessageDialog(null, theErrorMessage, getMessage(MESSAGEKEY_ERROR_DIALOG_TITLE), JOptionPane.ERROR_MESSAGE);		
 		}
 		else
 		{
 			JOptionPane.showMessageDialog(null, theErrorMessage, "Arghhhhhhhhh !", JOptionPane.ERROR_MESSAGE);		
 		}
 	}
 	
 	/**
 	 * Return the stack trace of a throwable as a String
 	 * @param theThrowable
 	 * @return
 	 */
 	protected String getStackTrace(Throwable theThrowable)
 	{
 		StringBuffer buffer = new StringBuffer();
 		StackTraceElement[] stackElements = theThrowable.getStackTrace();
 		for (int i=0; i < stackElements.length; i++)
 		{
 			buffer.append(stackElements[i] + "\n");
 		}
 		return buffer.toString();
 	}
 
 }
