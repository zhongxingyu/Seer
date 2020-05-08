 package com.aerodynelabs.habtk.connectors.parsers;
 
 import java.util.Calendar;
 import java.util.Scanner;
 import java.util.TimeZone;
 
 public class APRSPacket {
 	
 	String packet = "";
 	String fromCall = "";
 	String toCall = "";
 	String payload = "";
 	String comment = "";
 	char symbolTable;
 	char symbolSymbol;
 	long timestamp;
 	double latitude;
 	double longitude;
 	double altitude;
 	boolean validPos = false;
 	
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
 	
 	public APRSPacket(String from, String to, String payload) {
 		packet = from + ">" + to + ":" + payload;
 	}
 	
 	public APRSPacket(String pkt) {
 		int s = 0;
 		// Get source
 		for(int i = s; i < pkt.length(); i++) {
 			char c = pkt.charAt(i);
 			if(c != '>') {
 				fromCall += c;
 			} else {
 				s = i + 1;
 				break;
 			}
 		}
 		// Get destination
 		for(int i = s; i < pkt.length(); i++) {
 			char c = pkt.charAt(i);
 			if(c != ':') {
 				toCall += c;
 			} else {
 				s = i + 1;
 				break;
 			}
 		}
 		// Get payload
 		payload = pkt.substring(s);
 		s = 1;
 		// Parse payload
 		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 		switch(payload.charAt(0)) {
 			case ':':	// Message
 				// XXX message
 				break;
 			case '>':	// Status
 				// XXX status
 				break;
 			case 'T':	// Telemetry data
 				// XXX telemetry
 				break;
 			case 0x1c:	// Current mic-e
 			case '`':	// Current mic-e
 			case '\'':	// Old mic-e or new TM-D700
 			case 0x1d:	// Old mic-e
 				try {
 					// Parse destination field
 					// Decode latitude
 					int dLat[] = new int[6];
 					for(int i = 0; i < 6; i++) {
 						dLat[i] = decodeLat(toCall.charAt(i));
 						if(dLat[i] < 0) return;
 					}
 					latitude = dLat[0] * 10 + dLat[1];
 					latitude += ((dLat[2] * 10.0) + dLat[3] + (dLat[4] / 10.0) + (dLat[5] / 100.0)) / 60.0;
 					int NS = decodeHemisphere(toCall.charAt(3));
 					if(NS == 0) return;
 					if(NS < 0) latitude = -latitude;
 					int lonOffset = decodeHemisphere(toCall.charAt(4));
 					int WE = -decodeHemisphere(toCall.charAt(5));
 					if(lonOffset == 0 || WE == 0) return;
 					if(lonOffset < 0) {
 						lonOffset = 0;
 					} else if(lonOffset > 0) {
 						lonOffset = 100;
 					}
 
 					// Parse information field
 					longitude = decodeLonDeg(payload.charAt(1), lonOffset);
 					if(longitude == -1) return;
 					int min = decodeLonMin(payload.charAt(2));
 					if(min == -1) return;
 					int hun = decodeLonHun(payload.charAt(3));
 					if(hun == -1) return;
 					longitude += (min + (hun / 100.0)) / 60.0;
 					if(WE < 0) longitude = -longitude;
 
 					// XXX decode mic-e spd/cse
 
 					// XXX decode mic-e telemetry
 
 					symbolSymbol = payload.charAt(7);
 					symbolTable = payload.charAt(8);
 					comment = payload.substring(9);
 					char typeCode = comment.charAt(0);
 					if(typeCode == ' ' ||
 							typeCode == '>' ||
 							typeCode == ']' ||
 							typeCode == '\'' ||
 							typeCode == '`') comment = comment.substring(1);
 					// Decode altitude if possible
 					if(comment.charAt(3) == '}') {
 						int a = comment.charAt(0) - 33;
 						int b = comment.charAt(1) - 33;
 						int c = comment.charAt(2) - 33;
 						altitude = (a*91*91 + b*91 + c) - 10000;
 					} else {
 						Scanner scan = new Scanner(comment);
 						String tAlt = scan.findInLine("/A=[0-9]{6}");
 						if(tAlt != null) altitude = Integer.parseInt(tAlt.substring(3)) * 0.3048;
 					}
 					
 					timestamp = cal.getTimeInMillis();
 					validPos = true;
 				} catch(Exception e) {
 					validPos = false;
 				}
 				
 				break;
 			case '/':	// Position, w/ timestamp, w/o messaging
 			case '@':	// Position, w/ timestamp, w/ messaging
 				String time = "";
 				int stop = s;
 				for(int i = s; i < stop + 8; i++) {
 					s++;
 					time += payload.charAt(i);
 					if(!Character.isDigit(payload.charAt(i))) break;
 				}
 				char timeType = time.charAt(time.length() - 1);
 				switch(timeType) {
 					case '/':	// DDHHmm local
 						cal.setTimeZone(TimeZone.getDefault());
 						// resume parsing time as below
 					case 'z':	// DDHHmm zulu
 						cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(time.substring(0, 2)));
 						cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(2, 4)));
 						cal.set(Calendar.MINUTE, Integer.parseInt(time.substring(4, 6)));
 //						cal.set(Calendar.SECOND, 0);
 //						cal.set(Calendar.MILLISECOND, 0);
 						break;
 					case 'h':	// HHmmss zulu
 						cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
 						cal.set(Calendar.MINUTE, Integer.parseInt(time.substring(2, 4)));
 						cal.set(Calendar.SECOND, Integer.parseInt(time.substring(4, 6)));
 //						cal.set(Calendar.MILLISECOND, 0);
 						break;
 					default:	// MMDDHHmm zulu
 						cal.set(Calendar.MONTH, Integer.parseInt(time.substring(0, 2)));
 						cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(time.substring(2, 4)));
 						cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(4, 6)));
 						cal.set(Calendar.MINUTE, Integer.parseInt(time.substring(6)));
 //						cal.set(Calendar.SECOND, 0);
 //						cal.set(Calendar.MILLISECOND, 0);
 						break;
 				}
 				// Resume parsing position below
 			case '!':	// Position, w/o timestamp, w/o messaging
 			case '=':	// Position, w/o timestamp, w/ messaging
 				// Timestamp
 				timestamp = cal.getTimeInMillis();
 				Scanner scanner = new Scanner(payload.substring(s));
 				
 				// Read position info
 				// Test for compressed/uncompressed format
 				if(Character.isDigit(payload.charAt(1))) {	// Uncompressed position format
 					try {
 						String rLat = scanner.findInLine("[0-9.]+[NS]");
 						symbolTable = scanner.findInLine(".").charAt(0);
 						String rLon = scanner.findInLine("[0-9.]+[EW]");
 						symbolSymbol = scanner.findInLine(".").charAt(0);
 						// Parse lat
 						latitude = Integer.parseInt(rLat.substring(0, 2));
 						latitude += Double.parseDouble(rLat.substring(2, rLat.length() - 2)) / 60.0;
 						if(rLat.charAt(rLat.length() - 1) == 'S') latitude = -latitude;
 						// Parse lon
 						longitude = Integer.parseInt(rLon.substring(0, 3));
 						longitude += Double.parseDouble(rLon.substring(3, rLon.length() - 2)) / 60.0;
 						if(rLon.charAt(rLon.length() - 1) == 'W') longitude = -longitude;
 						
 						// Save remaining payload to reinit scanners
 						// Remaining fields can occur anywhere
 						comment = scanner.nextLine();
 
 						// Parse altitude if possible
 						scanner = new Scanner(comment);
 						String rAlt = scanner.findInLine("/A=[0-9]{6}");
 						if(rAlt != null) {
 							altitude = Integer.parseInt(rAlt.substring(3)) * 0.3048;
 						}
 
 						validPos = true;
 					} catch(Exception e) {
 						validPos = false;
 					}
 				} else {	// Compressed position format
 					try {
 						String position = payload.substring(1, 14);
 						// Split fields
 						symbolTable = position.charAt(0);
 						symbolSymbol = position.charAt(9);
 						String cLat = position.substring(1, 5);
 						String cLon = position.substring(5, 9);
 						String cExt = position.substring(10, 12);
 						char cType = position.charAt(12);
 						// Uncompress lat/lon
 						latitude = 90.0 - ((
 								(cLat.charAt(0) - 33) * 753571 +
 								(cLat.charAt(1) - 33) * 8281 +
 								(cLat.charAt(2) - 33) * 91 +
 								(cLat.charAt(3) - 33)
 								) / 380926.0);
 						longitude = -180.0 + ((
 								(cLon.charAt(0) - 33) * 753571 +
 								(cLon.charAt(1) - 33) * 8281 +
 								(cLon.charAt(2) - 33) * 91 +
 								(cLon.charAt(3) - 33)
 								) / 190463.0);
 						if(!cExt.equals("  ")) {
 							if(((byte)cType & 0x18) == 0x10) {
 								int e = (cExt.charAt(0) - 33) * 91 + (cExt.charAt(1) - 33);
 								altitude = (int)(Math.pow(1.002, e) + 0.5);
 							}
 						}
						
 						comment = payload.substring(14);
 						validPos = true;
 					} catch(Exception e) {
 						validPos = false;
 					}
 				}
 				
 				// Parse extensions
 				// TODO parse aprs extensions (APRS Spec p.27)
 				
 				break;
 		}
 	}
 	
 	private int decodeLonHun(char c) {
 		int h = c - 28;
 		if(h >= 0 && h < 100) return h;
 		return -1;
 	}
 	
 	private int decodeLonMin(char c) {
 		int m = c - 28;
 		if(m >= 60) m = m - 60;
 		if(m >= 0 && m < 60) return m;
 		return -1;
 	}
 	
 	private int decodeLonDeg(char c, int offset) {
 		int d = c - 28;
 		d += offset;
 		if(d >= 180 && d <= 189) d = d - 80;
 		if(d >= 190 && d <= 199) d = d - 190;
 		if(d >= 0 && d < 180) return d;
 		return -1;
 	}
 	
 	private int decodeHemisphere(char c) {
 		switch(c) {
 			case '0':
 			case '1':
 			case '2':
 			case '3':
 			case '4':
 			case '5':
 			case '6':
 			case '7':
 			case '8':
 			case '9':
 			case 'L':
 				return -1;
 			case 'P':
 			case 'Q':
 			case 'R':
 			case 'S':
 			case 'T':
 			case 'U':
 			case 'V':
 			case 'W':
 			case 'X':
 			case 'Y':
 			case 'Z':
 				return 1;
 		}
 		return 0;		
 	}
 	
 	private int decodeLat(char c) {
 		switch(c) {
 			case '0':
 			case 'A':
 			case 'P':
 				return 0;
 			case '1':
 			case 'B':
 			case 'Q':
 				return 1;
 			case '2':
 			case 'C':
 			case 'R':
 				return 2;
 			case '3':
 			case 'D':
 			case 'S':
 				return 3;
 			case '4':
 			case 'E':
 			case 'T':
 				return 4;
 			case '5':
 			case 'F':
 			case 'U':
 				return 5;
 			case '6':
 			case 'G':
 			case 'V':
 				return 6;
 			case '7':
 			case 'H':
 			case 'W':
 				return 7;
 			case '8':
 			case 'I':
 			case 'X':
 				return 8;
 			case '9':
 			case 'J':
 			case 'Y':
 				return 9;
 		}
 		return -1;
 	}
 	
 	public String getComment() {
 		return comment;
 	}
 	
 	public String getPayload() {
 		return payload;
 	}
 	
 	public boolean isPosition() {
 		return validPos;
 	}
 	
 	public String getFrom() {
 		return fromCall;
 	}
 	
 	public String getTo() {
 		return toCall;
 	}
 	
 	public long getTimestamp() {
 		return timestamp;
 	}
 	
 	public double getLatitude() {
 		return latitude;
 	}
 	
 	public double getLongitude() {
 		return longitude;
 	}
 	
 	public double getAltitude() {
 		return altitude;
 	}
 	
 	public String getPacket() {
 		return packet;
 	}
 	
 	public String toString() {
 		String out = 
 				"Source: " + fromCall + "\n" +
 				"Dest: " + toCall + "\n" +
 				"Payload: " + payload;
 		return out;
 	}
 
 }
