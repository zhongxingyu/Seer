 import java.net.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.io.*;
 
 public class UDP extends Thread {
 	private final static int c = 30; //number of seats in the theater
 	private final static int port = 8800;
 	private final static int len = 1024; //just following his example
 	
 	private static boolean[] seatOccupied;
 	private static int currentAvailable;
 	private static DatagramSocket socket;
 	private static InetAddress address;
 	private static int outport;
 	
 	private static String receiveMessage;
 	private static HashMap<String,ArrayList<Integer>> names;
 	
 	public UDP() {
 		//seatOccupied = new boolean[c];
 		//currentCount = 0;
 	}
 	
 	
 	
 	public static void main(String[] args) throws Exception {
 		seatOccupied = new boolean[c]; //should all be initialized to false
 		currentAvailable = c;
 		socket = new DatagramSocket(port);
 		names = new HashMap<String, ArrayList<Integer>>();
 		
		
 		
 		while(true) {
			byte[] receiveBuffer = new byte[len];
 			DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
 			socket.receive(receivePacket);
 			address = receivePacket.getAddress();
 			outport = receivePacket.getPort();
 			receiveMessage = new String(receivePacket.getData());
 			
			//System.out.println(receiveMessage);
 			UDP t = new UDP();
 			t.start();
 			
 		}
 	}
 	
 	//************************************************************************
 	
 	public static String selectRequest(String message) {
 		String[] parts = message.split(" ");
 		
 		if(parts[0].equals("reserve"))
 			return reserve(message);
 		else if(parts[0].equals("search"))
 			return search(message);
 		else if(parts[0].equals("delete"))
 			return delete(message);
 		
 		return "hello";
 	}
 	
 	public static synchronized String reserve(String message) {
		String[] parts = message.split("\\W+");
 		
 		//name exists?
 		if(names.containsKey(parts[1]))
 			return ("Seats already booked against the name provided.");
 		
 		int seats = Integer.parseInt(parts[2]);
 		
 		//not enough seats?
 		if(currentAvailable < seats)
 			return ("Seats not available.");
 		
 		//assign new reservation
 		currentAvailable = currentAvailable - seats;
 		int tempAssign = seats;
 		ArrayList<Integer> assignSeats = new ArrayList<Integer>();
 		for(int i = 0 ; i < c ; i++) {
 			if(!seatOccupied[i]) {
 				seatOccupied[i] = true;
 				assignSeats.add(i+1);
 				seats--;
 				
 				if(seats == 0)
 					break;
 			}
 		}
 		Collections.sort(assignSeats);
 		names.put(parts[1], assignSeats); //add new entry to hashmap, associate seats to name
 		 
 		String ret = "Seat assigned to you are";
 		int temp = tempAssign;
 		for(Integer i : assignSeats) {
 			ret = ret + " " + i;
 			
 			temp--;
 			if(temp > 0)
 				ret = ret + ",";
 			else if(temp == 0)
 				ret = ret + ".";
 		}
 		
 		return ret;
 	}
 	
 	public static synchronized String search(String message) {
		String[] parts = message.split("\\W+");
 		
 		//name not found?
 		if(!names.containsKey(parts[1]))
 			return ("No reservation found for " + parts[1]);
 		
 		//name exists, return seat numbers.
 		ArrayList<Integer> curSeats = names.get(parts[1]);
 		String ret = "Seat assigned to you are";
 		int temp = curSeats.size();
 		for(Integer i : curSeats) {
 			ret = ret + " " + i;
 			
 			temp--;
 			if(temp > 0)
 				ret = ret + ",";
 			else if(temp == 0)
 				ret = ret + ".";
 		}
 		return ret;
 	}
 	
 	public static synchronized String delete(String message) {
		String[] parts = message.split("\\W+");
 		
 		//name not found?
 		if(!names.containsKey(parts[1]))
 			return ("No reservation found for " + parts[1]);
 		
 		//name exists, gotta delete!
 		ArrayList<Integer> curSeats = names.get(parts[1]);
 		currentAvailable = currentAvailable + curSeats.size();
 		
 		String ret = "Freed up " + curSeats.size() + " seats:";
 		int temp = curSeats.size();
 		for(Integer i : curSeats) {
			seatOccupied[i-1] = false;
 			ret = ret + " " + i;
 			
 			temp--;
 			if(temp > 0)
 				ret = ret + ",";
 			else if(temp == 0)
 				ret = ret + ".";
 		}
 		names.remove(parts[1]); //delete name
 		
 		return ret;
 	}
 	
 	public static synchronized void sendPacket(String message) throws IOException {
 		byte[] sendBuffer = new byte[len];
 		sendBuffer = message.getBytes();
 		DatagramPacket returnPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, outport);
 		socket.send(returnPacket);
 	}
 
 	//************************************************************************
 	
 	public void run() {
 		String str = selectRequest(receiveMessage);
 		try {
 			sendPacket(str);
 		} catch (Exception e) {
 			
 		}
 	}
 }
