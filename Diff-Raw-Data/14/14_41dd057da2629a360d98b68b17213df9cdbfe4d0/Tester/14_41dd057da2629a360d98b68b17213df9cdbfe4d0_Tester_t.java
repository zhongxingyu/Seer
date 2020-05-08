 
 package tests;
 
import model.conferences.Conference;
 import model.database.DatabaseException;
 
 //TODO delete class when project is finished
 
 /**
  * This is just a class to test stuff
  * 
  * 
  */
 public class Tester {
     
     public static void main(final String[] args) throws DatabaseException, InterruptedException {
        System.out.println(Conference.conferenceFromID(151));
     }
 }
