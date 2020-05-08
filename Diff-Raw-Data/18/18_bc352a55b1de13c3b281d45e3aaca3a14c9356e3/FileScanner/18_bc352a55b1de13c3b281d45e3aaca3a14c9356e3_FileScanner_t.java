 package com;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import java.util.logging.*;
 import java.util.logging.Formatter;
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
  
 public class FileScanner
 {
 	//Declaring global variables
 	public static String day = null;
 	public static String cust_name = null;
 	public static int intCount = 0;
 	
 	//Main thread
 	@SuppressWarnings("deprecation")
 	public static void main(String[] args)
 	{	
 		
		//Setup logging. Outputs to C:\Git\Personal\FileScanner\extra\FileScanner_LogFile.txt
 		Logger logger = Logger.getLogger("MyLog");  
 		FileHandler fh;  
 		try 
 		{  
 			int limit = 100000000;
			fh = new FileHandler("C:\\Git\\Personal\\FileScanner\\extra\\FileScanner_LogFile.txt", limit, 1);  
 			logger.addHandler(fh);  
 			fh.setFormatter
 			(
 				new Formatter()
 					{   
 						public String format(LogRecord rec) 
 						{
 							StringBuffer buf = new StringBuffer(1000);
 							buf.append(new java.util.Date());
 							buf.append(' ');
 							buf.append(rec.getLevel());
 							buf.append(' ');
 							buf.append(formatMessage(rec));
 							buf.append('\n');
 							return buf.toString();
 						}
 					}
 			);
 		} 
 		catch (SecurityException e) 
 		{  
 			logger.warning(e.getMessage());
 			e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{  
 			logger.warning(e.getMessage());
 			e.printStackTrace();
 		}	  
     	
 		//Opening up connection to MySQL Database
 		Connection conn = null;
 		try
 		{
 			String userName = "root";
 			String password = "root";
 			String url 		= "jdbc:mysql://localhost:3306/filescanner";
 			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
 			conn = DriverManager.getConnection (url, userName, password);
 			logger.info ("Database connection established");
 		}
 		catch (Exception e)
 		{
 			logger.warning("Cannot connect to database server");
 			e.printStackTrace();
 		}
 		
 		//Opening file
 		try
 		{
 			Calendar cal 		= new GregorianCalendar();
 			int dayOfWeek 		= cal.get(Calendar.DAY_OF_WEEK);
 			String day 			= null;
 			if (dayOfWeek == 1)
 			{
 				day = "630PM";
 			}
 			if (dayOfWeek == 2)
 			{
 				day = "630PM";
 			}
 			if (dayOfWeek == 3)
 			{
 				day = "630PM";
 			}
 			if (dayOfWeek == 4)
 			{
 				day = "630PM";
 			}
 			if (dayOfWeek == 5)
 			{
 				day = "630PM";
 			}
 			if (dayOfWeek == 6)
 			{
 				day = "630PM";
 			}
 			if (dayOfWeek == 7)
 			{
 				day = "630PM";
 			}
 			
			File filename = new File("C:\\Git\\Personal\\FileScanner\\extra\\PickList" + day + ".txt");
 			
 			if (filename.exists())
 			{
 				FileReader fr 			= new FileReader(filename);
 				LineNumberReader lnr 	= new LineNumberReader(fr);
 				int linenumber 			= 0;
  
 				while (lnr.readLine() != null)
 				{
 					linenumber++;
 				}
 				FileInputStream fstream = null;
 				
 				//Deleting existing data from database
 				try
 				{
 					fstream                 			= new FileInputStream(filename);
 					DataInputStream in     				= new DataInputStream(fstream);
 					BufferedReader br      			 	= new BufferedReader(new InputStreamReader(in));
 					PreparedStatement deleteFields 		= null;
 					String deleteTable 					= "DELETE FROM move WHERE CUST_NAME IS NOT NULL;";
 					deleteFields 						= conn.prepareStatement(deleteTable);
 					deleteFields.executeUpdate();
 					deleteFields.close();
 					logger.info(deleteTable);
 					PreparedStatement updateFields 		= null;
 					String[] qmarks                		= new String[linenumber];
 					
 					//Adding data to the database
 					for(int i = 0; i < linenumber; i++)
 					{
 						String cust 				= in.readLine();
 						String subCust 				= cust.substring(24,28);
 						qmarks[i] 					= subCust;
 						String updateString 		= "REPLACE INTO move" + " (CUST_NAME) " + "VALUE ('" + (qmarks[i]) + "');";
 						logger.info(updateString);
 				
 						try
 						{	
 							updateFields = conn.prepareStatement(updateString);
 							updateFields.executeUpdate();
 							updateFields.close();
 						}
 						catch (SQLException e)
 						{
 							logger.warning("Error:" + e.getMessage());
 							e.printStackTrace();
 						}
 					}
 					
 					//Closing open streams to prevent memory leaks.
 					fstream.close();
 					in.close();
 					br.close();
 					
 					//Retrieving data from the database
 					Statement select = conn.createStatement();
 					ResultSet result = select.executeQuery ("SELECT DISTINCT * FROM move WHERE CUST_NAME REGEXP '^[0-9]' AND CUST_NAME != ('0Mov' OR '0000');");
 					result.last();
 					intCount = result.getRow();
 					logger.info ("Got results: " + intCount);
 					result.first();
 					
 					//Adding that data to an array
 					String[] reArray = new String[intCount];
 					for(int i = 0; i < intCount; i++)
 					{
 						reArray[i] = result.getString(1);
 						result.next();
 					}
 					select.close();
 	        
 					//Setting up email
 					Calendar currentDate = Calendar.getInstance();
 					
 					final String username = "richard.staehler@gmail.com";
 					final String password = "Ijnygv12";
 					
 					String host = "smtp.gmail.com";
 					String port = "587";
 					
 					Properties properties = new Properties();
 					properties.setProperty("mail.smtp.auth", "true");
 					properties.setProperty("mail.smtp.starttls.enable", "true");
 					properties.setProperty("mail.smtp.host", host);
 					properties.setProperty("mail.smtp.port", port);
 					
 					Session session = Session.getInstance(properties,
 							new javax.mail.Authenticator() 
 							{
 								protected PasswordAuthentication getPasswordAuthentication() 
 								{
 										return new PasswordAuthentication(username, password);
 								}
 							}
 					);
 
 					//Attempting to send the message
 					try
 					{
 						MimeMessage message = new MimeMessage(session);
 						message.setFrom(new InternetAddress("richard.staehler@gmail.com"));
 						message.addRecipient(Message.RecipientType.TO, new InternetAddress("richard.staehler@gmail.com"));
 						message.setSubject("Important Information from your TapeTrack Server");
 						message.setText("***This is an automated message from TapeTrack Server*** \r\r" 
 								+ currentDate.getTime()
 								+ ": There are " + intCount + " clients that have pending tape movements! \r\r"
 								+ "The following clients have pending tape movements: \r"
 								+ Arrays.toString(reArray)
 								+ "\r\rAs a reminder this list should be zeroed out daily. \r"
 								+ "Review these tape movements and resolve.");
 						Transport.send(message);
 						logger.info("Sent message successfully....");
 					}
 					catch (MessagingException e) 
 					{
 						logger.warning(e.getMessage());
 						e.printStackTrace();
 					}
 				}
 				
 				catch (FileNotFoundException e) 
 				{
 					logger.warning("File Not Found Error:" + e.getMessage());
 					e.printStackTrace();
 				}
 				catch (Exception e) 
 				{
 					logger.warning("General Error:" + e.getMessage());
 					e.printStackTrace();
 				}
 	    
 				finally
 				{		
 					if (conn != null)
 					{
 						try
 						{
 							conn.close ();
 							logger.info ("Database connection terminated");
 						}
 						catch (Exception e) 
 						{ 
 							logger.warning("Database did not close properly. Error:" + e.getMessage());	
 							e.printStackTrace();
 						}
 					}
 					lnr.close();
 				}
 			}
 			else
 			{
 				logger.warning("File does not exist!");
 			}
 		}
 		catch(IOException e)
 		{
 			logger.warning("I/O Error:" + e.getMessage());
 			e.printStackTrace();
 		}
 	}  
 }
