 package gui.sub;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
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
 
 public class EditAppointmentUI extends JDialog implements ActionListener, ItemListener {
 	private static EditAppointmentUI editAppointmentUI;
 	
 	private JButton okButton = new JButton("Save");
 	private JButton editButton = new JButton("Edit Patient");
 	private JButton cancelButton = new JButton("Cancel");
 	JCheckBox noShowsCheckBox = new JCheckBox();
 	private JTextArea textArea;
 	private JTextArea noteArea;
 	private Font font= new Font("Tahoma",Font.PLAIN,14);
 	
 	private static AppointmentDto appointment;
 	
 	private EditAppointmentUI(String name, AppointmentDto a) {
 		appointment = a;
 		setModal(true);
 		setTitle(name);
 		
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(350, 350));
 		setResizable(false);
 	
 		PatientDto patient = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appointment.getPatientID());
 		
 		String text = "Time Slot: " + appointment.getStart().toString() + " - " + appointment.getEnd().toString();
 		text += "\nPatient Name: " + patient.getFirst() + " " + patient.getLast();
 		text += "\nPhone Number: " + patient.getPhone();
 		text += "\nPatient Note: " + (patient.getNotes()).replaceAll("\t\t", "\n"); // TODO: NEED TO DELETE REPLACEALL?
 
 		JPanel textPanel = new JPanel(new BorderLayout());
 		textArea = new JTextArea();
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		textArea.setEditable(false);
 		textArea.setFont(font);
 		textArea.setOpaque(false);
 		textArea.setHighlighter(null);
 		textArea.setText(text);
 		
 		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		JLabel noShowsLabel = new JLabel("No Show");
 		noShowsLabel.setFont(font);
 		checkBoxPanel.add(noShowsCheckBox);
 		checkBoxPanel.add(noShowsLabel);
 		
 		textPanel.add(textArea, BorderLayout.NORTH);
 		textPanel.add(checkBoxPanel, BorderLayout.CENTER);
 		
 		JPanel notePanel = new JPanel(new BorderLayout());
 		JLabel noteLabel = new JLabel("Appointment Note:");
 		noteLabel.setFont(font);
 		JScrollPane notePane = new JScrollPane();
 		notePane.setPreferredSize(new Dimension(200,200));
 		noteArea = new JTextArea();
 		noteArea.setLineWrap(true);
 		noteArea.setWrapStyleWord(true);
 		noteArea.setFont(font);
 
 		noteArea.setText((a.getNote()).replaceAll("\t\t", "\n"));
 		notePane.setViewportView(noteArea);
 		notePanel.add(noteLabel, BorderLayout.NORTH);
 		notePanel.add(notePane, BorderLayout.CENTER);
 
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		editButton.addActionListener(this);
 		editButton.setActionCommand("edit");
 		editButton.setFont(font);
 		buttonPanel.add(editButton);
 
 		okButton.addActionListener(this);
 		okButton.setActionCommand("save");
 		okButton.setFont(font);
 		buttonPanel.add(okButton);
 		cancelButton.addActionListener(this);
 		cancelButton.setActionCommand("cancel");
 		cancelButton.setFont(font);
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
     
 	
 	public static AppointmentDto ShowDialog(Component owner, AppointmentDto a) {
 		editAppointmentUI = new EditAppointmentUI("Appointment Details", a);
 		editAppointmentUI.pack();
 		editAppointmentUI.setLocationRelativeTo(owner);
 		editAppointmentUI.setVisible(true);
 		return appointment;
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("save")) {
 			// Doesn't actually do anything important - edit patient saves any edits to patient info
 			// and notes are saved below
 		} else if (e.getActionCommand().equals("edit")) {
 			PatientDto patient = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appointment.getPatientID());
 			PatientDto editedPatient = EditPatientUI.ShowDialog(this, patient);
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.addPatientToAppointment(editedPatient.getPatID(), appointment);
 		} else if (e.getActionCommand().equals("cancel")) {
 			editAppointmentUI.setVisible(false);
 			return;
 		}
 		appointment.setNote((noteArea.getText()).replaceAll("[\r\n]+", "\t\t"));
 		editAppointmentUI.setVisible(false);
     }
 
 	public void itemStateChanged(ItemEvent e) {
 		if (e.getStateChange() == ItemEvent.SELECTED) {
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.checkAsNoShow(appointment);
 		} else {
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.uncheckAsNoShow(appointment);
 		}
 	}
 }
