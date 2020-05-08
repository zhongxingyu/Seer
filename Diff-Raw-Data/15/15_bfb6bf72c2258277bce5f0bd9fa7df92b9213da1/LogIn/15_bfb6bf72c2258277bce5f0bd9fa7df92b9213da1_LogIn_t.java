 
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.Serializable;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  * Scene to let user log into game.
  * 
  * @author TeamTroll
  * @version 1.0
  */
 public class LogIn implements Scene, Serializable {
 	private JLabel titleLabel, userLabel, passLabel, outputLabel;
 	private JTextField userText, passText;
 	private JButton loginButton, newAccButton, skipAheadButton;
 	private GameScreen screen;
 
 	/**
 	 * Creates the login scene.
 	 * 
 	 * @param GameScreen
 	 *            screen The screen of the game.
 	 */
 	public LogIn(final GameScreen screen) {
 		this.screen = screen;
 
 		titleLabel = new JLabel("Space Trader");
 		userLabel = new JLabel("Username:");
 		passLabel = new JLabel("Password:");
 		outputLabel = new JLabel();
 
 		userText = new JTextField(10);
 		passText = new JTextField(10);
 
 		loginButton = new JButton("Login");
 		newAccButton = new JButton("Create Account");
 		skipAheadButton = new JButton("skipAheadButton");
 
 		loginButton.addActionListener(new ActionListener() {
 			/**
 			 * Go to create player scene.
 			 * 
 			 * @param ActionEvent
 			 *            e The created action event.
 			 */
 			public void actionPerformed(ActionEvent e) {
				loginUser();
 			}
 		});
 
 		newAccButton.addActionListener(new ActionListener() {
 			/**
 			 * Goes to create account scene.
 			 * 
 			 * @param ActionEvent
 			 *            e The created action event.
 			 */
 			public void actionPerformed(ActionEvent e) {
 				screen.setScene(new CreateAccount(screen));
 			}
 		});
 
 		skipAheadButton.addActionListener(new ActionListener() {
 			/**
 			 * Skips to universe scene.
 			 * 
 			 * @param ActionEvent
 			 *            e The created action event.
 			 */
 			public void actionPerformed(ActionEvent e) {
 				screen.game.skip();
 			}
 		});
 	}
 
 	/**
 	 * Checks if login data is valid. If so, goes to the create player scene.
 	 * Otherwise, goes to the universe screen.
 	 */
	public void loginUser() {
 		String userInput = userText.getText();
 		String passInput = passText.getText();
 		// So I rewrote the Game.checkAccount, Game.getPlayer, etc. to
 		// screen.game.checkAccount etc.
 		// I really thought it'd be easier to access the information this way
 		// especially after we store
 		// it.
 		if (userText.getText().length() == 0
 				&& passText.getText().length() == 0)
 			outputLabel.setText("Enter account information");
 		else if (!screen.game
 				.checkAccount(new UserAccount(userInput, passInput)))
 			outputLabel.setText("Invalid Account");
 		else if (screen.game.getPlayer() == null) {
 			screen.game.setUserAccount(new UserAccount(userInput, passInput));
 			screen.setScene(new CreatePlayer(screen));
 		} else
 			screen.setScene(screen.game.getUniverse());
 	}
 
 	/**
 	 * Paints the components onto the screen.
 	 * 
 	 * @param JPanel
 	 *            p The panel to contain the components.
 	 * @param Graphics
 	 *            g The graphics to the corresponding page.
 	 */
 	public void paint(JPanel p, Graphics g) {
 		p.removeAll();
 
 		p.add(titleLabel);
 		p.add(userLabel);
 		p.add(userText);
 		p.add(passLabel);
 		p.add(passText);
 		p.add(loginButton);
 		p.add(outputLabel);
 		p.add(newAccButton);
 		p.add(skipAheadButton);
 
 		p.revalidate();
 	}
 	public String toString(){
 		return super.toString();
 	}
 }
