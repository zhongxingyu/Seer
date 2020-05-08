package goplayer;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Random;
 
 public class GoPlayer {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		GoPlayer myPlayer = new GoPlayer(9);
 		myPlayer.play();
 
 	}
 	
 	public GoPlayer()
 	{
 		myCurrentGame = new GoGame(9);
 		myCurrentGameTreeNode = new GoGameTreeNode(myCurrentGame);
 	}
 	
 	public GoPlayer(int boardSize)
 	{
 		myCurrentGame = new GoGame(boardSize);
 		myCurrentGameTreeNode = new GoGameTreeNode(myCurrentGame);
 	}
 
 	GoGame myCurrentGame;
 
 	GoGameTreeNode myCurrentGameTreeNode ;
 
 	public void play() {
 
 		// 1. Create an InputStreamReader using the standard input stream.
 		InputStreamReader isr = new InputStreamReader(System.in);
 
 		// 2. Create a BufferedReader using the InputStreamReader created.
 		BufferedReader stdin = new BufferedReader(isr);
 
 		// 3. Don't forget to prompt the user
 		System.out.println("Play? 0 no, 1 for play as black, 2 for"
 				+ " play as white");
 
 		String input = "";
 		// 4. Use the BufferedReader to read a line of text from the user.
 		// try {
 		// input = stdin.readLine();
 		// } catch (Exception e) {
 		// System.out.println("Failed reading player input ");
 		// return;
 		// }
 		// int in = Integer.decode(input);
 		int in = 0;
 		boolean playAsBlack = (in == 1);
 		boolean playAsWhite = (in == 2);
 
 		if (in == 3) {
 			Random blah = new Random();
 			playAsBlack = blah.nextBoolean();
 			playAsWhite = !playAsBlack;
 
 		}
 		boolean play = playAsBlack || playAsWhite;
 
 		System.out.println("black " + playAsBlack + " white " + playAsWhite);
 
 		int totalGames = 0;
 
 		while (true) {
 
 			System.out.println("CURRENT GAME");
 			myCurrentGame.printBoard();
 			if (!play) {
 				GoMove bestMove;
 				System.out.println(totalGames + " totalGames");
 				if (myCurrentGame.isBsTurn) {
 					System.out.println("playing as b");
 
 				} else {
 					System.out.println("playing as w");
 
 				}
 				bestMove = bestMoveSearch();
 				playMoveOnCurrentGame(bestMove);
 
 				myCurrentGame.printBoard();
 			} else {
 				if ((myCurrentGame.isBsTurn && playAsBlack)
 						|| (!myCurrentGame.isBsTurn && playAsWhite)) {
 					// read in move
 					boolean moveReadIn = false;
 					GoMove humanMove;
 					while (!moveReadIn) {
 						try {
 							System.out.println("Your move?");
 							input = stdin.readLine();
 							humanMove = GoMove.parseMove(input);
 							humanMove.printMove();
 							playMoveOnCurrentGame(humanMove);
 
 							myCurrentGame.printBoard();
 							System.out.println("Is this your move?");
 
 							input = stdin.readLine();
 							if (input.toString().length() == 0) {
 								System.out.println("THINKING!!!!");
 								moveReadIn = true;
 							} else {
 								undoMove();
 							}
 
 						} catch (Exception e) {
 							System.out.println("Failed reading in move");
 						}
 
 					}
 
 				} else // comps turn
 				{
 					GoMove bestMove;
 
 					bestMove = bestMoveSearch();
 
 					playMoveOnCurrentGame(bestMove);
 
 				}
 
 			}
 		}
 
 	}
 
 	public void undoMove() {
 		
 		if (myCurrentGameTreeNode.parent != null) {
 			boolean turn = myCurrentGame.isBsTurn;
 			myCurrentGameTreeNode = myCurrentGameTreeNode.parent;
 			myCurrentGame = myCurrentGameTreeNode.myGame;
 			if(turn == myCurrentGame.isBsTurn) System.exit(0);
 		}
 		
 	}
 
 	public void playMoveOnCurrentGame(GoMove theMove) {
 		myCurrentGameTreeNode = myCurrentGameTreeNode.playMove(theMove);
 		myCurrentGame = myCurrentGameTreeNode.myGame;
 	}
 
 	public GoMove receiveMoveAndFindBestMove(int x, int y)
 	{
 		playMoveOnCurrentGame(new GoMove(x,y));
 		GoMove bestMove = bestMoveSearch();
 		playMoveOnCurrentGame(bestMove);
 		return bestMove;
 	}
 	
 	public GoMove bestMoveSearch() {
 		GoGameTreeNode futureGame = new GoGameTreeNode(myCurrentGame);
 		int thinkTime = 30;
 		ArrayList<Thread> myThreads = new ArrayList<Thread>();
 		for (int i = 0; i < 1; ++i) {
 
 			Thread aThinkThread = new Thread(new ThinkThread(futureGame));
 
 			myThreads.add(aThinkThread);
 			aThinkThread.start();
 
 		}
 //		
 //		
 		try {
 			Thread.sleep(1000*thinkTime);
 		} catch (InterruptedException e) {
 			
 			e.printStackTrace();
 		}
 		
 		for(Thread t : myThreads)
 		{
 			t.interrupt();
 		}
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return futureGame.bestMoveSoFar();
 	}
 	
 	public class ThinkThread implements Runnable
 	{
 		GoGameTreeNode myStart;
 		public ThinkThread(GoGameTreeNode startNode)
 		{
 			myStart = startNode;
 		}
 		
 		public void run()
 		{
 			while(true)
 			{
 				myStart.playOneSimulation();
 				if(Thread.interrupted())
 					return;
 			}
 		}
 	}
 }
