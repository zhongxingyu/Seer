 package com.sandstonelabs.mimi;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
import java.io.InputStream;
 import java.util.List;
 
import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 
 import com.fasterxml.jackson.core.JsonParseException;
 
 public class CachedRestaurantSearchTest {
 
 	@Test
 	public void testRestaurantsAtLocation_returnsFilteredListFromCacheFile() throws JsonParseException, IOException {
 		RestaurantJsonParser jsonParser = new RestaurantJsonParser();
		
		File cacheFile = writeExampleDataToCache();
		
		CachedRestaurantSearch cachedRestaurantSearch = new CachedRestaurantSearch(cacheFile, jsonParser);
 		
 		float lat = 51.4915f;
 		float lon = -0.1650f;
 		List<Restaurant> restaurants = cachedRestaurantSearch.getRestaurantsAtLocation(lat, lon, 300);
 		
 		for (Restaurant restaurant : restaurants) {
 			System.out.println(restaurant);
 		}
 		assertEquals(7, restaurants.size());
 	}
 
	private File writeExampleDataToCache() throws IOException {
		File cacheFile = new File("test-cache.txt");
		InputStream exampleData = this.getClass().getResourceAsStream("/example-data.txt");
		FileUtils.copyInputStreamToFile(exampleData, cacheFile);
		return cacheFile;
	}

 }
