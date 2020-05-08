 import java.util.Comparator;
 
 public class XComparator implements Comparator<GameBoardNode> {
 
 	public static final int STOPMOVE_AFTER_EACH_STEP = 1;
 	public static final int STOPMOVE_AFTER_N_STEPS = 2;
 	public static final int STOPMOVE_WHEN_BOX_IS_DONE = 3;
 	public static final int STOPMOVE_WHEN_BOX_IS_WITHIN_K_STEPS = 4;
 	public static final int STOPMOVE_AFTER_RANDOM_NUMBER_OF_MOVES = 5;
 	
 	private int condition;
 	private int n;
 	
 	public XComparator(int condition) {
 		this.condition = condition;
 	}
 	
 	public XComparator(int condition, int n) {
 		this.condition = condition;
 		this.n = n;
 	}
 	
 	public int compare(GameBoardNode a, GameBoardNode b) {
		int currentMovingBox;
		
 		
 		switch(condition) {
 		case STOPMOVE_AFTER_EACH_STEP:
 			return 0;
 		case STOPMOVE_AFTER_N_STEPS:
 			return 0;
 		case STOPMOVE_WHEN_BOX_IS_DONE:
 			
 			return 0;
 		case STOPMOVE_WHEN_BOX_IS_WITHIN_K_STEPS:
 			return 0;
 		case STOPMOVE_AFTER_RANDOM_NUMBER_OF_MOVES:
 			return 0;
 		}
 		
 		return 0;
 	}
 	
 	private int getMovingBoxIndex() {
 		Move m = TreeSearcher.currentNode.priorMove;
 		if(m != null)
 			currentMovingBox = m.getBoxIndex();
 		else
 			currentMovingBox = -1;
 		
		
 	}
 }
