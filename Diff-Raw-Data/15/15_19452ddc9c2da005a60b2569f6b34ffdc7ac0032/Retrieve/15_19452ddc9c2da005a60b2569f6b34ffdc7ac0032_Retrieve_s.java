 package mainpackage.mysql;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import mainpackage.AccountItem;
 
 public class Retrieve 
 {
 
 	/**
 	 * @param args
 	 */
 	private static List<RecurringItem> resultSetRA = new ArrayList<RecurringItem>();
 	private RecurringItem item;
 	
 	public List<RecurringItem> getResultSetRA()
 	{
 		return this.resultSetRA;
 	}
 	
 	public void upcomingActivityRetrieveMySQL() 
 	{
 		Connection connection = null;
 		PreparedStatement pStatement = null;
 		ResultSet resultSet = null;
 		
 		Properties props = new Properties();
 		FileInputStream inStream = null;
 		
 		try
 		{
 			inStream = new FileInputStream("/usr/local/mysql/database.properties");
 			props.load(inStream);
 		}
 		catch(FileNotFoundException fe)
 		{
 			Logger lgr = Logger.getLogger(Retrieve.class.getName());
             lgr.log(Level.SEVERE, fe.getMessage(), fe);
 		}
 		catch (IOException ie) 
 		{
             Logger lgr = Logger.getLogger(Retrieve.class.getName());
             lgr.log(Level.SEVERE, ie.getMessage(), ie);
 		}
 		finally
 		{
 			try
 			{
 				if(inStream != null)
 				{
 					inStream.close();
 				}
 			}
 			catch(IOException ie)
 			{
 				Logger lgr = Logger.getLogger(Retrieve.class.getName());
                 lgr.log(Level.SEVERE, ie.getMessage(), ie);
 			}
 		}
 		
 		String url = props.getProperty("db.url");
 		String user = props.getProperty("db.user");
 		String passwd = props.getProperty("db.passwd");
 		//Above block sets up MySQL connection
 		
 		//Following runs SQL commands
 		
 		try
 		{
 			connection = DriverManager.getConnection(url, user, passwd);
 			pStatement = connection.prepareStatement("SELECT date, ActivityName, Amount, " +
					"ActivityDate FROM calendar_table, Activities WHERE " +
 					"calendar_table.day=Activities.ActivityDate AND calendar_table.date " +
 					"BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 2 WEEK);");
 			resultSet = pStatement.executeQuery();
 			
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			
 			while(resultSet.next())
 			{
				item = new RecurringItem(resultSet.getString(2), Double.parseDouble(resultSet.getString(3)), 
						(Date)df.parse(resultSet.getString(1)), "Bill", 0);
 				
 				resultSetRA.add(item);	
 			}
 		}
 		catch(SQLException sqlEx)
 		{
 			Logger logger = Logger.getLogger(Retrieve.class.getName());
 			logger.log(Level.SEVERE, sqlEx.getMessage(), sqlEx);
 		}
 		catch (NumberFormatException e) 
 		{
 			e.printStackTrace();
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
 		}
 		finally
 		{
 			try
 			{
 				if(resultSet != null)
 				{
 					resultSet.close();
 				}
 				if(pStatement != null)
 				{
 					pStatement.close();
 				}
 				if(connection != null)
 				{
 					connection.close();
 				}
 			}
 			catch (SQLException ex) 
 			{
                 Logger lgr = Logger.getLogger(Retrieve.class.getName());
                 lgr.log(Level.WARNING, ex.getMessage(), ex);
 			}
 		}
 	}
 
 }
