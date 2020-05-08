 package miklos.martin.learningandroid.model;
 
 /**
  * Dummy value object for showing results in the sqlite example
  */
 public class AwesomePerson {
 
     private int id;
     private String name;
     private int awesomeness;
 
     public AwesomePerson( int id, String name, int awesomeness ) {
         this.id = id;
         this.name = name;
         this.awesomeness = awesomeness;
     }
 
     public int getId () {
         return id;
     }
 
     public String getName () {
         return name;
     }
 
     public int getAwesomeness () {
         return awesomeness;
     }
 
     public boolean isAwesome() {
         return awesomeness > 10;
     }
 }
