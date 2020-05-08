 package com.comze_instancelabs.skins.mobs;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 public class Slime {
 	public static void buildSlime(Location start, String direction){
 		buildHead(start, direction);
 	}
 
 	public static void buildHead(Location start, String d){
 		for(int i__ = 0; i__ < 16; i__++){
 			for(int i_ = 0; i_ < 16; i_++){
 				for(int i = 0; i < 16; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i, start.getBlockZ() - i_));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i__ + 1, start.getBlockY() + i, start.getBlockZ() + i_));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_, start.getBlockY() + i, start.getBlockZ() - i__ - 1));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_, start.getBlockY() + i, start.getBlockZ() + i__ + 1));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i, start.getBlockZ() - i_));
 					}
 					b.setType(Material.STAINED_GLASS);
 					b.setData((byte)13);
 				}
 			}
 		}
 		
 		
 		for(int i__ = 0; i__ < 12; i__++){
 			for(int i_ = 0; i_ < 12; i_++){
 				for(int i = 0; i < 12; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 3, start.getBlockY() + i + 2, start.getBlockZ() - i_ - 2));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i__ + 3, start.getBlockY() + i + 2, start.getBlockZ() + i_ + 2));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ + 2, start.getBlockY() + i + 2, start.getBlockZ() - i__ - 3));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 2, start.getBlockY() + i + 2, start.getBlockZ() + i__ + 3));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 3, start.getBlockY() + i + 2, start.getBlockZ() - i_ - 2));
 					}
 					b.setType(Material.WOOL);
 					b.setData((byte)5);
 				}
 			}
 		}
 		
 		
 		
 		// eyes:
 		
 		for(int i_ = 0; i_ < 5; i_++){
 			for(int i = 0; i < 5; i++){
 				Block b;
 				if(d.equalsIgnoreCase("west")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + i + 8, start.getBlockZ() - i_ - 1));
 				}else if(d.equalsIgnoreCase("east")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + i + 8, start.getBlockZ() + i_ + 1));
 				}else if(d.equalsIgnoreCase("north")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ + 1, start.getBlockY() + i + 8, start.getBlockZ() - 2));
 				}else if(d.equalsIgnoreCase("south")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 1, start.getBlockY() + i + 8, start.getBlockZ() + 2));
 				}else{
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + i + 8, start.getBlockZ() - i_ - 1));
 				}
 				b.setType(Material.WOOL);
 				b.setData((byte)13);
 			}
 		}
 		
 		for(int i_ = 0; i_ < 5; i_++){
 			for(int i = 0; i < 5; i++){
 				Block b;
 				if(d.equalsIgnoreCase("west")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 3, start.getBlockY() + i + 8, start.getBlockZ() - 1));
 				}else if(d.equalsIgnoreCase("east")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ + 3, start.getBlockY() + i + 8, start.getBlockZ() + 1));
 				}else if(d.equalsIgnoreCase("north")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 1, start.getBlockY() + i + 8, start.getBlockZ() + i_ + 3));
 				}else if(d.equalsIgnoreCase("south")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 1, start.getBlockY() + i + 8, start.getBlockZ() - i_ - 3));
 				}else{
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 1, start.getBlockY() + i + 8, start.getBlockZ() - 2));
 				}
 				b.setType(Material.WOOL);
 				b.setData((byte)13);
 			}
 		}
 		
 		
 		
 		
 		
 		for(int i_ = 0; i_ < 5; i_++){
 			for(int i = 0; i < 5; i++){
 				Block b;
 				if(d.equalsIgnoreCase("west")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + i + 8, start.getBlockZ() - i_ - 10));
 				}else if(d.equalsIgnoreCase("east")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + i + 8, start.getBlockZ() + i_ + 10));
 				}else if(d.equalsIgnoreCase("north")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ + 10, start.getBlockY() + i + 8, start.getBlockZ() - 2));
 				}else if(d.equalsIgnoreCase("south")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 10, start.getBlockY() + i + 8, start.getBlockZ() + 2));
 				}else{
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + i + 8, start.getBlockZ() - i_ - 5));
 				}
 				b.setType(Material.WOOL);
 				b.setData((byte)13);
 			}
 		}
 		
 		
 		for(int i_ = 0; i_ < 5; i_++){
 			for(int i = 0; i < 5; i++){
 				Block b;
 				if(d.equalsIgnoreCase("west")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 3, start.getBlockY() + i + 8, start.getBlockZ() - 14));
 				}else if(d.equalsIgnoreCase("east")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ + 3, start.getBlockY() + i + 8, start.getBlockZ() + 14));
 				}else if(d.equalsIgnoreCase("north")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 14, start.getBlockY() + i + 8, start.getBlockZ() + i_ + 3));
 				}else if(d.equalsIgnoreCase("south")){
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 14, start.getBlockY() + i + 8, start.getBlockZ() - i_ - 3));
 				}else{
 					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 1, start.getBlockY() + i + 8, start.getBlockZ() - 2));
 				}
 				b.setType(Material.WOOL);
 				b.setData((byte)13);
 			}
 		}
 
 		
 		
 		// mouth
 		for(int i_ = 0; i_ < 2; i_++){
 			for(int i = 0; i < 2; i++){
 				Block b;
 				if(d.equalsIgnoreCase("west")){
					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + i + 4, start.getBlockZ() - i_ - 8));
 				}else if(d.equalsIgnoreCase("east")){
					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + i + 4, start.getBlockZ() + i_ + 8));
 				}else if(d.equalsIgnoreCase("north")){
					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ - 8, start.getBlockY() + i + 4, start.getBlockZ() + 2));
 				}else if(d.equalsIgnoreCase("south")){
					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ + 8, start.getBlockY() + i + 4, start.getBlockZ() - 2));
 				}else{
					b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + i + 4, start.getBlockZ() - i_ - 8));
 				}
 				b.setType(Material.WOOL);
 				b.setData((byte)13);
 			}
 		}
 
 	}
 	
 	public static void undoSlime(Location start, String d){
 		World w = start.getWorld();
 		for(int i__ = 0; i__ < 16; i__++){
 			for(int i_ = 0; i_ < 16; i_++){
 				for(int i = 0; i < 16; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() - i__ - 1, start.getBlockY() + i, start.getBlockZ() - i_));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() + i__ + 1, start.getBlockY() + i, start.getBlockZ() + i_));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() + i_, start.getBlockY() + i, start.getBlockZ() - i__ - 1));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() - i_, start.getBlockY() + i, start.getBlockZ() + i__ + 1));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() - i__ - 1, start.getBlockY() + i, start.getBlockZ() - i_));
 					}
 					if(b.getType() == Material.WOOL || b.getType() == Material.STAINED_GLASS){
 						b.setType(Material.AIR);
 					}
 				}
 			}	
 		}
 	}
 }
