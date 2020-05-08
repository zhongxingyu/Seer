 package controllers;
 
 import java.util.List;
 
 import models.Food;
 
 public class Foods extends BasicCrud{
 		
 		public static void index(){
 			List<Food> foods = Food.find("order by createdAt desc").fetch(5);
 			render(foods);
 		}
 		public static void show(Long id){
 			
 			Food food = Food.findById(id);
 			render(food);
 		}
 }
