 package model;
 
 import java.io.Serializable;
 import java.util.Observable;
 import java.util.Set;
 import java.util.TreeSet;
 
 @SuppressWarnings("serial")
 public class ConcreteProductContainerManager extends Observable implements Serializable,
 		ProductContainerManager {
 	private Set<StorageUnit> rootStorageUnits;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public ConcreteProductContainerManager() {
 		rootStorageUnits = new TreeSet<StorageUnit>();
 	}
 
 	/**
 	 * Determines whether the specified Storage Unit name is valid for adding a new Storage
 	 * Unit.
 	 * 
 	 * @param name
 	 *            The name to be tested
 	 * @return true if name is valid, false otherwise
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean isValidStorageUnitName(String name) {
 		if (name == null || name.equals(""))
 			return false;
 
 		// From the Data Dictionary: Must be non-empty. Must be unique among all
 		// Storage Units.
 
 		for (StorageUnit su : rootStorageUnits) {
			if (name.equals(su.name.toString()))
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * If pc is a StorageUnit, it is added to the list of StorageUnits managed. Notifies
 	 * observers of a change.
 	 * 
 	 * @param pc
 	 *            ProductContainer to be managed
 	 * 
 	 * @pre pc != null
 	 * @post if(pc instanceof StorageUnit) rootStorageUnits.contains(pc)
 	 */
 	@Override
 	public void manage(ProductContainer pc) {
 		if (pc instanceof StorageUnit)
 			rootStorageUnits.add((StorageUnit) pc);
 		setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * Remove pc from set of managed objects and notify observers of a change.
 	 * 
 	 * @param pc
 	 *            ProductContainer to be unmanaged
 	 * 
 	 * @pre true
 	 * @post !rootStorageUnits.contains(pc)
 	 */
 	@Override
 	public void unmanage(ProductContainer pc) {
 		if (pc instanceof StorageUnit)
 			rootStorageUnits.remove(pc);
 		setChanged();
 		this.notifyObservers();
 	}
 }
