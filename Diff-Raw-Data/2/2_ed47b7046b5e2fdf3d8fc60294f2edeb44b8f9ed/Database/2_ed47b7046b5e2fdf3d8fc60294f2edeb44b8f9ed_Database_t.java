 package com.mycompany.reservationsystem.peer.data;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class Database {
 	private static Database instance = null;
 	private Connection connection = null;  
 	private ResultSet resultSet = null;  
 	private Statement statement = null;
 	private static final String connectionString = "jdbc:sqlite:data\\peer.db";
     
     
    public synchronized static Database getInstance(){
     	if(instance == null){
     		instance = new Database();
     	}
     	return instance;
     }
     
     public synchronized void connect(){
     	try {
     		//Loading class
 			Class.forName("org.sqlite.JDBC");
 		    connection = DriverManager.getConnection(connectionString);  
 		    statement = connection.createStatement();
 		    
     	} 
     	catch (Exception e) {  
     		e.printStackTrace();  
     	}
     }
     
     public synchronized void disconnect(){
     	try {  
             if(resultSet != null){
             	resultSet.close();
             }
             statement.close();  
             connection.close();  
         } 
     	catch (Exception e) {  
             e.printStackTrace();  
         }
     }
     
     public synchronized ArrayList<String> getCompanies(){
     	try {
     		resultSet = statement.executeQuery("SELECT * FROM companies");
     		ArrayList<String> companies = new ArrayList<String>();
     		
     		while(resultSet.next()){
     			companies.add(resultSet.getString("company_name"));
     		}
     		return companies;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	return null;
     } 
     
     public synchronized void addBooking(FlightBooking flightBooking){
     	try {
     		
     		int isFromCamp;
     		if(flightBooking.isFromCamp()){
     			isFromCamp = 1;
     		}
     		else{
     			isFromCamp = 0;
     		}
     		
     		int isFromCity;
     		if(flightBooking.isFromCity()){
     			isFromCity = 1;
     		}
     		else{
     			isFromCity = 0;
     		}
     		statement.executeUpdate("INSERT INTO flightbookings (transaction_epoch,email,flight_to_city_at," +
     				"flight_to_camp_at,from_city,from_camp,price,state) " +
     				"VALUES (" + flightBooking.getTransactionTime() + ",'" + flightBooking.getEmail() +
     				"','" + flightBooking.getFlightToCityAt() + "','" + flightBooking.getFlightToCampAt() +
     				"'," + isFromCity + "," + isFromCamp + "," + flightBooking.getPrice() + ",'" +
     				flightBooking.getState().toString() + "')");  
         } 
     	catch (Exception e) {  
             e.printStackTrace();  
         }
     }
     
     public synchronized ArrayList<FlightBooking> findBookingByEmail(String email){
     	try {
     		resultSet = statement.executeQuery("SELECT * FROM flightbookings WHERE email = '" + email + "'");
     		
     		ArrayList<FlightBooking> flightSearch = new ArrayList<FlightBooking>();
     		
     		while(resultSet.next()){
     			FlightBooking flight = new FlightBooking();
         		flight.setTransactionTime(resultSet.getLong("transaction_epoch"));
         		flight.setEmail(resultSet.getString("email"));
         		flight.setFlightToCityAt(resultSet.getString("flight_to_city_at"));
         		flight.setFlightToCampAt(resultSet.getString("flight_to_camp_at"));
         		
         		if(resultSet.getInt("from_city") == 1){
         			flight.setFromCity(true);
         		}
         		else{
         			flight.setFromCity(false);
         		}
         		
         		if(resultSet.getInt("from_camp") == 1){
         			flight.setFromCamp(true);
         		}
         		else{
         			flight.setFromCamp(false);
         		}
         		
         		flight.setPrice(resultSet.getDouble("price"));
         		
         		if(resultSet.getString("state").equals(FlightBooking.STATE.REQUESTED.toString())){
         			flight.setState(FlightBooking.STATE.REQUESTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CONFIRMED.toString())){
         			flight.setState(FlightBooking.STATE.CONFIRMED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
         			flight.setState(FlightBooking.STATE.CANCELED);
         		}
         		flightSearch.add(flight);
     		}
     		return flightSearch;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	return null;
     } 
     
     public synchronized ArrayList<FlightBooking> getAllBookings(){
     	try {    		
 			resultSet = statement.executeQuery("SELECT * FROM flightbookings");
 			
 			ArrayList<FlightBooking> listOfBookings = new ArrayList<FlightBooking>();
 			
 			while(resultSet.next()){
 				FlightBooking booking = new FlightBooking();
 				booking.setTransactionTime(resultSet.getLong("transaction_epoch"));
 				booking.setEmail(resultSet.getString("email"));
 				booking.setFlightToCityAt(resultSet.getString("flight_to_city_at"));
 				booking.setFlightToCampAt(resultSet.getString("flight_to_camp_at"));
 				booking.setPrice(resultSet.getDouble("price"));
 				
 				if(resultSet.getInt("from_city") == 1){
 					booking.setFromCity(true);
 				}
 				else{
 					booking.setFromCity(false);
 				}
 				
 				if(resultSet.getInt("from_camp") == 1){
 					booking.setFromCamp(true);
 				}
 				else{
 					booking.setFromCamp(false);
 				}				
 				
 				if(resultSet.getString("state").equals(FlightBooking.STATE.REQUESTED.toString())){
 					booking.setState(FlightBooking.STATE.REQUESTED);
 				}
 				else if(resultSet.getString("state").equals(FlightBooking.STATE.CONFIRMED.toString())){
 					booking.setState(FlightBooking.STATE.CONFIRMED);
 				}
 				else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
 					booking.setState(FlightBooking.STATE.CANCEL);
 				}
 				else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
 					booking.setState(FlightBooking.STATE.CANCELED);
 				}
 				
 	    		listOfBookings.add(booking);
 			}
 			return listOfBookings;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	return null;
     }
     
     public FlightBooking findFlightBooking(long transactionEpoch, String email){
     	try {
     		resultSet = statement.executeQuery("SELECT * FROM flightbookings WHERE transaction_epoch = " + transactionEpoch + " AND email = '" + email + "'");
     		
     		FlightBooking booking = new FlightBooking();
     		
     		while(resultSet.next()){
     			FlightBooking flight = new FlightBooking();
         		flight.setTransactionTime(resultSet.getLong("transaction_epoch"));
         		flight.setEmail(resultSet.getString("email"));
         		flight.setFlightToCityAt(resultSet.getString("flight_to_city_at"));
         		flight.setFlightToCampAt(resultSet.getString("flight_to_camp_at"));
         		
         		if(resultSet.getInt("from_city") == 1){
         			flight.setFromCity(true);
         		}
         		else{
         			flight.setFromCity(false);
         		}
         		
         		if(resultSet.getInt("from_camp") == 1){
         			flight.setFromCamp(true);
         		}
         		else{
         			flight.setFromCamp(false);
         		}
         		
         		flight.setPrice(resultSet.getDouble("price"));
         		
         		if(resultSet.getString("state").equals(FlightBooking.STATE.REQUESTED.toString())){
         			flight.setState(FlightBooking.STATE.REQUESTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CONFIRMED.toString())){
         			flight.setState(FlightBooking.STATE.CONFIRMED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
         			flight.setState(FlightBooking.STATE.CANCELED);
         		}
     		}
     		return booking;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	return null;
     }
     
     public synchronized void addPeer(Peer peer){
     	try {  
            statement.executeUpdate("INSERT INTO Peers (peer_address, active, last_updated) VALUES ('" + peer.getPeerIpAddress() + 
         		   "', '" + peer.getState().toString() + "', '" + peer.getEpochTime() + "');");  
         } 
     	catch (Exception e) {  
             e.printStackTrace();  
         }
     }
     
     public synchronized void updatePeer(Peer peer){
     	try {
 			statement.executeUpdate("UPDATE Peers SET active='" + peer.getState().toString() + "', last_updated='" + peer.getEpochTime() +
 					"' WHERE peer_address='" + peer.getPeerIpAddress() + "'");
 		} 
     	catch (SQLException e) {
 			e.printStackTrace();
 		}
     }
     
     public synchronized Peer findPeerByIpAddress(String ipAddress){
     	try {
     		resultSet = statement.executeQuery("SELECT * FROM Peers WHERE peer_address='" + ipAddress + "'");
     		
     		while(resultSet.next()){
     			Peer peer = new Peer();
         		peer.setPeerIpAddress(resultSet.getString("peer_address"));
         		
         		if(resultSet.getInt("active") == 1)
         		{
         			peer.setState(Peer.STATE.ACTIVE);
         		}
         		else{
         			peer.setState(Peer.STATE.INACTIVE);
         		}
         		peer.setEpochTime(resultSet.getLong("last_updated"));
         		
         		return peer;
     		}
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	return null;
     }
     
     public synchronized ArrayList<Peer> findPeersByState(Peer.STATE state){
     	ArrayList<Peer> listOfPeers = new ArrayList<Peer>();
     	try {    		
     		resultSet = statement.executeQuery("SELECT * FROM Peers WHERE active='" + state.toString() + "'");
     		
     		listOfPeers = new ArrayList<Peer>();
     		
     		while(resultSet.next()){
     			Peer peer = new Peer();
         		peer.setPeerIpAddress(resultSet.getString("peer_address"));
         		
         		if(resultSet.getString("active").equals(Peer.STATE.ACTIVE.toString()))
         		{
         			peer.setState(Peer.STATE.ACTIVE);
         		}
         		else{
         			peer.setState(Peer.STATE.INACTIVE);
         		}
         		peer.setEpochTime(resultSet.getLong("last_updated"));
         		listOfPeers.add(peer);
     		}
     		return listOfPeers;
     	}
     	catch(NullPointerException e){
     		
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	return listOfPeers;
     }
     
     public synchronized ArrayList<Peer> getAllPeers(){
     	try {    		
     		
     		resultSet = statement.executeQuery("SELECT * FROM Peers");
     		
     		ArrayList<Peer> listOfPeers = new ArrayList<Peer>();
     		
     		while(resultSet.next()){
     			Peer peer = new Peer();
         		peer.setPeerIpAddress(resultSet.getString("peer_address"));
         		
         		if(resultSet.getString("active").equals(Peer.STATE.ACTIVE.toString()))
         		{
         			peer.setState(Peer.STATE.ACTIVE);
         		}
         		else{
         			peer.setState(Peer.STATE.INACTIVE);
         		}
         		peer.setEpochTime(resultSet.getLong("last_updated"));
         		listOfPeers.add(peer);
     		}
     		return listOfPeers;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	return null;
     }
     
     public synchronized void logPeerInactive(String ipAddress){
 		Peer peer = findPeerByIpAddress(ipAddress);
 		peer.setState(Peer.STATE.INACTIVE);
 		peer.setEpochTime(new Date().getTime());
 		updatePeer(peer);
 	}
     
     public synchronized ArrayList<Integer> getTimes(){
     	try {
     		resultSet = statement.executeQuery("SELECT * FROM timetable");
     		ArrayList<Integer> times = new ArrayList<Integer>();
     		
     		while(resultSet.next()){
     			times.add(Integer.valueOf(resultSet.getInt("flight_time")));
     		}
     		return times;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	return null;
     }  
 }
