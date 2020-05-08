 /**
  * 
  */
 package org.concord.otrunk.datamodel;
 
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTXMLString;
 
 /**
  * @author scott
  *
  */
 public class Copier 
 {
 	OTDatabase otDb;
 	OTDataObject root;
 	
 	Vector toBeCopied;
 	private OTDataList orphanList;
 
     private static class CopyEntry
     {    	
     	OTDataObject original;
     	int maxDepth;
     	OTDataObject copy;
     	
     	public CopyEntry(OTDataObject original, int maxDepth, OTDataObject copy)
     	{
     		this.original = original;
     		this.maxDepth = maxDepth;
     		this.copy = copy;
     	}    	
     }
     
 	public Copier(OTDatabase otDb, OTDataList orphanDataList)
 	{
 		this.otDb = otDb;
 		this.toBeCopied = new Vector();
 		this.orphanList = orphanDataList;
 	}
 	
     private CopyEntry getCopyEntry(OTID originalId)
     {
     	for(int i=0; i<toBeCopied.size(); i++){
     		CopyEntry entry = (CopyEntry)toBeCopied.get(i);
     		if(entry.original.getGlobalId().equals(originalId)){
     			return entry;
     		}
     	}
     	return null;
     }
     
      private Object handleChild(Object child, int maxDepth) 
     throws Exception
     {
 		// check if the value is an OTID
 		// and if we have more depth to go
 		if(child instanceof OTID && (maxDepth == -1 || maxDepth > 0)){
 			// check if this id has already been seen
 			CopyEntry itemCopyEntry = getCopyEntry((OTID)child);
 			if(itemCopyEntry == null){
 				OTDataObject itemObj = 
 					otDb.getOTDataObject(root, (OTID)child);
				if(itemObj == null){
					throw new IllegalStateException("Can't find child id: " + child);
				}
 				OTDataObject itemCopy = 
 					otDb.createDataObject(itemObj.getType());
 				int copyMaxDepth = -1;
 				if(maxDepth != -1){
 					copyMaxDepth = maxDepth-1; 
 				}
 				itemCopyEntry = new CopyEntry(itemObj, copyMaxDepth, itemCopy);
 				toBeCopied.add(itemCopyEntry);
 				
 			}
 
 			// put this new list object 
 			child = itemCopyEntry.copy.getGlobalId();
 		}
 		
 		return child;
     }
     
     public static void copyInto(OTDataObject source, OTDataObject dest,
     		OTDataList orphanDataList, int maxDepth) 
     throws Exception
     {
     	Copier copier = new Copier(dest.getDatabase(), orphanDataList);
     	copier.internalCopyInto(source, dest, maxDepth);    	
     }
     
     public void internalCopyInto(OTDataObject source, OTDataObject dest,
     		int maxDepth) throws Exception
     {
     	toBeCopied.add(new CopyEntry(source, maxDepth, dest));
     	OTDatabase otDb = dest.getDatabase();
     	
     	int currentIndex = 0;
     	while(currentIndex < toBeCopied.size()){
     		CopyEntry entry = (CopyEntry)toBeCopied.get(currentIndex);
     		OTDataObject original = entry.original; 
     		OTDataObject copy = entry.copy;
     		if(copy == null) {
     			copy = otDb.createDataObject(original.getType());
     		}
     		
     		String [] keys = original.getResourceKeys();
     		
     		// This is used to handle a hack for strings which reference objects
     		Vector secondPassKeys = new Vector();
     		
     		for(int i=0; i<keys.length; i++){
     			Object resource = original.getResource(keys[i]);
 
     			if(resource instanceof OTDataList){
     				OTDataList copyList =
     					(OTDataList)copy.getResourceCollection(keys[i], 
     							OTDataList.class);
     				OTDataList list = (OTDataList)resource;
     				copyList.removeAll();
     				for(int j=0; j<list.size(); j++){
     					Object listItem = list.get(j);
     					
     					listItem = handleChild(listItem, entry.maxDepth);
     					copyList.add(listItem);
     				}
     			} else if(resource instanceof OTDataMap){
     				OTDataMap copyMap =
     					(OTDataMap)copy.getResourceCollection(keys[i], 
     							OTDataMap.class);                    
     				OTDataMap map = (OTDataMap)resource;
     				copyMap.removeAll();
     				String [] mapKeys = map.getKeys();
     				for(int j=0; j<mapKeys.length; j++){
     					Object item = map.get(mapKeys[j]);
     					item = handleChild(item, entry.maxDepth);    					
     					copyMap.put(mapKeys[j], item);
     				}
     			} else if(resource instanceof OTXMLString) { 
     				secondPassKeys.add(keys[i]);
     			} else {
     				resource = handleChild(resource, entry.maxDepth);    
     				copy.setResource(keys[i], resource);
     			}
     		}
     		
     		for(int i=0; i<secondPassKeys.size(); i++){
     			String key = (String)secondPassKeys.get(i);
     			Object resource = original.getResource(key);
 
     			System.out.println("text copying " + key);
 
     			// This is a hack, if we had namespaces it wouldn't be as
 				// much of a hack.
 				OTXMLString xmlString = (OTXMLString) resource;
 				// search for the refid="" in the string and copy all the 
 				// referenced objects.
 				Pattern pattern = Pattern.compile("refid=\"([^\"]*)\"");
 				Matcher matcher = pattern.matcher(xmlString.getContent());
 				StringBuffer copiedStringBuf = new StringBuffer();
 				while(matcher.find()){
 					String otidStr = matcher.group(1);
 					// create an OTID from this id
 					OTID otid = OTIDFactory.createOTID(otidStr);
 					
 					// check if this id has aready been copied
 					CopyEntry copyEntry = getCopyEntry(otid);
 					
 					Object copiedId = null;
 					if(copyEntry != null){
 						copiedId = copyEntry.copy.getGlobalId();
 					} else {
 						// If we are here it is because we didn't already store this 
 						// copied object somewhere.  
						
						// We should first check if the otid is a valid id, if not then it is
						// an invalid match
						OTDataObject matchedDataObject = otDb.getOTDataObject(root, otid);
						if(matchedDataObject == null){
							// this id isn't in our database or it is invalid
							// print a warning and continue
							System.err.println("Can't find object to copy: " + otid + " skipping object");
							
							// put the same string back in. 
							matcher.appendReplacement(copiedStringBuf, "$0");
							
							continue;
							
						}

						
 						// If we have an orphanList then the object can be stored there
 						// if we not then print a warning message and use the original
 						// object.
						
						
						
 						if(orphanList == null){
 							System.err.println("Cannot copy objects referenced from " +
 							"xml strings, that are not stored somewhere else");
 							System.err.println("  original object: " + otid);
 							System.err.println("  object referencing it: " + original);
 							System.err.println("  string property: " + key);
 							
 							// Just use the original id
 							copiedId = otid;							
 						} else {
 							copiedId = handleChild(otid, entry.maxDepth);							
 							orphanList.add(copiedId);
 						}						
 					}
 										
 					// replace the id with the new id
 					matcher.appendReplacement(copiedStringBuf, 
 							"refid=\"" + copiedId + "\"");
 					
 					// now the next problem is where to store this copied object
 					// if it was already handled we don't need to figure out 
 					// where to store it.  If we had a record of the containment
 					// we could use that figure out where to store this copy
 				}
 				matcher.appendTail(copiedStringBuf);
 				
 				OTXMLString copiedXmlString = 
 					new OTXMLString(copiedStringBuf.toString());
 				copy.setResource(key, copiedXmlString);
     			
     		}
     		currentIndex++;
     	}
     }
 
 }
