 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package project.carPart.query_db;
 
 import java.sql.*;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 /**
  *
  * @author Jerry Phul
  */
 public class DBQuery {
     public static int RLINKCOL = 4;
     
     //private static ResultSet DBQuery() {
     private static ResultSet DBQuery(String query) {
         DBConnection dbc = new DBConnection();
         dbc.createQuery();
         dbc.setQuery(query);
         dbc.createConnection();
         return dbc.queryDb();
 //        dbc.disconnectFromDB();
     }
 
     private static String getMakeQuery() {
         String query = "select distinct mshort from cmakers order by mshort ASC";
         System.out.println(query);
         return query;
     }
 
     public static ResultSet getMakeMenuItems() {
         return DBQuery(getMakeQuery());
     }
     
     public static ResultSet getVendorMenuItems(){
         return DBQuery(getVendorQuery());
     }
     
     private static String getVendorQuery(){
         String query = "select distinct NAME from PARTMAKERS";
         System.out.println(query);
         return query;
     }
 
     private static String getModelQuery(String carModel) {
         String query = "select Distinct model from apl" + carModel;
         System.out.println(query);
         return query;
     }
 
     public static ResultSet getModelMenuItems(String carMake) {
         return DBQuery(getModelQuery(carMake));
     }
 
     private static String getYearQuery(String carMake, String carModel) {
         String query = "select distinct year from APL" + carMake + " where MODEL='" + carModel + "'";
         System.out.println(query);
         return query;
     }
 
     public static ResultSet getYears(String carMake, String carModel) {
         return DBQuery(getYearQuery(carMake, carModel));
     }
 
     private static String getEngDescQuery(String carMake, String carModel, String carYear) {
         //String query = "select description, litres as ltr, engine_type as ENG, Cubic_inches as CID, RLINK from aplche where model='BEL AIR' and year=74";
        String query = "select description, litres as ltr, engine_type as ENG, Cubic_inches as CID, RLINK from APL" + carMake + " where model='" + carModel + "' and year='" + carYear + "'";
         System.out.println(query);
         return query;
     }
     
     //TODO: Need to add parameters to method and then update Table-carEngineDesc file with same
     public static ResultSet getTableEngineDesc(String carMake, String carModel, String carYear) {
         return DBQuery(getEngDescQuery(carMake, carModel, carYear));
     }
     
     private static String getPartQuery(int partNumber) {
         String query = "select * from rdimmod where P_Number in (select MOD4 from radcrx where rlink=" + partNumber + ")";
         System.out.println(query);
         return query;
     }
     
     public static ResultSet getTablePartDesc(int partNumber) {
         return DBQuery(getPartQuery(partNumber));
     }
     
     public static TableModel getPartRows(String vendor, String partNumber)
     {
         String query = "select * from RDIM" +  vendor + " where p_number=" + partNumber;
         System.out.println(query);
         ResultSet rs = DBQuery(query);
         return DbUtils.resultSetToTableModel(rs);
     }
 
     public static ResultSet getVendorPartNumbers(String vendorName) 
     {
         return DBQuery(getPartNumberListQuery(vendorName));
     }
 
     private static String getPartNumberListQuery(String vendorName) 
     {
         String query = "select unique P_NUMBER from " + vendorName;
         System.out.println(query);
         return query;
     }
 
     public static ResultSet getVendors(String rlink) 
     {
         return DBQuery(getVendorsQuery(rlink));
     }
 
     private static String getVendorsQuery(String rlink) 
     {
         String query = "select * from radcrx where rlink=" + rlink;
         System.out.println(query);
         return query;
     }
     
     public static ResultSet getPartDesc(String vendor, String part) {
         return DBQuery(getPartDescQuery(vendor, part));
     }
 
     private static String getPartDescQuery(String vendor, String part) {
         String query = "select * from RDIM" + vendor + " where p_number=" + part;
         System.out.println(query);
         return query;
     }
     // ........................ M A I N M E T H O D ............................//
     /**
     * @param args
     */
 //    public static void main(String[] args) {
         
 //    } // main method
 }
