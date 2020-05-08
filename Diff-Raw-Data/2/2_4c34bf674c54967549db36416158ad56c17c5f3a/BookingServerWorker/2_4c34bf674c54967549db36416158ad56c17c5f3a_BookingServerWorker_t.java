 package com.mycompany.reservationsystem.peer.server.booking;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import com.mycompany.reservationsystem.peer.communication.COMMUNICATION_MESSAGES;
 import com.mycompany.reservationsystem.peer.data.FlightBooking;
 import com.mycompany.reservationsystem.peer.data.FlightBookingTable;
 
 /*
  * Booking server worker thread giving known ip address to clients
 
     Begin
     Wait for client to connect
     Client requests for a booking
     Find all known bookings at given point in time
     Send a booking  to client
     Wait for client to ask for another
     Repeat step 5 and 6 until given all bookings are sent
     When all bookings are given send blank message
     Client then disconnects
     End
  */
 public class BookingServerWorker extends Thread{
 	private Socket connection;
 	private ObjectOutputStream out;
 	private ObjectInputStream in;
 	private boolean isFinished;
 	private ArrayList<FlightBooking> bookingList;
 	private int nextBookingIndex = 0;
 	
 	public BookingServerWorker(Socket socket){
 		this.connection = socket;
 		this.isFinished = false;
 		bookingList = new ArrayList<FlightBooking>();
 	}
 	
 	public void run(){
 		FlightBookingTable bookingTable = FlightBookingTable.getInstance();
 		bookingTable.connect();
 		bookingList = bookingTable.getAllBookings();
 		bookingTable.disconnect();
 		
 		try{
 			out = new ObjectOutputStream(connection.getOutputStream());
 			out.flush();
 			in = new ObjectInputStream(connection.getInputStream());
 			
 			String message = null;
 			
 			while(isFinished() == false){
 				message = (String)in.readObject();
 				
 				if(nextBookingIndex >= bookingList.size()){
 					setFinished(true);
 				}
 				
 				if(message.equals(COMMUNICATION_MESSAGES.TRANSACTION_REQUEST.toString()) && isFinished() == false){
 					sendMessage(COMMUNICATION_MESSAGES.TRANSACTION_RESPONSE);
 				}
 			}
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		finally{
 			//Closing connection
 			try{
 				in.close();
 				out.close();
 				connection.close();
 			}
 			catch(IOException ioException){
 				ioException.printStackTrace();
 			}
 		}
 	}
 	
 	//Send message to client
 	private void sendMessage(COMMUNICATION_MESSAGES communicationMessage){
 		if(communicationMessage.toString().equals(COMMUNICATION_MESSAGES.TRANSACTION_RESPONSE.toString())){
 			String message = "";
 			System.out.println(bookingList.get(nextBookingIndex).getEmail());
 			
 			int isFromCity = 0;
 			int isFromCamp = 0;
 			if(bookingList.get(nextBookingIndex).isFromCity()){
 				isFromCity = 1;
 			}
 			
 			if(bookingList.get(nextBookingIndex).isFromCamp()){
 				isFromCamp = 1;
 			}
 			
 			String data = Long.toString(bookingList.get(nextBookingIndex).getTransactionTime()) + "," +
 			bookingList.get(nextBookingIndex).getEmail() + "," + 
 			bookingList.get(nextBookingIndex).getFlightToCityAt() + "," +
 			bookingList.get(nextBookingIndex).getFlightToCampAt() + "," +
 			isFromCity + "," +
 			isFromCamp + "," +
 			Double.toString(bookingList.get(nextBookingIndex).getPrice()) + "," +
 			bookingList.get(nextBookingIndex).getState().toString() + ",";
 			
 			
			message += COMMUNICATION_MESSAGES.TRANSACTION_RESPONSE.toString() + ":" + data;
 			nextBookingIndex++;
 			System.out.println(message);
 			
 			try{
 				out.writeObject(message);
 				out.flush();
 			}
 			catch(IOException ioException){
 				ioException.printStackTrace();
 			}
 		}
 	}
 	
 	public boolean isFinished() {
 		return isFinished;
 	}
 
 	public void setFinished(boolean isFinished) {
 		this.isFinished = isFinished;
 	}
 }
