 package sliceWars.logic.gameStates;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import sliceWars.logic.BoardCell;
 import sliceWars.logic.Player;
 import sliceWars.logic.gameStates.FirstDiceDistribution;
 import sliceWars.logic.gameStates.GameStateContextImpl.Phase;
 
 public class FirstDiceBatchDistributionTest {
 	
 	private BoardCellMock _p1Cell;
 	private BoardCellMock _p2Cell;
 	private GameStateContextMock _gameStateContextMock;
 
 	@Test
 	public void testState(){
 		_p1Cell = new BoardCellMock(Player.PLAYER1);
 		_p2Cell = new BoardCellMock(Player.PLAYER2);
 		
 		final int boardCellCount = 4;
 		FirstDiceDistribution subject = new  FirstDiceDistribution(new Player(1, 2),new BoardMockAdapter() {
 			
 			@Override
 			public BoardCell getCellAtOrNull(int x, int y) {
 				if(x == 0) return _p1Cell;
 				return _p2Cell;
 			}
 
 			@Override
 			public int getCellCount() {
 				return boardCellCount;
 			}
			
			@Override
			public int getValidCellsCount() {
				return boardCellCount;
			}
 		});
 		_gameStateContextMock = new GameStateContextMock(){@Override public Phase getPhase() {
 				if(_state == null)
 					return Phase.DICE_DISTRIBUTION;
 				return _state.getPhase();
 		}};
 		playTurn(1, subject);
 		playTurn(2, subject);
 		
 		assertEquals(Phase.FIRST_ATTACKS,_gameStateContextMock.getPhase());
 		assertEquals(Player.PLAYER1,_gameStateContextMock.getWhoIsPlaying());
 	}
 
 	private void playTurn(int turn, FirstDiceDistribution subject) {
 		subject.play(0, 0,_gameStateContextMock);
 		assertEquals(turn, _p1Cell.getDiceCount());
 		assertEquals(Player.PLAYER2,_gameStateContextMock.getWhoIsPlaying());
 		subject.play(1, 0,_gameStateContextMock);
 		assertEquals(turn, _p2Cell.getDiceCount());
 		assertEquals(Player.PLAYER1,_gameStateContextMock.getWhoIsPlaying());
 	}
 
 	
 }
