 package ca.ualberta.team2recipefinder.test;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ca.ualberta.team2recipefinder.*;
 
 /**
  * Tests RecipeModel class
  */
 public class RecipeModelTest
 {
 	
 	RecipeModel model;
 	
 	MyKitchen kitchen;
 
 	@Before
 	public void setUp() throws Exception
 	{
 		model = new RecipeModel();
 		
 		// we make sure we start with an empty model
 		model.getAllRecipes().removeAll(model.getAllRecipes());
 	}
 
 	@Test
 	public void testAdd()
 	{
 		Recipe recipe = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		
 		model.add(recipe);
 		
 		// make sure there is at least one recipe
 		assertEquals(model.getAllRecipes().size(), 1);
 		
 		// make sure it is the recipe we just added
 		assertEquals(model.searchRecipe(new String[] { "spaghetti" }).size(), 1);
 		
 		// make sure it is the recipe we just added
 		assertEquals(model.getRecipe(0).getName(), "spaghetti");
 		
 		// make sure it is the recipe we just added
 		assertEquals(model.getRecipeById(recipe.getRecipeID()).getRecipeID(), recipe.getRecipeID());
 	}
 	
 	@Test
 	public void testRemove()
 	{
 		Recipe recipe1 = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("rice", "test", "test", new ArrayList<Ingredient>(), false);
 		
 		model.add(recipe1);
 		model.add(recipe2);
 		
 		model.remove(recipe1);
 		
 		// make sure there is one recipe
 		assertEquals(model.getAllRecipes().size(), 1);
 		
		// make sure the recipe that is in the list is the "rice"
 		assertEquals(model.searchRecipe(new String[] { "rice" }).size(), 1);
 		
 		// make sure the recipe that is the list is the "rice"
 		assertEquals(model.getRecipe(0).getName(), "rice");
 	}
 	
 	@Test
 	public void testAddRecipeTwice()
 	{
 		Recipe recipe1 = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("spaghetti", "test", "test", new ArrayList<Ingredient>(), false);
 		
 		model.add(recipe1);
 		model.add(recipe2);
 				
 		// make sure there is one recipe
 		assertEquals(model.getAllRecipes().size(), 2);
 		
 		// make sure there are two spaghetti's in the list
 		assertEquals(model.searchRecipe(new String[] { "spaghetti" }).size(), 2);
 		
 		// make sure both sppaghetti's are in the list
 		if (model.getRecipe(0).equals(recipe1)) {
 			assertEquals(model.getRecipe(1), recipe2);
 		}
 		else {
 			assertEquals(model.getRecipe(0), recipe2);
 			assertEquals(model.getRecipe(1), recipe1);
 		}
 	}
 	
 	@Test
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
 		assertEquals(spaghettis.size(), 3);
 		
 		// and they should be recipes 1, 2 and 3
 		List<Recipe> expectedResults = new LinkedList<Recipe>();
 		expectedResults.add(recipe1);
 		expectedResults.add(recipe2);
 		expectedResults.add(recipe3);
 		checkExpectedResults(spaghettis, expectedResults);
 		
 		// if you search for "tomato", there must be two results
 		assertEquals(tomatos.size(), 2);
 		
 		// and they should be recipes 3 and 4
 		expectedResults.add(recipe3);
 		expectedResults.add(recipe4);
 		checkExpectedResults(tomatos, expectedResults);
 	}
 	
 	@Test
 	public void testSearchWithIngredients()
 	{
 		Ingredient ingredient1 = new Ingredient("tomato", 12.0, "unit");
 		Ingredient ingredient2 = new Ingredient("toma", 12.0, "unit");
 		Ingredient ingredient3 = new Ingredient("bacon", 0.0, "unit");
 		Ingredient ingredient4 = new Ingredient("rice", 0.0, "unit");
 		Ingredient ingredient5 = new Ingredient("tomato", 5.0, "unit");
 		
 		Recipe recipe1 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe2 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe3 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe4 = new Recipe("rice", "", "", new ArrayList<Ingredient>(), false);
 		Recipe recipe5 = new Recipe("spaghetti", "", "", new ArrayList<Ingredient>(), false);
 		
 		// there is no rice in the kitchen
 		ArrayList<Ingredient> kitchen = new ArrayList<Ingredient>();
 		kitchen.add(ingredient1);
 		kitchen.add(ingredient2);
 		kitchen.add(ingredient3);
 		kitchen.add(ingredient5);
 		
 		try {
 			recipe1.addIngredient(ingredient1);
 			recipe1.addIngredient(ingredient2);
 		} catch (DuplicateIngredientException e) {
 			e.printStackTrace();
 		}
 		
 		model.add(recipe1);
 		model.add(recipe2);
 		model.add(recipe3);
 		model.add(recipe4);
 		model.add(recipe5);
 						
 		List<Recipe> spaghettis = model.searchRecipe(new String[] { "spaghetti" });
 		List<Recipe> tomatos = model.searchRecipe(new String[] { "tomato" });
 		
 		// if you search for "spaghetti", there must be three results
 		assertEquals(spaghettis.size(), 3);
 		
 		// and they should be recipes 1, 2 and 3
 		List<Recipe> expectedResults = new LinkedList<Recipe>();
 		expectedResults.add(recipe1);
 		expectedResults.add(recipe2);
 		expectedResults.add(recipe3);
 		checkExpectedResults(spaghettis, expectedResults);
 		
 		// if you search for "tomato", there must be two results
 		assertEquals(tomatos.size(), 2);
 		
 		// and they should be recipes 3 and 4
 		expectedResults.add(recipe3);
 		expectedResults.add(recipe4);
 		checkExpectedResults(tomatos, expectedResults);
 	}
 	
 	private void checkExpectedResults(List<Recipe> results, List<Recipe> expectedResults) {
 		for (Recipe recipe : results) {
 			assertTrue(expectedResults.contains(recipe));
 			expectedResults.remove(recipe);
 		}
 	}
 
 }
