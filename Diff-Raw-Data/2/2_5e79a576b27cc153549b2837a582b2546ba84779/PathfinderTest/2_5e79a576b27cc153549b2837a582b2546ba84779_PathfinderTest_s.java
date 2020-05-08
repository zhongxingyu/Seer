 /**
 * PathfinderTest
 * <p>
 * Test class for the Pathfinder class
 * @author Marius Spix
 */
 
 package test;
 
 import static org.junit.Assert.*;
 
 import helpers.Pathfinder;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 import model.Airport;
 import model.BadFileFormatException;
 import model.ImplListDataModel;
 import model.ImplListDataModelFactory;
 
 import org.json.simple.parser.ParseException;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class PathfinderTest {
 	
 	private static ImplListDataModel m;
 	private static HashMap<Long,Airport> l;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws FileNotFoundException, IOException, ParseException, BadFileFormatException {
 		File f = new File("test/testconnection.json");
 		
 		m = ImplListDataModelFactory.INSTANCE.factory(f);
 		l = m.getAirportList();
 	}
 	
 	/***
 	 * pathfinderTest
 	 * <p>
 	 * Template for tests of the Pathfinder class
 	 * @param destination: destination from the start airport
 	 * @param should: expected hops
 	 */
 	private void pathfinderTest(Airport source, Airport destination, Airport[] should) {
 		Pathfinder pf = new Pathfinder(destination, l.values());
 		Airport[]  is = new Airport[should.length];
 		
 		List<Airport> q =
 		pf.determineShortestPathFrom(source);
 		
 		System.out.println(q);
 		
 	    q.toArray(is);
 		
 		assertArrayEquals(should, is);
 	}
 	
 	@Test
 	/**
 	 * testPathfinder1
 	 * <p>
 	 * Test the pathfinder class.
 	 */
 	public void testPathfinder1() {
		pathfinderTest(l.get(6L), l.get(1L), new Airport[]{l.get(1L), l.get(5L), l.get(6L)});
 	}
 	
 	@Test
 	/**
 	 * testPathfinder2
 	 * <p>
 	 * Test the route to the start airport itself
 	 */
 	public void testPathfinder2() {		
 		pathfinderTest(l.get(6L), l.get(6L), new Airport[]{l.get(6L)});
 	}
 	
 	@Test
 	/**
 	 * testPathfinder3
 	 * <p>
 	 * Test an unreachable airport
 	 */
 	public void testPathfinder3() {		
 		pathfinderTest(l.get(6L), l.get(8L), new Airport[]{l.get(6L)});
 	}
 	
 	@Test
 	/**
 	 * testPathfinder4
 	 * <p>
 	 * Test with directed graph (Bruessel (5) -> Frankfurt (1) only via London (4))
 	 */
 	public void testPathfinder4() {
 		pathfinderTest(
 				l.get(5L), // source
 				l.get(1L), // destination
 				new Airport[]{
 						l.get(5L),
 						l.get(4L),
 						l.get(1L)
 					}
 				);
 	}
 
 }
