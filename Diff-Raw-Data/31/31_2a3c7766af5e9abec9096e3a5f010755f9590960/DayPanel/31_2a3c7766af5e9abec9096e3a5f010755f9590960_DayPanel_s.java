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
 
 import data.Day;
 import data.DaySaver;
 import data.Practitioner;
 import data.Room;
 import data.TimeSlot;
 
 public class DayPanel extends JPanel {
 	private Day day;	
 	private JButton patientButton = new PanelButton("Schedule Patient");
 	private JButton addPracButton = new PanelButton("Add Practitioner");
 	private JButton removePracButton = new PanelButton("Remove Practitioner");
 	private JButton setTimeSlotButton = new PanelButton("Time of Operation");
 	private JButton switchViewButton = new PanelButton("Month View");
 	private JButton waitListButton = new PanelButton("Wait List");
 	
 	
 	private RoomPanel rp;
 	private AppointmentBlock ab;
 	private AppointmentSubpanel as;
 	private MainWindow mw;
 	
 	public DayPanel(Day day, MainWindow mw) {
 		this.mw = mw;
 		setBackground(Color.WHITE);
 		this.day = day;
 		
 		patientButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		addPracButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		removePracButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		setTimeSlotButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		switchViewButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		waitListButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		
 		
 		//We don't want these focusable, so they won't disrupt the focus of the main calendar area
 		patientButton.setAction(addPatAction);
 		patientButton.setFocusable(false);
 		patientButton.setEnabled(false);
 		addPracButton.setFocusable(false);
 		addPracButton.setAction(addPracAction);
 		removePracButton.setAction(removePracAction);
 		removePracButton.setFocusable(false);
 		removePracButton.setEnabled(false);
 		setTimeSlotButton.setAction(changeTimeSlotAction);
 		setTimeSlotButton.setFocusable(false);
 		switchViewButton.setAction(switchViewAction);
 		switchViewButton.setFocusable(false);
 		waitListButton.setAction(waitListAction);
 		waitListButton.setFocusable(true);
 		
 		JPanel buttonPanel = new JPanel(new GridLayout(0,1));
 		
 		buttonPanel.add(addPracButton);
 		buttonPanel.add(removePracButton);
 		buttonPanel.add(setTimeSlotButton);
 		buttonPanel.add(patientButton);
 		buttonPanel.add(switchViewButton);
 		buttonPanel.add(waitListButton);
 		add(buttonPanel);
 	}
 	
 	public Day getDay() {
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
 			if (ab.getAppointment().isFilled())
 				patientButton.setAction(removePatAction);
 			else
 				patientButton.setAction(addPatAction);
 		}
 		
 	}
 	
 	private void setTimeSlot(TimeSlot timeSlot) {
 		if (timeSlot == null) return;
 		Day newDay = new Day(timeSlot, day.getDate());
 		for (Room r : day.getRooms()) {
 			if (r.hasPrac()) newDay.addRoom(r.getPractitioner());
 		}
 		mw.setDay(newDay);
 		mw.setDate(day.getDate());
 	}
 	
 	public void clearRoom(RoomPanel panel) {
 		
 		if (JOptionPane.showConfirmDialog(mw, "Are you sure you want to remove this practitioner? This will clear any appointments that have been set.", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 			if (panel == null) return;
 			panel.setPractitioner(null);
 			as.removeRoom(panel.getRoom());
 			day.removeRoom(panel.getRoom());
 			new DaySaver().storeDay(day);
 		}
 	}
 	
 	//Following method is added by Aakash
 	public void isMonthViewValidate(){
 		if (mw.inMonthView()) switchViewButton.setText("<html>Switch to <br>Day View</html>");
 		else switchViewButton.setText("<html>Switch to <br>Month View</html>");
 	}
 	
 	private final AbstractAction removePracAction = new AbstractAction("<html>Remove Practitioner</html>") {
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
 	
 	private final AbstractAction removePatAction = new AbstractAction("<html>Remove Patient</html>") {
 		public void actionPerformed(ActionEvent e) {
 			AppointmentBlock block = ab;
 			if (JOptionPane.showConfirmDialog(mw, "Are you sure you'd like to clear this appointment?", "Please Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) block.clearAppt();
 		}
 	};
 	
 	private final AbstractAction addPatAction = new AbstractAction("<html>Schedule Patient</html>") {
 		public void actionPerformed(ActionEvent e) {
 			ab.setPatient(SelectPatientUI.ShowDialog(ab.getParent()));
 		}
 	};
 	
 	private final AbstractAction addPracAction = new AbstractAction("<html>Add Practitioner</html>") {
 		public void actionPerformed(ActionEvent e) {
 			Practitioner p = SelectPractitionerUI.ShowDialog(mw);
 			if (p==null) return;
 			as.addRoom(day.addRoom(p));
 			new DaySaver().storeDay(day);
 		}
 	};
 	
 	//private final AbstractAction changeTimeSlotAction = new AbstractAction("<html>Change <br>Hours of <br>Operation</html>") {
 	private final AbstractAction changeTimeSlotAction = new AbstractAction("<html>Change Work Hours<br>(for Selected Day)</html>") {
 		public void actionPerformed(ActionEvent e) {
 			setTimeSlot(SelectTimeSlotUI.ShowDialog(mw));
 		}
 	};
 	
 	private final AbstractAction waitListAction = new AbstractAction("<html>Show Wait List</html>") {
 		public void actionPerformed(ActionEvent e) {
			if (mw.showingWaitList()) waitListButton.setText("<html>Show <br>Wait List</html>");
			else waitListButton.setText("<html>Hide <br>Wait List</html>");
 			mw.toggleWaitList();
 		}
 	};
 	
 	public void showingWaitList() {
		if (mw.showingWaitList()) waitListButton.setText("<html>Hide <br>Wait List</html>");
 	}
 	
 }
