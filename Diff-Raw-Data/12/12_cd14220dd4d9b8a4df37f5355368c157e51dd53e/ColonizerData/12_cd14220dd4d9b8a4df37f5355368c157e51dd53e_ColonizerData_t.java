 package riskyspace.logic.data;
 
 import riskyspace.model.Player;
 import riskyspace.model.Position;
 
 public class ColonizerData extends SpriteData {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3800045081506741911L;
 
 	/**
 	 * Current, next and the pos after that for rotation data.
 	 */
 	private Position[] steps;
 	
 	public ColonizerData(Position pos, Player player, Position[] steps) {
 		super(pos, player);
 		if (steps.length < 2) {
 			throw new IllegalArgumentException("There must be at least 2 Positions including current");
 		}
 		this.steps = new Position[3];
 		for (int i = 0; i < steps.length && i < 3; i++) {
 			this.steps[i] = steps[i];
 		}
 	}
 
 	public Position[] getSteps() {
		return steps;
 	}
 
 	public void setSteps(Position[] path) {
 		
 	}
 }
