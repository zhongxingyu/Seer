 package org.concord.LabBook;
 
 import waba.io.*;
 import waba.util.*;
 import waba.sys.*;
 import extra.io.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import graph.*;
 
 public class LabBookCatalog 
     implements LabBookDB, DialogListener
 {
     DataStream ds;
 
     int [] objIndex = null;
 
     Vector objects = new Vector();
 
     int curDevId;
     int nextObjId;
     int rootDevId;
     int rootObjId;
 
     Catalog cat;
 
     boolean error = false;
 
     public LabBookCatalog(String name)
     {
 	cat = new Catalog(name + ".LaBk.DATA", Catalog.CREATE);
 	if(cat.isOpen()){
 	    cat.close();
 	    cat = new Catalog(name  + ".LaBk.DATA", Catalog.READ_WRITE);
 	    if(!cat.isOpen()){
 		//Errorr
 		cat = null;
 		error = true;
 		return;
 	    }		
 	} else {
 	    cat = null;
 	    error = true;
 	    return;
 	    // Error hmmm..  I don't know what to do here
 	}
 
 	int numRecs;
 	if((numRecs = cat.getRecordCount()) == 0){
 	    // New database
 	    curDevId = 0;
 	    nextObjId = 0;
 	    rootDevId = 0;
 	    rootObjId = 0;
 	    ds = new DataStream(cat);
 	    return;
 	}
 
 
 	ds = new DataStream(cat);
 	if(!readIndex()){
 	    // Failed
 	    openErr("1");
 	    error = true;
 	    return;
 	}
 	
     }
 
     public int getRootDevId(){return rootDevId;}
     public int getRootObjId(){return rootObjId;}
 
     public void setRootDevId(int id){rootDevId = id;}
     public void setRootObjId(int id){rootObjId = id;}
 
     public int getDevId()
     {
 	return curDevId;
     }
 
     public int getNewObjId()
     {
 	return nextObjId++;
     }
 
     String [] errButtons = {"Bummer"};
     Dialog errDialog = null;
     public void showError(String msg)
     {
 	errDialog = Dialog.showInputDialog(this, "Error saving LbBk", msg, errButtons, Dialog.EDIT_INP_DIALOG);
     }
 
     public void dialogClosed(DialogEvent e)
     {
 	String command = e.getActionCommand();
 	if(e.getSource() == errDialog){
 	    if(command.equals(errButtons[0])){
 	    }
 	}
     }
 
     public void openErr(String msg)
     {
 	showError("LbDB open: " + msg);
 	if(cat != null){
 	    cat.close();
 	    cat = null;
 	}
     }
 
     public boolean closeErr(String msg)
     {
 	showError("LbDB close: " + msg);
 	if(cat != null){
 	    cat.close();
 	    cat = null;
 	}
 	return false;
     }
 
     public boolean save()
     {
 	if(cat.getRecordCount() == 0 ||
 	   !cat.setRecordPos(0)) {
 	    return false;
 	}
     
 	cat.skipBytes(4);
 	ds.writeInt(nextObjId);
	ds.writeInt(rootDevId);
	ds.writeInt(rootObjId);
 
 	cat.setRecordPos(-1);
 
 	return true;
 
     }
     
     public void close()
     {
 	if(cat != null){	    
 	    cat.close();
 	}
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
     public boolean readIndex()
     {
 	
 	if(!cat.setRecordPos(0)) return false;
 
 	curDevId = ds.readInt();
 	nextObjId = ds.readInt();
 	rootDevId = ds.readInt();
 	rootObjId = ds.readInt();
 
 	int length = ds.readInt();
 	objIndex = new int [length*3];
 	int pos = 0;
 	int i;
 
 	for(i=0; i<length; i++){
 	    objIndex[pos++] = ds.readInt();
 	    objIndex[pos++] = ds.readInt();
 	    objIndex[pos++] = ds.readInt();
 	}
 
 	cat.setRecordPos(-1);
 	return true;
     }
 
     public boolean getError(){return error;};
 
     // search through object Index       
     // and find object bytes
     public byte [] readObjectBytes(int devId, int objId)
     {
 	int i;
 	int objSize = 0;
 	byte [] buffer = null;
 
 	if(objIndex == null) return null;
 
 	for(i=0; i < objIndex.length; i+=3){
 	    if(objIndex[i] == devId &&
 	       objIndex[i+1] == objId){
 		if(!cat.setRecordPos(objIndex[i+2])){
 		    error = true; 
 		    openErr("read:" + i);
 		    return null;
 		}
 
 		objSize = cat.getRecordSize();
 		buffer = new byte [objSize];
 		cat.readBytes(buffer, 0, objSize);
 		cat.setRecordPos(-1);		
 	    }
 	}
 	
 	return buffer;
     }
 
     // check if this object already exists
     // if not add it to the file and add it
     // to the objIndex ??? (objIndex should be vector)
     // if it does exist, then replace it's entry in the the
     // objIndex with the new value after adding it to the file
 
     // In the longer term we will have to write the objects in 
     // peices and keep track of free space in the file.
     public boolean writeObjectBytes(int devId, int objId, byte [] buffer, int start,
 			     int count)
     {
 	int i;
 
 	if(objIndex != null){
 	    for(i=0; i < objIndex.length; i+=3){
 		if(objIndex[i] == devId &&
 		   objIndex[i+1] == objId){
 		    if(!cat.setRecordPos(objIndex[i+2])){
 			error = true; 
 			openErr("write:" + i);
 			return false;
 		    }
 
 		    if(cat.getRecordSize() != count){
 			if(!cat.resizeRecord(count)) return closeErr("5:" + cat.getError());
 			if(!cat.setRecordPos(objIndex[i+2])) return closeErr("6:" + cat.getError());
 		    }
 
 		    cat.writeBytes(buffer, start, count);	
 		    cat.setRecordPos(-1);
 		    return true;
 		}	    
 	    }
 
 	    int [] newObjIndex = new int [objIndex.length + 3];
 	    Vm.copyArray(objIndex, 0, newObjIndex, 0, objIndex.length);
 	    objIndex = newObjIndex;
 
 	} else {
 	    objIndex = new int [3];
 	    
	    cat.addRecord(20);
 	    ds.writeInt(curDevId);
 	    ds.writeInt(nextObjId);
	    ds.writeInt(rootDevId);
	    ds.writeInt(rootObjId);
 	    ds.writeInt(0);
 	}
 
 	int newRecPos = cat.addRecord(count);
 	if(newRecPos < 0){
 	    return false;
 	}
 	    
 	cat.writeBytes(buffer, start, count);
 	cat.setRecordPos(-1);
 
 	int objPos = objIndex.length - 3;
 
 	if(!cat.setRecordPos(0)) return false;
	cat.resizeRecord(cat.getRecordSize() + 20);
 
	cat.skipBytes(16);
 	ds.writeInt(objIndex.length / 3);
 	cat.skipBytes(objPos*4);
 
 	ds.writeInt(devId);
 	ds.writeInt(objId);
 	ds.writeInt(newRecPos);
 
 	objIndex[objPos] = devId;
 	objIndex[objPos+1] = objId;
 	objIndex[objPos+2] = newRecPos;
 
 	cat.setRecordPos(-1);
 	return true;       
     }
   
 }
