 package au.com.mineauz.PlayerSpy.tracdata;
 
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 import au.com.mineauz.PlayerSpy.debugging.Debug;
 
 public class HoleIndex extends DataIndex<HoleEntry, HoleData>
 {
 
 	public HoleIndex( LogFile log, FileHeader header, RandomAccessFile file, SpaceLocator locator )
 	{
 		super(log, header, file, locator);
 	}
 
 	@Override
 	public String getIndexName()
 	{
 		return "Hole Index";
 	}
 
 	@Override
 	public long getLocation()
 	{
 		return mHeader.HolesIndexLocation;
 	}
 
 	@Override
 	public long getSize()
 	{
 		return mHeader.HolesIndexSize;
 	}
 
 	@Override
 	protected int getEntrySize()
 	{
 		return HoleEntry.cSize;
 	}
 
 	@Override
 	protected HoleEntry createNewEntry()
 	{
 		return new HoleEntry();
 	}
 
 	@Override
 	protected int getElementCount()
 	{
 		return mHeader.HolesIndexCount;
 	}
 
 	@Override
 	protected void updateElementCount( int newCount )
 	{
 		mHeader.HolesIndexCount = newCount;
 	}
 
 	@Override
 	protected void updateSize( long newSize )
 	{
 		mHeader.HolesIndexSize = newSize + mHeader.HolesIndexPadding;
 	}
 
 	@Override
 	protected void updateLocation( long newLocation )
 	{
 		mHeader.HolesIndexLocation = newLocation;
 	}
 	
 	long getEndOfFile()
 	{
 		try
 		{
 			return mFile.length();
 		}
 		catch(IOException e)
 		{
 			return 0;
 		}
 	}
 
 	private boolean canMergeHoles(HoleEntry a, HoleEntry b)
 	{
 		if((a.Location >= b.Location && a.Location <= b.Location + b.Size) ||
 		   (a.Location + a.Size >= b.Location && a.Location + a.Size <= b.Location + b.Size))
 			return true;
 		
 		return false;
 	}
 	private HoleEntry mergeHoles(HoleEntry a, HoleEntry b)
 	{
 		if(!canMergeHoles(a,b))
 			return null;
 		
 		HoleEntry merged = new HoleEntry();
 		if(a.Location < b.Location)
 		{
 			merged.Location = a.Location;
 			merged.Size = Math.max((b.Location - a.Location) + b.Size, a.Size);
 		}
 		else
 		{
 			merged.Location = b.Location;
 			merged.Size = Math.max(b.Size, (a.Location - b.Location) + a.Size);
 		}
 		
 		return merged;
 	}
 	
 	@Override
 	protected int getInsertIndex( HoleEntry entry )
 	{
 		int index = 0;
 		for(index = 0; index < mElements.size(); index++)
 		{
 			if(mElements.get(index).Location > entry.Location)
 				break;
 		}
 		
 		return index;
 	}
 	
 	// The holes index uses padding so it needs to do these methods custom
 	
 	@Override
 	public int add( HoleEntry entry ) throws IOException
 	{
 		if(entry.Size == 0)
 			return -1;
 		
 		Debug.finer("Adding hole from %X->%X", entry.Location, entry.Location + entry.Size - 1);
 		
		// Prevent holes from being added at eof
		if(entry.Location + entry.Size == getEndOfFile())
		{
			mFile.setLength(entry.Location);
			Debug.finer("Hole was at EOF, shrinking file to %X", getEndOfFile());
			return mElements.size();
		}
		
 		// Check if we need to merge it
 		for(int i = 0; i < mElements.size(); i++)
 		{
 			HoleEntry existing = mElements.get(i);
 			if(canMergeHoles(entry,existing))
 			{
 				HoleEntry newHole = mergeHoles(entry,existing);
 				mElements.set(i, newHole);
 				
 				// Write the changes
 				mFile.seek(getLocation() + i * getEntrySize());
 				newHole.write(mFile);
 				
 				Debug.finest("Merging new hole into @%d changing range from (%X->%X) into (%X->%X)", i, existing.Location, existing.Location + existing.Size-1,newHole.Location, newHole.Location + newHole.Size-1);
 				
				return i;
 			}
 		}
 		
 		int insertIndex = getInsertIndex(entry);
 		mElements.add(insertIndex, entry);
 		
 		// Use padding first
 		if(mHeader.HolesIndexPadding >= getEntrySize())
 		{
 			// Shift the entries
 			write(insertIndex);
 			
 			updateElementCount(mElements.size());
 			mHeader.HolesIndexPadding -= getEntrySize();
 			updateSize(mElements.size() * getEntrySize());
 			
 			
 			Debug.finest("Wrote %d/%d hole entries to %X -> %X Using padding. Remaining: %d bytes", mElements.size() - insertIndex, mElements.size(), getLocation() + insertIndex * getEntrySize(), getLocation() + getSize()-1, mHeader.HolesIndexPadding);
 		}
 		// Use a hole
 		else if(mLocator.isFreeSpace(getLocation() + getSize(), getEntrySize()))
 		{
 			write(insertIndex);
 			
 			updateElementCount(mElements.size());
 			updateSize(mElements.size() * getEntrySize());
 			
 			Debug.finest("Wrote %d/%d hole entries to %X -> %X", mElements.size() - insertIndex, mElements.size(), getLocation() + insertIndex * getEntrySize(), getLocation() + getSize()-1);
 
 			mLocator.consumeSpace(getLocation() + getSize() - getEntrySize(), getEntrySize());
 		}
 		// Relocate it
 		else
 		{
 			long oldLocation = getLocation();
 			long oldSize = getSize();
 			
 			HoleEntry oldIndexHole = new HoleEntry();
 			oldIndexHole.Location = oldLocation;
 			oldIndexHole.Size = oldSize;
 			
 			// Try to merge the old hole
 			boolean merged = false;
 			for(int i = 0; i < mElements.size(); i++)
 			{
 				if(canMergeHoles(oldIndexHole,mElements.get(i)))
 				{
 					mElements.set(i, mergeHoles(oldIndexHole,mElements.get(i)));
 					
 					if(merged)
 						mElements.remove(oldIndexHole);
 					
 					oldIndexHole = mElements.get(i);
 					merged = true;
 				}
 			}
 			
 			// Add it now if it wasnt merged
 			if(!merged && oldIndexHole.Size != 0)
 			{
 				mElements.add(getInsertIndex(oldIndexHole), oldIndexHole);
 			}
 			
 			mHeader.HolesIndexPadding = HoleEntry.cSize;
 			// The total size of the hole index now including 1 extra entry as padding
 			long newSize = mElements.size() * getEntrySize();
 			
 			// Find a new location for the index
 			long newLocation = mLocator.findFreeSpace(newSize + mHeader.HolesIndexPadding);
 			if(newLocation == 0)
 				newLocation = mFile.length();
 			
 			// Prepare the header info
 			updateElementCount(mElements.size());
 			updateSize(newSize);
 			updateLocation(newLocation);
 			
 			// Write the items
 			write(0);
 			
 			// Padding
 			mFile.write(new byte[HoleEntry.cSize]);
 
 			Debug.finest("Moving hole index from %X to (%X -> %X) setting %d bytes padding", oldLocation, getLocation(), getLocation() + getSize() - 1, mHeader.HolesIndexPadding);
 			
 			mLocator.consumeSpace(newLocation, newSize + mHeader.HolesIndexPadding);
 		}
 		
 		mFile.seek(0);
 		mHeader.write(mFile);
 		
 		return insertIndex;
 	}
 	
 	@Override
 	public void remove( int index ) throws IOException
 	{
 		mElements.remove(index);
 		
 		write(index);
 		
 		mHeader.HolesIndexPadding += getEntrySize();
 		updateSize(mElements.size() * getEntrySize() + mHeader.HolesIndexPadding);
 		updateElementCount(mElements.size());
 		
 		Debug.finer("Entry %d removed from %s", index, getIndexName());
 		
 		mFile.seek(0);
 		mHeader.write(mFile);
 	}
 	
 	public HoleEntry getHoleAfter(long location)
 	{
 		for(HoleEntry entry : mElements)
 		{
 			if(entry.Location >= location)
 				return entry;
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public void set( int index, HoleEntry entry ) throws IOException
 	{
 		super.set(index, entry);
 		Debug.fine("Set hole @%d range to %X->%X", index, entry.Location, entry.Location + entry.Size - 1);
 	}
 
 	@Override
 	protected HoleData createNewDataElement( HoleEntry entry )
 	{
 		return new HoleData(entry);
 	}
 }
