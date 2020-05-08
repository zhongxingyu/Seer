 package userinterface;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import utilities.ui.ImageHandler;
 
 public class DashboardPanel extends JPanel implements ActionListener {
 	private static final long serialVersionUID = 1L;
 	JButton logoutButton;
 //-----------------------------------------------------------------------------	
 	public DashboardPanel(){
 		super();
 		
 		ImageIcon logoIcon = ImageHandler.createImageIcon("images/ProgramLogo.png");
 		JLabel logo = new JLabel(logoIcon);
 		
		add(logo);
 		
		logoutButton = new JButton("Logout");
 		logoutButton.addActionListener(this);
 		logoutButton.setActionCommand("logout");
 		add(logoutButton, BorderLayout.EAST);
 	}
 //-----------------------------------------------------------------------------	
 	public void actionPerformed(ActionEvent e) 
 	{
 		String command = e.getActionCommand();
 		if(command.equals("logout"))
 		{
 			System.out.println("Logout Fired");
 			System.gc();
 			String args[] = new String[1];
 			// calls public method in Core that disposes main frame
 			program.Core.invalidateFrames();
 			// runs main in Core again
 			program.Core.main(args);
 		}
 	}
 }
