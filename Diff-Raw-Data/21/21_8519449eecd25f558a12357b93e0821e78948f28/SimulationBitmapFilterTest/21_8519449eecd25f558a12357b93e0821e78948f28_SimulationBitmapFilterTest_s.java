 package br.usp.ime.tcc.filter.simulation;
 
import static org.junit.Assert.*;
 import static org.junit.Assert.assertNull;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.robolectric.RobolectricTestRunner;
 
import br.usp.ime.tcc.filter.visocor.VisocorBitmapFilter;
 import br.usp.ime.tcc.utils.Constants;
 
 @RunWith(RobolectricTestRunner.class)
 public class SimulationBitmapFilterTest {
 	// Tests
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void deutFilterShouldReturnNullIfBitmapIsNull() {
 		SimulationBitmapFilter filter = new SimulationBitmapFilter(
 				Constants.DEUTERANOPIA_SIMULATION_FILTER);
 
 		assertNull(filter.applyTo(null));
 	}
 
 	@Test
 	public void protFilterShouldReturnNullIfBitmapIsNull() {
 		SimulationBitmapFilter filter = new SimulationBitmapFilter(
 				Constants.PROTANOPIA_SIMULATION_FILTER);
 
 		assertNull(filter.applyTo(null));
 	}
 
 	@Test
 	public void tritFilterShouldReturnNullIfBitmapIsNull() {
 		SimulationBitmapFilter filter = new SimulationBitmapFilter(
 				Constants.TRITANOPIA_SIMULATION_FILTER);
 
 		assertNull(filter.applyTo(null));
 	}
 
 	@Test
 	public void filterUpdateOnSimulationFilterShouldDoNothing() {
 		SimulationBitmapFilter filter = new SimulationBitmapFilter(
 				Constants.TRITANOPIA_SIMULATION_FILTER);
 		
 		filter.update(0);
 
 		assertTrue(true);
 	}
 }
