 package com.turt2live.antishare;
 
 import java.util.Vector;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 public class VirtualPerWorldStorage {
 
 	private AntiShare plugin;
 	private World world;
 	private Vector<Integer> blocked_break = new Vector<Integer>();
 	private Vector<Integer> blocked_place = new Vector<Integer>();
 	private Vector<Integer> blocked_drop = new Vector<Integer>();
 	private Vector<Integer> blocked_death = new Vector<Integer>();
 	private Vector<Integer> blocked_interact = new Vector<Integer>();
 	private Vector<String> blocked_commands = new Vector<String>();
 	private Vector<Block> creative_blocks = new Vector<Block>();
 	private boolean blocked_bedrock = false;
 
 	public VirtualPerWorldStorage(World world, AntiShare plugin){
 		this.plugin = plugin;
		this.world = world;
 		build();
 	}
 
 	public void build(){
 		blocked_break.clear();
 		blocked_place.clear();
 		blocked_drop.clear();
 		blocked_death.clear();
 		blocked_interact.clear();
 		blocked_commands.clear();
 		creative_blocks.clear();
 		blocked_bedrock = false;
 		reload();
 	}
 
 	public boolean command(String command, BlockedType type){
 		return blocked_commands.contains(command);
 	}
 
 	public boolean isBlocked(Material material, BlockedType type){
 		switch (type){
 		case BEDROCK:
 			return blocked_bedrock;
 		}
 		return false;
 	}
 
 	public void reload(){
 		boolean flatfile = true;
 		if(plugin.getSQLManager() != null){
 			if(plugin.getSQLManager().isConnected()){
 				flatfile = false;
 				//SQLManager sql = plugin.getSQLManager();
 				// TODO: Load in all vars
 			}
 		}
		blocked_bedrock = !plugin.config().getBoolean("other.allow_bedrock", world);
 		if(flatfile){
 
 		}
 	}
 }
