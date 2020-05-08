 /*
  * Program: Tic Tac Toe Game Helper
  * Author: Sharon Davis
  * Date: 5/08/2013
  * Description: CS161 Lab 6
  * 		This helper class has methods to complete game play.
  * Inputs: User chooses a square.
  * Outputs: Prompts for user to choose a square, error messages if user input is invalid and
  * 		uses GUI for board display, update of board and play. 
  */
 
 
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 
 
 public class TicTacToeHelper {
 	
 	//global variables
 	public static int square = 0;
 	public static String winner = "0";
 	public static int playerSymbolCount = 1;
 	public static JFrame frame = new JFrame();
 	private static int[] boardChoice = new int[9];
 	final static int DUP_NOT_FOUND = -1;
 	final static int TWO = 2;
 	final static int FOUR = 4;
 	final static int SIX = 6;
 	final static int EIGHT = 8;
 	
 	/*
 	 * Method: playGame()
 	 * Description: used to play game until end
 	 * Input: none
 	 * Outputs: prompts for user to play
 	 */
 	
 	public static void playGame(){
 		
 		//local variables
		Boolean playerOne = false;
 		final int GAME_OVER = 9;
 
 		String player = null;
 		TicTacToeBoard boardInfo = new TicTacToeBoard();
 		
 		//initialize boardChoice[] array one time
 		for(int k = 0; k < 9; k++){
 			boardChoice[k] = 0;
 		}
 		
 		System.out.println(boardInfo.getTitle());
 		
 		System.out.println("Player 1 is 'X' and Player 2 is 'O'.\n\nPlayer 1 Starts:");
 		
		for(int i = 0; i < 9; i++){							//loop to start play
			
			//flag used to determine winning player
			if(playerOne == false){					
				playerOne = true;
			}
			else{
				playerOne = false;
				
			}
			
 			System.out.println("Please Choose a Square (1 through 9): ");	
 			
 			checkUserInput();							//method to check for valid user input
 			
 			
 				
 			if(square >= 1 && square <= 9){				//protects boardChoice array from out of bounds
 				
 				boardChoice[square - 1] = square;				//used for duplicate user input
 			
 				player = playerSymbol();				//method to get which symbol to put on board
 				
 				TicTacToeComponent.setUserChoice(player, square);	//method to set user symbol
 				
 				winner = boardInfo.currentBoard(player, square);	//method to check for win
 				
 				updateBoard();										//method to update board
 	
 				if(!winner.equals("0")){						//if there is a winner
 					System.out.println(winner);
 					
 					//determines which player wins
					if(playerOne == true ){
 						System.out.println("Player 1 Wins!");
 					}
 					else{
 						System.out.println("Player 2 Wins!");
 					}
 					
 					i = GAME_OVER;					//breaks out of loop when there is a winner
 				}
 			}	
 		}
 	}	
 	
 	/*
 	 * Method: checkUserInput()
 	 * Description: used to check for valid user input
 	 * Input: none
 	 * Outputs: Error messages for user
 	 */
 	
 	public static void checkUserInput(){
 		
 		Scanner input = new Scanner(System.in);
 		
 		while(!input.hasNextInt()){						//check input for int, enter if non int input
 			input.next();
 			System.out.println("Please Enter a Valid Square Choice (1 to 9): ");
 			input.nextLine();
 		}
 	
 		square = input.nextInt();
 		
 		while(square <= 0 || square > 9){			//enter if out of bounds int
 			try{										//used to catch user user input runtime error	
 				System.out.println("Please Choose a Square (1 through 9): ");
 				input.nextLine();
 				square = input.nextInt();
 			}
 			catch(RuntimeException exception){				//runtime error output
 				
 				System.out.println("Invalid Input! ");
 			}
 			
 		}
 		
 		if(square >= 1 && square <= 9){			//enter id correct input
 			
 			int duplicate = DUP_NOT_FOUND;
 			
 			duplicate = searchForDuplicate(boardChoice, square);		//find if input is a duplicate
 			while(duplicate != DUP_NOT_FOUND){		//checks for duplicate user input
 				try{								//used to catch user user input runtime error
 					System.out.println("That Square has Already been Taken.\nPlease Choose Another Square.");
 					input.nextLine();
 					square = input.nextInt();
 					duplicate = searchForDuplicate(boardChoice, square);
 				}
 				catch(RuntimeException exception){				//runtime error output
 					
 					System.out.println("Invalid Input! ");
 				}
 			}
 		}			
 	}
 	
 	/*
 	 * Method: updateBoard()
 	 * Description: used to draw board and put symbols in proper square locations on board
 	 * Input: none
 	 * Outputs: none
 	 */
 	
 	public static void updateBoard(){
 	
 		frame.setSize(300, 500);					//size of GUI frame
 		frame.setTitle("Tic Tac Toe");				//title of GUI frame
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		TicTacToeComponent component = new TicTacToeComponent();		
 		frame.add(component);					//draw board
 		
 		frame.setAlwaysOnTop(true);				//set board alway on top
 		frame.setVisible(true);					//make frame visible
 	}
 	
 	/*
 	 * Method: playerSymbol()
 	 * Description: used to determine which symbol to pass to board
 	 * Input: none
 	 * Outputs: @returns symbol
 	 */
 	
 	public static String playerSymbol(){
 		
 		//local variable
 		String symbol = null;
 			
 		if(playerSymbolCount == TWO || playerSymbolCount == FOUR || playerSymbolCount == SIX || playerSymbolCount == EIGHT){
 			symbol = "O";					//sets player symbol 'O'
 		}
 		else{
 			symbol = "X";					//sets player symbol 'X'
 		}
 		
 		return symbol;
 	}
 	
 	/*
 	 * Method: searchForDuplicate()
 	 * Description: used to check for duplicate user square choice
 	 * Input: @param int[] boardChoice
 	 * 		  @param int square
 	 * Outputs: @returns index where duplicate is found
 	 * 			@returns -1 if no duplicate is found
 	 */
 	
 	public static int searchForDuplicate(int [] duplicateNumber , int key){
 		
 		for (int i = 0; i < duplicateNumber.length; i++)
 	      {
 	           if ( duplicateNumber[i] == key )				
 	                 return i;  
 	      }
 	     return DUP_NOT_FOUND;
 	}
 }	
