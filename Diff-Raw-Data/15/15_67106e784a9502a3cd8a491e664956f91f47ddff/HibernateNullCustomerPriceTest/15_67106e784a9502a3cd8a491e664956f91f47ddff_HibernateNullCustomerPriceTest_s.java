 package nz.ac.victoria.ecs.kpsmart.state.manipulation;
 
 import static org.junit.Assert.assertEquals;
 import nz.ac.victoria.ecs.kpsmart.GuiceServletContextListner;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.CustomerPrice;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Priority;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class HibernateNullCustomerPriceTest {
 
 	@BeforeClass
 	public static void setUp() throws Exception {
 		GuiceServletContextListner.init();
 	}
 
 	@Test
 	public void test() {
 		StateManipulator m = GuiceServletContextListner.getInjector().getInstance(StateManipulator.class);
 		
		CustomerPrice price = new CustomerPrice();
 		price.setStartLocation(null);
 		price.setEndLocation(m.getLocationForName("Rome"));
 		price.setProirity(Priority.Domestic_Air);
 		price.setPricePerUnitVolume(1);
		price.setPriceperUnitWeight(1);
 		m.save(price);
 		
 		assertEquals(price, m.getAllCustomerPrices().iterator().next());
 	}
 
 }
