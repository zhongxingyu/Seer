 package com.mycompany.reservationsystem.peer.daemon;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.mycompany.reservationsystem.peer.data.Database;
 import com.mycompany.reservationsystem.peer.data.FlightBooking;
 import com.mycompany.reservationsystem.peer.data.PropertyFile;
 
 public class TransactionDaemon extends Thread {
 	private final int EPOCH_MINUTE = 60;
 	
 	public void run(){
 		while(true){
 			try {
 				sleep(60000 * PropertyFile.getInstance().getExecuteTranactionDaemonTime());
 			} 
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
			long currentEpoch = new Date().getTime();
			long period = PropertyFile.getInstance().getTransactionTimePeriod() * EPOCH_MINUTE;
			long startOfPeriod = currentEpoch - (period*2);
			long endOfPeriod = currentEpoch - period;
 			
 			ArrayList<FlightBooking> timePeriodBookings = Database.getInstance().findBooking(startOfPeriod, endOfPeriod);
 			
 			ArrayList<FlightBooking> cancelTransactions = getTransactionsByState(timePeriodBookings, FlightBooking.STATE.CANCEL);
 			
 			//Do CANCEL transactions first
 			processCancelTransactions(cancelTransactions);
 			
 			//TODO transactions to commit
 			
			
 		}
 	}
 	
 	private ArrayList<FlightBooking> getTransactionsByState(ArrayList<FlightBooking> timePeriodBookings, FlightBooking.STATE state){
 		ArrayList<FlightBooking> bookings = new ArrayList<FlightBooking>();
 		
 		for(FlightBooking flightBooking : timePeriodBookings){
 			if(flightBooking.getState().toString().equals(state.toString())){
 				bookings.add(flightBooking);
 			}
 		}
 		
 		return bookings;
 	}
 	
 	private void processCancelTransactions(ArrayList<FlightBooking> cancelTransactions){
 		//For each cancel transaction
 		for(FlightBooking flightBooking : cancelTransactions){
 			//Check to see if there is a confirmed transaction
 			ArrayList<FlightBooking> confirmedBooking = Database.getInstance().findBooking(flightBooking.getEmail(), flightBooking.getFlightToCityAt(), 
					flightBooking.getFlightToCampAt(), flightBooking.getState());
 			
 			//If there are no confirmed transaction, then cannot cancel a transaction that has not been confirmed
 			if(confirmedBooking.size() == 0){ 
 				flightBooking.setTransactionTime(new Date().getTime());
 				flightBooking.setState(FlightBooking.STATE.CANCEL_REJECTED);
 			}
 			//Else there is a confirmed booking, cancel the confirmed transaction
 			else{ 
 				flightBooking.setTransactionTime(new Date().getTime());
 				flightBooking.setState(FlightBooking.STATE.CANCELED);
 			} 
 			Database.getInstance().addBooking(flightBooking);
 		}
 	}
 }
