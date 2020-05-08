 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package thesis.machine;
 
 import thesis.core.*;
 import com.db4o.ObjectSet;
 import com.db4o.ObjectContainer;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import thesis.core.Cobject;
 import thesis.core.Csoul;
 
 /**
  *
  * @author Daniel <dmilith> Dettlaff
  */
 public class ObjectIOTest {
 
     private ObjectIO myobj;
     private Cobject root;
     private Csoul player;
     private Cbody body, body2;
 
     public ObjectIOTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
         myobj = new ObjectIO();
         myobj.createConnectionToDb( "my_body_and_soul_database.db4o" );
 
         root = new Cobject(
             "00000000-0000-0000-0000-000000000000", // UUID
             "11111111-1111-1111-1111-111111111111" // parent UUID
             );
 
         player = new Csoul();
         player.setMana( 10 );
         player.setParent( root.getUUID() );
 
         body = new Cbody();
         body.setSoul( player ); // TODO: it should automaticly set player as parent
         body.setParent( player.getUUID() );
 
         body2 = new Cbody(); // we want two bodies! ;>
         body2.setSoul( player );
         body2.setParent( player.getUUID() );
 
         System.out.println("saving root");
         myobj.save( root );
         System.out.println("saving player");
         myobj.save( player );
         System.out.println("saving body2");
         myobj.save( body );
         System.out.println("saving body2");
         myobj.save( body2 );
     }
 
     @After
     public void tearDown() {
         myobj.closeConnectionToDb();
     }
 
     /**
      * Test of getDb method, of class ObjectIO.
      */
     @Test
     public void testGetDb() {
     }
 
     /**
      * Test of createConnectionToDb method, of class ObjectIO.
      */
     @Test
     public void testCreateConnectionToDb() {
     }
 
     /**
      * Test of closeConnectionToDb method, of class ObjectIO.
      */
     @Test
     public void testDatabaseTransaction() {
         ObjectContainer test = myobj.getDb();
         
     }
 
     /**
      * Test of save method, of class ObjectIO.
      */
     @Test
     public void testSave() {
         assertEquals(player.getParent(), root.getUUID());
         assertEquals(body.getParent(), player.getUUID());
         assertEquals(body2.getParent(), player.getUUID());
         assertEquals(body.getSoul(), player);
     }
 
     /**
      * Test of loadByUUID method, of class ObjectIO.
      */
     @Test
     public void testLoadByUUID() {
         System.out.println("load_by_uuid");
         ObjectSet sett;
         sett = myobj.loadByUUID( player.getUUID() );
         while (sett.hasNext()) {
            Cobject mee = (Cobject)sett.next();
               System.out.println( "DEBUG:GOT-> RC" + mee.getClass() + ", TC" + mee.getObjectType() + ", #" +
                    mee.getUUID() + ", %"  + mee.getParent() + ", @" + mee.getCreatedAt() );
               assertEquals( mee.getClass(), mee.getObjectType() );
               assertNotNull( mee.getCreatedAt() );
               assertNotNull( mee.getObjectType() );
               assertNotNull( mee.getUUID() );
               assertNotNull( mee.getParent() );
         }
         assertEquals(sett.size(), 1);
     }
 
     /**
      * Test of loadByParent method, of class ObjectIO.
      */
     @Test
     public void testLoadByParent() {
         System.out.println("load_by_parent");
         ObjectSet sett;
         sett = myobj.loadByParent( player.getUUID() );
         while (sett.hasNext()) {
            Cobject mee = (Cobject)sett.next();
               System.out.println( "DEBUG:GOT-> RC" + mee.getClass() + ", TC" + mee.getObjectType() + ", #" +
                    mee.getUUID() + ", %"  + mee.getParent() + ", @" + mee.getCreatedAt() );
               assertEquals( mee.getClass(), mee.getObjectType() );
               assertNotNull( mee.getCreatedAt() );
               assertNotNull( mee.getObjectType() );
               assertNotNull( mee.getUUID() );
               assertNotNull( mee.getParent() );
         }
     }
 
     /**
      * Test of loadAll method, of class ObjectIO.
      */
     @Test
     public void testLoadAll() {
         System.out.println("load_by_parent");
         ObjectSet sett = myobj.loadByType( root );
         assertTrue( sett.size() > 0 );
         while (sett.hasNext()) {
            Cobject mee = (Cobject)sett.next();
               System.out.println( "DEBUG:GOT-> RC" + mee.getClass() + ", TC" + mee.getObjectType() + ", #" +
                    mee.getUUID() + ", %"  + mee.getParent() + ", @" + mee.getCreatedAt() );
               assertEquals( mee.getClass(), mee.getObjectType() );
               assertNotNull( mee.getCreatedAt() );
               assertNotNull( mee.getObjectType() );
               assertNotNull( mee.getUUID() );
               assertNotNull( mee.getParent() );
         }
     }
 
     /**
      * Test of load method, of class ObjectIO.
      */
     @Test
     public void testLoadByType() {
         System.out.println("load");
           System.out.println( "\nDEBUG: Getting all objects:" );
 
             ObjectSet sett;
             sett = myobj.loadByType( root );
             while (sett.hasNext()) {
               Cobject mee = (Cobject)sett.next();
               assertNotNull( mee.getClass() );
               System.out.println( "DEBUG:GOT-> RC-" + mee.getClass() + ",TC-" + mee.getObjectType() + ", #-" +
                     mee.getUUID() + ", %-"  + mee.getParent() + ", @-" + mee.getCreatedAt() );
               assertNotNull( mee.getCreatedAt() );
               assertNotNull( mee.getObjectType() );
               assertNotNull( mee.getUUID() );
               assertNotNull( mee.getParent() );
             }
 
             ObjectSet sett2;
             sett2 = myobj.loadByType( player );
             while (sett2.hasNext()) {
               Cobject mee = (Cobject)sett2.next();
               System.out.println( "DEBUG:GOT-> RC-" + mee.getClass() + ",TC-" + mee.getObjectType() + ", #-" +
                     mee.getUUID() + ", %-"  + mee.getParent() + ", @-" + mee.getCreatedAt() );
               assertNotNull( mee.getCreatedAt() );
               assertNotNull( mee.getObjectType() );
               assertNotNull( mee.getUUID() );
               assertNotNull( mee.getParent() );
             }
 
 
     }
 
    /**
     * Test of objectCount method, of class ObjectIO.
     */
    @Test
    public void testObjectCount() {
        assertTrue( myobj.objectCount() > 3 );
        System.out.println( "\n\n\nObjects in database: " + myobj.objectCount() );
    }
 
 }
