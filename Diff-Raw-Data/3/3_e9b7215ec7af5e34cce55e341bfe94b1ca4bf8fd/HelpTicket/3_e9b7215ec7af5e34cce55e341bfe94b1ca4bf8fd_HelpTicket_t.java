 package org.monstercraft.support.plugin.wrappers;
 
 import org.bukkit.Location;
 import org.monstercraft.support.plugin.util.Status;
 
 public class HelpTicket {
 
 	private final int id;
 
 	private Status status;
 
 	private final String description;
 
 	private final String noobname;
 
 	private String modname;
 
 	private int x;
 
 	private int y;
 
 	private int z;
 
 	private String worldname;
 
 	public HelpTicket(final int id, final String description,
 			final String player, Location location) {
 		this.id = id;
 		this.description = description;
 		this.status = Status.OPEN;
 		this.noobname = player;
 		this.x = (int) location.getX();
 		this.y = (int) location.getY();
 		this.z = (int) location.getZ();
 		this.worldname = location.getWorld().getName();
		this.modname = "";
 	}
 	
 	public HelpTicket(final int id, final String description,
 			final String player, int x, int y, int z, String worldname) {
 		this.id = id;
 		this.description = description;
 		this.status = Status.OPEN;
 		this.noobname = player;
 		this.x = x;
 		this.y = y;
 		this.z = z;
 		this.worldname = worldname;
		this.modname = "";
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public int getID() {
 		return id;
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	public String getNoobName() {
 		return noobname;
 	}
 
 	public String getModName() {
 		return modname;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public int getZ() {
 		return z;
 	}
 	
 	public String getWorldName() {
 		return worldname;
 	}
 
 	public void Claim(String modname) {
 		this.modname = modname;
 		this.status = Status.CLAIMED;
 	}
 
 	public void close() {
 		this.status = Status.CLOSED;
 	}
 
 }
