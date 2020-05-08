 package edu.chl.dat076.foodfeed.model.dao;
 
 import edu.chl.dat076.foodfeed.exception.ResourceNotFoundException;
 import edu.chl.dat076.foodfeed.model.entity.Grocery;
 import edu.chl.dat076.foodfeed.model.entity.Ingredient;
 import edu.chl.dat076.foodfeed.model.entity.Recipe;
 import java.util.ArrayList;
 import java.util.List;
 import junit.framework.Assert;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
     "classpath*:spring/root-context.xml",
     "classpath*:spring/security-context.xml"})
 @Transactional
 public class RecipeDaoTest {
 
     @Autowired
     RecipeDao recipeDao;
     
     public Recipe recipe;
 
     /*
      * Creates a Recipe Object to be used in tests
      */
     private Recipe createTestRecipeObject(){
        List<Ingredient> ingredients = new ArrayList();
         ingredients.add(new Ingredient(new Grocery("Red pepper", "Swedish red pepper"), 2.0, "stycken"));
         ingredients.add(new Ingredient(new Grocery("Water", "Tap water"), 20.0, "liter"));
         
         Recipe recipe = new Recipe();
         recipe.setDescription("Best soup in the world");
         recipe.setName("Soup");
         recipe.setIngredients(ingredients);
         recipe.setInstructions("Add all ingredients");
         return recipe;
     }
     
     @Before
     public void createRecipe(){
         recipe = createTestRecipeObject();
         recipeDao.create(recipe);
     }
     
     @Test
     public void testCreate(){
         Assert.assertNotNull("recipe could not be created", recipe.getId());
     }
     
     @Test(expected = ResourceNotFoundException.class)
     public void testDelete(){
         recipeDao.delete(recipe);
         Assert.assertNull("recipe removed", recipeDao.find(recipe.getId()));
     }
     
     @Test(expected = ResourceNotFoundException.class)
     public void testDeleteID(){
         recipeDao.delete(recipe.getId());
         Assert.assertNull("recipe not removed", recipeDao.find(recipe.getId()));
     }
     
     @Test
     public void testFind(){
         Recipe result = recipeDao.find(recipe.getId());
         Assert.assertNotNull("recipe not found", result);
     }
     
     @Test
     public void testFindAll() {
         List<Recipe> recipes = recipeDao.findAll();
         assertFalse("Check that true is true", recipes.isEmpty());
     }
     
     @Test
     public void testUpdate(){
         Recipe old = new Recipe();
         old.setName(recipe.getName());
         recipe.setName("New name");
         recipeDao.update(recipe);
         Assert.assertNotSame("Recipe not updated", recipe.getName(), old.getName());
     }
     
     @Test
     public void testGetByIngredient(){
         List<Recipe> result = recipeDao.getByIngredient(recipe.getIngredients().get(0));
         Assert.assertFalse("found no recipe", result.isEmpty());
     }
     
     @Test
     public void testGetByName(){
         List<Recipe> result = recipeDao.getByName(recipe.getName());
         Assert.assertFalse("found no recipe", result.isEmpty());
     }
 }
