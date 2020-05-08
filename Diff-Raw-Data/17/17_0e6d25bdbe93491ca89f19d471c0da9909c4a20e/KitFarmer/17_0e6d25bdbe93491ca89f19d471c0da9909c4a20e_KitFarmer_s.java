 /*
  * AutoWalls by jkush321 is licensed under the
  * Creative Commons Attribution-NonCommercial 3.0 Unported License
  * 
  * You are fully allowed to modify the source code for your own network
  * of servers, but you may not distribute the modified code outside of
  * your servers.
  * 
  * AutoWalls was originally a personal project that was standalone for
  * my own private server, and it slowly accumulated into a giant plugin.
  * 
  * AutoWalls is for dedicated servers that are willing to run just Walls.
  * 
  * The license requires attribution and you have to give credit to jkush321
  * no matter how many changes were made to the code. In some clearly stated
  * way everyone who goes on the server must be able to easily see and be aware
  * of the fact that this code originated from jkush321 and was modified by
  * you or your team.
  * 
  * For more information visit http://bit.ly/AutoWalls
  * 
  */
 
 package com.jkush321.autowalls.kits;
 
 import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 
 public class KitFarmer extends Kit {
 
 	ItemStack[] contents;
 	
 	public KitFarmer()
 	{
 		ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
 		ItemStack shovel = new ItemStack(Material.DIAMOND_SPADE);
 		ItemStack dirt = new ItemStack(Material.DIRT, 64);
 		ItemStack sand = new ItemStack(Material.SAND, 64);
 		ItemStack bonemeal = new ItemStack(Material.INK_SACK, 64, (short)15);//bonemeal
 		ItemStack buckets = new ItemStack(Material.BUCKET, 8);
 		ItemStack ice = new ItemStack(Material.ICE, 16);
 		ItemStack wheatSeed = new ItemStack(Material.SEEDS, 16);
 		ItemStack carrot = new ItemStack(Material.CARROT, 16);
 		ItemStack potato = new ItemStack(Material.POTATO, 16);
 		ItemStack sapling = new ItemStack(Material.SAPLING, 16);
 		ItemStack pumpkin = new ItemStack(Material.PUMPKIN, 16);
 		ItemStack melon = new ItemStack(Material.MELON, 16);
 		ItemStack cactus = new ItemStack(Material.CACTUS, 16);
 		contents = new ItemStack[]{ hoe, shovel, dirt, sand, bonemeal, bonemeal, bonemeal, buckets, ice, wheatSeed, carrot, potato, sapling, pumpkin, melon, cactus };
 	}
 	
 	@Override
 	public ItemStack[] getItemStack() {
 		return contents;
 	}
 
 	@Override
 	public int getRequiredPriority() {
 		return 35;
 	}
 
 }
