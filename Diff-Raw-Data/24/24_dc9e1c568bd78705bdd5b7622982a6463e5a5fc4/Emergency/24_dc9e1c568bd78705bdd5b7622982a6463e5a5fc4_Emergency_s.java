 package projectswop20102011.domain;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import projectswop20102011.domain.validators.DispatchUnitsConstraint;
 import projectswop20102011.exceptions.InvalidEmergencySeverityException;
 import projectswop20102011.exceptions.InvalidEmergencyStatusException;
 import projectswop20102011.exceptions.InvalidLocationException;
 
 /**
  * A class that represents an emergency.
  *
  * @author Willem Van Onsem, Jonas Vanthornhout & Pieter-Jan Vuylsteke
  * @invar The location of an emergency is always valid.
  *		| isValidLocation(getLocation())
  * @invar The severity of an emergency is always valid.
  *		| isValidSeverity(getSeverity())
  * @invar The status of an emergency is always valid.
  *		| isValidStatus(getStatus())
  * @invar Every emergency has a unique id.
  */
 public abstract class Emergency {
 
 	/**
 	 * A static field that generates id's for the constructed emergencies,
 	 * and ensures that every emergency has a different id.
 	 */
 	private static long idDispatcher = 0;
 	/**
 	 * A variable registering the id of this emergency.
 	 */
 	private final long id;
 	/**
 	 * A variable registering the location of this emergency.
 	 */
 	private GPSCoordinate location;
 	/**
 	 * A variable registering the severity of this emergency.
 	 */
 	private EmergencySeverity severity;
 	/**
 	 * A variable registering the status of this emergency.
 	 */
 	private EmergencyStatus status;
 	/**
 	 * A variable registering the units needed to handle the emergency
 	 * and does the management of dispatching units and setting the status of this emergency.
 	 */
 	private UnitsNeeded unitsNeeded;
 	/**
 	 * A variable registering the description of the emergency.
 	 */
 	private final String description;
 
 	/**
 	 * Make a new emergency with the given location, severity and description.
 	 *
 	 * @param location
 	 *		The location of this emergency.
 	 * @param severity
 	 *		The severity of this emergency.
 	 * @param description
 	 *		The description of this emergency.
 	 * @throws InvalidLocationException
 	 *		If the given location is an invalid location for an emergency.
 	 * @throws InvalidEmergencySeverityException
 	 *		If the given severity is an invalid severity for an emergency.
 	 * @effect This location is equal to the parameter location.
 	 *		|location.equals(this.getLocation())
 	 * @effect This severity is equal to the parameter severity.
 	 *		|severity.equals(this.getSeverity())
 	 * @effect This status is equal to the EmergencyStatus RECORDED_BUT_UNHANDLED.
 	 *		|getStatus().equals(EmergencyStatus.RECORDED_BUT_UNHANDLED)
 	 * @post This description is equal to the given description.
 	 *		|new.getDescription().equals(description)
 	 */
 	protected Emergency(GPSCoordinate location, EmergencySeverity severity, String description) throws InvalidLocationException, InvalidEmergencySeverityException {
 		this.id = idDispatcher++;
 		setLocation(location);
 		setSeverity(severity);
 		this.description = description;
 		try {
 			setStatus(EmergencyStatus.RECORDED_BUT_UNHANDLED);
 		} catch (InvalidEmergencyStatusException ex) {
 			//Can't be thrown: We ensure that the status is valid.
 			Logger.getLogger(Emergency.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	/**
 	 * Sets the location of this emergency.
 	 * @param location
 	 *		The location of this emergency.
 	 * @throws InvalidLocationException
 	 *		If the given location isn't valid for an emergency.
 	 * @post This location is equal to the given location.
 	 *		| location.equals(getLocation())
 	 */
 	private void setLocation(GPSCoordinate location) throws InvalidLocationException {
 		if (!isValidLocation(location)) {
 			throw new InvalidLocationException(String.format("\"%s\" is an invalid location for an emergency.", location));
 		}
 		this.location = location;
 	}
 
 	/**
 	 * Sets the severity of this emergency.
 	 * @param severity
 	 *		The severity of this emergency.
 	 * @throws InvalidEmergencySeverityException
 	 *		If the given severity level is invalid for an emergency.
 	 * @post This severity level is equal to the given severity level.
 	 *		| severity.equals(getSeverity())
 	 */
 	private void setSeverity(EmergencySeverity severity) throws InvalidEmergencySeverityException {
 		if (!isValidSeverity(severity)) {
 			throw new InvalidEmergencySeverityException(String.format("\"%s\" is an invalid location for an emergency.", severity));
 		}
 		this.severity = severity;
 	}
 
 	/**
 	 * Sets the status of this emergency.
 	 * @param status
 	 *		The status of this emergency.
 	 * @throws InvalidEmergencyStatusException
 	 *		If the given status is invalid for an emergency.
 	 * @post This status is equal to the given status.
 	 *		| status.equals(getStatus())
 	 */
 	void setStatus(EmergencyStatus status) throws InvalidEmergencyStatusException {
 		if (!isValidStatus(status)) {
 			throw new InvalidEmergencyStatusException(String.format("\"%s\" is an invalid status for an emergency.", status));
 		}
 		this.status = status;
 	}
 
 	/**
 	 * Returns the location of this emergency.
 	 * @return The location of this emergency.
 	 */
 	public GPSCoordinate getLocation() {
 		return location;
 	}
 
 	/**
 	 * Returns the severity of this emergency.
 	 * @return The severity of this emergency.
 	 */
 	public EmergencySeverity getSeverity() {
 		return severity;
 	}
 
 	/**
 	 * Returns the status of this emergency.
 	 * @return The status of this emergency.
 	 */
 	public EmergencyStatus getStatus() {
 		return status;
 	}
 
 	/**
 	 * Returns the id of this emergency.
 	 * @return The id of this emergency.
 	 */
 	public long getId() {
 		return id;
 	}
 
 	/**
 	 * Returns the description of this emergency.
 	 * @return The description of this emergency.
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Checks if the given severity is valid as severity level of an emergency.
 	 * @param severity
 	 *		The severity level to check.
 	 * @return True if the severity level is effective, otherwise false.
 	 */
 	public static boolean isValidSeverity(EmergencySeverity severity) {
 		return (severity != null);
 	}
 
 	/**
 	 * Checks if the given status is a valid status of an emergency.
 	 * @param status
 	 *		The status to check.
 	 * @return True if the status is effective, otherwise false.
 	 */
 	public static boolean isValidStatus(EmergencyStatus status) {
 		return (status != null);
 	}
 
 	/**
 	 * Checks if the given location is a valid location for an emergency.
 	 * @param location
 	 *		The location to test.
 	 * @return True if the location is effective, otherwise false.
 	 */
 	public static boolean isValidLocation(GPSCoordinate location) {
 		return (location != null);
 	}
 
 	/**
 	 * Calculates the units needed for this Emergency.
 	 * @return The units needed for this Emergency.
 	 */
 	protected abstract UnitsNeeded calculateUnitsNeeded();
 
 	/**
 	 * Returns a UnitsNeeded structure that contains the units needed for this emergency.
 	 * @return A UnitsNeeded structure that contains the units needed for this emergency.
 	 * @note Handling dispatching and updating the status of the emergency is also done by this object.
 	 * @note The visibility of this method is package. No classes outside the domain have access to the UnitsNeeded object.
 	 */
 	private synchronized UnitsNeeded getUnitsNeeded() {
 		if (this.unitsNeeded == null) {
 			this.unitsNeeded = calculateUnitsNeeded();
 		}
 		return this.unitsNeeded;
 	}
 
 	/**
 	 * Assigning units to this emergency.
 	 * @param units
 	 *      A list of units to assign.
 	 * @throws InvalidEmergencyStatusException
 	 *      If the status of this emergency does not allow this action.
 	 * @throws Exception
 	 *      If another exception is thrown by performing this operation (for instance those units are already assigned).
 	 */
 	public void assignUnits(List<Unit> units) throws InvalidEmergencyStatusException, Exception {
		this.getStatus().assignUnits(this, units);
 	}
 
 	/**
 	 * Enable a unit to finish his job for this emergency.
 	 * @param unitToFinish
 	 *      The unit that wants to finish this emergency.
 	 * @throws InvalidEmergencyStatusException
 	 *      If the status of this emergency does not allow this action.
 	 * @throws Exception
 	 *      If another exception is thrown by performing this operation.
 	 * @note this method has a package visibility: Units need to finish on their own and call this method to register this to the emergency.
 	 */
 	void finishUnit(Unit unitToFinish) throws InvalidEmergencyStatusException, Exception {
		this.getStatus().finishUnit(this, unitToFinish);
 	}
 
 	/**
 	 * Withdraws a unit from this emergency.
 	 * @param unitToWithdraw
 	 *      The unit that wants to withdraw from this emergency.
 	 * @throws InvalidEmergencyStatusException
 	 *      If the status of this emergency does not allow this action.
 	 * @throws Exception
 	 *      If another exception is thrown by performing this operation.
 	 * @note this method has a package visibility: Units need to call withdraw and call this method to register this to the emergency.
 	 */
 	void withdrawUnit(Unit unitToWithdraw) throws InvalidEmergencyStatusException, Exception {
		this.getStatus().withdrawUnit(this, unitToWithdraw);
 	}
 
 	/**
 	 * Returns a hashtable that contains the information of this emergency.
 	 * This hashtable contains the id, location, severity, status and the working units.
 	 * @return A hashtable that contains the information of this emergency.
 	 */
 	public Hashtable<String, String> getShortInformation() {
 		return getInformation();
 	}
 
 	/**
 	 * Returns a hashtable that contains the information of this emergency.
 	 * This hashtable contains the id, type, location, severity, status, the working units and specific elements of the child of this emergency.
 	 * @return A hashtable that contains the information of this emergency.
 	 */
 	public abstract Hashtable<String, String> getLongInformation();
 
 	/**
 	 * Returns a hashtable that contains the information of this emergency.
 	 * This hashtable contains the id, location, severity, status and the working units.
 	 * @return A hashtable that contains the information of this emergency.
 	 */
 	protected Hashtable<String, String> getInformation() {
 		Hashtable<String, String> information = new Hashtable<String, String>();
 
 		information.put("id", "" + getId());
 		information.put("type", this.getClass().getSimpleName());
 		information.put("location", getLocation().toString());
 		information.put("severity", getSeverity().getTextual());
 		information.put("status", getStatus().toString());
 		ArrayList<Unit> units = this.getWorkingUnits();
 		int number = units.size();
 		String workingUnits = "[ ";
 		if (number > 0) {
 			for (int i = 0; i < number - 1; i++) {
 				workingUnits += units.get(i).getName() + ", ";
 			}
 			workingUnits += units.get(number - 1).getName();
 		}
 		workingUnits += " ]";
 		information.put("working units", workingUnits);
 
 		return information;
 	}
 
 	/**
 	 * Returns a list of units currently working at this emergency.
 	 * @return a list of units currently working at this emergency.
 	 */
 	public ArrayList<Unit> getWorkingUnits() {
 		return this.getUnitsNeeded().getWorkingUnits();
 	}
 
 	/**
 	 * Checks if the given unit can assign the given list of units to the emergency.
 	 * @param units
 	 *      A list of units to check.
 	 * @return True if the given list of units can be assigned, otherwise false.
 	 */
 	public boolean canAssignUnits(ArrayList<Unit> units) {
		return this.getStatus().canAssignUnits(this, units);
 	}
 
 	/**
 	 * Returns a proposal generated by the policy of this constraint.
 	 * @param availableUnits
 	 *      A list of units that are available for the proposal.
 	 * @return A list of units proposed by the policy of this constraint.
 	 */
 	public Collection<Unit> getPolicyProposal(List<? extends Unit> availableUnits) {
		return this.getStatus().getPolicyProposal(this, availableUnits);
 	}
 
 	/**
 	 * Returns the policy for allocation used by this emergency.
 	 * @return The policy for allocation used by this emergency.
 	 */
 	public DispatchPolicy getDispatchPolicy() {
 		return this.getUnitsNeeded().getPolicy();
 	}
 
 	/**
 	 * Returns the constraint representing what conditions need to be met before the emergency can be finished.
 	 * @return A DispatchUnitsConstraint representing the constraints to finish an emergency.
 	 */
 	public DispatchUnitsConstraint getDispatchConstraint() {
 		return this.getUnitsNeeded().getConstraint();
 	}
 
         /**
          * Cecks if this emergency can be resolved with a given collection of all available units.
          * @param availableUnits All the available units in the world.
          * @return True if the given emergency can be resolved, otherwise false.
          */
         public boolean canBeResolved (Collection<Unit> availableUnits) {
            return this.getStatus().canBeResolved(this, availableUnits);
         }
 }
