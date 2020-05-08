 package db_processor;
 
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 public class DbProcessor
 {
 	static final String DB_JDBC_DRIVER = "com.mysql.jdbc.Driver";  
 
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args)
 	{
 		// Test args:
 		// CleanHtml -d piqa -t node -u root -c 10 -l 100 -x 8
 		
 		if (args.length < 1)
 		{
 			System.out.println("Please specify the processor to run.");
 			return;
 		}
 		
 		String filter_class = args[0];
 		if (filter_class.toLowerCase() == "list")
 		{
 			System.out.println("CleanHtml");
 			System.out.println("CountStartWords");
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
 		long limit = Long.MAX_VALUE;
 		
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
 				filter = (Class<Filter>) Class.forName("db_processor.filter.Filter" + filter_class);
 			}
 			catch (ClassNotFoundException e)
 			{
 		        System.out.println("Class " + "Filter" + filter_class + " not found");
 		        return;
 			}
 			
 			System.out.println("Please verify these parameters:");
 			System.out.println("	processor: " + filter_class);
 			System.out.println("	host: " + host);
 			System.out.println("	port: " + port);
 			System.out.println("	user: " + user);
 			System.out.println("	database: " + db);
 			System.out.println("	table: " + table);
 			System.out.println("	chunk size: " + Integer.toString(chunk_size));
 			System.out.println("	limit: " + Long.toString(limit));
 			System.out.println("	threads: " + Integer.toString(threads));
 			System.out.print("Continue (Y/N)? ");
 			
 		    Scanner input = new Scanner(System.in);
 		    String answer = input.nextLine();
 		    if (!answer.startsWith("Y") && !answer.startsWith("y"))
 		    {
 		    	System.out.println("Cancelled :(");
 		    	return;
 		    }
 			
 	        Manager manager = new Manager(conn, "SELECT * FROM " + table, threads, filter);
 	        manager.run(0, chunk_size, limit);
 	        
 	        System.out.println("Finished!");
 	        
 	        if (!manager.counts.isEmpty())
 	        {
 	        	System.out.println("Counts:");
 	        	List<Map.Entry<String, int[]>> counts = new LinkedList<Map.Entry<String, int[]>>(manager.counts.entrySet());
 	            Collections.sort(counts, new Comparator<Map.Entry<String, int[]>>()
 	            {
 	                public int compare(Map.Entry<String, int[]> m1, Map.Entry<String, int[]> m2)
 	                {
 	                    return m2.getValue()[0] - m1.getValue()[0];
 	                }
 	            });
 
 		        PrintWriter file = new PrintWriter("counts.txt");
 	            i = 0;
 	            Iterator<Map.Entry<String, int[]>> it = counts.iterator();
 	        	while (it.hasNext())
 	        	{
 	        		Map.Entry<String, int[]> e = it.next();
 	        		String str = e.getKey() + ": " + Integer.toString(e.getValue()[0]);
 	        		file.println(str);
 	        		i++;
 	        		if (i <= 100)
 	        		{
 	        			System.out.println(str);
 	        		}
 	        	}
 	        	file.close();
 	        	
 	        	if (i > 100)
 	        	{
 	        		System.out.println("Output (" + Integer.toString(i) + " lines) truncated to 100 lines, see counts.txt for a full log.");
 	        	}
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
 		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
