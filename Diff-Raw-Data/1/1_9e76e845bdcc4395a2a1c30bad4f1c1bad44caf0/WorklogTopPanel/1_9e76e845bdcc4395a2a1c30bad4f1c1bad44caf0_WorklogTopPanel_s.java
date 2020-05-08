 package ui.swing.mainScreen.tasks.worklog;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import org.jdesktop.swingx.JXDatePicker;
 
 import ui.swing.utils.SwingUtils;
 import basic.Subscriber;
 
 public class WorklogTopPanel extends JPanel {
 
     private static final long serialVersionUID = 1L;
     private JXDatePicker datePicker;
     private JTextField totalHoursTextField;
     private JButton sendWorkLogButton;
     private final WorklogListModel worklogListModel;
     private JPanel pannel;
     private JPanel worklogPanel;
     private JComboBox selectedInterval;
 
     public WorklogTopPanel(final WorklogListModel dayTaskListModel) {
         this.worklogListModel = dayTaskListModel;
         initializeGUIComponents();
         initializeEventListeners();
         updateTotalHours();
     }
 
     private void initializeEventListeners() {
         addSendWorklogListener();
         addDatePickerListener();
         addTaskListChangeListener();
     }
 
     private void addTaskListChangeListener() {
         worklogListModel.addChangeSubscriber(new Subscriber() {
             @Override
             public void fire() {
                 updateTotalHours();
             }
         });
     }
 
     private void addDatePickerListener() {
         datePicker.addPropertyChangeListener("date", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 updateWorklogList();
             }
         });
     }
 
     private void addSendWorklogListener() {
         sendWorkLogButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 startSendWorklogThread();
             }
         });
     }
 
     private void initializeGUIComponents() {
         initializePanel();
         initializeWorklogPanel();
         setLayout(new BorderLayout());
         add(pannel, BorderLayout.WEST);
         add(worklogPanel, BorderLayout.EAST);
     }
 
     private void initializePanel() {
         pannel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         pannel.add(getIntervalCombo());
         addDatePicker();
         addTotalHoursTextField();
     }
 
     private JComboBox getIntervalCombo() {
         selectedInterval = new JComboBox(WorklogInterval.values());
         selectedInterval.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 updateWorklogList();
             }
         });
         return selectedInterval;
     }
 
     private void addTotalHoursTextField() {
         pannel.add(new JLabel("Total hours: "));
         totalHoursTextField = new JTextField(5);
         totalHoursTextField.setEditable(false);
         totalHoursTextField.setHorizontalAlignment(SwingConstants.RIGHT);
         pannel.add(totalHoursTextField);
     }
 
     private void addDatePicker() {
         datePicker = new JXDatePicker(new Date());
         pannel.add(datePicker);
     }
 
     private void initializeWorklogPanel() {
         worklogPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         addSendWorklogButton();
     }
 
     private void addSendWorklogButton() {
         sendWorkLogButton = new JButton("Send Worklog");
         sendWorkLogButton.setHorizontalAlignment(SwingConstants.LEFT);
         worklogPanel.add(sendWorkLogButton);
     }
 
     private void updateTotalHours() {
         SwingUtils.invokeAndWaitOrCry(new Runnable() {
             public void run() {
                 internalUpdateHours();
             }
         });
     }
 
     private void internalUpdateHours() {
         Double totalHours = worklogListModel.getDayTotalHours();
         final NumberFormat format = new DecimalFormat("#0.00");
         totalHoursTextField.setText(format.format(totalHours));
     }
 
     public void refrescate() {
         datePicker.setDate(new Date());
     }
 
     private void startSendWorklogThread() {
         new Thread(new Runnable() {
             @Override
             public void run() {
                 sendWorklog();
             }
         }).start();
     }
 
     private void sendWorklog() {
         try{
             sendWorkLogButton.setEnabled(false);
             worklogListModel.sendWorklog();
         }finally{
             sendWorkLogButton.setEnabled(true);
         }
     }
     
     private void updateWorklogList() {
         WorklogInterval selected = (WorklogInterval) selectedInterval.getSelectedItem();
         worklogListModel.setFilter(datePicker.getDate(), selected);
     }
 }
