 package ua.kiev.naiv.drinkit.cocktail.mixin;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import ua.kiev.naiv.drinkit.cocktail.model.IngredientWithQuantity;
 
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Pavel Kolmykov
  * Date: 02.08.13
  * Time: 0:39
  */
 public abstract class RecipeSearchResult {
 
     @JsonIgnore
     private byte[] image;
     @JsonIgnore
    private Set<IngredientWithQuantity> ingredientWithQuantities;
     @JsonIgnore
     private String description;
 
 }
