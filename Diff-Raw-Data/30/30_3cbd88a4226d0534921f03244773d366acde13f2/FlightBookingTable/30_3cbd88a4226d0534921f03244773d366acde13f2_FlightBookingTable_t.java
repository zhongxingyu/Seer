 package com.mycompany.reservationsystem.peer.data;
 
 import java.util.ArrayList;
 
 public class FlightBookingTable extends Database{
 	private static FlightBookingTable instance = null;
 	
     private FlightBookingTable(){
     	
     }
     
     public static FlightBookingTable getInstance(){
     	if(instance != null){
     		return instance;
     	}
     	else{
     		instance = new FlightBookingTable();
     		return instance;
     	}
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
 }
