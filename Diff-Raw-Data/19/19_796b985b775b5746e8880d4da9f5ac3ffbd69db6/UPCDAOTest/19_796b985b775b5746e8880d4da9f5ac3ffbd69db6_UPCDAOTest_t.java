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
 
 import Config.Statics;
 import Config.Config.ModeType;
 
 
 public class UPCDAOTest {
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
 	}
 
 	@After
 	public void tearDownAfterClass() throws Exception {
 		new File(Statics.TestDatabasePath).delete();
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void addEntryNullTest() {
 		upcd.addEntry(null, null);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void addEntryNullSourceTest() {
 		upcd.addEntry(entry, null);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void addEntryNullEntryTest() {
 		upcd.addEntry(null, "user");
 	}
 	
 	@Test
 	public void addEntryUser() {
 		System.out.println(upcd.lookUp("000120000"));
 		assertTrue(upcd.addEntry(entry, "user"));
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void addEntryEmptySource() {
 		upcd.addEntry(entry, "");
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void addEntryWrongSource() {
 		upcd.addEntry(entry, "thisisfake");
 	}
 	
 	@Test
 	public void addDuplicateEntry() {
 		assertTrue(upcd.addEntry(entry, "user"));
 		assertFalse(upcd.addEntry(entry, "user"));
 	}
 	
 	@Test
 	public void addDuplicateDiffSourceEntry() {
 		assertTrue(upcd.addEntry(entry, "user"));
 		assertFalse(upcd.addEntry(entry, "online"));
 	}
 	
 	@Test
 	public void lookupEntry() {
 		assertTrue(upcd.addEntry(entry, "user"));
 		
 		assertTrue(upcd.lookUp(entry.getUPC()) != null);
 	}
 	
 	@Test
 	public void lookupIncorrectUPC() {
 		assertTrue(upcd.lookUp("000000") == null);
 	}
 }
