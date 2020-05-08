 package controllers;
 
 import java.util.List;
 
 import models.Food;
import models.Materials;
 
 public class Foods extends BasicCrud{
 		
 		public static void index(){
 			List<Food> foods = Food.find("order by createdAt desc").fetch(5);
 			render(foods);
 		}
 		public static void show(Long id){
 			
 			Food food = Food.findById(id);
			List<Materials> mainMaterials = Materials.find("from FoodMaterials A ,Materials B where A.food.id=? and A.material.id = B.id and A.foodMaterialsType=?",
					id,"main").fetch();
			List<Materials> auxMaterials =  Materials.find("from FoodMaterials A ,Materials B where A.food.id=? and A.material.id = B.id and A.foodMaterialsType=?",
					id,"auxiliary").fetch();
			List<Materials> seaMaterials =  Materials.find("from FoodMaterials A ,Materials B where A.food.id=? and A.material.id = B.id and A.foodMaterialsType=?",
					id,"seasoning").fetch();
			renderArgs.put("mainMaterials", mainMaterials);
			renderArgs.put("auxMaterials", auxMaterials);
			renderArgs.put("seaMaterials", seaMaterials);
			
 			render(food);
 		}
 }
