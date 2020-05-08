 package com.martinbrook.tesseractuhc.startpoint;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 public class SmallGlassStartPoint extends UhcStartPoint {
 
 	public SmallGlassStartPoint(int number, Location location, boolean hasChest) {
 		super(number, location, hasChest);
 	}
 
 
 	/**
 	 * Build a starting trough at this start point, and puts a starter chest and sign there.
 	 *
 	 */
 	@Override
 	public void buildStartingTrough() {
 
 		// Get the block containing the player's feet
 		Block b = location.getBlock();
 				
 		// Two blocks of glass below feet
 		b.getRelative(0,-2,0).setType(Material.GLASS);
 		b.getRelative(0,-1,0).setType(Material.GLASS);
 		
 		// Four sides
 		b.getRelative(-1,-1,0).setType(Material.GLOWSTONE);
 		b.getRelative(-1,0,0).setType(Material.GLASS);
 		b.getRelative(-1,1,0).setType(Material.GLASS);
 		
 		b.getRelative(1,-1,0).setType(Material.GLOWSTONE);
 		b.getRelative(1,0,0).setType(Material.GLASS);
 		b.getRelative(1,1,0).setType(Material.GLASS);
 
 		b.getRelative(0,-1,-1).setType(Material.GLOWSTONE);
 		b.getRelative(0,0,-1).setType(Material.GLASS);
 		b.getRelative(0,1,-1).setType(Material.GLASS);
 
 		b.getRelative(0,-1,1).setType(Material.GLOWSTONE);
 		b.getRelative(0,0,1).setType(Material.GLASS);
 		b.getRelative(0,1,1).setType(Material.GLASS);
 		
 		// Fill with air
		b.getRelative(0,2,0).setType(Material.AIR);
 		b.getRelative(0,1,0).setType(Material.AIR);
 		b.setType(Material.AIR);
 		
 		
 		this.placeChest();
 		this.makeSign();
 	}
 
 
 	@Override
 	public Block getChestBlock() { return location.getBlock().getRelative(0,-1,0); }
 
 	@Override
 	public Block getSignBlock() { return location.getBlock().getRelative(0,0,2); }
 }
