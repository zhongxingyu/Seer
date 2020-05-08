 package gui;
 
import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
 
import swing.JSearchTextField;
import swing.JSearchPasswordField;
 
 public class LoginPanel extends OptionsPanel implements ActionListener{
 	
 	JSearchTextField username = new JSearchTextField("username");
 	JSearchPasswordField password = new JSearchPasswordField("password");
 	JSearchPasswordField repeat_password = new JSearchPasswordField("repeat password");
 	JButton login = new JButton("login");
 	JButton settings = new JButton("settings");
 	JButton register = new JButton("register");
 	JButton submit = new JButton("submit");
 	JButton cancel = new JButton("cancel");
 	
 	
 	public LoginPanel(ChatPanel p){
 		super(p);
 		this.setOpaque(false);
 		this.setLayout(null);
 		
 		
 		this.login.addActionListener(this);
 		this.settings.addActionListener(this);
 		this.register.addActionListener(this);
 		this.submit.addActionListener(this);
 		this.cancel.addActionListener(this);
 		
 		
 		this.login.setBounds(210, 318, 135, 65);
 		this.username.setBounds(5,318,200,30);
 		this.password.setBounds(5,353,200,30);
 		this.repeat_password.setBounds(5,388,200,30);
 		this.register.setBounds(210, 388, 135, 30);
 		this.settings.setBounds(5,488,200,60);
 		this.submit.setBounds(210, 318, 135, 65);
 		this.cancel.setBounds(210, 388, 135, 30);
 		
 		this.add(this.login);
 		this.add(this.username);
 		this.add(this.password);
 		this.add(this.settings);
 		this.add(this.register);
 	}
 	
 	
 	
 	
 	
 	public void paintComponent(Graphics g){
 		Color c = new Color(255, 255, 255, 90); //r,g,b,alpha
 		g.setColor(c);
 		g.fillRect(0, 0, this.getWidth(), this.getHeight());
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(this.login)){
 			this.p.connectToServer(p.getHostName(), p.getPort());
 			try {
 				this.p.login(this.username.getText(), this.password.getText());
 			} catch (Exception e1) {
 				this.p.showErrorPanel("Error: writer closed 001");
 			}
 			this.p.hideOptionsPane();
 		}
 		
 		if (e.getSource().equals(this.submit)){
 			if (!(this.password.getText().equals(this.repeat_password.getText()))){
 					this.p.showErrorPanel("passwords dont match");
 					return;
 			}
 			this.p.connectToServer(p.getHostName(), p.getPort());
 			try {
 				this.p.register(this.username.getText(), this.password.getText());
 			} catch (Exception e1) {
 				this.p.showErrorPanel("Error: writer closed 001");
 			}
 			this.p.hideOptionsPane();
 		}
 		
 		if (e.getSource().equals(this.settings)){
 			
 		}
 		
 		if (e.getSource().equals(this.register)){
 			this.remove(this.register);
 			this.remove(this.login);
 			this.add(this.repeat_password);
 			this.add(this.cancel);
 			this.add(this.submit);
 		}
 		
 		if (e.getSource().equals(this.cancel)){
 			this.remove(this.repeat_password);
 			this.remove(this.cancel);
 			this.remove(this.submit);
 			this.add(this.register);
 			this.add(this.login);
 			
 		}
 	}
 }
