 package ca.thewhitakers.recipebook.domain.model;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "recipe_info")
 public class RecipeInfo extends DomainObject {
 	private RecipeInfoType recipeInfoType;
 	private String value;
 	private Recipe recipe;
 
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
 	@JoinColumn(name = "recipeInfoType_id")
 	public RecipeInfoType getRecipeInfoType() {
 		return recipeInfoType;
 	}
 	
 	public void setRecipeInfoType(RecipeInfoType recipeInfoType) {
 		this.recipeInfoType = recipeInfoType;
 	}
 	
 	public String getValue() {
 		return value;
 	}
 	
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	@ManyToOne
 	@JoinColumn(name = "recipe_id")
 	public Recipe getRecipe() {
 		return recipe;
 	}
 
 	public void setRecipe(Recipe recipe) {
 		this.recipe = recipe;
 	}
 }
