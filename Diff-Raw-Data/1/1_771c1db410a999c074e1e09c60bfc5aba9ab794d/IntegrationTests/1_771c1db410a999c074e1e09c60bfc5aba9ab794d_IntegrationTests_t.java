 package tests;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import cpsc310.server.DataCatalogueObserverImpl;
 import cpsc310.server.FileParser;
 import cpsc310.server.HouseDataPoint;
 
 public class IntegrationTests {
 
 	//tests to ensure that different classes work correctly upon integration
 	
 	private FileParser fileParser;
 	private ArrayList<HouseDataPoint> houses;
 	private HouseDataPoint house;
 	private List<String> csvFile;
 	private DataCatalogueObserverImpl observerService;
 	private String validURL = "http://www.ugrad.cs.ubc.ca/~y0c7/property_tax_report3.csv";
 	private String validZipURL = "http://www.ugrad.cs.ubc.ca/~y0c7/property_tax_report_csv.zip";
 	private String invalidURL = "http://www.ugrad.cs.ubc.ca/~y0c7/fakefile.csv";;
 	
 	//setup all the necessary variables
 	@Before
 	public void setUp() throws Exception {
 		fileParser = new FileParser();
 		observerService = new DataCatalogueObserverImpl();
 	}	
 	
 	@Test
 	public void testValidFile() {
 		csvFile = observerService.downloadFile(validURL);
 		houses = fileParser.parseData(csvFile);
 		house = houses.get(0);
 		assertEquals("014900009", house.getPID());
 		assertEquals("1185 12TH AVE E", house.getAddress());
 		assertFalse(house.getIsSelling());
 		assertEquals(634000.0, house.getLandValue(), 0);
 		assertNull(house.getOwner());
 		assertEquals("V5T 2J8", house.getPostalCode());
 		assertEquals(0.0, house.getPrice(), 0);
 	}
 	
 	@Test
 	public void testValidZipFile() {
 		csvFile = observerService.downloadFile(validZipURL);
 		houses = fileParser.parseData(csvFile);
 		house = houses.get(0);
 		assertEquals("014900009", house.getPID());
 		assertEquals("1185 12TH AVE E", house.getAddress());
 		assertFalse(house.getIsSelling());
 		assertEquals(634000.0, house.getLandValue(), 0);
 		assertNull(house.getOwner());
 		assertEquals("V5T 2J8", house.getPostalCode());
 		assertEquals(0.0, house.getPrice(), 0);		
 	}
 	
 	@Test (expected = NullPointerException.class)
 	public void testInvalidURL() {
 		csvFile = observerService.downloadFile(invalidURL);
 		assertNull(csvFile);
 		houses = fileParser.parseData(csvFile);	
 	}
 
 }
