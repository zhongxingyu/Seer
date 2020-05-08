 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.io.File;
 import java.awt.Insets;
 import java.awt.BorderLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Component;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 
 public class Admin implements Runnable {
 	ClientController _clientController;
 	JFrame _mainFrame;
	String role = "Professor";
 	
 	Admin(ClientController clientController, JFrame mainFrame) {
 		_clientController = clientController;
 		_mainFrame = mainFrame;
 		SwingUtilities.invokeLater(this);
 	}
 
 	public void run() {
 		JMenuBar menubar = new JMenuBar();
 		JMenu file = new JMenu("File");
 
 		menubar.add(file);
 		_mainFrame.setJMenuBar(menubar);
 
 		// Toolbar
 		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
 		toolbar.setFloatable(false);
 		toolbar.setMargin(new Insets(10, 5, 5, 5));
 
 		JButton createAccount = new JButton("Create Account");
 		createAccount.setBorder(new EmptyBorder(3, 0, 3, 0));
 
 		toolbar.add(createAccount);
 
 		_mainFrame.add(toolbar, BorderLayout.WEST);
 
 		// Main working display
 		final JPanel mainPanel = new JPanel();
 
 		// Panel for creating account 
 		JLabel userIDLabel = new JLabel("User ID");
 		final JTextField userID = new JTextField();
 		userID.setPreferredSize(new Dimension(200, 20));
 		userID.setMaximumSize(new Dimension(400,20));
 		JLabel userRoleLabel = new JLabel("User Role");
 		String[] roles = {"Professor", "TA", "Student"};
 		final JComboBox roleList = new JComboBox(roles);
 
 		JButton createButton = new JButton("Create");
 
 		final JPanel createPanel = new JPanel();
 		createPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		createPanel.setLayout(new BoxLayout(createPanel, BoxLayout.Y_AXIS));
 		createPanel.add(userIDLabel);
 		createPanel.add(userID);
 		createPanel.add(userRoleLabel);
 		createPanel.add(roleList);
 		createPanel.add(createButton);
 
 		// Frame configuration
 		_mainFrame.setSize(500, 300);
 		_mainFrame.setTitle("Administrator");
 		_mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		_mainFrame.setLocationRelativeTo(null);
 		_mainFrame.setVisible(true);
 
 		createAccount.addActionListener(
 				new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						_mainFrame.remove(mainPanel);
 						mainPanel.removeAll();
 						userID.setText("");
 						mainPanel.add(createPanel);
 						_mainFrame.add(mainPanel, BorderLayout.CENTER);
 						_mainFrame.setVisible(false);
 						_mainFrame.setVisible(true);					
 					}
 				}
 				);
 
 		// Panel for 
 
 		createButton.addActionListener(
 				new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						String userIDString = userID.getText();
 						String userRoleString = role;
 						String result = _clientController
 							.createAccount(userIDString,userRoleString);
 						_mainFrame.remove(mainPanel);
 						mainPanel.removeAll();
 						mainPanel.add(new JLabel("Account created successfully!"));
 						mainPanel.add(new JLabel(result));
 						_mainFrame.add(mainPanel, BorderLayout.CENTER);
 						_mainFrame.setVisible(false);
 						_mainFrame.setVisible(true);
 					}
 				}
 				);
 
 		roleList.addActionListener(
 				new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						JComboBox cb = (JComboBox)e.getSource();
         				role = (String)cb.getSelectedItem();
         			}
         		}
         		);
 	}
 
 
 }
 
 
