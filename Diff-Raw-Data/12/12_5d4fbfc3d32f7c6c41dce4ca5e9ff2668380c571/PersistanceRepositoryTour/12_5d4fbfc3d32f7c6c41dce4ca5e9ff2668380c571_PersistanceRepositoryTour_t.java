 package persistance;
 
 import businessDomainObjects.Tour;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import utility.InputValidator;
 
 /**
  *
  * @author Oliver Brooks <oliver2.brooks@live.uwe.ac.uk>
  */
 public class PersistanceRepositoryTour {
 
     private DatabaseQueryExecutor db;
 
     public PersistanceRepositoryTour(DatabaseQueryExecutor db) {
         this.db = db;
     }
 
     public synchronized boolean addTour(String name, String description, ArrayList<String> exhibitIDs, String questionSetID) throws SQLException {
         name = InputValidator.clean(name);
         description = InputValidator.clean(description);
         String insertSQL = "INSERT INTO `tours` (TourName, "
                 + "TourDescription";
         
         if(questionSetID.equals("")){ // if not null, add in the questionSet too
             insertSQL += ", QuestionSetID) VALUES ('" + name + "','" 
                 + description + "', '-1'";
         }
         else{
            insertSQL += ", QuestionSetID) VALUES ('" + name + "','" 
                + description + "', '" + questionSetID + "'";
         }
 
             insertSQL += ");";
  
 
         boolean ret = db.executeUpdate(insertSQL);
         if (ret == false) {
             return false;
         }
 
         String tourIDSQL = "SELECT MAX(TourID) as TourID FROM `tours`;";
         ResultSet rs = db.executeStatement(tourIDSQL);
         rs.next();
         String latestTourID = rs.getString("TourID");
         for (String exhibitID : exhibitIDs) {
             String exhibitInsertSQL = "INSERT INTO `toursexhibitslink` (TourID, ExhibitID) VALUES ('" + latestTourID + "','" + exhibitID + "');";
             ret = db.executeUpdate(exhibitInsertSQL);
             if (ret == false) {
                 return false;
             }
         }
 
         return true;
     }
 
     public synchronized boolean removeTour(int tourID) throws SQLException {
         String tourIDString = InputValidator.clean(String.valueOf(tourID));
         String SQL = "DELETE FROM tours WHERE TourID = '" + tourIDString + "';";
         boolean ret = db.executeUpdate(SQL);
         if (!ret) {
             return false;
         }
         SQL = "DELETE FROM toursexhibitslink WHERE TourID = '" + tourIDString + "';";
         ret = db.executeUpdate(SQL);
         if (!ret) {
             return false;
         }
 
         return true;
     }
 
     public synchronized boolean modifyTour(int ID, String name, String description, ArrayList<String> exhibitIDs) throws SQLException {
         String modificationSQL = "UPDATE tours SET TourName='" + name + "', TourDescription='" + description + "' WHERE TourID=" + ID + ";";
         boolean ret = db.executeUpdate(modificationSQL);
         if (!ret) {
             return false;
         }
 
         String linkRemovalSQL = "DELETE FROM toursexhibitslink WHERE TourID = " + ID + ";";
         ret = db.executeUpdate(linkRemovalSQL);
         if (!ret) {
             return false;
         }
 
         for (String exhibitID : exhibitIDs) {
             String exhibitInsertSQL = "INSERT INTO `toursexhibitslink` (TourID, ExhibitID) VALUES ('" + ID + "','" + exhibitID + "');";
             ret = db.executeUpdate(exhibitInsertSQL);
             if (ret == false) {
                 return false;
             }
         }
         return true;
 
     }
 
     public ArrayList<Tour> getAllTours() {
         String sql = "SELECT * FROM "
                 + "`tours` `t` , `exhibits` `e` , `toursexhibitslink` `tel` "
                 + "WHERE `t`.`TourID` = `tel`.`TourID` "
                 + "AND `e`.`ExhibitID` = `tel`.`ExhibitID` ORDER BY `tel`.`TourID`;";
 
         ResultSet rs = db.executeStatement(sql);
         return mapResultSetToArrayList(rs);
     }
 
     private ArrayList<Tour> mapResultSetToArrayList(ResultSet rs) {
         ArrayList<Tour> listOfTours = new ArrayList<Tour>();
         String tourName = "", tourDescription = "", tourID = "", exhibitID = "";
         Tour tempTour = null;
         try {
             while (rs.next()) {
                 if (rs.getString("TourID").equals(tourID)) {
                     tempTour.addExhibit(rs.getInt("ExhibitID"));
                 } else {
                     tourName = rs.getString("TourName");
                     tourDescription = rs.getString("TourDescription");
                     tourID = rs.getString("TourID");
                     exhibitID = rs.getString("ExhibitID");
                     tempTour = new Tour(Integer.parseInt(tourID), tourName, tourDescription);
                     tempTour.addExhibit(Integer.parseInt(exhibitID));
                     listOfTours.add(tempTour);
                 }
             }
 
         } catch (SQLException ex) {
             Logger.getLogger(PersistanceRepositoryUser.class.getName()).log(Level.SEVERE, null, ex);
         }
         return listOfTours;
     }
 }
