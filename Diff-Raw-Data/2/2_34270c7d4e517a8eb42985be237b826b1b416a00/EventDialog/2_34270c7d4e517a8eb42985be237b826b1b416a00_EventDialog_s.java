 import java.util.Map;
 import java.util.HashMap;
 
 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.event.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * Dialog to implement the event dialog box to add, edit or remove
  * an event.
  *
  * @author cmcl
  * @version 1.0
  */
 public class EventDialog extends JDialog implements ActionListener {
     private JButton okayButton, delButton, cancelButton;
     private JTextField nameField, locField;
     private JTextField startDateField, endDateField;
     private JTextArea descArea;
     private CalendarModel model;
     private Map<JTextComponent, String> textMap;
     private boolean createEvent;
     private final DateDialog startDateDialog, endDateDialog;
     private final ReminderDialog reminderDialog;
     private final RepetitionDialog repetitionDialog;
     private final CategoryDialog catDialog;
     
     public EventDialog(JFrame frame, String title, CalendarModel model) {
         super(frame, title, Dialog.ModalityType.DOCUMENT_MODAL);
         
         this.model = model;
         textMap = new HashMap<JTextComponent, String>();
         createEvent = true;
         
         DateListener startListener = new DateListener();
         DateListener endListener = new DateListener();
         ActionListener dummyListener = new ActionListener() {
             public void actionPerformed(ActionEvent e) {}
         };
         startDateDialog = new DateDialog(this, "Event Start Date",
                                          startListener);
         endDateDialog = new DateDialog(this, "Event End Date",
                                        endListener);
                                        
         reminderDialog = new ReminderDialog(this, "Event reminder",
                                             dummyListener);
         repetitionDialog = new RepetitionDialog(this, "Event repetition",
                                                 dummyListener);
                                                 
         catDialog = new CategoryDialog(this, "Event Category",
                                        dummyListener);
         
         startListener.setOwner(startDateDialog);
         endListener.setOwner(endDateDialog);
         
         startDateDialog.setVisible(false);
         endDateDialog.setVisible(false);
         
         reminderDialog.setVisible(false);
         repetitionDialog.setVisible(false);
         catDialog.setVisible(false);
         
         startDateDialog.pack();
         endDateDialog.pack();
         
         reminderDialog.pack();
         repetitionDialog.pack();
         catDialog.pack();
         
         setLocationRelativeTo(frame);
         
         // Add panels to dialog
         add(createInputPanel(), BorderLayout.WEST);
         add(createSidePanel(), BorderLayout.EAST);
         add(createLowerPanel(), BorderLayout.SOUTH);
     }
     
     public CalendarModel getModel() { return model; }
     
     /**
      * Creates the panel that holds all input fields for the
      * event information.
      *
      * @return the input field panel.
      */
     private JPanel createInputPanel() {
         JPanel inputPanel = new SpacedPanel(new Dimension(5, 5));
         inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
         
         // Retrieve information for top of model
         Event e = model.peek();
         String name = e == null ? "Enter event name..." : e.getName();
         String loc = e == null ? 
                 "Enter event location..." : e.getLocation();
         String desc = (e == null ? "Enter event description..." : 
                                    e.getDescription());
         
         // Calculate largest string to have equal-sized text fields.
         String largest = name.length() > loc.length() ? name : loc;
         largest = largest.length() > desc.length() ? largest : desc;
         
         nameField = new JEventField(name, largest.length());
         locField = new JEventField(loc, largest.length());
         
         descArea = new JTextArea(desc, 4, largest.length());
         descArea.setBorder(BorderFactory.
                 createLineBorder(Color.BLACK, 1));
         
         startDateField = new JEventField("Enter event start date...",
                                          largest.length());
         endDateField = new JEventField("Enter event end date...",
                                        largest.length());
         
         // Add text fields to map
         textMap.put(nameField, "Enter event name...");
         textMap.put(locField, "Enter event location...");
         textMap.put(descArea, "Enter event description...");
         
         FocusListener focusListener = new FocusListener() {
             public void focusGained(FocusEvent e) {
                 JTextComponent source = (JTextComponent) e.getSource();
                 if (textMap.get(source).equals(source.getText())) {
                     source.setText(null);
                 }
             }
             
             public void focusLost(FocusEvent e) {
                 JTextComponent source = (JTextComponent) e.getSource();
                 if (source.getText().equals(""))
                     source.setText(textMap.get(source));
             }
         };
         
         nameField.addFocusListener(focusListener);
         locField.addFocusListener(focusListener);
         descArea.addFocusListener(focusListener);
 
         inputPanel.add(nameField);
         inputPanel.add(createDatePanel(startDateField, startDateDialog));
         inputPanel.add(createDatePanel(endDateField, endDateDialog));
         inputPanel.add(locField);
         inputPanel.add(descArea);
         
         inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
                                                              10));
         return inputPanel;
     }
     
     /**
      * Create the panel to hold a date text field
      * and button that displays a date dialog when clicked.
      *
      * @returns panel for the date input fields.
      */
     private JPanel createDatePanel(JTextField textField,
                                    final DateDialog dialog) {
         JPanel datePanel = new JPanel();
         datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.X_AXIS));
         
         textField.setEditable(false);
         
         JButton dialogButton = new JButton("..");
         dialogButton.setBackground(Color.BLACK);
         dialogButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 dialog.setVisible(true);
             }
         });
         
         datePanel.add(textField);
         datePanel.add(dialogButton);
         return datePanel;
     }
     
     /**
      * Creates the panel that holds operational buttons,
      * add, cancel and delete.
      *
      * @returns the side panel.
      */
     private JPanel createSidePanel() {
         JPanel sidePanel = new SpacedPanel(new Dimension(5, 5));
         sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
         
         cancelButton = new JButton("Cancel");
         delButton = new JButton("Delete");
         okayButton = new JButton("Okay");
         
         cancelButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 setVisible(false);
             }
         });
         delButton.addActionListener(this);
         okayButton.addActionListener(this);
         
         sidePanel.add(cancelButton);
         sidePanel.add(delButton);
         sidePanel.add(okayButton);
         
         sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
                                                             10));
 
         return sidePanel;    
     }
     
     
     /**
      * Creates the panel for buttons leading to more dialog
      * windows.
      *
      * @returns the dialog button panel.
      */
     private JPanel createLowerPanel() {
         JPanel lowerPanel = new JPanel(new BorderLayout());
         
         JPanel lowerLeftPanel = new SpacedPanel(new Dimension(5, 5));
         lowerLeftPanel.setLayout(new BoxLayout(lowerLeftPanel,
                                                BoxLayout.Y_AXIS));
         JButton reminder, repetition, category;
         lowerLeftPanel.add(reminder = new JButton("Set reminder"));
         lowerLeftPanel.add(repetition = new JButton("Set repetition"));
         lowerLeftPanel.add(category = new JButton("Set category"));
         
         reminder.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 reminderDialog.setVisible(true);
             }
         });
         
         repetition.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 repetitionDialog.setVisible(true);
             }
         });
         
        catDialog.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 catDialog.setVisible(true);
             }
         });
         
         lowerPanel.add(lowerLeftPanel, BorderLayout.WEST);
         lowerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
                                                              10));
         return lowerPanel;
     }
     
     /**
      * Handles the Okay and Delete button actions for
      * creation of Events.
      */
     public void actionPerformed(ActionEvent e) {
         JButton b = (JButton) e.getSource();
         if (b == okayButton) {
             // Take the top event and if non-null modify it with
             // the values in the text fields.
             Event event = createEvent ? null : model.peek();
             String name = nameField.getText();
             String loc = locField.getText();
             String desc = descArea.getText();
             Date start = startDateDialog.getDate();
             Date end = endDateDialog.getDate();
             Repetition rep = repetitionDialog.getRepetition();
             if (event != null) {
                 event.setName(name);
                 event.setLocation(loc);
                 event.setDescription(desc);
                 event.setStartDate(start);
                 event.setEndDate(end);
                 event.setRepetition(rep);
             } else model.push(new Event(name, loc, desc, start, end));
         } else if (b == delButton) {
             if (model.peek() != null) model.pop();
         }
         setVisible(false);
     }
     
     public void createEvent() {
         createEvent = true;
         updateFields(null);
     }
     
     public void editEvent() {
         createEvent = false;
         updateFields(model.peek());
     }
     
     public void deleteEvent() {
         delButton.doClick();
     }
     
     private void updateFields(Event e) {
         String name = e == null ? 
                 "Enter event name..." : e.getName();
         String loc = e == null ? 
                 "Enter event location..." : e.getLocation();
         String desc = (e == null ? "Enter event description..." : 
                                    e.getDescription());
         String startDate = (e == null ? "Enter event start date..." :
                                         e.getStartDate().toString());
         String endDate = (e == null ? "Enter event end date..." :
                                       e.getEndDate().toString());
         nameField.setText(name);
         locField.setText(loc);
         descArea.setText(desc);
         startDateField.setText(startDate);
         endDateField.setText(endDate);
     }
     
     private class DateListener implements ActionListener {
         private DateDialog owner;
         
         public DateListener() {
             owner = null;
         }
         
         public void setOwner(DateDialog d) { owner = d; }
         public DateDialog getOwner() { return owner; }
         
         /**
          * Invoked by a date dialog.
          * Update date text fields, corresponding to owner, to new date.
          */
         public void actionPerformed(ActionEvent e) {
             if (owner == startDateDialog) {
                 startDateField.setText(owner.getDate().toString());
             } else if (owner == endDateDialog) {
                 endDateField.setText(owner.getDate().toString());
             }
         }
     }
     
     /**
      * main method to simply test the dialog class
      */
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 JFrame frame = new JFrame("Dialog test");
                 frame.setLocation(200, 100);
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 JButton b = new JButton("Open dialog");
                 final JDialog dialog = new EventDialog(frame,
                         "This is a dialog", new CalendarModel());
                 dialog.setVisible(false);
                 dialog.pack();
                 
                 b.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         dialog.setVisible(true);
                     }
                 });
                 frame.getContentPane().add(b);
                 
                 frame.pack();
                 frame.setVisible(true);
             }
         });
     }
     
 }
 
