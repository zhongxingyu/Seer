 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import java.awt.*;
 import java.awt.event.*;
 
 
     
 /**
  * AboutDialog
  * -----------
  * @author Philipp Schieferdecker
  *
  * Display information about the application.
  */
 public class AboutDialog extends JDialog
 {
     //
     // member data
     //
 
     /** GUI components */
     JEditorPane jEditorPaneAbout      = new JEditorPane();
     JButton     jButtonOk             = new JButton();
     JTextField  jTextFieldApplication = new JTextField();
     JTextField  jTextFieldVersion     = new JTextField();
     
     
     //
     // construction
     //
     
     /** standard constructor */
     public AboutDialog(JFrame frame)
     {
 	super(frame,true);
 	
 	setTitle("About ConfDbGUI");
 	
 	setContentPane(initComponents());
 	
 	String txt =
 	    "<p>Thank you for using <b>ConfDbGUI</b>," +
 	    "a CMS tool to create and manage " +
 	    "CMSSW job configurations based on a " +
 	    "relational database.</p>" +
 	    
 	    "This software was originally developed " +
 	    "by Philipp Schieferdecker and is currently " +
 	    "maintained by Raul Jimenez Estupinan " +
 	    "with contributions from Ulf Behrens, " +
 	    "Jonathan Hollar, Vasundhara Chetluru " +
 	    "and Martin Gruenewald. " +
 	    "<p>For feedback please contact me at " +
 	    "<b>raul.jimenez.estupinan@cern.ch</b>.</p>" +
 	    
 	    
 	    "<p>Find documentation on the web under " +
 	    "<b>https://twiki.cern.ch/twiki/bin/view/CMS/EvfConfDBGUI</b>.</p>";
 
 	jTextFieldApplication.setText("ConfDbGUI");
	jTextFieldVersion.setText("V01-05-70");
 
 	jEditorPaneAbout.setContentType("text/html");
 	jEditorPaneAbout.setText(txt);
 	
     }
     
     
     //
     // member functions
     //
 
     /** close the dialog window if 'OK' was pressed */
     public void jButtonOkActionPerformed(ActionEvent e) { setVisible(false); }
     
     
     //
     // private member functions
     //
 
     /** initialize GUI components */
     private JPanel initComponents()
     {
 	JPanel      panel        = new JPanel();
         JScrollPane jScrollPane1 = new JScrollPane();
         JLabel      jLabel1      = new JLabel();
         JLabel      jLabel2      = new JLabel();
 	
         jScrollPane1.setViewportView(jEditorPaneAbout);
 	
 
 	jButtonOk.addActionListener( new ActionListener() {
 		public void actionPerformed(ActionEvent e)
 		{
 		    jButtonOkActionPerformed(e);
 		}
 	    });
 	
         jButtonOk.setText("OK");	
         jLabel1.setText("Application:");
         jLabel2.setText("Version:");
 	
 	jTextFieldApplication.setBackground(new Color(255, 255, 255));
         jTextFieldApplication.setEditable(false);
         jTextFieldApplication.setBorder(BorderFactory
 					.createBevelBorder(BevelBorder.RAISED));
 
         jTextFieldVersion.setBackground(new Color(255, 255, 255));
         jTextFieldVersion.setEditable(false);
         jTextFieldVersion.setBorder(BorderFactory
 				    .createBevelBorder(BevelBorder.RAISED));
 
 	jEditorPaneAbout.setEditable(false);
         
 	org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(panel);
         panel.setLayout(layout);
         layout
 	    .setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout
 								     .LEADING)
 					  .add(layout.createSequentialGroup()
 					       .addContainerGap()
 					       .add(layout
 						    .createParallelGroup(org.jdesktop.layout.GroupLayout
 									 .LEADING)
 						    .add(jScrollPane1,
 							 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 							 289, Short.MAX_VALUE)
 						    .add(layout
 							 .createSequentialGroup()
 							 .add(layout
 							      .createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 							      .add(jTextFieldApplication,
 								   org.jdesktop.layout.GroupLayout
 								   .PREFERRED_SIZE,
 								   145,
 								   org.jdesktop.layout.GroupLayout
 								   .PREFERRED_SIZE)
 							      .add(jLabel1))
 							 .add(4, 4, 4)
 							 .add(layout
 							      .createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 							      .add(jLabel2)
 							      .add(jTextFieldVersion,
 								   org.jdesktop.layout.GroupLayout
 								   .DEFAULT_SIZE,
 								   140,
 								   Short
 								   .MAX_VALUE)))))
 					  .add(layout.createSequentialGroup()
 					       .add(106, 106, 106)
 					       .add(jButtonOk,
 						    org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 						    101, Short.MAX_VALUE)
 					       .add(94, 94, 94)))
 				       .addContainerGap())
 				);
         layout
 	    .setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			      .add(layout.createSequentialGroup()
 				   .addContainerGap()
 				   .add(layout
 					.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					.add(jLabel1)
 					.add(jLabel2))
 				   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				   .add(layout
 					.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					.add(jTextFieldApplication,
 					     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 					     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 					     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 					.add(jTextFieldVersion,
 					     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 					     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 					     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				   .add(jScrollPane1,
 					org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 					300,
 					Short.MAX_VALUE)
 				   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				   .add(jButtonOk)
 				   .addContainerGap())
 			      );
 	return panel;
     }
     
 }
