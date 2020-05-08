 package item;
 
 import effect.PowerFailureEffect;
 import exception.NoItemException;
 import exception.OutsideTheGridException;
 import game.Game;
 import grid.Square;
 
 import java.util.Observable;
 import java.util.Random;
 
 
 /**
  * A class representing tertiary power failures.
  * 
  * @author 	Group 8.
  * 
  * @version	May 2013.
  */
 public class TertiaryPowerFailure extends PowerFailure {
 	
 	/**
 	 * The number of actions a tertiary power failure stays active.
 	 */
 	public static final int ACTIONS_ACTIVE = 1;
 
 	/**
 	 * The secondary power failure belonging with this tertiary power failure.
 	 */
 	private SecondaryPowerFailure secondaryPowerFailure;
 	
 	/**
 	 * The number corresponding with which possible place the power failure is located.
 	 */
 	private int place;
 
 	/**
 	 * Constructor for the tertiary power failure with given location.
 	 * 
 	 * @param 	location
 	 * 			An int corresponding with which of the three possible generated locations.
 	 */
 	public TertiaryPowerFailure(int location) {
 		super();
 		setPlace(location);
 	}
 
 	/**
 	 * A constructor for a random tertiary power failure.
 	 */
 	public TertiaryPowerFailure() {
 		this((new Random()).nextInt(3)+1);
 	}
 
 	/**
 	 * Return the duration of all tertiary power failures
 	 * 
 	 * @return	ACTIONS_ACTIVE
 	 * 			The number of actions active.
 	 */
 	@Override
 	public int getDuration() {
 		return ACTIONS_ACTIVE;
 	}
 	
 	/**
 	 * Returns the secondary power failure.
 	 * 
 	 * @return	secondaryPowerFailure
 	 * 			The secondary power failure.			
 	 */
 	public SecondaryPowerFailure getSecondaryPowerFailure() {
 		return secondaryPowerFailure;
 	}
 
 	/**
 	 * Sets the secondary power failure to the given power failure.
 	 * 
 	 * @param 	secondaryPowerFailure
 	 * 			The given secondary power failure.
 	 */
 	protected void setSecondaryPowerFailure(SecondaryPowerFailure secondaryPowerFailure) {
 		this.secondaryPowerFailure = secondaryPowerFailure;
 	}
 
 	/**
 	 * Returns the number corresponding with the location of the power failure.
 	 * 
 	 * @return	place
 	 * 			The number corresponding with the location of the power failure.
 	 */
 	public int getPlace() {
 		return place;
 	}
 
 	/**
 	 * Sets the number corresponding with the location of the power failure.
 	 * 
 	 * @param 	place
 	 * 			number corresponding with the location to set.
 	 */
 	protected void setPlace(int place) {
 		this.place = place;
 	}
 	
 	/**
 	 * Terminate this tertiary power failure generator.
 	 */
 	public void terminate() {
 		Game.getInstance().getActionNotifier().deleteObserver(this);
 		if(getLocation() != null) {
 			getLocation().unlinkEffect(new PowerFailureEffect());
 			try {
 				getLocation().removeItem(this);
 			} catch (NoItemException e) {
 				// will not occur
 			}
 		}
 	}
 	
 	/**
 	 * Spread a tertiary power failure on the right location.
 	 */
 	@Override
 	public void generate() {
 		//find the locations that could get a power failure
 		Square tertiaryLocation = null;
 		
 		if(place == 1) {
 			try {
 				tertiaryLocation = getSecondaryPowerFailure().getLocation().getNeighbour(getSecondaryPowerFailure().getDirection());
 			} catch (OutsideTheGridException e) {
 				//square is not added to tertiary locations
 			}
 		
 		if(getSecondaryPowerFailure().getDirection().isDiagonal())
 			
 			if(place == 2) {
 				try {
 					tertiaryLocation = getSecondaryPowerFailure().getLocation().getNeighbour(getSecondaryPowerFailure().getDirection().getxComponent());
 				} catch (OutsideTheGridException e) {
 					//square is not added to tertiary locations
 				}
 			} else if(place == 3) {
 				try {
 					tertiaryLocation = getSecondaryPowerFailure().getLocation().getNeighbour(getSecondaryPowerFailure().getDirection().getyComponent());
 				} catch (OutsideTheGridException e) {
 					//square is not added to tertiary locations
 				}
 			}
 			
 		} else {
 			
 			if(place == 2) {
 				try {
 					tertiaryLocation = getSecondaryPowerFailure().getLocation().getNeighbour(
 							getSecondaryPowerFailure().getDirection().getRotatedDirection(ClockDirection.CLOCKWISE));
 				} catch (OutsideTheGridException e) {
 					//square is not added to tertiary locations
 				}
 			} else if(place == 3) {
 				try {
 					
 					tertiaryLocation = getSecondaryPowerFailure().getLocation().getNeighbour(
 							getSecondaryPowerFailure().getDirection().getRotatedDirection(ClockDirection.COUNTERCLOCKWISE));
 				} catch (OutsideTheGridException e) {
 					//square is not added to tertiary locations
 				}
 			}
 			
 		}
 		
 		// spread a tertiary power failure at a random location
 		if(tertiaryLocation != null){
 			tertiaryLocation.addItem(this);
 			getLocation().linkEffect(new PowerFailureEffect());
 			Game.getInstance().getActionNotifier().addObserver(this);
			setDurationLeft(getDuration());
 		}
 		
 	}
 	
 	/**
 	 * Update this secondary power failure after observing an action switch
 	 * 
 	 * @effect 	Lower the duration this secondary power failure has left
 	 * 			| lowerDurationLeft()
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		lowerDurationLeft();
 		if(getDurationLeft() <= 0) {
 			terminate();
 		}
 	}
 
 }
