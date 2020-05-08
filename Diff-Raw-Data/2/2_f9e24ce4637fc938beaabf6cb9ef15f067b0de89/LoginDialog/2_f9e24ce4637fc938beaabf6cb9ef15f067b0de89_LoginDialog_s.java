 /**
  * Login Screen GUI.
  * Upon opening the program, user is presented with the dialog made in this class.
  * Login button sends the login info to the UM web portal for verification. If
  * a 'login successful' result is returned, this dialog closes. If 'login fail' 
  * result is returned, this dialog stays open and text appears at the top stating 
  * that the login has failed and prompts the user to try again.
  */
 package progAdmin;
 
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileReader;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import net.miginfocom.swing.MigLayout;
 import program.CurrentUser;
 import utilities.DatabaseHelper;
 import utilities.FileHelper;
 import utilities.ui.SwingHelper;
 //-----------------------------------------------------------------------------
 /**
  * The <code>LoginDialog</code> class
  *
  */
 public class LoginDialog extends JDialog implements ActionListener {
 	private static final long serialVersionUID = 1L;
 	private static boolean loginSuccessful=false;
 	//buttons
 	private static final String LOGIN = "login";
 	private static final String CANCEL = "cancel";
 	private static final String HELP = "help";
 	//error codes
 	private static final int NO_ERROR = 0;
 	private static final int ERROR_BAD_PASSWORD = 1;
 	private static final int ERROR_USR_DNE = 2;
 	//error messages
 	private static final String USR_DNE_MESSAGE = "<html><b><font color=#ff0000>" +
 			"The caneID you have entered does not correspond to an existing user. " +
 			"Please renter your information or see your <br> supervisor to be added" +
 			" to the system.</font></b></html>";
 	private static final String BAD_PASSWORD_MESSAGE = "<html><b><font color=#ff0000>" +
 			"You entered incorrect credentials.<br> Please try again.</font></b></html>";
 	private static final String HELP_MESSAGE = "You need help!";
 	private JLabel retryLabel;
 	private JTextField caneIdField;
 	private JPasswordField passwordField;
 	JFrame frame;
 //-----------------------------------------------------------------------------
 	/**
 	 * Creates Login JFrame
 	 * 
 	 * @param frame
 	 */
 	public LoginDialog(final JFrame frame){
 		super(frame, "UMPD Login", true);
 		this.frame = frame;
 		
 		//Set the size of the form
 		this.setPreferredSize(SwingHelper.LOGIN_DIALOG_DIMENSION);
 		this.setSize(SwingHelper.LOGIN_DIALOG_DIMENSION);
 				
 		JPanel dialogPanel = new JPanel(new MigLayout("nogrid, fillx"));
 		
 		
 		//Put the form in the middle of the screen
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		
 		//Set up the GUI
 		retryLabel = new JLabel();
 		dialogPanel.add(retryLabel, "wrap");
		JLabel badgeLabel = SwingHelper.createImageLabel("images/badge.png");
 		dialogPanel.add(badgeLabel);
 		JPanel inputPanel = createInputPanel();
 		dialogPanel.add(inputPanel, "wrap");
 		JPanel buttonsPanel = createButtonsPanel();
 		dialogPanel.add(buttonsPanel, "align center");
 		
 		//Add the dialog to the screen
 		Container contentPane = getContentPane();
 		contentPane.add(dialogPanel);
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Creates input fields for user to input their caneID and password
 	 * 
 	 * @return
 	 */
 	public JPanel createInputPanel(){
 		JPanel inputPanel = new JPanel(new MigLayout());
 		
 		caneIdField = SwingHelper.addLabeledTextField(inputPanel, "Cane ID: ", 
 				SwingHelper.DEFAULT_TEXT_FIELD_LENGTH, true);
 		
 
 		passwordField = SwingHelper.addLabeledPwdField(inputPanel, "Password: ",
 				SwingHelper.DEFAULT_TEXT_FIELD_LENGTH,true);
 		
 		return inputPanel;
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Creates buttons for logon panel
 	 * Buttons include "Login", "Cancel", and "Help"
 	 * 
 	 * @return
 	 */
 	public JPanel createButtonsPanel(){
 		JPanel buttonsPanel = new JPanel(new MigLayout("fillx"));
 		
 	    // Add login button
 	    JButton loginButton = new JButton("Login");
 	    loginButton.setActionCommand(LOGIN);
 	    loginButton.addActionListener(this);
 	    
 		//Add cancel button
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.setActionCommand(CANCEL);
 		cancelButton.addActionListener(this);
 
 		//Add help button
 		JButton helpButton = new JButton("Help");
 		helpButton.setActionCommand(HELP);
 		helpButton.addActionListener(this);
 		
 	    buttonsPanel.add(loginButton);
 	    buttonsPanel.add(cancelButton);
 	    buttonsPanel.add(helpButton);
 	    
 		return buttonsPanel;
 	}
 //-----------------------------------------------------------------------------
 	public void actionPerformed(ActionEvent ev) {
 		
 		if(ev.getActionCommand()==LOGIN){
 			//check user name and password
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 			int authenticationResult = authenticateUser();
 			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			if(authenticationResult==0){
 				//set the login result value to be true
 				loginSuccessful = true;
 				//close the dialog
 				setVisible(false);
 	
 /* *****************************************************************************
 * TODO: 1.Find and play a login sound upon success. 
 * TODO: 2.Create and display spash screen.
 * Since the program takes a few seconds to load, should play a login tone and
 * show a splash screen to let user know that their login has been successful
 * and the program is loading.
 *******************************************************************************/
 				
 			} else {
 				//display retry login text
 				displayRetryLabel(authenticationResult);
 			}
 		} else if(ev.getActionCommand()==CANCEL){
 			//close the dialog
 			loginSuccessful=false;
 			setVisible(false);
 		} else if(ev.getActionCommand()==HELP){
 			JOptionPane.showMessageDialog(frame, HELP_MESSAGE, 
 					"Help", JOptionPane.INFORMATION_MESSAGE);
 		}
 	}
 //-----------------------------------------------------------------------------
 	/**
 	 * Authenticates a user trying to login to the program. 
 	 * Checks the user name and password first against UM's web portal, then 
 	 * against the roster xml file to determine the user's permissions.
 	 * @return the error code of the corresponding login error, or 0 if 
 	 * login was successful
 	 */
 	  public int authenticateUser() {
 			String caneID = caneIdField.getText().trim();
 			String password = passwordField.getText().trim();
 			Employee user = null;
 			
 			//Get the employee object that matches the given caneID
 			user = PersonnelManager.getEmployeeByCaneID(caneID);
 			  
 			//If user matching given caneID isn't in system, don't bother checking password
 			if(user==null){ 
 				//employee matching the given caneID not found in system
 				return ERROR_USR_DNE; 
 			}
 			  
 //DEBUG System.out.println("caneID field = " + caneIdField.getText() + " and user" +
 //"caneID = "  + user.getCaneID());
 		  
 			//Set the current user to be the employee that just logged in 
 			CurrentUser.setCurrentUser(user);
 			
 			//Check if attempting to run in demo mode & perform appropriate actions
 			if(caneID.equals("demo")){
 				return NO_ERROR;
 			}
 			
 			
 			//Authenticate the caneID and password info via UM's web portal
 			String login_site = "https://caneid.miami.edu/cas/login"; // login url for umiami
 
 			ScriptEngineManager manager = new ScriptEngineManager();
 			ScriptEngine engine = manager.getEngineByName("python");
 			if(engine==null){ System.out.println("engine is null"); }
 			
 			//retrieve the python script that sends the info to the server
 			String passwdScript = FileHelper.getPasswordScript();
 			
 			try{
 				FileReader reader = new FileReader(passwdScript);
 				engine.put("path", login_site);
 				engine.put("usr", caneID);
 				engine.put("pw", password);
 				engine.eval(reader);
 				Object result = engine.get("isAccepted");
 				reader.close();
 				if((Integer)(result) == -1){
 					return ERROR_BAD_PASSWORD;
 				}
 			}catch(Exception e){
 				System.out.println("Caught exception in file reader in python script");
 				//e.printStackTrace();
 			}
 			
 			//TODO: SET CURRENT USER SET TIME
 			return NO_ERROR;
 		}
 //-----------------------------------------------------------------------------
 	  /**
 	   * Returns whether or not the most recent login attempt was successful.
 	   * 
 	   * @return true if login was successful; false otherwise
 	   */
 	  public boolean isSuccessful() {
 		  return loginSuccessful;
 	  }
 //-----------------------------------------------------------------------------
 	  /**
 	   * Displays a dialog box indicating there has been an error while
 	   * attempting to login
 	   * 
 	   * @param errorID
 	   */
 	  public void displayRetryLabel(int errorID) {
 		  //Display the error corresponding to the given error code
 		  switch(errorID){
 		  case ERROR_BAD_PASSWORD:
 			  retryLabel.setText(BAD_PASSWORD_MESSAGE);
 			  break;
 		  case ERROR_USR_DNE:
 			  retryLabel.setText(USR_DNE_MESSAGE);
 			  break;
 		  }
 
   }
 //-----------------------------------------------------------------------------
 }
