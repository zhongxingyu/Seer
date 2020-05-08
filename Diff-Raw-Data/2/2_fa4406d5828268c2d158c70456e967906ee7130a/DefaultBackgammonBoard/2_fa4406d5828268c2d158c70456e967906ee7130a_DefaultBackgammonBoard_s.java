 package backgammon.model.board;
 
 import java.util.Vector;
 import backgammon.model.interfaces.IBackgammonBoard;
 import backgammon.model.interfaces.ICheckerList;
 import backgammon.model.player.Player;
 
 public class DefaultBackgammonBoard implements IBackgammonBoard {
 	
	private ICheckerList[] points = new Point[IBackgammonBoard.NUMBER_OF_CHECKERS_PER_PLAYER];
 	private ICheckerList bar;
 	private ICheckerList[] outs = new Out[2];
 	
 	public DefaultBackgammonBoard(Player player1, Player player2) {
 		for (int i = 0; i < IBackgammonBoard.BAR_INDEX; i++) {
 			this.points[i] = new Point();
 		}
 		this.bar = new Bar(player1, player2);
 		this.outs[0] = new Out();
 		this.outs[1] = new Out();
 	}
 
 	public ICheckerList getFieldOnBoard(int index) {
 		if (0 <= index && index < IBackgammonBoard.BAR_INDEX) {
 			return this.points[index];
 		
 		} else if (index == IBackgammonBoard.BAR_INDEX) {
 			return this.bar;
 			
 		} else if (index == IBackgammonBoard.OUT_PLAYER1_INDEX) {
 			return this.outs[0];
 			
 		} else if (index == IBackgammonBoard.OUT_PLAYER2_INDEX) {
 			return this.outs[1];	
 		}
 		
 		return null;
 	}
 		
 	public boolean allCheckersInHouse(Player player, int playerID) {
 		
 		int beginIndex = (playerID == 1) ? (IBackgammonBoard.BAR_INDEX - 6) : (0);
 		int endIndex = (playerID == 1) ? (IBackgammonBoard.BAR_INDEX - 1) : (5);
 		int count = 0;
 		
 		for (int currentIndex = beginIndex; currentIndex <= endIndex; currentIndex++) {
 			count += this.points[currentIndex].getCheckerCountForPlayer(player);
 		}
 		
 		return (count == IBackgammonBoard.NUMBER_OF_CHECKERS_PER_PLAYER);
 	}
 	
 	public Vector<Integer> playerHasBlots(Player player) {
 		Vector<Integer> blots = new Vector<Integer>();
 		
 		for (int currentIndex = 0; currentIndex < IBackgammonBoard.BAR_INDEX; currentIndex++) {
 			if (this.points[currentIndex].isBlotOfPlayer(player)) {
 				blots.add(new Integer(currentIndex));
 			}
 		}
 		return blots;
 	}
 }
