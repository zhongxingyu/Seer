 package se.chalmers.dat255.sleepfighter.challenges;
 
 import java.util.Random;
 
import se.chalmers.dat255.sleepfighter.R;
import se.chalmers.dat255.sleepfighter.activities.ChallengeActivity;

public class SimpleMathChallenge implements Challenge{
 
 	private Random random = new Random();
 	private int operand1 = 0;
 	private int operand2 = 0;
 	private int operation = 0;
 	private int result = 0;
 
 	public SimpleMathChallenge(){
 		runChallenge();
 	}
 	
 	public void runChallenge() {
 		nextInts();
 		nextOp();
 		result();
 	}
 
 	private void nextInts() {
 		operand1 = random.nextInt(10) + 1;
 		operand2 = random.nextInt(10) + 1;
 	}
 
 	private void nextOp() {
 		operation = random.nextInt(4);
 	}
 
 	private void result() {
 		if (operation == 0) {
 			result = operand1 + operand2;
 		} else if (operation == 1) {
 			result = operand1 - operand2;
 		} else if (operation == 2) {
 			result = operand1 * operand2;
 		} else if (operation == 3) {
 			result = operand1 / operand2;
 		}
 	}
 
 	public int getResult() {
 		return result;
 	}
 
 	public String getCalculation() {
 		if (operation == 0) {
 			return operand1 + " + " + operand2;
 		} else if (operation == 1) {
 			return operand1 + " - " + operand2;
 		} else if (operation == 2) {
 			return operand1 + " * " + operand2;
 		} else {
 			return operand1 + " / " + operand2;
 		}
 	}
 
	@Override
	public void start(final ChallengeActivity activity) {
		activity.setContentView(R.layout.alarm_challenge_test);
		runChallenge();
	}

 }
