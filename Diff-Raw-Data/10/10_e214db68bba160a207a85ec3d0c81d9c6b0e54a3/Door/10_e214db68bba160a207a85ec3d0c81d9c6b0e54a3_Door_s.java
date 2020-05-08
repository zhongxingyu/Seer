 package com.moltendorf.bukkit.intellidoors;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 /**
  * Door wrapper class.
  *
  * @author moltendorf
  */
 abstract class Door extends DoorController implements Runnable {
 	protected Door(final Set instance, final boolean state, final long current) {
 		open = state;
 		power = !instance.powered();
 		set = instance;
 		time = current;
 	}
 
 	protected Door(final Set instance, final boolean state) {
 		open = state;
 		power = !instance.powered();
 		set = instance;
 	}
 
 	// Final data.
 	protected final Set set;
 
 	// Variable data.
 	protected boolean busy = false;
 	protected boolean open = false;
 	protected boolean power = false;
 	protected int task = -2;
 	protected long time = 0;
 	protected Door next = null, previous = null;
 
 	private static Handler CreateSet(final Material material, final Block block, final Listener listener) {
 		final Pair original;
 
 		final int data = GetData(block);
 
 		if (IsBottom(data)) {
 			original = new Pair(block, data);
 
 			if (IsNotDoor(material, original.top)) {
 				return null;
 			}
 		} else {
 			original = new Pair(GetBottom(block), block, data-8);
 
 			if (IsNotDoor(material, original.bottom)) {
 				return null;
 			}
 		}
 
 		final Pair primary, secondary;
 
 		primary = original.reverse() ? original.getPrimary(material) : null;
 		secondary = original.forward() ? original.getSecondary(material) : null;
 
 		if (primary != null) {
 			if (secondary != null) {
 				// We don't let doors be members of multiple sets.
 				return listener.make(material, new Set_Door_Single(original), original.open(data));
 			}
 
 			if (primary.getPrimary(material) != null) {
 				// We don't let doors be members of multiple sets.
 				return listener.make(material, new Set_Door_Single(original), original.open(data));
 			}
 
 			original.forward = false;
 
 			return listener.make(material, new Set_Door_Double(primary, original), original.open(data), false);
 		} else if (secondary != null) {
 			if (secondary.getSecondary(material) != null) {
 				// We don't let doors be members of multiple sets.
 				return listener.make(material, new Set_Door_Single(original), original.open(data));
 			}
 
 			return listener.make(material, new Set_Door_Double(original, secondary), original.open(data), true);
 		}
 
 		return listener.make(material, new Set_Door_Single(original), original.open(data));
 	}
 
 	protected static Handler Get(final Material material, final Block block, final Listener listener) {
 		if (material == Material.TRAP_DOOR) {
 			final int data = GetData(block);
 
 			return listener.make(new Set_Trap(block, data), IsOpen(data));
 		}
 
 		return CreateSet(material, block, listener);
 	}
 
 	protected boolean busy() {
 		return busy;
 	}
 
 	protected boolean busy(final long current) {
 		return busy || minimum > current-time;
 	}
 
 	protected boolean power() {
 		return open == power;
 	}
 
 	protected int getPower() {
 		return set.getPower(open);
 	}
 
 	protected int getPower(final boolean side) {
 		return open == side ? 1 : 0;
 	}
 
 	protected void set(final boolean side, final boolean state) {
 		busy = true;
 
 		open = state;
 		power = set.powered();
 		apply(side, open);
 	}
 
 	protected boolean reset() {
 		if (!power && set.powered()) {
 			busy = true;
 
 			open = true;
 			power = true;
 
 			if (apply(open)) {
 				set.sound();
 			}
 
 			busy = false;
 
 			return true;
 		}
 
 		if (task != -1) {
 			if (power()) {
 				return true;
 			} else if (task > -2) {
 				return false;
 			}
 		}
 
 		task = -1;
 		splice();
 
 		return false;
 	}
 
 	private void schedule(final long ticks, final long current) {
 		task = Plugin.scheduler.scheduleSyncDelayedTask(Plugin.instance, this, ticks);
 		time = current;
 	}
 
 	protected void cancel() {
 		Plugin.scheduler.cancelTask(task);
 
 		task = -1;
 	}
 
 	protected void reset(final long ticks, final long current) {
 		if (task >= 0) {
 			cancel();
 		}
 
 		if (ticks >= 0) {
 			schedule(ticks, current);
 		}
 	}
 
 	private void update(final long ticks, final long current) {
 		final long difference = ticks - (current-time)/(1000/rate);
 
 		reset(difference, current);
 	}
 
 	private void update(final long current, final List list, final Door[] doors) {
 		busy = true;
 		cancel();
 
 		final long ticks = open == power ? reset : delay;
 
 		for (int i = 0; i < doors.length; ++i) {
 			doors[i].update(ticks, current);
 		}
 
 		list.splice(this, doors);
 	}
 
 	protected synchronized boolean equals(final Set_Door_Double instance, final List list, final long current) {
 		if (task == -1) {
 			return false;
 		}
 
 		return set.equals(instance, this, list, current);
 	}
 
 	protected synchronized boolean equals(final Set_Door_Single instance, final List list, final long current) {
 		if (task == -1) {
 			return false;
 		}
 
 		return set.equals(instance, this, list, current);
 	}
 
 	protected synchronized boolean equals(final Set_Trap instance) {
 		if (task == -1) {
 			return false;
 		}
 
 		return set.equals(instance);
 	}
 
 	protected void merge(final Set instance, final List list, final long current) {
 		Door[] doors = {make(instance)};
 		update(current, list, doors);
 	}
 
 	protected void split(final Set set, final Pair pair, final List list, final long current) {
 		Door[] doors = {make(set), make(new Set_Door_Single(pair))};
 		update(current, list, doors);
 	}
 
 	protected synchronized void run(final List list) {
 		if (task != -1) {
 			busy = true;
 
 			open = set.powered();
 			power = open;
 
 			if (apply(open)) {
 				set.sound();
 			}
 
 			task = -1;
 
 			list.splice(this);
 		}
 	}
 
 	protected void unlock() {
 		Plugin.scheduler.scheduleSyncDelayedTask(Plugin.instance, new Unlock(this));
 	}
 
 	abstract protected boolean apply(final boolean open);
 	abstract protected boolean apply(final boolean side, final boolean open);
 	abstract protected Door make(final Set set);
 	abstract protected void splice();
 }
 
 class Unlock implements Runnable {
 	private final Door door;
 
 	Unlock(final Door instance) {
 		door = instance;
 	}
 
 	@Override
 	public void run() {
 		synchronized (door) {
 			door.busy = false;
 		}
 	}
 }
