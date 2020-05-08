 package gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import org.jdesktop.swingx.JXDatePicker;
 
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 import com.jgoodies.forms.factories.DefaultComponentFactory;
 
 import core.CalendarProgram;
 
 import db.Appointment;
 import db.User;
 
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import javax.swing.JTextArea;
 import javax.swing.JCheckBox;
 import javax.swing.JButton;
 import java.awt.Color;
 
 @SuppressWarnings("serial")
 public class AddAppointmentPanel extends JPanel implements ActionListener {
 
 	private Appointment appointment;
 	private JXDatePicker startPick;
 	private JXDatePicker endPick;
 	private JTextArea descriptionArea;
 	private JTextField titleField;
 	private JTextField startField;
 	private JTextField endField;
 	private JCheckBox meetingBox;
 	private JButton addAppButton;
 	private JButton cancelButton;
 	private boolean approved = false;
 	private CalendarProgram cp;
 	
 	/**
 	 * Create the panel.
 	 */
 	public AddAppointmentPanel(CalendarProgram cp) {
 		
 		//creates a default appiontment object based on today, with uniqe id
 		appointment = createAppointment();
 		//reference to the main program
 		this.cp=cp;
 		
 		//GridBagLayout
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] {0, 30, 189, 39};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		//Pick start
 		startPick = new JXDatePicker();
 		startPick.addActionListener(this);
 		
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
 		JLabel startLabel = DefaultComponentFactory.getInstance().createLabel("Start");
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
 		
 		//set start time
 		startField = new JTextField();
 		startField.setToolTipText("HH:MM");
 		startField.setText("--:--");
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
 		JLabel endLabel = DefaultComponentFactory.getInstance().createLabel("End");
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
 		endField.setText("--:--");
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
 		
 		//checkbox for the meetings
 		JCheckBox meetingBox = new JCheckBox("Meeting");
 		meetingBox.addActionListener(this);
 		meetingBox.setActionCommand("Meeting");
 		GridBagConstraints gbc_meetingBox = new GridBagConstraints();
 		gbc_meetingBox.insets = new Insets(0, 0, 5, 5);
 		gbc_meetingBox.gridx = 0;
 		gbc_meetingBox.gridy = 6;
 		add(meetingBox, gbc_meetingBox);
 		
 		//add appointment
 		addAppButton = new JButton("Add Appointment");
 		addAppButton.addActionListener(this);
 		addAppButton.setActionCommand("Add");
 		GridBagConstraints gbc_addAppButton = new GridBagConstraints();
 		gbc_addAppButton.insets = new Insets(0, 0, 0, 5);
 		gbc_addAppButton.gridx = 0;
 		gbc_addAppButton.gridy = 8;
 		add(addAppButton, gbc_addAppButton);
 		
 		//cancel appointment
 		cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(this);
 		cancelButton.setActionCommand("Cancel");
 		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
 		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
 		gbc_cancelButton.gridx = 2;
 		gbc_cancelButton.gridy = 8;
 		add(cancelButton, gbc_cancelButton);
 		
 	}
 
 
 	@SuppressWarnings("deprecation")
 	private Appointment createAppointment() {
 		Date today = new Date();
 		//if unique id is needed we can use theese
 //		int intID = (int)today.getTime();
 //		long longID = today.getTime();
 		
 		//unique for date+hours+minutes
 		String id = ""+today.getDate()+today.getHours()+today.getMinutes();
 		int creatorUserId = getUser().getId(); creatorUserId=0; // TODO fix how the get the user
 		
 		return new Appointment(Integer.parseInt(id), creatorUserId, "", today, today, "", false);
 	}
 
 
 	private User getUser() {
		// TODO Auto-generated method stub
 		return new User();
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 //		System.out.println(event.getActionCommand());
 		
 		//date picking start and end
 //		if(event.getActionCommand().equals("datePickerCommit")){
 //			JXDatePicker picker = (JXDatePicker) event.getSource();
 //			Date date = picker.getDate();
 //			if(event.getSource()==endPick){
 //				System.out.println("end");
 //				appointment.setEnd(date);
 //			}
 //			if(event.getSource()==startPick){
 //				System.out.println("start");
 //				appointment.setStart(date);
 //			}
 //		}
 		
 		//set title
 //		if(event.getActionCommand().equals("Title"))
 //			appointment.setTitle(titleField.getText());
 		
 		// TODO: check that time is correct format
 		//set start time
 //		if(event.getActionCommand().equals("Start time")){
 //			Date start = appointment.getStart();
 //			String[] time = startField.getText().split(":");
 //			int hours = Integer.parseInt(time[0]);
 //			int mins = Integer.parseInt(time[1]);
 //			start.setHours(hours);
 //			start.setMinutes(mins);
 //		}
 		//set end time
 //		if(event.getActionCommand().equals("End time")){
 //			Date end = appointment.getStart();
 //			String[] time = endField.getText().split(":");
 //			int hours = Integer.parseInt(time[0]);
 //			int mins = Integer.parseInt(time[1]);
 //			end.setHours(hours);
 //			end.setMinutes(mins);
 //		}
 		
 		//add meeting options
 		if(event.getActionCommand().equals("Meeting"))
 			//TODO: make the other stuff appear and disappear
 			;
 		
 		//add Appointment
 		if(event.getActionCommand().equals("Add")){
 			
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
 			
 			//check that start if before end
 			if(approved)
 				checkStartVsEndTime();
 			
 			//set description
 			if(approved)
 				appointment.setDescription(descriptionArea.getText());
 			
 			//debug
 			System.out.println(appointment);
 		}
 		
 		//cancel the appointment
 		if(event.getActionCommand().equals("Cancel"))
 			//TODO: exit the window without doing nothing, maybe an "are you  sure, you will loose all info" pop up
 			;
 		
 	}
 	
 	private void checkStartVsEndTime() {
 		if(appointment.getEnd().before(appointment.getStart())){
 			JOptionPane.showMessageDialog(this, "End-date is after start-date","Date error",JOptionPane.ERROR_MESSAGE);
 			approved=false;
 		}
 		approved=true;
 	}
 
 
 	private Date addTime(String text, Date date) {
 		String[] time = text.split(":");
 		int hours;
 		int mins;
 		
 		try{
 			checkTimeFormat(time);
 			hours = Integer.parseInt(time[0]);
 			mins = Integer.parseInt(time[1]);
 			if(hours>23||hours<0 || mins >60 || mins<0)
 				throw new IllegalArgumentException();
 			date.setHours(hours);
 			date.setMinutes(mins);
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
 		frame.getContentPane().add(new AddAppointmentPanel(new CalendarProgram()));
 		frame.pack();
         frame.setSize (800,300);
         frame.setVisible(true);
 	}
 	
 
 }
