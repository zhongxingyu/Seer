 package hygeia;
 
 import java.sql.*;
 
 /* Classes for handling food items */
 public class Food {
 
     /* Food.Update is used for adding to inventory */
     public static class Update {
         
         private int fid;
         private double count;
         
         public Update(int fid, int count) {
         
         }
         
         public int getFid() {
         
         }
         
         public double getCount() {
         
         }
         
         /* Create a Nutrition object with the values filled in from the db */
         public Nutrition getNutrition(Database db) {
         
         }
     }
     
     /* Food.Create is used for creating new foods in the database. */
     public static class Create {
         
         private String name;
        private int weight;
         private double count;
         private double calories, carbohydrates, protein, fat, factor;
         
         public Create(String name, int count, double factor, int wt, double cal, 
             double carb, double pro, double fat) {
         
         }
         
         /* getters for all fields */
         public String getName() {
         
         }
         
         public int getWeight() {
         
         }
         
         public double getWeight() {
         
         }
         
         public double getCount() {
         
         }
         
         public double getCalories() {
         
         }
         
         public double getCarbohydrates() {
         
         }
         
         public double getProtein() {
         
         }
         
         public double getFat() {
         
         }
     }
     
     /* Food.List is used for producing a list of foods visible to the user. */
     public static class List {
         
         private String name;
         private int fid;
         private double count;
         
         public List(String name, int fid, double count) {
         
         }
         
         public String getName() {
         
         }
         
         public int getFid() {
         
         }
         
         public double getCount() {
         
         }
     }
 
     /* Create a new food in the database. Returns fid if successful. */
     public static int createFood(Database db, Food.Create f) {
     
     } 
 
 }
