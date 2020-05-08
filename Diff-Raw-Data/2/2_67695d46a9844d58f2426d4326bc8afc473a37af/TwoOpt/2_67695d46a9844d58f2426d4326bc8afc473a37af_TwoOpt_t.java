 /**
  * @author Luuk Holleman
  * @date 15 april
  */
 package tspAlgorithm;
 
 import java.util.ArrayList;
 
 import order.Product;
 import order.Location;
 
 
 public class TwoOpt implements TSPAlgorithm {
 	public static String name = "2-Opt";
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public ArrayList<Product> calculateRoute(ArrayList<Product> products) {
 		// dit algoritme gaat verder op greedy
 		Greedy greedy = new Greedy();
 		
 		// we hebben greedy als basis nodig en dus laten we greedy ook de initele route bepalen
 		products = greedy.calculateRoute(products);
 
 		// de uiteindelijke route
 		ArrayList<Location> locations = getLocations(products);
 		
 		// de starting node
 		for(Path path1 : getPaths(products)) {
 			for(Path path2 : getPaths(products)) {
 				// een hele lijpe berekening om te berekenen of de 2 paden elkaar kruizen
 				// voor uitleg, vraag tim
 				boolean startCross = path1.startLocation.y - ((path1.startLocation.y - path1.endLocation.y) / (path1.endLocation.x - path1.startLocation.x) * (path2.startLocation.x - path1.startLocation.x)) > path2.startLocation.y;
 				boolean endCross = path1.startLocation.y - ((path1.startLocation.y - path1.endLocation.y) / (path1.endLocation.x - path1.startLocation.x) * (path2.endLocation.x - path1.startLocation.x)) > path2.endLocation.y;
 				
 				// kijken of de paden elkaar kruizen
 				if(startCross != endCross) {
 					// ja dat doen ze! dus nu de paden fixen
 					// de paden moeten wel weer een compleet rondje maken,
 					// door de paden te veranderen kan het namelijk dat je er 2 maakt
 					// dat doen we door de endlocation die als eerste in de route voorkomt te ruilen met de startlocation van die als laatste voorkomt
 					
 					int path1Index = locations.indexOf(path1.endLocation);
 					int path2Index = locations.indexOf(path2.startLocation);
 					
 					if(path1Index < path2Index) {
 						locations.set(path2Index, path1.endLocation);
 						locations.set(path1Index, path2.startLocation);
 					} else {
 						locations.set(path2Index, path1.startLocation);
 						locations.set(path1Index, path2.endLocation);
 					}
 				}
 			}
 		}
 		
 		// nu we de goeie volgorde van locaties hebben moeten we weer de juiste producten erbij zoeken
 		ArrayList<Product> newProducts = new ArrayList<Product>();
 		
 		for(Location location : locations)
 			for(Product product : products)
 				if(product.getLocation() == location)
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
 
 		for(Product product : products)
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
 		
 		// de eerste is een uitzondering, om de loop goed te laten verlopen moet endlocation al bepaalt zijn
 		// en niet meer in de array voorkomen omdat de oude endlocation gelijk de nieuwe startlocation is
 		endLocation = locations.get(0);
 		locations.remove(0);
 		
 		for(Location location : locations) {
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
