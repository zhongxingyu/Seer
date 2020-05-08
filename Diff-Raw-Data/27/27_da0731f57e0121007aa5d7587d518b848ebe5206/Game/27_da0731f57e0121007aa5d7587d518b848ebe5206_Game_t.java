 import java.util.*;
 import java.io.Console;
 public class Game{
 	public static void main(String args[]){
                 Code CorrectCode = new Code();  //instantiat code data structure for codemaker code
                 String stringcode = new String(); //temporary string to be set into code object
                 int alphsize;				//size of the alphabet
                 Scanner in = new Scanner(System.in);
 
 
 		//gets the information from the codemaker
                 System.out.println("Enter the size of the alphabet: ");
                 alphsize = in.nextInt();
                 while(!CorrectCode.setAlf(alphsize)){
                     try {alphsize = in.nextInt();}
                     
                     catch(Exception e){
                    	in.nextLine();
                     	continue;
                     }
                     
                 }
 
                 System.out.println("Enter the correct code: ");
 				Console console = System.console();
 				stringcode = new String(console.readPassword());
                 //stringcode = in.next();
                 while(!CorrectCode.setCode(stringcode)){
 					stringcode = new String(console.readPassword());
                 }
 
                 System.out.println("Enter the number of rounds: ");
                 int numRounds = 0;
                 while(numRounds <= 0)
                 {
                 	try{numRounds = in.nextInt();}
                 	
                 	catch(Exception q){
                		in.nextLine();
                 		continue;
                 		
                 	}
                 }
 
                 ActionList buttoncheck = new ActionList();
 		
 		//uses the information enetered by the codemaker to create the game gui
                 MasterGUI gameBoard = new MasterGUI(numRounds, stringcode, buttoncheck);
 
 		boolean won = false;
                 int numRight;
                 int numIn;
 		int onRound=0;
 		Code GuessCode = new Code();
 		System.out.println("Start guessing!");
 		
 		//The game Looop
 		while(won==false && onRound<numRounds)
 		{
 
                         if(buttoncheck.buttonpushed){
 
                             stringcode = gameBoard.getGuess();
                             
                             //only accept the guess if it is valid
                             if(GuessCode.setCode(stringcode)){		
                                  stringcode = gameBoard.getGuess();
                             	won=CorrectCode.won(GuessCode);
                             	numRight = CorrectCode.corrects(GuessCode);
                             	numIn = CorrectCode.ins(GuessCode);
                            		gameBoard.submitFeedback(numRight, numIn);
                             	onRound++;
 							}
                             buttoncheck.buttonback();
                         }
 
 
 		}
 		//games states for game over
 		if(won==true)
 		{
 			System.out.println("Codebreaker wins!");
 			gameBoard.gameOver();
 		} else {
 			System.out.println("Codemaker wins!");
 			gameBoard.gameOver();
 		}
 	}
 
 
 }

