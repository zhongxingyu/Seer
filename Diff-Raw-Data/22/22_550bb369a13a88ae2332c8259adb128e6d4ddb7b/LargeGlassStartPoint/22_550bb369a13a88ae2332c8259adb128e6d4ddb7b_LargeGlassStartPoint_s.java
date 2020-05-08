 package com.martinbrook.tesseractuhc.startpoint;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 public class LargeGlassStartPoint extends UhcStartPoint {
 
 	public LargeGlassStartPoint(int number, Location location, boolean hasChest) {
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
 		
 		// 3x3 floor
 		b.getRelative(-1,-1,-1).setType(Material.GLASS);
 		b.getRelative(-1,-1,0).setType(Material.GLASS);
 		b.getRelative(-1,-1,1).setType(Material.GLASS);
 		b.getRelative(0,-1,-1).setType(Material.GLASS);
 		b.getRelative(0,-1,0).setType(Material.GLASS);
 		b.getRelative(0,-1,1).setType(Material.GLASS);
 		b.getRelative(1,-1,-1).setType(Material.GLASS);
 		b.getRelative(1,-1,0).setType(Material.GLASS);
 		b.getRelative(1,-1,1).setType(Material.GLASS);
 		
 		// Four sides
 		b.getRelative(-2,0,-1).setType(Material.GLASS);
 		b.getRelative(-2,0,0).setType(Material.GLASS);
 		b.getRelative(-2,0,1).setType(Material.GLASS);
 		b.getRelative(-2,1,-1).setType(Material.GLASS);
 		b.getRelative(-2,1,0).setType(Material.GLASS);
 		b.getRelative(-2,1,1).setType(Material.GLASS);
 
 		b.getRelative(2,0,-1).setType(Material.GLASS);
 		b.getRelative(2,0,0).setType(Material.GLASS);
 		b.getRelative(2,0,1).setType(Material.GLASS);
 		b.getRelative(2,1,-1).setType(Material.GLASS);
 		b.getRelative(2,1,0).setType(Material.GLASS);
 		b.getRelative(2,1,1).setType(Material.GLASS);
 		
 		b.getRelative(-1,0,-2).setType(Material.GLASS);
 		b.getRelative(0,0,-2).setType(Material.GLASS);
 		b.getRelative(1,0,-2).setType(Material.GLASS);
 		b.getRelative(-1,1,-2).setType(Material.GLASS);
 		b.getRelative(0,1,-2).setType(Material.GLASS);
 		b.getRelative(1,1,-2).setType(Material.GLASS);
 		
 		b.getRelative(-1,0,2).setType(Material.GLASS);
 		b.getRelative(0,0,2).setType(Material.GLASS);
 		b.getRelative(1,0,2).setType(Material.GLASS);
 		b.getRelative(-1,1,2).setType(Material.GLASS);
 		b.getRelative(0,1,2).setType(Material.GLASS);
 		b.getRelative(1,1,2).setType(Material.GLASS);
 
 		
 		// Glowstone corners
 		
 		b.getRelative(-2,-1,-2).setType(Material.GLOWSTONE);
 		b.getRelative(-2,-1,2).setType(Material.GLOWSTONE);
 		b.getRelative(2,-1,-2).setType(Material.GLOWSTONE);
 		b.getRelative(2,-1,2).setType(Material.GLOWSTONE);
				
 		this.placeChest();
 		this.makeSign();
 	}
 
 
 	@Override
 	public Block getChestBlock() { return location.getBlock().getRelative(0,-1,0); }
 
 	@Override
 	public Block getSignBlock() { return location.getBlock().getRelative(0,0,3); }
 	
 }
