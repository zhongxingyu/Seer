 import java.util.*;
 import java.sql.*;
 
public class TestInMemoryTextHuge
 {
 
     static void executeUpdate(Statement stmt, String sql) throws SQLException
     {
       System.out.println("  " + sql);
       long startTime = System.currentTimeMillis();
       int res = stmt.executeUpdate(sql);
       System.out.println("    -> Elapsed time: "+ (System.currentTimeMillis() - startTime) + " msecs: " + res);
     }
 
     static ResultSet executeQuery(Statement stmt, String sql) throws SQLException
     {
       System.out.println("  " + sql);
       long startTime = System.currentTimeMillis();
       ResultSet rs = stmt.executeQuery(sql);
       System.out.println("    -> Elapsed time: "+ (System.currentTimeMillis() - startTime) + " msecs");
       return rs;
     }
 
     public static void main(String argv[]) {
 
         try {
             // Register the textFileDriver.
             //
             Class.forName("ORG.as220.tinySQL.textFileDriver");
         } catch (ClassNotFoundException e) {
             System.err.println(
                 "I could not find the tinySQL classes. Did you install\n" +
                 "them as directed in the README file?");
             e.printStackTrace();
         }
 
         try { // watch out for SQLExceptions!
 
             // Make a connection to the tinySQL Driver.
             //
             Properties p = new Properties ();
             p.setProperty ("user" , "me");
             p.setProperty ("encoding", "Cp850");
             p.setProperty("inmemory", "true");
 
             DriverManager.setLogStream (System.out);      
             Connection con = DriverManager.getConnection("jdbc:tinySQL:./test", p);
             System.out.println (p);
             
             // get a Statement object from the Connection
             //
             Statement stmt = con.createStatement();
 
             executeUpdate(stmt, "DROP TABLE IF EXISTS cars");
             executeUpdate(stmt, "DROP TABLE IF EXISTS people");
             executeUpdate(stmt, "DROP TABLE IF EXISTS party");
 
             System.out.println("");
             System.out.println("CREATE TABLE party ...");
             System.out.println("=====================");
             executeUpdate(stmt, "CREATE TABLE party (birthday DATE, age INT)");
 
 
             System.out.println("");
             System.out.println("CREATE TABLE cars ...");
             System.out.println("=====================");
             executeUpdate(stmt, "CREATE TABLE cars (name CHAR(25), id NUMERIC(4,0))");
 
             System.out.println("");
             System.out.println("CREATE TABLE people ...");
             System.out.println("=======================");
             executeUpdate(stmt, 
                 "CREATE TABLE people (pe_name CHAR(25), pe_id NUMERIC(8,0), car_id NUMERIC(4,0))");
             System.err.println("Created the tables.");
 
             System.out.println("");
             System.out.println("INSERT INTO party ...");
             System.out.println("====================");
             executeUpdate(stmt, "INSERT INTO party (birthday, age) VALUES('2000-12-01', 1)");
 
             System.out.println("");
             System.out.println("INSERT INTO cars ...");
             System.out.println("====================");
             executeUpdate(stmt, "INSERT INTO cars (name, id) VALUES('Fiat', 1)");
             executeUpdate(stmt, "insert into cars (name, id) values('Pinto', 2)");
             executeUpdate(stmt, "INSerT inTO cars (name, id) VALueS('Thing', 3)");
             executeUpdate(stmt, "INSERT INTO cars (name, id) VALUES('Bug', 4)");
             executeUpdate(stmt, "INSERT INTO cars (name, id) VALUES('Newport', 5)");
             executeUpdate(stmt, "INSERT INTO cars (name, id) VALUES('Rangerover', 6)");
             executeUpdate(stmt, "INSERT INTO cars (name, id) VALUES('Jeep', 7)");
             executeUpdate(stmt, "INSERT INTO cars (name, id) VALUES('Hummer', 8)");
           executeUpdate(stmt, "INSERT INTO cars (name, id) VALUES('Lexus', 9)");
           for (int i = 10; i < 20000; ++i) {
                        executeUpdate(stmt, "INSERT INTO cars (name, id) VALUES('Lexus"+ i + "', " + i + ")");
                      }
 
 
           for (int i = 10; i < 100; ++i) {
                  ResultSet rs = executeQuery(stmt, "SELECT * FROM cars WHERE id = " + i);
                  System.out.println ("No Of Recs: " + QueryDbf.displayResults(rs));
                }
 
             System.out.println("");
             System.out.println("INSERT INTO people ...");
             System.out.println("======================");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Irwin Garden', 1, 2)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Mr. Fiction', 2, 7)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Sal Paradise', 3, 8)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Dean Moriarty', 4, 3)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Bull Lee', 5, 7)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Jack Chip', 6, 1)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Glen Runciter', 7, 4)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Horselover Fat', 8, 2)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Gnossos Pappadopoulos', 9, 7)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Hef', 10, 6)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Matty Groves', 11, 7)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Raoul Frodus', 12, 5)");
             executeUpdate(stmt, "INSERT INTO people (pe_name, pe_id, car_id) VALUES('Hogar Boo', 12, 5)");
             System.out.println("");
             System.out.println("UPDATE Mr. Garden ...");
             System.out.println("=====================");
             executeUpdate(stmt, "UPDATE people SET car_id=6 WHERE pe_name=\'Irwin Garden\'");
 
             // Execute a query and get the result set.
             //
             System.out.println("");
             System.out.println("SELECT cars ...");
             System.out.println("=======================");
             ResultSet rs = executeQuery(stmt, "SELECT * FROM cars");
             QueryDbf.displayResults(rs);
             rs.close ();
 //
 //            // Execute a query and get the result set.
 //            //
 //            System.out.println("");
 //            System.out.println("SELECT party ...");
 //            System.out.println("=======================");
 //            rs = executeQuery(stmt, "SELECT * FROM party");
 //            System.out.println ("No Of Recs: " + QueryDbf.displayResults(rs));
 //
 //            System.out.println("");
 //            System.out.println("ALTER TABLE ...");
 //            System.out.println("===============");
 //            executeUpdate(stmt, "ALTER TABLE people ADD (nation CHAR(25), age NUMERIC(4,0))");
 //            rs = executeQuery(stmt, "SELECT * FROM people");
 //            QueryDbf.displayResults(rs);
 //            rs.close ();
 //
 //            System.out.println("");
 //            executeUpdate(stmt, "ALTER TABLE people DROP COLUMN age");
 //            rs = executeQuery(stmt, "SELECT * FROM people");
 //            QueryDbf.displayResults(rs);
 //            rs.close ();
 //
 //            System.out.println("");
 //            executeUpdate(stmt, "ALTER TABLE people DROP nation");
 //            rs = executeQuery(stmt, "SELECT * FROM people");
 //            QueryDbf.displayResults(rs);
 //            rs.close ();
 //
 //
 //            System.out.println("");
 //            System.out.println("Checking Meta data ...");
 //            System.out.println("======================");
 //            DatabaseMetaData dmd = con.getMetaData();
 //
 //            System.out.println("");
 //            System.out.println("*** Type Info: [DATA_TYPE is from java.sql.Types]");
 //            rs = dmd.getTypeInfo();
 //            QueryDbf.displayResults(rs);
 //            rs.close();
 //
 //            System.out.println("");
 //            System.out.println("*** Table Info:");
 //            String tableName = "%";
 //            // all found *.DBF files in the directory given as connection URL ...
 //            rs = dmd.getTables(null, null, tableName, null);
 //            QueryDbf.displayResults(rs);
 //            rs.close();
 
             stmt.close();
             con.close();
 
             System.out.println("\nGood bye");
 
         } catch( Exception e ) {
             System.out.println(e.getMessage());
             e.printStackTrace();
         }
     }
 
 }
 
