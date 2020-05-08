 package ca.cutterslade.edbafakac.db;
 
 /**
  * Thrown from {@link EntryService#getEntry(String)} when passed a key which does not identify an existing entry.
  * 
  * @author W.F. Hartford
  */
 public class EntryNotFoundException extends RuntimeException {
 
   private static final long serialVersionUID = 1L;
 
   private final String key;
 
  EntryNotFoundException(final String key) {
     super("Attempted to retrieve missing entry with key '" + key + "'");
     this.key = key;
   }
 
  EntryNotFoundException(final String key, final Throwable cause) {
     super("Attempted to retrieve missing entry with key '" + key + "'", cause);
     this.key = key;
   }
 
   /**
    * Get the key which was passed to {@link EntryService#getEntry(String)} but did not refer to any existing entry when
    * the method was invoked.
    * 
    * @return The key which does not relate to any existing entry
    */
   public String getKey() {
     return key;
   }
 
 }
