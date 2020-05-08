 package com.matrobot.gha;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.InputStream;
 import java.util.List;
 
 import org.junit.Test;
 
 public class ConfigurationTest {
 
 	@Test
 	public void testCommand() {
 		InputStream inputStream = getClass().getResourceAsStream("testdata/config1.yaml");
 		Configuration parser = new Configuration(inputStream);
 		
 		assertEquals("find_events", parser.getCommand());
 	}
 
 
 	@Test
 	public void testData() {
 		InputStream inputStream = getClass().getResourceAsStream("testdata/config1.yaml");
 		Configuration parser = new Configuration(inputStream);
 		
 		assertEquals("datasets/github/", parser.getDataPath());
 	}
 
 
 	@Test
 	public void testMonths() {
 		InputStream inputStream = getClass().getResourceAsStream("testdata/config1.yaml");
 		Configuration parser = new Configuration(inputStream);
 		
 		assertEquals("2011-10", parser.getStartDate());
 		assertEquals("2012-1", parser.getEndDate());
 	}
 
 
 	@Test
 	public void testRepo() {
 		InputStream inputStream = getClass().getResourceAsStream("testdata/config1.yaml");
 		Configuration parser = new Configuration(inputStream);
 		
 		assertEquals("rails/rails", parser.getRepositoryName());
 	}
 
 
 	@Test
 	public void testFolders() {
 		InputStream inputStream = getClass().getResourceAsStream("testdata/config1.yaml");
 		Configuration parser = new Configuration(inputStream);
 		
 		List<String> folders = parser.getMonthFolders();
 		
 		assertEquals(4, folders.size());
 		assertEquals("datasets/github/2012-1", folders.get(3));
 	}
 
 
 	@Test
 	public void testOrder() {
 		InputStream inputStream = getClass().getResourceAsStream("testdata/config1.yaml");
 		Configuration parser = new Configuration(inputStream);
 		
 		assertEquals("ala", parser.getOrderBy());
 	}
 }
