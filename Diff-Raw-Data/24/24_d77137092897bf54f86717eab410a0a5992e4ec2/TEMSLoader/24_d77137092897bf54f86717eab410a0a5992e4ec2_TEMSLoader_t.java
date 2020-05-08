 package org.amanzi.neo.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.refractions.udig.catalog.CatalogPlugin;
 import net.refractions.udig.catalog.ICatalog;
 import net.refractions.udig.catalog.IService;
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.ui.ApplicationGIS;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEvent;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEventType;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.MeasurementRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.loader.NetworkLoader.CRS;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 
 public class TEMSLoader {	
     private CRS crs = null;
 	private NeoService neo;
 	private String filename = null;
 	private String basename = null;
 	private Node file = null;
 	private Node point = null;
     private Node gis = null;
 	private String[] headers = null;
 	private HashMap<String,Integer> headerIndex = null;
 	private int line_number = 0;
     private int first_line = 0;
     private int last_line = 0;
     private String previous_ms = null;
     private String previous_time = null;
     private int previous_pn_code = -1;
     private String latlong = null;
     private long started = System.currentTimeMillis();
     private String time = null;
     private HashMap<Integer,int[]> stats = new HashMap<Integer,int[]>();
     private HashMap<String,float[]> signals = new HashMap<String,float[]>();
     private int count_valid_message = 0;
     private int count_valid_location = 0;
     private int count_valid_changed = 0;
     private long savedData = 0;
     private int limit = 0;
     private static int[] times = new int[2];
     private double[] bbox;
 	private String dataset=null;
 	private Node datasetNode=null;
 	/** How many units of work for the progress monitor for each file */
 	public static final int WORKED_PER_FILE = 10;
 
     public TEMSLoader(String filename) {
 		this(null,filename);
 	}
     
     /**
      * Additional constructor for starting neo from thread without Display 
      * 
      * @param filename
      * @param display 
      * @author Lagutko_N
      */
     
     public TEMSLoader(String filename, Display display, String dataset) {
     	this(null, filename, display);
     	if (dataset==null||dataset.trim().isEmpty()){
     		this.dataset=null;
     	}else{
     		this.dataset = dataset.trim();
     	}
     }
     
     /**
      * Additional constructor for starting neo from thread without Display 
      * 
      * @param neo
      * @param filename
      * @param display
      * @author Lagutko_N
      */
     
     public TEMSLoader(NeoService neo, String filename, Display display) {
 		this.neo = neo;		
 		this.filename = filename;
 		this.basename = (new File(filename)).getName();
 		initializeNeo(display);
 	}
 
 	public TEMSLoader(NeoService neo, String filename) {
 		this(neo, filename, null);
 	}
 	
 	/**
 	 * Start Neo from given Display or given Thread 
 	 * 
 	 * @param display Display
 	 */
 	
 	private void initializeNeo(Display display) {
 		//if Display is given than start Neo using syncExec
 		if (display != null) {
 			display.syncExec(new Runnable() {
 				public void run() {
 					if(neo == null) neo = NeoServiceProvider.getProvider().getService();
 				}
 			});
 		}
 		//if Display is not given than initialize Neo as usual
 		else {
 			if(this.neo == null) this.neo = NeoServiceProvider.getProvider().getService();
 		}
 	}
 
 	public void run(IProgressMonitor monitor) throws IOException {
         if(monitor!=null) monitor.subTask(filename);
 		BufferedReader reader = new BufferedReader(new FileReader(filename));
 		try{
 			String line;
 			while((line = reader.readLine())!=null) {
 				line_number++;
 				if(headers==null) parseHeader(line);
 				else parseLine(line);
 				if(monitor!=null && monitor.isCanceled()) break;
 			}
 		}finally{
             reader.close();
             saveData();
             if(monitor!=null) monitor.worked(WORKED_PER_FILE);
             if (gis != null) {
                 Transaction transaction = neo.beginTx();
                 try {
                     if (bbox != null) {
                         gis.setProperty(INeoConstants.PROPERTY_BBOX_NAME, bbox);
                         gis.setProperty("count", savedData + (Long)gis.getProperty("count", 0L));
                     }
                     HashSet<Node> nodeToDelete = new HashSet<Node>();
                     for (Relationship relation : gis.getRelationships(NetworkRelationshipTypes.AGGREGATION, Direction.OUTGOING)) {
                         nodeToDelete.add(relation.getEndNode());
                     }
                     for (Node node : nodeToDelete) {
                         NeoCorePlugin.getDefault().getProjectService().deleteNode(node);
                     }
                     transaction.success();
                     Node mainNode = datasetNode == null ? file : datasetNode;
                     NeoCorePlugin.getDefault().getProjectService().addDriveToProject(getAweProjectName(), mainNode);
                 } finally {
                     transaction.finish();
                     NeoServiceProvider.getProvider().commit();
                 }
             }
             String databaseLocation = NeoServiceProvider.getProvider().getDefaultDatabaseLocation();
             NeoCorePlugin.getDefault().getUpdateDatabaseManager()
                     .fireUpdateDatabase(new UpdateDatabaseEvent(UpdateDatabaseEventType.GIS));
             ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
             URL url = new URL("file://" + databaseLocation);
             List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(url);
             for (IService service : services) {
                 System.out.println("TEMS Found catalog service: " + service);
                 if (catalog.getById(IService.class, service.getIdentifier(), new NullProgressMonitor()) != null) {
                     catalog.replace(service.getIdentifier(), service);
                 } else {
                     catalog.add(service);
                 }
             }
         }
     }
 
 
     /**
      * return AWE project name of active map
      * 
      * @return
      */
     public static String getAweProjectName() {
         IMap map = ApplicationGIS.getActiveMap();
         return map == ApplicationGIS.NO_MAP ? null : map.getProject().getName();
     }
     private String status(){
     	if(started<=0) started = System.currentTimeMillis();
         return (line_number>0 ? "line:"+line_number : ""+((System.currentTimeMillis()-started)/1000.0)+"s");
     }
 
     private void debug(final String line){
     	PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				NeoLoaderPlugin.debug("TEMS:"+basename+":"+status()+": "+line);
 			}
 		});
 	}
 	
 	private void info(final String line){
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				NeoLoaderPlugin.notify("TEMS:"+basename+":"+status()+": "+line);
 			}
 		});	
         // TODO if this info is necessary, then put this info in display thread
         // NeoLoaderPlugin.info("TEMS:"+basename+":"+status()+": "+line);
 	}
 	
 	private void notify(final String line){
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				NeoLoaderPlugin.notify("TEMS:"+basename+":"+status()+": "+line);
 			}
 		});		
 	}
 	
 	private void error(final String line){
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				NeoLoaderPlugin.notify("TEMS:"+basename+":"+status()+": "+line);
 			}
 		});	
 	}
 	
 	public int getLimit(){
 		return limit;
 	}
 
 	public void setLimit(int value){
 		this.limit = value;
 	}
 	
 	/**
 	 * Converts to lower case and replaces all illegal characters with '_' and removes training '_'
 	 * @param original header String
 	 * @return edited String
 	 */
 	private static String cleanHeader(String header) {
     	return header.replaceAll("[\\s\\-\\[\\]\\(\\)\\/\\.]+", "_").replaceAll("\\_$", "").toLowerCase();
     }
 
 	private void parseHeader(String line){
 		debug(line);
 		String fields[] = line.split("\\t");
 		if(fields.length<2) return;
 		headers = fields;
 		headerIndex = new HashMap<String,Integer>();
 		int index=0;
 		for(String header:headers){
 			header = cleanHeader(header);
 			debug("Added header["+index+"] = "+header);
 			headerIndex.put(header,index++);
 		}
 	}
 	private int i_of(String header){
 		return headerIndex.get(header);
 	}
 	
     private double dbm2mw(int dbm){
     	return Math.pow(10.0, (((float)dbm)/10.0));
     }
     private float mw2dbm(double mw){
       return (float)(10.0*Math.log10(mw));
     }
 	
 	private void parseLine(String line){
 		//debug(line);
 		String fields[] = line.split("\\t");
 		if(fields.length<2) return;
 
         this.time = fields[i_of(INeoConstants.PROPERTY_TIME_NAME)];
         String ms = fields[i_of(INeoConstants.HEADER_MS)];
         String event = fields[i_of(INeoConstants.HEADER_EVENT)];    // currently only getting this as a change marker
         String message_type = fields[i_of(INeoConstants.HEADER_MESSAGE_TYPE)];    // need this to filter for only relevant messages
         //message_id = fields[i_of("message_id")];    // parsing this is not faster
         if(!INeoConstants.MESSAGE_TYPE_EV_DO.equals(message_type)) return;
         this.count_valid_message += 1;
         //return unless message_id == '27019'    // not faster
         //return unless message_id.to_i == 27019    // not faster
         
         // TODO: Ignore lines with Event=~/Idle/ since these generally contain invalid All-RX-Power
         // TODO: Also be careful of any All-RX-Power of -63 (since it is most often invalid data)
         // TODO: If number of PN codes does not match number of EC-IO make sure to align correct values to PNs
 
         String latitude = fields[i_of(INeoConstants.HEADER_ALL_LATITUDE)];
         String longitude = fields[i_of(INeoConstants.HEADER_ALL_LONGITUDE)];
         String thisLatLong = latitude+"\t"+longitude;
         if(!thisLatLong.equals(this.latlong)){
           saveData();	// persist the current data to database
           this.latlong = thisLatLong;
         }
         if(latitude.length()==0 || longitude.length()==0) return;
         this.count_valid_location += 1;
 
         int channel = 0;
         int pn_code = 0;
         int ec_io = 0;
         int measurement_count = 0;
         try{
         channel = Integer.parseInt(fields[i_of(INeoConstants.HEADER_ALL_ACTIVE_SET_CHANNEL_1)]);
         pn_code = Integer.parseInt(fields[i_of(INeoConstants.HEADER_ALL_ACTIVE_SET_PN_1)]);
         ec_io = Integer.parseInt(fields[i_of(INeoConstants.HEADER_ALL_ACTIVE_SET_EC_IO_1)]);
         measurement_count = Integer.parseInt(fields[i_of(INeoConstants.HEADER_ALL_PILOT_SET_COUNT)]);
         }catch(NumberFormatException e){
         	error("Failed to parse a field on line "+line_number+": "+e.getMessage());        	
         }
         if(measurement_count > 12){
             error("Measurement count "+measurement_count+" > 12");
             measurement_count = 12;
         }
         boolean changed = false;
         if(!ms.equals(this.previous_ms)){
             changed = true;
             this.previous_ms = ms;
         }
         if(!this.time.equals(this.previous_time)){
             changed = true;
             this.previous_time = this.time;
         }
         if(pn_code!=this.previous_pn_code){
         	if(this.previous_pn_code>=0){
         		error("SERVER CHANGED");
         	}
        		changed = true;
             this.previous_pn_code = pn_code;
         }
         if(measurement_count > 0 && (changed || event.length()>0)){
           if(this.limit>0 && this.count_valid_changed > this.limit) return;
           if(first_line==0) first_line = line_number;
           last_line = line_number;
           this.count_valid_changed += 1;
           debug(time+": server channel["+channel+"] pn["+pn_code+"] Ec/Io["+ec_io+"]\t"+event+"\t"+this.latlong);
           for(int i=1;i<=measurement_count;i++){
             // Delete invalid data, as you can have empty ec_io
             // zero ec_io is correct, but empty ec_io is not
         	try{
         		ec_io = Integer.parseInt(fields[i_of(INeoConstants.HEADER_PREFIX_ALL_PILOT_SET_EC_IO + i)]);
 	            channel = Integer.parseInt(fields[i_of(INeoConstants.HEADER_PREFIX_ALL_PILOT_SET_CHANNEL + i)]);
 	            pn_code = Integer.parseInt(fields[i_of(INeoConstants.HEADER_PREFIX_ALL_PILOT_SET_PN+i)]);
 	            debug("\tchannel["+channel+"] pn["+pn_code+"] Ec/Io["+ec_io+"]");
 	            if(!stats.containsKey(pn_code)) this.stats.put(pn_code,new int[2]);
 	            stats.get(pn_code)[0]+=1;
 	            stats.get(pn_code)[1]+=ec_io;
 	            String chan_code = ""+channel+"\t"+pn_code;
 	            if(!signals.containsKey(chan_code)) signals.put(chan_code,new float[2]);
 	            signals.get(chan_code)[0] += dbm2mw(ec_io);
 	            signals.get(chan_code)[1] += 1;
             }catch(Exception e){
             	error("Error parsing column "+i+" for EC/IO, Channel or PN: "+e.getMessage());
             	//e.printStackTrace(System.err);
             }
           }
         }
 	}
 
     private static Node getGISNode(NeoService neo, Node tems) {
         Node gis = null;
         Transaction transaction = neo.beginTx();
         try {
             Node reference = neo.getReferenceNode();
             for (Relationship relationship : reference.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(INeoConstants.GIS_TYPE_NAME)
                         && node.hasProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME).toString().equals(GisTypes.Tems.getHeader())){
                 	node.createRelationshipTo(tems, GeoNeoRelationshipTypes.NEXT);
                 	return node;
                 }
             }
             gis = neo.createNode();
             gis.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.GIS_TYPE_NAME);
             gis.setProperty(INeoConstants.PROPERTY_NAME_NAME, INeoConstants.GIS_TEMS_NAME);
             gis.setProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, GisTypes.Tems.getHeader());
             reference.createRelationshipTo(gis, NetworkRelationshipTypes.CHILD);
             gis.createRelationshipTo(tems, GeoNeoRelationshipTypes.NEXT);
             transaction.success();
         } finally {
             transaction.finish();
         }
         return gis;
     }
 	/**
 	 * This method is called to dump the current cache of signals as one located point
 	 * linked to a number of signal strength measurements.
 	 */
 	private void saveData() {
 		if (signals.size() > 0) {
 			Transaction transaction = neo.beginTx();
 			try {
 				Node mp = neo.createNode();
 				mp.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.MP_TYPE_NAME);
 				mp.setProperty(INeoConstants.PROPERTY_TIME_NAME, this.time);
 				mp.setProperty(INeoConstants.PROPERTY_FIRST_LINE_NAME, first_line);
 				mp.setProperty(INeoConstants.PROPERTY_LAST_LINE_NAME, last_line);
                 String[] ll = latlong.split("\\t");
                 double lat = Double.parseDouble(ll[0]);
                 mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, lat);
                 double lon = Double.parseDouble(ll[1]);
                 mp.setProperty(INeoConstants.PROPERTY_LON_NAME, lon);
				if (file == null) {
					Node reference = neo.getReferenceNode();
					datasetNode=findOrCreateDatasetNode(neo.getReferenceNode(),dataset);
					file = findOrCreateFileNode(reference,datasetNode);
					
					Node mainFileNode=datasetNode==null?file:datasetNode;
                    file.createRelationshipTo(mp, GeoNeoRelationshipTypes.NEXT);
                    gis = getGISNode(neo, mainFileNode);
                    bbox = (double[])gis.getProperty(INeoConstants.PROPERTY_BBOX_NAME, bbox);

					debug("Added '" + mp.getProperty(INeoConstants.PROPERTY_TIME_NAME) + "' as first measurement of '" + file.getProperty(INeoConstants.PROPERTY_FILENAME_NAME));
				}
                 if (bbox == null) {
                     bbox = new double[] {lon, lon, lat, lat};
                 } else {
                     if (bbox[0] > lon)
                         bbox[0] = lon;
                     if (bbox[1] < lon)
                         bbox[1] = lon;
                     if (bbox[2] > lat)
                         bbox[2] = lat;
                     if (bbox[3] < lat)
                         bbox[3] = lat;
                 }
                 if (crs == null) {
                     crs = CRS.fromLocation(Float.parseFloat(ll[0]), Float.parseFloat(ll[1]), null);
                     file.setProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, crs.getType());
                     file.setProperty(INeoConstants.PROPERTY_CRS_NAME, crs.toString());
                     gis.setProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, crs.getType());
                     gis.setProperty(INeoConstants.PROPERTY_CRS_NAME, crs.toString());
                 }
 				debug("Added measurement point: " + propertiesString(mp));
 				if (point != null) {
                     point.createRelationshipTo(mp, GeoNeoRelationshipTypes.NEXT);
 				}
 				point = mp;
 				Node prev_ms = null;
                 TreeMap<Float, String> sorted_signals = new TreeMap<Float, String>();
                 for (String chanCode : signals.keySet()) {
                     float[] signal = signals.get(chanCode);
                     sorted_signals.put(signal[1] / signal[0], chanCode);
                 }
                 for (Map.Entry<Float, String> entry : sorted_signals.entrySet()) {
                     String chanCode = entry.getValue();
 					float[] signal = signals.get(chanCode);
 					double mw = signal[0] / signal[1];
 					Node ms = neo.createNode();
 					String[] cc = chanCode.split("\\t");
 					ms.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.HEADER_MS);
                     ms.setProperty(INeoConstants.PRPOPERTY_CHANNEL_NAME, Integer.parseInt(cc[0]));
                     ms.setProperty(INeoConstants.PROPERTY_CODE_NAME, Integer.parseInt(cc[1]));
 					ms.setProperty(INeoConstants.PROPERTY_DBM_NAME, mw2dbm(mw));
 					ms.setProperty(INeoConstants.PROPERTY_MW_NAME, mw);
 					debug("\tAdded measurement: " + propertiesString(ms));
 					point.createRelationshipTo(ms, MeasurementRelationshipTypes.CHILD);
 					if (prev_ms != null) {
                         prev_ms.createRelationshipTo(ms, MeasurementRelationshipTypes.NEXT);
                     }
                     prev_ms = ms;
 				}
                 savedData ++;
 				transaction.success();
 			} finally {
 				transaction.finish();
 			}
 		}
 		signals.clear();
 		first_line = 0;
 		last_line = 0;
 	}
 
     /**
      * Finds or create if not exist necessary file node
      * 
      * @param root root node
      * @param datasetNode dataset node
      * @return
      */
     private Node findOrCreateFileNode(Node root, Node datasetNode) {
         Node file = findFileNode(filename);
         if (file == null) {
             file = neo.createNode();
             file.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.FILE_TYPE_NAME);
             file.setProperty(INeoConstants.PROPERTY_NAME_NAME, basename);
             file.setProperty(INeoConstants.PROPERTY_FILENAME_NAME, filename);
         }
         if (datasetNode != null) {
             datasetNode.createRelationshipTo(file, GeoNeoRelationshipTypes.NEXT);
         }
         return file;
     }
 
     /**
      * Find file Node
      * 
      * @param filename file name - value of property INeoConstants.PROPERTY_FILENAME_NAME
      * @return file node or null
      */
     private Node findFileNode(final String filename) {
         Iterator<Node> iterator = neo.getReferenceNode().traverse(Order.DEPTH_FIRST, new StopEvaluator() {
             
             @Override
             public boolean isStopNode(TraversalPosition traversalposition) {
                 return traversalposition.depth() > 0
                         && traversalposition.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").toString().equals(
                                 INeoConstants.FILE_TYPE_NAME);
                 }
         },new ReturnableEvaluator() {
             
             @Override
             public boolean isReturnableNode(TraversalPosition traversalposition) {
                 Node currentNode = traversalposition.currentNode();
                 return currentNode.getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(INeoConstants.FILE_TYPE_NAME)
                         && currentNode.getProperty(INeoConstants.PROPERTY_FILENAME_NAME, "").equals(filename);
             }
         }, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator();
         return iterator.hasNext() ? iterator.next() : null;
     }
 
     /**
      * Finds or create if not exist necessary dataset node
      * 
      * @param root root node
      * @param datasetName name of dataset node
      * @return dataset node
      */
     private Node findOrCreateDatasetNode(Node root, final String datasetName) {
         Transaction tx = null;
         Node result;
         try {
             tx = neo.beginTx();
             if (datasetName == null || datasetName.isEmpty()) {
                 return null;
             }
             Traverser traverse = NeoCorePlugin.getDefault().getProjectService().getAllDatasetTraverser(root);
             for (Node node : traverse) {
                 if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").equals(datasetName)) {
                     return node;
                 }
             }
             result = neo.createNode();
             result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.DATASET_TYPE_NAME);
             result.setProperty(INeoConstants.PROPERTY_NAME_NAME, datasetName);
             tx.success();
             return result;
         } finally {
             tx.finish();
         }
     }
 	private static String propertiesString(Node node){
 		StringBuffer properties = new StringBuffer();
 		for(String property:node.getPropertyKeys()) {
 			if(properties.length()>0) properties.append(", ");
 			properties.append(property).append(" => ").append(node.getProperty(property));
 		}
 		return properties.toString();
 	}
 
 	private static int addTimes(long taken) {
 		times[0] += 1;
 		times[1] += taken;
 		return times[0];
 	}
 
 	private static void printTimesStats() {
 		System.err.println("Finished " + times[0] + " loads in " + times[1] / 60000.0 + " minutes (average "
 				+ (times[1] / times[0]) / 1000.0 + " seconds per load)");
 	}
 	
 	public void printStats(boolean verbose){
         long taken = System.currentTimeMillis()-started;
         addTimes(taken);
         notify("Finished loading "+basename+" data in "+(taken/1000.0)+" seconds");
         notify("Read "+(line_number-1)+" data lines and then filtered down to:");
         notify("\t"+count_valid_message+" with valid messages");
         notify("\t"+count_valid_location+" with known locations");
         notify("\t"+count_valid_changed+" with changed data");
         notify("Read "+stats.keySet().size()+" unique PN codes:");
         for(int pn_code:stats.keySet()){
         	int[] pn_counts = stats.get(pn_code);
         	notify("\t"+pn_code+" measured "+pn_counts[0]+" times (average Ec/Io = "+pn_counts[1]/pn_counts[0]+")");
         }
 		if (file != null && verbose) {
             printMeasurements(file);
         } else {
             error("No measurement file node found");
         }
 	}
 
 	private void printMeasurement(Node measurement){
 		info("Found measurement: "+propertiesString(measurement)+" --- "+childrenString(measurement));
 	}
 
 	private String childrenString(Node node){
 		StringBuffer sb = new StringBuffer();
 		for(Relationship relationship:node.getRelationships(MeasurementRelationshipTypes.CHILD,Direction.OUTGOING)){
 			if(sb.length()>0) sb.append(", ");
 			Node ms = relationship.getEndNode();
 			sb.append(ms.getProperty(INeoConstants.PRPOPERTY_CHANNEL_NAME)).append(":");
 			sb.append(ms.getProperty(INeoConstants.PROPERTY_CODE_NAME)).append("=");
 			sb.append((ms.getProperty(INeoConstants.PROPERTY_DBM_NAME).toString()+"000000").substring(0,6));
 		}
 		return sb.toString();		
 	}
 	private void printMeasurements(Node file){
 		if (file == null)
 			return;
 		Transaction transaction = neo.beginTx();
 		try {
             for (Relationship relationship : file.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)) {
 				Node measurement = relationship.getEndNode();
 				printMeasurement(measurement);
                 Iterator<Relationship> relationships = measurement.getRelationships(GeoNeoRelationshipTypes.NEXT,
 						Direction.OUTGOING).iterator();
 				while (relationships.hasNext()) {
 					relationship = relationships.next();
 					measurement = relationship.getEndNode();
 					printMeasurement(measurement);
                     relationships = measurement.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)
 							.iterator();
 				}
 			}
 			transaction.success();
 		} finally {
 			transaction.finish();
 		}
 	}
 	
 	//TODO: Lagutko: is this methor required?
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length<1) args = new String[]{"amanzi/test.FMT","amanzi/0904_90.FMT","amanzi/0905_22.FMT","amanzi/0908_44.FMT"};
 		EmbeddedNeo neo = new EmbeddedNeo("var/neo");
 		try{
 			for(String filename:args){
 				TEMSLoader temsLoader = new TEMSLoader(neo,filename);
 				temsLoader.setLimit(100);
 				temsLoader.run(null);
 				temsLoader.printStats(true);	// stats for this load
 			}
 			printTimesStats();	// stats for all loads
 		} catch (IOException e) {
 			System.err.println("Error loading TEMS data: "+e);
 			e.printStackTrace(System.err);
 		}finally{
 			neo.shutdown();
 		}
 	}
 }
