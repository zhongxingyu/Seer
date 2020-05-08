 package memory;
 
 import java.util.LinkedList;
 import java.util.List;
import java.util.Queue;
 
 public class MemoryManager {
     private List<Memory> unallocated;
     private List<Memory> allocated;
    private Queue<MemoryRequest> deferredRequests;
 
     public MemoryManager(int memorySize, int minBlockSize) {
         unallocated = new LinkedList<Memory>();
         allocated = new LinkedList<Memory>();
         deferredRequests = new LinkedList<MemoryRequest>();
 
         /* Create the starting memory with the given */
         /* size and the minimum block size.          */
         unallocated.add(new Memory(memorySize));
         /* Set the minimum size for a memory block. */
         Memory.minBlockSize = minBlockSize;
     }
 
     /* Allocate memory given a request. Returns the memory  */
     /* block allocated if successful, null if unsuccessful. */
     public Memory allocate(MemoryRequest request) {
         Memory allocatedMemory = null;
 
         /* Check all unallocated cells and try to find the best fit. */
         for (Memory memory : unallocated) {
             /* Check if this memory cell could fulfill the request. */
             if (memory.getSize() >= request.getSize()) {
                 /* If this is the first block that can fulfill the request. */
                 if (allocatedMemory == null) {
                     allocatedMemory = memory;
                 } else if (memory.getSize() < allocatedMemory.getSize()) {
                     /* A better fit was found. */
                     allocatedMemory = memory;
                 }
             }
         }
 
         /* Check if a free memory block of enough size was found. */
         if (allocatedMemory != null) {
             unallocated.remove(allocatedMemory);
             /* Split memory in half until the correct size is made.  */
             /* Add the halved memory pieces to the unallocated list. */
             while (allocatedMemory.getSize() > request.getSize()) {
                 unallocated.add(allocatedMemory.split());
             }
             allocatedMemory.allocatedBy = request.getId();
             allocated.add(allocatedMemory);
             return allocatedMemory;
         } else {
             /* Request could not be filled right now. Put it on hold. */
             deferredRequests.add(request);
             return null;
         }
     }
 
     /* Takes in a request number, finds it in the allocated    */
     /* memory list and deallocates that memory block. Returns  */
     /* the resulting memory block after merging if successful. */
     public Memory deallocate(int requestNumber) {
         /* Search through the allocated memory to find */
         /* the memory block to be released.            */
         for (Memory allocatedMemory : allocated) {
             if (allocatedMemory.allocatedBy == requestNumber) {
                 /* Remove memory block from allocated status. */
                 allocated.remove(allocatedMemory);
                 allocatedMemory.allocatedBy = 0;
                 /* Merge it with all of it's buddies. */
                 Memory mergedMemory = merge(allocatedMemory);
                 /* Add the merged block to the unallocated list. */
                 unallocated.add(mergedMemory);
                 /* See if any deferred requests can now be fulfilled. */
                 attemptAllocationOfDeferred();
                 return mergedMemory;
             }
         }
 
         /* Cannot be deallocated because it had not been allocated. */
         return new Memory(0);
     }
 
     /* Takes in a memory block and attempts to merge it with  */
     /* the other memory blocks in the unallocated queue.      */
     /* Returns the original memory if could not be merged, or */
     /* returns a merged memory block.                         */
     private Memory merge(Memory memory) {
         int oldMemorySize = 0;
 
         while (oldMemorySize != memory.getSize()) {
             oldMemorySize = memory.getSize();
 
             /* Search through the unallocated memory for merge candidates. */
             for (Memory unallocatedMemory : unallocated) {
                 /* Attempt to merge them. If null, the merge was not possible. */
                 if (memory.merge(unallocatedMemory) != null) {
                     /* Take the old memory out of the unallocated list. */
                     unallocated.remove(unallocatedMemory);
                     break;
                 }
             }
         }
 
         /* Return the memory block that has been merged if possible. */
         return memory;
     }
 
     /* Go through all deferred requests and attempt to allocate them. */
     private void attemptAllocationOfDeferred() {
        int numRequests = deferredRequests.size();

        for (int i = 0; i < numRequests; ++i) {
            /* Attempt to allocate given the request. */
            /* If not possible, it is added back to   */
            /* the deferred requests list.            */
            Memory allocated = allocate(deferredRequests.poll());
            if (allocated != null) {
                System.out.printf("\tDeferred request %d allocated; addr = 0x%08x.\n",
                        allocated.allocatedBy, allocated.getAddress());
             }
         }
     }
 }
