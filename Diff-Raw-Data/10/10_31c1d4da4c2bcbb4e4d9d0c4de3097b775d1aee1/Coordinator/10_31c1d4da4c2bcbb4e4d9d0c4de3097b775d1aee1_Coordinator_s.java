 package com.virtualdisk.coordinator;
 
 
 import com.virtualdisk.network.*;
import com.virtualdisk.util.*;

import java.lang.*;
 import java.util.*;
 
 public class Coordinator
 {
     protected Integer blockSize;
     protected Integer segmentSize;
     protected Integer segmentGroupSize;
     protected Integer quorumSize;
 
     protected List<DataNodeIdentifier> datanodes;
 
     protected PriorityQueue<DataNodeStatusPair> datanodeStatuses;
     protected List<SegmentGroup> segmentGroupList;
     protected Map<Integer,Map<Integer,SegmentGroup>> volumeTable;
     protected Date lastTimestamp;
 
     protected Queue<WriteHandler> writeHandlers;
     protected Queue<ReadHandler> readHandlers;
 
     protected Map<Integer, Boolean> requestCompletionMap;
     protected Map<Integer, Boolean> writeResultMap;
     protected Map<Integer, byte[]> readResultMap;
 
     protected int lastAssignedId = 0;
 
     protected HandlerManager handlerManager;
 
     protected NetworkServer server;
 
     /*
      * This constructor initializes all the necessary information for the coordinator.
      */
     public Coordinator( Integer bs
                       , Integer ss
                       , Integer sgs
                       , Integer qs
                       , List<DataNodeIdentifier> initialNodes
                       , NetworkServer s
                       )
     {
         blockSize = bs;
         segmentSize = ss;
         segmentGroupSize = sgs;
         quorumSize = qs;
 
         server = s;
 
         datanodes = new ArrayList<DataNodeIdentifier>(initialNodes);
         datanodeStatuses = new PriorityQueue<DataNodeStatusPair>();
         for (DataNodeIdentifier each : datanodes)
         {
             DataNodeStatus status = new DataNodeStatus(blockSize, segmentSize);
 
             DataNodeStatusPair pair = new DataNodeStatusPair(each, status);
 
             datanodeStatuses.add(pair);
         }
 
         segmentGroupList = new ArrayList<SegmentGroup>();
 
         volumeTable = new HashMap<Integer,Map<Integer,SegmentGroup>>();
 
         lastTimestamp = new Date(0);
 
         writeHandlers = new LinkedList<WriteHandler>();
         readHandlers = new LinkedList<ReadHandler>();
 
         requestCompletionMap = new HashMap<Integer, Boolean>();
         writeResultMap = new HashMap<Integer, Boolean>();
         readResultMap = new HashMap<Integer, byte[]>();
 
         handlerManager = new HandlerManager(this);
         handlerManager.start();
     }
 
     /*
      * This method creates a new logical volume within the coordinator.
      */
     public Boolean createVolume(Integer volumeId)
     {
         if (volumeTable.get(volumeId) != null)
         {
             return false;
         }
         else
         {
             Map<Integer,SegmentGroup> volumeMap = new HashMap<Integer,SegmentGroup>();
             volumeTable.put(volumeId, volumeMap);
 
             // TODO: put network handling in here, make this create volume create it on all the nodes
 
             return true;
         }
     }
 
     /*
      * This method deletes a logical volume within the coordinator.
      */
     public Boolean deleteVolume(Integer volumeId)
     {
         if (volumeTable.get(volumeId) != null)
         {
             volumeTable.remove(volumeId);
 
             // TODO: put network handling in here, make this deleve volume delete it on all the nodes
 
             return true;
         }
         else
         {
             return false;
         }
     }
 
     /*
      * A synchronized method to generate new unique request IDs.
      */
     protected synchronized int generateNewRequestId()
     {
         ++lastAssignedId;
         return lastAssignedId;
     }
     
     /*
      * Initiates the write request and returns the request's ID.
      */
     public Integer write(Integer volumeId, Integer logicalOffset, byte[] block)
     {
         int id = generateNewRequestId();
 
         WriteHandler handler = new WriteHandler(volumeId, logicalOffset, block, this);
         handler.setRequestId(id);
         handler.start();
 
         writeHandlers.add(handler);
 
         return id;
     }
 
     /*
      * Returns the status of the write request (finished or not-finished).
      */
     public boolean writeCompleted(Integer requestId)
     {
         Boolean finished = requestCompletionMap.get(requestId);
         if (finished != null)
         {
             return finished;
         }
         else
         {
             return false;
         }
     }
 
     /*
      * This method fetches the result of the write request (success or failure).
      * Returns null if it has not finished.
      */
     public Boolean writeResult(Integer requestId)
     {
         return writeResultMap.get(requestId);
     }
 
     /*
      * This method initiates a read request for the given volume ID and logical offset and returns its request ID.
      */
     public Integer read(Integer volumeId, Integer logicalOffset)
     {
         int id = generateNewRequestId();
 
         ReadHandler handler = new ReadHandler(volumeId, logicalOffset, this);
         handler.setRequestId(id);
         handler.start();
 
         readHandlers.add(handler);
 
         return id;
     }
 
     /*
      * This method returns whether or not the specified read request has completed.
      */
     public Boolean readCompleted(Integer requestId)
     {
         Boolean finished = requestCompletionMap.get(requestId);
         if (finished)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     /*
      * This method fetches the results of a read request; If the request is in progress, it returns null.
      */
     public byte[] readResult(Integer requestId)
     {
         return readResultMap.get(requestId);
     }
 
     // TODO: IMPLEMENT LATER
     public Boolean addDataNode(DataNodeIdentifier node)
     {
         return null;
     }
 
     // TODO: IMPLEMENT LATER
     public Boolean removeDataNode(DataNodeIdentifier node)
     {
         return null;
     }
 
     /*
      * This method returns the segment group for a volumeId and logical offset.
      * If the volumeId and logical offset pair do not have a segment group, it will be assigned.
      */
     protected SegmentGroup getSegmentGroup(Integer volumeId, Integer logicalOffset)
     {
         SegmentGroup segmentgroup = volumeTable.get(volumeId).get(logicalOffset);
 
         if (segmentgroup == null)
         {
             segmentgroup = assignSegmentGroup(volumeId, logicalOffset);
         }
 
         return segmentgroup;
     }
 
     /*
      * This method takes a volumeId and logical offset and assigns that pair a segment group.
      * It generates the segment group based off which nodes have the lightest load.
      */
     protected SegmentGroup assignSegmentGroup(Integer volumeId, Integer logicalOffset)
     {
         SegmentGroup segmentgroup = volumeTable.get(volumeId).get(logicalOffset);
 
         if (segmentgroup != null)
         {
             return segmentgroup;
         }
         else
         {
             List<DataNodeStatusPair> segmentGroupMemberPairs = new ArrayList<DataNodeStatusPair>(segmentGroupSize);
             List<DataNodeIdentifier> segmentGroupMembers = new ArrayList<DataNodeIdentifier>(segmentGroupSize);
 
             for (int index = 0; index < segmentGroupSize; ++index)
             {
                 DataNodeStatusPair current = datanodeStatuses.poll();
                 current.getStatus().addStoredSegments(1);
 
                 segmentGroupMemberPairs.add(current);
                 segmentGroupMembers.add(current.getIdentifier());
             }
 
             segmentgroup = new SegmentGroup(segmentGroupMembers);
 
             for (int index = 0; index < segmentGroupSize; ++index)
             {
                 datanodeStatuses.add(segmentGroupMemberPairs.get(index));
             }
 
             volumeTable.get(volumeId).put(logicalOffset, segmentgroup);
 
             return segmentgroup;
         }
     }
 
     /*
      * This method generates a unique timestamp for use in network requests.
      */
     protected synchronized Date getNewTimestamp()
     {
         long current = lastTimestamp.getTime();
         lastTimestamp = new Date(current+1);
         return lastTimestamp;
     }
 
 }
 
