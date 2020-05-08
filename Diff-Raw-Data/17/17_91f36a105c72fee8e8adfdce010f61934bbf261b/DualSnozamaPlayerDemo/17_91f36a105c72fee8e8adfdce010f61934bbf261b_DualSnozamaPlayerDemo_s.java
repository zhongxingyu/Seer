 package snozama.runtime.demos;
 
 import snozama.amazons.global.GlobalFunctions;
 import snozama.amazons.mechanics.Board;
 import snozama.amazons.mechanics.MoveChoice;
 import snozama.amazons.mechanics.MoveManager;
 import snozama.amazons.mechanics.algo.TranspositionNegaScout;
 import snozama.client.SnozamaPlayer;
 import snozama.ui.api.AUI;
 import snozama.ui.eventListeners.UIReadyListener;
 import snozama.ui.exception.AUIException;
 
 public class DualSnozamaPlayerDemo
 {
 	public static void main(String args[])
 	{
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
		long decisionTime = 3000;	// In milliseconds.
 		MoveChoice choice = null;
 		
 		do
 		{
			TranspositionNegaScout search = new TranspositionNegaScout(System.currentTimeMillis()+decisionTime, 2000000, board);
			move = search.chooseMove(board, Board.WHITE, turn);
			MoveManager.applyUnmanagedMove(board, move);
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
 			
 			turn++;
 			colour = GlobalFunctions.flip(colour);
 		}
 		while (turn < 94 && move > 0);
 	}
 }
