 package edu.ucsb.cs56.projects.games.math_conversion_quiz;
 
 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.event.*;
 
 /**
  * GUI for binary/octal/hex conversion Quiz
  * Original program by Erick Valle & George Barrios for Mantis 0000391
  * Edited by Andrew Berls for Mantis 0000611
  * @author Daniel Ly
  * @version CS56, Spring 2013
  */
 
 public class QuizGui {
 	
 	// Window components
 	JFrame frame = new JFrame("Binary/Octal/Hex Conversion Quiz");
 	// Used for creating/inserting vertical structs to act as spacer margins
 	Box box = new Box(BoxLayout.X_AXIS);
 	
 	// Quiz-related variables
 	static QuizGui quizGui = new QuizGui();
 	private int numQuestions = 10;
 	Quiz quiz = new Quiz(numQuestions);	
 	
 	
 	// Sidebar references
 	JPanel sidebar                 = new JPanel();
 	JLabel currentQuestionNumLabel = new JLabel("Current Question: ");
 	JLabel currentQuestionNum      = new JLabel(String.format("%d/%d", current+1, quiz.getNumQuestions())); 
 	JLabel numCorrectLabel         = new JLabel("Number Correct: ");
 	JLabel numCorrect              = new JLabel(String.format("0/%d", quiz.getNumQuestions()));
 	
 	JPanel modePanel        = new JPanel();
 	JLabel practiceLabel    = new JLabel("I want to practice:");
 	JButton binaryMode      = new JButton("Binary Mode");
 	JButton octalMode       = new JButton("Octal Mode");
 	JButton decimalMode     = new JButton("Decimal Mode");
 	JButton hexadecimalMode = new JButton("Hexadecimal Mode");
 	JButton randomMode      = new JButton("Random Mode");
 	
 	
 	// Score chart references
 	ArrayList<Integer> scores = quiz.getScores();
 	JPanel scoreChart = new JPanel();
 	JLabel scoreTitle = new JLabel("High Scores:");
	JLabel score1 = new JLabel("1: " + scores.get(0) + "%");
	JLabel score2 = new JLabel("2: " + scores.get(1) + "%");
	JLabel score3 = new JLabel("3: " + scores.get(2) + "%");
	JLabel score4 = new JLabel("4: " + scores.get(3) + "%");
	JLabel score5 = new JLabel("5: " + scores.get(4) + "%");
	JLabel score6 = new JLabel("6: " + scores.get(5) + "%");
	JLabel score7 = new JLabel("7: " + scores.get(6) + "%");
	JLabel score8 = new JLabel("8: " + scores.get(7) + "%");
	JLabel score9 = new JLabel("9: " + scores.get(8) + "%");
	JLabel score10 = new JLabel("10: " + scores.get(9) + "%");
 	ArrayList<JLabel> scoreLabels = new ArrayList<JLabel>();
 	JButton resetScore = new JButton("Reset scores");
 	
 	// Content references
 	JPanel content = new JPanel();
 	JPanel userInput = new JPanel();
 	JLabel questionLabel = new JLabel("");
 	JTextField userAnswer = new JTextField(25);
 
 	JButton submit = new JButton("Submit");
 	
 	JPanel results  = new JPanel();
 	JLabel feedback = new JLabel("");
 	
 	JPanel scorePanel   = new JPanel();
 	JLabel scoreReadout = new JLabel("");
 	JButton tryAgain    = new JButton("Try Again!");
 	
 	
 	// Center panel; will contain both scorePanel and content
 	JPanel centerPanel = new JPanel();
 	
 	// Specific question references
 	private static int current;
 	private Question currentQuestion = new Question();
 	
 	/**
 	 * Build the Quiz GUI window
 	 */
 	public QuizGui build() {
 		
 		questionLabel.setPreferredSize(new Dimension(200, 20));
 		
 		int bottomMargin = 15;
 		
 		//---------------------
 		//-- Sidebar
 		//---------------------
 		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
 		
 		sidebar.add(currentQuestionNumLabel);
 		sidebar.add(currentQuestionNum);
 		
 		sidebar.add(box.createVerticalStrut(bottomMargin));
 		
 		sidebar.add(numCorrectLabel);
 		sidebar.add(numCorrect);
 		
 		modePanel.add(box.createVerticalStrut(bottomMargin));
 		
 		modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
 		
 		modePanel.add(practiceLabel);
 		
 		modePanel.add(box.createVerticalStrut(bottomMargin-5));
 
 		binaryMode.addActionListener(new binaryModeListener());
 		modePanel.add(binaryMode);
 		
 		modePanel.add(box.createVerticalStrut(bottomMargin-10));
 		
 		octalMode.addActionListener(new octalModeListener());
 		modePanel.add(octalMode);
 		
 		modePanel.add(box.createVerticalStrut(bottomMargin-10));
 		
 		decimalMode.addActionListener(new decimalModeListener());
 		modePanel.add(decimalMode);
 		
 		modePanel.add(box.createVerticalStrut(bottomMargin-10));
 		
 		hexadecimalMode.addActionListener(new hexadecimalModeListener());
 		modePanel.add(hexadecimalMode);
 		
 		modePanel.add(box.createVerticalStrut(bottomMargin-10));
 		
 		randomMode.addActionListener(new randomModeListener());
 		modePanel.add(randomMode);
 		
 		sidebar.add(modePanel);
 		
 		frame.getContentPane().add(BorderLayout.EAST, sidebar);
 		
 		//---------------------
 		//-- Score Chart
 		//---------------------
 		multiAdd(scoreLabels, score1, score2, score3, score4, score5, score6, 
 				score7, score8, score9, score10);
 		scoreChart.setLayout(new BoxLayout(scoreChart, BoxLayout.Y_AXIS));
		
 		scoreChart.add(scoreTitle);
 		scoreChart.add(score1);
 		scoreChart.add(score2);
 		scoreChart.add(score3);
 		scoreChart.add(score4);
 		scoreChart.add(score5);
 		scoreChart.add(score6);
 		scoreChart.add(score7);
 		scoreChart.add(score8);
 		scoreChart.add(score9);
 		scoreChart.add(score10);
 		resetScore.addActionListener(new resetScoreListener());
 		scoreChart.add(resetScore);
 		
 		frame.getContentPane().add(BorderLayout.WEST, scoreChart);
 		
 		//---------------------
 		//-- Main Content
 		//---------------------
 	
 		// userInput sub-pane
 		userInput.setLayout(new BoxLayout(userInput, BoxLayout.Y_AXIS));
 		userInput.add(questionLabel);
 		userInput.add(box.createVerticalStrut(5));
 		
 
 		userInput.add(userAnswer); // ----------------------------------------------
 		
 		userInput.add(box.createVerticalStrut(5));
 		
 		submit.addActionListener(new submitListener());
 		userInput.add(feedback);
 		userInput.add(box.createVerticalStrut(5));
 		userInput.add(submit);
 		
 		// Results sub-pane
 		results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
 		
 		// Score readout sub-pane (Visible at end)
 		scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
 		scorePanel.add(scoreReadout);
 		scorePanel.add(box.createVerticalStrut(10));
 		tryAgain.addActionListener(new tryAgainListener());
 		scorePanel.add(tryAgain);
 		scorePanel.setVisible(false); // Enabled at end of quiz
 		
 		// Attach sub-panes to content pane
 		content.add(userInput);
 		content.add(results);
 
 		// nests scorePanel and content together because they both use the same layout
 		centerPanel.add(scorePanel);
 		centerPanel.add(content);
 		frame.getContentPane().add(BorderLayout.CENTER, centerPanel);
 		
 		//---------------------
 		//-- Window setup
 		//---------------------
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(640, 480);
 		frame.setVisible(true);
 		
 		return this; // For chaining method calls
 	}
 	
 	/**
 	 * Helper function to add multiple JLabels to an ArrayList
 	 * @param list ArrayList where labels will be added to
 	 * @param labels JLabels to be added to list
 	 */
 	private void multiAdd(ArrayList<JLabel> list, JLabel ...labels){
 		for (JLabel label: labels){
 			list.add(label);
 		}
 	}
 	
 	/**
 	 * Updates the scoreboard
 	 */
 	private void updateScores(){
 		scores = quiz.getScores();
 		int i = 0;
 		for (JLabel score: scoreLabels){
 			score.setText((i+1) + ": " + scores.get(i) + "%");
 			i++;
 		}
 	}
 	/**
 	 * When the user clicks submit, send feedback on their answer
 	 * and update appropriate counters
 	 */
 	class submitListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {			
 			String answer = userAnswer.getText();
 			
 			if (currentQuestion.checkAnswer(answer)) {
 				feedback.setText("Correct!");
 				quiz.insertScore(true);
 			} else {
 				feedback.setText("Incorrect! Answer was: " + currentQuestion.getAnswer());
 				quiz.insertScore(false);
 			}
 			
 			String numCorrectStr = String.format("%d/%d", quiz.numCorrect(), quiz.getNumQuestions());
 			numCorrect.setText(numCorrectStr);
 			current++;
 			currentQuestionNum.setText(String.format("%d/%d", current+1, quiz.getNumQuestions()));
 			
 			currentQuestion = new Question();
 			quizGui.ask();
 		}
 	}
 	
 	/**
 	 * Try Again button - run a new Quiz on click
 	 */
 	class tryAgainListener implements ActionListener {
 		public void actionPerformed (ActionEvent e) {
 			quiz = new Quiz(numQuestions);
 			
 			// Re-enable quiz inputs		
 			numCorrect.setText((String.format("0/%d", quiz.getNumQuestions())));
 			sidebar.setVisible(true);
 			userInput.setVisible(true);
 			scorePanel.setVisible(false);
 			feedback.setText("");
 			
 			// Restart the quiz
 			current = 0;
 			currentQuestionNum.setText(String.format("%d/%d", current+1, quiz.getNumQuestions()));
 			quizGui.ask();
 		}
 	}
 	
 	/**
 	 * Set the quiz to practice converting only to binary
 	 */
 	class binaryModeListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			feedback.setText("");
 			currentQuestion = new Question(currentQuestion.getRandomNum(), 2);
 			quiz.setMode(2);			
 			quizGui.ask();
 		}
 	}
 	
 	/**
 	 * Set the quiz to practice converting only to octal
 	 */
 	class octalModeListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		  quiz.setMode(8);			
 			feedback.setText("");
 			currentQuestion = new Question(currentQuestion.getRandomNum(), 8);
 			quizGui.ask();
 		}
 	}
 	
 	/**
 	 * Set the quiz to practice converting only to decimal
 	 */
 	class decimalModeListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		  quiz.setMode(10);
 			feedback.setText("");
 			currentQuestion = new Question(currentQuestion.getRandomNum(), 10);				
 			quizGui.ask();
 		}
 	}
 	
 	/**
 	 * Set the quiz to practice converting only to hexadecimal
 	 */
 	class hexadecimalModeListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
   		quiz.setMode(16);
 			feedback.setText("");
 			currentQuestion = new Question(currentQuestion.getRandomNum(), 16);					
 			quizGui.ask();	
 		}
 	}
 	
 	/**
 	 * Set the quiz back to random conversions
 	 */
 	class randomModeListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
   		quiz.setMode(-1);			
 			currentQuestion = new Question();				
 			quizGui.ask();
 		}
 	}
 	
 	/**
 	 * Resets then updates the scoreboard
 	 */
 	class resetScoreListener implements ActionListener {
 		public void actionPerformed (ActionEvent e) {
 			quiz.resetScores();
 			updateScores();
 		}
 	}
 	
 	/**
 	 * Update fields to ask the current question, and end the quiz if necessary
 	 */
 	public void ask() {
 	  userAnswer.setText(""); // Clear the text field
 		
 		if (current >= quiz.getNumQuestions()) {
 			// If we're through with the questions, hide the inputs and show the final readout			
 			sidebar.setVisible(false);
 			userInput.setVisible(false);
 			scoreReadout.setText(quiz.getReadout());
 			scorePanel.setVisible(true);
 			// Update the scoreboard
 			quiz.writeScore(quiz.getPercentage());
 			updateScores();
 		} else {
 			// Else ask the current question			
 			String prompt = currentQuestion.generatePrompt(quiz.getMode());
 			questionLabel.setText(prompt);
 		}
 	}
 	
 	/**
 	 * Build and run the GUI
 	 */
 	public static void main (String [] args) {		
 		quizGui.build().ask();
   }
 	
 }
