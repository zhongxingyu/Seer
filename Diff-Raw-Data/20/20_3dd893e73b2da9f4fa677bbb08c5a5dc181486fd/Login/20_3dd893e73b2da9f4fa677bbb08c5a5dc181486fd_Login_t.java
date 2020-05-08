 
 
 import java.awt.BorderLayout;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.io.IOException;
 
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 public class Login extends JFrame implements ActionListener{
 	//WINDOW DIMENSIONS DEFAULT IS WIDTH = 350 HEIGHT = WIDTH/3
 	private final int WINDOW_WIDTH = 350,WINDOW_HEIGHT = WINDOW_WIDTH/3;
 	
 	//Initialize all components
 	private JPanel inputPanel,grid;
 	private JLabel nameLabel,passwordLabel;
 	private JTextField nameBox;
 	private JPasswordField passwordBox;
 	private JButton loginButton, newUserButton;
 	private FileReader loginReader;
 	
 	public Login(){
 		//set Attributes for our window
 		setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
 		setTitle("Login");
 		setResizable(false);
 		
 		//create login button with Text
 		loginButton = new JButton("Login");
 		//create button if there is a new user.
 		newUserButton = new JButton("New User");
 		
 		//Label our input boxes
 		nameLabel = new JLabel("Username: ");
 		passwordLabel = new JLabel("Password: ");
 		
 		//create our input boxes
 		nameBox = new JTextField(10);
 		passwordBox = new JPasswordField(10);
 		
 		//create Panel to harness our grid
 		inputPanel = new JPanel();
 		
 		//Create a 2x2 Grid
 		grid = new JPanel();
 		grid.setLayout(new GridLayout(2,2));
 		
 		//add components to grid
 		grid.add(nameLabel);
 		grid.add(nameBox);
 		grid.add(passwordLabel);
 		grid.add(passwordBox);
 		
 		//add grid to a separate component
 		inputPanel.add(grid);
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new GridLayout(1,2,0,30));
 		buttonPanel.add(loginButton);
 		buttonPanel.add(newUserButton);
 		
 		//add everything to our main window
 		getContentPane().add(inputPanel,BorderLayout.CENTER);
 		getContentPane().add(BorderLayout.SOUTH, buttonPanel);
 		
 		//add action listener to the button so it reacts to user click.
 		loginButton.addActionListener(this);
 		newUserButton.addActionListener(this);
 		
 		//reveal the window
 		setVisible(true);
 		
 	}
 
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getActionCommand() == "Login"){
 			String userNameEntered = nameBox.getText();
 			String passwordEntered;
 			String userNameHashed;
 			String passwordHashed;
 			System.out.println("Username: " + userNameEntered);
 			
 			String temp = "";
 			//stores our char[] password into a string
 			for(int i = 0;i<passwordBox.getPassword().length;i++){
 				temp = temp + passwordBox.getPassword()[i];
 			}
 			
 			passwordEntered = temp;
 			System.out.println("Password: " + passwordEntered);
 			
 			userNameHashed = Integer.toString(userNameEntered.hashCode());
 			passwordHashed = Integer.toString(passwordEntered.hashCode());
 			
 			//The method below is called to check to see if the username and password hashes match any in the database
 			//hashChecks(userNameHashed, passwordHashed);
 			
 			
 		}else if(e.getActionCommand() == "New User"){
 			System.out.println("New User Button Pressed");
 		}
 		
 		//insert file reader hash check here for login
 		
 		//if expression validating hashes, if match, login.
 		
 	}
 	
 	/*
 	 * This method takes the hashed username and password as parameters.
 	 * It then calls the FileReader's fetchLoginInfo method which will
 	 * return a 2D array with all hashed-usernames and passwords.
 	 * It will compare the hashes from the login with the database to try
 	 * and find a match.
 	 */
 	public boolean hashChecks(String someHashName, String someHashPass)
 	{
		//Need to set this to null and surround it with try catch block so it doesn't get an error.
		String[][] loginInfoToCompare = null;
		try {
			loginInfoToCompare = loginReader.fetchLoginInfo(); //Fetch the 2D array database
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
 		int loginFileSize = loginReader.fetchLoginInfoSize();//Fetch the size of the database
 		boolean userFound = false;
 		
 		//Each 'i' represents a new student.  Their user-hash is stored in the first element of 'j' and the password in the second element.
 		for(int i=0; i<loginFileSize; i++){
 			int j=0;
 			
 			//Checks to see if this is the right user
			//Need to use the compareTo method which returns -1 if false
			if(loginInfoToCompare[i][j].compareTo(someHashName) >-1){ 
 				userFound = true;	//User exists in the database so change boolean
 				System.out.println("Username Exists. Checking password...");
 				//If correct user, compare the passwords (the second element of 'j': [j+1] )
				//Need to use the compareTo method which returns -1 if false
				if(loginInfoToCompare[i][j+1].compareTo(someHashPass) >-1){ 
 					System.out.println("Passwords matched");//Found a match
 					return true;	//Return true because everything is a match
 				}
 				else{
 					System.out.println("Passwords not a match");//Not a match
 					return false; 	//Return false because it was not a match and they need to re-enter password
 				}
 			}
 			//If the user-password combo was not matched, 'i' will increment to check the next user in the database.
 		}
 		
 		//Checks to see if the user was found (i.e. the user is in the database)
 		if(userFound==false){
 			System.out.println("User not in the database");
 			return false;
 		}
 		
 		return true; //temporary until all cases are handled
 	}
 }
