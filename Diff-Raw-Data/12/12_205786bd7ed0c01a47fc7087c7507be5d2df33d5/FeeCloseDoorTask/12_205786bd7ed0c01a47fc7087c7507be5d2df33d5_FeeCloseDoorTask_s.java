 package org.melonbrew.fee;
 
 import org.bukkit.block.Block;
 import org.bukkit.material.Door;
 import org.bukkit.material.MaterialData;
 import org.bukkit.material.TrapDoor;
 
 public class FeeCloseDoorTask implements Runnable {
 	private final Fee plugin;
 	
 	private final Block door;
 	
 	public FeeCloseDoorTask(Fee plugin, Block door){
 		this.plugin = plugin;
 		
 		this.door = door;
 	}
 	
 	public void run(){
 		MaterialData data = door.getState().getData();
 		
 		if (data instanceof Door || data instanceof TrapDoor){
 			plugin.closeDoor(door);
 		}
 	}
 }
