 package edu.ucsb.cs56.projects.games.math_conversion_quiz;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Scanner;
 
 
 /**
  * A command line interface for a binary/octal/hex conversion Quiz.
  * Original program by Erick Valle & George Barrios for Mantis 0000391
  * Edited by Andrew Berls for Mantis 0000611
  * @author Daniel Ly
  * @version CS56, Spring 2013
  */
 
 public class Quiz {
 	
 	private ArrayList<Question> questions = new ArrayList<Question> ();
     
     private ArrayList<Boolean> scores = new ArrayList<Boolean>();
     
 	private String filePath = "scores.txt"; // File path to the text file containing the scores
 	private ArrayList<Integer> savedScores; // Holds scores loaded from the save file
 	private ScoreLoader sl = new ScoreLoader(filePath);
 
     /*
      * mode is a magic number to represent the current quiz radix.
      * For example, you can practice conversions to binary by setting it to 2
      * The same is true of radixes 2, 8, 10, and 16
      * -1 (default) is random mode: converts from any radix to any radix
      */
     private int mode;
     
     /**
      * Constructor initializes a fixed number of random questions
      * @param numQuestions The number of questions to initialize
      */
     public Quiz(int numQuestions) {
     	this.mode = -1; // Random mode by default
     	
 		for (int i=0; i<numQuestions; i++) {
 			// Generate a random Question and push it to the beginning of the list
 			Question q = new Question();
 			this.questions.add(0, q);
 		}
 	}
     
     public int getMode() {
     	return this.mode;
     }
     
     /**
      * Set the mode to a specific radix, e.g., 2, 8, 10, 16
      * in order to practice conversions to that radix
      */
     public void setMode(int mode) {
     	this.mode = mode;
     }
     
     public int getNumQuestions() {    	
     	return this.questions.size();
     }
     
     public Question getQuestion(int idx) {
     	return this.questions.get(idx);
     }
     
     /**
      * Insert a score into the list of user answers
      * @param correct A boolean indicating if the answer is correct
      */
     public void insertScore(boolean correct) {
     	this.scores.add(0, correct);
     }
     
     /**
      * Get the number of correct answers from the scores array
      * True indicates correct, false for incorrect
      * @return The number of correct answers
      */
     public int numCorrect() {
     	int result = 0;
     	for (boolean correct : this.scores) {
     		if (correct) {
     			result++;
     		}
     	}
     	return result;
     }
     
     /**
      * Get the user's percentage of correct answers
      * @return The percentage of correct answers as an integer
      */
     public int getPercentage() {
     	// numCorrect returns an integer, but must be coerced to a double
     	// so that (numCorrect/size) works correctly
     	
     	double numCorrect = (double) this.numCorrect();
     	int size = this.scores.size();
     	double result = Math.round((numCorrect / size)*100);
     	return (int) result;
     }
     
     /**
      * A readout showing the score percentage, e.g., "Your score was 67%!"
      * @return A string readout
      */
     public String getReadout() {
     	return "Your score was " + this.getPercentage() + "%!";
     }  
     
     /**
      * Saves multiple scores into the save file
      * @param scores Scores to be saved
      */
 	public void writeScore(int ...scores) {
 		try {
 			ArrayList<Integer> scoreList = sl.loadScores();
 			for (int score: scores)	scoreList.add(score);
 			sl.saveScore(scoreList);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
     /**
     * Resets the scores in the save file
      */
 	public void resetScores() {
 		try {
 			sl.resetScores();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Loads the scores from the save file into an ArrayList
 	 * @return List of scores in the save file
 	 */
 	public ArrayList<Integer> getScores(){
 		ArrayList<Integer> scores = new ArrayList<Integer>();
 		try {
 			scores = sl.loadScores();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Collections.sort(scores); // sorts scores from least to greatest
 		Collections.reverse(scores); // reverses it to greatest to least
 		return scores;
 	}
 	
     /**
      * Run the Quiz game on the command line
      * For each question, display its prompt and track the user's answer
      * At the end, display the percentage of correct answers.
      */
     public void run() {
     	Scanner scanner = new Scanner(System.in);
     	for (Question q : this.questions) {
     		System.out.println(q.generatePrompt());
     		String userAnswer = scanner.next();
     		
     		if (q.checkAnswer(userAnswer)) {
     			System.out.println("Correct!");
     			this.insertScore(true);
     		} else {    			
     			System.out.println("Incorrect! Actual answer was: " + q.convertTo(q.getIntRadix()));
     			this.insertScore(false);
     		}
     	}
     	writeScore(this.getPercentage());
     	System.out.println("Your score was " + this.getPercentage() + "%!");
     }
 	
     /**
      * main prompts the user for a number of questions and runs a new Quiz
      * @param args Unused
      */
     public static void main (String [] args) {
 		Scanner scanner = new Scanner(System.in);
 		System.out.println("Enter number of questions:");
 	    String s = scanner.next();
 		int numQuestions = Integer.parseInt(s);
 		Quiz quiz = new Quiz(numQuestions);
 		quiz.run();		
     }
 }
