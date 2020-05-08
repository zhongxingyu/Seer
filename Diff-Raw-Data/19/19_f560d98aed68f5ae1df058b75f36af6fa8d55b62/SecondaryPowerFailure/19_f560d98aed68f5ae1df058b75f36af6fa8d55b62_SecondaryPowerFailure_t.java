 package item;
 
 import effect.PowerFailureEffect;
 import exception.NoItemException;
 import exception.OutsideTheGridException;
 import game.Player;
 import grid.Direction;
 import grid.Square;
 
 import java.util.Observable;
 
 /**
  * A class representing secondary power failures.
  * 
  * @author 	Group 8.
  * 
  * @version	May 2013.
  */
 public class SecondaryPowerFailure extends PowerFailure {
 	
 	/**
 	 * The number of actions a secondary power failure stays active.
 	 */
 	public static final int ACTIONS_ACTIVE = 2;
 	
 	/**
 	 * The primary power failure that generated this secondary power failure.
 	 */
 	private PrimaryPowerFailure primaryPowerFailure;
 	
 	/**
 	 * The tertiary power failure of this secondary power failure.
 	 */
 	private TertiaryPowerFailure tertiaryPowerFailure;
 
 	/**
 	 * The direction in which the secondary power failure currently is.
 	 */
 	private Direction direction;
 	
 	/**
 	 * The clock direction in which the secondary power failure rotates.
 	 */
 	private ClockDirection clockDirection;
 	
 	/**
 	 * Initialize this new secondary power failure with a given tertiary power failure generator, clock direction 
 	 * and direction.
 	 * 
 	 * @param 	startDirection
 	 * 			The startdirection for this secondary power failure.
 	 * @param	cd
 	 * 			The ClockDirection for this secondary power failure.
 	 * @param 	tertiarypfg 
 	 * 			The tertiary power failure generator to set.
 	 * 
 	 * @effect 	Set the tertiary power failure generator to the given tertiary power failure generator.
 	 * 			|setTertiaryPowerFailure(tertiarypfg)
 	 * @effect	The start direction is set to the given direction.
 	 * 			|setDirection(startDirection)
 	 * @effect	The clock direction is set to the given clock direction.
 	 * 			|setClockDirection(cd)
 	 * @effect 	This secondary power failure observers the players
 	 * 		
 	 */
 	public SecondaryPowerFailure(Direction startDirection, ClockDirection cd, TertiaryPowerFailure tertiaryPF) {
 		super();
 		setTertiaryPowerFailure(tertiaryPF);
 		setDirection(startDirection);
 		setClockDirection(cd);
 	}
 	
 	/**
 	 * Initialize this new secondary power failure with random parameters.
 	 */
 	public SecondaryPowerFailure() {
 		this(Direction.getRandomDirection(), ClockDirection.getRandomClockDirection(), new TertiaryPowerFailure());
 	}
 	
 	/**
 	 * Return the duration of all secondary power failures
 	 * 
 	 * @return	the amount of actions active.
 	 * 			| return ACTIONS_ACTIVE
 	 */
 	@Override
 	public int getDuration() {
 		return ACTIONS_ACTIVE;
 	}
 	
 	/**
 	 * 
 	 */
 	@Override
 	public void setLocation(Square location) {
 		super.setLocation(location);
 		if(location != null) {
 			for(Player p: getLocation().getGrid().getGame().getPlayers()) {
 				p.addObserver(this);
 			}
 		} else {
 			for(Player p: getLocation().getGrid().getGame().getPlayers()) {
 				p.deleteObserver(this);
 			}
 		}
 	}
 
 	/**
 	 * Returns the primary power failure.
 	 * 
 	 * @return	The primary power failure.
 	 * 			| return primaryPowerFailure
 	 */
 	public PrimaryPowerFailure getPrimaryPowerFailure() {
 		return primaryPowerFailure;
 	}
 
 	/**
 	 * Sets the primary power failure to the given primary power failure.
 	 * 
 	 * @param 	primaryPowerFailure
 	 * 			The new primary power failure.
 	 * 
 	 * @post	The primary power failure for this secondary power failure is set.
 	 * 			| new.primaryPowerFailure == primaryPowerFailure
 	 */
 	protected void setPrimaryPowerFailure(PrimaryPowerFailure primaryPowerFailure) {
 		this.primaryPowerFailure = primaryPowerFailure;
 	}
 
 	/**
 	 * Return the tertiary power failure.
 	 * 
 	 * @return	The tertiary power failure.
 	 * 			| return tertiaryPowerFailure
 	 */
 	public TertiaryPowerFailure getTertiaryPowerFailure() {
 		return tertiaryPowerFailure;
 	}
 
 	/**
 	 * Set the tertiary power failure to the given tertiary power failure.
 	 * 
 	 * @param 	tertiaryPF
 	 * 			The tertiary power failure to set.
 	 * 
 	 * @post 	The tertiary power failure generator is set to the given tertiary power failure.
 	 * 			|new.getTertiarypfg() == tertiaryPF
 	 * @post	The tertairy power failure has this secondary power failure as secondary power failure.
 	 * 			|tertiaryPF.getSecondaryPowerFailure() == this
 	 */
 	protected void setTertiaryPowerFailure(TertiaryPowerFailure tertiaryPF) {
 		this.tertiaryPowerFailure = tertiaryPF;
 		if(tertiaryPF!=null)
 			tertiaryPF.setSecondaryPowerFailure(this);
 	}
 
 	/**
 	 * Returns the clock direction for this secondary power failure.
 	 * 
 	 * @return	clockDirection
 	 * 			The clock direction for this secondary power failure.
 	 */
 	public ClockDirection getClockDirection() {
 		return clockDirection;
 	}
 	
 	/**
 	 * Sets the clock direction to the given clock direction.
 	 * 
 	 * @param 	clockDirection
 	 * 			The clock direction we want to set.
 	 * 
 	 * @post	The clock direction is set to the given value.
 	 * 			| new.clockDirection == clockDirection
 	 */
 	protected void setClockDirection(ClockDirection clockDirection) {
 		this.clockDirection = clockDirection;
 	}
 
 	/**
 	 * Returns the direction of the secondary power failure.
 	 * 
 	 * @return	direction
 	 * 			The direction of this secondary power failure.
 	 */
 	public Direction getDirection() {
 		return direction;
 	}
 
 	/**
 	 * Sets the direction to the given direction.
 	 * 
 	 * @param 	direction
 	 * 			The direction we want to set.
 	 * 
 	 * @post	The direction is set to the given value.
 	 * 			| new.direction == direction
 	 */
 	protected void setDirection(Direction direction) {
 		this.direction = direction;
 	}
 	
 	/**
 	 * Generate a secondary power failure on the grid.
 	 */
 	@Override
 	public void generate() {
 		if(getLocation() != null){
 			try {
 				getLocation().removeItem(this);
 			} catch (NoItemException e) {
 				//cannot occur
 			}
 		}
 		
 		Square secondaryLocation = null;
 		setDirection(getDirection().getRotatedDirection(getClockDirection()));
 		
 		try {
 			secondaryLocation = getPrimaryPowerFailure().getLocation().getNeighbour(direction);
 			secondaryLocation.addItem(this);
 			getLocation().linkEffect(new PowerFailureEffect());
 			setDurationLeft(getDuration());
 			getTertiaryPowerFailure().generate();
 		} catch (OutsideTheGridException e) {
 			// nothing to do here
 		}
 	}
 	
 	/**
 	 * Update this secondary power failure after observing an action switch.
 	 * 
 	 * @effect 	Lower the duration this secondary power failure has left
 	 * 			|lowerDurationLeft()
 	 * @effect	When the duration of this secondary power failure reaches 0, terminate it and generate a
 	 * 			new secondary power failure.
 	 * 			|if(getDurationLeft() <= 0)
 	 * 			|	terminate()
 	 * 			|	generate()
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		lowerDurationLeft();
 		if(getDurationLeft() <= 0) {
 			terminate();
 			generate();
 		}
 	}
 	
 	/**
 	 * Terminate this secondary power failure
 	 * 
 	 * @effect 	This secondary power failure stops observing the players
 	 * @effect 	This secondary power failure is removed from its location.
 	 * 			| getLocation().removeItem(this)
 	 * @effect	The tertiary power failure is terminated.
 	 * 			| getTertiaryPowerFailure().terminate()
 	 */
 	@Override
 	public void terminate() {	
 		if(getLocation() != null) {
 			getLocation().unlinkEffect(new PowerFailureEffect());
 			try {
 				getLocation().removeItem(this);
 			} catch (NoItemException e) {
 				// will not occur
 			}
 		}
 
 		getTertiaryPowerFailure().terminate();
 	}
 
 }
