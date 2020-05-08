 package hashManagement;
 
 import heapManagement.Utilities;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.Arrays;
 
 import datatype.DataType;
 
 public class HashIndex {
 	private String indexFileName;
 	private String overFlowFileName;
 	private HashHeader hashHeader;
 	private RandomAccessFile indexRandomAccessFile;
 	private RandomAccessFile overFlowRandomAccessFile;
 	private char indexType;
 	private int indexSize;
 	public int overFlowPointerOffset = Utilities.overFlowPointerOffset;
 
 	private int attributeCode;
 	DataType[] datatype;
 
 	/*
 	 * HashIndex constructor will also create the default hashHeader
 	 * Input Datatype will be of form - c100 or i2 and so on...
 	 */
 	public HashIndex(String indexFileName, String overFlowFileName, String datatype, DataType[] datatypes) throws IOException {
 		this.indexFileName = indexFileName;
 		this.overFlowFileName = overFlowFileName;
 		this.datatype = datatypes;
 		this.setIndexType(datatype);
 		this.attributeCode = Utilities.getIntDatatypeCode(this.indexType,this.indexSize);
 
 
 		if(doesHeapFileExist()){
 			//get the header
 			this.openIndexFile();
 			this.openOverFlowFile();
 			getHashHeader();
 		} else {
 			this.openIndexFile();
 			this.openOverFlowFile();
 			hashHeader = new HashHeader(datatype);
 			hashHeader.setHeaderSize(20+datatype.length());
 			byte[] firstBucket = this.initializeBucket();
 			indexRandomAccessFile.seek(0);
 			indexRandomAccessFile.write(firstBucket);	
 		}
 
 	}
 
 	/*
 	 * This function is used to retrieve the header from the overflow file
 	 * 
 	 * Format of the header:
 	 *   1st 4 bytes - Size of the header
 	 *   2nd 4 bytes - Level
 	 *   3rd 4 bytes - Next Split Bucket
 	 *   4th 4 bytes - Free List Bucket Head
 	 *   5th 4 bytes - Next Bucket Id
 	 *   5th rest of the bytes - Index Type
 	 */
 	public HashHeader getHashHeader() {
 
 		try {
 			this.hashHeader = new HashHeader("");
 
 			overFlowRandomAccessFile.seek(0);
 			byte[] headSize = new byte[4];
 			overFlowRandomAccessFile.read(headSize);
 
 			int headIntSize = Utilities.toInt(headSize);
 			byte[] head = new byte[headIntSize];
 			overFlowRandomAccessFile.seek(0);
 			overFlowRandomAccessFile.read(head);
 
 			byte[] temp = Arrays.copyOfRange(head, 0, 4);
 			this.hashHeader.setHeaderSize(Utilities.toInt(temp));
 
 			temp = Arrays.copyOfRange(head, 4, 8);
 			this.hashHeader.setLevel(Utilities.toInt(temp));
 
 			temp = Arrays.copyOfRange(head, 8, 12);
 			this.hashHeader.setNext(Utilities.toInt(temp));
 
 			temp = Arrays.copyOfRange(head, 12, 16);
 			this.hashHeader.setFreeListBucketHead(Utilities.toInt(temp));
 
 			temp = Arrays.copyOfRange(head, 16, 20);
 			this.hashHeader.setNextBucketId(Utilities.toInt(temp));
 
 			temp = Arrays.copyOfRange(head, 20, head.length);
 			this.hashHeader.setIndexType(new String(temp));
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return this.hashHeader;
 
 	}
 
 	private void setIndexType(String datatype){
 		this.indexType = datatype.charAt(0);
 		this.indexSize = Integer.parseInt(datatype.substring(1));
 	}
 
 	public void openOverFlowFile() throws FileNotFoundException {
 		this.overFlowRandomAccessFile = new RandomAccessFile(new File(this.overFlowFileName), "rw");
 	}
 
 	public void openIndexFile() throws FileNotFoundException {
 		this.indexRandomAccessFile = new RandomAccessFile(new File(this.indexFileName), "rw");
 	}
 
 	public int closeHeap(){
 		try {
 			//print the index file and overflow file
 			printIndexFile();
 
 			//before closing the file, write the header to overflow file
 			this.writeIndexHeader(); 
 			indexRandomAccessFile.close();
 			overFlowRandomAccessFile.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return -1;
 		}
 
 		return 0 ;
 	}
 
 	public void printIndexFile() throws IOException{
 		try {
 			int totalBuckets = this.hashHeader.getTotalBuckets();
 			System.out.println("------------------------------------------------------------");
 			for (int i = 0 ; i < totalBuckets; i++){
 				Bucket current  = new Bucket(i, false);
 				current.bucketData = getBucketById(current);
 
 				int currentOffset = 0;
 				int sizeOfRecord = 8 + indexSize;//8 -> RID 
 				
 				StringBuffer bucketOutput = new StringBuffer();
 				
 				while(currentOffset + sizeOfRecord + Utilities.overflowPointerSize < Utilities.bucketSize){
 					//currentOffset < Utilities.bucketSize-Utilities.overflowPointerSize){ //-4 for the OverFlowPointer			
 					byte[] byteRecord = Arrays.copyOfRange(current.bucketData, currentOffset+8,currentOffset+sizeOfRecord);
 
 
 					ByteArrayOutputStream dataByteArray = new ByteArrayOutputStream();
 					datatype[attributeCode].read(dataByteArray, byteRecord);
 					String indexData = new  String(dataByteArray.toByteArray());
 					bucketOutput.append(indexData);
 					bucketOutput.append(",");
 					currentOffset += sizeOfRecord;
 				}
 				//get the overflow pointer from the bucket
 				byte[] overFlowPointer = Arrays.copyOfRange(current.bucketData, overFlowPointerOffset, overFlowPointerOffset+Utilities.overflowPointerSize);
 				int overflowId = Utilities.toInt(overFlowPointer);
 				bucketOutput.append("OF is ");
 				bucketOutput.append(overflowId);
 
 
 				int isOverFlowSet = checkOverflowPointerIsSet(overFlowPointer);
 
 				if(isOverFlowSet == 1){
 
 					bucketOutput.append("\t");
 					int overFlowBucketId = overflowId;
 					Bucket overFlowFileBucket = new Bucket(overFlowBucketId, true);
 					while(isOverFlowSet == 1 ){
 
 						overFlowFileBucket = new Bucket(overFlowBucketId, true);
 						overFlowFileBucket.bucketData = getBucketById(overFlowFileBucket);
 
 						currentOffset = 0;
 						sizeOfRecord = 8 + indexSize;//8 -> RID 
 						
 
 						while(currentOffset + sizeOfRecord + Utilities.overflowPointerSize < Utilities.bucketSize){
 							//currentOffset < Utilities.bucketSize-Utilities.overflowPointerSize){ //-4 for the OverFlowPointer			
 							byte[] byteRecord = Arrays.copyOfRange(overFlowFileBucket.bucketData, currentOffset+8,currentOffset+sizeOfRecord);
 
 
 							ByteArrayOutputStream dataByteArray = new ByteArrayOutputStream();
 							datatype[attributeCode].read(dataByteArray, byteRecord);
 							String indexData = new  String(dataByteArray.toByteArray());
 
 							bucketOutput.append(indexData);
 							bucketOutput.append(",");
 							currentOffset += sizeOfRecord;
 						}
 
 						//get the overflow pointer from the bucket
 						overFlowPointer = Arrays.copyOfRange(overFlowFileBucket.bucketData, overFlowPointerOffset, overFlowPointerOffset+Utilities.overflowPointerSize);
 						overFlowBucketId = Utilities.toInt(overFlowPointer);
 						
 						bucketOutput.append("OF is ");
						bucketOutput.append(overflowId);
 						bucketOutput.append("\t");
 						isOverFlowSet = checkOverflowPointerIsSet(overFlowPointer);
 					}
 				}
 				
 				System.out.println("Bucket id:" + i + " " + bucketOutput.toString());
 				
 			}
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public boolean doesHeapFileExist() {
 		File f = new File(indexFileName);
 		return f.exists();
 	}
 
 	/*
 	 * This function can be used to write the header for the first time 
 	 * and also to update the header for the  Level , Next and FreeList
 	 * 
 	 * Format of the header:
 	 *   1st 4 bytes - Size of the header
 	 *   2nd 4 bytes - Level
 	 *   3rd 4 bytes - Next Split Bucket
 	 *   4th 4 bytes - Free List Bucket Head
 	 *   5th 4 bytes - Next Bucket Id
 	 *   5th rest of the bytes - Index Type
 	 */
 	public void writeIndexHeader() {
 
 		try{
 			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getHeaderSize()));
 			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getLevel()));
 			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getNext()));
 			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getFreeListBucketHead()));
 			byteArrayOutputStream.write(Utilities.toByta(this.hashHeader.getNextBucketId()));
 			byteArrayOutputStream.write(this.hashHeader.getIndexType().getBytes());
 			overFlowRandomAccessFile.seek(0);
 			overFlowRandomAccessFile.write(byteArrayOutputStream.toByteArray());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public byte[] initializeBucket() {
 
 		byte[] bucket = new byte[Utilities.bucketSize]; 
 		int overFlowPointerSize = Utilities.overflowPointerSize;
 		Arrays.fill(bucket, 0, this.overFlowPointerOffset, new Integer(0).byteValue());
 		int overFlowPointer = -1;
 		byte[] overFlowByte = Utilities.toByta(overFlowPointer);
 		for ( int j =  bucket.length-overFlowPointerSize, k = 0  ; j < bucket.length; j++, k++){
 			bucket[j] = overFlowByte[k];
 		}
 		//Arrays.fill(bucket, bucket.length-overFlowPointerSize, bucket.length, new Integer(-1).byteValue());
 		return bucket;		
 	}
 
 	public int insertInhashIndex(HashIndexRecord record) {
 
 		
 		
 		// get the corresponding bucket of the record
 
 		int hashCode = datatype[attributeCode].getHashCode(record.getDataValue());
 		int bucketId = getIndexBucket(hashCode);
 
 		try {
 			
 			
 			//print index and overflow file
 			printIndexFile();
 			ByteArrayOutputStream dataByteArray = new ByteArrayOutputStream();
 			datatype[attributeCode].read(dataByteArray, record.getDataValue());
 			String indexData = new  String(dataByteArray.toByteArray());
 			System.out.println("Record :" + indexData);
 
 			int bucketSize = Utilities.bucketSize; 
 			int overflowPointerSize = Utilities.overflowPointerSize;
 			//get bucket for given bucketId from index file
 			Bucket indexFileBucket = new Bucket(bucketId, false);
 			indexFileBucket.bucketData = getBucketById(indexFileBucket);
 
 			boolean recordInsertedInBucket = insertRecordInBucket(indexFileBucket.bucketData, record,indexFileBucket, false); //last parameter is free bucket insert
 
 			if(!recordInsertedInBucket){
 
 				byte[] overFlowPointer = Arrays.copyOfRange(indexFileBucket.bucketData, overFlowPointerOffset, overFlowPointerOffset+overflowPointerSize);
 				int isOverFlowSet = checkOverflowPointerIsSet(overFlowPointer);
 
 				if(isOverFlowSet == 1){
 
 					int overFlowBucketId = Utilities.toInt(overFlowPointer);
 					Bucket overFlowFileBucket = new Bucket(overFlowBucketId, true);
 					//byte[] currentBucketData = indexFileBucket.bucketData;
 					while(!recordInsertedInBucket && isOverFlowSet == 1 ){
 						overFlowBucketId = Utilities.toInt(overFlowPointer);
 						overFlowFileBucket = new Bucket(overFlowBucketId, true);
 						overFlowFileBucket.bucketData = getBucketById(overFlowFileBucket);
 						recordInsertedInBucket = insertRecordInBucket(overFlowFileBucket.bucketData, record, overFlowFileBucket, false); //last parameter is free bucket insert
 
 						overFlowPointer = Arrays.copyOfRange(overFlowFileBucket.bucketData, overFlowPointerOffset, overFlowPointerOffset+overflowPointerSize);
 						isOverFlowSet = checkOverflowPointerIsSet(overFlowPointer);
 						//currentBucketData = overFlowFileBucket.bucketData;
 					}
 
 					/*there can be 2 case to exit the while loop
 					 *  -either insert was done
 					 *  - or no overflow avialable
 					 */
 					if(recordInsertedInBucket){
 						
 						return 1; //record inserted in overflow bucket
 					}
 
 					//the overflow pointer is not set... so its split
 					insertInFreeBucket(record, overFlowFileBucket);
 					splitBucketInIndex();
 
 				} else { 
 					//if there are no overflow bucket, insert in free bucket or new bucket in overflow file
 					insertInFreeBucket(record, indexFileBucket);
 
 					//split the bucket with id = next
 					//increment the next
 					splitBucketInIndex();
 
 				}
 
 			} 
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	public int getIndexSize() {
 		return indexSize;
 	}
 
 	public void splitBucketInIndex() throws IOException{
 
 		int splitBucketId = this.hashHeader.getNext();
 		this.hashHeader.setNext(splitBucketId + 1);
 		int totalBuckets = this.hashHeader.getTotalBuckets();
 
 		Bucket indexSplitBucket = new Bucket(splitBucketId, false);
 		Bucket newSplitBucket = new Bucket(splitBucketId, false);
 		Bucket newNthBucket = new Bucket(totalBuckets-1, false);
 
 		indexSplitBucket.bucketData = getBucketById(indexSplitBucket);
 		newSplitBucket.bucketData = initializeBucket();
 		newNthBucket.bucketData = initializeBucket();
 
 		int sizeOfRecord = 8 + indexSize;//8 -> RID 
 		int currentOffset = 0; 
 		boolean splitComplete = false;
 
 		while(!splitComplete){
 
 			currentOffset = 0; 
 			while(currentOffset + sizeOfRecord + Utilities.overflowPointerSize < Utilities.bucketSize){ //-8 for the OverFlowPointer		
 
 				byte[] byteRecord = Arrays.copyOfRange(indexSplitBucket.bucketData, currentOffset,currentOffset+sizeOfRecord);
 				int isSpace = checkByteArrayIsAllZero(byteRecord); //drawback - if overflow pointer is 0.
 
 				if(isSpace != 0){ //if record exists, rehash
 
 					//ByteArrayOutputStream dataByteArray = new ByteArrayOutputStream();
 					byte[] temp = Arrays.copyOfRange(byteRecord, 8, 8+this.indexSize);
 					//datatype[attributeCode].read(dataByteArray, temp);			
 					int hashCode = datatype[attributeCode].getHashCode(temp);
 					int bucketId = getIndexBucket(hashCode);
 
 					if ( bucketId == newSplitBucket.getBucketId()) {
 
 						//if the newSplitBucket  is full - insert in disk and get next freeBucket in the overflow file
 						if(newSplitBucket.nextOffset + sizeOfRecord >= overFlowPointerOffset) {
 							
 							int nextBucketId =  this.hashHeader.getFreeListBucketHead();
 							int  freeBucketId = this.hashHeader.getFreeListBucketHead();
 							if(freeBucketId == -1) {
 								
 								nextBucketId = this.hashHeader.getNextBucketId();
 								this.hashHeader.setNextBucketId(nextBucketId + 1);
 								
 							}
 							//if the newSplitBucket is full, overFlowPointer would be next freeBucketId in the overflow file
 							byte[] newSplitOverFlowPointer = Utilities.toByta(nextBucketId);				
 							for(int i = overFlowPointerOffset, j = 0 ; i <  overFlowPointerOffset+Utilities.overflowPointerSize ; i++, j++ ) {
 								newSplitBucket.bucketData[i] = newSplitOverFlowPointer[j];
 							}
 							writeBucketToDisk(newSplitBucket);
 							
 							if(freeBucketId == -1) {
 								newSplitBucket = new Bucket(nextBucketId, true);
 								newSplitBucket.bucketData = initializeBucket();
 							} else {
 								newSplitBucket = new Bucket(nextBucketId, true);
 								newSplitBucket.bucketData = getBucketById(newSplitBucket);
 							}
 							byte[] overFlowPointerindexSplitBucket = Arrays.copyOfRange(newSplitBucket.bucketData, overFlowPointerOffset, overFlowPointerOffset+Utilities.overflowPointerSize);
 							this.hashHeader.setFreeListBucketHead(Utilities.toInt(overFlowPointerindexSplitBucket));
 												
 						}
 
 
 						for(int i = newSplitBucket.nextOffset, j = 0 ; i < newSplitBucket.nextOffset+sizeOfRecord ; i++, j++ ) {
 							newSplitBucket.bucketData[i] = byteRecord[j];
 						}
 
 						newSplitBucket.nextOffset += sizeOfRecord;
 
 					} else if (bucketId == newNthBucket.getBucketId()) {
 
 						//if the newNthBucket  is full - insert in disk and get next freeBucket in the overflow file
 						if(newNthBucket.nextOffset + sizeOfRecord >= overFlowPointerOffset) {
 							
 							int nextBucketId =  this.hashHeader.getFreeListBucketHead();
 							if(nextBucketId == -1) {
 								
 								nextBucketId = this.hashHeader.getNextBucketId();
 								this.hashHeader.setNextBucketId(nextBucketId + 1);
 								
 							}
 							//if the newSplitBucket is full, overFlowPointer would be next freeBucketId in the overflow file
 							byte[] newNthBucketOverFlowPointer = Utilities.toByta(nextBucketId);				
 							for(int i = overFlowPointerOffset, j = 0 ; i <  overFlowPointerOffset+Utilities.overflowPointerSize ; i++, j++ ) {
 								newNthBucket.bucketData[i] = newNthBucketOverFlowPointer[j];
 							}
 							writeBucketToDisk(newNthBucket);
 							
 							if(nextBucketId == -1) {
 								newNthBucket = new Bucket(nextBucketId, true);
 								newNthBucket.bucketData = initializeBucket();
 							} else {
 								newNthBucket = new Bucket(nextBucketId, true);
 								newNthBucket.bucketData = getBucketById(newNthBucket);
 							}
 							byte[] overFlowPointerindexSplitBucket = Arrays.copyOfRange(newNthBucket.bucketData, overFlowPointerOffset, overFlowPointerOffset+Utilities.overflowPointerSize);
 							this.hashHeader.setFreeListBucketHead(Utilities.toInt(overFlowPointerindexSplitBucket));
 							
 						}
 
 						for(int i = newNthBucket.nextOffset, j = 0 ; i < newNthBucket.nextOffset+sizeOfRecord ; i++, j++ ) {
 							newNthBucket.bucketData[i] = byteRecord[j];
 						}
 
 						newNthBucket.nextOffset += sizeOfRecord;
 
 					} else {
 						System.out.println("Split : The hashcode is not correct ");
 					}
 
 				}
 				else{ //split complete, write the buckets to disk
 
 					splitComplete = true;
 					break;
 
 				}
 				currentOffset += sizeOfRecord;
 			}
 
 			//current indexSplitBucket is finished reading. it was the overFlow bucket , add it to the freeSpace list and write  the empty bucket to disk
 			if( indexSplitBucket.isOverFlowBucket ) {
 
 				// overflow pointer for the newly freed bucket will point to the freeHead pointer
 				// and the freeHead pointer will point to the newly freed bucket
 				int freeBucketHead  = this.hashHeader.getFreeListBucketHead();
 				Bucket freeOverFlowBucket = new Bucket(indexSplitBucket.bucketId, true);
 				freeOverFlowBucket.bucketData = initializeBucket();
 
 				byte[] freeBucketOverFlowPointer = Utilities.toByta(freeBucketHead);
 				for(int i = overFlowPointerOffset, j = 0 ; i <  freeOverFlowBucket.getBucketSize() ; i++, j++ ) {
 					freeOverFlowBucket.bucketData[i] = freeBucketOverFlowPointer[j];
 				}
 				writeBucketToDisk(freeOverFlowBucket);
 
 				this.hashHeader.setFreeListBucketHead(indexSplitBucket.bucketId);	
 			}
 
 
 			//get the next overFlow bucket if it exists.
 			//get the  next overflow bucket to be split
 			//int nextIndexSplitBucket = indexSplitBucket.getOverflowPointer();
 			byte[] overFlowIndexSplitPointer = new  byte[Utilities.overflowPointerSize];
 			for(int i  = overFlowPointerOffset, j = 0; i < indexSplitBucket.getBucketSize(); i++, j++){
 				overFlowIndexSplitPointer[j] = indexSplitBucket.bucketData[i];
 			}
 			int nextIndexSplitBucket = Utilities.toInt(overFlowIndexSplitPointer);
 
 			if(nextIndexSplitBucket == -1) { //split complete, write the buckets to disk 
 				splitComplete = true;
 			} else { //get next  overflow bucket
 
 				indexSplitBucket = new Bucket(nextIndexSplitBucket, true);
 				indexSplitBucket.bucketData = getBucketById(indexSplitBucket);
 			}
 
 		}
 		
 		//split complete
 			/*
 			 * first copy the pointers to the new buckets
 			 * For newSPlitBucket : overFlowPointer = currentSplitBucket->overFlowPointer
 			 * For newNthBucket : overFlowPointer = -1
 			 */
 			copyOverFlowPointer(indexSplitBucket, newSplitBucket);
 			//Arrays.fill(newNthBucket.bucketData, overFlowPointerOffset, newNthBucket.bucketData.length, new Integer(-1).byteValue());
 			int overFlowPointer = -1;
 			byte[] overFlowByte = Utilities.toByta(overFlowPointer);
 			for ( int j =  overFlowPointerOffset, k = 0  ; j < newNthBucket.bucketData.length; j++, k++){
 				newNthBucket.bucketData[j] = overFlowByte[k];
 			}
 			//at this point, the bucket are ready to be written to disk
 			writeBucketToDisk(newSplitBucket);
 			writeBucketToDisk(newNthBucket);
 			
 
 	}
 
 	private void copyOverFlowPointer(Bucket indexSplitBucket, Bucket newSplitBucket) {
 
 
 		byte[] newSplitOverFlowPointer = Arrays.copyOfRange(indexSplitBucket.bucketData, overFlowPointerOffset,overFlowPointerOffset+Utilities.overflowPointerSize);
 		indexSplitBucket.setOverflowPointer(Utilities.toInt(newSplitOverFlowPointer));
 
 		for(int i = overFlowPointerOffset, j = 0 ; i <  overFlowPointerOffset+Utilities.overflowPointerSize ; i++, j++ ) {
 			newSplitBucket.bucketData[i] = newSplitOverFlowPointer[j];
 		}
 	}
 
 	private int writeBucketToDisk(Bucket outputBucket){
 
 		try {
 
 			if(outputBucket.isOverFlowBucket) {
 				//System.out.println("During Split : record inserted in the overflow file in bucket id" + outputBucket.bucketId );
 				overFlowRandomAccessFile.seek(Utilities.bucketSize + outputBucket.bucketId*Utilities.bucketSize); //header size  + offset
 				overFlowRandomAccessFile.write(outputBucket.bucketData);
 
 			} else  {
 				//System.out.println("During Split : record inserted in the index file in bucket id" + outputBucket.bucketId );
 				indexRandomAccessFile.seek(outputBucket.bucketId*Utilities.bucketSize);
 				indexRandomAccessFile.write(outputBucket.bucketData);
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 		return 0;
 	}
 
 	public int getOverflowPointer(Bucket bucket){
 		
 		byte[] overFlowPointerFreeBucket = Arrays.copyOfRange(bucket.bucketData, overFlowPointerOffset, overFlowPointerOffset+Utilities.overflowPointerSize);
 //		byte[] newOverFlowPointer = new byte[Utilities.overflowPointerSize];
 //		for(int i = overFlowPointerOffset, j = 0 ; i < overFlowPointerOffset+ Utilities.overflowPointerSize ; i++, j++ ) {
 //			newOverFlowPointer[j] = overFlowPointerFreeBucket[i];
 //		}
 		
 		//int overFlowPointer = Utilities.toInt(newOverFlowPointer);
 		int overFlowPointer = Utilities.toInt(overFlowPointerFreeBucket);
 		return overFlowPointer;
 	}
 	
 	public int insertInFreeBucket(HashIndexRecord record, Bucket parentBucket) throws IOException{
 
 		int freeBucketHead  = this.hashHeader.getFreeListBucketHead();
 
 		if(freeBucketHead != -1) {
 
 			Bucket overFlowFileBucket = new Bucket(freeBucketHead, true);
 			overFlowFileBucket.bucketData = getBucketById(overFlowFileBucket);
 			boolean recordInsertedInBucket = insertRecordInBucket(overFlowFileBucket.bucketData, record, overFlowFileBucket,true);
 			parentBucket.setOverflowPointer(freeBucketHead);
 			//write parent bucket to  file
 
 			byte[] parentBucketOverFlowPointer = Utilities.toByta(freeBucketHead);
 			for(int i = overFlowPointerOffset, j = 0 ; i < parentBucket.getBucketSize(); i++, j++ ) {
 				parentBucket.bucketData[i] = parentBucketOverFlowPointer[j];
 			}
 			writeBucketToDisk(parentBucket);
 			
 			//change the free bucket head
 			int newFreeHead = getOverflowPointer(overFlowFileBucket);
 			this.hashHeader.setFreeListBucketHead(newFreeHead);
 			
 
 			//for debugging
 			if(!recordInsertedInBucket) {
 				System.out.println( "There is some problem in insertion to free bucket in overflow file");
 			}
 
 		} else {
 			//create a new bucket
 			byte[] overFlowBucket = initializeBucket();
 			int nextBucketInOverFLowFile = this.hashHeader.getNextBucketId();
 			Bucket overFlowFileBucket = new Bucket(nextBucketInOverFLowFile, true);
 			boolean recordInsertedInBucket = insertRecordInBucket(overFlowBucket, record, overFlowFileBucket,false);
 			parentBucket.setOverflowPointer(nextBucketInOverFLowFile);
 			this.hashHeader.setNextBucketId(nextBucketInOverFLowFile + 1);
 
 			//write parent bucket to  file
 			byte[] parentBucketOverFlowPointer = Utilities.toByta(nextBucketInOverFLowFile);
 			for(int i = overFlowPointerOffset, j = 0 ; i <  parentBucket.getBucketSize() ; i++, j++ ) {
 				parentBucket.bucketData[i] = parentBucketOverFlowPointer[j];
 			}
 			writeBucketToDisk(parentBucket);
 
 			//for debugging
 			if(!recordInsertedInBucket) {
 				System.out.println( "There is some problem in insertion to new bucket in overflow file");
 			}
 		}
 
 		return 0;
 	}
 
 
 	public boolean insertRecordInBucket(byte[]bucket, HashIndexRecord record, Bucket inputBucket, boolean freeBucket) throws IOException {
 
 		//check for space in the bucket for the given record
 		boolean recordInsertedInBucket = false;
 		int currentOffset = 0;
 		int sizeOfRecord = 8 + indexSize;//8 -> RID 
 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 
 		while(currentOffset + sizeOfRecord + Utilities.overflowPointerSize < Utilities.bucketSize){
 			//currentOffset < Utilities.bucketSize-Utilities.overflowPointerSize){ //-4 for the OverFlowPointer			
 			byte[] byteRecord = Arrays.copyOfRange(bucket, currentOffset,currentOffset+sizeOfRecord);
 			int isSpace = checkByteArrayIsAllZero(byteRecord); //drawback - if overflow pointer is 0.
 			currentOffset += sizeOfRecord;
 			if(!recordInsertedInBucket && isSpace == 0){ //if RID == 0, this is the vacant space
 
 				byte[] longRid = Utilities.toByta(record.getRecordId());
 				byteArrayOutputStream.write(longRid);
 				byteArrayOutputStream.write(record.getDataValue());
 				//datatype[attributeCode].write(byteArrayOutputStream, record.getDataValue() , this.indexSize );
 				recordInsertedInBucket = true;
 				break;
 			}
 			else{
 				byteArrayOutputStream.write(byteRecord);
 			}
 			
 		}
 		
 		//copy the rest of the record
 		byte[] remainingBucket = Arrays.copyOfRange(bucket, currentOffset,overFlowPointerOffset);
 		byteArrayOutputStream.write(remainingBucket);
 		
 		//get the overflow pointer from the bucket
 		byte[] overFlowPointer = Arrays.copyOfRange(bucket, overFlowPointerOffset, overFlowPointerOffset+Utilities.overflowPointerSize);
 
 		if(freeBucket) {
 
 			// if its a free bucket make the free list head to next  overflow pointer
 			// then make the  next overflow pointer to -1 
 			this.hashHeader.setFreeListBucketHead(Utilities.toInt(overFlowPointer));
 			
 			//byte[] overFlowTemp = new byte[Utilities.overflowPointerSize];
 			
 			int tempOverFlowPointer = -1;
 			byte[] overFlowTemp = Utilities.toByta(tempOverFlowPointer);
 			
 //			for ( int j =  overFlowPointerOffset, k = 0  ; j < bucket.length; j++, k++){
 //				overFlowTemp[j] = overFlowByte[k];
 //			}
 			//Arrays.fill(overFlowTemp, new Integer(-1).byteValue());
 			
 			byteArrayOutputStream.write(overFlowTemp);
 		} else {
 			byteArrayOutputStream.write(overFlowPointer);//write overflow pointer to the bufferstream
 		}
 
 
 		if(recordInsertedInBucket) {
 
 			if(inputBucket.isOverFlowBucket) {
 				
 				overFlowRandomAccessFile.seek(Utilities.bucketSize + inputBucket.bucketId*Utilities.bucketSize); //header size  + offset
 				overFlowRandomAccessFile.write(byteArrayOutputStream.toByteArray());
 
 			} else  {
 				
 				indexRandomAccessFile.seek(inputBucket.bucketId*Utilities.bucketSize);
 				indexRandomAccessFile.write(byteArrayOutputStream.toByteArray());
 			}
 
 		}
 
 		return recordInsertedInBucket;
 	}
 
 
 	public int checkOverflowPointerIsSet(byte[] overFlowPointer){
 
 		int overFlow = Utilities.toInt(overFlowPointer);
 		if ( overFlow != -1) {
 			return 1;
 		}
 
 		return 0;
 		//		for(int z = 0 ; z < overFlowPointer.length; z++){
 		//			
 		//			if(overFlowPointer[z] != -1){
 		//				return 1;
 		//			}
 		//		}	
 	}
 
 	public int checkByteArrayIsAllZero(byte[] bucket){
 
 		for(int z = 0 ; z < bucket.length; z++){
 			if(bucket[z] != 0){
 				return -1;
 			}
 		}
 		return 0;
 	}
 
 	public byte[] getBucketById(Bucket readBucket) throws IOException {
 
 		int bucketSize = Utilities.bucketSize;
 		byte[] bucket = new byte[bucketSize];
 		if(readBucket.isOverFlowBucket){
 			int bufferOffset = bucketSize + readBucket.getBucketId()*bucketSize;
 			overFlowRandomAccessFile.seek(bufferOffset);
 			overFlowRandomAccessFile.read(bucket);
 		}else{
 			int bufferOffset = readBucket.getBucketId()*bucketSize;
 			indexRandomAccessFile.seek(bufferOffset);
 			indexRandomAccessFile.read(bucket);	
 		}
 		return bucket;
 	}
 
 	public int getIndexBucket(int hashCode){
 
 		int bucketId  = 0;
 		int s = (int)Math.pow(2, hashHeader.getLevel());//2^hashHeader.getLevel();
 		int totalbuckets = hashHeader.getNext() + s;
 		bucketId = hashCode % (2*s);
 		if (bucketId > totalbuckets - 1) {
 			bucketId = bucketId - s;
 		}
 		return bucketId;
 	}
 
 
 
 
 }
