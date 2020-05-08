 package db_processor;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public abstract class Filter implements Runnable
 {
 	private Manager manager;
 	private ResultSet rows;
 	
 	final public void init(Manager manager, ResultSet rows)
 	{
 		this.manager = manager;
 		this.rows = rows;
 	}
 	
 	@Override
 	final public void run()
 	{
 		try
 		{
 			int i = 0;
 			while (rows.next())
 			{
 				try // This sub try/catch is so that exceptions thrown by processing a single row don't prevent processing the rest of the chunk.
 				{
 					process(rows);
 				}
 				catch (SQLException ex)
 				{
 					System.out.println("SQLException: " + ex.getMessage());
 					System.out.println("SQLState: " + ex.getSQLState());
 					System.out.println("VendorError: " + ex.getErrorCode());
 				}
 				i++;
 			}
 			if (i == 0) {manager.cont = false;}
 			rows.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			manager.cont = false;
 		}
 		synchronized(manager)
 		{
 			manager.notify();
 		}
 	}
 	
 	abstract protected void process(ResultSet row) throws SQLException;
 	
 	protected void count(String key)
 	{
 		count(key, 1);
 	}
 	protected void count(String key, int count)
 	{
 		int[] val = manager.counts.get(key);
 		if (val == null)
 		{
 			manager.counts.put(key, new int[] {count});
 		}
 		else
 		{
 			val[0] += count;
 		}
 	}
 }
