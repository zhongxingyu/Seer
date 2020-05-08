 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.server;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.gamezgalaxy.GGS.API.player.PlayerChatEvent;
 import com.gamezgalaxy.GGS.API.player.PlayerCommandEvent;
 import com.gamezgalaxy.GGS.chat.Messages;
 import com.gamezgalaxy.GGS.networking.IOClient;
 import com.gamezgalaxy.GGS.networking.Packet;
 import com.gamezgalaxy.GGS.networking.PacketManager;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.DespawnPlayer;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.GlobalPosUpdate;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.MOTD;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.SetBlock;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.SpawnPlayer;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.TP;
 import com.gamezgalaxy.GGS.world.Block;
 import com.gamezgalaxy.GGS.world.Level;
 import com.gamezgalaxy.GGS.world.PlaceMode;
 
 public class Player extends IOClient {
 	protected short X;
 	protected short Y;
 	protected short Z;
 	protected byte ID;
 	protected Level level;
 	protected Thread levelsender;
 	protected Messages chat;
 	protected ArrayList<Player> seeable = new ArrayList<Player>();
 	protected Ping tick = new Ping(this);
 	/**
 	 * Weather or not the player is logged in
 	 */
 	public boolean isLoggedin;
 	/**
 	 * The reason why the player was kicked
 	 */
 	public String kickreason;
 	/**
 	 * The username of the player
 	 */
 	public String username;
 	/**
 	 * The world the player is currently in
 	 */
 	public String world;
 	/**
 	 * The mppass the user used to login
 	 */
 	public String mppass;
 	/**
 	 * The message the player last send
 	 */
 	public String message;
 	/**
 	 * Weather or not the player is connected
 	 */
 	public boolean isConnected;
 	/**
 	 * Weather or not the player can use color codes
 	 */
 	public boolean cc = true; //Can Player use color codes
 	/**
 	 * What type of client the player is using.
 	 * On WoM and minecraft.net, this value should be 0
 	 */
 	public byte ClientType; //This might be used for custom clients *hint hint*
 	/**
 	 * The last X pos of the player
 	 */
 	public short oldX;
 	/**
 	 * The last Y pos of the player
 	 */
 	public short oldY;
 	/**
 	 * The last Z pos of the player
 	 */
 	public short oldZ;
 	/**
 	 * The current yaw of the player
 	 */
 	public byte yaw;
 	/**
 	 * The current pitch of the player
 	 */
 	public byte pitch;
 	/**
 	 * The old yaw of the player
 	 */
 	public byte oldyaw;
 	/**
 	 * The old pitch of the player
 	 */
 	public byte oldpitch;
 	/**
 	 * Create a new Player object
 	 * @param client The socket the player used to connect
 	 * @param pm The PacketManager the player connected to
 	 */
 	public Player(Socket client, PacketManager pm) {
 		this(client, pm, (byte)255);
 	}
 	public Player(Socket client, PacketManager pm, byte opCode) {
 		super(client, pm);
 		ID = getFreeID();
 		if (opCode != 255) {
 			Packet packet = pm.getPacket(opCode);
 			if (packet == null) {
 				pm.server.Log("Client sent " + opCode);
 				pm.server.Log("How do..?");
 			} else {
 				byte[] message = new byte[packet.length];
 				try {
 					reader.read(message);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if (message.length < packet.length) {
 					pm.server.Log("Bad packet..");
 				}
 				else
 					packet.Handle(message, pm.server, this);
 			}
 		}
 		Listen();
 		pm.server.Add(tick);
		this.chat = new Messages(pm.server);
 	}
 	
 	/**
 	 * Verify the player is using a valid account
 	 * @return Returns true if the account is valid, otherwise it will return false
 	 */
 	public boolean VerifyLogin() {
 		//TODO Check for real user and group and such
 		return true;
 	}
 	
 	/**
 	 * Login the player
 	 * @throws InterruptedException 
 	 */
 	public void Login() throws InterruptedException {
 		if (isLoggedin)
 			return;
 		SendWelcome();
 		setLevel(pm.server.MainLevel);
 		levelsender.join(); //Wait for finish
 		X = (short)((0.5 + level.spawnx) * 32);
 		Y = (short)((1 + level.spawny) * 32);
 		Z = (short)((0.5 + level.spawnz) * 32);
 		oldX = X;
 		oldY = Y;
 		oldZ = Z;
 		pm.server.Log(this.username + " has joined the server.");
 		chat.serverBroadcast(this.username + " has joined the server.");
 		spawnPlayer(this);
 		setPos((short)((0.5 + level.spawnx) * 32), (short)((1 + level.spawny) * 32), (short)((0.5 + level.spawnz) * 32));
 		for (Player p : pm.server.players) {
 			if (p.level == level) {
 				spawnPlayer(p); //Spawn p for me
 				p.spawnPlayer(this); //Spawn me for p
 			}
 		}
 		setPos((short)((0.5 + level.spawnx) * 32), (short)((1 + level.spawny) * 32), (short)((0.5 + level.spawnz) * 32));
 		isLoggedin = true;
 	}
 	
 	/**
 	 * Handle the block change packet
 	 * @param X The X pos where the client modified
 	 * @param Y The Y pos where the client modified
 	 * @param Z The Z pos where the client modifed
 	 * @param type The type of action it did
 	 * @param holding What the client was holding
 	 */
 	public void HandleBlockChange(short X, short Y, short Z, PlaceMode type, byte holding) {
 		//TODO Call event
 		//TODO Check other stuff
 		if (holding > 49) {
 			Kick("Hack Client detected!");
 			return;
 		}
 		if (X > level.width || X < 0 || Y > level.height || Y < 0 || Z > level.depth || Z < 0) {
 			Kick("Hack Client detected!");
 			return;
 		}
 		if (type == PlaceMode.PLACE)
 			GlobalBlockChange(X, Y, Z, Block.getBlock(holding), level, pm.server);
 		else if (type == PlaceMode.BREAK)
 			GlobalBlockChange(X, Y, Z, Block.getBlock((byte)0), level, pm.server);
 	}
 	
 	/**
 	 * Send a block update to this player
 	 * @param X The X pos of the update
 	 * @param Y The Y pos of the update
 	 * @param Z The Z pos of the update
 	 * @param block The block to send
 	 */
 	public void SendBlock(short X, short Y, short Z, Block block) {
 		SetBlock sb = (SetBlock)(pm.getPacket((byte)0x06));
 		sb.X = X;
 		sb.Y = Y;
 		sb.Z = Z;
 		sb.block = block.getVisableBlock();
 		sb.Write(this, pm.server);
 	}
 	
 	/**
 	 * Send a block update to all the players on level "l" in server "s"
 	 * @param X The X pos of the udpate
 	 * @param Y The Y pos of the update
 	 * @param Z The Z pos of the update
 	 * @param block The block to send
 	 * @param l The level the update happened in
 	 * @param s The server the update happened in
 	 */
 	public static void GlobalBlockChange(short X, short Y, short Z, Block block, Level l, Server s) {
 		l.setTile(block, X, Y, Z);
 		SetBlock sb = (SetBlock)(s.getPacketManager().getPacket((byte)0x05));
 		sb.X = X;
 		sb.Y = Y;
 		sb.Z = Z;
 		sb.block = block.getVisableBlock();
 		for (Player p : s.players)
 			if (p.level == l)
 				sb.Write(p, s);
 	}
 	
 	/**
 	 * Get a list of who the player can see
 	 * @return An ArrayList of players
 	 */
 	public ArrayList<Player> getSeeable() {
 		return seeable;
 	}
 	
 	/**
 	 * Send the player an MoTD screen
 	 * You can have the user hang on the MoTD screen when the player first joins
 	 * @param topline The top line of the MoTD screen
 	 * @param bottomline The bottom line of the MoTD screen
 	 */
 	public void sendMoTD(String topline, String bottomline) {
 		MOTD m = (MOTD)(pm.getPacket("MOTD"));
 		m.topLine = topline;
 		m.bottomLine = bottomline;
 		m.Write(this, pm.server);
 	}
 	
 	public void UpdatePos() throws IOException {
 		if (!isLoggedin)
 			return;
 		TP t = (TP)(pm.getPacket("TP"));
 		GlobalPosUpdate gps = (GlobalPosUpdate)(pm.getPacket("GlobalPosUpdate"));
 		byte[] tosend;
 		if (Math.abs(getX() - oldX) >= 127 || Math.abs(getY() - oldY) >= 127 || Math.abs(getZ() - oldZ) >= 127)
 			tosend = t.toSend(this);
 		else
 			tosend = gps.toSend(this);
 		for (Player p : pm.server.players) {
 			if (p == this)
 				continue;
 			if (p.level == level)
 				p.WriteData(tosend);
 		}
 	}
 	
 	/**
 	 * Spawn a new player for this player
 	 * @param p The player to spawn
 	 */
 	public void spawnPlayer(Player p) {
 		if (seeable.contains(p))
 			return;
 		SpawnPlayer sp = (SpawnPlayer)(pm.getPacket((byte)0x07));
 		sp.spawn = p;
 		sp.Write(this, p.pm.server);
 		seeable.add(p);
 	}
 
 	/**
 	 * Get the level this player is currently on
 	 * @return The level
 	 */
 	public Level getLevel() {
 		return level;
 	}
 	
 	/**
 	 * Send a new level to the player
 	 * @param level The level to send
 	 */
 	public void setLevel(Level level) {
 		if (this.level == level)
 			return;
 		this.level = level;
 		levelsender = new SendLevel(this);
 		levelsender.start();
 	}
 	
 	/**
 	 * Weather or not the player is loading the level
 	 * @return True if the player is loading the level, false if the player is not
 	 */
 	public boolean isLoading() {
 		return levelsender != null;
 	}
 	
 	/**
 	 * Wait for the player to finish loading the level
 	 * The method blocks until the player finishes loading the level 
 	 * This will block 
 	 * @throws InterruptedException
 	 */
 	public void waitForLoaded() throws InterruptedException {
 		if (levelsender == null)
 			return;
 		levelsender.join();
 	}
 	
 	
 	protected void SendWelcome() {
 		pm.getPacket("Welcome").Write(this, pm.server);
 	}
 
 	/**
 	 * Kick the player from the server
 	 * @param reason The reason why he was kicked
 	 */
 	public void Kick(String reason) {
 		if (reason.equals(""))
 			reason = "No reason given";
 		Packet p = pm.getPacket("Kick");
 		this.kickreason = reason;
 		p.Write(this, pm.server);
 	}
 
 	private byte getFreeID() {
 		boolean found = true;
 		byte toreturn = 0;
 		for (int i = 0; i < 255; i++) {
 			found = true;
 			for (Player p : pm.server.players) {
 				if (p.ID == i) {
 					found = false;
 					break;
 				}
 			}
 			if (found) {
 				toreturn = (byte)i;
 				break;
 			}
 		}
 		return toreturn;
 	}
 
 	/**
 	 * Get the X cord. of the player
 	 * This is NOT in block cord.
 	 * @return The value
 	 */
 	public short getX() {
 		return X;
 	}
 	
 	/**
 	 * Get the X cord. of the player on the level
 	 * This is in block cord.
 	 * @return The value
 	 */
 	public int getBlockX() {
 		return X / 32;
 	}
 	/**
 	 * Get the X cord. of the player
 	 * This is NOT in block cord.
 	 * @return The value
 	 */
 	public short getY() {
 		return Y;
 	}
 	/**
 	 * Get the X cord. of the player on the level
 	 * This is in block cord.
 	 * @return The value
 	 */
 	public int getBlockY() {
 		return Y / 32;
 	}
 	/**
 	 * Get the ID of the player
 	 * @return The value
 	 */
 	public byte getID() {
 		return ID;
 	}
 	/**
 	 * Get the X cord. of the player
 	 * This is NOT in block cord.
 	 * @return The value
 	 */
 	public short getZ() {
 		return Z;
 	}
 	/**
 	 * Get the X cord. of the player on the level
 	 * This is in block cord.
 	 * @return The value
 	 */
 	public int getBlockZ() {
 		return Z / 32;
 	}
 	/**
 	 * Set the current X pos of the player
 	 * This will NOT teleport the player
 	 * To teleport the player, use {@link #setPos(short, short, short)}
 	 * @param value
 	 */
 	public void setX(short value) {
 		oldX = X;
 		X = value;
 	}
 	/**
 	 * Set the current Y pos of the player
 	 * This will NOT teleport the player
 	 * To teleport the player, use {@link #setPos(short, short, short)}
 	 * @param value
 	 */
 	public void setY(short value) {
 		oldY = Y;
 		Y = value;
 	}
 	/**
 	 * Set the current Z pos of the player
 	 * This will NOT teleport the player
 	 * To teleport the player, use {@link #setPos(short, short, short)}
 	 * @param value
 	 */
 	public void setZ(short value) {
 		oldZ = Z;
 		Z = value;
 	}
 	/**
 	 * Teleport the player
 	 * @param x The X pos to teleport to
 	 * @param y The Y pos to teleport to
 	 * @param z The Z pos to teleport to
 	 * @param yaw The new yaw
 	 * @param pitch The new pitch
 	 */
 	public void setPos(short x, short y, short z, byte yaw, byte pitch) {
 		setX(x);
 		setY(y);
 		setZ(z);
 		oldyaw = this.yaw;
 		this.yaw = yaw;
 		oldpitch = this.pitch;
 		this.pitch = pitch;
 		TP();
 	}
 	/**
 	 * Teleport the player
 	 * @param x The X pos to teleport to
 	 * @param y The Y pos to teleport to
 	 * @param z The Z pos to teleport to
 	 */
 	public void setPos(short x, short y, short z) {
 		setPos(x, y, z, yaw, pitch);
 	}
 	
 	protected void TP() {
 		TP t = (TP)(pm.getPacket("TP"));
 		t.pID = ID;
 		t.tp = this; //This player is teleporting
 		t.Write(this, pm.server); //Tell him that
 		for (Player p : pm.server.players) {
 			if (p.level == level && p != this)
 				t.Write(p, p.pm.server); //Tell all the other players as well...
 		}
 	}
 	
 	/**
 	 * Sends a message to the player if the message is less than 64 characters
 	 * 
 	 * @param string message
 	 * @return boolean true on sent, false on not sent.
 	 */
 	public boolean sendMessage(String message){
 		Packet p = pm.getPacket("Message");
 		if(message.length() < 64){
 			this.message = message;
 			p.Write(this, pm.server);
 		}else{
 			return false; //Message is longer than permitted
 		}
 		return true; //Message was sent successfully
 	}
 
 	/**
 	 * Handles the messages a player sends to the server, could be used in the future for run command as player
 	 * 
 	 * @param string message
 	 * @return void
 	 */
 	public void recieveMessage(String message){
 		if(message.startsWith("/"))
 		{	
 			PlayerCommandEvent event = new PlayerCommandEvent(this, message);
 			pm.server.getEventSystem().callEvent(event);
 			if (event.isCancelled())
 				return;
 			
 			if(message.contains("/cc"))
 			{
 				if(this.cc)
 				{
 					this.sendMessage("Color Codes have been disabled.");
 					this.cc = false;
 				}else{
 					this.sendMessage("Color Codes have been enabled.");
 					this.cc = true;
 				}
 			}
 			else if (message.contains("/spawn"))
 				setPos((short)((0.5 + level.spawnx) * 32), (short)((1 + level.spawny) * 32), (short)((0.5 + level.spawnz) * 32));
 		}else{
 			String m = message;
 			if(m.matches(".*%([0-9]|[a-f]|[k-r])(.+?).*") && this.cc){
 				Pattern pattern = Pattern.compile("%([0-9]|[a-f]|[k-r])(.+?)");
 				Matcher matcher = pattern.matcher(m);
 				while (matcher.find()) {
 				  String code = Character.toString(matcher.group().charAt(1));
 				  m = m.replaceAll("%"+code, "&"+code);
 				}
 			}
 			PlayerChatEvent event = new PlayerChatEvent(this, message);
 			pm.server.getEventSystem().callEvent(event);
 			if (event.isCancelled())
 				return;
 			pm.server.Log("User "+ this.username + " sent: " + message);
 			chat.serverBroadcast(this.username + ": " + m);
 		}
 	}
 	
 	/**
 	 * Despawn a player for this player
 	 * @param p The player to despawn
 	 */
 	public void Despawn(Player p) {
 		if (!seeable.contains(p))
 			return;
 		DespawnPlayer pa = (DespawnPlayer)(pm.getPacket((byte)0x0c));
 		pa.pID = p.ID;
 		pa.Write(this, pm.server);
 		seeable.remove(p);
 	}
 	
 	/**
 	 * Close the connection of this client
 	 * If you want to kick the player, use {@link #Kick(String)}
 	 */
 	@Override
 	public void CloseConnection() {
 		if (pm.server.players.contains(this))
 			pm.server.players.remove(this);
 		if(this.username != null)
 		{
 			pm.server.Log(this.username + " has left the server.");
 			chat.serverBroadcast(this.username + " has left the server.");
 		}
 		for (Player p : pm.server.players)
 			p.Despawn(this);
 		super.CloseConnection();
 		pm.server.Remove(tick); //Do this last as this takes a while to remove
 	}
 
 	protected void finishLevel() {
 		levelsender = null;
 	}
 
 	protected class Ping extends Tick {
 
 		Player p;
 		public Ping(Player p) { this.p = p; }
 		@Override
 		public void Tick() {
 			Packet pa;
 			pa = pm.getPacket((byte)0x01);
 			pa.Write(p, pm.server);
 			pa = null;
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	protected class SendLevel extends Thread {
 
 		Player p;
 		public SendLevel(Player p) { this.p = p; }
 		@Override
 		public void run() {
 			Packet pa;
 			pa = pm.getPacket((byte)0x02);
 			pa.Write(p, pm.server);
 			pa = null;
 			pa = pm.getPacket((byte)0x03);
 			pa.Write(p, pm.server);
 			pa = null;
 			pa = pm.getPacket((byte)0x04);
 			pa.Write(p, pm.server);
 			pa = null;
 			p.finishLevel();
 		}
 	}
 }
