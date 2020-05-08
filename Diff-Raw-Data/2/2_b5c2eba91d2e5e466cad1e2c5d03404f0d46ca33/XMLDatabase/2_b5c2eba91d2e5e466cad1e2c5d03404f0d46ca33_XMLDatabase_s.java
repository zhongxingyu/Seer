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
  * $Revision: 1.43 $
  * $Date: 2007-10-22 01:20:28 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.otrunk.datamodel.BlobResource;
 import org.concord.otrunk.datamodel.OTDataCollection;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTPathID;
 import org.concord.otrunk.datamodel.OTRelativeID;
 import org.concord.otrunk.datamodel.OTUUID;
 import org.concord.otrunk.view.OTConfig;
 import org.concord.otrunk.xml.jdom.JDOMDocument;
 
 
 /**
  * XMLDatabase
  * Class name and description
  *
  * Date created: Nov 19, 2004
  *
  * @author scott<p>
  *
  */
 public class XMLDatabase
 	implements OTDatabase
 {
 	public static boolean TRACE_PACKAGES = 
 		OTConfig.getBooleanProp(OTConfig.TRACE_PACKAGES_PROP, false);
 	
 	OTID rootId = null;
 	
 	ArrayList importedOTObjectClasses = new ArrayList();
 	
 	Hashtable dataObjects = new Hashtable();
 	
 	// a map of xml file ids to UUIDs
 	Hashtable localIdMap = new Hashtable();
 
 	// track whether any object in this database
 	// has changed
 	boolean dirty = false;
 	
     private OTID databaseId;
 	
     PrintStream statusStream = null;
 
 	private Vector packageClasses = new Vector();
 
 	private Hashtable processedOTPackages = new Hashtable();
     
 	private boolean trackResourceInfo = false;
 	
 	private JDOMDocument document;
 	private URL contextURL;
 	
 	private ArrayList listeners;
 	
 	String label;
 	
 	protected static String getLabel(URL contextURL)
 	{
 		if(contextURL == null){
 			return "unknown_name";
 		}
 		return contextURL.toExternalForm();
 	}
 	
 	public XMLDatabase()
 	{
 	    // create an empty database with no root
 	}
 	
 	public XMLDatabase(File xmlFile)
 		throws Exception
 	{
 		this(new FileInputStream(xmlFile), xmlFile.toURL(), null);
 	}
 
 	public XMLDatabase(URL xmlURL)
 		throws Exception
 	{
 		this(xmlURL.openStream(), xmlURL, System.out);
 	}
 
 	public XMLDatabase(URL xmlURL, PrintStream statusStream)
 	throws Exception
 	{
 	    this(xmlURL.openStream(), xmlURL, statusStream);
 	}
 
 	public XMLDatabase(InputStream xmlStream, URL contextURL, PrintStream statusStream)
 	throws Exception
 	{
 		// The "" +  approach allows us to handle nulls.
 		this(xmlStream, contextURL, getLabel(contextURL), statusStream);
 	}
 	
 	public XMLDatabase(InputStream xmlStream, URL contextURL, String label, PrintStream statusStream)
 	throws Exception
 	{
 		this.statusStream = statusStream;
 		this.label = label;
 		
 		// create the database Id
 		// this might get overriden when the objects are loaded in.
 		databaseId = OTUUID.createOTUUID();
 		
 		// printStatus("Opening otml: " + label);
 		
 		// parse the xml file...
 		document = new JDOMDocument(xmlStream);
 		initialize();
 		
 		this.contextURL = contextURL;
 	}	
 	
 	public XMLDatabase(Reader xmlReader, URL contextURL, PrintStream statusStream)
 	throws Exception
 	{
 		this(xmlReader, contextURL, getLabel(contextURL), statusStream);		
 	}
 	
 	public XMLDatabase(Reader xmlReader, URL contextURL, String label, PrintStream statusStream)
 	throws Exception
 	{
 		this.statusStream = statusStream;
 		this.label = label;
 		
 		// printStatus("Opening otml: " + label);
 		document = new JDOMDocument(xmlReader);
 		initialize();
 			
 		this.contextURL = contextURL;
 	}	
 	
 	public boolean equals(Object object)
 	{
 		if(object == this){
 			return true;
 		}
 		
 		if(!(object instanceof XMLDatabase)){
 			return false;
 		}
 		
 		OTID id = getDatabaseId();
 		if(id == null){
 			return false;
 		}
 		
 		return id.equals(((XMLDatabase)object).getDatabaseId());
 	}
 	
 	protected void initialize()
 	{
 		OTXMLElement rootElement = document.getRootElement();
 		
 		String dbCodeBase = rootElement.getAttributeValue("codebase");
 		String systemCodeBase = OTConfig.getStringProp(OTConfig.CODEBASE_PROP);
 		if(systemCodeBase != null){
 			dbCodeBase = systemCodeBase;
 		}
 		if(dbCodeBase != null && dbCodeBase.length() > 0) {
 		    // this document has a specific base address
 		    
 		    // make sure the address ends with a slash
 		    if(!dbCodeBase.endsWith("/")) {
 		        dbCodeBase += "/";
 		    }
 		    
 		    // add a pseudo file to the end so the URL class treats
 		    // this as a correct contextURL: it strips off the last part
 		    // of the context url.
 		    dbCodeBase += "pseudo.txt";
 		    try {
 		        contextURL = new URL(dbCodeBase);
 		    } catch (MalformedURLException e) {
 		        // the base url was not formed right
 		        e.printStackTrace();
 		    }
 		}
 		
 		String dbId = rootElement.getAttributeValue("id");
 		if(dbId != null && dbId.length() > 0) {
 		    databaseId = OTIDFactory.createOTID(dbId);
 		}       		
 	}
 	
 	public void loadObjects()
 		throws Exception
 	{
 		OTXMLElement rootElement = document.getRootElement();
 		
 		TypeService typeService = new TypeService(contextURL);
 		ObjectTypeHandler objectTypeHandler = new ObjectTypeHandler(typeService, this);
 		typeService.registerUserType("object", objectTypeHandler);
 
 		OTXMLElement importsElement = rootElement.getChild("imports");
 		if (importsElement == null) {
 			throw new RuntimeException("<imports> element is missing");
 		}
 		
 		List imports = importsElement.getChildren();		
 		for(Iterator iterator=imports.iterator();iterator.hasNext();) {
 		    OTXMLElement currentImport=(OTXMLElement)iterator.next();
 			String className = currentImport.getAttributeValue("class");
 			importedOTObjectClasses.add(className);
 
 			// TODO look for package classes based on thise imports and 
 			// save them.  The OTrunkImpl will then ask for these packages from the 
 			// the database when it loads it and then initialize the packages
 			
 			Class packageClass = findPackageClass(className);
 			if(packageClass != null && !packageClasses .contains(packageClass)){
 				packageClasses.add(packageClass);
 			}
 		}		
 		
 		
 		ReflectionTypeDefinitions.registerTypes(importedOTObjectClasses, typeService,
 				this);		
 		
 		// Add local_ids listed in the otml's idMap to the localIdMap
 		// This way the objects with a defined uuid can be referred to
 		// by a human-readable local_id within the otml file
 		OTXMLElement idMapElement = rootElement.getChild("idMap");
 		if (idMapElement != null){
 			List idMappings = idMapElement.getChildren();
 			for(Iterator it = idMappings.iterator(); it.hasNext();) {
 				OTXMLElement mapping = (OTXMLElement)it.next();
 				String idStr = mapping.getAttributeValue("id");
 				String localIdStr = mapping.getAttributeValue("local_id");
 				OTID otid = OTIDFactory.createOTID(idStr);
 				
 				Object oldId = localIdMap.put(localIdStr, otid);
     			if(oldId != null) {
     				System.err.println("repeated local id: " + localIdStr);
     			}
 		    }
 
 		}
 		
 		// Pass 1:
 		// Load all the xml data objects in the file
 		// This also makes a list of all these objects so we 
 		// can handle them linearly in the next pass.
 		OTXMLElement objects = rootElement.getChild("objects");
 		List xmlObjects = objects.getChildren();
 		if(xmlObjects.size() != 1) {
 			throw new Exception("Can only load files that contain a single root object");
 		}
 
 		OTXMLElement rootObjectNode = (OTXMLElement)xmlObjects.get(0);		
 		
 		// Recursively load all the data objects
 		
 		// If the database does not have an id then use a path of anon_root
 		// Due to an error before, anon_root was being used even when the database had an
 		// id.  However all of our legacy data came from files that used local_ids everywhere.
 		// so in that case the correct database id is used.
 		String relativePath = "anon_root";
 		if(databaseId != null) {
 		    relativePath = databaseId.toExternalForm() + "/";
 		}
 		XMLDataObject rootDataObject = (XMLDataObject)typeService.handleLiteralElement(rootObjectNode, relativePath);
 		
 		statusStream.println("Loaded objects in: " + label);
 		
 		// Need to handle local_id this will be stored as XMLDataObjectRef with in the
 		// tree. this is what the objectReferences vector is for
 		// each references stores the source object and the key within that object
 		// where the object should be stored.  
 		secondPass();
 		
 		setRoot(rootDataObject.getGlobalId());
 		
 	}
 
 	public static class PackageNotFound{};
 	
 	/**
 	 * This will take a imported class name and figure out the 
 	 * name of the OT package class.  If for example the className is
 	 * org.concord.datagraph.state.OTDataCollector
 	 * It looks for a class called:
 	 * org.concord.datagraph.state.OTDatagraphPackage
 	 * 
 	 * This is figured out by 
	 * Taking of the classname
 	 *    org.concord.datagraph.state
 	 * striping off the .state (if there is one)
 	 *    org.concord.datagraph
 	 * taking the last element of the package name
 	 *    datagraph
 	 * capitalizing the first leter and adding OT to the front and package to back
 	 *    OTDatagraphPackage
 	 * using the original package of the imported class
 	 *    org.concord.datagraph.state.OTDatagraphPackage
      * @param className
      * @return
      */
     private Class findPackageClass(String className)
     {
     	int lastDot = className.lastIndexOf('.');
     	String packageName = className.substring(0,lastDot);
     	
     	Class otPackageClass;
     	otPackageClass = (Class)processedOTPackages .get(packageName);
     	if(otPackageClass == PackageNotFound.class){
     		// we looked for this package before but couldn't find it
     		return null;
     	} else if(otPackageClass != null){
     		return otPackageClass;
     	}
 
     	String otPackageStr = packageName;
     	if(packageName.endsWith(".state")){
     		otPackageStr = 
     			packageName.substring(0,packageName.length() - ".state".length());    		
     	}
     	
     	String capitalizedOTPackageStr = null;
     	// Special case org.concord packages    	
     	if(otPackageStr.startsWith("org.concord.")){
     		otPackageStr = otPackageStr.substring("org.concord.".length());
     		
     		String newOTPackageStr = "";
     		int curIndex = 0;
     		while(curIndex < otPackageStr.length()){
     			int nextIndex = otPackageStr.indexOf('.', curIndex);
     			if(nextIndex == -1){
     				nextIndex = otPackageStr.length();
     			}
     			newOTPackageStr += otPackageStr.substring(curIndex,curIndex+1).toUpperCase() + 
     				otPackageStr.substring(curIndex+1, nextIndex);
     			curIndex = nextIndex + 1;
     		}
 
     		capitalizedOTPackageStr = newOTPackageStr;
     		
     	} else {    	
     		lastDot = otPackageStr.lastIndexOf('.');
     		otPackageStr = otPackageStr.substring(lastDot+1);
 
     		capitalizedOTPackageStr = otPackageStr.substring(0,1).toUpperCase() +
     			otPackageStr.substring(1);
     	}
     	
     	String otPackageClassName = 
     		"OT" + capitalizedOTPackageStr + "Package";
     	
     	String fullyQualifiedOTPackageClassName = 
     		packageName + "." + otPackageClassName;
     	
         try {
 	        otPackageClass = getClass().getClassLoader().loadClass(fullyQualifiedOTPackageClassName);
         	if(TRACE_PACKAGES){
         		System.err.println("loaded package: " + otPackageClass);
         	}
 	        processedOTPackages.put(packageName, otPackageClass);
 	    	return otPackageClass;
         } catch (ClassNotFoundException e) {
         	if(TRACE_PACKAGES){
         		System.err.println("no OTPackage for: " + packageName);
         		System.err.println("  the classname should be: " + 
         				fullyQualifiedOTPackageClassName);
         	}
         	// add to a list of notfound otpackages so we don't look for it again
 	        processedOTPackages.put(packageName, PackageNotFound.class);
         }
 
         return null;
     }
 
 	protected void printStatus(String message)
 	{
 	    if(statusStream != null) {
 	        statusStream.println(message);
 	    }
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.datamodel.OTDatabase#setRoot(org.doomdark.uuid.UUID)
 	 */
 	public void setRoot(OTID rootId)
 		throws Exception
 	{
 		this.rootId = rootId;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.datamodel.OTDatabase#getRoot()
 	 */
 	public OTDataObject getRoot()
 		throws Exception
 	{
 		if(rootId == null){
 			return null;
 		}
 		return (OTDataObject)dataObjects.get(rootId);
 	}
 
 	public OTID getDatabaseId()
 	{
 	    return databaseId;
 	}
 	
 	public Hashtable getDataObjects() {
 		return dataObjects;
 	}
 	
 	public boolean isDirty()
 	{
 	    return dirty;
 	}
 	
 	public void setDirty(boolean dirty)
 	{
 		boolean oldValue = this.dirty;
 	    this.dirty = dirty;
 	    
 	    if (oldValue != this.dirty){
 			notifyListeners();
 		}
 	}
 	
 	private void notifyListeners()
     {
 		if (listeners == null) return;
 		
 		XMLDatabaseChangeEvent changeEvent = new XMLDatabaseChangeEvent(this);
 		String status = dirty ? XMLDatabaseChangeEvent.STATE_DIRTY : XMLDatabaseChangeEvent.STATE_CLEAN;
 		changeEvent.setValue(status);
 		
 	    for (int i=0; i< listeners.size(); i++){
 	    	((XMLDatabaseChangeListener)listeners.get(i)).stateChanged(changeEvent);
 	    }
     }
 	
 	public void addXMLDatabaseChangeListener(XMLDatabaseChangeListener listener){
 		if(listener == null){
 			throw new IllegalArgumentException("listener cannot be null");
 		}
 		
 		if (listeners == null){
 			listeners = new ArrayList();
 		}
 		
 		listeners.add(listener);
 	}
 
 	protected XMLDataObject createDataObject(OTXMLElement element, String idStr)
 		throws Exception
 	{
 		OTID id = null;
 		if(idStr != null) {
 			id = OTIDFactory.createOTID(idStr);
 		}
 		return createDataObject(element, id); 
 	}
 		
 	protected XMLDataObject createDataObject(OTXMLElement element, OTID id)
 		throws Exception
 	{
 		if(id == null) {
 //		    String path = TypeService.elementPath(element);
 //		    id = new OTXMLPathID(path);
 			id = OTUUID.createOTUUID();
 		}
 
     	XMLDataObject dataObject = new XMLDataObject(element, id, this);
 
     	Object oldValue = dataObjects.put(dataObject.getGlobalId(), dataObject);
     	if(oldValue != null) {
     		dataObjects.put(dataObject.getGlobalId(), oldValue);
     		throw new Exception("repeated unique id: " + dataObject.getGlobalId().toExternalForm());
     	}
 
     	if(element != null) {
     		String localIdStr = element.getAttributeValue("local_id");
     		if(localIdStr != null && localIdStr.length() > 0) {
     			dataObject.setLocalId(localIdStr);
 
     			// this is probably a temporary hack
     			// we want to save local id so it can be shown
     			// to the author.  It is useful for debugging
     			dataObject.setResource("localId", localIdStr);
     			
     			Object oldId = localIdMap.put(localIdStr, dataObject.getGlobalId());
     			if(oldId != null) {
     				System.err.println("repeated local id: " + localIdStr);
     			}
     		}
     	}
     	return dataObject;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.datamodel.OTDatabase#createDataObject()
 	 */
 	public OTDataObject createDataObject(OTDataObjectType type)
 		throws Exception
 	{
 		XMLDataObject xmlDataObject = createDataObject((OTXMLElement)null, (OTID)null);
 		xmlDataObject.setType(type);
 		return xmlDataObject;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.datamodel.OTDatabase#createDataObject(org.doomdark.uuid.UUID)
 	 */
 	public OTDataObject createDataObject(OTDataObjectType type, OTID id)
 		throws Exception
 	{
 		XMLDataObject xmlDataObject = createDataObject((OTXMLElement)null, id);
 		xmlDataObject.setType(type);
 		return xmlDataObject;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.datamodel.OTDatabase#createCollection(org.concord.otrunk.datamodel.OTDataObject, java.lang.Class)
 	 */
 	public OTDataCollection createCollection(OTDataObject parent,
 			Class collectionClass)
 		throws Exception
 	{
 		if(collectionClass.equals(OTDataList.class)) {
 			return new XMLDataList((XMLDataObject)parent);
 		} else if(collectionClass.equals(OTDataMap.class)) {
 			return new XMLDataMap((XMLDataObject)parent);
 		}
 		
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.datamodel.OTDatabase#getOTDataObject(org.concord.otrunk.datamodel.OTDataObject, org.doomdark.uuid.UUID)
 	 */
 	public OTDataObject getOTDataObject(OTDataObject dataParent, OTID childID)
 		throws Exception
 	{
 		// we are going to ignore the dataParent for now
 		return (OTDataObject)dataObjects.get(childID);
 	}
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#contains(org.concord.framework.otrunk.OTID)
      */
     public boolean contains(OTID id)
     {
         return id.equals(databaseId) || 
         	dataObjects.containsKey(id);
     }
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.datamodel.OTDatabase#close()
 	 */
 	public void close()
 	{
 		// TODO Auto-generated method stub
 		
 		// resave the xml file maybe???
 	}
 
 	public void secondPass()
 		throws Exception
 	{	
 		Collection objects = dataObjects.values();
 		
 		for(Iterator iter = objects.iterator(); iter.hasNext();){
 			XMLDataObject xmlDObj = (XMLDataObject)iter.next();
 			if(xmlDObj instanceof XMLDataObjectRef) {
 				throw new Exception("Found a reference in object list");
 			}
 						
 			Collection entries = xmlDObj.getResourceEntries(); 
 			Vector removedKeys = new Vector();
 			
 			for(Iterator entriesIter = entries.iterator(); entriesIter.hasNext(); )  {
 				Map.Entry resourceEntry = (Map.Entry)entriesIter.next();
 				Object resourceValue = resourceEntry.getValue();
 				Object newResourceValue = null;
 				String resourceKey = (String)resourceEntry.getKey();
 				if(resourceValue instanceof XMLDataObject) {
 					XMLDataObject resourceValueObj = (XMLDataObject) resourceValue;
 					if(!(resourceValueObj instanceof XMLDataObjectRef)){
 						resourceValueObj.setContainer(xmlDObj);
 						resourceValueObj.setContainerResourceKey(resourceKey);
 					}
 					newResourceValue = getOTID(resourceValueObj);
 					if(newResourceValue == null) {
 					    removedKeys.add(resourceKey);
 					} else {
 					    xmlDObj.setResource(resourceKey, newResourceValue);
 					}
 				} else if(resourceValue instanceof XMLDataList) {
 					XMLDataList list = (XMLDataList)resourceValue;
 					for(int j=0; j<list.size(); j++) {
 						Object oldElement = list.get(j);
 						if(oldElement instanceof XMLDataObject) {
 							XMLDataObject oldElementObj = (XMLDataObject) oldElement;
 							if(!(oldElementObj instanceof XMLDataObjectRef)){
 								oldElementObj.setContainer(xmlDObj);
 								oldElementObj.setContainerResourceKey(resourceKey);
 							}
 
 							OTID newElement = getOTID((XMLDataObject)oldElement);
 							list.set(j, newElement);
 						}
 						if(oldElement instanceof XMLParsableString) {
 							newResourceValue = ((XMLParsableString)oldElement).parse(localIdMap);
 							list.set(j, newResourceValue);							
 						}
 					}
 					// the resource list value doesn't need to be updated
 				} else if(resourceValue instanceof XMLDataMap) {
 					XMLDataMap map = (XMLDataMap)resourceValue;
 					String [] keys = map.getKeys();
 					for(int j=0; j<keys.length; j++) {
 						Object oldElement = map.get(keys[j]);
 						
 						// Check if the key is a local id reference
 						// if it is then replace it with the string
 						// representation of this key
 						if(keys[j].startsWith("${")){
 							OTID globalId = getGlobalId(keys[j]);
 							if(globalId != null) {
 								map.remove(keys[j]);
 								keys[j] = globalId.toExternalForm();
 								map.put(keys[j], oldElement);
 							}
 						}
 
 						if(oldElement instanceof XMLDataObject) {
 							XMLDataObject oldElementObj = (XMLDataObject) oldElement;
 							if(!(oldElementObj instanceof XMLDataObjectRef)){
 								oldElementObj.setContainer(xmlDObj);
 								oldElementObj.setContainerResourceKey(resourceKey);
 							}
 
 							OTID newElement = getOTID((XMLDataObject)oldElement);
 							map.put(keys[j], newElement);
 						}
 						if(oldElement instanceof XMLParsableString) {
 							newResourceValue = ((XMLParsableString)oldElement).parse(localIdMap);
 							map.put(keys[j], newResourceValue);							
 						}
 					}
 				} else if(resourceValue instanceof XMLParsableString) {
 					// replace the local ids from the string
 					newResourceValue = ((XMLParsableString)resourceValue).parse(localIdMap);
 					xmlDObj.setResource(resourceKey, newResourceValue);
 				}				
 			}	
 			
 			// remove the keys that have null values
 			// this can't be done in the previous loop because that screws up the
 			// the Iterator
 			for(int keyIndex=0; keyIndex<removedKeys.size(); keyIndex++){
 			    xmlDObj.setResource((String)removedKeys.get(keyIndex), null);
 			}
 		}			
 	}
 
 	private OTID getGlobalId(String idStr)
 	{
 		if(idStr.startsWith("${")) {
 			String localId = idStr.substring(2,idStr.length()-1);
 			OTID globalId =  (OTID)localIdMap.get(localId);
 			if(globalId == null) {
 				System.err.println("Can't find local id: " + localId);
 			}
 			return globalId;
 		} else {
 			return OTIDFactory.createOTID(idStr); 
 		}		
 	}
 	
 	private OTID getOTID(XMLDataObject xmlDObj)
 	{
 		if(xmlDObj instanceof XMLDataObjectRef) {
 			String refId = ((XMLDataObjectRef)xmlDObj).getRefId();
 			return getGlobalId(refId);
 		}
 		return xmlDObj.getGlobalId();		
 	}	
 	
 	public Hashtable getLocalIDMap() {
 		return localIdMap;
 	}
 	
     /**
      * @param localIdStr
      * @return
      */
     public OTID getOTIDFromLocalID(String localIdStr)
     {
         // if the db has a id use that plus
         // the local id to create a relative ID and change
         // the contains method to include that database id
         OTID dbId = getDatabaseId();
         if(dbId != null) {
             return new OTRelativeID(dbId, new OTPathID("/" + localIdStr));
         }
         
         // FIXME 
         // if the databse doesn't have a id then we use some 
         // standard anon relative id (I don't know if that will
         // work) otherwise we could hash something into an id
         return new OTRelativeID(null, new OTPathID("/" + localIdStr));        
     }
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#createBlobResource(java.net.URL)
      */
     public BlobResource createBlobResource(URL url)
     {
     	return new BlobResource(url);
     }
 	
     /* (non-Javadoc)
      * @see org.concord.otrunk.datamodel.OTDatabase#getPackageClasses()
      */
     public Vector getPackageClasses()
     {
     	return (Vector)packageClasses.clone();
     }
 
 	public boolean isTrackResourceInfo()
     {
     	return trackResourceInfo;
     }
 
 	public void setTrackResourceInfo(boolean trackResourceInfo)
     {
     	this.trackResourceInfo = trackResourceInfo;
     }
 
 	public ArrayList getImportedOTObjectClasses()
     {
     	return importedOTObjectClasses;
     }
 
 	public URL getContextURL()
     {
 		return contextURL;
     }
 }
