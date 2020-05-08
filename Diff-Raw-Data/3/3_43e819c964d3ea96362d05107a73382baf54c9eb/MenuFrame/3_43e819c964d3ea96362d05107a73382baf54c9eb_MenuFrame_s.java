 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.TextArea;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 /**
  * JFrame to display menu options to the user. Used to select game settings then
  * initiate a new game.
  * 
  * @author sturgedl. Created Apr 17, 2013.
  */
 public class MenuFrame extends JFrame {
 	private static final int FRAME_WIDTH = 600;
 	private static final int FRAME_HEIGHT = 800;
 
 	private static final int TITLE_WIDTH = 1000;
 	private static final int TITLE_HEIGHT = 100;
 
 	private JPanel buttonPanel;
 	private ArrayList<JButton> buttons;
 
 	private String language;
 	private String[] labels;
 
 	public MenuFrame(String lang) {
 		super();
 		this.language = lang;
 		this.labels = MenuFrame.obtainButtonLabels(lang);
 		this.setVisible(true);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 
 		JPanel titleLabel = new JPanel();
 		titleLabel.setBackground(Color.CYAN);
 
 		JLabel title = new JLabel("Sorry!");
 		title.setFont(new Font("Georgia", Font.BOLD, 60));
 		titleLabel.setPreferredSize(new Dimension(TITLE_WIDTH, TITLE_HEIGHT));
 		titleLabel.add(title, BorderLayout.CENTER);
 		this.add(titleLabel, BorderLayout.NORTH);
 
 		this.buttonPanel = new JPanel();
 		this.buttons = new ArrayList<JButton>();
 
 		this.initializeButtons();
 		this.add(buttonPanel);
 	}
 
 	/**
 	 * Fetch the button labels from an appropriate location based on the given
 	 * language.
 	 * 
 	 * @param lang
 	 * @return
 	 */
 	private static String[] obtainButtonLabels(String lang) {
 		String[] ret = new String[4];
 		ret[0] = "New Game";
 		ret[1] = "Load Game";
 		ret[2] = "Instructions";
 		ret[3] = "Exit";
 		return ret;
 	}
 
 	private void initializeButtons() {
 		if (this.buttonPanel == null)
 			this.buttonPanel = new JPanel();
 		if (this.buttons == null)
 			this.buttons = new ArrayList<JButton>();
 
 		JButton newGame = new JButton(this.labels[0]);
 		this.buttons.add(newGame);
 		JButton loadGame = new JButton(this.labels[1]);
 		this.buttons.add(loadGame);
 		JButton instructions = new JButton(this.labels[2]);
 		this.buttons.add(instructions);
 		JButton exit = new JButton(this.labels[3]);
 		this.buttons.add(exit);
 
 		newGame.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				MenuFrame.this.remove(MenuFrame.this.buttonPanel);
 				MenuFrame.this.repaint();
 				MenuFrame.this.createNewGame();
 			}
 		});
 
 		loadGame.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MenuFrame.this.loadExistingGame();
 
 			}
 		});
 
 		instructions.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MenuFrame.this.displayInstructions();
 
 			}
 		});
 
 		exit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 
 		for (JButton b : this.buttons)
 			this.buttonPanel.add(b);
 
 	}
 
 	protected void loadExistingGame() {
 		// TODO update to reflect the constructor reading from a file
 //		this.setEnabled(false);
 		SorryFrame sorry = new SorryFrame(this.language);
 		this.dispose();
 	}
 
 	/**
 	 * Method to ask the user for names then create a new save file, then load
 	 * the fresh game.
 	 * 
 	 */
 	protected void createNewGame() {
 		// TODO Auto-generated method stub.
 
 	}
 
 	private void displayInstructions() {
 		JFrame instr = new InstructionsFrame(this.language);
 	}
 
 	public static void main(String args[]) {
 		JFrame frame = new MenuFrame("english");
 		//JFrame frame2 = new SorryFrame("english");
 	}
 
 }
