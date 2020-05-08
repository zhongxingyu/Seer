 package ca.cutterslade.edbafakac.db;
 
 /**
  * Thrown from {@link EntryService#getNewEntry(String)} to indicate that the provided key points to an existing entry.
  * 
  * @author W.F. Hartford
  */
 public class EntryAlreadyExistsException extends RuntimeException {
 
   private static final long serialVersionUID = 1L;
 
   private final String key;
 
  EntryAlreadyExistsException(final String key) {
     super("Attempted to create a new entry specifying already used key '" + key + "'");
     this.key = key;
   }
 
   /**
    * Get the key which was passed to {@link EntryService#getNewEntry(String)}, but existed prior to the invocation which
    * threw this exception.
    * 
    * @return The key of an existing entry
    */
   public String getKey() {
     return key;
   }
 }
