 package business;
 
 import java.util.Scanner;
 
 import entities.*;
 import structures.*;
 
 public class Main {
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//Flights list
 		DoublyLinkedList<Flight> flights=new DoublyLinkedList<Flight>();
 		FlightsBusiness flightB=new FlightsBusiness();
 		flightB.setFlightsList(flights);
 		flightB.loadFlights();
 		//Passengers list
 		DoublyLinkedList<Passenger> passengers=new DoublyLinkedList<Passenger>();
 		PassengerBusiness passB=new PassengerBusiness();
 		passB.setPassengersList(passengers);
 		
 		Scanner inM=new Scanner(System.in);
 		boolean running=true;
 		System.out.println("Welcome to the airline booking system");
 		
 		while(running){
 			System.out.println("Please make your choice");
 			System.out.println("<--------------------->");
 			System.out.println("1 -- View available flights");
 			System.out.println("2 -- Add Flight");
 			System.out.println("3 -- Delete Flight");
 			System.out.println("4 -- Add Passenger");
 			System.out.println("5 -- List Passengers");
 			System.out.println("6 -- Delete Passenger");
 			System.out.println("0 -- Exit");
 			int choice=inM.nextInt();
 
 			switch (choice) {
 			case 1:
 				flightB.listFlights();
 				break;
 			case 2:
 				flightB.addFlight();
 				break;
 			case 3:
 				flightB.removeFlight();
 				break;
 			case 4:
 				//Add the user to the passengers list
 				String[] codeFlights=passB.askAddPassenger();
 				boolean valid=flightB.validateFlights(codeFlights);
 				if(valid){
 					//Add to the passenger list
 					String bookingID=passB.addPassenger(codeFlights);
 					//Add the booking ID to the passenger list or the waiting
 					//queue of a specific flight
 					//That block of code wasn't written by me, but it works!
 					int index=passB.searchForCode(bookingID);
 					Passenger newPassenger=passengers.getNodeValue(index);
 					int listLength=newPassenger.getBookedFlights().getLength();
 					boolean available=true;
 					for(int i=0;i<listLength;i++){
 						available=flightB.checkAvailability(newPassenger.getBookedFlights().getNodeValue(i));
 						if(available==false)
 							break;
 					}
 					for(int i=0;i<listLength;i++){
 						String flightID=newPassenger.getBookedFlights().getNodeValue(i);
 						System.out.println("Availability: "+available);
 						flightB.bookFlight(bookingID, flightID, available);
 					}
 					if(available){
 						newPassenger.setStatus(true);
 						System.out.println("Your booking was successful");
 					}else{
 						newPassenger.setStatus(false);
 						System.out.println("There were no available seats. You've been " +
 						"placed to the waiting queue");
 					}
 				}else{
 					System.out.println("The flights' details are incorrect");
 				}
 				break;
 			case 5:
 				passB.listPassengers();
 				break;
 			case 6:
 				String bookingID=passB.askRemovePassenger();
 				System.out.println("bookingID: "+bookingID);
 				int index=passB.searchForCode(bookingID);
 				Passenger delPassenger=passengers.getNodeValue(index);
 				SimplyLinkedList<String> bookedFlights=delPassenger.getBookedFlights();
 				int listLength=delPassenger.getBookedFlights().getLength();
 				boolean status=delPassenger.getStatus();
 				System.out.println("Passenger Status: "+status);					
 					if(status==false){
 						for(int i=0;i<listLength;i++){
 							String flightCode=delPassenger.getBookedFlights().getNodeValue(i);
 							flightB.delPendingCode(bookingID, flightCode);
 						}
						passB.removePassenger(bookingID);
 					}else{
 						SimplyLinkedList<String> queueCodes=flightB.delBoardedCodePre(bookingID, bookedFlights);
 						//Na ftia3o mia lista me ta objects pou antistoixoun sta queueCodes
 						SimplyLinkedList<Passenger> queuePassengers=new SimplyLinkedList<Passenger>();
 						for(int j=0;j<queueCodes.getLength();j++){
 							index=passB.searchForCode(queueCodes.getNodeValue(j));
 							System.out.println("Passengers length: "+passengers.getLength());
 							System.out.println("Index: "+index);
 							System.out.println("queueCode Value: "+queueCodes.getNodeValue(j));
 							System.out.println(passengers);
 							queuePassengers.addTail(passengers.getNodeValue(index));
 						}
						System.out.println("queuePassengers Length: "+queuePassengers.getLength());
 						if(queuePassengers!=null){
 							String luckyBookingCode=flightB.delBoardedCodePost(queuePassengers);
 							if(luckyBookingCode!=null){
 								int indexb=passB.searchForCode(luckyBookingCode);
 								//Set new status to true aka boarding
 								passengers.getNodeValue(indexb).setStatus(true);
 							}
 							passB.removePassenger(bookingID);
 						}
 					}
 				break;
 			case 0:
 				running=false;
 				break;
 			default:
 				flightB.listFlights();
 				break;
 			}
 		}
 	}
 }
