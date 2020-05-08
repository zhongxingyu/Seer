 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package badm.models;
 
 import badm.Accounts;
import badm.Audit;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author trescenzi
  */
 public class AuditTest {
    Audit audit;
     public AuditTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         if (!Accounts.login()) {
 		fail("Could not login.");
             }
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
        audit = new Audit();
        audit.setDescription("I'm an audit");
       // audit.set
     }
     
     @After
     public void tearDown() {
     }
     // TODO add test methods here.
     // The methods must be annotated with annotation @Test. For example:
     //
     // @Test
     // public void hello() {}
 }
