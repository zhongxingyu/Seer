 package com.MoofIT.Minecraft.BlueTelepads;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 
 public class BlueTelepadsBlockListener extends BlockListener {
 	private BlueTelepads plugin;
 
 	public BlueTelepadsBlockListener(BlueTelepads instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public void onBlockBreak(BlockBreakEvent event) {
 		Block block = event.getBlock();
 
 		if (block.getType() != Material.WALL_SIGN) return;
 
 		Sign sign = (Sign)block.getState();
 		String[] line0 = sign.getLine(0).split(":");
 
 		if (line0.length != 2 || !line0[0].equals("BlueTelepads") || !sign.getLine(1).equals(sign.getBlock().getWorld().getName())) return;
 		if (!event.getPlayer().hasPermission("bluetelepads.destroy")) {
 			event.setCancelled(true);
 			sign.update();
 			return;
 		}
 	}
 
	//ugh. make this static? add utlity class or telepad class? hack for now - duplicated function from playerlistener
 	public boolean isTelepadLapis(Block lapisBlock) {
 		if (lapisBlock.getTypeId() != plugin.telepadCenterID) return false;
 
 		//get the data val of the slab to the north to check that all slabs are the same
 		short slabType = lapisBlock.getRelative(BlockFace.NORTH).getData();
 		if (slabType != plugin.telepadSurroundingNormal && slabType != plugin.telepadSurroundingFree) return false;
 
 		BlockFace[] surroundingChecks = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
 		for (BlockFace check : surroundingChecks) {
 			if (lapisBlock.getRelative(check).getTypeId() != 43 && lapisBlock.getRelative(check).getData() != slabType) return false;
 		}
 
 		if (lapisBlock.getRelative(BlockFace.DOWN).getType() != Material.SIGN_POST && lapisBlock.getRelative(BlockFace.DOWN).getType() != Material.WALL_SIGN) return false;
 		if (lapisBlock.getRelative(BlockFace.UP).getType() != Material.STONE_PLATE) return false;
 
 		return true;
 	}
 }
