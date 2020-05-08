 package com.virtualdisk.datanode;
 
 import com.virtualdisk.datanode.Drive;
 import com.virtualdisk.util.DriveOffsetPair;
 import com.virtualdisk.util.Range;
 
 import java.util.Map;
 import java.util.List;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Collection;
 import java.io.RandomAccessFile;
 import java.io.File;
 import java.util.ArrayList;
 
 public class DataNode
 {
 
     /*
      * The volume table maps integer volume IDs onto diskmaps.
      * Diskmaps are simply maps of integers onto logical offsets (integers) onto physical offsets (integers).
      */
     protected Map<Integer,Map<Integer,DriveOffsetPair>> volumeTable;
 
     /*
      * The free space table contains ranges of free space which are able to be allocated.
      */
     protected List<List<Range>> freeSpaceTable;
 
     /*
      * The timestamp tables map each volume ID to a map of each logical offset to a timestamp.
      * The timestamps are Dates, so they are millisecond precision.
      */
     protected Map<Integer,Map<Integer,Date>> orderTimestampTable;
     protected Map<Integer,Map<Integer,Date>> valueTimestampTable;
 
     /*
      * A list of all the drives the node can use.
      */
     protected List<Drive> drives;
 
     /*
      * Sets up a DataNode with the provided characteristics
      */
     public DataNode(Integer blockSize, String[] driveHandles, Integer[] driveSizes)
     {
         // initialize and add all the drives
         drives = new ArrayList<Drive>(driveHandles.length);
         for (int index = 0; index < driveHandles.length; ++index)
         {
             Drive current = new Drive(blockSize, driveSizes[index], driveHandles[index]);
             drives.add(index, current);
         }
 
         // create an empty volume table
         volumeTable = new HashMap<Integer,Map<Integer,DriveOffsetPair>>();
 
         // initialize and add all the free spaces
         freeSpaceTable = new ArrayList<List<Range>>();
         for (int index = 0; index < driveHandles.length; ++index)
         {
             Range startingFreeSpace = new Range(0, driveSizes[index]-1);
            List current = new ArrayList<Range>();
             current.add(startingFreeSpace);
             freeSpaceTable.add(index, current);
         }
 
         // create empty timestamp tables
         orderTimestampTable = new HashMap<Integer,Map<Integer,Date>>(driveHandles.length);
         valueTimestampTable = new HashMap<Integer,Map<Integer,Date>>(driveHandles.length);
     }
 
     /*
      * Fetches the diskmap for a given volume ID.
      * It will either return a map or, if there is no such diskmap, it will return null.
      */
     private Map<Integer,DriveOffsetPair> getDiskmap(Integer volumeId)
     {
         return volumeTable.get(volumeId);
     }
 
     /*
      * Creates a diskmap for a new volume with the given volume ID.
      * Returns the status of the insertion.
      */
     public Boolean createVolume(Integer volumeId)
     {
         if (volumeTable.get(volumeId) != null)
         {
             return false;
         }
         else
         {
             Map<Integer, DriveOffsetPair> newDiskMap = new HashMap<Integer,DriveOffsetPair>();
             volumeTable.put(volumeId, newDiskMap);
 
             orderTimestampTable.put(volumeId, new HashMap<Integer,Date>());
             valueTimestampTable.put(volumeId, new HashMap<Integer,Date>());
 
             return true;
         }
     }
 
     /*
      * Attempts to delete the volume.
      * Returns the status of the deletion.
      */
     public Boolean deleteVolume(Integer volumeId)
     {
         Collection<DriveOffsetPair> allocatedLocations = volumeTable.get(volumeId).values();
 
         for (DriveOffsetPair location : allocatedLocations)
         {
             Range freedLocation = new Range(location.getOffset(), location.getOffset());
             freeSpaceTable.get(location.getDriveNumber()).add(freedLocation);
         }
 
         volumeTable.remove(volumeId);
 
         orderTimestampTable.remove(volumeId);
         valueTimestampTable.remove(volumeId);
 
         return true;
     }
 
     /*
      * This function performs an order request.
      */
     public Boolean order(Integer volumeId, Integer logicalOffset, Date timestamp)
     {
         Date currentOrderTimestamp = getOrderTimestamp(volumeId, logicalOffset);
         Date currentValueTimestamp = getValueTimestamp(volumeId, logicalOffset);
 
         // TODO: fix this logic so that negative timestamps to not break the system. fine for now...
         if (currentOrderTimestamp == null)
         {
             currentOrderTimestamp = new Date(0);
         }
         if (currentValueTimestamp == null)
         {
             currentValueTimestamp = new Date(0);
         }
  
         if (currentOrderTimestamp.after(timestamp) || currentValueTimestamp.after(timestamp))
         {
             return false;
         }
         else
         {
             setOrderTimestamp(volumeId, logicalOffset, timestamp);
 
             return true;
         }
     }
 
     /*
      * This function writes a single block to the disk.
      * It chooses the first available location on the disk and writes it in that location, along with updating all the necessary timestamps.
      */
     public Boolean write(Integer volumeId, Integer logicalOffset, byte[] block, Date timestamp)
     {
         // for each byte in the data:
         //      update/write timestamp
         //      check if it already has a written value
         //      if yes, then overwrite
         //      if no, then allocate space and write the value in
 
         //TODO: add exception-checking for the correct length of data (block size)
 
         Date orderTimestamp = getOrderTimestamp(volumeId, logicalOffset);
         Date valueTimestamp = getValueTimestamp(volumeId, logicalOffset);
 
         if (orderTimestamp == null)
         {
             return false; // you cannot write if an ordering has not been performed
         }
         if (valueTimestamp == null)
         {
             valueTimestamp = new Date(-1);
         }
 
         Boolean writeable = timestamp.after(valueTimestamp) && ! timestamp.before(orderTimestamp);
 
         if (writeable)
         {
 
             DriveOffsetPair location = getDriveOffsetPair(volumeId, logicalOffset);
 
             if (location == null)
             {
                 location = pickFreeBlock();
                 setDriveOffsetPair(volumeId, logicalOffset, location);
             }
 
             setValueTimestamp(volumeId, logicalOffset, timestamp);
 
             return drives.get(location.getDriveNumber()).write(location.getOffset(), block);
         }
         else
         {
             return false;
         }
     }
 
     /*
      * This method reads and returns the specified block from the disk.
      * If the block is empty, null is returned.
      */
     public byte[] read(Integer volumeId, Integer logicalOffset)
     {
         DriveOffsetPair physicalOffset = getDriveOffsetPair(volumeId, logicalOffset);
 
         if (physicalOffset == null)
         {
             return null;
         }
         else
         {
             return drives.get(physicalOffset.getDriveNumber()).read(physicalOffset.getOffset());
         }
     }
 
     /*
      * Fetches the ordering timestamp for a given volume ID and logical offset.
      */
     public Date getOrderTimestamp(Integer volumeId, Integer logicalOffset)
     {
         if (orderTimestampTable.get(volumeId) == null)
         {
             return null;
         }
 
         return orderTimestampTable.get(volumeId).get(logicalOffset);
     }
 
     /*
      * Fetches the value timestamp for a given volume ID and the logical offset.
      */
     public Date getValueTimestamp(Integer volumeId, Integer logicalOffset)
     {
         if (valueTimestampTable.get(volumeId) == null)
         {
             return null;
         }
 
         return valueTimestampTable.get(volumeId).get(logicalOffset);
     }
 
     /*
      * Sets the ordering timestamp for a given volume ID and logical offset.
      */
     public void setOrderTimestamp(Integer volumeId, Integer logicalOffset, Date timestamp)
     {
         if (orderTimestampTable.get(volumeId) == null)
         {
             createVolume(volumeId);
         }
 
         orderTimestampTable.get(volumeId).put(logicalOffset, timestamp);
     }
 
     /*
      * Sets the value timestamp for a given volume ID and the logical offset.
      */
     public void setValueTimestamp(Integer volumeId, Integer logicalOffset, Date timestamp)
     {
         if (valueTimestampTable.get(volumeId) == null)
         {
             createVolume(volumeId);
         }
 
 
         valueTimestampTable.get(volumeId).put(logicalOffset, timestamp);
     }
 
     /*
      * Fetches the drive-offset-pair for the specified values.
      */
     public DriveOffsetPair getDriveOffsetPair(Integer volumeId, Integer logicalOffset)
     {
         if (volumeTable.get(volumeId) == null)
         {
             return null;
         }
 
         return volumeTable.get(volumeId).get(logicalOffset);
     }
 
     /*
      * Sets the drive-offset-pair for the specified value.
      */
     public Boolean setDriveOffsetPair(Integer volumeId, Integer logicalOffset, DriveOffsetPair pair)
     {
         if (volumeTable.get(volumeId) == null)
         {
             return false;
         }
 
         volumeTable.get(volumeId).put(logicalOffset, pair);
         return true;
     }
 
     /*
      * Returns the free space of the node in number of blocks.
      */
     public Integer totalFreeSpace()
     {
         Integer totalFree = 0;
 
         for (List<Range> drive : freeSpaceTable)
         {
             for (Range each : drive)
             {
                 totalFree += each.getEnd() - each.getBegin() + 1;
             }
         }
 
         return totalFree;
     }
 
     /*
      * Picks the next free block to write to and removes it from the freespace table.
      */
     public DriveOffsetPair pickFreeBlock()
     {
         for (int driveNumber = 0; driveNumber < freeSpaceTable.size(); ++driveNumber)
         {
             if (freeSpaceTable.get(driveNumber).size() > 0)
             {
                 Range first = freeSpaceTable.get(driveNumber).get(0);
                 if (first.getBegin() != first.getEnd())
                 {
                     Range newSpace = new Range(first.getBegin() + 1, first.getEnd());
                     freeSpaceTable.get(driveNumber).set(0, newSpace);
                 }
                 else
                 {
                     freeSpaceTable.get(driveNumber).remove(0);
                 }
                 return new DriveOffsetPair(driveNumber, first.getBegin());
             }
         }
         return null;
     }
 
 }
 
 
