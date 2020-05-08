 package au.edu.qut.inn372.greenhat.dao;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.qut.inn372.greenhat.bean.Equipment;
 import au.edu.qut.inn372.greenhat.dao.gae.EquipmentDAOImpl;
 
 public class EquipmentDAOTest {
 	EquipmentDAO dao;
 	@Before
 	public void setUp() throws Exception {
 		dao = new EquipmentDAOImpl();
 	}
 	
 	@Test
 	public void testGetEquipments() {
 		List<Equipment> equipments = dao.getEquipments();
 		Equipment[] list = new Equipment[equipments.size()];
 		equipments.toArray(list);
		assertEquals(equipments.size(), 3);
 		
 	}
 }
