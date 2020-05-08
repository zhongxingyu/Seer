 package dfs;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 
 import common.Constants;
 import common.DFileID;
 import dblockcache.DBuffer;
 import dblockcache.DBufferCache;
 import dblockcache.DukeDBCache;
 
 public class DukeDFS extends DFS
 {
 	private static DFS dfs; 
 	
 	public DukeDFS(String volName, boolean format) {
 		super(volName, format); 
 		
 	}
 	
 	public DukeDFS(boolean format) {
 		super(Constants.vdiskName,format);
 
 	}
 
 	public DukeDFS() {
 		super(Constants.vdiskName,false);
 		
 	}
 	public static DFS getInstance()
 	{
 		if(dfs == null)
 		{
 			dfs = new DukeDFS(Constants.vdiskName, false);
 		}
 		return dfs; 
 	}
 	
     @Override
     public boolean format() 
     {
         // TODO Auto-generated method stub
     	DBuffer dFileDBuffer = DukeDBCache.getInstance().getBlock(1);
     	byte[] dFileBuffer = new byte[Constants.BLOCK_SIZE];
    	 	dFileDBuffer.read(dFileBuffer, 0, Constants.BLOCK_SIZE);
    	 	for(int k = 0; k < dFileBuffer.length; k++)
    	 	{
 			DFileID iD = new DFileID(k); 
 			byte[] emptyBuffer = new byte[Constants.BLOCK_SIZE]; 
 			Arrays.fill(emptyBuffer, (byte) 0);  
	 		write(iD, emptyBuffer, 0, Constants.BLOCK_SIZE); 
    	 	}
         return false;
     }
 
     @Override
     public synchronized DFileID createDFile() {
         // TODO Auto-generated method stub
     	DFileID retID = null;
    	 	DBuffer iNodeMapDBuffer = DukeDBCache.getInstance().getBlock(1);
    	 	byte[] iNodeBuffer = new byte[Constants.BLOCK_SIZE];
    	 	iNodeMapDBuffer.read(iNodeBuffer, 0, Constants.BLOCK_SIZE);
     	
    	 	for(int i=0;i<iNodeBuffer.length;i++){
    	 		if(iNodeBuffer[i] == 0){
    	 			retID = new DFileID(i);
    	 		}
    	 	}  	 	
    	 	
    	 	if(retID != null){
    	 		iNodeBuffer[retID.getDFileID()] = 1;
    	 		iNodeMapDBuffer.write(iNodeBuffer, 0, Constants.BLOCK_SIZE);
    	 	}
    	 	
    	 	return retID;
     }
 
     @Override
     public void destroyDFile(DFileID dFID) {
         // TODO Auto-generated method stub
         // Scan over iNodes and get the DFileID of each
         // one. When we find a match, kill the associated
         // data blocks and then kill the iNode itself
         
     }
 
 	/*
 	 * reads the file dfile named by DFileID into the buffer starting from the
 	 * buffer offset startOffset; at most count bytes are transferred
 	 */
     @Override
     public int read(DFileID dFID, byte[] buffer, int startOffset, int count) 
     {
     	//Get Inode for the file
     	// Calculate blockID for corresponding inode
     	// nothing in block 0 
     	// 1 = used dFileIds
     	// 2 - 5 = usedblocksMap
     	// 6 - 517 = inodes
     	// 518 - end = data blocks 
     	 int iNodeBlockID = 6 + dFID.getDFileID();
     	 DBuffer iNodeDBuffer = DukeDBCache.getInstance().getBlock(iNodeBlockID);
     	 byte[] iNodeBuffer = new byte[Constants.BLOCK_SIZE];
     	 iNodeDBuffer.read(iNodeBuffer, 0, Constants.BLOCK_SIZE);
     	 
     	 int fileSize = 0;
     	 byte[] fileSizeBytes = new byte[4];
     	 for(int i=0;i<4;i++){
     		 fileSizeBytes[i] = iNodeBuffer[i];
     	 }
     	 
     	 fileSize = java.nio.ByteBuffer.wrap(fileSizeBytes).getInt();
     	
     	//Get corresponding blockIDs for the file from the Inode
     	
     	List<Integer> listOfBlockIDs = new ArrayList<Integer>(); 
     	
     	for(int i=0;i<fileSize-4; i+=4)
     	{
     		byte[] byteArray = new byte[4]; 
     		byteArray[0] = iNodeBuffer[i+4]; 
     		byteArray[1] = iNodeBuffer[i+5]; 
     		byteArray[2] = iNodeBuffer[i+6]; 
     		byteArray[3] = iNodeBuffer[i+7];
     		int blockID = java.nio.ByteBuffer.wrap(byteArray).getInt(); 
     		listOfBlockIDs.add(blockID); 
     	}    	
     
     	int numRead; 
 
     	for(Integer i : listOfBlockIDs)
     	{
     		if(!(count <= 0) && !(startOffset >= buffer.length))
     		{  	
 	    		DBuffer buf = DukeDBCache.getInstance().getBlock(i);     		
 	    		numRead = buf.read(buffer, startOffset, count);     		
 	    		count = count - numRead; 
 	    		startOffset = startOffset + numRead;     		
     		}
     	}
     	    	
     	//Fetch corresponding DBuffer from DBufferCache using block ID
     	//Read data from those blocks into the byte[] up till count bytes
     	
     	return 0;
     }
 
     
     /*
      * writes to the file specified by DFileID from the buffer starting from the
      * buffer offset startOffsetl at most count bytes are transferred
      */
     @Override
     public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
         // TODO Auto-generated method stub
         // Convert the filename/dFID and filesize to byte[]
         // Write that data as the metadata to an iNode in the VDS
         // then store the buffer in blocks and reference
         // those blockIDs, sequentially, in iNode
     	
     	//Get Inode for the file
     	// Calculate blockID for corresponding inode
     	// nothing in block 0 
     	// 1 = dFileIds
     	// 2 - 5 = blockMap
     	// 6 - 517 = inodes
     	// 518 - end = data blocks 
     	
     	 DBuffer fileIDBuffer = DukeDBCache.getInstance().getBlock(1);
     	 byte[] iNodeMapBuffer = new byte[Constants.BLOCK_SIZE];
     	 int bytesRead = fileIDBuffer.read(iNodeMapBuffer, 0, Constants.BLOCK_SIZE);
     	 
     	 if (iNodeMapBuffer[dFID.getDFileID()] != 0)
     		 return -1;
     	 
     	 int iNodeBlockID = 6 + dFID.getDFileID();
     	 DBuffer iNodeDBuffer = DukeDBCache.getInstance().getBlock(iNodeBlockID);
     	 byte[] iNodeData = new byte[Constants.BLOCK_SIZE];
     	 int iNodeBytesRead = iNodeDBuffer.read(iNodeData, 0, Constants.BLOCK_SIZE);
     	 
     	 int fileSize = 0;
     	 byte[] fileSizeBytes = new byte[4];
     	 for(int i=0;i<4;i++){
     		 fileSizeBytes[i] = iNodeData[i];
     	 }
     	 
     	 fileSize = java.nio.ByteBuffer.wrap(fileSizeBytes).getInt();
     	 
     	 // Write size into buffer
     	 iNodeData[0] = (byte) (count >> 24); 
     	 iNodeData[1] = (byte) (count >> 16); 
     	 iNodeData[2] = (byte) (count >> 8); 
     	 iNodeData[3] = (byte) (count);     	 
     	
     	//Get corresponding blockIDs for the file from the Inode     	 
     	List<Integer> listOfBlockIDs = new ArrayList<Integer>();     	
     	for(int i=0;i<fileSize-4; i+=4)
     	{
     		byte[] byteArray = new byte[4]; 
     		byteArray[0] = iNodeData[i+4]; 
     		byteArray[1] = iNodeData[i+5]; 
     		byteArray[2] = iNodeData[i+6]; 
     		byteArray[3] = iNodeData[i+7];
     		int blockID = java.nio.ByteBuffer.wrap(byteArray).getInt(); 
     		listOfBlockIDs.add(blockID); 
     	}
     	
     	// Mark all blocks in list as unused in blockMap
     	
     	// Read all 4 blocks in blockMap into memory
     	byte[][] blockIdMap = new byte[4][Constants.BLOCK_SIZE];
     	DBuffer[] blockMapDBuffers = new DBuffer[4];
     	for(int i=0;i<4;i++){
     		blockMapDBuffers[i] = DukeDBCache.getInstance().getBlock(2+i);
     		int dataRead = blockMapDBuffers[i].read(blockIdMap[i], 0, Constants.BLOCK_SIZE);    		
     	}
     	
     	// Mark corresponding bits in blockMap as unused
     	for(Integer i : listOfBlockIDs){
     		int block = i / Constants.BLOCK_SIZE;
     		byte[] containingBlock = blockIdMap[block];
     		int index = i % Constants.BLOCK_SIZE;
     		int byteArrayIndex = index / 8;
     		int bitIndex = index % 8;
     		byte containingByte = containingBlock[byteArrayIndex];
     		containingByte &= ~( 1 << bitIndex);	
     		blockIdMap[block][byteArrayIndex] = containingByte;    		
     	}
     	
     	List<Integer> newlyPopulatedBlockIDs = new ArrayList<Integer>();
     	
     	// Find empty blocks and write data into them    	
     	for(int i=0;i<4;i++){
     		for(int j=0;j<blockIdMap[i].length;j++){
     			byte b = blockIdMap[i][j];
     			for(int k=0;k<8;k++){
     				// If bit is not set then fill it with data
     				if(((b >> k) & 1) == 0){
     					int index = i * Constants.BLOCK_SIZE * 8 + j * 8 + k;
     					newlyPopulatedBlockIDs.add(index);
     					DBuffer blockDBuffer = DukeDBCache.getInstance().getBlock(517+index);
     					if(count > Constants.BLOCK_SIZE){
     						int dataWritten = blockDBuffer.write(buffer, startOffset, Constants.BLOCK_SIZE);
     						count -= Constants.BLOCK_SIZE;
     					}
     					else{
     						int dataWritten = blockDBuffer.write(buffer, startOffset, count);
     						count = 0;
     					}
     				}
     			}
     		}
     	}
     	
     	// Set newly populated block ID's to be marked as used
     	for(Integer i : listOfBlockIDs){
     		int block = i / Constants.BLOCK_SIZE;
     		byte[] containingBlock = blockIdMap[block];
     		int index = i % Constants.BLOCK_SIZE;
     		int byteArrayIndex = index / 8;
     		int bitIndex = index % 8;
     		byte containingByte = containingBlock[byteArrayIndex];
     		containingByte |= ( 1 << bitIndex);	
     		blockIdMap[block][byteArrayIndex] = containingByte;    		
     	}
     	
     	// Write modified blockMap back to file
     	for(int i=0;i<blockMapDBuffers.length;i++){
     		blockMapDBuffers[i].write(blockIdMap[i], 0, Constants.BLOCK_SIZE);
     	}
     	
     	int iNodeLoc = 4;
     	// Write iNode from index 4 on with blockId numbers
     	for(int i : newlyPopulatedBlockIDs){
     		iNodeData[iNodeLoc + 0] = (byte) (i >> 24); 
     		iNodeData[iNodeLoc + 1] = (byte) (i >> 16); 
     		iNodeData[iNodeLoc + 2] = (byte) (i >> 8); 
     		iNodeData[iNodeLoc + 3] = (byte) i;  
     		iNodeLoc+=4;
     	}
     	
     	// Write iNode back to file
     	iNodeDBuffer.write(iNodeData, 0, Constants.BLOCK_SIZE);   	
 
         return 0;
     }
 
     @Override
     public int sizeDFile(DFileID dFID) {
         // TODO Auto-generated method stub
 	   	 int iNodeBlockID = 6 + dFID.getDFileID();
 		 DBuffer iNodeDBuffer = DukeDBCache.getInstance().getBlock(iNodeBlockID);
 		 byte[] iNodeBuffer = new byte[Constants.BLOCK_SIZE];
 		 iNodeDBuffer.read(iNodeBuffer, 0, Constants.BLOCK_SIZE);
 		 
 		 int fileSize = 0;
 		 byte[] fileSizeBytes = new byte[4];
 		 for(int i=0;i<4;i++){
 			 fileSizeBytes[i] = iNodeBuffer[i];
 		 }
 		 
 		 fileSize = java.nio.ByteBuffer.wrap(fileSizeBytes).getInt();
     	
         return fileSize;
     }
 
     @Override
     public List<DFileID> listAllDFiles() {
         // TODO Auto-generated method stub
    	List<DFileID> dFiles = new ArrayList<DFileID>(); 
    	DBuffer dFileDBuffer = DukeDBCache.getInstance().getBlock(1);
    	byte[] dFileBuffer = new byte[Constants.BLOCK_SIZE];
   	 	dFileDBuffer.read(dFileBuffer, 0, Constants.BLOCK_SIZE);
   	 	for(int k = 0; k < dFileBuffer.length; k++)
   	 	{
 			DFileID iD = new DFileID(k); 
 			if(dFileBuffer[k] == (byte) 1)
 			{
 				dFiles.add(iD); 
 			}
   	 	}
         return dFiles; 
     }
 
 }
