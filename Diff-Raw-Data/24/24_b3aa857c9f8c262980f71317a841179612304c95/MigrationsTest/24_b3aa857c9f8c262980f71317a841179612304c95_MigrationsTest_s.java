 package ca.ottawaandroid.velvet.test;
 
 import ca.ottawaandroid.velvet.migrations.MigrationSet;
 import ca.ottawaandroid.velvet.migrations.CreateTable;
 import ca.ottawaandroid.velvet.migrations.AlterTable;
 import ca.ottawaandroid.velvet.migrations.DropTable;
 import android.database.Cursor;
 
 public class MigrationsTest extends DbTestCase {
     
     public void setUp() throws Exception {
 	super.setUp();
     }
 
     public void tearDown() throws Exception {
 	super.tearDown();
     }
 
     public void testApplyVersion() {
 	MigrationSet migrations = new MigrationSet(){{
 	    version(1);
 	}};
 	migrations.apply(mDb);
 
 	assertEquals(1, getDatabaseVersion());
     }
 
     public void testCreateTable() {
 	MigrationSet migrations = new MigrationSet(){{
 	    version(1);
 	    add( new CreateTable("table1", "c", "d", "e"));
 	}};
 	migrations.apply(mDb);
 
 	assertEquals(1, getDatabaseVersion());
 	assertEntryCount(0, "table1");
     }
 
     public void testAddVersionWithMigration(){
 	MigrationSet migrations = new MigrationSet(){{
 	    add(1, new CreateTable("table1", "c", "d", "e"));
 	}};
 	migrations.apply(mDb);
 
 	assertEquals(1, getDatabaseVersion());
 	assertEntryCount(0, "table1");
     }
 
     public void testVersionMigrations(){
 	MigrationSet migrations = new MigrationSet(){{
 	    version(1);
 	    add( new CreateTable("table1", "c", "d", "e"));
 	    version(2);
 	    add(new CreateTable("table2", "f", "g"));
 	}};
 
 	migrations.apply(mDb, 0, 1);
 	assertEntryCount(0, "table1");
 	Cursor c = mDb.rawQuery("SELECT tbl_name FROM sqlite_master WHERE type='table' and tbl_name='table2'", null);
 	try {
 	    assertEquals(0, c.getCount());
 	} finally {
 	    c.close();
 	}
 
 	// Migrations should automatically where to continue
 	// from if there is an incomplete migration.
 	migrations.apply(mDb);
 	{
 	    assertEntryCount(0, "table1");
 	    assertEntryCount(0, "table2");
 	}
     }
 
     public void testAlterTable(){
 	MigrationSet migrations = new MigrationSet(){{
 	    version(1);
 	    add( new CreateTable("table1", "c", "d", "e") );
 	    version(2);
 	    add( 
 		new AlterTable("table1")
 		{{
 		    addColumn("f");
 		}});
 	}};
 	migrations.apply(mDb);
 	{
 	    // Table alterations add extra spaces to the generated schema SQL, which
 	    // explains why 'f' has extra spaces.
 	    String expected = "CREATE TABLE table1 ( c TEXT,d TEXT,e TEXT , f TEXT)";
 	    assertExpectedSQL(expected, "table1");
 	}
     }
 
     public void testDropTable(){
 	MigrationSet migrations = new MigrationSet(){{
 	    version(1);
 	    add(new CreateTable("table1", "c", "d", "e"));
 	    version(2);
 	    add(new DropTable("table1"));
 	}};
 	migrations.apply(mDb, 0, 1);
 
 	Cursor c= mDb.rawQuery("SELECT tbl_name FROM sqlite_master WHERE type='table' and tbl_name='table1'", null);
 	try{
 	    assertEquals(1, c.getCount());
 	} finally {
 	    c.close();
 	}
 
 	migrations.apply(mDb, 1, 2);
 	Cursor d= mDb.rawQuery("SELECT tbl_name FROM sqlite_master WHERE type='table' and tbl_name='table1'", null);
 	try{
	    assertEquals(0, c.getCount());
 	} finally {
 	    d.close();
 	}
     }
 
     // Helpers
 
     public int getDatabaseVersion(){
 	Cursor c = mDb.query("DATABASE_VERSION", null, null, null, null, null, null, null);
 	c.moveToFirst();
 	int version = c.getInt(c.getColumnIndex("CURRENT_VERSION"));
 	c.close();
 	return version;
     }
 
     public Cursor getAllForTable(String tbl){
 	return mDb.query(tbl, null, null, null, null, null, null, null);
     }
 
     public Cursor getTableSchema(String tbl){
 	return mDb.rawQuery(
 			    "SELECT sql FROM sqlite_master WHERE tbl_name=?",
 			    new String[] { tbl }
 			    );
     }
 
     public void assertEntryCount(int count, String tbl){
 	Cursor c = getAllForTable(tbl);
 	try {
 	    assertEquals(count, c.getCount());
 	} finally {
 	    c.close();
 	}
     }
 
     public void assertExpectedSQL(String expected, String tbl){
 	Cursor c = getTableSchema(tbl);
 	String tblSchema = "";
 	try {
 	    c.moveToFirst();
 	    tblSchema = c.getString(c.getColumnIndex("sql"));
 	} finally {
 	    c.close();
 	}
 	assertEquals(expected, tblSchema);
     }
 }
