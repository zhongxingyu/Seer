 package entities;
 
 import java.util.Date;
 import structures.FifoQueue;
 import structures.SimplyLinkedList;
 
 public class Flight {
 	private String flightCode;
 	private String startingPoint;
 	private String destination;
 	private Date departureTime;
 	private Date arrivalTime;
 	private double ticketPrice;
 	private String planeType;
 	private int totalSeats;
 	private int availableSeats;
 	private SimplyLinkedList<String> boardedPass;
 	private FifoQueue<String> waitingPass;
 	
 	public Flight(String flightCode, String startingPoint, String destination, Date departureTime,
 			Date arrivalTime, double ticketPrice, String planeType, int totalSeats, int availableSeats){
 		this.flightCode=flightCode;
 		this.startingPoint=startingPoint;
 		this.destination=destination;
 		this.departureTime=departureTime;
 		this.arrivalTime=arrivalTime;
 		this.ticketPrice=ticketPrice;
 		this.planeType=planeType;
 		this.totalSeats=totalSeats;
 		this.availableSeats=availableSeats;
 		boardedPass=new SimplyLinkedList<String>();
 		waitingPass=new FifoQueue<String>();
 	}
 	
 	public String getFlightCode(){
 		return flightCode;
 	}
 	public String getStartingPoint(){
 		return startingPoint;
 	}
 	public String getDestination(){
 		return destination;
 	}
 	public Date getDepartureTime(){
 		return departureTime;
 	}
 	public Date getArrivalTime(){
 		return arrivalTime;
 	}
 	public double getTicketprice(){
 		return ticketPrice;
 	}
 	public String getPlaneType(){
 		return planeType;
 	}
 	public int getTotalSeats(){
 		return totalSeats;
 	}
 	public int getAvailableSeats(){
 		return availableSeats;
 	}
 	public SimplyLinkedList<String> getBoardedPass(){
 		return boardedPass;
 	}
 	public FifoQueue<String> getWaitingPass(){
 		return waitingPass;
 	}
 	public void setBoardedPass(String bookingCode){
 		boardedPass.addTail(bookingCode);
 	}
 	public void setWaitingPass(String bookingCode){
 		waitingPass.enqueue(bookingCode);
 	}
 	public void setAvailableSeats(int availableSeats){
 		this.availableSeats=availableSeats;
 	}
 	@Override
 	//Print a flight's details
 	public String toString(){
 		StringBuilder sb=new StringBuilder();
 		sb.append("\n");
 		sb.append("--------------------").append("\n");
 		sb.append("Flight Code: ").append(flightCode).append("\n");
 		sb.append("Starting Point: ").append(startingPoint).append("\n");
 		sb.append("Destination: ").append(destination).append("\n");
 		sb.append("Departure Time: ").append(departureTime).append("\n");
 		sb.append("Arrival Time: ").append(arrivalTime).append("\n");
 		sb.append("Ticket Price: ").append(ticketPrice).append("\n");
 		sb.append("Plane Type: ").append(planeType).append("\n");
 		sb.append("Total Seats: ").append(totalSeats).append("\n");
 		sb.append("Available Seats: ").append(availableSeats);
 		if(waitingPass.getLength()>0){
 			sb.append("\n");
 			sb.append("Pending Seats: ").append(waitingPass.getLength()).append("\n");
 		}
 		
 		return sb.toString();
 	}
 }
