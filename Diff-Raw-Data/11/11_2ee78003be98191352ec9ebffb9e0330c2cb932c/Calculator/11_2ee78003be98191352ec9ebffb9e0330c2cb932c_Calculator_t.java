 /**
  * @author - Mira Hall
  * @author - Daniel Goldstien
  */
 package edu.grinnell.csc207.goldstei1.utils;
 
 import java.math.BigInteger;
 
 public class Calculator {
     
     
 	/**
 	 * Method to solve a mathematical expression given as a string
 	 *  with spaces between each number and operation. The numbers
 	 *  in the string can be larger than a normal integer.
 	 *  Preconditions: a number following a '^' cannot be larger than Integer.MAX_VALUE
 	 */
 	public static BigInteger eval0(String expression) {
 
 		StringBuffer evaluator = new StringBuffer(expression);
 		BigInteger endValue = new BigInteger("0"); //first number in string is added later
 		char nextOperation = '+'; // first operation is always add (add first number to 0)
 		int begOfNum;
 		int i = 0; //starting index of the evaluator string
 
 
 		while (i < evaluator.length()) {
 			//Check for which operation is going to be performed.
 			// Skipped first time through the loop
 			switch (evaluator.charAt(i)) {
 			case '+': nextOperation = '+';
 			i += 2;
 			break;
 			case '-': nextOperation = '-';
 			i += 2;
 			break;
 			case '*': nextOperation = '*';
 			i += 2;
 			break;
 			case '/': nextOperation = '/';
 			i += 2;
 			break;
 			case '^': nextOperation = '^';
 			i += 2;
 			break;
 			}//switch
 			
 			//find all digits of the next number
 			
 			begOfNum = i; //index of the beginning of the next number
 
 			while(i < evaluator.length() && evaluator.charAt(i) != ' ') { //find any more digits of the next number
 				i++;
 			}//while
 			//end of next number is now i
 			
 			//perform nextOperation on endValue and the number represented by the 
 			// substring between begNum and i
 			switch (nextOperation) {
 			case '+': endValue = endValue.add(new BigInteger(evaluator.substring(begOfNum, i).toString()));
 			break;
 			case '-': endValue = endValue.subtract(new BigInteger(evaluator.substring(begOfNum, i).toString()));
 			break;
 			case '*': endValue = endValue.multiply(new BigInteger(evaluator.substring(begOfNum, i).toString()));
 			break;
 			case '/': endValue = endValue.divide(new BigInteger(evaluator.substring(begOfNum, i).toString()));
 			break;
 			case '^': endValue = endValue.pow(new Integer(evaluator.substring(begOfNum, i).toString()));
 			break;
 			}//switch
 
 			i++;
 		}//While
 
 		return endValue;
 	}//eval0
 	
 	
 	/**
 	 * Method that takes an int amount and returns an array of ints that
 	 *  specify the amount provided by each coin in the combination of coins that 
 	 *  uses the lowest number of coins total. This method can easily be 
 	 *  generalized by simply changing the values of the coins or adding 
 	 *  parameters that take the value of each coin. 
 	 *  Precondition: amount cannot equal 1, 3, or 5.
	 *  Postcondition: if there is no way to make amount out of the specified,
	 *  returns a null array
 	 */
 	public static int[] fewestCoins(int amount) {
 		
 		int wot = 2;
 		int eater = 7;
 		int stickpair = 11;
 		int deck = 54;
 		int testNum = 0;
 		int currentMin = -1;
 		int maxWot = amount/wot;
 		int maxEater = amount/eater;
 		int maxStickpair = amount/stickpair;
 		int maxDeck = amount/deck;
 		int[] coinCount = new int[4];
 		int currentCount;
 		
 		//loop through all the permutations and if their sum = amount
 		// then check if the number of coins used is less than previous 
 		// possibilities
 		
 		for(int i = 0; i <= maxWot; i++) {
 			testNum += wot*i;
 			for(int j = 0; j <= maxEater; j++) {
 				testNum += eater*j;
 				for(int k = 0; k <= maxStickpair; k++) {
 					testNum += stickpair*k;
 					for(int p = 0; p <= maxDeck; p++) {
 						testNum += deck*p;
 						if(testNum == amount) {
 							currentCount = i +j + k + p;
 							if(currentCount < currentMin || currentMin == -1) {
 								//set coinCount to current coin values
 								coinCount[0] = wot*i;
 								coinCount[1] = eater*j;
 								coinCount[2] = stickpair*k;
 								coinCount[3] = deck*p;
 								currentMin = currentCount;
 							}//if(i+j+k+p....)
 						}//if(test=amount)
 						testNum -= deck*p;		
 					}//for(p)
 					testNum -= stickpair*k;
 				}//for(k)
 				testNum -= eater*j;
 			}//for(j)
 			testNum -= wot*i;
 		}//for(i)
 		return coinCount;	
 	}
 }
