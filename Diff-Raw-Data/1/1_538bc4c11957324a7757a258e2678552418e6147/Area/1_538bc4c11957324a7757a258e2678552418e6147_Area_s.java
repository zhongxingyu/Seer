 package base;
 import java.util.HashMap;
 import java.util.Collection;
 
 public class Area {
 	static final int DEFAULT_DIMENSION = 300;
 	static final int SEAT_LIMIT = 1000;
 	
 	int seats;
 
 	int key;
 	String name;
 	int width, height;
 
 	HashMap<Integer,Seat> seatMap;
 
 	public Area(Integer key, String name) {
 		this(key, name, DEFAULT_DIMENSION, DEFAULT_DIMENSION);
 	}
 	
 	public Area(Integer key, String name, Integer width, Integer height) {
 		seatMap = new HashMap<Integer,Seat>();
 		this.key = key;
 		this.name = name;
 		this.width = width;
 		this.height = height;
 		this.seats = 0;
 	}
 	
 	public Seat findEmptySeat() {
 		Seat result = null;
 		Collection<Seat> smValues = seatMap.values();
 		for (Seat s : smValues) {
 			if(!s.isOccupied()) {
 				result = s;
 				break;
 			}
 		}
 		return result;
 	}
 	
 	/**
 	*  Returns the key of the added seat.
 	*/
 	public int addSeat(int x, int y) {
 		int result = seats;
 		seatMap.put(seats, new Seat(x,y));
 		seats += 1;
 		return result;
 	}
 
 	public void removeSeat(int key) {
 		seatMap.remove(key);
 	}
 	
 	public Integer getWidth() {
 		return width;
 	}
 	public Integer getHeight() {
 		return height;
 	}
 	public Integer getKey() {
 		return key;
 	}
 	public String toString() {
 		return key + ": " + name;
 	}
 }
