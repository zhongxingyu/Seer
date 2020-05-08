 package snozama.runtime.demos;
 
 import snozama.amazons.global.GlobalFunctions;
 import snozama.amazons.mechanics.Board;
 import snozama.amazons.mechanics.MoveChoice;
 import snozama.amazons.mechanics.MoveManager;
import snozama.amazons.mechanics.algo.KillerTranspositionNegaScout;
 import snozama.amazons.mechanics.algo.NegaScout;
 import snozama.amazons.mechanics.algo.TranspositionNegaScout;
 import snozama.amazons.settings.Settings;
 import snozama.client.SnozamaPlayer;
 import snozama.ui.api.AUI;
 import snozama.ui.eventListeners.UIReadyListener;
 import snozama.ui.exception.AUIException;
 
 public class DualSnozamaPlayerDemo
 {
 	public static void main(String args[])
 	{
		// Debug data.
		Runtime runtime = Runtime.getRuntime();
		int mb = 1024*1024;
		System.out.println("----");
		//Print total available memory
		System.out.println("Total Memory:" + runtime.totalMemory() / mb);
		
		//Print Maximum available memory
		System.out.println("Max Memory:" + runtime.maxMemory() / mb);
		System.out.println("----");
		
 		Board board = new Board();
 		
 		AUI.getUI();
 		AUI.ready(new UIReadyListener(){
 			@Override
 			public void ready()
 			{
 				//Does anything need to go in here?
 				/* - alex: anything that requires the UI to be ready to interact with
 				 * the application should go in here. It's probably a good idea to put
 				 * everything in inside the ready.
 				 */
 			}
 		});
 		
 		// Play the game.
 		int turn = 1;
 		int move = 0;
 		int colour = Board.WHITE;
 		MoveChoice choice = null;
 		
 		do
 		{
 			AUI.startTurn(colour, Settings.turnTime);
 			if (colour == Board.WHITE)
 			{
				//NegaScout search = new NegaScout(System.currentTimeMillis()+Settings.decisionTime);
				TranspositionNegaScout search = new TranspositionNegaScout(System.currentTimeMillis()+Settings.decisionTime, 16000000, board);
				//KillerTranspositionNegaScout search = new KillerTranspositionNegaScout(System.currentTimeMillis()+Settings.decisionTime, 2000000, board);
 				move = search.chooseMove(board, colour, turn);
 			}
 			else
 			{
 				NegaScout search = new NegaScout(System.currentTimeMillis()+Settings.decisionTime);
				//TranspositionNegaScout search = new TranspositionNegaScout(System.currentTimeMillis()+Settings.decisionTime, 10000000, board);
				//KillerTranspositionNegaScout search = new KillerTranspositionNegaScout(System.currentTimeMillis()+Settings.decisionTime, 2000000, board);
 				move = search.chooseMove(board, colour, turn);
 			}
 			
 			if (move <= 0)
 			{
 				AUI.post("Game over.");
 				break;
 			}
 			choice = new MoveChoice(move, board);
 			try {
 				AUI.moveAmazon(choice.getStartingRow(),
 						choice.getStartingColumn(),
 						choice.getFinishingRow(),
 						choice.getFinishingColumn(),
 						choice.getArrowRow(),
 						choice.getArrowColumn());
 			} catch (AUIException e) {
 				e.printStackTrace();
 				System.exit(-1);
 			}
 			MoveManager.applyUnmanagedMove(board, move);
 			
 			turn++;
 			colour = GlobalFunctions.flip(colour);
 		}
 		while (turn < 94 && move > 0);
 	}
 }
