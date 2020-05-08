 package au.com.mineauz.PlayerSpy.monitoring;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
 import au.com.mineauz.PlayerSpy.Cause;
 import au.com.mineauz.PlayerSpy.RecordList;
 import au.com.mineauz.PlayerSpy.SpyPlugin;
 import au.com.mineauz.PlayerSpy.LogTasks.Task;
 import au.com.mineauz.PlayerSpy.Records.*;
 import au.com.mineauz.PlayerSpy.Utilities.Pair;
 import au.com.mineauz.PlayerSpy.Utilities.Utility;
 import au.com.mineauz.PlayerSpy.debugging.Debug;
 import au.com.mineauz.PlayerSpy.monitoring.CrossReferenceIndex.SessionInFile;
 import au.com.mineauz.PlayerSpy.tracdata.LogFileRegistry;
 
 /**
  * Used to find the causers of specific things. 
  * This is not for inspecting with. It is a helper for logging
  * @author Schmoller
  */
 public class CauseFinder 
 {
 	private HashMap<Location, Cause> mCurrentBlockTasks;
 	private HashMap<Location, Future<Cause>> mCurrentFutures;
 	
 	public CauseFinder()
 	{
 		mCurrentBlockTasks = new HashMap<Location, Cause>();
 		mCurrentFutures = new HashMap<Location, Future<Cause>>();
 	}
 	
 	/**
 	 * Finds the last cause for the block at that location
 	 * @param loc The location of the block to check
 	 * @return Either a cause if one was immediately found, or a placeholder cause if a further search needs to be done. If a further search needs to be done, it will be scheduled and when completed, the returned cause will be updated with the found details
 	 */
 	public Cause getCauseFor(Location loc)
 	{
 		// Search through the currently buffered data for an answer
 		Debug.fine("Looking for cause of " + Utility.locationToStringShort(loc));
 		
 		Pair<Long, Cause> answer = null;
 		for(ShallowMonitor mon : GlobalMonitor.instance.getAllMonitors())
 		{
 			List<Pair<String, RecordList>> inBuffer = mon.getBufferedRecords();
 			for(Pair<String, RecordList> pair : inBuffer)
 			{
 				// Get the cause
 				Cause cause;
 				if(pair.getArg1() == null)
 					cause = Cause.playerCause(mon.getMonitorTarget());
 				else
 					cause = Cause.playerCause(mon.getMonitorTarget(), pair.getArg1());
 				
 				// Now filter the records to find just what we are looking for
 				for(Record record : pair.getArg2())
 				{
 					if(record.getType() != RecordType.BlockChange)
 						continue;
 
 					if(!((BlockChangeRecord)record).wasPlaced())
 						continue;
 					
 					if(((BlockChangeRecord)record).getLocation().equals(loc));
 					{
 						// Record it
 						if(answer == null)
 							answer = new Pair<Long, Cause>(record.getTimestamp(), cause);
 						else if(record.getTimestamp() > answer.getArg1())
 							answer = new Pair<Long, Cause>(record.getTimestamp(), cause);
 					}
 				}
 			}
 		}
 		
 		// Now see if we have an answer
 		if(answer != null)
 		{
 			Debug.fine("Found cause in active buffers. Result: %s", answer.getArg2().friendlyName());
 			return answer.getArg2();
 		}
 		else
 		{
 			// It has not immediatly been found, send off to a worker thread to find the cause
 			Cause cause = null;
 			
 			if(mCurrentBlockTasks.containsKey(loc))
 				cause = mCurrentBlockTasks.get(loc);
 			else
 			{
 				Debug.fine("Cause not found in buffered records for " + Utility.locationToStringShort(loc) + ". Submitting search task.");
 				cause = Cause.placeholderCause();
 			
 				mCurrentBlockTasks.put(loc, cause);
 				mCurrentFutures.put(loc, SpyPlugin.getExecutor().submit(new BlockSearchTask(loc)));
 			}
 			
 			return cause;
 		}
 	}
 	
 	public void update()
 	{
 		Iterator<Entry<Location, Future<Cause>>> it = mCurrentFutures.entrySet().iterator();
 		
 		while(it.hasNext())
 		{
 			Entry<Location, Future<Cause>> ent = it.next();
 			if(ent.getValue().isDone())
 			{
 				// Notify that its done
 				try 
 				{
 					Cause foundCause = ent.getValue().get();
 					CauseFoundEvent event = new CauseFoundEvent(mCurrentBlockTasks.get(ent.getKey()), foundCause, ent.getKey());
 					Bukkit.getPluginManager().callEvent(event);
 				} 
 				catch (InterruptedException e) 
 				{
 					Debug.logException(e);
 				} 
 				catch (ExecutionException e) 
 				{
 					Debug.logException(e);
 				}
 				
 				mCurrentBlockTasks.remove(ent.getKey());
 				it.remove();
 				
 			}
 			
 		}
 	}
 	private class BlockSearchTask implements Task<Cause>
 	{
 		private Location mSearchLocation;
 		
 		public BlockSearchTask(Location searchLocation)
 		{
 			mSearchLocation = searchLocation;
 		}
 		@Override
 		public Cause call()
 		{
 			Pair<Long, Cause> answer = null;
 			
 			CrossReferenceIndex.Results results = CrossReferenceIndex.getSessionsFor(mSearchLocation);
 			
 			for(SessionInFile result : results.foundSessions)
 			{
 				if(answer != null && result.Session.EndTimestamp < answer.getArg1())
 					continue; // Dont bother searching old data
 				
 				RecordList records = result.Log.loadSession(result.Session);
 				String ownerTag = result.Log.getOwnerTag(result.Session);
 				
 				if(records.size() == 0)
 					continue;
 				
 				// Now filter the records to find just what we are looking for
 				// Go backwards to save time
 				ListIterator<Record> it = records.listIterator(records.size()-1);
 				
 				while(it.hasPrevious())
 				{
 					Record record = it.previous();
 					
 					// Dont bother searching before the newest known answer
 					if(answer != null && record.getTimestamp() < answer.getArg1())
 						break;
 					
 					if(record.getType() != RecordType.BlockChange)
 						continue;
 
 					if(!((BlockChangeRecord)record).wasPlaced())
 						continue;
 					
					if(((BlockChangeRecord)record).getLocation().equals(mSearchLocation));
 					{
 						Cause cause = null;
 						if(result.Log.getName().startsWith(LogFileRegistry.cGlobalFilePrefix))
 						{
 							if(ownerTag == null)
 								continue;
 							cause = Cause.globalCause(Bukkit.getWorld(result.Log.getName().substring(LogFileRegistry.cGlobalFilePrefix.length())), ownerTag);
 						}
 						else
 						{
 							cause = (ownerTag == null ? Cause.playerCause(Bukkit.getOfflinePlayer(result.Log.getName())) : Cause.playerCause(Bukkit.getOfflinePlayer(result.Log.getName()), ownerTag));
 						}
 						// Record it
 						if(answer == null)
 							answer = new Pair<Long, Cause>(record.getTimestamp(), cause);
 						else if(record.getTimestamp() > answer.getArg1())
 							answer = new Pair<Long, Cause>(record.getTimestamp(), cause);
 					}
 				}
 			}
 			
 			results.release();
 			
 			if(answer == null)
 			{
 				Debug.fine("BlockSearchTask finished. Result: Unknown");
 				return Cause.unknownCause();
 			}
 			else
 			{
 				Debug.fine("BlockSearchTask finished. Result: %s", answer.getArg2().friendlyName());
 				return answer.getArg2();
 			}
 		}
 		@Override
 		public int getTaskTargetId() 
 		{
 			return 8888;
 		}
 		
 	}
 	
 	public static class CauseFoundEvent extends Event
 	{
 		private static final HandlerList handlers = new HandlerList();
 		private Cause mFoundCause;
 		private Cause mPlaceholder;
 		private Location mLocation;
 		
 		public CauseFoundEvent(Cause placeholder, Cause foundCause, Location forLocation)
 		{
 			mFoundCause = foundCause;
 			mPlaceholder = placeholder;
 			mLocation = forLocation;
 		}
 		
 		public Cause getCause()
 		{
 			return mFoundCause;
 		}
 		public Cause getPlaceholder()
 		{
 			return mPlaceholder;
 		}
 		
 		public Location getLocation()
 		{
 			return mLocation;
 		}
 		
 		@Override
 		public HandlerList getHandlers() 
 		{
 			return handlers;
 		}
 		public static HandlerList getHandlerList()
 		{
 			return handlers;
 		}
 	}
 }
