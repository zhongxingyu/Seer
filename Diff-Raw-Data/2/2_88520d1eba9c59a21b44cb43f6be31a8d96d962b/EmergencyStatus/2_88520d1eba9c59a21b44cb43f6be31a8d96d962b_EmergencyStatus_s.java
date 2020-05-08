 package projectswop20102011.domain;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import projectswop20102011.exceptions.InvalidEmergencyException;
 import projectswop20102011.exceptions.InvalidEmergencyStatusException;
 
 /**
  * An enumeration that represents the status of an emergency.
  * @author Willem Van Onsem, Jonas Vanthornhout & Pieter-Jan Vuylsteke
  */
 public enum EmergencyStatus {
 
 	/**
 	 * A state where the emergency is recorded by the operator but not yet handled by the dispatcher.
 	 */
 	RECORDED_BUT_UNHANDLED("recorded but unhandled") {
 
 		@Override
 		void assignUnits(UnitsNeeded unitsNeeded, Set<Unit> units) throws InvalidEmergencyException {
 			unitsNeeded.assignUnitsToEmergency(units);
 			try {
 				unitsNeeded.getEmergency().setStatus(EmergencyStatus.RESPONSE_IN_PROGRESS);
 			} catch (InvalidEmergencyStatusException ex) {
 				//We assume this can't happen.
 				Logger.getLogger(EmergencyStatus.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 
 		@Override
 		void finishUnit(UnitsNeeded unitsNeeded, Unit unit) throws InvalidEmergencyStatusException {
 			throw new InvalidEmergencyStatusException("Can't finish units from an unhandled emergency.");
 		}
 
 		@Override
 		void withdrawUnit(UnitsNeeded unitsNeeded, Unit unit) throws InvalidEmergencyStatusException {
 			throw new InvalidEmergencyStatusException("Can't withdraw units from an unhandled emergency.");
 		}
 
 		@Override
 		boolean canAssignUnits(UnitsNeeded unitsNeeded, Set<Unit> unit) {
 			return unitsNeeded.canAssignUnitsToEmergency(unit);
 		}
 
 		@Override
 		Set<Unit> getPolicyProposal(UnitsNeeded unitsNeeded, List<? extends Unit> availableUnits) {
 			return unitsNeeded.getPolicyProposal(availableUnits);
 		}
 
 		@Override
 		boolean canBeResolved(UnitsNeeded unitsNeeded, Collection<Unit> availableUnits) {
 			return unitsNeeded.canBeResolved(availableUnits);
 		}
 	},
 	/**
 	 * A state of an emergency where the dispatcher has already sent units (this does not mean there are still units working or already finished).
 	 */
 	RESPONSE_IN_PROGRESS("response in progress") {
 
 		@Override
 		void assignUnits(UnitsNeeded unitsNeeded, Set<Unit> units) throws InvalidEmergencyException {
 			unitsNeeded.assignUnitsToEmergency(units);
 		}
 
 		@Override
 		void finishUnit(UnitsNeeded unitsNeeded, Unit unit) {
 			unitsNeeded.unitFinishedJob(unit);
			if (unitsNeeded.canFinish()) {
 				try {
 					unitsNeeded.getEmergency().setStatus(COMPLETED);
 				} catch (InvalidEmergencyStatusException ex) {
 					//We assume this can't happen
 					Logger.getLogger(EmergencyStatus.class.getName()).log(Level.SEVERE, null, ex);
 				}
 			}
 		}
 
 		@Override
 		void withdrawUnit(UnitsNeeded unitsNeeded, Unit unit) {
 			unitsNeeded.withdrawUnit(unit);
 		}
 
 		@Override
 		boolean canAssignUnits(UnitsNeeded unitsNeeded, Set<Unit> units) {
 			return unitsNeeded.canAssignUnitsToEmergency(units);
 		}
 
 		@Override
 		Set<Unit> getPolicyProposal(UnitsNeeded unitsNeeded, List<? extends Unit> availableUnits) {
 			return unitsNeeded.getPolicyProposal(availableUnits);
 		}
 
 		@Override
 		boolean canBeResolved(UnitsNeeded unitsNeeded, Collection<Unit> availableUnits) {
 			return unitsNeeded.canBeResolved(availableUnits);
 		}
 	},
 	/**
 	 * A state of an emergency where the emergency has been completly handled. All the units needed for this emergency have finished.
 	 */
 	COMPLETED("completed") {
 
 		@Override
 		void assignUnits(UnitsNeeded unitsNeeded, Set<Unit> units) throws InvalidEmergencyStatusException {
 			throw new InvalidEmergencyStatusException("Unable to assign units to a completed emergency.");
 		}
 
 		@Override
 		void finishUnit(UnitsNeeded unitsNeeded, Unit unit) throws InvalidEmergencyStatusException {
 			throw new InvalidEmergencyStatusException("Unable to finish units from a completed emergency.");
 		}
 
 		@Override
 		void withdrawUnit(UnitsNeeded unitsNeeded, Unit unit) throws InvalidEmergencyStatusException {
 			throw new InvalidEmergencyStatusException("Unable to withdraw units from a competed emergency.");
 		}
 
 		@Override
 		boolean canAssignUnits(UnitsNeeded unitsNeeded, Set<Unit> units) {
 			return false;
 		}
 
 		@Override
 		Set<Unit> getPolicyProposal(UnitsNeeded unitsNeeded, List<? extends Unit> availableUnits) {
 			return new HashSet<Unit>();//a proposal containing no units
 		}
 
 		@Override
 		boolean canBeResolved(UnitsNeeded unitsNeeded, Collection<Unit> availableUnits) {
 			return true;
 		}
 	};
 	/**
 	 * The textual representation of an EmergencyStatus.
 	 */
 	private final String textual;
 
 	/**
 	 * Creates a new instance of the EmergencyStatus class with a given textual representation.
 	 * @param textual
 	 *		The textual representation of the EmergencyStatus, used for parsing and user interaction.
 	 * @post The textual representation is set to the given textual representation.
 	 *		| new.toString().equals(textual)
 	 */
 	private EmergencyStatus(String textual) {
 		this.textual = textual;
 	}
 
 	/**
 	 * Returns the textual representation of the EmergencyStatus.
 	 * @return A textual representation of the EmergencyStatus.
 	 */
 	@Override
 	public String toString() {
 		return textual;
 	}
 
 	/**
 	 * Tests if a given textual representation of an EmergencyStatus matches this EmergencyStatus.
 	 * @param textualRepresentation
 	 *		The textual representation to test.
 	 * @return True if the textual representation matches, otherwise false.
 	 */
 	public boolean matches(String textualRepresentation) {
 		return this.toString().equals(textualRepresentation.toLowerCase());
 	}
 
 	/**
 	 * Parses a textual representation into its EmergencyStatus equivalent.
 	 * @param textualRepresentation
 	 *		The textual representation to parse.
 	 * @return An EmergencyStatus that is the equivalent of the textual representation.
 	 * @throws InvalidEmergencyStatusException
 	 *		If no EmergencyStatus matches the textual representation.
 	 */
 	public static EmergencyStatus parse(String textualRepresentation) throws InvalidEmergencyStatusException {
 		for (EmergencyStatus es : EmergencyStatus.values()) {
 			if (es.matches(textualRepresentation)) {
 				return es;
 			}
 		}
 		throw new InvalidEmergencyStatusException(String.format("Unknown emergency status level \"%s\".", textualRepresentation));
 	}
 
 	/**
 	 * A method representing a potential transition where units are allocated to the emergency.
 	 * @param unitsNeeded
 	 *      The unitsNeeded object of the emergency where the action takes place.
 	 * @param units
 	 *      The units to allocate to the emergency.
 	 * @throws InvalidEmergencyStatusException
 	 *      If the status of the emergency is invalid.
 	 * @throws Exception
 	 *      If another exception is thrown.
 	 * @note This method has a package visibility: Only the emergency class can call this method.
 	 */
 	abstract void assignUnits(UnitsNeeded unitsNeeded, Set<Unit> units) throws InvalidEmergencyStatusException, Exception;
 
 	/**
 	 * A method representing a transition where a unit signals it has finished it's job.
 	 * @param unitsNeeded
 	 *      The unitsNeeded object of the emergency where the action takes place.
 	 * @param unit
 	 *      The unit that signals it has finished its job.
 	 * @throws InvalidEmergencyStatusException
 	 *      If the status of the emergency is invalid.
 	 * @note This method has a package visibility: Only the emergency class can call this method.
 	 */
 	abstract void finishUnit(UnitsNeeded unitsNeeded, Unit unit) throws InvalidEmergencyStatusException;
 
 	/**
 	 * A method that handles a situation where a given unit withdraws from a given emergency.
 	 * @param unitsNeeded
 	 *      The unitsNeeded object of the emergency where the action takes place.
 	 * @param unit
 	 *      The unit that withdraws from an emergency.
 	 * @throws InvalidEmergencyStatusException
 	 *      If the status of the emergency is invalid.
 	 * @note This method has a package visibility: Only the emergency class can call this method.
 	 */
 	abstract void withdrawUnit(UnitsNeeded unitsNeeded, Unit unit) throws InvalidEmergencyStatusException;
 
 	/**
 	 * A method that checks if the given units can be assigned to the given emergency.
 	 * @param unitsNeeded
 	 *      The unitsNeeded object of the emergency where the action takes place.
 	 * @param units
 	 *      A list of units to check for.
 	 * @return True if the given list of units can be assigned, otherwise false (this also includes states where no allocation can be done).
 	 */
 	abstract boolean canAssignUnits(UnitsNeeded unitsNeeded, Set<Unit> units);
 
 	/**
 	 * Gets a proposal generated by the policy of this emergency.
 	 * @param unitsNeeded
 	 *      The unitsNeeded object of the emergency where the action takes place.
 	 * @param availableUnits
 	 *      A list of available units that can be selected.
 	 * @return A set of units that represents the proposal of the policy.
 	 */
 	abstract Set<Unit> getPolicyProposal(UnitsNeeded unitsNeeded, List<? extends Unit> availableUnits);
 
 	/**
 	 * Checks if the given emergency can be resolved with a given collection of all the available units.
 	 * @param unitsNeeded
 	 *      The unitsNeeded object of the emergency where the action takes place.
 	 * @param availableUnits
 	 *		A collection of all the available units.
 	 * @return True if the given emergency can be resolved, otherwise false.
 	 */
 	abstract boolean canBeResolved(UnitsNeeded unitsNeeded, Collection<Unit> availableUnits);
 }
