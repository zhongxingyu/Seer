 
 package pt.uac.cafeteria.model;
 
 import java.lang.reflect.Constructor;
 import java.sql.Connection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import pt.uac.cafeteria.model.persistence.*;
 import pt.uac.cafeteria.model.persistence.abstracts.DataMapper;
 
 /**
  * A well-known class that other objects can use to find data mappers.
  *
  * <p>The Registry holds references to previously loaded
  * <code>DataMapper</code> instances, as well as provide for
  * default constructor parameters for said mappers.
  *
  * <p>Each public class method is a factory for a different
  * <code>DataMapper</code> instance, initializing it with the
  * default parameter.
  */
 public class MapperRegistry {
 
     /** Common parent path of all files, for file based mappers. */
     private static final String DATA_PATH = "data/";
 
     /** Default database connection used in all database based mappers. */
     private static final Connection DB = Application.getDBConnection();
 
     /** Map with loaded mapper instances. */
     private static Map<String, DataMapper> instances = new HashMap<String, DataMapper>();
 
     /** Gets an AccountMapper instance. */
     public static AccountMapper account() {
         return getInstance("AccountMapper", pathTo("accounts.dat"));
     }
 
     /** Gets a MenuMapper instance. */
     public static MenuMapper menu() {
         return getInstance("MenuMapper", pathTo("meals/"));
     }
 
     /**
      * Gets a DishMapper instance, set for a particular type of dish.
      *
      * @param key unique string which describes a type of dish.
      * @return DishMapper instance, set for the given key.
      */
     private static DishMapper dish(String key) {
         return getInstance("DishMapper:" + key, pathTo("dishes/" + key + ".txt"));
     }
 
     /** Gets a DishMapper instance, set for meat dishes. */
     public static DishMapper meatDish() {
         return dish("carne");
     }
 
     /** Gets a DishMapper instance, set for fish dishes. */
     public static DishMapper fishDish() {
         return dish("peixe");
     }
 
     /** Gets a DishMapper instance, set for vegetarian dishes. */
     public static DishMapper vegetarianDish() {
         return dish("veget");
     }
 
     /** Gets a DishMapper instance, set for soup dishes. */
     public static DishMapper soupDish() {
         return dish("sopa");
     }
 
     /** Gets a DishMapper instance, set for dessert choices. */
     public static DishMapper dessertDish() {
         return dish("sobrem");
     }
 
     /** Gets an AdministratorMapper instance. */
     public static AdministratorMapper administrator() {
         return getInstance("AdministratorMapper", DB);
     }
 
     /** Gets a StudentMapper instance. */
     public static StudentMapper student() {
         return getInstance("StudentMapper", DB);
     }
 
     /** Gets an AddressMapper instance. */
     public static AddressMapper address() {
         return getInstance("AddressMapper", DB);
     }
 
     /** Gets a CourseMapper instance. */
     public static CourseMapper course() {
         return getInstance("CourseMapper", DB);
     }
 
     public static OldStudentMapper oldStudent() {
         return getInstance("OldStudentMapper", DB);
     }
 
     /**
      * Prefixes a default common path to any file path. This puts all files
      * or directories inside the same parent directory.
      *
      * @param path file path or directory.
      * @return file path or directory prefixed with a default common path.
      */
     private static String pathTo(String path) {
         return DATA_PATH + path;
     }
 
     /**
      * Lazily loads a DataMapper instance.
      *
      * <p>This is a generic solution and works for file based mappers, as well
      * as database mappers. It dynamically loads a DataMapper instance given
      * its name and constructor parameter, allowing to make the factory methods
      * clean and lean, without code duplication.
      *
      * <p>New instances of a mapper are only created when there's none already
      * loaded for the given key.
      *
      * <p>The key should be formated as such: "<code>ClassName[:suffix]</code>".
      * The suffix is optional, and allows different instances of the same class.
      * This is useful for different mappers of the same class, but with a
      * distinct sate or type.
      *
      * @param key unique key for an instance.
      * @param dep dependency for the DataMapper's constructor.
      * @return a DataMapper subclass object, as requested by the key parameter.
      */
     private static <T extends DataMapper, D> T getInstance(String key, D dep) {
         if (!instances.containsKey(key)) {
             try {
                 Constructor create = getClass(key).getConstructor(getType(dep));
                 instances.put(key, (T) create.newInstance(dep));
             }
             catch (Exception e) {
                 Logger.getLogger("Reflection").log(Level.SEVERE, null, e);
                 return null;
             }
         }
         return (T) instances.get(key);
     }
 
     /**
      * Gets the <code>Class</code> object for a given loaded mapper key.
      *
      * @param key unique key identifier for a <code>DataMapper</code> instance.
      * @return <code>Class</code> object corresponding to the mapper name
      *         extracted from key.
      */
     private static Class getClass(String key) throws ClassNotFoundException {
         return Class.forName(fullyQualifiedName(key.split(":")[0]));
     }
 
     /**
      * Class.forName() requires a FQCN (Fully-Qualified Class Name). This
      * is a nice abstraction that can get it for a given class name in the
      * current package.
      *
      * @param className name of the class.
      * @return fully-qualified class name.
      */
     private static String fullyQualifiedName(String className) {
        return MapperRegistry.class.getPackage().getName() + ".persistence." + className;
     }
 
     /**
      * Filters an object.getClass() for java.sql.Connection, because
      * Class.getConstructor(Class... types) requires an exact match.
      *
      * @param o object to query the class from.
      * @return Class type of object in parameter.
      */
     private static Class getType(Object o) {
         return o instanceof Connection ? Connection.class : o.getClass();
     }
 }
