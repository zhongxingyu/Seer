 package com.moltendorf.bukkit.intellidoors;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 /**
  * Door pair.
  *
  * @author moltendorf
  */
 class Pair extends BlockInfo {
 	Pair(final Block lower, final Block upper, final int data, final boolean orientation) {
 		rotation = GetRotation(data);
 
 		bottom = lower;
 		top = upper;
 
 		forward = orientation ? IsForward(top, rotation) : !IsReverse(top, rotation);
 
 		// Get the location of the block.
 		location = top.getLocation();
 	}
 
 	Pair(final Block lower, final Block upper, final int data) {
 		this(lower, GetTop(lower), data, true);
 	}
 
 	Pair(final Block lower, final int data, final boolean orientation) {
 		this(lower, GetTop(lower), data, orientation);
 	}
 
 	Pair(final Block lower, final int data) {
 		this(lower, data, true);
 	}
 
 	// Final data.
 	private final int rotation;
 	protected final Block bottom, top;
 	protected final Location location;
 
 	// Variable data.
 	protected boolean forward;
 
 	protected boolean apply(final boolean open, final Material material) {
		final int current = GetData(bottom);
 		final int data = forward == open ? rotation+4 : rotation;
 
		if (verifyDoor(material, current) || current == data) {
 			return false;
 		}
 
 		SetData(bottom, data, true);
 		SetData(top, data+8, true);
 
 		return true;
 	}
 
 	protected boolean equals(Pair pair) {
		if (location.equals(pair.location) && rotation == pair.rotation) {
			forward = pair.forward;

			return true;
		}

		return false;
 	}
 
 	protected boolean open(final int data) {
 		return forward == IsOpen(data);
 	}
 
 	protected int getPower(final boolean open) {
 		return forward ? 1 : 0;
 	}
 
 	protected boolean powered() {
 		return IsPowered(bottom) || IsPowered(top);
 	}
 
 	protected boolean forward() {
 		return IsForward(top, rotation);
 	}
 
 	protected boolean reverse() {
 		return IsReverse(top, rotation);
 	}
 
	protected boolean verifyDoor(final Material material, final int data) {
		return IsNotDoor(material, bottom) || IsNotDoor(material, top) || rotation != GetRotation(data);
 	}
 
 	protected Pair getPrimary(final Material material) {
 		if (!reverse()) {
 			return null;
 		}
 
 		final Block block = GetFront(bottom, rotation);
 		final int data = GetData(block);
 
 		if (IsNotDoor(material, block) || IsTop(data)) {
 			return null;
 		}
 
 		final Pair primary = new Pair(block, data);
 
 		if (primary.forward() && VerifySet(primary.rotation, rotation)) {
 			return primary;
 		}
 
 		return null;
 	}
 
 	protected Pair getSecondary(final Material material) {
 		if (!forward()) {
 			return null;
 		}
 
 		final Block block = GetRight(bottom, rotation);
 		final int data = GetData(block);
 
 		if (IsNotDoor(material, block) || IsTop(data)) {
 			return null;
 		}
 
 		final Pair secondary = new Pair(block, data, false);
 
 		if (secondary.reverse() && VerifySet(rotation, secondary.rotation)) {
 			return secondary;
 		}
 
 		return null;
 	}
 }
