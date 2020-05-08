 /*
  * File Name:  SosAgent.java
  * Created on: Dec 17, 2010
  */
 package net.ooici.eoi.datasetagent.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import net.ooici.Pair;
 import net.ooici.eoi.datasetagent.DataSourceRequestKeys;
 import net.ooici.eoi.datasetagent.obs.IObservationGroup;
 import net.ooici.eoi.datasetagent.NcdsFactory;
 import net.ooici.eoi.datasetagent.obs.ObservationGroupImpl;
 import net.ooici.eoi.datasetagent.VariableParams;
 import net.ooici.eoi.datasetagent.AbstractAsciiAgent;
 import ucar.nc2.dataset.NetcdfDataset;
 
 /**
  * TODO Add class comments
  * 
  * @author tlarocque
  * @version 1.0
  */
 public class SosAgent extends AbstractAsciiAgent{
     
     /** Static Fields */
     static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SosAgent.class);
     private static long beginTime = Long.MAX_VALUE;
     private static long endTime = Long.MIN_VALUE;
     protected static final SimpleDateFormat sdf;
     static {
         sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
     
     /* (non-Javadoc)
      * @see net.ooici.agent.abstraction.IDatasetAgent#buildRequest(java.util.Map)
      */
     @Override
     public String buildRequest(Map<String, String[]> parameters) {
         log.debug("");
         log.info("Building SOS Request for context [" + parameters.toString().substring(0, 40) + "...]");
         
         StringBuilder result = new StringBuilder();
 
         String baseUrl = parameters.get(DataSourceRequestKeys.BASE_URL)[0];
         String top[] = parameters.get(DataSourceRequestKeys.TOP);
         String bottom[] = parameters.get(DataSourceRequestKeys.BOTTOM);
         String left[] = parameters.get(DataSourceRequestKeys.LEFT);
         String right[] = parameters.get(DataSourceRequestKeys.RIGHT);
         String sTimeString = parameters.get(DataSourceRequestKeys.START_TIME)[0];
         String eTimeString = parameters.get(DataSourceRequestKeys.END_TIME)[0];
 
         String property = parameters.get(DataSourceRequestKeys.PROPERTY)[0];
         String stnId[] = parameters.get(DataSourceRequestKeys.STATION_ID);
 
         
         /** TODO: null-check here */
         /** Configure the date-time parameter (if avail) */
         String eventTime = null;
         if (null != sTimeString && null != eTimeString && !sTimeString.isEmpty() && !eTimeString.isEmpty()) {
             Date sTime = null;
             Date eTime = null;
             try {
                 sTime = DataSourceRequestKeys.ISO8601_FORMAT.parse(sTimeString);
             } catch (ParseException e) {
                 throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.START_TIME + "Unparsable value = " + sTimeString, e);
             }
             try {
                 eTime = DataSourceRequestKeys.ISO8601_FORMAT.parse(eTimeString);
             } catch (ParseException e) {
                 throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.END_TIME + "Unparsable value = " + eTimeString, e);
             }
             DateFormat sosUrlSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
             sosUrlSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
             sTimeString = sosUrlSdf.format(sTime);
             eTimeString = sosUrlSdf.format(eTime);
 
             eventTime = new StringBuilder(sTimeString).append('/').append(eTimeString).toString();
         }
 
 
 
         /** Configure the BBOX parameter (if avail) */
         String bbox = null;
         if (null != top && null != top[0] && !top[0].isEmpty()
                 && null != bottom && null != bottom[0] && !bottom[0].isEmpty()
                 && null != left && null != left[0] && !left[0].isEmpty()
                 && null != right && null != right[0] && !right[0].isEmpty()) {
 
             bbox = new StringBuilder(left[0]).append(',').append(bottom[0]).append('.').append(right[0]).append(',').append(top[0]).append(',').toString();
         }
 
 
 
         /** Build the query URL */
         result.append(baseUrl);
         result.append("request=").append("GetObservation");
         result.append("&service=").append("SOS");
         result.append("&observedproperty=").append(property);
         if (null != stnId) {
             result.append("&offering=urn:ioos:station:wmo:").append(stnId[0]);
         } else if (null != bbox) {
             result.append("&offering=urn:ioos:network:noaa.nws.ndbc:all");
             result.append("&featureofInterest=BBOX:").append(bbox);
         } else {
             throw new IllegalArgumentException("Cannot make a request without either a station ID or bounding box");
         }
         if (null != eventTime) { /* omitting time retrieves latest data */
             result.append("&eventtime=").append(eventTime);
         }
         result.append("&responseformat=").append("text/csv");
 
 
         
         log.debug("... built request: [" + result + "]");
         return result.toString();
     }
 
     /* (non-Javadoc)
      * @see net.ooici.agent.abstraction.AbstractAsciiAgent#parseObss(java.lang.String)
      */
     @Override
     protected List<IObservationGroup> parseObs(String asciiData) {
         log.debug("");
         log.info("Parsing observations from data [" + asciiData.substring(0, 40) + "...]");
         
         IObservationGroup obs = null;
         StringReader srdr = new StringReader(asciiData);
         try {
             obs = parseObservations(srdr);
         } finally {
             if (srdr != null) {
                 srdr.close();
             }
         }
 
         
         List<IObservationGroup> obsList = new ArrayList<IObservationGroup>();
         obsList.add(obs);
         return obsList;
     }
     
     /**
      * Parses the String data from the given reader into a list of observation groups.
      * <em>Note:</em><br />
      * The given reader is guaranteed to return from this method in a <i>closed</i> state.
      * @param rdr
      * @return a List of IObservationGroup objects if observations are parsed, otherwise this list will be empty
      */
     public static IObservationGroup parseObservations(Reader rdr) {
         IObservationGroup obs = null;
         BufferedReader csvReader = null;
         try {
             csvReader = new BufferedReader(rdr);
 
             String line = csvReader.readLine();//headerline
             if (line == null) {
                 /* Even when no dataVar is available, a header is returned.
                 Therefore, this should never happen unless the url is improperly formed which would result in an HTTP error of some sort. */
                 log.warn("Unusable argument: Given reader contains no String data");
                 return null;
             }
 
             String[] tokens;
             tokens = line.split(",");
             List<Pair<Integer, VariableParams>> dataCols = new ArrayList<Pair<Integer, VariableParams>>();
             for (int i = 6; i < tokens.length; i++) {
                 if (tokens[i].equalsIgnoreCase("\"sea_water_temperature (C)\"")) {
 //                    dataCols.add(new Pair<Integer, VariableParams>(i, VariableParams.SEA_WATER_TEMPERATURE));
                     dataCols.add(new Pair<Integer, VariableParams>(i, new VariableParams(VariableParams.SEA_WATER_TEMPERATURE, IObservationGroup.DataType.FLOAT)));
                 } else if (tokens[i].equalsIgnoreCase("\"sea_water_salinity (psu)\"")) {
 //                    dataCols.add(new Pair<Integer, VariableParams>(i, VariableParams.SEA_WATER_SALINITY));
                     dataCols.add(new Pair<Integer, VariableParams>(i, new VariableParams(VariableParams.SEA_WATER_SALINITY, IObservationGroup.DataType.FLOAT)));
                 }
             }
 
             String stnId = "";
 //            String snsId = "";
             int time = 0;
             float lat = -9999, tla = -99991, lon = -9999, tlo = -99991, depth = -9999/*, td = -99991*/;
             int obsId = 0;
             while ((line = csvReader.readLine()) != null) {
                 tokens = line.split(",");
                if (obs == null || (lat != (tla = Float.valueOf(tokens[2]))) || (lon != (tlo = Float.valueOf(tokens[3])))/*|| !stnId.equals(tokens[0]) || !snsId.equals(tokens[1]) || (lat != (tla = Float.valueOf(tokens[2]))) || (lon != (tlo = Float.valueOf(tokens[3])))*/) {
 
                     /* New group of observations */
                     stnId = tokens[0];
 //                    snsId = tokens[1];
                     lat = tla;
                     lon = tlo;
 //                    og = new ObservationGroupImpl(obsId++, stnId, snsId, lat, lon);
                     obs = new ObservationGroupImpl(obsId++, stnId, lat, lon);
                 }
 
                 /* Add the next time & depth */
                 try {
                     long longTime = sdf.parse(tokens[4]).getTime();
                     beginTime = Math.min(beginTime, longTime);
                     endTime = Math.max(endTime, longTime);
                     time = (int) (longTime * 0.001);
                 } catch (java.text.ParseException ex) {
                     time = 0;
                 } catch (NumberFormatException ex) {
                     time = 0;
                 } catch (ArrayIndexOutOfBoundsException ex) {
                     time = 0;
                 }
                 try {
                     depth = Float.valueOf(tokens[5]);
                 } catch (NumberFormatException ex) {
                     /* Can have missing depth (I believe when the instrument does not report it's depth or is an older record) - assign to depth of 0 when this happens. */
                     depth = 0.0f;
                 } catch (ArrayIndexOutOfBoundsException ex) {
                     depth = 0.0f;
                 }
                 Pair<Integer, VariableParams> dc;
                 float val;
                 for (int di = 0; di < dataCols.size(); di++) {
                     dc = dataCols.get(di);
                     try {
                         val = Float.valueOf(tokens[dc.getKey()]);
                     } catch (NumberFormatException ex) {
                         /* Data not a value... */
                         val = Float.NaN;
                     } catch (ArrayIndexOutOfBoundsException ex) {
                         /* If data is missing. */
                         val = Float.NaN;
                     }
                     obs.addObservation(time, depth, val, dc.getValue());
                 }
             }
         } catch (MalformedURLException ex) {
             log.error("Given URL cannot be parsed:  + url", ex);
         } catch (IOException ex) {
             log.error("General IO exception.  Please see stack trace", ex);
         } finally {
             if (null != csvReader) {
                 try {
                     csvReader.close();
                 } catch (IOException e) { /* NO-OP */
 
                 }
             } else if (null != rdr) {
                 try {
                     rdr.close();
                 } catch (IOException e) { /* NO-OP */
 
                 }
             }
         }
 
 
         /** Grab Global Attributes and copy into each observation group */
         Map<String, String> globalAttributes = new HashMap<String, String>();
 
 
         /* Extract the Global Attributes */
         /* title */
         String queryUrl = "http://sdf.ndbc.noaa.gov/sos/";
         globalAttributes.put("title", "NDBC Sensor Observation Service data from \"" + queryUrl + "\"");
 
         /* history */
         globalAttributes.put("history", "Converted from CSV to OOI CDM compliant NC by " + SosAgent.class.getName());
 
         /* references */
         globalAttributes.put("references", "[" + queryUrl + "; http://www.ndbc.noaa.gov/; http://www.noaa.gov/]");
 
         /* conventions */
         globalAttributes.put("Conventions", "CF-1.5");
 
         /* institution */
         globalAttributes.put("institution", "NOAA's National Data Buoy Center (http://www.ndbc.noaa.gov/)");
 
         /* source */
         globalAttributes.put("source", "NDBC SOS");
 
 
         /* Add begin and end time attributes */
         String beginAttrib = outSdf.format(new Date(beginTime));
         String endAttrib = outSdf.format(new Date(endTime));
 
         globalAttributes.put("utc_begin_time", beginAttrib);
         globalAttributes.put("utc_end_time", endAttrib);
 
 
         /* Add each attribute */
         obs.addAttributes(globalAttributes);
 
 
 
         return obs;
     }
 
     /* (non-Javadoc)
      * @see net.ooici.agent.abstraction.AbstractAsciiAgent#obs2Ncds(java.util.List)
      */
     @Override
     protected NetcdfDataset obs2Ncds(List<IObservationGroup> observations) {
         log.debug("");
         log.info("Creating NC Dataset as a 'station' feature type...");
         
         NetcdfDataset ncds = null;
         if (!observations.isEmpty()) {
             ncds = NcdsFactory.buildStation(observations.get(0));
         } else {
             log.warn("Unusable argument:  Given observations List is empty");
         }
         
         if (observations.size() > 1) {
             log.warn("Unexpected loss of data: Given List of observations contains more than 1 group which will not be used to produce NCDS output.  Total Observation Groups: " + observations.size());
         }
         
         return ncds;
     }
 
     
     
     /*****************************************************************************************************************/
     /* Testing                                                                                                       */
     /*****************************************************************************************************************/
 
     
     
     public static void main(String[] args) {
         Map<String, String[]> context = TEST_CONTEXT_STATION_1;
         if(true) {
             context = TEST_CONTEXT_GLIDER_1;
         }
         net.ooici.eoi.datasetagent.IDatasetAgent agent = net.ooici.eoi.datasetagent.AgentFactory.getDatasetAgent(context.get(DataSourceRequestKeys.SOURCE_NAME)[0]);
         NetcdfDataset dataset = agent.doUpdate(context);
         if (! new File(outDir).exists()) {
             new File(outDir).mkdirs();
         }
         String outName = "SOS_" + ++outCount + ".nc";
         try {
             log.info("Writing NC output to [" + outDir + outName + "]...");
             ucar.nc2.FileWriter.writeToFile(dataset, outDir + outName);
         } catch (IOException ex) {
             log.warn("Could not write NC to file: " + outDir + outName, ex);
         }
         
     }
     
     private static Map<String, String[]> TEST_CONTEXT_STATION_1 = new HashMap<String, String[]>();
     private static Map<String, String[]> TEST_CONTEXT_GLIDER_1 = new HashMap<String, String[]>();
     private static String outDir = "output/sos/";
     private static int outCount = 0;
     
     static {
         TEST_CONTEXT_STATION_1.put(DataSourceRequestKeys.SOURCE_NAME, new String[] {"SOS"});
         TEST_CONTEXT_STATION_1.put("base_url",   new String[] {"http://sdf.ndbc.noaa.gov/sos/server.php?"});
         TEST_CONTEXT_STATION_1.put("start_time", new String[] {"2008-08-01T00:00:00Z"});
         TEST_CONTEXT_STATION_1.put("end_time",   new String[] {"2008-08-02T00:00:00Z"});
         TEST_CONTEXT_STATION_1.put("property",   new String[] {"sea_water_temperature"});
         TEST_CONTEXT_STATION_1.put("stationId",  new String[] {"41012"});
 
         TEST_CONTEXT_GLIDER_1.put(DataSourceRequestKeys.SOURCE_NAME, new String[] {"SOS"});
         TEST_CONTEXT_GLIDER_1.put("base_url",   new String[] {"http://sdf.ndbc.noaa.gov/sos/server.php?"});
         TEST_CONTEXT_GLIDER_1.put("start_time", new String[] {"2010-07-26T00:00:00Z"});
         TEST_CONTEXT_GLIDER_1.put("end_time",   new String[] {"2010-07-27T00:00:00Z"});
         TEST_CONTEXT_GLIDER_1.put("property",   new String[] {"salinity"});
         TEST_CONTEXT_GLIDER_1.put("stationId",  new String[] {"48900"});
         
         
     }
     
 }
