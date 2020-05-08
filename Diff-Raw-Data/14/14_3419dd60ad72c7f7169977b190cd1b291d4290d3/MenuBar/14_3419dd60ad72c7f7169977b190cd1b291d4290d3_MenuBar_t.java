 package vms.gui;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 
 public class MenuBar extends JMenuBar implements ActionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8761848412891724604L;
 	private JFrame _ParentWindow;
 	private MainGUI _Main;
 	
 	public MenuBar(final JFrame window, final MainGUI main) {
 		_ParentWindow = window;
 		_Main = main;
 		add(createFileMenu());
 	}
 
 	public JMenu createFileMenu() {
 		//Build the first menu.
 		JMenu menu = new JMenu("File");
 		
 		JMenuItem menuItem = new JMenuItem("Exit");
 		menuItem.addActionListener(this);
 		menuItem.getAccessibleContext().setAccessibleDescription("Exits the program.");
 		
 		JMenuItem menuItem2 = new JMenuItem("Logout");
 		menuItem2.addActionListener(this);
 		menuItem2.getAccessibleContext().setAccessibleDescription("Log out of program.");
 		
 		menu.add(menuItem2);
 		menu.add(menuItem);
 
 		return menu;
 	}
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if (arg0.getActionCommand().compareToIgnoreCase("Logout") == 0 ) {
			_ParentWindow.setVisible(false);
 			_Main.showLogin();
 		}
 		else if (arg0.getActionCommand().compareToIgnoreCase("Exit") == 0) {
 			WindowEvent windowClosing = new WindowEvent(_ParentWindow, WindowEvent.WINDOW_CLOSING);
 			_ParentWindow.dispatchEvent(windowClosing);			
 		}
 		
 	}
 }
  
