 package shuman.airlineSystem.classes;
 
 /*import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;*/
 
 import java.sql.*;
 //import java.sql.Date;
 //import java.util.*;
 import java.util.ArrayList;
 
 /*
  * This class manages the properties file for the servlets.
  * It contains the methods for adding a user and verifying a user exists in the properties file already
  */
 public class UserSystem {
 	
 	Connection conn = null;
     Statement stmt = null;
     PreparedStatement pstmt = null;
     ResultSet rs = null;
 	
 //String contains properties file location
 //private static final String FILENAME = "C:\\Users\\jshuman3\\config.properties";
 //Properties userProp = new Properties();
 /*
  * Method is used to add a user with the parameters username and password
  */
 public boolean addUser(String username, String password, String email, String dob)
 {
 	/*try { //try to reload the file.
 		FileInputStream in = new FileInputStream(FILENAME);
 		userProp.load(in);
 	} catch (IOException e) {
 			e.printStackTrace();
 	}	
 	
 	try{
 		FileOutputStream out = new FileOutputStream(FILENAME);
 		userProp.setProperty(username, password);
 		userProp.store(out, null);
 		out.close();
 		
 	}
 	catch(IOException ex)
 	{
 		ex.printStackTrace();
 		return false;
 	}
 	return true;*/
 	
 	String conf = "";
     try {
     	conn = getConnection();
                  	       
             	
         pstmt = conn.prepareStatement("INSERT INTO SYSTEM.USERS (USERNAME, PASSWORD, EMAIL, DOB) VALUES (?, ?, ?, ?)");
         pstmt.setString(1, username);
         pstmt.setString(2, password);
         pstmt.setString(3, email);
         pstmt.setString(4, dob);
         
         rs = pstmt.executeQuery();
         
         if (rs.next()) {
            conf= "Success";		 
          } else {
            conf= "Failure";
          }
             
         
         
       } catch (Exception e) {
           e.printStackTrace();
           System.exit(1);
       } finally {
           try {
               rs.close();
               //stmt.close();
               pstmt.close();
               conn.close();
           } catch (SQLException e) {
                 e.printStackTrace();
           }
       }  
 	
 	
      
         if (conf == "Success"){
         	return true;
         } else {
         	return false;
         }
 }
 /*
  * Method attempts to load a properties file and then verifies whether or not a user
  * is already in the system.
  * 
  * Will only determine if the combination is wrong, not whether just the username or password
  * is invalid for security reasons.
  */
 public boolean isValidUser(String username, String password)
 {
 	
 	
 	String conf = "";
     try {
     	conn = getConnection();
                  	       
         /*	                 
         String query = "SELECT * FROM SYSTEM.USERS WHERE USERNAME='" + username  + "' AND PASSWORD='" + password + "' ";
         stmt = conn.createStatement();
         rs = stmt.executeQuery(query);
         */
     	
         pstmt = conn.prepareStatement("SELECT * FROM USERS WHERE USERNAME = ? AND PASSWORD = ?");
         pstmt.setString(1, username);
         pstmt.setString(2, password);
         rs = pstmt.executeQuery();
         
         if (rs.next()) {
            conf= "Success";	
            System.out.println("Found user " + username);
          } else {
            conf= "Failure";
            System.out.println("user not found..");
          }
             
         
         
       } catch (Exception e) {
           e.printStackTrace();
           System.exit(1);
       } finally {
           try {
               rs.close();
               //stmt.close();
               pstmt.close();
               conn.close();
           } catch (SQLException e) {
                 e.printStackTrace();
           }
       }  
 	
 	
      
         if (conf == "Success"){
         	return true;
         } else {
         	return false;
         }
 				
 }
 
 public static Connection getConnection() throws Exception {
 	String driver = "oracle.jdbc.driver.OracleDriver";
     String url = "jdbc:oracle:thin:@localhost:1521:XE";
     //String dbName = "testing";
     String username = "SYSTEM";
     String password = "password";
     
     Class.forName(driver); 
 	Connection conn = DriverManager.getConnection(url, username, password);
 	
 	return conn;
 
 }
 /*
  * This method queries the database and returns a completed Flight based on a unique flight number.
  * @param flightNum Flight number to query
  * @return flight Corresponding flight information
  * @return null Flight does not exist or an error occured
  */
 public Flight getFlight(int flightNum) {
 	// TODO Auto-generated method stub
 	Flight flight = new Flight();
 	try {
 		conn = getConnection();
 		
 		pstmt = conn.prepareStatement("SELECT * FROM FLIGHTS WHERE FLIGHTNUMBER = ?");
 		pstmt.setInt(1, flightNum);
 		rs = pstmt.executeQuery();
 
 		if(rs.next())
 		{
 			/**
 			 * Collects values from database and puts them in Flight object.
 			 */
 			System.out.println("Retrieved flight information.");
 			flight.setOperator(rs.getString("OPERATOR"));
 			flight.setSource(rs.getString("SOURCE"));
 			flight.setDestination(rs.getString("DESTINATION"));
 			flight.setSeatCost(Double.valueOf(rs.getString("COST")));
 			flight.setDeparture(rs.getTimestamp("DEPARTURE"));
 			flight.setArrival(rs.getTimestamp("ARRIVAL"));
 			flight.setSeatsAvail(rs.getInt("SEATS_TOTAL") - rs.getInt("SEATS_TAKEN"));
 			flight.setFlightId(flightNum);		
 		}
 		else
 			return null;
 	} catch (SQLException e) {
 		e.printStackTrace();
 		return null;
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}finally{
 		try {
             rs.close();
             //stmt.close();
             pstmt.close();
             conn.close();
         } catch (SQLException e) {
               e.printStackTrace();
         }
 	}
 	
 	return flight;
 	
 }
 public Account getAccount(int routingNum, int accountNum) {
 	// TODO Auto-generated method stub
 	Account account = new Account();
 	try {
 		conn = getConnection();
 		
 		pstmt = conn.prepareStatement("SELECT * FROM ACCOUNT WHERE ROUTINGNUMBER = ? AND ACCOUNTID = ?");
 		pstmt.setInt(1, routingNum);
 		pstmt.setInt(2, accountNum);
 		rs = pstmt.executeQuery();
 
 		if(rs.next())
 		{
 			/**
 			 * Collects values from database and puts them in Flight object.
 			 */
 			System.out.println("Retrieved account information.");
 			account.setAccountId(rs.getInt("ACCOUNTID"));
 			account.setBalance(rs.getInt("BALANCE"));
 			account.setHolderName(rs.getString("HOLDERNAME"));
 			account.setRoutingNumber(rs.getInt("ROUTINGNUMBER"));
 		}
 		else
 			return null;
 	} catch (SQLException e) {
 		e.printStackTrace();
 		return null;
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}finally{
 		try {
             rs.close();
             //stmt.close();
             pstmt.close();
             conn.close();
         } catch (SQLException e) {
               e.printStackTrace();
         }
 	}
 	
 	return account;
 	
 }
 public String[][] searchFlights(String source, String destination, String departDate, String returnDate, String seats, String flightClass) throws Exception{
 	
 	String[][] flights = new String[100][100];
 	try{
 	conn = getConnection();
 	
 	pstmt = conn.prepareStatement("SELECT * FROM FLIGHTS WHERE SOURCE = ? AND DESTINATION = ? AND SEATS > 0");
 	pstmt.setString(1, source);
 	pstmt.setString(2, destination);
	Timestamp depart = new Timestamp();
	depart.setTime(time)
	pstmt.setTimestamp(3, x)
 	rs = pstmt.executeQuery();
 	
 	int row = 1;
 	
 		while (rs.next()){
 						
 			flights[row][1] = rs.getString("FLIGHTNUMBER");
 			flights[row][2] = rs.getString("OPERATOR");
 			flights[row][3] = rs.getString("SOURCE");
 			flights[row][4] = rs.getString("DESTINATION");
 			flights[row][5] = rs.getString("SEATS_TOTAL");
 			flights[row][6] = rs.getString("SEATS_TAKEN");
 			flights[row][7] = rs.getString("COST");
 			flights[row][8] = rs.getString("DEPARTURE");
 			flights[row][9] = rs.getString("ARRIVAL");
 			row = row + 1;
 		}
 	
 	} catch (Exception e) {
         e.printStackTrace();
         System.exit(1);
     } finally {
         try {
             rs.close();
             stmt.close();
             conn.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 	
 
 	return flights;
 	
 }
 /*public void searchFlights(String source, String destination, java.util.Date dateDepart,
 		java.util.Date dateReturn, int nSeats, String flightClass) {
 	// TODO Auto-generated method stub
 	
 	
 }*/
 public Boolean debitBalance(Account account, double newCost) {
 	// TODO Auto-generated method stub
 		Boolean succeed = false;
 		try {
 			conn = getConnection();
 			
 			pstmt = conn.prepareStatement("UPDATE ACCOUNTS SET BALANCE = ? WHERE ACCOUNTID = ? AND ROUTINGNUMBER = ?");
 			pstmt.setDouble(1, newCost);
 			pstmt.setInt(2, account.getAccountId());
 			pstmt.setInt(2, account.getRoutingNumber());
 			
 			int status = pstmt.executeUpdate();
 			if (status == 0)
 				succeed = false;
 			else
 				succeed = true;
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			succeed = false;
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			try {
 	            rs.close();
 	            //stmt.close();
 	            pstmt.close();
 	            conn.close();
 	        } catch (SQLException e) {
 	              e.printStackTrace();
 	              succeed = false;
 	        }
 		}
 		return succeed;
 	
 }
 public Boolean addBooking(String username, ArrayList<Flight> flightCart, Account account) {
 	Boolean succeed = false;
 	try {
 		conn = getConnection();
 		for(Flight flight : flightCart)
 		{
 			/*
 			 * It is worth nothing that the BOOKINGID field was changed in our schema to a dynamically generated primary key.
 			 *  
 			 *  SQL********
 			 *  
 			 *  CREATE SEQUENCE booking_seq;
 
 				CREATE OR REPLACE TRIGGER booking_bir 
 				BEFORE INSERT ON BOOKINGS
 				FOR EACH ROW
 				
 				BEGIN
 				  SELECT booking_seq.NEXTVAL
 				  INTO   :new.BOOKINGID
 				  FROM   dual;
 				END;
 				/
 			 *
 			 * end SQL
 			 */
 			
 		pstmt = conn.prepareStatement("INSERT INTO BOOKINGS " +
 				"(USERNAME, FLIGHTID, DATE, NUMBEROFSEATS, ACCOUNTID, TOTALCOST)" +
 				"VALUES" +
 				"(?, ?, ?, ?, ?, ?");
 		
 		pstmt.setString(1, username);
 		pstmt.setInt(2, flight.getFlightId());
 		pstmt.setTimestamp(3, flight.getDeparture());
 		pstmt.setInt(4, flight.getNumberOfSeats());
 		pstmt.setInt(5, account.getAccountId());
 		pstmt.setDouble(6, flight.getTotalCost());
 		
 		int status = pstmt.executeUpdate();
 		if (status == 0){
 			System.err.print("Failed to add booking history: " + flight.getFlightId());
 			succeed = false;
 		}
 		else
 			succeed = true;
 		}
 
 	} catch (SQLException e) {
 		e.printStackTrace();
 		succeed = false;
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}finally{
 		try {
             rs.close();
             //stmt.close();
             pstmt.close();
             conn.close();
         } catch (SQLException e) {
               e.printStackTrace();
               succeed = false;
         }
 	}
 	return succeed;
 	
 }
 public ArrayList<Booking> getBooking(User user) {
 	ArrayList<Booking> bookingHistory = new ArrayList<Booking>();
 	try {
 		conn = getConnection();
 		
 		pstmt = conn.prepareStatement("SELECT * FROM BOOKINGS WHERE USERNAME = ? ");
 		pstmt.setString(1, user.getUser());
 		rs = pstmt.executeQuery();
 		
 		
 
 		while(rs.next())
 		{
 			Booking booking = new Booking();
 			/**
 			 * Collects values from database and puts them in Flight object.
 			 */
 			booking.setBookingID(rs.getInt("BOOKINGID"));
 			booking.setFlightId(rs.getInt("FLIGHTID"));
 			booking.setDeparture(rs.getTimestamp("DATE"));
 			booking.setNumberOfSeats(rs.getInt("NUMBEROFSEATS"));
 			booking.setTotalCost(rs.getDouble("TOTALCOST"));
 			booking.setAccountID(rs.getInt("ACCOUNTID"));
 			bookingHistory.add(booking);
 		}
 	} catch (SQLException e) {
 		e.printStackTrace();
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}finally{
 		try {
             rs.close();
             //stmt.close();
             pstmt.close();
             conn.close();
         } catch (SQLException e) {
               e.printStackTrace();
         }
 	}
 	
 	return bookingHistory;
 }
 }
