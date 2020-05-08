 
 package model.database;
 
 /**
  * Enum of Database errors.
  * 
  * @author Tim Mikeladze
  * @version 11-16-2013
  */
 public enum Errors {
     
     /**
      * Error for emails that are already registered.
      */
     EMAIL_EXISTS("This email already exists in the database"),
     
     /**
      * Error for emails that are not already registered.
      */
     EMAIL_DOES_NOT_EXIST("This email does not exist"),
     
     /**
      * Error for conferences that dont exist.
      */
     CONFERENCE_DOES_NOT_EXIST("This conference does not exist"),
     
     /**
      * Error for submitting more than the allowed number of papers per conference.
      */
     MAX_PAPER_SUBMISSIONS_EXCEEDED("You've submitted the maximum amount of papers allowed into this conference"),
     
     RECOMMENDATION_DOES_NOT_EXIST("A recommendation hasn't been submitted for this paper"),
     
     USER_ALREADY_IN_CONFERENCE("A user with this permission level already exists in this conference"),
     
     USER_NOT_REVIEWER("A user must be a reviewer to be designated a subprogram chair"),
     
     PAPER_DOES_NOT_EXIST("This paper does not exist"),
     
     CANT_ASSIGN_PAPER("A user can't be assigned to a paper that they authored"),
     
    CANT_REVIEW_PAPER("A user can't review a paper that they authored"),
    
    PAST_CONFERENCE_DATE("You cannot submit a paper to a conference after conference date.");
     
     /**
      * The error.
      */
     private String error;
     
     /**
      * Returns an error.
      * 
      * @param the error
      */
     private Errors(final String error) {
         this.error = error;
     }
     
     /**
      * Returns the error.
      * 
      * @return the error
      */
     public String getError() {
         return error;
     }
     
     /**
      * Returns the error.
      * 
      * @return the error
      */
     @Override
     public String toString() {
         return error;
     }
 }
