 package Test;
 
 import java.util.List;
 import java.util.Set;
 
 import UserInfo.Ingredient;
 import UserInfo.Recipe;
 
 public class MockRecipe implements Recipe {
 	
 	private String _name, _imageURL;
 	List<Ingredient> _ingredients;
 
 	public MockRecipe(String name) {
 		_name = name;
 	}
 	
 	public MockRecipe(String name, List<Ingredient> ingredients, String image) {
 		_name = name;
 		_ingredients = ingredients;
 		_imageURL = image;
 	}
 
 	@Override
 	public String getName() {
 		return _name;
 	}
 
 	@Override
 	public List<Ingredient> getIngredients() {
 		// TODO Auto-generated method stub
 		return _ingredients;
 	}
 
 	@Override
 	public String getYield() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getInstructions() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getNumberOfServings() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getTime() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getImageUrl() {
 		return _imageURL;
 	}
 
 	@Override
 	public String getID() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Set<Ingredient> getIngredientDifference(Set<Ingredient> fridge) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
	@Override
	public boolean hasImage() {
		return (_imageURL != null);
	}

 }
