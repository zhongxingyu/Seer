 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edu.cmu.cs.cimds.geogame.client.model.db;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.UUID;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.NotFound;
 import org.hibernate.annotations.NotFoundAction;
 
 
 /**
  *
  * @author ajuarez
  */
 @Entity
 @Table(name="user")
 public class User extends PersistentEntity {
 
 	private static final long serialVersionUID = 952823332683523754L;
 	
 	private String username;
 	private String password;
 	private String email;
 	private String firstName;
 	private String lastName;
 	private int score = 0;
 	private boolean admin;
 	private String iconFilename;
 //	private Date moveStart;
 	private boolean loggedIn;
 	private boolean moving;
 	private RoadMovement currentRoadMovement;
 	private Date lastRequest;
 	
 	private Long expiration;//OPTIONAL: expiration date for account, NULL means never expire
 	private String verifycode;
 	
 	//Way to deal with the bidirectional ManyToMany relationship
 	//neighbors1 and neighbors2 are mapped by Hibernate
 	//neighbors is transient
 //	private List<User> neighbors = new ArrayList<User>();
 	private List<User> neighbors;
 //	private List<User> neighbors1;
 //	private List<User> neighbors2;
 	private List<AcceptedForm> acceptedForms;
 	
 	//***Attributes on the current state of the game***//
 	private String authCode;
 	private String AMTCode;
 	private boolean AMTVisitor;
 	private Location currentLocation;
 	private Road currentRoad;
 	private boolean forward;
 	private List<Item> inventory;
 	private List<ItemType> itemsToCollect;
 	private List<Location> knownLocations;
 	private List<Location> accessibleLocations;
 	private List<Road> knownRoads;
 	
 	public HashMap<String, String> itemNameSynPairs = new HashMap<String, String>();
 	
 	private double latitude;
 	private double longitude;
 
 	private List<Message> receivedMessages;
 	
 	private static final long USER_ACTIVE_TIMEOUT = 20000;
 
 	@Column(name="username", nullable=false)
 	public String getUsername() { return username; }
 	public void setUsername(String username) { this.username = username; }
 	
 	@Column(name="email", nullable=false)
 	public String getEmail() { return email; }
 	public void setEmail(String email) { this.email = email; }
 
 	@Column(name="password", nullable=false)
 	public String getPassword() { return password; }
 	public void setPassword(String password) { this.password = password; }
 
 	
 	@Column(name="expiration", nullable=true)
 	public Long getExpiration() { return expiration; }
 	public void setExpiration(Long expiration) { this.expiration = expiration; }
 
 	
 	@Column(name="first_name")
 	public String getFirstName() { return firstName; }
 	public void setFirstName(String firstName) { this.firstName = firstName; }
 
 	@Column(name="last_name")
 	public String getLastName() { return lastName; }
 	public void setLastName(String lastName) { this.lastName = lastName; }
 
 	@Column(name="score")
 	public int getScore() { return score; }
 	public void setScore(int score) { this.score = score; }
 
 	@Column(name="admin", nullable=false)
 	public boolean isAdmin() { return admin; }
 	public void setAdmin(boolean admin) { this.admin = admin;}
 
 	@Column(name="icon_filename")
 	public String getIconFilename() { return iconFilename; }
 	public void setIconFilename(String iconFilename) { this.iconFilename = iconFilename; }
 
 	@Column(name="logged_in", nullable=false)
 	public boolean isLoggedIn() { return loggedIn; }
 	public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
 	
 	@Column(name="mturk_code", nullable=true)
 	public String getAMTCode() { return AMTCode; }
 	public void setAMTCode(String AMTCode) { this.AMTCode = AMTCode; }
 	
 	@Column(name="amt_visitor", nullable=false)
 	public boolean isAMTVisitor() { return AMTVisitor; }
 	public void setAMTVisitor(boolean AMTVisitor) { this.AMTVisitor = AMTVisitor; }
 
 	@Column(name="verifycode", nullable=true)
 	public String getVerifyCode(){ return verifycode; }
 	public void setVerifyCode(String code) {this.verifycode = code; }
 	
 	
 	//	@Transient
 //	public List<User> getNeighbors() {
 //		List<User> neighbors = new ArrayList<User>();
 //		for(User neighbor: this.neighbors1) {
 //			neighbors.add(neighbor);
 //		}
 //		for(User neighbor: this.neighbors2) {
 //			neighbors.add(neighbor);
 //		}
 //		return neighbors;
 //	}
 	
 	@ManyToMany(targetEntity=edu.cmu.cs.cimds.geogame.client.model.db.User.class, fetch=FetchType.LAZY, cascade={CascadeType.MERGE, CascadeType.PERSIST})
 	@JoinTable(
 		name="user_graph",
 		joinColumns=@JoinColumn(name="user1_id"),
 		inverseJoinColumns=@JoinColumn(name="user2_id")
 	)
 	public List<User> getNeighbors() { return neighbors; }
 	@SuppressWarnings("unused")
 	private void setNeighbors(List<User> neighbors) { this.neighbors = neighbors; }
 
 	//Meant to be used only when resetting database
 	public void addNeighbor(User neighbor) {
 		if (!this.neighbors.contains(neighbor)) {
 			this.neighbors.add(neighbor);
 		}
 	}
 	public void clearNeighbors() {
 		this.neighbors.clear();
 	}
 //	@SuppressWarnings("unused")
 //	@ManyToMany(targetEntity=edu.cmu.cs.cimds.geogame.client.User.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
 //	@JoinTable(
 //		name="user_graph",
 //		joinColumns=@JoinColumn(name="user2_id"),
 //		inverseJoinColumns=@JoinColumn(name="user1_id")
 //	)
 //	private List<User> getNeighbors2() { return neighbors2; }
 //	@SuppressWarnings("unused")
 //	private void setNeighbors2(List<User> neighbors2) { this.neighbors2 = neighbors2; }
 
 	@Column(name="auth_code")
 	public String getAuthCode() { return authCode; }
 	public void setAuthCode(String authCode) { this.authCode = authCode; }
 
 	@Column(name="is_moving")
 	public boolean isMoving() { return moving; }
 	public void setMoving(boolean moving) { this.moving = moving; }
 
 	@ManyToOne(fetch=FetchType.EAGER)
 	@JoinColumn(name="current_location_id")
 	public Location getCurrentLocation() { return currentLocation; }
 	public void setCurrentLocation(Location currentLocation) { this.currentLocation = currentLocation; }
 	
 	@ManyToOne(fetch=FetchType.EAGER)
 	@JoinColumn(name="current_road_id")
 	public Road getCurrentRoad() { return currentRoad; }
 	public void setCurrentRoad(Road currentRoad) { this.currentRoad = currentRoad; }
 	
 	@ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.ALL})
 	@NotFound(action=NotFoundAction.IGNORE)
 	@JoinColumn(name="current_road_movement_id")
 	public RoadMovement getCurrentRoadMovement() { return currentRoadMovement; }
 	public void setCurrentRoadMovement(RoadMovement currentRoadMovement) {
 		if (currentRoadMovement == null) {
 			//System.out.println("Setting currentRoadMovement to null!");
 		}
 		this.currentRoadMovement = currentRoadMovement;
 	}
 
 	@Column(name="forward")
 	public boolean getForward() { return forward; }
 	public void setForward(boolean forward) { this.forward = forward; }
 	
 //	@Column(name="move_start")
 //	public Date getMoveStart() { return moveStart; }
 //	public void setMoveStart(Date moveStart) { this.moveStart = moveStart; }
 
 	@OneToMany(mappedBy="owner", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
 	public List<Item> getInventory() { return inventory; }
 	public void setInventory(List<Item> inventory) { this.inventory = inventory; }
 	public void addInventory(Item item) {
 		this.inventory.add(item);
 		item.setOwner(this);
 	}
 	public void removeFromInventory(Item item) {
 		this.inventory.remove(item);
 		item.setOwner(null);
 	}
 	
 	@ManyToMany(targetEntity=edu.cmu.cs.cimds.geogame.client.model.db.ItemType.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
 	@JoinTable(
 		name="goal_items_per_player",
 		joinColumns=@JoinColumn(name="player_id"),
 		inverseJoinColumns=@JoinColumn(name="item_type_id")
 	)
 	public List<ItemType> getItemsToCollect() { return itemsToCollect; }
 	public void setItemsToCollect(List<ItemType> itemsToCollect) { this.itemsToCollect = itemsToCollect; }
 	public void addItemToCollect(ItemType itemType) {
 		if(this.itemsToCollect==null) {
 			this.itemsToCollect = new ArrayList<ItemType>();
 		}
 		this.itemsToCollect.add(itemType);
 	}
 	
 	//For Hibernate use only
 	@Column(name="latitude")
 	public Double getLatitude() { return latitude; }
 	public void setLatitude(Double latitude) { this.latitude = latitude; }
 	
 	//For Hibernate use only
 	@Column(name="longitude")
 	public Double getLongitude() { return longitude; }
 	public void setLongitude(Double longitude) { this.longitude = longitude; }
 	
 	@Column(name="last_request")
 	public Date getLastRequest() { return this.lastRequest; }
 	public void setLastRequest(Date lastRequest) { this.lastRequest = lastRequest; }
 	
 	@OneToMany(mappedBy="user", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
 	public List<AcceptedForm> getAcceptedForms() { return acceptedForms; }
 	public void setAcceptedForms(List<AcceptedForm> acceptedForms) { this.acceptedForms = acceptedForms; }
 
 	@ManyToMany(targetEntity=edu.cmu.cs.cimds.geogame.client.model.db.Message.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
 	@JoinTable(
 		name="message_graph",
 		joinColumns=@JoinColumn(name="receiver_id"),
 		inverseJoinColumns=@JoinColumn(name="message_id")
 	)
 	public List<Message> getReceivedMessages() { return receivedMessages; }
 	@SuppressWarnings("unused")
 	private void setReceivedMessages(List<Message> receivedMessages) { this.receivedMessages = receivedMessages; }
 
 	@ManyToMany(targetEntity=edu.cmu.cs.cimds.geogame.client.model.db.Location.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
 	@JoinTable(
 		name="location_info_per_player",
 		joinColumns=@JoinColumn(name="player_id"),
 		inverseJoinColumns=@JoinColumn(name="location_id")
 	)
 	public List<Location> getKnownLocations() { return knownLocations; }
 	@SuppressWarnings("unused")
 	private void setKnownLocations(List<Location> knownLocations) { this.knownLocations = knownLocations; }
 	public void addKnownLocation(Location knownLocation) { this.knownLocations.add(knownLocation); }
 	
 	@ManyToMany(targetEntity=edu.cmu.cs.cimds.geogame.client.model.db.Road.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
 	@JoinTable(
 		name="road_info_per_player",
 		joinColumns=@JoinColumn(name="player_id"),
 		inverseJoinColumns=@JoinColumn(name="road_id")
 	)
 	public List<Road> getKnownRoads() { return knownRoads; }
 	@SuppressWarnings("unused")
 	private void setKnownRoads(List<Road> knownRoads) { this.knownRoads = knownRoads; }
 	public void addKnownRoad(Road knownRoad) { this.knownRoads.add(knownRoad); }
 
 	@Override
 	public int hashCode() {
 		return this.getId().intValue()+this.getPassword().hashCode();
 	}
 	
 	@Transient
 	public Location getSource() {
 		if(this.currentRoadMovement!=null) {
 			return this.currentRoadMovement.getSource();
 		} else {
 			return null;
 		}
 	}
 
 	@Transient
 	public Location getDestination() {
 		if(this.currentRoadMovement!=null) {
 			return this.currentRoadMovement.getDestination();
 		} else {
 			return null;
 		}
 	}
 	
 	public boolean seemsActive() {
 		return this.loggedIn && (new Date().getTime()-this.getLastRequest().getTime()) <= USER_ACTIVE_TIMEOUT;
 	}
 
 	public boolean seemsActive(Date currentDate) {
 		return this.loggedIn && (currentDate.getTime()-this.getLastRequest().getTime()) <= USER_ACTIVE_TIMEOUT;
 	}
 }
