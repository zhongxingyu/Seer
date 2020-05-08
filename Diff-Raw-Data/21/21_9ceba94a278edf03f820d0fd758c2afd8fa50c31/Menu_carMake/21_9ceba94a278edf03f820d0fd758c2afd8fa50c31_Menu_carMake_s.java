 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package project.carPart.code;
 
 import java.sql.*;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ArrayList;
 import project.carPart.query_db.DBQuery;
 import project.carPart.gui.*;
 
 /**
  *
  * @author Jerry Phul
  */
 public class Menu_carMake {
     // column groups for vendors
 
     private final static int START_ARS = 2;
     private final static int END_ARS = 5;
     private final static int START_MOD = 6;
     private final static int END_MOD = 9;
     private final static int START_BEH = 10;
     private final static int END_BEH = 13;
     private final static int START_DAN = 14;
     private final static int END_DAN = 17;
 
     public static Object[] menu_carModel(String carModel) {
         ResultSet rs;
         ArrayList resultString = new ArrayList();
         Object[] resultArray = null;
 
         rs = DBQuery.getModelMenuItems(carModel);
 
         try {
             while (rs.next()) {
                 resultString.add(rs.getString(1));
             }
             resultArray = resultString.toArray();
 
         } catch (Exception e) {
             System.err.print("ERROR: " + e.toString());
         }
 
         return resultArray;
     }
 
     public static Object[] menu_carMake() {
         ResultSet rs;
         ArrayList resultString = new ArrayList();
         Object[] resultArray = null;
 
         rs = DBQuery.getMakeMenuItems();
 
         try {
             while (rs.next()) {
                 resultString.add(rs.getString(1));
             }
             resultArray = resultString.toArray();
         } catch (Exception e) {
             System.err.print("ERROR: " + e.toString());
         }
 
         //Object[] resultArray = resultString.toArray();
 
         return resultArray;
     }
 
     public static Object[] menu_carYears(String carMake, String carModel) {
         ResultSet rs;
         ArrayList resultString = new ArrayList();
 
         rs = DBQuery.getYears(carMake, carModel);
 
         try {
             while (rs.next()) {
                 resultString.add(rs.getString(1));
             }
         } catch (Exception e) {
             System.err.print(e.toString());
         }
 
         Object[] resultArray = resultString.toArray();
 
         return resultArray;
     }
 
     public static Object[] menu_vendor(String rlink) {
         ResultSet rs;
         ArrayList resultString = new ArrayList();
         ResultSetMetaData rsmd = null;
         int i = START_ARS, numCols;
 
         try {
             rs = DBQuery.getVendors(rlink);
             rsmd = rs.getMetaData();
             numCols = rsmd.getColumnCount();

             while (rs.next()) {
                 // list all vendors that have a part for this car
                 while (i <= numCols) {
                     if (i >= START_ARS && i <= END_ARS && rs.getString(i) != null) {
                         resultString.add(rsmd.getColumnName(i).substring(0, 3));
                        i = 6;
                     } else if (i >= START_MOD && i <= END_MOD && rs.getString(i) != null) {
                         resultString.add(rsmd.getColumnName(i).substring(0, 3));
                        i = 10;
                     } else if (i >= START_BEH && i <= END_BEH && rs.getString(i) != null) {
                         resultString.add(rsmd.getColumnName(i).substring(0, 3));
                        i = 14;
                     } else if (i >= START_DAN && i <= END_DAN && rs.getString(i) != null) {
                         resultString.add(rsmd.getColumnName(i).substring(0, 3));
                     } else {
                         i++;
                     }
                 }
             }
         } catch (Exception e) {
             System.err.print(e.toString());
         }
 
         //System.out.println(resultString.toString());
         Object[] resultArray = resultString.toArray();
         return resultArray;
     }
 
     public static Object[] menu_part(String vendor, String rlink) {
         ResultSet rs;
         ArrayList resultString = new ArrayList();
         ResultSetMetaData rsmd = null;
         int i = START_ARS, numCols;
 
         try {
             rs = DBQuery.getVendors(rlink);
             rsmd = rs.getMetaData();
             numCols = rsmd.getColumnCount();
 
             while (rs.next()) {
                 // list all parts for specific vendor
                 while (i <= numCols) {
                     if (vendor.compareTo(rsmd.getColumnName(i).substring(0, 3)) == 0) {
                         if (!resultString.contains(rs.getString(i)))
                             resultString.add(rs.getString(i));
                     }
                     i++;
                 }
             }
         } catch (Exception e) {
             System.err.print(e.toString());
         }
 
         //System.out.println(resultString.toString());
         Object[] resultArray = resultString.toArray();
         return resultArray;
     }
     public static Object[] menu_partDesc(String vendor, String part) {
         ResultSet rs;
         ArrayList resultString = new ArrayList();
         Object[] resultArray = null;
         
         try {
             rs = DBQuery.getPartDesc(vendor, part);
             while (rs.next()) {
                 resultString.add(rs.getString(1));
             }
             resultArray = resultString.toArray();
         } catch (Exception e) {
             System.err.print("ERROR: " + e.toString());
         }
 
         return resultArray;
     }
 }
