 package net.craigrm.dip.map;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Represents a Diplomacy map as a collection of 
  * {@link Provincesnet.craigrm.dip.map.Province Province}. 
  * Provides convenience methods to return collections of Province 
  * {@link net.ProvinceIdentifier.dip.map.Identifier Identifier}s by terrain type.
  * <p>
  * Holds the non-stateful elements of the game. Can be likened 
  * to the empty game board, showing the playing map but no unit pieces.
  * 
  * @author Ric Craig
  * 
  */
 public class DipMap {
 	
 	private static DipMap instance = null;
 
 	private final HashMap<ProvinceIdentifier, Province> map;
 	private final HashSet<ProvinceIdentifier> seaProvinces;
 	private final HashSet<ProvinceIdentifier> inlandProvinces;
 	private final HashSet<ProvinceIdentifier> coastalProvinces;
 	private final HashMap<ProvinceIdentifier, ProvinceIdentifier> aliases;
 	
 	/**
 	 * Constructs a new DipMap instance from a mapper if there is no 
 	 * current DipMap instance. The mapper is responsible for extracting 
 	 * the Province data from a map definition of whatever form.  
 	 * 
 	 * @param mapDataSource an {@link net.craigrm.dip.map.IMapDataSource IMapper} implementation that 
 	 * extracts Province data from a map definition.
 	 */
 	public static void makeMap(IMapDataSource mapDataSource) {
 		if (instance == null) {
 			instance = new DipMap(mapDataSource);
 		}
 	}
 
 	/**
	 * Constructs a new DipMap instance from a mapper.  
 	 * 
 	 * @param mapDataSource an {@link net.craigrm.dip.map.IMapDataSource IMapper} implementation that 
 	 * extracts Province data from a map definition.
 	 */
 	public static void reloadMap(IMapDataSource mapDataSource) {
 		instance = new DipMap(mapDataSource);
 	}
 
 	/**
 	 * Provides the sole instance of DipMap created by {@link net.craigrm.dip.map.DipMap#makeMap(IMapDataSource) makeMap(IMapper)}
 	 * @return the DipMap instance
 	 * @throws IllegalStateException if DipMap has not been instantiated
 	 */
 	public static DipMap getMap() {
 		if (instance == null) {
 			throw new IllegalStateException();
 		}
 		return instance;
 	}
 	
 	private DipMap(IMapDataSource mapDataSource) {
 		map = new HashMap<ProvinceIdentifier, Province>();
 		seaProvinces = new HashSet<ProvinceIdentifier>();
 		inlandProvinces = new HashSet<ProvinceIdentifier>();
 		coastalProvinces = new HashSet<ProvinceIdentifier>();
 		aliases = new HashMap<ProvinceIdentifier, ProvinceIdentifier>();
 		for(Province p: mapDataSource.getProvinces()) {
 			addProvince(p);
 		}
 	}
 
 	/**
 	 * 	
 	 * @return an unmodifiable view of the set of Province Identifiers that 
 	 * make up the sea areas of the map. 
 	 */
 	public Set<ProvinceIdentifier> getSeaProvinces() {
 		return Collections.unmodifiableSet(seaProvinces);
 	}
 
 	/**
 	 * 	
 	 * @return an unmodifiable view of the set of Province Identifiers that 
 	 * make up the land areas of the map that do not border sea areas. 
 	 */
 	public Set<ProvinceIdentifier> getInlandProvinces() {
 		return Collections.unmodifiableSet(inlandProvinces);
 	}
 	
 	/**
 	 * 	
 	 * @return an unmodifiable view of the set of Province Identifiers that 
 	 * make up the land areas of the map that border sea areas. 
 	 */
 	public Set<ProvinceIdentifier> getCoastalProvinces() {
 		return Collections.unmodifiableSet(coastalProvinces);
 	}
 
 	/**
 	 * 	
 	 * @param an Identifier of the Province, either the canonical Identifier or an alias 
 	 * @return the Province with the given Identifier, or null if it does not exist. 
 	 */
 	public Province getProvince(ProvinceIdentifier id) {
 		final ProvinceIdentifier canonicalId = aliases.get(id);
 		return map.get(canonicalId);
 	}
 
 	/**
 	 * 	
 	 * @param an Identifier of a Province, either the canonical Identifier or an alias 
 	 * @return true if the Identifier identifies a Province of the current map. 
 	 */
 		public boolean isValidProvinceIdentifier(ProvinceIdentifier id) {
 			
 			return aliases.containsKey(id);
 		}
 
 	/**
 	 * 	
 	 * @param an Identifier of a Province, either the canonical Identifier or an alias 
 	 * @param an Identifier of a Province, either the canonical Identifier or an alias 
 	 * @return true id the two Provinces are neighbours.
 	 */
 	public boolean isNeighbour(ProvinceIdentifier id1, ProvinceIdentifier id2) {
 		final ProvinceIdentifier canonicalId1 = aliases.get(id1);
 		final ProvinceIdentifier canonicalId2 = aliases.get(id2);
 		return map.get(canonicalId1).getNeighbours().contains(canonicalId2);
 	}
 
 	/**
 	 * Determines and returns the shortest of each alternative convoying route 
 	 * from a starting province to a destination province. Routes are composed 
 	 * of the supplied set of convoying provinces.<P>
 	 * For two routes A,B,C and A,C only A,C is returned as the two routes are 
 	 * effectively the same and A,C is the shorter.<p>
 	 * For two routes A, B, C and A, B, D, both are returned as they are 
 	 * genuinely alternative routes.    
     * If the start or destination is not a coastal province, an empty set 
      * of routes is returned.<P>
 	 * Convoying provinces which are not sea provinces are ignored when 
 	 * determining possible routes. 	
 	 * 
 	 * @param an Identifier of the starting Province    
 	 * @param an Identifier of the destination Province    
 	 * @param a set of ProvinceIdentifiers from which to construct the routes 
 	 * @return a set of unique shortest alternative routes, which is empty if no 
 	 * route exists.
 	 */
 	public Set<List<ProvinceIdentifier>> getConvoyRoutes(ProvinceIdentifier startProvince, ProvinceIdentifier endProvince, Set<ProvinceIdentifier> convoyingProvinces) {
 		
 		Set<List<ProvinceIdentifier>> routes = new HashSet<List<ProvinceIdentifier>>();
 		
 		// Check for valid start and end provinces
 		if (!coastalProvinces.contains(startProvince)) {
 			return routes;
 		}
 		if (!coastalProvinces.contains(endProvince)) {
 			return routes;
 		}
 		
 		// Generate pool of valid convoying provinces identified by canonical identifier
 		Set<ProvinceIdentifier> provincePool = new HashSet<ProvinceIdentifier>();
 		for(ProvinceIdentifier provinceId:convoyingProvinces) {
 			ProvinceIdentifier canonicalId = aliases.get(provinceId);
 			if(seaProvinces.contains(canonicalId)) {
 				provincePool.add(canonicalId);
 			}
 		}
 		
 		// Check for at least one remaining convoying province
 		if (provincePool.isEmpty()) {
 			return routes;
 		}
 		
 		Set<LinkedList<ProvinceIdentifier>> potentialRoutes = new HashSet<LinkedList<ProvinceIdentifier>>();
 		LinkedList<ProvinceIdentifier> initialRoute = new LinkedList<ProvinceIdentifier>();
 		initialRoute.add(startProvince);
 		potentialRoutes.add(initialRoute);
 		
 		while (!potentialRoutes.isEmpty()) {
 			Set<LinkedList<ProvinceIdentifier>> newRoutes = new HashSet<LinkedList<ProvinceIdentifier>>();
 			Set<LinkedList<ProvinceIdentifier>> checkedRoutes = new HashSet<LinkedList<ProvinceIdentifier>>();
 			Set<ProvinceIdentifier> usedProvinces = new HashSet<ProvinceIdentifier>();
 
 			for(LinkedList<ProvinceIdentifier> potentialRoute:potentialRoutes) {
 				ProvinceIdentifier waypoint = potentialRoute.peekLast();
 				Set<ProvinceIdentifier> neighbours = map.get(waypoint).getNeighbours();
 
 				//Are we there yet?
 				if (neighbours.contains(endProvince)) {
					@SuppressWarnings("unchecked") // Clone known to be of type LinkedList<ProvinceIdentifier>
					LinkedList<ProvinceIdentifier> actualRoute = (LinkedList<ProvinceIdentifier>) potentialRoute.clone();
					actualRoute.add(endProvince);
					routes.add(actualRoute);
 					checkedRoutes.add(potentialRoute);
 					//Don't need to check other neighbours at this point 
 					//because we're only interested in the shortest path 
 					//of this route
 					continue;
 				}
 
 				neighbours.retainAll(provincePool);
 				
 				//Hit a dead end?
 				if (neighbours.isEmpty()) {
 					checkedRoutes.add(potentialRoute);
 					continue;
 				}
 				
 				//Create a new route for each neighbour and remove the old route.
 				for(ProvinceIdentifier neighbour: neighbours) {
 					@SuppressWarnings("unchecked") // Clone known to be of type LinkedList<ProvinceIdentifier>
 					LinkedList<ProvinceIdentifier> newRoute = (LinkedList<ProvinceIdentifier>) potentialRoute.clone();
 					newRoute.add(neighbour);
 					newRoutes.add(newRoute);
 				}
 				
 				checkedRoutes.add(potentialRoute);
 
 				//Add neighbours to set of used provinces
 				usedProvinces.addAll(neighbours);
 			
 			}
 			//Remove any checked routes from the set of potentials.
 			//A checked route has either arrived at the destination or
 			//has hit a dead end or has split into new routes.
 			potentialRoutes.removeAll(checkedRoutes);
 
 			//Add any new routes to the set of potentials.
 			//A new route is created when an existing route is split or extended.
 			potentialRoutes.addAll(newRoutes);
 
 			//Remove any used provinces from the set of convoying provinces.
 			//This ensures we don't loop and include longer paths for routes.
 			provincePool.removeAll(usedProvinces);
 		}
 	
 		return routes;
 	}
 
 	private void addProvince(Province province) {
 		final ProvinceIdentifier id = province.getIdentifier();
 		map.put(id, province);
 		switch(province.getType()) { 
 			case SEA: {
 				seaProvinces.add(id);
 				break;
 			}
 			case INLAND: {
 				inlandProvinces.add(id);
 				break;
 			}
 			case COAST: {
 				coastalProvinces.add(id);
 				break;
 			}
 		}
 		for(ProvinceIdentifier aliasId:province.getAliases()) {
 			aliases.put(aliasId, id);
 		}
 		aliases.put(id, id);
 	}
 	
 }
