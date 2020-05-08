 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import com.sun.jna.*;
 	
 public class ViewPanel extends JPanel
 {
 	private static final long serialVersionUID = 1L;
 	private JButton logout, add;
 	private JLabel instructions;
 	private JList photos;
 	private JScrollPane scroll;
 	private String[] data;
 	private MainPanel panel;
 	 
 	public ViewPanel(MainPanel panel, String logString)
 	{	
 		this.panel = panel;
 		instructions = new JLabel("Click on the name of the photo you would like to view\nand it will show up on the right.");
 		
 		logout = new JButton(logString);
 		add = new JButton("Add Photos");
 		
 		setPreferredSize(new Dimension(700,400));
 		
 		ArrayList<String> temp = panel.getControl().getFiles(panel.getCurUser());
 		data = new String[temp.size()];
 		
 		for(int i = 0; i<temp.size();i++)
 		{
 			data[i]=temp.get(i);
 			JOptionPane.showMessageDialog(panel,temp.get(i) + " was added.");
 		}//end for loop
 		
 		JOptionPane.showMessageDialog(panel,"Current user is: " + panel.getCurUser().getUser());
 		
 		photos = new JList(data);
 		photos.setVisible(true);
 		photos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		photos.setVisibleRowCount(10);
 		
 		ListListener listlisten = new ListListener();
 		photos.addListSelectionListener(listlisten);
 		
 		scroll = new JScrollPane(photos);
 		scroll.setPreferredSize(new Dimension(250, 80));
 		
 		LogoutListener log = new LogoutListener();
 		logout.addActionListener(log);
 		
 		LogoutListener ad = new LogoutListener();
 		add.addActionListener(ad);
 		
 		add(instructions);
 		add(scroll);
 		add(add);
 		add(logout);
 	}//end ViewPanel constructor
 	
 	public void setPanel(MainPanel panel)
 	{
 		this.panel = panel;
 	}//end setPanel method
 	
 	public interface CStdLib extends Library 
 	{
         int syscall(int number, Object... args);
     }//end CStdLib interface
 	
 	private class ListListener implements ListSelectionListener
 	{
 		public void valueChanged(ListSelectionEvent e) 
 		{
 			CStdLib c = (CStdLib)Native.loadLibrary("c", CStdLib.class);
 			if (e.getValueIsAdjusting() == false) 
 		    {
 				c.syscall(289, 1);
 				Runtime run = Runtime.getRuntime();
 				try
 		    	{
 		    		Process proc = run.exec("display " + data[photos.getSelectedIndex()]);
 		    	}//end try block
 		    	catch(IOException i)
 		    	{
 		    		i.printStackTrace();
 		    		return;
 		    	}//end catch block
 				c.syscall(289,0);
 		    }//end if statement
 		}//end valueChanged method
 	}//end ButtonListener class
 	
 	private class LogoutListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			if(e.getSource()== logout)
 			{
 				panel.logout();
 			}//end if statement
 			else if (e.getSource()==add)
 			{
				panel.switchAdd();
 			}//end else statement
 		}//end ActionPerformed method
 	}//end ButtonListener class
 }//end ViewPanel class
