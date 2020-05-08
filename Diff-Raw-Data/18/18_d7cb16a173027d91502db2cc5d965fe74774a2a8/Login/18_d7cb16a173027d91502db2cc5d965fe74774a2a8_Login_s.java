 package com.chat.somechat;
 
 import java.awt.EventQueue;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class Login extends JFrame {
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane;
 	private JTextField txtUsername;
 	private JLabel lblport;
 	private JTextField txtPort;
 	private JLabel lblIPAddress;
 	private JTextField txtIPAddress;
 
 	public Login() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
			
 			e.printStackTrace();
 		}
 		
 		setResizable(false);
 		setTitle("Login");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(300,380);
 		setLocationRelativeTo(null);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblName = new JLabel("Name:");
 		lblName.setBounds(126, 20, 45, 15);
 		contentPane.add(lblName);
 		
 		txtUsername = new JTextField();
 		txtUsername.setBounds(67, 47, 164, 19);
 		contentPane.add(txtUsername);
 		txtUsername.setColumns(10);
 		
 		lblIPAddress = new JLabel("IP:");
 		lblIPAddress.setBounds(137, 98, 24, 15);
 		contentPane.add(lblIPAddress);
 		
 		txtIPAddress = new JTextField();
 		txtIPAddress.setColumns(10);
 		txtIPAddress.setBounds(67, 125, 157, 19);
 		contentPane.add(txtIPAddress);
 		
 		lblport = new JLabel("Port:");
 		lblport.setBounds(126, 170, 45, 15);
 		contentPane.add(lblport);
 		
 		txtPort = new JTextField();
 		txtPort.setText("4");
 		txtPort.setBounds(67, 197, 157, 19);
 		contentPane.add(txtPort);
 		txtPort.setColumns(10);
 		
 		JButton btnLogin = new JButton("Login");
 		btnLogin.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String name = txtUsername.getText();
 				String address = txtIPAddress.getText();
 				int port = Integer.parseInt(txtPort.getText());
 				login(name, address, port);
 			}
 		});
 		btnLogin.setBounds(90, 299, 117, 25);
 		contentPane.add(btnLogin);
 	}
 
 	private void login(String name, String address, int port) {
 		dispose();
 		//System.out.println("Name: " + name + "\nIP Address: " + address + "\nPort: " + port);
 		new Client(name, address, port);
 	}
 	
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
