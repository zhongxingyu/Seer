 package org.amanzi.neo.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.TreeSet;
 
 import net.refractions.udig.catalog.CatalogPlugin;
 import net.refractions.udig.catalog.ICatalog;
 import net.refractions.udig.catalog.IService;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEvent;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEventType;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.MeasurementRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.service.listener.NeoServiceProviderEventAdapter;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.loader.NetworkLoader.CRS;
 import org.amanzi.neo.loader.dialogs.DriveDialog;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 
 public abstract class DriveLoader extends NeoServiceProviderEventAdapter {
     private String typeName = "Drive";
     protected NeoService neo;
     protected String filename = null;
     protected String basename = null;
     protected String dataset=null;
     private long started = System.currentTimeMillis();
     private LinkedHashMap<String, String> knownHeaders = new LinkedHashMap<String, String>();
     private LinkedHashMap<String,Header> headers = new LinkedHashMap<String,Header>();
     protected class Header {
         private static final int MAX_PROPERTY_VALUE_COUNT = 100;
         int index;
         String key;
         String name;
         HashMap<Class<? extends Object>,Integer> parseTypes = new HashMap<Class<? extends Object>,Integer>();
         HashMap<Object,Integer> values = new HashMap<Object,Integer>();
         int parseCount = 0;
         Header(String name, String key, int index) {
             this.index = index;
             this.name = name;
             this.key = key;
             for(Class<? extends Object> klass: KNOWN_PROPERTY_TYPES){
                 parseTypes.put(klass,0);
             }
         }
         Header(Header old){
             this(old.name,old.key,old.index);
             this.parseCount = old.parseCount;
             this.values = old.values;
         }
         protected boolean invalid(String field) {
             return field==null || field.length()<1 || field.equals("?");
         }
         Object parse(String field) {
             if(invalid(field)) return null;
             parseCount++;
             try {
                 int value = Integer.parseInt(field);
                 incValue(value);
                 incType(Integer.class);
                 return value;
             } catch (Exception e) {
                 try {
                     float value = Float.parseFloat(field);
                     incValue(value);
                     incType(Float.class);
                     return value;
                 } catch (Exception e2) {
                     incValue(field);
                     incType(String.class);
                     return field;
                 }
             }
         }
         private void incType(Class<? extends Object> klass) {
             parseTypes.put(klass, parseTypes.get(klass)+1);
         }
         protected Object incValue(Object value) {
             if(values!=null) {
                 Integer count = values.get(value);
                 if (count == null) {
                     count = 0;
                 }
                 if(count == 0 && values.size() >= MAX_PROPERTY_VALUE_COUNT) {
                     //About to exceed maximum number of unique allowed values, stop counting
                     System.out.println("Property exceeded max value count: "+this.key);
                     values = null;
                 } else {
                     values.put(value, count + 1);
                 }
             }
             return value;
         }
         boolean shouldConvert() {
             return parseCount > 10;
         }
         Class<? extends Object> knownType() {
             Class<? extends Object> best = String.class;
             int maxCount = 0;
             int countFound = 0;
             for(Class<? extends Object> klass:parseTypes.keySet()){
                 int count = parseTypes.get(klass);
                 if(maxCount<parseTypes.get(klass)){
                     maxCount = count;
                     best = klass;
                 }
                 if(count>0){
                     countFound++;
                 }
             }
             if(countFound>1){
                 DriveLoader.this.notify("Header "+key+" had multiple type matches: ");
                 for(Class<? extends Object> klass:parseTypes.keySet()){
                     int count = parseTypes.get(klass);
                     if(count>0){
                         DriveLoader.this.notify("\t"+count+": "+klass+" => "+key);
                     }
                 }
             }
             return best;
         }
     }
     protected class IntegerHeader extends Header {
         IntegerHeader(Header old){
             super(old);
         }
         Integer parse(String field){
             if(invalid(field)) return null;
             parseCount++;
             return (Integer)incValue(Integer.parseInt(field));
         }
         boolean shouldConvert() {
             return false;
         }
         Class<Integer> knownType() {
             return Integer.class;
         }
     }
     protected class FloatHeader extends Header {
         FloatHeader(Header old){
             super(old);
         }
         Float parse(String field){
             if(invalid(field)) return null;
             parseCount++;
             return (Float)incValue(Float.parseFloat(field));
         }
         boolean shouldConvert() {
             return false;
         }
         Class<Float> knownType() {
             return Float.class;
         }
     }
     protected class StringHeader extends Header {
         StringHeader(Header old){
             super(old);
         }
         String parse(String field){
             if(invalid(field)) return null;
             parseCount++;
             return (String)incValue(field);
         }
         boolean shouldConvert() {
             return false;
         }
         Class<String> knownType() {
             return String.class;
         }
     }
     protected int line_number = 0;
     private int limit = 0;
     private CRS crs = null;
     private Node gis = null;
     private Node file = null;
     private double[] bbox;
     protected long savedData = 0;
     private Node datasetNode=null;
     private static int[] times = new int[2];
     private HashMap<Integer,int[]> stats = new HashMap<Integer,int[]>();
     private int countValidMessage = 0;
     private int countValidLocation = 0;
     private int countValidChanged = 0;
     private NeoServiceProvider neoProvider;
     private Display display;
     private String fieldSepRegex;
     private String[] possibleFieldSepRegexes = new String[]{"\\t","\\,","\\;"};
     /** How many units of work for the progress monitor for each file */
     public static final int WORKED_PER_FILE = 100;
     @SuppressWarnings("unchecked")
     public static final Class[] KNOWN_PROPERTY_TYPES = new Class[]{Integer.class, Float.class, String.class};
 
     /**
      * Initialize Loader with a specified set of parameters 
      * 
      * @param type defaults to 'Drive' if empty
      * @param neoService defaults to looking up from Neoclipse if null
      * @param fileName name of file to load
      * @param display Display to use for scheduling plugin lookups and message boxes, or null
      * @param datasetName to add data to, or null to add to same as filename
      */
     protected final void initialize(String typeString, NeoService neoService, String filenameString, Display display, String datasetName) {
         if (typeString != null && !typeString.isEmpty()) {
             this.typeName = typeString;
         }
         if(neoService == null) {
             //if Display is given than start Neo using syncExec
             if (display != null) {
                 display.syncExec(new Runnable() {
                     public void run() {
                         initializeNeo();
                     }
                 });
             }
             //if Display is not given than initialize Neo as usual
             else {
                 initializeNeo();
             }
         } else {
             this.neo = neoService;
         }
         if (datasetName==null||datasetName.trim().isEmpty()){
             this.dataset=null;
         }else{
             this.dataset = datasetName.trim();
         }
         this.display = display;
         this.filename = filenameString;
         this.basename = (new File(filename)).getName();
     }
     
     private void initializeNeo() {
         if(this.neoProvider == null) this.neoProvider = NeoServiceProvider.getProvider();
         if(this.neo == null) this.neo = this.neoProvider.getService();
         this.neoProvider.addServiceProviderListener(this);
     }
 
     /**
      * Converts to lower case and replaces all illegal characters with '_' and removes training '_'
      * @param original header String
      * @return edited String
      */
     protected final static String cleanHeader(String header) {
         return header.replaceAll("[\\s\\-\\[\\]\\(\\)\\/\\.\\\\\\:\\#]+", "_").replaceAll("[^\\w]+", "_").replaceAll("_+", "_").replaceAll("\\_$", "").toLowerCase();
     }
 
     protected boolean haveHeaders() {
         return headers.size()>0;
     }
     private void determineFieldSepRegex(String line){
         int maxMatch = 0;
         for(String regex:possibleFieldSepRegexes){
             String[] fields = line.split(regex);
             if(fields.length > maxMatch){
                 maxMatch = fields.length;
                 fieldSepRegex = regex;
             }
         }
     }
     protected void addKnownHeader(String key, String regex) {
         knownHeaders.put(key, regex);
     }
     protected String[] splitLine(String line){
         return line.split(fieldSepRegex);
     }
     protected final void parseHeader(String line){
         debug(line);
         determineFieldSepRegex(line);
         String fields[] = splitLine(line);
         if(fields.length<2) return;
         int index=0;
         for(String headerName:fields){
             boolean added = false;
             String header = cleanHeader(headerName);
             debug("Added header["+index+"] = "+header);
             for(String key:knownHeaders.keySet()){
                 if (!headers.containsKey(key) && header.matches(knownHeaders.get(key))) {
                     debug("Added known header[" + index + "] = " + key);
                     headers.put(key, new Header(headerName, key, index));
                     added = true;
                     break;
                 }
             }
             if(!added/*!headers.containsKey(header)*/){
                 headers.put(header, new Header(headerName, header, index));
             }
             index++;
         }
     }
     private HashMap<Class<? extends Object>,List<String>> typedProperties = null;
     private Transaction mainTx;
     protected List<String> getIntegerProperties() {
         return getProperties(Integer.class);
     }
     protected List<String> getFloatProperties() {
         return getProperties(Integer.class);
     }
     protected List<String> getStringProperties() {
         return getProperties(Integer.class);
     }
     protected List<String> getProperties(Class<? extends Object> klass) {
         if(typedProperties==null) {
             makeTypedProperties();
         }
         return typedProperties.get(klass);
     }
     private void makeTypedProperties() {
         this.typedProperties = new HashMap<Class<? extends Object>,List<String>>();
         this.typedProperties.put(Integer.class, new ArrayList<String>());
         this.typedProperties.put(Float.class, new ArrayList<String>());
         this.typedProperties.put(String.class, new ArrayList<String>());
         for(String key: headers.keySet()){
             Header header = headers.get(key);
             if(header.parseCount>0){
                 for(Class<? extends Object> klass:KNOWN_PROPERTY_TYPES){
                     if(header.knownType() == klass){
                         this.typedProperties.get(klass).add(header.key);
                     }
                 }
             }
         }
     }
     
     protected final LinkedHashMap<String, Object> makeDataMap(String[] fields) {
         LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
         for (String key : headers.keySet()) {
             try {
                 Header header = headers.get(key);
                 String field = fields[header.index];
                 if(field == null || field.length()<1 || field.equals("?")){
                     continue;
                 }
                 Object value = header.parse(field);
                 map.put(key, value); // TODO: Decide if we should actually use the name here
                 
                 //Now speed up parsing once we are certain of the column types
                 if (header.shouldConvert()) {
                     Class<? extends Object> klass = header.knownType();
                     if (klass == Integer.class) {
                         headers.put(key, new IntegerHeader(header));
                     } else if (klass == Float.class) {
                         headers.put(key, new FloatHeader(header));
                     } else {
                         headers.put(key, new StringHeader(header));
                     }
                 }
             } catch (Exception e) {
                 // TODO Handle Exception
             }
         }
         return map;
     }
 
     protected final int i_of(String header){
         debug("Looking up header index for "+header);
         return headers.get(header).index;
     }
     
     protected final String status(){
         if(started<=0) started = System.currentTimeMillis();
         return (line_number>0 ? "line:"+line_number : ""+((System.currentTimeMillis()-started)/1000.0)+"s");
     }
 
     private final void runInDisplay(Runnable runnable) {
         if(display!=null){
             PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
         } else {
             runnable.run();
         }
     }
     
     protected final void debug(final String line){
         runInDisplay(new Runnable() {
             public void run() {
                 NeoLoaderPlugin.debug(typeName + ":"+basename+":"+status()+": "+line);
             }
         });
     }
     
     protected final void info(final String line){
         runInDisplay(new Runnable() {
             public void run() {
                 NeoLoaderPlugin.notify(typeName + ":"+basename+":"+status()+": "+line);
             }
         }); 
     }
     
     protected final void notify(final String line){
         runInDisplay(new Runnable() {
             public void run() {
                 NeoLoaderPlugin.notify(typeName + ":"+basename+":"+status()+": "+line);
             }
         });     
     }
     
     protected final void error(final String line){
         runInDisplay(new Runnable() {
             public void run() {
                 NeoLoaderPlugin.notify(typeName + ":"+basename+":"+status()+": "+line);
             }
         }); 
     }
 
     public void setLimit(int value){
         this.limit = value;
     }
 
     protected final void addToMap() throws MalformedURLException {
         if (gis != null && neoProvider != null) {
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
                 NeoCorePlugin.getDefault().getProjectService().addDriveToProject(LoaderUtils.getAweProjectName(), mainNode);
             } finally {
                 transaction.finish();
 //                if(neoProvider!=null){
 //                    neoProvider.commit();
 //                }
             }
         }
         if(neoProvider!=null){
             String databaseLocation = neoProvider.getDefaultDatabaseLocation();
             NeoCorePlugin.getDefault().getUpdateDatabaseManager()
                     .fireUpdateDatabase(new UpdateDatabaseEvent(UpdateDatabaseEventType.GIS));
             ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
             URL url = new URL("file://" + databaseLocation);
             List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(url);
             for (IService service : services) {
                 if (catalog.getById(IService.class, service.getIdentifier(), new NullProgressMonitor()) != null) {
                     catalog.replace(service.getIdentifier(), service);
                 } else {
                     catalog.add(service);
                 }
             }
             if (dataset == null) {
                 DriveDialog.addGisToMap(gis);
             }
         }
     }
     
     public Node getGisNode() {
         return gis;
     }
 
     protected final void addStats(int pn_code, int ec_io) {
         if(!stats.containsKey(pn_code)) this.stats.put(pn_code,new int[2]);
         stats.get(pn_code)[0]+=1;
         stats.get(pn_code)[1]+=ec_io;
     }
 
     protected final void checkCRS(String[] latlon) {
         if (crs == null) {
             crs = CRS.fromLocation(Float.parseFloat(latlon[0]), Float.parseFloat(latlon[1]), null);
             file.setProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, crs.getType());
             file.setProperty(INeoConstants.PROPERTY_CRS_NAME, crs.toString());
             gis.setProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, crs.getType());
             gis.setProperty(INeoConstants.PROPERTY_CRS_NAME, crs.toString());
         }
     }
 
     protected final void updateBBox(double lat, double lon) {
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
     }
 
     protected final void findOrCreateFileNode(Node mp) {
         if (file == null) {
             Node reference = neo.getReferenceNode();
             datasetNode = findOrCreateDatasetNode(neo.getReferenceNode(), dataset);
             file = findOrCreateFileNode(reference, datasetNode);
 
             Node mainFileNode = datasetNode == null ? file : datasetNode;
             file.createRelationshipTo(mp, GeoNeoRelationshipTypes.NEXT);
             gis = getGISNode(neo, mainFileNode);
             bbox = (double[])gis.getProperty(INeoConstants.PROPERTY_BBOX_NAME, bbox);
 
             debug("Added '" + mp.getProperty(INeoConstants.PROPERTY_TIME_NAME) + "' as first measurement of '"
                     + file.getProperty(INeoConstants.PROPERTY_FILENAME_NAME));
         }
     }
 
     protected final void saveProperties() {
         if (gis != null) {
             Transaction transaction = neo.beginTx();
             try {
                 Node propNode;
                 Relationship propRel = gis.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
                 if(propRel==null){
                     propNode = neo.createNode();
                     propNode.setProperty("name", NeoUtils.getNodeName(gis));
                     propNode.setProperty("type", "gis_properties");
                     gis.createRelationshipTo(propNode, GeoNeoRelationshipTypes.PROPERTIES);
                 }else{
                     propNode = propRel.getEndNode();
                 }
                 HashMap<String,Node> propTypeNodes = new HashMap<String,Node>();
                 for(Node node: propNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)){
                     propTypeNodes.put(node.getProperty("name").toString(), node);
                 }
                 for(Class<? extends Object> klass:KNOWN_PROPERTY_TYPES){
                     String typeName = makePropertyTypeName(klass);
                     List<String> properties = getProperties(klass);
                     if(properties!=null && properties.size()>0){
                         Node propTypeNode = propTypeNodes.get(typeName);
                         if(propTypeNode==null){
                             propTypeNode = neo.createNode();
                             propTypeNode.setProperty(INeoConstants.PROPERTY_NAME_NAME, typeName);
                             propTypeNode.setProperty(INeoConstants.PROPERTY_TYPE_NAME, "gis_property_type");
                             savePropertiesToNode(propTypeNode,properties);
                             propNode.createRelationshipTo(propTypeNode, GeoNeoRelationshipTypes.CHILD);
                         } else {
                             TreeSet<String> combinedProperties = new TreeSet<String>();
                             String[] previousProperties = (String[])propTypeNode.getProperty(INeoConstants.NODE_TYPE_PROPERTIES, null);
                             if (previousProperties != null)
                                 combinedProperties.addAll(Arrays.asList(previousProperties));
                             combinedProperties.addAll(properties);
                             savePropertiesToNode(propTypeNode, combinedProperties);
                         }
                     }
                 }
                 transaction.success();
             } finally {
                 transaction.finish();
             }
         }
     }
     
     private void savePropertiesToNode(Node propTypeNode, Collection<String> properties) {
         propTypeNode.setProperty("properties", properties.toArray(new String[properties.size()]));
         HashMap<String,Node> valueNodes = new HashMap<String,Node>();
         for(Relationship relation: propTypeNode.getRelationships(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING)){
             Node valueNode = relation.getEndNode();
             String property = relation.getProperty("property", "").toString();
             valueNodes.put(property, valueNode);
         }
         for(String property: properties){
             Node valueNode = valueNodes.get(property);
             Header header = headers.get(property);
             HashMap<Object,Integer> values = header.values;
             if(values==null) {
                 if(valueNode != null){
                     for(Relationship relation: valueNode.getRelationships()){
                         relation.delete();
                     }
                     valueNode.delete();
                 }
             }else{
                 if(valueNode == null){
                     valueNode = neo.createNode();
                     Relationship relation = propTypeNode.createRelationshipTo(valueNode, GeoNeoRelationshipTypes.PROPERTIES);
                     relation.setProperty("property", property);
                 } else {
                     for(Object key: valueNode.getPropertyKeys()) {
                         Integer oldCount = (Integer)valueNode.getProperty(key.toString(),null);
                         if(oldCount == null) {
                             oldCount = 0;
                         }
                         Integer newCount = values.get(key);
                         if(newCount == null) {
                             newCount = 0;
                         }
                         values.put(key,oldCount + newCount);
                     }
                 }
                 for(Object key: values.keySet()) {
                     valueNode.setProperty(key.toString(), values.get(key));
                 }
             }
         }
     }
     
     public static String makePropertyTypeName(Class<? extends Object> klass){
         return klass.getName().replaceAll("java.lang.", "").toLowerCase();
     }
 
     /**
      * gets GIS node for
      * 
      * @param neo neo serice
      * @param mainNode main drive node
      * @return gis node for mainNode
      */
     protected final static Node getGISNode(NeoService neo, Node mainNode) {
         Node gis = null;
         Transaction transaction = neo.beginTx();
         try {
             Node reference = neo.getReferenceNode();
             for (Relationship relationship : mainNode.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.INCOMING)) {
                 Node node = relationship.getStartNode();
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(INeoConstants.GIS_TYPE_NAME)
                         && node.hasProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME).toString().equals(GisTypes.DRIVE.getHeader())){
                     if(!node.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator().hasNext()) {
                         node.createRelationshipTo(mainNode, GeoNeoRelationshipTypes.NEXT);
                     }
                     return node;
                 }
             }
             gis = neo.createNode();
             gis.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.GIS_TYPE_NAME);
             gis.setProperty(INeoConstants.PROPERTY_NAME_NAME, NeoUtils.getNodeName(mainNode));
             gis.setProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, GisTypes.DRIVE.getHeader());
             reference.createRelationshipTo(gis, NetworkRelationshipTypes.CHILD);
             gis.createRelationshipTo(mainNode, GeoNeoRelationshipTypes.NEXT);
             transaction.success();
         } finally {
             transaction.finish();
         }
         return gis;
     }
 
     /**
      * Finds or create if not exist necessary file node
      * 
      * @param root root node
      * @param datasetNode dataset node
      * @return
      */
     protected final Node findOrCreateFileNode(Node root, Node datasetNode) {
         ReturnableEvaluator fileReturnableEvaluator=new ReturnableEvaluator() {
             
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 final Node currentNode = currentPos.currentNode();
                 return NeoUtils.isFileNode(currentNode) && basename.equals(NeoUtils.getNodeName(currentNode));
             }
         };
         Traverser fileNodeTraverser;
         if (datasetNode!=null){
             fileNodeTraverser=datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, fileReturnableEvaluator, GeoNeoRelationshipTypes.NEXT,Direction.OUTGOING);
         }else{
             fileNodeTraverser = root.traverse(Order.DEPTH_FIRST, NeoUtils.getStopEvaluator(2), fileReturnableEvaluator,
                     GeoNeoRelationshipTypes.NEXT,
                     Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
         }
         final Iterator<Node> iterator = fileNodeTraverser.iterator();
         if (iterator.hasNext()) {
             return iterator.next();
         }
         Node result = neo.createNode();
         result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.FILE_TYPE_NAME);
         result.setProperty(INeoConstants.PROPERTY_NAME_NAME, basename);
         result.setProperty(INeoConstants.PROPERTY_FILENAME_NAME, filename);
         if (datasetNode != null) {
             datasetNode.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
         }
         return result;
     }
 
     /**
      * Finds or create if not exist necessary dataset node
      * 
      * @param root root node
      * @param datasetName name of dataset node
      * @return dataset node
      */
     protected final Node findOrCreateDatasetNode(Node root, final String datasetName) {
         Transaction tx = neo.beginTx();
         Node result;
         try {
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
 
     protected final static String propertiesString(Node node){
         StringBuffer properties = new StringBuffer();
         for(String property:node.getPropertyKeys()) {
             if(properties.length()>0) properties.append(", ");
             properties.append(property).append(" => ").append(node.getProperty(property));
             if(properties.length()>80) {
                 properties.append("...");
                 break;
             }
         }
         return properties.toString();
     }
 
     private void printHeaderStats() {
         notify("Determined Columns:");
         for(String key: headers.keySet()){
             Header header = headers.get(key);
             if(header.parseCount>0){
                 notify("\t"+header.knownType()+" loaded: "+header.parseCount+" => "+key);
             }
         }
     }
 
     public void printStats(boolean verbose) {
         printHeaderStats();
         long taken = System.currentTimeMillis() - started;
         addTimes(taken);
         notify("Finished loading " + basename + " data in " + (taken / 1000.0) + " seconds");
         notify("Read " + (line_number - 1) + " data lines and then filtered down to:");
         notify("\t" + countValidMessage + " with valid messages");
         notify("\t" + countValidLocation + " with known locations");
         notify("\t" + countValidChanged + " with changed data");
         notify("Read " + stats.keySet().size() + " unique PN codes:");
         for (int pn_code : stats.keySet()) {
             int[] pn_counts = stats.get(pn_code);
             notify("\t" + pn_code + " measured " + pn_counts[0] + " times (average Ec/Io = " + pn_counts[1] / pn_counts[0] + ")");
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
             if(ms.hasProperty(INeoConstants.PRPOPERTY_CHANNEL_NAME)){
                 sb.append(ms.getProperty(INeoConstants.PRPOPERTY_CHANNEL_NAME)).append(":");
                 sb.append(ms.getProperty(INeoConstants.PROPERTY_CODE_NAME)).append("=");
                 sb.append((ms.getProperty(INeoConstants.PROPERTY_DBM_NAME).toString()+"000000").substring(0,6));
             }else{
                 sb.append(propertiesString(ms));
             }
         }
         return sb.toString();       
     }
     private void printMeasurements(Node file){
         if (file == null)
             return;
         Transaction transaction = neo.beginTx();
         try {
             int count = 0;
             MEASUREMENTS: for (Relationship relationship : file.getRelationships(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)) {
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
                     count++;
                     if(count>20) {
                         notify("Exiting statistics after 100 measurement points");
                         break MEASUREMENTS;
                     }
                 }
             }
             transaction.success();
         } finally {
             transaction.finish();
         }
     }
 
     /**
      * Initialize the accumulating times stats. Do this at the beginning of a block of loading many
      * files and then at the end call the method printTimesStats to see the results.
      */
     public void initiTimes() {
         times = new int[2];
     }
 
     private static int addTimes(long taken) {
         times[0] += 1;
         times[1] += taken;
         return times[0];
     }
 
     public static void printTimesStats() {
         System.err.println("Finished " + times[0] + " loads in " + times[1] / 60000.0 + " minutes (average "
                 + (times[1] / times[0]) / 1000.0 + " seconds per load)");
     }
 
     protected int incValidMessage() {
         return ++countValidMessage;
     }
 
     protected int incValidLocation() {
         return ++countValidLocation;
     }
 
     protected int incValidChanged() {
         return ++countValidChanged;
     }
 
     protected boolean isOverLimit() {
         return limit > 0 && countValidChanged > limit;
     }
 
     /**
      * This is the main method of the class. It opens the file, iterates over the contents and calls
      * parseLine(String) on each line. The subclass needs to implement parseLine(String) to
      * interpret the data and save it to the database.
      * 
      * @param monitor
      * @throws IOException
      */
     public void run(IProgressMonitor monitor) throws IOException {
         if (monitor != null)
             monitor.subTask(filename);
         CountingFileInputStream is = new CountingFileInputStream(new File(filename));
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         mainTx = neo.beginTx();
         try {
             int perc = is.percentage();
             int prevPerc = 0;
             int prevLineNumber = 0;
             String line;
             while ((line = reader.readLine()) != null) {
                 line_number++;
                 if (!haveHeaders())
                     parseHeader(line);
                 else
                     parseLine(line);
                 if (monitor != null) {
                     if (monitor.isCanceled())
                         break;
                     perc = is.percentage();
                     if (perc > prevPerc) {
                         monitor.subTask(filename+":" + line_number + " ("+perc+"%)");
                         monitor.worked(perc - prevPerc);
                         prevPerc = perc;
                     }
                 }
                 // Commit external transaction on large blocks of code
                 if(line_number > prevLineNumber + 10000) {
                     commit(true);
                     prevLineNumber = line_number;
                 }
             }
         } finally {
             commit(true);
             reader.close();
             finishUp();
             addToMap();
             commit(false);
             mainTx = null;
         }
     }
 
     protected void commit(boolean restart){
         if(mainTx!=null) {
             mainTx.success();
             mainTx.finish();
             //System.out.println("Commit: Memory: "+(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
             if(restart){
                 mainTx = neo.beginTx();
             }else{
                 mainTx = null;
             }
         }
     }
     
     /**
      * This method must be implemented by all readers to parse the data lines. It might save data
      * directly to the database, or it might keep it in a cache for saving later, in the finishUp
      * method. A common pattern is to block data into chunks, saving these to the database at
      * reasonable points, and then using finishUp() to save any remaining data.
      * 
      * @param line
      */
     protected abstract void parseLine(String line);
     
     /**
      * After all lines have been parsed, this method is called, allowing the implementing class the
      * opportunity to save any cached information, or write any final statistics. It is not abstract
      * because it is possible, or even probable, to write an importer that does not need it.
      */
     protected void finishUp() {
         saveProperties();
     }
 
     @Override
     public void onNeoStop(Object source) {
         unregisterNeoManager();        
     }
     
     //Lagutko 21.07.2009, using of neo.core plugin
     private void unregisterNeoManager(){        
 //        neoProvider.commit();
         neoProvider.removeServiceProviderListener(this);        
     }
 
 }
