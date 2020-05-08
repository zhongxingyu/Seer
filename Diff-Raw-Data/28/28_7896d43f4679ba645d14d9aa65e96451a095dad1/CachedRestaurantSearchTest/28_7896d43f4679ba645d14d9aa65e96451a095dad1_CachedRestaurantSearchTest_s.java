 package com.sandstonelabs.mimi;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.fasterxml.jackson.core.JsonParseException;
 
 public class CachedRestaurantSearchTest {
 
 	@Test
 	public void testRestaurantsAtLocation_returnsFilteredListFromCacheFile() throws JsonParseException, IOException {
 		RestaurantJsonParser jsonParser = new RestaurantJsonParser();
		CachedRestaurantSearch cachedRestaurantSearch = new CachedRestaurantSearch(new File("response-clean.txt"), jsonParser);
 		
 		float lat = 51.4915f;
 		float lon = -0.1650f;
 		List<Restaurant> restaurants = cachedRestaurantSearch.getRestaurantsAtLocation(lat, lon, 300);
 		
 		for (Restaurant restaurant : restaurants) {
 			System.out.println(restaurant);
 		}
 		assertEquals(7, restaurants.size());
 	}
 
 }
