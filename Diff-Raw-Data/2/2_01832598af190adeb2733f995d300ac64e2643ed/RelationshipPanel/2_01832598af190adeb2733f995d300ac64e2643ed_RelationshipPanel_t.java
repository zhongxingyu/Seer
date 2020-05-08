 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 
 import com.sun.jna.Library;
 import com.sun.jna.Memory;
 import com.sun.jna.Native;
 
 public class RelationshipPanel extends JPanel
 {	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JButton logout, add, back;
 	private JLabel instructions;
 	private ButtonGroup group;
 	private JRadioButton friends, family;
 	private MainPanel panel;
 	private boolean fam;
 	private JTextField user;
 	
 	public RelationshipPanel(MainPanel panel)
 	{
 		this.panel = panel;
 		instructions = new JLabel("Type the username, select if they are a\nfriend or family member, and then click add.");
 		
 		user = new JTextField("Username");
 		
 		logout = new JButton("Logout");
 		
 		back = new JButton("Back");
 		
 		group = new ButtonGroup();
 		
 		friends = new JRadioButton("Friend");
 		
 		family = new JRadioButton("Family");
 		
 		group.add(friends);
 		group.add(family);
 		
 		add = new JButton("Add");
 		
 		setPreferredSize(new Dimension(700,400));
 		
 		ChoiceListener log = new ChoiceListener();
 		logout.addActionListener(log);
 		
 		ChoiceListener friend = new ChoiceListener();
 		friends.addActionListener(friend);
 		
 		ChoiceListener famil = new ChoiceListener();
 		family.addActionListener(famil);
 		
 		ChoiceListener addition = new ChoiceListener();
 		add.addActionListener(addition);
 		
 		ChoiceListener bac = new ChoiceListener();
 		back.addActionListener(bac);
 		
 		add(instructions);
 		add(user);
 		add(friends);
 		add(family);
 		add(add);
 		add(back);
 		add(logout);
 	}//end AddPanel constructor
 	
 	public interface CStdLib extends Library 
 	{
         int syscall(int number, Object... args);
     }//end CStdLib interface
 	
 	private class ChoiceListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			if(e.getSource()==logout)
 			{
 				panel.logout();
 			}//end if statement
			else if(e.getSource()== back)
 			{
 				panel.switchUser();
 			}
 			else if(e.getSource()==friends)
 			{
 				fam = false;
 			}//end else statement
 			else if(e.getSource()==family)
 			{
 				fam = true;
 			}//end else if statement	
 			else
 			{
 				CStdLib c = (CStdLib)Native.loadLibrary("c", CStdLib.class);
 				String usern = user.getText();
 				
 				Memory usermem = new Memory(usern.length());
 				usermem.write(0, usern.getBytes(), 0, usern.length());
 				
 				int userlength = Native.toCharArray(usern).length;
 				int exists = c.syscall(287, usermem,userlength);
 				if(exists==1)
 				{
 					User newu = new User(usern);
 					if(panel.getRelations().isFamily(panel.getCurUser(), newu)||panel.getRelations().isFriend(panel.getCurUser(), newu))
 					{
 						if(fam)
 						{
 							panel.getRelations().addFamily(panel.getCurUser(), newu);
 							panel.switchUser();
 						}
 						else
 						{
 							panel.getRelations().addFriend(panel.getCurUser(), newu);
 							panel.switchUser();
 						}
 					}//end if statement
 					else
 					{
 						JOptionPane.showMessageDialog(panel, "That user is already in one of your lists.");
 						panel.switchRelationship();
 					}//end else statement
 				}//end if statement
 				else
 				{
 					JOptionPane.showMessageDialog(panel, "That user does not exist.");
 					panel.switchRelationship();
 				}//end else statement
 			}//end else statement
 		}//end ActionPerformed method
 	}//end RadioListener class
 	
 	public void setPanel(MainPanel panel)
 	{
 		this.panel = panel;
 	}//end panel setter
 }//end NewPanel class
