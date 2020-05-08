 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import models.User;
 import models.Transaction;
 import models.Budget;
 
 import play.modules.morphia.Blob;
 import play.modules.morphia.MorphiaPlugin;
 
 public class TransactionTest extends UnitTest {
 
     @Test
     public void aVeryImportantThingToTest() {
         assertEquals(2, 1 + 1);
     }
 
     @Test
     public void testStoreTransaction() {
 	  Budget b = new Budget("derp", "derping around town", 0, 100, "derp");
 	  User u = new User();
      Transaction t = new Transaction(b, u, 0, "derp", 0, 0, 1);
      t.save();
 
      t.delete();
 	  b.delete();
 	  u.delete();
     }
   }
