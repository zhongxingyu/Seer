 package net.year4000.eprotect;
 
 import java.util.EnumSet;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.material.Attachable;
 
 import com.sk89q.commandbook.CommandBook;
 
 public class Protected {
 	
 	Set<BlockFace> blockFaces = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
     Set<BlockFace> blockUpDown = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
     boolean result = false;
 	
 	public boolean isProtected(Block block, Player player){
 		switch(block.getType()){
 			case WALL_SIGN:
 				checkSign(block, player);
 				return result;
 			case CHEST:
 				checkChest(block, player);
 				return result;
 			case IRON_DOOR:
 				return false;
 			case WOODEN_DOOR:
 				return false;
 			case TRAP_DOOR:
 				return false;
 			default:
				checkChunk(block, player);
 				checkBlock(block, player);
 		}
 		return result;
 	}
 	
 	private void checkSign(Block block, Player player){
 		Sign sign = (Sign) block.getState();
 		String[] lines = sign.getLines();
 		
 		if(lines[0].equalsIgnoreCase("[Protect]")){
 			if(player != null){
 				boolean override = false;
 				try {
 					CommandBook.inst().checkPermission(player, "eprotect.override");
 					override = true;
 				} catch (Exception e) {}
 				Boolean locked = true;
 				for(String line : lines){
 					if(line.equalsIgnoreCase(player.getName())){
 						locked = false;
 						break;
 					}
 				}
 				if(locked){
 					if(override){
 						result = false;
 						player.sendMessage(ChatColor.RED + "You are bypassing " +	lines[1] + "'s protection.");
 					} else{
 						result = true;
 						player.sendMessage(ChatColor.GOLD + "NOTICE: " + ChatColor.YELLOW + "This block is protected by " +	lines[1] + ".");
 					}
 				}
 			} else{
 				result = true;
 			}
 		}
 	}
 
 	private void checkBlock(Block block, Player player){
 		for(BlockFace blockface : blockFaces){
 			Block face = block.getRelative(blockface);
 			if(face.getType() == Material.WALL_SIGN){
 				Sign sign = (Sign) face.getState();
 				Attachable direction = (Attachable) sign.getData();
 				BlockFace blockfacesign = direction.getAttachedFace().getOppositeFace();
 				if(blockface.equals(blockfacesign)){
 					checkSign(face, player);
 				}
 			}
 		}
 	}
 	
 	private void checkChest(Block block, Player player){
 		for (BlockFace blockface : blockFaces) {
             Block adjacent = block.getRelative(blockface);
             if (adjacent.getState() instanceof Chest) {
             	checkBlock(adjacent, player);
             } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
 				Sign sign = (Sign) adjacent.getState();
 				Attachable direction = (Attachable) sign.getData();
 				BlockFace blockfacesign = direction.getAttachedFace().getOppositeFace();
 				if(blockface.equals(blockfacesign)){
 					checkSign(adjacent, player);
 				}
             }
         }
 	}
 	
 	
 	
 	public void checkChunk(Block block, Player player){
 		Chunk chunk = block.getChunk();
 		int blockX = chunk.getX();
 		int blockZ = chunk.getZ();
 		
 		int minX = blockX*16;
 		int minZ = blockZ*16;
 		int maxX = ((blockX*16)+16);
 		int maxZ = ((blockZ*16)+16);
 		int minY = 0;
 		int maxY = 0;
 		
 		try{
 			for(int b = minX; b < maxX; b++){
 				for(int c = minZ; c < maxZ; c++){
 					int currentY = chunk.getChunkSnapshot().getHighestBlockYAt(b, c);
 					if(currentY > maxY){
 						maxY = currentY;
 					}
 				}
 			}
 		} catch(Exception e){
 			maxY = 256;
 		}
 		
 		for(int a = minY; a < maxY; a++){
 			for(int b = minX; b < maxX; b++){
 				for(int c = minZ; c < maxZ; c++){
 					Block currentBlock = chunk.getBlock(b, a, c);
 					if(currentBlock.getType() == Material.SIGN_POST){
 						checkSign(currentBlock, player);
 						break;
 					}
 				}
 			}
 		}
 	}
 }
