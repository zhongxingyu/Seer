 package org.concord.LabBook;
 
 import waba.io.*;
 import waba.util.*;
 import waba.sys.*;
 import org.concord.waba.extra.io.*;
 
 class FileObject 
 {
     int devId;
     int objId;
 	int filePos;
 	int indexPos;
 }
 
 public class LabBookFile extends LabBookDB
 {
 	String fileName = null;
 
     int objIndexStart = 16;
 
     Vector objects = new Vector();
 	Vector objIndexVec = null;
 
     int curDevId;
     int nextObjId;
     int rootDevId;
     int rootObjId;
 	
     static public void writeString(DataStream ds, String s)
     {
 		ds.writeFixedString(s, s.length());
     }
 
     public LabBookFile(String name)
     {
 		boolean newDB = false;
 
 		fileName = name;
 		File file = new File(name, File.DONT_OPEN);
 		if(file.exists()){
 			file.close();
 			file = new File(name, File.READ_WRITE);
 		} else {
 			file.close();
 			file = new File(name, File.CREATE);
 			file.close();
 			file = new File(name, File.READ_WRITE);
 			newDB = true;
 		}
 
 		DataStream ds = new DataStream(file);
 
 		if(newDB){
 			curDevId = 0;
 			nextObjId = 0;
 			rootDevId = 0;
 			rootObjId = 0;
 			ds.writeInt(0);
 			ds.writeInt(0);
 			ds.writeInt(0);
 			ds.writeInt(0);
 
 			// length
 			ds.writeInt(0);
 			return;
 		} else {
 			curDevId = ds.readInt();
 			nextObjId = ds.readInt();
 			rootDevId = ds.readInt();
 			rootObjId = ds.readInt();
 		}
 
 		if(!readIndex(file, ds)){
 			// Failed
 			Debug.println("Error Reading index");
 		}
 
 		file.close();
     }
 
     public int getDevId()
     {
 		return curDevId;
     }
 
     public int getNewObjId()
     {
 		return nextObjId++;
     }
 
 	public LabObjectPtr getRootPtr()
 	{
 		return new LabObjectPtr(rootDevId, rootObjId, null, this);
 	}
 
 	public void setRootPtr(LabObjectPtr ptr)
 	{
 		rootDevId = ptr.devId;
 		rootObjId = ptr.objId;
 	}
 
 
 	
     public boolean save()
     {
 		/*
 		int curObjFilePos = 0;
 		int curIndexPos = 0;
 		int numObj = objects.getCount();
 		FileObject fObj;
 	
 		Debug.println("About to write " + numObj);
 
 		File file = new File(fileName, File.READ_WRITE);
 		DataStream ds = new DataStream(file);
 
 		file.seek(0);
 		ds.writeInt(curDevId);
 		ds.writeInt(nextObjId);
 		ds.writeInt(rootDevId);
 		ds.writeInt(rootObjId);
 	
 		file.seek(objIndexStart);
 		ds.writeInt(numObj);
 		ds.writeInt(numObj);
 		curIndexPos = objIndexStart + 8;
 		curObjFilePos = curIndexPos + numObj*3*4 + 4;
 		for(int i=0; i<numObj; i++){
 			fObj = (FileObject)objects.get(i);
 			file.seek(curIndexPos);
 			ds.writeInt(fObj.devId);
 			ds.writeInt(fObj.objId);
 			ds.writeInt(curObjFilePos);
 			curIndexPos += 3*4;
 	    
 			file.seek(curObjFilePos);
 			ds.writeInt(fObj.buffer.length);
 			file.writeBytes(fObj.buffer, 0, fObj.buffer.length);
 			curObjFilePos += 4 + fObj.buffer.length;
 		}
 
 		file.seek(curIndexPos);
 		ds.writeInt(-1);
 
 		file.close();
 		*/
 
 		return true;
     }
 
 
     public void close()
     {
 
     }
 
     /*
      * The ObjectIndex format is:
      * [ length ]
      * [ subLength ]
      * [ file pos ] [ device id ] [ object id ]
      * ...
      * [ next subIndex file pos ]
      * --------------------
      * [ subLength ]
      * [ file pos ] [ device id ] [ object id ]
      * ...
      * [ next subIndex file pos ]
      * --------------------
      * .............
      * --------------------
      * [ subLength ]
      * [ file pos ] [ device id ] [ object id ]
      * ...
      * [ <-1> ]
      */
     public boolean readIndex(File file, DataStream ds)
     {
 		int curFilePos = objIndexStart;
 		file.seek(objIndexStart);
 		int length = ds.readInt();		
 		curFilePos += 4;
 
 		int subLen, subSize;
 		int pos = 0;
 		int i;
 		int nextPos = 0;
 		FileObject fObj = null;
 
 		if(objIndexVec == null) objIndexVec = new Vector();
 
 		while(true){
 			int [] oIndex = {curFilePos, 0, START_INDEX_SIZE};
 			nextPos = ds.readInt();			
 			curFilePos += 4;
 
 			subLen = ds.readInt();
 			curFilePos += 4;
 			oIndex[1] = subLen;
 
 			subSize = ds.readInt();
 			curFilePos += 4;
 			oIndex[2] = subSize;
 
 			objIndexVec.add(oIndex);
 
 			for(i=0; i<subLen; i++){
 				if(pos  >= length){
 					// file format error
 					return false;
 				}
 				fObj = new FileObject();
 				fObj.indexPos = curFilePos;
 				fObj.devId = ds.readInt();
 				curFilePos += 4;
 				fObj.objId = ds.readInt();
 				curFilePos += 4;
 				fObj.filePos = ds.readInt();
 				curFilePos += 4;
 				objects.add(fObj);
 				pos++;
 			}
 			if(nextPos == 0 || pos >= length*3){
 				break;
 			} else {
 				file.seek(nextPos);
 				curFilePos = nextPos;
 			}
 		}
 
 		return true;
     }
 
 	private static int START_INDEX_COUNT = 30;
 	private static int START_INDEX_SIZE = 12+START_INDEX_COUNT*12;
 
 	private static int getIndexOffset(int pos)
 	{
 		return 12+pos*12;
 	}
 
 	private void addObjectIndex(File file)
 	{
 		DataStream ds = new DataStream(file);
 
 		int [] oIndex = {file.getLength(), 0, START_INDEX_SIZE};
 		objIndexVec.add(oIndex);
 		int newFileIndex = file.getLength();
 		file.seek(newFileIndex);
 		// next index
 		ds.writeInt(0);
 		// num objects in index
 		ds.writeInt(0);
 		// available size in bytes
 		ds.writeInt(START_INDEX_SIZE);
 		for(int i=0; i<START_INDEX_COUNT; i++){
 			ds.writeInt(0);
 			ds.writeInt(0);
 			ds.writeInt(0);
 		}
 		if(objIndexVec.getCount() > 1){
 			int [] prevIndex = (int [])objIndexVec.get(objIndexVec.getCount() - 2);
 			file.seek(prevIndex[0]);
 			ds.writeInt(newFileIndex);
 		}
 	}
 
 	private void growObjectIndex(File file)
 	{
 		DataStream ds = new DataStream(file);
 
 		if(objIndexVec == null){
 			// this is brand new file
 			objIndexVec = new Vector();
 			addObjectIndex(file);
 		} else {
 			int [] lastIndex = (int [])objIndexVec.get(objIndexVec.getCount() - 1);
 			if(getIndexOffset(lastIndex[1]) >= lastIndex[2]){
 				// we've hit the end of the current index
 				addObjectIndex(file);
 			}
 		}
 	}
 
 	private void setObjectIndex(FileObject fObj, File file)
 	{
 		DataStream ds = new DataStream(file);
 		if(fObj == null) return;
 		if(fObj.indexPos == -1){
 			// this is a new object
 			int [] lastIndex = (int [])objIndexVec.get(objIndexVec.getCount() - 1);
 			file.seek(lastIndex[0] + 4);
 			ds.writeInt(lastIndex[1] + 1);
 
 			fObj.indexPos = lastIndex[0]+getIndexOffset(lastIndex[1]);
 			lastIndex[1]++;
 			file.seek(fObj.indexPos);
 			ds.writeInt(fObj.devId);
 			ds.writeInt(fObj.objId);
 			objects.add(fObj);
 
 			file.seek(16);
 			ds.writeInt(objects.getCount());
 		}
 		file.seek(fObj.indexPos+8);
 		ds.writeInt(fObj.filePos);
 	}
 
 	private FileObject findObj(LabObjectPtr ptr)
 	{
 		int numObj = objects.getCount();
 		int i;
 		FileObject fObj = null;
 		
 		for(i=0; i<numObj; i++){
 			fObj = (FileObject)objects.get(i);
 			if(ptr.devId == fObj.devId &&
 			   ptr.objId == fObj.objId){
 				break;
 			}
 			fObj = null;
 		}
 
 		if(fObj == null) return null;
 
 		return fObj;
 	}
 
     public boolean getError(){return false;};
 
     // search through object Index       
     // and find object bytes
     public byte [] readObjectBytes(LabObjectPtr ptr, int numBytes)
     {
 		int curPos = 0;
 		FileObject fObj = null;
 		int objSize = 0;
 		int filePos = -1;
 
 		fObj = findObj(ptr);
 		if(fObj == null) return null;
 
 		File file = new File(fileName, File.READ_WRITE);
 		DataStream ds = new DataStream(file);
 
 		file.seek(fObj.filePos);
 		objSize = ds.readInt();
 		if(numBytes > 0 && numBytes < objSize){
 			objSize = numBytes;
 		}
 
 		byte [] buffer = new byte [objSize];
 		file.readBytes(buffer, 0, objSize);
 
 		file.close();
 		return buffer;
     }
 
     // check if this object already exists
     // if not add it to the file and add it
     // to the objIndex ??? (objIndex should be vector)
     // if it does exist, then replace it's entry in the the
     // objIndex with the new value after adding it to the file
 
     // In the longer term we will have to write the objects in 
     // peices and keep track of free space in the file.
     public boolean writeObjectBytes(LabObjectPtr ptr, byte [] buffer, int start,
 									int count)
     {
 		int numObj = objects.getCount();
 		int i;
 		FileObject fObj = null;
 		File file;
 		DataStream ds;
 
 		Debug.println(" Saving " + count + " bytes to fObj");
 
 		if(nextObjId <= ptr.objId) nextObjId = ptr.objId + 1;
 		


 		fObj = findObj(ptr);
 		int objectFilePos = -1;
 		file = new File(fileName, File.READ_WRITE);
 		ds = new DataStream(file);
 
 		if(fObj == null){
 			// we need to add an object
 			growObjectIndex(file);
 
 			fObj = new FileObject();
 			fObj.devId = ptr.devId;
 			fObj.objId = ptr.objId;
 			fObj.indexPos = -1;
 
 			objectFilePos = file.getLength();
 		} else {
 			file.seek(fObj.filePos);
 			int oldSize = ds.readInt();
 			if(oldSize < count){
 				// we need to put the object at the end of the file
 				objectFilePos = file.getLength();
 			} else {
 				objectFilePos =  fObj.filePos;
 			}
 		}
 		file.seek(objectFilePos);
 		fObj.filePos = objectFilePos;
 		ds.writeInt(count);
 		file.writeBytes(buffer, start, count);
 
 		setObjectIndex(fObj, file);
 
 		if(ptr.devId == curDevId && nextObjId <= ptr.objId){
 			nextObjId = ptr.objId;
 		}
 
 		file.seek(4);
 		ds.writeInt(nextObjId);
 
 		file.close();
 		return true;	
 	}
 }
