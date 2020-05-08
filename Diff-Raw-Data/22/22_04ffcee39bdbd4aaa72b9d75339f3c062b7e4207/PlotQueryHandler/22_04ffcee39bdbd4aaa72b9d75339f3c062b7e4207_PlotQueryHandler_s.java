 package QueryHandlers;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 public class PlotQueryHandler {
 
     private Connection con = null;
     private Statement stmt = null;
     private ResultSet rs = null;
     private ResultSet rs2 = null;
     private String sql = "";
     private ArrayList<String> duchyList, qualityList;
 
     public PlotQueryHandler(Connection c) {
         super();
         con = c;
 
         duchyList = new ArrayList();
         qualityList = new ArrayList();
 
         try {
             sql = "SELECT DuchyName FROM Duchy";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 duchyList.add(rs.getString("DuchyName"));
             }
 
             sql = "SELECT QualityDescription FROM Quality";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 qualityList.add(rs.getString("QualityDescription"));
             }
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler Constructor");
             System.out.println(e.getMessage());
         }
     }
 
     /**
      * This function capitalizes the first letter of the word sent in.
      */
     public String capitalizeFirst(String name) {
         String correction = Character.toString(name.charAt(0));
         correction = correction.toUpperCase();
         name = name.replaceFirst(Character.toString(name.charAt(0)), correction);
         return name;
     }
 
     /**
      * This function returns the price of a certain plot, and the amount needed
      * for monthly upkeep - depending on the provided duchy, and quality of the
      * plot.
      */
     public ArrayList<String[]> queryPlotPrice(String duchy, String quality) {
         boolean correctDuchy = false, correctQuality = false;
         int duchyID, qualityID;
         int plotPrice, plotUpkeep;
         ArrayList<String[]> values;
         String[] line;
 
         duchy = duchy.toLowerCase();
         quality = quality.toLowerCase();
         values = new ArrayList();
         line = new String[3];
 
         //Check parameter validity
         for (int a = 0; a < duchyList.size(); a++) {
             if (duchyList.get(a).toLowerCase().equals(duchy)) {
                 correctDuchy = true;
             }
         }
 
         for (int a = 0; a < qualityList.size(); a++) {
             if (qualityList.get(a).toLowerCase().equals(quality)) {
                 correctQuality = true;
             }
         }
 
         if (correctDuchy != true || correctQuality != true) {
             System.out.println("Incorrect parameters supplied to function queryPlotPrice()");
         } else {
             //Capitalize first letter of duchy and quality
             duchy = capitalizeFirst(duchy);
             quality = capitalizeFirst(quality);
 
             try {
                 //Get the unique ID's of both the duchy, and quality
                 sql = "SELECT DuchyID FROM Duchy WHERE DuchyName = '" + duchy + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
 
                 rs.next();
                 duchyID = Integer.parseInt(rs.getString("DuchyID"));
 
                 sql = "SELECT QualityID FROM Quality WHERE QualityDescription = '" + quality + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
 
                 rs.next();
                 qualityID = Integer.parseInt(rs.getString("QualityID"));
 
                 //Determine price and upkeep costs
                 sql = "SELECT PriceLand, PriceMonthlyUpkeep FROM Price "
                         + "WHERE DuchyID = " + duchyID + " AND QualityID = "
                         + qualityID;
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 plotPrice = Integer.parseInt(rs.getString("PriceLand"));
                 plotUpkeep = Integer.parseInt(rs.getString("PriceMonthlyUpkeep"));
 
                 //Find price values
                 sql = "SELECT AmountPlatinum, AmountGold, AmountSilver "
                         + "FROM Amount WHERE AmountID = "
                         + plotPrice;
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 line[0] = rs.getString("AmountPlatinum");
                 line[1] = rs.getString("AmountGold");
                 line[2] = rs.getString("AmountSilver");
                 values.add(line);
 
                 line = new String[3];
                 sql = "SELECT AmountPlatinum, AmountGold, AmountSilver "
                         + "FROM Amount WHERE AmountID = "
                         + plotUpkeep;
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 line[0] = rs.getString("AmountPlatinum");
                 line[1] = rs.getString("AmountGold");
                 line[2] = rs.getString("AmountSilver");
                 values.add(line);
 
                 return values;
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
         }
 
         return null;
     }
 
     /**
      * This function will convert a 2D integer array, to make it suitable to
      * store in the database.
      */
     public String convertToArray(int[][] inArray) {
         String result = "";
 
         for (int a = 0; a < inArray.length; a++) {
             for (int b = 0; b < inArray[a].length; b++) {
                 if (b == inArray[a].length - 1) {
                     result += inArray[a][b];
                 } else {
                     result += inArray[a][b] + ",";
                 }
             }
             result += ";";
         }
 
         return result;
     }
 
     /**
      * This function will convert the 1D integer array, that was stored in the
      * database, back to a 2D integer array usable by the grid system.
      */
     public int[][] convertFromArray(String inArray) {
         int[][] result;
         StringTokenizer str, stc;
         int rows, columns;
 
         str = new StringTokenizer(inArray, ";");
         rows = str.countTokens();
         stc = new StringTokenizer(str.nextToken(), ",");
         columns = stc.countTokens();
         result = new int[rows][columns];
 
         for (int a = 0; a < rows; a++) {
             for (int b = 0; b < columns; b++) {
                 result[a][b] = Integer.parseInt(stc.nextToken());
             }
             if (a < rows - 1) {
                 stc = new StringTokenizer(str.nextToken(), ",");
             }
         }
 
         return result;
     }
 
     /**
      * This function will add a certain plot to a character, as each character
      * in the game system can own one or more plots.
      */
     public boolean addPlotToCharacter(String characterName, String duchyName, String quality,
             int sizeValue, int[][] groundArray, int[][] buildingArray, int happiness,
             double monthlyIncome, int workersUsed, int workerMax, String baronie, String name) {
         int characterID, duchyID, qualityID;
         String ground, building;
         int exquisite = 0, fine = 0, poor = 0;
 
         //Resolve all ID's
         try {
             sql = "SELECT UserCharacterID FROM UserCharacter WHERE "
                     + "LOWER(UserCharacterName) = '" + characterName.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             characterID = Integer.parseInt(rs.getString("UserCharacterID"));
 
             sql = "SELECT DuchyID FROM Duchy WHERE "
                     + "LOWER(DuchyName) = '" + duchyName.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             duchyID = Integer.parseInt(rs.getString("DuchyID"));
 
             sql = "SELECT QualityID FROM Quality WHERE "
                     + "LOWER(QualityDescription) = '" + quality.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             qualityID = Integer.parseInt(rs.getString("QualityID"));
 
             if (qualityID == 3) {
                 exquisite += 1;
             } else if (qualityID == 2) {
                 fine += 1;
             } else if (qualityID == 1) {
                 poor += 1;
             }
 
             ground = convertToArray(groundArray);
             building = convertToArray(buildingArray);
 
             //Add the plot to database
             int amountID;
             sql = "INSERT INTO Amount VALUES(0,0,0);";
             stmt = con.createStatement();
             stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
 
             rs = stmt.getGeneratedKeys();
             rs.next();
             amountID = rs.getInt(1);
 
             stmt = con.createStatement();
             rs = stmt.executeQuery("SELECT CountyID FROM County WHERE CountyDescription='" + baronie + "'");
             rs.next();
             String CountyID = rs.getString("CountyID");
 
             sql = "INSERT INTO Plot (PlotOwnedBy, PlotAmount, PlotDuchy, PlotSize, "
                     + "PlotGroundArray, PlotBuildingArray, PlotHappiness, "
                     + "PlotMonthlyIncome, PlotWorkersUsed, PlotWorkerMax, "
                     + "PlotAcreExquisite, PlotAcreExquisiteMax, "
                     + "PlotAcreFine, PlotAcreFineMax, "
                     + "PlotAcrePoor, PlotAcrePoorMax, PlotDefenseValue,PlotEstateName,CountyID) VALUES "
                     + "(" + characterID + ", " + amountID + ", " + duchyID + ", " + sizeValue
                     + ", '" + ground + "', '" + building + "', "
                     + +happiness + ", " + monthlyIncome + ", " + workersUsed + ", " + workerMax
                     + ",0 , " + exquisite + ", 0, " + fine + ", 0, " + poor + ", 0.0,'" + name + "','" + CountyID + "')";
 
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Could not execute function addPlotToCharacter()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /**
      * This function will retrieve a list of all the plots owned by the provided
      * character.
      */
     public ArrayList<String[]> retrievePlotsOwnedByCharacter(int characterID) {
         ArrayList<String[]> values;
         String[] line;
 
         values = new ArrayList();
 
         try {
             sql = "SELECT * FROM Plot WHERE "
                     + "PlotOwnedBy = " + characterID;
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             while (rs.next()) {
                 line = new String[20];
                 line[0] = rs.getString("PlotID");
                 line[1] = rs.getString("PlotOwnedBy");
                 line[2] = rs.getString("PlotAmount");
                 line[3] = rs.getString("PlotDuchy");
                 line[4] = rs.getString("PlotSize");
                 line[5] = rs.getString("PlotGroundArray");
                 line[6] = rs.getString("PlotBuildingArray");
                 line[7] = rs.getString("PlotHappiness");
                 line[8] = rs.getString("PlotMonthlyIncome");
                 line[9] = rs.getString("PlotWorkersUsed");
                 line[10] = rs.getString("PlotWorkerMax");
                 line[11] = rs.getString("PlotAcreExquisite");
                 line[12] = rs.getString("PlotAcreExquisiteMax");
                 line[13] = rs.getString("PlotAcreFine");
                 line[14] = rs.getString("PlotAcreFineMax");
                 line[15] = rs.getString("PlotAcrePoor");
                 line[16] = rs.getString("PlotAcrePoorMax");
                 line[17] = rs.getString("PlotDefenseValue");
                 line[18] = (rs.getString("PlotEstateName"));//18
                 line[19] = (rs.getString("CountyID"));//19
                 values.add(line);
             }
 
             //Exchange ID's with values
             for (int a = 0; a < values.size(); a++) {
                 sql = "SELECT UserCharacterName FROM UserCharacter "
                         + "WHERE UserCharacterID = " + values.get(a)[1];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[1] = rs.getString("UserCharacterName");
 
                 sql = "SELECT * FROM Amount WHERE AmountID = "
                         + values.get(a)[2];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[2] = rs.getString("AmountPlatinum") + "-"
                         + rs.getString("AmountGold") + "-"
                         + rs.getString("AmountSilver");
 
                 sql = "SELECT DuchyName FROM Duchy WHERE "
                         + "DuchyID = " + values.get(a)[3];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[3] = rs.getString("DuchyName");
 
                 sql = "SELECT CountyDescription FROM County WHERE CountyID = "
                         + values.get(a)[19];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[19] = rs.getString("CountyDescription");
             }
             return values;
         } catch (Exception e) {
             System.out.println("Could not execute function retrievePlotsOwnedByCharacter()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     /**
      * This function returns all details regarding the provided plotID.
      */
     public ArrayList<String> retrievePlotDetails(int plotID) {
         ArrayList<String> value;
 
         value = new ArrayList();
 
         try {
             sql = "SELECT * FROM Plot WHERE PlotID = " + plotID;
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
 
             value.add(rs.getString("PlotID")); //0
             value.add(rs.getString("PlotOwnedBy"));//1
             value.add(rs.getString("PlotAmount"));//2
             value.add(rs.getString("PlotDuchy"));//3
             value.add(rs.getString("PlotSize"));//4
             value.add(rs.getString("PlotGroundArray"));//5
             value.add(rs.getString("PlotBuildingArray"));//6
             value.add(rs.getString("PlotHappiness"));//7
             value.add(rs.getString("PlotMonthlyIncome"));//8
             value.add(rs.getString("PlotWorkersUsed"));//9
             value.add(rs.getString("PlotWorkerMax"));//10
             value.add(rs.getString("PlotAcreExquisite"));//11
             value.add(rs.getString("PlotAcreExquisiteMax"));//12
             value.add(rs.getString("PlotAcreFine"));//13
             value.add(rs.getString("PlotAcreFineMax"));//14
             value.add(rs.getString("PlotAcrePoor"));//15
             value.add(rs.getString("PlotAcrePoorMax"));//16
             value.add(rs.getString("PlotDefenseValue"));//17
             value.add(rs.getString("PlotEstateName"));//18
             value.add(rs.getString("CountyID"));//19
 
             sql = "SELECT UserCharacterName FROM UserCharacter "
                     + "WHERE UserCharacterID = " + value.get(1);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             value.set(1, rs.getString("UserCharacterName"));
 
             sql = "SELECT * FROM Amount WHERE AmountID = "
                     + value.get(2);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             value.set(2, rs.getString("AmountPlatinum") + "-"
                     + rs.getString("AmountGold") + "-"
                     + rs.getString("AmountSilver"));
 
             sql = "SELECT DuchyName FROM Duchy WHERE "
                     + "DuchyID = " + value.get(3);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             value.set(3, rs.getString("DuchyName"));
 
             sql = "SELECT CountyDescription FROM County WHERE "
                     + "CountyID = " + value.get(19);
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             value.set(19, rs.getString("CountyDescription"));
 
            try {
                sql = "SELECT CountyDescription FROM County WHERE "
                        + "CountyID = " + value.get(19);
                stmt = con.createStatement();
                rs = stmt.executeQuery(sql);
                rs.next();

                value.set(19, "unknown");

                value.set(19, rs.getString("CountyDescription"));
            } catch (Exception ex) {
                value.set(19, "Unknown");
            }

             return value;
         } catch (Exception e) {
             System.out.println("Error, could not execute function retrievePlotDetails()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     /**
      * This function changes plot details to the ones provided. *Danger: This
      * function has the power to change values beyond their specified
      * parameters. PlotAmount supplied in the following format:
      * amountExquisite-amountFine-amountPoor i.e. 1-0-1
      */
     public boolean modifyPlot(int plotID, String characterName, String plotAmount, String duchyName,
             int sizeValue, int[][] groundArray, int[][] buildingArray, int happiness, double monthlyIncome,
             int workersUsed, int workerMax, double acreE, int acreEM, double acreF, int acreFM,
             double acreP, int acrePM, double defenseValue, String estateName, String county) {
 
         int characterID, duchyID, amountID, countyID;
         String ground, building;
         int plat, gold, silver;
 
         //Exchange ID's with values  
         try {
             sql = "SELECT UserCharacterID FROM UserCharacter WHERE "
                     + "LOWER(UserCharacterName) = '" + characterName.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             characterID = Integer.parseInt(rs.getString("UserCharacterID"));
 
             sql = "SELECT DuchyID FROM Duchy WHERE "
                     + "LOWER(DuchyName) = '" + duchyName.toLowerCase() + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             duchyID = Integer.parseInt(rs.getString("DuchyID"));
 
             StringTokenizer st = new StringTokenizer(plotAmount, "-");
             plat = Integer.parseInt(st.nextToken());
             gold = Integer.parseInt(st.nextToken());
             silver = Integer.parseInt(st.nextToken());
 
             sql = "SELECT AmountID FROM Amount WHERE AmountPlatinum = "
                     + plat + " AND AmountGold = " + gold
                     + " AND AmountSilver = " + silver;
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             amountID = Integer.parseInt(rs.getString("AmountID"));
 
             sql = "SELECT CountyID FROM County WHERE CountyDescription = '"
                     + county + "'";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             countyID = Integer.parseInt(rs.getString("CountyID"));
 
             ground = convertToArray(groundArray);
             building = convertToArray(buildingArray);
 
             sql = "UPDATE Plot SET "
                     + "PlotOwnedBy = " + characterID + ", "
                     + "PlotAmount = " + amountID + ", "
                     + "PlotDuchy = " + duchyID + ", "
                     + "PlotSize = " + sizeValue + ", "
                     + "PlotGroundArray = '" + ground + "', "
                     + "PlotBuildingArray = '" + building + "', "
                     + "PlotHappiness = " + happiness + ", "
                     + "PlotMonthlyIncome = " + monthlyIncome + ", "
                     + "PlotWorkersUsed = " + workersUsed + ", "
                     + "PlotWorkerMax = " + workerMax + ", "
                     //+ "PlotAcreExquisite = " + acreE + ", "
                     //+ "PlotAcreExquisiteMax = " + acreEM + ", "
                     //+ "PlotAcreFine = " + acreF + ", "
                     //+ "PlotAcreFineMax = " + acreFM + ", "
                     //+ "PlotAcrePoor = " + acreP + ", "
                     //+ "PlotAcrePoorMax = " + acrePM + ", "
                     + "PlotDefenseValue = " + defenseValue + ", "
                     + "PlotEstateName = '" + estateName + "', "
                     + "CountyID = " + countyID + " "
                     + "WHERE PlotID = " + plotID;
 
             //System.out.println(sql);
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Could not execute function modifyPlot()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /**
      * This function acts as a search function, with multiple criteria, that
      * returns a certain plot's details depending on the criteria provided.
      * Empty fields will yield all results.
      */
     public ArrayList<String[]> searchPlotBy(String characterName, String duchy, int size, String quality) {
         ArrayList<String[]> values = new ArrayList();
         int characterID = 0, duchyID = 0, qualityID = 0;
         String[] line;
 
         values = new ArrayList();
 
         try {
 
 
             if (!duchy.equals("")) {
                 sql = "SELECT DuchyID FROM Duchy WHERE "
                         + "LOWER(DuchyName) = '" + duchy.toLowerCase() + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 duchyID = Integer.parseInt(rs.getString("DuchyID"));
             }
 
             if (!quality.equals("")) {
                 sql = "SELECT QualityID FROM Quality WHERE "
                         + "LOWER(QualityDescription) = '" + quality.toLowerCase() + "'";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 qualityID = Integer.parseInt(rs.getString("QualityID"));
             }
 
 
             //Get ID's
             // if (!characterName.equals("")) {
 
             if ("".equals(characterName.toLowerCase())) {
                 sql = "SELECT UserCharacterID FROM UserCharacter";
             } else {
                 sql = "SELECT UserCharacterID FROM UserCharacter WHERE "
                         + "LOWER(UserCharacterName) like '%" + characterName.toLowerCase() + "%'";
             }
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 characterID = Integer.parseInt(rs.getString("UserCharacterID"));
 
                 sql = "SELECT * FROM Plot";
                 if (characterID != 0) {
                     sql += " WHERE PlotOwnedBy LIKE " + characterID;
                 }
 
                 if (duchyID != 0 && characterID != 0) {
                     sql += " AND PlotDuchy LIKE " + duchyID;
                 } else if (duchyID != 0) {
                     sql += " WHERE PlotDuchy LIKE " + duchyID;
                 }
 
                 if (size > 0 && (characterID != 0 || duchyID != 0)) {
                     sql += " AND PlotSize LIKE " + size;
                 } else if (size > 0) {
                     sql += " WHERE PlotSize LIKE " + size;
                 }
 
                 if (qualityID != 0 && (characterID != 0 || duchyID != 0 || size > 0)) {
                     if (qualityID == 1) {
                         sql += " AND PlotAcrePoorMax > 0";
                     } else if (qualityID == 2) {
                         sql += " AND PlotAcreFineMax > 0";
                     } else if (qualityID == 3) {
                         sql += " AND PlotAcreExquisiteMax > 0";
                     }
                 } else if (qualityID != 0) {
                     if (qualityID == 1) {
                         sql += " WHERE PlotAcrePoorMax > 0";
                     } else if (qualityID == 2) {
                         sql += " WHERE PlotAcreFineMax > 0";
                     } else if (qualityID == 3) {
                         sql += " WHERE PlotAcreExquisiteMax > 0";
                     }
                 }
 
                 stmt = con.createStatement();
                 rs2 = stmt.executeQuery(sql);
 
                 while (rs2.next()) {
                     line = new String[20];
                     line[0] = rs2.getString("PlotID");
                     line[1] = rs2.getString("PlotOwnedBy");
                     line[2] = rs2.getString("PlotAmount");
                     line[3] = rs2.getString("PlotDuchy");
                     line[4] = rs2.getString("PlotSize");
                     line[5] = rs2.getString("PlotGroundArray");
                     line[6] = rs2.getString("PlotBuildingArray");
                     line[7] = rs2.getString("PlotHappiness");
                     line[8] = rs2.getString("PlotMonthlyIncome");
                     line[9] = rs2.getString("PlotWorkersUsed");
                     line[10] = rs2.getString("PlotWorkerMax");
                     line[11] = rs2.getString("PlotAcreExquisite");
                     line[12] = rs2.getString("PlotAcreExquisiteMax");
                     line[13] = rs2.getString("PlotAcreFine");
                     line[14] = rs2.getString("PlotAcreFineMax");
                     line[15] = rs2.getString("PlotAcrePoor");
                     line[16] = rs2.getString("PlotAcrePoorMax");
                     line[17] = rs2.getString("PlotDefenseValue");
                     line[18] = rs2.getString("PlotEstateName");
                     line[19] = rs2.getString("CountyID");
                     values.add(line);
                 }
             }
 
             return values;
         } catch (Exception e) {
             System.out.println("Could not execute function searchPlotBy()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     /**
      * This function deletes a plot. Returns true if deleted successfully, and
      * false if not.
      */
     public boolean deletePlot(int plotID) {
         try {
             sql = "DELETE FROM Plot WHERE PlotID = " + plotID;
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Could not execute function deletePlot()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     public ArrayList<String[]> retrieveAllPlots() {
         String sql = "";
         ArrayList<String[]> result = null;
         String[] line = null;
 
         try {
             sql = "SELECT * FROM Plot";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             ResultSet lrs;
             ResultSet lrs2;
             result = new ArrayList();
             while (rs.next()) {
                 line = new String[20];
                 line[0] = rs.getString("PlotID");
                 line[1] = rs.getString("PlotOwnedBy");
                 line[2] = rs.getString("PlotAmount");
                 line[3] = rs.getString("PlotDuchy");
                 line[4] = rs.getString("PlotSize");
                 line[5] = rs.getString("PlotGroundArray");
                 line[6] = rs.getString("PlotBuildingArray");
                 line[7] = rs.getString("PlotHappiness");
                 line[8] = rs.getString("PlotMonthlyIncome");
                 line[9] = rs.getString("PlotWorkersUsed");
                 line[10] = rs.getString("PlotWorkerMax");
                 line[11] = rs.getString("PlotAcreExquisite");
                 line[12] = rs.getString("PlotAcreExquisiteMax");
                 line[13] = rs.getString("PlotAcreFine");
                 line[14] = rs.getString("PlotAcreFineMax");
                 line[15] = rs.getString("PlotAcrePoor");
                 line[16] = rs.getString("PlotAcrePoorMax");
                 line[17] = rs.getString("PlotDefenseValue");
                 line[18] = rs.getString("PlotEstateName");//18
                 line[19] = rs.getString("CountyID");//19
 
                 sql = "SELECT UserCharacterName FROM UserCharacter "
                         + "WHERE UserCharacterID = " + line[1];
                 stmt = con.createStatement();
                 lrs = stmt.executeQuery(sql);
                 lrs.next();
                 line[1] = lrs.getString("UserCharacterName");
 
                 sql = "SELECT * FROM Amount WHERE AmountID = "
                         + line[2];
                 stmt = con.createStatement();
                 lrs2 = stmt.executeQuery(sql);
                 lrs2.next();
                 line[2] = lrs2.getString("AmountPlatinum") + "-"
                         + lrs2.getString("AmountGold") + "-"
                         + lrs2.getString("AmountSilver");
 
                 sql = "SELECT DuchyName FROM Duchy WHERE "
                         + "DuchyID = " + line[3];
                 stmt = con.createStatement();
                 lrs = stmt.executeQuery(sql);
                 lrs.next();
                 line[3] = lrs.getString("DuchyName");
 
                 result.add(line);
             }
 
             return result;
         } catch (Exception e) {
             System.out.println("Could not execute function retrieveAllPlots()");
             System.out.println(e.getMessage());
         }
         return null;
     }
 
     /* This function retrieves the balance of the provided plot.
      */
     public ArrayList<String> getCurrentAmount(int plotID) {
         ArrayList<String> amounts = new ArrayList();
         int amountID, curPlat, curGold, curSil;
 
         sql = "SELECT PlotAmount FROM Plot WHERE "
                 + "PlotID = " + plotID;
 
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             amountID = Integer.parseInt(rs.getString("PlotAmount"));
 
             sql = "SELECT AmountPlatinum, AmountGold, AmountSilver FROM Amount "
                     + "WHERE AmountID = " + amountID;
             rs = stmt.executeQuery(sql);
             rs.next();
             curPlat = Integer.parseInt(rs.getString("AmountPlatinum"));
             curGold = Integer.parseInt(rs.getString("AmountGold"));
             curSil = Integer.parseInt(rs.getString("AmountSilver"));
 
             amounts.add("" + curPlat);
             amounts.add("" + curGold);
             amounts.add("" + curSil);
 
             return amounts;
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler, function getCurrentAmount().");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
     /* This function modifies they amount of currency(i.e. silver, gold, platinum)
      * that an estate ownes.
      */
 
     public boolean modifyAmount(int inPlotID, int amountPlatinum, int amountGold, int amountSilver) {
         String sql = "";
         int amountID = 0;
 
         sql = "SELECT PlotAmount FROM Plot WHERE "
                 + "PlotID = " + inPlotID;
 
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             amountID = Integer.parseInt(rs.getString("PlotAmount"));
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler, function modifyAmount().");
             System.out.println(e.getMessage());
         }
         sql = "UPDATE Amount SET "
                 + "AmountPlatinum = " + amountPlatinum
                 + ", AmountGold = " + amountGold
                 + ", AmountSilver = " + amountSilver
                 + " WHERE AmountID = " + amountID;
 
 
         try {
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler, function modifyAmount() marker2");
             System.out.println(e.getMessage());
         }
 
         return false;  //Not successful
     }
 
     /* This function adds an amount to the provided plot.
      */
     public boolean depositAmount(int plotID, int amountPlatinum, int amountGold, int amountSilver) {
         int amountID, curPlat, curGold, curSil;
 
         sql = "SELECT PlotAmount FROM Plot WHERE PlotID = " + plotID;
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             amountID = Integer.parseInt(rs.getString("PlotAmount"));
 
             sql = "SELECT AmountPlatinum, AmountGold, AmountSilver FROM Amount "
                     + "WHERE AmountID = " + amountID;
             rs = stmt.executeQuery(sql);
             rs.next();
             curPlat = Integer.parseInt(rs.getString("AmountPlatinum"));
             curGold = Integer.parseInt(rs.getString("AmountGold"));
             curSil = Integer.parseInt(rs.getString("AmountSilver"));
 
             curPlat += amountPlatinum;
             curGold += amountGold;
             curSil += amountSilver;
 
             sql = "UPDATE Amount SET "
                     + "AmountPlatinum = " + curPlat + ", "
                     + "AmountGold = " + curGold + ", "
                     + "AmountSilver = " + curSil + " "
                     + "WHERE AmountID = " + amountID;
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler, function addAmount()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /* This function removes an amount from the provided plot.
      */
     public boolean withdrawAmount(int plotID, int amountPlatinum, int amountGold, int amountSilver) {
         int amountID, curPlat, curGold, curSil;
 
         sql = "SELECT PlotAmount FROM Plot WHERE PlotID = " + plotID;
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
             amountID = Integer.parseInt(rs.getString("PlotAmount"));
 
             sql = "SELECT AmountPlatinum, AmountGold, AmountSilver FROM Amount "
                     + "WHERE AmountID = " + amountID;
             rs = stmt.executeQuery(sql);
             rs.next();
             curPlat = Integer.parseInt(rs.getString("AmountPlatinum"));
             curGold = Integer.parseInt(rs.getString("AmountGold"));
             curSil = Integer.parseInt(rs.getString("AmountSilver"));
 
             curPlat -= amountPlatinum;
             curGold -= amountGold;
             curSil -= amountSilver;
 
             if (curPlat < 0 || curGold < 0 || curSil < 0) {
                 System.out.println("Error in removeAmount() - removed too much. An amount fell below zero.");
             } else {
                 sql = "UPDATE Amount SET "
                         + "AmountPlatinum = " + curPlat + ", "
                         + "AmountGold = " + curGold + ", "
                         + "AmountSilver = " + curSil + " "
                         + "WHERE AmountID = " + amountID;
                 stmt = con.createStatement();
                 stmt.execute(sql);
 
                 return true;
             }
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler, function addAmount()");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     /*Adds an extra plot(3x3 grid) to the provided estate.
      * plotID - Unique ID of the character's plot
      * quality - Description of the quality of plot to be added. e.g. 'fine'
      * groundArray - 2D array of ground tiles.
      * 
      * *Note: Need to supply groundArray because of lack of functionality
      * in the PlotQueryHandler to generate random water tiles. (To be added
      * later).
      */
     public boolean expandPlot(int plotID, String quality, int[][] groundArray, int[][] buildingArray) {
         int exquisite, fine, poor;
         String gArray = "";
 
         quality = quality.toLowerCase();
 
         if (quality.equals("exquisite") || quality.equals("fine") || quality.equals("poor")) {
             try {
                 sql = "SELECT PlotGroundArray, PlotAcreExquisiteMax, PlotAcreFineMax, PlotAcrePoorMax "
                         + "FROM Plot WHERE PlotID = " + plotID;
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
 
                 //System.out.println(sql);
                 exquisite = Integer.parseInt(rs.getString("PlotAcreExquisiteMax"));
                 //System.out.println("c " + exquisite);
                 fine = Integer.parseInt(rs.getString("PlotAcreFineMax"));
                 //System.out.println("c " + fine);
                 poor = Integer.parseInt(rs.getString("PlotAcrePoorMax"));
                 //System.out.println("c " + poor);
 
                 // gArray = rs.getString("PlotGroundArray");
                 if (quality.equals("exquisite")) {
                     exquisite += 1;
                 } else if (quality.equals("fine")) {
                     fine += 1;
                 } else {
                     poor += 1;
                 }
 
                 gArray = gArray + convertToArray(groundArray);
                 //Add back to database
 
                 sql = "UPDATE Plot SET "
                         + "PlotAcreExquisiteMax = " + exquisite + ", "
                         + "PlotAcreFineMax = " + fine + ", "
                         + "PlotAcrePoorMax = " + poor + ","
                         + "PlotGroundArray = '" + gArray + "', "
                         + "PlotBuildingArray = '" + convertToArray(buildingArray) + "', "
                         + "PlotSize = '" + groundArray.length + "' "
                         + "WHERE PlotID = " + plotID;
                 //System.out.println(sql);
                 stmt = con.createStatement();
                 stmt.execute(sql);
                 return true;  //Update successful
             } catch (Exception e) {
                 System.out.println("Error in PlotQueryHander, function expandPlot()");
                 System.out.println(e.getMessage());
 
             }
         } else {
             System.out.println("Invalid quality supplied.");
         }
 
         return false;  //Unsuccessful expansion
     }
 
     /* This function returns the amount of exquisite, fine, poor acres the
      * provided plot has.
      */
     public ArrayList<String> getAcreQualityAmounts(int plotID) {
         ArrayList<String> amounts = new ArrayList();
 
         sql = "SELECT PlotAcreExquisiteMax, PlotAcreFineMax, PlotAcrePoorMax "
                 + "FROM Plot WHERE PlotID = " + plotID;
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
 
             amounts.add("" + Integer.parseInt(rs.getString("PlotAcreExquisiteMax")));
             amounts.add("" + Integer.parseInt(rs.getString("PlotAcreFineMax")));
             amounts.add("" + Integer.parseInt(rs.getString("PlotAcrePoorMax")));
 
             return amounts;
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler, function getAcreQualityAmount()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     /* This function will be used when buildings are bought, to add to acres used,
      * and to check for limit overflow.
      * 
      * Returns false if modification is unsuccessful.
      */
     public boolean useAcresOnPlot(int plotID, double acreExquisite, double acreFine, double acrePoor) {
         double curEx, curFine, curPoor;
 
         sql = "SELECT PlotAcreExquisite, PlotAcreExquisiteMax, PlotAcreFine, PlotAcreFineMax, "
                 + "PlotAcrePoor, PlotAcrePoorMax "
                 + "FROM Plot WHERE PlotID = " + plotID;
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
 
             curEx = Double.parseDouble(rs.getString("PlotAcreExquisite"));
             curFine = Double.parseDouble(rs.getString("PlotAcreFine"));
             curPoor = Double.parseDouble(rs.getString("PlotAcrePoor"));
 
             //Check for overflow
             if ((curEx + acreExquisite) > Integer.parseInt(rs.getString("PlotAcreExquisiteMax"))) {
                 System.out.println("Error in function useAcresOnPlot() - Exquisite acre overflow.");
             } else if ((curFine + acreFine) > Integer.parseInt(rs.getString("PlotAcreFineMax"))) {
                 System.out.println("Error in function useAcresOnPlot() - Fine acre overflow.");
             } else if ((curPoor + acrePoor) > Integer.parseInt(rs.getString("PlotAcrePoorMax"))) {
                 System.out.println("Error in function useAcresOnPlot() - Poor acre overflow.");
             } else {
                 //All acre useages are within limits. Add to table.
                 sql = "UPDATE Plot SET "
                         + "PlotAcreExquisite = " + (curEx + acreExquisite)
                         + ", PlotAcreFine = " + (curFine + acreFine)
                         + ", PlotAcrePoor = " + (curPoor + acrePoor)
                         + " WHERE PlotID = " + plotID;
                 stmt = con.createStatement();
                 stmt.execute(sql);
 
                 return true;
             }
         } catch (Exception e) {
             System.out.println("Error in PlotQueryHandler, in function useAcresOnPlot().");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     //-----------------------------------------------------------------------//
     /* This function allows one to add a character to a plot, thus 'sharing' 
      * access to that plot. These are the following rights:
      * deposit - character can deposit gold into plot.
      * withdraw - character can withdraw gold from the plot.
      * buy - character can buy buildings for the plot(with the plot's gold).
      * place - character can place building tokens.
      * expand - character can expand the plot.
      * 
      * Function returns true if successful, false if not.
      * 
      * *Note: The PlotID/UserCharacterID pair in the Plot table means that that
      *  character has all rights to that plot.
      */
     public boolean addPlotAccess(int plotID, int userID, boolean deposit,
             boolean withdraw, boolean buy, boolean place, boolean expand,
             boolean status) {
         int depositBit, withdrawBit, buyBit, placeBit, expandBit, statusBit;
 
         if (deposit) {
             depositBit = 1;
         } else {
             depositBit = 0;
         }
 
         if (withdraw) {
             withdrawBit = 1;
         } else {
             withdrawBit = 0;
         }
 
         if (buy) {
             buyBit = 1;
         } else {
             buyBit = 0;
         }
 
         if (place) {
             placeBit = 1;
         } else {
             placeBit = 0;
         }
 
         if (expand) {
             expandBit = 1;
         } else {
             expandBit = 0;
         }
 
         if (status) {
             statusBit = 1;
         } else {
             statusBit = 0;
         }
 
         sql = "INSERT INTO PlotAccess VALUES("
                 + plotID + ", " + userID + ", " + depositBit + ", "
                 + withdrawBit + ", " + buyBit + ", " + placeBit + ", "
                 + expandBit + ", " + statusBit + ")";
 
         try {
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Error in function addPlotAccess():");
             System.out.println(e.getMessage());
         }
 
 
         return false;
     }
 
     /* This function modifies a certain characters access rights to a certain
      * plot.
      */
     public boolean modifyPlotAccess(int plotID, int userID, boolean deposit,
             boolean withdraw, boolean buy, boolean place, boolean expand,
             boolean status) {
         int depositBit, withdrawBit, buyBit, placeBit, expandBit, statusBit;
 
         if (deposit) {
             depositBit = 1;
         } else {
             depositBit = 0;
         }
 
         if (withdraw) {
             withdrawBit = 1;
         } else {
             withdrawBit = 0;
         }
 
         if (buy) {
             buyBit = 1;
         } else {
             buyBit = 0;
         }
 
         if (place) {
             placeBit = 1;
         } else {
             placeBit = 0;
         }
 
         if (expand) {
             expandBit = 1;
         } else {
             expandBit = 0;
         }
 
         if (status) {
             statusBit = 1;
         } else {
             statusBit = 0;
         }
 
         sql = "UPDATE PlotAccess SET "
                 + "PlotAccessDeposit = " + depositBit + ", "
                 + "PlotAccessWithdraw = " + withdrawBit + ", "
                 + "PlotAccessBuy = " + buyBit + ", "
                 + "PlotAccessPlace = " + placeBit + ", "
                 + "PlotAccessExpand = " + expandBit + ", "
                 + "PlotAccessStatus = " + statusBit + " "
                 + "WHERE PlotID = " + plotID + " AND "
                 + "UserCharacterID = " + userID;
 
         try {
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Error in function addPlotAccess():");
             System.out.println(e.getMessage());
         }
 
 
         return false;
     }
 
     /* This function returns the access rights of a certain user on a certain
      * plot.
      */
     public ArrayList<String> getPlotAccess(int plotID, int userID) {
         ArrayList<String> result = null;
 
         sql = "SELECT PlotAccessDeposit, PlotAccessWithdraw, PlotAccessBuy, "
                 + "PlotAccessPlace, PlotAccessExpand, PlotAccessStatus "
                 + "FROM PlotAccess "
                 + "WHERE PlotID = " + plotID
                 + " AND UserCharacterID = " + userID;
 
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             if (rs.next()) {
                 result = new ArrayList();
 
                 result.add(rs.getString("PlotAccessDeposit"));
                 result.add(rs.getString("PlotAccessWithdraw"));
                 result.add(rs.getString("PlotAccessBuy"));
                 result.add(rs.getString("PlotAccessPlace"));
                 result.add(rs.getString("PlotAccessExpand"));
                 result.add(rs.getString("PlotAccessStatus"));
 
                 return result;
             } else {
                 System.out.println("Error in function getPlotAccess():");
                 System.out.println("No results");
             }
         } catch (Exception e) {
             System.out.println("Error in function getPlotAccess():");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
 // returns all acces rights for a plot
     public ArrayList<String[]> getAllAccess(int plotID) {
         ArrayList<String[]> result = new ArrayList();
         String[] line;
 
         sql = "SELECT PlotAccessDeposit, PlotAccessWithdraw, PlotAccessBuy, "
                 + "PlotAccessPlace, PlotAccessExpand, PlotAccessStatus,UserCharacterID "
                 + "FROM PlotAccess "
                 + "WHERE PlotID = " + plotID;
 
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 line = new String[7];
 
                 line[0] = rs.getString("PlotAccessDeposit");
                 line[1] = rs.getString("PlotAccessWithdraw");
                 line[2] = rs.getString("PlotAccessBuy");
                 line[3] = rs.getString("PlotAccessPlace");
                 line[4] = rs.getString("PlotAccessExpand");
                 line[5] = rs.getString("PlotAccessStatus");
                 line[6] = rs.getString("UserCharacterID");
 
                 result.add(line);
             }
             return result;
         } catch (Exception e) {
             System.out.println("Error in function getPlotAccess():");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     //removes a person from a plots access
     public boolean removeAccess(int PlotID, int userID) {
 
         sql = "DELETE "
                 + "FROM PlotAccess "
                 + "WHERE PlotID = " + PlotID
                 + " AND UserCharacterID = " + userID;
 
         try {
             stmt = con.createStatement();
             stmt.execute(sql);
 
             return true;
         } catch (Exception e) {
             System.out.println("Error in function removeAccess():");
             System.out.println(e.getMessage());
         }
 
         return false;
     }
 
     //Returns all the plots that the userID has rights to
     public ArrayList<String[]> AllPlotsIHaveAccess(int userID) {
         ArrayList<String[]> result = new ArrayList();
         String[] line = null;
 
         sql = "SELECT PlotID,PlotAccessDeposit, PlotAccessWithdraw, PlotAccessBuy, "
                 + "PlotAccessPlace, PlotAccessExpand, PlotAccessStatus "
                 + "FROM PlotAccess "
                 + "WHERE UserCharacterID = " + userID;
 
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
 
 
                 line = new String[7];
                 line[0] = rs.getString("PlotID");
                 line[1] = rs.getString("PlotAccessDeposit");
                 line[2] = rs.getString("PlotAccessWithdraw");
                 line[3] = rs.getString("PlotAccessBuy");
                 line[4] = rs.getString("PlotAccessPlace");
                 line[5] = rs.getString("PlotAccessExpand");
                 line[6] = rs.getString("PlotAccessStatus");
 
                 result.add(line);
 
             }
             return result;
         } catch (Exception e) {
             System.out.println("Error in function getPlotAccess():");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     //-----------------------------------------------------------------------//
     public void DoExspand(int pId, double Upkeep, int workerMax) {
 
         try {
             stmt = con.createStatement();
             stmt.execute("UPDATE Plot set PlotMonthlyIncome=" + Upkeep + ",PlotWorkerMax=" + workerMax + " where PlotID=" + pId);
 
         } catch (Exception e) {
             System.out.println("Error in function DoExspand():");
             System.out.println(e.getMessage());
         }
     }
 
     public void PlaceBuilding(int PlotID, int[][] buildings) {
         sql = "UPDATE Plot SET PlotBuildingArray='" + convertToArray(buildings) + "' WHERE PlotID=" + PlotID;
         try {
             stmt = con.createStatement();
             stmt.execute(sql);
 
         } catch (Exception e) {
             System.out.println("Error in function PlaceBuilding():");
             System.out.println(e.getMessage());
         }
     }
 
     public void MarkBuildingAsPlaced(int BuildLogID) {
         sql = "UPDATE BuildLog SET BuildLogPlaced='1' WHERE BuildLogID=" + BuildLogID;
         try {
             stmt = con.createStatement();
             stmt.execute(sql);
 
         } catch (Exception e) {
             System.out.println("Error in function MarkBuildingAsPlaced():");
             System.out.println(e.getMessage());
         }
     }
 
     public void setDescription(String desc,int PlotID) {
         sql = "UPDATE Plot SET PlotDescription='"+desc+"' WHERE PlotID=" + PlotID;
         try {
             stmt = con.createStatement();
             stmt.execute(sql);
 
         } catch (Exception e) {
             System.out.println("Error in function setDescription():");
             System.out.println(e.getMessage());
         }
     }
 
     public String getDescription(int PlotID) {
 
         sql = "SELECT PlotDescription FROM Plot WHERE PlotID="+PlotID;
         try {
             stmt = con.createStatement();
             ResultSet executeQuery = stmt.executeQuery(sql);
             executeQuery.next();
             return executeQuery.getString("PlotDescription");
         } catch (Exception e) {
             System.out.println("Error in function getDescription():");
             System.out.println(e.getMessage());
         }
         return "";
     }
     
     public ArrayList<String> getHappinessEffect(int happiness) {
         ArrayList<String> result = null;
         
         result = new ArrayList();
         
         if(happiness > 1000) {  //Max amount of happiness
             result.add("25");
             result.add("0");
             return result;
         }
         else {
             try {
                 stmt = con.createStatement();
                 rs = stmt.executeQuery("SELECT HappinessEffectIncome, HappinessEffectRebel "
                         + "FROM HappinessEffect WHERE HappinessEffectBottom <= " + happiness
                         + " AND HappinessEffectTop >= " + happiness);
                 rs.next();
                 
                 result.add(rs.getString("HappinessEffectIncome"));
                 result.add(rs.getString("HappinessEffectRebel"));
             }
             catch(Exception e) {
                 System.out.println("Error in function getHappinessEffect()");
                 System.out.println(e.getMessage());
             }
 
             return result;
         }
     }
 }
