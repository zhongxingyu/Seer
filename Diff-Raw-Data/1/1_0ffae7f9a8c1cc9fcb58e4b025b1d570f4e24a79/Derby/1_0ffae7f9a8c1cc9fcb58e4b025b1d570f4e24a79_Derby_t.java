 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Time;
 
 import java.util.ArrayList;
 
 public class Derby
 {
     /* the default framework is embedded*/
     private String framework = "embedded";
     private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
     private String protocol = "jdbc:derby:";
     
     Connection conn = null;
 	
     /* This ArrayList usage may cause a warning when compiling this class
      * with a compiler for J2SE 5.0 or newer. We are not using generics
      * because we want the source to support J2SE 1.4.2 environments. */
 	// NEVERMIND I KILELD IT MUAHAHAHAHAHAHA
 	
     Statement s = null;
 	PreparedStatement ps = null;
     ResultSet rs = null;
 	
     public Derby(){
         try {
             System.out.println("Derby starting in " + framework + " mode");
             loadDriver();
             String dbName = "derbyDB"; // the name of the database
             conn = DriverManager.getConnection(protocol + dbName + ";create=true");
             System.out.println("Connected to and created database " + dbName);
             conn.setAutoCommit(false);
 
             /* Creating a statement object that we can use for running various
              * SQL statements commands against the database.*/
 			
             createTables();      
             System.out.println("Database Initialization Complete");
         } catch (SQLException sqle){
             printSQLException(sqle);
         }
     }
     
     public void createTables() { //If the tables already exist, this code block will NOT execute
         try{
             System.out.println("Creating Tables");
 			
 			s = conn.createStatement();
             s.execute("CREATE TABLE entry(id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), name varchar(255), description varchar(255), startTime timestamp, CONSTRAINT pk PRIMARY KEY (id))");
             System.out.println("Created table entry");
             s.execute("CREATE TABLE event(id int NOT NULL PRIMARY KEY, repeating int, repeatKey int)");
             System.out.println("Created table event");
             s.execute("CREATE TABLE task(id int NOT NULL PRIMARY KEY, status int, priority int)");
 			s.close();
 			
             System.out.println("Created table task");
         } catch(SQLException sqle){
             //Do nothing
         }
     }
 	
 	public void destroyEverything(){
         try{
 			s = conn.createStatement();
             s.execute("drop table entry");
 			System.out.println("Destroyed table entry");
             s.execute("drop table event");
 			System.out.println("Destroyed table event");
             s.execute("drop table task");
 			System.out.println("Destroyed table task");
 			s.close();
         } catch(SQLException sqle){
             printSQLException(sqle);
         }
     }
 	
 	public void reset()
 	{
 		destroyEverything();
 		createTables();
 	}
 	
 	public void exit()
 	{
         try{
 		  conn.close();
         } catch(SQLException e){
             printSQLException(e);
         }
 	}
 	
 	public int insertEntry(String n, String d, Timestamp st)
 	{
 		try{
 			ps = conn.prepareStatement("INSERT INTO entry (name, description, startTime) VALUES ( ?, ?, ? )", Statement.RETURN_GENERATED_KEYS);
 			
 			ps.setString(1, n);
 			ps.setString(2, d);
 			ps.setTimestamp(3, st);
 			ps.execute();
 			
 			rs = ps.getGeneratedKeys();
 			
 			int key = 0;
 			
 			while(rs.next())
 			{
 				key = rs.getInt(1);
 			}
 			
 			ps.close();
 			
 			return key;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public int insertEvent(String n, String d, Timestamp st, int r, int rk)
 	{
 		try{
 			int key = insertEntry(n, d, st);
 			
 			ps = conn.prepareStatement("INSERT INTO event (id, repeating, repeatKey) VALUES (?, ?, ?)");
 			ps.setInt(1, key);
 			ps.setInt(2, r);
 			ps.setInt(3, rk);
 			ps.execute();
 			
 			ps.close();
 			
 			return key;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public int insertTask(String n, String d, Timestamp st, int s, int p)
 	{
 		try{
 			int key = insertEntry(n, d, st);
 			
 			ps = conn.prepareStatement("INSERT INTO task(id, status, priority) VALUES (?, ?, ?)");
 			ps.setInt(1, key);
 			ps.setInt(2, s);
 			ps.setInt(3, p);
 			ps.execute();
             System.out.println("Insert task successful");
 			
 			ps.close();
 
 			return key;
 		} catch (SQLException balls) {
             printSQLException(balls);
         }
 		
 		return 0;
 	}
 	
 	public int updateEntry(int i, String n, String d, Timestamp ts)
 	{
 		try {
 			ps = conn.prepareStatement("UPDATE entry SET name = ?, description = ?, startTime = ts WHERE id = ?");
 			ps.setString(1, n);
 			ps.setString(2, d);
 			ps.setTimestamp(3, ts);
 			ps.setInt(4, i);
 			
 			int returny = ps.executeUpdate();
 			
 			ps.close();
 			
 			return returny;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public int updateEvent(int i, String n, String d, Timestamp ts, int r, int rk)
 	{
 		try{
 			updateEntry(i, n, d, ts);
 			
 			ps = conn.prepareStatement("UPDATE event SET repeating = ?, repeatKey = ? WHERE id = ?");
 			ps.setInt(1, r);
 			ps.setInt(2, rk);
 			ps.setInt(3, i);
 			
 			int returny = ps.executeUpdate();
 			
 			ps.close();
 			
 			return returny;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public int updateTask(int i, String n, String d, Timestamp ts, int s, int p)
 	{
 		try {
 			updateEntry(i, n, d, ts);
 			
 			ps = conn.prepareStatement("UPDATE event SET status = ?, priority = ? WHERE id = ?");
 			ps.setInt(1, s);
 			ps.setInt(2, p);
 			ps.setInt(3, i);
 			
 			int returny = ps.executeUpdate();
 			
 			ps.close();
 			
 			return returny;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public int deleteEntry(int i)
 	{
 		try{
 			ps = conn.prepareStatement("DELETE FROM entry WHERE id = ?");
 			ps.setInt(1, i);
 			
 			int returny = ps.executeUpdate();
 			
 			ps.close();
 			
 			return returny;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public int deleteEvent(int i )
 	{
 		try{
 			ps = conn.prepareStatement("DELETE FROM event WHERE id = ?");
 			ps.setInt(1, i);
 			
 			int returny = ps.executeUpdate();
 			
 			ps.close();
 			
 			deleteEntry(i);
 			
 			return returny;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public int deleteTask(int i )
 	{
 		try{
 			ps = conn.prepareStatement("DELETE FROM task WHERE id = ?");
 			ps.setInt(1, i);
 			
 			int returny = ps.executeUpdate();
 			
 			ps.close();
 			
 			deleteEntry(i);
 			
 			return returny;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public ResultSet queryEvents(Timestamp start, Timestamp end)
 	{
 		try{
 			ps = conn.prepareStatement("SELECT entry.id, name, description, timeStart, repeating, repeatKey FROM entry JOIN event ON entry.id = event.id ORDER BY timeStart WHERE timeStart BETWEEN ? and ?");
 			ps.setTimestamp(1, start);
 			ps.setTimestamp(2, end);
 			
 			rs = ps.executeQuery();
 			
 			ps.close();
 			
 			return rs;
 		} catch (SQLException balls) {}
 		
 		return null;
 	}
 	
 	public ResultSet queryTasks(Timestamp start, Timestamp end)
 	{
 		try{
 			ps = conn.prepareStatement("SELECT entry.id, name, description, timeStart, status, priority FROM task JOIN task ON entry.id = task.id ORDER BY timeStart WHERE timeStart BETWEEN ? and ?");
 			ps.setTimestamp(1, start);
 			ps.setTimestamp(2, end);
 			
 			rs = ps.executeQuery();
 			
 			ps.close();
 			
 			return rs;
 		} catch (SQLException balls) {}
 		
 		return null;
 	}
 	
 	public int insertRepeatingEvent(String n, String d, Timestamp st, int r)
 	{
 		try{
 			int key = insertEntry(n, d, st);
 			
 			ps = conn.prepareStatement("INSERT INTO event (id, repeating, repeatKey) VALUES (?, ?, ?)");
 			ps.setInt(1, key);
 			ps.setInt(2, r);
 			ps.setInt(3, key);
 			ps.execute();
 			
 			ps.close();
 			
 			return key;
 		} catch (SQLException balls) {}
 		
 		return 0;
 	}
 	
 	public void deleteRepeatingEvent(int i)
 	{
 		try{
 			ps = conn.prepareStatement("SELECT startTime, repeatKey FROM entry JOIN event ON entry.id = event.id WHERE entry.id = ?");
 			ps.setInt(1, i);
 			rs = ps.executeQuery();
 			
 			if(!rs.next())
 			{
				ps.close();
 				return;
 			}
 			
 			Timestamp st = rs.getTimestamp(1);
 			int rk = rs.getInt(2);
 			
 			ps.close();
 			
 			deleteEvent(i);
 			
 			ps = conn.prepareStatement("DELETE FROM event WHERE repeatKey = ? AND startTime >= ?");
 			ps.setInt(1, rk);
 			ps.setTimestamp(2, st);
 			
 			int returny = ps.executeUpdate();
 			
 			ps.close();
 		} catch (SQLException balls) {}
 	}
 	
 	public ResultSet queryPinboardEvents(Timestamp start)
 	{
 		try{
 			ps = conn.prepareStatement("SELECT entry.id, name, description, timeStart, repeating, repeatKey FROM entry JOIN event ON entry.id = event.id ORDER BY timeStart WHERE timeStart >= ?");
 			ps.setTimestamp(1, start);
 			
 			rs = ps.executeQuery();
 			
 			ps.close();
 			
 			return rs;
 		} catch (SQLException balls) {}
 		
 		return null;
 	}
 	
 	public ResultSet queryPinboardTasks(Timestamp start, int status)
 	{
 		try{
 			ps = conn.prepareStatement("SELECT entry.id, name, description, timeStart, status, priority FROM entry JOIN task ON entry.id = task.id ORDER BY timeStart WHERE timeStart >= ? AND status = ?");
 			ps.setTimestamp(1, start);
 			ps.setInt(2, status);
 			
 			rs = ps.executeQuery();
 			
 			ps.close();
 			
 			return rs;
 		} catch (SQLException balls) {}
 		
 		return null;
 	}
 
     /*=====================================*/
     /** All statements are prepared here **/
     /*===================================*/
     // NOPE NOT ANYMORE
 
     /*=======================/
         11 = insert task
         12 = insert event
         21 = update task
         22 = update event
         31 = delete task
         32 = delete event
         41 = query task
         42 = query event
     /*=====================*/
 	// NO METHOD IS SAFE FROM THE KILLER
     
     
     /*=================================================*/
     /** The following are copypasta from Derby sauce **/
     /*===============================================*/
 
     public void closeDatabase() {
         /*
          * In embedded mode, an application should shut down the database.
          * If the application fails to shut down the database,
          * Derby will not perform a checkpoint when the JVM shuts down.
          * This means that it will take longer to boot (connect to) the
          * database the next time, because Derby needs to perform a recovery
          * operation.
          *
          * It is also possible to shut down the Derby system/engine, which
          * automatically shuts down all booted databases.
          *
          * Explicitly shutting down the database or the Derby engine with
          * the connection URL is preferred. This style of shutdown will
          * always throw an SQLException.
          *
          * Not shutting down when in a client environment, see method
          * Javadoc.
          */
        try
         {
             // the shutdown=true attribute shuts down Derby
             DriverManager.getConnection("jdbc:derby:;shutdown=true");
 
             // To shut down a specific database only, but keep the
             // engine running (for example for connecting to other
             // databases), specify a database in the connection URL:
             //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
         }
         catch (SQLException se)
         {
             if (( (se.getErrorCode() == 50000)
                     && ("XJ015".equals(se.getSQLState()) ))) {
                 // we got the expected exception
                 System.out.println("Derby shut down normally");
                 // Note that for single database shutdown, the expected
                 // SQL state is "08006", and the error code is 45000.
             } else {
                 // if the error code or SQLState is different, we have
                 // an unexpected exception (shutdown failed)
                 System.err.println("Derby did not shut down normally");
                 printSQLException(se);
             }
         }
     }
     
     //Loads the embedded driver
     private void loadDriver() {
         /*
          *  The JDBC driver is loaded by loading its class.
          *  If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
          *  be automatically loaded, making this code optional.
          *
          *  In an embedded environment, this will also start up the Derby
          *  engine (though not any databases), since it is not already
          *  running.
          */
         try {
             Class.forName(driver).newInstance();
             System.out.println("Loaded the appropriate driver");
         } catch (ClassNotFoundException cnfe) {
             System.err.println("\nUnable to load the JDBC driver " + driver);
             System.err.println("Please check your CLASSPATH.");
             cnfe.printStackTrace(System.err);
         } catch (InstantiationException ie) {
             System.err.println(
                         "\nUnable to instantiate the JDBC driver " + driver);
             ie.printStackTrace(System.err);
         } catch (IllegalAccessException iae) {
             System.err.println(
                         "\nNot allowed to access the JDBC driver " + driver);
             iae.printStackTrace(System.err);
         }
     }
 
     /**
      * Reports a data verification failure to System.err with the given message.
      *
      * @param message A message describing what failed.
      */
     private void reportFailure(String message) {
         System.err.println("\nData verification failed:");
         System.err.println('\t' + message);
     }
 
     /**
      * Prints details of an SQLException chain to <code>System.err</code>.
      * Details included are SQL State, Error code, Exception message.
      *
      * @param e the SQLException from which to print details.
      */
     public static void printSQLException(SQLException e)
     {
         // Unwraps the entire exception chain to unveil the real cause of the
         // Exception.
         while (e != null)
         {
             System.err.println("\n----- SQLException -----");
             System.err.println("  SQL State:  " + e.getSQLState());
             System.err.println("  Error Code: " + e.getErrorCode());
             System.err.println("  Message:    " + e.getMessage());
             // for stack traces, refer to derby.log or uncomment this:
             e.printStackTrace(System.err);
             e = e.getNextException();
         }
     }    
 }
