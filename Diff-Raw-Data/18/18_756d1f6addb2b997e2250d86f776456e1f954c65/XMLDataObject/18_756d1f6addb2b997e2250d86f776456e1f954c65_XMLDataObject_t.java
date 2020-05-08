 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.17 $
  * $Date: 2007-07-25 20:25:34 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.otrunk.datamodel.OTDataCollection;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTObjectRevision;
 
 
 /**
  * XMLDataObject
  * Class name and description
  *
  * Date created: Oct 4, 2004
  *
  * @author scott<p>
  *
  */
 public class XMLDataObject
 	implements OTDataObject
 {
 	private static final Logger logger = Logger.getLogger(XMLDataObject.class.getName());
 	private OTDataObjectType type;
 	private OTID globalId;
 	private XMLDatabase database = null;
 	private OTXMLElement element;
 	private String localId = null;
 	private boolean preserveUUID = false;
 	
 	/**
 	 * If this is true then nulls which are set on this object will
 	 * be saved in a "unset" property on the object.  The value is a 
 	 * space separated list of properties which are null. 
 	 * 
 	 * If this is false then nulls simply not recorded.  This approach
 	 * is used when this object is layered on top of another object
 	 * and needs to override a value set on that lower object.
 	 */
 	private boolean saveNulls = false;
 	
 	HashMap<String, Object> resources = new LinkedHashMap<String, Object>();
 	HashMap<String, XMLReferenceInfo> referenceInfoMap = new HashMap<String, XMLReferenceInfo>();
 	XMLDataObject container = null;
 	String containerResourceKey = null;
 	
 	XMLDataObject(OTXMLElement element, OTID id, XMLDatabase db)
 	{
 		this.element = element;
 		globalId = id;
 		database = db;
 	}
 	
 	public void setType(OTDataObjectType type)
 	{
 		this.type = type;
 	}
 	
 	public OTXMLElement getElement()
 	{
 		return element;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.OTDataObject#getGlobalId()
 	 */
 	public OTID getGlobalId()
 	{
 		return globalId;
 	}
 
 	public OTDatabase getDatabase()
 	{
 	    return database;
 	}
 	
 	public void setGlobalId(String id)
 	{
 		setGlobalId(OTIDFactory.createOTID(id));
 	}
 	
 	public void setGlobalId(OTID id)
 	{
 		globalId = id;
 	}
 	
 	public boolean setResource(String key, Object resource)
 	{
 	    return setResource(key, resource, true);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.OTDataObject#setResource(java.lang.String, java.lang.Object)
 	 */
 	boolean setResource(String key, Object resource, boolean markDirty)
 	{
 		Object oldObject;
 
 	    if(resource == null && !saveNulls) {
 	    	oldObject = resources.remove(key);
 	    	if (oldObject instanceof OTDataObject) {
 	    		database.removeReference(this, (OTDataObject) oldObject);
 	    	}
 	    } else {
 	    	oldObject = resources.put(key, resource);
 	    	if (oldObject instanceof OTDataObject) {
 	    		database.removeReference(this, (OTDataObject) oldObject);
 	    	}
 	    	if (resource instanceof OTDataObject) {
 	    		database.recordReference(this, (OTDataObject) resource, key);
 	    	}
 	    	if (resource instanceof OTID) {
 	    		try {
 	    			database.recordReference(this, database.getOTDataObject(this, (OTID) resource), key);
 	    		} catch (Exception e) {
 	    			// TODO do we care?
 	    		}
 	    	}
 		}
 
 	    if(!markDirty) return true;
 	    
 		if((oldObject == null && resource == null) || 
 		         (oldObject != null && 
 		                 oldObject.equals(resource))) {		    
 		    // the object wasn't really modified
 			return false;
 		} else {
 			updateModifiedTime();
 		}		
 		
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.OTDataObject#getResource(java.lang.String)
 	 */
 	public Object getResource(String key)
 	{
 		return resources.get(key);
 	}
 
 	Object getResourceWithSuffix(String key)
 	{
 		int suffixStartIndex = key.indexOf('[');
 		if(suffixStartIndex == -1){
 			return getResource(key);
 		}
 		
 		int suffixEndIndex = key.indexOf(']');
 		if(suffixEndIndex == -1){
 			throw new IllegalStateException("invalid key format: " + key);
 		}
 		
 		String property = key.substring(0, suffixStartIndex);
 		Object resource = getResource(property);
 		if(resource instanceof XMLDataList){
 			XMLDataList list = (XMLDataList) resource;
 			String indexStr = key.substring(suffixStartIndex+1, suffixEndIndex);
			int index = Integer.parseInt(indexStr);
			if(index < 0 || index >= list.size()){
				return null;
			}
			return list.get(index);
 		} else if(resource instanceof XMLDataMap){
 			XMLDataMap map = (XMLDataMap) resource;
 			String mapKey = key.substring(suffixStartIndex+2, suffixEndIndex-1);
 			return map.get(mapKey);
 		} else {
 			throw new IllegalStateException("unknown resource type for key: " + key);
 		}		
 	}
 	
 	public void setResourceInfo(String key, XMLReferenceInfo info)
 	{
 		referenceInfoMap.put(key, info);
 	}
 	
 	public XMLReferenceInfo getReferenceInfo(String key)
 	{
 		return referenceInfoMap.get(key);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.OTDataObject#getResourceKeys()
 	 */
 	public String[] getResourceKeys()
 	{
 		Set<String> keySet = resources.keySet();
 		return keySet.toArray(new String [keySet.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.OTDataObject#getCurrentRevision()
 	 */
 	public OTObjectRevision getCurrentRevision()
 	{
 		return null;
 	}
 
 	void updateModifiedTime()
 	{
 	    // update our revision 
 	    // tell the database that it is "dirty" and needs to 
 	    // be synced
 	    database.setDirty(true);
 	}
 	
 	public Collection<Entry<String, Object>> getResourceEntries()
 	{
 		return resources.entrySet();
 	}
 	
 	/**
 	 * @return Returns the localId.
 	 */
 	public String getLocalId()
 	{
 		return localId;
 	}
 	/**
 	 * @param localId The localId to set.
 	 */
 	public void setLocalId(String localId)
 	{
 		this.localId = localId;
 	}
 	
 	@SuppressWarnings("unchecked")
     public <T extends OTDataCollection> T getResourceCollection(String key, 
 		Class<T> collectionClass)
 	{
 		Object listObj = getResource(key);
 		if(collectionClass.isInstance(listObj)) {
 			return (T)listObj;
 		}
 
 	    OTDataCollection collection = null;
 		if(collectionClass.equals(OTDataList.class)) {
 			collection =  new XMLDataList(this);
 		} else if(collectionClass.equals(OTDataMap.class)) {
 			collection =  new XMLDataMap(this);
 		}
 		
 		if(collection != null) {
 		    // don't mark the object as dirty until the collection has actually be modified
 		    setResource(key, collection, false);
 		}
 	    return (T)collection;
 	}
 
 	public OTDataObjectType getType()
     {
 		return type;
     }
 
 	public XMLDataObject getContainer()
     {
     	return container;
     }
 
 	public void setContainer(XMLDataObject container)
     {
     	this.container = container;
     }
 
 	public String getContainerResourceKey()
     {
     	return containerResourceKey;
     }
 
 	public void setContainerResourceKey(String containerResourceKey)
     {
     	this.containerResourceKey = containerResourceKey;
     }
 
 	public URL getCodebase()
     {
 		return database.getContextURL();
     }
 
 	public boolean isPreserveUUID()
     {
     	return preserveUUID;
     }
 
 	public void setPreserveUUID(boolean preserveUUID)
     {
     	this.preserveUUID = preserveUUID;
     }
 
 	public boolean containsKey(String key)
     {
 		return resources.containsKey(key);
     }
 	
 	public void setSaveNulls(boolean flag)
 	{
 		saveNulls = flag;
 	}
 	
 	public boolean getSaveNulls()
 	{
 		return saveNulls;
 	}
 }
