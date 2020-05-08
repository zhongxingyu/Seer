 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright © 2012  Calle Lejdbrandt, Mattias Andrée, Peyman Eshtiagh
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.game;
 import cnt.local.*;
 
 import java.util.*;
 import java.io.*;
 
 
 /**
  * Player class
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  * @author  Calle Lejdbrandt, <a href="mailto:callel@kth.se">callel@kth.se</a>
  */
 public class Player implements Serializable
 {
     /**
      * Compatibility versioning for {@link Serializable}
      */
     private static final long serialVersionUID = 1L;
     
     
     
     /**
      * Constructor
      * 
      * @param  name         The name of the player
      * @param  id           The ID of the player
      * @param  extip        The public IP address of the player
      * @param  locip        The local IP address of the player
      * @param  connectedTo  The ID of the player to which this player is connected, loop (this player's ID) if none
      * @param  dnses        The player's DNS names
      */
     public Player(final String name, final int id, final String extip, final String locip, final int connectedTo, final String... dnses)
     {
 	this(name, Friends.getPersonalUUID(), id, extip, locip, connectedTo, dnses);
     }
     
     
     /**
      * Constructor
      * 
      * @param  name         The name of the player
      * @param  id           The ID of the player
      * @param  extip        The public IP address of the player
      * @param  locip        The local IP address of the player
      * @param  connectedTo  The ID of the player to which this player is connected, loop (this player's ID) if none
      * @param  dnses        The player's DNS names
      */
     public Player(final String name, final int id, final String extip, final String locip, final int connectedTo, final ArrayList<String> dnses)
     {
 	this(name, Friends.getPersonalUUID(), id, extip, locip, connectedTo, dnses);
     }
     
     
     /**
      * Constructor
      * 
      * @param  name         The name of the player
      * @param  uuid         Universally unique ID for the player
      * @param  id           The ID of the player
      * @param  extip        The public IP address of the player
      * @param  locip        The local IP address of the player
      * @param  connectedTo  The ID of the player to which this player is connected, loop (this player's ID) if none
      * @param  dnses        The player's DNS names
      */
     public Player(final String name, final UUID uuid, final int id, final String extip, final String locip, final int connectedTo, final String... dnses)
     {
 	assert name != null : "Players must be named";
 	//assert uuid != null : "UUID cannot be null";
 	//assert extip != null : "extip cannot be null";
 	//assert locip != null : "locip cannot be null";
 	assert dnses != null : "DNS collection cannot be null";
 	
 	this.name = name;
 	this.id = id;
 	this.uuid = uuid;
 	this.extip = extip;
 	this.locip = locip;
 	this.connectedTo = connectedTo;
 	this.dnses = new ArrayList<String>();
 	for (final String dns : dnses)
 	    this.dnses.add(dns);
     }
     
     
     /**
      * Constructor
      * 
      * @param  name         The name of the player
      * @param  uuid         Universally unique ID for the player
      * @param  id           The ID of the player
      * @param  extip        The public IP address of the player
      * @param  locip        The local IP address of the player
      * @param  connectedTo  The ID of the player to which this player is connected, loop (this player's ID) if none
      * @param  dnses        The player's DNS names
      */
     public Player(final String name, final UUID uuid, final int id, final String extip, final String locip, final int connectedTo, final ArrayList<String> dnses)
     {
 	assert name != null : "Players must be named";
 	//assert uuid != null : "UUID cannot be null";
 	//assert extip != null : "extip cannot be null";
 	//assert locip != null : "locip cannot be null";
 	assert dnses != null : "DNS collection cannot be null";
 	
 	this.name = name;
 	this.id = id;
 	this.uuid = uuid;
 	this.extip = extip;
 	this.locip = locip;
 	this.connectedTo = connectedTo;
 	this.dnses = dnses;
     }
     
     
     
     /**
      * Initialiser
      */
     {
 	try
 	{   readResolve();
 	}
 	catch (final ObjectStreamException err)
 	{   //This will never happen
 	    throw new Error("Impossible error");
 	}
     }
     
     
     
     /**
      * All instances of this class
      */
     private static HashMap<Integer, Player> instances = new HashMap<Integer, Player>();
     
     /**
      * All instances of this class, by UUID
      */
     private static HashMap<UUID, Player> instancesUUID = new HashMap<UUID, Player>();
     
     
     
     /**
      * The name of the player
      */
     protected String name;
     
     /**
      * The ID of the player
      */
     protected int id;
     
     /**
      * Universally unique ID for the player
      */
     protected UUID uuid;
     
     /**
      * The public IP address of the player
      */
     protected String extip;
     
     /**
      * The local IP address of the player
      */
     protected String locip;
     
     /**
      * The ID of the player to which this player is connected, loop (this player's ID) if none
      */
     protected int connectedTo;
     
     /**
      * The player's DNS names
      */
     protected ArrayList<String> dnses;
     
     
     
     /**
      * Gets an instance of this class
      * 
      * @param   id  The ID of the {@link Player} instance
      * @return      The instance with the ID
      */
     public static Player getInstance(final int id)
     {
 	synchronized (instances)
 	{   return instances.get(Integer.valueOf(id));
 	}
     }
     
     
     /**
      * Gets an instance of this class by UUID
      * 
      * @param   uuid  The UUID of the {@link Player} instance
      * @return        The instance with the UUID
      */
     public static Player getInstanceByUUID(final UUID uuid)
     {
 	synchronized (instances)
	{   return instancesUUID.get(uuid);
 	}
     }
     
     
     /**
      * Used to store deserialised instances
      * 
      * @see Serializable
      */
     public Object readResolve() throws ObjectStreamException
     {
 	synchronized (instances)
 	{   instances.put(Integer.valueOf(this.id), this);
	    final Player override = this.uuid == null ? null : instancesUUID.get(this.uuid);
 	    instancesUUID.put(this.uuid, this);
 	    Friends.updateFriend(this);
 	    
	    if (override != null)
	    {
		override.name = this.name;
		override.id = this.id;
		override.uuid = this.uuid;
		override.extip = this.extip;
		override.locip = this.locip;
		override.connectedTo = this.connectedTo;
		override.dnses = this.dnses;
	    }
 	}
 	return this;
     }
     
     
     /**
      * {@inheritDoc}
      */
     public boolean equals(final Object object)
     {
 	if (object == this)  return true;
 	if (object == null)  return false;
 	if (object instanceof Player == false)
 	    return false;
 	
 	final Player p = (Player)object;
 	
 	return ((this.uuid == null) || (p.uuid == null))
 	           ? (this.id == p.id)
 	           : (this.uuid.equals(p.uuid));
     }
     
     
     /**
      * {@inheritDoc}
      */
     public int hashCode() {
	return this.uuid == null ? 0 : this.uuid.hashCode();
     }
     
     
     /**
      * Gets the name of the player
      * 
      * @return  The name of the player
      */
     public String getName() {
 	return this.name;
     }
     
     
     /**
      * Gets the ID of the player
      * 
      * @return  The ID of the player
      */
     public int getID() {
 	return this.id;
     }
     
     
     /**
      * Gets the yniversally unique ID for the player
      * 
      * @return  Universally unique ID for the player
      */
     public UUID getUUID() {
 	return this.uuid;
     }
     
     
     /**
      * Gets the public IP address of the player
      * 
      * @return  The public IP address of the player
      */
     public String getPublicIP() {
 	return this.extip;
     }
     
     
     /**
      * Gets the local IP address of the player
      * 
      * @return  The local IP address of the player
      */
     public String getLocalIP() {
 	return this.locip;
     }
     
     
     /**
      * Gets the player's DNS names
      * 
      * @return  The player's DNS names
      */
     public ArrayList<String> getDNSes() {
 	return this.dnses;
     }
     
     
     /**
      * Gets the ID of the player to which this player is connected, loop (this player's ID) if none
      * 
      * @return  The ID of the player to which this player is connected, loop (this player's ID) if none
      */
     public int getConnectedTo() {
 	return this.connectedTo;
     }
     
     
     /**
      * {@inheritDoc}
      */
     public String toString() {
	return ((this.name + " (") + (this.id + " → ") + (this.connectedTo + ", ")) + ((this.extip + "/") + (this.locip + ", ") + (this.dnses + ")"));
     }
     
 }
 
