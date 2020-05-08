 package hms.views;
 
import hms.Managers.PatientManager;
 
 import javax.swing.JFrame;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.SwingConstants;
 
 public class DeletePatientConfirmationView {
 	
 	public JFrame frmConfirmDeletePatient;
 	private JLabel lblMessage;
 	private JButton btnConfirm;
 	private JButton btnCancel;
 	private String healthcareNumber;
 	
 	/**
 	 * Create the view
 	 * @param healthcareNumber the healthcare number of the patient to be deleted
 	 */
 	public DeletePatientConfirmationView(String healthcareNumber) {
 		initialize();
 		this.healthcareNumber = healthcareNumber;
 		
 		centreWindow(frmConfirmDeletePatient);
 	}
 	
 	public static void centreWindow(JFrame frmConfirmDeletePatient) {
 	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
 	    int x = (int) ((dimension.getWidth() - frmConfirmDeletePatient.getWidth()) / 2);
 	    int y = (int) ((dimension.getHeight() - frmConfirmDeletePatient.getHeight()) / 2);
 	    frmConfirmDeletePatient.setLocation(x, y);
 	}
 	
 	/**
 	 * initialize the window
 	 */
 	public void initialize() {
 		frmConfirmDeletePatient = new JFrame();
 		frmConfirmDeletePatient.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.png")));
 		frmConfirmDeletePatient.setTitle("Confirm Patient Deletion");
 		frmConfirmDeletePatient.setBounds(100, 100, 450, 200);
 		frmConfirmDeletePatient.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		lblMessage = new JLabel("Are you sure you wish to delete this patient?");
 		
 		btnConfirm = new JButton("OK");
 		btnCancel = new JButton("Cancel");
 		
 		btnConfirm.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				PatientManager.doDeletePatient(healthcareNumber);
 				frmConfirmDeletePatient.dispose();
 			}
 		});
 		
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				frmConfirmDeletePatient.dispose();
 			}
 		});
 		
 		GroupLayout groupLayout = new GroupLayout(frmConfirmDeletePatient.getContentPane());
 		
 		groupLayout.setAutoCreateContainerGaps(true);
 		
 		groupLayout.setHorizontalGroup(
 				groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 					.addComponent(lblMessage)
 					.addGroup(groupLayout.createSequentialGroup()
 							.addContainerGap(172, Short.MAX_VALUE)
 							.addComponent(btnConfirm)
 							.addComponent(btnCancel)
 							.addContainerGap(172, Short.MAX_VALUE))
 		);
 		
 		groupLayout.setVerticalGroup(
 				groupLayout.createSequentialGroup()
 					.addGap(40)
 					.addComponent(lblMessage)
 					.addGap(18)
 					.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 							.addComponent(btnConfirm)
 							.addComponent(btnCancel))
 		);
 		
 		frmConfirmDeletePatient.getContentPane().setLayout(groupLayout);
 	}
 	
 	
 }
