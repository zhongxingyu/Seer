 package distributed.systems.das.units;
 
 import java.io.Serializable;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Map;
 
 import distributed.systems.core.IMessageReceivedHandler;
 import distributed.systems.core.Message;
 import distributed.systems.core.SynchronizedClientSocket;
 import distributed.systems.core.SynchronizedSocket;
 import distributed.systems.das.MessageRequest;
 
 /**
  * Base class for all players whom can 
  * participate in the DAS game. All properties
  * of the units (hitpoints, attackpoints) are
  * initialized in this class.
  *  
  * @author Pieter Anemaet, Boaz Pat-El
  */
 public abstract class Unit implements Serializable, IMessageReceivedHandler {
 	private static final long serialVersionUID = -4550572524008491160L;
 
 	//
 	private InetSocketAddress battlefieldAddress;
 
 	// Position of the unit
 	protected Integer x, y;
 
 	// Health
 	private Integer maxHitPoints;
 	protected Integer hitPoints;
 
 	// Attack points
 	protected Integer attackPoints;
 
 	// Identifier of the unit
 	private Integer unitID;
 
 	// The communication socket between this client and the board
 	//protected transient SynchronizedClientSocket clientSocket;
 	
 	private transient SynchronizedSocket serverSocket;
 	private InetSocketAddress address;
 
 	// Map messages from their ids
 	private Map<Integer, Message> messageList;
 	// Is used for mapping an unique id to a message sent by this unit
 	private int localMessageCounter = 0;
 	
 	// If this is set to false, the unit will return its run()-method and disconnect from the server
 	protected boolean running;
 	
 	private transient Unit[][] map;
 
 	/* The thread that is used to make the unit run in a separate thread.
 	 * We need to remember this thread to make sure that Java exits cleanly.
 	 * (See stopRunnerThread())
 	 */
 	protected transient Thread runnerThread;
 
 	public enum Direction {
 		up, right, down, left
 	};
 	
 	public enum UnitType {
 		player, dragon, undefined,
 	};
 
 	/**
 	 * Create a new unit and specify the 
 	 * number of hitpoints. Units hitpoints
 	 * are initialized to the maxHitPoints. 
 	 * @param bfUrl 
 	 * 
 	 * @param maxHealth is the maximum health of 
 	 * this specific unit.
 	 * @param unitID2 
 	 */
 	public Unit(String url, int port, String bfUrl, int bfPort, int maxHealth, int attackPoints, int unitID ) {
 
 		battlefieldAddress = new InetSocketAddress(bfUrl, bfPort);
 		messageList = new HashMap<Integer, Message>();
 
 		// Initialize the max health and health
 		hitPoints = maxHitPoints = new Integer(maxHealth);
 
 		// Initialize the attack points
 		this.attackPoints = new Integer(attackPoints);
 
 		// Get a new unit id
 		this.unitID = unitID;
 
 		// Create a new socket
 		//clientSocket = new SynchronizedClientSocket(message, address, handler)
 /*
 		try {
 			// Try to register the socket
 			clientSocket.register("D" + unitID);
 		}
 		catch (AlreadyAssignedIDException e) {
 			System.err.println("Socket \"D" + unitID + "\" was already registered.");
 		}
 */
 		//clientSocket.addMessageReceivedHandler(this);
 		address = new InetSocketAddress(url, port);
 		serverSocket = new SynchronizedSocket(url, port);
 		serverSocket.addMessageReceivedHandler(this);	
 	}
 
 	/**
 	 * Adjust the hitpoints to a certain level. 
 	 * Useful for healing or dying purposes.
 	 * 
 	 * @param modifier is to be added to the
 	 * hitpoint count.
 	 */
 	public synchronized void adjustHitPoints(int modifier) {
 		if (hitPoints <= 0)
 			return;
 
 		hitPoints += modifier;
 
 		if (hitPoints > maxHitPoints)
 			hitPoints = maxHitPoints;
 
 		if (hitPoints <= 0)
 			removeUnit(x, y);
 	}
 	
 	public void dealDamage(int x, int y, int damage) {
 		/* Create a new message, notifying the board
 		 * that a unit has been dealt damage.
 		 */
 		int id;
 		Message damageMessage;
 		synchronized (this) {
 			id = localMessageCounter++;
 		
 			damageMessage = new Message();
 			damageMessage.put("request", MessageRequest.dealDamage);
 			damageMessage.put("x", x);
 			damageMessage.put("y", y);
			damageMessage.put("address", y);
 			damageMessage.put("damage", damage);
 			damageMessage.put("id", id);
 		}
 		
 		// Send a spawn message
 		//clientSocket.sendMessage(damageMessage, "localsocket://" + BattleField.serverID);
 		SynchronizedClientSocket clientSocket;
 		clientSocket = new SynchronizedClientSocket(damageMessage, battlefieldAddress, this);
 		clientSocket.sendMessage();
 		
 		/*
 		// Wait for the reply
 		while(!messageList.containsKey(id)) {
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e) {
 			}
 		}*/
 
 	}
 	
 	public void healDamage(int x, int y, int healed) {
 		/* Create a new message, notifying the board
 		 * that a unit has been healed.
 		 */
 		int id;
 		Message healMessage;
 		synchronized (this) {
 			id = localMessageCounter++;
 
 			healMessage = new Message();
 			healMessage.put("request", MessageRequest.healDamage);
 			healMessage.put("x", x);
 			healMessage.put("y", y);
 			healMessage.put("healed", healed);
 			healMessage.put("id", id);
 		}
 
 		// Send a spawn message
 		//clientSocket.sendMessage(healMessage, "localsocket://" + BattleField.serverID);
 		SynchronizedClientSocket clientSocket;
 		clientSocket = new SynchronizedClientSocket(healMessage, battlefieldAddress, this);
 		clientSocket.sendMessage();
 		
 		//waitForMessage(id);
 
 	}
 
 	/**
 	 * @return the maximum number of hitpoints.
 	 */
 	public int getMaxHitPoints() {
 		return maxHitPoints;		
 	}
 
 	/**
 	 * @return the unique unit identifier.
 	 */
 	public int getUnitID() {
 		return unitID;
 	}
 
 	/**
 	 * Set the position of the unit.
 	 * @param x is the new x coordinate
 	 * @param y is the new y coordinate
 	 */
 	public void setPosition(int x, int y) {
 		this.x = new Integer(x);
 		this.y = new Integer(y);
 	}
 
 	/**
 	 * @return the x position
 	 */
 	public Integer getX() {
 		return x;
 	}
 
 	/**
 	 * @return the y position
 	 */
 	public Integer getY() {
 		return y;
 	}
 
 	/**
 	 * @return the current number of hitpoints.
 	 */
 	public int getHitPoints() {
 		return hitPoints;
 	}
 
 	/**
 	 * @return the attack points
 	 */
 	public int getAttackPoints() {
 		return attackPoints;
 	}
 
 	/**
 	 * Tries to make the unit spawn at a certain location on the battlefield
 	 * @param x x-coordinate of the spawn location
 	 * @param y y-coordinate of the spawn location
 	 * @return true iff the unit could spawn at the location on the battlefield
 	 */
 	protected boolean spawn(int x, int y) {
 		//setPosition(x, y);
 		//System.out.println(unitID + ":Spawn Message:" + x + " " + y);
 		/* Create a new message, notifying the board
 		 * the unit has actually spawned at the
 		 * designated position. 
 		 */
 		int id = localMessageCounter++;
 		Message spawnMessage = new Message();
 		spawnMessage.put("request", MessageRequest.spawnUnit);
 		spawnMessage.put("x", x);
 		spawnMessage.put("y", y);
 		spawnMessage.put("unit", this);
 		spawnMessage.put("address", address);
 
 		spawnMessage.put("id", 0);
 		
 		
 		/*// Send a spawn message
 		try {
 			clientSocket.sendMessage(spawnMessage, "localsocket://" + BattleField.serverID);
 		} catch (IDNotAssignedException e) {
 			System.err.println("No server found while spawning unit at location (" + x + ", " + y + ")");
 			return false;
 		}*/
 		SynchronizedClientSocket clientSocket;
 		clientSocket = new SynchronizedClientSocket(spawnMessage, battlefieldAddress, this);
 		clientSocket.sendMessage();
 
 		//System.out.println("BLOCK SPAWN");
 
 		waitForMessage(0);
 		//System.out.println("UNLOCK SPAWN");
 /*
 		Message result = messageList.get(id);
 		if(result != null){
 			if((Boolean)result.get("succeded") == true) {
 				//setPosition((Integer)result.get("x"), (Integer)result.get("y"));
 				setPosition(x, y);
 
 				System.out.println("SUCCEDED");
 				return true;
 			} else return false;
 			
 		}
 		*/
 		// Remove the result from the messageList
 		//messageList.put(0, null);
 		
 		return true;
 	}
 	
 	/**
 	 * Returns whether the indicated square contains a player, a dragon or nothing. 
 	 * @param x: x coordinate
 	 * @param y: y coordinate
 	 * @return UnitType: the indicated square contains a player, a dragon or nothing.
 	 */
 	public UnitType getType(int x, int y) {
 		
 		if (getUnit(x, y) instanceof Player)
 			return UnitType.player;
 		else if (getUnit(x, y) instanceof Dragon)
 			return UnitType.dragon;
 		else return UnitType.undefined;
 	}
 
 	protected Unit getUnit(int x, int y)
 	{
 		return map[x][y];
 	}
 
 	protected void removeUnit(int x, int y)
 	{
 		//map[x][y] = null;
 		
 		Message removeMessage = new Message();
 		int id = localMessageCounter++;
 		removeMessage.put("request", MessageRequest.removeUnit);
 		removeMessage.put("x", x);
 		removeMessage.put("y", y);
 		removeMessage.put("id", id);
 
 		// Send the removeUnit message
 		//clientSocket.sendMessage(removeMessage, "localsocket://" + BattleField.serverID);
 		//SynchronizedClientSocket clientSocket;
 		//clientSocket = new SynchronizedClientSocket(removeMessage, battlefieldAddress, this);
 		//clientSocket.sendMessage();
 
 	}
 
 	protected void moveUnit(int x, int y)
 	{
 
 		System.out.println(unitID+ ":Move unit:" + x + " " + y);
 		Message moveMessage = new Message();
 		int id = localMessageCounter++;
 		moveMessage.put("request", MessageRequest.moveUnit);
 		moveMessage.put("x", x);
 		moveMessage.put("y", y);
 		moveMessage.put("id", id);
 		moveMessage.put("address", this.getAddress());
 
 		// Send the getUnit message
 		//clientSocket.sendMessage(moveMessage, "localsocket://" + BattleField.serverID);
 		SynchronizedClientSocket clientSocket;
 		clientSocket = new SynchronizedClientSocket(moveMessage, battlefieldAddress, this);
 		//clientSocket.sendMessageWithResponse();
 		clientSocket.sendMessage();
 
 		
 		//waitForMessage(id);
 		/*
 		Message reply = messageList.get(id);
 		if(reply != null){
 			setPosition((Integer)reply.get("x"), (Integer)reply.get("y"));
 			//System.out.println("SET POS");
 
 		}*/
 		// Remove the result from the messageList
 		//messageList.put(id, null);
 	}
 
 	public Message onMessageReceived(Message message) {
 		//if(message == null ) return null;
 		//System.out.println("UNIT MSG RCV:" + message.toString());
 		if ((MessageRequest)message.get("request") == MessageRequest.gameState) {
 			System.out.println("Games State update");
 			//Who am I?
 			map = (Unit[][])message.get("gamestate");
 			//Unit u = searchMapForThisUnit(map);//Could return null if it isn't in the map anymore
 			Unit u = (Unit)message.get("unit");//Could return null if it isn't in the map anymore
 			updateUnitState(u);
 			System.out.println("Unit:" + u.unitID + " " + u.getX() + " " + u.getY());
 			//Update this instance variables
 			
 		}
 		if((MessageRequest)message.get("request") == MessageRequest.spawnAck) {
 			map = (Unit[][])message.get("gamestate");
 			Unit u = (Unit)message.get("unit");//Could return null if it isn't in the map anymore
 			updateUnitState(u);
 
 			messageList.put(0, null);
 
 		}
 		
 		//System.out.println("Unit receives message");
 		//messageList.put((Integer)message.get("id"), message);
 		return null;
 	}
 	
 	//Return the unit in the map that is equal(same address) to this instance
 	//TODO Could be improved... like using a HashMap.
 	private Unit searchMapForThisUnit(Unit[][] map) {
 		for( int i = 0; i < map.length; i++) {
 			for( int j = 0; j < map[0].length; j++) {
 				if(map[i][j] == null) continue;
 				if(this.equals(map[i][j])){
 					updateUnitState(map[i][j]);
 					return map[i][j];
 				}
 			}
 		}
 		return null;
 		
 	}
 	
 	//If type is Undefined can return Unit of either type
 	protected Unit closestUnitOfType(UnitType type) {
 		Unit closest = null;
 		int distance = map.length*map.length; //Bigger than max distance
 
 		for( int i = 0; i < map.length; i++) {
 			for( int j = 0; j < map[0].length; j++) {
 				if(map[i][j] == null || Math.abs(this.getX()-i) + Math.abs(this.getY()-j) > distance) continue;
 				if(type == UnitType.undefined || getType(i,j) == type){
 					closest = map[i][j];
 					distance = Math.abs(this.getX()-i) + Math.abs(this.getY()-j);
 				}
 			}
 		}
 		return closest;
 		
 	}
 	
 	protected Direction inDirectionOfUnit(Unit unit) {
 		int difX = this.getX()-unit.getX();
 		int difY = this.getY()-unit.getY();
 		if(difX > 0) return Direction.left;
 		if(difX < 0) return Direction.right;
 		if(difY > 0) return Direction.up;
 		if(difY < 0) return Direction.down;
 		return Direction.up;
 	}
 
 	
 
 	
 	private void updateUnitState(Unit u){
 		setPosition(u.getX(), u.getY());
 		this.attackPoints = u.getAttackPoints();
 		this.hitPoints = u.getHitPoints();
 	}
 	
 	// Disconnects the unit from the battlefield by exiting its run-state
 	public void disconnect() {
 		running = false;
 	}
 	
 	void waitForMessage(int id) {
 		int tries = 0;
 		while(!messageList.containsKey(id)) {
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e) {
 			}
 		}
 		
 	}
 
 	/**
 	 * Stop the running thread. This has to be called explicitly to make sure the program 
 	 * terminates cleanly.
 	 */
 	public void stopRunnerThread() {
 		try {
 			runnerThread.join();
 		} catch (InterruptedException ex) {
 			assert(false) : "Unit stopRunnerThread was interrupted";
 		}
 		
 	}
 	
 	public InetSocketAddress getAddress(){
 		return address;
 	}
 	
 	public InetSocketAddress getBattlefieldAddress() {
 		return battlefieldAddress;
 	}
 
 	
 	//Unit is equal if it has the same address
 	public boolean equals(Object o) {
 		if(((Unit)o).getAddress().equals(address))return true;
 		return false;
 	}
 }
