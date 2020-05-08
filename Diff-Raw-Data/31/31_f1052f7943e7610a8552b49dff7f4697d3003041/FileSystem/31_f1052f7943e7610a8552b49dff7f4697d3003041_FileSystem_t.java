 import java.util.*;
 public class FileSystem extends Thread{
   private SuperBlock superblock;
   private Directory directory;
   private FileTable filetable;
 
   public static final int SEEK_SET = 0;
   public static final int SEEK_CUR = 1;
   public static final int SEEK_END = 2;
 
   public FileSystem(int diskBlocks) {
     superblock = new SuperBlock(diskBlocks);
     directory = new Directory(superblock.totalInodes);
     filetable = new FileTable(directory);
 
     //read the "/" file from disk
     FileTableEntry dirEnt = open("/", "r");
     int dirSize = fsize(dirEnt);
     if (dirSize > 0) {
       // the directory has some data.
       byte[] dirData = new byte[dirSize];
       read(dirEnt, dirData);
       directory.bytes2directory(dirData);
     }
     close(dirEnt);
   }
 
   //public FileTableEntry open(String filename, String mode) {
   //reasoning is that we could open a file in write mode which would
   //truncatei to 0, then before the seek could be adjusted, it would get a
   //context switch
   public synchronized FileTableEntry open(String filename, String mode) {
    //Check if this is a new file before falloc because falloc will create a
    //new file if one does not exist
    boolean new_file = directory.namei(filename) == -1;
   //public FileTableEntry open(String filename, String mode) {
     FileTableEntry ftEnt = filetable.falloc(filename, mode);
     short flag;
 
     if (mode.equals("a")) {
       seek(ftEnt, 0, SEEK_END);
       flag = Inode.WRITE;
     } else if (mode.equals("w")) { //truncate the file to 0
       deallocAllBlocks(ftEnt);
       new_file = true;
       //TODO: deallocate all blocks, set file length to 0
       flag = Inode.WRITE;
     } else if (mode.equals("w+")) { //w+ sets the seek to the beginning of the file
       flag = Inode.WRITE;
     } else { //mode is read, sets seek to beginning of the file
       flag = Inode.READ;
     }
 
     //We only want to set the flag if we are the first one in
     if (ftEnt.count == 1) {
       ftEnt.inode.flag = flag;
     }
 
    SysLib.cout("file " + filename + " inumber is " + directory.namei(filename) + " a new file.\n");
     //Allocate hard drive space for the file if its new
     if (new_file) {
       //assign a direct block to it
       short direct_block = superblock.getFreeBlock();
      SysLib.cout("GOT NEW FREE BLOCK FOR NEW FILE " + direct_block + "\n");
       if (direct_block == -1) {
         return null; //Not enough space for a direct block
       }
 
       ftEnt.inode.setNextBlockNumber(direct_block);
       ftEnt.inode.toDisk(ftEnt.iNumber); 
     }
 
     return ftEnt;
   }
 
    //FIXME: I considered making this synchronized but it would cause indefinite starvation as no thread could ever unset the write flag I THINK. doublecheck
   //the position currently pointed to by the seek pointer. If bytes remaining
   //between the current seek pointer and the end of file are less than
   //buffer.length, SysLib.read reads as many bytes as possible, putting them
   //into the beginning of buffer. It increments the seek pointer by the number
   //of bytes to have been read. The return value is the number of bytes that
   //have been read, or a negative value upon an error.
 
   //FIXME: I considered making this synchronized but it would cause indefinite starvation as no thread could ever unset the write flag I THINK. doublecheck
   public synchronized int read(FileTableEntry ftEnt, byte[] buffer) {
   //public int read(FileTableEntry ftEnt, byte[] buffer) {
     //FIXME: check logic: should we be checking
     //Make sure to check inode first	
     //FIXME: forgot what on the inode we are checking? flag?
 
 
     //TODO: check modes, wait/notify, etc
     while (true) {
 
       switch(ftEnt.inode.flag) {
         case Inode.WRITE:
           SysLib.cout("read() with WRITE flag\n");
           //We cannot read if something is writing
           try { wait(); } catch (InterruptedException e) {}
           break;
         case Inode.DELETE:
           SysLib.cout("read() with DELETE flag\n");
           return -1;
         default:
           SysLib.cout("read() with READ OR UNUSED flag\n");
           //First we mark the inode as being in a read state. this SHOULD prevent writer threads from interfering even if this method's execution gets preempted
           ftEnt.inode.flag = Inode.READ;
           //read a block at a time
           byte[] temp_block = new byte[Disk.blockSize];
 
           int bytes_read = 0;
           int read_length = 0;
           while (bytes_read < buffer.length) {
             int block_num = seek2block(ftEnt.seekPtr, ftEnt.inode);
             SysLib.cout("BLOCK NUM FROM read() seek2block is " + block_num + "\n");
             //Something terrible has happened
             if (block_num == -1) {
               return -1;
             }
 
             //Read from disk
             if (SysLib.rawread(block_num, temp_block) == -1) {
               SysLib.cout("RAWREAD in read() came back with error for block number " + block_num + "\n");
               return -1;
             }
 
             //TODO: beginning offset is it possible to have a read start part of the way into the buffer? do we care?
 
             //If we are on the last block and it doesn't use all the space, dont read all of it
             boolean last_block = (ftEnt.inode.length - ftEnt.seekPtr) < Disk.blockSize;
 
             read_length = last_block ? (ftEnt.inode.length - ftEnt.seekPtr) : Disk.blockSize;
 
             System.arraycopy(temp_block, 0, buffer, 0, read_length);
             bytes_read += read_length;
 
             //ftEnt.seekPtr += read_length;
             seek(ftEnt, read_length, SEEK_CUR);
           }
           //end default switch case
 
 
           //FIXME!!!!!!!!!!!!!! is it safe to assume filesystem is supposed to decrease # of threads waiting on this ftEnt?
           //FIXME: also would we decrement this ALWAYS or only when done reading the file?
           ftEnt.count--;
 
           //If there's another thread waiting, wake it up
           //FIXME!!!!!!!!! do we care if the inode flag is READ/WRITE?
           //if (ftEnt.count > 0 && (inode.flag == Inode.READ || inode.flag == Inode.WRITE)) {
           if (ftEnt.count > 0) {
             notify();         
           } else {
             //FIXME: check logic
             //This inode is no longer in a read state.
             ftEnt.inode.flag = Inode.USED;
           }
 
           return bytes_read;
       }
     }
   }
 
     //writes the contents of buffer to the file indicated by fd, starting at the position indicated by the seek pointer. The operation may overwrite existing data in the file and/or append to the end of the file. SysLib.write increments the seek pointer by the number of bytes to have been written. The return value is the number of bytes that have been written, or a negative value upon an error.
   public synchronized int write(FileTableEntry ftEnt, byte[] buffer) {
     if (ftEnt == null) {
       return -1;
     }
   //public int write(FileTableEntry ftEnt, byte[] buffer) {
     //FIXME: check logic: should we be checking
     //Make sure to check inode first	
     //FIXME: forgot what on the inode we are checking? flag?
 
 
     //TODO: check modes, wait/notify, etc
     while (true) {
 
       switch(ftEnt.inode.flag) {
         case Inode.WRITE:
         case Inode.READ:
           //We cannot write if something is writing or reading
           if (ftEnt.count > 1) {//DEBUG
             try { wait(); } catch (InterruptedException e) {}
           } else {
             ftEnt.inode.flag = Inode.USED;//DEBUG
           }
             break;
         case Inode.DELETE:
           return -1;
         default:
           //First we mark the inode as being in a write state. this SHOULD prevent reader/writer threads from interfering even if this method's execution gets preempted
           ftEnt.inode.flag = Inode.WRITE;
           //read a block at a time
           byte[] temp_block = new byte[Disk.blockSize];
 
           short inode_offset = seek2offset(ftEnt.seekPtr);
 
           //check to see if this offset would be in the indirect block AND that
           //indirect block is not set
           if (inode_offset >= Inode.directSize && ftEnt.inode.getIndexBlockNumber() == -1) {
             //we should allocate an index block to it
             short index_block = superblock.getFreeBlock();
             if (index_block == -1) {
               return -1;//no space for an indirect block
             }
             //otherwise set the indirect block and save the inode to disk
             ftEnt.inode.setIndexBlock(index_block);
             
             //save the inode to disk immediately
             ftEnt.inode.toDisk(ftEnt.iNumber);
           }
 
           short block_num = seek2block(ftEnt.seekPtr, ftEnt.inode);
 
           int bytes_written = 0;
           int offset_in_block = ftEnt.seekPtr % Disk.blockSize;
 
           while (bytes_written < buffer.length) {
 
             //The block in question is not available yet, reserve it
             if (block_num == -1) {
               block_num = superblock.getFreeBlock();
               //Out of space, cant do it.
               if (block_num == -1) {
                 return -1;
               }
 
               //Set this new block in the inode so it knows where the rest of the file will go
               if (!(ftEnt.inode.setNextBlockNumber(block_num))) {
                 return -1;
               }
               //write the inode to disk immediately FIXME check logic
               ftEnt.inode.toDisk(ftEnt.iNumber);
             }
             
             //Now we have the block number, read it from the disk
             SysLib.rawread(block_num, temp_block);
 
             //If there is more than a block left in the bluffer, write a block
             //and then reenter the loop, otherwise, write until the buffer is
             //empty
             int bytes_left_in_buffer = buffer.length - bytes_written;
             int bytes_to_write = (bytes_left_in_buffer < Disk.blockSize) ? bytes_left_in_buffer : Disk.blockSize;
 
             System.arraycopy(buffer, bytes_written, temp_block, offset_in_block, bytes_to_write);
 
             bytes_written += bytes_to_write;
           }
           //end default switch case
 
           //FIXME: also would we decrement this ALWAYS or only when done reading the file?
           ftEnt.count--;
 
           //If there's another thread waiting, wake it up
           //FIXME!!!!!!!!! do we care if the inode flag is READ/WRITE?
           //if (ftEnt.count > 0 && (inode.flag == Inode.READ || inode.flag == Inode.WRITE)) {
           if (ftEnt.count > 0) {
             notify();         
           } else {
             //FIXME: check logic
             //This inode is no longer in a read state.
             ftEnt.inode.flag = Inode.USED;
           }
 
           return bytes_written;
       }
     }
   }
 
 
   public int fsize(FileTableEntry ftEnt) {
     return ftEnt.inode.length;
   }
 
   //NOTE: we are a bit iffy on the arguments here but it should return 0 on
   //success, -1 on failure
   //TODO: doublecheck arguments
   //close should wait for all threads to finish with the file before actually closing it
   public int close(FileTableEntry ftEnt) {
     //If returnFd didn't find the file table entry in question, ftEnt would be null
     if (ftEnt == null || ftEnt.count == 0) {
       return -1;
     }
 
     ftEnt.count--;
 
     //FIXME: assuming that "commit transactions" means to write the inode
     //state back to the disk, and we only want to do that if we are the last
     //thread
     if (ftEnt.count == 0) {
       return filetable.ffree(ftEnt) ? 0 : -1;
     }
 
     return 0;
   }
 
   //TODO: doublecheck arguments
   public int seek(FileTableEntry ftEnt, int offset, int whence) {
     //offset can never be greater than the filesize
     int temp_seek = ftEnt.seekPtr;
     
     switch(whence) {
       case SEEK_SET: 
         temp_seek = offset;
         break;
       case SEEK_CUR: 
         temp_seek += offset;
         break;
       case SEEK_END: 
         temp_seek = fsize(ftEnt) + offset;
       default:
         return -1;//FIXME: why does this return int?
     }
 
     if (temp_seek < 0) {
       return -1;
     }
 
     //we're ok, set the seek pointer in the entry
     ftEnt.seekPtr = temp_seek;
 
     return Kernel.OK;
   }
 
   public int format(int files) {
     //TODO
 	//if (!filetable.fempty()){				// test should be the opposite.. dont understand how to check this
     if (files > 0){
       SysLib.cout("in fs format");
       superblock.format(files);
       //directory = new Directory(files);
       filetable = new FileTable(directory);
       return 0;
     }
     SysLib.cout("did not format");
     return -1;
   }
 
   public int delete(String filename) {
     int iNumber = directory.namei(filename);
     if (iNumber == -1) {
       return -1;
     }
 
     FileTableEntry ftEnt = filetable.getEntryAtInumber(iNumber);
 
     if (ftEnt == null) {
       return -1;
     }
 
     //synchronized(ftEnt) {//FIXME: turn this back on
 
       //close if its open, close will not return until the count is 0
       if (ftEnt.inode.flag == Inode.READ || ftEnt.inode.flag == Inode.WRITE) {
         close(ftEnt);
       }
 
       //WE know nothing else is using it so we set the delete block
       ftEnt.inode.flag = Inode.DELETE;
 
       if (!directory.ifree(ftEnt.iNumber)) {
         return -1;
       }
 
       //FIXME
       //NOTE: assuming "commit all file transactions means wait for threads to finish with it?"
       //decrement the count since we are already using it
       //we were the last thread
       //reset the seek
       ftEnt.seekPtr = 0;
 
       //This will set the flag back to USED
       if (!filetable.ffree(ftEnt)) {
         return -1;
       }
     //}//FIXME: turn this back on
 
     return 0;
   }
 
   //TODO more
 
   //FIXME: should this be private?
   private boolean deallocAllBlocks(FileTableEntry ftEnt) {
     //Empty out the inode, delete any blocks it frees in the process
     Vector<Short> blocks_freed = ftEnt.inode.deallocAllBlocks(ftEnt.iNumber);
     for (int i = 0; i < blocks_freed.size(); i++) {
       Short block = (Short)blocks_freed.elementAt(i);
       superblock.returnBlock((short)block);
     }
     return true;
   }
 
  //Given the seek pointer for a file, returns the block number at which it can
   //be found, -1 otherwise
   private short seek2block(int f_pos, Inode inode) {
     SysLib.cout("seek2block() f_pos: " + f_pos + "\n");
     return inode.findTargetBlock(seek2offset(f_pos));
   }
 
   private short seek2offset(int f_pos) {
     return (short)(f_pos / Disk.blockSize);
   }
 }
