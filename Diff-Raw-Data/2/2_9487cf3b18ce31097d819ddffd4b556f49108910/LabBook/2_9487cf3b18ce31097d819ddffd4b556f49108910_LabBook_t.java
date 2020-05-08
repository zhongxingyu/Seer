 package org.concord.LabBook;
 
 import waba.io.*;
 import waba.util.*;
 import extra.io.*;
 
 public class LabBook 
 {
 //	public static Vector objFactories;
 	public static LabObjectFactory []objFactories = null;
     int curDeviceId = 0;
 
     /*
      * Need a hash table of the loaded obj. and
      * match them by the values of the pointers
      *
      * For temporary purposes I could just use a vector
      * and scan through it to find the objects
      */
 
     /*
      * The tricky stuff here is keeping the object reference
      * around or not around.  All un-freed objects need to be kept
      * in the cache, so that multiple references don't exist. 
      * this makes it kind of ugly.  The only other approach is
      * to build this directly into the objects.  If there was 
      * a clean up call when the object was gc'd then this would
      * be cleaner.
      * 
      * How does real Java do this?
      * The other options is to keep all objects in the cache while
      * a transaction is open.  And remove them all when the transaction
      * is closed.  This would mean that when the objects are loaded at
      * the beginning they'd need a name that could be used to identify
      * them when they are saved at the end.  
      *
      * The odmg doesn't apear to handle this at all.  When an object
      * is loaded or lookedup it doesn't necessarly handle 
      */
 
     int objIndex [];
 
     static LabObjectPtr nullLObjPtr = new LabObjectPtr(-1,-1,null);
     LabObjectPtr rootPtr = null;
 
     LabBookDB db;
 
 	public static void init()
 	{
 		registerFactory(new DefaultFactory());
 	}
 
     // Should get a list of the pointer to objects from the 
     // beginning of the file.  This will also help us know
     // what ids are available.  And it will tell the file address of 
     // the object.  This could be stored in an 3-tuple int array
     // this entire array will be loaded at the beginning.
     public void open(LabBookDB db)
     {
 		this.db = db;
 		curDeviceId = db.getDevId();
 		rootPtr = new LabObjectPtr(db.getRootDevId(), db.getRootObjId(), null);
     }
 
     public LabObjectPtr getRoot()
     {
 		return rootPtr;
     }
 
 	
 	public static LabObjectFactory findFactoryByType(int factoryType){
 		if(objFactories == null || objFactories.length < 1) return null;
 		for(int i = 0; i < objFactories.length; i++){
 			if(factoryType == objFactories[i].getFactoryType()){
 				return objFactories[i];
 			}
 		}
 		return null;
 	}
 	
 	public static void registerFactory(LabObjectFactory objFact)
 	{
 		if(objFact == null) return;
 		if(findFactoryByType(objFact.getFactoryType()) != null) return;
 		
 		int nFactories = (objFactories == null)?0:objFactories.length;
 		LabObjectFactory []newFactories = new LabObjectFactory[nFactories + 1];
 		if(objFactories != null){
 			waba.sys.Vm.copyArray(objFactories,0,newFactories,0,nFactories);
 		}
 		newFactories[nFactories] = objFact;	
 		objFactories = newFactories;
 
 	}
 
 	public static LabObject makeNewObj(int type)
 	{
 		return makeNewObj(type, true);
 	}
 
 	public static LabObject makeNewObj(int type, boolean init)
 	{
 		if(objFactories == null) return null;
 		LabObject newObj = null;
 		for(int i=0;i<objFactories.length; i++){
 			LabObjectFactory objFact = objFactories[i];
 			newObj = objFact.makeNewObj(type, init);
 			if(newObj != null) break;
 		}
 
 		return newObj;
 	}
 
     // add this object to list to be stored
     // and return its pointer.
     // if it has a pointer thats easy
     // if not them make a new one using 
     // curDeviceId and nextObjectId
 
     Vector toBeStored = new Vector();
     Vector alreadyStored = null;
     public LabObjectPtr store(LabObject lObj)
     {
 		LabObjectPtr lObjPtr;
 	
 		if(lObj == null) return nullLObjPtr;
 
 		// Need to check if this object has already been stored
 		if(lObj.ptr != null){
 			lObjPtr = lObj.ptr;
 			// Should be check to see if this pointer already exist in store
 			// list??
 		} else {
 			Debug.println("Creating new ptr");
 			lObjPtr = new LabObjectPtr(curDeviceId, db.getNewObjId(),
 									   lObj);
 			lObj.ptr = lObjPtr;
 		}
 
 		int i;
 		Object [] curObjArr;
 		if(alreadyStored != null){
 			curObjArr = alreadyStored.toObjectArray();
 			for(i=0; i< curObjArr.length; i++){
 				if(curObjArr[i] == lObjPtr){
 					// this ptr has already been stored.
 					break;
 				}
 			}
 
 			if(i != curObjArr.length){
 				// This object has already been written out
 				return lObjPtr;
 			}
 		}
 
 		curObjArr = toBeStored.toObjectArray();
 		for(i=0; i< curObjArr.length; i++){
 			if(curObjArr[i] == lObjPtr){
 				// this ptr will be stored.
 				break;
 			}
 		}
 
 		if(i != curObjArr.length){
 			// This object is slated to be written out
 			return lObjPtr;
 		}
 
 		toBeStored.add(lObjPtr);
 		loaded.add(lObjPtr);
 		return lObjPtr;
     }
 
     /*
      * Need to iterate until all the objects are written
      */
     // write all the "stored" objects
     // in the process this might add more stored objects
     
     // Question: should load look at unstored objects?
     // I guess no, because it won't know about unstored objects
     // until they are commited.
     
     // this calls the writeExternal call of each object
     // it also sets up the bufferstream for the object
     // if a store is called in the middle of the writeExternal
     // a pointer needs to be returned and the object is schedualed
     // for later storage.
 
     // what if there are objects in the database that weren't loaded
     // what do we do with them?  And if they are loaded and changed
     // then we need to replace them.
 
     // One way to handle all this is to write all the "new" objects 
     // to a temporary file and then copy any un-written objects from
     // the old file.  But that is a time consuming operation.  It seems
     // that should be a special function (perhaps compact).  The alternative
     // is write the object in the empty spaces of the file, or be able
     // to write parts of the object to multiple sections of the file.  
     // That would be the best.
     BufferStream bsOut = new BufferStream();
     DataStream dsOut = new DataStream(bsOut);
 
     public boolean commit()
     {
 		Object [] curObjArr;
 		int i,j;
 		LabObjectPtr curObjPtr;
 		byte [] outBuf;
 
 		alreadyStored = new Vector();
 
 		while(toBeStored.getCount() > 0){
 			curObjArr = alreadyStored.toObjectArray();
 
 			curObjPtr = (LabObjectPtr)toBeStored.get(0);
 
 			for(i=0; i< curObjArr.length; i++){
 				if(curObjArr[i] == curObjPtr){
 					// this ptr has already been stored.
 					toBeStored.del(0);
 					break;
 				}
 			}
 			if(i == curObjArr.length){
 				// This object hasn't been stored yet.
 				alreadyStored.add(curObjPtr);
 				toBeStored.del(0);
 
 				dsOut.writeInt(curObjPtr.obj.objectType);
 				dsOut.writeString(curObjPtr.obj.getName());
 
 				// This might call store which will change the toBeStored vector
 				curObjPtr.obj.writeExternal(dsOut);
 				outBuf = bsOut.getBuffer();
 				if(!db.writeObjectBytes(curObjPtr.devId, curObjPtr.objId, 
 										outBuf, 0, outBuf.length)){
 					toBeStored = new Vector();
 					loaded = new Vector();
 					alreadyStored = null;
 		    
 					return false;
 				}
 
 				bsOut.setBuffer(null);
 			}
 		}	       
 
 		toBeStored = new Vector();
 		loaded = new Vector();
 		alreadyStored = null;
 
 		db.save();
 
 		return true;
     }
 
 	boolean commit(LabObjectPtr lObjPtr)
 	{
 		byte [] outBuf;
 
 		// delete this from the to be stored list
 		int index = toBeStored.find(lObjPtr);
 		if(index < 0) return false;
 
 		// write object header
 		dsOut.writeInt(lObjPtr.obj.objectType);
 		dsOut.writeString(lObjPtr.obj.getName());
 
 		// This might call store which will change the toBeStored vector
 		lObjPtr.obj.writeExternal(dsOut);
 		outBuf = bsOut.getBuffer();
 		if(!db.writeObjectBytes(lObjPtr.devId, lObjPtr.objId, 
 								outBuf, 0, outBuf.length)){
 			return false;
 		}
 
 
 		bsOut.setBuffer(null);
 
 		toBeStored.del(index);
 
 		return true;
 	}
 
     public boolean reload(LabObject lObj)
     {
 		if(lObj == null) return false;
 
 		// Need to check if this object has already been stored
 		if(lObj.ptr == null){
 			Debug.println("reload: Null pointer");
 			return false;
 		}
 		int i;
 		int numLoaded = loaded.getCount();
 		LabObjectPtr curObjPtr;
 
 		for(i=0; i<numLoaded; i++){
 			curObjPtr = (LabObjectPtr)loaded.get(i);
 			if(curObjPtr.equals(lObj.ptr)){
 				break;
 			}
 		}
 
 		if(i == numLoaded){
 			// This object hasn't been loaded or there was a commit since it 
 			// was last loaded.  So it should be loaded using the regular load			
 			Debug.println("reload: Not pre-loaded");
 			loaded.add(lObj.ptr);
 			return false;
 		}
 
 		BufferStream bsIn = new BufferStream();
 		DataStream dsIn = new DataStream(bsIn);
 
 		// We didn't find it so we need to parse it from the file
 		byte [] buffer = db.readObjectBytes(lObj.ptr.devId, lObj.ptr.objId);
 		if(buffer == null) return false;
 
 		// set bufferStream buffer
 		// read buffer by
 		bsIn.setBuffer(buffer);
 	
 		int objectType = dsIn.readInt();
 
 		if(lObj.objectType != objectType){
 			Debug.println("reload: Non-matching object");
 			return false;
 		}
 
 		// We should check if the object is in the loaded list
 		//	.. loaded.add(lObjPtr);
 		    
 		lObj.readExternal(dsIn);
 
 		return true;
 	      
     }
 
     // increase reference count in lab Object
     // check if value of lObjPtr matches a prev. loaded object
     // or stored object
     // return that object
     // otherwise, 
     // find object in file
     // instanciate object
     // get length of object data from file
     // this requires a bufferDataStream for input and output
     // the input buffer size can be fixed, but the output
     // buffer size needs to be dynamic and beable to report
     // can use wextras DataStream and BufferStream
     // the length of what has been written
     // read object byes 
     // add new object to hashtable
     // send bytes to object readExternal
     // if another object is loaded in the middle of the readExternal
     // this should be ok. Loops won't be formed because the current
     // object is already in the hashtable so it won't be "loaded" again.
 
     Vector loaded = new Vector();
 
     public LabObject load(LabObjectPtr lObjPtr)
     {
 		if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return null;
 
 		DataStream dsIn = initPointer(lObjPtr);
 		if(dsIn == null){
 			// this object was already loaded
			if(lObjPtr.obj != null) lObjPtr.obj.incRefCount();
 			return lObjPtr.obj;
 		}
 
 		LabObject lObj = null;
 
 		// We need a way to instanciate object.
 		// We could have a list of objects and every new lab object will
 		// need to be added to this list.
 		lObj = makeNewObj(lObjPtr.objType, false);
 		if(lObj == null){
 			Debug.println("error: objectType: " + lObjPtr.objType + " devId: " + lObjPtr.devId +
 						  " objId: " + lObjPtr.objId);
 			return null;
 		}
 		lObj.ptr = lObjPtr;
 		lObjPtr.obj = lObj;
 		lObj.setName(lObjPtr.name);
 		lObj.incRefCount();
 
 		// This might be recursive so add this object to the 
 		// loaded array so we don't load it again
 		loaded.add(lObjPtr);
 		    
 		lObj.readExternal(dsIn);
 
 		return lObj;
     }
 
 	/**
 	 * Check if the object this pointer points to is already loaded
 	 * If so, update this pointers name and type info
 	 * If not, read the heading information from the object
 	 * 
 	 * returns the datastream of the object data if the object hasn't
 	 * already been loaded, otherwise it returns null
 	 */
 	private DataStream initPointer(LabObjectPtr lObjPtr)
 	{
 		int i;
 		int numLoaded = loaded.getCount();
 		LabObjectPtr curObjPtr;
 
 		// if this is true we have major problems
 		// if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return null;
 		for(i=0; i<numLoaded; i++){
 			curObjPtr = (LabObjectPtr)loaded.get(i);
 			if(curObjPtr.equals(lObjPtr)){
 				lObjPtr.obj = curObjPtr.obj;
 				lObjPtr.objType = lObjPtr.obj.objectType;
 				lObjPtr.name = lObjPtr.obj.getName();
 				return null;
 			}
 		}
 
 
 		BufferStream bsIn = new BufferStream();
 		DataStream dsIn = new DataStream(bsIn);
 
 		// We didn't find it so we need to parse it from the file
 		byte [] buffer = db.readObjectBytes(lObjPtr.devId, lObjPtr.objId);
 		if(buffer == null){
 			return null;
 		}
 		// set bufferStream buffer
 		// read buffer by
 		bsIn.setBuffer(buffer);
 	
 		lObjPtr.objType = dsIn.readInt();
 		lObjPtr.name = dsIn.readString();
 
 		return dsIn;
 	}
 
 	public boolean readHeader(LabObjectPtr lObjPtr)
 	{
 		// if this is true we have major problems
 		if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return false;
 
 		initPointer(lObjPtr);
 		return true;
 	}
 
 	public void release(LabObject lObj)
 	{
 		LabObjectPtr curObjPtr = null;
 		LabObjectPtr lObjPtr = lObj.ptr;
 
 		int numLoaded = loaded.getCount();
 		// if this is true we have major problems
 		// if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return null;
 		for(int i=0; i<numLoaded; i++){
 			curObjPtr = (LabObjectPtr)loaded.get(i);
 			if(curObjPtr.equals(lObjPtr)){
 				// found it
 				commit(lObjPtr);
 				loaded.del(i);
 				return;
 			}
 		}		
 	}
 
 	public void export(LabObject lObj, LabBookDB db)
     {
 		if(waba.sys.Vm.getPlatform().equals("PalmOS")){
 			Catalog memoDB = new Catalog("MemoDB.memo.DATA", Catalog.READ_WRITE);
 			memoDB.addRecord(100);
 			DataStream ds = new DataStream(memoDB);
 			ds.writeCString("Hello World");
 			ds.writeCString("dlroW olleH");
 			memoDB.close();
 			return;
 		}
 
 		LabBookDB oldDb = this.db;
 
 		commit();
 		// this might switch the root obj pointer
 		LabObjectPtr lObjPtr = exportAll(lObj);
 		this.db = db;
 		db.setRootDevId(lObjPtr.devId);
 		db.setRootObjId(lObjPtr.objId);
 
 
 
 		commit();
 		this.db = oldDb;	
     }
 
 	/*
 	  This seems like it might cause problems because
 	  The loaded objects are just getting stored again
 	  but since the pointers are the same we don't have to
 	  worry about any pointer conflicts
 	*/
 	private LabObject export(LabObjectPtr lObjPtr)
 	{
 		LabObject obj = load(lObjPtr);
 		store(obj);
 		return obj;		
 	}
 
     private LabObjectPtr exportAll(LabObject lObj)
     {	
 		LabObjectPtr lObjPtr = null;
 		if(lObj instanceof LObjSubDict){
 			// hmm. we need the subDict
 			lObj = ((LObjSubDict)lObj).getDict();
 		}
 		lObjPtr = lObj.ptr;
 
 		LabObject obj = export(lObjPtr);
 		Vector dictionaries = new Vector();
 				
 		if(obj instanceof LObjDictionary){
 			dictionaries.add(obj);
 		}
 
 		int curDict=0;
 		while(curDict < dictionaries.getCount()){
 			LObjDictionary dict = (LObjDictionary)dictionaries.get(curDict);
 			for(int i=0; i<dict.getChildCount(); i++){
 				
 				obj = export((LabObjectPtr)dict.objects.get(i));
 				if(obj instanceof LObjDictionary){
 					int oldIndex = dictionaries.find(obj);
 					if(oldIndex < 0){
 						// this object hasn't been added to dicts yet
 						dictionaries.add(obj);
 					}
 				}
 			}
 			curDict++;
 		}
 
 		return lObjPtr;
     }
 
 
     public LabObject importDB(LabBookDB db)
     {
 		LabBookDB oldDb = this.db;
 
 		commit();
 		this.db = db;
 		LabObject root = 
 			importAllPtr(new LabObjectPtr(db.getRootDevId(), 
 										  db.getRootObjId(), null), 
 						 oldDb);
 		this.db = oldDb;
 		commit();
 		return root;
     }
     
 	Vector importedObjs = null;	
     public LabObject importPtr(LabObjectPtr ptr, LabBookDB oldDB)
     {
 		for(int i=0; i<importedObjs.getCount(); i++){
 			LabObjectPtr origPtr = (LabObjectPtr)importedObjs.get(i);
 			if(origPtr.equals(ptr)){
 				return (LabObject)importedObjs.get(i+1);
 			}
 			// skip new object that goes with the origPtr
 			i++;
 		}
 
 		LabObject obj = load(ptr);
 		if(obj == null) return null;
 
 		// we are blasting the pointer from the import file
 		// and replacing it with a pointer from the "current" database
 		// "current == old"
 		obj.ptr = new LabObjectPtr(curDeviceId, oldDB.getNewObjId(),
 								   obj);
 		importedObjs.add(ptr);
 		importedObjs.add(obj);
 		return obj;
 	}
 
     public LabObject importAllPtr(LabObjectPtr ptr, LabBookDB oldDB)
     {
 		Vector dictionaries = new Vector();
 		importedObjs = new Vector();
 
 		LabObject firstObj = importPtr(ptr, oldDB);
 		LabObject obj = firstObj;
 
 		if(obj instanceof LObjDictionary){
 			dictionaries.add(obj);
 		}
 
 		int curDict=0;
 		while(curDict < dictionaries.getCount()){
 			LObjDictionary dict = (LObjDictionary)dictionaries.get(curDict);
 			LabObject child = null;
 			for(int i=0; i<dict.getChildCount(); i++){				
 				child = importPtr((LabObjectPtr)dict.objects.get(i), oldDB);
 				// this is weird I don't know if it is correct
 				if(child == null){
 					dict.objects.set(i, nullLObjPtr);
 				} else {
 					dict.objects.set(i, child.ptr);
 				}
 				if(child instanceof LObjDictionary){
 					int oldIndex = dictionaries.find(child);
 					if(oldIndex < 0){
 						// this object hasn't been added to dicts yet
 						dictionaries.add(child);
 					}
 				}
 			}
 			curDict++;
 		}
 
 		/* We need to store all the objects after we have done
 		   all the loading form the import DB.
 		   these stored objects get put into the loaded vector
 		   so they can conflict with the incoming objects from the
 		   import db.
 		*/
 		for(int i=1; i<importedObjs.getCount(); i+=2){
 			LabObject newObj = (LabObject)importedObjs.get(i);
 			store(newObj);
 		}
 		importedObjs = null;
 		return firstObj;
 	}
 
     public boolean close()
     {
 		boolean ret = db.save();
 
 		db.close();
 
 		return ret;
     }
 }
