 package ca.teamTen.recitopia.test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import ca.teamTen.recitopia.Recipe;
 import ca.teamTen.recitopia.RecipeBook;
 import junit.framework.TestCase;
 
 /**
  * abstract RecipeBook junit tests
  *
  */
 public abstract class RecipeBookTest extends TestCase
 {
 
 	protected abstract RecipeBook createRecipeBook();
 	
 	protected RecipeBook recipeBook;
 	
 	/**
 	 * Stores recipes that have been added with addTestData();
 	 */
 	protected ArrayList<Recipe> defaultRecipes;
 	
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		defaultRecipes = new ArrayList<Recipe>();
 		generateDefaultRecipes();
 		recipeBook = createRecipeBook();
 		addTestData();
 	}
 
 	protected void tearDown() throws Exception
 	{
 		super.tearDown();
 	}
 	
 	public void testQueryByTitle()
 	{
 		addTestData();
 		assertEquals(recipeBook.query("Spiky Melon").length, 4);
 		assertEquals(recipeBook.query("Soup").length, 1);
 	}
 	
 	public void testMixedQuery()
 	{
 		addTestData();
 		
 		for (Recipe queryBy: defaultRecipes) {
 			assertTrue(queryResultContains(recipeBook.query(queryBy.getRecipeName()), queryBy));
 			assertTrue(queryResultContains(recipeBook.query(
 					queryBy.getRecipeName() + " " + queryBy.showAuthor()
 					), queryBy));
 			assertTrue(queryResultContains(recipeBook.query(
					queryBy.showIngredients().get(0) + " " + queryBy.showAuthor()
 					), queryBy));
 			assertTrue(queryResultContains(recipeBook.query(
 					queryBy.showCookingInstructions() + " " + queryBy.showIngredients().get(0)
 					), queryBy));
 		}
 	}
 	
 	public void testUpdatesRecipes()
 	{
 		addTestData();
 		Recipe oldRecipe = defaultRecipes.get(0);
 		String newInstructions = "these are not the same instructions";
 		Recipe modifiedRecipe = new Recipe(oldRecipe.getRecipeName(),
 				oldRecipe.showIngredients(), newInstructions,
 				oldRecipe.showAuthor());
 		
 		recipeBook.addRecipe(modifiedRecipe);
		Recipe results[] = recipeBook.query(modifiedRecipe.showAuthor());
 		
		assertTrue(queryResultContains(results, modifiedRecipe));
		assertTrue(!queryResultContains(results, oldRecipe));		
 	}
 	
 	protected void generateDefaultRecipes()
 	{
 		defaultRecipes.add(new Recipe("Spiky Melon Salad",
 			new ArrayList<String>(Arrays.asList("spiky melon", "lettuce", "cucumber")),
 			"Cube the melon, chop the lettuce and cumbers. Mix in bowl and enjoy",
 			"alex@test.com"));
 		defaultRecipes.add(new Recipe("Spiky Melon Soup",
 			new ArrayList<String>(Arrays.asList("spiky melon", "cream", "spices")),
 			"mix, heat and enjoy",
 			"zhexin@test.com"));
 		defaultRecipes.add(new Recipe("Spiky Melon Shake",
 			new ArrayList<String>(Arrays.asList("spiky melon", "cream", "sugar")),
 			"mix, blend and enjoy",
 			"osipovas@test.com"));
 		defaultRecipes.add(new Recipe("Spiky Melon Fries",
 			new ArrayList<String>(Arrays.asList("spiky melon", "salt", "cooking oil")),
 			"chop, fry, eat",
 			"zou@test.com"));
 	}
 	
 	protected void addTestData()
 	{		
 		for (Recipe recipe: defaultRecipes) {
 			recipeBook.addRecipe(recipe);
 		}
 	}
 	
 	protected boolean queryResultContains(Recipe[] recipes, Recipe recipe) {
 		for (Recipe result: recipes) {
 			if (recipe.equalData(result)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
