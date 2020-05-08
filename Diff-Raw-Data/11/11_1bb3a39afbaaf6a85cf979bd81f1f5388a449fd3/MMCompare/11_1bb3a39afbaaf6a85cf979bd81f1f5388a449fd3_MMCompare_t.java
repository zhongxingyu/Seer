 import java.util.*;
 import java.io.*;
 
 public class MMCompare {
 		public void compare( ) {
 			MMInput c = new MMInput( );
 			String g;
 			String code;
			int exact=0;
			int close=0;
 			int rounds;
 			boolean winner = false;
 			
 			while(!winner) {
 				c.setGuess( );
 				rounds = c.getRemaining( );
 				g = c.getGuess( );
 				code = c.getCode( );
 				exact = 0;
 				close = 0;
 				for(int i = 0; i <= 3; i++) {
 						char gL = g.charAt(i);
 						char cL = code.charAt(i);
 						if(gL == cL) {
 							exact++;
 							
 						}
 						for(int j = 0; j <= 3; j++) {
 							if(j != i) {
 								cL = code.charAt(j);
 								if(gL == cL) {
 									close++;
 								}
 							}
 						
 				
 
 						}
 
 
 				}				
 				System.out.println("You got " + exact + " exact.");
 				System.out.println("You got " + close + " correct letter but wrong position.");
 
 				if(exact == 4 || rounds == 0) {
 					winner = true;
 					
 
 				}
 					
 
 
 
 			}
 			if(exact == 4) {
 				System.out.println("You won!");
 			} else {
 				System.out.println("You lose!");	
 			}
 	}
 
 
 }
