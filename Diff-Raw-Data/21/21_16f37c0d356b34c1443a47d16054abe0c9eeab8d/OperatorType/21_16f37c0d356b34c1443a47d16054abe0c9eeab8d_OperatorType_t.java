 package Countdown;
 import java.util.Random;
 
 /**
  * 
  */
 
 /**
  * @author me92
  * 
  */
 public enum OperatorType {
	Plus, Minus, Times, Divide;
 
 	private static final Random rng = new Random();
 
 	@Override
 	public String toString() {
 		switch (this) {
 		case Plus:
 			return " + ";
 		case Minus:
 			return " - ";
 		case Times:
 			return " * ";
		case Divide:
 			return " / ";
 		}
 		return null;
 	}
 
 	static public OperatorType randomOpp() {
 		int rand = rng.nextInt(4);
 		return intToType(rand);
 	}
 	
 	static public OperatorType intToType(int type) {
 		switch (type) {
 		case 0:
 			return Plus;
 		case 1:
 			return Minus;
 		case 2:
 			return Times;
 		case 3:
			return Divide;
 		default:
 			return Plus;
 		}
 	}
 }
