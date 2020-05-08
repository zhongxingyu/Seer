 package database;
 
 /**
  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  *
  */
 public class DatabaseEntryNotFound extends Exception {
 
    private static final long serialVersionUID = 1L;

     public DatabaseEntryNotFound(String message) {
         super(message);
     }
 
 
 }
