 package g53.exceedvote.ui;
 
 
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
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.UnsupportedEncodingException;
 import java.util.ResourceBundle;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 
 import g53.exceedvote.controller.Controller;
 import g53.exceedvote.domain.RecordLog;
 
 /**
  * A Class which User Interface of Any Actor to access application
  * @author	Guysit Koonrungruang 5310547185
  * @Version 2012.November.15
  */
 public class LoginUI extends RecordLog implements InterfaceUI{
 
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
     private String typepass;
 	private Boolean isLogin = false;
 	private Controller control;
 	private ResourceBundle language;
 
     public LoginUI(Controller control, ResourceBundle language){
     	this.control = control;
     	this.language = language;
     	frame = new JFrame();
     	frame.setTitle(encode("login"));
     	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         initComponents();
         frame.pack();
         frame.setSize(400, 130);
         frame.setLocation(500, 250);
         frame.setResizable(false);
         //control.connect();
         
     }
 
     public void run() {
     	frame.setVisible(true);
     }
 
     public void close() {
     	frame.dispose();
     }
 
     public void initComponents(){
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
         ok.setText(encode("login"));
 
         ok.addActionListener(new ButtonListener());
         cancel = new JButton();
         cancel.setFont(font2);
         cancel.setText(encode("Cancel"));
         cancel.addActionListener(new CancelListener());
         user = new JLabel();
         password = new JLabel();
         user.setFont(font);
         password.setFont(font);
         user.setText(encode("Username"));
         password.setText(encode("Password"));
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
         
      // confirm exit
      		frame.addWindowListener(new WindowAdapter() {
      			public void windowClosing(WindowEvent e) {
      				String textYes = encode("textYes");
 					String textNo = encode("textNo");
 					String textTitle = encode("textTitle");
 					String textMessage = encode("textMessage");
      				Object[] options = { textYes, textNo };
      				int result = JOptionPane
      						.showOptionDialog(frame, textTitle, textMessage,
      								JOptionPane.YES_NO_OPTION,
      								JOptionPane.QUESTION_MESSAGE, null, options,
      								options[0]);
 
      				if(result == JOptionPane.YES_OPTION) {
      			           ((JFrame)e.getSource()).setDefaultCloseOperation(
      			                   JFrame.EXIT_ON_CLOSE);
      			        } else {
      			           ((JFrame)e.getSource()).setDefaultCloseOperation(
      			                   JFrame.DO_NOTHING_ON_CLOSE);
      			        }
      			}
      		});
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
 			typepass = inputField2.getText();
 			if (control.login(userName, typepass)) {				
 				JOptionPane.showMessageDialog(null, control.getCurMessage());
 				close();
 				isLogin = true;
 			} 
 			else if (control.loginElectionCommittee(userName, typepass)) {
 				JOptionPane.showMessageDialog(null, encode("success"));
 				close();
 				isLogin = true;
 			}
 			else {	
 				JOptionPane.showMessageDialog(null, encode("fail"));
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
 
 	@Override
 	public String encode(String key) {
 		try {
 			return new String(language.getString(key).getBytes("ISO8859-1"), "UTF-8");
 			
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println(e.getMessage());
 			return null;
 		}
 	}
 }
