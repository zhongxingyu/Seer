 package org.lh.dmlj.schema.editor;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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
 		for (Relationship relationship :
 			 recordNode.getRelationships(RelTypes.RECORD_ELEMENT)) {
 			
 			Node elementNode = relationship.getStartNode();
 			
 			ElementInfoValueObject elementInfoValueObject =
 				createElementInfoValueObject(elementNode);
 			
			recordInfoValueObject.getElementInfoValueObjects().add(elementInfoValueObject);
 		}			
		Collections.sort(recordInfoValueObject.getElementInfoValueObjects(),
 						 new Comparator<ElementInfoValueObject>() {
 			@Override
 			public int compare(ElementInfoValueObject valueObject1,
 							   ElementInfoValueObject valueObject2) {
 				
 				return valueObject1.getSeqNo() - valueObject2.getSeqNo();
 			}
		});				
 		
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
 		
 		// copy the IDMSNTWK mapping file to the temporary files folder...
 		InputStream source = 
 			cl.getResourceAsStream("/resources/org.lh.dmlj.idmsntwk.mapping");
 		File target = new File(tmpFolder, "org.lh.dmlj.idmsntwk.mapping");
 		if (!copy(source, target)) {
 			throw new Error("cannot copy " + target.getName() + 
 						    " to the temporary files folder");
 		}
 		
 		// copy the IDMSNTWK (old-style) schema file to the temporary files 
 		// folder...
 		source = 
 			cl.getResourceAsStream("/resources/IDMSNTWK version 1.xml");
 		target = new File(tmpFolder, "IDMSNTWK version 1.xml");
 		if (!copy(source, target)) {
 			throw new Error("cannot copy " + target.getName() + 
 						    " to the temporary files folder");
 		}
 		
 		// copy the Javadoc .zip file to the temporary files folder, create and 
 		// populate the Neo4j database that will hold this information, which 
 		// will be used in the property view's "Info" tab when a record of 
 		// schema IDMSNTWK version 1 is selected in the editor...
 		source = 
 			cl.getResourceAsStream("/resources/Javadoc_IDMSNTWK_r16SP2.zip");		
 		target = new File(tmpFolder, "Javadoc_IDMSNTWK_r16SP2.zip");
 		final File fTarget = target;
 		if (!copy(source, target)) {
 			throw new Error("cannot copy " + target.getName() + 
 						    " to the temporary files folder");
 		}
 		File dbFolder = new File(tmpFolder, "Javadoc_IDMSNTWK_r16SP2");
 		neo4jDb = new EmbeddedGraphDatabase(dbFolder.getAbsolutePath());
 		final ZipFile zipFile = new ZipFile(target);
 		JavadocDatabaseBuildJob job = 
 			new JavadocDatabaseBuildJob(zipFile, neo4jDb);
 		job.addJobChangeListener(new JobChangeAdapter() {
 			@Override
 			public void done(IJobChangeEvent event) {	
 				// close the Javadoc .zip file ant (try to) delete it...
 				try {
 					zipFile.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				fTarget.delete();
 			}
 		});
 		job.schedule();		
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
 			t.printStackTrace();
 		}
 		
 		// cleanup our temporary file folder...
 		deleteDirectoryContents(tmpFolder);
 		tmpFolder.delete();		
 		
 		figureFont.dispose();
 		syntaxFont.dispose();
 		plugin = null;
 		super.stop(context);
 	}
 
 }
