 package business;
 
 import java.util.Scanner;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import entities.Flight;
 import entities.Passenger;
 import structures.DoublyLinkedList;
 import structures.FifoQueue;
 import structures.SimplyLinkedList;
 
 public class FlightsBusiness {
 	private DoublyLinkedList<Flight> flights;
 	public void setFlightsList(DoublyLinkedList<Flight> flights){
 		this.flights=flights;
 	}
 	public DoublyLinkedList<Flight> getFlightsList(){
 		return flights;
 	}
 	//Dummy method that pre-load some flights
 	public void loadFlights(){
 		Date departureDate=new GregorianCalendar(2011,04,15,18,15).getTime();
 		Date arrivalDate=new GregorianCalendar(2011,04,15,20,30).getTime();
 		Flight flight=new Flight("EZY8567", "Athens", "London", departureDate, arrivalDate, 180.50, "Airbus 320", 100, 1);
 		flights.addTail(flight);
 		
 		departureDate=new GregorianCalendar(2011, 04, 16, 15, 00).getTime();
 		arrivalDate=new GregorianCalendar(2011, 04, 16, 17, 30).getTime();
		flight=new Flight("ABC1234", "London", "Dublin", departureDate, arrivalDate, 100, "Airbus123", 50, 0);
 		flights.addTail(flight);
 		
 		departureDate=new GregorianCalendar(2011, 04, 16, 20, 10).getTime();
 		arrivalDate=new GregorianCalendar(2011, 05, 1, 13, 40).getTime();
		flight=new Flight("DEF5678", "Dublin", "Alexandria", departureDate, arrivalDate, 200.20, "Airbus 450", 200, 0);
 		flights.addTail(flight);
 		
 		departureDate=new GregorianCalendar(2011, 06, 1, 8, 2).getTime();
 		arrivalDate=new GregorianCalendar(2011, 06, 1, 13, 40).getTime();
		flight=new Flight("ERT1234", "Athens", "Rome", departureDate, arrivalDate, 200.20, "Airbus 450", 100, 0);
 		flights.addTail(flight);
 	}
 	//Prints all the flights available
 	public void listFlights(){
 		System.out.println(flights);
 	}
 	//Adds a new flight in the list
 	public void addFlight(){
 		Scanner in=new Scanner(System.in);
 		System.out.println("Flight Code:");
 		String flightCode=in.nextLine();
 		System.out.println("Starting Point:");
 		String startingPoint=in.nextLine();
 		System.out.println("Destination:");
 		String destination=in.nextLine();
 		System.out.println("Departure Time (yyyy:mm:dd:hh:mm)");
 		String depTime=in.nextLine();
 		System.out.println("Arrival Time (yyyy:mm:dd:hh:mm)");
 		String arTime=in.nextLine();
 		System.out.println("Plane Type:");
 		String planeType=in.nextLine();
 		System.out.println("Total Seats:");
 		int totalSeats=in.nextInt();
 		System.out.println("Available Seats:");
 		int availableSeats=in.nextInt();
 		System.out.println("Ticket Price:");
 		double ticketPrice= in.nextDouble();
 		//Split the year,month,day,hour,minute
 		String[] tmpDTime=depTime.split("[:]");
 		String[] tmpATime=arTime.split("[:]");
 		Date departureTime=new GregorianCalendar(Integer.parseInt(tmpDTime[0]), Integer.parseInt(tmpDTime[1])-1, Integer.parseInt(tmpDTime[2]), Integer.parseInt(tmpDTime[3]), Integer.parseInt(tmpDTime[4])).getTime();
 		Date arrivalTime=new GregorianCalendar(Integer.parseInt(tmpATime[0]), Integer.parseInt(tmpATime[1])-1, Integer.parseInt(tmpATime[2]), Integer.parseInt(tmpATime[3]), Integer.parseInt(tmpATime[4])).getTime();
 		Flight newFlight=new Flight(flightCode, startingPoint, destination, departureTime, arrivalTime, ticketPrice, planeType, totalSeats, availableSeats);
 		//Add the new flight to the existing list
 		flights.addTail(newFlight);
 	}
 	//Remove a flight from the list
 	public void removeFlight(){
 		System.out.println("Give flight code:");
 		Scanner in=new Scanner(System.in);
 		String flightCode=in.nextLine();
 		int index=searchForFlightCode(flightCode);
 		flights.removeNode(index);
 	}
 	//A passenger of course can book a flight
 	public int bookFlight(String bookingCode, String flightCode, boolean available){
 		int index=searchForFlightCode(flightCode);
 		//Get flight node
 		Flight flight=flights.getNodeValue(index);
 		int availableSeats=flight.getAvailableSeats();
 		if(available){
 			//There are available seats on the plane
 			//so we add the booking code to the boardedPass list
 			flight.getBoardedPass().addTail(bookingCode);
 			System.out.println("Boarding List: "+flight.getBoardedPass());
 			//Decrease the available seats
 			flight.setAvailableSeats(--availableSeats);
 			return 1;
 		}else{
 			//There aren't available seats on the plane
 			//so we add the booking code to the waiting queue
 			flight.getWaitingPass().enqueue(bookingCode);
 			System.out.println("Waiting queue: "+flight.getWaitingPass());
 			return 0;
 		}
 	}
 	//Check seat availability
 	public boolean checkAvailability(String flightCode){
 		int index=searchForFlightCode(flightCode);
 		//Get flight node
 		Flight flight=flights.getNodeValue(index);
 		int availableSeats=flight.getAvailableSeats();
 		boolean available;
 		if(availableSeats>=1){
 			available=true;
 		}else{
 			available=false;
 		}
 		
 		return available;
 	}
 	public boolean validateFlights(String[] codeFlights){
 		boolean valid=true;
 		if(codeFlights.length==1){
 			valid=true;
 		}else{
 			for(int i=0;i<codeFlights.length-1;i++){
 				int fIndex=searchForFlightCode(codeFlights[i]);
 				int nIndex=searchForFlightCode(codeFlights[i+1]);
 				System.out.println("fIndex: "+fIndex);
 				System.out.println("nIndex: "+nIndex);
 				Flight fFlight=flights.getNodeValue(fIndex);
 				Flight nFlight=flights.getNodeValue(nIndex);
 				Date fArrTime=fFlight.getArrivalTime();
 				String fDest=fFlight.getDestination();
 				Date nDepTime=nFlight.getDepartureTime();
 				String nStart=nFlight.getStartingPoint();
 				long timeDiff=nDepTime.getTime()-fArrTime.getTime();
 				System.out.println("Time Diff: "+timeDiff);
 				System.out.println("fDest: "+fDest);
 				System.out.println("nStart: "+nStart);
 				if((timeDiff>0) && (fDest.equals(nStart))){
 					valid=true;
 				}else{
 					valid=false;
 				}
 			}
 		}
 		return valid;
 	}
 	
 	public void delPendingCode(String bookingID, String flightCode){
 		int index=searchForFlightCode(flightCode);
 		Flight curFlight=flights.getNodeValue(index);
 		FifoQueue<String> waitingQueue=curFlight.getWaitingPass();
 		int queueLength=waitingQueue.getLength();
 		for(int i=0;i<queueLength;i++){
 			if(waitingQueue.getNodeValue(i).equals(bookingID)){
				System.out.println("Just removed pending passenger");
 				waitingQueue.removeNode(i);
 			}
 		}
		curFlight.setWaitingPass(waitingQueue);
 	}
 	//Afairei apo ti boarded list ton epibath kai ftiaxnei mia nea lista
 	//me tous kodikous krathseon pou einai se katastash pending
 	public SimplyLinkedList<String> delBoardedCodePre(String bookingID, SimplyLinkedList<String> bookedFlights){
 		for(int i=0;i<bookedFlights.getLength();i++){
 			int index=searchForFlightCode(bookedFlights.getNodeValue(i));
 			Flight curFlight=flights.getNodeValue(index);
 			System.out.println("Current FLights");
 			System.out.println(curFlight);
 			//Arxika diagrafoume ton xrhsth apo tin boarding list
 			SimplyLinkedList<String> boardingList=curFlight.getBoardedPass();
 			for(int j=0;j<boardingList.getLength();j++){
 				if(boardingList.getNodeValue(j).equals(bookingID)){
 					boardingList.removeNode(j);
 					System.out.println("Passenger just removed from boarding list");
 					int availableSeats=curFlight.getAvailableSeats();
 					availableSeats++;
 					curFlight.setAvailableSeats(availableSeats);
 					break;
 				}
 			}
 		}
 		//Gia ka8e ena xrhsth sthn oura anamonhs koitazo an oi pthseis
 		//sthn bookedFlight tou exoun dia8esimes 8eseis.
 		
 		//Kodikoi krathshs
 		int index=searchForFlightCode(bookedFlights.getNodeValue(0));
 		Flight curFlight=flights.getNodeValue(index);
 		FifoQueue<String> waitingQueue=curFlight.getWaitingPass();
 		SimplyLinkedList<String> queueCodes=new SimplyLinkedList<String>();
 		if(waitingQueue.getLength()>0){
 			for(int i=0;i<waitingQueue.getLength();i++){
 				queueCodes.addTail(waitingQueue.getNodeValue(i));
 			}
 		}
 		
 		return queueCodes;
 	}
 	
 	public String delBoardedCodePost(SimplyLinkedList<Passenger> queuePassengers){
 		int availableSeats;
 		//Gia ka8e mia pthsh au3anetai to available flag
 		//an yparxoun dia8esimes 8eseis
 		int availableFlag=0;
 		//Kodikos krathshs tou tuxairou epivath
 		String luckyBookingCode=null;
 		for(int i=0;i<queuePassengers.getLength();i++){
 			SimplyLinkedList<String> passBookedFlights=queuePassengers.getNodeValue(i).getBookedFlights();
 			for(int j=0;j<passBookedFlights.getLength();j++){
 				int index=searchForFlightCode(passBookedFlights.getNodeValue(j));
 				Flight curFlight=flights.getNodeValue(index);
 				availableSeats=curFlight.getAvailableSeats();
 				if(availableSeats>0)
 					availableFlag++;
 			}
 			if(availableFlag==passBookedFlights.getLength()){
 				//Tuxairos epivaths
 				luckyBookingCode=queuePassengers.getNodeValue(i).getUid();
 				//Gia ka8e mia pthsh sto bookedFlight kane krathsh
 				for(int j=0;j<passBookedFlights.getLength();j++){
 					String flightCode=passBookedFlights.getNodeValue(j);
 					int index=searchForFlightCode(flightCode);
 					Flight curFlight=flights.getNodeValue(index);
 					FifoQueue<String> pendingList=curFlight.getWaitingPass();
 					for(int k=0;k<pendingList.getLength();k++){
 						if(pendingList.getNodeValue(k).equals(luckyBookingCode))
 							pendingList.removeNode(k);
 					}
 					bookFlight(luckyBookingCode, flightCode, true);
 				}
 				break;
 			}
 		}
 		
 		return luckyBookingCode;
 	}
 	//Search for a flight code (flight) and return its position to the flight list
 	public int searchForFlightCode(String flightCode){
 		Flight searchFlight;
 		int index;
 		boolean found=false;
 		int listLength=flights.getLength();
 		
 		for(index=1;index<=listLength;index++){
 			searchFlight=flights.getNodeValue(index);
 			if(searchFlight.getFlightCode().equals(flightCode)){
 				found=true;
 				break;
 			}
 		}
 		if(found==false)
 			index=-1;
 		
 		return index;
 	}
 }
