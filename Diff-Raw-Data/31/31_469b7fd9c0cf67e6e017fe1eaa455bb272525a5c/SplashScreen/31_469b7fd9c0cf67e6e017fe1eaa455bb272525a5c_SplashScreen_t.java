 package WordSearch;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 /**
  * @author Naomi Plasterer, Brandon Bosso, Jason Steinberg, Austin Diviness
  */
 
 public class SplashScreen extends JDialog {
 	private JTextField playerName;
 	private JComboBox categories;
 	private JButton okButton;
 	private JLabel nameLabel, categoryLabel;
 	private Game game;
 	
 	public SplashScreen() {
		// TODO This should be fleshed out better as a default case
		playerName = new JTextField();
		categories = new JComboBox();
		okButton = new JButton("OK");
		nameLabel = new JLabel("Enter Name: ");
		categoryLabel = new JLabel("Select a category: ");
		this.game = null;
		categories = null;
		//instantiate frame
		setSize(new Dimension(400,300));
		setLayout(new GridLayout(3,2));
		setTitle("Welcome!");
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		okButton.addActionListener(new buttonListener());
		//add components
		add(nameLabel);
		add(playerName);
		add(categoryLabel);
		add(categories);
		add(okButton);
 	}
 	
 	public SplashScreen(ArrayList<String> words, Game game) {
 		//instantiate variables
 		playerName = new JTextField();
 		categories = new JComboBox();
 		okButton = new JButton("OK");
 		nameLabel = new JLabel("Enter Name: ");
 		categoryLabel = new JLabel("Select a category: ");
 		this.game = game;
 		
 		//instantiate frame
 		setSize(new Dimension(400,300));
 		setLayout(new GridLayout(3,2));
 		setTitle("Welcome!");
 		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
 		
 		okButton.addActionListener(new buttonListener());
 		
 		for(String s : words) {
 			categories.addItem(s);
 		}
 		
 		
 		//add components
 		add(nameLabel);
 		add(playerName);
 		add(categoryLabel);
 		add(categories);
 		add(okButton);
 	}
 	
 	public class buttonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String name = playerName.getText();
 			
 			//if player has entered name, set name and category
 			if(name.length() != 0) {
 				//set player name
 				getGame().setPlayerName(name);
 				//set word file
 				String category = (String) categories.getSelectedItem();
 				getGame().setCategory(category + ".txt");
 				//close screen
 				dispose();
 			}
 			//otherwise give error message
 			else {
 				JOptionPane.showMessageDialog(null, "Please enter a name");
 			}
 		}
 		
 	}
 
 	public void setCategoriesText(String category) {
 		categories.setSelectedItem(category);
 	}
 	
 	public void click() {
 		okButton.doClick();
 	}
 	
 	public Game getGame() {
 		return game;
 	}
 	
 	public static void main(String[] args) {
 		Game game = new Game();
 		ArrayList<String> words = new ArrayList<String>();
 		words.add("Brandon");
 		words.add("Dylon");
 		words.add("Clayton");
 		SplashScreen screen = new SplashScreen(words,game);
 		screen.setVisible(true);
 	}
 }
