 /**
  * Name:    Amuthan Narthana and Nicholas Dyszel
  * Section: 2
  * Program: Scheduler Project
  * Date:    11/1/2012
  */
 
 import java.awt.Graphics;
 import java.awt.event.*;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import javax.swing.*;
 
 /**
  * EditEventPage class to display panel for user to create/edit an event
  * 
  * @author Nicholas Dyszel
  * @version 1.0  11/1/2012
  */
 public class EditEventPage extends JPanel implements ActionListener, ItemListener {
     private Event      newEvent;            // event object to create / edit
     private String     title;               // object to store title
     private JTextField titleField;          // text field for user to enter title
     private String     location;            // object to store location
     private JTextField locationField;       // text field for user to enter location
     private String     attendees;           // who is attending
     private JTextField addAttendeeField;    // text field for user to add new attendee
     private JList      attendeesList;       // list of all attendees for user to edit
     private Calendar   date;                // Date object to store in event
     private JComboBox  monthDropDown;       // drop-down list to choose month
     private JTextField dayField;            // text field for user to enter day
     private JTextField yearField;           // text field for user to enter year
     private TimeBlock  times;               // block of time stored in event
     private JTextField startHourField;      // text field for user to enter hour of start time
     private JTextField startMinField;       // text field for user to enter minute of start time
     private JComboBox  startAMPMDropDown;   // drop-down list to choose AM/PM of start time
     private JTextField endHourField;        // text field for user to enter hour of end time
     private JTextField endMinField;         // text field for user to enter minute of end time
     private JComboBox  endAMPMDropDown;     // drop-down list to choose AM/PM of end time
     private Boolean    isRecurring;         // is the event a recurring event?
     private JCheckBox  isRecurringBox;      // check box for user to select recurring event
     private RecurType  recurType;           // type of recurrance
                                                 // NOTE: recurrance parameters inferred from date
     private JComboBox  recurDropDown;       // drop-down list for types of recurring events
     private Calendar   stopDate;
     private JComboBox  stopMonthDropDown;
     private JTextField stopDayField;
     private JTextField stopYearField;
     
     // enumeration of different recurring event types
     private enum RecurType { DAILY, WEEKLY, MONTHLY_BY_DATE, MONTHLY_BY_DAY, YEARLY };
     
     private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                                             "Sep", "Oct", "Nov", "Dec"};
     private static final String[] RECUR_TYPES = {"Daily", "Weekly", "Monthly (by Date)",
                                                  "Monthly (by Day)", "Yearly"};
     
     /**
      * Default constructor
      * Creates new event
      */
     /* public EditEventPage() {
         this(new OneTimeEvent("", "", { getUser() }, getUser(), new Date(), new Date()));
     } */
     
     /**
      * Init constructor
      * Edits an existing event
      * @throws Exception  a TimeBlock exception
      */
     public EditEventPage(Event eventToEdit) throws Exception {
         Calendar tempEnd;
         
         newEvent = eventToEdit;
         title = eventToEdit.getName();
         location = eventToEdit.getLocation();
             
         if (eventToEdit instanceof OneTimeEvent) {
             (date = new GregorianCalendar()).setTime(((OneTimeEvent) eventToEdit).getStartDate());
             (tempEnd = new GregorianCalendar()).setTime(((OneTimeEvent) eventToEdit).getEndDate());
             times = new TimeBlock(date.get(Calendar.HOUR_OF_DAY),
                                   date.get(Calendar.MINUTE),
                                   tempEnd.get(Calendar.HOUR_OF_DAY),
                                   tempEnd.get(Calendar.MINUTE));
             isRecurring = false;
         } else {
             date = ((RecurringEvent) eventToEdit).getIntervalStart();
             stopDate = ((RecurringEvent) eventToEdit).getIntervalEnd();
             times = ((RecurringEvent) eventToEdit).getTimes();
             isRecurring = true;
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
         }
         
         add(new JLabel("Name:"));
         titleField = new JTextField(title);
         titleField.addActionListener(this); 
         add(titleField);
         
         add(new JLabel("Location:"));
         locationField = new JTextField(location);
         locationField.addActionListener(this);
         add(locationField);
         
         add(new JLabel("Date:"));
         monthDropDown = new JComboBox(MONTHS);
         monthDropDown.setSelectedIndex(date.get(Calendar.MONTH));
         monthDropDown.addActionListener(this);
         add(monthDropDown);
         dayField = new JTextField(Integer.toString(date.get(Calendar.DATE)));
         dayField.addActionListener(this);
         add(dayField);
         add(new JLabel(","));
         yearField = new JTextField(Integer.toString(date.get(Calendar.YEAR)));
         yearField.addActionListener(this);
         add(yearField);
         
         add(new JLabel("Time:"));
         startHourField = new JTextField(Integer.toString(times.getStartHourAP()));
         startHourField.addActionListener(this);
         add(startHourField);
         add(new JLabel(":"));
         startMinField = new JTextField(Integer.toString(times.getStartMinute()));
         startMinField.addActionListener(this);
         add(startMinField);
         startAMPMDropDown = new JComboBox(new String[] {"AM", "PM"} );
         startAMPMDropDown.setSelectedIndex(times.isStartPM());
         startAMPMDropDown.addActionListener(this);
         add(startAMPMDropDown);
         add(new JLabel("-"));
         endHourField = new JTextField(Integer.toString(times.getEndHourAP()));
         endHourField.addActionListener(this);
         add(endHourField);
         add(new JLabel(":"));
         endMinField = new JTextField(Integer.toString(times.getEndMinute()));
         endMinField.addActionListener(this);
         add(endMinField);
         endAMPMDropDown = new JComboBox(new String[] {"AM", "PM"} );
         endAMPMDropDown.setSelectedIndex(times.isEndPM());
         endAMPMDropDown.addActionListener(this);
         add(endAMPMDropDown);
         
         isRecurringBox = new JCheckBox("Recurring Event:");
         isRecurringBox.setSelected(isRecurring);
         isRecurringBox.addItemListener(this);
         add(isRecurringBox);
         recurDropDown = new JComboBox(RECUR_TYPES);
         recurDropDown.setSelectedIndex(recurType.ordinal());
         recurDropDown.addActionListener(this);
         recurDropDown.setEditable(isRecurring);
         add(recurDropDown);
         add(new JLabel("Stop Date:"));
         stopMonthDropDown = new JComboBox(MONTHS);
         stopMonthDropDown.setSelectedIndex(stopDate.get(Calendar.MONTH));
         stopMonthDropDown.addActionListener(this);
         stopMonthDropDown.setEditable(isRecurring);
         add(stopMonthDropDown);
         stopDayField = new JTextField(stopDate.get(Calendar.DATE));
         stopDayField.setEditable(isRecurring);
         stopDayField.addActionListener(this);
         add(stopDayField);
         add(new JLabel(","));
         stopYearField = new JTextField(stopDate.get(Calendar.YEAR));
         stopYearField.setEditable(isRecurring);
         stopYearField.addActionListener(this);
         add(stopYearField);
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
         int hour;
         
         if (e.getSource() == titleField) {
             title = titleField.getText();
         } else if (e.getSource() == locationField) {
             location = locationField.getText();
         } else if (e.getSource() == monthDropDown) {
             date.set(Calendar.MONTH, monthDropDown.getSelectedIndex());
         } else if (e.getSource() == dayField) {
             try {
                 date.set(Calendar.DATE, Integer.parseInt(dayField.getText()));
             }
             catch (NumberFormatException nfe) {
                 dayField.setText(Integer.toString(date.get(Calendar.DATE)));
             }
         } else if (e.getSource() == yearField) {
             try {
                 date.set(Calendar.YEAR, Integer.parseInt(yearField.getText()));
             }
             catch (NumberFormatException nfe) {
                 yearField.setText(Integer.toString(date.get(Calendar.YEAR)));
             }
         } else if (e.getSource() == startHourField) {
             try {
                 times.setStartHourAP(Integer.parseInt(startHourField.getText()),
                                      startAMPMDropDown.getSelectedIndex() == 1);
             }
             catch (NumberFormatException nfe) {
                 startHourField.setText(Integer.toString(times.getStartHourAP()));
             }
             catch (Exception ex) {
                 startHourField.setText(Integer.toString(times.getStartHourAP()));
             }
         } else if (e.getSource() == startMinField) {
             try {
                 times.setStartMinute(Integer.parseInt(startMinField.getText()));
             }
             catch (NumberFormatException nfe) {
                 startMinField.setText(Integer.toString(times.getStartMinute()));
             }
             catch (Exception ex) {
                 startMinField.setText(Integer.toString(times.getStartMinute()));
             }
         } else if (e.getSource() == startAMPMDropDown) {
             try {
                 times.setStartHourAP(Integer.parseInt(startHourField.getText()),
                                      startAMPMDropDown.getSelectedIndex() == 1);
             }
             catch (NumberFormatException nfe) {
                 startAMPMDropDown.setSelectedIndex(times.isStartPM());
             }
             catch (Exception ex) {
                 startAMPMDropDown.setSelectedIndex(0);
             }
         } else if (e.getSource() == endHourField) {
             try {
                 times.setEndHourAP(Integer.parseInt(endHourField.getText()),
                                    endAMPMDropDown.getSelectedIndex() == 1);
             }
             catch (NumberFormatException nfe) {
                 endHourField.setText(Integer.toString(times.getEndHourAP()));
             }
             catch (Exception ex) {
                 endHourField.setText(Integer.toString(times.getEndHourAP()));
             }
         } else if (e.getSource() == endMinField) {
             try {
                 times.setStartMinute(Integer.parseInt(startMinField.getText()));
             }
             catch (NumberFormatException nfe) {
                 endMinField.setText(Integer.toString(times.getEndMinute()));
             }
             catch (Exception ex) {
                 endMinField.setText(Integer.toString(times.getEndMinute()));
             }
         } else if (e.getSource() == endAMPMDropDown) {
             try {
                 times.setEndHourAP(Integer.parseInt(startHourField.getText()),
                                    endAMPMDropDown.getSelectedIndex() == 1);
             }
             catch (NumberFormatException nfe) {
                 endAMPMDropDown.setSelectedIndex(times.isEndPM());
             }
             catch (Exception ex) {
                 endAMPMDropDown.setSelectedIndex(1);
             }
         } else if (e.getSource() == recurDropDown) {
             recurType = RecurType.values()[recurDropDown.getSelectedIndex()];
         } else if (e.getSource() == stopMonthDropDown) {
             stopDate.set(Calendar.MONTH, stopMonthDropDown.getSelectedIndex());
         } else if (e.getSource() == stopDayField) {
             try {
                 stopDate.set(Calendar.DATE, Integer.parseInt(stopDayField.getText()));
             }
             catch (NumberFormatException nfe) {
                 stopDayField.setText(Integer.toString(stopDate.get(Calendar.DATE)));
             }
         } else if (e.getSource() == stopYearField) {
             try {
                 stopDate.set(Calendar.YEAR, Integer.parseInt(stopYearField.getText()));
             }
             catch (NumberFormatException nfe) {
                 stopYearField.setText(Integer.toString(stopDate.get(Calendar.YEAR)));
             }
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
             recurDropDown.setEditable(true);
             stopMonthDropDown.setEditable(true);
             stopDayField.setEditable(true);
             stopYearField.setEditable(true);
         } else {
             recurDropDown.setEditable(false);
             stopMonthDropDown.setEditable(false);
             stopDayField.setEditable(false);
             stopYearField.setEditable(false);
         }
     }
 }
