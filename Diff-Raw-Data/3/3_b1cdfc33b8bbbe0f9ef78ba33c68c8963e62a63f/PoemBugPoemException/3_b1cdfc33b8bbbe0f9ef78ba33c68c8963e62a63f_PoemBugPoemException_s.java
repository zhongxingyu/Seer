 package org.melati.poem;
 
 public class PoemBugPoemException extends SeriousPoemException {
  String bug;
 
   public PoemBugPoemException(String bug) {
     this.bug = bug;
   }
 
   public PoemBugPoemException() {
     this(null);
   }
 }
