 package com.moltendorf.bukkit.intellidoors;
 
 import java.util.Calendar;
 import org.bukkit.Material;
 
 /**
  * Door event handler class.
  *
  * @author moltendorf
  */
 class Handler {
 	protected Handler(final Material material, final Set_Door_Single set, final boolean open) {
 		switch (material) {
 			case IRON_DOOR_BLOCK:
 				door = Door_Iron.Get(set, open, time);
 				return;
 
 			case WOODEN_DOOR:
 				door = Door_Wood.Get(set, open, time);
 				return;
 
 			default:
 				door = null;
 		}
 	}
 
 	protected Handler(final Material material, final Set_Door_Double set, final boolean open) {
 		switch (material) {
 			case IRON_DOOR_BLOCK:
 				door = Door_Iron.Get(set, open, time);
 				return;
 
 			case WOODEN_DOOR:
 				door = Door_Wood.Get(set, open, time);
 				return;
 
 			default:
 				door = null;
 		}
 	}
 
 	protected Handler(final Set_Trap set, final boolean open) {
 		door = Door_Trap.Get(set, open);
 	}
 
 	// Final data.
 	protected final long time = Calendar.getInstance().getTimeInMillis();
 	protected final Door door;
 
 	protected int getPower() {
 		synchronized (door) {
 			if (door.busy()) {
 				return door.getPower();
 			}
 
 			if (door.reset()) {
 				door.reset(Door.reset, time);
 			}
 
 			return door.getPower();
 		}
 	}
 
 	protected boolean busy() {
 		synchronized (door) {
 			return door.busy(time);
 		}
 	}
 
 	protected void onInteract() {
 		synchronized (door) {
 			door.reset(Door.delay, time);
 			door.unlock();
 		}
 	}
 
 	protected void onPhysics() {
 		synchronized (door) {
 			if (door.busy()) {
 				return;
 			}
 
 			if (door.reset()) {
 				door.reset(Door.reset, time);
 			}
 		}
 	}
 }
