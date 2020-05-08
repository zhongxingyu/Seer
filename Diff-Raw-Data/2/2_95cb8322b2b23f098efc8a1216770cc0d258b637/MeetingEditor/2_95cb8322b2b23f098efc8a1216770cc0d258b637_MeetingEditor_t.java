 package ui.SwingMeeting;
 
 import Email.MessageController;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import ui.LabeledTextField;
 
 /**
  * Edit Meeting Window
  */
 public class MeetingEditor extends JFrame {
 
     String messageId;
     JLabel dateLabel;
     JFormattedTextField dateField;
     JLabel startTimeLabel;
     JFormattedTextField startTimeField;
     JLabel endTimeLabel;
     JFormattedTextField endTimeField;
     LabeledTextField subjectField;
     LabeledTextField toField;
     JTextArea meetingContentTextArea;
 
 
     /**
      * Enum types for viewing windows
      */
     public enum Type {
 
         /**
          * Type for composing message
          */
         COMPOSE_MEETING,
         /**
          * Type for viewing message
          */
         VIEW_MEETING,
         /**
          * Type for responding to messages
          */
         RESPOND_MEETING
     };
     Type type;
 
     /**
      * Constructor of MeetingEditor
      * @param messageId
      */
     public MeetingEditor(String messageId, Type type) {
         super("Meeting");
         this.messageId = messageId;
         this.type = type;
     }
 
     /**
      * Function to initialize meeting compose window
      */
     public void init() {
         subjectField = new LabeledTextField("Subject");
 
         //FIXME Use JSpinner here and for time
         dateLabel = new JLabel("Date (dd/mm/yyyy):");
         SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
         dateField = new JFormattedTextField(dateformat);
         Date now = new Date();
         dateField.setValue(now);
         dateLabel.setLabelFor(dateField);
 
         startTimeLabel = new JLabel("Start Time (HH:MM):");
         startTimeField = new JFormattedTextField(new SimpleDateFormat("HH:mm"));
         startTimeField.setValue(now);
         endTimeLabel = new JLabel("End Time (HH:MM):");
         endTimeField = new JFormattedTextField(new SimpleDateFormat("HH:mm"));
         endTimeField.setValue(now);
 
         toField = new LabeledTextField("To");
         meetingContentTextArea = new JTextArea();
 
         JButton sendMeeting;
         JButton chooseDate;
         JButton closeMeeting;
         JButton acceptButton;
         JButton declineButton;
         //UtilDateModel model = new UtilDateModel();
         //JDatePickerImpl datePanel = new JDatePickerImpl(new JDatePanelImpl(model));
 
         sendMeeting = new JButton("Send");
         sendMeeting.setToolTipText("Send meeting to the recepients");
         sendMeeting.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 if(doValidate() == true)
                 {
                 send();
                 setVisible(false);
                 dispose();
                 }
             }
         });
 
         closeMeeting = new JButton("Close");
         closeMeeting.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 setVisible(false);
                 dispose();
             }
         });
         acceptButton = new JButton("Accept");
         acceptButton.setToolTipText("Accept this meeting, and respond");
         acceptButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 accept();
                 setVisible(false);
                 dispose();
             }
         });
         declineButton = new JButton("Decline");
         declineButton.setToolTipText("Decline this meeting, and respond");
         declineButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent ae) {
                 decline();
                 setVisible(false);
                 dispose();
             }
         });
         if (type != Type.COMPOSE_MEETING) {
             toField.setEditable(false);
             subjectField.setEditable(false);
 
             dateField.setEditable(false);
             startTimeField.setEditable(false);
             endTimeField.setEditable(false);
 
             meetingContentTextArea.setEditable(false);
         }
 
         // Make header
         JPanel headerPanel = new JPanel();
         BoxLayout headerLayout = new BoxLayout(headerPanel, BoxLayout.Y_AXIS);
         headerPanel.setLayout(headerLayout);
 
         JPanel datePanel = new JPanel();
         BoxLayout dateLayout = new BoxLayout(datePanel, BoxLayout.X_AXIS);
         datePanel.add(dateLabel);
         datePanel.add(dateField);
         datePanel.setLayout(dateLayout);
 
         JPanel startTimePanel = new JPanel();
         BoxLayout startTimeLayout = new BoxLayout(startTimePanel, BoxLayout.X_AXIS);
         startTimePanel.add(startTimeLabel);
         startTimePanel.add(startTimeField);
         startTimePanel.setLayout(startTimeLayout);
 
         JPanel endTimePanel = new JPanel();
         BoxLayout endTimeLayout = new BoxLayout(endTimePanel, BoxLayout.X_AXIS);
         endTimePanel.add(endTimeLabel);
         endTimePanel.add(endTimeField);
         endTimePanel.setLayout(endTimeLayout);
 
         headerPanel.add(toField);
         headerPanel.add(subjectField);
         headerPanel.add(datePanel);
         headerPanel.add(startTimePanel);
         headerPanel.add(endTimePanel);
 
         // Make footer
         JPanel footerPanel = new JPanel();
         BoxLayout footerLayout = new BoxLayout(footerPanel, BoxLayout.X_AXIS);
         footerPanel.setLayout(footerLayout);
 
         if (type == Type.VIEW_MEETING) {
             footerPanel.add(closeMeeting);
         } else if (type == Type.RESPOND_MEETING) {
             footerPanel.add(acceptButton);
             footerPanel.add(declineButton);
         } else {
             footerPanel.add(sendMeeting);
         }
         this.setLayout(new BorderLayout());
 
         this.add(headerPanel, BorderLayout.NORTH);
         this.add(meetingContentTextArea);
         this.add(footerPanel, BorderLayout.SOUTH);
         this.setSize(650, 380);
     }
 
     public void refresh() {
 
         MessageController controller = MessageController.getInstance();
         subjectField.setText(controller.getEmailHeader(messageId, "Subject"));
         toField.setText(controller.getEmailHeader(messageId, "To"));
         dateField.setText(controller.getEmailHeader(messageId, "MeetingDate"));
         startTimeField.setText(controller.getEmailHeader(messageId, "MeetingStartTime"));
         endTimeField.setText(controller.getEmailHeader(messageId, "MeetingEndTime"));
         meetingContentTextArea.setText(controller.getEmailContent(messageId));
     }
     private void send() {
         //TODO Verify input is valid
         //Date is in future, Start Time < End Time
 
         MessageController controller = MessageController.getInstance();
         controller.setEmailHeader(messageId, "Subject", subjectField.getText());
         controller.setEmailHeader(messageId, "To", toField.getText());
         controller.setEmailHeader(messageId, "MeetingDate", dateField.getText());
         controller.setEmailHeader(messageId, "MeetingStartTime", startTimeField.getText());
         controller.setEmailHeader(messageId, "MeetingEndTime", endTimeField.getText());
         controller.setEmailHeader(messageId, "From", controller.getRootFolderId());
         controller.setEmailContent(messageId, meetingContentTextArea.getText());
         controller.updateDate(messageId);
         controller.sendMeeting(messageId);
 
     }
 
     private boolean doValidate()
     {
         boolean validMsg = false;
 
             validMsg =   checkTime();
        if (subjectField.getText().isEmpty()){
              JOptionPane.showMessageDialog(
               null,
               "Subject field is empty. Please enter any text for subject."
                );
         }
         return validMsg;
     }
       private boolean checkTime() {
         String[] starttime = startTimeField.getText().split(":");
         String[] endtime = endTimeField.getText().split(":");
         String[] dateT = dateField.getText().split("/");
          boolean validMsg = true;
         DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
         Date date = new Date();
         String todayDate = dateFormat.format(date);
         String[] dateNow = todayDate.split("/");
         if (Integer.parseInt(dateT[2]) < Integer.parseInt(dateNow[2]))
         {
             JOptionPane.showMessageDialog(
         null,
         "Year of the meeting is expired."
            );
         validMsg = false;
         }
         if (Integer.parseInt(dateT[1]) < Integer.parseInt(dateNow[1])){
             JOptionPane.showMessageDialog(
         null,
         "Month of the meeting is expired."
            );
         validMsg = false;}
         if (Integer.parseInt(dateT[0]) < Integer.parseInt(dateNow[0])){
             JOptionPane.showMessageDialog(
         null,
         "Date of the meeting is expired."
            );
         validMsg = false;}
         if (startTimeField.getText().equalsIgnoreCase(endTimeField.getText()) ){
         JOptionPane.showMessageDialog(
         null,
         "Start time and end time is same pls edit the time for meeting."
            );
         validMsg = false;
           }
         else if(Integer.parseInt(endtime[0]) < Integer.parseInt(starttime[0])){
             JOptionPane.showMessageDialog(
         null,
         "End time is earlier then starting time of the meeting."
            );
         validMsg = false;
         }
         else if(Integer.parseInt(starttime[0]) == Integer.parseInt(endtime[0]) && Integer.parseInt(starttime[1]) > Integer.parseInt(endtime[1]) ){
             JOptionPane.showMessageDialog(
         null,
         "End time is earlier then starting time of the meeting."
            );
         validMsg = false;
         }
       return validMsg;
       }
 
 
     private void accept() {
         MessageController controller = MessageController.getInstance();
         controller.acceptMeeting(messageId);
     }
 
     private void decline() {
         MessageController controller = MessageController.getInstance();
         controller.declineMeeting(messageId);
     }
 }
