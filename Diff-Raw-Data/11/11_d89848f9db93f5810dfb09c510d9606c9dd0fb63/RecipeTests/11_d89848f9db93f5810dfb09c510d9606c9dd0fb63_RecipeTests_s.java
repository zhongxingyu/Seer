 package ca.teamTen.recitopia.test;
 
 
 import ca.teamTen.recitopia.Recipe;
 import junit.framework.TestCase;
 
 
 public class RecipeTests extends TestCase
 {
 
 	private Recipe recipe;
 	private String name = "spiky melon salad";
 	private String ingredients[] = {"spiky melon", "spices", "salad dresing"};
 	private String instructions = "make a salad";
 	private String author = "Clark Kent";
 	
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 
		recipe = new Recipe(name, ingredients, instructions, author);
 	}
 
 	protected void tearDown() throws Exception
 	{
 		super.tearDown();
 	}
 	
 	public void testConstructor() {
		assertEquals(recipe.getName(), name);
		assertEquals(recipe.getAuthor(), author);
 	}
 }
