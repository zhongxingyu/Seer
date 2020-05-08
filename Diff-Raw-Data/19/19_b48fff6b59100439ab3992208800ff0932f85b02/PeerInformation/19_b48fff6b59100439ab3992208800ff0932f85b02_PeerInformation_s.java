 package common;
 import java.io.*;
 import java.net.*;
 
 public class PeerInformation implements Serializable {
 	private static final long serialVersionUID = 3667108226485766929L;
	public static final String DEFAULT_ID = "something unique";
 
 	public InetAddress address;
 	public int port;
 	public PeerCoordinates location;
 	public String id;	
 	
 	public PeerInformation() {
 		id = DEFAULT_ID;
 	}
 
 	public PeerInformation(PeerInformation other) {
 		this(other.address, other.port, other.location);
 	}
 	
 	public PeerInformation(InetAddress address, int port, PeerCoordinates location) {
 		this();
 		this.address = address;
 		this.port = port;
 		this.location = location;
 	}
 	
 	public String toString() {
 		return address + ":" + port + " @ " + location;
 	}
 
 	public boolean equals(Object other) {
 		if (!(other instanceof PeerInformation))
 			return false;
 		
 		return location.equals(((PeerInformation)other).location);
 	}
 }
