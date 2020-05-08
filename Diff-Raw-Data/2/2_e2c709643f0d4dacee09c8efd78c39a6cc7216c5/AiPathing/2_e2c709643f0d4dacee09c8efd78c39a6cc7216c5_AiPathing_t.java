 package atrophy.combat.ai;
 
 import java.util.Stack;
 
 import watoydoEngine.gubbinz.Maths;
 import atrophy.combat.CombatVisualManager;
 import atrophy.combat.level.LevelBlock;
 import atrophy.combat.level.LevelManager;
 import atrophy.combat.level.Portal;
 
 public class AiPathing {
 
 	private Stack<Portal> portalPathway;
 	
 	private Stack<double[]> roomPathway;
 	
 	private int defaultMoveDistance;
 	
 	private int moveDistance;
 	
 	private int moveUnits;
 	
 	private LevelBlock residentBlock;
 	
 	private double[] location;
 	
 	private double[] moveLocation;
 	
 	private LevelManager levelManager;
 	
 	public AiPathing(LevelManager levelManager, double x, double y){
 		
 		this.levelManager = levelManager;
 		
 		moveDistance = 40;
 		defaultMoveDistance = moveDistance;
 		location = new double[2];
 		location[0] = x;
 		location[1] = y;
 		
 		moveLocation = new double[2];
 		moveLocation[0] = x;
 		moveLocation[1] = y;
 		
 		this.residentBlock = levelManager.getBlock(x,y);
 	}
 	
 	public void move(Ai invoker){
 		try{
 			invoker.setSwing(0);
 			invoker.setOldTargetSwing(0);
 			
 			Portal targetPortal = null;
 			
 			if(this.portalPathway != null){
 				targetPortal = portalPathway.peek();
 			}
 			
 			int moveUnitLast = this.moveUnits;
 			
 			// if the unit can move and is not at destination then move
 			while(moveUnits > 0 && Maths.getDistance(location,moveLocation) != 0){
 				
 				if(targetPortal != null){
 					
 					int movedIntoPortal = targetPortal.enter(invoker);
 					
 					// try to enter portal
 					if(movedIntoPortal == 0){
 						// if entry of portal successful then remove it from the portal pathway
 						this.portalPathway.pop();
 						targetPortal = null;
 						// remove the current room pathway
 						this.roomPathway = null;
 						
 						// if the pathway has no portals now then make it null
 						if(this.portalPathway.size() == 0){
 							this.portalPathway = null;
 						}
 					}
 					else if(movedIntoPortal == 1){
 						// try to recalculate path
 						try {
 							invoker.setMoveLocation(moveLocation, false);
 							targetPortal = null;
 							continue;
 						} 
 						catch (PathNotFoundException e) {
 							//return;
 							invoker.setMoveLocationToSelf();
 							invoker.setAction("");
 							targetPortal = null;
 							return;
 						}
 					}
 				}
 				
 				// if within the same block
 				if(levelManager.getBlock(this.moveLocation) == this.residentBlock){
 					
 					// If room pathway still exists move to next point
 					if(this.roomPathway != null){
 						
 						// can see move location
 						if(CombatVisualManager.isInSight(this.location[0], this.location[1], this.moveLocation[0],  this.moveLocation[1], this.getLevelBlock().getHitBox())){
 							moveIntra(invoker, this.moveLocation);
 							continue;
 						}
 						else {
 							
 							if(this.roomPathway.size() == 0){
 								this.roomPathway = null;
 								continue;
 							}
 							
 							moveIntra(invoker,roomPathway.peek());
 							
 							// reset action
 							invoker.setAction("Move");
 							if(Maths.getDistance(this.getLocation(), this.roomPathway.peek()) == 0){
 								this.roomPathway.pop();
 								
 								if(this.roomPathway.size() == 0){
 									this.roomPathway = null;
 									invoker.setAction("");
 								}
 							}
 						}
 					}
 					else{
 						
 						try {
 							this.roomPathway = new Stack<double[]>();
 							this.roomPathway.addAll(PathFinder.findIntraPath(invoker, this.moveLocation));
 						} 
 						catch (PathNotFoundException e) {
 							System.out.println("Bad internal path to target local");
 						}
 						
 						if(roomPathway != null && roomPathway.size() > 0){
 							moveIntra(invoker,roomPathway.get(roomPathway.size() - 1));
 							// reset action
 							invoker.setAction("Move");
 						}
 					}
 				}
 				// find a way to another block to get to the target
 				else if(portalPathway != null){
 					
 					// If room pathway still exists move to next point
					if(this.roomPathway != null && this.roomPathway.size() > 0){
 						
 						targetPortal = portalPathway.peek();
 						
 						// can see move location
 						if(CombatVisualManager.isInSight(this.location[0],
 								                this.location[1],
 								                targetPortal.getLocation(this.getLevelBlock())[0],
 								                targetPortal.getLocation(this.getLevelBlock())[1], this.getLevelBlock().getHitBox())){
 							
 							roomPathway = null;
 							continue;
 						}
 						
 						moveIntra(invoker,roomPathway.get(roomPathway.size() - 1));
 						
 						// reset action
 						invoker.setAction("Move");
 						
 						if(Maths.getDistance(this.getLocation(), this.roomPathway.peek()) == 0){
 							this.roomPathway.remove(this.roomPathway.size() - 1);
 							
 							if(this.roomPathway.size() == 0){
 								this.roomPathway = null;
 								// reset action
 								invoker.setAction("");
 							}
 						}
 					}
 					else{
 						
 						targetPortal = portalPathway.peek();
 						
 						// create path to portal if inside of room
 						try {
 							this.roomPathway = new Stack<double[]>();
 							this.roomPathway.addAll(PathFinder.findIntraPath(invoker, targetPortal.getLocation(this.getLevelBlock())));
 						} 
 						catch (PathNotFoundException e) {
 							System.out.println("Bad internal path to portal " + invoker.getName());
 						}
 						
 						// commit movement
 						moveIntra(invoker, roomPathway.get(roomPathway.size() - 1));
 						
 						if(Maths.getDistance(this.getLocation(), this.roomPathway.peek()) == 0){
 							this.roomPathway.pop();
 							
 							if(this.roomPathway.size() == 0){
 								this.roomPathway = null;
 							}
 						}
 					}
 					
 					// reset action
 					invoker.setAction("Move");
 				}
 				else{
 					//return;
 					invoker.setMoveLocationToSelf();
 					invoker.setAction("");
 					return;
 				}
 				
 				// couldn't move anywhere
 				if(this.moveUnits == moveUnitLast){
 					//return;
 					invoker.setMoveLocationToSelf();
 					invoker.setAction("");
 					return;
 				}
 				moveUnitLast = this.moveUnits;
 			}
 			
 			if(Maths.getDistance(this.getLocation(), this.moveLocation) == 0){
 				invoker.setAction("");
 			}
 		}
 		// cannot find what actually throws this, nothing shows when stepped through
 		catch(NullPointerException npe){}
 	}
 	 
 	/**
 	 * Move intra.
 	 *
 	 * @param moveLocation the move location
 	 */
 	protected void moveIntra(Ai invoker, double[] moveLocation){
 		
 		double angle;
 		
 		// angle from ai to target
 		angle = Maths.getRads(this.location,moveLocation);
 
 		// if can't reach target in 1 turn, move toward it
 		if(Maths.getDistance(this.location,moveLocation) > this.moveUnits){
 			
 			this.location[0] += this.moveUnits * Math.cos(angle);
 			this.location[1] += this.moveUnits * Math.sin(angle);
 
 			// set moveUnits to 0 since it has all been expended
 			this.moveUnits = 0;
 		}
 		// Teleport to target since it's in range
 		else{
 
 			this.moveUnits -= Maths.getDistance(this.location, moveLocation);
 			
 			this.location[0] = moveLocation[0];
 			this.location[1] = moveLocation[1];
 			
 			// Update action to empty to stop ui errors if it was move
 			if(invoker.getAction().equals("Move") && this.portalPathway == null){
 				invoker.setAction("");
 			}
 		}
 	}
 
 	public void resetMoveUnits() {
 		moveUnits = this.moveDistance;
 	}
 
 	public LevelBlock getLevelBlock() {
 		
 		if(this.residentBlock == null)
 			this.residentBlock = levelManager.getBlock(location);
 		
 		return this.residentBlock;
 	}
 
 	public double[] getLocation() {
 		return this.location;
 	}
 
 	public void removeOrders() {
 		this.moveLocation = this.location.clone();
 		this.portalPathway = null;
 		this.roomPathway = null;
 	}
 
 	public void setMoveLocation(Ai invoker, double x, double y) throws PathNotFoundException {
 		this.moveLocation[0] = x;
 		this.moveLocation[1] = y;
 		
 		this.roomPathway = null;
 		
 		// Create new pathway if outside of current room
 		if(levelManager.getBlock(this.moveLocation) != this.residentBlock){
 			// try to create a pathway to the location
 			portalPathway = new Stack<>();
 			portalPathway.addAll(PathFinder.createPathway(this.getLocation(), moveLocation, this.residentBlock, levelManager.getBlock(this.moveLocation)));
 		}
 		// make the pathway null to avoid issues with ui drawing an old pathway
 		else{
 			portalPathway = null;
 			
 			this.roomPathway = new Stack<>();
 			this.roomPathway.addAll(PathFinder.findIntraPath(invoker, this.moveLocation));
 		}
 	}
 
 	public void setMoveLocation(Ai invoker, double[] location) throws PathNotFoundException {
 		this.moveLocation[0] = location[0];
 		this.moveLocation[1] = location[1];
 		
 		this.roomPathway = null;
 		
 		// Create new pathway if outside of current room
 		if(levelManager.getBlock(this.moveLocation) != this.residentBlock){
 			// try to create a pathway to the location
 			portalPathway = new Stack<>();
 			portalPathway.addAll(PathFinder.createPathway(this.getLocation(), moveLocation, this.residentBlock, levelManager.getBlock(this.moveLocation), false));
 		}
 		// make the pathway null to avoid issues with ui drawing an old pathway
 		else{
 			portalPathway = null;
 			
 			this.roomPathway = new Stack<>();
 			this.roomPathway.addAll(PathFinder.findIntraPath(invoker, this.moveLocation));
 		}
 	}
 	
 	protected void deletePortalPathway(){
 		this.portalPathway = null;
 	}
 	
 	public void setMoveDistance(int moveDistance){
 		this.moveDistance = moveDistance;
 	}
 	
 	public void resetMoveDistance(){
 		this.moveDistance = this.defaultMoveDistance;
 	}
 
 	public void setLevelBlock(LevelBlock residentBlock) {
 		this.residentBlock = residentBlock;
 	}
 
 	public void cancelMovement() {
 		// Cancel move
 		this.moveLocation[0] = this.location[0];
 		this.moveLocation[1] = this.location[1];
 		
 		this.clearPathways();
 	}
 
 	public void clearPathways() {
 		this.roomPathway = null;
 		this.portalPathway = null;
 	}
 
 	public void setMoveLocation(Ai invoker, double x, double y, boolean ignoreBlockedDoors) throws PathNotFoundException {
 		this.moveLocation[0] = x;
 		this.moveLocation[1] = y;
 		
 		this.roomPathway = null;
 		
 		// Create new pathway if outside of current room
 		if(levelManager.getBlock(this.moveLocation) != this.residentBlock){
 			// try to create a pathway to the location
 			portalPathway = new Stack<>();
 			portalPathway.addAll(PathFinder.createPathway(this.getLocation(), moveLocation, this.residentBlock, levelManager.getBlock(this.moveLocation),ignoreBlockedDoors));
 		}
 		// make the pathway null to avoid issues with ui drawing an old pathway
 		else{
 			portalPathway = null;
 			
 			this.roomPathway = new Stack<>();
 			this.roomPathway.addAll(PathFinder.findIntraPath(invoker, this.moveLocation));
 		}
 	}
 
 	public void setLocation(double x, double y) {
 		this.location[0] = x;
 		this.location[1] = y;
 	}
 
 	public Stack<Portal> getPortalPathway() {
 		return this.portalPathway;
 	}
 
 	public double[] getMoveLocation() {
 		return this.moveLocation;
 	}
 
 	public int getDefaultMoveDistance() {
 		return this.defaultMoveDistance;
 	}
 	
 	public void setDefaultMoveDistance(int defaultMoveDistance){
 		this.defaultMoveDistance = defaultMoveDistance;
 	}
 
 	public int getMoveDistance() {
 		return this.moveDistance;
 	}
 
 	public boolean didMove() {
 		return this.moveUnits != this.moveDistance;
 	}
 
 	public void moveWithinRadius(Ai invoker, double[] location, double radius) {
 		double angleToPoint = Maths.getRads(this.getLocation(), location);
 		// +1 to cover rounding errors
 		double distance = Maths.getDistance(this.getLocation(), location) - radius + 1;
 		
 		if(distance > 0){
 			try{
 				invoker.setMoveLocation(this.getLocation()[0] + (distance * Math.cos(angleToPoint)),
 									(this.getLocation()[1] + (distance * Math.sin(angleToPoint))));
 			}
 			catch(PathNotFoundException pnfe){
 				invoker.setMoveLocationToSelf();
 			}
 		}		
 	}
 
 	public void moveWithinRadius(Ai invoker, double x, double y, double radius) {
 		double angleToPoint = Maths.getRads(this.getLocation()[0],this.getLocation()[1], x,y);
 		// +1 to cover rounding errors
 		double distance = Maths.getDistance(this.getLocation()[0],this.getLocation()[1], x,y) - radius + 1;
 		
 		if(distance > 0){
 			try{
 				invoker.setMoveLocation(this.getLocation()[0] + (distance * Math.cos(angleToPoint)),
 									(this.getLocation()[1] + (distance * Math.sin(angleToPoint))));
 			}
 			catch(PathNotFoundException pnfe){
 				invoker.setMoveLocationToSelf();
 			}
 		}
 	}
 	
 }
