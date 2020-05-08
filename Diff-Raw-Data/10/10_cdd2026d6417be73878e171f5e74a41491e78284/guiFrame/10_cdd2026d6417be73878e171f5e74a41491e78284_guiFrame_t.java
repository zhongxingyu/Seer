 import java.awt.Color; 
 
 import java.awt.SystemColor;
 import javax.swing.JFrame;
 import java.awt.CardLayout;
 
 import javax.swing.JPanel;
 
 import javax.swing.UIManager;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.swing.JOptionPane;
 
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import java.awt.Component;
 import java.io.IOException;
 
 import javax.swing.JTabbedPane;
 
 
 
 public class guiFrame {
 	public JFrame frame;
 	private JPanel blank;
 	private JPanel blank_1;
 	private JPanel mainWindow;
 	private JMenu mnNewMenu;
 	private JMenuItem menuSave;
 	private JMenuItem menuLogout;
 	private JPanel blank_2;
 	private JTabbedPane tabbedPane;
 
 
 	/**
 	 * Launch the application.
 	 */
 
 	/**
 	 * Create the application.
 	 */
 	public guiFrame() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setResizable(false);
 		frame.setBounds(700, 350, 694, 565);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new CardLayout(0, 0));
 
 		mainWindow = new JPanel();
 		frame.getContentPane().add(mainWindow, "name_18970707890334");
 		mainWindow.setBackground(SystemColor.textHighlight);
 		mainWindow.setLayout(null);
 
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBorder(UIManager.getBorder("CheckBox.border"));
 		tabbedPane.setBounds(0, 0, 688, 515);
 		mainWindow.add(tabbedPane);
 
 		blank_2 = new JPanel();
 		blank_2.setBackground(Color.WHITE);
 		tabbedPane.addTab("Blank", null, blank_2, null);
 		blank_2.setLayout(null);
 		
 
 		blank = new JPanel();
 		tabbedPane.addTab("Blank", null, blank, null);
 		blank.setBackground(Color.WHITE);
 		blank.setLayout(null);
 		
 
 		blank_1 = new JPanel();
 		tabbedPane.addTab("Blank", null, blank_1, null);
 		blank_1.setLayout(null);
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setBorder(UIManager.getBorder("MenuBar.border"));
 		menuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
 		menuBar.setBackground(Color.WHITE);
 		menuBar.setForeground(Color.BLACK);
 		frame.setJMenuBar(menuBar);
 
 		mnNewMenu = new JMenu("Menu");
 		mnNewMenu.setForeground(Color.BLACK);
 		mnNewMenu.setBackground(Color.WHITE);
 		menuBar.add(mnNewMenu);
 
 		menuSave = new JMenuItem("Save ");
 		mnNewMenu.add(menuSave);
 		menuSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
			
 			}
 		});
 
 
 		menuLogout = new JMenuItem("Logout");
 		mnNewMenu.add(menuLogout);
 		menuLogout.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				frame.dispose();
 				LoginGUI window = new LoginGUI();
 				window.frame.setVisible(true);
 			}
 		});
 
 	}
 }
