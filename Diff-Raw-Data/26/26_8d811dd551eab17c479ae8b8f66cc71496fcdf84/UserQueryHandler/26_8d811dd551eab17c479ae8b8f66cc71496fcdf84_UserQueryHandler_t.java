 package QueryHandlers;
 
 import Connection.DatabaseConnection;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 public class UserQueryHandler {
     private DatabaseConnection db = null;
     private Connection con = null;
     private Statement stmt = null;
     private ResultSet rs = null;
     private String sql = "";
     private ArrayList<String> userList;
 
     public UserQueryHandler(Connection c) {
         super();
         con = c;
 
         userList = new ArrayList();
 
         try {
             sql = "SELECT UserId FROM aspnet_Users";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 userList.add(rs.getString("UserId"));
             }
         } catch (Exception e) {
             System.out.println("Error in UserQueryHandler Constructor");
             System.out.println(e.getMessage());
         }
     }
 
     public void closeConnection() {
         db.closeConnection();
     }
 
     /**
      * This function prints a list of all the users in the TalesProd database
      * (not all of these users are relevant to the estate system).
      */ 
     public void printUserList() {
         if (userList.size() > 0) {
             for (int a = 0; a < userList.size(); a++) {
                 System.out.println(userList.get(a));
             }
         }
     }
 
     /**
      * This function checks whether a certain userID exists.
      */
     public boolean checkLogin(String userID) {
         for (int a = 0; a < userList.size(); a++) {
             if (userID.equals(userList.get(a))) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * This function checks whether a certain user has a character that is
      * registered to the estate system.
      */ 
     public boolean checkHasCharacter(String userID) {
         String id;
         ArrayList<String> ids;
         boolean go;
 
         ids = new ArrayList();
 
         try {
             //Get ID of PC type
             sql = "SELECT ProfileTypeID FROM ProfileTypes WHERE "
                     + "ProfileTypeName = 'PC'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             id = rs.getString("ProfileTypeID");
 
             //Get all characters owned by a user
             sql = "SELECT * FROM UserHasProfiles WHERE "
                     + "Convert(VARCHAR(255), UserId) = '" + userID + "' "
                     + "AND CharacterProfileId IS NOT NULL";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             //Check character list for only type PC
             while (rs.next()) {
                 ids.add(rs.getString("CharacterProfileId"));
             }
 
             go = false;
 
             for (int a = 0; a < ids.size(); a++) {
                 sql = "SELECT * FROM CharacterProfile WHERE "
                         + "Convert(VARCHAR(255), CharacterID) = '" + ids.get(a) + "' "
                         + "AND Convert(VARCHAR(255), ProfileTypeID) = '" + id + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 if (rs.next()) {
                     go = true;
                 }
             }
 
             return go;
         } catch (Exception e) {
             System.out.println("Unable to execute function checkHasCharacter()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /**
      * This function retrieves a list of all the estate-ready characters owned
      * by the provided user.
      */
     public ArrayList<String> retrieveCharactersOwnedByUser(String userID) {
         ArrayList<String> values = null, check = null;
         String id;
 
         values = new ArrayList();
         check = new ArrayList();
 
         try {
             //Get ID of PC type
             sql = "SELECT ProfileTypeID FROM ProfileTypes WHERE "
                     + "ProfileTypeName = 'PC'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             id = rs.getString("ProfileTypeID");
 
             sql = "SELECT CharacterProfileId FROM UserHasProfiles "
                     + "WHERE Convert(VARCHAR(255), UserId) = "
                     + "'" + userID + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             while (rs.next()) {
                 check.add(rs.getString("CharacterProfileId"));
             }
 
             for (int a = 0; a < check.size(); a++) {
                 sql = "SELECT CharacterName FROM CharacterProfile "
                         + "WHERE Convert(VARCHAR(255), CharacterId) = "
                         + "'" + check.get(a) + "' AND "
                         + "Convert(VARCHAR(255), ProfileTypeId) = '" + id + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 if (rs.next()) {
                     values.add(rs.getString("CharacterName"));
                 }
             }
 
             return values;
         } catch (Exception e) {
             System.out.println("Unable to execute function retrieveCharacterList()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
         
     /* This function takes the primary key of the UserCharacter and returns
      * the amount of silver that the character has
      */
     public int getCharacterSilver(String characterID) {
         int silver;
         
         sql = "SELECT SUM(Quantity) AS TotalSilver FROM CharacterSilverLog WHERE "
                 + "Convert(VARCHAR(255), CharacterId) = '" + characterID+"'";
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
            silver = rs.getInt(1);
             
             return silver;
         }
         catch(Exception e) {
             System.out.println("Error in UserQueryHandler, function getCharact"
                     + "erSilver().");
             System.out.println(e.getMessage());
         }
         
         return -1;
     }
     
    public boolean setCharacterSilver(String characterID, String userID, int silver) {
         
         sql = "INSERT INTO CharacterSilverLog VALUES ("
                + "NEWID(), "
                 + "'" + characterID + "', "
                 + "'" + "Estate system" + "', "
                 + "'" + userID + "', "
                 + silver + ", "
                + "NULL, "
                 + "GETDATE()"
                 + ");";
         try {
             stmt = con.createStatement();
             stmt.execute(sql);
             
             return true;
         }
         catch(Exception e) {
             System.out.println("Error in UserQueryHandler, function setCharac"
                     + "terSilver()");
             System.out.println(e.getMessage());
         }
         
         return false;
     }
     
     public String getCharacterName(String charID) {
         String result = "";
         
         sql = "SELECT CharacterName FROM CharacterProfile WHERE "
                 + "Convert(VARCHAR(255), CharacterId) = '" + charID + "'";
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             
             result = rs.getString("CharacterName");
             if(!result.equals("")) {
                 return result;
             }
             else
                 throw new Exception();
         }
         catch(Exception e) {
             System.out.println("Error in UserQueryHandler, function getCharac"
                     + "terName()");
             System.out.println(e.getMessage());
         }
         
         return "";
     }
 }
