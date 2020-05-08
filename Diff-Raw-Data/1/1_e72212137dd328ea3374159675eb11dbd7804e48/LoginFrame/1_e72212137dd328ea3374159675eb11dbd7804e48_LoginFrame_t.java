 
 
 
 package overwatch.gui;
 
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.security.LoginManager;
 import java.util.ArrayList;
 import java.awt.event.*;
 import javax.swing.*;
 import net.miginfocom.swing.MigLayout;
 
 
 
 
 
 /**
  * User login GUI.
  * 
  * @author  Lee Coakley
  * @version 3
  * @see     LoginListener
  */
 
 
 
 
 
 public class LoginFrame extends JFrame
 {
 	public JLabel		  nameLabel;
 	public JTextField     nameField;
 	public JLabel         passLabel;
 	public JPasswordField passField;
 	public JButton		  loginButton;
 	
 	private ArrayList<LoginListener> loginListeners;
 	
 	
 	
 	
 	
 	public LoginFrame()
 	{
 		super( "Overwatch - Login" );	
 		setLayout( new MigLayout( "", "128px[][]128px", "128px[][][]128px" ) );
 		setIconImages( IconLoader.getIcons() );
 		
 		loginListeners = new ArrayList<LoginListener>();
 		
 		setupComponents();
 		setupActions();
 	}
 	
 	
 	
 	
 	
 	public void addLoginButtonListener( ActionListener al ) {
 		loginButton.addActionListener( al );
 	}
 	
 	
 	
 	
 	public void addLoginListener( LoginListener ll ) {
 		loginListeners.add( ll );
 	}
 	
 	
 		
 	
 	
 	public void removeLoginListener( LoginListener ll ) {
 		loginListeners.remove( ll );
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void setupComponents()
 	{
 		nameLabel   = new JLabel( "Login:" );
 		nameField   = new JTextField( 16 );
 		passLabel   = new JLabel( "Pass:" );
 		passField   = new JPasswordField( 16 );
 		loginButton = new JButton( "Log in" );
 		
 		add( nameLabel, "alignx right" );
 		add( nameField, "wrap" );
 		
 		add( passLabel, "alignx right" );
 		add( passField, "wrap" );
 		
 		add( loginButton, "skip 1, alignx right" );
 		
 		
 		loginButton.setEnabled( false );
 	}
 	
 	
 	
 	
 	
 	private void setupActions()
 	{
 		KeyListener commonFieldListener = new KeyAdapter() {
 			public void keyReleased( KeyEvent e ) { onKeyRelease( e ); }
 		};
 		
 		nameField.addKeyListener( commonFieldListener );
 		passField.addKeyListener( commonFieldListener );
 
 		loginButton.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e )
 			{
 				notifyLoginListeners(
 					nameField.getText(),
 					new String(passField.getPassword())
 				);
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void notifyLoginListeners( String user, String pass ) {
 		for (LoginListener l: loginListeners) {
 			l.onLoginAttempt( user, pass );
 		}
 	}
 	
 	
 	
 	
 	
 	private boolean canLogin() {
 		return (nameField.getText()    .length() > 0)
 			&& (passField.getPassword().length   > 0);
 	}
 	
 	
 	
 	
 	private void onKeyRelease( KeyEvent ke )
 	{
 		boolean canLogin = canLogin();
 		
 		loginButton.setEnabled( canLogin );
 		
 		if (canLogin)
 		if (ke.getKeyCode() == KeyEvent.VK_ENTER)
 			loginButton.doClick();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Test
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	public static void main( String[] args )
 	{	
 		Gui.setNativeStyle();
 		
 		LoginFrame lf = new LoginFrame();
 		
 		lf.addLoginListener( new LoginListener() {
 			public void onLoginAttempt( String user, String pass ) {
 				boolean goodLogin = LoginManager.doLogin( user, pass );
 				System.out.println( "Login result: " + goodLogin );
 				Database.disconnect();
 			}
 		});
 		
 		lf.pack();
 		lf.setVisible( true );
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
