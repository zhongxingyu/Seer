 package backEnd;
 
 import functions.Constant;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * A class that adds functionality to a map. It automatically loads the functions in the
  * specified properties file.
  * It also handles user-defined things and knows what value to return given a certain key.
  * Also allows to load/save map information from/to a file using it's resource manager.
  * 
  * @author Francesco Agosti
  * 
  */
 
 public class FunctionDictionary {
     private Map<String, Executable> myFunctions;
     private Map<String, Executable> myUserExecutables;
     private Model myModel;
     private MapResourceManager myResourceManager;
 
     /**
      * Makes a map of functions from the resource bundle
      * 
      * @param model
      */
 
     public FunctionDictionary(Model model) {
         myModel = model;
         myUserExecutables = new HashMap<String, Executable>();
         Factory factory = new Factory();
         myResourceManager = new MapResourceManager(model);
         try {
             myFunctions = factory.make(model);
         }
         catch (ClassNotFoundException | NoSuchMethodException | SecurityException
                 | InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException e) {
             myModel.showErrorMsg("LoadError");
         }
 }
 
     /**
      * Returns the value corresponding to a key from our map. If our map does not contain
      * the key, it assumes the value is a number and returns a constant.
      * 
      * @param key
      * @return
      */
     public Executable get (String key) {
         int workSpaceIndex = myModel.getController().getWorkspaceIndex();
         if (myFunctions.containsKey(key)) {
             return myFunctions.get(key);
         }
         else if (myUserExecutables.containsKey(workSpaceIndex + key)) {
             return myUserExecutables.get(workSpaceIndex + key);
         }
         else {
             try {
                 int value = Integer.parseInt(key);
                 return new Constant(value);
             }
             catch (NumberFormatException e) {
                 myModel.showErrorMsg("MapGetError");
             }
         }
         return new Constant(0);
     }
 
     /**
      * Returns true if the smartMap contains the given key.
      * 
      * @param key
      * @return boolean
      */
     public boolean contains (String key) {
         return myFunctions.containsKey(key) || myUserExecutables.containsKey(key);
     }
 
    /**
     * Adds a function to the map
     * 
     * @param name of function
     * @param function to be added
     */
    public void add (String name, Executable function) {
        int workSpaceIndex = myModel.getController().getWorkspaceIndex();
        myUserExecutables.put(workSpaceIndex + name, function);
    }
 
     /**
      * Associates the given executable (a function or a constant) to the given key.
      * 
      * @param name
      * @param function
      */
     public void put (String name, Executable function) {
         int workSpaceIndex = myModel.getController().getWorkspaceIndex();
         myUserExecutables.put(workSpaceIndex + name, function);
     }
 
     /**
      * Saves the myUserExecutables map to a properties file that is specified in the view.
      * 
      * @param fileName
      */
     public void save (String fileName) {
         myResourceManager.saveToFile(myUserExecutables, fileName);
     }
 
     /**
      * Loads a properties file chosen from the view into the myUserExecutables map.
      * 
      * @param root
      */
     public void load (String root) {
         myResourceManager.loadFromFile(root);
     }
 }
