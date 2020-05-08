 package PatternGame;
 
 import javax.swing.JOptionPane;
 import javax.swing.JFrame;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /*
  * BUGS:
  *  This program does not record randomly selected 1 second long intervals of the break
  */
 
 /*  NOTES:
  *  This program does not do the "check quality or exit" feature, as I think this is unnecessary since we visually
  *  confirm connection quality before running the program
  */
 
 /*
  * The pattern game
  * 
  * 
  * Turn on the helmet, start the program.
  * Wait 10 sec for patterns to settle down.
  * Check the Connection Quality (abort the program if not all connections are perfect with an error message, don't waste time on any UI here)
  *
  * Repeat for <p> = {A, B, C}
  *   ask user to think pattern <p>, record the data into a Matrix object for 10s. Name it file_<p>.txt
  *
  * #train SVM
  *
  * Repeat for <i> = 1...N  (N = 10)
  *   repeat pattern <p> = {A, R1, B, R2, C, R3}
  *       if <p> == R1 or R2 or R3
  *           ask the user to take a break for a couple seconds, record a randomly selected 1 seconds long interval of the break into file_<i>_<R?>.txt
  *       else
  *           ask user to think <p> for 1 sec. record data into file_<i>_<p>.txt
  *       end if
  *       #SVM_predict
  *       # record SVM predict
  *
  */
 public class PatternDriver extends JFrame {
 	
 	private static final long serialVersionUID = 1L;
 	private static String fileName = null;
 	private static int n = 10; //number of test data rounds
 	private static int trainingDuration = 10; //duration of training data in seconds
 	private static int testDuration = 1; //duration of test data in seconds
 	
 	
 	private static String firstTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
 		    					  "left side of your head.\nFocus on the ball and follow its movement.\n" +
 		    					  "You will continue this thought for 10 seconds.\n" +
 		    					  "Click OK when you are ready to begin.";
 	
 	private static String secondTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
 		    								  "right side of your head.\nFocus on the ball and follow its movement.\n" +
 		    								  "You will continue this thought for 10 seconds.\n" +
 		    								  "Start thinking about the thought before you click." +
 		    								  "Click OK when you are ready to begin.";
 	
 	private static String thirdTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is flying up to towards the " +
 		    								 "top of your head.\nFocus on the ball and follow its movement.\n" +
 		    								 "You will continue this thought for 10 seconds.\n" +
 		    								 "Click OK when you are ready to begin.";
 	
 	private static String breakText = "Good Job. Take a short break before the next pattern.\n" +
 									  "Click Ok when you are ready to continue.";
 	
 	private static String firstTestPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
 		    								 "left side of your head.\nFocus on the ball and follow its movement.\n" +
 		    								 "You will continue this thought for 1 second.\n" +
 		    								 "Start thinking about the thought before you click." +
 		    								 "Click OK when you are ready to begin.";
 	
 	private static String secondTestPattern = 
 					"Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
 				    "right side of your head.\nFocus on the ball and follow its movement.\n" +
 					"You will continue this thought for 1 second.\n" +
 					"Start thinking about the thought before you click." +
 				    "Click OK when you are ready to begin.";
 	
 	private static String thirdTestPattern = "Imagine a spinning ball inside the middle your head. This ball is flying up to towards the " +
 		    "top of your head.\nFocus on the ball and follow its movement.\n" +
 			"You will continue this thought for 10 seconds.\n" +
 		    "Click OK when you are ready to begin.";
 	
 	/*
 	 * May not be instanced
 	 */
 	private PatternDriver() {
 		
 	}
 	
 	/*
 	 * The main flow for the Pattern Game
 	 */
 	public static void main(String[] args) {
 	
 		Matrix M = null; //handle for the matrices
 		
 		//the filename is the date
 		fileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm").format(new Date());
 		
 		//start the data collecting thread
 		DataCollector dc = new DataCollector("thread1", fileName);
 		
 		//wait for data to stabilize
 		System.out.println("Waiting 10 seconds for signals to stabalize...");
 		try {
			Thread.sleep(3000); //TODO fix after testing
 		} catch (InterruptedException e) {
 			System.err.println(e.getMessage());
 			System.exit(-1);
 		}
 		
 		//Elicit pattern A
 		JOptionPane.showMessageDialog(null, firstTrainingPattern, "The first pattern", JOptionPane.PLAIN_MESSAGE);
 		dc.setMatrix(trainingDuration);
 		while(dc.writingMatrix) {
 			//wait for the matrix to be written
 		}
 		M = dc.getMatrix();
 		M.toFile(fileName, "BallRollingLeft");
 		//Let the user take a break
 		JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
 		
 		//Elicit pattern B
 		JOptionPane.showMessageDialog(null, secondTrainingPattern,  "The second pattern", JOptionPane.PLAIN_MESSAGE);
 		dc.setMatrix(trainingDuration);
 		while(dc.writingMatrix) {
 			//wait for the matrix to be written
 		}
 		M = dc.getMatrix();
 		M.toFile(fileName, "BallRollingRight");
 		
 	   //Let the user take a break
 	  	JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
 		
 		//elicit pattern C
 		JOptionPane.showMessageDialog(null, thirdTrainingPattern, "The third pattern", JOptionPane.PLAIN_MESSAGE);
 		dc.setMatrix(trainingDuration);
 		while(dc.writingMatrix) {
 			//wait for the matrix to be written
 		}
 		M = dc.getMatrix();
 		M.toFile(fileName, "BallFloatingUp");
 		
 	    //Let the user take a break
 		JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
 		
 		/*Elicit patterns n times*/
 		for(int i = 1; i <= n; i++) {
 			//Elicit pattern A
 			JOptionPane.showMessageDialog(null, firstTestPattern, "The first pattern", JOptionPane.PLAIN_MESSAGE);
 			
 			dc.setMatrix(testDuration);
 			while(dc.writingMatrix) {
 				//wait for the matrix to be written
 			}
 			M = dc.getMatrix();
 			M.toFile(fileName, "BallRollingLeft_" + i);
 		    
 			 //Let the user take a break
 		  	JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
 		
 			//Elicit pattern B
 			JOptionPane.showMessageDialog(null, secondTestPattern, "The second pattern", JOptionPane.PLAIN_MESSAGE);
 			dc.setMatrix(testDuration);
 			while(dc.writingMatrix) {
 				//wait for the matrix to be written
 			}
 			M = dc.getMatrix();
 			M.toFile(fileName, "BallRollingRight_" + i);
 			
 			//Let the user take a break
 		  	JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
 			
 			//elicit pattern C
 			JOptionPane.showMessageDialog(null, thirdTestPattern, "The third pattern", JOptionPane.PLAIN_MESSAGE);
 			dc.setMatrix(testDuration);
 			while(dc.writingMatrix) {
 				//wait for the matrix to be written
 			}
 			M = dc.getMatrix();
 			M.toFile(fileName, "BallFloatingUp_" + i);
 			
 			//Let the user take a break unless they are done
 		    if(i != n) {
 		 	    //Let the user take a break
 			  	JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
 		    } else {
 		    	JOptionPane.showMessageDialog(null, 
 						"Thats it! You're done.\n" +
 						"Sorry, you didn't win the prize.\nPlease play again",
 						"You're done!", 
 					    JOptionPane.PLAIN_MESSAGE);
 		    }
 		} //END for()
 		
 		
 		//stop the thread and wait for it to exit
 		dc.collecting = false;
 		try {
 			dc.join();
 		} catch (InterruptedException e) {
 			System.err.println(e.getMessage());
 			System.exit(-1);
 		}
 		System.out.println("Output files are prefixed with the date " + fileName);
 		System.out.println("Exiting");
 	}
 }
