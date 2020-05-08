 package views;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 import lib.Logger;
 import lib.OSProperties;
 import lib.RoundedBorder;
 import controllers.File_System_Controller;
 import controllers.Login_Controller;
 
 /**
  * <p>Houses the GUI components of the login window that's displayed
  * to the user. Controls action events fired by the user attempting
  * to login.
  * 
  * @author Adam Childs
  */
 public class Login_View implements ActionListener, KeyListener
 {
 	boolean logged_in = false;
 	JDialog dialog = null;
 	JLabel feedbackLabel = null;
 	JPanel buttonPanel = null;
 	JPasswordField passwordField = null;
 	JTextField usernameField = null;
 	Login_Controller controller = null;
 	OSProperties osp = new OSProperties();
 
 	public Login_View(Login_Controller c)
 	{
 		controller = c;
 	}
 
 	public Component show()
 	{
 		dialog = new JDialog();
 
 		osp = new OSProperties();
 		Container container = dialog.getContentPane();
 		container.add(dialogPanel());
 
 		if (osp.isUnix())
 			dialog.setSize(295, 155);
 		else if (osp.isMac())
 			if (osp.isHighResolution())
 				dialog.setSize(260, 175);
 			else
 				dialog.setSize(280, 155); // TODO: Fix to correctly size on Mac OS
 		else
 			dialog.setSize(250, 180); // width, height
 
 		dialog.addKeyListener(this);
 		dialog.setLocationRelativeTo(null);
 		dialog.setModal(true);
 		dialog.setTitle("Login");
 		dialog.setResizable(false);
 		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		dialog.setVisible(true);
 
 		return dialog;
 	}
 
 	private JPanel dialogPanel()
 	{
 		JButton button;
 		JPanel p = new JPanel(new BorderLayout());
 		JPanel textfieldPanel, labelPanel;
 		JLabel label;
 
 		labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		label = new JLabel("Please login to your account.");
 		labelPanel.add(label);
 
 		/*
 		 * Username field
 		 */
 		label = new JLabel("Username:");
 		if (osp.isMac())
 		{
 			if (osp.isHighResolution())
 				label.setPreferredSize(new Dimension(70, 25));
 		} else {
 			label.setPreferredSize(new Dimension(65, 25));
 		}
 		textfieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		usernameField = new JTextField();
 		usernameField.setPreferredSize(new Dimension(125, 25));
 		usernameField.addKeyListener(this);
 
 		textfieldPanel.add(label);
 		textfieldPanel.add(usernameField);
 
 		/*
 		 * Password field
 		 */
 		label = new JLabel("Password:");
 		if (osp.isMac())
 		{
 			if (osp.isHighResolution())
 				label.setPreferredSize(new Dimension(70, 25));
 		} else {
 			label.setPreferredSize(new Dimension(65, 25));
 		}
 		passwordField = new JPasswordField();
 		passwordField.setPreferredSize(new Dimension(125, 25));
 		passwordField.addKeyListener(this);
 
 		textfieldPanel.add(label);
 		textfieldPanel.add(passwordField);
 
 		textfieldPanel.add(Box.createRigidArea(new Dimension(9999, -5)));
 		feedbackLabel = new JLabel();
 		textfieldPanel.add(feedbackLabel);
 
 		/*
 		 * Login Button
 		 */
 		buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		button = new JButton("Login");
 		button.setBorder(new RoundedBorder(5));
 		button.setPreferredSize(new Dimension(75, 30));
 		button.setToolTipText("Login");
 		button.setFocusable(true);
 		button.setActionCommand("login");
 		button.addActionListener(this);
 		button.addKeyListener(this);
 		buttonPanel.add(button);
 
 		/*
 		 * Cancel Button
 		 */
 		button = new JButton("Cancel");
 		button.setBorder(new RoundedBorder(5));
 		button.setPreferredSize(new Dimension(75, 30));
 		button.setToolTipText("This will terminate the program");
 		button.setFocusable(true);
 		button.setActionCommand("cancel");
 		button.addActionListener(this);
 		buttonPanel.add(button);
 
 		p.add(labelPanel, BorderLayout.NORTH);
 		p.add(textfieldPanel, BorderLayout.CENTER);
 		p.add(buttonPanel, BorderLayout.SOUTH);
 
 		return p;
 	}
 
 	/**
 	 * <p>Checks the validity of the entered username and password and matches it
 	 * to the account files stored on the user's computer.
 	 */
 	public void attemptLogin()
 	{
 		String user = usernameField.getText();
 		String pass = new String(passwordField.getPassword());
 
 		if (usernameField.getText().length() == 0 || passwordField.getPassword().length == 0)
 		{
 			feedbackLabel.setText( "You must fill all fields." );
 			feedbackLabel.setForeground(Color.RED);
 			Logger.write("Not all fields filled in during login procedure.", Logger.Level.USER_ERROR);
 			return;
 		}
 
 		if (new File(new File_System_Controller().getModel().findFile("Accounts" + osp.getSeparator() + user + ".xml")).exists())
 		{
 			if (controller.validateLogin(user, pass))
 			{
 				feedbackLabel.setText("Login successful!");
 				feedbackLabel.setForeground(new Color(0, 139, 0));
 				usernameField.setEnabled(false);
 				passwordField.setEnabled(false);
 				buttonPanel.removeAll();
 	
				JButton button = new JButton("OK");
				button.setBorder(new RoundedBorder(5));
				button.setPreferredSize(new Dimension(75, 30));
				button.setActionCommand("ok");
				button.addActionListener(this);
				button.addKeyListener(this);
				buttonPanel.add(button);
 				buttonPanel.revalidate();
 				buttonPanel.repaint();
 				logged_in = true;
 			} else {
 				feedbackLabel.setText("Invalid username or password!");
 				feedbackLabel.setForeground(Color.RED);
 				Logger.write("Invalid password [" + pass + "] for account [" + user + "].", Logger.Level.USER_ERROR);
 			}
 		} else {
 			feedbackLabel.setText("Invalid username or password!");
 			feedbackLabel.setForeground(Color.RED);
 			Logger.write("Invalid username [" + user + "].", Logger.Level.USER_ERROR);
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getActionCommand().equals("login"))
 		{
 			if (usernameField.getText().length() == 0 || passwordField.getPassword().length == 0)
 			{
 				feedbackLabel.setText( "You must fill all fields." );
 				feedbackLabel.setForeground(Color.RED);
 				Logger.write("Not all fields filled in during login procedure.", Logger.Level.USER_ERROR);
 				return;
 			}
 			attemptLogin();
 		}
 		else if (e.getActionCommand().equals("cancel"))
 		{
 			System.exit(0);
 		}
 		else if (e.getActionCommand().equals("ok"))
 		{
 			dialog.dispose();
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		if (e.getKeyCode() == KeyEvent.VK_ENTER)
 		{
 			if (usernameField.hasFocus())
 			{
 				String p = new String(passwordField.getPassword());
 				if (p.length() > 0)
 					attemptLogin();
 				else
 					passwordField.requestFocusInWindow();
			} else if (logged_in) {
 				dialog.dispose();
 			} else {
 				attemptLogin();
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e)
 	{
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e)
 	{
 		
 	}
 }
