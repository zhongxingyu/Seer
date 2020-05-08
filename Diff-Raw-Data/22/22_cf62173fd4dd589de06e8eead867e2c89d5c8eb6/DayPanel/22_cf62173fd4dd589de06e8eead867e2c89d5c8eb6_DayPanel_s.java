 /**
  * The DayPanel is the panel on the left hand side of the application underneath the calendar. 
  * It holds buttons to add/remove patients, add/remove practitioners, add or remove a room, and 
  * change the time slot for the day. It also contains a table holding the names of patients with 
  * appointments. 
  */
 
 package gui.main;
 
 import gui.sub.SelectPatientUI;
 import gui.sub.SelectPractitionerUI;
 import gui.sub.SelectTimeSlotUI;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.DayDto;
 
 import java.util.Date;
 import java.util.List;
 
 import backend.DataTransferObjects.PractitionerDto;
 import backend.DataTransferObjects.SchedulePractitionerDto;
 import gui.TimeSlot;
 
 public class DayPanel extends JPanel {
 	SidePanel sidePanel;
 	
 	private DayDto day;
 	private List<SchedulePractitionerDto> schedulePractitionerList;
 	private JButton switchViewButton = new PanelButton("Month View");
 	private JButton patientButton = new PanelButton("Schedule Patient");
 	private JButton addPracButton = new PanelButton("Schedule Practitioner");
 	private JButton removePracButton = new PanelButton("Remove Practitioner From Current Day");
 	private JButton setTimeSlotButton = new PanelButton("Hours of Operation");
 	private JButton searchButton = new PanelButton("Search");
 	private JButton apptConfirmationButton = new PanelButton("Appointment Confirmations");
 	private JButton waitListButton = new PanelButton("Wait List");
 	
 	
 	private RoomPanel rp;
 	private AppointmentBlock ab;
 	private AppointmentSubpanel as;
 	private MainWindow mw;
 	
 	public DayPanel(DayDto day, MainWindow mw) {
 		this.mw = mw;
 		setBackground(Color.WHITE);
 		this.day = day;
 		
 		switchViewButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		patientButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		addPracButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		removePracButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		setTimeSlotButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		searchButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		apptConfirmationButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		waitListButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		
 		
 		//We don't want these focusable, so they won't disrupt the focus of the main calendar area
 		switchViewButton.setAction(switchViewAction);
 		switchViewButton.setFocusable(false);
 		patientButton.setAction(addPatAction);
 		patientButton.setFocusable(false);
 		patientButton.setEnabled(false);
 		addPracButton.setFocusable(false);
 		addPracButton.setAction(addPracAction);
 		removePracButton.setAction(removePracAction);
 		removePracButton.setFocusable(false);
 		removePracButton.setEnabled(false);
 		setTimeSlotButton.setFocusable(false);
 		searchButton.setAction(searchAction);
 		searchButton.setFocusable(false);
 		apptConfirmationButton.setAction(apptConfirmationAction);
 		apptConfirmationButton.setFocusable(false);
 		setTimeSlotButton.setAction(changeTimeSlotAction);
 		waitListButton.setAction(waitListAction);
 		waitListButton.setFocusable(true);
 		
 		JPanel buttonPanel = new JPanel(new GridLayout(0,1));
 		
 		buttonPanel.add(switchViewButton);
 		buttonPanel.add(addPracButton);
 		buttonPanel.add(removePracButton);
 		buttonPanel.add(patientButton);
 		buttonPanel.add(apptConfirmationButton);
 		buttonPanel.add(waitListButton);
 		buttonPanel.add(searchButton);
 		buttonPanel.add(setTimeSlotButton);
 		
 		
 		add(buttonPanel);
 	}
 	
 	public DayDto getDay() {
 		return day;
 	}
 	
 	/**
 	 * Used by the AppointmentSubpanel to register itself. Useful for 
 	 * rebuilding the subpanel
 	 * @param as the AppointmentSubpanel
 	 */
 	public void registerAppointmentSubpanel(AppointmentSubpanel as) {
 		this.as = as;
 	}
 	
 	public void setRemovePracButtonEnabled(boolean b, RoomPanel rp) {
 		this.rp = rp;
 		removePracButton.setEnabled(b);
 	}
 	
 	public void setPatButtonEnabled(boolean b, AppointmentBlock a) {
 		ab = a;
 		patientButton.setEnabled(b);
 		if (b) {
 			if (ab.getAppointment().getPatientID() != null)
 				patientButton.setAction(removePatAction);
 			else {
 				patientButton.setAction(addPatAction);
 			}
 		}
 		
 	}
 
 	public void setSidePanel(SidePanel sidePanel) {
 		this.sidePanel = sidePanel;
 	}
 	
 	private void setTimeSlot(TimeSlot timeSlot) {
 		if (timeSlot == null) return;
 		DataServiceImpl.GLOBAL_DATA_INSTANCE.setHoursForDay(day, timeSlot.getStartTime(), timeSlot.getEndTime());
 		day.setEnd(timeSlot.getEndTime());
 		day.setStart(timeSlot.getStartTime());
 		sidePanel.refreshTimeSlot(timeSlot);
		System.out.println("RESFRESHINGDFJSKDLFJDKSLFJ\n");
		// Refresh the UI
 	}
 	
 	public void clearRoom(RoomPanel panel) {
 		
 		if (JOptionPane.showConfirmDialog(mw, "Are you sure you want to remove this practitioner from the schedule? \nThis will cancel any appointments that have been set for this day.", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 			if (panel == null) return;
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.removePractitionerFromDay
 				(panel.getRoom().getPractSchedID(), day);
 			as.removeRoom(panel.getRoom());
 		}
 	}
 	
 	//Following method is added by Aakash
 	public void isMonthViewValidate(){
 		if (mw.inMonthView()) switchViewButton.setText("<html>Switch to <br>Day View</html>");
 		else switchViewButton.setText("<html>Switch to <br>Month View</html>");
 	}
 	
 	private final AbstractAction removePracAction = new AbstractAction("<html>Remove Practitioner<br>From Current Day</html>") {
 		public void actionPerformed(ActionEvent e) {
 			
 			clearRoom(rp);
 		}
 	};
 	
 	public final AbstractAction switchViewAction = new AbstractAction("<html>Switch to Month View</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (mw.inMonthView()) switchViewButton.setText("<html>Switch to <br>Month View</html>");
 			else switchViewButton.setText("<html>Switch to <br>Day View</html>");
 			mw.switchView();
 		}
 	};
 	
 	private final AbstractAction removePatAction = new AbstractAction("<html>Cancel Appointment</html>") {
 		public void actionPerformed(ActionEvent e) {
 			AppointmentBlock block = ab;
 			if (JOptionPane.showConfirmDialog(mw, "Are you sure you'd like to cancel this appointment?", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) block.clearAppt();
 		}
 	};
 	
 	private final AbstractAction addPatAction = new AbstractAction("<html>Schedule Patient</html>") {
 		public void actionPerformed(ActionEvent e) {
 			ab.setPatient(SelectPatientUI.ShowDialog(ab.getParent()).getPatID());
 		}
 	};
 	
 	private final AbstractAction addPracAction = new AbstractAction("<html>Schedule Practitioner</html>") {
 		public void actionPerformed(ActionEvent e) {
 			PractitionerDto p = SelectPractitionerUI.ShowDialog(mw);
 			if (p==null) return;
 			SchedulePractitionerDto room = DataServiceImpl.GLOBAL_DATA_INSTANCE.addPractitionerToDay(
 					p, day, day.getStart(), day.getEnd());
                         if (room != null){
                             as.addRoom(room);
                         }
                         else{
                             System.out.println("null");
                         }
 		}
 	};
 	
 	//private final AbstractAction changeTimeSlotAction = new AbstractAction("<html>Change <br>Hours of <br>Operation</html>") {
 	private final AbstractAction changeTimeSlotAction = new AbstractAction("<html>Hours of Operation</html>") {
 		public void actionPerformed(ActionEvent e) {
 			setTimeSlot(SelectTimeSlotUI.ShowDialog(mw));
 		}
 	};
 	
 	// TODO: Add search button functionality
 	private final AbstractAction searchAction = new AbstractAction("<html>Search</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (mw.showingSearch()) {
 				searchButton.setText("<html>Search</html>");
 			} else {
 				searchButton.setText("<html>Hide Search</html>");
 				apptConfirmationButton.setText("<html>Appointment <br> Confirmation</html>");
 				waitListButton.setText("<html>Wait List</html>");
 			}
 			mw.toggleSearch();
 		}
 	};
 	
 	// TODO: Add no shows functionality
 	private final AbstractAction apptConfirmationAction = new AbstractAction("<html>Appointment <br> Confirmation</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (mw.showingApptConfirmation()) {
 				apptConfirmationButton.setText("<html>Appointment <br> Confirmation</html>");
 			} else {
 				apptConfirmationButton.setText("<html>Hide Appointment <br> Confirmation</html>");
 				searchButton.setText("<html>Search</html>");
 				waitListButton.setText("<html>Wait List</html>");
 			}
 			mw.toggleApptConfirmation();
 		}
 	};
 	
 	private final AbstractAction waitListAction = new AbstractAction("<html>Wait List</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (mw.showingWaitList()) {
 				waitListButton.setText("<html>Wait List</html>");
 			}
 			else {
 				waitListButton.setText("<html>Hide Wait List</html>");
 				searchButton.setText("<html>Search</html>");
 				apptConfirmationButton.setText("<html>Appointment <br> Confirmation</html>");
 			}
 			mw.toggleWaitList();
 		}
 	};
 	
 	public void showingWaitList() {
 		if (mw.showingWaitList()) waitListButton.setText("<html>Hide Wait List</html>");
 	}
 	
 }
