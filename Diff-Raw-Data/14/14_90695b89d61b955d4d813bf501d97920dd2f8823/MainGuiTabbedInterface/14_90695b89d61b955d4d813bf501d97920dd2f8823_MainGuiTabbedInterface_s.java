 package overwatch.gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 import com.sun.org.apache.bcel.internal.generic.NEW;
 
 public class MainGuiTabbedInterface extends JFrame{
 	
 	private JTabbedPane mainTabbedPane;
 	private JPanel personnel;
 	private JPanel orders;
 
 	
 	
 	
 	public MainGuiTabbedInterface()
 	{
 		PersonnelTab personnelTabReference	 = new PersonnelTab();
 		OrderTab orderTabReference			 = new OrderTab();
 		
 		mainTabbedPane = new JTabbedPane();
 						
 		//Add the tabs to the tabbed pane
		mainTabbedPane.addTab("Personnel", new PersonnelTab() );
 		mainTabbedPane.addTab("Orders", new OrderTab() );
 		
 		//Set the frame up
 		pack();
 		setVisible(true);
 	}
 	
 	
 	
 	
 	//Test
 	public static void main (String[] args)
 	{
 		MainGuiTabbedInterface mainInt = new MainGuiTabbedInterface();
 	}
 	
 
 }
