 package org.openmrs.module.addresshierarchy.service;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.PersonAddress;
 import org.openmrs.annotation.Authorized;
 import org.openmrs.module.addresshierarchy.AddressField;
 import org.openmrs.module.addresshierarchy.AddressHierarchyConstants;
 import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
 import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
 import org.openmrs.module.addresshierarchy.AddressToEntryMap;
 
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
 	 * @param address the address we are testing against
 	 * @param fieldName name of the address field to look up possible values for
 	 * @return a list of the names of the possible valid address values for the specified field; returns an empty list if no matches, should return null only if error
 	 */
 	public List<String> getPossibleAddressValues(PersonAddress address, String fieldName);
 	
 	/**
 	 * Given a map of address fields to address field values, returns the names of all entries that are hierarchically valid for the
 	 * specified addressField.  (Excluding duplicate names and ignoring any current value of the specified addressField)
 	 * 
 	 *  This method can handle restrictions based on address field values not only above but also *below* the specified level.
 	 * (For instance, if the city is set to "Boston", and we ask for possible values for the "state" level,
 	 *  only Massachusetts should be returned) 
 	 * 
 	 * @param addressMap a map of address fields names to address field values
 	 * @param fieldName name of the address field to look up possible values for
 	 * @return a list of the names of the possible valid address values for the specified field; returns an empty list if no matches, should return null only if error
 	 */
 	public List<String> getPossibleAddressValues(Map<String,String> addressMap, String fieldName);
 	
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
 	 * Given an AddressHierarchyEntry, returns all the "full addresses" that contain that entry, represented as a pipe-delimited string of 
 	 * address hierarchy entry names, ordered from the entry at the highest level to the entry at the lowest level in the tree.
 	 * For example, the full address for the Beacon Hill neighborhood in the city of Boston might be:
 	 * "United States|Massachusetts|Suffolk County|Boston|Beacon Hill"
 	 * 
 	 * @param addressHierarchyEntry
 	 * @return a list of full addresses associated with that entry
 	 */
 	public List<String> getPossibleFullAddresses(AddressHierarchyEntry entry);
 	
 	/**
 	 * Given a search string, returns all the "full addresses" that match that search string
 	 * Returns a list of full addresses, represented as a pipe-delimited string of 
 	 * address hierarchy entry names, ordered from the entry at the highest level to the entry at the lowest level in the tree.
 	 * For example, the full address for the Beacon Hill neighborhood in the city of Boston might be:
 	 * "United States|Massachusetts|Suffolk County|Boston|Beacon Hill"
 	 * 
 	 * Note that if the Name Phonetics module has been installed, and the global property addresshierarchy.soundexProcessor
 	 * has been set to the name of a recognized soundex processor, this method will perform a soundex search as
 	 * opposed to a straight word search
 	 * 
 	 * @param searchString the search string to search for
 	 * @return a list of full addresses; returns an empty list if no matches
 	 */
 	public Set<String> searchAddresses(String searchString);
 	
 	/**
 	 * Given a search string, returns the name of all the entries for the specified level 
      * that match that search string
      * 
 	 * Note that if the Name Phonetics module has been installed, and the global property addresshierarchy.soundexProcessor
 	 * has been set to the name of a recognized soundex processor, this method will perform a soundex search as
 	 * opposed to a straight word search
 	 * 
 	 * If no level is specified, this method operates as if a searchAddress(String) call
      *
 	 * @param searchString the search string to search for
 	 * @param level level to search
 	 * @return a list of address hierarchy entry names; returns an empty list if no matches
 	 */
 	public Set<String> searchAddresses(String searchString, AddressHierarchyLevel level);
 	
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
 	 * @should return null if either level or name is blank or empty
 	 */
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel level, String name);
 	
 	/**
 	 * Returns all address hierarchy entries at the given level that have both the specified name and the specified parent
 	 * (name match is case-insensitive)
 	 *
 	 * @param level
 	 * @param name
 	 * @param parent
 	 * @return a list of all address hierarchy entries at the given level that have the specified name
 	 * @should return null if level, name, or parent is blank or empty
 	 */
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndNameAndParent(AddressHierarchyLevel level, String name, AddressHierarchyEntry parent);
 	
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
 	 * Gets the address hierarchy levels, ordered from the top of the hierarchy to the bottom
 	 * 
 	 * @param includeUnmapped specifies whether or not to include hierarchy levels that aren't mapped to an underlying address field
 	 * @param includeEmptyLevels specified whether or not include hierarchy levels that don't have any address hierarchy entries entries
 	 * @return the ordered list of address hierarchy levels
 	 */
	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels(Boolean includeUnmapped, Boolean includeEmptyLevels);
 	
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
 	 * Gets the AddressHierarchyLevel associated with the specified AddressField
 	 * 
 	 * @param addressField
 	 * @return the address hierarchy level associated with the specified AddressField
 	 */
 	public AddressHierarchyLevel getAddressHierarchyLevelByAddressField(AddressField addressField);
 	
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
 	 * Builds a key/value map of pipe-delimited strings that represents all the possible full addresses,
 	 * and stores this in a local cache for use by the searchAddresses(String) method
 	 * 
 	 * The map values are full addresses represented as a pipe-delimited string of address hierarchy entry names,
 	 * ordered from the entry at the highest level to the entry at the lowest level in the tree.
 	 * For example, the full address for the Beacon Hill neighborhood in the city of Boston might be:
 	 * "United States|Massachusetts|Suffolk County|Boston|Beacon Hill"
 	 * 
 	 * In the standard implemention, the keys are the same as the values.  However, if the Name Phonetics module
 	 * has been installed, and the addresshierarchy.soundexProcessor global property has been configured, the keys
 	 * will be the same pipe-delimited string, but with each entry name transformed via the specified soundex processor
 	 * 
 	 * The searchAddresses method compares the input string against the keys, and returns the values of any matches
 	 * 
 	 * Need to make sure we synchronize to avoid having multiple threads
 	 * trying to initialize it at the same time, or one using it before it is initialized
 	 * (Note that the one thing this won't prevent against is it being re-initialized while another
 	 * thread is accessing it)
 	 * 
 	 * NOTE: this method ONLY initializes the full address cache if it has not yet been initialized--
 	 * if you want to RE-initialized the full address cache, first call resetFullAddressCache
 	 * and then this method
 	 */
 	public void initializeFullAddressCache();
 	
 	/**
 	 * Resets the internal address cache used to perform full address services to null
 	 * @return 
 	 */
 	public void resetFullAddressCache();
 	
 	/**
 	 * Fetches the AddressToEntryMap with the given id
 	 * 
 	 * @param id id of the AddressToEntryMap object to fetch
 	 */
 	public AddressToEntryMap getAddressToEntryMap(Integer id);
 	
 	/**
 	 * Fetches the AddressToyEntryMap objects that match the given PersonAddress
 	 * 
 	 * @param personAddress the PersonAddreses to retrieve AddressToEntry records for
 	 */
 	public List<AddressToEntryMap> getAddressToEntryMapsByPersonAddress(PersonAddress address);
 	
 	/**
 	 * Saves the passed AddressToEntry map
 	 * 
 	 * @param addressToEntry the AddressToyEntryMap to save
 	 */
 	public void saveAddressToEntryMap(AddressToEntryMap addressToEntry);
 	
 	/**
 	 * Deletes the given AddressToEntryMap
 	 * 
 	 * @param addressToEntryMap
 	 */
 	public void deleteAddressToEntryMap(AddressToEntryMap addressToEntryMap);
 
 	/**
 	 * Deletes all AddressToEntryMaps for the given PersonAddress
 	 * 
 	 * @param address
 	 */
 	public void deleteAddressToEntryMapsByPersonAddress(PersonAddress address);
 	
 	/**
 	 * Given a PersonAddress, updates all the AddressToEntryMaps for that
 	 * PersonAddress
 	 * 
 	 * @param address
 	 */
 	public void updateAddressToEntryMapsForPersonAddress(PersonAddress address);
 	
 	/**
 	 * Given a person, updates all the AddressToEntryMaps for all non-voided
 	 * PersonAddresses associated with that person
 	 *  
 	 * @param person
 	 */
 	public void updateAddressToEntryMapsForPerson(Person person);
 	
 	
 	/**
 	 * Finds all patients with a date changed after the given date
 	 * 
 	 * @param date
 	 */
 	public List<Patient> findAllPatientsWithDateChangedAfter(Date date);
 	
 	/**
 	 * Deprecated methods
 	 */
 	
 	// this has been renamed searchAddresses
 	@Deprecated
 	public List<String> getPossibleFullAddresses(String searchString);
 	
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
 	public AddressHierarchyLevel getHierarchyType(int levelId);
 
 }
