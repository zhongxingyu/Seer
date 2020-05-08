 package me.arno.blocklog.schedules;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.logs.BlockEntry;
 import me.arno.blocklog.logs.ChestEntry;
 import me.arno.blocklog.logs.DataEntry;
 import me.arno.blocklog.logs.InteractionEntry;
 import me.arno.blocklog.managers.DatabaseManager;
 import me.arno.blocklog.managers.QueueManager;
 import me.arno.blocklog.util.Query;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 public class SaveSchedule implements Runnable {
 	private final CommandSender sender;
 	private final int count;
 	private final boolean messages;
 	
 	public SaveSchedule(int count) {
 		this(count, null, false);
 	}
 
 	public SaveSchedule(int count, CommandSender sender) {
 		this(count, sender, true);
 	}
 
 	public SaveSchedule(int count, CommandSender sender, boolean messages) {
 		this.count = count;
 		this.sender = sender;
 		this.messages = messages;
 	}
 	
 	private QueueManager getQueueManager() {
 		return BlockLog.getInstance().getQueueManager();
 	}
 	
 	@Override
 	public void run() {
 		if(messages && sender != null) sender.sendMessage(ChatColor.DARK_RED + "[BlockLog]" + ChatColor.GOLD + " Saving " + ((count == 0) ? "all the" : count) + " logs");
 		
 		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
 		List<HashMap<String, Object>> blocks = new ArrayList<HashMap<String, Object>>();
 		List<HashMap<String, Object>> chests = new ArrayList<HashMap<String, Object>>();
 		List<HashMap<String, Object>> interactions = new ArrayList<HashMap<String, Object>>();
 		
 		List<DataEntry> dataEntries = (count == 0) ? getQueueManager().getDataEntries() : getQueueManager().getDataEntries(count);
 		List<BlockEntry> blockEntries = (count == 0) ? getQueueManager().getBlockEntries() : getQueueManager().getBlockEntries(count);
 		List<ChestEntry> chestEntries = (count == 0) ? getQueueManager().getChestEntries() : getQueueManager().getChestEntries(count);
 		List<InteractionEntry> interactionEntries = (count == 0) ? getQueueManager().getInteractionEntries() : getQueueManager().getInteractionEntries(count);
 		
 		for(DataEntry dataEntry : dataEntries)
 			data.add(dataEntry.getValues());
 		
 		for(BlockEntry blockEntry : blockEntries)
 			blocks.add(blockEntry.getValues());
 		
 		for(ChestEntry chestEntry : chestEntries)
 			chests.add(chestEntry.getValues());
 		
 		for(InteractionEntry interactionEntry : interactionEntries)
 			interactions.add(interactionEntry.getValues());
 		
 		try {
 			Query query = new Query();
 			
 			query.from(DatabaseManager.databasePrefix + "data");
 			query.insert(data);
 			
 			dataEntries.clear();
 			
 			query.from(DatabaseManager.databasePrefix + "blocks");
 			query.insert(blocks);
 			
 			blockEntries.clear();
 			
 			query.from(DatabaseManager.databasePrefix + "chests");
 			query.insert(chests);
 			
 			chestEntries.clear();
 			
 			query.from(DatabaseManager.databasePrefix + "interactions");
 			query.insert(interactions);
 			
 			interactionEntries.clear();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		if(messages && sender != null) sender.sendMessage(ChatColor.DARK_RED + "[BlockLog]" + ChatColor.GOLD + " Successfully saved " + ((count == 0) ? "all the" : count) + " logs");
 	}
 }
