 package printClient;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.MenuBar;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Array;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import printClient.Api.Driver;
 
 public class Ui implements ActionListener {
 	static Ui instance;
 
 	JButton loadNextBtn;
 	JComboBox driverComboBox;
 	Api api = new Api();
 	JFrame frame;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				instance = new Ui();
 			}
 		});
 	}
 
 	public Ui() {
 		// Create the window
 		frame = new JFrame("3D Print Client");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		/*
 		// TODO: This needs to integrate better with osx's menu style
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu = new JMenu("File");
 		menuBar.add(menu);
 		JMenuItem changePrinterToken = new JMenuItem("Change Printer Token");
 		menu.add(changePrinterToken);
 		frame.setJMenuBar(menuBar);
 		*/
 		
 		
 		BorderLayout layout = new BorderLayout();
 		frame.getContentPane().setLayout(layout);
 		
 		Container pane = frame.getContentPane();
 
 		JPanel innerPane = new JPanel(new BorderLayout());
 		innerPane.setBorder(BorderFactory.createEmptyBorder(-10, 10, 10, 15));
 		pane.add(innerPane, BorderLayout.CENTER);
 		
 		// Populate the window
 		JLabel text = new JLabel("<html><h2>3D Print Client</h2><p>Your printer is now better.</p><p>Use this wisely.</p></html>");
 		innerPane.add(text, BorderLayout.CENTER);
 
 		JPanel southPane = new JPanel(new BorderLayout());
 		innerPane.add(southPane, BorderLayout.SOUTH);
 
 		loadNextBtn = new JButton("Load Next Print Job");
 		loadNextBtn.addActionListener(this);
 		southPane.add(loadNextBtn, BorderLayout.CENTER);
 
 		String[] names = {"Pronterface", "ReplicatorG"};
 		driverComboBox = new JComboBox( names );
 
 		driverComboBox.setSelectedIndex(api.getDriver().ordinal());
 		driverComboBox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				api.setDriver(Driver.values()[driverComboBox.getSelectedIndex()]);
 			}
 		});
 		southPane.add(driverComboBox, BorderLayout.NORTH);
 
 		// Display the welcome dialog if no token is set
 		if(api.getToken().length() == 0 || api.getToken() == null)
 		{
 			displayWelcomeDialog();
 		}
 		else
 		{
 			displayWindow();
 		}
 		
 	}
 
 	public void displayWindow() {
 		// Display the window
 		frame.pack();
 		frame.setSize(200,250);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
 	public void displayWelcomeDialog() {
 		new WelcomeDialog(frame, api).addComponentListener(new ComponentAdapter() {
 			public void componentHidden(ComponentEvent e)
 			{
 				// Exit the application if no token is set
 				if(api.getToken().length() == 0 || api.getToken() == null)
 				{
 					System.exit(-1);
 				}
 				else
 				{
 					displayWindow();
 				}
 			}
 		});
 	}
	
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		try {
 			// Updating the api and getting the next file
 			System.out.println("Updating the api..");
 			api.finishPrinting();
 			File g_code_file = api.loadNextJob();
 			api.startPrinting();
 			System.out.println("Updating the api.. [ DONE ]");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(frame, "<html><h2>No Print Jobs are ready to print yet</h2><p>Your print queue might be empty or we may still be generating the gcode. Please use your web browser to check online.</p></html>");
 		} catch (Exception e2) {
 			e2.printStackTrace();
 			JOptionPane.showMessageDialog(frame, "<html><h2>Unable to open Driver</h2><p>If you are using the pronterface driver please follow the instructions at https://github.com/D1plo1d/Printrun#installing-dependencies to install all the dependencies for your system.</p></html>");
 		}
 	}
 }
