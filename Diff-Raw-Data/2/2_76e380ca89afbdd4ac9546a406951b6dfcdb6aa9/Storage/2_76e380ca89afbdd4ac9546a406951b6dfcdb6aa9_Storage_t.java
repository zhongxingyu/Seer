 /**
  * 
  */
 package forscene.core.util;
 
 import playn.core.Json;
 import playn.core.PlayN;
 import playn.core.ResourceCallback;
 import forscene.system.entities.ForSceneConfigurator;
 
 /**
  * @author blackdevil
  * 
  */
 public class Storage {
   private boolean            initialized = false;
   private playn.core.Storage storage;
 
   private Json.Object        json        = null;
 
   /**
  * 
  */
   public Storage() {
 
     PlayN.assets().getText(ForSceneConfigurator.STORAGE_FILENAME,
         new ResourceCallback<String>() {
           public void done(String resource) {
             setJson(PlayN.json().parse(resource));
           }
 
           public void error(Throwable err) {
            PlayN.log().error("Storage - Failed to load: " + err.getMessage());
           }
 
         });
 
     storage = playn.core.PlayN.storage();
     String init = PlayN.storage().getItem("initialized");
     if ((init == null)) {
       PlayN.storage().setItem("initialized", "true");
       initialized = true;
       PlayN.log().debug("STORAGE IS NOT INIT");
     } else {
       PlayN.log().debug("STORAGE IS INIT");
     }
     if (storage.isPersisted()) {
       PlayN.log().debug("STORAGE IS PERSISTENT");
     }
 
   }
 
   public boolean save(String key, String value) {
     storage.setItem(key, value);
     return true;
   }
 
   public String load(String key) {
     return storage.getItem(key);
   }
 
   /**
    * @return the initialized
    */
   public boolean isInitialized() {
     return initialized;
   }
 
   /**
    * @param initialized
    *          the initialized to set
    */
   public void setInitialized(boolean initialized) {
     this.initialized = initialized;
   }
 
   /**
    * @return the json
    */
   public Json.Object getJson() {
     return json;
   }
 
   /**
    * @param json
    *          the json to set
    */
   public void setJson(Json.Object json) {
     this.json = json;
   }
 
 }
