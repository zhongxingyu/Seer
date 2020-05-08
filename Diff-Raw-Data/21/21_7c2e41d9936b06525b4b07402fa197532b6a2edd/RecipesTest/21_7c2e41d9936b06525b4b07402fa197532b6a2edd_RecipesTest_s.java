 package ruffkat.hombucha.store;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.Rollback;
import ruffkat.hombucha.util.CalendarUtils;
 import ruffkat.hombucha.model.Recipe;
 
 import javax.persistence.EntityNotFoundException;
 import java.util.Calendar;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 public class RecipesTest extends FunctionalTest {
     @Autowired
     private Recipes recipes;
 
     @Test
     @Rollback(false)
     public void testSaveAndLoad() {
         Recipe recipe = recipes.create();
         recipe.setName("SCOBY DO");
         recipe.setReceived(CalendarUtils.date(Calendar.MAY, 12, 2011));
 
         entityManager.persist(recipe);
 
         Long id = recipe.getId();
 
         assertNotNull(id);
         assertEquals(recipe, recipes.load(id));
     }
 
     @Test
     @Rollback(false)
     public void testSaveAndDelete() {
         Recipe recipe = recipes.create();
         recipe.setName("SCOBY DO");
         recipe.setReceived(CalendarUtils.date(Calendar.MAY, 12, 2011));
 
         entityManager.persist(recipe);
 
         Long id = recipe.getId();
 
         recipe = recipes.load(id);
         recipes.delete(recipe);
 
         try {
             recipes.load(id);
             fail("expected an exception");
         } catch (EntityNotFoundException e) {
         }
     }
 }
