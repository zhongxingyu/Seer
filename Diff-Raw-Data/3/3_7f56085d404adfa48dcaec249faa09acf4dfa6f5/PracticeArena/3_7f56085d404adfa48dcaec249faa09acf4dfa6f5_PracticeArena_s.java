 package iago.learning;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Random;
 
 import iago.Board;
 import iago.DebugFunctions;
 import iago.Move;
 import iago.players.AlphaBetaPlayer;
 import iago.players.GreedyPlayer;
 import iago.players.MetaPlayer;
 import iago.players.NegamaxPlayer;
 import iago.players.Player.PlayerType;
 
 public class PracticeArena{
 	static final int BLOCKED_COUNT=4; //TODO: move this
 	static final int LEARNING_ITERATIONS=1000000;//ONE MILLION
 	static final int RUNNING_WIN_LOSS_SIZE=50; //We get the win loss over the past RUNNING_WIN_LOSS_SIZE games
 	static final int LOG_SAVE_COUNT = 100; //Saves the file every LOG_SAVE_COUNT games
 	static final String LOG_DIRECTORY = "LearningLogs";
 	
 		
 	public static void main(String[] args)
 	{
 		LinkedList<Double> runningWinLoss = new LinkedList<Double>();
 
 		
 		AlphaBetaPlayer blackOpponent = new AlphaBetaPlayer(PlayerType.BLACK,2);
 		AlphaBetaPlayer whiteOpponent = new AlphaBetaPlayer(PlayerType.WHITE,2);
 		//This is the learning player. They could both learn, but it's easy to reference them this way
 		MetaPlayer whiteLearner = new MetaPlayer(PlayerType.WHITE,2); 
 		MetaPlayer blackLearner = new MetaPlayer(PlayerType.BLACK,2); 
 		
 		//Start our history file
 		try{
 			/**<META CODE>**/
 			//Make a directory for the logs if it doesn't exist
 			try{new File(LOG_DIRECTORY).mkdir();}
 			catch (Exception e){//Catch exception if any
 				System.err.println("Error: " + e.getMessage());
 			}
 		    // Create file 
 		    FileWriter smallStream = new FileWriter(LOG_DIRECTORY+"/LearningHistory_1-"+(LOG_SAVE_COUNT+1)+".csv");
 		    FileWriter longStream = new FileWriter(LOG_DIRECTORY+"/LearningHistory.csv");
 		    BufferedWriter partialWinLossLog = new BufferedWriter(smallStream);
 		    BufferedWriter allWinLossLog = new BufferedWriter(longStream);
 		    partialWinLossLog.write("Iteration,Win/Loss\n");
 		    allWinLossLog.write("Iteration,Win/Loss\n");
 		    /**</META CODE>**/
 		    
 			for(int a = 0; a < LEARNING_ITERATIONS; a++){
 				double feedback = 0;
 				System.out.println("=====================");
 				System.out.println("Learning iteration "+(a+1));
 				System.out.println("=====================");
 	
 				int side = 0;
 				MetaPlayer learner;
 				AlphaBetaPlayer opponent;
 				String initialBoardRepresentation = generateRandomBoard();
 				//We want the player to play from both sides
 				for(side = 0; side <= 1; side++) //side==0 means Learner is playing white
 				{
 					System.out.println("Playing game "+(a+1)+"/"+LEARNING_ITERATIONS+((side==0)?"":" reversed"));
 					
 					//Set up a game
 					Board board = new Board(initialBoardRepresentation);
 					boolean learnerTurn;
 					if(side==0){
 						learner = whiteLearner;
 						opponent = blackOpponent;
 						learnerTurn = true;
 					}else{
 						learner = blackLearner;
 						opponent = whiteOpponent;
 						learnerTurn = false;
 					}
 					Move nextMove = new Move(0,0);
 					
 					//Start playing the game
 					int consecutivePasses = 0;
 					while(consecutivePasses < 2)
 					{		
 						if(!learnerTurn)
 						{
 							nextMove = opponent.chooseMove(board);
 							//apply the move
 							board.apply(nextMove, (side==0)?PlayerType.BLACK:PlayerType.WHITE, true);
 	
 						}else{
 							nextMove = learner.chooseMove(board);
 							//apply the move
 							board.apply(nextMove, (side==0)?PlayerType.WHITE:PlayerType.BLACK, true);
 						}
 						learnerTurn = !learnerTurn;
 						
 						//For figuring out if the game is over
 						if(nextMove.equals(new Move(-1,-1))){
 							consecutivePasses++;
 						}else{
 							consecutivePasses = 0;
 						}
 					}
 					int boardScore = board.scoreBoard(PlayerType.WHITE);
 					
 					boolean whiteWins = boardScore > 0;
 					boolean tie = boardScore == 0;
 					double thisGameFeedback = 0.0;
 					
 					
 					//Allocate points
 					if(whiteWins && side==0 && !tie){ //Learner won while white
 						thisGameFeedback = 1.0 / 2; // div 2 since we're going to play from both sides
 					}
 					if(!whiteWins && side==1 && !tie){ //Learner won while black
 						thisGameFeedback = 1.0 / 2; 
 					}
 					if(tie){
 						thisGameFeedback = 0.5 / 2;
 					}
 					feedback += thisGameFeedback;
 					System.out.println("Feedback: "+thisGameFeedback);
 				}
 			
 				//Improve our feature weights (maybe)
 				whiteLearner.receiveFeedback(feedback);
 				//Both players can learn, and we can check out the weights for playing from both sides separately
 				blackLearner.receiveFeedback(feedback);
 				System.out.println("White: "+whiteLearner.getFeatureSet());
 				System.out.println("Black: "+blackLearner.getFeatureSet());
 				
 				
 				/**<META CODE>**/
 				//Update the win/loss log
 				runningWinLoss.add(feedback);
 				if(runningWinLoss.size() > RUNNING_WIN_LOSS_SIZE){
 					runningWinLoss.remove();
 				}
 				Double avgFeedback = 0.0;
 				for(Double result : runningWinLoss){
 					avgFeedback += result;
 				}
				avgFeedback /=  RUNNING_WIN_LOSS_SIZE;
 				partialWinLossLog.write(a+","+avgFeedback.toString()+"\n");
 				allWinLossLog.write(a+","+avgFeedback.toString()+"\n");
 				if(a % LOG_SAVE_COUNT == 0){
 					partialWinLossLog.close();
 					smallStream = new FileWriter(LOG_DIRECTORY+"/LearningHistory_"+(a+1)+"-"+(a+1+LOG_SAVE_COUNT)+".csv");
 					partialWinLossLog = new BufferedWriter(smallStream);
 				}
 			    /**</META CODE>**/
 				
 			}
 			
 		System.out.println("Done");
 		
 	    //Close the output stream
 		partialWinLossLog.close();
 		allWinLossLog.close();
 	    }catch (Exception e){//Catch exception if any
 	    	  System.err.println("Error: " + e.getMessage());
 	    }
 	}
 
 	
 	private static String generateRandomBoard()
 	{
 		char[][] randomBoardRepresentation = DebugFunctions.makeSolidBoardCharArray('.');
 
 		
 		//Block positions
 		HashSet<int[]> possibleBlockedPositions = new HashSet<int[]>();
 		HashSet<int[]> impossibleBlockedPositions = new HashSet<int[]>();
 		impossibleBlockedPositions.add(new int[]{4,4});
 		impossibleBlockedPositions.add(new int[]{4,5});
 		impossibleBlockedPositions.add(new int[]{5,4});
 		impossibleBlockedPositions.add(new int[]{5,5});
 
 		for(int x = 0; x < Board.BOARD_SIZE; x++){
 			for(int y = 0; y < Board.BOARD_SIZE; y++){
 				//Make a list of positions we might block
 				int[] thisPosition = new int[]{x,y};
 				if(!impossibleBlockedPositions.contains(thisPosition))
 				{
 					possibleBlockedPositions.add(thisPosition);
 				}
 			}
 		}
 		for(int i = 0; i < BLOCKED_COUNT;i++)
 		{
 			Random random = new Random();
 			int blockIndex = random.nextInt(possibleBlockedPositions.size());
 			int[] positionToBlock = (int[]) possibleBlockedPositions.toArray()[blockIndex];
 			possibleBlockedPositions.remove(positionToBlock); //We don't want to choose this one twice
 			randomBoardRepresentation[positionToBlock[1]][positionToBlock[0]] = '*';
 		}
 		//Add the starting positions
 		randomBoardRepresentation[4][4] = 'w';
 		randomBoardRepresentation[5][5] = 'w';
 		randomBoardRepresentation[4][5] = 'b';
 		randomBoardRepresentation[5][4] = 'b';
 		
 		
 		return (DebugFunctions.charArrayToBoardString(randomBoardRepresentation));
 	}
 
 }
