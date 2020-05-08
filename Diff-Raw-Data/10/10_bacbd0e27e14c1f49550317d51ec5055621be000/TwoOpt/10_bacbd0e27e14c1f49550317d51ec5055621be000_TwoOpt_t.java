 /**
  * @author Luuk Holleman
  * @date 15 april
  */
 package tspAlgorithm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import order.Product;
 import order.Location;
 
 public class TwoOpt extends TSPAlgorithm {
 	public static String name = "2-Opt";
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public ArrayList<Product> calculateRoute(ArrayList<Product> products,
 			int numberOfRobots, int currentRobot) {
 		// dit algoritme gaat verder op greedy
 		Greedy greedy = new Greedy();
 
 		// we hebben greedy als basis nodig en dus laten we greedy ook de
 		// initele route bepalen
 		products = greedy
 				.calculateRoute(products, numberOfRobots, currentRobot);
 
 		// de uiteindelijke route
 		ArrayList<Location> locations = getLocations(products);
 
 		// de starting node
 		for (Path path1 : getPaths(products)) {
 			for (Path path2 : getPaths(products)) {
 				// een hele lijpe berekening om te berekenen of de 2 paden
 				// elkaar kruizen
 				
 				boolean startCross = false;
 				boolean endCross = false;
 				
 				//Als begin en eind val pad1 op zelfde x zijn...
 				if (path1.startLocation.x == path1.endLocation.x) {
 					
 					//Als de x van begin & eind van pad2 niet gelijk zijn, en  het begin en eind van pad2 liggen aan weerzijden van pad1...
 					if(path2.endLocation.x != path2.startLocation.x && (path2.startLocation.x < path1.startLocation.x) != (path2.endLocation.x < path1.endLocation.x)) {
 						
 						//Bereken de steiging in Y per X coordinaat van pad2
 						float elevation = ((float) (path2.endLocation.y - path2.startLocation.y) / (float) (path2.endLocation.x - path2.startLocation.x));
 						
 						//Bereken het verschil in x' start punten van de paden
 						int diff = path2.startLocation.x - path1.startLocation.x;
 						
 						//bereken de Y coordinaat waarop de lijnen mogelijk kruisen
 						float crossY = path2.startLocation.y + elevation * diff;
 						
 						//Als de kruisende Y tussen de Y start en eind van pad1 ligt. Dan ligt het einde van pad2 rechts van pad1
 						endCross = Math.max(path1.startLocation.y, path1.endLocation.y) > crossY && crossY > Math.min(path1.startLocation.y, path1.endLocation.y);
 					}
 				} else {
 					//Berekende de steiging in Y per X coordinaat van pad1
 					float elevation = ((float) (path1.endLocation.y - path1.startLocation.y) / (float) (path1.endLocation.x - path1.startLocation.x));
 
 					//Bereken het verschil in het start(X)coordinaat van de paden
 					int startDiff = path2.startLocation.x
 							- path1.startLocation.x;
 					
 					//Bereken het verschil in het eind(X)coordinaat van de paden					
 					int endDiff = path2.endLocation.x - path1.startLocation.x;
 
 					//Bereken aan welke zijden van pad 2 de begin en eind punten van pad 1 liggen
 					startCross = path1.startLocation.y - elevation * startDiff > path2.startLocation.y;
 					endCross = path1.startLocation.y - elevation * endDiff > path2.endLocation.y;
 				}
 				// kijken of de paden elkaar kruizen
 				if (startCross != endCross) {
 					// ja dat doen ze! dus nu de paden fixen
 					// de paden moeten wel weer een compleet rondje maken,
 					// door de paden te veranderen kan het namelijk dat je er 2
 					// maakt
 					// dat doen we door de endlocation die als eerste in de
 					// route voorkomt te ruilen met de startlocation van die als
 					// laatste voorkomt
 
 					int path1Index = locations.indexOf(path1.endLocation);
 					int path2Index = locations.indexOf(path2.startLocation);
 
 					Collections.swap(locations, path1Index, path2Index);
 				}
 			}
 		}
 
 		// nu we de goeie volgorde van locaties hebben moeten we weer de juiste
 		// producten erbij zoeken
 		ArrayList<Product> newProducts = new ArrayList<Product>();
 
 		for (Location location : locations)
 			for (Product product : products)
 				if (product.getLocation() == location)
 					newProducts.add(product);
 
 		return newProducts;
 	}
 
 	/**
 	 * Haal alle locaties op van alle producten
 	 * 
 	 * @param products
 	 * @return
 	 */
 	private ArrayList<Location> getLocations(ArrayList<Product> products) {
 		ArrayList<Location> locations = new ArrayList<Location>();
 
 		for (Product product : products)
 			locations.add(product.getLocation());
 
 		return locations;
 	}
 
 	/**
 	 * Haal de paden op, een pad bestaat uit 2 locaties, start en eind
 	 * 
 	 * @param products
 	 * @return
 	 */
 	private ArrayList<Path> getPaths(ArrayList<Product> products) {
 		ArrayList<Path> paths = new ArrayList<Path>();
 
 		Location startLocation;
 		Location endLocation;
 
 		ArrayList<Location> locations = getLocations(products);
 
		//Als er geen locaties zijn, stuur niks terug
		if(locations.size() == 0)
			return paths;
		//Als er een locatie is, stuur alleen die terug
		else if(locations.size() == 1)
		{
			paths.add(new Path(locations.get(0), locations.get(0)));
			return paths;
		}
		
 		// de eerste is een uitzondering, om de loop goed te laten verlopen moet
 		// endlocation al bepaalt zijn
 		// en niet meer in de array voorkomen omdat de oude endlocation gelijk
 		// de nieuwe startlocation is
 		endLocation = locations.get(0);
 		locations.remove(0);
 
 		for (Location location : locations) {
 			// oude endlocation is nieuwe startlocation
 			startLocation = endLocation;
 
 			endLocation = location;
 
 			paths.add(new Path(startLocation, endLocation));
 		}
 
 		return paths;
 	}
 
 	/**
 	 * Een local class voor paths
 	 * 
 	 * @author Luuk
 	 * 
 	 */
 	private class Path {
 		public Location startLocation;
 		public Location endLocation;
 
 		public Path(Location startLocation, Location endLocation) {
 			this.startLocation = startLocation;
 			this.endLocation = endLocation;
 		}
 	}
 }
