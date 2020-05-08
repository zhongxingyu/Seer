 package net.thiagoalz.hermeto.view.strategies;
 
 import java.util.Set;
 
 import net.thiagoalz.hermeto.panel.GameContext;
 import net.thiagoalz.hermeto.panel.GameManager;
 import net.thiagoalz.hermeto.panel.Position;
 import net.thiagoalz.hermeto.player.Player;
 import android.util.Log;
 
 public class LineSelectionStrategy extends AbstractSelectionStrategy {
 	
 	private static final String TAG = LineSelectionStrategy.class.getCanonicalName();
 	
 	public LineSelectionStrategy(GameManager gameManager) {
 		super(gameManager);
 	}
 	
 	@Override
 	public boolean mark(Player player) {
 		// Retrieving the game context.
 		GameContext gameContext = getGameManager().getGameContext();
 		
 		// Retrieving all the marked squares
 		Set<Position> markedSquares = gameContext.getMarkedSquares();
 		
 		Position selectedPosition = new Position(player.getPosition().getX(),
 				player.getPosition().getY());
 	
 		if (markedSquares.contains(selectedPosition)) {
 			synchronized(this) {
				// If the position is already selected, so undo it.
 				Log.d(TAG, "Dismarking square [" + player.getPosition().getX() + ", "
 						+ player.getPosition().getY() + "]");
 				markedSquares.remove(selectedPosition);
 				notifyDeselection(player, selectedPosition);
 				return false;
 			}
 		} else {
 			synchronized(this) {
 				// Verify if any position in the same line is already selected, if it is, undo it.
 				for (Position markedSquare: markedSquares) {
 					if (markedSquare.getX() == player.getPosition().getX()) {
						Log.d(TAG, "Dismarking square [" + markedSquare.getX() + ", "
								+ markedSquare.getY() + "]");
						markedSquares.remove(markedSquare);
						notifyDeselection(player, markedSquare);
 					}
 				}
 				// If the position is not marked yet, mark it.
 				Log.d(TAG, "Marking square [" + player.getPosition().getX() + ", "
 						+ player.getPosition().getY() + "]");
 				markedSquares.add(player.getPosition());
 				notifySelection(player, selectedPosition);
 				return true;
 			}
 		}
 	}
 	
 	
 }
