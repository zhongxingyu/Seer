 package kimononet.peer;
 
 import kimononet.geo.GeoLocation;
 import kimononet.geo.GeoVelocity;
 import kimononet.net.parcel.Parcel;
 import kimononet.net.parcel.ParcelException;
 import kimononet.net.parcel.Parcelable;
 
 /**
  * The Peer object stores information about the peer that is unique to a 
  * specific peer. This information includes the peer's GPS location, velocity,
  * human-readable name, and uniquely identifying peer address. 
  * 
  *  A peer parcel contains the following fields:
  *  -id (PeerAddress/8)
  *  -location (GeoLocation/24)
  *  -velocity (GeoVelocity/12)
  *  
  *  
  * 
  * @author Zorayr Khalapyan
  *
  */
 public class Peer implements Parcelable{
 
 	/**
 	 * Parcel size includes PeerAddress, GeoLocation, and GeoVelocity.
 	 */
 	public static final int PARCEL_SIZE = PeerAddress.PARCEL_SIZE + 
 										  GeoLocation.PARCEL_SIZE + 
 										  GeoVelocity.PARCEL_SIZE;
 	
 	/**
 	 * Represents a human readable name for the this peer.
 	 */
 	private String name;
 	
 	/**
 	 * A unique peer identification address. 
 	 */
 	private PeerAddress address;
 	
 	/**
 	 * Stores peer's current location.
 	 */
 	private GeoLocation location;
 	
 	/**
 	 * Stores peer's current velocity as calculated by change of GPS location 
 	 * over time.
 	 */
 	private GeoVelocity velocity;
 	
 	/**
 	 * Default name of a peer. 
 	 */
 	public static final String DEFAULT_PEER_NAME = "unnamed-peer";
 	
 	/**
 	 * Constructs a peer from a parcel. Since the name of the peer is not 
 	 * contained within the parcel, the parsed peer's name will be set to
 	 * the value specified by {@link #DEFAULT_PEER_NAME}.
 	 * 
 	 * @param parcel The parcel that represents a peer.
 	 * @see #parse(Parcel)
 	 */
 	public Peer(Parcel parcel){
 		parse(parcel);
 		
 		//Since the parcel doesn't contain the peer's name, set it to the 
 		//default value.
 		this.name = DEFAULT_PEER_NAME;
 	}
 	
 	/**
 	 * Creates a peer with the specified unique address and default name 
 	 * specified by {@link #DEFAULT_PEER_NAME}.
 	 * 
 	 * @param address A peer address that uniquely identifies a peer.
 	 */
 	public Peer(String address){
 		this(new PeerAddress(address));
 	}
 	
 	public Peer(PeerAddress address){
 		this(address, DEFAULT_PEER_NAME);
 	}
 	
 	/**
 	 * Creates a new peer from the specified name and address.
 	 * 
 	 * @param name Peer's name.
 	 * @param address Peer's unique address.
 	 */
 	public Peer(PeerAddress address, String name){
 		this.name = name;
 		this.address = address;		
 		this.velocity = new GeoVelocity();
 	}
 	
 	public Peer(PeerAddress address, GeoLocation location, GeoVelocity velocity){
 		this(address);
 		
 		this.location = location;
 		this.velocity = velocity;
 		
 	}
 	
 	/**
 	 * Converts the current peer to a parcel representation. The final parcel
 	 * will include the peer's address, location, and velocity. A new 
 	 * {@link ParcelException} will be thrown in case any of these values have 
 	 * not been specified.
 	 * 
 	 * @return A parcel representation of the current peer.
 	 * @throws ParcelException
 	 */
 	@Override
 	public Parcel toParcel(){
 		
 		String error = null;
 		
 		if(address == null){
 			error = "Peer's address is not specified or is null.";
 		} else if(location == null){
 			error = "Peer's location is not specified or is null.";
 		} else if(velocity == null){
 			error = "Peer's velocity is not specified or is null.";
 		}
 		
 		if(error != null){
 			throw new ParcelException("Unable to convert object to parcel. " + error);
 		}
 		
 		return Parcel.combineParcelables(address, location, velocity);
 	}
 	
 	/**
 	 * Parses a peer from the provided parcel. Affected values include peer's 
 	 * address, location, and velocity. Peer's name will remain unchanged.
 	 * 
 	 * @param parcel The parcel representation of a peer.
 	 */
 	public void parse(Parcel parcel){
 		address = new PeerAddress(parcel);
 		location = new GeoLocation(parcel);
 		velocity = new GeoVelocity(parcel);
		
		//Since a parceled version of velocity does not include a current 
		//location value, this needs to be manually set.
		velocity.update(location);
 	}
 	
 	/**
 	 * Sets the peer's current GPS location and updates the velocity.
 	 * @param location The peer's current GPS location.
 	 */
 	public void setLocation(GeoLocation newLocation){
 		this.location = newLocation; 
 		this.velocity.update(newLocation);
 	}
 	
 	
 	/**
 	 * Returns peer's current velocity. 
 	 * @return Peer's current velocity.
 	 */
 	public GeoVelocity getVelocity(){
 		return this.velocity;
 	}
 
 	/**
 	 * Returns the peer's current GPS location.
 	 * @return The peer's current GPS location.
 	 */
 	public GeoLocation getLocation(){
 		return this.location;
 	}
 	
 	/**
 	 * Returns the peer's name.
 	 * @return The peer's name.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the peer's name.
 	 * @param name Peer's name.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
	/**
	 * Returns the current peer's address.
	 * 
	 * @return Peer's address.
	 */
 	public PeerAddress getAddress() {
 		return address;
 	}
 
 	public void setAddress(PeerAddress address) {
 		this.address = address;
 	}
 	
 	public int getParcelSize(){
 		return PARCEL_SIZE;
 	}
 }
