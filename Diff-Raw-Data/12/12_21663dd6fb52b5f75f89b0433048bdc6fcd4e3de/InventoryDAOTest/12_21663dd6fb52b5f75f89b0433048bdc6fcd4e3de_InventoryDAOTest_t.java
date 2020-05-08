 package tests.database;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.junit.*;
 
 import adapters.db.sqlite.inventory.InventoryDAO;
 import adapters.db.sqlite.inventory.InventoryEntry;
 import adapters.db.sqlite.upcMap.UPCDAO;
 import adapters.db.sqlite.upcMap.UPCEntry;
 
 import Config.Config.ModeType;
 import Config.Statics;
 
 
 public class InventoryDAOTest {
 	UPCDAO upcd = null;
 	InventoryDAO invd = null;
 	
 	UPCEntry entry = null;
 	
 	@Before
 	public void setUpBeforeClass() throws Exception {
 		new File(Statics.TestDatabasePath).delete();
 		InputStream in = new FileInputStream(Statics.CleanDatabasePath);
 		OutputStream out = new FileOutputStream(Statics.TestDatabasePath);
 		byte[] buf = new byte[1024];
 		int len;
 		while ((len = in.read(buf)) > 0) {
 		   out.write(buf, 0, len);
 		}
 		in.close();
 		out.close(); 
 		Config.Config.Mode = ModeType.Test;
 		
 		upcd = UPCDAO.getInstance();
 
 		entry = new UPCEntry("000120000","Whatever","EACH");
 		
 		upcd.addEntry(entry, "user");
 		
		entry = upcd.lookUp(entry.getUPC());
		
 		invd = InventoryDAO.getInstance();
 	}
 
 	@After
 	public void tearDownAfterClass() throws Exception {
 		new File(Statics.TestDatabasePath).delete();
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void addEntryNullTest() {
 		invd.addEntry(null);
 	}
 	
 	@Test
 	public void addEntry() {
 		InventoryEntry inve = new InventoryEntry(entry, System.currentTimeMillis());
 		assertTrue(invd.addEntry(inve));
 	}
 	
 	@Test
 	public void addDuplicateEntry() {
 		InventoryEntry inve = new InventoryEntry(entry, System.currentTimeMillis());
 		assertTrue(invd.addEntry(inve));
 		assertTrue(invd.addEntry(inve));
 	}
 	
 	@Test
 	public void lookupEntry() {
 		long currTime = System.currentTimeMillis();
 		InventoryEntry inve = new InventoryEntry(entry, currTime);
 		assertTrue(invd.addEntry(inve));
		assertEquals(1, invd.lookUp(currTime/1000 - 100, currTime/1000 + 100).size());
 	}
 	
 	@Test
 	public void lookupIncorrectTimeEntry() {
 		long currTime = System.currentTimeMillis();
 		InventoryEntry inve = new InventoryEntry(entry, currTime);
 		assertTrue(invd.addEntry(inve));
 		
 		assertTrue(invd.lookUp(0, 0).size() == 0);
 	}
 	
 	@Test
 	public void lookupBoundaryLowerTimeEntry() {
 		long currTime = System.currentTimeMillis();
 		InventoryEntry inve = new InventoryEntry(entry, currTime);
 		assertTrue(invd.addEntry(inve));
 		
 		assertTrue(invd.lookUp(currTime, currTime + 10).size() > 0);
 	}
 	
 	@Test
 	public void lookupBoundaryUpperTimeEntry() {
 		long currTime = System.currentTimeMillis();
 		InventoryEntry inve = new InventoryEntry(entry, currTime);
 		assertTrue(invd.addEntry(inve));
 		
 		assertTrue(invd.lookUp(currTime - 10, currTime).size() > 0);
 	}
 	
 	@Test
 	public void lookupBoundarySingleTimeEntry() {
 		long currTime = System.currentTimeMillis();
 		InventoryEntry inve = new InventoryEntry(entry, currTime);
 		assertTrue(invd.addEntry(inve));
 		
 		assertTrue(invd.lookUp(currTime, currTime).size() > 0);
 	}
 }
