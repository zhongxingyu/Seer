 package mykitchen.postgresql;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.Query;
 
 import mykitchen.model.Product;
 import mykitchen.model.Recipe;
 import mykitchen.repositories.RecipeRepository;
 
 @Stateless
 public class PostgreRecipeRepository extends PostgreBaseRepository<Recipe> implements RecipeRepository {
 
 	public List<Recipe> getAvailableRecipes(List<Product> products) {
 		return null;
 	}
 
 	public List<String> getAllImages() {
 		List<String> result;
 		
 		Query query = entityManager.createQuery("SELECT r.image FROM Recipe r");
 		result = query.getResultList();
 	   
 		return result;
 	}
 
 }
