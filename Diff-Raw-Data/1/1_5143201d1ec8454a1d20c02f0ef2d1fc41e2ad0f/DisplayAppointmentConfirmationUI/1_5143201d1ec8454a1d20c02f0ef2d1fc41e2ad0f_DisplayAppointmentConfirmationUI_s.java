 
 package gui.sub;
 
 import gui.Constants;
 import gui.main.AppointmentConfirmationPane;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Date;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.AppointmentDto;
 import backend.DataTransferObjects.PatientDto;
 
 import gui.main.MainWindow;
 /**
  * DisplayAppointmentConfirmationUI shows information about a patient's appointment when a patient in the table
  * is clicked.
  */
 public class DisplayAppointmentConfirmationUI extends JDialog implements ActionListener {
 	private static DisplayAppointmentConfirmationUI displayAppointmentConfirmationUI;
 	
 	private static AppointmentDto appointment;
 	
 	private JPanel infoPanel;
 	private JPanel buttonPanel;
 	private JPanel notePanel;
 	private JButton confirmButton = new JButton("Confirm");
 	private JButton okButton = new JButton("OK");
 	private JButton cancelButton = new JButton("Cancel");
 	private JTextArea textArea;
 	private JTextArea noteArea;
 	
         private MainWindow main;
         
 	/**
 	 * Constructor - creates the actual UI to display the patient's appointment information
 	 * @param name - the title to be displayed in the top bar of the pop up window
 	 * @param appt - the appointment information to be displayed
 	 */
 	private DisplayAppointmentConfirmationUI(String name, AppointmentDto appt, MainWindow main) {
 		appointment = appt;
 		
                 this.main = main;
                 
 		setModal(true);
 		setTitle(name);
 		
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(350, 350));
 		setResizable(false);
 		
 		infoPanel = new JPanel(new BorderLayout());
 		buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		
 		PatientDto patient = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appt.getPatientID());
 		String confirmed = (appt.getConfirmation() == true ? "Yes" : "No");
 		Date date = new Date(appt.getApptDate().getTime());
 		
 		String text = "Date: " + date.toString() + "\n" +
 					  "Time Slot: " + appt.prettyPrintStart() + " - " + appt.prettyPrintEnd() + "\n" +
 					  "Patient Name: " + patient.getFirst() + " " + patient.getLast() + "\n" +
 					  "Phone Number: " + patient.getPhone() + "\n" +
 					  "Confirmed: " + confirmed;
 		
 		textArea = new JTextArea();
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		textArea.setEditable(false);
 		textArea.setFont(Constants.PARAGRAPH);
 		textArea.setOpaque(false);
 		textArea.setHighlighter(null);
 		textArea.setText(text);
 		infoPanel.add(textArea);
 
 		notePanel = new JPanel(new BorderLayout());
 		JLabel noteLabel = new JLabel("Appointment Confirmation Note:");
 		noteLabel.setFont(Constants.PARAGRAPH);
 		JScrollPane notePane = new JScrollPane();
 		notePane.setPreferredSize(new Dimension(200,200));
 		noteArea = new JTextArea();
 		noteArea.setLineWrap(true);
 		noteArea.setWrapStyleWord(true);
 		noteArea.setFont(Constants.PARAGRAPH);
 
 		noteArea.setText((appt.getNote()).replaceAll("\t\t", "\n"));
 		notePane.setViewportView(noteArea);
 		notePanel.add(noteLabel, BorderLayout.NORTH);
 		notePanel.add(notePane, BorderLayout.CENTER);
 		
 		confirmButton.setFont(Constants.DIALOG);
 		okButton.setFont(Constants.DIALOG);
 		cancelButton.setFont(Constants.DIALOG);
 		
 		confirmButton.setActionCommand("confirm");
 		okButton.setActionCommand("OK");
 		cancelButton.setActionCommand("cancel");
 		
 		confirmButton.setAction(changeConfirmationAction);
 		isConfirmedValidate();
 		okButton.addActionListener(this);
 		cancelButton.addActionListener(this);
 
 		buttonPanel.add(confirmButton);
 		buttonPanel.add(okButton);
 		buttonPanel.add(cancelButton);
 		
 		infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		buttonPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
 		
 		add(infoPanel, BorderLayout.NORTH);
 		add(notePanel, BorderLayout.CENTER);
 		add(buttonPanel, BorderLayout.SOUTH);
 		
 		setResizable(false);
 	}
 	
 	private void refreshPatientInfo(AppointmentDto appt) {
 		PatientDto patient = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appt.getPatientID());
 		String confirmed = (appt.getConfirmation() == true ? "Yes" : "No");
 		Date date = new Date(appt.getApptDate().getTime());
 		
 		String text = "Date: " + date.toString() + "\n" +
 					  "Time Slot: " + appt.prettyPrintStart() + " - " + appt.prettyPrintEnd() + "\n" +
 					  "Patient Name: " + patient.getFirst() + " " + patient.getLast() + "\n" +
 					  "Phone Number: " + patient.getPhone() + "\n" +
 					  "Confirmed: " + confirmed;
 		
 		textArea.setText(text);
 		textArea.updateUI();
 	}
 	
 	/**
 	 * Makes the pop up window visible when a patient from the table is selected.
 	 * 
 	 * @param owner - the component that owns this pane (the AppointmentConfirmationListener)
 	 * @param appt  - the appointment information
 	 */
 	public static AppointmentDto ShowDialog(Component owner, AppointmentDto appt, MainWindow main) {
 		displayAppointmentConfirmationUI = new DisplayAppointmentConfirmationUI("View Appointment", appt, main);
 		displayAppointmentConfirmationUI.pack();
 		displayAppointmentConfirmationUI.setLocationRelativeTo(owner);
 		displayAppointmentConfirmationUI.setVisible(true);
 		return appointment;
 	}
 	
 	public void isConfirmedValidate() {
 		if (appointment.getConfirmation()) {
 			confirmButton.setText("<html>Unconfirm</html>");
 		} else {
 			confirmButton.setText("<html>Confirm</html>");
 		}
 	}
 	
 	private final AbstractAction changeConfirmationAction = new AbstractAction("<html>Confirm</html>") {
 		public void actionPerformed(ActionEvent e) {
 			if (!appointment.getConfirmation()) {
 				appointment.setConfirmation(true);
 				DataServiceImpl.GLOBAL_DATA_INSTANCE.confirmAppointment(appointment);
 				confirmButton.setText("<html>Unconfirm</html>");
 			} else {
 				appointment.setConfirmation(false);
 				DataServiceImpl.GLOBAL_DATA_INSTANCE.unConfirmAppointment(appointment);
 				confirmButton.setText("<html>Confirm</html>");
 			}
 			refreshPatientInfo(appointment);
             main.refreshAppointments(appointment.getApptDate());
 		}
 	};
 	
 	/**
 	 * Closes the window once the user hits the "OK" button.
 	 */
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand() == "OK") {
 
                     //TODO: write text box to appointment note
 
 			appointment.setNote(noteArea.getText());
 
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.addNotesToAppointment(appointment);
 			displayAppointmentConfirmationUI.setVisible(false);
 		} else {
 			displayAppointmentConfirmationUI.setVisible(false);
 		}
 	}
 	
 }
