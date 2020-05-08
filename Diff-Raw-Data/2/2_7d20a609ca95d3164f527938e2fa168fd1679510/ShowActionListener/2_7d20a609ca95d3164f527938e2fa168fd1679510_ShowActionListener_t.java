 package frontend.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 
 import service.dto.UserDTO;
 import service.manager.UserManager;
 
 import frontend.NamePanel;
 
 public class ShowActionListener implements ActionListener {
 
 	private NamePanel namePanel;
 	private UserManager uMgr;
 
 	
 	public ShowActionListener(NamePanel namePanel) {
 		this.namePanel = namePanel;
 		uMgr = new UserManager();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		String name = namePanel.getNameTF().getText();
 		
 		JOptionPane.showMessageDialog(null, name);
 		
 		UserDTO dto = new UserDTO();
 		dto.setName(name);				
 		uMgr.saveUserDTO(dto);	
 		
 		namePanel.getNameTF().setText("");
		
 	}
 
 }
