 package me.cheddar262.RedstoneJukeboxTrig;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Jukebox;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 public class RJTBlockListener implements Listener{
 	public RedstoneJukeboxTrig plugin;
 	RJTPlayerListener PListener = new RJTPlayerListener(plugin);
 	
 	public RJTBlockListener (RedstoneJukeboxTrig instance) {
 		plugin = instance;
 	}
 	@EventHandler
 	public void onBlockRedstoneChange(BlockRedstoneEvent event){
		Block reds = (Block) event.getBlock();
		checkPowerJukebox(reds.getRelative(BlockFace.NORTH));
		checkPowerJukebox(reds.getRelative(BlockFace.SOUTH));
		checkPowerJukebox(reds.getRelative(BlockFace.WEST));
		checkPowerJukebox(reds.getRelative(BlockFace.EAST));
 	}
 	private void checkPowerJukebox (Block block){
 		if( block.getType() == Material.JUKEBOX){
 			boolean redPow = redPowerChecker(block);
 			if(redPow && (!block.isBlockPowered())){
 				Jukebox jukeBox = (Jukebox) block.getState();
 				String pos = jukeBox.getX() + "," + jukeBox.getY() + "," + jukeBox.getZ();
 				long StartTime = System.currentTimeMillis();
 				if(jukeBox.getPlaying() != Material.AIR)
 				{
 					if(RJTPlayerListener.posETime.containsKey(pos)){
 						long eTimeTwo = RJTPlayerListener.posETime.get(pos).longValue();
 						if(eTimeTwo <= (System.currentTimeMillis())){
 							long EndTime = RJTPlayerListener.findEndTime(StartTime, jukeBox.getPlaying());
 							RJTPlayerListener.posETime.put(pos, EndTime);
 							jukeBox.setPlaying(jukeBox.getPlaying());
 						}
 					}
 					else{
 						long EndTime = RJTPlayerListener.findEndTime(StartTime, jukeBox.getPlaying());
 						RJTPlayerListener.posETime.put(pos, EndTime);
 						jukeBox.setPlaying(jukeBox.getPlaying());
 					}
 				}
 			}
 		}
 	}
 	private boolean redPowerChecker(Block block) {
 		Material BlockNorthEastType = block.getRelative(BlockFace.NORTH_EAST).getType();
 		Material BlockNorthWestType = block.getRelative(BlockFace.NORTH_WEST).getType();
 		Material BlockSouthEastType = block.getRelative(BlockFace.SOUTH_EAST).getType();
 		Material BlockSouthWestType = block.getRelative(BlockFace.SOUTH_WEST).getType();
 		boolean NE = false;
 		boolean NW = false;
 		boolean SE = false;
 		boolean SW = false;
 		if((BlockNorthEastType != Material.REDSTONE_WIRE) && (BlockNorthEastType != Material.REDSTONE_TORCH_ON) && (BlockNorthEastType != Material.REDSTONE_TORCH_OFF) && (BlockNorthEastType != Material.WOOD_PLATE) && (BlockNorthEastType != Material.STONE_PLATE) && (BlockNorthEastType != Material.LEVER)) {
 			NE = true;
 		}
 		if((BlockNorthWestType != Material.REDSTONE_WIRE) && (BlockNorthWestType != Material.REDSTONE_TORCH_ON) && (BlockNorthWestType != Material.REDSTONE_TORCH_OFF) && (BlockNorthWestType != Material.WOOD_PLATE) && (BlockNorthWestType != Material.STONE_PLATE) && (BlockNorthWestType != Material.LEVER)) {
 			NW = true;
 		}
 		if((BlockSouthEastType != Material.REDSTONE_WIRE) && (BlockSouthEastType != Material.REDSTONE_TORCH_ON) && (BlockSouthEastType != Material.REDSTONE_TORCH_OFF) && (BlockSouthEastType != Material.WOOD_PLATE) && (BlockSouthEastType != Material.STONE_PLATE) && (BlockSouthEastType != Material.LEVER)) {
 			SE = true;
 		}
 		if((BlockSouthWestType != Material.REDSTONE_WIRE) && (BlockSouthWestType != Material.REDSTONE_TORCH_ON) && (BlockSouthWestType != Material.REDSTONE_TORCH_OFF) && (BlockSouthWestType != Material.WOOD_PLATE) && (BlockSouthWestType != Material.STONE_PLATE) && (BlockSouthWestType != Material.LEVER)) {
 			SW = true;
 		}
 		if(NE && NW && SE && SW){
 			return true;
 		} else{
 			return false;
 		}
 	}
 }
