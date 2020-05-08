 import java.util.*;
 import java.io.Console;
 public class Game{
 	public static void main(String args[]){
                 Code CorrectCode = new Code();
                 String stringcode = new String();
                 int alphsize;
                 Scanner in = new Scanner(System.in);
 
                 System.out.println("Enter the size of the alphabet: ");
                 alphsize = in.nextInt();
                 while(!CorrectCode.setAlf(alphsize)){
                     alphsize = in.nextInt();
                 }
 
                 System.out.println("Enter the correct code: ");
 				Console console = System.console();
				stringcode = console.readPassword();
                 //stringcode = in.next();
                 while(!CorrectCode.setCode(stringcode)){
					stringcode = console.readPassword();
                 }
 
                System.out.println("Enter the number of round: ");
                 int numRounds = in.nextInt();
 
                 ActionList buttoncheck = new ActionList();
 
                 MasterGUI gameBoard = new MasterGUI(numRounds, stringcode, buttoncheck);
 
 		boolean won = false;
                 int numRight;
                 int numIn;
 		int onRound=0;
 		Code GuessCode = new Code();
 		System.out.println("Start guessing!");
 		while(won==false && onRound<numRounds)
 		{
 
                         if(buttoncheck.buttonpushed){
 
                             stringcode = gameBoard.getGuess();
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
 /*
 scrap all that
 game begins
 Board calls "getAlfSize" on code
 Board calls "getRounds" and stores the int in "numRounds"
 Board creates a code object and names it "CorrectCode"
 Board calls "inputCodeSecret" on "CorrectCode", giving it a value
 	function "inputCodeSecret" has input validation (assume validation function)
 Board sets boolean "won" to false
 Board sets "onRound" to zero
 while(won==false && onRound < numRounds)
 	accept input and put it in "GuessCode"
 	call "compareCode" on "CorrectCode" with "GuessCode" as argument
 		"compareCode" prints string of form "x correct y in puzzle"
 		"compareCode" returns a boolean which is true if "GuessCode" is the same as "CorrectCode"
 	make "won" equal to output of "compareCode"
 	increment "onRound"
 if(won==true)
 	print "Codebreaker wins!"
 else
 	print "Codemaker wins!"
 */
