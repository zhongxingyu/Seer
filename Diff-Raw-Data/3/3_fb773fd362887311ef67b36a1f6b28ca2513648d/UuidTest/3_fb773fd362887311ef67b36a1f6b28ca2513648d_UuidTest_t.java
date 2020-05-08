 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import com.mymed.utils.TimeUuid;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import java.util.UUID;
 //import com.eaio.uuid.*;
 
 /**
  *
  * @author peter
  */
 public class UuidTest {
 
     public UuidTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     // TODO add test methods here.
     // The methods must be annotated with annotation @Test. For example:
     //
     // @Test
     // public void hello() {}
 
     /**
      * Let's just make a few uuids, and test that consecutive uuids are different
      */
     @Test
     public void createUuid() {
        UUID lastId = null;
         for (int i = 0; i < 10; i++) {
             UUID id = TimeUuid.getTimeUUID();
             System.out.println(id);
             if (lastId != null)
                 assertFalse(lastId.equals(id));
             lastId = id;
         }
     }
 
     @Test
     public void uuidConversion() {
         for (int i = 0; i < 10; i++) {
             UUID id = TimeUuid.getTimeUUID();
             byte[] ba = TimeUuid.asByteArray(id);
             UUID id2 = TimeUuid.toUUID(ba);
             assertEquals(id, id2);
         }
     }
 }
