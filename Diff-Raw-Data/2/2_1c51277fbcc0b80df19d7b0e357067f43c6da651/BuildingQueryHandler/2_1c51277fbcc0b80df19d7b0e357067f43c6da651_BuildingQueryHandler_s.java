 package QueryHandlers;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 public class BuildingQueryHandler {
 
     private Connection con = null;
     private Statement stmt = null;
     private ResultSet rs = null;
     private String sql = "";
     private ArrayList<String> duchyList, industryList, qualityList;
 
     public BuildingQueryHandler(Connection c) {
         super();
         con = c;
 
         //Create lists of descriptions/names of the following:
         //(Used when resolving ID's)
         duchyList = new ArrayList();
         industryList = new ArrayList();
         qualityList = new ArrayList();
 
         try {
             sql = "SELECT DuchyName FROM Duchy";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 duchyList.add(rs.getString("DuchyName"));
             }
 
             sql = "SELECT BuildingTypeDescription FROM BuildingType";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 industryList.add(rs.getString("BuildingTypeDescription"));
             }
 
             sql = "SELECT QualityDescription FROM Quality";
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 qualityList.add(rs.getString("QualityDescription"));
             }
         } catch (Exception e) {
             System.out.println("Error in BuildingQueryHandler Constructor");
             System.out.println(e.getMessage());
         }
     }
 
     /* This function takes in a string, and capitalizes the first letter of that
      * string.
      */
     public String capitalizeFirst(String name) {
         String correction = Character.toString(name.charAt(0));
         correction = correction.toUpperCase();
         name = name.replaceFirst(Character.toString(name.charAt(0)), correction);
 
         return name;
     }
 
     /* This function retrieves a list of all the buildings, across all industries.
      */
     public ArrayList<String> retrieveCompleteBuildingList() {
         ArrayList<String> values = null;
 
         values = new ArrayList();
         sql = "SELECT BuildingTypeOfIndustry FROM Building";
 
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             while (rs.next()) {
                 values.add(rs.getString("BuildingTypeOfIndustry"));
             }
         } catch (Exception e) {
             System.out.println("Unable to execute function retrieveBuildingList()");
             System.out.println(e.getMessage());
         }
 
         return values;
     }
 
     /* This function retrieves a list of all the buildings (buildingID, and 
      * buildingName) of a certain industry that can only be built in the 
      * provided duchy.
      */
     public ArrayList<String[]> listBuildingBy(String duchy, String industry) {
         ArrayList<String[]> values = null;
         String[] line;
 
         duchy = duchy.toLowerCase();
         industry = industry.toLowerCase();
         boolean correctDuchy = false, correctIndustry = false;
 
         //Check parameter validity
         for (int a = 0; a < duchyList.size(); a++) {
             if (duchyList.get(a).toLowerCase().equals(duchy)) {
                 correctDuchy = true;
             }
         }
 
         for (int b = 0; b < industryList.size(); b++) {
             if (industryList.get(b).toLowerCase().equals(industry)) {
                 correctIndustry = true;
             }
         }
 
         if (correctDuchy != true || correctIndustry != true) {
             System.out.println("Incorrect parameters supplied to function listBuildingBy()");
         } else {
             try {
                 sql = "SELECT * FROM BuildingType";
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
 
                 values = new ArrayList();
 
                 while (rs.next()) {
                     line = new String[2];
                     line[0] = rs.getString("BuildingTypeID");
                     line[1] = rs.getString("BuildingTypeDescription");
                     values.add(line);
                 }
 
                 int remember = -1;
                 for (int a = 0; a < values.size(); a++) {
                     for (int b = 0; b < values.get(a).length; b++) {
                         if (values.get(a)[1].toLowerCase().equals(industry)) {
                             remember = Integer.parseInt(values.get(a)[0]);
                         }
                     }
                 }
 
                 //Ensure first letter of duchy is uppercase
                 duchy = capitalizeFirst(duchy);
 
                 sql = "SELECT BuildingAvailabilityID FROM BuildingAvailability WHERE "
                         + "BuildingAvailability" + duchy + " = 1";
                 ArrayList<String> regions = new ArrayList();
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
 
                 while (rs.next()) {
                     regions.add(rs.getString("BuildingAvailabilityID"));
                 }
 
                 sql = "SELECT BuildingID, BuildingTypeOfIndustry FROM Building WHERE BuildingTypeID"
                         + " = " + remember + " AND (BuildingAvailabilityID = " + regions.get(0);
 
                 if (regions.size() > 1) {
                     for (int a = 1; a < regions.size(); a++) {
                         sql += " OR BuildingAvailabilityID = " + regions.get(a);
                     }
                 }
 
                 sql += ")";
 
 
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
 
                 values = new ArrayList();
 
                 while (rs.next()) {
                     line = new String[2];
                     line[0] = rs.getString("BuildingID");
                     line[1] = rs.getString("BuildingTypeOfIndustry");
                     values.add(line);
                 }
             } catch (Exception e) {
                 System.out.println("Unable to execute function listBuildingBy()");
                 System.out.println(e.getMessage());
             }
 
             return values;
         }
 
         return null;
     }
 
     /* This function retrieves all the details of a specific building, which
      * depends on the buildingID provided.
      */
     public ArrayList<String[]> retrieveBuildingDetailsById(int id) {
         ArrayList<String[]> values;
         String[] line;
         String answer = "";
         int count;
 
         //Test if id exists
 
         //Retrieve details
         try {
             sql = "SELECT * FROM Building WHERE BuildingID = " + id;
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
 
             values = new ArrayList();
 
             while (rs.next()) {
                 line = new String[12];
                 line[0] = rs.getString("BuildingTypeID");
                 line[1] = rs.getString("BuildingTypeOfIndustry");
                 line[2] = rs.getString("BuildingAvailabilityID");
                 line[3] = rs.getString("BuildingPrerequisiteID");
                 line[4] = rs.getString("BuildingCost");
                 line[5] = rs.getString("BuildingSetupCost");
                 line[6] = rs.getString("BuildingMonthlyIncome");
                 line[7] = rs.getString("BuildingWorkersNeeded");
                 line[8] = rs.getString("BuildingTimeToBuild");
                 line[9] = rs.getString("BuildingSizeRequired");
                 line[10] = rs.getString("BuildingHappiness");
                 line[11] = rs.getString("BuildingDefenseValue");
                 values.add(line);
             }
 
             for (int a = 0; a < values.size(); a++) {
                 sql = "SELECT BuildingTypeDescription FROM BuildingType "
                         + "WHERE BuildingTypeID = " + values.get(a)[0];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[0] = rs.getString("BuildingTypeDescription");
 
                 sql = "SELECT * FROM BuildingAvailability WHERE "
                         + "BuildingAvailabilityID = " + values.get(a)[2];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 count = 0;
                 answer = "";
 
                 if (rs.getString("BuildingAvailabilityThegnheim").equals("1")) {
                     answer = "Thegnheim";
                     ++count;
                 }
                 if (rs.getString("BuildingAvailabilitySarkland").equals("1")) {
                     if (count > 0) {
                         answer += ",Sarkland";
                     } else {
                         answer += "Sarkland";
                     }
                 }
                 if (rs.getString("BuildingAvailabilityRagonvaldr").equals("1")) {
                     if (count > 0) {
                         answer += ",Ragonvaldr";
                     } else {
                         answer += "Ragonvaldr";
                     }
                 }
                 if (rs.getString("BuildingAvailabilitySvaerstein").equals("1")) {
                     if (count > 0) {
                         answer += ",Svaerstein";
                     } else {
                         answer += "Svaerstein";
                     }
                 }
                 if (rs.getString("BuildingAvailabilityRotheim").equals("1")) {
                     if (count > 0) {
                         answer += ",Rotheim";
                     } else {
                         answer += "Rotheim";
                     }
                 }
                 if (rs.getString("BuildingAvailabilityLangzerund").equals("1")) {
                     if (count > 0) {
                         answer += ",Langzerund";
                     } else {
                         answer += "Langzerund";
                     }
                 }
 
                 values.get(a)[2] = answer;
 
                 sql = "SELECT BuildingPrerequisiteDescription FROM BuildingPrerequisite "
                         + "WHERE BuildingPrerequisiteID = " + values.get(a)[3];
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql);
                 rs.next();
                 values.get(a)[3] = rs.getString("BuildingPrerequisiteDescription");
             }
 
             return values;
         } catch (Exception e) {
             System.out.println("Unable to execute function retrieveBuildingDetailsById()");
             System.out.println(e.getMessage());
         }
 
         return null;
     }
 
     public ArrayList<String[]> retrieveAllBuildingsOwnedByCharacter(int charid, int plotid) {
         ArrayList<String[]> results = new ArrayList();
         String[] line;
 
         sql = "SELECT BuildLogBuildingID, BuildLogTimeToComplete, "
                 + "BuildLogCompleted FROM BuildLog WHERE "
                 + "BuildLogCharacterID = " + charid + " AND "
                 + "BuildLogPlotID = " + plotid;
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
         } catch (Exception e) {
             System.out.println("Error in function retrieveAllBuildingsOwnedByCharacter(): "
                     + "Could not retrieve Building ID's from BuildLog table.");
             System.out.println(e.getMessage());
         }
 
         try {
 
             while (rs.next()) {
                 line = new String[3];
                 line[0]=rs.getString("BuildLogBuildingID");
                 line[1]=rs.getString("BuildLogTimeToComplete");
                 line[2]=rs.getString("BuildLogCompleted");
                 results.add(line);
             }
         } catch (Exception e) {
             System.out.println("Error in function retrieveAllBuildingsOwnedByCharacter(): "
                     + "Could not loop through BuildLog results.");
         }
 
         return results;
     }
 
     /* Will return an empty string ("") if the building may be placed on the
      * provided plot.
      * Otherwise, will return text in string describing the required pre-
      * requisite.
      * 
      * Note: have to hard code for worker limit.
      * Note: when removing buildings, prerequisites have to be rechecked.
      */
     public String checkBuildingPrerequisites(int plotID, int buildingID) {
         ArrayList<String> plot = null;
         int prereq = 0;
         int workerCount = 0, buildingCount = 0, start = 0;
 
         //Get the plot to be checked against
         sql = "SELECT PlotDuchy, PlotGroundArray, PlotBuildingArray, "
                 + "PlotWorkersUsed, PlotWorkerMax, PlotAcreExquisiteMax, "
                 + "PlotAcreFineMax, PlotAcrePoorMax "
                 + "FROM Plot "
                 + "WHERE PlotID = " + plotID;
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
 
             if (rs != null) {
                 //Store retrieved plot
                 plot = new ArrayList();
                 plot.add(rs.getString("PlotDuchy"));           //0
                 plot.add(rs.getString("PlotGroundArray"));      //1
                 plot.add(rs.getString("PlotBuildingArray"));    //2
                 plot.add(rs.getString("PlotWorkersUsed"));      //3
                 plot.add(rs.getString("PlotWorkerMax"));        //4
                 plot.add(rs.getString("PlotAcreExquisiteMax")); //5
                 plot.add(rs.getString("PlotAcreFineMax"));      //6
                 plot.add(rs.getString("PlotAcrePoorMax"));      //7
             } else {
                 return "plot does not exist";
             }
         } catch (Exception e) {
             System.out.println("Error in function checkBuildingPrerequisites():");
             System.out.println("Error retrieving plot.");
             System.out.println(e.getMessage());
         }
 
         //Get prerequisite
         sql = "SELECT BuildingPrerequisiteID FROM Building "
                 + "WHERE BuildingID = " + buildingID;
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             rs.next();
 
             prereq = Integer.parseInt(rs.getString("BuildingPrerequisiteID"));
         } catch (Exception e) {
             System.out.println("Error in function checkBuildingPrerequisites():");
             System.out.println("Error retrieving building prerequisite.");
             System.out.println(e.getMessage());
         }
 
         //Check all 'per worker' requirements'
         start = 0;
 
         //Check all 1 per 30 worker buildings
         if (buildingID == 52) {
             buildingCount = countOccurrences(plot.get(2), Integer.toString(buildingID));
             workerCount = Integer.parseInt(plot.get(3));
 
             if (buildingCount > 0 && workerCount > 0) {
                 if (((workerCount / 30) - 1) < buildingCount) {
                     return "worker limit";
                 }
             }
         } else if (buildingID == 69) {
             buildingCount = countOccurrences(plot.get(2), Integer.toString(buildingID));
             workerCount = Integer.parseInt(plot.get(3));
 
             if (buildingCount > 0 && workerCount > 0) {
                 if (((workerCount / 50) - 1) < buildingCount) {
                     return "worker limit";
                 }
             }
         } else if (buildingID == 45 || buildingID == 50 || buildingID == 54
                 || buildingID == 68) {
             buildingCount = countOccurrences(plot.get(2), Integer.toString(buildingID));
             workerCount = Integer.parseInt(plot.get(3));
 
             if (buildingCount > 0 && workerCount > 0) {
                 if (((workerCount / 100) - 1) < buildingCount) {
                     return "worker limit";
                 }
             }
         } else if (buildingID == 65 || buildingID == 66) {
             buildingCount = countOccurrences(plot.get(2), Integer.toString(buildingID));
             workerCount = Integer.parseInt(plot.get(3));
 
             if (buildingCount > 0 && workerCount > 0) {
                 if (((workerCount / 200) - 1) < buildingCount) {
                     return "worker limit";
                 }
             }
         } else if (buildingID == 53) {
             buildingCount = countOccurrences(plot.get(2), Integer.toString(buildingID));
             workerCount = Integer.parseInt(plot.get(3));
 
             if (buildingCount > 0 && workerCount > 0) {
                 if (((workerCount / 300) - 1) < buildingCount) {
                     return "worker limit";
                 }
             }
         } else if (buildingID == 55 || buildingID == 56 || buildingID == 57
                 || buildingID == 58 || buildingID == 59 || buildingID == 64
                 || buildingID == 67) {
             buildingCount = countOccurrences(plot.get(2), Integer.toString(buildingID));
             workerCount = Integer.parseInt(plot.get(3));
 
             if (buildingCount > 0 && workerCount > 0) {
                 if (((workerCount / 500) - 1) < buildingCount) {
                     return "worker limit";
                 }
             }
         }
 
         //Check all prerequisites
         switch (prereq) {
             case 1: //No prerequisites
                 break;
             case 2: //At least 1 poor
                 if (Integer.parseInt(plot.get(7)) > 0) {
                     break;
                 } else {
                     return "poor acre";
                 }
             case 3: //At least 1 fine
                 if (Integer.parseInt(plot.get(6)) > 0) {
                     break;
                 } else {
                     return "fine acre";
                 }
             case 4: //At least 1 exquisite
                 if (Integer.parseInt(plot.get(5)) > 0) {
                     break;
                 } else {
                     return "exquisite acre";
                 }
             case 5: //No exquisite
                 if (Integer.parseInt(plot.get(5)) > 0) {
                     return "no exquisite";
                 } else {
                     break;
                 }
             case 6: //Water
                 if (plot.get(1).contains("3")) {
                     break;
                 } else {
                     return "water";
                 }
             case 7: //Near water. This check must be done elsewhere as well - 
                 //when they building is placed. i.e. Shipyard.
                 //return values in both cases below; but distinguish between them
                 //by checking if there is suitable water.
                 if (plot.get(1).contains("3")) {
                     return "good-near water";
                 } else {
                     return "bad-near water";
                 }
             case 8:
                 return "wild land";
             //break;
             case 9:
                if (plot.get(0).equals("Sarkland") || plot.get(0).equals("Ragonvaldr")) {
                     break;
                 } else {
                     return "not on bay of maresco";
                 }
             //break;
             case 10: //Check for suitable source
                 break;
             case 11: //Iron mine on plot
                 if (plot.get(2).contains("13")) {
                     break;
                 } else {
                     return "iron mine";
                 }
             case 12: //Check for vineyard
                 if (plot.get(2).contains("4")) {
                     break;
                 } else {
                     return "vineyard";
                 }
             case 13: //Check for primary source
                 break;
             case 14: //Check for timber plantation. No natural image/block for this yet
                 break;
             case 15: //Check for flour mill
                 if (plot.get(2).contains("44")) {
                     break;
                 } else {
                     return "flour mill";
                 }
             case 16: //Livestock or hunters (check for building no. 6?)
                 if (plot.get(2).contains("1") || plot.get(2).contains("2")
                         || plot.get(2).contains("8")) {
                     break;
                 } else {
                     return "livestock or hunters";
                 }
             case 17: //Barracks
                 if (plot.get(2).contains("60")) {
                     break;
                 } else {
                     return "barracks";
                 }
             case 18: //Inside walls. Will check for 1 wall~
                 if (plot.get(2).contains("73") || plot.get(2).contains("74")
                         || plot.get(2).contains("75") || plot.get(2).contains("76")) {
                     break;
                 } else {
                     return "walls";
                 }
         }
 
         return "";  //All prerequisites met.
     }
 
     /* Helper function for checkBuildingPrerequisites();
      * Counts the number of times a certain substring occurs in a string.
      */
     public int countOccurrences(String whole, String part) {
         int count = 0, remem = 0;
 
         while (remem != -1) {
             remem = whole.indexOf(part, remem);
             if (remem != -1) {
                 ++count;
                 remem += part.length();
             }
         }
 
         return count;
     }
 
     /* This function returns the time a building takes to build in weeks. The
      * result is returned as an integer.
      * This function returns -1 if an error occurred.
      */
     public String getBuildingTTB(int buildingID) {
         sql = "SELECT BuildingTimeToBuild FROM Building WHERE "
                 + "BuildingID = " + buildingID;
         try {
             stmt = con.createStatement();
             rs = stmt.executeQuery(sql);
             if (rs.next()) {
                 return rs.getString("BuildingTimeToBuild");
             } else {
                 System.out.println("Error in function getBuildingTTB():");
                 System.out.println("No results returned.");
             }
         } catch (Exception e) {
             System.out.println("Error in function getBuildingTTB():");
             System.out.println(e.getMessage());
         }
 
         return "-1";
     }
 }
