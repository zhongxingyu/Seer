 import java.util.*;
 public class SuperBlock {
   private final int defaultInodeBlocks = 64;
   public int totalBlocks;
   public int totalInodes;
   public int freeList;
  // public short freeList;
 
   public SuperBlock(int diskSize) {
      //read the superblock from disk
 	byte[] superBlock = new byte[Disk.blockSize];
 
 	SysLib.rawread(0, superBlock);
 	totalBlocks = SysLib.bytes2int(superBlock, 0);
   SysLib.cout("totalBlocks in SuperBlock() now " + totalBlocks + "\n");
 	totalInodes = SysLib.bytes2int(superBlock, 4);
   SysLib.cout("totalInodes in SuperBlock() now " + totalInodes + "\n");
 	freeList = SysLib.bytes2int(superBlock, 8);
     
 	if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {
 	  // disk contents are valid
 	    return;
 	} else {
       // need to format disk
       totalBlocks = diskSize;
 	  SysLib.cout("default format ( " + defaultInodeBlocks + " )\n");
       format(defaultInodeBlocks);
     }
   }
 
   //FIXME: should any of the below be synchronized?
 
   //FIXME: I'm *assuming* this method is here from the constructor but it is
   // not mentioned explicitly anywhere else in the project documentation. Is
   // the return type still int?
   public synchronized void format(int files){
     //TODO
     byte[] superBlock = new byte[512];
     totalBlocks = 1000;
     totalInodes = files;
 	  freeList = (files % 16) == 0 ? files / 16 + 1 : files / 16 + 2;
   
     SysLib.int2bytes(totalBlocks, superBlock, 0);
     SysLib.int2bytes(totalInodes, superBlock, 4);
     SysLib.int2bytes(freeList, superBlock, 8);
 
 
     SysLib.cout("totalBlocks in SuperBlock() now " + totalBlocks + "\n");
     SysLib.cout("totalInodes in SuperBlock() now " + totalInodes + "\n");
 
 
 
 	  SysLib.cout("in superblock format");  
     SysLib.rawwrite(0, superBlock);
 // setting -1 to all other disk blocks? 
 	  //short num = -1;
 	  byte[] data = new byte[512];
 	  //SysLib.short2bytes(num, data, 0);
 	  //SysLib.int2bytes(num, data, 0);
 	  for (short i = (short)freeList; i < totalBlocks; i++) {
       //calculate the next freeblock
       //if we are at the end, the next block in the free list is invalid (0)
       short next_block = (short)((i == totalBlocks - 1) ? 0 : i + 1);
       SysLib.cout("next free block is " + next_block + "\n");
       SysLib.short2bytes(next_block, data, 0);
 
      //fill the rest of the block with 0s
      for (int j = 1; j < Disk.blockSize; j++) {
        data[j] = (byte)0;
      }
 
       //Save back to the disk
 		  SysLib.rawwrite(i, data); 
     }
   }
 
   //FIXME: assuming void return value and no arguments
   public void sync()
   {
     // Write back totalBlocks, inodeBlocks, and freeList to disk.
     int offset = 0;
     byte[] superBlock = new byte[Disk.blockSize];
     SysLib.rawread(0, superBlock);
     SysLib.int2bytes(totalBlocks, superBlock, 0);
     SysLib.int2bytes(totalInodes, superBlock, 4);
     SysLib.int2bytes(freeList, superBlock, 8);
     SysLib.rawwrite(0, superBlock);
   }
 
   //FIXME: assuming int return value (of the free block's number) and no arguments
   //FIXME: should this return a block instead?
  public short getFreeBlock() {
     // Dequeue the top block from the free list
     // save a temp copy of the current head of our "linked list"
     short ret = (short)freeList;
     //NOTE: assuming the first 2 (not 4) bytes of a block is the next free number 
     // read the first free block
     byte[] data = new byte[Disk.blockSize];
     SysLib.rawread(freeList, data);
     // get the next free block from the one to be removed
     //NOTE: our notes say the first 2 characters of the block are the next t free block but that cannot be possible if it's an integer, which occupies 4 bytes
     freeList = (int)SysLib.bytes2short(data, 0);
     SysLib.cout("SUPERBLOCK~~~~~~~WAS " + ret + " is now " + freeList + "\n");
 
     return ret;
   }
 
   //FIXME: assuming void return value
   //NOTE: assuming that once we reach the end of the free blocks, the last one's "next block" will be -1
   //FIXME: this algorithm requires us to traverse
   public void returnBlock(short blockNumber) {
     //Enqueue a given block to the end of the free list
     
     // first traverse the free list until we get to the end
     short last_free = (short)freeList;
     short next_free = 0;
     byte[] current_end = null;  
     byte[] new_end = null;  
     
     //Set the new end up so it has a -1 as it's next pointer.
     SysLib.rawread(blockNumber, new_end);
     SysLib.short2bytes((short)0, new_end, 0);
     SysLib.rawwrite(blockNumber, new_end);
     
     while (last_free < totalBlocks) { //(current_end < totalBlocks) {
       // read the next block in the free list
       SysLib.rawread(last_free, current_end);
       //if the first 4 bytes are -1 then we have hit the end
       next_free = SysLib.bytes2short(current_end, 0);
       if (next_free == 0) {
         //set that block's next free block to the given argument blockNumber
         SysLib.short2bytes(blockNumber, current_end, 0);
         SysLib.rawwrite(last_free, current_end);
 
         //set the given block's next pointer to be -1, indicating it is at
         //the end of the list
         return;
       }
       // we haven't found it yet, keep traversing
       last_free = next_free;
     }
   }
 }
