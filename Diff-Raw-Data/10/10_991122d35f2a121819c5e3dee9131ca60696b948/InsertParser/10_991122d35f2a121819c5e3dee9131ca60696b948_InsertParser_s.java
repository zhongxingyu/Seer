 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test.parser;
 
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Scanner;
 import test.tablesTest;
 import users.Conn;
 
 /**
  * This class will parse a .sql file, separating each query
  * and executing it.
  * @author Mitch
  */
 public class InsertParser {
   
   private String filename;
   private final String DEFAULT_FILE = "insert_test_values.sql";
   
   public InsertParser()
   {
     filename = DEFAULT_FILE;
   }
   
   public InsertParser(String filename)
   {
     this.filename = filename;
   }
   
   public String getFilename()
   {
     return filename;
   }
   
   public void setFilename(String filename)
   {
     this.filename = filename;
   }
   
   /**
    * Opens a file and parses it,
    * then executes all sql statements
    */
   public void parse()
   {
     String allQueriesInOneString = read();
     String[] allQueriesInOneArray = allQueriesInOneString.split(";");
     execute(allQueriesInOneArray);
   }
   
   private String read()
   {
     FileReader fr = null;
     try
     {
       fr = new FileReader(filename);
     }
     catch (FileNotFoundException e)
     {
       System.out.println("File not found."
               + " Using default file '"+DEFAULT_FILE+"'");
       try
       {
         fr = new FileReader(DEFAULT_FILE);
       }
       catch (FileNotFoundException fnf)
       {
         System.out.println("Default file not found. "
                 + "Exiting.");
         System.exit(-1);
       }
     } // end init FileREader
     
     Scanner fin = new Scanner(fr);
     
     String queries="";
     while (fin.hasNext())
     {
       queries += fin.nextLine();
       queries += ' ';
     }
     return queries;
   }
   
   /**
    * Executes every statement in an array.
    */
   private void execute(String[] queries)
   {
     Connection c = Conn.getInstance().getConnection();
     try
     {
       c.setAutoCommit(false);
       Statement s = c.createStatement();
       // length - 1 to ignore the last line, commit;
       int n = queries.length - 1;
       for (int i = 0; i < n; i++)
       {
         try
         {
           //System.out.println(queries[i]);
           s.execute(queries[i]);
         }
         catch (SQLException e)
         {
           if ((e.getMessage()).contains("ORA-00001"))
           {
             // do nothing
           }
           else
           {
             throw new SQLException(e);
           }
         }
       }
       c.setAutoCommit(true);
       c.commit();
     }
     catch (SQLException e)
     {
       System.out.println("Failed. Rolling back.");
       try
       {
         //c.commit();
         c.rollback();
         c.setAutoCommit(true);
         (new tablesTest()).setup(); // reset tables
         System.out.println(e.getMessage());
         e.printStackTrace();
       }
       catch(SQLException ex)
       {
         System.out.println(ex.getMessage());
         ex.printStackTrace();
       }
       
     }
     
   }
   
   /**
    * Inserts the contents of the file
    * @param args 
    */
   public static void main(String[] args) throws Exception{
     (new InsertParser()).parse();
     System.out.println("End of program");
   }
 }
