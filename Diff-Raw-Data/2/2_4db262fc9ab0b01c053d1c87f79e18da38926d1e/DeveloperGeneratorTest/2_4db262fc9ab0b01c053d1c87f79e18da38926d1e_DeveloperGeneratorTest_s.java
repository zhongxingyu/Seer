 package edu.ncsu.csc.realsearch.devsurvey.test;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createControl;
 import static org.junit.Assert.assertEquals;
 
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.easymock.classextension.IMocksControl;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.ncsu.csc.realsearch.devsurvey.Developer;
 import edu.ncsu.csc.realsearch.devsurvey.DeveloperDAO;
 import edu.ncsu.csc.realsearch.devsurvey.DeveloperGenerator;
 
 public class DeveloperGeneratorTest {
 
 	private IMocksControl ctrl;
 	private DeveloperDAO devDAO;
 
 	@Before
 	public void init() {
 		ctrl = createControl();
 		devDAO = ctrl.createMock(DeveloperDAO.class);
 	}
 
 	@Test
 	public void chopTo3Correct() throws Exception {
 		List<Developer> list = Arrays.asList(new Developer("1", 1.0), //
 				new Developer("2", 2.0), // 
 				new Developer("3", 3.0), // 
 				new Developer("4", 4.0), // 
 				new Developer("5", 5.0), //
 				new Developer("6", 6.0), // 
 				new Developer("7", 7.0), // 
 				new Developer("8", 8.0));
 		expect(devDAO.getAllDeveloperDistances("bob")).andReturn(list).once();
 		expectFillNames("1");
 		expectFillNames("5");
 		expectFillNames("8");
 		ctrl.replay();
 		List<Developer> actuals = new DeveloperGenerator(3, devDAO).getDevelopers("bob");
 		assertEquals("size is correct", 3, actuals.size());
 		assertEquals("always have the closest", "1", actuals.get(0).getUsername());
 		assertEquals("have the middle one", "5", actuals.get(1).getUsername());
 		assertEquals("always have the farthest", "8", actuals.get(2).getUsername());
 		ctrl.verify();
 	}
 
 	@Test
 	public void chopTo6Correct() throws Exception {
 		List<Developer> list = Arrays.asList(new Developer("1", 1.0), //
 				new Developer("2", 2.0), //
 				new Developer("3", 3.0), //
 				new Developer("4", 4.0), //
 				new Developer("5", 5.0), //
 				new Developer("6", 6.0), //
 				new Developer("7", 7.0), //
 				new Developer("8", 8.0));
 		expect(devDAO.getAllDeveloperDistances("bob")).andReturn(list).once();
 		expectFillNames("1");
 		expectFillNames("2");
 		expectFillNames("3");
 		expectFillNames("4");
 		expectFillNames("5");
 		expectFillNames("8");
 		ctrl.replay();
 		List<Developer> actuals = new DeveloperGenerator(6, devDAO).getDevelopers("bob");
 		assertOrder(new String[] { "1", "2", "3", "4", "5", "8" }, actuals);
 		ctrl.verify();
 	}
 
 	@Test
 	public void chop10To6Correct() throws Exception {
 		List<Developer> list = Arrays.asList(new Developer("1", 1.0), //
 				new Developer("2", 2.0), //
 				new Developer("3", 3.0), //
 				new Developer("4", 4.0), //
 				new Developer("5", 5.0), //
 				new Developer("6", 6.0), //
 				new Developer("7", 7.0), //
 				new Developer("8", 8.0), //
 				new Developer("9", 9.0), //
 				new Developer("10", 10.0)//
 				);
 		expect(devDAO.getAllDeveloperDistances("bob")).andReturn(list).once();
 		expectFillNames("1");
 		expectFillNames("3");
 		expectFillNames("5");
 		expectFillNames("7");
 		expectFillNames("9");
 		expectFillNames("10");
 		ctrl.replay();
 		List<Developer> actuals = new DeveloperGenerator(6, devDAO).getDevelopers("bob");
 		assertOrder(new String[] { "1", "3", "5", "7", "9", "10" }, actuals);
 		ctrl.verify();
 	}
 
 	@Test
 	public void chop10To5Correct() throws Exception {
 		List<Developer> list = Arrays.asList(new Developer("1", 1.0), //
 				new Developer("2", 2.0), //
 				new Developer("3", 3.0), //
 				new Developer("4", 4.0), //
 				new Developer("5", 5.0), //
 				new Developer("6", 6.0), //
 				new Developer("7", 7.0), //
 				new Developer("8", 8.0), //
 				new Developer("9", 9.0), //
 				new Developer("10", 9.0)//
 				);
 		expect(devDAO.getAllDeveloperDistances("bob")).andReturn(list).once();
 		expectFillNames("1");
 		expectFillNames("3");
 		expectFillNames("5");
 		expectFillNames("7");
 		expectFillNames("10");
 		ctrl.replay();
 		List<Developer> actuals = new DeveloperGenerator(5, devDAO).getDevelopers("bob");
 		assertOrder(new String[] { "1", "3", "5", "7", "10" }, actuals);
 		ctrl.verify();
 	}
 
 	private void expectFillNames(String username) throws SQLException {
 		expect(devDAO.getDeveloper(username)).andReturn(new Developer(username, 1.0)).once();
 	}
 
 	private void assertOrder(String[] expectedUserNames, List<Developer> actuals) {
 		assertEquals("size is correct", expectedUserNames.length, actuals.size());
 		for (int i = 0; i < expectedUserNames.length; i++) {
 			assertEquals("the developer at position " + i, expectedUserNames[i], actuals.get(i).getUsername());
 		}
 	}
 
 	@Test
 	public void doubleConversion() throws Exception {
 		int i = 2;
 		double chunk = 1.5;
		assertEquals(3, i * chunk);
 	}
 }
