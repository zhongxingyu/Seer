 package WordSearch;
 import java.awt.BorderLayout;
 import java.awt.Point;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 
 /**
  * @author Naomi Plasterer, Brandon Bosso, Jason Steinberg, Austin Diviness
  */
 
 public class Game extends JFrame{
 	private String playerName;
 	private GregorianCalendar time;
 	private String category;
 	private ArrayList<String> words;
 	private int wordsLeft;
 	private Point start;
 	private Point end;
 	private Board board;
 	private WordBank wordBank;
 	private SplashScreen splashScreen;
 	
 	public Game() {
 		//initialize frame
 		setSize(700, 600);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//initialize variables
 		start = null;
 		end = null;
 		
 		//create splash screen and get player name and category file
 		splashScreen = new SplashScreen(getListOfFiles("SecretFiles", ".txt", true));
 		//need a way to wait here for player to press button
 		playerName = splashScreen.getPlayerName();
 		category = splashScreen.getCategory() + ".txt";
 		
 		words = getListOfLinesFromFile("SecretFiles", category);
 		wordsLeft = words.size();
 		board = new Board(20, 20, words);
 		this.add(board, BorderLayout.CENTER);
 		
 		wordBank = new WordBank(words);
 		this.add(wordBank, BorderLayout.EAST);
 		
 	}
 	
 	
 	public void loadConfigFile(String file){
 		// TODO implement function
 	}
 	
 	
 	public boolean checkIfDone(){
 		if(wordsLeft == 0)
 			return true;
 		else	
 			return false;
 	}
 	
 	public void showWinScreen(){
 		// TODO implement function
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
        // vertical word
        if (start.x == end.x) {
            for (int i = start.x; i <= end.x; ++i) {
                board.highlight(start.y, i);
            }
        }
        // horizontal word
        else if (start.y == end.y) {
            for (int i = start.y; i <= end.y; ++i) {
                board.highlight(i, start.x);
            }
        }
        // / diagonal word
        else if (start.x + start.y == end.x + end.y) {
            for (int i = 0; i < (end.x - start.x); ++i) {
                board.hightlight(start.y + i, start.x - i);
            }
        }
        // \ diagonal
        else if (end.x - start.x == end.y - start.y) {
            for (int i = 0; i < (end.x - start.x); ++i) {
                board.highlight(start.x + i, start.x + i);
            }
        }
 	}
 	
 	public String getCategory() {
 		return category;
 	}
 	
 	public void setWordsLeft(int left) {
 		wordsLeft = left;
 	}
 	
 	public ArrayList<String> getWords() {
 		return words;
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
 	
 	
 	public static void main(String[] args) {
 		Game game = new Game();
 		game.setVisible(true);
 		//game.splashScreen.setVisible(true);
 	}
 }
