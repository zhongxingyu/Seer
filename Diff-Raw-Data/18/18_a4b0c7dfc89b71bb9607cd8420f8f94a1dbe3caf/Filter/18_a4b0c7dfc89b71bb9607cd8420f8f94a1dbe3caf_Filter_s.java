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
 				process(rows);
 				i++;
 			}
 			if (i == 0) {manager.cont = false;}
 			rows.close();
 		}
 		catch (SQLException ex)
 		{
 			synchronized(this)
 			{
 				String nl = System.getProperty("line.separator");
 				manager.error.append("SQLException: " + ex.getMessage() + nl);
 				manager.error.append("SQLState: " + ex.getSQLState() + nl);
 				manager.error.append("VendorError: " + ex.getErrorCode() + nl);
 				manager.cont = false;
 			}
 		}
 		synchronized(manager)
 		{
 			manager.notify();
 		}
 	}
 	
 	abstract protected void process(ResultSet row) throws SQLException;
 }
