 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import play.db.jpa.Model;
 @Entity
 @Table(name="food_materials")
 public class FoodMaterials extends Model{
 
 	@OneToOne
 	public Materials material;
 	
 	@ManyToOne
 	public Food food;
 	
 	public String foodMaterialsType;
 	
 	public String foodMaterialsCount;
 }
