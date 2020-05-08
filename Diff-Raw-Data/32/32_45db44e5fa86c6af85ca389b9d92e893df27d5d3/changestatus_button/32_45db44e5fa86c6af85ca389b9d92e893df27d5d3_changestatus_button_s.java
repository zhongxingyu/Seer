 package windows.actions.buttons;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComboBox;
 
 import socket.packet.handlers.Send_handler;
 import socket.packet.handlers.sends.StatusSender_handler;
 
 public class ChangeStatus_button implements ActionListener {
 
 	private JComboBox newstatus;
 	public ChangeStatus_button(JComboBox changstatus) {
 		this.newstatus = changstatus;
 	}
 
 	
 	public void actionPerformed(ActionEvent e) 
 	{
 		Integer st = newstatus.getSelectedIndex();
		Send_handler pck = new StatusSender_handler(st);
 		pck.Send();				
 	}
 }
