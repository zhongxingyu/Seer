 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.CallProperties.CallResult;
 import org.amanzi.neo.core.enums.CallProperties.CallType;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.loader.NetworkLoader.CRS;
 import org.amanzi.neo.loader.ams.parameters.AMSCommandParameters;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.loader.sax_parsers.AbstractTag;
 import org.amanzi.neo.loader.sax_parsers.IXmlTag;
 import org.amanzi.neo.loader.sax_parsers.IXmlTagFactory;
 import org.amanzi.neo.loader.sax_parsers.PropertyCollector;
 import org.amanzi.neo.loader.sax_parsers.ReadContentHandler;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Traverser;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 // TODO: Auto-generated Javadoc
 /**
  * <p>
  * AMS XML Loader
  * </p>
  * .
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class AMSXMLoader extends AbstractCallLoader {
 
     /** String CALL_MP_KEY field */
     public static final String CALL_MP_KEY = "call";
 
     /** TOC-TTC call. */
     private AMSCall tocttc;
 
     /** The tocttc group. */
     private AMSCall tocttcGroup;
 
     /** The msg call. */
     private AMSCall msgCall;
 
     /** The Constant subNodes. */
     protected final static Map<String, Class< ? extends AbstractEvent>> subNodes = new HashMap<String, Class< ? extends AbstractEvent>>();
 
     /** The Constant EMER_PRIORITY. */
     public static final Integer EMER_PRIORITY = 15;
     /** Initialize events map */
     static {
         subNodes.put("itsiAttach", ItsiAttach.class);
         subNodes.put("cellResel", CellResel.class);
         subNodes.put("handover", Handover.class);
         subNodes.put("groupAttach", GroupAttach.class);
         subNodes.put("toc", Toc.class);
         subNodes.put("ttc", Ttc.class);
         subNodes.put("tpc", Tpc.class);
         subNodes.put("sendMsg", SendMsg.class);
         subNodes.put("receiveMsg", ReceiveMsg.class);
     }
     /** The LOGGER. */
     public static Logger LOGGER = Logger.getLogger(AMSXMLoader.class);
 
     /** The formatter. */
     // TODO temporary remove handling of time zone
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");// ,SSSz");
 
     /** The directory name. */
     private final String directoryName;
 
     /** The network name. */
     private String networkName;
 
     /** The last dataset node. */
     private Node lastDatasetNode;
 
     /** The in. */
     private CountingFileInputStream in;
 
     /** The handler. */
     private final ContentHandler handler;
 
     /** The probe cache. */
     private final Map<String, Node> probeCache = new HashMap<String, Node>();
 
     /** The probe call cache. */
     private final Map<String, Node> probeCallCache = new HashMap<String, Node>();
 
     /** The phone number cache. */
     private final Map<String, String> phoneNumberCache = new HashMap<String, String>();
 
     /** The group call. */
     private final Map<String, Set<String>> groupCall = new HashMap<String, Set<String>>();
     private final Map<String, Set<Call>> sortedCall = new HashMap<String, Set<Call>>();
 
     /** active file node for event dataset. */
     private Node datasetFileNode;
 
     /** The is test. */
     private final boolean isTest;
 
     /** The event set. */
     private final Set<AbstractEvent> eventSet = new HashSet<AbstractEvent>();
 
     /** The gps set. */
     private final Map<String, Set<GPSData>> gpsMap = new HashMap<String, Set<GPSData>>();
 
     private Node datasetGis;
 
     private String calldatasetName;
 
     // TODO change after implement feature 1131
 
     /**
      * Gets the storing node.
      * 
      * @param key the key
      * @return the storing node
      */
     @Override
     protected Node getStoringNode(Integer key) {
         switch (key) {
         case REAL_DATASET_HEADER_INDEX:
             return datasetNode;
         case CALL_DATASET_HEADER_INDEX:
             return callDataset;
         case PROBE_NETWORK_HEADER_INDEX:
             return networkNode;
         default:
             return null;
         }
     }
 
     /**
      * Gets the prymary type.
      * 
      * @param key the key
      * @return the prymary type
      */
     @Override
     protected String getPrymaryType(Integer key) {
         switch (key) {
         case REAL_DATASET_HEADER_INDEX:
             return NodeTypes.M.getId();
         case CALL_DATASET_HEADER_INDEX:
             return NodeTypes.CALL.getId();
         case PROBE_NETWORK_HEADER_INDEX:
             return NodeTypes.PROBE.getId();
         default:
             return null;
         }
     }
 
     /**
      * Need parce headers.
      * 
      * @return true, if successful
      */
     @Override
     protected boolean needParceHeaders() {
         return false;
     }
 
     /**
      * Parses the line.
      * 
      * @param line the line
      */
     @Override
     protected void parseLine(String line) {
         // do nothing
     }
 
     /**
      * Parse string to timestamp.
      * 
      * @param stringData the string data format
      * @return the time
      * @throws ParseException the parse exception
      */
     private Long getTime(String stringData) throws ParseException {
         if (stringData == null) {
             return null;
         }
         int i = stringData.lastIndexOf(':');
         StringBuilder time = new StringBuilder(stringData.substring(0, i)).append(stringData.substring(i + 1, stringData.length()));
         long time2 = formatter.parse(time.toString()).getTime();
         return time2;
     }
 
     /**
      * Instantiates a new aMS loader from xml.
      * 
      * @param directoryName the directory name
      * @param display the display
      * @param datasetName the dataset name
      * @param networkName the network name
      */
     public AMSXMLoader(String directoryName, Display display, String datasetName, String networkName) {
         driveType = DriveTypes.AMS;
         handler = new ReadContentHandler(new Factory());
         if (datasetName == null) {
             int startIndex = directoryName.lastIndexOf(File.separator);
             if (startIndex < 0) {
                 startIndex = 0;
             } else {
                 startIndex++;
             }
             datasetName = directoryName.substring(startIndex);
         }
 
         this.directoryName = directoryName;
         this.filename = directoryName;
         this.networkName = networkName;
         isTest = false;
         initialize("AMS", null, directoryName, display, datasetName);
 
         // timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
     }
 
     /**
      * Instantiates a new aMSXM loader.
      * 
      * @param directoryName the directory name
      * @param display the display
      * @param datasetName the dataset name
      * @param networkName the network name
      * @param neo the neo
      * @param isTest the is test
      */
     public AMSXMLoader(String directoryName, Display display, String datasetName, String networkName, GraphDatabaseService neo, boolean isTest) {
         driveType = DriveTypes.AMS;
         handler = new ReadContentHandler(new Factory());
         if (datasetName == null) {
             int startIndex = directoryName.lastIndexOf(File.separator);
             if (startIndex < 0) {
                 startIndex = 0;
             } else {
                 startIndex++;
             }
             datasetName = directoryName.substring(startIndex);
         }
 
         this.directoryName = directoryName;
         this.filename = directoryName;
         this.networkName = networkName;
         this.isTest = isTest;
         initialize("AMS", neo, directoryName, display, datasetName);
     }
 
     /**
      * Adds the drive indexes.
      */
     private void addDriveIndexes() {
         try {
             addIndex(NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(dataset));
             addMappedIndex(CALL_MP_KEY, NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(DriveTypes.AMS_CALLS.getFullDatasetName(dataset)));
             addIndex(NodeTypes.M.getId(), NeoUtils.getTimeIndexProperty(dataset));
             addIndex(NodeTypes.CALL.getId(), NeoUtils.getTimeIndexProperty(DriveTypes.AMS_CALLS.getFullDatasetName(dataset)));
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * Run.
      * 
      * @param monitor the monitor
      * @throws IOException Signals that an I/O exception has occurred.
      */
     @Override
     public void run(IProgressMonitor monitor) throws IOException {
         monitor.beginTask("Loading AMS data", 2);
         monitor.subTask("Searching for files to load");
         List<File> allFiles = LoaderUtils.getAllFiles(directoryName, new FileFilter() {
 
             @Override
             public boolean accept(File pathname) {
                 return pathname.isDirectory() || LoaderUtils.getFileExtension(pathname.getName()).equalsIgnoreCase(".xml");
             }
         });
 
         monitor = SubMonitor.convert(monitor, allFiles.size());
         monitor.beginTask("Loading AMS data", allFiles.size());
         lastDatasetNode = null;
         Transaction tx = neo.beginTx();
         try {
             initializeNetwork(networkName);
             initializeDatasets(dataset);
             addDriveIndexes();
             initializeIndexes();
             long count = 0;
             long time = System.currentTimeMillis();
             for (File logFile : allFiles) {
                 if (monitor.isCanceled()) {
                     return;
                 }
                 monitor.subTask("Loading file " + logFile.getAbsolutePath());
                 try {
                     handleFile(logFile, monitor);
                 } catch (Exception e) {
                     // TODO Handle SAXException
                     throw (RuntimeException)new RuntimeException().initCause(e);
                 }
                 if (count++ > 10) {
                     commit(tx);
                 }
                 monitor.worked(1);
             }
             time = System.currentTimeMillis() - time;
             LOGGER.info(new StringBuilder("total handle time ").append(time).toString());
             saveProperties();
             finishUpIndexes();
             finishUp();
 
             cleanupGisNode();
             if (!isTest) {
                 finishUpGis();
             }
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Handle file.
      * 
      * @param singleFile the file
      * @param monitor the monitor
      * @throws SAXException the sAX exception
      * @throws IOException Signals that an I/O exception has occurred.
      */
     private void handleFile(File singleFile, IProgressMonitor monitor) throws SAXException, IOException {
         if (monitor.isCanceled()) {
             return;
         }
         eventSet.clear();
         gpsMap.clear();
         tocttc = null;
         tocttcGroup = null;
         lastDatasetNode = null;
         datasetFileNode = NeoUtils.findOrCreateFileNode(neo, datasetNode, singleFile.getName(), singleFile.getPath()).getRight();
         XMLReader rdr = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
         rdr.setContentHandler(handler);
         in = new CountingFileInputStream(singleFile);
         rdr.parse(new InputSource(new BufferedInputStream(in, 64 * 1024)));
         if (tocttcGroup != null) {
             saveCall(tocttcGroup);
             tocttcGroup = null;
         }
         if (tocttc != null) {
             saveCall(tocttc);
             tocttc = null;
         }
         if (msgCall != null) {
             saveCall(msgCall);
             msgCall = null;
         }
         groupCall.clear();
 
         handleLocations();
         sortedCall.clear();
     }
 
     /**
      * Handle locations.
      */
     private void handleLocations() {
         if (gpsMap.isEmpty() || eventSet.isEmpty()) {
             return;
         }
         Map<String, Set<AbstractEvent>> sortedEvent = new HashMap<String, Set<AbstractEvent>>();
         for (AbstractEvent event : eventSet) {
             if (StringUtils.isEmpty(event.probeId)) {
                 continue;
             }
             Set<AbstractEvent> set = sortedEvent.get(event.probeId);
             if (set == null) {
                 set = new HashSet<AbstractEvent>();
                 sortedEvent.put(event.probeId, set);
             }
             set.add(event);
         }
         Transaction tx = neo.beginTx();
         try {
             for (Map.Entry<String, Set<GPSData>> entry : gpsMap.entrySet()) {
                 Set<AbstractEvent> evSet = sortedEvent.get(entry.getKey());
                 Set<Call> callSet = sortedCall.get(entry.getKey());
                 createLocations(entry, evSet, dataset);
                 createLocations(entry, callSet, calldatasetName);
             }
             tx.success();
         } finally {
             tx.finish();
         }
     }
 
     /**
      *
      * @param entry
      * @param evSet
      */
     private void createLocations(Map.Entry<String, Set<GPSData>> entry, Set< ? extends IAdaptable> evSet, String dataset) {
         if (evSet != null) {
             if (entry.getValue().size() == 1) {
                 createOneMP(entry.getValue().iterator().next(), evSet, dataset);
             } else {
                 Pair<GPSData, GPSData> minMaxOnTime = getMinMaxGpsByTime(entry.getValue());
                 GPSData min = minMaxOnTime.getLeft();
                 GPSData max = minMaxOnTime.getRight();
                 if (min.timestamp == max.timestamp) {
                     createOneMP(min, evSet, dataset);
                 } else {
                     long difTime = max.timestamp - min.timestamp;
                     double aLat = (max.lat - min.lat) / difTime;
                     double aLon = (max.lon - min.lon) / difTime;
                     double bLat = min.lat - aLat * min.timestamp;
                     double bLon = min.lon - aLon * min.timestamp;
                     for (IAdaptable event : evSet) {
                         Node node = (Node)event.getAdapter(Node.class);
                         Long timestamp = (Long)event.getAdapter(Long.class);
                         if (node != null && timestamp != null) {
                             Node mp = neo.createNode();
                             NeoUtils.setNodeName(mp, "mp", neo);
                             double lat = aLat * timestamp + bLat;
                             double lon = aLon * timestamp + bLon;
                             mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, lat);
                             mp.setProperty(INeoConstants.PROPERTY_LON_NAME, lon);
                             mp.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
                             NodeTypes.MP.setNodeType(mp, null);
                             GisProperties gisProperties = getGisProperties(dataset);
                             gisProperties.updateBBox(lat, lon);
                             gisProperties.incSaved();
                             if (this.dataset.equals(dataset)) {
                                 index(mp);
                             } else {
                                 assert DriveTypes.AMS_CALLS.getFullDatasetName(this.dataset).equals(dataset);
                                 index(CALL_MP_KEY, mp);
                             }
                             node.createRelationshipTo(mp, GeoNeoRelationshipTypes.LOCATION);
                             // TODO add call location
                         }
                     }
                 }
             }
         }
     }
 
     private void createOneMP(GPSData gpsData, Set< ? extends IAdaptable> evSet, String dataset) {
         Node mp = neo.createNode();
         NeoUtils.setNodeName(mp, "mp", neo);
         gpsData.store(mp);
         GisProperties gisProperties = getGisProperties(dataset);
         gisProperties.updateBBox(gpsData.lat, gpsData.lon);
         gisProperties.incSaved();
         if (this.dataset.equals(dataset)) {
             index(mp);
         } else {
             assert DriveTypes.AMS_CALLS.getFullDatasetName(this.dataset).equals(dataset);
             index(CALL_MP_KEY, mp);
         }
         for (IAdaptable event : evSet) {
             Node node = (Node)event.getAdapter(Node.class);
             if (node != null) {
                 node.createRelationshipTo(mp, GeoNeoRelationshipTypes.LOCATION);
                 // TODO add call location
             }
         }
     }
 
     /**
      * @param gpsSet2
      * @return
      */
     private Pair<GPSData, GPSData> getMinMaxGpsByTime(Set<GPSData> gpsSet) {
         GPSData min = null;
         GPSData max = null;
         for (GPSData data : gpsSet) {
             if (min == null || min.timestamp > data.timestamp) {
                 min = data;
             }
             if (max == null || max.timestamp < data.timestamp) {
                 max = data;
             }
         }
         return new Pair<GPSData, GPSData>(min, max);
     }
 
     /**
      * Initializes Network for probes.
      * 
      * @param networkName name of network
      */
     private void initializeNetwork(String networkName) {
         String oldBasename = basename;
         if ((networkName == null) || (networkName.length() == 0)) {
             networkName = basename + " Probes";
         } else {
             networkName = networkName.trim();
         }
 
         basename = networkName;
         Node networkGis = findOrCreateGISNode(basename, GisTypes.NETWORK.getHeader(), NetworkTypes.PROBE);
         networkNode = findOrCreateNetworkNode(networkGis);
         // driveType=DriveTypes.PROBE;
         // networkNode = findOrCreateDatasetNode(neo.getReferenceNode(), networkName);
         // networkGis=findOrCreateGISNode(networkNode, GisTypes.DRIVE.getHeader());
         // getGisProperties(networkName).setCrs(CRS.fromCRS("geographic","EPSG:4326"));
         this.networkName = basename;
         basename = oldBasename;
         fillProbeMap(networkNode);
     }
 
     /**
      * Fill probe map.
      * 
      * @param networkNode the network node
      */
     private void fillProbeMap(Node networkNode) {
         Transaction tx = neo.beginTx();
         try {
             Traverser probeTraverser = NeoUtils.getChildTraverser(networkNode);
             for (Node probe : probeTraverser) {
                 String id = NeoUtils.getNodeName(probe);
                 probeCache.put(id, probe);
             }
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Initializes a Call dataset.
      * 
      * @param datasetName name of Real dataset
      */
     private void initializeDatasets(String datasetName) {
         Transaction tx = neo.beginTx();
         try {
             driveType = DriveTypes.AMS;
             datasetNode = findOrCreateDatasetNode(neo.getReferenceNode(), dataset);
             datasetGis = findOrCreateGISNode(datasetNode, GisTypes.DRIVE.getHeader());
             getGisProperties(dataset).setCrs(CRS.fromCRS("geographic", "EPSG:4326"));
             callDataset = getVirtualDataset(DriveTypes.AMS_CALLS, true);
             calldatasetName = DriveTypes.AMS_CALLS.getFullDatasetName(dataset);
             tx.success();
         } catch (Exception e) {
             tx.failure();
             NeoCorePlugin.error(null, e);
             throw new RuntimeException(e);
         } finally {
             tx.finish();
         }
     }
 
     /**
      * <p>
      * Abstract handler of one event
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public abstract class AbstractEvent extends PropertyCollector implements IAdaptable {
 
         /** The node. */
         protected Node node;
         protected String probeId;
 
 
         /** The defined values. */
         protected final Map<String, Class< ? extends Object>> definedValues;
 
         /** The parameter map. */
         protected final Map<String, AMSCommandParameters> parameterMap;
 
         /** The header. */
         protected LinkedHashMap<String, Header> header;
 
         /** The timestamp. */
         protected Long timestamp = null;
 
         protected Node lastMM = null;
 
         /**
          * Instantiates a new abstract event.
          * 
          * @param tagName the tag name
          * @param parent the parent
          * @param computeSubChild the compute sub child
          */
         public AbstractEvent(String tagName, IXmlTag parent, Boolean computeSubChild) {
             super(tagName, parent, computeSubChild);
             definedValues = new HashMap<String, Class< ? extends Object>>();
             parameterMap = new HashMap<String, AMSCommandParameters>();
             header = getHeaderMap(REAL_DATASET_HEADER_INDEX).headers;
             eventSet.add(this);
         }
 
         @Override
         public Object getAdapter(Class adapter) {
             if (adapter == Node.class) {
                 return node;
             } else if (adapter == Long.class) {
                 return timestamp;
             }
             return null;
         }
         /**
          * Check inclusive.
          * 
          * @param call the call
          */
         protected void checkInconclusive(Call call) {
             if (!call.isInclusive()) {
                 PropertyCollector isInclus = getSubCollectorByName("isInconclusive");
                 call.setInclusive(isInclus != null);
             }
         }
 
         /**
          * End element.
          * 
          * @param localName the local name
          * @param chars the chars
          * @return the i xml tag
          */
         @Override
         public IXmlTag endElement(String localName, StringBuilder chars) {
 
             if (openTag == null) {
                 Transaction tx = neo.beginTx();
                 try {
                     timestamp = null;
                     createEventChild();
                     handleCollector();
                     index(node);
 
                     tx.success();
                 } catch (Exception e) {
                     NeoLoaderPlugin.exception(e);
                     LOGGER.error("event parsed with exception:", e);
                 } finally {
                     tx.finish();
                 }
                 return parent;
             }
             propertyMap.put(localName, chars.toString());
             openTag = null;
             return this;
         }
 
         /**
          * Handle property store (stored property in neo Node).
          * 
          * @throws ParseException the parse exception
          */
         protected void handleCollector() throws ParseException {
 
             Map<String, String> map = getPropertyMap();
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 Pair<Object, Class< ? extends Object>> pair = getParcedValue(entry.getKey(), entry.getValue(), definedValues);
                 Object parsedValue = pair.getLeft();
                 Class< ? extends Object> klass = pair.getRight();
                 if (klass == Timestamp.class) {
                     timestamp = timestamp == null ? (Long)parsedValue : Math.min(timestamp, (Long)parsedValue);
                 }
                 AMSCommandParameters amsCommandParameters = parameterMap.get(entry.getKey());
                 if (amsCommandParameters != null) {
                     handleAMSCommand(amsCommandParameters, entry.getKey(), parsedValue);
                 } else {
                     setIndexProperty(header, node, entry.getKey(), parsedValue);
                 }
             }
             PropertyCollector isInclus = getSubCollectorByName("isInconclusive");
             if (isInclus != null) {
                 map = isInclus.getPropertyMap();
                 for (Map.Entry<String, String> entry : map.entrySet()) {
                     Object parsedValue;
                     if (entry.getKey().equals("errCode")) {
                         parsedValue = Integer.parseInt(entry.getValue());
                     } else {
                         parsedValue = entry.getValue();
                     }
                     setIndexProperty(header, node, entry.getKey(), parsedValue);
                 }
                 createAttachmentNode(isInclus);
             }
             if (timestamp != null) {
                 node.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
                 updateTimestampMinMax(REAL_DATASET_HEADER_INDEX, timestamp);
             }
             probeId = getPropertyMap().get("probeID");
         }
 
         /**
          * Handle ams command.
          * 
          * @param amsCommandParameters the ams command parameters
          * @param key the key
          * @param parsedValue the parsed value
          */
         protected void handleAMSCommand(AMSCommandParameters amsCommandParameters, String key, Object parsedValue) {
             setIndexProperty(header, node, amsCommandParameters.getName(), amsCommandParameters.convert(parsedValue));
         }
 
         /**
          * Gets the parced value.
          * 
          * @param key the key
          * @param value the value
          * @param castMap the cast map
          * @return the parced value
          * @throws ParseException the parse exception
          */
         protected Pair<Object, Class< ? extends Object>> getParcedValue(String key, String value, Map<String, Class< ? extends Object>> castMap)
                 throws ParseException {
 
             Class< ? extends Object> klass = castMap.get(key);
             if (klass == null) {
                 klass = String.class;
             }
             Object parsedValue = null;
             if (klass == String.class) {
                 parsedValue = value;
             } else if (klass == Double.class) {
                 parsedValue = Double.parseDouble(value);
             } else if (klass == Integer.class) {
                 parsedValue = Integer.parseInt(value);
             } else if (klass == Timestamp.class) {
                 parsedValue = getTime(value);
             }
             return new Pair<Object, Class< ? extends Object>>(parsedValue, klass);
         }
 
         /**
          * create new event node.
          */
         protected void createEventChild() {
             node = neo.createNode();
             NodeTypes.M.setNodeType(node, neo);
             NeoUtils.addChild(datasetFileNode, node, lastDatasetNode, neo);
             lastDatasetNode = node;
             setNewIndexProperty(header, node, INeoConstants.PROPERTY_NAME_NAME, getClass().getSimpleName());
         }
 
         /**
          * Creates the attachment node.
          * 
          * @param collector the collector
          * @throws ParseException the parse exception
          */
         private void createAttachmentNode(PropertyCollector collector) throws ParseException {
             Node mm = neo.createNode();
             NeoUtils.addChild(node, mm, lastMM, neo);
             NodeTypes.MM.setNodeType(mm, neo);
             mm.setProperty(INeoConstants.PROPERTY_NAME_NAME, "ntp");
             lastMM = mm;
             Map<String, String> map = collector.getPropertyMap();
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 Object parsedValue = getParcedValue(entry.getKey(), entry.getValue(), definedValues).getLeft();
                 if (entry.getKey().equals("errCode")) {
                     parsedValue = Integer.parseInt(entry.getValue());
                 } else {
                     parsedValue = entry.getValue();
                 }
                 setIndexProperty(header, mm, entry.getKey(), parsedValue);
             }
         }
 
     }
 
     /**
      * <p>
      * Handler ItsiAttach tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class ItsiAttach extends AbstractEvent {
 
         /**
          * Instantiates a new itsi attach.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public ItsiAttach(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("itsiAtt_Req", Timestamp.class);
             definedValues.put("itsiAtt_Accept", Timestamp.class);
             definedValues.put("locationAreaBefore", Integer.class);
             definedValues.put("locationAreaAfter", Integer.class);
             definedValues.put("errorCode", Integer.class);
         }
 
         /**
          * Handle collector.
          * 
          * @throws ParseException the parse exception
          */
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
             handleCall();
         }
 
         /**
          * Handle call.
          */
         private void handleCall() {
             Call call = new Call();
             call.addRelatedNode(node);
             call.setCallType(CallType.ITSI_ATTACH);
             Long beginTime = (Long)node.getProperty("itsiAtt_Req", null);
             if (beginTime != null) {
                 call.setCallSetupBeginTime(beginTime);
                 call.setCallSetupEndTime(beginTime);
             }
             Long endTime = (Long)node.getProperty("itsiAtt_Accept", null);
             if (endTime != null) {
                 call.setCallTerminationBegin(endTime);
                 call.setCallTerminationEnd(endTime);
             }
             if (node.hasProperty("errorCode") || node.hasProperty("errCode")) {
                 call.setCallResult(CallResult.FAILURE);
             } else {
                 call.setCallResult(CallResult.SUCCESS);
             }
             final String probe = getPropertyMap().get("probeID");
             Node callerProbe = probeCallCache.get(probe);
             if (!StringUtils.isEmpty(probe)) {
                 Set<Call> callSet = sortedCall.get(probe);
                 if (callSet == null) {
                     callSet = new HashSet<Call>();
                     sortedCall.put(probe, callSet);
                 }
                 callSet.add(call);
             }
             call.setCallerProbe(callerProbe);
             call.addRelatedNode(node);
             checkInconclusive(call);
             saveCall(call);
         }
     }
 
     /**
      * <p>
      * Handler CellResel tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class CellResel extends AbstractEvent {
 
         /**
          * Instantiates a new cell resel.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public CellResel(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("cellReselReq", Timestamp.class);
             definedValues.put("cellReselAccept", Timestamp.class);
             definedValues.put("locationAreaBefore", Integer.class);
             definedValues.put("locationAreaAfter", Integer.class);
             definedValues.put("errorCode", Integer.class);
         }
 
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
             handleCall();
         }
 /**
  *
  */
 private void handleCall() {
     Call call = new Call();
     call.addRelatedNode(node);
     call.setTimestamp(timestamp);
     call.setCallType(CallType.ITSI_CC);
     Long beginTime = (Long)node.getProperty("cellReselReq", null);
     Long endTime = (Long)node.getProperty("cellReselAccept", null);
     if (node.hasProperty("errorCode") || node.hasProperty("errCode")) {
         call.setCallResult(CallResult.FAILURE);
     } else {
         call.setCallResult(CallResult.SUCCESS);
     }
     if (beginTime==null||endTime==null){
         call.setCallResult(CallResult.FAILURE);
         call.setReselectionTime(-1l);
     }else{
         call.setReselectionTime(endTime-beginTime);
         
     }
     String probe = getPropertyMap().get("probeID");
     Node callerProbe = probeCallCache.get(probe);
     if (!StringUtils.isEmpty(probe)) {
         Set<Call> callSet = sortedCall.get(probe);
         if (callSet == null) {
             callSet = new HashSet<Call>();
             sortedCall.put(probe, callSet);
         }
         callSet.add(call);
     }
     call.setCallerProbe(callerProbe);
     call.addRelatedNode(node);
     checkInconclusive(call);
     saveCall(call);
 }
     }
 
     /**
      * <p>
      * Handler handover tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class Handover extends AbstractEvent {
 
         /**
          * Instantiates a new cell resel.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public Handover(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("ho_Req", Timestamp.class);
             definedValues.put("ho_Accept", Timestamp.class);
             definedValues.put("locationAreaBefore", Integer.class);
             definedValues.put("locationAreaAfter", Integer.class);
             definedValues.put("errorCode", Integer.class);
         }
 
         /**
          * Handle collector.
          * 
          * @throws ParseException the parse exception
          */
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
             handleCall();
         }
 
         /**
          * Handle call.
          */
         private void handleCall() {
             Call call = new Call();
             call.addRelatedNode(node);
             call.setTimestamp(timestamp);
             call.setCallType(CallType.ITSI_HO);
             Long beginTime = (Long)node.getProperty("ho_Req", null);
             Long endTime = (Long)node.getProperty("ho_Accept", null);
             if (node.hasProperty("errorCode") || node.hasProperty("errCode")) {
                 call.setCallResult(CallResult.FAILURE);
             } else {
                 call.setCallResult(CallResult.SUCCESS);
             }
             if (beginTime==null||endTime==null){
                 call.setCallResult(CallResult.FAILURE);
                 call.setHandoverTime(-1l);
             }else{
                 call.setHandoverTime(endTime-beginTime);
                 
             }
             String probe = getPropertyMap().get("probeID");
             Node callerProbe = probeCallCache.get(probe);
             if (!StringUtils.isEmpty(probe)) {
                 Set<Call> callSet = sortedCall.get(probe);
                 if (callSet == null) {
                     callSet = new HashSet<Call>();
                     sortedCall.put(probe, callSet);
                 }
                 callSet.add(call);
             }
             call.setCallerProbe(callerProbe);
             call.addRelatedNode(node);
             checkInconclusive(call);
             saveCall(call);
         }
 
     }
 
     /**
      * <p>
      * Handler Ttc tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class Ttc extends AbstractEvent {
 
         /** The pesq cast map. */
         protected final Map<String, Class< ? extends Object>> pesqCastMap;
 
         /** The delay. */
         int delay = 0;
 
         /** The delay count. */
         int delayCount = 0;
         /** The last mm. */
         Node lastMM = null;
 
         /** The hook. */
         private Integer hook = null;
 
         /** The simplex. */
         private Integer simplex = null;
 
         /**
          * Instantiates a new cell resel.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public Ttc(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("hook", Integer.class);
             definedValues.put("simplex", Integer.class);
             definedValues.put("indicationTime", Timestamp.class);
             definedValues.put("answerTime", Timestamp.class);
             definedValues.put("connectTime", Timestamp.class);
             definedValues.put("disconnectTime", Timestamp.class);
             definedValues.put("releaseTime", Timestamp.class);
             definedValues.put("causeForTermination", Integer.class);
             definedValues.put("errorCode", Integer.class);
             pesqCastMap = new HashMap<String, Class< ? extends Object>>();
             pesqCastMap.put("sendSampleStart", Timestamp.class);
             pesqCastMap.put("pesq", Double.class);
             pesqCastMap.put("delay", Integer.class);
             parameterMap.put("hook", AMSCommandParameters.HOOK);
             parameterMap.put("simplex", AMSCommandParameters.SIMPLEX);
 
         }
 
         /**
          * Handle ams command.
          * 
          * @param amsCommandParameters the ams command parameters
          * @param key the key
          * @param parsedValue the parsed value
          */
         @Override
         protected void handleAMSCommand(AMSCommandParameters amsCommandParameters, String key, Object parsedValue) {
             super.handleAMSCommand(amsCommandParameters, key, parsedValue);
             // Field field = this.getClass().getField(key);
             // field.set(this, parsedValue);
             if (key.equals("hook")) {
                 hook = (Integer)parsedValue;
             } else if (key.equals("simplex")) {
                 simplex = (Integer)parsedValue;
             }
         }
 
         /**
          * Handle collector.
          * 
          * @throws ParseException the parse exception
          */
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
             List<PropertyCollector> collectorList = getSubCollectors();
             handleCall();
             delay = 0;
             delayCount = 0;
             for (PropertyCollector collector : collectorList) {
                 if (collector.getName().equals("pesqResult")) {
                     createAttachmentNode(collector);
                 }
             }
             if (delayCount > 0) {
                 delay = delay / delayCount;
                 if (tocttc != null) {
                     tocttc.addDelay(delay / 1000f);
                 }
                 if (tocttcGroup != null) {
                     tocttcGroup.addDelay(delay / 1000f);
                 }
             }
             if (tocttc != null && hook != null && simplex != null && hook == 0 && simplex == 0) {
                 saveCall(tocttc);
                 tocttc = null;
             }
         }
 
         /**
          * Handle call.
          */
         protected void handleCall() {
             if (hook == null || simplex == null) {
                 return;
             }
             assert hook.equals(simplex) : hook + "\t" + simplex;
             if (tocttc != null) {
                 if (hook == 0 && simplex == 0) {
                     tocttc.setCallSetupEndTime((Long)node.getProperty("connectTime", 0l));
                     tocttc.setCallTerminationEnd((Long)node.getProperty("releaseTime", 0l));
                     if (getPropertyMap().get("errorCode") != null) {
                         tocttc.setCallResult(CallResult.FAILURE);
                     }
                     tocttc.addRelatedNode(node);
                     probeId = phoneNumberCache.get(tocttc.getCalledPhoneNumber());
                     tocttc.addCalleeProbe(probeCallCache.get(probeId));
                     checkInconclusive(tocttc);
                 }
             } else if (tocttcGroup != null) {
                 if (hook == 1 && simplex == 1) {
                     // tocttcGroup.setCallSetupEndTime((Long)node.getProperty("connectTime", 0l));
                     tocttcGroup.setCallTerminationEnd((Long)node.getProperty("releaseTime", 0l));
                     if (getPropertyMap().get("errorCode") != null) {
                         tocttcGroup.setCallResult(CallResult.FAILURE);
                     }
                     Node calleeProbe = probeCallCache.get(getPropertyMap().get("probeID"));
                     if (calleeProbe == null) {
                         Set<String> probesIdSet = groupCall.get(tocttcGroup.getCalledPhoneNumber());
                         if (probesIdSet == null) {
                             LOGGER.error("Not found probe for TTC tag");
                             tocttcGroup.setCallResult(CallResult.FAILURE);
                         } else {
                             for (String probeId : probesIdSet) {
                                 Node probe = probeCache.get(probeId);
                                 if (probe == null) {
                                     LOGGER.error("Not found probe with id =" + probeId);
                                 } else {
                                     // TODO check documentation
                                     if (probe.equals(tocttcGroup.getCallerProbe())) {
                                         continue;
                                     }
 
                                     // get probes call node
                                     this.probeId = probeId;
                                     tocttcGroup.addCalleeProbe(probeCallCache.get(probeId));
                                 }
                             }
                         }
                     } else {
                         tocttcGroup.addCalleeProbe(calleeProbe);
                     }
                     tocttcGroup.addRelatedNode(node);
                     checkInconclusive(tocttcGroup);
 
                 }
             }
 
         }
 
         /**
          * Creates the attachment node.
          * 
          * @param collector the collector
          * @throws ParseException the parse exception
          */
         private void createAttachmentNode(PropertyCollector collector) throws ParseException {
             Node mm = neo.createNode();
             NeoUtils.addChild(node, mm, lastMM, neo);
             NodeTypes.MM.setNodeType(mm, neo);
             mm.setProperty(INeoConstants.PROPERTY_NAME_NAME, "pesq");
             lastMM = mm;
             Map<String, String> map = collector.getPropertyMap();
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 Object parseValue = getParcedValue(entry.getKey(), entry.getValue(), pesqCastMap).getLeft();
                 if (entry.getKey().equals("delay")) {
                     delay += ((Number)parseValue).intValue();
                     delayCount++;
                 }
                 if (entry.getKey().equals("pesq")) {
                     if (tocttc != null) {
                         tocttc.addLq(((Number)parseValue).floatValue());
                     }
                     if (tocttcGroup != null) {
                         tocttcGroup.addLq(((Number)parseValue).floatValue());
                     }
                 }
                 setProperty(mm, entry.getKey(), parseValue);
             }
         }
     }
 
     /**
      * <p>
      * Handler Toc tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class Toc extends AbstractEvent {
 
         /** The pesq cast map. */
         protected final Map<String, Class< ? extends Object>> pesqCastMap;
 
         /** The delay. */
         int delay = 0;
 
         /** The delay count. */
         int delayCount = 0;
         /** The last mm. */
         Node lastMM = null;
 
         /** The call. */
         AMSCall call = null;;
 
         /** The hook. */
         Integer hook = null;
 
         /** The simplex. */
         Integer simplex = null;
 
         /**
          * Instantiates a new cell resel.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public Toc(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("configTime", Timestamp.class);
             definedValues.put("setupTime", Timestamp.class);
             definedValues.put("connectTime", Timestamp.class);
             definedValues.put("disconnectTime", Timestamp.class);
             definedValues.put("releaseTime", Timestamp.class);
             definedValues.put("hook", Integer.class);
             definedValues.put("simplex", Integer.class);
             definedValues.put("priority", Integer.class);
             definedValues.put("causeForTermination", Integer.class);
             definedValues.put("errorCode", Integer.class);
             pesqCastMap = new HashMap<String, Class< ? extends Object>>();
             pesqCastMap.put("sendSampleStart", Timestamp.class);
             pesqCastMap.put("pesq", Double.class);
             pesqCastMap.put("delay", Integer.class);
             parameterMap.put("hook", AMSCommandParameters.HOOK);
             parameterMap.put("simplex", AMSCommandParameters.SIMPLEX);
             parameterMap.put("priority", AMSCommandParameters.PRIORITY);
         }
 
         /**
          * Handle ams command.
          * 
          * @param amsCommandParameters the ams command parameters
          * @param key the key
          * @param parsedValue the parsed value
          */
         @Override
         protected void handleAMSCommand(AMSCommandParameters amsCommandParameters, String key, Object parsedValue) {
             super.handleAMSCommand(amsCommandParameters, key, parsedValue);
             // Field field = this.getClass().getField(key);
             // field.set(this, parsedValue);
             if (key.equals("hook")) {
                 hook = (Integer)parsedValue;
             } else if (key.equals("simplex")) {
                 simplex = (Integer)parsedValue;
             }
         }
 
         /**
          * Handle collector.
          * 
          * @throws ParseException the parse exception
          */
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
             List<PropertyCollector> collectorList = getSubCollectors();
             handleCall();
             delay = 0;
             delayCount = 0;
             for (PropertyCollector collector : collectorList) {
                 if (collector.getName().equals("pesqResult")) {
                     createAttachmentNode(collector);
                 }
             }
             if (delayCount > 0) {
                 delay = delay / delayCount;
                 if (tocttc != null) {
                     tocttc.addDelay(delay / 1000f);
                 }
                 if (tocttcGroup != null) {
                     tocttcGroup.addDelay(delay / 1000f);
                 }
             }
 
         }
 
         /**
          * Handle call.
          */
         protected void handleCall() {
             if (hook != null && simplex != null) {
 
                 if (hook == 0 && simplex == 0) {
                     tocttc = new AMSCall();
                     call = tocttc;
                     tocttc.setTimestamp(timestamp);
                     tocttc.setCallType(CallType.INDIVIDUAL);
                     String priority = getPropertyMap().get("priority");
                     if (priority != null) {
                         if (Integer.valueOf(priority) >= EMER_PRIORITY) {
                             tocttc.setCallType(CallType.HELP);
                         }
                     }
                     tocttc.addRelatedNode(node);
                     Long disconnectTime = (Long)node.getProperty("disconnectTime", null);
                     if (disconnectTime != null) {
                         tocttc.setCallTerminationBegin(disconnectTime);
                     }
                     if (node.hasProperty("errorCode") || node.hasProperty("errCode")) {
 
                         tocttc.setCallResult(CallResult.FAILURE);
                     } else {
                         tocttc.setCallResult(CallResult.SUCCESS);
                     }
                     final String probe = getPropertyMap().get("probeID");
                     Node callerProbe = probeCallCache.get(probe);
                     if (!StringUtils.isEmpty(probe)) {
                         Set<Call> callSet = sortedCall.get(probe);
                         if (callSet == null) {
                             callSet = new HashSet<Call>();
                             sortedCall.put(probe, callSet);
                         }
                         callSet.add(tocttc);
                     }
                     tocttc.setCallerProbe(callerProbe);
                     tocttc.setCalledPhoneNumber(getPropertyMap().get("calledNumber"));
                     tocttc.setCallSetupBeginTime((Long)node.getProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, 0l));
                     tocttc.setCallTerminationBegin((Long)node.getProperty("releaseTime", 0l));
                     Integer ct = (Integer)node.getProperty("causeForTermination", 1);
                     if (ct != 1) {
                         tocttc.setCallResult(CallResult.FAILURE);
                     }
                     checkInconclusive(tocttc);
                 } else if (hook == 1 && simplex == 1) {
                     tocttcGroup = new AMSCall();
 
                     call = tocttc;
                     tocttcGroup.setTimestamp(timestamp);
                     tocttcGroup.setCallType(CallType.GROUP);
                     String priority = getPropertyMap().get("priority");
                     if (priority != null) {
                         if (Integer.valueOf(priority) >= EMER_PRIORITY) {
                             tocttcGroup.setCallType(CallType.EMERGENCY);
                         }
                     }
                     tocttcGroup.addRelatedNode(node);
                     Long disconnectTime = (Long)node.getProperty("disconnectTime", null);
                     if (disconnectTime != null) {
                         tocttcGroup.setCallTerminationBegin(disconnectTime);
                     }
                     if (node.hasProperty("errorCode") || node.hasProperty("errCode")) {
                         tocttcGroup.setCallResult(CallResult.FAILURE);
                     } else {
                         tocttcGroup.setCallResult(CallResult.SUCCESS);
                     }
                     Node callerProbe = probeCallCache.get(getPropertyMap().get("probeID"));
                     tocttcGroup.setCallerProbe(callerProbe);
                     tocttcGroup.setCalledPhoneNumber(getPropertyMap().get("calledNumber"));
                     tocttcGroup.setCallSetupBeginTime((Long)node.getProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, 0l));
                     tocttcGroup.setCallSetupEndTime((Long)node.getProperty("connectTime", 0l));
 
                     tocttcGroup.setCallTerminationBegin((Long)node.getProperty("releaseTime", 0l));
                     Integer ct = (Integer)node.getProperty("causeForTermination", 1);
                     if (ct != 1) {
                         tocttcGroup.setCallResult(CallResult.FAILURE);
                     }
                     checkInconclusive(tocttcGroup);
                 }
             }
         }
 
         /**
          * Creates the attachment node.
          * 
          * @param collector the collector
          * @throws ParseException the parse exception
          */
         private void createAttachmentNode(PropertyCollector collector) throws ParseException {
             Node mm = neo.createNode();
             NeoUtils.addChild(node, mm, lastMM, neo);
             NodeTypes.MM.setNodeType(mm, neo);
             mm.setProperty(INeoConstants.PROPERTY_NAME_NAME, "pesq");
             lastMM = mm;
 
             Map<String, String> map = collector.getPropertyMap();
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 Object parseValue = getParcedValue(entry.getKey(), entry.getValue(), pesqCastMap).getLeft();
                 if (entry.getKey().equals("delay")) {
                     delay += ((Number)parseValue).intValue();
                     delayCount++;
                 }
                 if (entry.getKey().equals("pesq")) {
                     if (tocttc != null) {
                         tocttc.addLq(((Number)parseValue).floatValue());
                     }
                     if (tocttcGroup != null) {
                         tocttcGroup.addLq(((Number)parseValue).floatValue());
                     }
                 }
                 setProperty(mm, entry.getKey(), parseValue);
             }
 
         }
     }
 
     /**
      * <p>
      * Handler Tpc tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class Tpc extends AbstractEvent {
 
         /** The pd result. */
         protected final Map<String, Class< ? extends Object>> pdResult;
 
         /** The last mm. */
         Node lastMM = null;
 
         /**
          * Instantiates a new cell resel.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public Tpc(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("connectTime", Timestamp.class);
             definedValues.put("pdpRequest", Timestamp.class);
             definedValues.put("pdpAccept", Timestamp.class);
             definedValues.put("ftpConnReq", Timestamp.class);
             definedValues.put("ftpConnAccept", Timestamp.class);
             definedValues.put("releaseTime", Integer.class);
             definedValues.put("causeForTermination", Integer.class);
             definedValues.put("errorCode", Integer.class);
             pdResult = new HashMap<String, Class< ? extends Object>>();
             pdResult.put("size", Integer.class);
             pdResult.put("transmitStart", Timestamp.class);
             pdResult.put("transmitEnd", Timestamp.class);
 
         }
 
         /**
          * Handle collector.
          * 
          * @throws ParseException the parse exception
          */
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
             List<PropertyCollector> collectorList = getSubCollectors();
             for (PropertyCollector collector : collectorList) {
                 if (collector.getName().equals("pdResult")) {
                     createAttachmentNode(collector);
                 }
             }
         }
 
         /**
          * Creates the attachment node.
          * 
          * @param collector the collector
          * @throws ParseException the parse exception
          */
         private void createAttachmentNode(PropertyCollector collector) throws ParseException {
             Node mm = neo.createNode();
             NeoUtils.addChild(node, mm, lastMM, neo);
             NodeTypes.MM.setNodeType(mm, neo);
             mm.setProperty(INeoConstants.PROPERTY_NAME_NAME, "pdResult");
             lastMM = mm;
             Map<String, String> map = collector.getPropertyMap();
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 Object parseValue = getParcedValue(entry.getKey(), entry.getValue(), pdResult).getLeft();
                 setProperty(mm, entry.getKey(), parseValue);
             }
         }
     }
 
     /**
      * <p>
      * Handler sendMsg tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class SendMsg extends AbstractEvent {
 
         /** The send report. */
         protected final Map<String, Class< ? extends Object>> sendReport;
 
         /** The last mm. */
         Node lastMM = null;
 
         /** The time end. */
         Long timeEnd;
 
         /**
          * Instantiates a new cell resel.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public SendMsg(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("msgType", Integer.class);
             definedValues.put("dataLength", Integer.class);
             definedValues.put("sendTime", Timestamp.class);
             definedValues.put("msgRef", Integer.class);
             definedValues.put("releaseTime", Integer.class);
             definedValues.put("errorCode", Integer.class);
             sendReport = new HashMap<String, Class< ? extends Object>>();
             sendReport.put("reportTime", Timestamp.class);
             sendReport.put("status", Integer.class);
         }
 
         /**
          * Handle collector.
          * 
          * @throws ParseException the parse exception
          */
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
             handleCall();
             List<PropertyCollector> collectorList = getSubCollectors();
             for (PropertyCollector collector : collectorList) {
                 if (collector.getName().equals("sendReport")) {
                     createAttachmentNode(collector);
                 }
             }
             msgCall.setAcknowlegeTime(timeEnd - msgCall.getCallSetupBegin());
         }
 
         /**
          * Handle call.
          */
         private void handleCall() {
             assert msgCall == null;
             msgCall = new AMSCall();
 
             msgCall.addRelatedNode(node);
             msgCall.setTimestamp(timestamp);
             Long sendTime = (Long)node.getProperty("sendTime", null);
             msgCall.setCallSetupBeginTime(sendTime);
             timeEnd = sendTime;
             if (node.hasProperty("errorCode") || node.hasProperty("errCode")) {
                 msgCall.setCallResult(CallResult.FAILURE);
             } else {
                 msgCall.setCallResult(CallResult.SUCCESS);
             }
             final String probe = getPropertyMap().get("probeID");
             Node callerProbe = probeCallCache.get(probe);
             if (!StringUtils.isEmpty(probe)) {
                 Set<Call> callSet = sortedCall.get(probe);
                 if (callSet == null) {
                     callSet = new HashSet<Call>();
                     sortedCall.put(probe, callSet);
                 }
                 callSet.add(msgCall);
             } 
             msgCall.setCallerProbe(callerProbe);
             msgCall.addRelatedNode(node);
             msgCall.setCalledPhoneNumber(getPropertyMap().get("calledNumber"));
             String type = getPropertyMap().get("msgType");
             if (type.equals("12")) {
                 msgCall.setCallType(CallType.SDS);
             } else {
                 assert type.equals("13");
                 msgCall.setCallType(CallType.TSM);
             }
             checkInconclusive(msgCall);
 
         }
 
         /**
          * Creates the attachment node.
          * 
          * @param collector the collector
          * @throws ParseException the parse exception
          */
         private void createAttachmentNode(PropertyCollector collector) throws ParseException {
             Node mm = neo.createNode();
             NeoUtils.addChild(node, mm, lastMM, neo);
             NodeTypes.MM.setNodeType(mm, neo);
             mm.setProperty(INeoConstants.PROPERTY_NAME_NAME, "sendReport");
             lastMM = mm;
             Map<String, String> map = collector.getPropertyMap();
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 Object parseValue = getParcedValue(entry.getKey(), entry.getValue(), sendReport).getLeft();
                 if (sendReport.get(entry.getKey()) == Timestamp.class) {
                     timeEnd = Math.max(timeEnd, (Long)parseValue);
                 }
                 setProperty(mm, entry.getKey(), parseValue);
             }
         }
     }
 
     /**
      * <p>
      * Handler ReceiveMsg tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class ReceiveMsg extends AbstractEvent {
 
         /** The last mm. */
         Node lastMM = null;
 
         /**
          * Instantiates a new cell resel.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public ReceiveMsg(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("msgType", Integer.class);
             definedValues.put("dataLength", Integer.class);
             definedValues.put("receiveTime", Timestamp.class);
             definedValues.put("msgRef", Integer.class);
             definedValues.put("releaseTime", Integer.class);
             definedValues.put("errorCode", Integer.class);
         }
 
         /**
          * Handle collector.
          * 
          * @throws ParseException the parse exception
          */
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
             handleCall();
         }
 
         /**
          * Handle call.
          */
         private void handleCall() {
             if (msgCall == null) {
                 LOGGER.debug("Found resive message without send event " + basename);
                 return;
             }
             Long reciveTime = (Long)node.getProperty("receiveTime", null);
             if (reciveTime != null) {
                 msgCall.setResivedTime(reciveTime - msgCall.getCallSetupBegin());
             }
             if (node.hasProperty("errorCode") || node.hasProperty("errCode")) {
                 msgCall.setCallResult(CallResult.FAILURE);
             }
             probeId = getPropertyMap().get("probeID");
             msgCall.addCalleeProbe(probeCallCache.get(probeId));
             msgCall.addRelatedNode(node);
             checkInconclusive(msgCall);
 
         }
 
     }
 
     /**
      * <p>
      * Handler groupAttach tag
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class GroupAttach extends AbstractEvent {
 
         /** The last mm. */
         Node lastMM = null;
 
         /**
          * Instantiates a new group attach.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public GroupAttach(String tagName, IXmlTag parent) {
             super(tagName, parent, true);
             definedValues.put("groupAttachTime", Timestamp.class);
             definedValues.put("errorCode", Integer.class);
         }
 
         /**
          * Handle collector.
          * 
          * @throws ParseException the parse exception
          */
         @Override
         protected void handleCollector() throws ParseException {
             super.handleCollector();
 
             List<PropertyCollector> collectorList = getSubCollectors();
             String id = getPropertyMap().get("probeID");
             boolean haveProbeId = StringUtils.isNotEmpty(id);
             for (PropertyCollector collector : collectorList) {
                 if (collector.getName().equals("attachment")) {
                     createAttachmentNode(collector);
                     if (haveProbeId) {
                         String phone = collector.getPropertyMap().get("gssi");
                         if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(phone)) {
                             Set<String> probes = groupCall.get(phone);
                             if (probes == null) {
                                 probes = new HashSet<String>();
                                 groupCall.put(phone, probes);
                             }
                             probes.add(id);
                         }
                     }
                 }
             }
 
         }
 
         /**
          * Creates the attachment node.
          * 
          * @param collector the collector
          */
         private void createAttachmentNode(PropertyCollector collector) {
             Node mm = neo.createNode();
             NeoUtils.addChild(node, mm, lastMM, neo);
             NodeTypes.MM.setNodeType(mm, neo);
             mm.setProperty(INeoConstants.PROPERTY_NAME_NAME, "attachment");
             lastMM = mm;
             Map<String, String> map = collector.getPropertyMap();
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 setProperty(mm, entry.getKey(), entry.getValue());
             }
         }
     }
 
     /**
      * The Class CompleteGpsData.
      */
     public class CompleteGpsData extends PropertyCollector {
         /** The Constant TAG_NAME. */
         public static final String TAG_NAME = "completeGpsData";
 
         /**
          * Instantiates a new complete gps data.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         public CompleteGpsData(String tagName, IXmlTag parent) {
             super(tagName, parent, false);
         }
 
         /**
          * End element.
          * 
          * @param localName the local name
          * @param chars the chars
          * @return the i xml tag
          */
         @Override
         public IXmlTag endElement(String localName, StringBuilder chars) {
 
             if (openTag == null) {
                 try {
                     handleGpsData();
 
                 } catch (Exception e) {
                     NeoLoaderPlugin.exception(e);
                     LOGGER.error("event parsed with exception:", e);
                 }
                 return parent;
             }
             propertyMap.put(localName, chars.toString());
             openTag = null;
             return this;
         }
 
         /**
          * Handle gps data.
          */
         private void handleGpsData() {
             String probeID = getPropertyMap().get("probeID");
             Node probe = probeCache.get(probeID);
             if (probe == null) {
                 LOGGER.error(String.format("Not found probe with id=%s", probeID));
                 return;
             }
             String gpsSentence = getPropertyMap().get("gpsSentence");
             String probeId = getPropertyMap().get("probeID");
             if (StringUtils.isEmpty(gpsSentence) || StringUtils.isEmpty(probeId)) {
                 return;
             }
             GPSData gps = new GPSData(gpsSentence);
             if (gps.haveValidLocation()) {
                 String stringData = null;
                 try {
                     stringData = getPropertyMap().get("deliveryTime");
                     Long time = getTime(stringData);
                     if (time != null) {
                         gps.setTimestamp(time);
                         Set<GPSData> gpsSet = gpsMap.get(probeId);
                         if (gpsSet == null) {
                             gpsSet = new HashSet<GPSData>();
                             gpsMap.put(probeId, gpsSet);
                         }
                         gpsSet.add(gps);
                     }
                 } catch (ParseException e) {
                     LOGGER.error(String.format("Can't parse time: %s ", stringData), e);
                 }
                 // Transaction tx = neo.beginTx();
                 // try {
                 // Node mp = neo.createNode();
                 // probe.createRelationshipTo(mp, GeoNeoRelationshipTypes.LOCATION);
                 //
                 // gps.store(mp);
                 // String time=getPropertyMap().get("deliveryTime");
                 // String name="mp";
                 // NeoUtils.setNodeName(mp, name, neo);
                 // if (StringUtils.isNotEmpty(time)){
                 // try {
                 // Long timestamp = getTime(time);
                 // mp.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
                 // } catch (ParseException e) {
                 // LOGGER.error("wrong data: "+time, e);
                 // }
                 //                        
                 // }
                 // GisProperties gis = getGisProperties(networkName);
                 // gis.updateBBox(gps.lat, gps.lon);
                 // gis.checkCRS(((Double)gps.lat).floatValue(),
                 // ((Double)gps.lon).floatValue(),null);
                 // index(mp);
                 // tx.success();
                 // } finally {
                 // tx.finish();
                 // }
             }
         }
     }
 
     /**
      * <p>
      * Handler of tag "events"
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class EventTag extends AbstractTag {
 
         /** The Constant TAG_NAME. */
         public static final String TAG_NAME = "events";
 
         /**
          * Instantiates a new event tag.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         protected EventTag(String tagName, IXmlTag parent) {
             super(tagName, parent);
         }
 
         /**
          * Start element.
          * 
          * @param localName the local name
          * @param attributes the attributes
          * @return the i xml tag
          */
         @Override
         public IXmlTag startElement(String localName, Attributes attributes) {
             Class< ? extends AbstractEvent> klass = subNodes.get(localName);
             assert klass != null : localName;
             try {
                 Constructor< ? extends AbstractEvent> konstr = klass.getConstructor(AMSXMLoader.class, String.class, IXmlTag.class);
                 return konstr.newInstance(AMSXMLoader.this, localName, this);
             } catch (SecurityException e) {
                 // TODO Handle SecurityException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             } catch (NoSuchMethodException e) {
                 // TODO Handle NoSuchMethodException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             } catch (IllegalArgumentException e) {
                 // TODO Handle IllegalArgumentException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             } catch (InstantiationException e) {
                 // TODO Handle InstantiationException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             } catch (IllegalAccessException e) {
                 // TODO Handle IllegalAccessException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             } catch (InvocationTargetException e) {
                 // TODO Handle InvocationTargetException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
 
         }
 
     }
 
     /**
      * The Class ProbeIDNumberMap.
      */
     public class ProbeIDNumberMap extends PropertyCollector {
 
         /** The Constant TAG_NAME. */
         public static final String TAG_NAME = "probeIDNumberMap";
 
         /**
          * Instantiates a new probe id number map.
          * 
          * @param tagName the tag name
          * @param parent the parent
          */
         protected ProbeIDNumberMap(String tagName, IXmlTag parent) {
             super(tagName, parent, false);
         }
 
         /**
          * End element.
          * 
          * @param localName the local name
          * @param chars the chars
          * @return the i xml tag
          */
         @Override
         public IXmlTag endElement(String localName, StringBuilder chars) {
             if (openTag == null) {
                 handleCollector();
                 return parent;
             }
             propertyMap.put(localName, chars.toString());
             openTag = null;
             return this;
         }
 
         /**
          * Handle collector.
          */
         private void handleCollector() {
             Map<String, String> map = getPropertyMap();
             String id = map.get("probeID");
             if (probeCache.get(id) != null) {
                 return;
             }
             Node probeNew = NeoUtils.findOrCreateProbeNode(networkNode, id, neo);
 
             // Node probeNew;
             // Transaction tx = neo.beginTx();
             // try{
             // probeNew=neo.createNode();
             // NodeTypes.PROBE.setNodeType(probeNew, neo);
             // probeNew.setProperty(INeoConstants.PROPERTY_NAME_NAME, id);
             // NeoUtils.addChild(networkNode, probeNew, null, neo);
             // tx.success();
             // }finally{
             // tx.finish();
             // }
             Node currentProbeCalls = NeoUtils.getCallsNode(callDataset, id, probeNew, neo);
             probeCache.put(id, probeNew);
             probeCallCache.put(id, currentProbeCalls);
             String phone = map.get("phoneNumber");
             if (!StringUtils.isEmpty(phone)) {
                 phoneNumberCache.put(phone, id);
             }
             for (Map.Entry<String, String> entry : map.entrySet()) {
                 Object valueToSave;
                 String key = entry.getKey();
                 if (key.equals("locationArea")) {
                     key = INeoConstants.PROBE_LA;
                     valueToSave = Integer.parseInt(entry.getValue());
                 } else if (key.equals("frequency")) {
                     key = INeoConstants.PROBE_F;
                     valueToSave = Double.parseDouble(entry.getValue());
                 } else {
                     valueToSave = entry.getValue();
                 }
                 setIndexProperty(getHeaderMap(PROBE_NETWORK_HEADER_INDEX).headers, probeNew, key, valueToSave);
             }
 
             index(probeNew);
         }
 
     }
 
     /**
      * <p>
      * Factory class
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public class Factory implements IXmlTagFactory {
 
         /**
          * Creates the instance.
          * 
          * @param tagName the tag name
          * @param attributes the attributes
          * @return the i xml tag
          */
         @Override
         public IXmlTag createInstance(String tagName, Attributes attributes) {
             if (ProbeIDNumberMap.TAG_NAME.equals(tagName)) {
                 return new ProbeIDNumberMap(tagName, null);
             } else if (EventTag.TAG_NAME.equals(tagName)) {
                 return new EventTag(tagName, null);
             } else if (CompleteGpsData.TAG_NAME.equals(tagName)) {
                 return new CompleteGpsData(tagName, null);
             }
             return null;
         }
 
     }
 
     /**
      * <p>
      * AMS call
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public static class AMSCall extends Call {
 
         /** The called phone number. */
         protected String calledPhoneNumber;
 
         /**
          * Gets the called phone number.
          * 
          * @return the called phone number
          */
         public String getCalledPhoneNumber() {
             return calledPhoneNumber;
         }
 
         /**
          * Sets the called phone number.
          * 
          * @param callerPhoneNumber the new called phone number
          */
         public void setCalledPhoneNumber(String callerPhoneNumber) {
             this.calledPhoneNumber = callerPhoneNumber;
         }
 
     }
 
     /**
      * <p>
      * Contains information about gps data
      * </p>
      * .
      * 
      * @author tsinkel_a
      * @since 1.0.0
      */
     public static class GPSData {
 
         /** The valid. */
         private boolean valid;
 
         /** The command id. */
         private String commandId;
 
         /** The lat. */
         private double lat;
 
         /** The lon. */
         private double lon;
 
         /** The timestamp. */
         private long timestamp;
 
         /** The time. */
         private String time;
 
         /**
          * Instantiates a new gPS data.
          * 
          * @param gpsSentence the gps sentence
          */
         public GPSData(String gpsSentence) {
             valid = false;
             parse(gpsSentence);
         }
 
         /**
          * Sets the time.
          * 
          * @param time the new time
          */
         public void setTimestamp(long time) {
             timestamp = time;
         }
 
         /**
          * Parses the.
          * 
          * @param gpsSentence the gps sentence
          */
         private void parse(String gpsSentence) {
             valid = false;
             StringTokenizer st = new StringTokenizer(gpsSentence, ",");
             if (st.hasMoreTokens()) {
                 commandId = st.nextToken();
                 // NOW parse only GPGLL command
                 if (!commandId.equalsIgnoreCase("$GPGLL")) {
                     return;
                 }
 
                 try {
                     String latStr = st.nextToken();
                     String latNS = st.nextToken();
                     String lonStr = st.nextToken();
                     String lonNS = st.nextToken();
                     time = st.nextToken();
                     String validate = st.nextToken();
                     if (validate.equalsIgnoreCase("A")) {
                         lat = Double.parseDouble(latStr);
                         if (latNS.equalsIgnoreCase("S")) {
                             lat = -lat;
                         }
                         lon = Double.parseDouble(lonStr);
                         if (lonNS.equalsIgnoreCase("W")) {
                             lon = -lon;
                         }
                         valid = true;
                     }
                 } catch (Exception e) {
                     String message = "can't parse GPS data: " + gpsSentence;
                     NeoLoaderPlugin.error(message);
                     LOGGER.error(message, e);
                     valid = false;
                     return;
                 }
             }
         }
 
         /**
          * Store.
          * 
          * @param mp the mp
          */
         public void store(Node mp) {
             assert valid;
             mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, lat);
             mp.setProperty(INeoConstants.PROPERTY_LON_NAME, lon);
             if (timestamp > 0) {
                 mp.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
             }
             NodeTypes.MP.setNodeType(mp, null);
         }
 
         /**
          * Have valid location.
          * 
          * @return true, if successful
          */
         public boolean haveValidLocation() {
             return valid;
         }
 
         /**
          * Gets the timestamp.
          * 
          * @return the timestamp
          */
         public Long getTimestamp() {
             return timestamp;
         }
 
     }
 
 }
