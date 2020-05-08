 package main.crossover;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import main.Problem;
 import params.Params;
 import representations.path.Path;
 import factory.RepresentationFactory;
 
 public class EdgeRecombination extends CrossOver<Path> {
 
 	// TODO the tie case in choose city(edgeMap) should maybe be chosen completely random!
 	
 	public EdgeRecombination(RepresentationFactory<Path> factory,
 			Problem problem, Params params) {
 		super(factory, problem, params);
 	}
 
 	@Override
 	public List<Integer> breed(Path p1, Path p2) {
 		List<Integer> result = new ArrayList<Integer>();
 		List<Integer> unvisitedCities = getCities();
 		List<Set<Integer>> edgeMap = constructEdgeMap(p1, p2);
 		int currentCity = chooseCity(p1, p2);
 		while (unvisitedCities.size() > 1) {
 			removeOccurences(currentCity, edgeMap);
 			result.add(currentCity);
 			unvisitedCities.remove(new Integer(currentCity));
 			if (hasNeighbours(currentCity, edgeMap))
 				currentCity = chooseCity(currentCity, edgeMap);
 			else
 				currentCity = chooseCity(unvisitedCities);
 		}
 		result.add(unvisitedCities.get(0));
 		return result;
 	}
 
 	/**
 	 * Choose a random city from the list of unvisited cities (corresponds to
 	 * step 5 of the algorithm).
 	 * 
 	 * @param unvisitedCities
 	 * @return
 	 */
 	private int chooseCity(List<Integer> unvisitedCities) {
 		return unvisitedCities.get(params.rand.nextInt(unvisitedCities.size()));
 	}
 
 	/**
 	 * Choose the city which has the fewest entities in its (own) edge list. In case of ties, the last city is chosen.
 	 * 
 	 * @param edgeMap
 	 * @return
 	 */
 	private int chooseCity(int currentCity, List<Set<Integer>> edgeMap) {
 		int currentMinimum = -1;
 		int minimumNbNeighbours = 4;
 		for (int neighbour : edgeMap.get(currentCity)) {
 			int nbNeighbours = edgeMap.get(neighbour).size();
			if(nbNeighbours <= minimumNbNeighbours && nbNeighbours > 0) {
 				currentMinimum = neighbour;
 				minimumNbNeighbours = nbNeighbours;
 			}
 		}
 		return currentMinimum;
 	}
 
 	/**
 	 * Returns a list with all the integers between 0 (inc.) and the size of the
 	 * problem (inc.).
 	 * 
 	 * @return
 	 */
 	private List<Integer> getCities() {
 		List<Integer> cities = new ArrayList<Integer>();
 		for (int i = 0; i < problem.size(); i++) {
 			cities.add(i);
 		}
 		return cities;
 	}
 
 	/**
 	 * Returns true if the given city still has entities in its edge list.
 	 * @param city
 	 * @param edgeMap
 	 * @return
 	 */
 	private boolean hasNeighbours(int city, List<Set<Integer>> edgeMap) {
 		return !edgeMap.get(city).isEmpty();
 	}
 
 	/**
 	 * Remove all occurrences of the given city from the right hand side of
 	 * the given edgemap. (corresponds to step 2 of the algorithm).
 	 * 
 	 * @param city
 	 * @param edgeMap
 	 */
 	private void removeOccurences(int city, List<Set<Integer>> edgeMap) {
 		for (int i = 0; i < edgeMap.size(); i++) {
 			Set<Integer> currentEdgeSet = edgeMap.get(i);
 			if(currentEdgeSet.contains(city))
 				currentEdgeSet.remove(city);
 		}
 	}
 
 	/**
 	 * Choose a city of one of the 2 parents at random.
 	 * 
 	 * @param p1
 	 * @param p2
 	 * @return
 	 */
 	private int chooseCity(Path p1, Path p2) {
 		return params.rand.nextFloat() > 0.5 ? p1.getRandomCity(params.rand)
 				: p2.getRandomCity(params.rand);
 	}
 
 	public List<Set<Integer>> constructEdgeMap(Path parent1, Path parent2) {
 		List<Set<Integer>> edgeMap = new ArrayList<Set<Integer>>();
 		for (int city = 0; city < parent1.size(); city++) {
 			edgeMap.add(getConnectedCities(city, parent1, parent2));
 		}
 		return edgeMap;
 	}
 
 	private Set<Integer> getConnectedCities(int city, Path parent1, Path parent2) {
 		Set<Integer> result = new HashSet<Integer>();
 		for (int index = 0; index < parent1.size(); index++) {
 			extractNeighbours(city, parent1, result, index);
 			extractNeighbours(city, parent2, result, index);
 		}
 		return result;
 	}
 
 	private void extractNeighbours(int city, Path parent, Set<Integer> neighbours, int j) {
 		if (parent.getPath().get(j) == city) {
 			neighbours.add(convertToValidLeftNeighbour(parent, j,
 					parent.size()));
 			neighbours.add(convertToValidRightNeighbour(parent, j,
 					parent.size()));
 		}
 	}
 
 	private int convertToValidRightNeighbour(Path parent, int j, int length) {
 		int index = (j + 1 < length) ? j + 1 : 0;
 		return parent.getPath().get(index);
 	}
 
 	private int convertToValidLeftNeighbour(Path parent, int j, int length) {
 		int index = (j - 1 >= 0) ? j - 1 : length - 1;
 		return parent.getPath().get(index);
 
 	}
 
 }
