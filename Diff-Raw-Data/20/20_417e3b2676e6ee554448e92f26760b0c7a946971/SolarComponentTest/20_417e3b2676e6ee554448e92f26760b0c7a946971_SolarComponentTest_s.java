 package com.qut.spc.model;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
 import javax.persistence.Entity;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
import org.mockito.Mockito;
 
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.qut.spc.db.Database;
import com.qut.spc.postcode.PostcodeUtil;
 
 /**
  * Used for testing the abstract class
  */
 @Entity
 class MockSolarComponent extends SolarComponent {
 	private int mockValue;
 	
 	public MockSolarComponent() {
 	}
 	
 	public static MockSolarComponent load(long id) {
 		return Database.loadComponent(id, MockSolarComponent.class);
 	}
 
 	public int getMockValue() {
 		return mockValue;
 	}
 
 	public void setMockValue(int mockValue) {
 		this.mockValue = mockValue;
 	}
 }
 
 /**
  * Test for Solar component abstract class
  * 
  * @author QuocViet
  */
 public class SolarComponentTest {
 
 	SolarComponent component;
 	
 	private final LocalServiceTestHelper helper =
 	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
 	// For comparing double values
 	private static final double EPSILON = 0.001;
 	
 	@Before
 	public void setUp() throws Exception {
 		helper.setUp();
 		component = new MockSolarComponent();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		helper.tearDown();
 	}
 
 	@Test
 	public void testConstructor_InitVariable() throws Exception {
 		// TODO: check all properties
 		assertEquals(0.0, component.getPrice(), EPSILON);
 		assertEquals(0.0, component.getEfficiencyDecrease(), EPSILON);
 		assertEquals("", component.getManufacture());
 	}
 	
 	@Test
 	public void testEfficiencyByYear_OneYear() throws Exception {
 		double eff[] = component.getEfficiencyByYear(1);
 		double result[] = new double[] { 100.0 };
 		
 		assertArrayEquals(result, eff, EPSILON);
 	}
 	
 	@Test
 	public void testEfficiencyByYear_ZeroDecrease() throws Exception {
 		double eff[] = component.getEfficiencyByYear(7);
 		double result[] = new double[] { 100.0, 100.0, 100.0, 100.0, 100.0,
 				100.0, 100.0};
 		
 		assertArrayEquals(result, eff, EPSILON);
 	}
 	
 	@Test
 	public void testEfficiencyByYear_SomeYears() throws Exception {
 		component.setEfficiencyDecrease(10.0);
 		
 		double eff[] = component.getEfficiencyByYear(5);
 		double result[] = new double[] { 100.0, 90.0, 80.0, 70.0, 60.0 };
 		
 		assertArrayEquals(result, eff, EPSILON);
 	}
 	
 	@Test
 	public void testEfficiencyByYear_ZeroYear() throws Exception {
 		double eff[] = component.getEfficiencyByYear(0);
 		
 		assertNotNull(eff);
 		assertEquals(0, eff.length);
 	}
 
 	@Test(expected=Exception.class)
 	public void testEfficiencyByYear_NegativeYear() throws Exception {
 		component.getEfficiencyByYear(-1);
 	}
 	
 	@Test
 	public void testSetPostcode_validPostcode_postCodeIsSet(){
 		component.setPostcode("1234");
 		assertEquals("1234", component.getPostcode());		
 	}
 
 	
 	@Test
 	public void testSaveComponent() throws Exception {
 		MockSolarComponent c1, c2;
 		
 		c1 = (MockSolarComponent)component;
 		c1.setEfficiencyDecrease(100.0);
 		c1.setManufacture("A new manufacture");
 		c1.setPrice(8000.0);
 		c1.setMockValue(12);
 		c1.setPostcode("7777");
 		
 		c1.save();
 		
 		c2 = MockSolarComponent.load(c1.getId());
 		assertNotNull(c2);
 		
 		assertEquals(c1.getEfficiencyDecrease(), c2.getEfficiencyDecrease(), EPSILON);
 		assertEquals(c1.getManufacture(), c2.getManufacture());
 		assertEquals(c1.getPrice(), c2.getPrice(), EPSILON);
 		assertEquals(c1.getMockValue(), c2.getMockValue(), EPSILON);
 		assertEquals("7777", c2.getPostcode());
 	}
 }
