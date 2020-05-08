 package kpsmart;
 
 import java.awt.BorderLayout;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import priority.Priority;
import routes.DistributionCentre;
 
 import gui.*;
 import backend.*;
 
 public class KPSmart implements ActionListener{
 	
 	KPSBackend kBackend;
 	KPSFrame kFrame;
 	JPasswordField kPasswordField;
 	
 	public KPSmart() {
 	
 		kBackend = new KPSBackend();
 		kFrame = new KPSFrame(this);
 		
 		kPasswordField = new JPasswordField(10);
 		//KPSpasswordField.setActionCommand("OK");
 		//KPSpasswordField.addActionListener(this);
 	
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		System.out.println(e.getActionCommand());
 		
 		//FILE OPTIONS
 		if ("New".equals(e.getActionCommand())) {
 			System.out.println(kBackend.testMethod());
 			return;
 		}
 		
 		
 		//EDIT OPTIONS
 		
 		//MAIL OPTIONS
 		if ("Send Mail".equals(e.getActionCommand())) {
 			//JOptionPane.showMessageDialog(kFrame, "Theoretically, you are sending new mail.");
 			
 			//prompt for data
 			
 			//create and send
 			kFrame.sendMail();
			kBackend.sendMail(12345, 0, 0, new DistributionCentre("Christchurch", "Christchurch", "New Zealand", 20, 20), new DistributionCentre("Auckland", "Auckland", "New Zealand", 60, 10), Priority.DOMESTIC);
 			return;
 		}
 		
 		//VIEW OPTIONS
 		if ("Sign out as manager".equals(e.getActionCommand())) {
 			kFrame.notManager();
 			return;
 		}
 		
 		if ("Sign in as manager".equals(e.getActionCommand())) {
 			kPasswordField = new JPasswordField(10);
 			JOptionPane.showMessageDialog(kFrame, kPasswordField, "Password Required", JOptionPane.WARNING_MESSAGE);
 		//}
 		
 			String pass = String.valueOf(kPasswordField.getPassword());
 			if(kBackend.authenticateManager(pass)) {
 				kFrame.manager();
 				System.out.println("MANAGER MODE");
 				return;
 			}
 			
 		}
 		
 		//HELP OPTIONS
 		if ("About KPSSmart".equals(e.getActionCommand())) {
 			JOptionPane.showMessageDialog(kFrame, "KPSSmart by\n  Nick Malcolm\n  Liam O'Connor\n " +
 					" Janella Espinas\n  Sean Arnold\n  Robert Crowe");
 			return;
 		}
 	}
 }
