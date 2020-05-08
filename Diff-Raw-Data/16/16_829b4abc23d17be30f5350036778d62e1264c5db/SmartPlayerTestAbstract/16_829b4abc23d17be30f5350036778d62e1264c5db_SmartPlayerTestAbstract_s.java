 package iago;
 
 import iago.players.Player;
 import iago.players.Player.PlayerType;
 
 import org.junit.Test;
 
 public abstract class SmartPlayerTestAbstract extends PlayerTestAbstract {
 	protected Player smartWhitePlayer, smartBlackPlayer;
 	
 	//This is a solved game, white (2nd player) wins by 8
 	@Test
 	public void testPerfectPlay() {
 		Board small4x4Board = new Board(DebugFunctions.make4x4OthelloString());
 		Boolean blacksTurn = true;
 		Move nextMove = new Move(0,0);
		while(!nextMove.equals(new Move(-1,-1)))
 		{		
 			if(blacksTurn)
 			{
 				nextMove = smartBlackPlayer.chooseMove(small4x4Board);
 			}else{
 				nextMove = smartWhitePlayer.chooseMove(small4x4Board);
 			}
			//apply the move
			small4x4Board.apply(nextMove, blacksTurn?PlayerType.BLACK:PlayerType.WHITE, true);
 			blacksTurn = !blacksTurn;
 		}
 
 
		assertEquals(small4x4Board.scoreBoard(PlayerType.WHITE),8);
 	}
 }
