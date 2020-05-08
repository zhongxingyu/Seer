 package XML;
 
 import Main.Database;
 import com.ximpleware.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Bind the XML file to an in memory database.
  * @author Tonnie Boersma
  */
 public class XMLBinder {
     
     public static void main(String[] args) throws Exception 
     {
         GenerateContainerDatabase("C:/one/xml7.xml");
         Database.dumpDatabase();
     }
     
     /**
      * Binds the XML file located at the filename path to an inmemory datebase.
      * And checks the content on minor mistakes: weight, position and dates.
      * @param fileName The path to the XML file.
      * @throws Exception If the path isn't directed to an XML file, or if something goes wrong while bindng the file.
      */
     @SuppressWarnings("empty-statement")
     public static void GenerateContainerDatabase(String fileName) throws Exception{
         
         String query = "INSERT INTO container (id, arrivalDateStart, arrivalDateEnd, arrivalTransportType, arrivalCompany, arrivalPositionX, arrivalPositionY, arrivalPositionZ, owner, containerNr, departureDateStart, departureDateEnd, departureTransportType, departureCompany, empty, weight, name, kind, danger, storageLocation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
         Connection con = Database.getConnection();
         PreparedStatement stm = con.prepareStatement(query);
 
         VTDGen vg = new VTDGen();
         
         // <editor-fold defaultstate="collapsed" desc="AutoPilot = new AutoPilot()">
         AutoPilot record = new AutoPilot();
         
         AutoPilot aankomst_datum_d = new AutoPilot();
         AutoPilot aankomst_datum_m = new AutoPilot();
         AutoPilot aankomst_datum_j = new AutoPilot();
         AutoPilot aankomst_tijd_van = new AutoPilot();
         AutoPilot aankomst_tijd_tot = new AutoPilot();
         AutoPilot aankomst_soort_vervoer = new AutoPilot();
         AutoPilot aankomst_bedrijf = new AutoPilot();
         AutoPilot aankomst_positie_x = new AutoPilot();
         AutoPilot aankomst_positie_y = new AutoPilot();
         AutoPilot aankomst_positie_z = new AutoPilot();
         
         AutoPilot eigenaar_naam = new AutoPilot();
         AutoPilot eigenaar_containernr = new AutoPilot();
         
         AutoPilot vertrek_datum_d = new AutoPilot();
         AutoPilot vertrek_datum_m = new AutoPilot();
         AutoPilot vertrek_datum_j = new AutoPilot();
         AutoPilot vertrek_tijd_van = new AutoPilot();
         AutoPilot vertrek_tijd_tot = new AutoPilot();
         AutoPilot vertrek_soort_vervoer = new AutoPilot();
         AutoPilot vertrek_bedrijf = new AutoPilot();
         
         AutoPilot afmetingen_l = new AutoPilot();
         AutoPilot afmetingen_b = new AutoPilot();
         AutoPilot afmetingen_h = new AutoPilot();
         
         AutoPilot gewicht_leeg = new AutoPilot();
         AutoPilot gewicht_inhoud = new AutoPilot();
         
         AutoPilot inhoud_naam = new AutoPilot();
         AutoPilot inhoud_soort = new AutoPilot();
         AutoPilot inhoud_gevaar = new AutoPilot();
         // </editor-fold>
         
         // <editor-fold defaultstate="collapsed" desc=".selectXPath("location");">
         record.selectXPath("/recordset/record");
         
         aankomst_datum_d.selectXPath("aankomst/datum/d"); 
         aankomst_datum_m.selectXPath("aankomst/datum/m"); 
         aankomst_datum_j.selectXPath("aankomst/datum/j");
         aankomst_tijd_van.selectXPath("aankomst/tijd/van");
         aankomst_tijd_tot.selectXPath("aankomst/tijd/tot");
         aankomst_soort_vervoer.selectXPath("aankomst/soort_vervoer");
         aankomst_bedrijf.selectXPath("aankomst/bedrijf");
         aankomst_positie_x.selectXPath("aankomst/positie/x");
         aankomst_positie_y.selectXPath("aankomst/positie/y");
         aankomst_positie_z.selectXPath("aankomst/positie/z");
         
         eigenaar_naam.selectXPath("eigenaar/naam");
         eigenaar_containernr.selectXPath("eigenaar/containernr");
         
         vertrek_datum_d.selectXPath("vertrek/datum/d"); 
         vertrek_datum_m.selectXPath("vertrek/datum/m"); 
         vertrek_datum_j.selectXPath("vertrek/datum/j");
         vertrek_tijd_van.selectXPath("vertrek/tijd/van");
         vertrek_tijd_tot.selectXPath("vertrek/tijd/tot");
         vertrek_soort_vervoer.selectXPath("vertrek/soort_vervoer");
         vertrek_bedrijf.selectXPath("vertrek/bedrijf");
                 
         afmetingen_l.selectXPath("afmetingen/l");
         afmetingen_b.selectXPath("afmetingen/b");
         afmetingen_h.selectXPath("afmetingen/h");
         
         gewicht_leeg.selectXPath("gewicht/leeg");
         gewicht_inhoud.selectXPath("gewicht/inhoud");
         
         inhoud_naam.selectXPath("inhoud/naam");
         inhoud_soort.selectXPath("inhoud/soort");
         inhoud_gevaar.selectXPath("inhoud/gevaar");
         // </editor-fold>
         
         if (vg.parseFile(fileName,false)){
             VTDNav vn = vg.getNav();
             
             // <editor-fold defaultstate="collapsed" desc=".bind(vn);">
             record.bind(vn);
             
             aankomst_datum_d.bind(vn);
             aankomst_datum_m.bind(vn);
             aankomst_datum_j.bind(vn);
             aankomst_tijd_van.bind(vn);
             aankomst_tijd_tot.bind(vn);
             aankomst_soort_vervoer.bind(vn);
             aankomst_bedrijf.bind(vn);
             aankomst_positie_x.bind(vn);
             aankomst_positie_y.bind(vn);
             aankomst_positie_z.bind(vn);
 
             eigenaar_naam.bind(vn);
             eigenaar_containernr.bind(vn);
 
             vertrek_datum_d.bind(vn);
             vertrek_datum_m.bind(vn);
             vertrek_datum_j.bind(vn);
             vertrek_tijd_van.bind(vn);
             vertrek_tijd_tot.bind(vn);
             vertrek_soort_vervoer.bind(vn);
             vertrek_bedrijf.bind(vn);
 
             afmetingen_l.bind(vn);
             afmetingen_b.bind(vn);
             afmetingen_h.bind(vn);
 
             gewicht_leeg.bind(vn);
             gewicht_inhoud.bind(vn);
 
             inhoud_naam.bind(vn);
             inhoud_soort.bind(vn);
             inhoud_gevaar.bind(vn);
             // </editor-fold>
             
             int counter = -1;
             HashSet<String> containerNrs = new HashSet<>();
             while(record.evalXPath()!=-1){
                 counter++;
                 
                 String containerNr = eigenaar_containernr.evalXPathToString();
                 // Prevent adding double containerNr records
                 if(containerNrs.contains(containerNr)) {
                     continue;
                 }
                 containerNrs.add(containerNr);
                 
                 // prefent adding container with a wrong weight
                 if(!CheckWeight(gewicht_inhoud.evalXPathToString())){
                     continue;
                 }
                 
                 // prefent adding containers with a wrong position
                 String x = aankomst_positie_x.evalXPathToString();
                 String y = aankomst_positie_y.evalXPathToString();
                 String z = aankomst_positie_z.evalXPathToString();
                 if(!CheckPosition(x,y,z)){
                     continue;
                 }
                 
                 // prefent adding containers with a wrong dates
                 String arrivalDate = aankomst_datum_j.evalXPathToString() + " " + aankomst_datum_m.evalXPathToString() + " " + aankomst_datum_d.evalXPathToString() + " ";
                 String arrivalStartDate = arrivalDate + aankomst_tijd_van.evalXPathToString().replace('.', ':');
                 String arrivalEndDate = arrivalDate + aankomst_tijd_tot.evalXPathToString().replace('.', ':');
                 
                 String departureDate = vertrek_datum_j.evalXPathToString() + " " + vertrek_datum_m.evalXPathToString() + " " + vertrek_datum_d.evalXPathToString() + " ";
                 String departureStartDate = departureDate + vertrek_tijd_van.evalXPathToString().replace('.', ':');
                 String departureEndDate = departureDate + vertrek_tijd_tot.evalXPathToString().replace('.', ':');
                 if(!CheckDate(arrivalStartDate, arrivalEndDate, departureStartDate, departureEndDate)){
                     continue;
                 }
                 
                 // add values to the update query.
                 String aankomstDatum = AddZero(aankomst_datum_j.evalXPathToString()) + "-" + AddZero(aankomst_datum_m.evalXPathToString()) + "-" + AddZero(aankomst_datum_d.evalXPathToString()) + " ";
                 stm.setString(1, "id" + counter); // id
                 String[] aankomstTijdVan = aankomst_tijd_van.evalXPathToString().split("\\.");
                 stm.setString(2,  aankomstDatum + AddZero(aankomstTijdVan[0]) + ":" +  AddZero(aankomstTijdVan[1])); //arrivalDateStart
                 String[] aankomstTijdTot = aankomst_tijd_tot.evalXPathToString().split("\\.");
                 stm.setString(3,  aankomstDatum + AddZero(aankomstTijdTot[0]) + ":" +  AddZero(aankomstTijdTot[1])); //arrivalDateEnd
                 stm.setString(4, aankomst_soort_vervoer.evalXPathToString()); //arrivalTransportType
                 stm.setString(5, aankomst_bedrijf.evalXPathToString()); //arrivalCompany
                 stm.setString(6, AddZero(aankomst_positie_x.evalXPathToString())); //arrivalPositionX
                 stm.setString(7, AddZero(aankomst_positie_z.evalXPathToString())); //arrivalPositionY
                 stm.setString(8, AddZero(aankomst_positie_y.evalXPathToString())); //arrivalPositionZ
                 stm.setString(9, eigenaar_naam.evalXPathToString()); //owner
                 stm.setInt(10, Integer.parseInt(eigenaar_containernr.evalXPathToString())); //containerNr
                 String vertrekDatum = AddZero(vertrek_datum_j.evalXPathToString()) + "-" + AddZero(vertrek_datum_m.evalXPathToString()) + "-" + AddZero(vertrek_datum_d.evalXPathToString()) + " ";
                 String[] vertrekTijdVan = vertrek_tijd_van.evalXPathToString().split("\\.");
                 stm.setString(11,  vertrekDatum + AddZero(vertrekTijdVan[0]) + ":" +  AddZero(aankomstTijdVan[1])); //departureDateStart
                String[] vertrekTijdTot = vertrek_tijd_van.evalXPathToString().split("\\.");
                 stm.setString(12, vertrekDatum + AddZero(vertrekTijdTot[0]) + ":" +  AddZero(vertrekTijdTot[1])); //departureDateEnd
                 stm.setString(13, vertrek_soort_vervoer.evalXPathToString()); //departureTransportType
                 stm.setString(14, vertrek_bedrijf.evalXPathToString()); //departureCompany
                 stm.setInt(15, Integer.parseInt(gewicht_leeg.evalXPathToString())); //empty
                 stm.setInt(16, Integer.parseInt(gewicht_inhoud.evalXPathToString())); //weight
                 stm.setString(17, inhoud_naam.evalXPathToString()); //name
                 stm.setString(18, inhoud_soort.evalXPathToString()); //kind
                 stm.setString(19, inhoud_gevaar.evalXPathToString()); //danger
                 
                 // try to execute the update query.
                 try {
                     stm.executeUpdate();
                 }
                 catch(Exception e) {
                     System.err.println(e.getMessage());
                 }
             }
             
             record.resetXPath();
             System.out.println("Proccesed: " + (1+counter));
         }
     }
     
     /**
      * Checks if the weight is a vailid value.
      * @param weight The weight
      * @return True if the weight is valid, false otherwise.
      */
     private static boolean CheckWeight(String weight){
         return Integer.parseInt(weight) > 0;
     }
     /**
      * Checks if the given coords are valid
      * @param x The x
      * @param y The y
      * @param z The z
      * @return True if the coords are valid, false otherwise.
      */
     private static boolean CheckPosition(String x, String y, String z){
         return (Integer.parseInt(x) >= 0 && Integer.parseInt(y) >= 0 && Integer.parseInt(z) >= 0);
     }
     /**
      * Converts the 4 input strings to Date objects and checks if they are valid
      * @param startDateBegin The startDateBegin.
      * @param startDateEnd The startDateEnd.
      * @param endDateBegin The endDateBegin.
      * @param endDateEnd The endDateEnd.
      * @return True if the values are valid, false otherwise.
      */
     private static boolean CheckDate(String startDateBegin, String startDateEnd, String endDateBegin, String endDateEnd){
         DateFormat df = new SimpleDateFormat("dd MM yy HH:mm");
         try{
             Date sdb = df.parse(startDateBegin);
             Date sde = df.parse(startDateEnd);
             Date edb = df.parse(endDateBegin);
             Date ede = df.parse(endDateEnd);
             
             if (sdb.after(sde) || edb.after(ede) || sde.after(edb)){
                 return false;
             }
             
         }
         catch(Exception ex){
             return false;
         }
         
         return true;
     }
     /**
      * Adds an zero in front the input string if the input lenght is equel to 1
      * @param input The input.
      * @return 0input or input
      */
     private static String AddZero(String input){
         if (input.length() == 1){
             return "0" + input;
         }
         else{
             return input;
         }
     }
 }
