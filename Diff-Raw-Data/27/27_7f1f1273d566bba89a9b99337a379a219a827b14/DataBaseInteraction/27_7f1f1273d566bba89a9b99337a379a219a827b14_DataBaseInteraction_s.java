 package additionalFunc;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vasya
  * Date: 09.04.13
  * Time: 9:27
  * To change this template use File | Settings | File Templates.
  */
 public class DataBaseInteraction {
     private final static String DB_URL = "jdbc:firebirdsql://localhost:3050/C:/Users/Vasya/Git/data_base/DB.FDB";
     private final static String DB_DEFAULT_USER = "SYSDBA";
     private final static String DB_DEFAULT_PASSWORD = "masterkey";
 
     public static ResultSet getAllTable(String table) {
         try {
             Class.forName("org.firebirdsql.jdbc.FBDriver");
             Connection connection = DriverManager.getConnection(DB_URL, DB_DEFAULT_USER, DB_DEFAULT_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table);
             ResultSet resultSet = statement.executeQuery();
            statement.close();
            connection.close();
             return resultSet;
         } catch (ClassNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
     }
 
     public static int loadTableRow(String table, ArrayList<String> column, ArrayList<String> values) {
         if (column.size() != values.size()){
             return -1;
         }
 
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("INSERT INTO ");
         stringBuilder.append(table);
         stringBuilder.append(" (");
         stringBuilder.append(column.get(0));
         for (int i=1; i<column.size(); i++){
             stringBuilder.append(", ");
             stringBuilder.append(column.get(i));
         }
         stringBuilder.append(") VALUES ('");
         stringBuilder.append(values.get(0));
         for (int i=1; i<column.size(); i++){
             stringBuilder.append("', '");
            stringBuilder.append(column.get(i));
         }
         stringBuilder.append("')");
 
         try {
 
             Class.forName("org.firebirdsql.jdbc.FBDriver");
             Connection connection = DriverManager.getConnection(DB_URL, DB_DEFAULT_USER, DB_DEFAULT_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(stringBuilder.toString());
             statement.executeUpdate();
            connection.commit();
             statement.close();
             connection.close();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return 1;
     }
 
    public static void main(){
         PlantRecord plantRecord = new PlantRecord();
         plantRecord.setValuesFromStrings(new String[]{"ВВПУ", "81", "З", "15", "1", "7,9"
                                                     , "39", "10,2", "7,5", "0,74", "71,2", "1,5"
                                                     , "0", "7,34", "10,4", null, "Вод-д", null
                                                     , null, null, null, null, null, null});
         System.out.println(plantRecord);
         plantRecord.putRecordInDb();
         System.out.println("send to db");
         ResultSet resultSet = DataBaseInteraction.getAllTable("PLANT");
         try {
             while (resultSet.next()){
                 plantRecord.setValuesFromResultSet(resultSet);
             }
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 }
