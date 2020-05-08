 package ua.vntu.amon.gui;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import ua.vntu.amon.provider.zabbix.ZabbixClient;
 
 public class LoginUI extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	ZabbixClient client = new ZabbixClient();
 
 	ArrayList<String> host = new ArrayList<>();
 
 	public LoginUI() {
 		setTitle("Login Form");
 		getContentPane().setLayout(new FlowLayout());
 		setSize(500, 250);
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		/* GUI Panels */
 		JPanel informationPanel = new JPanel(new GridLayout(3, 2));
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		JPanel errorPanel = new JPanel(new GridLayout(1, 1));
 
 		/* Text Font */
 		Font errorFont = new Font("Verdana", Font.BOLD, 14);
 		Font simpleFont = new Font("Verdana", Font.PLAIN, 14);
 
 		/* Label */
 		final JLabel serverIpLabel = createLabel("Server Ip adress",
 				simpleFont, true);
 		final JLabel userNameLabel = createLabel("Login name", simpleFont, true);
 		final JLabel passwordLabel = createLabel("Password", simpleFont, true);
 		final JLabel errorLabel = createLabel("", errorFont, false);
 		errorLabel.setForeground(Color.RED);
 
 		/* TextField */
 		final JTextField userNameTextField = new JTextField(20);
 		userNameTextField.setText("admin");
 		userNameTextField.setToolTipText("Enter your register user name");
 
 		final JPasswordField passwordField = new JPasswordField(20);
 		passwordField.setText("zabbix");
 		passwordField.setToolTipText("Enter your password ");
 
 		final JTextField serverIpTextField = new JTextField();
 		serverIpTextField.setText("http://192.168.56.101/api_jsonrpc.php");
 		serverIpTextField
 				.setToolTipText("Enter IP adress of Zabbix server, such as 'http://192.168.56.101/api_jsonrpc.php' ");
 
 		/* Buttons */
 		JButton submitButton = createButton("Log In",
 				"Press to login in network monitoring program", simpleFont,
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						// TODO Auto-generated method stub
 						String name = userNameTextField.getText();
 						@SuppressWarnings("deprecation")
 						String password = passwordField.getText();
 						String url = serverIpTextField.getText();
 
 						if ((name.equals("")) && password.equals("")) {
 							errorLabel
 									.setText("Login name and Password are required !!");
 							errorLabel.setVisible(true);
 						} else if ((name.equals(""))) {
 							errorLabel.setText("Login name is required !!");
 							errorLabel.setVisible(true);
 						} else if (password.equals("")) {
 							errorLabel.setText("Password is required !!");
 							errorLabel.setVisible(true);
 						} else {
 							try {
 								client.setUrl(url);
 								client.register(name, password);
 								if (client.getTokenSession() != null) {
 									// create Gui
 									MainUI graphics = new MainUI(
 											name, password, url);
 									graphics.createGUI();
 									setVisible(false);
 								} else {
 									errorLabel
 											.setText("Login name  or password is incorrect !!");
 									errorLabel.setVisible(true);
 								}
 							} catch (Exception e1) {
 								// e1.printStackTrace();
 								errorLabel.setText("HTTP 404 Not Found ");
 								errorLabel.setVisible(true);
 							}
 						}
 					}
 				});
 		
 		try {
			Image img = ImageIO.read(ClassLoader.class.getResourceAsStream("/resources/login_icom.gif"));
 			submitButton.setIcon(new ImageIcon(img));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 
 		JButton resetButton = createButton("Reset",
 				"Press to clean all fields", simpleFont, new ActionListener() {
 
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						// TODO Auto-generated method stub
 						userNameTextField.setText("");
 						passwordField.setText("");
 						errorLabel.setVisible(false);
 						serverIpTextField.setText("");
 					}
 				});
 
 		/* Add components to Panels */
 
 		informationPanel.add(serverIpLabel);
 		informationPanel.add(serverIpTextField);
 		informationPanel.add(userNameLabel);
 		informationPanel.add(userNameTextField);
 		informationPanel.add(passwordLabel);
 		informationPanel.add(passwordField);
 
 		buttonPanel.add(submitButton);
 		buttonPanel.add(resetButton);
 		errorPanel.add(errorLabel);
 
 		getContentPane().add(errorPanel);
 		getContentPane().add(informationPanel);
 		getContentPane().add(buttonPanel);
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent we) {
 				int value = JOptionPane.showConfirmDialog(null,
 						"Are you sure you want to close login form?", "Close",
 						JOptionPane.YES_NO_OPTION);
 				if (value == 0) {
 					System.exit(0);
 				}
 			}
 		});
 		setVisible(true);
 	}
 
 	private JButton createButton(String title, String toolTip, Font font,
 			ActionListener listener) {
 		JButton butoon = new JButton(title);
 		butoon.setToolTipText(toolTip);
 		butoon.setFont(font);
 		butoon.addActionListener(listener);
 		return butoon;
 	}
 
 	private JLabel createLabel(String title, Font font, Boolean visible) {
 		JLabel label = new JLabel(title);
 		label.setVisible(visible);
 		label.setFont(font);
 		return label;
 	}
 
 	public static void main(String args[]) throws Exception {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new LoginUI();
 			}
 		});
 	}
 }
