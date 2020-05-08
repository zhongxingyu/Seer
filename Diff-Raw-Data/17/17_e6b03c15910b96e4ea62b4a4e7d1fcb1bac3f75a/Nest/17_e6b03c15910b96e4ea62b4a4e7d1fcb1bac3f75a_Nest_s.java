 package org.fourdnest.androidclient;
 
 import java.net.URI;
 
 import org.fourdnest.androidclient.comm.Protocol;
 import org.fourdnest.androidclient.comm.ProtocolFactory;
 import org.fourdnest.androidclient.comm.UnknownProtocolException;
 
 public class Nest {
 		
 	private int id;
 	private String name;
 	private String description;	
 	private URI baseURI;
 	private Protocol protocol;
 	
 	private String userName;
 	private String secretKey;
 	
 	/**
 	 * Empty constructor to allow subclassing
 	 */
 	protected Nest() { }
 	
 	/**
 	 * Construct a Nest using the given parameters.
 	 * @param id Nest id.
 	 * @param name User visible name of Nest.
 	 * @param description User visible description.
 	 * @param address Base URI for the Nest 
 	 * @param protocolId The id of the protocol to use when communicating with the Nest.
 	 * @param userName The username.
 	 * @param secretKey The password of the user. FIXME?
 	 * @throws UnknownProtocolException
 	 */
 	public Nest(int id,
 				String name,
 				String description,
 				URI address,
 				int protocolId,
 				String userName,
 				String secretKey) throws UnknownProtocolException {
 		
 		this.id = id;
 		this.name = name;
 		this.description = description;		
 		this.baseURI = address;		
 		this.protocol = ProtocolFactory.createProtocol(protocolId);
 		this.protocol.setNest(this);
 		this.userName = userName;
 		this.secretKey = secretKey;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(!(o instanceof Nest)) {
 			return false;
 		}
 		
 		Nest nest = (Nest)o;		
 		boolean eq = (this.id == nest.id &&
 				Util.objectsEqual(this.name, nest.name) &&
 				Util.objectsEqual(this.description, nest.description) &&
 				Util.objectsEqual(this.baseURI, nest.baseURI) &&
 				this.protocol.getProtocolId() == nest.protocol.getProtocolId() &&
 				Util.objectsEqual(this.userName, nest.userName) &&
 				Util.objectsEqual(this.secretKey, nest.secretKey)
 		);
 		
 		return eq;
 	}
 	
 	@Override
 	public int hashCode() {
 		long hash = this.id;
         hash = hash * 3 + (this.name == null ? 0 : this.name.hashCode());
         hash = hash * 7 + (this.description == null ? 0 : this.description.hashCode());
         hash = hash * 11 + (this.baseURI == null ? 0 : this.baseURI.hashCode());
         hash = hash * 13 + (this.protocol == null ? 0 : this.protocol.hashCode());
         hash = hash * 17 + (this.userName == null ? 0 : this.userName.hashCode());
         hash = hash * 19 + (this.secretKey == null ? 0 : this.secretKey.hashCode());
         
         int intHash = (int) (hash % Integer.MAX_VALUE);
         
 		return intHash;
 	}
 
 	public int getId() {
 		return this.id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return this.description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public URI getBaseURI() {
 		return this.baseURI;
 	}
 
 	public void setBaseURI(URI address) {
 		this.baseURI = address;
 	}
 
 	public int getProtocolId() {
 		return this.protocol.getProtocolId();
 	}
 
 	/**
 	 * @return the protocol object for communicating with this Nest
 	 */
 	public Protocol getProtocol() {
 		return protocol;
 	}
 	
 	/**
 	 * @return the userName
 	 */
 	public String getUserName() {
 		return userName;
 	}
 
 	/**
 	 * @param userName the userName to set
 	 */
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	/**
 	 * @return the secretKey
 	 */
 	public String getSecretKey() {
 		return secretKey;
 	}
 
 	/**
 	 * @param secretKey the secretKey to set
 	 */
 	public void setSecretKey(String secretKey) {
 		this.secretKey = secretKey;
 	}
 
 
 }
