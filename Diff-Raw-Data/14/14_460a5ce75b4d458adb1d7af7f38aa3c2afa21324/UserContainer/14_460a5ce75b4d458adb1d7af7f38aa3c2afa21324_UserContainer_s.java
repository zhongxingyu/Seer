 package wielwijk;
 
 import java.util.*;
 
 /**
  *
  * @author jeroen
  */
 public class UserContainer {
 
     private Database db;
     
     UserContainer() {
         db = new Database();
         db.connect();
     }
 
     /**
      *
      * @param name
      * @param password
      * @param birthdate
      * @param active
      * @param picture
      * @param board
      */
     public void addBoard(String name, String password, int picture, String address, String birthdate) {
         Board newBoard = new Board(name, password, picture, address, birthdate);
 
        db.exec("INSERT INTO users (name, password, active, board, picture, address, birthdate)" +
        " VALUES (" + name + ", " + password + ", 1, 1, NULL, " + address + ", " + birthdate);
         //picture nog niet toegevoegd
     }
 
     /**
      *
      * @param name
      * @param password
      * @param birthdate
      * @param active
      * @param picture
      * @param board
      */
     public void addMember(String name, String password, int picture, String address, String birthdate) {
         Member newMember = new Member(name, password, picture, address, birthdate);
         
        db.exec("INSERT INTO users (name, password, active, board, picture, address, birthdate)" +
        " VALUES (" + name + ", " + password + ", 1, 0, NULL, " + address + ", " + birthdate);
         //database create verhaaltje komt hier
     }
     
     /**
      *
      * @param user
      */
     public void removeUser(User user) {
         //datebase remove user verhaalte
     }
 
     /**
      *
      * @param user
      */
     public List findUser(String name) {
       List results = db.query("SELECT * FROM users WHERE name LIKE '%" + name + "%'");
       return results;
         //database zoek user verhaaltje
     }
     
 }
