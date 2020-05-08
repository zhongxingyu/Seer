 package controllers.user;
 
 /**
  * Enumerates the different types of user.
  * @author Sander Demeester
  */
 public enum Type {
 
     ADMINISTRATOR,
     ORGANIZER,
     INDEPENDENT,
    PUPIL, //TODO: for what is this role? The DB makes no difference between Ind & pupil
     TEACHER,
    AUTHOR, //TODO: same question
    ANON; //TODO: for what is this one needed?
 
 }
