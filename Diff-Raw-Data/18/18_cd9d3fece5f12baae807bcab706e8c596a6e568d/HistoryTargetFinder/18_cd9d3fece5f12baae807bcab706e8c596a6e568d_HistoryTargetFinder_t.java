 package bombgame.controller.ai;
 
 import java.util.Arrays;
 
 import bombgame.controller.ai.ManAI;
 import bombgame.entities.GameObject;
 import bombgame.entities.Man;
 import bombgame.entities.Wall;
 import bombgame.controller.ai.NodeFinder.Node;
 
 /**
  * This Class searches a target for an AI. It considers the movement history of the AI's enemy
  * and chooses the next Node in that direction.
  * @author Rookfighter
  *
  */
 public final class HistoryTargetFinder implements TargetFinder {
 	
 	/**
 	 * target Position
 	 */
 	private Position target;
 	
 	/**
 	 * AI that uses this TargetFinder
 	 */
 	private final ManAI ai;
 	
 	/**
 	 * Creates a new TargetFinder with the given AI.
 	 * @param ai - AI that searches a target
 	 */
 	public HistoryTargetFinder(ManAI ai) {
 		this.ai = ai;
 	}
 	
 	/**
 	 * Searches a target considering the direction history of the AI and the position of the enemy focused by the AI.
 	 */
 	@Override
 	public Position searchTarget() {
 		
 		target = null;
 		
 		ai.countDirections();
 		
 		//get direction with highest count
 		int direction = getMaxIndex(ai.getDirectionCount());
 		ai.setDirectionCount(direction, 0);
 		
 		//find suitable Node as Target
 		Position enemy = new Position(ai.getFocusedEnemyX(), ai.getFocusedEnemyY());
 		calculateTarget(enemy, direction);
 		
 		return target;
 	}
 	
 	
 	/**
 	 * Calculates the target by looking for a suitable Node in the given direction from
 	 * the position (x/y)
 	 * @param x - start x-coordinate
 	 * @param y - start y-coordinate
 	 * @param direction - searching direction
 	 */
 	protected void calculateTarget(final Position enemy, int direction) {
 		
 
 		switch(direction) {
 		
 		case Man.UP:
 			findTarget(enemy, 0, -1);
 			break;
 			
 		case Man.DOWN:
 			findTarget(enemy, 0, 1);
 			break;
 			
 		case Man.RIGHT:
 			findTarget(enemy, 1, 0);
 			break;
 			
 		case Man.LEFT:
 			findTarget(enemy, -1, 0);
 			break;
 			
 		default:
 			//no direction
			target = new Position(enemy.getX(), enemy.getY());
 		}
 		
 	}
 	
 	
 	/**
 	 * Finds a target from the given Position (x/y) in the direction determined by xfac and yfac.
 	 * If no suitable target is found, this method calls calculateDirection() with another 
 	 * direction from the last unused coordinate.
 	 * @param x - start x-coordinate
 	 * @param y - start y-coordinate
 	 * @param xfac - factor for the y-direction
 	 * @param yfac - factor for the x-direction
 	 */
 	protected void findTarget( Position start, int xfac, int yfac) {
 		GameObject[][] field = ai.getHandler().getField();
 		
 		int i = 1;
 		int xtmp = start.getX() + xfac * i;
 		int ytmp = start.getY() + yfac * i;
 		
 		while(ytmp >= 0 &&  ytmp < field[0].length &&
 				xtmp < field.length && xtmp >= 0) {
 			
 			if(field[xtmp][ytmp] instanceof Wall) {
 				//if way is blocked by Wall break the loop
 				break;
 			}
 			
 			//check if there is a node with the calculated coordinates
 			Node[] nodes = ai.getNodes();
 			int nidx = Arrays.binarySearch(nodes, new Node(xtmp, ytmp, 0, null));
 			if(nidx >= 0) {
 				target = new Position(xtmp, ytmp);
 				return;
 			}
 			
 			//calculate next position
 			i++;
 			xtmp = start.getX() + xfac * i;
 			ytmp = start.getY() + yfac * i;
 			
 		}
 		//no suitable node found (Wall blocked or out of field)
 		int direction = getMaxIndex(ai.getDirectionCount());
 		ai.setDirectionCount(direction,0);
 		calculateTarget(new Position(xtmp - xfac, ytmp - yfac), direction);
 		
 	}	
 	
 	
 	/**
 	 * This method calculates and returns the index with the highest value from the given
 	 * int array.
 	 * @param ia - examined int array 
 	 * @return - index of highest value in the array
 	 */
 	protected int getMaxIndex(int[] ia) {
 		int maxval = 0;
 		int maxindex = 0;
 		for(int i = 0; i < ia.length; i++) {
 			if(ia[i] > maxval ) {
 				maxval = ia[i];
 				maxindex = i;
 			}
 		}
 		return maxindex;
 	}
 	
 	
 	/**
 	 * Sums up the values of the given int array.
 	 * @param ia - examined int array
 	 * @return - sum of all values
 	 */
 	protected int sum(int[] ia) {
 		int sum = 0;
 		for(int i: ia) {
 			sum+=i;
 		}
 		return sum;
 	}
 	
 	public Position getTarget() {
 		return target;
 	}
 
 }
