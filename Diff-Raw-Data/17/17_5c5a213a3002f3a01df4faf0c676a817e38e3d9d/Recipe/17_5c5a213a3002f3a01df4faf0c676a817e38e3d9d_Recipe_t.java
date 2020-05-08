 package hygeia;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 public class Recipe {
 
     private Database db;
     private int rid;
     
     /* Create object from recipe id */
     public Recipe(Database db, int rid) {
         this.db = db;
         this.rid = rid;
     }
     
     /* Remove the recipe from the database. Positive if successful. */
     public int removeRecipe() {
         int result = this.db.update("delete from recipes where rid=" + this.rid + ";");
         return result;
     }
     
     /* Create a new recipe in the d:atabase. Returns recipe id. */
     public static int createRecipe(Database db, User user,  String name, 
                 String instructions, Food.Update ingred[] ) {
         if ((db == null) || (user == null) || (ingred == null) ||
             (name == null) || (instructions == null) ) {
             return -1;
         }
         
         name = Algorithm.Clean(name);
         
        int uid = user.getUid(); 
         
         /* Ensure each item in recipe is not null. */
         /* Create Recipe.Update objects to get Nutrition. */
         Nutrition sum = new Nutrition(0, 0, 0, 0);
         for (int i = 0; i < ingred.length; i++) {
             if (ingred[i] == null) {
                 return -2;
             }
             sum.addNutrition(ingred[i].getNutrition(db));
         }
         
         /* Create recipe record in database */
         int result = db.update("insert into recipes (uid, name, instructions, ingrediants, calories, " +
             "carbohydrates, protein, fat) values (" + uid + ", '" + name + "', " + instructions 
             + "', " + ingred + "', " + sum.getCalories() + ", " + sum.getCarbohydrates() +
             ", " + sum.getProtein() + ", " + sum.getFat() + ");");
         
         /* Check to see if it went correctly. */
         if (result != 1) {
             return -3;
         }
         
         /* Now get the rid.. probably not the best way. */
         int rid = 0;
         ResultSet rs = db.execute("select rid from recipes where uid = " + uid +
             " and name = '" + name + "';");
         try {
             if (rs == null) {
                 return -4;
             } else {
                 rs.next();
                 rid = rs.getInt("rid");
             }
         } catch (SQLException e) {
             return -5;
         }
         
         /* Not optimal but works to add components to database. */
        for (Food.Update i : ingred) { 
             result = db.update("insert into components(rid, fid, count) values (" +
                 rid + ", " + i.getFid() + ", " + i.getCount() + ");");
             if (result != 1) {
                 /* This is an unclean exit but this shouldn't happen. */
                 return -6;
             }
         }
         return rid;
     }
     
     /* Return recipe id. */
     public int getRid() {
         return this.rid;
     }
     
     /* Return timestamp. */
     public Timestamp getOccurrence() {
         ResultSet rs = this.db.execute("select occurrence from history where " +
             "rid = " + this.rid + ";");
         
         Timestamp t;
             
         try {
             if (rs == null) {
                 return null;
             }
             rs.next();
             t = rs.getTimestamp("occurrence");
             db.free();
         } catch (SQLException e) {
             return null;
         }
         
         return t;
     }
     
     /* Fetches and then returns name from database. */
     public String getName() {
         if (this.db == null) {
             return null;
         }
         
         ResultSet rs = this.db.execute("select name from recipes where rid = " +
             this.rid + ";");
             
         try {
             if (rs == null) {
                 return null;
             }
             if (rs.next()) {
                 return rs.getString("name");
             }
             return null;
         } catch (SQLException e) {
             return null;
         }
     }
     
     /* Return the type of recipes 
     public int getType() {
         ResultSet rs = this.db.execute("select type from recipes where rid=" +
             this.rid + ";");
         
         try {
             if (rs == null) {
                 return 0x10000000;
             }
             rs.next();
             int type = rs.getInt("type");
             db.free();
             return type;
         } catch (SQLException e) {
             db.free();
             return 0x10000001;
         }
     } */
     
     /* Return array of Food items that make up recipe */
     public Food.Update[] getIngrediants() {
         ResultSet rs = this.db.execute("select fid, count from components where"
             + " rid = " + this.rid + ";");
         
         ArrayList<Food.Update> list = new ArrayList<Food.Update>();
         
         try {
             if (rs == null) {
                 return null;
             } else {
                 while(rs.next()) {
                     int fid = rs.getInt("fid");
                     double count = rs.getDouble("count");
                     list.add(new Food.Update(fid, count));
                 }
             }
         } catch (SQLException e) {
             if (list.isEmpty()) {
                return null;w
             }
         }
         
         return (Food.Update[])list.toArray(new Food.Update[1]);
     }
     
     /* Return array of Food.List items that make up meal to display to user. */
     public Food.List[] getFoodList() {
         ResultSet rs = this.db.execute("select foods.fid, foods.name, " +
              "components.count from components inner join foods on " +
              "components.fid=foods.fid where components.rid = " + this.rid +
              " order by foods.name;");
         
         ArrayList<Food.List> list = new ArrayList<Food.List>();
         
         try {
             if (rs == null) {
                 return null;
             } else {
                 while(rs.next()) {
                     int fid = rs.getInt("fid");
                     String name = rs.getString("name");
                     double count = rs.getDouble("count");
                     list.add(new Food.List(name, fid, count));
                 }
             }
         } catch (SQLException e) {
             if (list.isEmpty()) {
                 return null;
             }
         }
         
         return (Food.List[])list.toArray(new Food.List[1]);
     }
     
     /* Returns a nutrition object with info for the whole recipe */
     public String getNutrition() {
         
         if (db == null) {
             return null;
         }
         
         ResultSet rs = this.db.execute("select instructions from recipes "
                                         + "where rid = " + this.rid + ";");
             
         try {
             if (rs == null) {
                 return null;
             }
             if (rs.next()) {
                 String instructions  = rs.getString("instructions");
 
                 return instructions;
             }
             return null;
         } catch (SQLException e) {
             return null;
         }
     
     }
 
     /* Returns an array of String representing the instructions for the recipe */
     public String getInstructions() {
         
         if (db == null) {
             return null;
         }
         
         ResultSet rs = this.db.execute("select instruction " +
             "from recipes where rid = " + this.rid + ";");
             
         try {
             if (rs == null) {
                 return null;
             }
             if (rs.next()) {
                 String insturctions = rs.getString("instructions");
             }
             return null;
         } catch (SQLException e) {
             return null;
         }
     
     }
 
     /* creates a meal based on the recipe for the user */
     public boolean addToMeals(User user, int type) {
         if ( type < 0)
         {
             return false;
         }        
 
        if ( createMeal( this.db, user, getIngrediants(), getName(), type) < 1 )
        {
            return false;
        }

        return true;
 
     }
     
     /* Recipe.List is used for listing meals to the user. */
     public static class List {
     
         private String name;
         private int rid;
         private Timestamp occurrence;
         
         public List(String name, int rid, Timestamp occurrence) {
             this.name = name;
             this.rid = rid;
             this.occurrence = occurrence;
         }
         
         public String getName() {
             return this.name;
         }
         
         public int getRid() {
             return this.rid;
         }
         
         public Timestamp getOccurrence() {
             return this.occurrence;
         }
         
         public Recipe toRecipeAdapter(Database db) {
             return new Recipe(db, this.rid);
         }
     
     }
     
 
 }
