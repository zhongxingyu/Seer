 package com;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import java.util.logging.*;
 import java.util.logging.Formatter;
 
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import java.text.*;
  
 public class Handles
 {
 	//Declaring global variables
 	public static String day = null;
 	public static String cust_name = null;
 	public static Integer intCountOne = null;
 	public static Integer intCountTwo = null;
 	public static Integer intCountThree = null;
 	public static Integer intCountFour = null;
 	public static Integer intCountFive = null;
 	public static Integer intCountSix = null;
 	public static Integer intCountSeven = null;
 	public static Integer intCountEight = null;
 	public static Integer intCountNine = null;
 	public static Integer intCountTen = null;
 	public static Integer intCountTotal = null;
 	
 	//Main thread
 	@SuppressWarnings("deprecation")
 	public static void main(String[] args)
 	{	
 		
 		//Setup logging. Outputs to C:\Eclipse\workspace\Handles\extra\Handles_LogFile.txt
 		Logger logger = Logger.getLogger("MyLog");
 		FileHandler fh;  
 		try 
 		{  
 			int limit = 100000000;
			fh = new FileHandler("C:\\Git\\Personal\\Handles\\extra\\ListHistory_LogFile.txt", limit, 1);
 			logger.addHandler(fh);
 			fh.setFormatter(new Formatter()
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
 			});
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
			File filename = new File("C:\\Git\\Personal\\Handles\\extra\\ListHistory.txt");
 
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
 					String deleteTable 					= "DELETE FROM info WHERE emp_name IS NOT NULL;";
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
 						if (cust.length() >= 64) 
 						{
 							String subCust 				= cust.substring(46,64);
 							qmarks[i] 					= subCust;
 						}
 						String updateString 		= "REPLACE INTO info" + " (emp_name) " + "VALUE ('" + (qmarks[i]) + "');";
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
 					ResultSet result = select.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'pmesikapp        S';");
 					result.last();
 					intCountOne = result.getInt(1);
 					logger.info ("Got results for employee Phil Mesikapp: " + intCountOne);
 					result.first();
 					
 					Statement selectTwo = conn.createStatement();
 					ResultSet resultTwo = selectTwo.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'rrios            S';");
 					resultTwo.last();
 					intCountTwo = resultTwo.getInt(1);
 					logger.info ("Got results for employee Roberto Rios: " + intCountTwo);
 					resultTwo.first();
 					
 					Statement selectThree = conn.createStatement();
 					ResultSet resultThree = selectThree.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'rstaehler        S';");
 					resultThree.last();
 					intCountThree = resultThree.getInt(1);
 					logger.info ("Got results for employee Richard Staehler: " + intCountThree);
 					resultThree.first();
 					
 					Statement selectFour = conn.createStatement();
 					ResultSet resultFour = selectFour.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'bjensen          S';");
 					resultFour.last();
 					intCountFour = resultFour.getInt(1);
 					logger.info ("Got results for employee Brian Jensen: " + intCountFour);
 					resultFour.first();
 					
 					Statement selectFive = conn.createStatement();
 					ResultSet resultFive = selectFive.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'gbullard         S';");
 					resultFive.last();
 					intCountFive = resultFive.getInt(1);
 					logger.info ("Got results for employee Greg Bullard: " + intCountFive);
 					resultFive.first();
 					
 					Statement selectSix = conn.createStatement();
 					ResultSet resultSix = selectSix.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'gstangle         S';");
 					resultSix.last();
 					intCountSix = resultSix.getInt(1);
 					logger.info ("Got results for employee Greg Stangle: " + intCountSix);
 					resultSix.first();
 					
 					Statement selectSeven = conn.createStatement();
 					ResultSet resultSeven = selectSeven.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'emurray          S';");
 					resultSeven.last();
 					intCountSeven = resultSeven.getInt(1);
 					logger.info ("Got results for employee Eric Murray: " + intCountSeven);
 					resultSeven.first();
 					
 					Statement selectEight = conn.createStatement();
 					ResultSet resultEight = selectEight.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'rarunrung        S';");
 					resultEight.last();
 					intCountEight = resultEight.getInt(1);
 					logger.info ("Got results for employee Bert Arunrung: " + intCountEight);
 					resultEight.first();
 					
 					Statement selectNine = conn.createStatement();
 					ResultSet resultNine = selectNine.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'amartinez        S';");
 					resultNine.last();
 					intCountNine = resultNine.getInt(1);
 					logger.info ("Got results for employee Aldo Martinez: " + intCountNine);
 					resultNine.first();
 					
 					Statement selectTen = conn.createStatement();
 					ResultSet resultTen = selectTen.executeQuery ("SELECT COUNT(*) FROM info WHERE emp_name = 'jprager          S';");
 					resultTen.last();
 					intCountTen = resultTen.getInt(1);
 					logger.info ("Got results for employee Jon Prager: " + intCountTen);
 					resultTen.first();
 					
 					intCountTotal = (intCountOne + intCountTwo + intCountThree + intCountFour + intCountFive + intCountSix + intCountSeven + intCountEight + intCountNine + intCountTen);
 					
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
 						DecimalFormat oneDigit = new DecimalFormat("#,##0.0");
 						MimeMessage message = new MimeMessage(session);
 						message.setFrom(new InternetAddress("richard.staehler@gmail.com"));
 						message.addRecipients(Message.RecipientType.TO, "richard.staehler@gmail.com");
 						message.setSubject("Important Information from your TapeTrack Server");
 						message.setText("***This is an automated message from TapeTrack Server*** \r\r" 
 								+ currentDate.getTime()
 								+ ": The employee Phil Mesikapp had " + intCountOne + " handles! (" + (oneDigit.format((intCountOne*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee Roberto Rios had " + intCountTwo + " handles! (" + (oneDigit.format((intCountTwo*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee Eric Murray had " + intCountSeven + " handles! (" + (oneDigit.format((intCountSeven*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee Richard Staehler had " + intCountThree + " handles! (" + (oneDigit.format((intCountThree*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee Brian Jensen had " + intCountFour + " handles! (" + (oneDigit.format((intCountFour*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee Bert Arunrung had " + intCountEight + " handles! (" + (oneDigit.format((intCountEight*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee Aldo Martinez had " + intCountNine + " handles! (" + (oneDigit.format((intCountNine*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee John Prager had " + intCountTen + " handles! (" + (oneDigit.format((intCountTen*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee Greg Bullard had " + intCountFive + " handles! (" + (oneDigit.format((intCountFive*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": The employee Greg Stangle had " + intCountSix + " handles! (" + (oneDigit.format((intCountSix*100.0)/intCountTotal)) + "%)\r"
 								+ currentDate.getTime()
 								+ ": TOTAL HANDLES: " + intCountTotal);
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
