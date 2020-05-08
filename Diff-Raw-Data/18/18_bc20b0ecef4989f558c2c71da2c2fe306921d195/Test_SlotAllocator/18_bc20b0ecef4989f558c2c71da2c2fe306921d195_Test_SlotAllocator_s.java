 package at.ac.tuwien.dse.allocator.controller;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import at.ac.tuwien.dse.core.db.HospitalDAO;
 import at.ac.tuwien.dse.core.model.Hospital;
 import at.ac.tuwien.dse.core.model.TimeSlot;
 
 
 public class Test_SlotAllocator extends Test_InsertTestData {
 	private static SlotAllocator sa;
 
 	@Test
 	public void testGetNearestSlot() {
 		sa = new SlotAllocator(db);
 		TimeSlot ts = sa.getNearestSlot(1, 1, 80d, 60);
 		assertNotNull(ts);
 		assertTrue((ts.getEnd().getTime() - ts.getStart().getTime())/1000/60 >= 60);
 		assertEquals((int)ts.getOperationTypeId(), 1);
 		HospitalDAO hDao = fact.getHospitalDAO();
 		Hospital h = hDao.findById(ts.getHospitalId());
 		assertEquals(h.getName(), "KH St. Poelten");
 		
 		ts = sa.getNearestSlot(3, 1, 20d, 90);
 		assertNotNull(ts);
 		assertTrue((ts.getEnd().getTime() - ts.getStart().getTime())/1000/60 >= 90);
 		assertEquals((int)ts.getOperationTypeId(), 1);
 		h = hDao.findById(ts.getHospitalId());
		assertEquals(h.getName(), "Barmherzige Brder Wien");
 		
 		ts = sa.getNearestSlot(2, 0, 150d, 30);
 		assertNull(ts);
 	}
 	
 	@Test
 	public void testGetNearestSlot_isEarliest() {
 		sa = new SlotAllocator(db);
 	}
 }
