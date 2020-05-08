 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 
 import com.sun.jna.*;
 
 public class NewPanel extends JPanel
 {
 	private MainPanel panel;
 	private static final long serialVersionUID = 1L;
 	private JTextField user;
 	private JPasswordField text;
 	private JButton create;
 	private JLabel instructions;
 
 	public NewPanel(MainPanel panel)
 	{
 		this.panel = panel;
 		instructions = new JLabel("Type a new username and password, and hit the 'Create' button.");
 		
 		text = new JPasswordField("Password"); 
 		user = new JTextField("Username");
 
 		create = new JButton("Create");
 		
 		setPreferredSize(new Dimension(700,400));
 		
 		CreateListener creat = new CreateListener();
 		create.addActionListener(creat);
 		
 		add(instructions);
 		add(user);
 		add(text);
 		add(create);
 	}//end NewPanel constructor
 	
 	public void setPanel(MainPanel panel)
 	{
 		this.panel = panel;
 	}//end panel setter
 	
 	public interface CStdLib extends Library 
 	{
         int syscall(int number, Object... args);
     }//end CStdLib interface
 	
 	private class CreateListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			CStdLib c = (CStdLib)Native.loadLibrary("c", CStdLib.class);
 			char[] temp= text.getPassword();
 			String pass = new String(temp);
 			String usern = user.getText();
 				
 			Memory passmem = new Memory(pass.length());
 			passmem.write(0, pass.getBytes(), 0, pass.length());
 			
 			Memory usermem = new Memory(usern.length());
 			usermem.write(0, usern.getBytes(), 0, usern.length());
 			
 			int passlength = Native.toCharArray(pass).length;
 			int userlength = Native.toCharArray(usern).length;
 				
 			int exists = c.syscall(287, usermem,userlength);
 			if(exists!=1)
 			{
 				int success = c.syscall(288, usermem, passmem, userlength, passlength);
 				if(success == 0)
 				{
 					panel.saveControl();
 					panel.saveRelations();
 					System.exit(0);
 				}//end if statement
 				User newu = new User(usern);
 				panel.setCurUser(newu);
 				panel.switchUser();
 			}//end if statement
 			else
 			{
 				JOptionPane.showMessageDialog(panel, "That username already exists.\nPlease choose a new one.");
 				panel.switchNew();
 			}//end else statement
 		}//end ActionPerformed method
 	}//end CreateListener class
 }//end NewPanel class
