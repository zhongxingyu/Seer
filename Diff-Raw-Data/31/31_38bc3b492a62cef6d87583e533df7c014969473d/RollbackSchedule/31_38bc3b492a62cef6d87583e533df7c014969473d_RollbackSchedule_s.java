 package me.arno.blocklog.schedules;
 
 import java.util.ArrayList;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.Rollback;
 import me.arno.blocklog.Undo;
 import me.arno.blocklog.logs.BlockEntry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class RollbackSchedule implements Runnable {
 	private final BlockLog plugin;
 	private final Player player;
 	private final int rollback;
 	private final int limit;
 	private final int totalBlocks;
 	
 	private final boolean isRollback;
 	
 	private ArrayList<BlockEntry> blockEntries = new ArrayList<BlockEntry>();
 	
 	private int blockCount = 0;	
 	private int sid;
 	
 	public RollbackSchedule(Undo undo) {
 		this.plugin = BlockLog.getInstance();
 		this.player = undo.getSender();
 		this.limit = undo.getLimit();
		this.rollback = undo.getRollback();
 		
 		this.isRollback = false;
 		
 		this.blockEntries = undo.getBlocks();
 		this.totalBlocks = Integer.valueOf(blockEntries.size());
 	}
 	
 	public RollbackSchedule(Rollback rb) {
 		this.plugin = BlockLog.getInstance();
 		this.player = rb.getSender();
 		this.limit = rb.getLimit();
 		this.rollback = rb.getId();
 		
 		this.isRollback = true;
 		
 		this.blockEntries = rb.getBlocks();
 		this.totalBlocks = Integer.valueOf(blockEntries.size());
 	}
 	
 	public void setId(int sid) {
 		this.sid = sid;
 	}
 	
 	public int getBlockCount() {
 		return totalBlocks;
 	}
 	
 	@Override
 	public void run() {
 		for(int i=0;i<limit;i++) {
 			if(blockEntries.size() > 0) {
 				BlockEntry blockEntry = blockEntries.get(0);
 				if(blockEntry.rollback(rollback))
 					blockCount++;
 				
 				blockEntries.remove(0);
 			} else {
 				player.sendMessage(ChatColor.DARK_RED + "[BlockLog] " + ChatColor.GREEN + blockCount + ChatColor.GOLD + " blocks of the " + ChatColor.GREEN + totalBlocks + ChatColor.GOLD + " blocks changed!");
 				if(isRollback)
 					player.sendMessage(ChatColor.DARK_RED + "[BlockLog] " + ChatColor.GOLD + "use the command " + ChatColor.GREEN + "/bl undo " + rollback + ChatColor.GOLD + " to undo this rollback!");
 				plugin.getSchedules().remove(sid);
 				plugin.getServer().getScheduler().cancelTask(sid);
 				break;
 			}
 		}
 	}
 }
