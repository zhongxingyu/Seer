 package backend;
 
 /**
  * An Edit is a datatype that makes it easier to make sure
  * that everything is threadsafe. It has a value which is 
  * the string that it represents. It also has an owner which
  * is either the client that originally created it or is the
  * document itself. When any document, regardless of the
  * document's name, owns an edit, that edit is owned by 
  * "document", not by the document's name. This is secure 
  * because no client can have a name longer than six letters
  * as enforced in the GUI, and "document" is more than six
  * letters long.
  *
  * Testing strategy -- This will be tested like most simple 
  * objects. There will be an EditTest class which will create
  * edits, use the provided methods on those edits, and assert
  * that those methods worked correctly.
  *
  */
 public class Edit {
     // The value never changes but the owner will change once.
     final private String value;
     private String owner;
     /**
      * Edit class: includes buffer
      * @param input The value of the edit
      * @param clientName The client who made the edit
      */
     public Edit(String input, String clientName) {
         this.value = input;
         this.owner = clientName;
     }
 
     /**
      * Returns the owner
      * @return The owner of the edit
      */
     public String getOwner() {
         return this.owner;
     }
     
     /**
      * Returns the value
      * @return The value of this edit
      */
     public String getValue() {
         return this.value;
     }
     
     /**
      * Changes the owner to the doc
      */
     public void changeOwner() {
        this.owner = "document";
     }
     
     /**
      * To String of values
      */
     public String toString() {
         return value;
     }
 }
