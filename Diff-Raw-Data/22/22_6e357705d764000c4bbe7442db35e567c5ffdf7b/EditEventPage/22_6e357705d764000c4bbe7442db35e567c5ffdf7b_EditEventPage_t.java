 /**
  * Name:    Amuthan Narthana and Nicholas Dyszel
  * Section: 2
  * Program: Scheduler Project
  * Date:    11/1/2012
  */
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.event.*;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import javax.swing.*;
 
 /**
  * EditEventPage class to display panel for user to create/edit an event
  * 
  * @author Nicholas Dyszel
  * @version 1.0  11/1/2012
  */
 @SuppressWarnings("serial")
 public class EditEventPage extends PagePanel implements ActionListener, ItemListener {
     private JTextField titleField;          // text field for user to enter title
     private JTextField locationField;       // text field for user to enter location
     private JList      attendeesList;       // list of all attendees for user to edit
     private JComboBox  monthDropDown;       // drop-down list to choose month
     private JTextField dayField;            // text field for user to enter day
     private JTextField yearField;           // text field for user to enter year
     private JTextField startHourField;      // text field for user to enter hour of start time
     private JTextField startMinField;       // text field for user to enter minute of start time
     private JComboBox  startAMPMDropDown;   // drop-down list to choose AM/PM of start time
     private JTextField endHourField;        // text field for user to enter hour of end time
     private JTextField endMinField;         // text field for user to enter minute of end time
     private JComboBox  endAMPMDropDown;     // drop-down list to choose AM/PM of end time
     private JCheckBox  isRecurringBox;      // check box for user to select recurring event
     private RecurType  recurType;           // type of recurrance
                                                 // NOTE: recurrance parameters inferred from date
     private JComboBox  recurDropDown;       // drop-down list for types of recurring events
     private JPanel     stopDatePanel;
     private JComboBox  stopMonthDropDown;
     private JTextField stopDayField;
     private JTextField stopYearField;
     private JButton    ok;
     private JButton    cancel;
     private JLabel     messageLabel;        // label for displaying error messages to user
     
     private Event      oldEvent = null;
 
     private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                                             "Sep", "Oct", "Nov", "Dec"};
     private static final String[] RECUR_TYPES = {"Daily", "Weekly", "Monthly (by Date)",
                                                  "Monthly (by Day)", "Yearly"};
     
     // enumeration of different recurring event types
     private static enum RecurType { DAILY, WEEKLY, MONTHLY_BY_DATE, MONTHLY_BY_DAY, YEARLY };
     
     /**
      * Init constructor
      * Edits an existing event
      * @throws Exception  a TimeBlock exception
      */
     public EditEventPage() {
         super();
         
         JPanel titlePanel = new JPanel();
         JPanel locationPanel = new JPanel();
         JPanel attendeesPanel = new JPanel();
         JPanel datePanel = new JPanel();
         JPanel timesPanel = new JPanel();
         JPanel recurPanel = new JPanel();
         JPanel buttonPanel = new JPanel();
         JPanel messagePanel = new JPanel();
         
         stopDatePanel = new JPanel();
         
         setLayout(new FlowLayout(FlowLayout.CENTER));
         
         titlePanel.add(new JLabel("Name:"));
         titleField = new JTextField(30);
         titlePanel.add(titleField);
         
         locationPanel.add(new JLabel("Location:"));
         locationField = new JTextField(30);
         locationPanel.add(locationField);
         
         attendeesPanel.add(new JLabel("Attendees:"));
         attendeesList = new JList();
         attendeesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         attendeesPanel.add(attendeesList);
         
         datePanel.add(new JLabel("Start Date:"));
         monthDropDown = new JComboBox(MONTHS);
         datePanel.add(monthDropDown);
         dayField = new JTextField(2);
         datePanel.add(dayField);
         datePanel.add(new JLabel(","));
         yearField = new JTextField(4);
         datePanel.add(yearField);
         
         timesPanel.add(new JLabel("Time:"));
         startHourField = new JTextField(2);
         timesPanel.add(startHourField); 
         timesPanel.add(new JLabel(":"));
         startMinField = new JTextField(2);
         timesPanel.add(startMinField);
         startAMPMDropDown = new JComboBox(new String[] {"AM", "PM"} );
         timesPanel.add(startAMPMDropDown);
         timesPanel.add(new JLabel("-"));
         endHourField = new JTextField(2);
         timesPanel.add(endHourField);
         timesPanel.add(new JLabel(":"));
         endMinField = new JTextField(2);
         timesPanel.add(endMinField);
         endAMPMDropDown = new JComboBox(new String[] {"AM", "PM"} );
         timesPanel.add(endAMPMDropDown);
         
         isRecurringBox = new JCheckBox("Recurring Event:");
         isRecurringBox.addItemListener(this);
         recurPanel.add(isRecurringBox);
         recurType = RecurType.DAILY;
         recurDropDown = new JComboBox(RECUR_TYPES);
         recurDropDown.addActionListener(this);
         recurDropDown.setEditable(false);
         recurPanel.add(recurDropDown);
         
         stopDatePanel.add(new JLabel("Stop Date:"));
         stopMonthDropDown = new JComboBox(MONTHS);
         stopMonthDropDown.setEditable(false);
         stopDatePanel.add(stopMonthDropDown);
         stopDayField = new JTextField(2);
         stopDayField.setEditable(false);
         stopDatePanel.add(stopDayField);
         stopDatePanel.add(new JLabel(","));
         stopYearField = new JTextField(4);
         stopYearField.setEditable(false);
         stopDatePanel.add(stopYearField);
         stopDatePanel.setVisible(false);
         
         ok = new JButton("OK");
         ok.addActionListener(this);
         buttonPanel.add(ok);
         cancel = new JButton("Cancel");
         cancel.addActionListener(this);
         buttonPanel.add(cancel);
         
         messageLabel = new JLabel();
         messageLabel.setForeground(Color.RED);
         messagePanel.add(messageLabel);
         
         JPanel[] mainPanels = {titlePanel, locationPanel, attendeesPanel, datePanel, timesPanel, recurPanel, stopDatePanel, buttonPanel, messagePanel};
         add(Utils.stackPanels(mainPanels));
         
         this.setEnabled(false);
     }
     
     /**
      * Activates panel
      * @throws Exception  invalid TimeBlock, though for this method, it will not be thrown
      */
     @Override
     public void activate() {
         Calendar today = Calendar.getInstance();
         TimeBlock times = new TimeBlock();
         Calendar stopDate = new GregorianCalendar();
         
         oldEvent = null;
         
         titleField.setText("");
         locationField.setText("");
         attendeesList.clearSelection();
         attendeesList.setListData(SchedulerMain.application.getOtherUsernames());
         
         monthDropDown.setSelectedIndex(today.get(Calendar.MONTH));
         dayField.setText(Integer.toString(today.get(Calendar.DATE)));
         yearField.setText(Integer.toString(today.get(Calendar.YEAR)));
         
         startHourField.setText(Integer.toString(times.getStartHourAP()));
         startMinField.setText(Integer.toString(times.getStartMinute()));
         startAMPMDropDown.setSelectedIndex(times.isStartPM());
         endHourField.setText(Integer.toString(times.getEndHourAP()));
         endMinField.setText(Integer.toString(times.getEndMinute()));
         endAMPMDropDown.setSelectedIndex(times.isEndPM());
         
         recurType = RecurType.DAILY;
         recurDropDown.setSelectedIndex(0);
         recurDropDown.setEnabled(false);
         isRecurringBox.setSelected(false);
         
         stopDatePanel.setVisible(false);
         stopDate = today;
         stopDate.add(Calendar.YEAR, 1);
         stopMonthDropDown.setSelectedIndex(stopDate.get(Calendar.MONTH));
         stopMonthDropDown.setEnabled(false);
         stopDayField.setText(Integer.toString(stopDate.get(Calendar.DATE)));
         stopDayField.setEditable(false);
         stopYearField.setText(Integer.toString(stopDate.get(Calendar.YEAR)));
         stopYearField.setEditable(false);
         
         messageLabel.setText("");
         
         this.setEnabled(true);
     }
     
     /**
      * Sets fields in edit menu to certain event
      * @throws Exception  invalid TimeBlock
      */
     public void setFields(Event eventToEdit) throws Exception {
         Calendar date = new GregorianCalendar();
         Calendar endDate = new GregorianCalendar();
         TimeBlock times;
         
         oldEvent = eventToEdit;
         
         titleField.setText(eventToEdit.getName());
         locationField.setText(eventToEdit.getLocation());
         
         if (eventToEdit instanceof OneTimeEvent) {
             date.setTime(((OneTimeEvent) eventToEdit).getStartDate());
             endDate.setTime(((OneTimeEvent) eventToEdit).getEndDate());
             times = new TimeBlock(date.get(Calendar.HOUR_OF_DAY),
                                   date.get(Calendar.MINUTE),
                                   endDate.get(Calendar.HOUR_OF_DAY),
                                   endDate.get(Calendar.MINUTE));
             
             isRecurringBox.setSelected(false);
             recurDropDown.setEnabled(false);
             stopDatePanel.setVisible(false);
             stopMonthDropDown.setEnabled(false);
             stopDayField.setEditable(false);
             stopYearField.setEditable(false);
         } else {
             date = ((RecurringEvent) eventToEdit).getIntervalStart();
             endDate = ((RecurringEvent) eventToEdit).getIntervalEnd();
             times = ((RecurringEvent) eventToEdit).getTimes();
             
             isRecurringBox.setSelected(true);
             recurDropDown.setEnabled(true);
             if (eventToEdit instanceof DailyRecurringEvent) {
                 recurType = RecurType.DAILY;
             } else if (eventToEdit instanceof WeeklyRecurringEvent) {
                 recurType = RecurType.WEEKLY;
             } else if (eventToEdit instanceof MonthlyDateRecurringEvent) {
                 recurType = RecurType.MONTHLY_BY_DATE;
             } else if (eventToEdit instanceof MonthlyDayRecurringEvent) {
                 recurType = RecurType.MONTHLY_BY_DAY;
             } else {
                 recurType = RecurType.YEARLY;
             }
             recurDropDown.setSelectedIndex(recurType.ordinal());
             
             stopDatePanel.setVisible(true);
             stopMonthDropDown.setEnabled(true);
             stopMonthDropDown.setSelectedIndex(endDate.get(Calendar.MONTH));
             stopDayField.setEditable(true);
             stopDayField.setText(Integer.toString(endDate.get(Calendar.DATE)));
             stopYearField.setEditable(true);
             stopYearField.setText(Integer.toString(endDate.get(Calendar.YEAR)));
         }
         
         monthDropDown.setSelectedIndex(date.get(Calendar.MONTH));
         dayField.setText(Integer.toString(date.get(Calendar.DATE)));
         yearField.setText(Integer.toString(date.get(Calendar.YEAR)));
         
         startHourField.setText(Integer.toString(times.getStartHourAP()));
         startMinField.setText(Integer.toString(times.getStartMinute()));
         startAMPMDropDown.setSelectedIndex(times.isStartPM());
         endHourField.setText(Integer.toString(times.getEndHourAP()));
         endMinField.setText(Integer.toString(times.getEndMinute()));
         endAMPMDropDown.setSelectedIndex(times.isEndPM());
     }
     
     /**
      * Paints panel
      * @param g
      */
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
     }
 
     /**
      * Handles events to text fields and drop-down lists
      * @param e  action data
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         Event newEvent = null;
         String title;
         String location;
         int startYear;
         int startMonth;
         int startDateNum;
         int startHour;
         int startMin;
         int endHour;
         int endMin;
         Calendar date;
         Calendar endDate;
         
         if (e.getSource() == recurDropDown) {
             recurType = RecurType.values()[recurDropDown.getSelectedIndex()];
         } else if (e.getSource() == ok) {
             try {
                 User current = SchedulerMain.application.currentUser;
                 
                 title = titleField.getText();
                 location = locationField.getText();
                 
                 Object[] selectedListValues = attendeesList.getSelectedValues();
                 User[] attendees = new User[selectedListValues.length];
                 for (int i=0; i<selectedListValues.length; i++){
                     attendees[i] = SchedulerMain.application.searchForUser(selectedListValues[i].toString());
                 }
                 
                 date = new GregorianCalendar();
                 startYear = Integer.parseInt(yearField.getText());
                 startMonth = monthDropDown.getSelectedIndex();
                 startDateNum = Integer.parseInt(dayField.getText());
                 date.set(startYear, startMonth, startDateNum, 0, 0);
                 
                 startHour = TimeBlock.apToMilitary(Integer.parseInt(startHourField.getText()),
                                                    startAMPMDropDown.getSelectedIndex()==1);
                 startMin = Integer.parseInt(startMinField.getText());
                 endHour = TimeBlock.apToMilitary(Integer.parseInt(endHourField.getText()),
                                                  endAMPMDropDown.getSelectedIndex()==1);
                 endMin = Integer.parseInt(endMinField.getText());
                 
                 int endYear = Integer.parseInt(stopYearField.getText());
                 int endMonth = stopMonthDropDown.getSelectedIndex();
                 int endDateNum = Integer.parseInt(stopDayField.getText());
                 
                 // Make sure the input is valid before proceeding
                 String errorMessage = null;
                 if (title.trim().length() == 0){
                     errorMessage = "Event name cannot be empty.";
                 } else if (!TimeBlock.isValid(startHour, startMin, endHour, endMin)){
                     errorMessage = "'Time' field is invalid.";
                 } else if (!Utils.dateIsValid(startYear, startMonth, startDateNum)){
                     errorMessage = "'Start Date' field is invalid.";
                 } else if (isRecurringBox.isSelected()){
                     if (!Utils.dateIsValid(endYear, endMonth, endDateNum)){
                         errorMessage = "'End Date' field is invalid.";
                     } else if (!Utils.dateIsLessThan(startYear, startMonth, startDateNum, endYear, endMonth, endDateNum)){
                         errorMessage = "Start Date must come before End Date";
                     }
                 }
                 
                 // If errorMessage is null, then there is no error
                 if (errorMessage == null){
                     if (isRecurringBox.isSelected()) {
                         endDate = new GregorianCalendar();
                         endDate.set(endYear, endMonth, endDateNum, 23, 59);
                         switch (recurType) {
                         case DAILY:
                             newEvent = new DailyRecurringEvent(title, location, attendees, current, startHour,
                                                                startMin, endHour, endMin, date, endDate);
                             break;
                         case WEEKLY:
                             newEvent = new WeeklyRecurringEvent(title, location, attendees, current, startHour,
                                                                 startMin, endHour, endMin,
                                                                 date.get(Calendar.DAY_OF_WEEK), date,
                                                                 endDate);
                             break;
                         case MONTHLY_BY_DATE:
                             newEvent = new MonthlyDateRecurringEvent(title, location, attendees, current,
                                                                      startHour, startMin, endHour, endMin,
                                                                      date.get(Calendar.DATE), date,
                                                                      endDate);
                             break;
                         case MONTHLY_BY_DAY:
                             newEvent = new MonthlyDayRecurringEvent(title, location, attendees, current, startHour,
                                                                     startMin, endHour, endMin,
                                                                     date.get(Calendar.DAY_OF_WEEK),
                                                                     date.get(
                                                                             Calendar.DAY_OF_WEEK_IN_MONTH),
                                                                     date, endDate);
                             break;
                         case YEARLY:
                             newEvent = new YearlyRecurringEvent(title, location, attendees, current, startHour,
                                                                 startMin, endHour, endMin,
                                                                 date.get(Calendar.MONTH),
                                                                 date.get(Calendar.DATE), date, endDate);
                             break;
                         }
                     } else {
                         date.getTime(); // force the Calendar to recompute its date
                         date.set(Calendar.HOUR_OF_DAY, startHour);
                         date.set(Calendar.MINUTE, startMin);
                         
                         endDate = new GregorianCalendar();
                         endDate.set(startYear, startMonth, startDateNum, endHour, endMin);
                         
                         newEvent = new OneTimeEvent(title, location, attendees, current, date.getTime(),
                                                     endDate.getTime());
                     }
                     
                     
                     if (oldEvent != null){
                         newEvent.setEventID(oldEvent.getEventID());
                         SchedulerMain.application.editEvent(oldEvent, newEvent);
                     } else {
                        newEvent.setEventID(SQL.getNextEventID());
                         SchedulerMain.application.addEvent(newEvent);
                     }
                     SchedulerMain.application.showMonthlyView();
                     this.setEnabled(false);
                 } else {
                     messageLabel.setText(errorMessage);
                 }
             }
             catch (Exception ex) {
                 messageLabel.setText("Invalid Input.");
                 // TODO: temp
                 ex.printStackTrace();
             }
         } else if (e.getSource() == cancel) {
             SchedulerMain.application.showMonthlyView();
             this.setEnabled(false);
         }
     }
 
     /**
      * Handles event to recurring event check box
      * @param e  event data
      */
     @Override
     public void itemStateChanged(ItemEvent e) {
         // if (e.getSource() == isRecurringBox)
         if (e.getStateChange() == ItemEvent.SELECTED) {
             recurDropDown.setEnabled(true);
             stopDatePanel.setVisible(true);
             stopMonthDropDown.setEnabled(true);
             stopDayField.setEditable(true);
             stopYearField.setEditable(true);
         } else {
             recurDropDown.setEnabled(false);
             stopDatePanel.setVisible(false);
             stopMonthDropDown.setEnabled(false);
             stopDayField.setEditable(false);
             stopYearField.setEditable(false);
         }
     }
 }
