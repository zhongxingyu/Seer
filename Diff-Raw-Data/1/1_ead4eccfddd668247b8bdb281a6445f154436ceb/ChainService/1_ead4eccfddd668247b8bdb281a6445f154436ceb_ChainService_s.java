 package services;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import models.Category;
 import models.Location;
 
 public class ChainService {
 
 	public List getCategoryHeads() {
 		List categoryContainers = new ArrayList();
 		categoryContainers.add(newCategoryContainer(Category.MOTOR, getMockLocations()));
 		categoryContainers.add(newCategoryContainer(Category.BICYCLE, getMockLocations()));
 		categoryContainers.add(newCategoryContainer(Category.FEET, getMockLocations()));
 		return categoryContainers;
 	}
 
 	private HashMap newCategoryContainer(Category category, List<Location> locations) {
 		HashMap categoryContainer = new HashMap();
 		categoryContainer.put("categoryName", category.toString());
 		categoryContainer.put("chainHeads", getMockLocations());
 		return categoryContainer;
 	}
 	
 	private List<Location> getMockLocations() {
 		List<Location> locations = new ArrayList<Location>();
 		locations.add(getMockLocation());
 		locations.add(getMockLocation());
 		locations.add(getMockLocation());
 		locations.add(getMockLocation());
 		locations.add(getMockLocation());
 
 		return locations;
 	}
 
 	private Location getMockLocation() {
 		Location location = new Location();
 		location.setRoughDistance((1+rand(10)) + " km");
 		location.setChainId(1L);
		location.setLocationId(1L);
 		location.setPictureUrl("http://thekeyresult.com/wp-content/uploads/2011/02/4548.jpg");
 		return location;
 	}
 	
 	private int rand(int n) {
 		return new Random().nextInt(n);
 	}
 }
