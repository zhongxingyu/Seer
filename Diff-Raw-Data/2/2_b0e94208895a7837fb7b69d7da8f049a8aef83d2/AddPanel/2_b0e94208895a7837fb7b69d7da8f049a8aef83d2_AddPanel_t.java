 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.io.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import com.sun.jna.*;
 
 public class AddPanel extends JPanel
 {
 	private static final long serialVersionUID = 1L;
 	private JButton logout, choose, view;
 	private JLabel instructions;
 	private ButtonGroup group;
 	private JRadioButton level1, level2, level3, level4;
 	private JFileChooser chooser;
 	private FileNameExtensionFilter filter;
 	private JFrame chooseframe;
 	private MainPanel panel;
 	private int curlevel;
 	 
 	public AddPanel(MainPanel panel)
 	{
 		this.panel = panel;
 		
 		chooser = new JFileChooser();
 	    filter = new FileNameExtensionFilter("JPG images","jpg");
 	    chooser.setFileFilter(filter);
 	    
 	    chooseframe = new JFrame();
 		
 		instructions = new JLabel("Please first select a privacy level for the photo,\nand then click the 'Choose Photo' button to select\na photo to add.");
 		
 		logout = new JButton("Logout");
 		
 		group = new ButtonGroup();
 		
 		level1 = new JRadioButton("Level 1: Only Me");
 		level1.setSelected(true);
 		
 		level2 = new JRadioButton("Level 2: Friends and I");
 		
 		level3 = new JRadioButton("Level 3: Family, Friends and I");
 		
 		level4 = new JRadioButton("Level 4: Everyone");
 		
 		group.add(level1);
 		group.add(level2);
 		group.add(level3);
 		group.add(level4);
 		
 		choose = new JButton("Choose Photo");
 		
 		view = new JButton("View Photos");
 		
 		setPreferredSize(new Dimension(700,400));
 		
 		RadioListener log = new RadioListener();
 		logout.addActionListener(log);
 		
 		RadioListener lev1 = new RadioListener();
 		level1.addActionListener(lev1);
 		
 		RadioListener lev2 = new RadioListener();
 		level2.addActionListener(lev2);
 		
 		RadioListener lev3 = new RadioListener();
 		level3.addActionListener(lev3);
 		
 		RadioListener lev4 = new RadioListener();
 		level4.addActionListener(lev4);
 		
 		RadioListener choice = new RadioListener();
 		choose.addActionListener(choice);
 		
 		RadioListener vie = new RadioListener();
 		view.addActionListener(vie);
 		
 		add(instructions);
 		add(level1);
 		add(level2);
 		add(level3);
 		add(level4);
 		add(choose);
 		add(view);
 		add(logout);
 	}//end AddPanel constructor
 	
 	public void setPanel(MainPanel panel)
 	{
 		this.panel = panel;
 	}//end setPanel method
 	
 	public interface CStdLib extends Library 
 	{
         int syscall(int number, Object... args);
     }//end CStdLib interface
 	
 	private class RadioListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			CStdLib c = (CStdLib)Native.loadLibrary("c", CStdLib.class);
 			if(e.getSource()==logout)
 			{
 				panel.logout();
 			}//end if statement
 			else if(e.getSource()==level1)
 			{
 				curlevel = 1;
 			}//end else statement
 			else if(e.getSource()==level2)
 			{
 				curlevel = 2;
 			}//end else if statement
 			else if(e.getSource()==level3)
 			{
 				curlevel = 3;
 			}//end else if statement
 			else if(e.getSource()==level4)
 			{
 				curlevel = 4;	
 			}//end else if statement
 			else if(e.getSource()==view)
 			{
 				panel.switchView(false);
 			}
 			else
 			{
 				int returnVal = chooser.showOpenDialog(chooseframe);
 			    if(returnVal == JFileChooser.APPROVE_OPTION) 
 			    {
 			    	File file = chooser.getSelectedFile();
 			    	c.syscall(289,true);
			    	file.renameTo(new File("//nethome//kpowell32//proj4//example//mountdir"));
 			    	c.syscall(289,false);
 			    	if(curlevel==1)
 			    	{
 			    		panel.getControl().addFile(file.getPath(), panel.getCurUser(), false, false, false);
 			    	}//end if statement
 			    	else if(curlevel==2)
 			    	{
 			    		panel.getControl().addFile(file.getPath(), panel.getCurUser(), true, false, false);
 			    	}//end else if statement
 			    	else if(curlevel==3)
 			    	{
 			    		panel.getControl().addFile(file.getPath(), panel.getCurUser(), true, true, false);
 			    	}//end else if statement
 			    	else if(curlevel==4)
 			    	{
 			    		panel.getControl().addFile(file.getPath(), panel.getCurUser(), true, true, true);
 			    	}//end else if statement
 			       JOptionPane.showMessageDialog(panel, "The File has been added. Please add another file or logout.");
 			    }//end if statement
 			    panel.switchAdd();
 			}//end else statement
 		}//end ActionPerformed method
 	}//end RadioListener class
 }//end AddPanel class
