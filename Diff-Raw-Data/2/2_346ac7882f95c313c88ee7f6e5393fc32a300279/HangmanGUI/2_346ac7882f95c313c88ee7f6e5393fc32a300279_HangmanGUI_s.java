 package edu.ucsb.cs56.projects.games.cs56_games_hangman;
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import javax.swing.*;
 import java.applet.*;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 
 /** HangmanGUI.java is a program that has added features from a
  * previous Hangman game
 
  *@author Gyeonghun Lee
  *@author David Borden and Ernesto Cojulun
  *@author Evan West
  *@author Blake Zimmerman, CS56 W14
  *@version Winter 2014, CS56
  *@see HangmanGame
  *@see WordList
  */
 
 public class HangmanGUI extends JFrame implements HangmanInterface {
 
     public static final boolean soundOn = false;
 
     private HangmanGame hg;
     private JComponent gallow;
     private JLabel prompt, board, message, guesses;
     private JTextField lettertf;
     private JButton submit, exit, restart, instructions, hint;
     private WordList wordList;
     private Panel upper, lower;
     private JFrame f;
     private Applet song;
         
     //Button handler
     private SubmitButtonHandler submitHandler;
     private ExitButtonHandler exitHandler;
     private RestartButtonHandler restartHandler;
     private instructButtonHandler instructHandler;
     private hintButtonHandler hintHandler;
         
     /** Constructor for GUI that takes a Wordlist parameter
      *@param wordList takes in the wordList.txt by default but will also take in a user created wordList
      */
 
     public HangmanGUI(WordList wordList) throws IOException {
 	hg = new HangmanGame(wordList.getRandomLine());
 	this.wordList = wordList;
     }
     
     /** Initializes and shows all GUI elements
      */
     public void play() {
 	f = new JFrame();
 	f.setSize(700, 700);
 	Panel upper = new Panel();
 	Font newFont = new Font("serif", Font.BOLD, 16);
 
 	upper.setLayout(new BoxLayout(upper,BoxLayout.Y_AXIS));
 	
      	gallow = new hanger();
     	message = new JLabel();
 	board = new JLabel(hg.getBoard());
 
 	guesses = new JLabel("Wrong guesses: ");
       	guesses.setFont(newFont);
 	prompt = new JLabel("Guess a letter: ");
 	prompt.setFont(newFont);
 	lettertf = new JTextField(3);
 	
 
 	submit = new JButton("Submit");
 	submit.setFont(newFont);
 	submitHandler = new SubmitButtonHandler();
 	submit.addActionListener(submitHandler);
 	f.getRootPane().setDefaultButton(submit); //Use "Enter key" as default for submit button.
 	f.getContentPane().add(upper,BorderLayout.NORTH);
 
 	//add items to the upper pannel
 	upper.add(gallow);
 	upper.add(message);
 	upper.add(board);
 	upper.add(guesses);
 	upper.add(prompt);
 	upper.add(lettertf);
 	upper.add(submit);
 	submit.setAlignmentX(Component.CENTER_ALIGNMENT);	
 	
 	Panel lower = new Panel();
 	lower.setLayout(new FlowLayout());
 
 	instructions = new JButton("Instructions");
 	instructions.setFont(newFont);
 	instructHandler = new instructButtonHandler();
 	instructions.addActionListener(instructHandler);
 
 	exit = new JButton("Exit");
 	exit.setFont(newFont);
 	exitHandler = new ExitButtonHandler();
 	exit.addActionListener(exitHandler);
 	
 	restart = new JButton("Restart");
 	restart.setFont(newFont);
 	restartHandler = new RestartButtonHandler();
 	restart.addActionListener(restartHandler);
 
 	hint = new JButton("Hint");
 	hint.setFont(newFont);
         hintHandler = new hintButtonHandler();
 	hint.addActionListener(hintHandler);
 
 	f.getContentPane().add(lower,BorderLayout.SOUTH);
 
 	//add buttons to lower pannel
 	lower.add(instructions);
 	lower.add(exit);
 	lower.add(restart);
 	lower.add(hint);
 	
 	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 	f.setVisible(true);
 	f.setTitle("Hangman Game: GUI Version");
     }
 
 	//Specify handlers for each button and add (register) ActionListeners to each button.
     
     /** Inner class for Handler for Submit button
 	(button that handles guess commits)
 	Handles all game state and UI changes
      */
     public class SubmitButtonHandler implements ActionListener {
 	public void actionPerformed(ActionEvent e) {
 	    String word = lettertf.getText();   
 	    repaint();
 	    MakeSound sound = new MakeSound();
 	    Font newFont = new Font("serif", Font.BOLD, 16);
 
 	    lettertf.requestFocusInWindow();
 	    
 	    if(word.length() < 1) {
 		message.setText("Must type something!");
 		message.setFont(newFont);
 		if (soundOn) {
 		    sound.playSound( GUIMain.class.getClassLoader().getResourceAsStream("resources/Glass.aiff")); 
 		}
 		
 		return;
 	    } else if(word.length() > 1) {
 		message.setText("Do not type more than one letter at a time.");
 		message.setFont(newFont);
 		if (soundOn) { 
 		    sound.playSound( GUIMain.class.getClassLoader().getResourceAsStream("resources/Sosumi.aiff"));
 		}
 		lettertf.setText("");
 		repaint();
 		return;
 	    }
 	    
 	    char letter = word.charAt(0);
 	    lettertf.setText("");
 	    
 	    try{
 		if(hg.guessLetter(letter) == true) {
 		    message.setText("Good Guess!");
 		    message.setFont(newFont);
 		    if (soundOn) {
 			sound.playSound( GUIMain.class.getClassLoader().getResourceAsStream("resources/Glass.aiff"));
 		    }
 		    repaint();
 		}
 		else
 	        {
 		    message.setText("Incorrect. Guesses Left: " + hg.getWrongAttemptsLeft());
 		    message.setFont(newFont);
 		    if (soundOn) {
 			sound.playSound( GUIMain.class.getClassLoader().getResourceAsStream("resources/Glass.aiff"));
 		    }
 		    gallow.repaint();
 		repaint();
 
 		}
 
 	    } catch(AlreadyTriedException ex) {
 		message.setText("You have already tried that letter, please try another one.");
 			if (soundOn) {
 		    sound.playSound( GUIMain.class.getClassLoader().getResourceAsStream("resources/Glass.aiff"));
 			}
 		repaint();
 
 	    }
 	    
 	    board.setText(hg.getBoard());
 	    
 	    //update incorrect guesses
 	    String wrongGuesses = "Wrong guesses:";
 	    for(Character item : hg.getWrongLetters()){
 		wrongGuesses+=(" "+item);
 	    }
 	    guesses.setText(wrongGuesses);
 	    guesses.setFont(newFont);
 
 	    if(hg.hasWon()) {
 		submit.setEnabled(false);
 		lettertf.setEditable(false);
 		prompt.setText("");
 		message.setText("Congratulations, You have won!");
 		message.setFont(newFont);
 			if (soundOn) {
 		     sound.playSound( GUIMain.class.getClassLoader().getResourceAsStream("resources/BOO.wav"));
 			}  
 		repaint();
 
 	    }
 	    if(hg.hasLost()) {
 		submit.setEnabled(false);
 		lettertf.setEditable(false);
 		prompt.setText("");
 		message.setText("Sorry, you have lost!" + "  " + "The secret word was " + hg.getSecretWord() + ".  " + "Try again!");
 		message.setFont(newFont);
 	
 			if (soundOn) {
 		      sound.playSound( GUIMain.class.getClassLoader().getResourceAsStream("resources/Glass.aiff"));
 			}
 		repaint();
 	    }
 	     repaint();
 	}
     }
 
     /** Handler for instructions button
 	Button displays a messageDialog with text instructions
      */
 
     public class instructButtonHandler implements ActionListener {
 	public void actionPerformed(ActionEvent e) {
 	    JOptionPane.showMessageDialog(f,
 		"Each * represents a letter in a word. Your task\nis to guess each letter in the word until you\ncomplete the word. Each wrong guess adds a \nbody part to the hanger. Once an entire\nman is created from wrong guesses\n(6 wrong guesses) then you lose the game.\nIf you guess the word before then, you win!\n To get a letter that is in the word press the hint button and a pop-up button will display with a letter from a random position in the word",
 		"Instructions", 
 		JOptionPane.INFORMATION_MESSAGE);
 	}
     }
     
     /** Handler for exit button.
 	Button closes the application
      */
     public class ExitButtonHandler implements ActionListener {
 	public void actionPerformed(ActionEvent e) {
 	    System.exit(0);
 	}
     }
     
     /** Handler for Restart Button.
 	Clears old game state, create a new game, and updates UI accordingly.
      */
     public class RestartButtonHandler implements ActionListener {
 	public void actionPerformed(ActionEvent e) {
 	    lettertf.requestFocusInWindow();
 	    submit.setEnabled(true);
 	    lettertf.setEditable(true);
 	    Font newFont = new Font("serif", Font.BOLD, 16);
 	    prompt.setText("Guess a letter: ");
 	    prompt.setFont(newFont);
 	    
 	    try {
 		hg = new HangmanGame(wordList.getRandomLine());
 		board.setText(hg.getBoard());
 		message.setText("");
 		guesses.setText("Wrong guesses:");
 		guesses.setFont(newFont);
 		repaint();
 	    } catch(IOException ex) {
 		throw new RuntimeException(ex);
 	    }
 	}
     }
 
     /** Handler for Hint button.
 	Button pops up a messageDialog with a letter that is in the word
      */
     public class hintButtonHandler implements ActionListener {
 	public void actionPerformed(ActionEvent e) {
 	    int wordlength = hg.getSecretWord().length();
 	    int randomNum = (int) (Math.random()*(wordlength));
 
 	   char displayletter = hg.getSecretWord().charAt(randomNum);
 	    JOptionPane.showMessageDialog(f,"Your hint is  "+displayletter );
 		}
 	}
         
     /** Class to handle loading and playing of game sounds
      */
     public class MakeSound {
 	
 	private final int BUFFER_SIZE = 128000;
 	private File soundFile;
 	private AudioInputStream audioStream;
 	private AudioFormat audioFormat;
 	private  SourceDataLine sourceLine;
 	
 	/** Plays a sound by filename
 	    @param filename Path to the sound file to play
 	 */
 	public void playSound(String filename){
 	        
 	    String strFilename = filename;
 	    
 	    try {
 		soundFile = new File(strFilename);
 	    } catch (Exception e) {
 		e.printStackTrace();
 		System.exit(1);
 	    }
 	    
 	    try {
 		audioStream = AudioSystem.getAudioInputStream(soundFile);
 	    } catch (Exception e){
 		e.printStackTrace();
 		System.exit(1);
 	    }
 	    
 	    audioFormat = audioStream.getFormat();
 	    
 	    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
 	    try {
 		sourceLine = (SourceDataLine) AudioSystem.getLine(info);
 		sourceLine.open(audioFormat);
 	    } catch (LineUnavailableException e) {
 		e.printStackTrace();
 		System.exit(1);
 	    } catch (Exception e) {
 		e.printStackTrace();
 		System.exit(1);
 	    }
 	    
 	    sourceLine.start();
 	    
 	    int nBytesRead = 0;
 	    byte[] abData = new byte[BUFFER_SIZE];
 	    while (nBytesRead != -1) {
 		try {
 		    nBytesRead = audioStream.read(abData, 0, abData.length);
 		} catch (IOException e) {
 		    e.printStackTrace();
 		}
 		if (nBytesRead >= 0) {
 		    @SuppressWarnings("unused")
 			int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
 		}
 	    }
 	    
 	    sourceLine.drain();
 	    sourceLine.close();
 	}
 
 	/** Plays a sound by inputstream
 	    @param is InputStream representing file to play
 	 */
 
 	public void playSound(InputStream is){
 	    
 	    try {
 		audioStream = AudioSystem.getAudioInputStream(is);
 	    } catch (Exception e){
 		e.printStackTrace();
 		System.exit(1);
 	    }
 	    
 	    audioFormat = audioStream.getFormat();
 	    
 	    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
 	    try {
 		sourceLine = (SourceDataLine) AudioSystem.getLine(info);
 		sourceLine.open(audioFormat);
 	    } catch (LineUnavailableException e) {
 		e.printStackTrace();
 		System.exit(1);
 	    } catch (Exception e) {
 		e.printStackTrace();
 		System.exit(1);
 	    }
 	    
 	    sourceLine.start();
 	    
 	    int nBytesRead = 0;
 	    byte[] abData = new byte[BUFFER_SIZE];
 	    while (nBytesRead != -1) {
 		try {
 		    nBytesRead = audioStream.read(abData, 0, abData.length);
 		} catch (IOException e) {
 		    e.printStackTrace();
 		}
 		if (nBytesRead >= 0) {
 		    @SuppressWarnings("unused")
 			int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
 		}
 	    }
 	    
 	    sourceLine.drain();
 	    sourceLine.close();
 	}
 	
 	
     } // inner class MakeSound
     
     /** Component that draws a gallows in progressive states
      */
     public class hanger extends JComponent {
 	hanger() {
 	    setPreferredSize(new Dimension(300,300));
 	}
 
 	/** This is an override from Component, and draws a gallows at proper state directly from getWrongAttemptsLeft retrieved from game instance
 	 */
 	@Override
 	public void paintComponent (Graphics g) {
 	    Graphics2D g2 = (Graphics2D)g;
 
 	    g2.setColor(Color.BLACK);
 	    g2.drawRect(190,250, 200, 15);
 	    g2.drawLine(290, 250, 290, 5);
 	    g2.drawLine(290,5,410,5);
 	    g2.drawLine(410,5,410,20);
 	    
 	    if(hg.getWrongAttemptsLeft() < 6)
 		g2.drawOval(385, 20, 50, 50);
 	    if(hg.getWrongAttemptsLeft() < 5)
 		g2.drawLine(410, 70, 410, 150);
 	    if(hg.getWrongAttemptsLeft() < 4)
 		g2.drawLine(410,150,350,190);
 	    if(hg.getWrongAttemptsLeft() < 3)
 		g2.drawLine(410,150,470,190);
 	    if(hg.getWrongAttemptsLeft() < 2)
 		g2.drawLine(410, 100, 350, 75);
 	    if(hg.getWrongAttemptsLeft() < 1)
 		g2.drawLine(410, 100, 470, 75);
 	}
     } // inner class hanger
 }
