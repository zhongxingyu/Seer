 /*
  * File Name:  UsgsAgent.java
  * Created on: Dec 20, 2010
  */
 package net.ooici.eoi.datasetagent.impl;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.TreeMap;
 
 import net.ooici.eoi.datasetagent.DataSourceRequestKeys;
 import net.ooici.eoi.datasetagent.obs.IObservationGroup;
 import net.ooici.eoi.datasetagent.obs.IObservationGroup.DataType;
 import net.ooici.eoi.datasetagent.obs.ObservationGroupImpl;
 import net.ooici.eoi.netcdf.VariableParams;
 import net.ooici.eoi.datasetagent.AbstractAsciiAgent;
 import net.ooici.eoi.datasetagent.AgentUtils;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.xpath.XPath;
 
 import ucar.nc2.dataset.NetcdfDataset;
 
 /**
  * TODO Add class comments
  * 
  * @author tlarocque
  * @version 1.0
  */
 public class UsgsAgent extends AbstractAsciiAgent {
 
     /**
      * NOTE: this Object uses classes from org.jdom.* The JDOM library is included as a transitive dependency from
      * ooi-netcdf-full-4.2.4.jar. Should the JDOM jar be included explicitly??
      * 
      * TODO: check that each element selected by the xpath queries defined below are required per the WaterML1.1 spec. When these elements
      * are selected via xpath NULLs are not checked. If an element is not required, missing values will cause NPE
      */
     /** Static Fields */
     static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UsgsAgent.class);
     private static final SimpleDateFormat valueSdf;
     private static final SimpleDateFormat inSdf;
     private static int currentGroupId = -1;
     
     /** Maths */
     public static final double CONVERT_FT_TO_M = 0.3048;
     public static final double CONVERT_FT3_TO_M3 = Math.pow(CONVERT_FT_TO_M, 3);
 
     private boolean isDailyValue = false;
 
     /** Static Initializer */
     static {
         valueSdf = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss.sssZ");
         valueSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         inSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
         inSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     /* (non-Javadoc)
      * @see net.ooici.agent.abstraction.IDatasetAgent#buildRequest(java.util.Map)
      */
     @Override
     public String buildRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage context) {
         log.debug("");
         log.info("Building Request for context [" + context.toString() + "...]");
 
         String result = "";
 
         String baseurl = context.getBaseUrl();
         if (baseurl.endsWith("nwis/iv?")) {
             result = buildWaterServicesRequest(context);
         } else if (baseurl.endsWith("NWISQuery/GetDV1?")) {
             result = buildDailyValuesRequest(context);
             isDailyValue = true;
         }
 
 
 //        String baseUrl = context.getBaseUrl();
 //        String sTimeString = context.getStartTime();
 //        String eTimeString = context.getEndTime();
 //        String properties[] = context.getPropertyList().toArray(new String[0]);
 //        String siteCodes[] = context.getStationIdList().toArray(new String[0]);
 
 
         /** TODO: null-check here */
         /** Configure the date-time parameter */
 //        Date sTime = null;
 //        Date eTime = null;
 //        DateFormat usgsUrlSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
 //        if (isDailyQuery) {
 //            usgsUrlSdf = new SimpleDateFormat("yyyy-MM-dd");
 //        }
 //        usgsUrlSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
 //        try {
 //            sTime = AgentUtils.ISO8601_DATE_FORMAT.parse(sTimeString);
 //            sTimeString = usgsUrlSdf.format(sTime);
 //        } catch (ParseException e) {
 //            log.error("Error parsing start time - the start time will not be specified", e);
 //            sTimeString = null;
 ////            throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.START_TIME + "Unparsable value = " + sTimeString, e);
 //        }
 //
 //        if (sTimeString == null) {
 //            sTimeString =
 //        }
 //
 //        try {
 //            eTime = AgentUtils.ISO8601_DATE_FORMAT.parse(eTimeString);
 //            eTimeString = usgsUrlSdf.format(eTime);
 //        } catch (ParseException e) {
 //            eTimeString = null;
 //            log.error("Error parsing end time - the end time will not be specified", e);
 ////            throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.END_TIME + "Unparsable value = " + eTimeString, e);
 //        }
 //
 //
 //        if (isDailyQuery) {
 //            // http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?SiteNum=01463500&ParameterCode=00060&StatisticCode=00003&StartDate=2003-01-01
 //            // http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?SiteNum=01463500&ParameterCode=00060&StatisticCode=00003&StartDate=2003-01-01&EndDate=2011-03-15
 //            result.append(baseUrl);
 //            result.append("SiteNum=").append(siteCodes[0]);
 //            result.append("&ParameterCode=").append(properties[0]);
 //            result.append("&StatisticCode=00003");//Mean only for now
 //            if (sTimeString != null && !sTimeString.isEmpty()) {
 //                result.append("&StartDate=").append(sTimeString);
 //            }
 //            if (eTimeString != null && !eTimeString.isEmpty()) {
 //                result.append("&EndDate=").append(eTimeString);
 //            }
 //        } else {
 //            /** Build the propertiesString*/
 //            StringBuilder propertiesString = new StringBuilder();
 //            for (String property : properties) {
 //                if (null != property) {
 //                    propertiesString.append(property.trim()).append(",");
 //                }
 //            }
 //            if (propertiesString.length() > 0) {
 //                propertiesString.deleteCharAt(propertiesString.length() - 1);
 //            }
 //
 //            /** Build the list of sites (siteCSV)*/
 //            StringBuilder siteCSV = new StringBuilder();
 //            for (String siteCode : siteCodes) {
 //                if (null != siteCode) {
 //                    siteCSV.append(siteCode.trim()).append(",");
 //                }
 //            }
 //            if (siteCSV.length() > 0) {
 //                siteCSV.deleteCharAt(siteCSV.length() - 1);
 //            }
 //
 //            /** Build the query URL */
 //            result.append(baseUrl);
 //            result.append("&sites=").append(siteCSV);
 //            result.append("&parameterCd=").append(propertiesString);
 //            if (sTimeString != null && !sTimeString.isEmpty()) {
 //                result.append("&startDT=").append(sTimeString);
 //            }
 //            if (eTimeString != null && !eTimeString.isEmpty()) {
 //                result.append("&endDT=").append(eTimeString);
 //            }
 //        }
 
 
         log.debug("... built request: [" + result + "]");
         return result.toString();
     }
 
     private String buildWaterServicesRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage context) {
         StringBuilder result = new StringBuilder();
 
         String baseUrl = context.getBaseUrl();
         String sTimeString = context.getStartTime();
         String eTimeString = context.getEndTime();
         String properties[] = context.getPropertyList().toArray(new String[0]);
         String siteCodes[] = context.getStationIdList().toArray(new String[0]);
 
 
         /** TODO: null-check here */
         /** Configure the date-time parameter */
         Date sTime = null;
         Date eTime = null;
         try {
             sTime = AgentUtils.ISO8601_DATE_FORMAT.parse(sTimeString);
         } catch (ParseException e) {
             throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.START_TIME + "Unparsable value = " + sTimeString, e);
         }
         try {
             eTime = AgentUtils.ISO8601_DATE_FORMAT.parse(eTimeString);
         } catch (ParseException e) {
             throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.END_TIME + "Unparsable value = " + eTimeString, e);
         }
         DateFormat usgsUrlSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
         usgsUrlSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         sTimeString = usgsUrlSdf.format(sTime);
         eTimeString = usgsUrlSdf.format(eTime);
 
 
 
         /** Build the propertiesString*/
         StringBuilder propertiesString = new StringBuilder();
         for (String property : properties) {
             if (null != property) {
                 propertiesString.append(property.trim()).append(",");
             }
         }
         if (propertiesString.length() > 0) {
             propertiesString.deleteCharAt(propertiesString.length() - 1);
         }
 
 
 
         /** Build the list of sites (siteCSV)*/
         StringBuilder siteCSV = new StringBuilder();
         for (String siteCode : siteCodes) {
             if (null != siteCode) {
                 siteCSV.append(siteCode.trim()).append(",");
             }
         }
         if (siteCSV.length() > 0) {
             siteCSV.deleteCharAt(siteCSV.length() - 1);
         }
 
 
 
         /** Build the query URL */
         result.append(baseUrl);
         result.append("&sites=").append(siteCSV);
         result.append("&parameterCd=").append(propertiesString);
         result.append("&startDT=").append(sTimeString);
         result.append("&endDT=").append(eTimeString);
 
 
 
         log.debug("... built request: [" + result + "]");
         return result.toString();
     }
 
     private String buildDailyValuesRequest(net.ooici.services.sa.DataSource.EoiDataContextMessage context) {
         StringBuilder result = new StringBuilder();
 
         String baseUrl = context.getBaseUrl();
         String sTimeString = context.getStartTime();
         String eTimeString = context.getEndTime();
         String properties[] = context.getPropertyList().toArray(new String[0]);
         String siteCodes[] = context.getStationIdList().toArray(new String[0]);
 
 
         /** TODO: null-check here */
         /** Configure the date-time parameter */
         Date sTime = null;
         Date eTime = null;
         try {
             sTime = AgentUtils.ISO8601_DATE_FORMAT.parse(sTimeString);
         } catch (ParseException e) {
             throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.START_TIME + "Unparsable value = " + sTimeString, e);
         }
         try {
             eTime = AgentUtils.ISO8601_DATE_FORMAT.parse(eTimeString);
         } catch (ParseException e) {
             throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.END_TIME + "Unparsable value = " + eTimeString, e);
         }
         DateFormat usgsUrlSdf = new SimpleDateFormat("yyyy-MM-dd");
         usgsUrlSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         sTimeString = usgsUrlSdf.format(sTime);
         eTimeString = usgsUrlSdf.format(eTime);
 
         //TODO: If eTimeString is empty/null, set to "now"
 
         /** Build the query URL */
         result.append(baseUrl);
         result.append("SiteNum=").append(siteCodes[0]);
         result.append("&ParameterCode=").append(properties[0]);
         result.append("&StatisticCode=00003");//Mean only for now
         if (sTimeString != null && !sTimeString.isEmpty()) {
             result.append("&StartDate=").append(sTimeString);
         }
         if (eTimeString != null && !eTimeString.isEmpty()) {
             result.append("&EndDate=").append(eTimeString);
         }
 
         log.debug("... built request: [" + result + "]");
         return result.toString();
     }
 
     /* (non-Javadoc)
      * @see net.ooici.agent.abstraction.AbstractAsciiAgent#parseObss(java.lang.String)
      */
     @Override
     protected List<IObservationGroup> parseObs(String asciiData) {
         log.debug("");
         log.info("Parsing observations from data [" + asciiData.substring(0, Math.min(asciiData.length(), 40)) + "...]");
 
         net.ooici.SysClipboard.copyString(asciiData);
 
         List<IObservationGroup> obsList = new ArrayList<IObservationGroup>();
         StringReader srdr = new StringReader(asciiData);
         try {
             if(!isDailyValue) {
                 obsList.add(ws_parseObservations(srdr));
             } else {
                 obsList.add(dv_parseObservations(srdr));
             }
         } finally {
             if (srdr != null) {
                 srdr.close();
             }
         }
 
         return obsList;
     }
 
     /**
      * Parses the String data from the given reader into a list of observation groups.
      * <em>Note:</em><br />
      * The given reader is guaranteed to return from this method in a <i>closed</i> state.
      * @param rdr
      * @return a List of IObservationGroup objects if observations are parsed, otherwise this list will be empty
      */
     public static IObservationGroup ws_parseObservations(Reader rdr) {
         /* TODO: Fix exception handling in this method, it is too generic; try/catch blocks should be as confined as possible */
 
         /** XPATH queries */
         final String XPATH_ELEMENT_TIME_SERIES = ".//ns1:timeSeries";
         final String XPATH_ELEMENT_SITE_CODE = "./ns1:sourceInfo/ns1:siteCode";
         final String XPATH_ATTRIBUTE_AGENCY_CODE = "./ns1:sourceInfo/ns1:siteCode/@agencyCode";
         final String XPATH_ELEMENT_LATITUDE = "./ns1:sourceInfo/ns1:geoLocation/ns1:geogLocation/ns1:latitude"; /* NOTE: geogLocation is (1..*) */
 
         final String XPATH_ELEMENT_LONGITUDE = "./ns1:sourceInfo/ns1:geoLocation/ns1:geogLocation/ns1:longitude"; /* NOTE: geogLocation is (1..*) */
 
         final String XPATH_ELEMENT_VALUE = "./ns1:values/ns1:value";
         final String XPATH_ELEMENT_VARIABLE_CODE = "./ns1:variable/ns1:variableCode";
         final String XPATH_ELEMENT_VARIABLE_NAME = "./ns1:variable/ns1:variableName";
         final String XPATH_ELEMENT_VARIABLE_NaN_VALUE = "./ns1:variable/ns1:noDataValue";
         final String XPATH_ATTRIBUTE_QUALIFIERS = "./@qualifiers";
         final String XPATH_ATTRIBUTE_DATETIME = "./@dateTime";
 
 
         IObservationGroup obs = null;
         SAXBuilder builder = new SAXBuilder();
         Document doc = null;
         String datetime = "[no date information]"; /* datetime defined here (outside try) for error reporting */
 
         try {
             doc = builder.build(rdr);
 
             /** Grab Global Attributes (to be copied into each observation group */
             Namespace ns1 = Namespace.getNamespace("ns1", "http://www.cuahsi.org/waterML/1.1/");
 //          Namespace ns2 = Namespace.getNamespace("ns2", "http://waterservices.usgs.gov/WaterML-1.1.xsd");
 //          Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema");
 
 
             Element root = doc.getRootElement();
             Element queryInfo = root.getChild("queryInfo", ns1);
             Map<String, String> globalAttributes = new HashMap<String, String>();
 
 
             /* Extract the Global Attributes */
             /* title */
             String queryUrl = xpathSafeSelectValue(queryInfo, ".//ns2:queryURL", null);
             globalAttributes.put("title", "USGS rivers data timeseries.  Requested from \"" + queryUrl + "\"");
 
             /* history */
             globalAttributes.put("history", "Converted from WaterML1.1 to OOI CDM compliant NC by " + UsgsAgent.class.getName());
 
             /* references */
             globalAttributes.put("references", new StringBuilder().append("[").append("http://waterservices.usgs.gov/mwis/iv?").append("]").toString());
 
 //            /* utc_start_time */
 //            Date tempDate = new Date(0);
 //            String beginDate = xpathSafeSelectValue(queryInfo, ".//ns2:timeParam/ns2:beginDateTime", null);
 //
 //            tempDate = inSdf.parse(beginDate);
 //            String beginDateISO = outSdf.format(tempDate);
 //            globalAttributes.put("utc_start_time", beginDateISO);
 //
 //
 //            /* utc_end_time */
 //            tempDate = new Date(0);
 //            String endDate = xpathSafeSelectValue(queryInfo, ".//ns2:timeParam/ns2:endDateTime", null);
 //
 //
 //            tempDate = inSdf.parse(endDate);
 //            String endDateISO = outSdf.format(tempDate);
 //            globalAttributes.put("utc_end_time", endDateISO);
 
 
             /* conventions */
             globalAttributes.put("Conventions", "CF-1.5");
 
 
 
             /** Get a list of provided time series */
             List<?> timeseriesList = XPath.selectNodes(doc, XPATH_ELEMENT_TIME_SERIES);
 
 
 
             /** Build an observation group for each unique sitecode */
             Object nextTimeseries = null;
             Iterator<?> iterTimeseries = timeseriesList.iterator();
             while (iterTimeseries.hasNext()) {
                 /* Grab the next element */
                 nextTimeseries = iterTimeseries.next();
                 if (null == nextTimeseries) {
                     continue;
                 }
 
 
                 /** Grab data for the current site */
                 String stnId = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_SITE_CODE)).getTextTrim();
                 String latitude = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_LATITUDE)).getTextTrim();
                 String longitude = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_LONGITUDE)).getTextTrim();
                 String noDataString = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_VARIABLE_NaN_VALUE)).getTextTrim();
 
                 float lat = Float.parseFloat(latitude);
                 float lon = Float.parseFloat(longitude);
                 // float noDataValue = Float.parseFloat(noDataString);
 
 
                 /* Check to see if the observation group already exists */
                 if (obs == null) {
                     /* Create a new observation group if one does not currently exist */
                     obs = new ObservationGroupImpl(getNextGroupId(), stnId, lat, lon);
                 }
 
 
 
                 /** Grab variable data */
                 String variableCode = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_VARIABLE_CODE)).getTextTrim();
                 // String variableUnits = getUnitsForVariableCode(variableCode);
                 // String variableName = getStdNameForVariableCode(variableCode);
 
 
 
                 /** Add each timeseries value (observation) to the observation group */
                 /* Get a list of each observation */
                 List<?> observationList = XPath.selectNodes(nextTimeseries, XPATH_ELEMENT_VALUE);
 
 
                 /* Add an observation for each "value" parsed */
                 Object next = null;
                 Iterator<?> iter = observationList.iterator();
                 while (iter.hasNext()) {
                     /* Grab the next element */
                     next = iter.next();
                     if (null == next) {
                         continue;
                     }
 
                     /* Grab observation data */
                     // String qualifier = ((org.jdom.Attribute) XPath.selectSingleNode(next, XPATH_ATTRIBUTE_QUALIFIERS)).getValue();
                     datetime = ((org.jdom.Attribute) XPath.selectSingleNode(next, XPATH_ATTRIBUTE_DATETIME)).getValue();
                     String value = ((Element) next).getTextTrim();
                     datetime = datetime.replaceAll("\\:", "");
 
 
                     /* Convert observation data */
                     int time = 0;
                     float data = 0;
                     float dpth = 0;
                     VariableParams name = null;
 
                     time = (int) (valueSdf.parse(datetime).getTime() * 0.001);
                     // data = Float.parseFloat(value);
                     name = getDataNameForVariableCode(variableCode);
                     /* Only convert data if we are dealing with Steamflow) */
                     if (name == VariableParams.RIVER_STREAMFLOW) {
                         data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value) * CONVERT_FT3_TO_M3); /* convert from (f3 s-1) --> (m3 s-1) */
                     } else {
                         data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value));
                     }
                     dpth = 0;
 
 
                     /* Add the observation data */
                     obs.addObservation(time, dpth, data, new VariableParams(name, DataType.FLOAT));
 
                     /* TODO: Add the data observation qualifier */
                     // og.addObservation(time, dpth, qualifier, new VariableParams(name, DataType.FLOAT));
 
                 }
 
 
                 /** Grab attributes */
                 Map<String, String> tsAttributes = new TreeMap<String, String>();
 
 
                 /* Extract timeseries-specific attributes */
                 String sitename = xpathSafeSelectValue(nextTimeseries, "//ns1:siteName", "[n/a]");
                 String network = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode/@network", "[n/a]");
                 String agency = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode/@agencyCode", "[n/a]");
                 String sCode = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode", "[n/a]");
                 tsAttributes.put("institution", new StringBuilder().append(sitename).append(" (network:").append(network).append("; agencyCode:").append(agency).append("; siteCode:").append(sCode).append(";)").toString());
 
                 String method = xpathSafeSelectValue(nextTimeseries, ".//ns1:values//ns1:methodDescription", "[n/a]");
                 tsAttributes.put("source", method);
 
 
 
                 /** Add global and timeseries attributes */
                 obs.addAttributes(globalAttributes);
                 obs.addAttributes(tsAttributes);
 
 
             }
 
         } catch (JDOMException ex) {
             log.error("Error while parsing xml from the given reader", ex);
         } catch (IOException ex) {
             log.error("General IO exception.  Please see stack-trace", ex);
         } catch (ParseException ex) {
             log.error("Could not parse date information from XML result for: " + datetime, ex);
         }
 
         return obs;
     }
 
     /**
      * Parses the String data from the given reader into a list of observation groups.
      * <em>Note:</em><br />
      * The given reader is guaranteed to return from this method in a <i>closed</i> state.
      * @param rdr
      * @return a List of IObservationGroup objects if observations are parsed, otherwise this list will be empty
      */
     public static IObservationGroup dv_parseObservations(Reader rdr) {
         /* TODO: Fix exception handling in this method, it is too generic; try/catch blocks should be as confined as possible */
         
         /** XPATH queries */
         final String XPATH_ELEMENT_TIME_SERIES = ".//ns1:timeSeries";
         final String XPATH_ELEMENT_SITE_CODE = "./ns1:sourceInfo/ns1:siteCode";
         final String XPATH_ATTRIBUTE_AGENCY_CODE = "./ns1:sourceInfo/ns1:siteCode/@agencyCode";
         final String XPATH_ELEMENT_LATITUDE = "./ns1:sourceInfo/ns1:geoLocation/ns1:geogLocation/ns1:latitude"; /* NOTE: geogLocation is (1..*) */
 
         final String XPATH_ELEMENT_LONGITUDE = "./ns1:sourceInfo/ns1:geoLocation/ns1:geogLocation/ns1:longitude"; /* NOTE: geogLocation is (1..*) */
 
         final String XPATH_ELEMENT_VALUE = "./ns1:values/ns1:value";
         final String XPATH_ELEMENT_VARIABLE_CODE = "./ns1:variable/ns1:variableCode";
         final String XPATH_ELEMENT_VARIABLE_NAME = "./ns1:variable/ns1:variableName";
         final String XPATH_ELEMENT_VARIABLE_NaN_VALUE = "./ns1:variable/ns1:NoDataValue";
         final String XPATH_ATTRIBUTE_QUALIFIERS = "./@qualifiers";
         final String XPATH_ATTRIBUTE_DATETIME = "./@dateTime";
         
         
         IObservationGroup obs = null;
         SAXBuilder builder = new SAXBuilder();
         Document doc = null;
         String datetime = "[no date information]"; /* datetime defined here (outside try) for error reporting */
 
         try {
             doc = builder.build(rdr);
 
             /** Grab Global Attributes (to be copied into each observation group */
             Namespace ns = Namespace.getNamespace("ns1", "http://www.cuahsi.org/waterML/1.0/");
 //            Namespace ns1 = Namespace.getNamespace("ns1", "http://www.cuahsi.org/waterML/1.1/");
 //          Namespace ns2 = Namespace.getNamespace("ns2", "http://waterservices.usgs.gov/WaterML-1.1.xsd");
 //          Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema");
 
 
             Element root = doc.getRootElement();
             root.setNamespace(ns);
 //            Element queryInfo = root.getChild("queryInfo", ns);
             Map<String, String> globalAttributes = new HashMap<String, String>();
 
 
             /* Extract the Global Attributes */
             /* title */
 //            String queryUrl = xpathSafeSelectValue(queryInfo, ".//ns2:queryURL", null);
             globalAttributes.put("title", "USGS rivers data timeseries.  Requested from \"NWIS DailyValues Service\"");
 
             /* history */
             globalAttributes.put("history", "Converted from WaterML1.1 to OOI CDM compliant NC by " + UsgsAgent.class.getName());
 
             /* references */
             globalAttributes.put("references", new StringBuilder().append("[").append("http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?").append("]").toString());
 
 //            /* utc_start_time */
 //            Date tempDate = new Date(0);
 //            String beginDate = xpathSafeSelectValue(queryInfo, ".//ns2:timeParam/ns2:beginDateTime", null);
 //
 //            tempDate = inSdf.parse(beginDate);
 //            String beginDateISO = outSdf.format(tempDate);
 //            globalAttributes.put("utc_start_time", beginDateISO);
 //
 //
 //            /* utc_end_time */
 //            tempDate = new Date(0);
 //            String endDate = xpathSafeSelectValue(queryInfo, ".//ns2:timeParam/ns2:endDateTime", null);
 //
 //
 //            tempDate = inSdf.parse(endDate);
 //            String endDateISO = outSdf.format(tempDate);
 //            globalAttributes.put("utc_end_time", endDateISO);
 
 
             /* conventions */
             globalAttributes.put("Conventions", "CF-1.5");
 
 
 
             /** Get a list of provided time series */
             List<?> timeseriesList = XPath.selectNodes(root, ".//ns1:timeSeries");
 //            List<?> timeseriesList = XPath.selectNodes(doc, XPATH_ELEMENT_TIME_SERIES);
 //            List<?> timeseriesList = XPath.selectNodes(doc, ".//timeSeries");
 //            List<?> timeseriesList = root.getChildren("timeSeries", ns);
 
             System.out.println("Total timeseries in doc: " + timeseriesList.size());
 
 
             /** Build an observation group for each unique sitecode */
             Object nextTimeseries = null;
             Iterator<?> iterTimeseries = timeseriesList.iterator();
             while (iterTimeseries.hasNext()) {
                 /* Grab the next element */
                 nextTimeseries = iterTimeseries.next();
                 if (null == nextTimeseries) {
                     continue;
                 }
 
 
                 /** Grab data for the current site */
                 String stnId = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_SITE_CODE)).getTextTrim();
                 String latitude = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_LATITUDE)).getTextTrim();
                 String longitude = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_LONGITUDE)).getTextTrim();
                 String noDataString = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_VARIABLE_NaN_VALUE)).getTextTrim();
 
                 float lat = Float.parseFloat(latitude);
                 float lon = Float.parseFloat(longitude);
                 // float noDataValue = Float.parseFloat(noDataString);
 
 
                 /* Check to see if the observation group already exists */
                 if (obs == null) {
                     /* Create a new observation group if one does not currently exist */
                     obs = new ObservationGroupImpl(getNextGroupId(), stnId, lat, lon);
                 }
 
 
 
                 /** Grab variable data */
                 String variableCode = ((Element) XPath.selectSingleNode(nextTimeseries, XPATH_ELEMENT_VARIABLE_CODE)).getTextTrim();
                 // String variableUnits = getUnitsForVariableCode(variableCode);
                 // String variableName = getStdNameForVariableCode(variableCode);
 
 
 
                 /** Add each timeseries value (observation) to the observation group */
                 /* Get a list of each observation */
                 List<?> observationList = XPath.selectNodes(nextTimeseries, XPATH_ELEMENT_VALUE);
 
 
                 /* Add an observation for each "value" parsed */
                 Object next = null;
                 Iterator<?> iter = observationList.iterator();
                 while (iter.hasNext()) {
                     /* Grab the next element */
                     next = iter.next();
                     if (null == next) {
                         continue;
                     }
 
                     /* Grab observation data */
                     // String qualifier = ((org.jdom.Attribute) XPath.selectSingleNode(next, XPATH_ATTRIBUTE_QUALIFIERS)).getValue();
                     datetime = ((org.jdom.Attribute) XPath.selectSingleNode(next, XPATH_ATTRIBUTE_DATETIME)).getValue();
                     String value = ((Element) next).getTextTrim();
                     datetime = datetime.replaceAll("\\:", "");
 
 
                     /* Convert observation data */
                     int time = 0;
                     float data = 0;
                     float dpth = 0;
                     VariableParams name = null;
 
                     final SimpleDateFormat dvInputSdf;
                     dvInputSdf = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
                     dvInputSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                     
                     time = (int) (dvInputSdf.parse(datetime).getTime() * 0.001);
                     // data = Float.parseFloat(value);
                     name = getDataNameForVariableCode(variableCode);
                     /* Only convert data if we are dealing with Steamflow) */
                     if (name == VariableParams.RIVER_STREAMFLOW) {
                         data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value) * CONVERT_FT3_TO_M3); /* convert from (f3 s-1) --> (m3 s-1) */
                     } else {
                         data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value));
                     }
                     dpth = 0;
 
 
                     /* Add the observation data */
                     obs.addObservation(time, dpth, data, new VariableParams(name, DataType.FLOAT));
 
                     /* TODO: Add the data observation qualifier */
                     // og.addObservation(time, dpth, qualifier, new VariableParams(name, DataType.FLOAT));
 
                 }
 
 
                 /** Grab attributes */
                 Map<String, String> tsAttributes = new TreeMap<String, String>();
 
 
                 /* Extract timeseries-specific attributes */
                 String sitename = xpathSafeSelectValue(nextTimeseries, "//ns1:siteName", "[n/a]");
                 String network = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode/@network", "[n/a]");
                 String agency = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode/@agencyCode", "[n/a]");
                 String sCode = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode", "[n/a]");
                 tsAttributes.put("institution", new StringBuilder().append(sitename).append(" (network:").append(network).append("; agencyCode:").append(agency).append("; siteCode:").append(sCode).append(";)").toString());
 
                 String method = xpathSafeSelectValue(nextTimeseries, ".//ns1:values//ns1:methodDescription", "[n/a]");
                 tsAttributes.put("source", method);
 
 
 
                 /** Add global and timeseries attributes */
                 obs.addAttributes(globalAttributes);
                 obs.addAttributes(tsAttributes);
 
 
             }
 
         } catch (JDOMException ex) {
             log.error("Error while parsing xml from the given reader", ex);
         } catch (IOException ex) {
             log.error("General IO exception.  Please see stack-trace", ex);
         } catch (ParseException ex) {
             log.error("Could not parse date information from XML result for: " + datetime, ex);
         }
 
         return obs;
     }
 
     private static String xpathSafeSelectValue(Object context, String path, String defaultValue, Namespace... namespaces) {
 
         Object result = null;
         try {
             XPath xp = XPath.newInstance(path);
             if (null != namespaces) {
                 for (Namespace namespace : namespaces) {
                     xp.addNamespace(namespace);
                 }
             }
             result = xp.selectSingleNode(context);
         } catch (JDOMException ex) {
             log.debug("Could not select node via XPath query: \"" + path + "\"", ex);
         }
 
 
         return xpathNodeValue(result, defaultValue);
     }
 
     /**
      * <b>Note:</b><br />
      * This method does not support all types of returns from XPath.selectSingleNode().
      * It will currently support:<br />
      *      org.jdom.Attribute<br />
      *      org.jdom.Element<br />
      * @param node
      * @param defaultValue
      * @return
      */
     private static String xpathNodeValue(Object node, String defaultValue) {
         /* Overwrite defaultValue if value can be retrieved from node */
         if (node instanceof org.jdom.Attribute) {
             defaultValue = ((org.jdom.Attribute) node).getValue();
         } else if (node instanceof org.jdom.Element) {
             defaultValue = ((org.jdom.Element) node).getText();
         }
 
 
         return defaultValue;
     }
 
     protected static int getCurrentGroupId() {
         return currentGroupId;
     }
 
     protected static int getNextGroupId() {
         return ++currentGroupId;
     }
 
     protected static VariableParams getDataNameForVariableCode(String variableCode) {
         VariableParams result = null;
 
         if ("00010".equals(variableCode)) {
             result = VariableParams.WATER_TEMPERATURE;
         } else if ("00060".equals(variableCode)) {
             result = VariableParams.RIVER_STREAMFLOW;
         } else {
             throw new IllegalArgumentException("Given variable code is not known: " + variableCode);
         }
 
         return result;
     }
 
     @Override
     public String[] processDataset(IObservationGroup... obsList) {
         List<String> ret = new ArrayList<String>();
         NetcdfDataset ncds = obs2Ncds(obsList);
         /* Send this via the send dataset method of DAC */
         ret.add(this.sendNetcdfDataset(ncds, "ingest"));
         return ret.toArray(new String[0]);
     }
 
     /*****************************************************************************************************************/
     /* Testing                                                                                                       */
     /*****************************************************************************************************************/
     public static void main(String[] args) throws IOException {
         try {
             ion.core.IonBootstrap.bootstrap();
         } catch (Exception ex) {
             log.error("Error bootstrapping", ex);
         }
 
         boolean makeSamples = true;
         if (makeSamples) {
             generateRutgersSamples();
         } else {
             net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
             cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
             cBldr.setBaseUrl("http://waterservices.usgs.gov/nwis/iv?");
             int switcher = 4;
             switch (switcher) {
                 case 1://test temp
                     cBldr.setStartTime("2011-2-10T00:00:00Z");
                     cBldr.setEndTime("2011-2-11T00:00:00Z");
                     cBldr.addProperty("00010");
                     cBldr.addStationId("01463500");
                     break;
                 case 2://test discharge
                     cBldr.setStartTime("2011-2-10T00:00:00Z");
                     cBldr.setEndTime("2011-2-11T00:00:00Z");
                     cBldr.addProperty("00060");
                     cBldr.addStationId("01463500");
                     break;
                 case 3://test temp & discharge
                     cBldr.setStartTime("2011-2-10T00:00:00Z");
                     cBldr.setEndTime("2011-2-11T00:00:00Z");
                     cBldr.addProperty("00010");
                     cBldr.addProperty("00060");
                     cBldr.addStationId("01463500");
                     break;
                 case 4:
                     cBldr.setBaseUrl("http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?");
                     cBldr.setStartTime("2003-01-01T00:00:00Z");
 //                    cBldr.setStartTime("2011-02-01T00:00:00Z");
                     cBldr.setEndTime("2011-03-01T00:00:00Z");
                     cBldr.addProperty("00060");
                     cBldr.addStationId("01463500");
                     break;
             }
 //            cBldr.setStartTime("2011-01-29T00:00:00Z");
 //            cBldr.setEndTime("2011-01-31T00:00:00Z");
 //            cBldr.addProperty("00010");
 //            cBldr.addProperty("00060");
 //            cBldr.addAllStationId(java.util.Arrays.asList(new String[] {"01184000", "01327750", "01357500", "01389500", "01403060", "01463500", "01578310", "01646500", "01592500", "01668000", "01491000", "02035000", "02041650", "01673000", "01674500", "01362500", "01463500", "01646500" }));
 
             runAgent(cBldr.build());
         }
     }
 
     private static void generateRutgersSamples() throws IOException {
         /* Generates samples for near-realtime high-resolution data */
         String baseURL = "http://waterservices.usgs.gov/nwis/iv?";
         String sTime = "2011-03-01T00:00:00Z";
         String eTime = "2011-03-10T00:00:00Z";
 
         /* Generates samples for "historical" low-resolution data */
         baseURL = "http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?";
         sTime = "2003-01-01T00:00:00Z";
         eTime = "2011-03-17T00:00:00Z";
         String[] disIds = new String[]{"01184000", "01327750", "01357500", "01389500", "01403060", "01463500", "01578310", "01646500", "01592500", "01668000", "01491000", "02035000", "02041650", "01673000", "01674500"};
         String[] disNames = new String[]{"Connecticut", "Hudson", "Mohawk", "Passaic", "Raritan", "Delaware", "Susquehanna", "Potomac", "Patuxent", "Rappahannock", "Choptank", "James", "Appomattox", "Pamunkey", "Mattaponi"};
         String[] tempIds = new String[]{"01362500", "01463500", "01646500"};
         String[] tempNames = new String[]{"Hudson", "Delware", "Potomac"};
 
         for (int i = 0; i < disIds.length; i++) {
             net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
             cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
             cBldr.setBaseUrl(baseURL);
             cBldr.setStartTime(sTime);
             cBldr.setEndTime(eTime);
             cBldr.addProperty("00060");
             cBldr.addStationId(disIds[i]);
             String[] res = runAgent(cBldr.build());
             NetcdfDataset dsout = null;
             try {
                 dsout = NetcdfDataset.openDataset("ooici:" + res[0]);
                 ucar.nc2.FileWriter.writeToFile(dsout, "/Users/cmueller/Dropbox/EOI_Shared/dataset_samples/rutgers/Rivers/" + disNames[i] + "_discharge.nc");
             } catch (IOException ex) {
                 log.error("Error writing netcdf file", ex);
             } finally {
                 if (dsout != null) {
                     dsout.close();
                 }
             }
         }
 
         for (int i = 0; i < tempIds.length; i++) {
             net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
             cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
             cBldr.setBaseUrl(baseURL);
             cBldr.setStartTime(sTime);
             cBldr.setEndTime(eTime);
             cBldr.addProperty("00010");
             cBldr.addStationId(tempIds[i]);
             String[] res = runAgent(cBldr.build());
             NetcdfDataset dsout = null;
             try {
                 dsout = NetcdfDataset.openDataset("ooici:" + res[0]);
                 ucar.nc2.FileWriter.writeToFile(dsout, "/Users/cmueller/Dropbox/EOI_Shared/dataset_samples/rutgers/Rivers/" + tempNames[i] + "_temp.nc");
             } catch (IOException ex) {
                 log.error("Error writing netcdf file", ex);
             } finally {
                 if (dsout != null) {
                     dsout.close();
                 }
             }
         }
 
         System.out.println("******FINISHED******");
     }
 
     private static String[] runAgent(net.ooici.services.sa.DataSource.EoiDataContextMessage context) throws IOException {
         net.ooici.eoi.datasetagent.IDatasetAgent agent = net.ooici.eoi.datasetagent.AgentFactory.getDatasetAgent(context.getSourceType());
 //        agent.setTesting(true);
 
 //        java.util.HashMap<String, String> connInfo = IospUtils.parseProperties(new java.io.File(System.getProperty("user.dir") + "/ooici-conn.properties"));
 //        java.util.HashMap<String, String> connInfo = new java.util.HashMap<String, String>();
 //        connInfo.put("exchange", "eoitest");
 //        connInfo.put("service", "eoi_ingest");
 //        connInfo.put("server", "localhost");
 //        connInfo.put("topic", "magnet.topic");
         java.util.HashMap<String, String> connInfo = null;
         try {
             connInfo = ion.core.utils.IonUtils.parseProperties();
         } catch (IOException ex) {
             log.error("Error parsing \"ooici-conn.properties\" cannot continue.", ex);
             System.exit(1);
         }
         String[] result = agent.doUpdate(context, connInfo);
         log.debug("Response:");
         for (String s : result) {
             log.debug(s);
         }
         return result;
     }
 }
