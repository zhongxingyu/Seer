 package arcade.database;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Main {
     
     public static void main(String[] args) throws SQLException {
         
         Database myDatabase = new Database();
 
         //myDatabase.printUserTable();
         //myDatabase.printGameTable();
         
         myDatabase.createUser("kayzooo", "mypassword", "Kevin", "Zhu", "11/12/1990");
         myDatabase.createUser("natx13", "nataliapassword", "Natalia", "Carvalho", "04/26/1991", "blahblahblh");
        myDatabase.createUser("bob", "mypassword", "Joe", "Smith", "11/11/1911");   
        myDatabase.createUser("test", "test", "test", "test", "01/01/1901");
         
         myDatabase.deleteGame("example");
         
 //       myDatabase.addAvatarToUser("natx13", "C:/blahbalhblah");
         
        //myDatabase.createGame("example", "examplegenre");
        
         myDatabase.authenticateUsernameAndPassword("kayzooo", "mypassword");
         
         //myDatabase.printUserGameDataTable();
         
   //      myDatabase.userPlaysGameFirst("kayzooo", "Pacman", "10");
         //myDatabase.updateHighScore("kayzooo", "Pacman", "1000");
         //System.out.println(myDatabase.retrieveDOB("natx13"));
         //System.out.println(myDatabase.retrieveAvatar("natx13"));
 
         //myDatabase.deleteUser("kayzooo");
         //myDatabase.deleteUser("natx13");
         
         //myGameDatabase.registerGame("Pacman");
         
         List<String> myGameNames = myDatabase.retrieveListOfGames();
         
         for (String game : myGameNames) {
             System.out.println(game);
         }
         
         myDatabase.printUserTable();
         myDatabase.printGameTable();
         myDatabase.printUserGameDataTable();
         
         myDatabase.closeDatabaseConnection();
         
 
     }
 
 }
