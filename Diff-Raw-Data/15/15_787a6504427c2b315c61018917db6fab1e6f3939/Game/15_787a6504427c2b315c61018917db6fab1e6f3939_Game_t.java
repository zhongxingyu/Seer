 package WordSearch;
 import java.awt.BorderLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 /**
  * @author Naomi Plasterer, Brandon Bosso, Jason Steinberg, Austin Diviness
  */
 
 public class Game extends JFrame{
 	private String playerName;
 	private GregorianCalendar time;
 	private String category;
 	public static Game currentGame;
 	//private ArrayList<String> words;
 	private int wordsLeft;
 	private Point start;
 	private Point end;
 	private Board board;
 	private WordBank wordBank;
 	private SplashScreen splashScreen;
 	private JMenuBar menu;
 	private JMenu file;
 	private JMenuItem play, exit, newGame;
 	
 	public Game() {
 		currentGame = this;
 		//initialize frame
 		setSize(700, 600);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//initialize variables
 		start = null;
 		end = null;
 		
 		//create JMenuBar
 		menu = new JMenuBar();
 		file = new JMenu("File");
 		play = new JMenuItem("Play/Pause");
 		exit = new JMenuItem("Exit");
 		newGame = new JMenuItem("New Game");
 		newGame.addActionListener(new fileListener());
 		play.addActionListener(new fileListener());
 		exit.addActionListener(new fileListener());
 		file.add(play);
 		file.add(newGame);
 		file.add(exit);
 		menu.add(file);
 		setJMenuBar(menu);
 		
 		//create splash screen and get player name and category file
 		splashScreen = new SplashScreen(getListOfFiles("SecretFiles", ".txt", true));
 
 		
 	}
 	
 	private class fileListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(e.getSource() == play) {
 				if(board.isVisible())
 					board.setVisible(false);
 				else
 					board.setVisible(true);
 			}
 			else if(e.getSource() == newGame) {
 				int reply = JOptionPane.showConfirmDialog(null, "Are you sure you would like to start a new game?", "Please choose an option", JOptionPane.YES_NO_OPTION);
 				if(reply == JOptionPane.YES_OPTION) {
 					dispose();
 					startNewGame();
 				}
 			}
 			else if(e.getSource() == exit) 
 				System.exit(0);
 		}
 		
 	}
 	
 	
 	public void loadConfigFile(String file){
 		playerName = splashScreen.getPlayerName();
 		ArrayList<String> words = getListOfLinesFromFile("SecretFiles", file);
 		wordsLeft = words.size();
 		wordBank = new WordBank(words);
 		this.add(wordBank, BorderLayout.EAST);
 		board = new Board(20, 20, words);
 		this.add(board, BorderLayout.CENTER);
 		
 		
 		
 		this.setVisible(true);
 		board.listen();
 	}
 	
 	
 	public boolean checkIfDone(){
		return (wordsLeft == 0) ? true : false;
 	}
 	
 	public void showWinScreen(){
 		int reply = JOptionPane.showConfirmDialog(null, "Congratulations " + playerName + ", you win! Would you like to play again?", "Please choose an option", JOptionPane.YES_NO_OPTION);
 		if(reply == JOptionPane.YES_OPTION) {
 			dispose();
 			startNewGame();
 		}
 		else {
 			System.exit(0);
 		}
 	}
 	
 	public boolean checkValidWord(String word){
 		// TODO implement function
 		return wordBank.contains(word);
 	}
 	
 	public boolean checkValidSelection(Point start, Point end){
 		// checks rows, cols, \ diagonal, / diagonal
 		boolean flag = false;
 		if (start.x == end.x) {
 			flag = true;
 		}
 		else if (start.y == end.y) {
 			flag = true;
 		}
 		else if ((start.x - start.y) == (end.x - end.y)) {
 			flag = true;
 		}
 		else if ((start.x + start.y) == (end.x + end.y)) {
 			flag = true;
 		}
 		return flag;
 	}
 	
 	public void highlightWords(Point start, Point end) {
         // vertical word downwards
         if (start.x == end.x && start.y < end.y) {
             for (int i = start.y; i <= end.y; ++i) {
                 board.highlight(i, start.x);
             }
         }
         // vertical word upwards
         if (start.x == end.x && start.y > end.y) {
             for (int i = start.y; i >= end.y; --i) {
                 board.highlight(i, start.x);
             }
         }
         // horizontal word left->right
         else if (start.y == end.y && start.x < end.x) {
             for (int i = start.x; i <= end.x; ++i) {
                 board.highlight(start.y, i);
             }
         }
         // horizontal word right->left
         else if (start.y == end.y && start.x > end.x) {
             for (int i = start.x; i >= end.x; --i) {
                 board.highlight(start.y, i);
             }
         }
         // / diagonal word upper right to lower left
         else if (start.x + start.y == end.x + end.y && start.x > end.x) {
             for (int i = 0; i <= (start.x - end.x); ++i) {
                 board.highlight(start.y + i, start.x - i);
             }
         }
         // / diagonal word lower left to upper right
         else if (start.x + start.y == end.x + end.y && start.x < end.x) {
             for (int i = 0; i <= (end.x - start.x); ++i) {
                 board.highlight(start.y - i, start.x + i);
             }
         }
         // \ diagonal upper left to lower right
         else if (end.x - start.x == end.y - start.y && start.x < end.x) {
             for (int i = 0; i <= (end.x - start.x); ++i) {
                 board.highlight(start.y + i, start.x + i);
             }
         }
         // \ diagonal lower right to upper left
         else if (end.x - start.x == end.y - start.y && start.x > end.x) {
             for (int i = 0; i <= (start.x - end.x); ++i) {
                 board.highlight(start.y - i, start.x - i);
             }
         }
         board.repaint();
 	}
 	
 	public String getCategory() {
 		return category;
 	}
 	
 	public void setWordsLeft(int left) {
 		wordsLeft = left;
 	}
 	
 	public ArrayList<String> getWords() {
 		return wordBank.getWords();
 	}
 	
 	public WordBank getWordBank() {
 		return wordBank;
 	}
 	
 	public void setWordBank(WordBank bank) {
 		// TODO should this set the words left?
 		wordBank = bank;
 	}
 	
 	public Board getBoard() {
 		return board;
 	}
 	
 	public String getDataDirectoryFullPath(String dataDirectory) throws IOException {
 		String currentPath = new java.io.File(".").getCanonicalPath(); // Get current path
 		return currentPath + File.separatorChar + dataDirectory;  // Add on name of data directory
 	}
 	
 	public ArrayList<String> getListOfLinesFromFile(String dataDirectory, String inputFile) {
 		FileReader fileReader = null;
 		String dataPath = "";
 		ArrayList<String> result = new ArrayList<String>();
 		
 		try {
 			dataPath =  getDataDirectoryFullPath(dataDirectory);
 			
 			fileReader = new FileReader(dataPath + File.separatorChar + inputFile);
 		} 	catch (FileNotFoundException e) {
 			System.out.println("Unable to load file: " + dataPath + File.separatorChar + inputFile);
 			System.exit(1);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("Unable to get path: " + dataPath + File.separatorChar + inputFile);
 			System.exit(1);
 		}
 
 		Scanner in = new Scanner(fileReader);
 		
 		String line = " ";
 		while (in.hasNext()) {
 			line = in.nextLine();
 			result.add(line);			
 		}
 		
 		return result;		
 	}
 	
 	public ArrayList<String> getListOfFiles(String dataDirectory, String onlyLookAtExtension, boolean removeExtension) {		
 		ArrayList<String> results = new ArrayList<String>();
 		String dataPath = "";
 				
 		try {
 			dataPath = getDataDirectoryFullPath(dataDirectory); // Get data path
 
 			// This snippet was inspired by Sean Kleinjung and follows 
 			//  closely to his code, with some code added to see if the user wanted an extension.
 			//  From http://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
 			File[] files = new File(dataPath).listFiles();
 
 			for (File file : files) {
 			    if (file.isFile()) {
 			    	if (removeExtension && onlyLookAtExtension != "") { 
 			    		if (file.getName().substring(file.getName().lastIndexOf(onlyLookAtExtension)).equalsIgnoreCase(onlyLookAtExtension)) {
 			    			results.add(file.getName().substring(0,file.getName().lastIndexOf(onlyLookAtExtension)));
 			    		}
 			    	} else {
 			    		results.add(file.getName());
 			    	}
 			    }
 			}
 			//End snippet
 			return results;			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("Unable to find path.");
 			System.exit(2);
 		}
 		return null;
 	}
 	
 	public void setPlayerName(String name) {
 		this.playerName = name;
 	}
 	
 	public void setCategory(String category) {
 		this.category = category;
 	}
 	
 	public static void startNewGame() {
 		Game game = new Game();
 
 	}
 	
 	public void runWord() {
 		Point start = board.getMousePress();
 		Point end = board.getMouseRelease();
 		String word;
 		if (checkValidSelection(start, end)) {
 			word = board.getWordAt(start, end);
 			if (checkValidWord(word)) {	
 				if(!wordBank.getWordBank().get(word).isSelected()) {
 					wordBank.checkBox(word);
 					--wordsLeft;
 					this.highlightWords(start, end);
 				}
 			}
 			else if(checkValidWord(new StringBuffer(word).reverse().toString())) {
 				if(!wordBank.getWordBank().get(new StringBuffer(word).reverse().toString()).isSelected()) {
 					this.highlightWords(start, end);
 					wordBank.checkBox(new StringBuffer(word).reverse().toString());
 					--wordsLeft;
 				}
 			}
			if (checkIfDone()) {
				showWinScreen();
			}
 		}
 	}
 	
 	public void decreaseWordsLeft() {
 		wordsLeft--;
 	}
 	
 	
 	public static void main(String[] args) {
 		startNewGame();
 		//game.setVisible(true);
 		//game.highlightWords(new Point(0,0), new Point(5,5));
 		//game.splashScreen.setVisible(true);
 	}
 }
