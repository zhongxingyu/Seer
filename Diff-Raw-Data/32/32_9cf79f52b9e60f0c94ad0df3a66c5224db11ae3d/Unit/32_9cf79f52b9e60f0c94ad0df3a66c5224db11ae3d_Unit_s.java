 package projectswop20102011.domain;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import projectswop20102011.exceptions.InvalidTargetableException;
 import projectswop20102011.exceptions.InvalidDurationException;
 import projectswop20102011.exceptions.InvalidEmergencyStatusException;
 import projectswop20102011.exceptions.InvalidFinishJobException;
 import projectswop20102011.exceptions.InvalidLocationException;
 import projectswop20102011.exceptions.InvalidSpeedException;
 import projectswop20102011.exceptions.InvalidMapItemException;
 import projectswop20102011.exceptions.InvalidMapItemNameException;
 import projectswop20102011.exceptions.InvalidWithdrawalException;
 
 /**
  * A class that represents a unit.
  *
  * @author Willem Van Onsem, Jonas Vanthornhout & Pieter-Jan Vuylsteke
  * @invar The name of a unit is always valid.
  *		|isValidName(getName())
  * @invar The current location of a unit is always valid.
  *		|isValidCurrentLocation(getCurrentLocation())
  * @invar The home location of a unit is always valid.
  *		|isValidHomeLocation(getHomeLocation())
  * @invar The speed of a unit is always valid.
  *		|isValidSpeed(getSpeed())
  * @invar The target of the unit is always valid.
  *              |isValidTarget(getTarget())
  */
 public abstract class Unit extends MapItem implements TimeSensitive {
 
     /**
      * A variable registering the speed of this unit.
      */
     private long speed;
     /**
      * A variable registering the current location of this unit.
      */
     private GPSCoordinate currentLocation;
     /**
      * A variable registering the emergency of this unit.
      */
     private Emergency emergency;
     /**
      * A variable registering whether a unit was already at the site of emergency.
      */
     private boolean wasAlreadyAtSite;
     /**
      * A variable registering the target where the Units will go to (for instance an emergency, a hospital or it's home location).
      */
     private Targetable target;
 	/**
 	 * A variable registering the state of a unit
 	 */
 	private UnitStatus unitStatus;
 
     /**
      * Initialize a new unit with given parameters.
      *
      * @param name
      *		The name of the new unit.
      * @param homeLocation
      *		The home location of the new unit.
      * @param speed
      *		The speed of the new unit.
      * @effect The new unit is a unit with given name and home location.
      *		|super(name,homeLocation);
      * @effect This speed is equal to the given parameter speed.
      *		|speed.equals(getSpeed())
      * @effect This currentLocation is equal to the given parameter homeLocation.
      *		|homeLocation.equals(getCurrentLocation())
      * @effect This emergency is equal to null.
      *		|getEmergency().equals(null)
      * @effect The unit wasn't already at the site of emergency.
      *		|setWasAlreadyAtSite(false)
      * @throws InvalidMapItemNameException
      *		If the given name is an invalid name for a unit.
      * @throws InvalidLocationException
      *		If the given location is an invalid location for a unit.
      * @throws InvalidSpeedException
      *		If the given speed is an invalid speed for a unit.
      */
     Unit(String name, GPSCoordinate homeLocation, long speed) throws InvalidLocationException, InvalidMapItemNameException, InvalidSpeedException {
         super(name, homeLocation);
         setSpeed(speed);
         setCurrentLocation(homeLocation);
         setEmergency(null);
         setWasAlreadyAtSite(false);
 		setUnitStatus(UnitStatus.IDLE);
         try {
             setTarget(this);
         } catch (InvalidTargetableException ex) {
             //We assume this can't happen.
             Logger.getLogger(Unit.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Returns whether this unit is assigned.
      * @return True if this unit is assigned; false otherwise.
      */
     public boolean isAssigned() {
         return (getEmergency() != null);
     }
 
     /**
      * Returns whether this unit is at its destination.
      * @return True if this unit is at its destination; false otherwise.
      */
     public boolean isAtDestination() {
         if (getEmergency() != null) {
             return getCurrentLocation().equals(getDestination());
         } else {
             return false;
 			//TODO: waarom wordt hier false teruggegeven? Wanneer hij geen opdracht heeft, test hij toch of de Unit terug op zijn HomeLocation staat?
 			//IMHO: Dit is iets dat public moet staan, dus moet er altijd een juist antwoord gegeven worden.
 			//Als je hier dus true teruggeeft dan is hij op de destination wanneer hij geen emergency geeft. Dit strookt niet met mijn gevoel :) -> false returnen
         }
     }
 
     /**
      * Returns the speed of this unit.
      * @return The speed of this unit.
      */
     public long getSpeed() {
         return speed;
     }
 
     /**
      * Returns the current location of this unit.
      * @return The current location of this unit.
      */
     public GPSCoordinate getCurrentLocation() {
         return currentLocation;
     }
 
     /**
      * Returns the destination of this unit.
      * @return The location of the assigned emergency if the unit is assigned, otherwise the homelocation.
      */
     public GPSCoordinate getDestination() {
         return this.getTarget().getTargetLocation();
     }
 
     /**
      * Returns whether a unit was already at the site of emergency.
      * @return True if this unit was already at the site of emergency, false otherwise.
      */
     private boolean wasAlreadyAtSite() {
         //TODO: waarom wordt deze boolean eigenlijk bijgehouden??
 		//Ik vraag mij dit ook af :), ik heb ze voorlopig private gezet
         return wasAlreadyAtSite;
     }
 
 	/**
 	 * Return the state of this unit
 	 * @return the state of this unit.
 	 */
 	public UnitStatus getUnitStatus(){
 		return unitStatus;
 	}
 
 	/**
 	 * Sets the state of this unit to the given value
 	 * @param unitStatus
 	 *		The new state of this unit
 	 */
 	public void setUnitStatus(UnitStatus unitStatus){
 		this.unitStatus = unitStatus;
 	}
 
 
     /**
      * Sets the speed of this unit to the given value.
      * @param speed
      *		The new speed of this unit.
      * @throws InvalidSpeedException
      *		If the given speed isn't a valid speed for a unit.
      * @post The speed of this unit is set according to the given speed.
      *		|new.getSpeed() == speed
      */
     private void setSpeed(long speed) throws InvalidSpeedException {
         if (!isValidSpeed(speed)) {
             throw new InvalidSpeedException(String.format("\"%s\" is an invalid speed for this unit.", speed));
         } else {
             this.speed = speed;
         }
     }
 
     /**
      * Sets the current location of this unit to the given value.
      * @param currentLocation
      *		The new speed of this unit.
      * @post The current location of this unit is set according to the given current location.
      *		|new.getCurrentLocation() == currentLocation
      */
     private void setCurrentLocation(GPSCoordinate currentLocation) {
         this.currentLocation = currentLocation;
     }
 
     /**
      * Sets the emergency of this Unit.
      * @param emergency
      *		The new emergency of this unit.
      * @post The emergency of this unit is set to the given emergency.
      *		|new.getEmergency() == emergency
      */
     final void setEmergency(Emergency emergency) {
         this.emergency = emergency;
         try {
             if (emergency != null) {
                 this.setTarget(emergency);
             } else {
                 this.setTarget(this);
             }
         } catch (InvalidTargetableException ex) {
             //We assume this can't happen
             Logger.getLogger(Unit.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Sets whether a unit was already at the site of emergency.
      * @param wasAlreadyAtSite
      *		The condition whether a unit was already at the site of emergency.
      * @post The condition wasAlreadyAtSite is set to the given value.
      *		|new.wasAlreadyAtSite() == wasAlreadyAtSite
      */
     protected final void setWasAlreadyAtSite(boolean wasAlreadyAtSite) {
         this.wasAlreadyAtSite = wasAlreadyAtSite;
     }
 
     /**
      * Returns the emergency of this unit.
      * @return The emergency of this unit if the unit is assigned, otherwise null.
      */
     public Emergency getEmergency() {
         return emergency;
     }
 
     /**
      * Checks if the given speed is a valid speed for a unit.
      * @param speed
      *		The speed of a timesensitive mapitem to test.
      * @return True if the speed is valid; false otherwise.
      */
     public boolean isValidSpeed(long speed) {
         return speed >= 0;
     }
 
     /**
      * Checks if the given current location is a valid current location for a unit.
      * @param currentLocation
      *		The current location of a unit to test.
      * @return True if the current location is valid; false otherwise.
      */
     public static boolean isValidCurrentLocation(GPSCoordinate currentLocation) {
         return (currentLocation != null);
     }
 
     /**
      * Change the current location of this unit over a given time interval.
      * @param duration
      *		The duration of the displacement of this unit.
      * @effect
      *		The location of this unit has been changed according to a given time interval.
      *		Stop is the current location of this unit after the time interval.
      *		|changeCurrentLocation(stop)
      * @throws InvalidDurationException
      *		If the given duration is invalid.
      */
     private void changeLocation(long duration) throws InvalidDurationException {
         if (duration > 0) {
             if (!this.isAtDestination()) {
                 this.setCurrentLocation(this.getCurrentLocation().calculateMovingTo(this.getDestination(), (double) this.getSpeed() * duration / 3600));
                 checkArrivedAtDestination();
             }
         } else if (duration < 0) {
             throw new InvalidDurationException(String.format("\"%s\" is an invalid duration for a unit.", duration));
         }
 
     }
 
     /**
      * Advances the time with a given amount of seconds.
      * @param seconds
      *		The amount of seconds that the time must be advanced.
      * @throws InvalidDurationException
      *		If the given amount of seconds is invalid.
      * @effect The location of this unit is changed.
      *		|changeLocation(seconds)
      * @effect If this unit arrived at the location of the emergency during this action, the flag wasAlreadyAtSite is set to true.
      *		| wasAlreadyAtSite().equals(true)
      */
     @Override
     public void timeAhead(long seconds) throws InvalidDurationException {
         changeLocation(seconds);
         if (isAssigned() && !wasAlreadyAtSite() && getCurrentLocation().equals(getEmergency().getLocation())) {
             setWasAlreadyAtSite(true);
         }
     }
 
     /**
      * Assign the unit to a given emergency.
      * @param emergency
      *		The emergency where this unit has to respond to.
      * @effect The units emergency is equal to the given emergency
      *		| this.getEmergency().equals(emergency)
      * @effect The unit is assigned.
      * @throws InvalidMapItemException
      *          If the unit is already assigned to an emergency.
      */
     void assignTo(Emergency emergency) throws InvalidMapItemException {
         if (!canBeAssigned()) {
             throw new InvalidMapItemException("Unit can't be assigned");
         } else {
             setEmergency(emergency);
             if (getCurrentLocation().equals(emergency.getLocation())) {
                 setWasAlreadyAtSite(true);
             }
 			setUnitStatus(UnitStatus.ASSIGNED);
         }
     }
 
     /**
      * Checks if this unit can be assigned.
      * @return True if this unit can be assigned, otherwise false.
      */
     public boolean canBeAssigned() {
         return !isAssigned();
     }
 
     /**
      * Withdraw this unit from the emergency he is currently assigned to
      * @effect The unit is withdrawn from its emergency.
      *		|this.getEmergency().withdrawUnit(this)
      * @effect The emergency of this unit is set to null.
      *		|this.setEmergency(null)
      * @throws InvalidWithdrawalException
      *			If the unit is already at site of the emergency.
      * @throws InvalidEmergencyStatusException
      *                  If the emergency of this unit is in a state where the unit cannot withdraw.
      */
     public void withdraw() throws InvalidWithdrawalException, InvalidEmergencyStatusException {
         if (!canBeWithdrawn()) {
             throw new InvalidWithdrawalException("Unit can't be withdrawn.");
         } else {
             this.getEmergency().withdrawUnit(this);
             this.setEmergency(null);
 			setUnitStatus(UnitStatus.IDLE);
         }
     }
 
     /**
      * Checks if the unit can be withdrawn.
      * @return True if the unit is assigned to an emergency and was alread at the site of the emergency, otherwise false.
      */
     public boolean canBeWithdrawn() {
         return (this.isAssigned() && !this.wasAlreadyAtSite());
     }
 
     /**
      * Calculates the estimated time of arrival to a location.
      * @param location
      *		The given location.
      * @return The number of seconds this unit would use to reach the given location.
      */
     public long getETA(GPSCoordinate location) {
         return Math.round(3600 * this.getDistanceTo(location) / this.getSpeed());
     }
 
     /**
      * Calculates the distance of this unit to the given location.
      * @param location
      *		The location to calculate the distance to.
      * @return The distance between this unit and the given location.
      */
     public double getDistanceTo(GPSCoordinate location) {
         return this.getCurrentLocation().getDistanceTo(location);
     }
 
     /**
      * Finishes the job of this Unit.
      * @effect The emergency of this unit is null
      *		| this.getEmergency().equals(null)
      * @effect The flag wasAlreadyAtSite is set to false.
      *		|this.getWasAlreadyAtSite().equals(false)
      * @throws InvalidFinishJobException
      *          If the unit can't finish his job (not assigned to an emergency, not at it's destination).
      * @throws InvalidEmergencyStatusException
      *          If the status of the emergency where this unit is assigned to, does not allow units to finish their job.
      */
     public void finishedJob() throws InvalidEmergencyStatusException, InvalidFinishJobException {
         if (!canFinishJob()) {
             throw new InvalidFinishJobException("Unit can't finish his job.");
         } else {
             getEmergency().finishUnit(this);
             setEmergency(null);
             setWasAlreadyAtSite(false);
 			setUnitStatus(UnitStatus.IDLE);
         }
     }
 
     /**
      * Checks if the unit can finish from it's job.
      * @return True if the unit is assigned and is at it's destination, orherwise false.
      */
     public boolean canFinishJob() {
         return (isAssigned() && isAtDestination());
     }
 
     /**
      * A virtual method that will be called by the system when the unit is arrived at his location.
      * @note This method can be called from two methods: changeLocation(long) and setTarget(Targetable).
      * @note If this method is called from the changeLocation method, it will not call this method anymore unit it has a new target.
      * @see #setTarget(projectswop20102011.domain.Targetable)
      * @see #changeLocation(long) 
      */
     protected void arrivedAtDestination() {
     }
 
     /**
      * Returns the target where the Unit is driving to.
      * @return the target where the Unit is driving to.
      */
     private Targetable getTarget() {
         return this.target;
     }
 
     /**
      * Sets the target where the Unit will drive to.
      * @param target The new target of the Unit.
      */
     protected void setTarget(Targetable target) throws InvalidTargetableException {
         if (!isValidTarget(target)) {
             throw new InvalidTargetableException("The target must be effective.");
         }
         this.target = target;
         checkArrivedAtDestination();
     }
 
     /**
      * Checks if the given target is valid.
      * @param target The target to check for.
      * @return True if the target is effective, otherwise false.
      */
     public boolean isValidTarget(Targetable target) {
         return (target != null);
     }
 
     /**
      * Checks if the Unit arrived at it's Destination and if so, calls the arrivedAtDestination method.
      * @see #arrivedAtDestination() 
      */
     private void checkArrivedAtDestination() {
         if (this.isAtDestination()) {
             this.arrivedAtDestination();
         }
     }
 }
