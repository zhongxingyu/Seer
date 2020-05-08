 package database;
 
 import example.Point;
 import gamelogic.Item;
 import gamelogic.PlayingGround;
 import gamelogic.Spieler;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Julian
  */
 public class Datenbankzugriffe {
     MysqlConnect instance;
  
     public Datenbankzugriffe() {
         instance = new MysqlConnect();        
     }
     
     
 
     /**
      * Diese Methode mit Code füllen...
      */
     public void erstelleHighScore(List<IdNamePair> idNamePairs) {
         
         for(IdNamePair idNamePair : idNamePairs) {
             instance.connect();
             instance.issueInsertOrDeleteStatement("insert into spieler (id,name) values (?,?) " , idNamePair.getId(), idNamePair.getName());
             instance.close();
         }
         
     }
     
     /**
      * Diese Methode mit Code füllen...
      * @return 
      */
     public List<IdNamePair> holeHighscore() { 
         instance.connect();
         
         VereinfachtesResultSet result = instance.issueSelectStatement("select id, name from spieler;");
         
         List<IdNamePair> idNamePairs = new ArrayList<IdNamePair>();
 
         while (result.next()) {                        
             idNamePairs.add(new IdNamePair(result.getInt("id"), result.getString("name")));
         }
         instance.close();
         return idNamePairs;
         
         
     }    
     
     
     public int getId() {
    /// datenbankgruppe organisiert die id dies testspielers                                
        return 0;
     }
     
     public List<Point> getPosition(int id) { 
     instance.connect();
     
     
     
     VereinfachtesResultSet result = instance.issueSelectStatement("select x, y from positionschlange where id = ?;",id);
     
     
     
     
     return null;
     }
     
     public void move(Integer id, String richtung){
         
     }
     
     public String getRichtung(int id) {
         throw new UnsupportedOperationException("Methode noch nicht implemetiert."); //To change body of generated methods, choose Tools | Templates.
     }
     
     public PlayingGround getPlayingGround(){
         throw new UnsupportedOperationException("Methode noch nicht implemetiert."); 
     }
     
     public List<Item> getItems(){
         throw new UnsupportedOperationException("Methode noch nicht implemetiert."); 
     }
     
     public void spielerAnlegen(Spieler spieler) { 
         // bitte implementiere
     }
     
     public Spieler getSpieler(Integer id){
          throw new UnsupportedOperationException("Methode noch nicht implemetiert."); 
     }
 }
