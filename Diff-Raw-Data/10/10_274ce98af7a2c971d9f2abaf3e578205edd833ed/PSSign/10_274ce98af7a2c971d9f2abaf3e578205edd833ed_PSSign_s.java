 package net.eonz.bukkit.psduo.signs;
 
 /*
  * This code is Copyright (C) 2011 Chris Bode, Some Rights Reserved.
  *
  * Copyright (C) 1999-2002 Technical Pursuit Inc., All Rights Reserved. Patent 
  * Pending, Technical Pursuit Inc.
  *
  * Unless explicitly acquired and licensed from Licensor under the Technical 
  * Pursuit License ("TPL") Version 1.0 or greater, the contents of this file are 
  * subject to the Reciprocal Public License ("RPL") Version 1.1, or subsequent 
  * versions as allowed by the RPL, and You may not copy or use this file in 
  * either source code or executable form, except in compliance with the terms and 
  * conditions of the RPL.
  *
  * You may obtain a copy of both the TPL and the RPL (the "Licenses") from 
  * Technical Pursuit Inc. at http://www.technicalpursuit.com.
  *
  * All software distributed under the Licenses is provided strictly on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TECHNICAL
  * PURSUIT INC. HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
  * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the Licenses for specific 
  * language governing rights and limitations under the Licenses. 
  */
 
 import net.eonz.bukkit.psduo.Direction;
 import net.eonz.bukkit.psduo.PailStone;
 import net.eonz.bukkit.psduo.signs.normal.*;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.material.MaterialData;
 
 /**
  * The base class for all pailstone signs.
  * 
  * @author Chris "Hafnium" Bode
  * 
  */
 public abstract class PSSign {
 
 	public void initialize(SignType type, String[] lines, String owner, String data, String world, Location l, Direction facing, boolean reload, SignChangeEvent event, PailStone main) {
 		this.safeLines = lines;
 		this.location = l;
 		this.world = world;
 		this.owner = owner;
 		this.main = main;
 		this.myType = type;
 
 		this.facing = facing;
 		this.inputLocation = new Location[3];
 		this.inputLocation[0] = Direction.shift(l, Direction.right(facing), 1);
 		this.inputLocation[1] = Direction.shift(l, facing, 1);
 		this.inputLocation[2] = Direction.shift(l, Direction.left(facing), 1);
 
 		this.hostblockLocation = Direction.shift(l, Direction.opposite(facing), 1);
 		this.leverLocation = Direction.shift(l, Direction.opposite(facing), 2);
 
 		this.main.sgc.trigger(TriggerType.CLEAR, l);
 
 		this.setData(data);
 		this.declare(reload, event);
 		this.main.sgc.register(this, TriggerType.PING);
 		this.main.sgc.register(this, TriggerType.CLEAR);
 	}
 
 	// SIGN DATA
 
 	private String[] safeLines;
 	private Location location;
 	private String world;
 	private String owner;
 	protected PailStone main;
 	private Direction facing;
 	private Location[] inputLocation;
 	private Location hostblockLocation, leverLocation;
 	@SuppressWarnings("unused")
 	private SignType myType;
 
 	// HELPER INFO FUNCTIONS
 
 	public Direction getFacing() {
 		return facing;
 	}
 
 	/**
 	 * Returns the input state of a certain input. Takes the supplied redstone
 	 * change event into account.
 	 * 
 	 * @param direction
 	 * @return
 	 */
 	public InputState getInput(int direction, BlockRedstoneEvent event) {
 		if (event != null) {
 			Block iblock = this.getInputBlock(direction);
 			if (!iblock.getLocation().equals(event.getBlock().getLocation())) {
 				return this.getInput(direction);
 			} else {
 				if (event.getBlock().getType() == Material.REDSTONE_WIRE) {
 					if (event.getNewCurrent() > 0) {
 						return InputState.HIGH;
 					} else {
 						return InputState.LOW;
 					}
 				} else {
 					return InputState.DISCONNECTED;
 				}
 			}
 		} else {
 			return this.getInput(direction);
 		}
 	}
 
 	/**
 	 * Returns the input state of a certain input.
 	 * 
 	 * @param direction
 	 * @return
 	 */
 	public InputState getInput(int direction) {
 		Block iblock = this.getInputBlock(direction);
 		if (iblock.getType() == Material.REDSTONE_WIRE) {
 			if (iblock.getState().getRawData() > 0) {
 				return InputState.HIGH;
 			} else {
 				return InputState.LOW;
 			}
 		} else {
 			return InputState.DISCONNECTED;
 		}
 	}
 
 	public static enum InputState {
 		HIGH, LOW, DISCONNECTED;
 	}
 
 	/**
 	 * <b>NOT UNLOADED SAFE</b><br/>
 	 * Returns the lever's block object.
 	 */
 	public Block getLeverBlock() {
 		return this.getWorld().getBlockAt(this.getLeverLocation());
 	}
 
 	/**
 	 * <b>Unloaded Safe</b><br/>
 	 * Gets the location of the lever block.
 	 * 
 	 * @return
 	 */
 	public Location getLeverLocation() {
 		return leverLocation.clone();
 	}
 
 	/**
 	 * <b>NOT UNLOADED SAFE</b><br/>
 	 * Returns the host block's block object.
 	 */
 	public Block getHostBlock() {
 		return this.getWorld().getBlockAt(this.getHostLocation());
 	}
 
 	/**
 	 * <b>Unloaded Safe</b><br/>
 	 * Gets the location of the host block.
 	 * 
 	 * @return
 	 */
 	public Location getHostLocation() {
 		return hostblockLocation.clone();
 	}
 
 	/**
 	 * <b>NOT UNLOADED SAFE</b><br/>
 	 * Retrieves the block of each redstone input.
 	 */
 	public Block getInputBlock(int direction) {
 		return this.getWorld().getBlockAt(getInputLocation(direction));
 	}
 
 	/**
 	 * <b>Unloaded Safe</b><br/>
 	 * Retrieves the location of each redstone input.
 	 * 
 	 * @param direction
 	 * @return
 	 */
 	public Location getInputLocation(int direction) {
 		return inputLocation[direction];
 	}
 
 	/**
 	 * <b>NOT UNLOADED SAFE</b><br/>
 	 * Gets the sign block. Note that this will probably do something weird if
 	 * the chunk with the sign on it is unloaded. You may want to check for that
 	 * <i>BEFORE</i> you call this.
 	 */
 	public Block getBlock() {
 		return main.getServer().getWorld(world).getBlockAt(location);
 	}
 
 	/**
 	 * <b>Unloaded Safe</b><br/>
 	 * Returns the location of the sign.
 	 * 
 	 * @return
 	 */
 	public Location getLocation() {
 		return location.clone();
 	}
 
 	/**
 	 * <b>Unloaded Safe</b><br/>
 	 * Gets the world object.
 	 * 
 	 * @return
 	 */
 	public World getWorld() {
 		return this.main.getServer().getWorld(this.world);
 	}
 
 	/**
 	 * <b>Unloaded Safe</b><br/>
 	 * Returns the player that owns the sign, or null if the player is not
 	 * logged in. If you need the player's name only, user getPlayerName()
 	 * 
 	 * @return
 	 */
 	public Player getOwner() {
 		return this.main.getServer().getPlayer(this.getOwnerName());
 	}
 
 	/**
 	 * <b>Unloaded Safe</b><br/>
 	 * Returns the name of the owner of the sign, logged in or not.
 	 * 
 	 * @return
 	 */
 	public String getOwnerName() {
 		return this.owner;
 	}
 
 	/**
 	 * Checks if the sign
 	 * 
 	 * @return
 	 */
 	public boolean isLoaded() {
 		return this.getWorld().isChunkLoaded(this.getWorld().getChunkAt(this.getLocation()));
 	}
 
 	/**
 	 * <b>NOT UNLOADED SAFE</b><br/>
 	 * Checks to see if the output block contains a lever.
 	 */
 	public boolean isOutputLever() {
 		return this.getLeverBlock().getType() == Material.LEVER;
 	}
 
 	// HELPER SET FUNCTIONS
 
 	/**
 	 * Clears the argument lines of the sign.
 	 */
 	public void clearArgLines() {
 		setLine(1, " ");
 		setLine(2, " ");
 		setLine(3, " ");
 	}
 
 	/**
 	 * Safely sets a line of the sign.
 	 */
 	public void setLine(int line, String newLine) {
 		Sign mySign = (Sign) this.getBlock().getState();
 		mySign.setLine(line, newLine);
 		this.safeLines[line] = newLine;
 		this.safeLines = PailStone.formatLines(this.safeLines);
 		mySign.update(true);
 	}
 
 	/**
 	 * Clears the argument lines of the sign.
 	 */
 	public void clearArgLines(SignChangeEvent event) {
 		setLine(1, " ", event);
 		setLine(2, " ", event);
 		setLine(3, " ", event);
 	}
 
 	/**
 	 * Sets a line of a sign using the event object if it isn't null.
 	 * 
 	 * @param line
 	 * @param newLine
 	 * @param event
 	 */
 	public void setLine(int line, String newLine, SignChangeEvent event) {
 		if (event == null) {
 			this.setLine(line, newLine);
 		} else {
 			event.setLine(line, newLine);
 			this.safeLines[line] = newLine;
 			this.safeLines = PailStone.formatLines(this.safeLines);
 		}
 	}
 
 	/**
 	 * Sends a message to the sign owner. Best used in declare methods where
 	 * reload = false.
 	 */
 	public void init(String message) {
 		this.main.alert(this.owner, message);
 	}
 
 	/**
 	 * Sets the output lever
 	 */
 	public void setOutput(boolean out) {
 		if (isLoaded() && isOutputLever()) {
 			byte s = this.getLeverBlock().getState().getRawData();
 
 			if (out) {
 				s = (byte) (s | 8);
 			} else {
 				s = (byte) (s & 7);
 			}
 
 			BlockState state = this.getLeverBlock().getState();
 			MaterialData stateData = state.getData();
 			stateData.setData(s);
 			state.update();
 			this.getLeverBlock().getState().update();
 			this.getHostBlock().getState().update();
 			this.getLeverBlock().getState().getData().setData(s);
 		}
 	}
 
 	// ADMIN FUNCTIONS
 
 	private boolean internallyInvalidated = false;
 
 	/**
 	 * Checks to see if the sign is still valid. Returns ValidationState.UNKNOWN
 	 * if the sign's chunk is unloaded.
 	 */
 	public ValidationState isValid() {
 		// Check if chunk is loaded
 
 		if (internallyInvalidated)
 			return ValidationState.INVALID;
 
 		if (this.isLoaded()) {
 
 			// Chunk loaded, check if its a wall sign.
 			Block myBlock = this.getBlock();
 
 			if (myBlock.getTypeId() != 68) {
 				return ValidationState.INVALID;
 			} else {
 				// It's a wall sign. Check the text.
 				Sign mySign = (Sign) myBlock.getState();
 				String[] plines = PailStone.formatLines(mySign.getLines());
 				boolean matches = true;
 				boolean hasText = false;
 				for (int i = 0; i < 4; i++) {
 					if (!safeLines[i].trim().equalsIgnoreCase(plines[i].trim())) {
 						matches = false;
 						if ((!plines[i].trim().equals("")) && plines[i] != null)
 							hasText = true;
 						System.out.println(i + ": " + safeLines[i] + " vs " + plines[i]);
 						break;
 					}
 				}
 				if (matches) {
 					return ValidationState.VALID;
 				} else {
 					if (hasText)
 						return ValidationState.INVALID;
 					return ValidationState.BLANK;
 				}
 			}
 
 		} else {
 			return ValidationState.UNLOADED;
 		}
 	}
 
 	public static enum ValidationState {
 
 		/**
 		 * The sign is valid.
 		 */
 		VALID,
 
 		/**
 		 * The sign is blank.
 		 */
 		BLANK,
 
 		/**
 		 * The sign is invalid.
 		 */
 		INVALID,
 
 		/**
 		 * Can't tell if the sign the server has is valid because it is
 		 * unloaded.
 		 */
 		UNLOADED;
 
 	}
 
 	protected int getInputId(BlockRedstoneEvent e) {
 		int id = -1;
 		for (int i = 0; i < this.inputLocation.length; i++) {
 			if (this.inputLocation[i].equals(e.getBlock().getLocation())) {
 				id = i;
 				break;
 			}
 		}
 		return id;
 	}
 
 	private long lastTriggered[] = { 0, 0, 0 };
 	private static final long delay = 1;
 
 	/**
 	 * Handles triggers at a universal level before passing them on to the
 	 * individual signs' update methods.
 	 * 
 	 * @param type
 	 * @param args
 	 */
 	public synchronized void trigger(TriggerType type, Object args) {
 		// Ping is a meta-event that is only used to force validation of all
 		// signs.
 		if (type == TriggerType.PING) {
 
 			return;
 		}
 
 		if (type == TriggerType.CLEAR) {
 			if (this.getLocation().equals((Location) args)) {
 				this.main.c("A sign was placed in the location of an older sign. The older sign has been invalidated.");
 				this.internallyInvalidated = true;
 			}
 			return;
 		}
 
 		if (type == TriggerType.REDSTONE_CHANGE) {
 
 			BlockRedstoneEvent event = (BlockRedstoneEvent) args;
 			boolean ismine = false;
 			int pos = getInputId(event);
 
 			if (pos != -1)
 				ismine = true;
 
 			/*
 			 * OLD METHOD for (int i = 0; i < this.inputLocation.length; i++) {
 			 * if (this.inputLocation[i].equals(l)) { ismine = true; pos = i;
 			 * break; } }
 			 */
 
 			if (!ismine)
 				return;
 
 			if ((event.getNewCurrent() > 0 && event.getOldCurrent() > 0) || (event.getNewCurrent() <= 0 && event.getOldCurrent() <= 0)) {
 				// Not an edge, ignore.
 				return;
 			}
 
 			long ctime = this.getWorld().getFullTime();
 
 			if ((ctime - lastTriggered[pos]) > delay || (lastTriggered[pos] > ctime)) {
 				lastTriggered[pos] = ctime;
 				triggersign(type, args);
 				return;
 			} else {
 				return;
 			}
 
 		}
 
 		triggersign(type, args);
 	}
 
 	/**
 	 * This method is implemented in the sign subclasses. It is used as a final
 	 * handle for all triggers. This is where the signs do their stuff.
 	 * 
 	 * @param type
 	 * @param args
 	 */
 	protected abstract void triggersign(TriggerType type, Object args);

 	protected void startTicking() {
 		this.main.tickctrl.register(this);
 	}

 	/**
	 * Called each tick after sign is registered with PSTickControl untill this
	 * method returns true.
	 * 
 	 * @return
 	 */
 	public boolean tick() {
 		return false;
 	}
 
 	/**
 	 * The PSSign superclass will call this when it is done initializing and its
 	 * clear for the sign to initialize, declare itself, and define its hooks.
 	 */
 	protected abstract void declare(boolean reload, SignChangeEvent event);
 
 	/**
 	 * Sets the saved data in the sign. The sign will ignore null data.
 	 * 
 	 * @param data
 	 */
 	protected abstract void setData(String data);
 
 	/**
 	 * This method is used to get non sign-printed data that needs to be saved
 	 * between plugin disables and loads and restarts.
 	 * 
 	 * @return
 	 */
 	public abstract String getData();
 
 	/**
 	 * This method returns the purported lines of the sign, not including color
 	 * codes.
 	 * 
 	 * @return
 	 */
 	public String[] getLines() {
 		return safeLines;
 	}
 
 	/**
 	 * This method returns the purported lines of the sign, not including color
 	 * codes.
 	 * 
 	 * @return
 	 */
 	public String[] getLines(SignChangeEvent event) {
 		if (event != null) {
 			return PailStone.formatLines(event.getLines());
 		} else {
 			return getLines();
 		}
 	}
 
 	public boolean equals(Object o) {
 		return (o instanceof PSSign && o == this);
 	}
 
 	/**
 	 * This is where the new sign data will be converted into an object of the
 	 * sign class.
 	 * 
 	 * @param type
 	 * @param lines
 	 * @param data
 	 * @param l
 	 */
 	public static void signFactory(String[] lines, String owner, String data, String world, Location l, Direction facing, boolean reload, SignChangeEvent event, PailStone main) {
 		String[] rlines = null;
 		if (reload) {
 			rlines = PailStone.formatLines(lines);
 		} else {
 			rlines = PailStone.formatLines(event.getLines());
 		}
 
 		// Check to see if this was intended as a PailStone sign.
 		if (rlines[0].length() <= 0 || !(rlines[0].charAt(0) == ':')) {
 			return;
 		}
 
 		String signName = rlines[0].substring(1);
 		SignType type = null;
 
 		// Get the proper type.
 		for (int i = 0; i < SignType.values().length; i++) {
 			if (SignType.values()[i].name().equalsIgnoreCase(signName)) {
 				type = SignType.values()[i];
 				break;
 			}
 		}
 
 		if (type == null) {
 			main.c("Not a pailstone sign...");
 			return;
 		}
 
 		PSSign newSign = null;
 
 		String permission = type.name().toLowerCase();
 
 		if ((!reload) && (!main.hasPermission(main.getServer().getPlayer(owner), permission, world))) {
 			main.c(owner + " tried to make a " + type + " sign, but lacks the proper permissions.");
 			main.alert(owner, "You do not have permission to create this sign.");
 			return;
 		}
 
 		switch (type) {
 		case TEST:
 			newSign = new TestSign();
 			break;
 		case RAND:
 			newSign = new RandSign();
 			break;
 		case LOGIC:
 			newSign = new LogicSign();
 			break;
 		case DELAY:
 			newSign = new DelaySign();
 			break;
 		case TOGGLE:
 			newSign = new ToggleSign();
 			break;
 		case PULSE:
 			newSign = new PulseSign();
 			break;
 		case SEND:
 			newSign = new SendSign();
 			break;
 		case RECV:
 			newSign = new RecvSign();
 			break;
 		case TRIGGER:
 			newSign = new TriggerSign();
 			break;
 		case CLICK:
 			newSign = new ClickSign();
 			break;
 		case COUNT:
 			newSign = new CountSign();
 			break;
 		case CLOCK:
 			newSign = new ClockSign();
 			break;
 		case CTIME:
 			newSign = new CtimeSign();
 			break;
 		case CWEATHER:
 			newSign = new CweatherSign();
 			break;
 		case LOGGED:
 			newSign = new LoggedSign();
 			break;
 		case SENSOR:
 			newSign = new SensorSign();
 			break;
 		case DISP:
 			newSign = new DispSign();
 			break;
 		case ANNOUNCE:
 			newSign = new AnnounceSign();
 			break;
 		case CUBOID:
 			newSign = new CuboidSign();
 			break;
 		case SPAWN:
 			newSign = new SpawnSign();
 			break;
 		case ITEM:
 			newSign = new ItemSign();
 			break;
 		case BOLT:
 			newSign = new BoltSign();
 			break;
 		}
 
 		if (newSign != null) {
 			newSign.initialize(type, lines, owner, data, world, l, facing, reload, event, main);
 		} else if (!reload) {
 			main.alert(owner, "Error while creating sign.");
 			event.setCancelled(true);
 		}
 
 		if (!reload) {
 			main.c(owner + " created a sign of type " + type.name() + ".");
 		}
 	}
 
 	/**
 	 * Grabs the direction of a wall sign.
 	 * 
 	 * @return
 	 */
 	public static int getNumericDirection(Sign sign) {
 
 		// A sign's direction is stored in its damage value. This value is set
 		// when the sign is placed.
 		return sign.getRawData();
 	}
 
 	/**
 	 * Returns a Direction that the sign is facing.
 	 * 
 	 * @return
 	 */
 	public static Direction getDirection(Sign sign) {
 
 		switch (getNumericDirection(sign)) {
 
 		case 2:
 			return Direction.EAST;
 		case 3:
 			return Direction.WEST;
 		case 4:
 			return Direction.NORTH;
 		case 5:
 			return Direction.SOUTH;
 
 		}
 
 		return Direction.NORTH;
 	}
 
 }
