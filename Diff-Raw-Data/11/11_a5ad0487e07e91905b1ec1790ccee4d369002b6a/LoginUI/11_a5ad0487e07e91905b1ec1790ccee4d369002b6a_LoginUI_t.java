 package ui;
 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import domain.RecordLog;
 import domain.VoterDB;
 
 /**
  *
  * @author Guy2jz
  */
 public class LoginUI extends RecordLog {
 
     private JFrame frame;
     private JTextField inputField1;
     private JPasswordField inputField2;
     private JLabel user;
     private JLabel password;
     private JButton ok;
     private JButton cancel;
     private Font font = new Font("Tahoma", Font.BOLD, 16);
     private Font font2 = new Font("Tahoma", Font.PLAIN, 14);
     private String userName;
 	private Boolean isLogin = false;
 	private VoterDB vote;
 
     public LoginUI(VoterDB vote) {
     	frame = new JFrame();
     	this.vote = vote;
     	frame.setTitle("Login");
     	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         initComponents();
         frame.pack();
         frame.setSize(400, 130);
         frame.setLocation(500, 250);
         frame.setResizable(false);
     }
 
     public void run() {
     	frame.setVisible(true);
     }
 
     public void close() {
     	frame.dispose();
     }
 
     public void initComponents() {
     	frame.setLayout(new BorderLayout());
         JPanel pl1 = new JPanel(new FlowLayout());
         JPanel pl2 = new JPanel(new FlowLayout());
         JPanel pl3 = new JPanel(new FlowLayout());
         inputField1 = new JTextField(15);
         inputField1.setPreferredSize(new Dimension(200, 20));
         inputField2 = new JPasswordField(15);
         inputField2.setPreferredSize(new Dimension(200, 20));
         ok = new JButton();
         ok.setFont(font2);
         ok.setText("Login");
 
         ok.addActionListener(new ButtonListener());
         cancel = new JButton();
         cancel.setFont(font2);
         cancel.setText("Cancel");
         cancel.addActionListener(new CancelListener());
         user = new JLabel();
         password = new JLabel();
         user.setFont(font);
         password.setFont(font);
         user.setText("Username : ");
         password.setText("Password  : ");
         inputField1.setFont(font2);
         inputField2.setFont(font2);
         inputField1.addActionListener(new ButtonListener());
         inputField2.addActionListener(new ButtonListener());
         pl1.add(user);
         pl1.add(inputField1);
         pl2.add(password);
         pl2.add(inputField2);
         pl3.add(ok);
         pl3.add(cancel);
 
         frame.add(pl1, BorderLayout.NORTH);
         frame.add(pl2, BorderLayout.CENTER);
         frame.add(pl3, BorderLayout.SOUTH);
     }
 
     public String getName() {
         return userName;
     }
 
     public Boolean getSatus() {
 		return isLogin;
 	}
 
     class ButtonListener implements ActionListener {
 
     	@SuppressWarnings("deprecation")
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			userName = inputField1.getText();
 			if (vote.logIn(vote.getVoter(userName, inputField2.getText()))) {				
 				JOptionPane.showMessageDialog(null, vote.getMessage());
 				close();
 				isLogin = true;
 			} else {
 				JOptionPane.showMessageDialog(null, vote.getMessage());
 			}
 			inputField2.setText(null);
 		}
     	
     }
 
     class CancelListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             close();
         }
     }
 }
