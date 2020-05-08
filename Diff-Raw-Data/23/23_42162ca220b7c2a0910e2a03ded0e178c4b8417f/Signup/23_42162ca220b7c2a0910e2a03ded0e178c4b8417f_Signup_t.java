 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 
 public class Signup extends JFrame {
 
 	JFrame frame;
 	Container content;
 	JPanel userPanel;
 	JLabel userLabel;
 	JPanel passPanel;
 	JLabel passLabel;
 	JTextField passField;
 	JTextField userField;
 	JPanel namePanel;
 	JLabel nameLabel;
 	JPanel addressPanel;
 	JLabel addressLabel;
 	JPanel statePanel;
 	JLabel stateLabel;
 	JTextField stateTextField;
 	JPanel phoneNumPanel;
 	JLabel phoneNumLabel;
 	JTextField phoneNumField;
 	JPanel mainPanel;
 	JTextField nameTextField;
 	JTextField addressTextField;
 	JPanel emailPanel;
 	JLabel emailLabel;
 	JTextField emailTextField;
 	JPanel ssnPanel;
 	JLabel ssnLabel;
 	JTextField ssnTextField;
 	JButton done;
 	JPanel one;
 	JPanel two;
 	JPanel three;
 	JPanel four;
 	JPanel five;
 	JPanel six;
 	JPanel seven;
 
 	DB db;
 	int id;
 
 	public Signup(String username, String password, DB mydb, int myid) {
 
 		frame = new JFrame("Sign Up Information");
 		content = frame.getContentPane();
 
 		db = mydb;
 		id = myid;
 
 		done = new JButton("Sign Up");
 
 		userPanel = new JPanel(new BorderLayout());
 		userLabel = new JLabel("Username: ");
 		userLabel.setDisplayedMnemonic(KeyEvent.VK_U);
 		userField = new JTextField();
 		userField.setText(username);
 		userLabel.setLabelFor(userField);
 		userPanel.add(userLabel, BorderLayout.WEST);
 		userPanel.add(userField, BorderLayout.CENTER);
 
 		passPanel = new JPanel(new BorderLayout());
 		passLabel = new JLabel("Password: ");
 		passLabel.setDisplayedMnemonic(KeyEvent.VK_U);
 		passField = new JTextField();
 		passField.setText(password);
 		passLabel.setLabelFor(userField);
 		passPanel.add(passLabel, BorderLayout.WEST);
 		passPanel.add(passField, BorderLayout.CENTER);
 
 		namePanel = new JPanel(new BorderLayout());
 		nameLabel = new JLabel("First and Last Name: ");
 		nameLabel.setDisplayedMnemonic(KeyEvent.VK_U);
 		nameTextField = new JTextField();
 		nameLabel.setLabelFor(nameTextField);
 		namePanel.add(nameLabel, BorderLayout.WEST);
 		namePanel.add(nameTextField, BorderLayout.CENTER);
 
 		addressPanel = new JPanel(new BorderLayout());
 		addressLabel = new JLabel("Address: ");
 		addressLabel.setDisplayedMnemonic(KeyEvent.VK_U);
 		addressTextField = new JTextField();
 		addressLabel.setLabelFor(addressTextField);
 		addressPanel.add(addressLabel, BorderLayout.WEST);
 		addressPanel.add(addressTextField, BorderLayout.CENTER);
 
 		statePanel = new JPanel(new BorderLayout());
 		stateLabel = new JLabel("State: ");
 		stateLabel.setDisplayedMnemonic(KeyEvent.VK_U);
 		stateTextField = new JTextField();
 		stateLabel.setLabelFor(stateTextField);
 		statePanel.add(stateLabel, BorderLayout.WEST);
 		statePanel.add(stateTextField, BorderLayout.CENTER);
 
 		phoneNumPanel = new JPanel(new BorderLayout());
 		phoneNumLabel = new JLabel("Phone Number: ");
 		phoneNumLabel.setDisplayedMnemonic(KeyEvent.VK_U);
 		phoneNumField = new JTextField();
 		phoneNumLabel.setLabelFor(phoneNumField);
 		phoneNumPanel.add(phoneNumLabel, BorderLayout.WEST);
 		phoneNumPanel.add(phoneNumField, BorderLayout.CENTER);
 
 		emailPanel = new JPanel(new BorderLayout());
 		emailLabel = new JLabel("Email: ");
 		emailLabel.setDisplayedMnemonic(KeyEvent.VK_U);
 		emailTextField = new JTextField();
 		emailLabel.setLabelFor(emailTextField);
 		emailPanel.add(emailLabel, BorderLayout.WEST);
 		emailPanel.add(emailTextField, BorderLayout.CENTER);
 
 		ssnPanel = new JPanel(new BorderLayout());
 		ssnLabel = new JLabel("SSN: ");
 		ssnLabel.setDisplayedMnemonic(KeyEvent.VK_U);
 		ssnTextField = new JTextField();
 		ssnLabel.setLabelFor(ssnTextField);
 		ssnPanel.add(ssnLabel, BorderLayout.WEST);
 		ssnPanel.add(ssnTextField, BorderLayout.CENTER);
 
 		one = new JPanel(new BorderLayout());
 		one.add(userPanel, BorderLayout.NORTH);
 		one.add(passPanel, BorderLayout.SOUTH);
 
 		two = new JPanel(new BorderLayout());
 		two.add(namePanel, BorderLayout.NORTH);
 		two.add(addressPanel, BorderLayout.SOUTH);
 
 		three = new JPanel(new BorderLayout());
 		three.add(one, BorderLayout.NORTH);
 		three.add(two, BorderLayout.SOUTH);
 
 		four = new JPanel(new BorderLayout());
 		four.add(statePanel, BorderLayout.NORTH);
 		four.add(phoneNumPanel, BorderLayout.SOUTH);
 
 		five = new JPanel(new BorderLayout());
 		five.add(emailPanel, BorderLayout.NORTH);
 		five.add(ssnPanel, BorderLayout.SOUTH);
 
 		seven = new JPanel(new BorderLayout());
 		seven.add(five, BorderLayout.NORTH);
 		seven.add(done, BorderLayout.SOUTH);
 
 		six = new JPanel(new BorderLayout());
 		six.add(four, BorderLayout.NORTH);
 		six.add(seven, BorderLayout.SOUTH);
 
 
 		mainPanel = new JPanel(new BorderLayout());
 		mainPanel.add(three, BorderLayout.NORTH);
 		mainPanel.add(six, BorderLayout.SOUTH);
 
 
 		content.add(mainPanel, BorderLayout.CENTER);
 
 		MyHandler handler = new MyHandler();
 		done.addActionListener(handler);
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(700, 275);
 		frame.setVisible(true);
 		frame.setResizable(false);
 
 	}
 
 
 	private class MyHandler implements ActionListener {
 		public void actionPerformed(ActionEvent event) {
 			if (event.getSource() == done){
 				int tax_ID =0;
 				String username = userField.getText();
 				String password = passField.getText();
 				String name = nameTextField.getText();
 				String address = addressTextField.getText();
 				String state = stateTextField.getText();
 				String phoneNum = phoneNumField.getText();
 				String email = emailTextField.getText();
 				String ssn = ssnTextField.getText();
 
     			//call andrews signup function
 				userField.setText("made it");
 				try{
					tax_ID = db.newCustomer(name, username, password, address, state, phoneNum, email, ssn);
 					// some sort of bug causing newCustomer to crash
					if(tax_ID != 0){
 						nameTextField.setText("about to switch");
 						GUI gui = new GUI(db, tax_ID);
 						gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 						gui.setSize(700,800);
 						gui.setVisible(true);
 						gui.setResizable(false);
 						frame.dispose();
					}
 				}
 				catch (Exception e){
 					e.printStackTrace();
 				}
 
 				
 
 			}
 		}
 	}
 
 }
