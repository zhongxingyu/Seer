 package calculator;
 
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 public class RootFrame extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = -1406044665804707164L;
 	private Container contentPane;
 	private SystemController controller;
 
 	public RootFrame(SystemController controller) {
 		this.controller = controller;
 		setTitle("GPACalc");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(300, 300);
 		setVisible(true);
 		contentPane = getContentPane();
 		contentPane.setLayout(new CardLayout());
 
 		// Menu.
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		// Define and add two drop down menu to the menubar.
 		JMenu fileMenu = new JMenu("File");
 		menuBar.add(fileMenu);
 
 		// Create and add menu item to drop down menus.
 		JMenuItem newAction = new JMenuItem("New");
 		JMenuItem logOut = new JMenuItem("Log out");
 		logOut.setActionCommand("logOut");
 		logOut.addActionListener(this);
 		fileMenu.add(newAction);
 		fileMenu.add(logOut);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String action = e.getActionCommand();
 		if (action.equals("logOut")) {
 			controller.activeUser = null;
 			controller.panels.clear();
 			controller.addPanel(new WelcomePanel(controller), "welcome");
 			controller.showPanel("welcome");
 		}
 	}
 }
