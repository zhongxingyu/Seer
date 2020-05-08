 package org.melati.poem;
 
 public class PoemBugPoemException extends SeriousPoemException {

  public String bug;
 
   public PoemBugPoemException(String bug) {
     this.bug = bug;
   }
 
   public PoemBugPoemException() {
     this(null);
   }
 }
