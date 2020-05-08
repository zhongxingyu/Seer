 package ca.thewhitakers.recipebook.persistence;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import ca.thewhitakers.recipebook.domain.model.DomainFixture;
 import ca.thewhitakers.recipebook.domain.model.FoodItem;
 import ca.thewhitakers.recipebook.domain.model.Ingredient;
 import ca.thewhitakers.recipebook.domain.model.Measurement;
 import ca.thewhitakers.recipebook.domain.model.Recipe;
 import ca.thewhitakers.recipebook.domain.model.RecipeInfo;
 import ca.thewhitakers.recipebook.domain.model.RecipeInfoType;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({ "/app-config.xml" })
 @Transactional
 public class RecipeRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests{
 	@Autowired
 	private RecipeRepository repository;
 	
 	@Autowired
 	private SessionFactory sessionFactory;
 	
 	private Session getSession() {
 		return sessionFactory.getCurrentSession();
 	}
 	
 	@Test
 	public void shouldLoadAllRecipes() {
 		List<Recipe> recipes = repository.getAll();
 		
 		Assert.assertEquals(2, recipes.size());
 		
 		for (Recipe r : recipes) {
 			
 			for (RecipeInfo ri : r.getRecipeInfos()) {
 				Assert.assertNotNull(ri);
 				Assert.assertNotNull(ri.getValue());
 				Assert.assertNotNull(ri.getId());
 				
 				System.out.println(ri.getValue());
 			}
 		}
 	}
 	
 //	private Recipe createPancakesRecipe() {
 //		Session session = getSession();
 //		
 //		// Create some recipe information types
 //		RecipeInfoType description = new RecipeInfoType("Description");
 //		RecipeInfoType author = new RecipeInfoType("Author");
 //
 //		session.save(description);
 //		session.save(author);
 //		
 //		// Create some measurements
 //		Measurement cup = new Measurement();
 //		cup.setName("Cup");
 //		
 //		Measurement tbsp = new Measurement();
 //		tbsp.setName("Tbsp");
 //		
 //		session.save(cup);
 //		session.save(tbsp);
 //		
 //		// Create some food items
 //		FoodItem flour = new FoodItem("Flour");
 //		FoodItem sugar = new FoodItem("Sugar");
 //		
 //		session.save(flour);
 //		session.save(sugar);
 //		
 //		Recipe pancakes = new Recipe();
 //		pancakes.setName("Pancakes");
 //		pancakes.addInfo(description, "An awesome recipe");
 //		pancakes.addInfo(author, "Jason Whitaker");
 //		
 //		pancakes.addIngredient(2, cup, flour);
 //		pancakes.addIngredient(2, tbsp, sugar);
 //		
 //		return pancakes;
 //	}
 //	
 //	@Test
 //	public void shouldSaveRecipe() {
 //		Recipe pancakes = createPancakesRecipe();
 //		
 //		repository.save(pancakes);
 //		getSession().flush();
 //		
 //		Recipe foundRecipe = repository.getById(pancakes.getId());
 //		Assert.assertNotNull(foundRecipe);
 //		
 //		Collection<RecipeInfo> properties = foundRecipe.getRecipeInfos();
 //		Assert.assertEquals(2, properties.size());
 //		
 //		ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>(foundRecipe.getIngredients());
 //		Assert.assertEquals(2.0f, ingredients.get(0).getQuantity());
 //		Assert.assertEquals("Cup", ingredients.get(0).getMeasurement().getName());
 //		Assert.assertEquals("Flour", ingredients.get(0).getFoodItem().getName());
 //		Assert.assertEquals(2, ingredients.size());
 //		
 //		sessionFactory.getCurrentSession().flush();
 //		
 //		Assert.assertEquals(1, countRowsInTable("recipe"));
 //		Assert.assertEquals(2, countRowsInTable("recipeinfo"));
 //		Assert.assertEquals(2, countRowsInTable("ingredient"));
 //		Assert.assertEquals(2, countRowsInTable("recipeinfotype"));
 //		Assert.assertEquals(2, countRowsInTable("fooditem"));
 //		Assert.assertEquals(2, countRowsInTable("measurement"));
 //	}
 //	
 //	@Test
 //	public void saveIngredientWithNoMeasurement() {
 //		getSession().save(DomainFixture.getFoodItems("Bayleaves"));
 //		
 //		Recipe recipe = new Recipe();
 //		recipe.addIngredient(2, DomainFixture.getFoodItems("Bayleaves"));
 //		
 //		repository.save(recipe);
 //		getSession().flush();
 //		
 //		Recipe foundRecipe = repository.getById(recipe.getId());
 //		
 //		ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>(foundRecipe.getIngredients());
 //		Assert.assertEquals(1, ingredients.size());
 //		Assert.assertNull(ingredients.get(0).getMeasurement());
 //	}
 //	
 //	@Test
 //	public void shouldDeleteRecipe() {
 //		Recipe pancakes = createPancakesRecipe();
 //		
 //		repository.save(pancakes);
 //		repository.delete(pancakes);
 //		getSession().flush();
 //		
 //		Assert.assertEquals(0, countRowsInTable("recipe"));
 //		Assert.assertEquals(0, countRowsInTable("recipeinfo"));
 //		Assert.assertEquals(0, countRowsInTable("ingredient"));
 //		Assert.assertEquals(2, countRowsInTable("recipeinfotype"));
 //		Assert.assertEquals(2, countRowsInTable("fooditem"));
 //		Assert.assertEquals(2, countRowsInTable("measurement"));
 //	}
 }
