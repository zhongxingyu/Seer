 package hygeia;
 
 import java.sql.*;
 import java.security.*;
 import java.util.StringTokenizer;
 import java.util.ArrayList;
 import java.util.Random;
 
 public class Algorithm {
 
     private Database db;
     private int uid;
     private static final int BREAKFAST = 0x08;
     private static final int LUNCH = 0x04;
     private static final int DINNER = 0x02;
     private static final int SNACK = 0x01;
     private static final double SAME = 1.1; //used as a margin of error for relatively balanced meals
 
     public Algorithm(Database db, User u) {
 		this.uid = u.getUid();
 		this.db = u.getDb();
     }
 
     /* Main algorithm. */
     /* Based on what kind of meal requested an int matching the legend is passed. */
     
     public static Meal suggestMeal(User u, int type) {
         try {
             Meal m = suggestMeal0(u, type);
             u.getDb().free();
             return m;
         } catch (SQLException e) {
             u.getDb().free();
             return null;
         }
     }
     
     public static Meal suggestMeal0(User u, int type) throws SQLException {
         if (u == null) {
             return null;
         }
         
         Database db = u.getDb();
     
 		//pulls all meals from the universal meal list and the user's personal meals
         ResultSet rs = db.execute("select mid from meals where (uid = " + 
             u.getUid() + " or uid = 0) and type & " + type + " = " + type + ";");
         //arraylist of meal IDs that come from the database
         ArrayList<Integer> results = new ArrayList<Integer>();
 		while(rs.next())
 		{
 			results.add(rs.getInt("mid"));
 		}
 		//retrieves a list of food in the inventory
 		Inventory inven = new Inventory(u);
 		
 		Food.Update[] fu = inven.getInventory();
 		if (fu == null)
 		{
 			return null;
 		}
 		//random generator to select a meal at random from available MIDs
 		Random r = new Random();
 		//if the inventorymatchcount variable equals the number of ingredients in a recipe, all necessary ingredients are available
 		int inventorymatchcount = 0;
 		//Meal m is the variable used to store meals as they are accessed for comparison to ingredients
 		Meal m = null;
 		//while loop runs while a suitable meal isn't found yet
 		while (results.size() > 0)
 		{
 			inventorymatchcount = 0;
 			int nextInt = r.nextInt(results.size());
 			m = new Meal(db, results.get(nextInt));
 			Food.Update mu[] = m.getMeal();
                        // System.out.println("mu length " + mu.length + " fu length " + fu.length);
 			for (int i = 0; i < mu.length; i++)
 			{
 				for (int j = 0; j < fu.length; j++)
 				{
 					if (mu[i].equals(fu[j]))
 					{
 						inventorymatchcount += 1;
 					}
 				}
 			}
 			if (inventorymatchcount == mu.length)
 			{
 				//begins balanced suggestion based on the 40:30:30 ideal,
 				//+ and - 10% (defined as constant SAME, Suggest A Meal Error) to find relatively balanced meals
 				Nutrition n = m.getNutrition();
 				double totalGrams = 0;
 				totalGrams = (n.getCarbohydrates() + n.getProtein() + n.getFat());
 				if (n.getCarbohydrates() / totalGrams > 0.4 - SAME 
 						&& n.getCarbohydrates() / totalGrams < 0.4 + SAME)
 				{
 					if (n.getProtein() / totalGrams > 0.3 - SAME 
 							&& n.getProtein() / totalGrams < 0.3 + SAME)
 					{
 						if (n.getFat() / totalGrams > 0.3 - SAME 
 								&& n.getFat() / totalGrams < 0.3 + SAME)
 						{
 							return m;
 						}
 					}
 				}
 			}
 			/*else
 			{ */
 				//if the contents of the inventory don't satisfy the recipe, remove that recipe
 				//from the ArrayList of meals so it won't accidentally be compared again
 				results.remove(nextInt);
 		/*	}*/
 
 		}
 		//if no meal matches the SAME margin of error for balancedness, return null
        return new Meal(db, 0);
     }
     
     /* Sanitizes a String for use. */
     public static String Clean(String s) {
     	StringTokenizer toke = new StringTokenizer(s, "*/\\\"\':;-()=+[]");
     	String r = new String("");
     	while(toke.hasMoreTokens())
     	{
     		r = new String(r + toke.nextToken());
     	}
         return r;
     }
     
     /* Should return MD5 hashes.. but may be platform dependent. And this was 
        written by some anonymous author. FYI.  */
     public static String MD5(String md5) {
         try {
             java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
             byte[] array = md.digest(md5.getBytes());
             StringBuffer sb = new StringBuffer();
             for (int i = 0; i < array.length; ++i) {
                 sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
             }
             return sb.toString();
         } catch (java.security.NoSuchAlgorithmException e) {
         }
         return null;
     }
 }
