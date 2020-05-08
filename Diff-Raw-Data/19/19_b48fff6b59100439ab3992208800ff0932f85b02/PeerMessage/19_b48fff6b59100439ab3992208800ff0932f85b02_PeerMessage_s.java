 package common;
 import java.io.*;
 import java.util.*;
 
 
 public class PeerMessage implements Serializable {
 	private static final long serialVersionUID = 3667108226485766929L;
	public static final String DEFAULT_ID = "TesseractProject"; 
 	
 	public enum Type {
 		JOIN, JOIN_RESULT, ADD_PEERS, REMOVE_PEERS, PAYLOAD, EXTRA;
 	}
 
 	public Type type;
 	public PeerInformation sender;
 	public PeerCoordinates location;
 	public ArrayList<PeerInformation> peers;
 	public CollidableObject payload;
 	public Object extra;
	public String id;
 
 	public PeerMessage(Type type, PeerInformation sender) {
 		this.type = type;
 		this.sender = sender;
 		peers = new ArrayList<PeerInformation>();
		this.id = DEFAULT_ID;
 	}
 }
