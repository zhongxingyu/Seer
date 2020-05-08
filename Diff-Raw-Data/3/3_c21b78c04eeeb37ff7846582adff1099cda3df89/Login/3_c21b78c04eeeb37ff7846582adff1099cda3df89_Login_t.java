 package views.gui;
 
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.border.EmptyBorder;
 
 import models.Organizer;
 import controllers.LoginController;
 
 public class Login extends JFrame {
 
 	private static final long serialVersionUID = -6145788675663931852L;
 	private JPanel contentPane;
 	private JPasswordField pwdPasswd;
 
 	/**
 	 * Launch the application.
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
 
 	/**
 	 * Create the frame.
 	 */
 	public Login() {
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 400, 250);
 		setResizable(false);
 		setTitle("User manager");
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JOptionPane alert = new JOptionPane("The password is invalid!",
 				JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
 		JDialog dialog = alert.createDialog(this, "Error");
 		
 		JLabel lblUser = new JLabel("User");
 		
		JComboBox<String> comboBox = new JComboBox<String>(Organizer.getInstance().getUsers().getUsernames());
 		
 		JLabel lblPassword = new JLabel("Password");
 		
 		comboBox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				pwdPasswd.requestFocusInWindow();
 			}
 		});
 		
 		pwdPasswd = new JPasswordField();
 		pwdPasswd.addActionListener(new LoginController(this, comboBox, pwdPasswd, dialog));
 		
 		JPanel panel = new JPanel();
 		
 		JButton btnLogIn = new JButton("Log in");
 		btnLogIn.addActionListener(new LoginController(this, comboBox, pwdPasswd, dialog));
 		
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblUser)
 						.addComponent(lblPassword))
 					.addGap(21)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
 						.addComponent(pwdPasswd)
 						.addComponent(comboBox, 0, 166, Short.MAX_VALUE))
 					.addGap(18)
 					.addComponent(btnLogIn, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addGap(21)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblUser)
 						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(btnLogIn))
 					.addGap(18)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblPassword)
 						.addComponent(pwdPasswd, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 		);
 		
 		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		
 		JButton btnNewUser = new JButton("New user");
 		btnNewUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("New user clicked");
 				//TODO user adding form
 			}
 		});
 		panel.add(btnNewUser);
 		
 		JButton btnDeleteUser = new JButton("Delete user");
 		btnDeleteUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Delete user clicked");
 				//TODO user deleting form
 			}
 		});
 		panel.add(btnDeleteUser);
 		panel.add(btnCancel);
 		
 		contentPane.setLayout(gl_contentPane);
 		setVisible(true);
 	}
 }
