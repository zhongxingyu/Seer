 import org.junit.*;
 import java.util.*;
 import play.test.*;
 import models.Budget;
 
 import play.modules.morphia.Blob;
 import play.modules.morphia.MorphiaPlugin;
 
 public class BudgetTest extends UnitTest {
   
   @Test
   public void aVeryImportantThingToTest() {
     assertEquals(2, 1 + 1);
   }
 
   @Test
   public void testStoreBudget() {
     Budget b = new Budget("derp", "derping around town", 0, 100, "derp");
     b.save();
   }
 }
