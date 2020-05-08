 package de.splitstudio.androidb;
 
 import java.io.File;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.test.AndroidTestCase;
 
 public class MetadataTest extends AndroidTestCase {
 
 	private SQLiteDatabase db;
 
 	private File dbFile;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		String dbFilename = "test.db";
 		dbFile = getContext().getDatabasePath(dbFilename);
 		dbFile.delete();
		Table.openOrCreateDB(getContext());
 		assertTrue(db.isOpen());
 		assertFalse(db.isReadOnly());
 		//assertTrue(db.isDbLockedByCurrentThread());
 		assertFalse(db.isDbLockedByOtherThreads());
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		db.close();
 	}
 
 	public void testConstructor_createMetadataTable() {
 		Table table = new TableExample();
 		Metadata metadata = new Metadata();
		assertTrue(metadata.findByName("TableColumnWithAnnotations"));
 		assertEquals(table.getVersion(), metadata.getTableVersion());
 	}
 }
