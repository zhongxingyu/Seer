 package com.jollex.TypingTest;
 
 import javax.swing.*;
 import java.util.Random;
 import java.util.Scanner;
 import java.awt.*;
 import java.awt.event.*;
 
 public class Screen {
 	private static String[] words = new String[270];
 	
 	private Random generator = new Random();
 	private static int num = 270;
 	private static int[] numbers;
 	
 	private JFrame frame;
 	private JPanel game;
 	private JLabel instructions;
 	private JButton start;
 	private JPanel topGame;
 	private JLabel word;
 	private JLabel timeLeft;
	private int timeLeftInt = 60;
 	private JTextField ans;
 	private JLabel score;
 	private JLabel accuracy;
 	private JButton restart;
 	private final int WINDOW_WIDTH = 500;
 	private final int WINDOW_HEIGHT = 115;
 	
	private final int TIMER_SECONDS = 60;
	private int count = 0;
	
 	private static int wordsCorrect = 0;
 	private static int wordsTotal = 0;
 	
 	private Screen() {
 		this.loadWords();
 		
 		//Create and set up the frame
 		frame = new JFrame("Typing Test");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//Create a game content pane
 		game = new JPanel();
 		game.setLayout(new BoxLayout(game, BoxLayout.Y_AXIS));
 		
 		//Create and add instructions label
 		instructions = new JLabel("<html><center>Type words in the text box as they appear." +
 				"<br>After 60 seconds, your score and accuracy will be displayed." +
 				"<br>Press start when you're ready. Good luck!</center></html>");
 		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
 		int center = (WINDOW_WIDTH - (int)(instructions.getPreferredSize().getWidth())) / 2;
 		instructions.setBorder(BorderFactory.createEmptyBorder(5, center, 5, 0));
 		game.add(instructions);
 		instructions.setVisible(true);
 		
 		//Create and add start button
 		start = new JButton("Start");
 		start.setAlignmentX(Component.CENTER_ALIGNMENT);
 		game.add(start);
 		start.setVisible(true);
 		
 		//Create action listener and add it to button
 		ActionListener startAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gameStart();
 			}
 		};
 		start.addActionListener(startAction);
 		
 		//Create content pane for word and timer
 		topGame = new JPanel();
 		topGame.setLayout(new FlowLayout());
 		topGame.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
 		game.add(topGame);
 		
 		//Create and add word label
 		word = new JLabel(newWord());
 		word.setFont(new Font("Dialog", Font.PLAIN, 18));
 		word.setAlignmentX(Component.LEFT_ALIGNMENT);
		int align = (WINDOW_WIDTH - 160) / 2;
 		word.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, align));
 		topGame.add(word);
 		word.setVisible(false);
 		
 		//Create and add timer label
 		timeLeft = new JLabel(Integer.toString(timeLeftInt));
 		timeLeft.setFont(new Font("Dialog", Font.PLAIN, 18));
 		timeLeft.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		timeLeft.setBorder(BorderFactory.createEmptyBorder(0, align, 0, 10));
 		topGame.add(timeLeft);
 		timeLeft.setVisible(false);
 		
 		//Create and add answer text field
 		ans = new JTextField(20);
 		ans.setFont(new Font("Dialog", Font.PLAIN, 16));
 		game.add(ans);
 		ans.setSize(WINDOW_WIDTH, 10);
 		ans.setVisible(false);
 		
 		//Creates action listener for text field and adds it
 		ActionListener enter = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				update();
 			}
 		};
 		ans.addActionListener(enter);
 		
 		//Create and add score label
 		score = new JLabel("Score: ");
 		score.setFont(new Font("Dialog", Font.PLAIN, 18));
 		score.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
 		score.setAlignmentX(Component.CENTER_ALIGNMENT);
 		game.add(score);
 		score.setVisible(false);
 		
 		//Create and add accuracy label
 		accuracy = new JLabel("Accuracy: ");
 		accuracy.setFont(new Font("Dialog", Font.PLAIN, 18));
 		accuracy.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
 		accuracy.setAlignmentX(Component.CENTER_ALIGNMENT);
 		game.add(accuracy);
 		accuracy.setVisible(false);
 		
 		//Create and add restart button
 		restart = new JButton("Play again");
 		restart.setAlignmentX(Component.CENTER_ALIGNMENT);
 		game.add(restart);
 		restart.setVisible(false);
 		
 		//Create action listener for restart button and add it
 		ActionListener restartAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				restart();
 			}
 		};
 		restart.addActionListener(restartAction);
 		
 		//Add content pane to frame
 		frame.setContentPane(game);
 		
 		//Size and then display the frame
 		frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 		frame.setVisible(true);
 	}
 	
 	//Creates a 60 second timer
 	ActionListener updater = new ActionListener() {
 		public void actionPerformed(ActionEvent time) {
			if (count > TIMER_SECONDS) {
 				timer.stop();
 				gameDone();
 			}
 			if (timeLeftInt > 0) {
 			timeLeftInt--;
 			timeLeft.setText(Integer.toString(timeLeftInt));
 			}
			count++;
 		}
 	};
 	Timer timer = new Timer(1000, updater);
 	
 	/**
 	 *Updates the display:
 	 *Checks the answer
 	 *Sets the label to a new word
 	 *Removes all text in the text field.
 	 */
 	private void update() {
 		checkAns();
 		word.setText(newWord());
 		ans.setText("");
 	}
 	
 	//Checks if the answer is correct and adds points accordingly.
 	private void checkAns() {
 		String word = getCurrentWord();
 		String word2 = null;
 		word2 = getAns();
 		if (word2.equals(word)) {
 			wordsCorrect += 1;
 			wordsTotal += 1;
 		} else {
 			wordsTotal += 1;
 		}
 	}
 
 	//Returns a new word from the words array.
 	private String newWord() {
 		return words[randomInt()];
 	}
 	
 	//Returns current word being displayed in the label.
 	private String getCurrentWord() {
 		return words[num];
 	}
 	
 	//Returns text from text field.
 	private String getAns() {
 		String userAns = null;
 		userAns = ans.getText();
 		return userAns;
 	}
 	
 	//Keeps an array of numbers from 0-269. Returns a random number and removes that number after it's been used.
 	private int randomInt() {
 		while (numbers[num] == 270) num = generator.nextInt(270);
 		numbers[num] = 270;
 		return num;
 	}
 	
 	//Displays the word and starts the text field
 	private void gameStart() {
 		timer.start();
 		
 		instructions.setVisible(false);
 		start.setVisible(false);
 		word.setVisible(true);
 		timeLeft.setVisible(true);
 		ans.setVisible(true);
 		ans.requestFocus();
 	}
 	
 	//Displays score and hides word and text field
 	private void gameDone() {
 		double accuracyPercent = ((double)wordsCorrect / (double)wordsTotal) * 100;
 		
 		score.setText("Score: " + wordsCorrect + " words per minute");
 		accuracy.setText("Accuracy: " + (int)accuracyPercent + "%");
 		
 		word.setVisible(false);
 		timeLeft.setVisible(false);
 		topGame.setVisible(false);
 		ans.setVisible(false);
 		score.setVisible(true);
 		accuracy.setVisible(true);
 		restart.setVisible(true);
 	}
 	
 	private void restart() {
 		loadRandom();
 		loadWords();
 		word.setText(newWord());
 		ans.setText("");
 		wordsCorrect = 0;
 		wordsTotal = 0;
 		score.setVisible(false);
 		accuracy.setVisible(false);
 		restart.setVisible(false);
 		word.setVisible(true);
 		ans.setVisible(true);
 		ans.requestFocus();
		count = 0;
 		timer.restart();
 	}
 	
 	//Loads words from words.txt to a string array called words.
 	private void loadWords() {
 		Scanner reader = new Scanner(getClass().getResourceAsStream("words.txt"));
 		int i = 0;
 		while (reader.hasNextLine() && i < words.length) {
 			words[i] = reader.nextLine();
 			i++;
 		}
 		reader.close();
 	}
 	
 	//Creates and populates numbers array with integers 0-270
 	private static void loadRandom() {
 		numbers = new int[271];
 		for (int i = 0; i < 271; i++) numbers[i] = i;
 	}
 	
 	//Creates and shows the GUI
 	private static void runGUI() {
 		JFrame.setDefaultLookAndFeelDecorated(true);
 		
 		@SuppressWarnings("unused")
 		Screen screen = new Screen();
 	}
 	
 	public static void main(String[] args) {
 		loadRandom();
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				runGUI();
 			}
 		});
 	}
 }
