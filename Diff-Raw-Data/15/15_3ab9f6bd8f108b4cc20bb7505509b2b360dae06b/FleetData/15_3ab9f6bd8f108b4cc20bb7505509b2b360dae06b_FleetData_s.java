 package riskyspace.logic.data;
 
 import riskyspace.model.Player;
 import riskyspace.model.Position;
 import riskyspace.model.ShipType;
 
 public class FleetData extends SpriteData {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7281617499767850994L;
 	
 	/**
 	 * Current, next and the pos after that for rotation data.
 	 */
 	private Position[] steps;
 	
 	private ShipType flagship;
 	
 	public FleetData(Position pos, Player player, ShipType flagship, Position[] steps) {
 		super(pos, player);
 		this.flagship = flagship;
 		if (steps.length < 2) {
 			throw new IllegalArgumentException("There must be at least 2 Positions including current");
 		}
 		this.steps = new Position[3];
		for (int i = 0; i < steps.length && i < 3; i++) {
			this.steps[i] = steps[i];
 		}
 	}
 	
 	public ShipType getFlagships(){
 		return flagship;
 	}
 	
 	public void setFlagShip(ShipType flagship) {
 		this.flagship = flagship;
 	}
 	
 	public Position[] getSteps() {
 		return steps;
 	}
 
 	public void setSteps(Position[] steps) {
 		this.steps = steps;
 	}
 }
