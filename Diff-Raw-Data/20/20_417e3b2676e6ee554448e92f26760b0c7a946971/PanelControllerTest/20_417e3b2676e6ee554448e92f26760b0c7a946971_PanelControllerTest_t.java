 package com.qut.spc.controller;
 
 
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

 import org.junit.Before;
 import org.junit.Test;
 
 import com.qut.spc.model.PanelFilterAPI;
 
 /**
  * Tests for PanelController
  * @author Simen
  *
  */
 public class PanelControllerTest {
 	
 	private PanelFilterAPI db;
 	private PanelController controller;
 
 	@Before
 	public void setup(){
 		db = mock(PanelFilterAPI.class);
 		controller = new PanelController(db);		
 	}
 	
 	@Test
 	public void getPanelsByPriceCapacityLocation_validValues_setMaxPriceOnFilterIsCalled() throws Exception{
 		controller.getPanelsByPriceCapacityLocation(1, 2, 3, 4, "2222");
 		verify(db).setMaxPrice(2);
 	}
 
 	@Test
 	public void getPanelsByPriceCapacityLocation_validValues_setMinPriceOnFilterIsCalled()throws Exception{
 		controller.getPanelsByPriceCapacityLocation(1, 2, 3, 4, "2222");
 		verify(db).setMinPrice(1);
 	}
 
 	@Test
 	public void getPanelsByPriceCapacityLocation_validValues_setMaxCapacityOnFilterIsCalled()throws Exception{
 		controller.getPanelsByPriceCapacityLocation(1, 2, 3, 4, "2222");
 		verify(db).setMaxCapacity(4);
 	}
 
 	@Test
 	public void getPanelsByPriceCapacityLocation_validValues_setMinCapacityOnFilterIsCalled()throws Exception{
 		controller.getPanelsByPriceCapacityLocation(1, 2, 3, 4, "2222");
 		verify(db).setMinCapacity(3);
 	}
 
 	@Test
 	public void getPanelsByPriceCapacityLocation_validValues_setLocationOnFilterIsCalled()throws Exception{
 		controller.getPanelsByPriceCapacityLocation(1, 2, 3, 4, "2222");
 		verify(db).setPostcode("2222");
 	}
 
 	@Test
 	public void getPanelsByPriceCapacityLocation_validValues_searchOnFilterIsCalled()throws Exception{
 		controller.getPanelsByPriceCapacityLocation(1, 2, 3, 4, "2222");
 		verify(db).search();
 	}
 	
 }
