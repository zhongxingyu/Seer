 package org.lh.dmlj.schema.editor;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.PropertyResourceBundle;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.lh.dmlj.schema.editor.job.JavadocDatabaseBuildJob;
 import org.lh.dmlj.schema.editor.property.ElementInfoValueObject;
 import org.lh.dmlj.schema.editor.property.RecordInfoValueObject;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Plugin extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String 		PLUGIN_ID = 
 		"org.lh.dmlj.schema.editor"; //$NON-NLS-1$
 	
 	private static final String 	JAVADOC_LEVEL_NAME = "Javadoc_IDMSNTWK";
 	private static final String 	JAVADOC_LEVEL_VALUE = "r16SP2";
 	
 	private final static String 	PLUGIN_PROPERTIES = "plugin.properties";
 
 	// The shared instance
 	private static Plugin 			plugin;
 	
 	// Neo4j relationship(s)
 	public static enum RelTypes implements RelationshipType { RECORD_ELEMENT };	
 			
 	private GraphDatabaseService 	neo4jDb;
 	private Font 					figureFont = 
 		new Font(Display.getCurrent(), "Arial", 6, SWT.NORMAL);
 	private Map<String, Image> 	 	images = new HashMap<String, Image>();
 	private Font 					syntaxFont = 
 		new Font(Display.getCurrent(), "Courier New", 10, SWT.NORMAL);
 	private File 					tmpFolder;
 	
 	private PropertyResourceBundle pluginProperties;
 	
 	private static boolean copy(InputStream source, File target) {
 		try {
 			OutputStream out = new FileOutputStream(target);
 			boolean b = copy(source, out);
 			out.flush();
 			out.close();
 			return b;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 	
 	private static boolean copy(InputStream source, OutputStream target) {
 		try {
 			int chunkSize = 63 * 1024;			
 			byte[] ba = new byte[chunkSize];
 			while (true) {
 				int bytesRead = readBlocking(source, ba, 0, chunkSize);
 				if (bytesRead > 0) {
 					target.write(ba, 0, bytesRead);					
 				} else {
 					break;
 				}
 			}
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}		
 	
 	/**
 	 * Creates an ElementInfoValueObject from a database node.
 	 * @param node the (element) database node
 	 * @return the ElementInfoValueObject
 	 */
 	private static ElementInfoValueObject createElementInfoValueObject(Node node) {
 		
 		Integer seqNo = (Integer) node.getProperty("seqNo");
 		
 		ElementInfoValueObject valueObject = 
 			new ElementInfoValueObject(seqNo.intValue());
 		String elementName = (String) node.getProperty("elementName");
 		valueObject.setElementName(elementName);
 		if (node.hasProperty("levelAndElementName")) {
 			String levelAndElementName =
 				(String) node.getProperty("levelAndElementName");
 			valueObject.setLevelAndElementName(levelAndElementName);
 		}
 		if (node.hasProperty("pictureAndUsage")) {
 			String pictureAndUsage = 
 				(String) node.getProperty("pictureAndUsage");
 			valueObject.setPictureAndUsage(pictureAndUsage);
 		}
 		if (node.hasProperty("description")) {
 			String description = (String) node.getProperty("description");
 			valueObject.setDescription(description);
 		}
 		
 		return valueObject;
 	}
 	
 	/**
 	 * Creates a RecordInfoValueObject with an empty list of element info value
 	 * objects from a database node.
 	 * @param node the (record) database node
 	 * @return the RecordInfoValueObject
 	 */
 	private static RecordInfoValueObject createRecordInfoValueObject(Node node) {
 
 		RecordInfoValueObject valueObject = new RecordInfoValueObject();
 			
 		valueObject.setRecordName((String) node.getProperty("recordName"));				
 		valueObject.setDescription((String) node.getProperty("description"));
 		if (node.hasProperty("documentId")) {
 			String documentId = (String) node.getProperty("documentId");
 			valueObject.setDocumentId(documentId);
 		}
 		if (node.hasProperty("documentName")) {
 			String documentName = 
 				(String) node.getProperty("documentName");
 			valueObject.setDocumentName(documentName);
 		}
 		if (node.hasProperty("recordLength")) {
 			String recordLength = 
 				(String) node.getProperty("recordLength");
 			valueObject.setRecordLength(recordLength);
 		}
 		if (node.hasProperty("establishedBy")) {
 			String establishedBy = 
 				(String) node.getProperty("establishedBy");
 			valueObject.setEstablishedBy(establishedBy);
 		}
 		if (node.hasProperty("ownerOf")) {
 			String ownerOf = (String) node.getProperty("ownerOf");				
 			valueObject.setOwnerOf(ownerOf);
 		}
 		if (node.hasProperty("memberOf")) {
 			String memberOf = (String) node.getProperty("memberOf");				
 			valueObject.setMemberOf(memberOf);
 		}
 		if (node.hasProperty("locationMode")) {
 			String locationMode = 				
 				(String) node.getProperty("locationMode");
 			valueObject.setLocationMode(locationMode);
 		}
 		if (node.hasProperty("withinArea")) {
 			String withinArea = (String) node.getProperty("withinArea");
 			valueObject.setWithinArea(withinArea);
 		}
 		
 		return valueObject;
 	}
 
 	private static void deleteDirectoryContents(File directory) {
 		for (String fileName : directory.list()) {
 			File file = new File(directory, fileName);
 			if (file.isDirectory()) {
 				deleteDirectoryContents(file);
 			}
 			file.delete();			
 		}		
 	}	
 	
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Plugin getDefault() {
 		return plugin;
 	}
 	
 	private static int readBlocking(InputStream in, byte b[], int off, int len)
 		throws IOException {
 		
 		int totalBytesRead = 0;
 		while (totalBytesRead < len) {
 			int bytesRead = 
 				in.read(b, off + totalBytesRead, len - totalBytesRead);
 			if (bytesRead < 0) {
 				break;
 			}
 			totalBytesRead += bytesRead;
 		}
 		return totalBytesRead;
 	}	
 
 	/**
 	 * The constructor
 	 */
 	public Plugin() {
 	}
 
 	public Font getFigureFont() {
 		return figureFont;
 	}
 	
 	public Image getImage(String path) {
 		if (images.containsKey(path)) {
 			return images.get(path);
 		}
 		Image image = null;
 		URL baseURL = getBundle().getEntry("/");
 		try {
 			URL url = new URL(baseURL, path);
 			image = ImageDescriptor.createFromURL(url).createImage();
 		} catch (MalformedURLException e) {			
 		}
 		if (image != null) {
 			images.put(path, image);
 		}
 		return image;
 	}	
 	
 	public PropertyResourceBundle getPluginProperties() {
 		return pluginProperties;
 	}
 
 	/**
 	 * Creates a RecordInfoValueObject for the given record, provided that a
 	 * record node exists in the database.  The RecordInfoValueObject will
 	 * contain a list with one ElementInfoValueObject instance per element in 
 	 * the record. 
 	 * @param recordName the name of the record 
 	 * @return a RecordInfoValueObject or null if no information exists in the
 	 *         database
 	 */
 	public RecordInfoValueObject getRecordInfoValueObject(String recordName) {		
 		
 		// adjust the record name in the case of DDLCATLOD records...
 		String adjustedRecordName;
 		if (recordName.endsWith("_")) {
 			StringBuilder p = new StringBuilder(recordName);
 			p.setLength(p.length() - 1);
 			adjustedRecordName = p.toString();
 		} else {
 			adjustedRecordName = recordName;
 		}
 		
 		// locate the record database node; if we cannot find it, return null to
 		// indicate to the caller that no information can be found...
 		Index<Node> recordsIndex = neo4jDb.index().forNodes("RECORDS");
 		Node recordNode = 
 			recordsIndex.query("recordName", adjustedRecordName).getSingle();
 		if (recordNode == null) {
 			return null;
 		}
 		
 		// create the RecordInfoValueObject with an empty list of element info
 		// value objects...
 		RecordInfoValueObject recordInfoValueObject = 
 			createRecordInfoValueObject(recordNode);		
 		
 		// we need to create an ElementInfoValueObject for each related element
 		// database node and make sure the elements are in the right order...
 		List<ElementInfoValueObject> elementInfoValueObjects = 
 			new ArrayList<>();
 		for (Relationship relationship :
 			 recordNode.getRelationships(RelTypes.RECORD_ELEMENT)) {
 			
 			Node elementNode = relationship.getStartNode();
 			
 			ElementInfoValueObject elementInfoValueObject =
 				createElementInfoValueObject(elementNode);
 			
 			elementInfoValueObjects.add(elementInfoValueObject);
 		}			
 		Collections.sort(elementInfoValueObjects,
 						 new Comparator<ElementInfoValueObject>() {
 			@Override
 			public int compare(ElementInfoValueObject valueObject1,
 							   ElementInfoValueObject valueObject2) {
 				
 				return valueObject1.getSeqNo() - valueObject2.getSeqNo();
 			}
 		});		
 		recordInfoValueObject.getElementInfoValueObjects()
 							 .addAll(elementInfoValueObjects);
 		
 		// we're done
 		return recordInfoValueObject;
 	}
 
 	public Font getSyntaxFont() {
 		return syntaxFont;
 	}
 
 	public File getTemporaryFilesFolder() {
 		return tmpFolder;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;		
 				
 		try {
 			InputStream in = 
 				FileLocator.openStream(this.getBundle(), 
 									   new Path(PLUGIN_PROPERTIES), false);
 			pluginProperties = new PropertyResourceBundle(in);
 			in.close();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 		// Locate the temporary files folder in the workspace.  We should 
 		// probably put our temporary files somewhere else, since the folder 
 		// returned by the call to the plug-in's getStateLocation() method 
 		// "is recommended for plug-in preference settings and other 
 		// configuration parameters".  That location is perfectly usable though.
 		tmpFolder = new File(getStateLocation().toFile(), "tmp");
 		if (tmpFolder.exists()) {
 			deleteDirectoryContents(tmpFolder);
 			tmpFolder.delete();	
 		}	
 		if (!tmpFolder.exists()) {
 			tmpFolder.mkdir();
 		}
 		
 		// we need to place some files in the temporary files folder - we need
 		// those files to create IDatabase instances denoting dictionaries; we
 		// need the plug-in's classloader for this...
		ClassLoader cl = Plugin.class.getClassLoader();		
 		
 		// Determine the Neo4j database path and create and populate the 
 		// database if needed.  The Neo4j database is used in the property 
 		// view's "Info" tab when a record of schema IDMSNTWK version 1 is 
 		// selected in the editor.  We keep an indicator in the plug-in's
 		// preference store so that we know if the database is fully loaded or
 		// not.
 		File dbFolder = new File(getStateLocation().toFile(), 
 								 JAVADOC_LEVEL_NAME + "_" + JAVADOC_LEVEL_VALUE);	
 		boolean createDatabase = true;
 		if (getPreferenceStore().contains(JAVADOC_LEVEL_NAME) &&
 			getPreferenceStore().getString(JAVADOC_LEVEL_NAME)
 							    .equals(JAVADOC_LEVEL_VALUE) &&
 			dbFolder.exists() && dbFolder.listFiles().length > 0) {
 			
 			// The database exists and is fully loaded if the preference store
 			// contains the right Javadoc level value AND the database folder
 			// exists AND if opening the database doesn't provide any trouble.
 			try {
 				// open the existing database
 				neo4jDb = new EmbeddedGraphDatabase(dbFolder.getAbsolutePath());
 				// the database seems to have opened without problems, so it
 				// looks to be OK; we're done !				
 				createDatabase = false;				
 			} catch (Throwable t) {
 				// we will not be able to delete the database folder and so the
 				// creation of a fresh database will only fail - the only way to
 				// fix this situation is a workbench restart
 			}
 			
 		}
 						
 		if (createDatabase) {
 			
 			// set the preference store property to a question mark; if the user
 			// exits the database before the database is fully loaded, we will
 			// have a trace of that and rebuild the database again			
 			getPreferenceStore().setValue(JAVADOC_LEVEL_NAME, "?");
 			InstanceScope.INSTANCE.getNode(PLUGIN_ID).flush();
 			
 			// clean the database folder
 			if (dbFolder.exists()) {
 				deleteDirectoryContents(dbFolder);
 			} 
 			
 			// copy the Javadoc .zip file to the temporary files folder
			InputStream source = 
				cl.getResourceAsStream("/resources/" + JAVADOC_LEVEL_NAME + 
							   		   "_" + JAVADOC_LEVEL_VALUE + ".zip");		
			File target = new File(tmpFolder, JAVADOC_LEVEL_NAME + "_" + 
							  	   JAVADOC_LEVEL_VALUE + ".zip");
 			final File fTarget = target;
 			if (!copy(source, target)) {
 				throw new Error("cannot copy " + target.getName() + 
 							    " to the temporary files folder");
 			}
 			
 			// create the database; if it already exited and the files in the
 			// folder would be corrupt, we will get an error here, for which 
 			// there really is only 1 solution: a restart of the workbench...
 			try {
 				neo4jDb = new EmbeddedGraphDatabase(dbFolder.getAbsolutePath());
 			} catch (Throwable t) {
 				throw new Error("Cannot build IDMSNTWK V1 Info cache, " +
 								"RESTART THE WORKBENCH to fix this please");
 			}
 			
 			// ...and populate it in the background using a job
 			final ZipFile zipFile = new ZipFile(target);
 			JavadocDatabaseBuildJob job = 
 				new JavadocDatabaseBuildJob(zipFile, neo4jDb);
 			job.addJobChangeListener(new JobChangeAdapter() {
 				@Override
 				public void done(IJobChangeEvent event) {
 					// close the Javadoc .zip file and (try to) delete it...
 					try {
 						zipFile.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					fTarget.delete(); // delete the .zip file in the tmpFolder
 					// set the plug-in's preference store indicator if the job
 					// finished succesfully
 					if (event.getResult().isOK()) {
 						getPreferenceStore().setValue(JAVADOC_LEVEL_NAME, 
 													  JAVADOC_LEVEL_VALUE);
 						try {
 							InstanceScope.INSTANCE.getNode(PLUGIN_ID).flush();
 						} catch (Throwable t) {
 							// ignore, hopefully the plug-in's preference store
 							// gets flushed somewhere later :-)
 						}
 					}
 				}
 			});
 			job.schedule();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {				
 		
 		// close the IDMSNTWK version 1 Javadoc database...
 		try {
 			neo4jDb.shutdown();
 		} catch (Throwable t) {
 			//t.printStackTrace();
 		}
 				
 		/* 
 		We leave the Neo4j database as it is, even if the user is exiting the 
 		workbench before it is fully loaded and thus missing some information 
 		(reason: we might not be able to delete it); the database folder will be 
 		cleaned and the database built again when the workbench restarts, 
 		provided this plug-in is active of course.
 		 */
 		
 		// cleanup our temporary file folder...
 		deleteDirectoryContents(tmpFolder);
 		tmpFolder.delete();		
 		
 		figureFont.dispose();
 		syntaxFont.dispose();
 		plugin = null;
 		super.stop(context);
 	}
 
 }
