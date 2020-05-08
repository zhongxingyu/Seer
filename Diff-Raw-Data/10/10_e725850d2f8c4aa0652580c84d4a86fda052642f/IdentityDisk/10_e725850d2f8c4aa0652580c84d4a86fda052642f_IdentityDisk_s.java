 package item;
 
 import effect.IdentityDiskEffect;
 import exception.InvalidDirectionException;
 import exception.NoItemException;
 import exception.OutsideTheGridException;
 import game.Actor;
 import grid.Direction;
 import grid.Square;
 import coverage.*;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * This class represents an identity disk.
  * Identity discs are items that can be picked up and used by a player to shoot at other players.
  *
  * @author 	Groep 8
  * @version March 2013
  */
 public class IdentityDisk extends PortableItem implements MovableItem, Actor {
 
 	/**
 	 * Represents the direction in which this identity disk will be thrown.
 	 */
 	private Direction direction;
 	
 	/**
 	 * Represents the range of the identity disk.
 	 */
 	public final static int RANGE = 4;
 	
 	/**
 	 * Represents a list of teleporters that are already used by this identity disk.
 	 */
 	private Set<Teleporter> teleportersLandedOn = new HashSet<Teleporter>();
 	
 	/**
 	 * Set the direction in which this identity disk will be thrown.
 	 * 
 	 * @Post	The given direction is set as the direction in which this identity disk will be thrown.
 	 * 			| new.getDirection() == direction
 	 * @throws 	InvalidDirectionException [must]
 	 * 			When the direction is not a valid direction.
 	 */
 	@Override
 	public void setDirection(Direction direction) throws InvalidDirectionException {
 		if(direction != Direction.NORTH && direction != Direction.WEST &&
 				direction != Direction.EAST && direction != Direction.SOUTH)
 			throw new InvalidDirectionException();
 		this.direction = direction;
 	}
 
 	/**
 	 * Returns the direction in which this identity disk will be thrown.
 	 * 
 	 * @return	The direction in which this identity disk will be thrown.
 	 */
 	@Override
 	public Direction getDirection() {
 		return direction;
 	}
 	
 	/**
 	 * TODO
 	 * @param sq
 	 */
 	public void changeLocation(Square sq) {
 		try {
 			getLocation().removeItem(this);
 		} catch (NoItemException e) {
 			// cannot occur
 		}
 		sq.addItem(this);
 	}
 	
 	/**
 	 * TODO
 	 * @throws OutsideTheGridException 
 	 */
 	public void move(Direction dir) throws OutsideTheGridException {
 		getLocation().onLeave(this);
 		changeLocation(getLocation().getNeighbour(dir));
 		getLocation().onStep(this);
 	}
 	
 	/**
 	 * TODO
 	 */
 	public void teleport(Teleporter destination) {
 		if(teleportersLandedOn.contains(destination)) {
 			setLocation(destination.getLocation());
 			getLocation().onLand(this);
 		}
 	}
 	
 	/**
 	 * Shoots the disc in a direction chosen by the user.
 	 */
 	@Override
 	public void use() {
 		super.use();
 		Direction direction = getDirection();
 		int range = RANGE;
 		getInventory().getOwner().getLocation().addItem(this);
 		while(range > 0) { 
 			try {
 				if(getLocation().getNeighbour(direction).getObstacle() instanceof Wall) { // Disc hits a wall.
 					// Drop it.
 					break;
 				} else {
 					// If no obstacle on next square, go to next square
 					move(direction);
 					range--;		
 					
 					if(getLocation().hasPlayer()) { // Disc hits a player.
						// The disk is still "flying".
 						getLocation().addEffect(new IdentityDiskEffect());
 						break;
 					}
 					if(getLocation().hasPowerFailure()) {
 						// Each square without power on the trajectory of the identity disc, including the square
 						// from which the disc was launched, decreases the total range of the identity disc by 1.
 						range--;
 						// Don't add the item, because it is still 'flying'.
 					}
 				}
 			} catch (OutsideTheGridException e) { // Disc hits a grid boundary.
 				// Drop it.
 				break;
 			}
 		}
 		// Clear the list of teleporters already landed on for the next use of a teleporter.
 		teleportersLandedOn.clear();
 	}
 
 	@Override
 	public boolean isVisible() {
 		return true;
 	}
 	
 	@Override
 	public boolean canBeSteppedOn() {
 		return true;
 	}
 
 	@Override
 	public boolean canBeOnSquareWith(Item... items) {
 		return true;
 	}
 
 	@Override
 	public void onStep(Actor actor) {}
 
 	@Override
 	public void onLeave(Actor actor) {}
 
 	@Override
 	public void onStart(Actor actor) {}
 
 	@Override
 	public void onLand(Actor actor) {}
 	
 	/**
 	 * TODO
 	 */
 	@Override
 	public String toString() {
 		return "Identity Disk";
 	}
 	
 	public Direction[] getPossibleDirections()
 	{
 		Direction[] rv = new Direction[4];
 		rv[0] = Direction.NORTH;
 		rv[1] = Direction.EAST;
 		rv[2] = Direction.SOUTH;
 		rv[3] = Direction.WEST;
 		return rv;
 	}
 			
 }
