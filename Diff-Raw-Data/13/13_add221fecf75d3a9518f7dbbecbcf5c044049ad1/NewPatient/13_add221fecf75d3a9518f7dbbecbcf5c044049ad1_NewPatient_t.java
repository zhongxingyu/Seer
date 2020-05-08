 package com.medicalmanager.views;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
@SuppressWarnings("serial")
 public class NewPatient extends JDialog {
 
 	private final JPanel contentPanel = new JPanel();
 
 
 //			NewPatient dialog = new NewPatient();
 //			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 //			dialog.setVisible(true);
 
 
 	/**
 	 * Create the dialog.
 	 */
 	public NewPatient() {
 		setBounds(100, 100, 450, 300);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setLayout(new FlowLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			{
 				JButton okButton = new JButton("OK");
 				okButton.setActionCommand("OK");
 				buttonPane.add(okButton);
 				getRootPane().setDefaultButton(okButton);
 			}
 			{
 				JButton cancelButton = new JButton("Cancel");
 				cancelButton.setActionCommand("Cancel");
 				buttonPane.add(cancelButton);
 			}
 		}
 	}
 
 }
