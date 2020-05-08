 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import model.JukeboxModel;
 import view.JukeboxGUI;
 import view.LoginGUI;
 
 /*Parker Mathewson Homer Glascock IV\
  * hi have a look at this http://www.cs.wcupa.edu/~rkline/java/mvc-design.html*/
 
 public class JukeboxController
 {
 
 	private JukeboxModel theModel = new JukeboxModel();
 	private JukeboxGUI theGUI = new JukeboxGUI();
 	private LoginGUI loginWindow = new LoginGUI();
 	
 	public static void main(String args[])
 	{
 		new JukeboxController();
 	}
 
 	public JukeboxController()
 	{
 		theModel.initStudents();
 
 		loginWindow.addButtonActionListener(new ButtonListener());
 
//		theGUI.setVisible(true);
 		loginWindow.setVisible(true);
 	}
 	
 	private void login()
 	{
 		if(theModel.login(loginWindow.getUsername(), loginWindow.getPassword()))
 		{
 			System.out.println("fuck Yeah");
			theGUI.setVisible(true);
			loginWindow.setVisible(false);
 		}
 		else
 		{
 			loginWindow.displayInvalidLogin();
 		}
 	}
 
 	private class ButtonListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			if(e.getActionCommand().equals("login"))
 			{
 				login();
 			}
 		}
 	}
 }
