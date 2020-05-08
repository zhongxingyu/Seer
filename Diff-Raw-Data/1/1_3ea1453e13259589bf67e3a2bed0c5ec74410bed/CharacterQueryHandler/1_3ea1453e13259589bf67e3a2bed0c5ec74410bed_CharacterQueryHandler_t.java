 package QueryHandlers;
 
 import Connection.DatabaseConnection;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 public class CharacterQueryHandler {
 
     private Connection con = null;
     private Statement stmt = null;
     private ResultSet rs = null;
     private ResultSetMetaData rsmd = null;
     private String sql = "";
 
      /* Constructor
      */
     public CharacterQueryHandler(Connection c) {
         super();
         con = c;
     }
 
     /* This function registers a certain character to the estate system database.
      * The function returns true if a character has been successfully added, and
      * false if not.
      */
     public boolean registerEstateCharacter(String characterName, String userID) {
         try {
             //Check if character is already registered to the Estate system
             sql = "SELECT * FROM UserCharacter "
                     + "WHERE  UserCharacterName like '" + characterName + "%' AND ProdUserID='" + userID + "'";
             System.out.println(sql);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             System.out.println("registering " + characterName);
 
             if (!rs.next()) {
 
 
                 int num = 0;
 
                 DatabaseConnection prod = new DatabaseConnection();
                 Connection prodCon = prod.openConnectionProd();
                 Statement s1 = prodCon.createStatement();
                 ResultSet unqiIDs = s1.executeQuery("SELECT CharacterID FROM CharacterProfile WHERE CharacterName='" + characterName + "'");
 
                 unqiIDs.next();
 
 
 
                 sql = "INSERT INTO UserCharacter (UserCharacterName, UserCharacterStatus,ProdUserID,ProdCharacterID) "
                         + "VALUES ('" + characterName + "&*&" + num + "', " + "0,'" + userID + "','" + unqiIDs.getString("CharacterID") + "')";
 
                 stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
                 rs = stmt.getGeneratedKeys();
                 rs.next();
                 num = rs.getInt(1);
 
                 sql = "UPDATE UserCharacter SET UserCharacterName='" + characterName + "&*&" + num + "' WHERE UserCharacterName='" + characterName + "&*&" + 0 + "'";
 
                 stmt = con.createStatement();
                 stmt.execute(sql);
 
                 return true;
             } else {
                 System.out.println("set not empty=====================================");
                 System.out.println(rs.getString("UserCharacterName"));
                 return false;
             }
 
         } catch (Exception e) {
             System.out.println("Could not execute function registerEstateCharacter()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /* This function returns a certain character's ID based on the character
      * name supplied.
      */
     public int retrieveCharacterID(String characterName) {
         System.out.println(characterName);
         try {
             sql = "SELECT UserCharacterID FROM UserCharacter WHERE "
                     + "UserCharacterName LIKE '%" + characterName + "%'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             return Integer.parseInt(rs.getString("UserCharacterID"));
         } catch (Exception e) {
             System.out.println("Could not execute function retrieveCharacterID()");
             System.out.println(e.getMessage());
         }
         return 0;
     }
     /* This function returns a certain character's name based on the character
      * id supplied.
      */
 
     /* This function returns a character name that responds to the unique
      * identifier provided: characterID (UserCharacterID)
      */
     public String retrieveCharacterName(int characterID) {
         try {
             sql = "SELECT UserCharacterName FROM UserCharacter WHERE "
                     + "UserCharacterID = '" + characterID + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             return rs.getString("UserCharacterName");
         } catch (Exception e) {
             System.out.println("Could not execute function retrieveCharacterID()");
             System.out.println(e.getMessage());
         }
         return "";
     }
 
     /* This function returns a certain character's ID based on the character
      * name supplied. This function was modified to return more results.
      */
     public ArrayList<String[]> retrieveCharacterIDExtra(String characterName) {
         ArrayList<String[]> result = new ArrayList();
         String[] line = null;
         
         try {
             sql = "SELECT UserCharacterID,UserCharacterName FROM UserCharacter WHERE "
                     + "UserCharacterName LIKE '%" + characterName + "%'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             while (rs.next()) {
                 line = new String[2];
                 line[0] = rs.getString("UserCharacterID");
                 line[1] = rs.getString("UserCharacterName");
                 result.add(line);
             }
 
         } catch (Exception e) {
             System.out.println("Could not execute function retrieveCharacterID()");
             System.out.println(e.getMessage());
         }
         return result;
     }
 
     /* This function returns a list of all the characters registered to the
      * estate system database.
      */
     public ArrayList<String[]> retrieveAllCharacters() {
         ArrayList<String[]> values = null;
         String[] line = null;
 
         values = new ArrayList();
 
         try {
             sql = "SELECT * FROM UserCharacter";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             while (rs.next()) {
                 line = new String[3];
                 line[0] = rs.getString("UserCharacterID");
                 line[1] = rs.getString("UserCharacterName");
                 line[2] = rs.getString("UserCharacterStatus");
                 values.add(line);
             }
 
             return values;
         } catch (Exception e) {
             System.out.println("Could not execute function retrieveAllCharacters()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     /* This function returns the amount of platinum, gold, and silver the
      * character currently has.
      */
     public ArrayList<String> getCharacterAmounts(String characterName) {
         DatabaseConnection prod = null;
         Connection prodCon = null;
         UserQueryHandler uqh = null;
         ArrayList<String> result = new ArrayList();
         int silver = 0, amPlat = 0, amGold = 0, amSil = 0;
         String charID = "";
 
         prod = new DatabaseConnection();
         prodCon = prod.openConnectionProd();
         uqh = new UserQueryHandler(prodCon);
 
         //Convert name to unique characterId
         try {
             sql = "SELECT ProdCharacterID FROM UserCharacter WHERE UserCharacterName = "
                     + "'" + characterName + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             charID = rs.getString("ProdCharacterID");
         } catch (Exception e) {
             System.out.println("Error in CharacterQueryHandler, function "
                     + "getCharacterAmounts()");
             System.out.println(e.getMessage());
         }
 
         silver = uqh.getCharacterSilver(charID);
 
         while (silver >= 100) {
             silver = silver - 100;
             amPlat += 1;
         }
 
         while (silver >= 10) {
             silver = silver - 10;
             amGold += 1;
         }
 
         result.add(Integer.toString(amPlat));
         result.add(Integer.toString(amGold));
         result.add(Integer.toString(amSil));
 
         return result;
     }
 
     /* This function outright modifies the character's amount of silver.
      * 
      * WARNING: this function can be misused to give a character no silver. 
      * Do not mistake this function for addition/subtraction.
      */
     public boolean modifyAmount(String characterName, int amountPlatinum, int amountGold, int amountSilver) {
         DatabaseConnection prod = null;
         Connection prodCon = null;
         UserQueryHandler uqh = null;
         int currentSilver = 0, targetSilver = 0, difference = 0;
         String charID = "", userID = "";
 
         //Convert name to unique characterId
         try {
             sql = "SELECT ProdUserID, ProdCharacterID FROM UserCharacter WHERE UserCharacterName = "
                     + "'" + characterName + "'";
             System.out.println(sql);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             charID = rs.getString("ProdCharacterID");
             userID = rs.getString("ProdUserID");
 
             //Get current silver total
             prod = new DatabaseConnection();
             prodCon = prod.openConnectionProd();
             uqh = new UserQueryHandler(prodCon);
 
             currentSilver = uqh.getCharacterSilver(charID);
             targetSilver = (amountPlatinum * 100) + (amountGold * 10) + (amountSilver);
 
             if (targetSilver == currentSilver) {
                 return true;  //success
             } else {
                 difference = targetSilver - currentSilver;
             }
 
             //insert difference into log table
             uqh.setCharacterSilver(charID, userID, difference);
 
             return true;
 
         } catch (Exception e) {
             System.out.println("Error in CharacterQueryHandler, function "
                     + "modifyAmount()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /* Addd to, or removes from, the character's social status.
      * statusAmount can be positive, or negative depeding on whether
      * the amount must be increased, or decreased respectively.
      * i.e. 10, or -23
      */
     public boolean modifyStatus(int characterID, int statusAmount) {
         int currentStatus = 0;
 
         try {
             //Get current status value
             sql = "SELECT UserCharacterStatus FROM UserCharacter "
                     + "WHERE UserCharacterID = " + characterID;
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
 
             currentStatus = Integer.parseInt(rs.getString("UserCharacterStatus"));
             if ((currentStatus + statusAmount) >= 0 || (currentStatus + statusAmount) <= 100) {
                 currentStatus = currentStatus + statusAmount;
                 sql = "UPDATE UserCharacter SET "
                         + "UserCharacterStatus = " + currentStatus
                         + " WHERE UserCharacterID = " + characterID;
                 stmt = con.createStatement();
                 stmt.execute(sql);
 
                 return true;
             } else {
                 System.out.println("Error in CharacterQueryHandler, function modifyStatus()");
                 System.out.println("Character status parameter overflow.");
             }
 
         } catch (Exception e) {
             System.out.println("Error in CharacterQueryHandler, function modifyStatus()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /* This function checks if the provided character (ID) has admin rights
      */
     public boolean isAdmin(String userID) {
 
         try {
             //Get current status value
             sql = "SELECT UserCharacterAdmin FROM UserCharacter "
                     + "WHERE UserCharacterID = " + userID;
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
 
             String is = rs.getString("UserCharacterAdmin");
             
             if (is == null) {
                 return false;
             } else if ("true".equals(is.toLowerCase()) || "1".equals(is)) {
                 return true;
             } else {
                 return false;
             }
 
         } catch (Exception e) {
             System.out.println("Error in CharacterQueryHandler, function isAdmin()");
             System.out.println(e.getMessage());
         }
 
         return false;
 
     }
 }
