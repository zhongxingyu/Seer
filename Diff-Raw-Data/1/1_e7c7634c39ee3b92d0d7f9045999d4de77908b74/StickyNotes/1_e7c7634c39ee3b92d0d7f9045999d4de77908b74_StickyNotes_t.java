 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package stickynotes;
 
 import java.util.LinkedList;
 
 /**
  *
  * @author mhdsyrwan
  */
 public class StickyNotes {
 
     // a list to store the notes
     // Linked list is a Generic/Template Class so we should provide the Template Child class we want which is "Note"
     private LinkedList<Note> notes = new LinkedList<Note>();    
     
     /// Important Note:
     /// we chose the LinkedList because we dont care about getting elements at someindex quickly 
     /// if we care about that we should choose Vector or Array.
     
     /// LinkedList is Fast for adding elements ... and fast for getting all the elements
     /// But it's slow for getting a certain element (ex. list.get(3) // getting the third element).
     
     
     /**
      * Deletes a note from the notes list
      * @param note the note object to delete
      */
     public void delete(Note note) {
         notes.remove(note); // this method will delete by value (by comparing the given object to the lists' objects)
        note.dispose();
     }
     
     /**
      * Creates a Note by giving its body text
      * @param body the body text
      */
     public void create(String body) {
         // here we should create a new Note giving it our instance (this) as a parameter so it knows its parent :D
         notes.add(new Note(this, body));
     }
     
     private void loadNotes() {
         // Just put any content to try the app
         create("aksdaasdasdasdasdasd");
         create("alksdasdasdaaaaaaaaa");
         
         //TODO replace this code with files code
         //TODO we should load the notes from files
     }
     
     /**
      * this method will run the whole program 
      * will load the notes then it will show them
      */
     public void run() {
         // loading notes
         loadNotes();
         
         // running/showing notes
         for(Note note : notes) { // this is foreach loop, it means : for each "note" as instance of "Note" in the collection "notes"
             // showing the note
             // the Note class is derived from JFrame Class so it can be shown by "setVisible(true)"
             note.setVisible(true);
         }
     }
     
     /**
      * this is the main method that JAVA will call
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // creating a new StickyNotes Object (Our Manager Object) then asking it to run :)
         new StickyNotes().run();
     }
 }
