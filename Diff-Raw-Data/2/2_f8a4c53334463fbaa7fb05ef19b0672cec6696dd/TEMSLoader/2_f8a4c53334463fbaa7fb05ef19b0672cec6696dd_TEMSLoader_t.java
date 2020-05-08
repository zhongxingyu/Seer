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
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.loader.core.preferences.DataLoadPreferences;
 import org.amanzi.neo.loader.ui.utils.LoaderUiUtils;
 import org.amanzi.neo.services.GisProperties;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.DriveTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 public class TEMSLoader extends DriveLoader {
     Pattern signalProp = Pattern
             .compile("(all_((active)|(pilot))_set_channel.*)|(all_((active)|(pilot))_set_pn.*)|(all_((active)|(pilot))_set_ec_io.*)|(all_((active)|(pilot))_set_count)");
     private static final String TIMESTAMP_DATE_FORMAT = "HH:mm:ss.S";
     private static final String MS_KEY = "ms";
     // private Node point = null;
     private int first_line = 0;
     private int last_line = 0;
     private String previous_ms = null;
     private String previous_time = null;
     private int previous_pn_code = -1;
     private Float currentLatitude = null;
     private Float currentLongitude = null;
     private String time = null;
     private long timestamp = 0L;
     private final HashMap<String, float[]> signals = new HashMap<String, float[]>();
     private String event;
     private final ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
     private Node mNode;
     private Node virtualMnode;
 
     /**
      * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
      * 
      * @param filename of file to load
      * @param display for opening message dialogs
      * @param dataset to add data to
      */
     public TEMSLoader(Calendar workDate, String filename, Display display, String dataset) {
         _workDate = workDate;
         driveType = DriveTypes.TEMS;
         mNode = null;
         virtualMnode = null;
         initialize("TEMS", null, filename, display, dataset);
         initializeLuceneIndex();
         initializeKnownHeaders();
         addDriveIndexes();
     }
 
     /**
      * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
      * and workdate
      * 
      * @param filename of file to load
      * @param display for opening message dialogs
      * @param dataset to add data to
      */
     public TEMSLoader(String filename, Display display, String dataset, Node mNode, Node virtualMnode) {
         _workDate = new GregorianCalendar();
         _workDate.setTimeInMillis(System.currentTimeMillis());
         driveType = DriveTypes.TEMS;
         this.mNode = mNode;
         virtualMnode = null;
         initialize("TEMS", null, filename, display, dataset);
         initializeLuceneIndex();
         initializeKnownHeaders();
         addDriveIndexes();
     }
 
     /**
      * Constructor for loading data in test mode, with no display and NeoService passed
      * 
      * @param neo database to load data into
      * @param filename of file to load
      * @param display
      */
     public TEMSLoader(GraphDatabaseService neo, String filename) {
         driveType = DriveTypes.TEMS;
         virtualMnode = null;
         mNode = null;
         _workDate = new GregorianCalendar();
         _workDate.setTimeInMillis(new File(filename).lastModified());
         initialize("TEMS", neo, filename, null, null);
         initializeLuceneIndex();
         initializeKnownHeaders();
         addDriveIndexes();
     }
 
     /**
      * Constructor for loading data in test mode, with no display and NeoService passed
      * 
      * @param neo database to load data into
      * @param filename of file to load
      * @param display
      */
     public TEMSLoader(GraphDatabaseService neo, String filename, String datasetName, LuceneIndexService anIndex) {
         driveType = DriveTypes.TEMS;
         virtualMnode = null;
         mNode = null;
         _workDate = new GregorianCalendar();
         _workDate.setTimeInMillis(new File(filename).lastModified());
         initialize("TEMS", neo, filename, null, datasetName);
         if (anIndex == null) {
             initializeLuceneIndex();
         } else {
             index = anIndex;
         }
         initializeKnownHeaders();
         addDriveIndexes();
     }
 
     /**
      * Add a known header entry as well as mark it as a main header. All other fields will be
      * assumed to be sector properties.
      * 
      * @param key
      * @param regexes
      */
     private void addMainHeader(String key, String[] regexes) {
         addKnownHeader(1, key, regexes, false);
         // mainHeaders.add(key);
     }
 
     /**
      * Build a map of internal header names to format specific names for types that need to be known
      * in the algorithms later.
      */
     private void initializeKnownHeaders() {
         // addMainHeader(INeoConstants.PROPERTY_LATITUDE_NAME,
         // getPossibleHeaders(DataLoadPreferences.DR_LATITUDE));
         // addMainHeader(INeoConstants.PROPERTY_LONGITUDE_NAME,
         // getPossibleHeaders(DataLoadPreferences.DR_LONGITUDE));
         addMainHeader(INeoConstants.PROPERTY_BCCH_NAME, getPossibleHeaders(DataLoadPreferences.DR_BCCH));
         addMainHeader(INeoConstants.PROPERTY_TCH_NAME, getPossibleHeaders(DataLoadPreferences.DR_TCH));
         addMainHeader(INeoConstants.PROPERTY_SC_NAME, getPossibleHeaders(DataLoadPreferences.DR_SC));
         addMainHeader(INeoConstants.PROPERTY_PN_NAME, getPossibleHeaders(DataLoadPreferences.DR_PN));
         addMainHeader(INeoConstants.PROPERTY_EcIo_NAME, getPossibleHeaders(DataLoadPreferences.DR_EcIo));
         addMainHeader(INeoConstants.PROPERTY_RSSI_NAME, getPossibleHeaders(DataLoadPreferences.DR_RSSI));
         addMainHeader(INeoConstants.PROPERTY_CI_NAME, getPossibleHeaders(DataLoadPreferences.DR_CI));
 
         // String[] latH = getPossibleHeaders(DataLoadPreferences.DR_LATITUDE);
         // addKnownHeader(1, INeoConstants.PROPERTY_LATITUDE_NAME, latH, false);
 
         // addMainHeader(INeoConstants.PROPERTY_LONGITUDE_NAME,
         // getPossibleHeaders(DataLoadPreferences.DR_LONGITUDE));
 
         PropertyMapper cpm = new PropertyMapper() {
 
             @Override
             public Object mapValue(String originalValue) {
                 try {
                     return Float.valueOf(originalValue);
                 } catch (NumberFormatException e) {
                     // TODO: handle exception
                 }
                 Pattern p = Pattern.compile("^([+-]{0,1}\\d+(\\.\\d+)*)([NESW]{0,1})$");
                 Matcher m = p.matcher(originalValue);
                 if (m.matches()) {
                     try {
                         // System.out.println(m.group(1));
                         return Float.valueOf(m.group(1));
                     } catch (NumberFormatException e) {
                         e.printStackTrace();
                         // TODO: handle exception
                     }
                 } else {
                     // System.out.println("originalValue: " + originalValue);
                     return null;
                 }
                 return null;
             }
         };
         String[] headers = getPossibleHeaders(DataLoadPreferences.DR_LONGITUDE);
         for (String singleHeader : headers) {
             addMappedHeader(1, singleHeader, singleHeader, "parsedLongitude", cpm);
         }
 
         headers = getPossibleHeaders(DataLoadPreferences.DR_LATITUDE);
         for (String singleHeader : headers) {
             addMappedHeader(1, singleHeader, singleHeader, "parsedLatitude", cpm);
         }
         // addKnownHeader(1, "longitude", ".*longitude", false);
         addKnownHeader(1, "ms", "MS", false);
         addMappedHeader(1, "ms", "MS", "ms", new StringMapper());
         addMappedHeader(1, "message_type", "Message Type", "message_type", new StringMapper());
         addMappedHeader(1, "event", "Event Type", "event_type", new PropertyMapper() {
 
             @Override
             public Object mapValue(String originalValue) {
                 return originalValue.replaceAll("HO Command.*", "HO Command");
             }
         });
 
         // lagutko, add additional header for cell id
         addKnownHeader(1, INeoConstants.SECTOR_ID_PROPERTIES, ".*Cell.*Id.*", true);
 
         final SimpleDateFormat df = new SimpleDateFormat(TIMESTAMP_DATE_FORMAT);
         addMappedHeader(1, "time", "Timestamp", "timestamp", new PropertyMapper() {
 
             @Override
             public Object mapValue(String time) {
                 Date datetime;
                 try {
                     datetime = df.parse(time);
                 } catch (ParseException e) {
                     SimpleDateFormat dfn = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
                     try {
                         datetime = dfn.parse(time);
                         _workDate.setTimeInMillis(datetime.getTime());
                         hasDate=true;
                     } catch (ParseException e1) {
                         error(e.getLocalizedMessage());
                         return 0L;
                     }
 
                 }
                 return datetime;
             }
         });
         PropertyMapper intMapper = new PropertyMapper() {
 
             @Override
             public Object mapValue(String value) {
                 try {
                     return Integer.parseInt(value);
                 } catch (NumberFormatException e) {
                     error(e.getLocalizedMessage());
                     return 0L;
                 }
             }
         };
         addMappedHeader(1, "all_rxlev_full", "All-RxLev Full", "all_rxlev_full", intMapper);
         addMappedHeader(1, "all_rxlev_sub", "All-RxLev Sub", "all_rxlev_sub", intMapper);
         addMappedHeader(1, "all_rxqual_full", "All-RxQual Full", "all_rxqual_full", intMapper);
         addMappedHeader(1, "all_rxqual_sub", "All-RxQual Sub", "all_rxqual_sub", intMapper);
         addMappedHeader(1, "all_sqi", "All-SQI", "all_sqi", intMapper);
         List<String> keys = new ArrayList<String>();
 
         for (int i = 1; i <= 12; i++) {
             keys.add("all_active_set_channel_" + i);
             keys.add("all_active_set_pn_" + i);
             keys.add("all_active_set_ec_io_" + i);
         }
         keys.add("all_pilot_set_count");
         addNonDataHeaders(1, keys);
         PropertyMapper floatMapper = new PropertyMapper() {
 
             @Override
             public Object mapValue(String value) {
                 try {
                     return Float.parseFloat(value);
                 } catch (NumberFormatException e) {
                     error(e.getLocalizedMessage());
                     return 0L;
                 }
             }
         };
         addMappedHeader(1, "all_sqi_mos", "All-SQI MOS", "all_sqi_mos", floatMapper);
     }
 
     private void addDriveIndexes() {
         try {
             String virtualDatasetName = DriveTypes.MS.getFullDatasetName(dataset);
 
             addIndex(NodeTypes.M.getId(), NeoUtils.getTimeIndexProperty(dataset));
             addIndex(INeoConstants.HEADER_MS, NeoUtils.getTimeIndexProperty(virtualDatasetName));
             addIndex(NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(dataset));
             addMappedIndex(MS_KEY, NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(virtualDatasetName));
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * After all lines have been parsed, this method is called. In this loader we save remaining
      * cached data, and call the super method to finalize saving of data to gis node and the
      * properties map.
      */
     @Override
     protected void finishUp() {
         saveData();
         super.finishUp();
     }
 
     @Override
     protected int parseLine(String line) {
         int saved = 0;
 
         // debug(line);
         List<String> fields = splitLine(line);
         if (fields.size() < 2)
             return 0;
         if (this.isOverLimit())
             return 0;
         Map<String, Object> lineData = makeDataMap(fields);
         // debug(line);
         try {
             this.time = String.valueOf(lineData.get("time"));
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println(lineData.get("time"));
         }
         Object timest = lineData.get("timestamp");
         this.timestamp = getTimeStamp(1, (Date)timest);
         String ms = (String)lineData.get("ms");
         event = (String)lineData.get("event"); // currently only getting this as a change
 
         // marker
         String message_type = (String)lineData.get("message_type"); // need this to filter for only
 
         this.incValidMessage();
         // TODO test
         Float latitude = (Float)lineData.get("parsedLatitude");
         Float longitude = (Float)lineData.get("parsedLongitude");
         if (time == null || latitude == null || longitude == null) {
             return 0;
         }
         if ((latitude != null)
                 && (longitude != null)
                 && (((currentLatitude == null) && (currentLongitude == null)) || ((Math.abs(currentLatitude - latitude) > 10E-10) || (Math
                         .abs(currentLongitude - longitude) > 10E-10)))) {
             currentLatitude = latitude;
             currentLongitude = longitude;
             saved = saveData(); // persist the current data to database
         }
         if (!lineData.isEmpty()) {
             data.add(lineData);
         }
         this.incValidLocation();
 
         if (!"EV-DO Pilot Sets Ver2".equals(message_type))
             return 0;
         int channel = 0;
         int pn_code = 0;
         int ec_io = 0;
         int measurement_count = 0;
         try {
             channel = (Integer)(lineData.get("all_active_set_channel_1"));
             pn_code = (Integer)(lineData.get("all_active_set_pn_1"));
             ec_io = (Integer)(lineData.get("all_active_set_ec_io_1"));
             measurement_count = (Integer)(lineData.get("all_pilot_set_count"));
         } catch (Exception e) {
             error("Failed to parse a field on line " + lineNumber + ": " + e.getMessage());
             return 0;
         }
         if (measurement_count > 12) {
             error("Measurement count " + measurement_count + " > 12");
             measurement_count = 12;
         }
         boolean changed = false;
         if (!ms.equals(this.previous_ms)) {
             changed = true;
             this.previous_ms = ms;
         }
         if (!this.time.equals(this.previous_time)) {
             changed = true;
             this.previous_time = this.time;
         }
         if (pn_code != this.previous_pn_code) {
             if (this.previous_pn_code >= 0) {
                 error("SERVER CHANGED");
             }
             changed = true;
             this.previous_pn_code = pn_code;
         }
         if (measurement_count > 0 && (changed || (event != null && event.length() > 0))) {
             if (this.isOverLimit())
                 return 0;
             if (first_line == 0)
                 first_line = lineNumber;
             last_line = lineNumber;
             this.incValidChanged();
             debug(time + ": server channel[" + channel + "] pn[" + pn_code + "] Ec/Io[" + ec_io + "]\t" + event + "\t"
                     + this.currentLatitude + "\t" + this.currentLongitude);
             for (int i = 1; i <= measurement_count; i++) {
                 // Delete invalid data, as you can have empty ec_io
                 // zero ec_io is correct, but empty ec_io is not
                 try {
                     ec_io = (Integer)(lineData.get("all_pilot_set_ec_io_" + i));
                     channel = (Integer)(lineData.get("all_pilot_set_channel_" + i));
                     pn_code = (Integer)(lineData.get("all_pilot_set_pn_" + i));
                     debug("\tchannel[" + channel + "] pn[" + pn_code + "] Ec/Io[" + ec_io + "]");
                     addStats(pn_code, ec_io);
                     String chan_code = "" + channel + "\t" + pn_code;
                     if (!signals.containsKey(chan_code))
                         signals.put(chan_code, new float[2]);
                     signals.get(chan_code)[0] += LoaderUiUtils.dbm2mw(ec_io);
                     signals.get(chan_code)[1] += 1;
                 } catch (Exception e) {
                     error("Error parsing column " + i + " for EC/IO, Channel or PN: " + e.getMessage());
                 }
             }
         }
         return saved;
     }
 
     /**
      * This method is called to dump the current cache of signals as one located point linked to a
      * number of signal strength measurements.
      */
     private int saveData() {
         int saved = 0;
         if (signals.size() > 0 || !data.isEmpty()) {
             Transaction transaction = neo.beginTx();
             try {
                 Node mp = null;
                 if (!data.isEmpty()) {
                     mp = neo.createNode();
                     if (timestamp != 0) {
                         mp.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, this.timestamp);
                         updateTimestampMinMax(1, timestamp);
                     }
                     mp.setProperty(INeoConstants.PROPERTY_FIRST_LINE_NAME, first_line);
                     mp.setProperty(INeoConstants.PROPERTY_LAST_LINE_NAME, last_line);
                     mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, currentLatitude.doubleValue());
                     mp.setProperty(INeoConstants.PROPERTY_LON_NAME, currentLongitude.doubleValue());
                     mp.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.MP.getId());
                     mp.setProperty(INeoConstants.PROPERTY_TIME_NAME, time);
                     index(mp);
                     boolean haveEvents = false;
                     for (Map<String, Object> dataLine : data) {
                         Node m = neo.createNode();
                         findOrCreateFileNode(m);
                         m.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.M.getId());
                         for (Map.Entry<String, Object> entry : dataLine.entrySet()) {
                             if (isMMProperties(entry.getKey())) {
                                 continue;
                             }
                             if (!tryToIndexSectorId(m, entry) && "timestamp".equals(entry.getKey())){
                                 long timeStamp = getTimeStamp(1, ((Date)entry.getValue()));
                                 if (timeStamp != 0) {
                                     m.setProperty(entry.getKey(), timeStamp);
                                     mp.setProperty(entry.getKey(), timeStamp);
                                 }
                             } else {
                                 m.setProperty(entry.getKey(), entry.getValue());
                                 haveEvents = haveEvents || INeoConstants.PROPERTY_TYPE_EVENT.equals(entry.getKey());
                             }
                         }
                         // debug("\tAdded measurement: " + propertiesString(ms));
                         m.createRelationshipTo(mp, GeoNeoRelationshipTypes.LOCATION);
                         if (mNode != null) {
                             mNode.createRelationshipTo(m, GeoNeoRelationshipTypes.NEXT);
                         }
                         m.setProperty(INeoConstants.PROPERTY_NAME_NAME, getMNodeName(dataLine));
                         mNode = m;
                         index(m);
                         if (storingProperties.get(1) != null) {
                             storingProperties.get(1).incSaved();
                         }
                         saved++;
                     }
                     if (haveEvents) {
                         index.index(mp, INeoConstants.EVENTS_LUCENE_INDEX_NAME, dataset);
                     }
                     GisProperties gisProperties = getGisProperties(dataset);
                     gisProperties.updateBBox(currentLatitude, currentLongitude);
                     gisProperties.checkCRS(currentLatitude, currentLongitude, null);
                     gisProperties.incSaved();
                 }
                 if (!signals.isEmpty()) {
                     if (mp == null) {
                         mp = neo.createNode();
                         if (timestamp != 0) {
                             mp.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, this.timestamp);
                             updateTimestampMinMax(1, timestamp);
                         }
                         mp.setProperty(INeoConstants.PROPERTY_FIRST_LINE_NAME, first_line);
                         mp.setProperty(INeoConstants.PROPERTY_LAST_LINE_NAME, last_line);
                         mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, currentLatitude.doubleValue());
                         mp.setProperty(INeoConstants.PROPERTY_LON_NAME, currentLongitude.doubleValue());
                         mp.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.MP.getId());
                         index(mp);
                         GisProperties gisProperties = getGisProperties(dataset);
                         gisProperties.updateBBox(currentLatitude, currentLongitude);
                         gisProperties.checkCRS(currentLatitude, currentLongitude, null);
                         gisProperties.incSaved();
                         saved++;
                     }
 
                     LinkedHashMap<String, Header> statisticHeader = getHeaderMap(2).headers;
                     if (statisticHeader.isEmpty()) {
                         Header header = new Header(INeoConstants.PRPOPERTY_CHANNEL_NAME, INeoConstants.PRPOPERTY_CHANNEL_NAME, 0);
                         header = new IntegerHeader(header);
                         statisticHeader.put(INeoConstants.PRPOPERTY_CHANNEL_NAME, header);
                         header = new Header(INeoConstants.PROPERTY_CODE_NAME, INeoConstants.PROPERTY_CODE_NAME, 0);
                         header = new IntegerHeader(header);
                         statisticHeader.put(INeoConstants.PROPERTY_CODE_NAME, header);
                         header = new Header(INeoConstants.PROPERTY_DBM_NAME, INeoConstants.PROPERTY_DBM_NAME, 0);
                         header = new FloatHeader(header);
                         statisticHeader.put(INeoConstants.PROPERTY_DBM_NAME, header);
                         header = new Header(INeoConstants.PROPERTY_MW_NAME, INeoConstants.PROPERTY_MW_NAME, 0);
                         header = new FloatHeader(header);
                         statisticHeader.put(INeoConstants.PROPERTY_MW_NAME, header);
                     }
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
                         Header header = statisticHeader.get(INeoConstants.PRPOPERTY_CHANNEL_NAME);
                         Object valueToSave = header.parse(cc[0]);
                         ms.setProperty(INeoConstants.PRPOPERTY_CHANNEL_NAME, valueToSave);
                         header = statisticHeader.get(INeoConstants.PROPERTY_CODE_NAME);
                         valueToSave = header.parse(cc[1]);
                         ms.setProperty(INeoConstants.PROPERTY_CODE_NAME, valueToSave);
                         ms.setProperty(INeoConstants.PROPERTY_NAME_NAME, cc[1]);
                         float dbm = LoaderUiUtils.mw2dbm(mw);
                         header = statisticHeader.get(INeoConstants.PROPERTY_DBM_NAME);
                         valueToSave = header.parse(String.valueOf(dbm));
                         ms.setProperty(INeoConstants.PROPERTY_DBM_NAME, valueToSave);
 
                         header = statisticHeader.get(INeoConstants.PROPERTY_MW_NAME);
                         valueToSave = header.parse(String.valueOf(mw));
                         ms.setProperty(INeoConstants.PROPERTY_MW_NAME, mw);
                         debug("\tAdded measurement: " + propertiesString(ms));
                         if (timestamp != 0) {
                             ms.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, this.timestamp);
                             updateTimestampMinMax(2, timestamp);
                         }
                         index(ms);
                         findOrCreateVirtualFileNode(ms);
                         if (virtualMnode != null) {
                             virtualMnode.createRelationshipTo(ms, GeoNeoRelationshipTypes.NEXT);
                         }
 
                         virtualMnode = ms;
                         ms.createRelationshipTo(mp, GeoNeoRelationshipTypes.LOCATION);
                         saved++;
                     }
                 }
 
                 transaction.success();
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 transaction.finish();
             }
         } else {
             debug("Nothing to store for "+dataset);
         }
         signals.clear();
         data.clear();
         first_line = 0;
         last_line = 0;
         return saved;
     }
 
     private boolean isMMProperties(String string) {
         return signalProp.matcher(string).matches();
     }
 
     /**
      * get name of m node
      * 
      * @param dataLine - node data
      * @return node name
      */
     private Object getMNodeName(Map<String, Object> dataLine) {
         Object timeNode = dataLine.get("time");
         return timeNode == null ? "m" : timeNode.toString();
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         if (args.length < 1)
             args = new String[] {"amanzi/test.FMT", "amanzi/0904_90.FMT", "amanzi/0905_22.FMT", "amanzi/0908_44.FMT"};
         EmbeddedGraphDatabase neo = new EmbeddedGraphDatabase("../../testing/neo");
         try {
             for (String filename : args) {
                 TEMSLoader driveLoader = new TEMSLoader(neo, filename);
                 driveLoader.setLimit(100);
                 driveLoader.run(null);
                 driveLoader.printStats(true); // stats for this load
             }
             printTimesStats(); // stats for all loads
         } catch (IOException e) {
             System.err.println("Error loading TEMS data: " + e);
             e.printStackTrace(System.err);
         } finally {
             neo.shutdown();
         }
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         if (key == 1) {
             return datasetNode;
         } else {
             return getVirtualDataset(DriveTypes.MS, false);
         }
 
     }
 
     @Override
     protected String getPrymaryType(Integer key) {
 
         if (key == 1) {
             return NodeTypes.M.getId();
         } else {
             return INeoConstants.HEADER_MS;
         }
     }
 
     @Override
     protected ArrayList<Node> getGisNodes() {
         ArrayList<Node> result = new ArrayList<Node>();
 
         Transaction transaction = neo.beginTx();
         try {
             Iterator<Node> gisNodes = datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH,
                     new ReturnableEvaluator() {
 
                         @Override
                         public boolean isReturnableNode(TraversalPosition currentPos) {
                             return NeoUtils.isGisNode(currentPos.currentNode());
                         }
                     }, GeoNeoRelationshipTypes.VIRTUAL_DATASET, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT,
                     Direction.INCOMING).iterator();
 
             while (gisNodes.hasNext()) {
                 result.add(gisNodes.next());
             }
         } catch (Exception e) {
            NeoCorePlugin.error("Failed to find GIS nodes", e);
             transaction.failure();
         } finally {
             transaction.finish();
         }
 
         return result;
     }
 
     @Override
     protected void saveProperties() {
         LinkedHashMap<String, Header> header = getHeaderMap(1).headers;
         Iterator<String> iter = header.keySet().iterator();
         while (iter.hasNext()) {
             String key = iter.next();
             if (isMMProperties(key)) {
                 iter.remove();
             }
         }
         super.saveProperties();
     }
 
 }
