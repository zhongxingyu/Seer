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
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTPackage;
 import org.concord.framework.util.IResourceLoader;
 import org.concord.framework.util.IResourceLoaderFactory;
 import org.concord.otrunk.datamodel.BlobResource;
 import org.concord.otrunk.datamodel.OTDataCollection;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTDataPropertyReference;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTPathID;
 import org.concord.otrunk.datamodel.OTRelativeID;
 import org.concord.otrunk.datamodel.OTTransientMapID;
 import org.concord.otrunk.datamodel.OTUUID;
 import org.concord.otrunk.net.IndentingPrintWriter;
 import org.concord.otrunk.transfer.Transfer;
 import org.concord.otrunk.view.OTConfig;
 import org.concord.otrunk.xml.jdom.JDOMDocument;
 
 /**
  * XMLDatabase Class name and description
  * 
  * Date created: Nov 19, 2004
  * 
  * @author scott
  *         <p>
  * 
  */
 public class XMLDatabase
     implements OTDatabase
 {
 	private static final Logger logger = Logger.getLogger(XMLDatabase.class.getName());
 	public static boolean TRACE_PACKAGES =
 	    OTConfig.getBooleanProp(OTConfig.TRACE_PACKAGES_PROP, false);
 
 	OTID rootId = null;
 
 	ArrayList<String> importedOTObjectClasses = new ArrayList<String>();
 
 	HashMap<OTID, XMLDataObject> dataObjects = new HashMap<OTID, XMLDataObject>();
 
 	// a map of xml file ids to UUIDs
 	HashMap<String, OTID> localIdMap = new HashMap<String, OTID>();
 
 	// track whether any object in this database
 	// has changed
 	boolean dirty = false;
 
 	private OTID databaseId;
 
 	private ArrayList<Class<? extends OTPackage>> packageClasses = 
 		new ArrayList<Class<? extends OTPackage>>();
 
 	private HashMap<String, Class<? extends OTPackage>> processedOTPackages = 
 		new HashMap<String, Class<? extends OTPackage>>();
 
 	private boolean trackResourceInfo = false;
 
 	private JDOMDocument document;
 
 	private URL contextURL;
 	
 	private URL sourceURL = null;
 
 	private ArrayList<XMLDatabaseChangeListener> listeners;
 
 	String label;
 
 	private HashMap<OTID, ArrayList<OTDataPropertyReference>> incomingReferences = 
 		new HashMap<OTID, ArrayList<OTDataPropertyReference>>();
 	
 	private HashMap<OTID, ArrayList<OTDataPropertyReference>> outgoingReferences = 
 		new HashMap<OTID, ArrayList<OTDataPropertyReference>>();
 	
 	private long urlOpenTime;
 	private long downloadTime = -1;
 	private long parseTime;
 
 	private boolean sourceVerified = false;
 	
 	private long urlLastModifiedTime = -1;
 	private int urlContentLength = -1;
 
 	private IResourceLoader resourceLoader;
 	
 	private static IResourceLoaderFactory resourceLoaderFactory =
 		new org.concord.otrunk.net.OTrunkResourceLoaderFactory();
 	
 	protected static String getLabel(URL contextURL)
 	{
 		if (contextURL == null) {
 			return "unknown_name";
 		}
 		return contextURL.toExternalForm();
 	}
 
 	public XMLDatabase()
 	{
 		// create an empty database with no root
 	}
 
 	public XMLDatabase(File xmlFile) throws Exception
 	{
 		this(new FileInputStream(xmlFile), xmlFile.toURL(), null);
 	}
 
 	public XMLDatabase(URL xmlURL) throws Exception
 	{
 		this(xmlURL, System.out);
 	}
 
 	public XMLDatabase(URL xmlURL, PrintStream statusStream) throws Exception
 	{
 		this(xmlURL, false, statusStream);
 	}
 	
 	/**
 	 * This will print information about the url if there is an exception loading it.  This should
 	 * be refactored because in some cases this method might be used to see if the URL exists.
 	 * So it will throw a 404 and that shouldn't be printed. 
 	 * 
 	 * @param xmlURL
 	 * @param required If implemented correctly required might prompt the user to retry the url
 	 *    and if that fails it will throw an Error with the intention of stopping the application.
 	 * @param statusStream
 	 * @throws Exception
 	 */
 	public XMLDatabase(URL xmlURL, boolean promptRetryQuit, PrintStream statusStream) throws Exception
 	{
 		initialize(xmlURL, xmlURL.toExternalForm(), statusStream);
 		long openingStart = System.currentTimeMillis();
 		InputStream urlInStream = null;
 		resourceLoader = resourceLoaderFactory.getResourceLoader(xmlURL, promptRetryQuit);
 		try {
 			urlInStream = resourceLoader.getInputStream();
 		} catch (Exception e) {
 			printErrorDetails();
 			throw e;
 		}
 		urlLastModifiedTime = resourceLoader.getLastModified();
 		urlContentLength = resourceLoader.getContentLength();
 
 		urlOpenTime = System.currentTimeMillis() - openingStart;
 		
 		// parse the xml file...
 		long startMillis = -1;
 		JDOMDocument xmlDocument = null;
 		InputStream inputStream = urlInStream;
 		if(OTConfig.getBooleanProp(OTConfig.TRACE_DB_LOAD_TIME, false)){				
 			long transferStart = System.currentTimeMillis();
 			Transfer transfer = new Transfer();
 			ByteArrayOutputStream byteCache = new ByteArrayOutputStream();
 			transfer.transfer(urlInStream, byteCache);
 			inputStream = new ByteArrayInputStream(byteCache.toByteArray());
 			downloadTime = System.currentTimeMillis() - transferStart;
 		}
 		startMillis = System.currentTimeMillis();
 		try {
 			xmlDocument = new JDOMDocument(inputStream);
 		} catch (Exception e){
 			printErrorDetails();
 			throw e;
 		}
 		parseTime = System.currentTimeMillis() - startMillis;
 
 		initializeDoc(xmlDocument);
 	}
 
 	public void printErrorDetails()
     {
 	    IndentingPrintWriter writer = new IndentingPrintWriter(System.err);
 	    writer.printFirstln("Error Loading XMLDatabase: ");
 	    resourceLoader.writeResourceErrorDetails(writer, true);
 	    writer.flush();
     }
 	
 	public XMLDatabase(InputStream xmlStream, URL contextURL, PrintStream statusStream)
 	        throws Exception
 	{
 		// The "" + approach allows us to handle nulls.
 		this(xmlStream, contextURL, getLabel(contextURL), statusStream);
 	}
 
 	public XMLDatabase(InputStream xmlStream, URL contextURL, String label,
 	        PrintStream statusStream) throws Exception
 	{
 		initialize(contextURL, label, statusStream);
 		
 		// printStatus("Opening otml: " + label);
 
 		// parse the xml file...
 		long startMillis = -1;
 		JDOMDocument xmlDocument = null;
 		try {
 			InputStream inputStream = xmlStream;
 			if(OTConfig.getBooleanProp(OTConfig.TRACE_DB_LOAD_TIME, false)){				
 				long transferStart = System.currentTimeMillis();
 				Transfer transfer = new Transfer();
 				ByteArrayOutputStream byteCache = new ByteArrayOutputStream();
 				transfer.transfer(xmlStream, byteCache);
 				inputStream = new ByteArrayInputStream(byteCache.toByteArray());
 				downloadTime = System.currentTimeMillis() - transferStart;
 			}
 			startMillis = System.currentTimeMillis();
 			xmlDocument = new JDOMDocument(inputStream);
 		} catch (Exception e) {
 
 			if (contextURL != null) {
 				URLConnection connection = contextURL.openConnection();
 				if (connection instanceof HttpURLConnection) {
 					logger.severe("Error created xml document");
 					logger.severe("Response code for " + contextURL + ":");
 					logger.severe("   "
 					        + ((HttpURLConnection) connection).getResponseCode());
 				}
 				logger.severe("Length of xmlstream from " + contextURL + ":");
 				logger.severe("   " + connection.getInputStream().available());
 			}
 
 			throw e;
 		}
 		parseTime = System.currentTimeMillis() - startMillis;
 
 		initializeDoc(xmlDocument);
 	}
 
 	public XMLDatabase(Reader xmlReader, URL contextURL, PrintStream statusStream)
 	        throws Exception
 	{
 		this(xmlReader, contextURL, getLabel(contextURL), statusStream);
 	}
 
 	public XMLDatabase(Reader xmlReader, URL contextURL, String label,
 	        PrintStream statusStream) throws Exception
 	{
 		initialize(contextURL, label, statusStream);
 		// printStatus("Opening otml: " + label);
 		long startMillis = System.currentTimeMillis();		
 		JDOMDocument xmlDocument = new JDOMDocument(xmlReader);
 		parseTime = System.currentTimeMillis() - startMillis;
 		
 		initializeDoc(xmlDocument);
 
 	}
 
 	@Override
     public boolean equals(Object object)
 	{
 		if (object == this) {
 			return true;
 		}
 
 		if (!(object instanceof XMLDatabase)) {
 			return false;
 		}
 
 		OTID id = getDatabaseId();
 		if (id == null) {
 			return false;
 		}
 
 		return id.equals(((XMLDatabase) object).getDatabaseId());
 	}
 
 	/**
 	 * @deprecated use initialize without the statusStream
 	 * @param contextURL
 	 * @param label
 	 * @param statusStream
 	 */
 	protected void initialize(URL contextURL, String label, PrintStream statusStream)
 	{
 		initialize(contextURL, label);
 	}
 	
 	protected void initialize(URL contextURL, String label)
 	{
 		this.label = label;
 		
 		// create the database Id
 		// this might get overriden when the objects are loaded in.
 		databaseId = OTUUID.createOTUUID();
 
 		// The order here matters because initialize will look at the codebase attribute of the otrunk
 		// element, and set the contextURL to be that.  That codebase attribute should override 
 		// the passed in contextURL
 		this.contextURL = contextURL;
 		setSourceURL(contextURL);
 	}
 	
 	protected void initializeDoc(JDOMDocument document)
 	{
 		this.document = document;
 		
 		OTXMLElement rootElement = document.getRootElement();
 
 		String dbCodeBase = rootElement.getAttributeValue("codebase");
 		
 		// the system property overrides what is in the xml document
 		String systemCodeBase = OTConfig.getStringProp(OTConfig.CODEBASE_PROP);
 		if (systemCodeBase != null) {
 			dbCodeBase = systemCodeBase;
 		}
 		
 		if (dbCodeBase != null && dbCodeBase.length() > 0) {
 			// this document has a specific base address
 
 			// make sure the address ends with a slash
 			if (!dbCodeBase.endsWith("/")) {
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
 		if (dbId != null && dbId.length() > 0) {
 			databaseId = OTIDFactory.createOTID(dbId);
 		}
 		
 		// Everything is loaded, so we know the sourceURL was valid
 		setSourceVerified(true);
 	}
 
 	public void loadObjects()
 	    throws Exception
 	{
 		long startMillis = System.currentTimeMillis();
 
 		OTXMLElement rootElement = document.getRootElement();
 
 		TypeService typeService = new TypeService(contextURL);
 		ObjectTypeHandler objectTypeHandler = new ObjectTypeHandler(typeService, this);
 		typeService.registerUserType("object", objectTypeHandler);
 
 		OTXMLElement importsElement = rootElement.getChild("imports");
 		if (importsElement == null) {
 			throw new RuntimeException("<imports> element is missing");
 		}
 
 		List<?> imports = importsElement.getChildren();
 		for (Iterator<?> iterator = imports.iterator(); iterator.hasNext();) {
 			OTXMLElement currentImport = (OTXMLElement) iterator.next();
 			String className = currentImport.getAttributeValue("class");
 			importedOTObjectClasses.add(className);
 
 			// TODO look for package classes based on thise imports and
 			// save them.  The OTrunkImpl will then ask for these packages from the 
 			// the database when it loads it and then initialize the packages
 
 			Class<? extends OTPackage> packageClass = findPackageClass(className);
 			if (packageClass != null && !packageClasses.contains(packageClass)) {
 				packageClasses.add(packageClass);
 			}
 		}
 
 		ReflectionTypeDefinitions.registerTypes(importedOTObjectClasses, typeService,
 		    this);
 
 		// Add local_ids listed in the otml's idMap to the localIdMap
 		// This way the objects with a defined uuid can be referred to
 		// by a human-readable local_id within the otml file
 		OTXMLElement idMapElement = rootElement.getChild("idMap");
 		if (idMapElement != null) {
 			List<?> idMappings = idMapElement.getChildren();
 			for (Iterator<?> it = idMappings.iterator(); it.hasNext();) {
 				OTXMLElement mapping = (OTXMLElement) it.next();
 				String idStr = mapping.getAttributeValue("id");
 				String localIdStr = mapping.getAttributeValue("local_id");
 				OTID otid = OTIDFactory.createOTID(idStr);
 
 				Object oldId = localIdMap.put(localIdStr, otid);
 				if (oldId != null) {
 					logger.warning("repeated local id: " + localIdStr);
 				}
 			}
 
 		}
 
 		// Pass 1:
 		// Load all the xml data objects in the file
 		// This also makes a list of all these objects so we
 		// can handle them linearly in the next pass.
 		OTXMLElement objects = rootElement.getChild("objects");
 		List<?> xmlObjects = objects.getChildren();
 		if (xmlObjects.size() != 1) {
 			throw new Exception("Can only load files that contain a single root object");
 		}
 
 		OTXMLElement rootObjectNode = (OTXMLElement) xmlObjects.get(0);
 
 		// Recursively load all the data objects
 
 		// If the database does not have an id then use a path of anon_root
 		// Due to an error before, anon_root was being used even when the database had an
 		// id.  However all of our legacy data came from files that used local_ids everywhere.
 		// so in that case the correct database id is used.
 		String relativePath = "anon_root";
 		if (databaseId != null) {
 			relativePath = databaseId.toExternalForm() + "/";
 		}
 		XMLDataObject rootDataObject =
 		    (XMLDataObject) typeService.handleLiteralElement(rootObjectNode, relativePath, null, null);
 
 		// Need to handle local_id this will be stored as XMLDataObjectRef with in the
 		// tree. this is what the objectReferences vector is for
 		// each references stores the source object and the key within that object
 		// where the object should be stored.
 		secondPass();
 
 		setRoot(rootDataObject.getGlobalId());
 
 		long endMillis = System.currentTimeMillis();
 
 		String parsedLabel = "downloaded and parsed xml";
 		String downloadString = "";
 		if(downloadTime >= 0){
 			downloadString = " downloaded in " + downloadTime + "ms";
 			parsedLabel = "parsed xml";
 		}
 		String sizeString = " (unknown size)";
 		if (urlContentLength > -1) {
 			sizeString = " (" + urlContentLength + " bytes)";
 		}
 		logger.info("Loaded " + dataObjects.size() + " objects from: " + label
 	            + sizeString + " opened url in " + urlOpenTime + "ms"
 		        + downloadString 
 				+ " " + parsedLabel + " in " + parseTime + "ms" 
 				+ " loaded ot db in " + (endMillis - startMillis) + "ms" );
 	}
 
 	public static abstract class PackageNotFound implements OTPackage
 	{
 	};
 
 	/**
 	 * This will take a imported class name and figure out the name of the OT
 	 * package class. If for example the className is
 	 * org.concord.datagraph.state.OTDataCollector It looks for a class called:
 	 * org.concord.datagraph.state.OTDatagraphPackage
 	 * 
 	 * This is figured out by Taking off the classname
 	 * org.concord.datagraph.state striping off the .state (if there is one)
 	 * org.concord.datagraph taking the last element of the package name
 	 * datagraph capitalizing the first leter and adding OT to the front and
 	 * package to back OTDatagraphPackage using the original package of the
 	 * imported class org.concord.datagraph.state.OTDatagraphPackage
 	 * 
 	 * @param className
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
     private Class<? extends OTPackage> findPackageClass(String className)
 	{
 		int lastDot = className.lastIndexOf('.');
 		String packageName = className.substring(0, lastDot);
 
 		Class<? extends OTPackage> otPackageClass;
 		otPackageClass = processedOTPackages.get(packageName);
 		if (otPackageClass == PackageNotFound.class) {
 			// we looked for this package before but couldn't find it
 			return null;
 		} else if (otPackageClass != null) {
 			return otPackageClass;
 		}
 
 		String otPackageStr = packageName;
 		if (packageName.endsWith(".state")) {
 			otPackageStr =
 			    packageName.substring(0, packageName.length() - ".state".length());
 		}
 
 		String capitalizedOTPackageStr = null;
 		// Special case org.concord packages
 		if (otPackageStr.startsWith("org.concord.")) {
 			otPackageStr = otPackageStr.substring("org.concord.".length());
 
 			String newOTPackageStr = "";
 			int curIndex = 0;
 			while (curIndex < otPackageStr.length()) {
 				int nextIndex = otPackageStr.indexOf('.', curIndex);
 				if (nextIndex == -1) {
 					nextIndex = otPackageStr.length();
 				}
 				newOTPackageStr +=
 				    otPackageStr.substring(curIndex, curIndex + 1).toUpperCase()
 				            + otPackageStr.substring(curIndex + 1, nextIndex);
 				curIndex = nextIndex + 1;
 			}
 
 			capitalizedOTPackageStr = newOTPackageStr;
 
 		} else {
 			lastDot = otPackageStr.lastIndexOf('.');
 			otPackageStr = otPackageStr.substring(lastDot + 1);
 
 			capitalizedOTPackageStr =
 			    otPackageStr.substring(0, 1).toUpperCase() + otPackageStr.substring(1);
 		}
 
 		String otPackageClassName = "OT" + capitalizedOTPackageStr + "Package";
 
 		String fullyQualifiedOTPackageClassName = packageName + "." + otPackageClassName;
 
 		try {
 			otPackageClass = (Class<? extends OTPackage>) 
 			    getClass().getClassLoader().loadClass(fullyQualifiedOTPackageClassName);
 			if (TRACE_PACKAGES) {
 				logger.info("loaded package: " + otPackageClass);
 			}
 			processedOTPackages.put(packageName, otPackageClass);
 			return otPackageClass;
 		} catch (ClassNotFoundException e) {
 			if (TRACE_PACKAGES) {
 				logger.info("no OTPackage for: " + packageName);
 				logger.info("  the classname should be: "
 				        + fullyQualifiedOTPackageClassName);
 			}
 			// add to a list of notfound otpackages so we don't look for it
 			// again
 			processedOTPackages.put(packageName, PackageNotFound.class);
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.concord.otrunk.datamodel.OTDatabase#setRoot(org.doomdark.uuid.UUID)
 	 */
 	public void setRoot(OTID rootId)
 	    throws Exception
 	{
 		this.rootId = rootId;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.otrunk.datamodel.OTDatabase#getRoot()
 	 */
 	public OTDataObject getRoot()
 	    throws Exception
 	{
 		if (rootId == null) {
 			return null;
 		}
 		return dataObjects.get(rootId);
 	}
 
 	public OTID getDatabaseId()
 	{
 		return databaseId;
 	}
 
 	public HashMap<OTID, XMLDataObject> getDataObjects()
 	{
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
 
 		if (oldValue != this.dirty) {
 			notifyListeners();
 		}
 	}
 
 	private void notifyListeners()
 	{
 		if (listeners == null)
 			return;
 
 		XMLDatabaseChangeEvent changeEvent = new XMLDatabaseChangeEvent(this);
 		String status =
 		    dirty ? XMLDatabaseChangeEvent.STATE_DIRTY
 		            : XMLDatabaseChangeEvent.STATE_CLEAN;
 		changeEvent.setValue(status);
 
 		for(XMLDatabaseChangeListener listener: listeners){
 			listener.stateChanged(changeEvent);
 		}
 	}
 
 	public void addXMLDatabaseChangeListener(XMLDatabaseChangeListener listener)
 	{
 		if (listener == null) {
 			throw new IllegalArgumentException("listener cannot be null");
 		}
 
 		if (listeners == null) {
 			listeners = new ArrayList<XMLDatabaseChangeListener>();
 		}
 
 		listeners.add(listener);
 	}
 
 	protected XMLDataObject createDataObject(OTXMLElement element, String idStr)
 	    throws Exception
 	{
 		OTID id = null;
 		if (idStr != null) {
 			id = OTIDFactory.createOTID(idStr);
 		}
 		return createDataObject(element, id);
 	}
 
 	protected XMLDataObject createDataObject(OTXMLElement element, OTID id)
 	    throws Exception
 	{
 		logger.finest("Creating data object for: " + (id == null ? "null" : id.toExternalForm()) );
 		if (id == null) {
 			// String path = TypeService.elementPath(element);
 			// id = new OTXMLPathID(path);
 			id = OTUUID.createOTUUID();
 		}
 
 		XMLDataObject dataObject = new XMLDataObject(element, id, this);
 
 		XMLDataObject oldValue = dataObjects.put(dataObject.getGlobalId(), dataObject);
 		if (oldValue != null) {
 			dataObjects.put(dataObject.getGlobalId(), oldValue);
 			throw new Exception("repeated unique id: "
 			        + dataObject.getGlobalId().toExternalForm());
 		}
 
 		if (element != null) {
 			String localIdStr = element.getAttributeValue("local_id");
 			if (localIdStr != null && localIdStr.length() > 0) {
 				dataObject.setLocalId(localIdStr);
 
 				// this is probably a temporary hack
 				// we want to save local id so it can be shown
 				// to the author. It is useful for debugging
 				dataObject.setResource("localId", localIdStr);
 
 				Object oldId = localIdMap.put(localIdStr, dataObject.getGlobalId());
 				if (oldId != null) {
 					logger.warning("repeated local id: " + localIdStr);
 				}
 			}
 		}
 		return dataObject;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.otrunk.datamodel.OTDatabase#createDataObject()
 	 */
 	public OTDataObject createDataObject(OTDataObjectType type)
 	    throws Exception
 	{
 		XMLDataObject xmlDataObject = createDataObject((OTXMLElement) null, (OTID) null);
 		xmlDataObject.setType(type);
 		return xmlDataObject;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.concord.otrunk.datamodel.OTDatabase#createDataObject(org.doomdark
 	 * .uuid.UUID)
 	 */
 	public OTDataObject createDataObject(OTDataObjectType type, OTID id)
 	    throws Exception
 	{
 		XMLDataObject xmlDataObject = createDataObject((OTXMLElement) null, id);
 		xmlDataObject.setType(type);
 		return xmlDataObject;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.concord.otrunk.datamodel.OTDatabase#createCollection(org.concord.
 	 * otrunk.datamodel.OTDataObject, java.lang.Class)
 	 */
 	public OTDataCollection createCollection(OTDataObject parent, Class<?> collectionClass)
 	    throws Exception
 	{
 		if (collectionClass.equals(OTDataList.class)) {
 			return new XMLDataList((XMLDataObject) parent);
 		} else if (collectionClass.equals(OTDataMap.class)) {
 			return new XMLDataMap((XMLDataObject) parent);
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.concord.otrunk.datamodel.OTDatabase#getOTDataObject(org.concord.otrunk
 	 * .datamodel.OTDataObject, org.doomdark.uuid.UUID)
 	 */
 	public OTDataObject getOTDataObject(OTDataObject dataParent, OTID childID)
 	    throws Exception
 	{
 		// we are going to ignore the dataParent for now
 		return dataObjects.get(childID);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.concord.otrunk.datamodel.OTDatabase#contains(org.concord.framework
 	 * .otrunk.OTID)
 	 */
 	public boolean contains(OTID id)
 	{
 		return id.equals(databaseId) || dataObjects.containsKey(id);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
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
 		Collection<XMLDataObject> objects = dataObjects.values();
 
 		for (Iterator<XMLDataObject> iter = objects.iterator(); iter.hasNext();) {
 			XMLDataObject xmlDObj = iter.next();
 			if (xmlDObj instanceof XMLDataObjectRef) {
 				throw new Exception("Found a reference in object list");
 			}
 
 			Collection<Entry<String, Object>> entries = xmlDObj.getResourceEntries();
 			ArrayList<String> removedKeys = new ArrayList<String>();
 
 			for(Entry<String, Object> resourceEntry: entries){
 				Object resourceValue = resourceEntry.getValue();
 				Object newResourceValue = null;
 				String resourceKey = resourceEntry.getKey();
 				logger.finest("Processing key: " + resourceKey + ", with value: " + resourceValue);
 				if (resourceValue instanceof XMLDataObject) {
 					XMLDataObject resourceValueObj = (XMLDataObject) resourceValue;
 					newResourceValue = getOTID(resourceValueObj);
 					if (newResourceValue == null) {
 						removedKeys.add(resourceKey);
 					} else {					
 						xmlDObj.setResource(resourceKey, newResourceValue, false);
 						recordSecondPassReference(resourceValueObj);
 					}					
 				} else if (resourceValue instanceof XMLDataList) {
 					XMLDataList list = (XMLDataList) resourceValue;
 					for (int j = 0; j < list.size(); j++) {
 						Object oldElement = list.get(j);
 						if (oldElement instanceof XMLDataObject) {
 							XMLDataObject oldElementObj = (XMLDataObject) oldElement;
 							OTID newElement = getOTID((XMLDataObject) oldElement);
 							list.set(j, newElement);
 							recordSecondPassReference(oldElementObj);
 						}
 						if (oldElement instanceof XMLParsableString) {
 							newResourceValue =
 							    ((XMLParsableString) oldElement).parse(localIdMap);
 							list.set(j, newResourceValue);
 						}
 					}
 					// the resource list value doesn't need to be updated
 				} else if (resourceValue instanceof XMLDataMap) {
 					XMLDataMap map = (XMLDataMap) resourceValue;
 					String[] keys = map.getKeys();
 					for (int j = 0; j < keys.length; j++) {
 						Object oldElement = map.get(keys[j]);
 
 						// Check if the key is a local id reference
 						// if it is then replace it with the string
 						// representation of this key
 						if (keys[j].startsWith("${")) {
 							OTID globalId = getGlobalId(keys[j]);
 							if (globalId != null) {
 								map.remove(keys[j]);
 								keys[j] = globalId.toExternalForm();
 								map.put(keys[j], oldElement);
 							}
 						}
 
 						if (oldElement instanceof XMLDataObject) {
 							XMLDataObject oldElementObj = (XMLDataObject) oldElement;
 							OTID newElement = getOTID((XMLDataObject) oldElement);
 							map.put(keys[j], newElement);
 							
 							// update the references map
 							recordSecondPassReference(oldElementObj);							
 						}
 						
 						if (oldElement instanceof XMLParsableString) {
 							newResourceValue =
 							    ((XMLParsableString) oldElement).parse(localIdMap);
 							map.put(keys[j], newResourceValue);
 						}
 					}
 				} else if (resourceValue instanceof XMLParsableString) {
 					// replace the local ids from the string
 					newResourceValue =
 					    ((XMLParsableString) resourceValue).parse(localIdMap);
 					xmlDObj.setResource(resourceKey, newResourceValue);
 				} else {
					logger.finest("Not valid object type: " + resourceValue.getClass().getName());
 				}
 			}
 
 			// remove the keys that have null values
 			// this can't be done in the previous loop because that screws up
 			// the
 			// the Iterator
 			for (int keyIndex = 0; keyIndex < removedKeys.size(); keyIndex++) {
 				xmlDObj.setResource(removedKeys.get(keyIndex), null);
 			}
 		}
 	}
 
 	private void recordSecondPassReference(XMLDataObject dataObject)
     {
 	    if(dataObject instanceof XMLDataObjectRef){
 	    	OTID otid = getOTID(dataObject);
 	    	XMLDataObjectRef ref = (XMLDataObjectRef) dataObject;
 	    	if(ref.parent == null){
 	    		// scytacki: I'm not sure this will ever happen
 	    		logger.finest("Parent was null (object): " + otid);								
 	    	} else {
 	    		recordReference(ref.parent.getGlobalId(), otid, ref.property);
 	    	}
 	    }
     }
 
 	private OTID getGlobalId(String idStr)
 	{
 		if (idStr.startsWith("${")) {
 			if(!idStr.endsWith("}")){
 				logger.warning("local id reference must end with }: " + idStr.substring(2,idStr.length()));
 				return null;
 			}
 			String localId = idStr.substring(2, idStr.length() - 1);
 			OTID globalId = localIdMap.get(localId);
 			if (globalId == null) {
 				logger.warning("Can't find local id: " + localId);
 			}
 			return globalId;
 		} else {
 			return OTIDFactory.createOTID(idStr);
 		}
 	}
 
 	private OTID getOTID(XMLDataObject xmlDObj)
 	{
 		if (xmlDObj instanceof XMLDataObjectRef) {
 			String refId = ((XMLDataObjectRef) xmlDObj).getRefId();
 			return getGlobalId(refId);
 		}
 		return xmlDObj.getGlobalId();
 	}
 
 	public HashMap<String, OTID> getLocalIDMap()
 	{
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
 		if (dbId != null) {
 			return new OTRelativeID(dbId, new OTPathID("/" + localIdStr));
 		}
 
 		// FIXME
 		// if the databse doesn't have a id then we use some
 		// standard anon relative id (I don't know if that will
 		// work) otherwise we could hash something into an id
 		return new OTRelativeID(null, new OTPathID("/" + localIdStr));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.concord.otrunk.datamodel.OTDatabase#createBlobResource(java.net.URL)
 	 */
 	public BlobResource createBlobResource(URL url)
 	{
 		return new BlobResource(url);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.otrunk.datamodel.OTDatabase#getPackageClasses()
 	 */
 	@SuppressWarnings("unchecked")
     public ArrayList<Class<? extends OTPackage>> getPackageClasses()
 	{
 		return (ArrayList<Class<? extends OTPackage>>) packageClasses.clone();
 	}
 
 	public boolean isTrackResourceInfo()
 	{
 		return trackResourceInfo;
 	}
 
 	public void setTrackResourceInfo(boolean trackResourceInfo)
 	{
 		this.trackResourceInfo = trackResourceInfo;
 	}
 
 	public ArrayList<String> getImportedOTObjectClasses()
 	{
 		return importedOTObjectClasses;
 	}
 
 	public URL getContextURL()
 	{
 		return contextURL;
 	}
 
 	public void recordReference(OTDataObject parent, OTDataObject child, String property)
 	{
 		if (parent == null || child == null) {
 			// can't reference "null"
 			return;
 		}
 
 		if (child instanceof XMLDataObjectRef) {
 			// this will be handled in the second pass
 			return;
 		}
 		
 		OTID parentID = parent.getGlobalId();
 		OTID childID = child.getGlobalId();
 
 		recordReference(parentID, childID, property);
 	}
 	
 	public void recordReference(OTID parentID, OTID childID, String property) {
 		logger.finer("Recording reference: " + parentID + " (" + property + ") --> " + childID);
 		
 		OTDataPropertyReference ref = new OTDataPropertyReference(parentID, childID, property);
 		
 		ArrayList<OTDataPropertyReference> parents = incomingReferences.get(childID);
 		ArrayList<OTDataPropertyReference> children = outgoingReferences.get(parentID);
 		if (parents == null) {
 			parents = new ArrayList<OTDataPropertyReference>();
 		}
 		if (children == null) {
 			children = new ArrayList<OTDataPropertyReference>();
 		}
 		if (! parents.contains(ref)) {
 			parents.add(ref);
 			incomingReferences.put(childID, parents);
 		}
 		
 		if (! children.contains(ref)) {
 			children.add(ref);
 			outgoingReferences.put(parentID, children);
 		}
 	}
 	
 	public void removeReference(OTDataObject parent, OTDataObject child) {
 		if (parent == null || child == null) {
 			// can't reference null
 			return;
 		}
 		
 		OTID parentID = parent.getGlobalId();
 		OTID childID = child.getGlobalId();
 
 		removeReference(parentID, childID);
 	}
 	
 	public void removeReference(OTID parentID, OTID childID) {
 		logger.finest("Removing reference: " + parentID + " --> " + childID);	
 		
 		ArrayList<OTDataPropertyReference> parents = incomingReferences.get(childID);
 		ArrayList<OTDataPropertyReference> children = outgoingReferences.get(parentID);
 
 		if (parents != null) {
 			OTDataPropertyReference refToRemove = null;
 			for (OTDataPropertyReference ref : parents) {
 				if (ref.getSource().equals(parentID) && ref.getDest().equals(childID)) {
 					refToRemove = ref;
 					break;
 				}
 			}
 			
 			if (refToRemove != null) {
 				parents.remove(refToRemove);
 				incomingReferences.put(childID, parents);
 			}
 		}
 		
 		if (children != null) {
 			OTDataPropertyReference refToRemove = null;
 			for (OTDataPropertyReference ref : children) {
 				if (ref.getSource().equals(parentID) && ref.getDest().equals(childID)) {
 					refToRemove = ref;
 					break;
 				}
 			}
 			
 			if (refToRemove != null) {
 				children.remove(refToRemove);
 				outgoingReferences.put(parentID, children);
 			}
 		}
 	}
 	
 	/**
 	 * This returns the URL which points to the location that this db came from or should be saved to.
 	 * It is similar to the contextURL, except that this won't be changed if the source resets the codebase.
 	 * 
 	 * @return URL The URL from which this db was loaded, or to which this db has/will be persisted
 	 */
 	public URL getSourceURL() {
 		return this.sourceURL;
 	}
 	
 	/**
 	 * Set a new location for this db to be persisted. Setting this will cause the source to
 	 * be unverified since it does no checking to make sure the URL points to a valid, accessible location.
 	 * 
 	 * @param source URL The location to which this db should be persisted.
 	 */
 	public void setSourceURL(URL source) {
 		this.sourceURL = source;
 		this.sourceVerified  = false;
 	}
 	
 	/**
 	 * Denotes if the sourceURL is known to be a valid, accessible location.
 	 * 
 	 * @return boolean true if the sourceURL is valid and accessible, false if it's not valid/accessible or it's unknown
 	 */
 	public boolean isSourceVerified() {
 		return this.sourceVerified;
 	}
 	
 	/**
 	 * Sets whether the sourceURL is valid and accessible. This should only be set once the db has been successfully persisted
 	 * to the sourceURL!
 	 * 
 	 * @param verified boolean true if the sourceURL is valid and accessible, false if it's not valid/accessible or it's unknown
 	 */
 	public void setSourceVerified(boolean verified) {
 		this.sourceVerified = verified;
 	}
 
 	/**
 	 * Sets the time that the document located at the sourceURL was last modified, or 0 if unknown
      * @param urlLastModifiedTime the urlLastModifiedTime to set
      */
     public void setUrlLastModifiedTime(long urlLastModifiedTime)
     {
 	    this.urlLastModifiedTime = urlLastModifiedTime;
     }
 
 	/**
 	 * The time at which the sourceURL document was last modified, in ms since Jan 1, 1970, or 0 if unknown. This can be used to determine if an update is needed.
      * @return the urlLastModifiedTime
      */
     public long getUrlLastModifiedTime()
     {
 	    return urlLastModifiedTime;
     }
     
     public static void setResourceLoaderFactory(IResourceLoaderFactory factory) {
     	resourceLoaderFactory = factory;
     }
 
     /**
      * This verifies the particular object is valid to add to this database.
      * If not it throws a runtime exception. 
      * Invalid objects are transient otobjects, or objects that are not primitives, otid, lists, 
      * maps, or blobs.
      * @param obj
      */
     final static void checkObject(Object obj)
     {
     	if(obj instanceof OTTransientMapID){
     		throw new RuntimeException("Can't add transient id to XMLDatabase. id: " + 
     			((OTTransientMapID)obj).toInternalForm());
     	}
     }
     
     public URI getURI()
     {
     	URL srcURL = getSourceURL();
     	try {
     		if(srcURL != null){
     			return srcURL.toURI();
     		} else {
     			return new URI("xml-db:/" + getDatabaseId());
     		}
     	} catch (URISyntaxException e) {
     		e.printStackTrace();
     	}    		
     	
     	return null;
     }
 
 	public ArrayList<OTDataPropertyReference> getIncomingReferences(OTID otid)
     {
 	    return incomingReferences.get(otid);
     }
 	
 	public ArrayList<OTDataPropertyReference> getOutgoingReferences(OTID otid)
     {
 	    return outgoingReferences.get(otid);
     }
 }
