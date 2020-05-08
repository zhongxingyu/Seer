 package org.openmrs.module.addresshierarchy.service;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.PersonAddress;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.addresshierarchy.AddressField;
 import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
 import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
 import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;
 import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
 import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * The Class AddressHierarchyServiceImpl default implementation of AddressHierarchyService.
  */
 public class AddressHierarchyServiceImpl implements AddressHierarchyService {
 	
 	protected static final Log log = LogFactory.getLog(AddressHierarchyServiceImpl.class);
 	
 	private AddressHierarchyDAO dao;
 	
 	public void setAddressHierarchyDAO(AddressHierarchyDAO dao) {
 		this.dao = dao;
 	}
 	
 	@Transactional(readOnly = true)
 	public List<String> getPossibleAddressValues(PersonAddress address, String fieldName) {	
 		
 		AddressField field = AddressField.getByName(fieldName);
 	
 		if (field == null) {
 			throw new AddressHierarchyModuleException(fieldName + " is not the name of a valid address field");
 		}
 		
 		return getPossibleAddressValues(address, field);
 	}
 	
 	@Transactional(readOnly = true)
 	public List<String> getPossibleAddressValues(PersonAddress address, AddressField field) {	
 		
 		Map<String,String> possibleAddressValues = new HashMap<String,String>();
 		AddressHierarchyLevel targetLevel = null;
 		
 		// iterate through the ordered levels until we reach the level associated with the specified fieldName
 		for (AddressHierarchyLevel level : getOrderedAddressHierarchyLevels(false)) {
 			if (level.getAddressField() != null && level.getAddressField().equals(field)) {
 				targetLevel = level;
 				break;
 			}
 		}
 		
 		if (targetLevel == null) {
 			log.error("Address field " + field + " is not mapped to address hierarchy level.");
 			return null;
 		}
 		
 		// calls getPossibleAddressHierarchyEntries(PersonAddress, AddressHierarchLevel) to perform the actual search
 		List<AddressHierarchyEntry> entries = getPossibleAddressHierarchyEntries(address, targetLevel);
 		
		// note that by convention entries should not be null, so we don't test for null here
 		
 		// take the entries returns and convert them into a list of *unique* names (using case-insensitive comparison)
 		// we use a map here to make this process more efficient
 		for (AddressHierarchyEntry entry : entries) {
 			// see if there is already key for this entry name (converted to lower-case)
 			if(!possibleAddressValues.containsKey(entry.getName().toLowerCase())) {
 				// if not, add the key/value pair for this entry name, where the value equals the entry name,
 				// and the key is the entry name converted to lower-case
 				possibleAddressValues.put(entry.getName().toLowerCase(), entry.getName());
 			}
 		}
 		
 		List<String> results = new ArrayList<String>();
 		results.addAll(possibleAddressValues.values());
 		return results;
 	}
 	
 	public List<AddressHierarchyEntry> getPossibleAddressHierarchyEntries(PersonAddress address, AddressHierarchyLevel targetLevel) {
 		
 		// split the levels into levels before and after the level associated with the field name
 		Boolean reachedFieldLevel = false;
 		List<AddressHierarchyLevel> higherLevels = new ArrayList<AddressHierarchyLevel>();
 		List<AddressHierarchyLevel> lowerLevels = new ArrayList<AddressHierarchyLevel>();
 		
 		for (AddressHierarchyLevel level : getOrderedAddressHierarchyLevels()) {
 			if (reachedFieldLevel) {
 				lowerLevels.add(level);
 			}
 			else {
 				higherLevels.add(level);
 			}
 			if (level.equals(targetLevel)) {
 				lowerLevels.add(level);  // we want the target level in both the higher and lower level lists
 				reachedFieldLevel = true;
 			}
 		}
 		
 		if (!reachedFieldLevel) {
 			// if we haven't found an address hierarchy level associated with this field, then we certainly aren't going to be
 			// able to find a list of possible values
			// return an empty set here, not null, because null is the default method in core if not overridden
			return new ArrayList<AddressHierarchyEntry>();
 		}
 		
 		List<AddressHierarchyEntry> possibleEntries = new ArrayList<AddressHierarchyEntry>();
 		
 		// first handle the top level
 		AddressHierarchyLevel topLevel= higherLevels.remove(0);
 		String topLevelValue = topLevel.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, topLevel.getAddressField()) : null;
 		
 		// if we have a top level value, find the top-level entry that matches that value
 		if (StringUtils.isNotBlank(topLevelValue)) {
 			possibleEntries.add(getChildAddressHierarchyEntryByName(null, topLevelValue));
 		}
 		// otherwise, get all the entries at the top level
 		else {
 			possibleEntries.addAll(getAddressHierarchyEntriesAtTopLevel());
 		}
 		
 		// now go through all the other levels above the level we are looking for
 		for (AddressHierarchyLevel level : higherLevels) {
 			List<AddressHierarchyEntry> possibleEntriesAtNextLevel = new ArrayList<AddressHierarchyEntry>();
 			
 			// find the value of the address field at the level we are dealing with
 			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField()) : null;
 			
 			// loop through all the possible entries
 			for (AddressHierarchyEntry entry : possibleEntries) {
 				// if a value has been specified, we only want child entries that match that value
 				if(StringUtils.isNotBlank(levelValue)) {
 					AddressHierarchyEntry childEntry = getChildAddressHierarchyEntryByName(entry, levelValue);
 					if (childEntry != null) {
 						possibleEntriesAtNextLevel.add(childEntry);
 					}
 				}
 				// otherwise, we need to add all children of the possible entry
 				else {
 					possibleEntriesAtNextLevel.addAll(getChildAddressHierarchyEntries(entry));
 				}
 			}
 			// now continue the loop and move on to the next level
 			possibleEntries = possibleEntriesAtNextLevel;
 		}
 		
 		// assign the possible entry to results array
 		List<AddressHierarchyEntry> results = possibleEntries;
 	
 		// now we need to handle the any person address field values that may have been specified *below* the field
 		// we are looking for in the hierarchy
 		
 		// we need to loop through the results in reverse and find any fields that have values
 		Collections.reverse(lowerLevels);
 		Iterator<AddressHierarchyLevel> i = lowerLevels.iterator();
 		
 		possibleEntries = null;
 		
 		while (i.hasNext()) {
 			AddressHierarchyLevel level = i.next();
 			
 			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField()) : null;
 			
 			// we are looking for the first level with a value
 			if (StringUtils.isNotBlank(levelValue)) {
 				possibleEntries = getAddressHierarchyEntriesByLevelAndName(level, levelValue);		
 				break;
 			}
 		}
 		
 		// if we haven't found any possible lower level entries, then we can just return the possibleEntries we calculated by higher level
 		if (possibleEntries == null) {
 			return results;
 		}
 		
 		// now that we've go something to start with, we need to work our way back up the tree 
 		while (i.hasNext()) {
 			
 			AddressHierarchyLevel level = i.next();
 			
 			String levelValue = level.getAddressField() != null ? AddressHierarchyUtil.getAddressFieldValue(address, level.getAddressField()) : null;
 			
 			List<AddressHierarchyEntry> possibleEntriesAtNextLevel = new ArrayList<AddressHierarchyEntry>();
 			
 			for (AddressHierarchyEntry entry : possibleEntries) {
 				AddressHierarchyEntry parentEntry = entry.getParent();
 				
 				if (parentEntry != null) {
 					possibleEntriesAtNextLevel.add(parentEntry);
 				}	
 			}
 			
 			// if we have a value restriction here, remove any entries that don't fit the restriction
 			if (StringUtils.isNotBlank(levelValue)) {
 				Iterator<AddressHierarchyEntry> j = possibleEntriesAtNextLevel.iterator();
 				while (j.hasNext()) {
 					AddressHierarchyEntry entry = j.next();
 					if (!entry.getName().equalsIgnoreCase(levelValue)) {
 						j.remove();
 					}
 				}
 			}
 			
 			possibleEntries = possibleEntriesAtNextLevel;
 		}
 		
 		// do a union of the results from the higher and lower level tests
 		if (results.retainAll(possibleEntries));
 		
 		return results;
 	}
 	
 	@Transactional(readOnly = true)
 	public Integer getAddressHierarchyEntryCount() {
 		return dao.getAddressHierarchyEntryCount();
 	}
 	
 	@Transactional(readOnly = true)
 	public Integer getAddressHierarchyEntryCountByLevel(AddressHierarchyLevel level) {
 		return dao.getAddressHierarchyEntryCountByLevel(level);
 	}
 	
 	@Transactional(readOnly = true)
 	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyId) {
 		return dao.getAddressHierarchyEntry(addressHierarchyId);
 	}
 	
 	@Transactional(readOnly = true)
 	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId) {
 		return dao.getAddressHierarchyEntryByUserGenId(userGeneratedId);
 	}
 	
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevel(AddressHierarchyLevel level) {
 		return dao.getAddressHierarchyEntriesByLevel(level);
 	}
 	
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel level, String name) {
 		return dao.getAddressHierarchyEntriesByLevelAndName(level, name);
 	}
 	
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesAtTopLevel() {
 		return getAddressHierarchyEntriesByLevel(getTopAddressHierarchyLevel());
 	}
 	
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry) {
 		if (entry != null) {
 			return dao.getChildAddressHierarchyEntries(entry);
 		}
 		else {
 			return getAddressHierarchyEntriesAtTopLevel();
 		}
 	}
 	
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(Integer entryId) {
 		AddressHierarchyEntry entry = getAddressHierarchyEntry(entryId);
 		
 		if (entry == null) {
 			throw new AddressHierarchyModuleException("Invalid Address Hierarchy Entry Id " + entryId);
 		}
 		
 		return getChildAddressHierarchyEntries(entry);
 	}
 	
 	@Transactional(readOnly = true)
 	public AddressHierarchyEntry getChildAddressHierarchyEntryByName(AddressHierarchyEntry entry, String childName) {
 		if (entry != null) {
 			return dao.getChildAddressHierarchyEntryByName(entry, childName);
 		}
 		else {
 			List<AddressHierarchyEntry> entries = dao.getAddressHierarchyEntriesByLevelAndName(getTopAddressHierarchyLevel(), childName);
 			if (entries.size() == 0) {
 				return null;
 			}
 			else if (entries.size() == 1) {
 				return entries.get(0);
 			}
 			else {
 				throw new AddressHierarchyModuleException("Two or more top-level entries have the same name");
 			}
 		}
 	} 
 	
 	@Transactional
 	public void saveAddressHierarchyEntry(AddressHierarchyEntry entry) {
 		dao.saveAddressHierarchyEntry(entry);
 	}
 	
 	@Transactional
 	public void saveAddressHierarchyEntries(List<AddressHierarchyEntry> entries) {
 		for (AddressHierarchyEntry entry : entries) {
 			dao.saveAddressHierarchyEntry(entry);
 		}
 	}
 	
 	@Transactional
 	public void deleteAllAddressHierarchyEntries() {
 		dao.deleteAllAddressHierarchyEntries();
 	}
 	
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels() {
 		return getOrderedAddressHierarchyLevels(true);
 	}
 	
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyLevel> getOrderedAddressHierarchyLevels(Boolean includeUnmapped) {	
 		List<AddressHierarchyLevel> levels = new ArrayList<AddressHierarchyLevel>();
 		
 		// first, get the top level level
 		AddressHierarchyLevel level = getTopAddressHierarchyLevel();
 		
 		if (level != null) {
 			// add the top level to this list
 			if (level.getAddressField() != null || includeUnmapped == true) {
 				levels.add(level);
 			}
 				
 			// now fetch the children in order
 			while (getChildAddressHierarchyLevel(level) != null) {
 				level = getChildAddressHierarchyLevel(level);
 				if (level.getAddressField() != null || includeUnmapped == true) {	
 					levels.add(level);
 				}
 			}
 		}
 		
 		return levels;
 	}
 	
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyLevel> getAddressHierarchyLevels() {
 		return dao.getAddressHierarchyLevels();
 	}
 	
 	@Transactional(readOnly = true)
 	public Integer getAddressHierarchyLevelsCount() {
 		List<AddressHierarchyLevel> levels = getAddressHierarchyLevels();
 		return levels != null ? levels.size() : 0;
 	}
 	
 	@Transactional(readOnly = true)
 	public AddressHierarchyLevel getTopAddressHierarchyLevel() {
 		return dao.getTopAddressHierarchyLevel();
 	}
 	
 	@Transactional(readOnly = true)
     public AddressHierarchyLevel getBottomAddressHierarchyLevel() {
 		
 		// get the ordered list
 		List<AddressHierarchyLevel> levels = getOrderedAddressHierarchyLevels();
 		
 		// return the last member in the list
 		if (levels != null && levels.size() > 0) {
 			return levels.get(levels.size() - 1);
 		}
 		else {
 			return null;
 		}
     }
 	
 	@Transactional(readOnly = true)
 	public AddressHierarchyLevel getAddressHierarchyLevel(Integer levelId) {
 		return dao.getAddressHierarchyLevel(levelId);
 	}
 	
 	@Transactional(readOnly = true)
     public AddressHierarchyLevel getChildAddressHierarchyLevel(AddressHierarchyLevel level) {
 	    return dao.getAddressHierarchyLevelByParent(level);
     }
 	
 	@Transactional
 	public AddressHierarchyLevel addAddressHierarchyLevel() {
 		AddressHierarchyLevel newLevel = new AddressHierarchyLevel();
 		newLevel.setParent(getBottomAddressHierarchyLevel());
 		// need to call the service method through the context to take care of AOP
 		Context.getService(AddressHierarchyService.class).saveAddressHierarchyLevel(newLevel);
 		return newLevel;
 	}
 	
 	@Transactional
 	public void saveAddressHierarchyLevel(AddressHierarchyLevel level) {
 		dao.saveAddressHierarchyLevel(level);
 	}
 	
 	@Transactional
     public void deleteAddressHierarchyLevel(AddressHierarchyLevel level) {
     	dao.deleteAddressHierarchyLevel(level);  
     }
 	
 	@Transactional
 	public void setAddressHierarchyLevelParents() {
 		// iterate through the levels
 		for (AddressHierarchyLevel level : getAddressHierarchyLevels()) {
 			
 			if (getAddressHierarchyEntryCountByLevel(level) > 0) {
 				// get an entry for this level
 				AddressHierarchyEntry entry = getAddressHierarchyEntriesByLevel(level).get(0);
 				// if needed, set the parent for this level based on the level of the parent of this entry
 				if (entry.getParent() != null && entry.getParent().getLevel() != level.getParent()) {
 					level.setParent(entry.getParent().getLevel());
 					// need to call the service method through the context to take care of AOP
 					Context.getService(AddressHierarchyService.class).saveAddressHierarchyLevel(level);
 				}
 			}
 			// if is level has no entries and no parents, delete it
 			else if (level.getParent() == null){
 				// need to call the service method through the context to take care of AOP
 				Context.getService(AddressHierarchyService.class).deleteAddressHierarchyLevel(level);
 			}
 		}
 	}
 	
 	/**
 	 * The following methods are deprecated and just exist to provide backwards compatibility to
 	 * Rwanda Address Hierarchy module
 	 */
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah) {
 		return dao.getLeafNodes(ah);
 	}
 	
 	@Deprecated
 	@Transactional
 	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude) {
 		dao.associateCoordinates(ah, latitude, longitude);
 	}
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyEntry> getTopOfHierarchyList() {
 		return getAddressHierarchyEntriesAtTopLevel();
 	}
 
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public List<Object[]> getLocationAddressBreakdown(int locationId) {
 		return dao.getLocationAddressBreakdown(locationId);
 	}
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public List<Object[]> findUnstructuredAddresses(int page, int locationId) {
 		return dao.findUnstructuredAddresses(page, locationId);
 	}
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public List<Object[]> getAllAddresses(int page) {
 		return dao.getAllAddresses(page);
 	}
 	
 	@Deprecated
 	@Transactional
 	public void initializeRwandaHierarchyTables() {
 		dao.initializeRwandaHierarchyTables();
 	}
 	
 	/**
 	 * I've renamed the following methods to make them a little more clear, but kept the old method
 	 * names for backwards compatibility
 	 */
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public int getAddressHierarchyCount() {
 		return getAddressHierarchyEntryCount();
 	}
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public List<AddressHierarchyEntry> getNextComponent(Integer locationId) {
 		return getChildAddressHierarchyEntries(locationId);
 	}
 	
 	@Deprecated
 	@Transactional
 	public void saveAddressHierarchy(AddressHierarchyEntry ah) {
 		saveAddressHierarchyEntry(ah);
 	}
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public AddressHierarchyEntry getLocationFromUserGenId(String userGeneratedId) {
 		return getAddressHierarchyEntryByUserGenId(userGeneratedId);
 	}
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public AddressHierarchyEntry getAddressHierarchy(int addressHierarchyId) {
 		return getAddressHierarchyEntry(addressHierarchyId);
 	}
 	
 	@Deprecated
 	@Transactional(readOnly = true)
 	public AddressHierarchyLevel getHierarchyType(int levelId) {
 		return getAddressHierarchyLevel(levelId);
 	}
 	
 }
