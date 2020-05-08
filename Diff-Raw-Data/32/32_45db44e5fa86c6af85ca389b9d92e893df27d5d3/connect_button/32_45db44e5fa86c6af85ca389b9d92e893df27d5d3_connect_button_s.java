 package windows.actions.buttons;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import session.Session;
 import socket.packet.handlers.Send_handler;
 import socket.packet.handlers.sends.Connect_handler;
 import java.io.*;
 import thread.threading;
 
 public class Connect_button implements ActionListener {
 	private JComboBox status;
 	private JTextField username;
 	private JPasswordField passwd;
 	private JCheckBox need_save;
 	public Connect_button(JComboBox statusco, JTextField mail, JPasswordField password, JCheckBox save) 
 	{
 		this.status=statusco;
 		this.username=mail;
 		this.passwd=password;
 		need_save = save;
 	}
 	
 	public void actionPerformed(ActionEvent e) 
 	{
 		if(need_save.isSelected())
 			SaveFile();
 		// Store status in session
		Session.setStatus(status.getSelectedIndex() + 1);
 
 		// Launcher socket with server
 		threading.LaunchSender(true);
 		
 		Send_handler pck = new Connect_handler(username.getText() + " " + new String(passwd.getPassword()));
 		if(pck != null)
 			pck.Send();	
 	}
 	
 	public void SaveFile(){
 		String file = "savedvariables";
 		try {
 			FileWriter fw = new FileWriter (file);
 			BufferedWriter bw = new BufferedWriter (fw);
 			PrintWriter wfile = new PrintWriter (bw); 
 			wfile.println (username.getText()); 
 			wfile.println (passwd.getPassword()); 
 			wfile.close();
 		}
 		catch (Exception e){
 			System.out.println(e.toString());
 		}	
 	}
 	
 }
 		
