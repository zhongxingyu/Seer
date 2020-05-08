 package neuralNets.matlab;
 
 import java.util.HashMap;
 
 /**
  * Represents settings for the program.
  * Used to modify how the program run from MATLAB.
  * @author Maarten Slenter
  */
 public class Settings 
 {
     /**
      * The settings
      */
     private final HashMap<String, Object> settings = new HashMap<String, Object>();
     
     /**
      * The default settings
      */
     private final HashMap<String, Object> defaults = new HashMap<String, Object>();
     
     /**
      * The category for upcoming puts
      */
     private String category = "";
     
     {
         //General
         defaults.put("nonConvergingGenCount", 50);
         defaults.put("runBatch", true);
         
         //Single runs
         defaults.put("renderUpdateTimeBase", 50);
         defaults.put("slowdownFactor", 10);
         defaults.put("testCaseUpdateTimeBase", 1);
         defaults.put("updateCountMax", 50);
         defaults.put("updateCountMin", 1);
         defaults.put("windowHeight", 800);
         defaults.put("windowWidth", 1500);
         
         //Batch runs
         defaults.put("runToTarget", false);
         defaults.put("solutionTarget", 25);
         defaults.put("threadCount", 4);
         
         //Test cases
             //CircleMoveToXYDirection
             defaults.put("circleMoveToXYDirection.crossOverRate", 0.7);
             defaults.put("circleMoveToXYDirection.goalX", 200);
             defaults.put("circleMoveToXYDirection.goalY", 200);
             defaults.put("circleMoveToXYDirection.mutationRate", 0.01);
             defaults.put("circleMoveToXYDirection.popSize", 100);
             defaults.put("circleMoveToXYDirection.visualizeAmount", 0);
     }
     
     /**
      * Sets a property
      * @param key The property to set
      * @param value The new value of the property
      */
     public void set(String key, Object value)
     {
         if(!category.isEmpty())
         {
             key = category + "." + key;
         }
         
         settings.put(key, value);
     }
     
     /**
      * Sets the category for all following property changes
      * @param category The new category
      */
     public void setCategory(String category)
     {
         this.category = category;
     }
     
     /**
      * Clear the category
      */
     public void clearCategory()
     {
         category = "";
     }
     
     /**
      * Returns the value under key as an integer, or the default if no value is set.
      * @param key The key to fetch the value for
      * @return The value under the specified key, as an integer.
      */
     public int getInt(String key)
     {
         Object value = getValue(key);
         if(value instanceof Number)
         {
             Number number = (Number) value;
             return number.intValue();
         }
         
         throw new RuntimeException("The value under key '" + key + "' is not a number");
     }
     
     /**
      * Returns the value under key as a double, or the default if no value is set.
      * @param key The key to fetch the value for
      * @return The value under the specified key, as a double.
      */
     public double getDouble(String key)
     {
         Object value = getValue(key);
         if(value instanceof Number)
         {
             Number number = (Number) value;
             return number.doubleValue();
         }
         
         throw new RuntimeException("The value under key '" + key + "' is not a number");
     }
     
     /**
      * Returns the value under key as a boolean, or the default if no value is set.
      * @param key The key to fetch the value for
      * @return The value under the specified key, as a boolean.
      */
     public boolean getBool(String key)
     {
         Object value = getValue(key);
         if(value instanceof Boolean)
         {
             return (Boolean) value;
         }
         
         throw new RuntimeException("The value under key '" + key + "' is not a boolean");
     }
     
     /**
      * Returns the value under the key or the default is no value is set.
      * Throws an exception when the key doesn't exist.
      * @param key The key to fetch the value for
      * @return The value under the specified key
      */
     private Object getValue(String key)
     {
         Object value;
         if(settings.containsKey(key))
         {
             value = settings.get(key);
         }
        else if(settings.containsKey(key))
         {
             value = defaults.get(key);
         }
         else
         {
             throw new IllegalArgumentException("Key '" + key + "' not found in settings");
         }
         
         return value;
     }
 }
