 package CSV.Parser;
 
 import CSV.main.Database;
 import java.io.File;
 import java.util.Scanner;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  * takes a file, and builds a database.
  * @author kdsweenx
  */
 public class Parser {
     /**
      * Returns a database which holds the info in the CSV passed in.
      * @param CSVFile The file to be parsed
      * @param separatorType the separator type found in the Database class.
      * @return The properly built database. Returns Null if an exception is found.
      */
     public Database makeDatabase(File CSVFile, int separatorType){
         String sep=getSeperator(separatorType);
         Database database= new Database();
         try{
             Scanner reader = new Scanner(CSVFile);
             int c=0, r=0;
             while(reader.hasNext()){
                 Scanner stringRead=new Scanner(reader.nextLine());
                 stringRead.useDelimiter(sep);
                 while(stringRead.hasNext()){
                     String s=stringRead.next();
                     database.put(s, r, c);
                     c++;
                 }
                 c=0;
                 r++;
             }
             return database;
         }catch(Exception e){
             e.printStackTrace();
             return null;
         }
     }
     
     private static String getSeperator(int type){
         switch(type){
             case Database.COMMA:
                 return ",";
            case Database.SEMICOLON:
                 return ";";
             case Database.SPACE:
                 return " ";
             case Database.TAB:
                 return "\t";
             default:
                 return ",";
         }
     }
 }
