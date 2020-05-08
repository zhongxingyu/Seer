 package com.rosenthal.ramchat;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
 
 public class Login extends JFrame {
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane;
 	private JTextField txtUsername;
 	private JTextField txtIpAddress;
 	private JTextField txtPort;
 	private JLabel lblIpAddress;
 	private JLabel lblPort;
 	private JLabel lblExampleIp;
 	private JLabel lblExamplePort;
 
 	/**
 	 * This method Creates the elements in the window for the game
 	 * <p>
 	 * It is instantiated in the main class
 	 * 
 	 * @author Andrew Rosenthal
 	 * @param none
 	 */
 
 	public Login() {
 
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		setAlwaysOnTop(true);
 		setResizable(false);
 		setTitle("Login to Ram Chat");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(300, 290);
 		setLocationRelativeTo(null);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		txtUsername = new JTextField();
		txtUsername.setBounds(66, 103, 165, 28);
 		contentPane.add(txtUsername);
 		txtUsername.setColumns(10);
 
 		JLabel lblEnterUsername = new JLabel("Choose Username:");
		lblEnterUsername.setBounds(81, 26, 150, 23);
 		contentPane.add(lblEnterUsername);
 
 		txtIpAddress = new JTextField();
 		txtIpAddress.setColumns(10);
		txtIpAddress.setBounds(66, 48, 165, 28);
 		contentPane.add(txtIpAddress);
 
 		txtPort = new JTextField();
 		txtPort.setColumns(10);
		txtPort.setBounds(66, 187, 165, 28);
 		contentPane.add(txtPort);
 
 		lblIpAddress = new JLabel("IP Address:");
		lblIpAddress.setBounds(104, 79, 80, 16);
 		contentPane.add(lblIpAddress);
 
 		lblPort = new JLabel("Port:");
		lblPort.setBounds(135, 171, 36, 16);
 		contentPane.add(lblPort);
 
 		lblExampleIp = new JLabel("(Example: 192.168.1.1)");
		lblExampleIp.setBounds(66, 143, 177, 16);
 		contentPane.add(lblExampleIp);
 
 		lblExamplePort = new JLabel("(Example: Port: 1337)");
		lblExamplePort.setBounds(66, 227, 165, 16);
 		contentPane.add(lblExamplePort);
 
 		JButton btnLogin = new JButton("Login");
 		btnLogin.addActionListener(new ActionListener() {
 			
 			/**
 			 * Checks to see if an action is performed and logs variables if
 			 * there is an action
 			 * <p>
 			 * <b>starts the actual login method</b>
 			 * 
 			 * @see login(string, string, int)
 			 * @author Andrew Rosenthal
 			 * @implements ActionEvent Interface
 			 */
 
 			public void actionPerformed(ActionEvent e) {
 				String name = txtUsername.getText();
 				String ipAddress = txtIpAddress.getText();
 				int port = Integer.parseInt(txtPort.getText());
 				login(name, ipAddress, port);
 			}
 		});
		btnLogin.setBounds(92, 255, 127, 23);
 		contentPane.add(btnLogin);
 	}
 
 	/**
 	 * Logs you into the game!
 	 */
 
 	private void login(String name, String address, int port) {
 		dispose();
 		new Client(name, address, port);
 	}
 
 	/**
 	 * Main method that starts at launch
 	 * 
 	 * @param args
 	 */
 	
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Login frame = new Login();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 }
