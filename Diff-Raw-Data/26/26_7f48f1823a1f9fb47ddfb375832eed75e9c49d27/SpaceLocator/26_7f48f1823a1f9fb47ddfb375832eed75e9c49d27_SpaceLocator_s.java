 package au.com.mineauz.PlayerSpy.tracdata;
 
 import java.io.IOException;
 
 import au.com.mineauz.PlayerSpy.debugging.Debug;
 
 public class SpaceLocator
 {
 	private HoleIndex mIndex;
 	private LogFile mLog;
 	public SpaceLocator(LogFile log)
 	{
 		mLog = log;
 	}
 	
 	public void setHoleIndex(HoleIndex index)
 	{
 		mIndex = index;
 	}
 	
 	private int findHoleAt(long location)
 	{
 		int i = 0;
 		for(HoleEntry hole : mIndex)
 		{
			if(hole.Location <= location && hole.Location + hole.Size >= location)
 				return i;
 			else if(hole.Location + hole.Size > location)
 				return -1;
 			
 			i++;
 		}
 		
 		return -1;
 	}
 	
 	public long getFreeSpace(long location)
 	{
 		int hole = findHoleAt(location);
 		
 		if(hole != -1)
 		{
 			HoleEntry entry = mIndex.get(hole);
 			mLog.checkSpaceStatus(entry.Location, entry.Size, true);
 			return entry.Size;
 		}
 		else
 			mLog.checkSpaceStatus(location, 1, false);
 		
 		if(location == mIndex.getEndOfFile())
 			return Long.MAX_VALUE;
 
 		return 0;
 	}
 	public boolean isFreeSpace(long location, long size)
 	{
 		return getFreeSpace(location) >= size;
 	}
 	
 	public long findFreeSpace(long size)
 	{
 		for(HoleEntry hole : mIndex)
 		{
 			if(hole.Size >= size)
 			{
 				mLog.checkSpaceStatus(hole.Location, hole.Size, true);
 				
 				Debug.finest("Found free space at %X->%X", hole.Location, hole.Location + hole.Size - 1);
 				return hole.Location;
 			}
 		}
 		
 		Debug.finest("Found free space at EOF (%X)", mIndex.getEndOfFile());
 		return mIndex.getEndOfFile();
 	}
 	
 	public void consumeSpace(long location, long size) throws IOException
 	{
 		int hole = findHoleAt(location);
 		
 		if(hole != -1)
 		{
 			HoleEntry entry = mIndex.get(hole);
 			
 			if(location == entry.Location)
 			{
				if(size == entry.Size)
 				{
 					// It completely covers the hole
 					mIndex.remove(hole);
 				}
 				else
 				{
 					// It covers the start of the hole
 					entry.Size = (entry.Location + entry.Size) - (location + size);
 					entry.Location += size;
 					
 					if(entry.Size > 0)
 						mIndex.set(hole, entry);
 					else
 						mIndex.remove(hole);
 				}
 			}
 			else
 			{
				if(location + size == entry.Location + entry.Size)
 				{
 					// It covers the end of the hole
 					entry.Size = (location - entry.Location);
					entry.Location = location;
 					
 					mIndex.set(hole, entry);
 				}
 				else
 				{
 					// It covers the middle of the hole
 					HoleEntry newHole = new HoleEntry();
 					newHole.Location = location + size;
 					newHole.Size = (entry.Location + entry.Size) - (location + size);
 					
 					entry.Size = (location - entry.Location);
 					
 					mIndex.set(hole, entry);
 					mIndex.add(newHole);
 				}
 			}
 		}
 	}
 	
 	public void releaseSpace(long location, long size) throws IOException
 	{
 		HoleEntry hole = new HoleEntry();
 		hole.Location = location;
 		hole.Size = size;
 		
 		mIndex.add(hole);
 	}
 }
