 package com.aerodynelabs.habtk.connectors.parsers;
 
import java.util.Scanner;

 public class APRSPacket {
 	
 	String packet;
 	String fromCall;
 	String toCall;
 	String timestamp;
 	double latitude;
 	double longitude;
 	int altitude;
 	boolean validPos = true;
 	
 	public static final int SSID_DOT = 0;
 	public static final int SSID_AMBULENCE = 1;
 	public static final int SSID_BUS = 2;
 	public static final int SSID_FIRETRUCK = 3;
 	public static final int SSID_BIKE = 4;
 	public static final int SSID_YACHT = 5;
 	public static final int SSID_HELO = 6;
 	public static final int SSID_AIRCRAFT = 7;
 	public static final int SSID_SHIP = 8;
 	public static final int SSID_CAR = 9;
 	public static final int SSID_MOTORCYCLE = 10;
 	public static final int SSID_BALLOON = 11;
 	public static final int SSID_JEEP = 12;
 	public static final int SSID_RV = 13;
 	public static final int SSID_TRUCK = 14;
 	public static final int SSID_VAN = 15;
 	
 	public APRSPacket(String from, String to) {
 		//TODO parse fields to packet
 		packet = from + ">" + to;
 	}
 	
 	public APRSPacket(String pkt) {
 		// Get data type ID
 		//TODO detect type of aprs packet
 	}
 	
 	private boolean parsePosition() {
 		//TODO parse standard aprs packet
 		return true;
 	}
 	
 	private boolean parseMicPosition() {
 		//TODO parse mic-e packet
 		return true;
 	}
 	
 	public boolean isPosition() {
 		if(fromCall == null) return false;
 		if(validPos == false) return false;
 		//TODO test position packet validity
 		return true;
 	}
 	
 	public String getFrom() {
 		return fromCall;
 	}
 	
 	public String getTo() {
 		return toCall;
 	}
 	
 	public String getTimestamp() {
 		return timestamp;
 	}
 	
 	public double getLatitude() {
 		return latitude;
 	}
 	
 	public double getLongitude() {
 		return longitude;
 	}
 	
 	public int getAltitude() {
 		return altitude;
 	}
 	
 	public String getPacket() {
 		return packet;
 	}
 	
 	public String toString() {
 		return packet;
 	}
 
 }
