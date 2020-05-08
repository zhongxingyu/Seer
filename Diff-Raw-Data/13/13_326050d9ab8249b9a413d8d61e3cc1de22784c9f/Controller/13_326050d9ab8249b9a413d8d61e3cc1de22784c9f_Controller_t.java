 package ausleihe.controller;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import ausleihe.db.DataBase;
 import ausleihe.view.View;
 
 public class Controller {
     DataBase database;
     View view;
     
     public Controller(){
         database = new DataBase(this);
         view = new View(this);
     }
     
     public boolean connect(String username, String password){
         return database.connect(username, password);
     }
     
     public void executeQuery(String query){
         try {
             database.executeQuery(query);
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     public String[][] getTable(String name){
         try {
             ResultSet result = database.executeQuery("SELECT * FROM " + name);
             
             int colum_count = result.getMetaData().getColumnCount();
             String[] labels = new String[colum_count];
             for(int i = 1; i <= colum_count; ++i){
                labels[i-1] = result.getMetaData().getColumnLabel(i);
             }
         } catch (SQLException e) {
             System.err.println("error");
            e.printStackTrace();
             return null;
         }
         
         
         String[][] table = new String[10][10];
         
         return table;
     }
 }
