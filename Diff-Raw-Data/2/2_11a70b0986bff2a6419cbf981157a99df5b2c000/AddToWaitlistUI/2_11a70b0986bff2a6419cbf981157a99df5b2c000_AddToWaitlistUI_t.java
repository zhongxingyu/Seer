 package gui.sub;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.*;
 
 public class AddToWaitlistUI extends JDialog implements ActionListener {
 	private static AddToWaitlistUI addToWaitlistUI;
 	
 	private JButton okButton = new JButton("OK");
 	private JButton cancelButton = new JButton("Cancel");
 	private JButton selectPatientButton = new JButton("Select Patient");
 	
 	private PatientDto patient;
 	private JLabel patientLabel;
 	private JComboBox typeCombo;
 	private JTextArea commentArea;
 	
 	private Font font = new Font("Arial", Font.PLAIN, 16);
 	
 	private static int change = -1; // -1 if canceled, other is the typeID
 	
 	private AddToWaitlistUI(String name) {
 		setModal(true);
 		setTitle(name);
 		
 		setLayout(new BorderLayout());
 		
 		JPanel topPanel = new JPanel(new BorderLayout());
 		
 		JPanel patientPanel = new JPanel(new BorderLayout());
 		patientPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		patientLabel = new JLabel("No Patient Selected", JLabel.CENTER);
 		patientLabel.setFont(new Font("Arial", Font.BOLD, 16));
 		patientPanel.add(patientLabel, BorderLayout.CENTER);
 		
 		topPanel.add(patientPanel, BorderLayout.NORTH);
 		
 		JPanel typePanel = new JPanel(new BorderLayout());
 		typePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		JLabel typeLabel = new JLabel("Select Specific Wait List: ");
 		typeLabel.setFont(font);
 		List<TypeDto> types = DataServiceImpl.GLOBAL_DATA_INSTANCE.getAllPractitionerTypes();
 		typeCombo = new JComboBox(types.toArray());
 		typeCombo.setFont(font);
 		typePanel.add(typeLabel, BorderLayout.NORTH);
 		typePanel.add(typeCombo, BorderLayout.CENTER);
 		
 		topPanel.add(typePanel, BorderLayout.SOUTH);
 		add(topPanel, BorderLayout.NORTH);
 		
 		JPanel commentPanel = new JPanel(new BorderLayout());
 		commentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		JLabel commentLabel = new JLabel("Comments: ");
 		commentLabel.setFont(font);
 		commentArea = new JTextArea();
 		commentArea.setFont(font);
     	commentArea.setLineWrap(true);
     	commentArea.setWrapStyleWord(true);
     	JScrollPane commentPane = new JScrollPane(commentArea);
 		commentPanel.add(commentLabel, BorderLayout.NORTH);
 		commentPanel.add(commentPane, BorderLayout.CENTER);
 		
 		add(commentPanel, BorderLayout.CENTER);
 		
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		buttonPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
 		selectPatientButton.addActionListener(this);
 		selectPatientButton.setActionCommand("select");
 		selectPatientButton.setFont(font);
 		buttonPanel.add(selectPatientButton, BorderLayout.CENTER);
 		okButton.addActionListener(this);
 		okButton.setActionCommand("ok");
 		okButton.setFont(font);
 		buttonPanel.add(okButton);
 		cancelButton.addActionListener(this);
 		cancelButton.setActionCommand("cancel");
 		cancelButton.setFont(font);
 		buttonPanel.add(cancelButton);
 		
 		
 		add(buttonPanel, BorderLayout.SOUTH);
 		
 		setPreferredSize(new Dimension(330, 300));
 		setResizable(false);
 		
 	}
     
 	
 	public static int ShowDialog(Component owner) {
 		addToWaitlistUI = new AddToWaitlistUI("Add to Waitlist");
 		addToWaitlistUI.pack();
 		addToWaitlistUI.setLocationRelativeTo(owner);
 		addToWaitlistUI.setVisible(true);
 		return change;
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		change = -1;
 		if (e.getActionCommand().equals("ok")) {
 			if (patient == null) {
 				JOptionPane.showMessageDialog(this, "Please select a patient.", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} 
 			TypeDto type = (TypeDto)typeCombo.getSelectedItem();
 			String comment = commentArea.getText().replaceAll("[\r\n]+","\t\t"); 
 			List<WaitlistDto> waitlist = DataServiceImpl.GLOBAL_DATA_INSTANCE.getWaitlist();
 			for (int i = 0; i < waitlist.size(); i++) {
 				// If patient is already on the waitlist, give a warning
                                 //TODO: I don't think this works as desired? Might have fixed, come back to
 				if (waitlist.get(i).getPatientID() == patient.getPatID() && waitlist.get(i).getTypeID() == type.getTypeID()) {
 					JLabel errorMsg = new JLabel("This patient has already been added to the waitlist for this type of service.");
 					errorMsg.setFont(font);
					JOptionPane.showMessageDialog(this, errorMsg, "Error!", JOptionPane.ERROR_MESSAGE);
 				    return;
 				} 
 			}
 			DataServiceImpl.GLOBAL_DATA_INSTANCE.addPatientToWaitlist(patient, type, comment);
             change = type.getTypeID();
 		} else if (e.getActionCommand().equals("select")) {
 			patient = SelectPatientUI.ShowDialog(this);
 			if (patient != null) patientLabel.setText(patient.getFirst() + " " + patient.getLast());
 			return;
 		}
 		addToWaitlistUI.setVisible(false);
     }
 	
 }
