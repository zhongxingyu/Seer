 package org.openmrs.module.addresshierarchy.service;
 
 import java.util.List;
 
 import org.openmrs.PersonAddress;
 import org.openmrs.annotation.Authorized;
 import org.openmrs.module.addresshierarchy.AddressField;
 import org.openmrs.module.addresshierarchy.AddressHierarchyConstants;
 import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
 import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
 
 /**
  * The Interface AddressHierarchyService has the service methods for AddressHierarchy module.
  */
 public interface AddressHierarchyService{
 	
 	/**
 	 * Given a person address, returns the names of all entries that are hierarchically valid for the
 	 * specified addressField.  (Excluding duplicate names and ignoring any current value of the specified addressField)
 	 * 
 	 *  This method can handle restrictions based on address field values not only above but also *below* the specified level.
 	 * (For instance, if the city is set to "Boston", and we ask for possible values for the "state" level,
 	 *  only Massachusetts should be returned) 
 	 * 
 	 * @param address
 	 * @param fieldName
	 * @return a list of the names of the possible valid address hierarchy entries; returns an empty list if no matches, should return null only if error
 	 */
 	public List<String> getPossibleAddressValues(PersonAddress address, String fieldName);
 	
 	/**
 	 * Given a person address, returns the names of all entries that are hierarchically valid for the
 	 * specified addressField.  (Excluding duplicate names and ignoring any current value of the specified addressField)
 	 * 
 	 *  This method can handle restrictions based on address field values not only above but also *below* the specified level.
 	 * (For instance, if the city is set to "Boston", and we ask for possible values for the "state" level,
 	 *  only Massachusetts should be returned) 
 	 * 
 	 * @param address
 	 * @param field
	 * @return a list of the names of the possible valid address hierarchy entries; returns an empty list if no matches, should return null only if error
 	 */
 	public List<String> getPossibleAddressValues(PersonAddress address, AddressField field);
 	
 	/**
 	 * Given a person address, returns all the address hierarchy entries that are hierarchically valid for the
 	 * specified level.  (Ignoring any current value of the addressField associated with the specified level).
 	 * 
 	 * This method can handle restrictions based on address field values not only above but also *below* the specified level.
 	 * (For instance, if the city is set to "Boston", and we ask for possible values for the "state" level,
 	 *  only Massachusetts should be returned) 
 	 * 
 	 * @param address
 	 * @param level
	 * @return a list of possible valid address hierarchy entries; returns an empty list if no matches, should return null only if error
 	 */
 	public List<AddressHierarchyEntry> getPossibleAddressHierarchyEntries(PersonAddress address, AddressHierarchyLevel level);
 	
 	/**
 	 * Returns a count of the total number of address hierarchy entries
 	 * 
 	 * @return the number of address hierarchy entries
 	 */
 	public Integer getAddressHierarchyEntryCount();
 	
 	/**
 	 * Returns a count of the total number of address hierarchy entries associated with the given level
 	 * 
 	 * @param level
 	 * @return the number of address hierarchy entries associated with the given level
 	 */
 	public Integer getAddressHierarchyEntryCountByLevel(AddressHierarchyLevel level);
 	
 	/**
 	 * Returns the address hierarchy entry with the given id
 	 * 
 	 * @param addressHierarchyEntryId
 	 * @return the address hierarchy entry with the given id
 	 */
 	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyEntryId);
 	
 	/**
 	 * Returns the address hierarchy entry with the given user generated id
 	 * 
 	 * @param userGeneratedId
 	 * @return the address hierarchy entry with the given user generated id
 	 */
 	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId);
 	
 	/**
 	 * Returns all address hierarchy entries at with the given level 
 	 * 
 	 * @param level
 	 * @return a list of all address hierarchy entries at the given level
 	 */
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevel(AddressHierarchyLevel level);
 	
 	/**
 	 * Returns all address hierarchy entries at the given level that have the specified name 
 	 * (name match is case-insensitive)
 	 *
 	 * @param level
 	 * @param name
 	 * @return a list of all address hierarchy entries at the given level that have the specified name
 	 */
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel level, String name);
 	
 	/**
 	 * Returns all address hierarchy entries at the top level in the hierarchy
 	 * 
 	 * @return a list of all the address hierarchy entries at the top level of the hierarchy
 	 */
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesAtTopLevel();
 	
 	/**
 	 * Returns all address hierarchy entries that are children of the specified entry
 	 * (If no entry specified, returns all the entries at the top level)
 	 * 
 	 * @param entry
 	 * @return a list of all the address hierarchy entries that are children of the specified entry
 	 */
 	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry);
 	
 	/**
 	 * Returns all address hierarchy entries that are child of the entry with the given id
 	 * 
 	 * @param entryId
 	 * @return a list of all 
 	 */
 	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(Integer entryId);
 	
 	/**
 	 * Returns the address hierarchy entry that is the child entry of the
 	 * specified entry and have the specified name (case-insensitive)
 	 * (If no entry specified, tests against all entries at the top level)
 	 * (Throws an exception if there is only one match, because there should
 	 * be no two entries with the same parent and name)
 	 * 
 	 * @param entry
 	 * @param name
 	 * @return the entry with the specified parent and name
 	 */
 	public AddressHierarchyEntry getChildAddressHierarchyEntryByName(AddressHierarchyEntry entry, String childName);
 	
 	/**
 	 * Saves the specified address hierarchy entry
 	 * 
 	 * @param entry
 	 */
 	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
 	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry);
 	
 	/**
 	 * Saves a block of address hierarchy entries within a single transaction
 	 * (This should be used with care since the save interceptors may not be
 	 * used properly. This method is mainly used to speed up the performance
 	 * of importing a hierarchy from a file)
 	 * 
 	 * @param entries
 	 */
 	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
 	public void saveAddressHierarchyEntries(List<AddressHierarchyEntry> entries);
 	
 	/**
 	 * Removes all address hierarchy entries--use with care
 	 */
 	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
 	public void deleteAllAddressHierarchyEntries();
 	
 	/**
 	 * Gets all address hierarchy levels, ordered from the top of hierarchy to the bottom
 	 * 
 	 * @return the ordered list of address hierarchy levels
 	 */
 	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels();
 	
 	/**
 	 * Gets the address hierarchy levels, ordered from the top the hierarchy to the bottom
 	 * 
 	 * @param includeUnmapped specifies whether or not to include hierarchy levels that aren't mapped to an underlying address field
 	 * @return the ordered list of address hierarchy levels
 	 */
 	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels(Boolean includeUnmapped);
 	
 	/**
 	 * Gets all address hierarchy levels
 	 * 
 	 * @return a list of all address hierarchy levels
 	 */
 	public List<AddressHierarchyLevel> getAddressHierarchyLevels();
 	
 	/**
 	 * Gets a count of the number of address hierarchy levels
 	 * 
 	 * @return the number of address hierarchy levels
 	 */
 	public Integer getAddressHierarchyLevelsCount();
 	
 	/**
 	 * Gets the address hierarchy level that represents the top level of the hierarchy
 	 * 
 	 * @return the address hierarchy level at the top level of the hierarchy
 	 */
 	public AddressHierarchyLevel getTopAddressHierarchyLevel();
 	
 	/**
 	 * Gets the address hierarchy level that represents the lowest level of the hierarchy
 	 * 
 	 * @return the address hierarchy level at the lowest level of the hierarchy
 	 */
 	public AddressHierarchyLevel getBottomAddressHierarchyLevel();
 	
 	/**
 	 * Gets an AddressHierarchyLevel by id
 	 * 
 	 * @param levelId
 	 * @return the address hierarchy level with the given id
 	 */
 	public AddressHierarchyLevel getAddressHierarchyLevel(Integer levelId);
 	
 	/**
 	 * Finds the child AddressHierarchyLevel of the given AddressHierarchyLevel
 	 * 
 	 * @param level
 	 * @return the address hierarchy level that is the child of the given level
 	 */
 	public AddressHierarchyLevel getChildAddressHierarchyLevel(AddressHierarchyLevel level);
 	
 	/**
 	 * Adds (and saves) a new AddressHierarchyLevel at the bottom of the hierarchy
 	 * 
 	 * @return the new address hierarchy level
 	 */
 	public AddressHierarchyLevel addAddressHierarchyLevel();
 	
 	/**
 	 * Saves an AddressHierarchyLevel
 	 * 
 	 * @param the level to save
 	 */
 	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
 	public void saveAddressHierarchyLevel(AddressHierarchyLevel level);
 	
 	/**
 	 * Deletes an AddressHierarchyLevel
 	 * 
 	 * @param the level to delete
 	 */
 	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
 	public void deleteAddressHierarchyLevel(AddressHierarchyLevel level);
 	
 	/**
 	 * Attempt to determine the hierarchy of address hierarchy levels based on the hierarchy of entries
 	 * and assign the parent levels accordingly (used as part of 1.2 to 1.6 migration)
 	 */
 	public void setAddressHierarchyLevelParents();
 	
 	/**
 	 * The following methods are deprecated and just exist to provide backwards compatibility to
 	 * Rwanda Address Hierarchy module
 	 */
 	
 	@Deprecated
 	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah);
 	
 	@Deprecated
 	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude);
 	
 	@Deprecated
 	public List<AddressHierarchyEntry> getTopOfHierarchyList();
 	
 	@Deprecated
 	public void initializeRwandaHierarchyTables();
 	
 	@Deprecated
 	public List<Object[]> findUnstructuredAddresses(int page, int locationId);
 	
 	@Deprecated
 	public List<Object[]> getLocationAddressBreakdown(int locationId);
 	
 	@Deprecated
 	public List<Object[]> getAllAddresses(int page);
 	
 	/**
 	 * I've renamed the following methods to make them a little more clear, but kept the old methods
 	 * for backwards compatibility
 	 */
 	
 	@Deprecated
 	public int getAddressHierarchyCount();
 	
 	@Deprecated
 	public List<AddressHierarchyEntry> getNextComponent(Integer locationId);
 	
 	@Deprecated
 	public AddressHierarchyEntry getAddressHierarchy(int addressHierarchyId);
 	
 	@Deprecated
 	@Authorized( { AddressHierarchyConstants.PRIV_MANAGE_ADDRESS_HIERARCHY })
 	public void saveAddressHierarchy(AddressHierarchyEntry ah);
 	
 	
 	@Deprecated
 	public AddressHierarchyEntry getLocationFromUserGenId(String userGeneratedId);
 	
 	@Deprecated
 	public AddressHierarchyLevel getHierarchyType(int levelId);;
 	
 }
