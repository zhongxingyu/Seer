 package com.korshyadoo.calendar;
 
 import java.awt.ComponentOrientation;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 import javax.swing.border.EmptyBorder;
 
 import com.google.gdata.data.calendar.CalendarEventEntry;
 import com.google.gdata.data.calendar.CalendarEventFeed;
 import com.google.gdata.util.ServiceException;
 import com.pff.PSTAppointment;
 import com.pff.PSTException;
 import com.pff.PSTFolder;
 import javax.swing.JProgressBar;
 import javax.swing.JTextArea;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 
 /**
  * The main frame of the application
  *
  */
 public class MainFrame extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	private JLabel lblClock;
 	private JLabel lblCheck;
 	private JLabel lblSuccess;
 	protected JLabel lblNumEvents;
 	private JButton btnSync;
 	private JButton btnDeleteAllEvents;
 	private JButton btnChangeUser;
 	private JComboBox<String> cboFromMonth;
 	private JComboBox<String> cboToMonth;
 	private JComboBox<Integer> cboFromDay;
 	private JComboBox<Integer> cboToDay;
 	private JLabel lblDeleteError;
 	protected SyncReturn syncReturn;
 	protected List<CalendarEventEntry> deleteDateRangeEvents;
 	private int minutes;
 	private int seconds;
 	private JButton btnDeleteDateRange;
 	private JTextField txtFromYear;
 	private JTextField txtToYear;
 	private JLabel lblActionTime;
 	protected JLabel lblUsername;
 	private JProgressBar progressBar;
 	private JTextArea txtTaskOutput;
 	private DeleteAllEventsWorker daew;
 	private SyncWorker sworker;
 	private DeleteDateRangeWorker ddrw;
 	
 	/**
 	 * Create the frame.
 	 */
 	public MainFrame() {
		setTitle("Outlook To Google Calendar Sync Build 0003");
 		setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 725, 362);
 		setPreferredSize(new Dimension((int)getBounds().getWidth(), (int)getBounds().getHeight()));
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		centreWindow();
 
 		lblUsername = new JLabel("Logged in as " + OutlookToGoogleCalendarSync.getUserrname());
 		lblUsername.setBounds(57, 11, 346, 14);
 		contentPane.add(lblUsername);
 
 		lblClock = new JLabel("");
 		lblClock.setVisible(false);
 		lblClock.setIcon(new ImageIcon(MainFrame.class.getResource("/com/korshyadoo/calendar/clock_e0.gif")));
 		lblClock.setBounds(59, 50, 75, 75);
 		contentPane.add(lblClock);
 
 		lblCheck = new JLabel("");
 		lblCheck.setVisible(false);
 		lblCheck.setIcon(new ImageIcon(MainFrame.class.getResource("/com/korshyadoo/calendar/check_mark_green.jpg")));
 		lblCheck.setBounds(166, 50, 83, 75);
 		contentPane.add(lblCheck);
 
 		lblSuccess = new JLabel("Success!");
 		lblSuccess.setVisible(false);
 		lblSuccess.setBounds(187, 136, 62, 14);
 		contentPane.add(lblSuccess);
 
 		lblNumEvents = new JLabel("");
 		lblNumEvents.setBounds(6, 228, 397, 14);
 		contentPane.add(lblNumEvents);
 
 		btnSync = new JButton("Sync");
 		btnSync.addActionListener(new BTNSyncActionListener());
 		btnSync.setBounds(57, 162, 89, 23);
 		contentPane.add(btnSync);
 
 		btnDeleteAllEvents = new JButton("Delete All Events");
 		btnDeleteAllEvents.addActionListener(new BTNDeleteAllEventsActionListener());
 		btnDeleteAllEvents.setBounds(166, 161, 120, 24);
 		contentPane.add(btnDeleteAllEvents);
 
 		btnChangeUser = new JButton("Change User");
 		btnChangeUser.addActionListener(new BTNChangeUserActionListener());
 		btnChangeUser.setBounds(113, 196, 120, 23);
 		contentPane.add(btnChangeUser);
 
 		cboFromMonth = new JComboBox<>();
 		cboFromMonth.addActionListener(new CBOFromMonthActionListener());
 		cboFromMonth.setModel(new DefaultComboBoxModel<String>(new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"}));
 		cboFromMonth.setBounds(418, 53, 120, 23);
 		contentPane.add(cboFromMonth);
 
 		cboFromDay = new JComboBox<>();
 		cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}));
 		cboFromDay.setBounds(550, 52, 62, 25);
 		contentPane.add(cboFromDay);
 
 		cboToMonth = new JComboBox<>();
 		cboToMonth.addActionListener(new CBOToMonthActionListener());
 		cboToMonth.setModel(new DefaultComboBoxModel<String>(new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"}));
 		cboToMonth.setBounds(418, 85, 120, 23);
 		contentPane.add(cboToMonth);
 
 		cboToDay = new JComboBox<>();
 		cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}));
 		cboToDay.setBounds(550, 84, 62, 25);
 		contentPane.add(cboToDay);
 
 		btnDeleteDateRange = new JButton("Delete date range");
 		btnDeleteDateRange.addActionListener(new BTNDeleteDateRangeActionListener());
 		btnDeleteDateRange.setBounds(452, 120, 130, 28);
 		contentPane.add(btnDeleteDateRange);
 
 		JLabel lblFrom = new JLabel("From:");
 		lblFrom.setBounds(380, 54, 37, 16);
 		contentPane.add(lblFrom);
 
 		JLabel lblTo = new JLabel("To:");
 		lblTo.setBounds(380, 88, 37, 16);
 		contentPane.add(lblTo);
 
 		lblDeleteError = new JLabel("<html><font color='red'>From date is before To date</font></html>");
 		lblDeleteError.setBounds(441, 160, 156, 14);
 		lblDeleteError.setVisible(false);
 		contentPane.add(lblDeleteError);
 
 		txtFromYear = new JTextField();
 		txtFromYear.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				//When focus is lost, if February is selected as the from month, update the number of days in cboFromDay
 				if(cboFromMonth.getSelectedItem().equals("February")) {
 					updateCboFromDay();
 				}
 			}
 		});
 		txtFromYear.setText("2013");
 		txtFromYear.setBounds(630, 50, 47, 28);
 		contentPane.add(txtFromYear);
 		txtFromYear.setColumns(10);
 
 		txtToYear = new JTextField();
 		txtToYear.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				//When focus is lost, if February is selected as the to month, update the number of days in cboToDay
 				if(cboToMonth.getSelectedItem().equals("February")) {
 					updateCboToDay();
 				}
 			}
 		});
 		txtToYear.setText("2013");
 		txtToYear.setColumns(10);
 		txtToYear.setBounds(630, 85, 47, 28);
 		contentPane.add(txtToYear);
 
 		lblActionTime = new JLabel("");
 		lblActionTime.setBounds(22, 267, 211, 20);
 		contentPane.add(lblActionTime);
 		
 		progressBar = new JProgressBar();
 		progressBar.setVisible(false);
 		progressBar.setBounds(20, 137, 150, 19);
 		progressBar.setStringPainted(true);
 		contentPane.add(progressBar);
 		
 		txtTaskOutput = new JTextArea();
 		txtTaskOutput.setVisible(false);
 		txtTaskOutput.setBounds(380, 214, 297, 73);
 		contentPane.add(txtTaskOutput);
 		
 		JSeparator separator = new JSeparator();
 		separator.setOrientation(SwingConstants.VERTICAL);
 		separator.setBounds(333, 11, 12, 307);
 		contentPane.add(separator);
 	}
 
 	/**
 	 * A SwingWorker that allows UI updates to occur while using the delete date range feature.
 	 *
 	 */
 	protected class DeleteDateRangeWorker extends SwingWorker<List<CalendarEventEntry>, Void> {
 		private Date from;						//The beginning of the range to be deleted
 		private Date to;						//The end of the range to be deleted
 		private Date startTimer;				//Tracks how long the process takes
 
 		//Constructor
 		public DeleteDateRangeWorker(Date f, Date t, Date st) {
 			from = f;
 			to = t;
 			startTimer = st;
 		}
 		
 		@Override
 		/**
 		 * Retrieves the CalendarEventEntry objects from the Google calendar that are within the selected time range,
 		 * sends a batch request to have the deleted, and reports the length of time the process took.
 		 */
 		protected List<CalendarEventEntry> doInBackground() {
 			List<CalendarEventEntry> delete = null;
 			try {
 				//Retrieve the events within the selected date range and send batch request to delete them
 				delete = OutlookToGoogleCalendarSync.timeQuery(from, to);
 				OutlookToGoogleCalendarSync.deleteEvents(delete);
 				
 				//Update UI: 
 				//reset mouse cursor, turn off clock .gif, display success checkmark and label, enable all buttons, and hide progress bar
 				MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				lblClock.setVisible(false);
 				lblCheck.setVisible(true);
 				lblSuccess.setVisible(true);
 				btnSync.setEnabled(true);
 				btnDeleteAllEvents.setEnabled(true);
 				btnChangeUser.setEnabled(true);
 				btnDeleteDateRange.setEnabled(true);
 				progressBar.setVisible(false);
 
 				//Find the time it took to perform the action
 				Date endTimer = new Date();
 				int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
 				minutes = (int)(totalTime / 60);
 				seconds = (int)totalTime - (minutes * 60);
 				lblActionTime.setText("Action time: " + minutes + " minutes, " + seconds + " seconds");
 			} catch (ServiceException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} 
 			return delete;
 		}
 
 		@Override
 		/**
 		 * Writes a log entry recording all events deleted
 		 */
 		public void done() {
 			//Get the List of deleted CalendarEventEntrys
 			try {
 				deleteDateRangeEvents = get();
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			} catch (ExecutionException e1) {
 				e1.printStackTrace();
 			}
 
 			//Write log file entry recording all events deleted
 			if(deleteDateRangeEvents != null && deleteDateRangeEvents.size() > 0) {														//If at least one event was deleted
 				try (BufferedWriter log = new BufferedWriter(new FileWriter(new File(OutlookToGoogleCalendarSync.LOG_TXT), true))) {
 					//Add formatting and record current Date
 					Date now = new Date();
 					log.write("\n" + OutlookToGoogleCalendarSync.SEPARATOR + 
 							"\n" + "DELETED the following events at " + now.toString() + 
 							"\nACTION TIME: " + minutes + " minutes, " + seconds + " seconds");
 
 					//Write each deleted event to the log
 					for(CalendarEventEntry cee : deleteDateRangeEvents) {													//For each deleted appointment
 						log.write("\n\n\"" + cee.getTitle().getPlainText() + "\"\n" +
 								"Start: " + new Date(cee.getTimes().get(0).getStartTime().getValue()).toString() + "\n" +
 								"End: " + new Date(cee.getTimes().get(0).getEndTime().getValue()).toString() + "\n");
 						if(cee.getLocations().get(0).getValueString().length() > 0) {						//If the event has a location, write it
 							log.write("Location: " + cee.getLocations().get(0).getValueString() + "\n");
 						}
 						if(cee.getPlainTextContent().length() > 0) {										//If the event has content, write it
 							log.write("Content: " + cee.getPlainTextContent() + "\n");
 						}
 						log.write("Created: " + new Date(cee.getPublished().getValue()).toString());
 					}
 				} catch (IOException e) {
 					//Run IOExceptionFrame
 					try {
 						java.awt.EventQueue.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								new IOExceptionFrame(IOExceptionFrame.RETRY_DELETE_DATE_RANGE, MainFrame.this).setVisible(true);
 							}
 						});
 						MainFrame.this.setVisible(false);
 						System.out.println("another test");
 					} catch(Throwable ev) {
 						System.out.println("Couldn't run IOExceptionFrame");
 					}
 				}
 			}
 		}
 	}
 
 	protected class SyncWorker extends SwingWorker<SyncReturn, Void> {
 		private Date startTimer;
 
 		public SyncWorker(Date start) {
 			startTimer = start;
 		}
 
 		/**
 		 * The Google calendar is queried to retrieve an ArrayList containing 
 		 * all appointments with a start time 6 months prior to run time 
 		 * and 100 years ahead. Then, each appointment in the .pst file 
 		 * is compared to each appointment in events to look for matches. 
 		 * If an appointment from the .pst is found in events, it is removed 
 		 * from events. If a .pst appointment is not in events, it is added to 
 		 * a second, insertQueue, ArrayList. When all .pst files in the time 
 		 * frame have been checked, a batch request is sent to Google to delete 
 		 * all appointments remaining in events and insert all appointments 
 		 * in insertQueue.
 		 * @return Has no use.
 		 * @throws PSTException
 		 * @throws ServiceException
 		 * @throws IOException 
 		 */
 		@Override
 		protected SyncReturn doInBackground() throws PSTException, ServiceException, IOException {
 			//Retrieve all gmail events 6 months prior and 100 years into future
 			List<CalendarEventEntry> events = OutlookToGoogleCalendarSync.timeRangeQuery();
 			SyncReturn numEvents = new SyncReturn(events.size());
 
 			List<CalendarEventEntry> insertQueue = new ArrayList<>();                                  		//List of CEEs to be added to gmail calendar
 
 			//For each PSTAppointment, check if it's in events; delete it from events if it is; add it to addQueue if it's not
 			List<PSTFolder> outlookFolders = OutlookToGoogleCalendarSync.getOutlookFolders();
 			for(int x = 0; x < outlookFolders.size(); x++) {                                                //For each outlook folder
 				if(outlookFolders.get(x).getDisplayName().equals("Calendar")) {                       		//If the folder is named Calendar
 					for(int l = 0; l < outlookFolders.get(x).getContentCount(); l++) {                		//For each appointment
 						PSTAppointment appointment = (PSTAppointment)outlookFolders.get(x).getNextChild();
 
 						//Check if the appointment is in the time range
 						//If it is not, continue to next appointment
 						//If it is, and events is not empty, compare it to each event to find a match
 						//If a match if found, remove the corresponding event from events
 						boolean found = false;
 						if(appointment.getStartTime().getTime() >= OutlookToGoogleCalendarSync.getStartDate().getTime() && 
 								appointment.getStartTime().getTime() <= OutlookToGoogleCalendarSync.getEndDate().getTime()) {	//If appointment is within the desired time range
 							System.out.println("Start time: " + OutlookToGoogleCalendarSync.getStartDate().toString());
 							System.out.println("app start" + appointment.getStartTime().toString());
 							if(!events.isEmpty()) {
 								for(int y = 0; y < events.size(); y++) {									//For each event CEE
 									//Compare appointment to event[y]
 									//If they match, delete event[y]
 									if(OutlookToGoogleCalendarSync.compareCEEToPST(events.get(y), appointment)) {
 										events.remove(y);
 										found = true;														//Prevents adding appointment to insertQueue
 										break;
 									}
 								}
 							}
 							//If no match was found in events, or if events is empty, add appointment to insertqueue
 							if(!found) {
 								ConvertReturn cr = OutlookToGoogleCalendarSync.convertPSTToCEE(appointment);
 								insertQueue.add(cr.getCEE());
 								System.out.println("Adding PST \"" + appointment.getSubject() + "\"" + ", TZ = " +
 										appointment.getStartTimeZone().getDaylightBias() + 
 										" bias " + appointment.getStartTimeZone().getBias() + 
 										" time " + appointment.getStartTime() + " to insert queue" );
 							}
 						}
 					}
 				}
 			}
 
 			//Delete events from gmail calendar
 			if(events.isEmpty()) {
 				System.out.println("no events to delete");
 			} else {
 				for(int x = 0; x < events.size(); x++) {
 					System.out.println("deleting: ");
 					System.out.println("" + new Date(events.get(x).getTimes().get(0).getStartTime().getValue()).toString());
 				}
 				OutlookToGoogleCalendarSync.deleteEvents(events);
 				numEvents.after = numEvents.after - events.size();
 			}
 
 			//Add events in insertQueue
 			if(insertQueue.isEmpty()) {
 				System.out.println("no events to insert");
 			} else {
 				for(int x = 0; x < insertQueue.size(); x++) {
 					System.out.println("inserting: ");
 					System.out.println(new Date(insertQueue.get(x).getTimes().get(0).getStartTime().getValue()).toString() +
 							" / " + insertQueue.get(x).getTitle().getPlainText());
 					System.out.println("CEE Tzshift = " + insertQueue.get(x).getTimes().get(0).getStartTime().getTzShift());
 				}
 				OutlookToGoogleCalendarSync.insertEvents(insertQueue);
 				numEvents.after = numEvents.after + insertQueue.size();
 			} 
 			MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			lblClock.setVisible(false);
 			lblCheck.setVisible(true);
 			lblSuccess.setVisible(true);
 			btnSync.setEnabled(true);
 			btnDeleteAllEvents.setEnabled(true);
 			btnChangeUser.setEnabled(true);
 			btnDeleteDateRange.setEnabled(true);
 			progressBar.setVisible(false);
 
 			//Find the time it took to perform the action
 			Date endTimer = new Date();
 			int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
 			minutes = (int)(totalTime / 60);
 			seconds = (int)totalTime - (minutes * 60);
 
 			return numEvents;
 		}
 
 		@Override
 		public void done() {
 			try {
 				//Update label
 				if(get() != null) {
 					syncReturn = get();
 					lblNumEvents.setText("In Google: " + syncReturn.before + " events before, " + syncReturn.after + " after");
 					lblActionTime.setText("Action Time: " + minutes + " minutes, " + seconds + " seconds");
 
 					//Write log file entry with current time and number of Google events before and after
 					try (BufferedWriter log = new BufferedWriter(new FileWriter(new File(OutlookToGoogleCalendarSync.LOG_TXT), true))) {
 						log.newLine();
 						log.write(OutlookToGoogleCalendarSync.SEPARATOR);
 						log.newLine();
 						Date now = new Date();
 						log.write("SYNC at " + now.toString() + "\nACTION TIME: " + minutes + " minutes, " + seconds + " seconds");
 						log.newLine();
 						log.write("In Google: " + syncReturn.before + " events before, " + syncReturn.after + " after");
 					} catch (IOException e) {
 						//IOException can be caused by file in use
 						//Run IOExceptionFrame
 						try {
 							java.awt.EventQueue.invokeLater(new Runnable() {
 								@Override
 								public void run() {
 									new IOExceptionFrame(IOExceptionFrame.RETRY_SYNC, MainFrame.this).setVisible(true);
 								}
 							});
 							MainFrame.this.setVisible(false);
 						} catch(Throwable ev) {
 							System.out.println("Couldn't run IOExceptionFrame");
 						}
 					}
 				}
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (ExecutionException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}	
 
 	protected class DeleteAllEventsWorker extends SwingWorker<Boolean, Void> {
		private Date startTimer;
 		
 		public DeleteAllEventsWorker(Date d) {
 			startTimer = d;
 		}
 
 		@Override
 		protected Boolean doInBackground() {
 			try {
 				CalendarEventFeed resultFeed = OutlookToGoogleCalendarSync.getMyService().getFeed(OutlookToGoogleCalendarSync.getEventFeedURL(),
 						CalendarEventFeed.class);
 				double progress = 0;
 				double totalResults = resultFeed.getTotalResults();
 				setProgress(0);
 				System.out.println("umber of e: " + totalResults);
 				ArrayList<CalendarEventEntry> allCEE = new ArrayList<>();
 				int total = 0;
 				int page = 1;
 				do {
 					for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each entry in the resultFeed
 						CalendarEventEntry entry = resultFeed.getEntries().get(i);
 						allCEE.add(entry);
 						progress += ((1 / totalResults) * 100);
 						setProgress((int)progress);
 					}
 					System.out.println("page " + page + "; " + allCEE.size() + " items");
 					total += allCEE.size();
 					OutlookToGoogleCalendarSync.deleteEvents(allCEE);
 					allCEE.clear();
 					if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
 						resultFeed = OutlookToGoogleCalendarSync.getMyService().getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
 					} else {															//If there are no more pages of results, start the feed over
 						resultFeed = OutlookToGoogleCalendarSync.getMyService().getFeed(OutlookToGoogleCalendarSync.getEventFeedURL(),
 								CalendarEventFeed.class);
 						if(resultFeed.getEntries().size() == 0) {						//If the new feed is empty, all the events are deleted
 							resultFeed = null;
 						} else {														
 							//The new feed has more events to delete
 							System.out.println("Retrieving a new feed of events to delete");
 						}
 						if (allCEE.size() > 0) {	
 							//Delete the retrieved events
 							OutlookToGoogleCalendarSync.deleteEvents(allCEE);
 							total += allCEE.size();
 							allCEE.clear();
 						}
 					}
 					page++;
 				} while(resultFeed != null);
 				System.out.println("Deleted " + total + " events");
 				MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				lblClock.setVisible(false);
 				lblCheck.setVisible(true);
 				lblSuccess.setVisible(true);
 				btnSync.setEnabled(true);
 				btnDeleteAllEvents.setEnabled(true);
 				btnChangeUser.setEnabled(true);
 				btnDeleteDateRange.setEnabled(true);
 				progressBar.setVisible(false);
 
 				//Find the time it took to perform the action
 				Date endTimer = new Date();
 				int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
 				minutes = (int)(totalTime / 60);
 				seconds = (int)totalTime - (minutes * 60);
 				lblActionTime.setText("Action time: " + minutes + " minutes, " + seconds + " seconds");
 			} catch (MalformedURLException e) {
 				System.out.println("MalformedURL exception deleting");
 				e.printStackTrace();
 			} catch (IOException e) {
 				System.out.println("IO exception deleting");
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				System.out.println("Service exception deleting");
 				e.printStackTrace();
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * Fires when a property of the DeleteAllEventsWorker is changed.
 	 * Used to update the progress bar
 	 * 
 	 */
 	private class BTNDeleteAllEventsPropertyChangeListener implements PropertyChangeListener {
 	    /**
 	     * Invoked when task's progress property changes.
 	     */
 	    public void propertyChange(PropertyChangeEvent evt) {
 	        if ("progress" == evt.getPropertyName()) {
 	            int progress = (Integer)evt.getNewValue();
 	            progressBar.setValue(progress);
 	            txtTaskOutput.append(String.format(
 	                    "Completed %d%% of task.\n", MainFrame.this.daew.getProgress()));
 	        } 
 	    }
 	}
 	
 	private class BTNSyncPropertyChangesListener implements PropertyChangeListener {
 	    /**
 	     * Invoked when task's progress property changes.
 	     */
 	    public void propertyChange(PropertyChangeEvent evt) {
 	        if ("progress" == evt.getPropertyName()) {
 	            int progress = (Integer)evt.getNewValue();
 	            progressBar.setValue(progress);
 	            txtTaskOutput.append(String.format(
 	                    "Completed %d%% of task.\n", MainFrame.this.daew.getProgress()));
 	        } 
 	    }
 	}
 	
 	private class BTNDeleteDateRangePropertyChangeListener implements PropertyChangeListener {
 	    /**
 	     * Invoked when task's progress property changes.
 	     */
 	    public void propertyChange(PropertyChangeEvent evt) {
 	        if ("progress" == evt.getPropertyName()) {
 	            int progress = (Integer)evt.getNewValue();
 	            progressBar.setValue(progress);
 	            txtTaskOutput.append(String.format(
 	                    "Completed %d%% of task.\n", MainFrame.this.daew.getProgress()));
 	        } 
 	    }
 	}
 	
 	/**
 	 * Adjust the from date list to reflect valid days of the month
 	 */
 	public void updateCboFromDay() {
 		int day = cboFromDay.getSelectedIndex();			//Store the currently selected from day
 		switch(cboFromMonth.getSelectedIndex()) {
 		case 3:
 		case 5:
 		case 8:
 		case 10:
 			//30 days
 			cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30}));
 			break;
 		case 1:
 			//February:28 or 29
 			if(Integer.parseInt(txtFromYear.getText()) % 4 == 0) {
 				cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29}));
 			} else {
 				cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28}));
 			}
 			break;
 		default:
 			//31 days
 			cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}));
 		}
 		//Set the from day to what was previously selected
 		//If it's not a valid day, selected the last day of the selected month
 		try {
 			cboFromDay.setSelectedIndex(day);
 		} catch(IllegalArgumentException e) {
 			cboFromDay.setSelectedIndex(cboFromDay.getItemCount() - 1);
 		}
 	}
 
 	private void updateCboToDay() {
 		int day = cboToDay.getSelectedIndex();			//Store the currently selected to day
 		switch(cboToMonth.getSelectedIndex()) {
 		case 3:
 		case 5:
 		case 8:
 		case 10:
 			//30 days
 			cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30}));
 			break;
 		case 1:
 			//February:28 or 29
 			if(Integer.parseInt(txtToYear.getText()) % 4 == 0) {
 				cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29}));
 			} else {
 				cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28}));
 			}
 			break;
 		default:
 			//31 days
 			cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}));
 		}
 		//Set the to day to what was previously selected
 		//If it's not a valid day, selected the last day of the selected month
 		try {
 			cboToDay.setSelectedIndex(day);
 		} catch(IllegalArgumentException e) {
 			cboToDay.setSelectedIndex(cboToDay.getItemCount() - 1);
 		}
 	}
 
 	/**
 	 * Centres the window on the screen
 	 * @param jf JFrame for the window to be centred
 	 */
 	private void centreWindow() {
 		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 		int X = (screen.width / 2) - ((int)this.getPreferredSize().getWidth() / 2); // Center horizontally.
 		int Y = (screen.height / 2) - ((int)this.getPreferredSize().getHeight() / 2); // Center vertically.
 		this.setBounds(X,Y , (int)this.getPreferredSize().getWidth(), (int)this.getPreferredSize().getHeight());        
 	}
 	
 	private void btnChangeUserActionPerformed(java.awt.event.ActionEvent evt) {
 		//Run FirstRunFrame and close this window
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				new FirstRunFrame(MainFrame.this).setVisible(true);
 			}
 		});
 		this.setVisible(false);  
 	}
 	
 	/**
 	 * Starts the timer to time how long it takes to sync.
 	 * Updates the UI to have applicable labels set to not visible, makes the 
 	 * clock visible, changes to the wait mouse cursor, deactivates all buttons, 
 	 * makes the progress bar visible, and then calls the SwingWorker.
 	 * Called by BTNDeleteAllEventsActionListener.
 	 * @param evt
 	 */
 	private void btnSyncActionPerformed(ActionEvent evt) {
 		//Start the timer that times how long until the action is completed
 		Date startTimer = new Date();
 
 		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		lblClock.setVisible(true);
 		lblCheck.setVisible(false);
 		lblSuccess.setVisible(false);
 		lblDeleteError.setVisible(false);
 		lblActionTime.setText("");
 		btnSync.setEnabled(false);
 		btnDeleteAllEvents.setEnabled(false);
 		btnChangeUser.setEnabled(false);
 		btnDeleteDateRange.setEnabled(false);
		progressBar.setVisible(true);
 		sworker = new SyncWorker(startTimer);
 		sworker.addPropertyChangeListener(new BTNSyncPropertyChangesListener());
 		sworker.execute();
 	}
 	
 	/**
 	 * Starts the timer to time how long it takes to delete date range.
 	 * Updates the UI to have applicable labels set to not visible, makes the 
 	 * clock visible, changes to the wait mouse cursor, deactivates all buttons, 
 	 * makes the progress bar visible, and then calls the SwingWorker.
 	 * Called by BTNDeleteAllEventsActionListener.
 	 * @param evt
 	 */
 	private void btnDeleteDateRangeActionPerformed (ActionEvent evt) {
 		//Start the timer that times how long until the action is completed
 		Date startTimer = new Date();
 
 		//Hide the success image and label and clear label showing the number of events
 		lblCheck.setVisible(false);
 		lblSuccess.setVisible(false);
 		lblActionTime.setText("");
 		lblNumEvents.setText("");
 
 		//If the to year is equal to or greater than the from year
 		if(Integer.parseInt(txtToYear.getText()) >= Integer.parseInt(txtFromYear.getText())) {
 			//Get From date
 			Calendar from = new GregorianCalendar();
 			int month = cboFromMonth.getSelectedIndex();
 			int day = (Integer)cboFromDay.getSelectedItem();
 			int year = Integer.parseInt(txtFromYear.getText());
 			from.set(year, month, day, 0, 0, 0);
 			Date fromDate = from.getTime();
 
 			//Get To date
 			Calendar to = new GregorianCalendar();
 			month = cboToMonth.getSelectedIndex();
 			day = (Integer)cboToDay.getSelectedItem();
 			year = Integer.parseInt(txtToYear.getText());
 			to.set(year, month, day, 23, 59, 59);
 			Date toDate = to.getTime();
 
 			//If the from date is before the to date, execute the SwingWorker. Otherwise, make lblDeleteError visible
 			assert fromDate.compareTo(toDate) != 0;			//Because the hours, minutes, and seconds are hard-coded, the from and to dates cannot be equal
 			if(fromDate.compareTo(toDate) > 0) {
 				//fromDate is after toDate: error
 				lblDeleteError.setVisible(true);
 			} else {
 				//fromDate is before toDate: execute DeleteDateRangeWorker
 				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				lblClock.setVisible(true);
 				lblDeleteError.setVisible(false);
 				btnSync.setEnabled(false);
 				btnDeleteAllEvents.setEnabled(false);
 				btnChangeUser.setEnabled(false);
 				btnDeleteDateRange.setEnabled(false);
				progressBar.setVisible(true);
 				ddrw = new DeleteDateRangeWorker(fromDate, toDate, startTimer);
 				ddrw.addPropertyChangeListener(new BTNDeleteDateRangePropertyChangeListener());
 				sworker.addPropertyChangeListener(new BTNSyncPropertyChangesListener());
 				ddrw.execute();
 			}
 		} else {									//From year is after to year
 			lblDeleteError.setVisible(true);		//Display error
 		}
 	}
 	
 	/**
 	 * Starts the timer to time how long it takes to delete all events.
 	 * Updates the UI to have applicable labels set to not visible, makes the 
 	 * clock visible, changes to the wait mouse cursor, deactivates all buttons, 
 	 * makes the progress bar visible, and then calls the SwingWorker.
 	 * Called by BTNDeleteAllEventsActionListener.
 	 * @param evt
 	 */
 	private void btnDeleteAllEventsActionPerformed(ActionEvent evt) {
 		//Start the timer that times how long until the action is completed
 		Date startTimer = new Date();
 
 		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		lblClock.setVisible(true);
 		lblCheck.setVisible(false);
 		lblSuccess.setVisible(false);
 		lblDeleteError.setVisible(false);
 		btnSync.setEnabled(false);
 		btnDeleteAllEvents.setEnabled(false);
 		btnChangeUser.setEnabled(false);
 		btnDeleteDateRange.setEnabled(false);
 		lblActionTime.setText("");
 		progressBar.setVisible(true);
 		daew = new DeleteAllEventsWorker(startTimer);
 		daew.addPropertyChangeListener(new BTNDeleteAllEventsPropertyChangeListener());
 		daew.execute();
 	}
 	
 	/**
 	 * When the from month is selected, adjust the from date list to reflect valid days of the month
 	 * @param evt
 	 */
 	private void cboFromMonthActionPerformed(ActionEvent evt) {
 		updateCboFromDay();
 	}
 	
 	/**
 	 * Fires when btnSync is pressed
 	 *
 	 */
 	public class BTNSyncActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			btnSyncActionPerformed(arg0);
 		}
 	}
 	
 	/**
 	 * Fires when btnDeleteAllEvents is pressed 
 	 *
 	 */
 	private class BTNDeleteAllEventsActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			btnDeleteAllEventsActionPerformed(arg0);
 		}
 	}
 	
 	/**
 	 * Fires when btnDeleteDateRange is pressed
 	 *
 	 */
 	private class BTNDeleteDateRangeActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			btnDeleteDateRangeActionPerformed(arg0);
 		}
 	}
 	
 	/**
 	 * Fires when cboFromMonth is changed
 	 *
 	 */
 	private class CBOFromMonthActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			cboFromMonthActionPerformed(arg0);
 		}
 	}
 	
 	/**
 	 * Fires when cboToMonth is changed
 	 *
 	 */
 	private class CBOToMonthActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			cboToMonthActionPerformed(arg0);
 		}
 	}
 
 	/**
 	 * Called by CBOToMonthActionListener
 	 * @param evt
 	 */
 	private void cboToMonthActionPerformed(ActionEvent evt) {
 		updateCboToDay();
 	}
 	
 	/**
 	 * Fires when btnChangeUser is pressed
 	 *
 	 */
 	public class BTNChangeUserActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			btnChangeUserActionPerformed(arg0);
 		}
 	}
 	
 	//Getters
 	public JLabel getLBLClock() { return lblClock; }
 	public SyncReturn getSyncReturn() { return syncReturn; }
 	public int getMinutes() { return minutes; }
 	public int getSeconds() { return seconds; }
 }
