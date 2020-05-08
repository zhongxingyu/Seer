 package ms.server.database;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 import java.util.Random;
 
 import ms.common.domain.Auftrag;
import ms.common.domain.MedienSammlung;
 import ms.server.domain.ServerAuftrag;
 import ms.server.domain.ServerExportMedium;
import ms.server.domain.ServerMedienSammlung;

 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CreateDeleteDomainObjectsTest {
 	
 	private static int randomint;
 
 	@Before
 	public void setUp() throws Exception {
 		Random generator = new Random();
 		randomint = generator.nextInt();
 				
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		
 	}
 
 	@Test
 	public void testAuftragDbTable() {
 		ServerAuftrag myAuftrag = new ServerAuftrag(randomint);
 		
 		myAuftrag.save();
 		
 		
 		String sql = "select * from Auftrag WHERE id = " + myAuftrag.getID();
 		List<ServerAuftrag> myList = ActiveRecordManager.getObjectList(sql, ServerAuftrag.class);
 		
 		for (Auftrag item: myList) assertEquals(myAuftrag, item);
 		
 		myAuftrag.delete();
 		assertTrue(ActiveRecordManager.getObjectList(sql, ServerAuftrag.class).isEmpty());
 	}
 	
 	
 	
 	
 	
 
 	
 	
 }
