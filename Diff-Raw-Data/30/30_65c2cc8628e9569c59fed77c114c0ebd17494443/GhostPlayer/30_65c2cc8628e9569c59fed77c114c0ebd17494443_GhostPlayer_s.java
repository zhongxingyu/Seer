 package org.whired.ghost.player;
 
 import java.awt.Point;
import java.io.Serializable;
 
 /**
  * Provides basic details about a player
  * @author Whired
  */
public class GhostPlayer implements Serializable {
 
 	/**
 	 * The name of this player
 	 */
 	private String name;
 	/**
 	 * The rights of this player
 	 */
 	private byte rights;
 	private int x, y;
 
 	/**
 	 * Creates a new player with the specified name, rights, and location
 	 * @param name the name of the player
 	 * @param rights the rights of the player (-128 to 127)
 	 * @param x the x-coordinate of this player
 	 * @param y the y-coordinate of this player
 	 */
 	public GhostPlayer(final String name, final int rights, final int x, final int y) {
		this.name = name;
 		this.rights = (byte) rights;
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Creates a new player with the specified name and rights
 	 * @param name the name of the player
 	 * @param rights the rights of the player (-128 to 127)
 	 */
 	public GhostPlayer(final String name, final int rights) {
		this.name = name;
 		this.rights = (byte) rights;
 	}
 
 	public Point getLocation() {
 		return new Point(x, y);
 	}
 
 	public void setLocation(final int x, final int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Gets the long that represents this player's name
 	 * @return the converted long
 	 */
 	public long nameToLong() {
 		long l = 0L;
 		for (int i = 0; i < getName().length() && i < 12; i++) {
 			final char c = getName().charAt(i);
 			l *= 37L;
 			if (c >= 'A' && c <= 'Z') {
 				l += 1 + c - 65;
 			}
 			else if (c >= 'a' && c <= 'z') {
 				l += 1 + c - 97;
 			}
 			else if (c >= '0' && c <= '9') {
 				l += 27 + c - 48;
 			}
 		}
 		while (l % 37L == 0L && l != 0L) {
 			l /= 37L;
 		}
 		return l;
 	}
 
 	/**
 	 * Gets the name of this player
 	 * @return the name of this player
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name of this player
 	 * @param name the name to set
 	 */
 	public void setName(final String name) {
		this.name = name;
 	}
 
 	/**
 	 * Gets the rights of this player
 	 * @return the rights
 	 */
 	public int getRights() {
 		return rights;
 	}
 
 	/**
 	 * Sets the rights of this player
 	 * @param rights the rights to set (-128 to 127)
 	 */
 	public void setRights(int rights) {
 		if (rights > 127) {
 			rights = 127;
 		}
 		if (rights < 0) {
 			rights = 0;
 		}
 		this.rights = (byte) rights;
 	}
 
 	@Override
 	public String toString() {
 		return this.name;
 	}
 
 	@Override
 	public int hashCode() {
 		return this.name.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object paramObject) {
 		if (this.hashCode() == paramObject.hashCode()) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 }
