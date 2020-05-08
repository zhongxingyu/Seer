 package gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import org.jdesktop.swingx.JXDatePicker;
 
 import sun.security.krb5.internal.CredentialsUtil;
 
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 
 import core.CalendarProgram;
 import core.alarm.AlarmHandler;
 
 import db.Appointment;
 import db.MeetingPoint;
 import db.User;
 
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import javax.swing.JTextArea;
 import javax.swing.JCheckBox;
 import javax.swing.JButton;
 import java.awt.Color;
 import javax.swing.JComboBox;
 
 @SuppressWarnings("serial")
 public class AddAppointmentPanel extends JPanel implements ActionListener {
 
 	protected Appointment appointment;
 	protected JXDatePicker startPick;
 	protected JXDatePicker endPick;
 	protected JTextArea descriptionArea;
 	protected JTextField titleField;
 	protected JTextField startField;
 	protected JTextField endField;
 	protected JCheckBox meetingBox;
 	protected JButton addAppButton;
 	protected JButton cancelButton;
 	protected boolean approved = false;
 	protected CalendarProgram cp;
 	protected JCheckBox alarmBox;
 	protected JTextField alarmValueField;
 	protected JComboBox valueTypePick;
 	protected JLabel beforeStartLabel;
 	protected MeetingPanel meetingPanel;
 	
 	/**
 	 * Create the panel.
 	 */
 	public AddAppointmentPanel(CalendarProgram cp, Appointment appointment) {
 		
 		//creates a default appointment object based on today, with unique id
 		this.appointment = appointment;
 		//reference to the main program
 		this.cp=cp;
 		
 		//GridBagLayout
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] {0, 30, 136, -6};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0, 1.0};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		//Pick start
 		startPick = new JXDatePicker();
 		startPick.addActionListener(this);
 		startPick.setDate(new Date());
 		
 		//title label
 		JLabel titleLabel = new JLabel("Title");
 		GridBagConstraints gbc_titleLabel = new GridBagConstraints();
 		gbc_titleLabel.insets = new Insets(0, 0, 5, 5);
 		gbc_titleLabel.gridx = 0;
 		gbc_titleLabel.gridy = 1;
 		add(titleLabel, gbc_titleLabel);
 		
 		//title field
 		titleField = new JTextField();
 		titleField.addActionListener(this);
 		titleField.setActionCommand("Title");
 		GridBagConstraints gbc_titleField = new GridBagConstraints();
 		gbc_titleField.insets = new Insets(0, 0, 5, 5);
 		gbc_titleField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_titleField.gridx = 2;
 		gbc_titleField.gridy = 1;
 		add(titleField, gbc_titleField);
 		titleField.setColumns(10);
 		
 		//start label
 		JLabel startLabel = new JLabel("Start");
 		startLabel.setVerticalAlignment(SwingConstants.TOP);
 		GridBagConstraints gbc_startLabel = new GridBagConstraints();
 		gbc_startLabel.insets = new Insets(0, 0, 5, 5);
 		gbc_startLabel.gridx = 0;
 		gbc_startLabel.gridy = 2;
 		add(startLabel, gbc_startLabel);
 		GridBagConstraints gbc_startPick = new GridBagConstraints();
 		gbc_startPick.fill = GridBagConstraints.HORIZONTAL;
 		gbc_startPick.insets = new Insets(0, 0, 5, 5);
 		gbc_startPick.gridx = 2;
 		gbc_startPick.gridy = 2;
 		add(startPick, gbc_startPick);
 		
 		//pick end
 		endPick = new JXDatePicker();
 		endPick.addActionListener(this);
 		endPick.setDate(new Date());
 		
 		//set start time
 		startField = new JTextField();
 		startField.setToolTipText("HH:MM");
 		//TODO format properly
 		startField.setText(appointment.getStart().get(Calendar.HOUR)+":"+appointment.getStart().get(Calendar.MINUTE));
 		startField.addActionListener(this);
 		startField.setActionCommand("Start time");
 		GridBagConstraints gbc_startField = new GridBagConstraints();
 		gbc_startField.anchor = GridBagConstraints.WEST;
 		gbc_startField.insets = new Insets(0, 0, 5, 0);
 		gbc_startField.gridx = 3;
 		gbc_startField.gridy = 2;
 		add(startField, gbc_startField);
 		startField.setColumns(6);
 		
 		//end label
 		JLabel endLabel = new JLabel("End");
 		endLabel.setVerticalAlignment(SwingConstants.TOP);
 		GridBagConstraints gbc_endLabel = new GridBagConstraints();
 		gbc_endLabel.insets = new Insets(0, 0, 5, 5);
 		gbc_endLabel.gridx = 0;
 		gbc_endLabel.gridy = 3;
 		add(endLabel, gbc_endLabel);
 		GridBagConstraints gbc_endPick = new GridBagConstraints();
 		gbc_endPick.insets = new Insets(0, 0, 5, 5);
 		gbc_endPick.fill = GridBagConstraints.HORIZONTAL;
 		gbc_endPick.gridx = 2;
 		gbc_endPick.gridy = 3;
 		add(endPick, gbc_endPick);
 		
 		//set end time
 		endField = new JTextField();
 		endField.setToolTipText("HH:MM");
 		//TODO format properly
 		endField.setText(appointment.getEnd().get(Calendar.HOUR)+":"+appointment.getEnd().get(Calendar.MINUTE));
 		startField.addActionListener(this);
 		startField.setActionCommand("End time");
 		GridBagConstraints gbc_endField = new GridBagConstraints();
 		gbc_endField.anchor = GridBagConstraints.WEST;
 		gbc_endField.insets = new Insets(0, 0, 5, 0);
 		gbc_endField.gridx = 3;
 		gbc_endField.gridy = 3;
 		add(endField, gbc_endField);
 		endField.setColumns(6);
 		
 		//description label
 		JLabel descriptionLabel = new JLabel("Description");
 		GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
 		gbc_descriptionLabel.gridwidth = 2;
 		gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
 		gbc_descriptionLabel.gridx = 0;
 		gbc_descriptionLabel.gridy = 4;
 		add(descriptionLabel, gbc_descriptionLabel);
 		
 		//description textarea
 		descriptionArea = new JTextArea();
 		startField.setActionCommand("Start time");
 		descriptionArea.setColumns(35);
 		descriptionArea.setRows(4);
 		GridBagConstraints gbc_descriptionArea = new GridBagConstraints();
 		gbc_descriptionArea.insets = new Insets(0, 0, 5, 5);
 		gbc_descriptionArea.gridwidth = 3;
 		gbc_descriptionArea.fill = GridBagConstraints.BOTH;
 		gbc_descriptionArea.gridx = 0;
 		gbc_descriptionArea.gridy = 5;
 		add(descriptionArea, gbc_descriptionArea);
 		
 		//add appointment
 		addAppButton = new JButton("Add Appointment");
 		addAppButton.addActionListener(this);
 		
 		//checkbox for alarms
 		alarmBox = new JCheckBox("Alarm");
 		alarmBox.addActionListener(this);
 		alarmBox.setActionCommand("alarm");
 		GridBagConstraints gbc_alarmBox = new GridBagConstraints();
 		gbc_alarmBox.insets = new Insets(0, 0, 5, 5);
 		gbc_alarmBox.gridx = 0;
 		gbc_alarmBox.gridy = 6;
 		add(alarmBox, gbc_alarmBox);
 		
 		//field for the value you want
 		alarmValueField = new JTextField();
 		alarmValueField.setText("10");
 		alarmValueField.setVisible(false);
 		
 		beforeStartLabel = new JLabel("Before Start");
 		beforeStartLabel.setVisible(false);
 		GridBagConstraints gbc_beforeStartLabel = new GridBagConstraints();
 		gbc_beforeStartLabel.insets = new Insets(0, 0, 5, 5);
 		gbc_beforeStartLabel.gridx = 2;
 		gbc_beforeStartLabel.gridy = 6;
 		add(beforeStartLabel, gbc_beforeStartLabel);
 		GridBagConstraints gbc_alarmValueField = new GridBagConstraints();
 		gbc_alarmValueField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_alarmValueField.insets = new Insets(0, 0, 5, 5);
 		gbc_alarmValueField.gridx = 0;
 		gbc_alarmValueField.gridy = 7;
 		add(alarmValueField, gbc_alarmValueField);
 		alarmValueField.setColumns(3);
 		
 		//pick what type the value should representent e.g. hours, minutes days.
 		String[] valueTypes = {"Minute", "Hour", "Day"};
 		valueTypePick = new JComboBox(valueTypes);
 		valueTypePick.setVisible(false);
 		GridBagConstraints gbc_valueTypePick = new GridBagConstraints();
 		gbc_valueTypePick.anchor = GridBagConstraints.WEST;
 		gbc_valueTypePick.insets = new Insets(0, 0, 5, 5);
 		gbc_valueTypePick.gridx = 2;
 		gbc_valueTypePick.gridy = 7;
 		add(valueTypePick, gbc_valueTypePick);
 		
 		
 		//checkbox for the meetings
 		meetingBox = new JCheckBox("Meeting");
 		meetingBox.addActionListener(this);
 		meetingBox.setActionCommand("Meeting");
 		
 		GridBagConstraints gbc_meetingBox = new GridBagConstraints();
 		gbc_meetingBox.insets = new Insets(0, 0, 5, 5);
 		gbc_meetingBox.gridx = 0;
 		gbc_meetingBox.gridy = 8;
 		add(meetingBox, gbc_meetingBox);
 		
 		//Add and hide meetingPanel
 		meetingPanel = new MeetingPanel();
 		GridBagConstraints gbc_meetingPanel = new GridBagConstraints();
 		gbc_meetingPanel.gridwidth = 3;
 		gbc_meetingPanel.insets = new Insets(0, 0, 5, 5);
 		gbc_meetingPanel.fill = GridBagConstraints.BOTH;
 		gbc_meetingPanel.gridx = 0;
 		gbc_meetingPanel.gridy = 9;
 		add(meetingPanel, gbc_meetingPanel);
 		meetingPanel.setVisible(false);
 		
 		
 		//add appointment
 		addAppButton.setActionCommand("Add");
 		GridBagConstraints gbc_addAppButton = new GridBagConstraints();
 		gbc_addAppButton.insets = new Insets(0, 0, 0, 5);
 		gbc_addAppButton.gridx = 0;
 		gbc_addAppButton.gridy = 10;
 		add(addAppButton, gbc_addAppButton);
 		
 		//cancel appointment
 		cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(this);
 		cancelButton.setActionCommand("Cancel");
 		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
 		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
 		gbc_cancelButton.gridx = 2;
 		gbc_cancelButton.gridy = 10;
 		add(cancelButton, gbc_cancelButton);
 		
 	}
 
 	private User getUser() {
 		return cp.getUser();
 	}
 
 
 	
 	public void actionPerformed(ActionEvent event) {
 //		System.out.println(event.getActionCommand());
 
 		//add alarm options
 		if(event.getActionCommand().equals("alarm")){
 				boolean bool = alarmBox.isSelected();
 				alarmValueField.setVisible(bool);
 				valueTypePick.setVisible(bool);
 				beforeStartLabel.setVisible(bool);
 				//if checkbox gets unchecked it sets alarmfield to null
 				if(!bool)
 					setAlarm(bool);
 		}
 			
 		
 		//add meeting options
 		if(event.getActionCommand().equals("Meeting")){
 			meetingPanel.setVisible(meetingBox.isSelected());
 		}
 		
 		//add Appointment
 		if(event.getActionCommand().equals("Add")){
 			
 			//start with a fresh instance
 //			appointment = new Appointment(getUser());
 			
 			//set title
 			appointment.setTitle(titleField.getText());
 			
 			//set dates
 			appointment.setStart(startPick.getDate());
 			appointment.setEnd(endPick.getDate());
 			if(appointment.getStart()==null || appointment.getEnd()==null){
 				approved=false;
 				JOptionPane.showMessageDialog(this, "You must pick dates.","Date error",JOptionPane.ERROR_MESSAGE);
 			} else approved=true;
 			
 			//check format and set time
 			if(approved)
 				appointment.setStart(addTime(startField.getText(), appointment.getStart()));
 			if(approved)
 				appointment.setEnd(addTime(endField.getText(), appointment.getEnd()));
 			
 			//check that start is before end
 			if(approved)
 				checkStartVsEndTime();
 			
 			//set description
 			if(approved)
 				appointment.setDescription(descriptionArea.getText());
 			
 			//set alarm
 			if(alarmBox.isSelected())
 				setAlarm(true);
 			
 			//meeting
 			if(meetingBox.isSelected()){
 				appointment.setMeeting(true);
				appointment.setMeetingPoint((MeetingPoint) meetingPanel.comboBox.getSelectedItem());
 				for(int i = 0; i < meetingPanel.plp.getParticipantList().size(); i++){
 					appointment.addParticipant(meetingPanel.plp.getParticipantList().get(i));
 				}
 			}
 			
 			//if approved
 			if(approved)
 				cp.addAppointment(appointment);
 			
 			//debug
 			System.out.println(appointment);
 		}
 		
 		//cancel the appointment
 		if(event.getActionCommand().equals("Cancel"))
 			cp.displayMainProgram();
 		
 	}
 	
 	private void setAlarm(boolean bool) {
 		if(bool){
 			appointment.setAlarm(getAlarmValue());
 		}
 		else
 			appointment.setAlarm(null);
 	}
 	
 	private GregorianCalendar getAlarmValue() {
 		String type = (String) valueTypePick.getSelectedItem();
 		GregorianCalendar alarm = (GregorianCalendar) appointment.getStart().clone();
 		int value = Integer.parseInt(alarmValueField.getText());
 		if(value<0){
 			JOptionPane.showMessageDialog(this, "No negative numbers i alarmfield","Alarm Error",JOptionPane.ERROR_MESSAGE);
 			approved=false;
 			return appointment.getAlarm().getAlarmTime();
 		}
 		if(type.equals("Minute"))
 			alarm.set(Calendar.MINUTE,alarm.get(Calendar.MINUTE) - value);
 		else if(type.equals("Hour"))
 			alarm.set(Calendar.HOUR,alarm.get(Calendar.HOUR) - value);
 		else
 			alarm.set(Calendar.DATE,alarm.get(Calendar.DATE) - value);
 		return alarm;
 		
 	}
 
 
 	public boolean hasAlarm(){
 		return appointment.getAlarm() != null;
 	}
 
 
 	private void checkStartVsEndTime() {
 		if(appointment.getEnd().before(appointment.getStart())){
 			JOptionPane.showMessageDialog(this, "End-date is after start-date","Date error",JOptionPane.ERROR_MESSAGE);
 			approved=false;
 		}
 		approved=true;
 	}
 
 
 	private GregorianCalendar addTime(String text, GregorianCalendar date) {
 		String[] time = text.split(":");
 		int hours;
 		int mins;
 		
 		try{
 			checkTimeFormat(time);
 			hours = Integer.parseInt(time[0]);
 			mins = Integer.parseInt(time[1]);
 			if(hours>23||hours<0 || mins >60 || mins<0)
 				throw new IllegalArgumentException();
 			date.set(GregorianCalendar.HOUR,hours);
 			date.set(GregorianCalendar.MINUTE,mins);
 			approved=true;
 		}catch(Exception ex){
 			JOptionPane.showMessageDialog(this, "Wrong time-format","Time Error",JOptionPane.ERROR_MESSAGE);
 			approved=false;
 		}
 		return date;
 	}
 
 
 	private void checkTimeFormat(String[] text) throws IllegalArgumentException{
 		if(text.length!=2)
 			throw new IllegalArgumentException();
 		if(text[1].length()!=2 && text[0].length()!=2)
 			throw new IllegalArgumentException();
 	}
 
 
 	public static void main(String[] args){
 		JFrame frame = new JFrame();
 		frame.getContentPane().add(new AddAppointmentPanel(new CalendarProgram(),new Appointment(new User())));
 		frame.pack();
         frame.setSize (800,500);
         frame.setVisible(true);
 	}
 	
 
 }
