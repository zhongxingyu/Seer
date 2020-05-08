 package db_processor;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class DbProcessor
 {
 	static final String DB_JDBC_DRIVER = "com.mysql.jdbc.Driver";  
 
 	public static void main(String[] args)
 	{
 		// Test args:
		// CleanHtml -d piqa -t node -u root -c 10 -l 100 -x 8
 		
 		if (args.length < 1)
 		{
 			System.out.println("Please specify the processor to run.");
 			return;
 		}
 		
 		String processor_class = args[0];
 		if (processor_class.toLowerCase() == "list")
 		{
 			System.out.println("CleanHtml");
 			return;
 		}
 		
 		String host = "127.0.0.1";
 		String port = "3306";
 		String db = "";
 		String user = "";
 		String password = "";
 		String table = "";
 		int threads = 16;
 		int chunk_size = 1000;
 		int limit = Integer.MAX_VALUE;
 		
 		int i = 1;
 		int c = args.length - 1;
 		while (i < c)
 		{
 			if (args[i].equals("-h") || args[i].equals("--host"))
 			{
 				host = args[++i];
 			}
 			else if (args[i].equals("-p") || args[i].equals("--port"))
 			{
 				port = Integer.toString(Integer.parseInt(args[++i]));
 			}
 			else if (args[i].equals("-d") || args[i].equals("--db"))
 			{
 				db = args[++i];
 			}
 			else if (args[i].equals("-u") || args[i].equals("--user"))
 			{
 				user = args[++i];
 			}
 			else if (args[i].equals("-w") || args[i].equals("--password"))
 			{
 				password = args[++i];
 			}
 			else if (args[i].equals("-t") || args[i].equals("--table"))
 			{
 				table = args[++i];
 			}
 			else if (args[i].equals("-x") || args[i].equals("--threads"))
 			{
 				threads = Integer.parseInt(args[++i]);
 			}
 			else if (args[i].equals("-c") || args[i].equals("--chunk"))
 			{
 				chunk_size = Integer.parseInt(args[++i]);
 			}
 			else if (args[i].equals("-l") || args[i].equals("--limit"))
 			{
 				limit = Integer.parseInt(args[++i]);
 			}
 			else
 			{
 				System.out.println("Warning: Unrecognized argument \"" + args[i] + "\"");
 			}
 			i++;
 		}
 		
 		if (db.isEmpty())
 		{
 			System.out.println("Please specify a database with -d or --db");
 			return;
 		}
 		if (user.isEmpty())
 		{
 			System.out.println("Please specify a user with -u or --user");
 			return;
 		}
 		if (table.isEmpty())
 		{
 			System.out.println("Please specify a table with -t or --table");
 			return;
 		}
 		
 		try
 		{
 			try
 			{
 				Class.forName(DB_JDBC_DRIVER);
 			}
 			catch (ClassNotFoundException e)
 			{
 		        System.out.println("Class " + DB_JDBC_DRIVER + " not found");
 		        return;
 			}
 	        Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, password);
 	        System.out.println("Connected to database...");
 	        	        
 	        Class<Filter> filter;
 			try
 			{
 				filter = (Class<Filter>) Class.forName("db_processor.filter.Filter" + processor_class);
 			}
 			catch (ClassNotFoundException e)
 			{
 		        System.out.println("Class " + "Filter" + processor_class + " not found");
 		        return;
 			}
 			
 	        Manager manager = new Manager(conn, "SELECT * FROM " + table, threads, filter);
 	        StringBuilder error = manager.run(0, chunk_size, limit);
 	        
 	        if (error.length() > 0)
 	        {
 	        	System.out.println("Finished with errors:");
 	        	System.out.println(error);
 	        }
 	        else
 	        {
 	        	System.out.println("Finished!");
 	        }
 		}
 		catch (SQLException ex)
 		{
 			ex.printStackTrace();
 		    System.out.println("SQLException: " + ex.getMessage());
 		    System.out.println("SQLState: " + ex.getSQLState());
 		    System.out.println("VendorError: " + ex.getErrorCode());
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
