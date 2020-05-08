 package client;
 
import java.awt.BorderLayout;
import java.awt.EventQueue;

 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
 import javax.swing.JTextField;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import javax.swing.JPasswordField;
 import javax.swing.JButton;
 import javax.swing.JTextPane;
 import java.awt.Font;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
 
 public class LoginGUI extends JFrame {
 
 	private JPanel LoginFrame;
 	private JTextField Username;
 	private JTextField Password;
 	private JTextField txtHostoptional;
 	private JTextField txtPortoptional;
 	
 
 	/**
 	 * Create the frame.
 	 */
 	public LoginGUI() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 477, 374);
 		LoginFrame = new JPanel();
 		LoginFrame.setBackground(Color.decode("#2C3E50"));
 		LoginFrame.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(LoginFrame);
 		
 		JPanel LoginPanel = new JPanel();
 		LoginPanel.setBackground(Color.decode("#2C3E50"));
 		
 		Username = new JTextField();
 		Username.setText("Username");
 		Username.setToolTipText("");
 		Username.setColumns(10);
 		Username.setBackground(Color.decode("#ECF0F1"));
 		
 		final JButton LogInButton = new JButton("Log In");
 		LogInButton.setBackground(Color.decode("#1ABC9C"));
 		
 		JTextPane Lable = new JTextPane();
 		Lable.setEditable(false);
 		Lable.setFont(new Font("Tahoma", Font.BOLD, 16));
 		Lable.setText("SimpleChat");
 		Lable.setBackground(Color.decode("#2C3E50"));
 		Lable.setForeground(Color.decode("#1ABC9C"));
 		
 		JTextPane txtpnLogIn = new JTextPane();
 		txtpnLogIn.setEditable(false);
 		txtpnLogIn.setFont(new Font("Tahoma", Font.BOLD, 16));
 		txtpnLogIn.setText("Log In");
 		txtpnLogIn.setBackground(Color.decode("#2C3E50"));
 		txtpnLogIn.setForeground(Color.decode("#1ABC9C"));
 		
 		Password = new JTextField();
 		Password.setText("Password");
 		Password.setColumns(10);
 		GroupLayout gl_LoginFrame = new GroupLayout(LoginFrame);
 		gl_LoginFrame.setHorizontalGroup(
 			gl_LoginFrame.createParallelGroup(Alignment.LEADING)
 				.addComponent(LoginPanel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
 		);
 		gl_LoginFrame.setVerticalGroup(
 			gl_LoginFrame.createParallelGroup(Alignment.LEADING)
 				.addComponent(LoginPanel, GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
 		);
 		
 		txtHostoptional = new JTextField();
 		txtHostoptional.setText("LocalHost");
 		txtHostoptional.setColumns(10);
 		
 		txtPortoptional = new JTextField();
 		txtPortoptional.setText("DEFAULT_PORT");
 		txtPortoptional.setColumns(10);
 		GroupLayout gl_LoginPanel = new GroupLayout(LoginPanel);
 		gl_LoginPanel.setHorizontalGroup(
 			gl_LoginPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_LoginPanel.createSequentialGroup()
 					.addGroup(gl_LoginPanel.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_LoginPanel.createSequentialGroup()
 							.addGap(124)
 							.addGroup(gl_LoginPanel.createParallelGroup(Alignment.LEADING, false)
 								.addGroup(gl_LoginPanel.createSequentialGroup()
 									.addGap(33)
 									.addComponent(Lable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 								.addGroup(gl_LoginPanel.createSequentialGroup()
 									.addGap(54)
 									.addComponent(txtpnLogIn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 								.addComponent(Username, GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
 								.addComponent(Password, GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
 								.addComponent(txtHostoptional)
 								.addComponent(txtPortoptional)))
 						.addGroup(gl_LoginPanel.createSequentialGroup()
 							.addGap(176)
 							.addComponent(LogInButton)))
 					.addContainerGap(144, Short.MAX_VALUE))
 		);
 		gl_LoginPanel.setVerticalGroup(
 			gl_LoginPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_LoginPanel.createSequentialGroup()
 					.addGap(52)
 					.addComponent(Lable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(txtpnLogIn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addGap(38)
 					.addComponent(Username, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addGap(6)
 					.addComponent(Password, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(txtHostoptional, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(txtPortoptional, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(LogInButton)
 					.addContainerGap(96, Short.MAX_VALUE))
 		);
 		LoginPanel.setLayout(gl_LoginPanel);
 		LoginFrame.setLayout(gl_LoginFrame);
 		
 		LogInButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = LogInButton.getActionCommand();
 				if(command == "Log In"){
 					//get new channel
 					String id = Username.getText();
 					String pw = Password.getText();
 					String host = txtHostoptional.getText();
 					String port = txtPortoptional.getText();
 					int portNumber;
 					if(id.length() < 1 || pw.length() < 1) {
 						Username.setText("");
 						Password.setText("");
 						Username.setBackground(Color.RED);
 						Password.setBackground(Color.RED);
 					}else {
 						if (host.length() == 0) {
 							host = "LocalHost";
 						}
 						if (port.length() == 0 || port.equals("DEFAULT_PORT")) {
 							portNumber = 5555;
 						}else {
 							portNumber = Integer.parseInt(port);
 						}
 						ClientGUI client = new ClientGUI(id, pw, host, portNumber);
 						setVisible(false);
 						
 					}
 				}
 			}
 		});
 		
 		pack();
 		setVisible(true);
 		
 
 	}
 }
