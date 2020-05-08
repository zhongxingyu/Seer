 package ca.ualberta.team2recipefinder.test;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 
 import ca.ualberta.team2recipefinder.model.*;
 
 /**
  * Tests RecipeModel class
  */
 public class RecipeModelTest extends TestCase {
 	
 	RecipeModel model;
 	
 	MyKitchen kitchen;
 
 	public void setUp() throws Exception {
 		model = new RecipeModel("model.sav");
 		
 		// we make sure we start with an empty model
 		model.getAllRecipes().removeAll(model.getAllRecipes());
 	}
 
 	public void testAdd()
 	{
 		Recipe recipe = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		
 		model.add(recipe);
 		
 		// make sure there is at least one recipe
 		assertEquals(1, model.getAllRecipes().size());
 		
 		// make sure it is the recipe we just added
 		assertEquals(1, model.searchRecipe(new String[] { "spaghetti" }).size());
 		
 		// make sure it is the recipe we just added
 		assertEquals("spaghetti", model.getRecipe(0).getName());
 		
 		// make sure it is the recipe we just added
 		assertEquals(recipe.getRecipeID(), model.getRecipeById(recipe.getRecipeID()).getRecipeID());
 	}
 	
 	public void testRemove()
 	{
 		Recipe recipe1 = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("rice", "test", "test", new ArrayList<Ingredient>(), false);
 		
 		model.add(recipe1);
 		model.add(recipe2);
 		
 		model.remove(recipe1);
 		
 		// make sure there is one recipe
 		assertEquals(1, model.getAllRecipes().size());
 		
 		// make sure the recipe that is in the list is the "rice"
 		assertEquals(1, model.searchRecipe(new String[] { "rice" }).size());
 		
 		// make sure the recipe that is the list is the "rice"
 		assertEquals("rice", model.getRecipe(0).getName());
 	}
 	
 	public void testAddRecipeTwice()
 	{
 		Recipe recipe1 = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		
 		model.add(recipe1);
 		model.add(recipe2);
 				
 		// make sure there is one recipe
 		assertEquals(2, model.getAllRecipes().size());
 		
 		// make sure there are two spaghetti's in the list
 		assertEquals(2, model.searchRecipe(new String[] { "spaghetti" }).size());
 		
 		// make sure both sppaghetti's are in the list
 		if (model.getRecipe(0).equals(recipe1)) {
 			assertEquals(recipe2, model.getRecipe(1));
 		}
 		else {
 			assertEquals(recipe2, model.getRecipe(0));
 			assertEquals(recipe1, model.getRecipe(1));
 		}
 	}
 	
 	public void testSearchKeyInName()
 	{
 		Recipe recipe1 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe3 = new Recipe("tomato_spaghetti_tomato", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe4 = new Recipe("tomato", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe5 = new Recipe("paghett", "", "", new ArrayList<Ingredient>(), false);
 		
 		model.add(recipe1);
 		model.add(recipe2);
 		model.add(recipe3);
 		model.add(recipe4);
 		model.add(recipe5);
 						
 		List<Recipe> spaghettis = model.searchRecipe(new String[] { "spaghetti" });
 		List<Recipe> tomatos = model.searchRecipe(new String[] { "tomato" });
 		
 		// if you search for "spaghetti", there must be three results
 		assertEquals(3, spaghettis.size());
 		
 		// and they should be recipes 1, 2 and 3
 		List<Recipe> expectedResults = new LinkedList<Recipe>();
 		expectedResults.add(recipe1);
 		expectedResults.add(recipe2);
 		expectedResults.add(recipe3);
 		checkExpectedResults(spaghettis, expectedResults);
 		
 		// if you search for "tomato", there must be two results
 		assertEquals(2, tomatos.size());
 		
 		// and they should be recipes 3 and 4
 		expectedResults.add(recipe3);
 		expectedResults.add(recipe4);
 		checkExpectedResults(tomatos, expectedResults);
 	}
 	
 	public void testSearchWithIngredients()
 	{
 		Ingredient ingredient1 = new Ingredient("tomato", 12.0, "unit");
 		Ingredient ingredient2 = new Ingredient("toma", 12.0, "unit"); // "toma" ==> substring of "tomato"
 		Ingredient ingredient3 = new Ingredient("bacon", 1.0, "unit");
 		Ingredient ingredient4 = new Ingredient("rice", 1.0, "unit");
 		Ingredient ingredient5 = new Ingredient("tomato", 5.0, "kg");
 		
 		Recipe recipe1 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe3 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe4 = new Recipe("rice", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe5 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		
 		// there is no rice and no toma's in the kitchen
 		ArrayList<Ingredient> kitchen = new ArrayList<Ingredient>();
 		kitchen.add(ingredient1); // there are 12 tomatos in the kitchen
 		kitchen.add(ingredient3); // there is 1 bacon (even though no recipe needs bacon)
 		
 		try {
 			// recipe1 ==> a spaghetti with 12 tomato's and 12 toma's
 			// shouldn't be a result -> there is no toma in the kitchen
 			recipe1.addIngredient(ingredient1);
 			recipe1.addIngredient(ingredient2);
 			
 			// recipe2 ==> a spaghetti with rice and 5 kg of tomato
 			// shouldn't be a result -> there is no rice in the kitchen
 			recipe2.addIngredient(ingredient4);
 			recipe2.addIngredient(ingredient5);
 			
 			// recipe3 ==> a spaghetti with nothing
 			// should be a result
 			// if the recipe has nothing, so we'll always have the ingredients to cook it
 
 			// recipe4 ==> rice that doesn't take rice, but 5 kg of tomato
 			// we're looking for rice (the recipe, not the ingredient)
 			// so this should be a result, because we have tomato's in the kitchen
 			recipe4.addIngredient(ingredient5);
 			
 			// recipe5 ==> a spaghetti that takes tomato's and no toma's
 			// should be a result ==> we have tomatos
 			recipe5.addIngredient(ingredient1);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		model.add(recipe1);
 		model.add(recipe2);
 		model.add(recipe3);
 		model.add(recipe4);
 		model.add(recipe5);
 						
 		List<Recipe> spaghettis = model.searchWithIngredient(new String[] { "spaghetti" }, kitchen);
 		List<Recipe> rice = model.searchWithIngredient(new String[] { "rice" }, kitchen);
 		
 		// if you search for "spaghetti", there must be two results
 		assertEquals(2, spaghettis.size());
 		
 		// and they should be recipes 3 and 5
 		List<Recipe> expectedResults = new LinkedList<Recipe>();
 		expectedResults.add(recipe3);
 		expectedResults.add(recipe5);
 		checkExpectedResults(spaghettis, expectedResults);
 		
 		// if you search for "rice", there must be one result
 		assertEquals(1, rice.size());
 		
 		// and it should be recipe 4
 		expectedResults.add(recipe4);
 		checkExpectedResults(rice, expectedResults);
 	}
 	
 	public void testReplace()
 	{
 		Recipe recipe1 = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("rice", "test", "test", new ArrayList<Ingredient>(), false);
 		
 		model.add(recipe1);
 		
 		model.replaceRecipe(recipe2, recipe1.getRecipeID());
 		
 		// make sure there is one recipe
 		assertEquals(1, model.getAllRecipes().size());
 		
 		// make sure the recipe that is in the list is the "rice"
 		assertEquals(1, model.searchRecipe(new String[] { "rice" }).size());
 		
 		// make sure the recipe that is in the list is the "rice"
 		assertEquals("rice", model.getRecipe(0).getName());
 	}
 	
 	public void getNextOrPrevRecipeById() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
 	{
 		Recipe recipe1 = new Recipe("id1", "test", "test", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("id2", "test", "test", new ArrayList<Ingredient>(), false);
 		Recipe recipe3 = new Recipe("id3", "test", "test", new ArrayList<Ingredient>(), false);
 				
 		Field id = recipe1.getClass().getDeclaredField("id");
 		id.setAccessible(true);
 		
 		// uses reflection to force id's
 		id.setLong(recipe1, 1);
 		id.setLong(recipe2, 2);
 		id.setLong(recipe3, 3);
 				
 		// adds them in reverse order, so that we can test
 		// if it'll use the id or the position in the list
 		model.add(recipe3);
 		model.add(recipe2);
 		model.add(recipe1);
 		
 		// after recipe 2, recipe 3
 		assertEquals(recipe3, model.getNextRecipeById(recipe2.getRecipeID()));
 		
 		// after recipe 3, recipe 1
 		assertEquals(recipe1, model.getNextRecipeById(recipe3.getRecipeID()));
 		
 		// before recipe 2, recipe 1
 		assertEquals(recipe1, model.getPreviousRecipeById(recipe2.getRecipeID()));
 		
 		// before recipe 1, recipe 3
 		assertEquals(recipe3, model.getPreviousRecipeById(recipe1.getRecipeID()));
 	}
 	
 	public void testGetRecipeBySeverId() {
 		Recipe recipe = new Recipe();
 		String serverId = "TestingRecipe";
 		recipe.setServerId(serverId);
 		model.add(recipe);
 		assertTrue("Get Recipe By Server ID", recipe.equals(model.getRecipeByServerId(serverId)));
 		model.remove(recipe);
 	}
 	
 	public void testRemoveAll() {
 		model.add(new Recipe());
 		model.add(new Recipe());
 		model.removeAll();
 		assertEquals("Remove all", 0, model.getAllRecipes().size());
 	}
 	private void checkExpectedResults(List<Recipe> results, List<Recipe> expectedResults) {
 		for (Recipe recipe : results) {
 			assertTrue(expectedResults.contains(recipe));
 			expectedResults.remove(recipe);
 		}
 	}
 
 }
