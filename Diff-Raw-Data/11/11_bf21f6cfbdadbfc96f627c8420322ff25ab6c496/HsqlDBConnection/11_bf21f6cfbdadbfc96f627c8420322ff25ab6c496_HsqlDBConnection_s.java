 package csheets.ext.database.core;
 
 import csheets.core.Cell;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 
 /**
  * Creates a connection to a HSQL database
  * @author João Carreira
  */
 public class HsqlDBConnection implements DBConnectionStrategy
 {
     private String driverPath = "org.hsqldb.jdbcDriver";
     private Connection connection;
     private String url, user, pwd;
 
     @Override
     public void createConnection(String url, String user, String pass) throws ClassNotFoundException, SQLException
     {
         try{
             Class.forName(driverPath);
 //            /* test line -- DELETE AFTERWARDS */
 //            System.out.println(url);
             connection = DriverManager.getConnection(url, user, pass);
 //            /* test line -- DELETE AFTERWARDS */
 //            System.out.println("HSQLAdaptee: connected!");
         }
         catch(ClassNotFoundException e)
         {
 //             /* test line -- DELETE AFTERWARDS */
 //            System.out.println("HSQLAdaptee: class not found");
             
             /* keep this until observer is implemented */
             JOptionPane.showMessageDialog(null, "Error: class not found!");
         }
         catch(SQLException e)
         {
 //            /* test line -- DELETE AFTERWARDS */
 //            System.out.println("HSQLAdaptee: sql error");
             /* keep this until observer is implemented */
             JOptionPane.showMessageDialog(null, "Error: connection to database!");
         }
     }
 
     @Override
     public void createTable(String tableName, Cell[][] cells) 
     {
         /* defining row and column number */
         int numberOfColumns = cells[0].length;
         int numberOfRows = cells.length;
         
         /* beginning the construction of the sql statement */
        String stat = "CREATE TABLE " + tableName + "(id INTEGER IDENTITY, ";
         
         /* the first line of the exported cells is the name of each column */
         String []columnsName = new String[cells[0].length];
         String []columnsNameCopy = new String[cells[0].length];
         
         /* cycle to build the columns name string */
         for(int i = 0; i < cells[0].length; i++)
         {
             /* if it's not the last column */
             if(i != (cells[0].length) - 1)
             {
                 columnsName[i] = cells[0][i].getContent() + " VARCHAR(200), ";
                 columnsNameCopy[i] = cells[0][i].getContent();
             }
             /* if it's the last column must close with bracket */
             else
             {
                 columnsName[i] = cells[0][i].getContent() + " VARCHAR(200))";
                 columnsNameCopy[i] = cells[0][i].getContent();
             }
         }
         
         /* in the end of this cycle we should have the final sql statement 
          for table creation */
         for(int i = 0; i < columnsName.length; i++)
         {
             stat += columnsName[i];
         }
         
         /* sql statement */
         Statement st = null;
         
         /* creates the table based on the sql statement */
         try 
         {
             st = connection.createStatement();
             int i = st.executeUpdate(stat);
             /* keep this until observer is implemented */
             JOptionPane.showMessageDialog(null, "Data succesfully exported!");
         } 
         catch (SQLException ex) 
         {
             Logger.getLogger(HsqlDBConnection.class.getName()).log(Level.SEVERE, null, ex);
             /* keep this until observer is implemented */
             JOptionPane.showMessageDialog(null, "Error: table already exists");
         }
         
         /* now beggins the "insert into" sql statement */
        String insertStat = "insert into " + tableName + "(";
         for(int i = 0; i < columnsNameCopy.length; i++)
         {
             if(i != columnsNameCopy.length - 1)
             {
                 insertStat += columnsNameCopy[i] + ",";
             }
             else
             {
                 insertStat += columnsNameCopy[i] + ")";
             }
         }
         /* continuing concatenation */
         insertStat += " VALUES(";
         
         /* creating a number of insert statements equal to number of rows -1 */
         String []insertVector = new String[numberOfRows - 1];
         for(int i = 0; i < insertVector.length; i++)
         {
            insertVector[i] = insertStat;
         }
         
         /* concatenating the respecting insert statements */
         for(int i = 1; i < numberOfRows; i++)
         {
             for(int j = 0; j < numberOfColumns; j++)
             {
                 if(j != numberOfColumns - 1)
                 {
                     insertVector[i - 1] += "'" + cells[i][j].getContent() + "',"; 
                 }
                 else
                 {
                     insertVector[i - 1] += "'" + cells[i][j].getContent() + "')";
                    //break;
                 }
             }
         }
         
         /* inserting values into the table */
         for(int i = 0; i < insertVector.length; i++)
         {
             Statement insertSt = null;
             try 
             {
                 insertSt = connection.createStatement();
                 int j = st.executeUpdate(insertVector[i].toString());
             }
             catch (SQLException ex) 
             {
                 Logger.getLogger(HsqlDBConnection.class.getName()).log(Level.SEVERE, null, ex);
                 /* keep this until observer is implemented */
                 JOptionPane.showMessageDialog(null, "Error: data not inserted");
             }
         }
         
         /* test msg -- DELETE THIS */
 //        System.out.println("CREATE TABLE: done");
     }
 
     @Override
     public void disconnet() 
     {
         Statement st;
         try 
         {
             st = connection.createStatement();
             st.execute("SHUTDOWN");
             connection.close();    
         } 
         catch (SQLException ex) 
         {
             Logger.getLogger(HsqlDBConnection.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     @Override
     public synchronized ArrayList queryToArray() 
     {
        String sqlStat = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES where TABLE_TYPE='TABLE'"; 
        /* SQL statement */
        Statement stat = null;
        /* SQL result set */
        ResultSet resSet = null;
        /* ArrayList to save results from the query */
        ArrayList array = new ArrayList();
        
        try 
        {
             stat = connection.createStatement();
             resSet = stat.executeQuery(sqlStat);
             stat.close();
             ResultSetMetaData metaData = resSet.getMetaData();
             int cols = metaData.getColumnCount();
             Object obj = null;
             for(; resSet.next(); )
             {
                 for(int i = 0; i < cols; i++)
                 {
                     obj = resSet.getObject(i + 1);
                     array.add(obj);
                 }
             }  
        } 
        catch (SQLException ex) 
        {
             Logger.getLogger(HsqlDBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return array;
     }
     
     @Override
     public String[] getTableList(ArrayList list) 
     {
         int size = list.size();
         String []temp = new String[size];
         for(int i = 0; i < size; i++)
         {
             temp[i] = list.get(i).toString();
         }
         return temp;
     }
 
     @Override
     public void getTableContent()
     {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void updateTable() 
     {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }  
 
     
 
 }
