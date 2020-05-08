 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.Color;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerDateModel;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.TimeZone;
 import java.text.SimpleDateFormat;
 import javax.swing.JPanel;
 import org.jdesktop.swingx.JXDatePicker;
 
 /**
  * iCalendarGUI for Team Cocoa's ICS 314 Project.
  * 
 * FFunctions
  * -iCalendarGUI()						|	iCalendarGUI constructor.
  * -initialize()						|	Initializes the fields of the GUI.
  * -focusGained(FocusEvent event)		|	Performs actions when focus is gained.
  * -focusLost(FocusEvent event)			|	Performs actions when focus field is lost.
  * -highlight(Color color, String type)	|	Highlights field with given color.
  * -actionPerformed(ActionEvent event)	|	Responds to actions of user.
  * -generate()							|	Generates the GUI for the .ics FileMaker.
  *
  * 8/10/2013
  */
 
 public class iCalendarGUI extends javax.swing.JPanel implements ActionListener,
 		FocusListener {
 
 	/* all the buttons, fields, labels */
 	private final JLabel statusMessage = new JLabel();
 	private final JLabel exceptionMessage = new JLabel();
 	private final JTextField descriptionField = new JTextField();
 	private final JTextField locationField = new JTextField();
 	private final JTextField eventField = new JTextField();
 	private final JTextField commentField = new JTextField();
 	private final JButton generateButton = new JButton();
 	private final JButton clearAllButton = new JButton();
 	private final JButton addExceptionButton = new JButton();
 	private final JLabel eventLabel = new JLabel();
 	private final JLabel descriptionLabel = new JLabel();;
 	private final JLabel endTimeLabel = new JLabel();
 	private final JLabel locationLabel = new JLabel();
 	private final JLabel startTimeLabel = new JLabel();
 	private final JLabel startTimeDateLabel = new JLabel();
 	private final JLabel endTimeDateLabel = new JLabel();
 	private final JLabel startTimeTimeLabel = new JLabel();
 	private final JLabel endTimeTimeLabel = new JLabel();
 	private final JLabel recurringLabel = new JLabel();
 	private final JLabel repeatLabel = new JLabel();
 	private final JLabel untilLabel = new JLabel();
 	private final JLabel exceptionDatesLabel = new JLabel();
     private final JLabel commentLabel = new JLabel();
     private final JLabel classLabel = new JLabel();
 	private final JPanel startTimeDateField = new JPanel();
 	private final JPanel endTimeDateField = new JPanel();
 	private final JPanel startTimeTimeField = new JPanel();
 	private final JPanel endTimeTimeField = new JPanel();
 	private final JPanel exceptionDatesField = new JPanel();
 	private final JPanel untilField = new JPanel();
 	private final JCheckBox recurringCheckBox = new JCheckBox("", false);
 	private JComboBox<String> repeatDropDown = new JComboBox<String>();
     private JComboBox<String> classDropDown = new JComboBox<String>();
     private JXDatePicker sdPicker = new JXDatePicker();
     private JXDatePicker edPicker = new JXDatePicker();
     private JXDatePicker untilPicker = new JXDatePicker();
     private JXDatePicker exPicker = new JXDatePicker();
     private SpinnerDateModel sModel = new SpinnerDateModel();
     private SpinnerDateModel eModel = new SpinnerDateModel();
     private JSpinner startSpinner = new JSpinner();
     private JSpinner endSpinner = new JSpinner();
     private ArrayList<Date> exceptionDates = new ArrayList<Date>();
     private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
 	private SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
 
 	/**
 	 * iCalendarGUI constructor.
 	 */
 	public iCalendarGUI() {
 		initialize();
 	}
 	
 	/**
 	 * Initializes the fields of the GUI.
 	 * @return True - Successful initialization of GUI.
 	 * 		   False - Failed initialization of GUI.
 	 */
 	private boolean initialize() {
 		try {
 			/* date picker */
 	        sdPicker.setDate(Calendar.getInstance().getTime());
 	        sdPicker.setFormats(new SimpleDateFormat("MM/dd/yyyy"));
 	        edPicker.setDate(Calendar.getInstance().getTime());
 	        edPicker.setFormats(new SimpleDateFormat("MM/dd/yyyy"));
 	        untilPicker.setDate(Calendar.getInstance().getTime());
 	        untilPicker.setFormats(new SimpleDateFormat("MM/dd/yyyy"));
 	        exPicker.setDate(Calendar.getInstance().getTime());
 	        exPicker.setFormats(new SimpleDateFormat("MM/dd/yyyy"));
 	        
 	        /* time picker */
 	        sModel.setCalendarField(Calendar.MINUTE);
 	        eModel.setCalendarField(Calendar.MINUTE);
 	        startSpinner.setModel(sModel);
 	        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "h:mm a"));
 	        endSpinner.setModel(eModel);
 	        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "h:mm a"));
 	        
 
 			/* add the labels, fields, buttons */
 			add(statusMessage);
 			add(exceptionMessage);
 			add(descriptionField);
 			add(locationField);
 			add(eventField);
 			add(generateButton);
 			add(clearAllButton);
 			add(addExceptionButton);
 			add(eventLabel);
 			add(descriptionLabel);
 			add(endTimeLabel);
 			add(locationLabel);
 			add(startTimeLabel);
 			add(startTimeDateLabel);
 			add(endTimeDateLabel);
 			add(startTimeTimeLabel);
 			add(endTimeTimeLabel);
 			add(startTimeDateField);
 			add(endTimeDateField);
 			add(startTimeTimeField);
 			add(endTimeTimeField);
 			add(recurringCheckBox);
 			add(recurringLabel);
 			add(repeatLabel);
 			add(untilLabel);
 			add(untilField);
 			add(repeatDropDown);
 			add(exceptionDatesField);
 			add(exceptionDatesLabel);
             add(commentLabel);
             add(classLabel);
             add(classDropDown);
             add(commentField);
             
 			/* set initial text */
 			statusMessage.setFont(new java.awt.Font("Verdana", 1, 12));
 			statusMessage.setText("Welcome to team Cocoa's .ICS generator!");
 			eventLabel.setText("Event");
 			locationLabel.setText("Location");
 			descriptionLabel.setText("Description");
 			descriptionField.setText("Describe the event");
 			commentLabel.setText("Comment");
 			commentField.setText("Insert comment");
 			classLabel.setText("Classification");
 			eventField.setText("What's happening?");
 			locationField.setText("Where at?");
 			startTimeLabel.setText("Start Time");
 			endTimeLabel.setText("End Time");
 			startTimeDateLabel.setText("Date");
 			endTimeDateLabel.setText("Date");
 			startTimeTimeLabel.setText("Time");
 			endTimeTimeLabel.setText("Time");
 			startTimeDateField.add(sdPicker);
 			endTimeDateField.add(edPicker);
 			startTimeTimeField.add(startSpinner);
 			endTimeTimeField.add(endSpinner);
 			untilField.add(untilPicker);
 			recurringLabel.setText("Will this be a recurring event?");
 			repeatLabel.setText("Repeat");
 			untilLabel.setText("Until");
 			generateButton.setText("Generate!");
 			clearAllButton.setText("Clear All");
 			addExceptionButton.setText("Add Exception");
 			repeatDropDown.addItem("Daily");
 			repeatDropDown.addItem("Weekly");
 			repeatDropDown.addItem("Monthly");
 			repeatDropDown.addItem("Yearly");
             classDropDown.addItem("Public");
 			classDropDown.addItem("Private");
 			classDropDown.addItem("Confidential");
 			exceptionDatesLabel.setText("Exception Dates");
 			exceptionDatesField.add(exPicker);
 
 			/* listeners */
 			eventField.addActionListener(this);
 			locationField.addActionListener(this);
 			descriptionField.addActionListener(this);
 			commentField.addActionListener(this);
 			clearAllButton.addActionListener(this);
 			generateButton.addActionListener(this);
 			addExceptionButton.addActionListener(this);
 			sdPicker.addActionListener(this);
 			edPicker.addActionListener(this);
 			exPicker.addActionListener(this);
 			untilPicker.addActionListener(this);
 			recurringCheckBox.addActionListener(this);
 			descriptionField.addFocusListener(this);
 			commentField.addFocusListener(this);
 			locationField.addFocusListener(this);
 			eventField.addFocusListener(this);
 			sdPicker.addFocusListener(this);
 			edPicker.addFocusListener(this);
 			startSpinner.addFocusListener(this);
 			endSpinner.addFocusListener(this);
 			untilPicker.addFocusListener(this);
 			exPicker.addFocusListener(this);
                         
 			/* layout for components */
 			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(startTimeDateLabel)
                     .addComponent(startTimeTimeLabel))
                 //here
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGap(154, 154, 154)
                         .addComponent(generateButton)
                         .addGap(29, 29, 29)
                         .addComponent(clearAllButton))
                     .addGroup(layout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(startTimeLabel)
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(startTimeDateField, javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE , javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addGap(102, 102, 102)
                                         .addComponent(endTimeDateLabel))
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(recurringLabel)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                         .addComponent(recurringCheckBox))
                                     .addGroup(layout.createSequentialGroup()
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                             .addComponent(repeatLabel)
                                             .addComponent(untilLabel)
                                             .addComponent(exceptionDatesLabel)
                                             .addComponent(addExceptionButton))
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 
                                          //after the check box label
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         	.addComponent(repeatDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addComponent(untilField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addComponent(exceptionDatesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                             )//here
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(startTimeTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addGap(148, 148, 148)
                                         .addComponent(endTimeTimeLabel)))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(endTimeLabel)
                                     .addComponent(endTimeDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(endTimeTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(eventLabel)
                                     .addComponent(locationLabel)
                                     .addComponent(descriptionLabel)
                                     .addComponent(commentLabel)
                                     .addComponent(classLabel))
                                 .addGap(24, 24, 24)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(eventField, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                                     .addComponent(locationField)
                                     .addComponent(descriptionField)
                                     .addComponent(commentField)
                                     .addComponent(classDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addGroup(layout.createSequentialGroup()
                 .addGap(175, 175, 175)
                 .addComponent(statusMessage)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         
         //vertical gaps
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(30, 30, 30)
                 .addComponent(statusMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(eventLabel)
                     .addComponent(eventField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(7, 7, 7)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(locationLabel)
                     .addComponent(locationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(descriptionLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(commentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(commentLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(classLabel)
                     .addComponent(classDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 
                 /*gap in between upper info and start time info*/
                 .addGap(24, 24, 24)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(startTimeLabel)
                     .addComponent(endTimeLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(startTimeDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(endTimeDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(startTimeDateLabel)
                     .addComponent(endTimeDateLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(startTimeTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(endTimeTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(startTimeTimeLabel)
                     .addComponent(endTimeTimeLabel))
                 .addGap(14, 14, 14)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(recurringLabel)
                     .addComponent(recurringCheckBox))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(repeatLabel)
                     .addComponent(repeatDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(untilLabel)
                     .addComponent(untilField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)//, 15, Short.MAX_VALUE)
                 
                 //exceptions group right before buttons
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(exceptionDatesLabel)
                     .addComponent(exceptionDatesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(addExceptionButton))
                 .addGap(30, 30, 30)
                 
                 //buttons group
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(generateButton)
                     .addComponent(clearAllButton))
                 .addContainerGap())
         );
             
 			untilPicker.setEnabled(false);
 			repeatDropDown.setEnabled(false);
 			exPicker.setEnabled(false);
 			classDropDown.setEnabled(true);
 			addExceptionButton.setEnabled(false);
             }catch(Exception e){
                 return false;
             }
             return true;
 	}
 	
 	/**
 	 * Performs actions when focus is gained.
 	 */
 	public void focusGained(FocusEvent event) {
 		if (event.getSource() == descriptionField) {
 			descriptionField.setText("");
 		}
 		if(event.getSource() == commentField) {
 			commentField.setText("");
 		}
 		if (event.getSource() == locationField) {
 			locationField.setText("");
 		}
 		if (event.getSource() == eventField) {
 			eventField.setText("");
 		}
 	}
 	
 	/**
 	 * Performs actions when focus field is lost.
 	 */
 	public void focusLost(FocusEvent event) {
 		if (event.getSource() == sdPicker) {
 			Color status = (ICSFormat.valid(sdf.format(sdPicker.getDate()),"00:00:00").equals(""))? 
 					Color.YELLOW: 
 					Color.GREEN;
 			startTimeDateField.setBackground(status);
 		}
 		if (event.getSource() == edPicker) {
 			Color status = (ICSFormat.valid(sdf.format(edPicker.getDate()),"00:00:00").equals(""))? 
 					Color.YELLOW: 
 					Color.GREEN;
 			endTimeDateField.setBackground(status);
 		}
 		if (event.getSource() == startTimeTimeField) {
 			Color status = (ICSFormat.valid("1/1/0001",stf.format(startSpinner.getValue())).equals(""))? 
 					Color.YELLOW: 
 					Color.GREEN;
 			startTimeTimeField.setBackground(status);
 		}
 		if (event.getSource() == endTimeTimeField) {
 			Color status = (ICSFormat.valid("1/1/0001",stf.format(endSpinner.getValue())).equals(""))? 
 					Color.YELLOW: 
 					Color.GREEN;
 			endTimeTimeField.setBackground(status);
 		}
 		if (event.getSource() == exceptionDatesField) {
 			Color status = (ICSFormat.valid(sdf.format(exPicker.getDate()),"00:00").equals(""))? 
 					Color.YELLOW: 
 					Color.GREEN;
 			exceptionDatesField.setBackground(status);
 		}
 		if (event.getSource() == untilField) {
 			Color status = (ICSFormat.valid(sdf.format(untilPicker.getDate()),"00:00").equals(""))? 
 					Color.YELLOW: 
 					Color.GREEN;
 			untilField.setBackground(status);
 		}
 	}
 	
 	/**
 	 * Highlights field with given color.
 	 * @param color - color to be used 
 	 * @param type - date or time
 	 */
 	public void highlight(Color color, String type) {
 		if (type.equals("date")) {
 			startTimeDateField.setBackground(color);
 			endTimeDateField.setBackground(color);
 		} else if (type.equals("time")) {
 			startTimeTimeField.setBackground(color);
 			endTimeTimeField.setBackground(color);
 		} else if(type.equals("until")) {
 			untilField.setBackground(color);
 		}
 	}
 
 	/**
 	 * Responds to actions of user.
 	 */
 	public void actionPerformed(ActionEvent event) {
 		try {
 			//recurring event actions.
 			if (recurringCheckBox.isSelected() == true) {
 				untilPicker.setEnabled(true);
 				repeatDropDown.setEnabled(true);
 				exPicker.setEnabled(true);
 				addExceptionButton.setEnabled(true);
 			} else {
 				untilPicker.setEnabled(false);
 				repeatDropDown.setEnabled(false);
 				exPicker.setEnabled(false);
 				addExceptionButton.setEnabled(false);
 			}
 			
 			if (event.getSource() == addExceptionButton) {
 				if (!exceptionDates.contains(exPicker.getDate())) {
 					exceptionDates.add(exPicker.getDate());
 					Collections.sort(exceptionDates);
 					statusMessage.setText("Exception date added");
 				}else {
 					statusMessage.setText("Date exists in exceptions");
 				}
 			}
 			
 			// Clears all fields including exception dates (convenient)
 			if (event.getSource() == clearAllButton) {
 				statusMessage.setText("All fields cleared!!");
 				eventField.setText("");
 				commentField.setText("");
 				locationField.setText("");
 				descriptionField.setText("");
 				exceptionDates = new ArrayList<Date>();
 			}
 
 			if (event.getSource() == generateButton) {
 				// check that the times make contextual sense
 				String beginTime = ICSFormat.valid(sdf.format(sdPicker.getDate()),stf.format(startSpinner.getValue()));
 				String endTime = ICSFormat.valid(sdf.format(edPicker.getDate()),stf.format(endSpinner.getValue()));
 				int state = ICSFormat.compare(beginTime,endTime);
 				if(state==1){
 					//do nothing
 				}else if(state==-2){	//if date was bad
 					highlight(Color.YELLOW, "date");
 					highlight(Color.GREEN, "time");
 					statusMessage.setText("Check your start and end dates");
 					return;
 				}else if(state==-3) {	//if time was bad
 					highlight(Color.GREEN, "date");
 					highlight(Color.YELLOW, "time");
 					statusMessage.setText("Check your start and end times");
 					return;
 				}else {			//error
 					statusMessage.setText("failed");
 					return;
 				}
 			
 				TimeZone tz = Calendar.getInstance().getTimeZone();
 				FileMaker generator = new FileMaker();
 				generator.set_attribute("VERSION", "2.0", "calendar");
 				generator.set_attribute("CALSCALE", "GREGORIAN", "calendar");
 				generator.set_attribute("SUMMARY", eventField.getText());
 				generator.set_attribute("CLASS", classDropDown.getSelectedItem().toString().toUpperCase());
 				generator.set_attribute("DESCRIPTION",descriptionField.getText());
 				generator.set_attribute("COMMENT", commentField.getText());
 				generator.set_attribute("DTSTAMP;TZID=" + tz.getID(),ICSFormat.timestamp());
 				generator.set_attribute("DTSTART;TZID=" + tz.getID(),ICSFormat.valid(sdf.format(sdPicker.getDate()),stf.format(startSpinner.getValue())));
 				generator.set_attribute("DTEND;TZID=" + tz.getID(),ICSFormat.valid(sdf.format(edPicker.getDate()),stf.format(endSpinner.getValue())));
 				if(recurringCheckBox.isSelected() == true){
 					//need to make check boxes that make the event run forever.
 					state = ICSFormat.compare(endTime, ICSFormat.valid(sdf.format(untilPicker.getDate()),stf.format(endSpinner.getValue())));
 					if(state==1) {
 						//do nothing
 					}else if(state==-2) {
 						highlight(Color.YELLOW, "date");
 						highlight(Color.YELLOW, "until");
 						statusMessage.setText("Check your end and until dates");
 						return;
 					}else {
 						statusMessage.setText("failed");
 						return;
 					}
 					
 					String rruleAttributes = "FREQ=" + repeatDropDown.getSelectedItem().toString().toUpperCase();
 					String rruleExceptions ="";
 					//if(!ICSFormat.valid(untilField.getText()).equals("") && !untilField.getText().equals("MM/DD/YYYY")) {
 						rruleAttributes += ";UNTIL="+ICSFormat.valid(sdf.format(untilPicker.getDate()), "00:00");
 					//}
 					generator.set_attribute("RRULE", rruleAttributes);
 					if (!exceptionDates.isEmpty()) {
 						for (int i = 0; i < exceptionDates.size(); i++) {
 							rruleExceptions += ICSFormat.valid(sdf.format(exceptionDates.get(i)), stf.format(startSpinner.getValue()));
 							rruleExceptions += (i== exceptionDates.size() - 1)?"":",";
 						}
 						generator.set_attribute("EXDATE;TZID=" + tz.getID(), rruleExceptions);
 					}
 				}
 				generator.set_attribute("LOCATION", locationField.getText());
 				if (generator.generate(eventField.getText().replaceAll(" ", "_"))) {
 					statusMessage.setText(".ICS file generated! Check it out.");
 				} else {
 					statusMessage.setText("Bad Input");
 				}
 			}
 		} catch (Exception e) {
 			statusMessage.setText("Something went wrong.");
 		}
 	}
 
 	/**
 	 * Generates the GUI for the .ics FileMaker.
 	 * @return True - Successfully generated GUI.
 	 * 		   False - Failed generation of GUI.
 	 */
 	 public boolean generate() {
 		try {
 			JFrame window = new JFrame();
 			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			window.setTitle("t3@m C0cO@ LOL");
 			iCalendarGUI panel = new iCalendarGUI();
 			window.add(panel);
 			window.pack();
 			window.setVisible(true);
 			panel.setLayout(new BorderLayout());
 			
 		} catch (Exception ex) {
 			return false;
 		}
 		return true;
 	}
 }
