 package gui.sub;
 
 import gui.Constants;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
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
 
 public class EditAppointmentUI extends JDialog implements ActionListener {
 	private static EditAppointmentUI editAppointmentUI;
 	
 	private JButton editButton = new JButton("Edit Patient");
 	private JButton confirmButton = new JButton("Confirm");
 	private JButton okButton = new JButton("Save");
 	private JButton cancelButton = new JButton("Cancel");
 	JCheckBox noShowsCheckBox = new JCheckBox();
 	private JTextArea textArea;
 	private JTextArea noteArea;
 	
 	private static AppointmentDto appointment;
         private MainWindow window;
 	
 	private EditAppointmentUI(String name, AppointmentDto a, MainWindow window) {
 		appointment = a;
         this.window = window;
 		
         setModal(true);
 		setTitle(name);
 		
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(480, 350));
 		setResizable(false);
 	
 		PatientDto patient = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appointment.getPatientID());
 		
 		String text = "Time Slot: " + appointment.prettyPrintStart() + " - " + appointment.prettyPrintEnd();
 		text += "\nPatient Name: " + patient.getFirst() + " " + patient.getLast();
 		text += "\nPhone Number: " + patient.getPhone();
 		text += "\nPatient Note: " + (patient.getNotes()).replaceAll("\t\t", "\n");
 		text += "\nAppointment Confirmed: " + (appointment.getConfirmation() ? "Yes" : "No");
 
 		JPanel textPanel = new JPanel(new BorderLayout());
 		textArea = new JTextArea();
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		textArea.setEditable(false);
 		textArea.setFont(Constants.PARAGRAPH);
 		textArea.setOpaque(false);
 		textArea.setHighlighter(null);
 		textArea.setText(text);
 		
 		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		JLabel noShowsLabel = new JLabel("No Show");
 		noShowsLabel.setFont(Constants.PARAGRAPH);
 		if (appointment.isNoShow()) {
 			noShowsCheckBox.setSelected(true);
 		} else {
 			noShowsCheckBox.setSelected(false);
 		}
 
 		checkBoxPanel.add(noShowsCheckBox);
 		checkBoxPanel.add(noShowsLabel);
 		
 		textPanel.add(textArea, BorderLayout.NORTH);
 		textPanel.add(checkBoxPanel, BorderLayout.CENTER);
 		
 		JPanel notePanel = new JPanel(new BorderLayout());
 		JLabel noteLabel = new JLabel("Appointment Note:");
 		noteLabel.setFont(Constants.PARAGRAPH);
 		JScrollPane notePane = new JScrollPane();
 		notePane.setPreferredSize(new Dimension(200,200));
 		noteArea = new JTextArea();
 		noteArea.setLineWrap(true);
 		noteArea.setWrapStyleWord(true);
 		noteArea.setFont(Constants.PARAGRAPH);
 
 		noteArea.setText((a.getNote()).replaceAll("\t\t", "\n"));
 		notePane.setViewportView(noteArea);
 		notePanel.add(noteLabel, BorderLayout.NORTH);
 		notePanel.add(notePane, BorderLayout.CENTER);
 
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		editButton.addActionListener(this);
 		editButton.setActionCommand("edit");
 		editButton.setFont(Constants.DIALOG);
 		buttonPanel.add(editButton);
 		
 		confirmButton.setAction(changeConfirmationAction);
 		isConfirmedValidate();
 		confirmButton.setActionCommand("confirm");
 		confirmButton.setFont(Constants.DIALOG);
 		buttonPanel.add(confirmButton);
 
 		okButton.addActionListener(this);
 		okButton.setActionCommand("save");
 		okButton.setFont(Constants.DIALOG);
 		buttonPanel.add(okButton);
 		
 		cancelButton.addActionListener(this); 
 		cancelButton.setActionCommand("cancel");
 		cancelButton.setFont(Constants.DIALOG);
 		buttonPanel.add(cancelButton);
 		
 		// Add borders to all panels
 		textPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		notePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		
 		//add(textArea, BorderLayout.NORTH);
 		add(textPanel, BorderLayout.NORTH);
 		add(notePanel, BorderLayout.CENTER);
 		add(buttonPanel, BorderLayout.SOUTH);
 		
 		setResizable(false);
 		
 	}
     
 	
 	public static AppointmentDto ShowDialog(Component owner, AppointmentDto a, MainWindow w) {
 		editAppointmentUI = new EditAppointmentUI("Appointment Details", a, w);
 		editAppointmentUI.pack();
 		editAppointmentUI.setLocationRelativeTo(owner);
 		editAppointmentUI.setVisible(true);
 		return appointment;
 	}
 	
 	private void refreshPatientInfo(AppointmentDto appt) {
 		PatientDto patient = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appt.getPatientID());
 		
 		String text = "Time Slot: " + appointment.prettyPrintStart() + " - " + appointment.prettyPrintEnd();
 		text += "\nPatient Name: " + patient.getFirst() + " " + patient.getLast();
 		text += "\nPhone Number: " + patient.getPhone();
 		text += "\nPatient Note: " + (patient.getNotes()).replaceAll("\t\t", "\n");
 		text += "\nAppointment Confirmed: " + (appointment.getConfirmation() ? "Yes" : "No");
 		
 		textArea.setText(text);
 		textArea.updateUI();
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
 		}
 	};
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("save")) {
 			// Saves the noShow status
 			if (noShowsCheckBox.isSelected() && !appointment.isNoShow()) {
 				DataServiceImpl.GLOBAL_DATA_INSTANCE.checkAsNoShow(appointment);
 			} else if (!noShowsCheckBox.isSelected() && appointment.isNoShow()) {
 				DataServiceImpl.GLOBAL_DATA_INSTANCE.uncheckAsNoShow(appointment);
 			} else {
 				// nothing needs to be done
 			}
 		} else if (e.getActionCommand().equals("edit")) {
 			PatientDto patient = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appointment.getPatientID());
 			PatientDto editedPatient = EditPatientUI.ShowDialog(this, patient);
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.addPatientToAppointment(editedPatient.getPatID(), appointment);
                         window.refreshConfirmationPane();
             refreshPatientInfo(appointment);
 			return;
 		} else if (e.getActionCommand().equals("cancel")) {
 			editAppointmentUI.setVisible(false);
 			return;
 		}
 		appointment.setNote((noteArea.getText()).replaceAll("[\r\n]+", "\t\t"));
 		editAppointmentUI.setVisible(false);
     }
 }
