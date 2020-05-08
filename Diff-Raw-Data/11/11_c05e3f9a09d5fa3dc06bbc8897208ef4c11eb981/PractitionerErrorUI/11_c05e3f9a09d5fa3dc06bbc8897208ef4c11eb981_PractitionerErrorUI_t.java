 package gui.sub;
 
 import gui.Constants;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 public class PractitionerErrorUI extends JDialog implements ActionListener {	
 	public enum PractitionerTask {
 		CREATENEW,
 		USEEXISTING,
 		REACTIVATE,
 		NONE
 	}
 	
 	private static PractitionerErrorUI practitionerErrorUI;
 	private static PractitionerTask task;
 	
 	private JButton useExistingPractitioner = new JButton("Use Existing Practitioner");
 	private JButton reactivateOldPractitioner = new JButton("Reactivate Old Practitioner");
 	private JButton cancel = new JButton("Cancel");
 	
 	private PractitionerErrorUI(int active) {
 		setModal(true);
 		setTitle("New Practitioner Error");
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(420, 250));
 		
 		JPanel panel = new JPanel(new BorderLayout());
 		JPanel buttonPanel = new JPanel(new FlowLayout());
 		
 		String text = "";
 		if (active == 0) {
 			text = "<b>There is a practitioner who used to work here under " +
 				   "the same credentials.</b> Would you like to reactivate the " +
				   "profile?";
 			
 			reactivateOldPractitioner.setFont(Constants.DIALOG);
 			reactivateOldPractitioner.addActionListener(this);
 			buttonPanel.add(reactivateOldPractitioner);
 			
 		} else {
 			text = "<b>This practitioner already exists.</b> Do you want to use " +
				   "the existing practitioner?";
 			
 			useExistingPractitioner.setFont(Constants.DIALOG);
 			useExistingPractitioner.addActionListener(this);
 			buttonPanel.add(useExistingPractitioner);
 		}
		text += "<br><br>(If you would like to create a new profile, please hit Cancel and " +
				"start over, but give the practitioner a different name.)";
 		
 		cancel.setFont(Constants.DIALOG);
 		cancel.addActionListener(this);
 		buttonPanel.add(cancel);
 		
 		JLabel msg = new JLabel();
 		msg.setFont(Constants.DIALOG);
 		msg.setText("<html>" + text + "</html>");
 		msg.setBorder(new EmptyBorder(20, 20, 20, 20));
 		
 		panel.add(msg, BorderLayout.NORTH);
 		panel.add(buttonPanel, BorderLayout.CENTER);
 		add(panel);	
 	}
 	
 	public static PractitionerTask ShowDialog(int active) {
 		practitionerErrorUI = new PractitionerErrorUI(active);
 		practitionerErrorUI.pack();
 		practitionerErrorUI.setLocationByPlatform(true);
 		practitionerErrorUI.setVisible(true);
 		return task;
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("Use Existing Practitioner")) {
 			task = PractitionerTask.USEEXISTING;
 		} else if (e.getActionCommand().equals("Reactivate Old Practitioner")) {
 			task = PractitionerTask.REACTIVATE;
 		} else {
 			task = PractitionerTask.NONE;
 		}
 		practitionerErrorUI.setVisible(false);
 	}
 }
