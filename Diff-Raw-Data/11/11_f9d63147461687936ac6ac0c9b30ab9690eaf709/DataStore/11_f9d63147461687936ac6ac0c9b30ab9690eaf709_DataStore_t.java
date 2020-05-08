 package cpsc310.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 
 /**
  * Provides datastore access and in memory data query access. Emulates databases
  * actions but only store and retrieves houses that are changed or at inital
  * startup
  * 
  * @author Justin
  * 
  */
 public class DataStore {
 	// Currently we store whole file on module load.
 	private HashMap<String, HouseDataPoint> store;
 
 	// To facilitate address look up
 	private HashMap<String, List<Integer>> streetNames;
 
 	// To facilitate civic Number look up
 	private HashMap<Integer, List<String>> civicNumbers;
 
 	// To facilitate postalCode look up
 	private HashMap<String, List<String>> postalCodes;
 
 	// To facilitate owner lookups
 	private HashMap<String, List<String>> owners;
 
 	// To facilitate forSale lookups
 	private HashSet<String> forSaleHomes;
 
 	// To facilitate land value search
 	private TreeMap<Integer, List<String>> currentLandValues;
 
 	// To facilitate improvement values search
 	private TreeMap<Integer, List<String>> currentImprovementValues;
 
 	// To facilitate assessment year search
 	private TreeMap<Integer, List<String>> assessmentYears;
 
 	// To facilitate previous land value search
 	private TreeMap<Integer, List<String>> previousLandValues;
 
 	// To facilitate previous improvement year search
 	private TreeMap<Integer, List<String>> previousImprovementValues;
 
 	// To facilitate year build search
 	private TreeMap<Integer, List<String>> yearsBuilt;
 
 	// To facilitate big improvement year search
 	private TreeMap<Integer, List<String>> bigImprovementYears;
 
 	// To facilitate price search
 	private TreeMap<Integer, List<String>> price;
 
 	public DataStore() {
 		store = populateMemoryStore();
 		initalizeLookups(store);
 	}
 
 	/**
 	 * Retrives data from data store and datasources parsing into a hashmap of
 	 * houseDataPoints returns HashMap of houseDataPoints
 	 */
 	private HashMap<String, HouseDataPoint> populateMemoryStore() {
 		DataCatalogueObserverImpl observerService = new DataCatalogueObserverImpl();
 		List<String> rawData = observerService
 				.downloadFile("http://www.ugrad.cs.ubc.ca/~d2t6/property_tax_report_csv.zip");
 		// Parse raw data
 		// TODO throws an error if download fails and the tempStore is null
 		FileParser parser = new FileParser();
 		HashMap<String, HouseDataPoint> tempStore = parser.parseData(rawData);
 
 		// register objectify objects if not already registered
 		initilizeDataStorage();
 
 		try {
 			// Populate memory store with changed entries
 			Objectify ofy = ObjectifyService.begin();
 			Iterable<Key<HouseDataPoint>> allKeys = ofy.query(
 					HouseDataPoint.class).fetchKeys();
 			Iterator<Key<HouseDataPoint>> houseIDs = allKeys.iterator();
 			while (houseIDs.hasNext()) {
 				HouseDataPoint tempHouse = ofy.get(houseIDs.next());
 				tempStore.put(tempHouse.getHouseID(), tempHouse);
 			}
 		} catch (NullPointerException e) {
 			// need to handle the fact there is no objectify data store in junit
 			// tests
 			System.err.println("This should only print when using junit tests");
 		}
 		return tempStore;
 	}
 
 	/**
 	 * initalizes lookup for search functions
 	 * 
 	 * @param houses
 	 */
 	private void initalizeLookups(HashMap<String, HouseDataPoint> houses) {
 		// initialize data-lookup structures
 		streetNames = new HashMap<String, List<Integer>>();
 		postalCodes = new HashMap<String, List<String>>();
 		civicNumbers = new HashMap<Integer, List<String>>();
 		owners = new HashMap<String, List<String>>();
 		forSaleHomes = new HashSet<String>();
 		currentLandValues = new TreeMap<Integer, List<String>>();
 		currentImprovementValues = new TreeMap<Integer, List<String>>();
 		assessmentYears = new TreeMap<Integer, List<String>>();
 		previousLandValues = new TreeMap<Integer, List<String>>();
 		previousImprovementValues = new TreeMap<Integer, List<String>>();
 		yearsBuilt = new TreeMap<Integer, List<String>>();
 		bigImprovementYears = new TreeMap<Integer, List<String>>();
 		price = new TreeMap<Integer, List<String>>();
 
 		Iterator<String> tempItr = houses.keySet().iterator();
 		while (tempItr.hasNext()) {
 			HouseDataPoint currentHouse = houses.get(tempItr.next());
 
 			// Populate Street Name Hash
 			if (streetNames.containsKey(currentHouse.getStreetName())) {
 				streetNames.get(currentHouse.getStreetName()).add(
 						currentHouse.getCivicNumber());
 			} else {
 				ArrayList<Integer> tempCivicList = new ArrayList<Integer>();
 				tempCivicList.add(currentHouse.getCivicNumber());
 				streetNames.put(currentHouse.getStreetName(), tempCivicList);
 			}
 
 			// Populate Civic Number Hash
 			if (civicNumbers.containsKey(currentHouse.getCivicNumber())) {
 				civicNumbers.get(currentHouse.getCivicNumber()).add(
 						currentHouse.getStreetName());
 			} else {
 				ArrayList<String> tempStreetList = new ArrayList<String>();
 				tempStreetList.add(currentHouse.getStreetName());
 				civicNumbers.put(currentHouse.getCivicNumber(), tempStreetList);
 			}
 
 			// Populate current land value tree
 			if (currentLandValues.containsKey(currentHouse
 					.getCurrentLandValue())) {
 				currentLandValues.get(currentHouse.getCurrentLandValue()).add(
 						currentHouse.getHouseID());
 			} else {
 				List<String> tempHouseIDs = new ArrayList<String>();
 				tempHouseIDs.add(currentHouse.getHouseID());
 				currentLandValues.put(currentHouse.getCurrentLandValue(),
 						tempHouseIDs);
 			}
 
 			// Populate improvementValues
 			if (currentHouse.getCurrentImprovementValue() != -1) {
 				if (currentImprovementValues.containsKey(currentHouse
 						.getCurrentImprovementValue())) {
 					currentImprovementValues.get(
 							currentHouse.getCurrentImprovementValue()).add(
 							currentHouse.getHouseID());
 				} else {
 					List<String> tempHouseIDs = new ArrayList<String>();
 					tempHouseIDs.add(currentHouse.getHouseID());
 					currentImprovementValues.put(
 							currentHouse.getCurrentImprovementValue(),
 							tempHouseIDs);
 				}
 			}
 
 			// Populate assessmentYears
 			if (currentHouse.getAssessmentYear() != -1) {
 				if (assessmentYears.containsKey(currentHouse
 						.getAssessmentYear())) {
 					assessmentYears.get(currentHouse.getAssessmentYear()).add(
 							currentHouse.getHouseID());
 				} else {
 					List<String> tempHouseIDs = new ArrayList<String>();
 					tempHouseIDs.add(currentHouse.getHouseID());
 					assessmentYears.put(currentHouse.getAssessmentYear(),
 							tempHouseIDs);
 				}
 			}
 
 			// Populate previousLandValues
 			if (currentHouse.getPreviousLandValue() != -1) {
 				if (previousLandValues.containsKey(currentHouse
 						.getPreviousLandValue())) {
 					previousLandValues.get(currentHouse.getPreviousLandValue())
 							.add(currentHouse.getHouseID());
 				} else {
 					List<String> tempHouseIDs = new ArrayList<String>();
 					tempHouseIDs.add(currentHouse.getHouseID());
 					previousLandValues.put(currentHouse.getPreviousLandValue(),
 							tempHouseIDs);
 				}
 			}
 
 			// Populate previousImprovementValues
 			if (currentHouse.getPreviousImprovementValue() != -1) {
 				if (previousImprovementValues.containsKey(currentHouse
 						.getPreviousImprovementValue())) {
 					previousImprovementValues.get(
 							currentHouse.getPreviousImprovementValue()).add(
 							currentHouse.getHouseID());
 				} else {
 					List<String> tempHouseIDs = new ArrayList<String>();
 					tempHouseIDs.add(currentHouse.getHouseID());
 					previousImprovementValues.put(
 							currentHouse.getPreviousImprovementValue(),
 							tempHouseIDs);
 				}
 			}
 
 			// Populate yearsBuilt
 			if (currentHouse.getYearBuilt() != -1) {
 				if (yearsBuilt.containsKey(currentHouse.getYearBuilt())) {
 					yearsBuilt.get(currentHouse.getYearBuilt()).add(
 							currentHouse.getHouseID());
 				} else {
 					List<String> tempHouseIDs = new ArrayList<String>();
 					tempHouseIDs.add(currentHouse.getHouseID());
 					yearsBuilt.put(currentHouse.getYearBuilt(), tempHouseIDs);
 				}
 			}
 
 			// Populate bigImprovementYears
 			if (currentHouse.getBigImprovementYear() != -1) {
 				if (bigImprovementYears.containsKey(currentHouse
 						.getBigImprovementYear())) {
 					bigImprovementYears.get(
 							currentHouse.getBigImprovementYear()).add(
 							currentHouse.getHouseID());
 				} else {
 					List<String> tempHouseIDs = new ArrayList<String>();
 					tempHouseIDs.add(currentHouse.getHouseID());
 					bigImprovementYears.put(
 							currentHouse.getBigImprovementYear(), tempHouseIDs);
 				}
 			}
 
 			updateIndexes(currentHouse);
 		}
 	}
 
 	/**
 	 * given a house updates the house indexes that can change due to house
 	 * updates
 	 * 
 	 * @param currentHouse
 	 */
 	private void updateIndexes(HouseDataPoint currentHouse) {
 
 		// Populate postal code Hash
 		if (!currentHouse.getPostalCode().equals("")) {
 			if (postalCodes.containsKey(currentHouse.getPostalCode())) {
 				postalCodes.get(currentHouse.getPostalCode()).add(
 						currentHouse.getHouseID());
 			} else {
 				List<String> tempHouseIDs = new ArrayList<String>();
 				tempHouseIDs.add(currentHouse.getHouseID());
 				postalCodes.put(currentHouse.getPostalCode(), tempHouseIDs);
 			}
 		}
 
 		// Populate Owners Hash
 		if (!currentHouse.getOwner().equals("")) {
 			if (owners.containsKey(currentHouse.getOwner())) {
 				owners.get(currentHouse.getOwner()).add(
 						currentHouse.getHouseID());
 			} else {
 				List<String> tempHouseIDs = new ArrayList<String>();
 				tempHouseIDs.add(currentHouse.getHouseID());
 				owners.put(currentHouse.getOwner(), tempHouseIDs);
 			}
 		}
 
 		// Populate edited Entries Hash
 		if (currentHouse.getIsSelling()) {
 			forSaleHomes.add(currentHouse.getHouseID());
 		} else {
 			forSaleHomes.remove(currentHouse.getHouseID());
 		}
 
 		// Populate price
 		if (currentHouse.getPrice() != -1) {
 			if (price.containsKey(currentHouse.getPrice())) {
 				price.get(currentHouse.getPrice()).add(
 						currentHouse.getHouseID());
 			} else {
 				List<String> tempHouseIDs = new ArrayList<String>();
 				tempHouseIDs.add(currentHouse.getHouseID());
 				price.put(currentHouse.getPrice(), tempHouseIDs);
 			}
 		}
 	}
 
 	/**
 	 * Remove house from indexes, needs to be invoked before an existing house
 	 * is updated
 	 * 
 	 * @param house
 	 */
 	private void removeFromIndexes(HouseDataPoint currentHouse) {
 		// remove from postal code Hash
 		if (!currentHouse.getPostalCode().equals("")) {
 			if (postalCodes.containsKey(currentHouse.getPostalCode())) {
 				postalCodes.get(currentHouse.getPostalCode()).remove(
 						currentHouse.getHouseID());
 				// if position has any empty array remove the whole thing
 				if (postalCodes.get(currentHouse.getPostalCode()).size() <= 0) {
 					postalCodes.remove(currentHouse.getPostalCode());
 				}
 			}
 		}
 
 		// remove from owners hash
 		if (!currentHouse.getOwner().equals("")) {
 			if (owners.containsKey(currentHouse.getOwner())) {
 				owners.get(currentHouse.getOwner()).remove(
 						currentHouse.getHouseID());
 				// if position has any empty array remove the whole thing
 				if (owners.get(currentHouse.getOwner()).size() <= 0) {
 					owners.remove(currentHouse.getOwner());
 				}
 			}
 		}
 
 		// remove price tree
 		if (currentHouse.getPrice() != -1) {
 			if (price.containsKey(currentHouse.getPrice())) {
 				price.get(currentHouse.getPrice()).remove(
 						currentHouse.getHouseID());
 				// if position has any empty array remove the whole thing
 				if (price.get(currentHouse.getPrice()).size() <= 0) {
 					price.remove(currentHouse.getPrice());
 				}
 			}
 		}
 	}
 
 	/*
 	 * Initializes DataStore Objects (houseDataPoints) if not already
 	 */
 	private void initilizeDataStorage() {
 		// try to register
 		try {
 			ObjectifyService.register(HouseDataPoint.class);
 		} catch (IllegalArgumentException e) {
 			// already registered
 			System.out
 					.println("HouseDataPoints already registered in data store");
 		}
 	}
 
 	// HouseID retrieval Methods
 
 	/**
 	 * Gets keys of all of entire datastore as an array
 	 * 
 	 * @return keys
 	 */
 	public List<String> getAllKeys() {
 		List<String> keys = new ArrayList<String>();
 		keys.addAll(store.keySet());
 		return keys;
 	}
 
 	/**
 	 * Gets keys of all of entire datastore as a set
 	 * 
 	 * @return keys
 	 */
 	public Set<String> getAllKeysSet() {
 		Set<String> keys = new HashSet<String>();
 		keys.addAll(store.keySet());
 		return keys;
 	}
 
 	/**
 	 * Gets the streets names of entire datastore
 	 * 
 	 * @return keys (list of street names)
 	 */
 	public List<String> getStreets() {
 		List<String> keys = new ArrayList<String>();
 		keys.addAll(streetNames.keySet());
 		Collections.sort(keys);
 		return keys;
 	}
 
 	/**
 	 * Get house data for table display
 	 * 
 	 * @param tempArray
 	 * @param start
 	 * @param range
 	 * @return list of HouseDataPoint
 	 */
 	public List<HouseDataPoint> getHouses(List<String> tempArray, int start,
 			int range) {
 		List<HouseDataPoint> grab = new ArrayList<HouseDataPoint>();
 		int end = start + range;
 		// Check for end condition to prevent accessing out-of-array.
 		if (end > tempArray.size()) {
 			end = tempArray.size();
 		}
 		for (int i = start; i < end; i++) {
 			grab.add(store.get(tempArray.get(i)));
 		}
 		// Change below to return grab
 		return grab;
 	}
 
 	// Search methods (subset of retrival Methods, retrives only arrays of keys)
 	/**
 	 * Searches for a user inputed address.
 	 * 
 	 * @param civicNumber
 	 * @param street
 	 * @return housesFound
 	 */
 	public Set<String> searchByAddress(int civicNumber, String street) {
 		Set<String> keys = new HashSet<String>();
 		if (store.containsKey(civicNumber + " " + street)) {
 			keys.add(civicNumber + " " + street);
 		}
 		return keys;
 	}
 
 	/**
 	 * Searches for user inputed address returns all address within that street.
 	 * 
 	 * @param street
 	 * @return housesFound
 	 */
 	public Set<String> searchByStreet(String street) {
 		Set<String> keys = new HashSet<String>();
 		List<Integer> currentCivicNumbers = streetNames.get(street);
 		if (currentCivicNumbers != null) {
 			Iterator<Integer> tempItr = currentCivicNumbers.iterator();
 			while (tempItr.hasNext()) {
 				keys.add(tempItr.next() + " " + street);
 			}
 		}
 		return keys;
 	}
 
 	/**
 	 * Searches for user inputed civic number returns all address associated.
 	 * 
 	 * @param civicNumber
 	 * @return housesFound
 	 */
 	public Set<String> searchByCivicNumber(int civicNumber) {
 		Set<String> keys = new HashSet<String>();
 		List<String> currentCivicNumbers = civicNumbers.get(civicNumber);
 		if (currentCivicNumbers != null) {
 			Iterator<String> tempItr = currentCivicNumbers.iterator();
 			while (tempItr.hasNext()) {
 				keys.add(civicNumber + " " + tempItr.next());
 			}
 		}
 		return keys;
 	}
 
 	/**
 	 * Searches for houses via postal code
 	 * 
 	 * @param postalCode
 	 * @return houseFound
 	 */
 	public Set<String> searchByPostalCode(String postalCode) {
 		Set<String> keys = new HashSet<String>();
 		List<String> currentPostalCodes = postalCodes.get(postalCode);
 		if (currentPostalCodes != null) {
 			Iterator<String> tempItr = currentPostalCodes.iterator();
 			while (tempItr.hasNext()) {
 				keys.add(tempItr.next());
 			}
 		}
 		return keys;
 	}
 
 	/**
 	 * Searches for houses via owner
 	 * 
 	 * @param owner
 	 *            - realtor
 	 * @return houseFound
 	 */
 	public Set<String> searchByOwner(String realtor) {
 		Set<String> keys = new HashSet<String>();
 		List<String> currentOwners = owners.get(realtor);
 		if (currentOwners != null) {
 			Iterator<String> tempItr = currentOwners.iterator();
 			while (tempItr.hasNext()) {
 				keys.add(tempItr.next());
 			}
 		}
 		return keys;
 	}
 
 	/**
 	 * return all houses that are for sale
 	 * 
 	 * @return house - houses that are for sale
 	 */
 	public Set<String> getForSaleHomes() {
 		return forSaleHomes;
 	}
 
 	/**
 	 * Searches for houses current land value
 	 * 
 	 * @precondition min <= max
 	 * @param min
 	 *            - upper bound
 	 * @param max
 	 *            -lower bound
 	 * @return houseFound
 	 */
 	public Set<String> searchByCurrentLandValue(int min, int max) {
 		SortedMap<Integer, List<String>> tempMap = currentLandValues.subMap(
 				min, true, max, true);
 		return convertRangedSearchResult(tempMap);
 	}
 
 	/**
 	 * Searches for houses in range of improvementValues
 	 * 
 	 * @precondition min <= max
 	 * @param min
 	 *            - upper bound
 	 * @param max
 	 *            -lower bound
 	 * @return houseFound
 	 */
 	public Set<String> searchByCurrentImprovementValue(int min, int max) {
 		SortedMap<Integer, List<String>> tempMap = currentImprovementValues
 				.subMap(min, true, max, true);
 		return convertRangedSearchResult(tempMap);
 	}
 
 	/**
 	 * Searches for houses in range of assessmentYear
 	 * 
 	 * @precondition min <= max
 	 * @param min
 	 *            - upper bound
 	 * @param max
 	 *            -lower bound
 	 * @return houseFound
 	 */
 	public Set<String> searchByAssessmentYear(int min, int max) {
 		SortedMap<Integer, List<String>> tempMap = assessmentYears.subMap(min,
 				true, max, true);
 		return convertRangedSearchResult(tempMap);
 	}
 
 	/**
 	 * Searches for houses in range of previousLandValue
 	 * 
 	 * @precondition min <= max
 	 * @param min
 	 *            - upper bound
 	 * @param max
 	 *            -lower bound
 	 * @return houseFound
 	 */
 	public Set<String> searchByPreviousLandValue(int min, int max) {
 		SortedMap<Integer, List<String>> tempMap = previousLandValues.subMap(
 				min, true, max, true);
 		return convertRangedSearchResult(tempMap);
 	}
 
 	/**
 	 * Searches for houses in range of previousImprovementValue
 	 * 
 	 * @precondition min <= max
 	 * @param min
 	 *            - upper bound
 	 * @param max
 	 *            -lower bound
 	 * @return houseFound
 	 */
 	public Set<String> searchByPreviousImprovementValue(int min, int max) {
 		SortedMap<Integer, List<String>> tempMap = previousImprovementValues
 				.subMap(min, true, max, true);
 		return convertRangedSearchResult(tempMap);
 	}
 
 	/**
 	 * Searches for houses in range of year Built
 	 * 
 	 * @precondition min <= max
 	 * @param min
 	 *            - upper bound
 	 * @param max
 	 *            -lower bound
 	 * @return houseFound
 	 */
 	public Set<String> searchByYearBuilt(int min, int max) {
 		SortedMap<Integer, List<String>> tempMap = yearsBuilt.subMap(min, true,
 				max, true);
 		return convertRangedSearchResult(tempMap);
 	}
 
 	/**
 	 * Searches for houses in range of bigImprovementYear
 	 * 
 	 * @precondition min <= max
 	 * @param min
 	 *            - upper bound
 	 * @param max
 	 *            -lower bound
 	 * @return houseFound
 	 */
 	public Set<String> searchByBigImprovementYear(int min, int max) {
 		SortedMap<Integer, List<String>> tempMap = bigImprovementYears.subMap(
 				min, true, max, true);
 		return convertRangedSearchResult(tempMap);
 	}
 
 	/**
 	 * Searches for houses in range of price
 	 * 
 	 * @precondition min <= max
 	 * @param min
 	 *            - upper bound
 	 * @param max
 	 *            -lower bound
 	 * @return houseFound
 	 */
 	public Set<String> searchByPrice(int min, int max) {
 		SortedMap<Integer, List<String>> tempMap = price.subMap(min, true, max,
 				true);
 		return convertRangedSearchResult(tempMap);
 	}
 
 	/**
 	 * Helper method to convert SortedMaps to arraylists created when search in
 	 * a range of numbers
 	 * 
 	 * @param result
 	 * @return convertedList - ArrayList of houseIDs
 	 */
 	private Set<String> convertRangedSearchResult(
 			SortedMap<Integer, List<String>> result) {
 		Set<String> keys = new HashSet<String>();
 		Iterator<Integer> tempItr = result.keySet().iterator();
 		while (tempItr.hasNext()) {
 			// retrieve from list of houses
 			List<String> currentList = result.get(tempItr.next());
 			Iterator<String> tempKeyItr = currentList.iterator();
 			while (tempKeyItr.hasNext()) {
 				keys.add(tempKeyItr.next());
 			}
 		}
 		return keys;
 	}
 
 	// sorting functions
 
 	/**
 	 * Sorts houses by HouseID
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByHouseID(List<String> currentList) {
 		HouseIDComparator comp = new HouseIDComparator(store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by Owner
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByOwner(List<String> currentList) {
 		OwnerComparator comp = new OwnerComparator(store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by postalCodes
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByPostalCode(List<String> currentList) {
 		PostalCodeComparator comp = new PostalCodeComparator(store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by forSaleHomes
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByForSale(List<String> currentList) {
 		ForSaleComparator comp = new ForSaleComparator(store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by currentLandValues
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByCurrentLandValue(List<String> currentList) {
 		CurrentLandValueComparator comp = new CurrentLandValueComparator(store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by currentImprovementValue
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByCurrentImprovementValue(List<String> currentList) {
 		CurrentImprovementValueComparator comp = new CurrentImprovementValueComparator(
 				store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by assessmentYear
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByAssessmentYear(List<String> currentList) {
 		AssessmentYearComparator comp = new AssessmentYearComparator(store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by previousLandValue
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByPreviousLandValue(List<String> currentList) {
 		PreviousLandValueComparator comp = new PreviousLandValueComparator(
 				store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by previousImprovementValue
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByPreviousImprovementValue(List<String> currentList) {
 		PreviousImprovementValueComparator comp = new PreviousImprovementValueComparator(
 				store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by yearBuilt
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByYearBuilt(List<String> currentList) {
 		YearBuiltComparator comp = new YearBuiltComparator(store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by bigImprovementYear
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByBigImprovementYear(List<String> currentList) {
 		BigImprovementYearComparator comp = new BigImprovementYearComparator(
 				store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by price
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByPrice(List<String> currentList) {
 		PriceComparator comp = new PriceComparator(store);
 		Collections.sort(currentList, comp);
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by HouseID descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByHouseIDDes(List<String> currentList) {
 		HouseIDComparator comp = new HouseIDComparator(store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by Owner descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByOwnerDes(List<String> currentList) {
 		OwnerComparator comp = new OwnerComparator(store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by postalCodes descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByPostalCodeDes(List<String> currentList) {
 		PostalCodeComparator comp = new PostalCodeComparator(store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by forSaleHomes descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByForSaleDes(List<String> currentList) {
 		ForSaleComparator comp = new ForSaleComparator(store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by currentLandValues descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByCurrentLandValueDes(List<String> currentList) {
 		CurrentLandValueComparator comp = new CurrentLandValueComparator(store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by currentImprovementValue descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByCurrentImprovementValueDes(
 			List<String> currentList) {
 		CurrentImprovementValueComparator comp = new CurrentImprovementValueComparator(
 				store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by assessmentYear descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByAssessmentYearDes(List<String> currentList) {
 		AssessmentYearComparator comp = new AssessmentYearComparator(store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by previousLandValue descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByPreviousLandValueDes(List<String> currentList) {
 		PreviousLandValueComparator comp = new PreviousLandValueComparator(
 				store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by previousImprovementValue descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByPreviousImprovementValueDes(
 			List<String> currentList) {
 		PreviousImprovementValueComparator comp = new PreviousImprovementValueComparator(
 				store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by yearBuilt descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByYearBuiltDes(List<String> currentList) {
 		YearBuiltComparator comp = new YearBuiltComparator(store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by bigImprovementYear descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByBigImprovementYearDes(List<String> currentList) {
 		BigImprovementYearComparator comp = new BigImprovementYearComparator(
 				store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	/**
 	 * Sorts houses by price descending
 	 * 
 	 * @param currentList
 	 *            - list to be sorted
 	 * @return sortedList - ArrayList of houseIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> sortByPriceDes(List<String> currentList) {
 		PriceComparator comp = new PriceComparator(store);
 		Collections.sort(currentList, Collections.reverseOrder(comp));
 		return currentList;
 	}
 
 	// Other methods
 	/**
 	 * validates house given restriction criteria
 	 * 
 	 * @param houseID
 	 * @param userSearchInput
 	 * @return boolean - true if house is valid, false if not valid
 	 */
 	public boolean validateHouseParams(String houseID, String[] userSearchInput) {
 		// [0]"civicNumber"
 		// [1]"streetName",
 		// Value "Current Land Value" - [2]min, [3]max
 		// Value "Price" - [4]min, [5]max
 		// [6]"Realtor"
 		// [7]"Postal Code"
 		// Value "Current Improvement Value" - [8]min, [9]max
 		// Year "Assessment Year" - [10]min, [11]max
 		// Value "Previous Land Value" - [12]min, [13]max
 		// Value "Previous Improvement Value" - [14]min, [15]max
 		// Year "Year Built" - [16]min, [17]max
 		// Year "Big Improvement Year" - [18]min, [19]max
 		HouseDataPoint currentHouse = store.get(houseID);
 		if (currentHouse.getCivicNumber() != Integer
 				.parseInt(userSearchInput[0])) {
 			return false;
 		}
 		if (!currentHouse.getStreetName().equals(userSearchInput[1])) {
 			return false;
 		}
 		if (currentHouse.getCurrentLandValue() < Integer
 				.parseInt(userSearchInput[2])) {
 			return false;
 		}
 		if (currentHouse.getCurrentLandValue() > Integer
 				.parseInt(userSearchInput[4])) {
 			return false;
 		}
 		if (currentHouse.getCurrentLandValue() > Integer
 				.parseInt(userSearchInput[3])) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Add/update user specified information about the specified data in the
 	 * database.
 	 * 
 	 * @pre Owner, price, isSelling, house, longitude, latitude != null
 	 * @post the current memory set is refreshed to reflect changes and when
 	 *       datastore is updated a true value is returned
 	 * 
 	 * @param Owner
 	 *            - name of realtor
 	 * @param price
 	 *            - price of house
 	 * @param isSelling
 	 *            - for-sale indicator
 	 * @param houses
 	 *            - set of houses to update
 	 * @param latitude
 	 *            - latitude geolocation for house
 	 * @param longitude
 	 *            - longitude geolocation for house
 	 * @param postalCode
 	 *            - postal Code for house
 	 * @return boolean - if successful true, if not false
 	 * 
 	 */
 	public void updateHouse(String Owner, int price, boolean isSelling,
 			String house, double longitude, double latitude, String postalCode) {
 		
 		//TODO Remove - Debugging purposes
 		System.out.println("Add New User");
 		System.out.println(Owner);
 		System.out.println(price);
 		System.out.println(isSelling);
 		System.out.println(latitude);
 		System.out.println(longitude);
 		System.out.println(postalCode);
 		
 		// create and set object variables
 		HouseDataPoint currentHouse = store.get(house);
 
 		// remove from indexes
 		removeFromIndexes(currentHouse);
 
 		// Update house values
 		currentHouse.setOwner(Owner);
 		currentHouse.setPrice(price);
 		currentHouse.setIsSelling(isSelling);
 		currentHouse.setLatLng(latitude, longitude);
 		if (postalCode != null) {
 			currentHouse.setPostalCode(postalCode);
 		}
 
 		// re-add to indexes
 		updateIndexes(currentHouse);
 
 		// Store object
 		Objectify ofy = ObjectifyService.begin();
 		ofy.put(currentHouse);
 	}
 
 	/**
 	 * Given a houseID, removes house from datastore and resets owner and price
 	 * 
 	 * @param house
 	 */
 	public void resetHouse(String house) {
 		// create and set object variables
 		HouseDataPoint currentHouse = store.get(house);
 
 		// remove from datastore
 		Objectify ofy = ObjectifyService.begin();
 		ofy.delete(currentHouse);
 
 		// remove from indexes
 		removeFromIndexes(currentHouse);
 
 		// Update house values
 		currentHouse.setOwner("");
 		currentHouse.setPrice(-1);
 		currentHouse.setIsSelling(false);
 
 		// re-add to indexes
 		updateIndexes(currentHouse);
 	}
 
 	/**
 	 * Searchs within the list of for sale houses within a specified polygon
 	 * represented by list of latitude and longitude values
 	 * 
 	 * @precondition latitudes.size() == longitudes.size()
 	 * @param latitudes
 	 *            - the latitudes of the polygon per vertex
 	 * @param longitudes
 	 *            - the longitudes of the polygon per vertex
 	 * @return HouseIDs - list of house IDs in that are for sale in the polygon
 	 */
 	public Set<String> searchForSaleInPolygon(double[] latitudes,
 			double[] longitudes) {
 
 		Set<String> keys = new HashSet<String>();
 
 		if (latitudes != null && longitudes != null) {
 
 			Iterator<String> tempItr = forSaleHomes.iterator();
 			int numVertexes = latitudes.length;
 
 			while (tempItr.hasNext()) {
 				HouseDataPoint currentHouse = store.get(tempItr.next());
 
 				int j = 0;
 				boolean oddNodes = false;
 				double y = currentHouse.getLatitude();
 				double x = currentHouse.getLongitude();
 				
 				System.out.println(y + " " + x);
 
 				for (int i = 0; i < numVertexes; i++) {
 					j++;
 					if (j == numVertexes) {
 						j = 0;
 					}
 					if (((latitudes[i] < y) && (latitudes[j] >= y))
						|| ((latitudes[j] < y) && (latitudes[i] >= y))) 
					
					{
						if (longitudes[i] 
						  + (y - latitudes[i]) 
						  / (latitudes[j] - latitudes[i])
						  * (longitudes[j] - longitudes[i]) < x) {
 							oddNodes = !oddNodes;
 						}
 					}
 				}
 				if (oddNodes) {
 					keys.add(currentHouse.getHouseID());
 				}
 			}
 		}
 		return keys;
 	}
 }
