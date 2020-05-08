 package de.kumpelblase2.dragonslair.logging;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.*;
 import org.bukkit.Location;
 import org.bukkit.block.BlockState;
 import de.kumpelblase2.dragonslair.*;
 import de.kumpelblase2.dragonslair.api.ActiveDungeon;
 import de.kumpelblase2.dragonslair.utilities.WorldUtility;
 
 public class LoggingManager
 {
 	private Map<String, Map<Integer, Map<Location, Recoverable>>> m_logEntries = new HashMap<String, Map<Integer, Map<Location, Recoverable>>>();
 	public static final String logQuery = "REPLACE INTO " + Tables.LOG + "(dungeon_name, party_id, log_type, location, before_data, after_data) VALUES(?,?,?,?,?,?)";
 	
 	public Map<String, Map<Integer, Map<Location, Recoverable>>> getEntries()
 	{
 		return this.m_logEntries;
 	}
 	
 	public void loadEntries()
 	{
 		try
 		{
 			PreparedStatement st = DragonsLairMain.createStatement("SELECT * FROM " + Tables.LOG);
 			ResultSet result = st.executeQuery();
 			while(result.next())
 			{
 				LogType type = LogType.valueOf(result.getString(TableColumns.Log.LOG_TYPE).toUpperCase());
 				Recoverable entry = null;
 				Map<String, String> before = new HashMap<String, String>();
 				Map<String, String> after = new HashMap<String, String>();
 				String[] split = result.getString(TableColumns.Log.BEFORE_DATA).split(";");
 				for(String s : split)
 				{
 					String[] value = s.split("" + ((byte)0x1D));
 					before.put(value[0], value[1]);
 				}
 				
 				split = result.getString(TableColumns.Log.AFTER_DATA).split(";");
 				for(String s : split)
 				{
 					String[] value = s.split("" + ((byte)0x1D));
 					after.put(value[0], value[1]);
 				}
 				String dungeon = result.getString(TableColumns.Log.DUNGEON_NAME);
 				int party = result.getInt(TableColumns.Log.PARTY_ID);
 				Location loc = WorldUtility.stringToLocation(result.getString(TableColumns.Log.LOCATION));
 				
 				switch(type)
 				{
 					case BLOCK_CHANGE:
 						entry = new BlockContentChangeEntry(dungeon, party, loc, before, after);
 						break;
 					case BLOCK_REMOVE:
 						entry = new BlockBreakEntry(dungeon, party, loc, before, after);
 						break;
 					case BLOCK_PLACE:
 						entry = new BlockPlaceEntry(dungeon, party, loc, before, after);
 						break;
 					default:
 						break;
 					
 				}
 				this.addEntry(dungeon, party, entry);
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public Map<Integer, Map<Location, Recoverable>> getEntriesForDungeon(String inDungeons)
 	{
 		if(!this.m_logEntries.containsKey(inDungeons))
 			return new HashMap<Integer, Map<Location, Recoverable>>();
 		else
 			return this.m_logEntries.get(inDungeons);
 	}
 	
 	public void logBlockBreak(ActiveDungeon ad, BlockState inBroken)
 	{
 		BlockBreakEntry entry = new BlockBreakEntry(inBroken, ad);
 		this.logEntry(ad, inBroken, entry);
 	}
 	
 	public void logBlockPlace(ActiveDungeon ad, BlockState inPlaced)
 	{
 		BlockPlaceEntry entry = new BlockPlaceEntry(inPlaced, ad);
 		this.logEntry(ad, inPlaced, entry);
 	}
 	
 	public void logBlockContentChange(ActiveDungeon ad, BlockState inCurrent, Map<String, String> inNew, Map<String, String> inOldItem)
 	{
 		BlockContentChangeEntry entry = new BlockContentChangeEntry(inCurrent, ad);
 		entry.m_before.putAll(inOldItem);
 		entry.m_new.putAll(inNew);
 		this.logEntry(ad, inCurrent, entry);
 	}
 	
 	public void logBlockDataChange(ActiveDungeon ad, BlockState inCurrent)
 	{
 		BlockDataChangeEntry entry = new BlockDataChangeEntry(inCurrent, ad);
 		this.logEntry(ad, inCurrent, entry);
 	}
 	
 	public void logEntry(ActiveDungeon ad, BlockState inNew, Recoverable entry)
 	{
 		Map<Integer, Map<Location, Recoverable>> partyEntries = new HashMap<Integer, Map<Location, Recoverable>>();
 		if(this.m_logEntries.containsKey(ad.getInfo().getName()))
 		{
 			partyEntries = this.m_logEntries.get(ad.getInfo().getName());
 			Map<Location, Recoverable> entries = new HashMap<Location, Recoverable>();
 			if(partyEntries.containsKey(ad.getCurrentParty().getID()))
 			{
 				entries = partyEntries.get(ad.getCurrentParty().getID());
 			}
 			
 			if(entries.containsKey(inNew.getLocation()))
 			{
 				Recoverable old = entries.get(inNew.getLocation());
 				if(old.isNegotiation(entry))
 				{
 					old.remove();
 					entries.remove(inNew.getLocation());
 					if(entries.size() == 0)
 					{
 						partyEntries.remove(ad.getCurrentParty().getID());
 						if(partyEntries.size() == 0)
 							this.m_logEntries.remove(ad.getInfo().getName());
 					}
 					return;
 				}
 				entry.setOldData(old.getOldData());
 			}
 			entries.put(inNew.getLocation(), entry);
 			partyEntries.put(ad.getCurrentParty().getID(), entries);
 		}
 		else
 		{
 			HashMap<Location, Recoverable> entries = new HashMap<Location, Recoverable>();
 			entries.put(inNew.getLocation(), entry);
 			partyEntries = new HashMap<Integer, Map<Location, Recoverable>>();
 			partyEntries.put(ad.getCurrentParty().getID(), entries);
 			this.m_logEntries.put(ad.getInfo().getName(), partyEntries);
 		}
 		entry.save();
 	}
 	
 	public void addEntry(String inDungeon, int inParty, Recoverable inEntry)
 	{
		if(inEntry == null)
			return;
		
 		Map<Integer, Map<Location, Recoverable>> partyEntries = new HashMap<Integer, Map<Location, Recoverable>>();
 		if(this.m_logEntries.containsKey(inDungeon))
 		{
 			partyEntries = this.m_logEntries.get(inDungeon);
 			Map<Location, Recoverable> entries = new HashMap<Location, Recoverable>();
 			if(partyEntries.containsKey(inParty))
 			{
 				entries = partyEntries.get(inParty);
 				entries.put(inEntry.getLocation(), inEntry);
 			}
 			partyEntries.put(inParty, entries);
 		}
 		else
 		{
 			HashMap<Location, Recoverable> entries = new HashMap<Location, Recoverable>();
 			entries.put(inEntry.getLocation(), inEntry);
 			partyEntries = new HashMap<Integer, Map<Location, Recoverable>>();
 			partyEntries.put(inParty, entries);
 			this.m_logEntries.put(inDungeon, partyEntries);
 		}
 	}
 }
