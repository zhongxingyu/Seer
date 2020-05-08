 package edu.chl.dat076.foodfeed.model.dao;
 
import edu.chl.dat076.foodfeed.model.entity.Grocery;
 import edu.chl.dat076.foodfeed.model.entity.Ingredient;
 import edu.chl.dat076.foodfeed.model.entity.Recipe;
 import java.util.List;
 import javax.persistence.TypedQuery;
 import org.springframework.stereotype.Repository;
 
 @Repository("recipeDao")
 public class RecipeDao extends AbstractDao<Recipe, Long> {
 
     
     RecipeDao() {
         super(Recipe.class);
     }
     
    public List<Recipe> getByGrocery(Grocery g){
        TypedQuery<Recipe> res= entityManager.createQuery("select r from Recipe r left join r.ingredients as i where i.grocery = :grocery)", Recipe.class);
        res.setParameter("grocery", g);
         return res.getResultList();
     }
     
     public List<Recipe> getByName(String name){
         TypedQuery<Recipe> res = entityManager.createQuery("select r from Recipe r where r.name=:name", Recipe.class);
         res.setParameter("name", name);
         return res.getResultList();
     }
 }
