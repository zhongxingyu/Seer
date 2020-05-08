 package item;
 
 import game.Actor;
 
 public class Teleporter extends UnportableItem {
 	
 	private Teleporter destination;
 	
 	public Teleporter(Teleporter destination) {
 		setDestination(destination);
 	}
 
 	/**
 	 * @return the destination
 	 */
 	public Teleporter getDestination() {
 		return destination;
 	}
 
 	/**
 	 * @param destination the destination to set
 	 */
 	public void setDestination(Teleporter destination) {
 		this.destination = destination;
 	}
 	
 	public boolean isValidDestination(Teleporter destination) {
 		return destination != this;
 	}
 	
 	public boolean hasValidSourceAndDestination() {
 		return isValidDestination(destination);
 	}
 
 	@Override
 	public boolean isVisible() {
 		return true;
 	}
 	
 	@Override
 	public boolean canBeSteppedOn() {
		return !getDestination().getLocation().hasPlayer() && !getDestination().getLocation().hasObstacle();
 	}
 
 	@Override
 	public boolean canBeOnSquareWith(Item... items) {
 		boolean can = true;
 		for(Item item: items) {
 			if(item instanceof Teleporter) {
 				can = false;
 			}
 		}
 		return can;
 	}
 
 	@Override
 	public void onStep(Actor actor) {
 		actor.teleport(getDestination());
 	}
 
 	@Override
 	public void onLeave(Actor actor) {
 	}
 
 	@Override
 	public void onStart(Actor actor) {
 	}
 
 	@Override
 	public void onLand(Actor actor) {}
 	
 }
