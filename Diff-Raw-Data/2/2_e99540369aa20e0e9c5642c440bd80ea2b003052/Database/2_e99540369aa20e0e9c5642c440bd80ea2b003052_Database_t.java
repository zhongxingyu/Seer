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
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM companies");
     		ArrayList<String> companies = new ArrayList<String>();
     		
     		while(resultSet.next()){
     			companies.add(resultSet.getString("company_name"));
     		}
     		disconnect();
     		return companies;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return null;
     } 
     
     public synchronized void addBooking(FlightBooking flightBooking){
     	try {
     		connect();
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
     	disconnect();
     }
     
     public synchronized ArrayList<FlightBooking> findBookingByEmail(String email){
     	try {
     		connect();
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
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.REJECTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
         			flight.setState(FlightBooking.STATE.CANCELED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL_REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL_REJECTED);
         		}
         		flightSearch.add(flight);
     		}
     		disconnect();
     		return flightSearch;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return null;
     }
     
     public synchronized ArrayList<FlightBooking> findBooking(long startOfPeriod, long endOfPeriod){
     	ArrayList<FlightBooking> flightBookingsTimePeriod = new ArrayList<FlightBooking>();
     	try {
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM flightbookings " +
     				"WHERE transaction_epoch >= " + startOfPeriod + " AND transaction_epoch <= " + endOfPeriod + " " +
     				"ORDER BY transaction_epoch");
     		
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
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.REJECTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
         			flight.setState(FlightBooking.STATE.CANCELED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL_REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL_REJECTED);
         		}
         		flightBookingsTimePeriod.add(flight);
     		}
     		disconnect();
     		return flightBookingsTimePeriod;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return flightBookingsTimePeriod;
     }
     
     public synchronized ArrayList<FlightBooking> findBookings(String email, String flightToCityAt,String flightToCampAt, FlightBooking.STATE state){
     	ArrayList<FlightBooking> flightBookingsByFlightDateTimeState = new ArrayList<FlightBooking>();
     	try {
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM flightbookings " +
     				"WHERE email = '" + email + "' AND flight_to_city_at = '" + flightToCityAt +
     				"' AND flight_to_camp_at = '" + flightToCampAt + "' AND state = '" + state.toString() + "'");
     		
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
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.REJECTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
         			flight.setState(FlightBooking.STATE.CANCELED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL_REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL_REJECTED);
         		}
         		flightBookingsByFlightDateTimeState.add(flight);
     		}
     		disconnect();
     		return flightBookingsByFlightDateTimeState;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return flightBookingsByFlightDateTimeState;
     }
     
     
     public synchronized ArrayList<FlightBooking> findBooking(String email, FlightBooking.STATE state){
     	ArrayList<FlightBooking> flightSearch = new ArrayList<FlightBooking>();
     	try {
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM flightbookings WHERE email = '" + email + "' " +
     				"AND state = '" + state.toString() + "'");
     		
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
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.REJECTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
         			flight.setState(FlightBooking.STATE.CANCELED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL_REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL_REJECTED);
         		}
         		flightSearch.add(flight);
     		}
     		disconnect();
     		return flightSearch;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return flightSearch;
     }
     
     public synchronized ArrayList<FlightBooking> findBooking(String flightDateTime, boolean toCity){
     	ArrayList<FlightBooking> flightDateTimeTransaction = new ArrayList<FlightBooking>();
     	try {
     		connect();
     		String query = "";
     		
     		if(toCity){
     			query = "SELECT * FROM flightbookings WHERE flight_to_city_at = '" + flightDateTime + "'";
     		}
     		else{
     			query = "SELECT * FROM flightbookings WHERE flight_to_camp_at = '" + flightDateTime + "'";
     		}
     		
     		resultSet = statement.executeQuery(query);
     		
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
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.REJECTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
         			flight.setState(FlightBooking.STATE.CANCELED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL_REJECTED.toString())){
         			flight.setState(FlightBooking.STATE.CANCEL_REJECTED);
         		}
         		flightDateTimeTransaction.add(flight);
     		}
     		disconnect();
     		return flightDateTimeTransaction;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return flightDateTimeTransaction;
     }
     
     public synchronized ArrayList<FlightBooking> getAllBookings(){
     	try {    	
     		connect();
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
 				else if(resultSet.getString("state").equals(FlightBooking.STATE.REJECTED.toString())){
 					booking.setState(FlightBooking.STATE.REJECTED);
 				}
 				else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
 					booking.setState(FlightBooking.STATE.CANCEL);
 				}
 				else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
 					booking.setState(FlightBooking.STATE.CANCELED);
 				}
 				else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL_REJECTED.toString())){
 					booking.setState(FlightBooking.STATE.CANCEL_REJECTED);
 				}
 				
 	    		listOfBookings.add(booking);
 			}
 			disconnect();
 			return listOfBookings;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return null;
     }
     
     public synchronized FlightBooking findFlightBooking(long transactionEpoch, String email){
     	FlightBooking booking = new FlightBooking();
     	try {
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM flightbookings WHERE transaction_epoch = " + transactionEpoch + " AND email = '" + email + "'");
     		
     		while(resultSet.next()){
         		booking.setTransactionTime(resultSet.getLong("transaction_epoch"));
         		booking.setEmail(resultSet.getString("email"));
         		booking.setFlightToCityAt(resultSet.getString("flight_to_city_at"));
         		booking.setFlightToCampAt(resultSet.getString("flight_to_camp_at"));
         		
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
         		
         		booking.setPrice(resultSet.getDouble("price"));
         		
         		if(resultSet.getString("state").equals(FlightBooking.STATE.REQUESTED.toString())){
         			booking.setState(FlightBooking.STATE.REQUESTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CONFIRMED.toString())){
         			booking.setState(FlightBooking.STATE.CONFIRMED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.REJECTED.toString())){
         			booking.setState(FlightBooking.STATE.REJECTED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL.toString())){
         			booking.setState(FlightBooking.STATE.CANCEL);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCELED.toString())){
         			booking.setState(FlightBooking.STATE.CANCELED);
         		}
         		else if(resultSet.getString("state").equals(FlightBooking.STATE.CANCEL_REJECTED.toString())){
         			booking.setState(FlightBooking.STATE.CANCEL_REJECTED);
         		}
     		}
     		disconnect();
     		return booking;
     	}
     	catch (Exception e) {  
     		e.printStackTrace();
         }
     	disconnect();
     	return booking;
     }
     
     public synchronized boolean isFlightBooking(long transactionEpoch, String email){
     	boolean isFlightPresent = false;
     	try {
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM flightbookings WHERE transaction_epoch = " + transactionEpoch + " AND email = '" + email + "'");
     		
     		while(resultSet.next()){
     			isFlightPresent = true;
     		}
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return isFlightPresent;
     }
     
     public synchronized void addPeer(Peer peer){
     	try {  
     		connect();
            statement.executeUpdate("INSERT INTO Peers (peer_address, active, last_updated) VALUES ('" + peer.getPeerIpAddress() + 
         		   "', '" + peer.getState().toString() + "', '" + peer.getEpochTime() + "');");  
            disconnect();
         } 
     	catch (Exception e) {  
             //e.printStackTrace();  
         }
     }
     
     public synchronized void updatePeer(Peer peer){
     	try {
     		connect();
 			statement.executeUpdate("UPDATE Peers SET active='" + peer.getState().toString() + "', last_updated='" + peer.getEpochTime() +
 					"' WHERE peer_address='" + peer.getPeerIpAddress() + "'");
 			disconnect();
 		} 
     	catch (SQLException e) {
 			e.printStackTrace();
 		}
     }
     
     public synchronized Peer findPeerByIpAddress(String ipAddress){
     	Peer peer = new Peer();
     	try {
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM Peers WHERE peer_address='" + ipAddress + "'");
     		
     		while(resultSet.next()){
         		peer.setPeerIpAddress(resultSet.getString("peer_address"));
         		
         		if(resultSet.getInt("active") == 1)
         		{
         			peer.setState(Peer.STATE.ACTIVE);
         		}
         		else{
         			peer.setState(Peer.STATE.INACTIVE);
         		}
         		peer.setEpochTime(resultSet.getLong("last_updated"));
         		disconnect();
         		return peer;
     		}
     	}
     	catch (Exception e) {  
     		e.printStackTrace();
         }
     	disconnect();
     	return peer;
     }
     
     public synchronized ArrayList<Peer> findPeersByState(Peer.STATE state){
     	ArrayList<Peer> listOfPeers = new ArrayList<Peer>();
     	try {   
     		connect();
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
     		disconnect();
     		return listOfPeers;
     	}
     	catch(NullPointerException e){
     		e.printStackTrace();
     	}
     	catch (Exception e) {  
     		e.printStackTrace();
         }
     	disconnect();
     	return listOfPeers;
     }
     
     public synchronized ArrayList<Peer> getAllPeers(){
     	try {    		
     		connect();
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
     		disconnect();
     		return listOfPeers;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return null;
     }
     
     public synchronized void logPeerInactive(String ipAddress){
     	connect();
 		Peer peer = findPeerByIpAddress(ipAddress);
 		peer.setState(Peer.STATE.INACTIVE);
 		peer.setEpochTime(new Date().getTime());
 		updatePeer(peer);
 		disconnect();
 	}
     
     public synchronized ArrayList<FlightTime> getTimes(){
     	ArrayList<FlightTime> times = new ArrayList<FlightTime>();
     	try {
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM timetable");
     		
     		while(resultSet.next()){
     			FlightTime flightTime = new FlightTime();
     			flightTime.setFlightTime(resultSet.getString("flight_time"));
     			flightTime.setNumOfSeats(Integer.valueOf(resultSet.getString("number_seats")));
     			times.add(flightTime);
     		}
     		disconnect();
     		return times;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return times;
     }
     
     public synchronized FlightTime getNumberSeats(String time){
     	FlightTime flightTime = new FlightTime();
     	try {
     		connect();
     		resultSet = statement.executeQuery("SELECT * FROM timetable WHERE flight_time = '" + time + "'");
     		
     		while(resultSet.next()){
     			flightTime.setFlightTime(resultSet.getString("flight_time"));
     			flightTime.setNumOfSeats(Integer.valueOf(resultSet.getString("number_seats")));
     		}
     		disconnect();
     		return flightTime;
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     	disconnect();
     	return flightTime;
     }
     
     public synchronized void removeDuplicateBookings(){
     	ArrayList<FlightBooking> allBookings = getAllBookings();
     	
     	for(FlightBooking flightBooking : allBookings){
     		ArrayList<FlightBooking> bookings = findBookings(flightBooking.getEmail(), flightBooking.getFlightToCityAt(), 
     				flightBooking.getFlightToCampAt(), flightBooking.getState());
     		
     		if(bookings.size() > 1){ //if more then 1 then there is a duplicate
     			removeBookingByEpoch(bookings.get(0).getTransactionTime());
     		}
     	}
     	
     }
     
     public synchronized void removeBookingByEpoch(long epochTime){
     	try {
     		connect();
    		statement.execute("DELETE FROM flightbookings WHERE transaction_epoch = " + epochTime);
     		disconnect();
     	}
     	catch (Exception e) {  
         e.printStackTrace();
         }
     }
 }
