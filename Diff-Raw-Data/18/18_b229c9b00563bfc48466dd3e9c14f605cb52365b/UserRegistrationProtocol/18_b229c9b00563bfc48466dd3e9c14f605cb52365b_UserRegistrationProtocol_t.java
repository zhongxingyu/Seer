 package peer;
 
 import java.awt.Button;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.security.PublicKey;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 
 import utils.KeyStoreUtils;
import utils.Constants;
 import utils.Protocol;
 import utils.SignUtils;
 
 public class UserRegistrationProtocol extends PeerProtocol {
 	
 	private String userName = null;
 	private String password = null;
 	
 	@Override
 	public String processInput(String input) {
 		String output = "default";
 		BufferedReader stdin = new BufferedReader(
 				new InputStreamReader(System.in));
 		
 		try {
 			if (input.equals("Start")) {
 				output = "create";
 			} else if (input.equals("username:password")) {
 				System.out.println("Please enter new username");
 				output = "username\t" + stdin.readLine();
 				
 				System.out.println("Please enter new password");
 				output += "\tpassword\t" + stdin.readLine();
 			} else {
 				output = "default";
 			}
 		} catch (IOException e) {
 			System.out.println("Error reading user input");
 		}
 		return output;
 	}
 	
 	public void execute(Socket s) {
 		CreateUserGui createUser = new CreateUserGui(this, s);
 	}
 	
 	void sendNewCredentials(String username, String password, Socket s) {
 		setCredentials(username, password);
 		
 		byte[] message = userName.getBytes();
 		try {
 			sendProtocol(s);
 			sendMessage(s, message);
 			
 			handleNameResponse(s);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	private void setCredentials(String username, String password) {
 		this.userName = username;
 		this.password = password;
 	}
 	
 	private void handleNameResponse(Socket s) throws IOException {
 		byte[] fromServerBytes = readSignedMessage(s, getHubKey());
 		String fromServer = new String(fromServerBytes);
 		
 		if (fromServer.equals("AVAILABLE,"+userName)) {
 			//TODO: add key generation/recovery code here
 			//for now assume keys in public/private.key files
 			//NOTE: I think the above happens automatically in SignUtils.init()
 			sendKey(s, KeyStoreUtils.getPublicKey(null, this.userName));
 			sendMessage(s, userName.getBytes());
 			
 			fromServerBytes = readSignedMessage(s, getHubKey());
 			fromServer = new String(fromServerBytes);
 			if (fromServer.equals("OK,"+userName)) {
 				//TODO:Save password and Username locally
 				System.out.println("Username " + userName + " successfully created");
 			} else {
 				System.out.println("Unknown message from server");
 			}			
 		} else if (fromServer.equals("IN_USE,"+userName)) {
 			System.out.println("Username " + userName + " is not " +
 					"available, please pick another");
 		} else {
 			System.out.println("Unknown message from server");
 		}
 	}
 	
 	private void sendKey(Socket s, PublicKey key) throws IOException {
 		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
 		out.writeObject(key);
 	}
 	
 	private void sendProtocol(Socket s) throws IOException {
 		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		out.writeInt(Constants.REGISTER);
 	}
 	
  	private void getNewCredentials() {
 		BufferedReader stdin = new BufferedReader(
 				new InputStreamReader(System.in));
 		try {
 			System.out.println("Please enter new username");
 			userName = stdin.readLine();
 		
 			System.out.println("Please enter new password");
 			password = stdin.readLine();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 }
 
 class CreateUserGui extends JFrame implements ActionListener {
 	
 	private JTextField userName;
 	private JPasswordField password;
 	private Button createUsrBtn;
 	private static final String createUsrString = "Create User";
 	private UserRegistrationProtocol p = null;
 	private Socket s;
 
 	public CreateUserGui(UserRegistrationProtocol p, Socket s) {
 		this.p = p;
 		this.s = s;
 		
 		getContentPane().setLayout(
 				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
 		setTitle("Chinese Checkers 0.1");
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		//set size
 		int windowWidth = 300;
 		int windowHeight = 100;
 		setSize(windowWidth, windowHeight);
 		
 		//set location
 		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 		int centerX = (screen.width / 2) - (windowWidth / 2);
 		int centerY = (screen.height / 2) - (windowHeight / 2);
 		setLocation(centerX, centerY);
 		
 		userName = new JTextField("Username");
 		userName.setSize(new Dimension(100, 50));
 		password = new JPasswordField("Password");
 		createUsrBtn = new Button(createUsrString);
 	
 		getContentPane().add(userName);
 		getContentPane().add(password);
 		getContentPane().add(createUsrBtn);
 		createUsrBtn.addActionListener(this);
 		this.setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 		if (arg0.getActionCommand().equals(createUsrString)) {
 			//Create new window for loggin
 			String userNameString = userName.getText();
 			String passwordString = new String(password.getPassword());
 			p.sendNewCredentials(userNameString, passwordString, this.s);
 		}
 	}
 }
