 package chapter3;
 
 import java.util.Random;
 
 public class ExerciseEightSwitch {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Random rand = new Random();
 		for (int i = 1; i < 11; i++){
 			int randomInt = rand.nextInt(10);
 			switch(randomInt){
			case 0: System.out.println("this is a Zero: " + randomInt); break;
 			case 1: System.out.println("this is a One: " + randomInt); break;
 			case 2: System.out.println("this is a Two: " + randomInt); break;
 			case 3: System.out.println("this is a Three: " + randomInt); break;
 			case 4: System.out.println("this is a Four: " + randomInt); break;
 			case 5: System.out.println("this is a Five: " + randomInt); break;
 			case 6: System.out.println("this is a Six: " + randomInt); break;
 			case 7: System.out.println("this is a Seven: " + randomInt); break;
 			case 8: System.out.println("this is a Eight: " + randomInt); break;
 			case 9: System.out.println("this is a Nine: " + randomInt); break;
 			}
 		}
 
 	}
 
 }
