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
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.database.services.events.UpdateViewEventType;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.MeasurementRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 
 /**
  * <p>
  * Nemo loader (new version nemo files)
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class NemoLoader extends DriveLoader {
     protected static final SimpleDateFormat EVENT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
 
     /** String EVENT_ID field */
     protected static final String EVENT_ID = INeoConstants.EVENT_ID;
     /** String TIME_FORMAT field */
     protected static final String TIME_FORMAT = "HH:mm:ss.S";
     protected char fieldSepRegex;
     protected Node pointNode;
     protected Node parentMnode;
     protected SimpleDateFormat timeFormat;
     protected LinkedHashMap<String, Header> headers;
 
     // protected Node msNode;
 
     /**
      * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
      * 
      * @param filename of file to load
      * @param display for opening message dialogs
      * @param dataset to add data to
      */
     public NemoLoader(Calendar time, String filename, Display display, String dataset) {
         _workDate = time;
         driveType = DriveTypes.NEMO2;
         initialize("Nemo", null, filename, display, dataset);
         headers = getHeaderMap(1).headers;
         timeFormat = new SimpleDateFormat(TIME_FORMAT);
         pointNode = null;
         initializeKnownHeaders();
         addDriveIndexes();
         possibleFieldSepRegexes = new String[] {"\t", ",", ";"};
 
     }
 
     /**
      * initialize headers
      */
     protected void initializeKnownHeaders() {
         headers.put(EVENT_ID, new StringHeader(new Header(EVENT_ID, EVENT_ID, 0)));
         // headers.put(TIME, new Header(TIME, TIME, 1));
         // MappedHeaderRule mapRule = new MappedHeaderRule("timestamp", TIME, new
         // DateTimeMapper(TIME_FORMAT));
         // headers.put(mapRule.key, new MappedHeader(headers.get(TIME), mapRule));
 
     }
 
     @Override
     protected void parseLine(String line) {
         if (parser == null) {
             determineFieldSepRegex(line);
         }
 
         List<String> parsedLine = splitLine(line);
         if (parsedLine.size() < 1) {
             return;
         }
         Event event = new Event(parsedLine);
         try {
             event.analyseKnownParameters(headers);
         } catch (Exception e) {
             e.printStackTrace();
             NeoLoaderPlugin.error(e.getLocalizedMessage());
             return;
         }
 
         String eventId = event.eventId;
         createMNode(event);
         if ("GPS".equalsIgnoreCase(eventId)) {
             createPointNode(event);
             return;
         }
     }
 
     /**
      * create mp node
      * 
      * @param event - event
      */
     protected void createPointNode(Event event) {
         Transaction transaction = neo.beginTx();
         try {
             Float lon = (Float)event.parsedParameters.get("Lon.");
             Float lat = (Float)event.parsedParameters.get("Lat.");
             String time = event.time;
             if ((lon == null || lat == null) ||
                 (lon == 0) && (lat == 0)) {
                 return;
             }
             Node mp = neo.createNode();
             mp.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.MP.getId());
             mp.setProperty(INeoConstants.PROPERTY_TIME_NAME, time);
 
             mp.setProperty(INeoConstants.PROPERTY_LAT_NAME, lat.doubleValue());
             mp.setProperty(INeoConstants.PROPERTY_LON_NAME, lon.doubleValue());
             GisProperties gisProperties = getGisProperties(dataset);
             gisProperties.updateBBox(lat, lon);
             gisProperties.checkCRS((float)lat, (float)lon, null);
             index(mp);
             transaction.success();
             pointNode = mp;
         } catch (Exception e) {
             NeoLoaderPlugin.error(e.getLocalizedMessage());
             return;
         } finally {
             transaction.finish();
         }
     }
 
     /**
      * create ms node
      * 
      * @param event - event
      */
     protected void createMNode(Event event) {
         Transaction transaction = neo.beginTx();
         try {
             String id = event.eventId;// getEventId(event);
             String time = event.time;// getEventTime(event);
             long timestamp;
             try {
                 timestamp = getTimeStamp(1, timeFormat.parse(time));
             } catch (ParseException e) {
                 // some parameters do not have time
                 // NeoLoaderPlugin.error(e.getLocalizedMessage());
                 timestamp = 0;
             }
             Node ms = neo.createNode();
             findOrCreateFileNode(ms);
             ms.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.M.getId());
             event.store(ms, headers);
             if (timestamp != 0) {
                 ms.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
             }
             if (parentMnode != null) {
                 parentMnode.createRelationshipTo(ms, MeasurementRelationshipTypes.NEXT);
             }
             if (pointNode != null) {
                 ms.createRelationshipTo(pointNode, GeoNeoRelationshipTypes.LOCATION);
                 if (timestamp != 0) {
                     pointNode.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
                 }
             }
             ms.setProperty(INeoConstants.PROPERTY_NAME_NAME, id);
             index(ms);
             parentMnode = ms;
             transaction.success();
         } catch (Exception e) {
             e.printStackTrace();
             NeoLoaderPlugin.error(e.getLocalizedMessage());
             return;
         } finally {
             transaction.finish();
         }
     }
 
     /**
      * Get event time
      * 
      * @param parsedLine - list of fields
      * @return String
      */
     protected String getEventTime(List<String> parsedLine) {
         return parsedLine.get(1);
     }
 
     /**
      * Get event id
      * 
      * @param parsedLine - list of fields
      * @return String
      */
     protected String getEventId(List<String> parsedLine) {
         return parsedLine.get(0);
     }
 
     /**
      * add index
      */
     private void addDriveIndexes() {
         try {
             addIndex(NodeTypes.M.getId(), NeoUtils.getTimeIndexProperty(dataset));
             addIndex(NodeTypes.MP.getId(), NeoUtils.getLocationIndexProperty(dataset));
         } catch (IOException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * <p>
      * Event - provide information about command (1 row from log file)
      * </p>
      * 
      * @author cinkel_a
      * @since 1.0.0
      */
     public class Event {
         /** SimpleDateFormat EVENT_DATE_FORMAT field */
         protected String eventId;
         protected String time;
         protected List<Integer> contextId = new ArrayList<Integer>();
         protected List<String> parameters;
         protected Map<String, Object> parsedParameters;
         protected NemoEvents event;
 
         /**
          * constructor
          */
         public Event(List<String> parcedLine) {
             parsedParameters = new LinkedHashMap<String, Object>();
             parse(parcedLine);
         }
 
         /**
          *parse line
          * 
          * @param parcedLine - list of string tag
          */
         protected void parse(List<String> parcedLine) {
             eventId = parcedLine.get(0);
             event = NemoEvents.getEventById(eventId);
             time = parcedLine.get(1);
             String numberContextId = parcedLine.get(2);
             contextId.clear();
             Integer firstParamsId = 3;
             if (!numberContextId.isEmpty()) {
                 int numContext = Integer.parseInt(numberContextId);
                 for (int i = 1; i <= numContext; i++) {
                     int value = 0;
                     String field = parcedLine.get(firstParamsId++);
                     if (!field.isEmpty()) {
                         try {
                             value = Integer.parseInt(field);
                         } catch (NumberFormatException e) {
                             // TODO Handle NumberFormatException
                             NeoLoaderPlugin.error("Wrong context id:" + field);
                             value = 0;
                         }
                     }
                     contextId.add(value);
                 }
             }
             parameters = new ArrayList<String>();
             for (int i = firstParamsId; i < parcedLine.size(); i++) {
                 parameters.add(parcedLine.get(i));
             }
         }
 
         /**
          *create parsedParameters - list of parsed parameters of event.
          */
         protected void analyseKnownParameters(Map<String, Header> statisticHeaders) {
             if (parameters.isEmpty()) {
                 return;
             }
 
             if (event == null) {
                 return;
             }
             Map<String, Object> parParam;
             try {
                 parParam = event.fill(getVersion(), parameters);
             } catch (Exception e1) {
                 System.out.println(eventId);
                 System.out.println(parameters.toString());
                 // TODO Handle Exception
                 throw (RuntimeException) new RuntimeException( ).initCause( e1 );
             }
             if (parParam.isEmpty()) {
                 return;
             }
             // add context field
             if (parParam.containsKey(NemoEvents.FIRST_CONTEXT_NAME)) {
                 List<String> contextName = (List<String>)parParam.get(NemoEvents.FIRST_CONTEXT_NAME);
                 parParam.remove(NemoEvents.FIRST_CONTEXT_NAME);
                 for (int i = 0; i < contextId.size() && i < contextName.size(); i++) {
                     if (contextId.get(i) != 0) {
                         parParam.put(contextName.get(i), contextId.get(i));
                     }
                 }
             }
             if (_workDate == null && event == NemoEvents.START) {
                 _workDate = new GregorianCalendar();
                 Date date;
                 try {
                     date = EVENT_DATE_FORMAT.parse((String)parParam.get("Date"));
 
                 } catch (Exception e) {
                     NeoLoaderPlugin.error("Wrong time format" + e.getLocalizedMessage());
                     date = new Date(new File(filename).lastModified());
                 }
                 _workDate.setTime(date);
             }
             parsedParameters.putAll(parParam);
             if (statisticHeaders == null) {
                 return;
             }
             for (String key : parParam.keySet()) {
                 if (!statisticHeaders.containsKey(key)) {
                     Object value = parParam.get(key);
                     if (value == null) {
                         continue;
                     }
                     Header header = new Header(key, key, 0);
                     if (value instanceof Float) {
                         header = new FloatHeader(header);
                     } else if (value instanceof Integer) {
                         header = new IntegerHeader(header);
                     } else if (value instanceof String) {
                         header = new StringHeader(header);
                     } else {
                         continue;
                     }
                     statisticHeaders.put(key, header);
                 }
             }
         }
 
         /**
          * get version of file format
          * 
          * @return ff version
          */
         protected String getVersion() {
             return "2.01";
         }
 
         /**
          * store all properties in ms node
          * 
          * @param msNode - ms node
          * @param statisticHeaders - statistic headers
          */
         public void store(Node msNode, Map<String, Header> statisticHeaders) {
             storeProperties(msNode, EVENT_ID, eventId, statisticHeaders);
             storeProperties(msNode, INeoConstants.PROPERTY_TIME_NAME, time, statisticHeaders);
             storeProperties(msNode, INeoConstants.EVENT_CONTEXT_ID, contextId.toArray(new Integer[0]), null);
             for (String key : parsedParameters.keySet()) {
                 storeProperties(msNode, key, parsedParameters.get(key), statisticHeaders);
             }
         }
 
         /**
          * Store property in node
          * 
          * @param msNode - ms node
          * @param key - key
          * @param value - value
          * @param statisticHeaders - statistic headers
          */
         protected void storeProperties(Node msNode, String key, Object value, Map<String, Header> statisticHeaders) {
             if (value == null) {
                 return;
             }
             Header header = statisticHeaders == null ? null : statisticHeaders.get(key);
             if (header == null) {
                 msNode.setProperty(key, value);
             } else {
                 // TODO remove double parsing
                 Object valueToSave = header.parse(value.toString());
                 if (valueToSave == null) {
                     // NeoLoaderPlugin.info("Not saved key=" + key + "\t value=" + value);
                     return;
                 }
                 msNode.setProperty(key, valueToSave);
             }
             msNode.setProperty(key, value);
         }
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         return gisNodes.get(dataset).getGis();
     }
 
     @Override
     protected boolean needParceHeaders() {
         return false;
     }
     
     @Override
     protected void finishUp() {
         sendUpdateEvent(UpdateViewEventType.GIS);
     }
 }
