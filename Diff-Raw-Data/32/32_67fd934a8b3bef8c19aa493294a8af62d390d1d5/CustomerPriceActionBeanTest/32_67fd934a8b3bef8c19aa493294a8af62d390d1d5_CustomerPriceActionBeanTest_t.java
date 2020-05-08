 package nz.ac.victoria.ecs.kpsmart.controller;
 
 import static nz.ac.victoria.ecs.kpsmart.util.Assertions.assertContains;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.Mockito.never;
 import net.sourceforge.stripes.mock.MockRoundtrip;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.CustomerPriceUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.state.CustomerPrice;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Direction;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Location;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Priority;
 import nz.ac.victoria.ecs.kpsmart.util.StripesActionBeanTest;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CustomerPriceActionBeanTest extends StripesActionBeanTest {
 	@Before
 	public void createTestData() {
 		when(this.state.getLocationForName("wellington")).thenReturn(new Location("wellington", 0, 0, false));
 		when(this.state.getLocationForName("rome")).thenReturn(new Location("rome", 1, 1, true));
 	}
 	
 	@Test
 	public void testAddSucceeds() throws Exception {
 		MockRoundtrip trip = new MockRoundtrip(this.context, CustomerPriceActionBean.class);
 		
 		trip.setParameter("location", "rome");
 		trip.setParameter("direction", Direction.To.toString());
 		trip.setParameter("priority", Priority.International_Air.toString());
 		trip.setParameter("volumePrice", "1");
 		trip.setParameter("weightPrice", "1");
 		
 		trip.execute("new");
 		
 		assertContains("\"status\":true", trip.getResponse().getOutputString());
 		
		CustomerPrice expected = new CustomerPrice(new Location("rome", 1, 1, true), Direction.To, Priority.International_Air);
 		expected.setPricePerUnitVolume(1);
 		expected.setPricePerUnitWeight(1);
 		
 		verify(this.manager, times(1)).performEvent(new CustomerPriceUpdateEvent(expected));
 	}
 	
 	@Test
 	public void testAddWithZeroWeightCostFails() throws Exception {
 		MockRoundtrip trip = new MockRoundtrip(this.context, CustomerPriceActionBean.class);
 		
 		trip.setParameter("location", "rome");
 		trip.setParameter("direction", Direction.To.toString());
 		trip.setParameter("priority", Priority.International_Air.toString());
 		trip.setParameter("volumePrice", "1");
 		trip.setParameter("weightPrice", "0");
 		
 		trip.execute("new");
 		
 		assertContains("\"status\":false", trip.getResponse().getOutputString());
 		
 		CustomerPrice expected = new CustomerPrice(new Location("rome", 1, 1, true), Direction.From, Priority.International_Air);
 		expected.setPricePerUnitVolume(1);
 		expected.setPricePerUnitWeight(1);
 		
 		verify(this.manager, never()).performEvent(new CustomerPriceUpdateEvent(expected));
 	}
 	
 	@Test
 	public void testAddWithZeroVolumeCostFails() throws Exception {
 		MockRoundtrip trip = new MockRoundtrip(this.context, CustomerPriceActionBean.class);
 		
 		trip.setParameter("location", "rome");
 		trip.setParameter("direction", Direction.To.toString());
 		trip.setParameter("priority", Priority.International_Air.toString());
 		trip.setParameter("volumePrice", "0");
 		trip.setParameter("weightPrice", "1");
 		
 		trip.execute("new");
 		
 		assertContains("\"status\":false", trip.getResponse().getOutputString());
 		
 		CustomerPrice expected = new CustomerPrice(new Location("rome", 1, 1, true), Direction.From, Priority.International_Air);
 		expected.setPricePerUnitVolume(1);
 		expected.setPricePerUnitWeight(1);
 		
 		verify(this.manager, never()).performEvent(new CustomerPriceUpdateEvent(expected));
 	}
 }
