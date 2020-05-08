 package projectswop20102011.externalsystem.adapters;
 
 import be.kuleuven.cs.swop.api.EmergencyState;
 import be.kuleuven.cs.swop.api.IEmergency;
 import be.kuleuven.cs.swop.api.IUnit;
 import java.util.ArrayList;
 import java.util.List;
 import projectswop20102011.domain.Emergency;
 import projectswop20102011.domain.EmergencyStatus;
 import projectswop20102011.domain.Unit;
 
 /**
  * An adapter that implements an IEmergency and offers operations to inspect the state of an emergency.
  * @author Willem Van Onsem, Jonas Vanthornhout & Pieter-Jan Vuylsteke
  */
 public class EmergencyAdapter implements IEmergency {
 
 	/**
 	 * The emergency of this EmergencyAdapter.
 	 */
 	private final Emergency emergency;
 
 	/**
 	 * Creates an EmergencyAdapter from the given Emergency.
 	 * @param emergency
 	 *		The Emergency that is used to create an EmergencyAdapter.
 	 */
 	public EmergencyAdapter(Emergency emergency) {
 		this.emergency = emergency;
 	}
 
 	/**
 	 * Returns the Emergency of this EmergencyAdapter.
 	 * @return The Emergency of this EmergencyAdapter.
 	 */
 	public Emergency getEmergency() {
 		return emergency;
 	}
 
 	/**
 	 * Retrieves the current state of the Emergency.
 	 * @return The state of the Emergency.
 	 */
 	@Override
 	public EmergencyState getState() {
 		EmergencyStatus status = getEmergency().getStatus();
 		if (status.toString().equalsIgnoreCase("recorded but unhandled")) {
 			return EmergencyState.UNHANDLED;
 		} else if (status.toString().equalsIgnoreCase("response in progress")) {
 			return EmergencyState.RESPONDED;
 		} else {
 			return EmergencyState.COMPLETED;
 		}
 	}
 
 	/**
 	 * Retrieves the list of assigned units.
 	 * @return The list of assigned units.
 	 * @note The list is not expected in a specific order.
 	 */
 	@Override
 	public List<IUnit> getAssignedUnits() {
 		List<Unit> units = getEmergency().getWorkingUnits();
 		List<IUnit> iUnits = new ArrayList<IUnit>();
 		for (Unit u : units) {
			iUnits.add(new UnitAdapter(u));
 		}
 
 		return iUnits;
 	}
 
 	/**
 	 * Indicates whether this emergency is part of a disaster or not.
 	 * @return True if the emergency is part of a disaster; false otherwise.
 	 */
 	@Override
 	public boolean isPartOfDisaster() {
 		return getEmergency().isPartOfADisaster();
 	}
 }
