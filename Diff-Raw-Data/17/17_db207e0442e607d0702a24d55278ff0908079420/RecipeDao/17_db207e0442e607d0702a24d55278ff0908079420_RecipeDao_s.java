 package edu.chl.dat076.foodfeed.model.dao;
 
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
     
    public List<Recipe> getByIngredient(Ingredient i){
        TypedQuery<Recipe> res= entityManager.createQuery("select r from Recipe r where r.id=(select ri.recipeid from Recipe_Ingredient where ri.ingredientid =:i_id)", Recipe.class);
        res.setParameter("i_id", i.getId());
         return res.getResultList();
     }
     
     public List<Recipe> getByName(String name){
         TypedQuery<Recipe> res = entityManager.createQuery("select r from Recipe r where r.name=:name", Recipe.class);
         res.setParameter("name", name);
         return res.getResultList();
     }
 }
