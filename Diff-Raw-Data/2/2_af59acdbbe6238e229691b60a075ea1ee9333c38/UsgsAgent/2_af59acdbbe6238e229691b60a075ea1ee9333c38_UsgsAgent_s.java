 /*
  * File Name:  UsgsAgent.java
  * Created on: Dec 20, 2010
  */
 package net.ooici.eoi.datasetagent.impl;
 
 import ion.core.IonException;
 import ion.core.utils.GPBWrapper;
 import ion.core.utils.IonUtils;
 import java.io.BufferedReader;
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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.ooici.eoi.datasetagent.obs.IObservationGroup;
 import net.ooici.eoi.datasetagent.obs.IObservationGroup.DataType;
 import net.ooici.eoi.datasetagent.obs.ObservationGroupImpl;
 import net.ooici.eoi.netcdf.VariableParams;
 import net.ooici.eoi.netcdf.VariableParams.StandardVariable;
 import net.ooici.eoi.datasetagent.AbstractAsciiAgent;
 import net.ooici.eoi.datasetagent.AgentFactory;
 import net.ooici.eoi.datasetagent.AgentUtils;
 import net.ooici.eoi.netcdf.NcDumpParse;
 import net.ooici.services.dm.IngestionService.DataAcquisitionCompleteMessage.StatusCode;
 import net.ooici.services.sa.DataSource.EoiDataContextMessage;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.xpath.XPath;
 
 import ucar.nc2.dataset.NetcdfDataset;
 
 /**
  * The UsgsAgent class is designed to fulfill updates for datasets which originate from USGS services. Ensure the update context (
  * {@link EoiDataContextMessage}) to be passed to {@link #doUpdate(EoiDataContextMessage, HashMap)} has been constructed for USGS agents by
  * checking the result of {@link EoiDataContextMessage#getSourceType()}
  *
  * @author cmueller
  * @author tlarocque
  * @version 1.0
  * @see {@link EoiDataContextMessage#getSourceType()}
  * @see {@link AgentFactory#getDatasetAgent(net.ooici.services.sa.DataSource.SourceType)}
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
     private static final String USR_HOME = System.getProperty("user.home");
     /** Maths */
     public static final double CONVERT_FT_TO_M = 0.3048;
     public static final double CONVERT_FT3_TO_M3 = Math.pow(CONVERT_FT_TO_M, 3);
     private boolean isDailyValue = false;
     private WSType wsType = WSType.IV;
     private String data_url;
 
     /** Static Initializer */
     static {
         valueSdf = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss.sssZ");
         valueSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         inSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
         inSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     enum WSType {
 
         IV,
         DV,
         IDV
     }
 
     /** Used to convert qualifier code to a byte storage value */
     enum Qualifier {
 
         PROVISIONAL("P", (byte) 1), APPROVED("A", (byte) 2);
         static final byte DEFAULT_VALUE = 0;
         final String code;
         final byte byteValue;
 
         Qualifier(String code, byte byteValue) {
             this.code = code;
             this.byteValue = byteValue;
         }
 
         public static byte getByteValue(String code) {
             byte result = DEFAULT_VALUE;
             for (Qualifier value : Qualifier.values()) {
                 if (value.code.equals(code)) {
                     result = value.byteValue;
                     break;
                 }
             }
 
             return result;
         }
     }
 
     /**
      * Constructs a URL from the given data <code>context</code> by appending necessary USGS-specific query string parameters to the base URL
      * returned by <code>context.getBaseUrl()</code>. This URL may subsequently be passed through {@link #acquireData(String)} to procure
      * updated data according to the <code>context</code> given here.  Requests may be built differently depending on which USGS service
      * <code>context.getBaseUrl()</code> specifies.  Valid services include the WaterService webservice and the DailyValues service.
      * 
      * @param context
      *            the current or required state of a USGS dataset providing context for building data requests to fulfill dataset updates
      * @return A dataset update request URL built from the given <code>context</code> against a USGS service.
      */
     @Override
     public String buildRequest() {
         log.debug("");
         log.info("Building Request for context [" + context.toString() + "...]");
 
         String result = "";
 
         String baseurl = context.getBaseUrl();
         if (baseurl.endsWith("nwis/iv?")) {
             result = buildWaterServicesIVRequest();
         } else if (baseurl.endsWith("nwis/dv?")) {
             result = buildWaterServicesDVRequest();
             wsType = WSType.DV;
         } else if (baseurl.endsWith("NWISQuery/GetDV1?")) {
             result = buildInterimWaterServicesDVRequest();
             wsType = WSType.IDV;
         }
 
 // <editor-fold defaultstate="collapsed" desc="OLD - COMMENTED">
 
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
 // </editor-fold>
         data_url = result.toString();
         log.debug("... built request: [" + data_url + "]");
         return data_url;
     }
 
     private String buildWaterServicesIVRequest() {
         StringBuilder result = new StringBuilder();
 
         String baseUrl = context.getBaseUrl();
 //        String sTimeString = context.getStartTime();
 //        String eTimeString = context.getEndTime();
         String properties[] = context.getPropertyList().toArray(new String[0]);
         String siteCodes[] = context.getStationIdList().toArray(new String[0]);
 
 
         /** TODO: null-check here */
         /** Configure the date-time parameter */
         Date sTime = null;
         Date eTime = null;
         if (context.hasStartDatetimeMillis()) {
             sTime = new Date(context.getStartDatetimeMillis());
         }
         if (context.hasEndDatetimeMillis()) {
             eTime = new Date(context.getEndDatetimeMillis());
         }
 //        try {
 //            sTime = AgentUtils.ISO8601_DATE_FORMAT.parse(sTimeString);
 //        } catch (ParseException e) {
 //            throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.START_TIME + "Unparsable value = " + sTimeString, e);
 //        }
 //        try {
 //            eTime = AgentUtils.ISO8601_DATE_FORMAT.parse(eTimeString);
 //        } catch (ParseException e) {
 //            throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.END_TIME + "Unparsable value = " + eTimeString, e);
 //        }
         DateFormat usgsUrlSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
         usgsUrlSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         String sTimeString = usgsUrlSdf.format(sTime);
         String eTimeString = usgsUrlSdf.format(eTime);
 
         /** Build the propertiesString*/
         StringBuilder propertiesString = new StringBuilder();
         for (String property : properties) {
             if (null != property) {
                 propertiesString.append(property.trim()).append(",");
             }
         }
         if (propertiesString.length() > 0) {
             propertiesString.setLength(propertiesString.length() - 1);
         }
 
 
 
         /** Build the list of sites (siteCSV)*/
         StringBuilder siteCSV = new StringBuilder();
         for (String siteCode : siteCodes) {
             if (null != siteCode) {
                 siteCSV.append(siteCode.trim()).append(",");
             }
         }
         if (siteCSV.length() > 0) {
             siteCSV.setLength(siteCSV.length() - 1);
         }
 
 
 
         /** Build the query URL */
         result.append(baseUrl).append("&format=waterml,1.1");
         result.append("&sites=").append(siteCSV);
         result.append("&parameterCd=").append(propertiesString);
         result.append("&startDT=").append(sTimeString);
         result.append("&endDT=").append(eTimeString);
 
         return result.toString();
     }
 
     private String buildInterimWaterServicesDVRequest() {
         StringBuilder result = new StringBuilder();
 
         String baseUrl = context.getBaseUrl();
 //        String sTimeString = context.getStartTime();
 //        String eTimeString = context.getEndTime();
         String properties[] = context.getPropertyList().toArray(new String[0]);
         String siteCodes[] = context.getStationIdList().toArray(new String[0]);
 
 
         /** TODO: null-check here */
         /** Configure the date-time parameter */
         Date sTime = null;
         Date eTime = null;
         if (context.hasStartDatetimeMillis()) {
             sTime = new Date(context.getStartDatetimeMillis());
         }
 //        try {
 //            sTime = AgentUtils.ISO8601_DATE_FORMAT.parse(sTimeString);
 //        } catch (ParseException e) {
 //            throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.START_TIME + "Unparsable value = " + sTimeString, e);
 //        }
         if (context.hasEndDatetimeMillis()) {
             eTime = new Date(context.getEndDatetimeMillis());
         }
 //        try {
 //            eTime = AgentUtils.ISO8601_DATE_FORMAT.parse(eTimeString);
 //        } catch (ParseException e) {
 //            throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.END_TIME + "Unparsable value = " + eTimeString, e);
 //        }
         DateFormat usgsUrlSdf = new SimpleDateFormat("yyyy-MM-dd");
         usgsUrlSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         String sTimeString = usgsUrlSdf.format(sTime);
         String eTimeString = usgsUrlSdf.format(eTime);
 
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
 
         return result.toString();
     }
 
     private String buildWaterServicesDVRequest() {
         StringBuilder result = new StringBuilder();
 
         String baseUrl = context.getBaseUrl();
 //        String sTimeString = context.getStartTime();
 //        String eTimeString = context.getEndTime();
         String properties[] = context.getPropertyList().toArray(new String[0]);
         String siteCodes[] = context.getStationIdList().toArray(new String[0]);
 
 
         /** TODO: null-check here */
         /** Configure the date-time parameter */
         Date sTime = null;
         Date eTime = null;
         if (context.hasStartDatetimeMillis()) {
             sTime = new Date(context.getStartDatetimeMillis());
         }
         if (context.hasEndDatetimeMillis()) {
             eTime = new Date(context.getEndDatetimeMillis());
         }
 //        try {
 //            sTime = AgentUtils.ISO8601_DATE_FORMAT.parse(sTimeString);
 //        } catch (ParseException e) {
 //            throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.START_TIME + "Unparsable value = " + sTimeString, e);
 //        }
 //        try {
 //            eTime = AgentUtils.ISO8601_DATE_FORMAT.parse(eTimeString);
 //        } catch (ParseException e) {
 //            throw new IllegalArgumentException("Could not convert DATE string for context key " + DataSourceRequestKeys.END_TIME + "Unparsable value = " + eTimeString, e);
 //        }
         DateFormat usgsUrlSdf = new SimpleDateFormat("yyyy-MM-dd");
         usgsUrlSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         String sTimeString = usgsUrlSdf.format(sTime);
         String eTimeString = usgsUrlSdf.format(eTime);
 
         /** Build the propertiesString*/
         StringBuilder propertiesString = new StringBuilder();
         for (String property : properties) {
             if (null != property) {
                 propertiesString.append(property.trim()).append(",");
             }
         }
         if (propertiesString.length() > 0) {
             propertiesString.setLength(propertiesString.length() - 1);
         }
 
 
 
         /** Build the list of sites (siteCSV)*/
         StringBuilder siteCSV = new StringBuilder();
         for (String siteCode : siteCodes) {
             if (null != siteCode) {
                 siteCSV.append(siteCode.trim()).append(",");
             }
         }
         if (siteCSV.length() > 0) {
             siteCSV.setLength(siteCSV.length() - 1);
         }
 
 
 
         /** Build the query URL */
         result.append(baseUrl).append("&format=waterml,1.1");
         result.append("&sites=").append(siteCSV);
         result.append("&parameterCd=").append(propertiesString);
         result.append("&statCd=00003");
         result.append("&startDT=").append(sTimeString);
         result.append("&endDT=").append(eTimeString);
 
         return result.toString();
     }
 
     /**
      * Parses the given <code>asciiData</code> for any signs of error
      * 
      * @param asciiData
      *            <code>String</code> data as retrieved from {@link #acquireData(String)}
      * 
      * @throws AsciiValidationException
      *             When the given <code>asciiData</code> is invalid or cannot be validated
      */
     @Override
     protected void validateData(String asciiData) {
         super.validateData(asciiData);
         StringReader sr = new StringReader(asciiData);
         BufferedReader br = new BufferedReader(sr);
         String firstLine = null;
         try {
             firstLine = br.readLine();
         } catch (IOException e) {
             throw new AsciiValidationException("Could not read the ascii input data during validation.", e);
         }
 
         /* Check the response for errors by looking for the word "Error"
          * ...sometimes the response will be an error even though the
          * connection's resonse code returns "200 OK" */
         if (null != firstLine && firstLine.matches("Error [0-9][0-9][0-9] \\- .*")) {
             int errStart = firstLine.indexOf("Error ");
             int msgStart = firstLine.indexOf(" - ");
             String respCode = firstLine.substring(errStart + 6, errStart + 9);
             String respMsg = firstLine.substring(msgStart + 3);
             throw new AsciiValidationException(new StringBuilder("Received HTTP Error ").append(respCode).append(" with response message: \"").append(respMsg).append("\"").toString());
         }
     }
 
     /**
      * Parses the given USGS <code>String</code> data (XML) as a list of <code>IObservationGroup</code> objects
      * 
      * @param asciiData
      *            XML (<code>String</code>) data passed to this method from {@link #acquireData(String)}
      * 
      * @return a list of <code>IObservationGroup</code> objects representing the observations parsed from the given <code>asciiData</code>
      */
     @Override
     protected List<IObservationGroup> parseObs(String asciiData) {
         log.debug("");
         log.info("Parsing observations from data [" + asciiData.substring(0, Math.min(asciiData.length(), 40)) + "...]");
 
         List<IObservationGroup> obsList = new ArrayList<IObservationGroup>();
         StringReader srdr = new StringReader(asciiData);
         try {
             switch (wsType) {
                 case IV:
                     obsList.add(wsIV_parseObservations(srdr));
                     break;
                 case DV:
                     obsList.add(wsDV_parseObservations(srdr));
                     break;
                 case IDV:
                     obsList.add(wsIDV_parseObservations(srdr));
                     break;
             }
         } finally {
             if (srdr != null) {
                 srdr.close();
             }
         }
 
         return obsList;
     }
 
     /**
      * Parses the String data from the given reader into a list of observation groups.<br />
      * <br />
      * <b>Note:</b><br />
      * The given reader is guaranteed to return from this method in a <i>closed</i> state.
      * 
      * @param rdr
      *            a <code>Reader</code> object linked to a stream of USGS ascii data from the Waterservices Service
      * @return a List of IObservationGroup objects if observations are parsed, otherwise this list will be empty
      */
     public IObservationGroup wsIV_parseObservations(Reader rdr) {
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
 //            globalAttributes.put("title", "USGS rivers data timeseries.  Requested from \"" + queryUrl + "\"");
             String siteName = xpathSafeSelectValue(root, "//ns1:sourceInfo/ns1:siteName", null);
             String locationParam = xpathSafeSelectValue(root, "//ns1:queryInfo/ns1:criteria/ns1:locationParam", null);
             locationParam = locationParam.substring(locationParam.indexOf(":") + 1, locationParam.indexOf("]"));
             String variableName = xpathSafeSelectValue(root, "//ns1:variable/ns1:variableName", null);
             int cut = variableName.indexOf(",");
             if (cut >= 1) {
                 variableName = variableName.substring(0, cut);
             }
             String title = siteName + " (" + locationParam + ") - Instantaneous Value";// + variableName;
             title = title.replace(",", "").replace(".", "");
             globalAttributes.put("title", title);
             globalAttributes.put("institution", "USGS NWIS");
 
             /* history */
             globalAttributes.put("history", "Converted from WaterML1.1 to OOI CDM by " + UsgsAgent.class.getName());
 
             /* references */
 
             globalAttributes.put("references", "http://waterservices.usgs.gov/rest/WOF-IV-Service.html");
 
             /* source */
             globalAttributes.put("source", "Instantaneous Values Webservice (http://waterservices.usgs.gov/mwis/iv?)");
 
             /* conventions - from schema */
 //            globalAttributes.put("Conventions", "CF-1.5");
 
             /* Data URL */
             /* CAN'T have this because it changes every update and we don't have a way of merging attributes across multiple updates */
 //            globalAttributes.put("data_url", data_url);
 
 
             /** Get a list of provided time series */
             List<?> timeseriesList = XPath.selectNodes(doc, XPATH_ELEMENT_TIME_SERIES);
 
             /** Build an observation group for each unique sitecode */
             Object nextTimeseries = null;
             Iterator<?> iterTimeseries = timeseriesList.iterator();
             boolean hasWaterSurface;
             while (iterTimeseries.hasNext()) {
                 hasWaterSurface = false;
 
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
                     String qualifier = ((org.jdom.Attribute) XPath.selectSingleNode(next, XPATH_ATTRIBUTE_QUALIFIERS)).getValue();
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
                     /* Check to see if this is the waterSurface var */
                     hasWaterSurface = (!hasWaterSurface) ? name == VariableParams.StandardVariable.RIVER_WATER_SURFACE_HEIGHT.getVariableParams() : hasWaterSurface;
 
                     /* DON'T EVER CONVERT - Only convert data if we are dealing with Steamflow */
 //                    if (name == VariableParams.RIVER_STREAMFLOW) {
 //                        data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value) * CONVERT_FT3_TO_M3); /* convert from (f3 s-1) --> (m3 s-1) */
 //                    } else {
 //                        data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value));
 //                    }
 
                     data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value));
                     dpth = 0;
 
                     /* Add the observation data */
                     obs.addObservation(time, dpth, data, new VariableParams(name, DataType.FLOAT));
 
                     /* Add the data observation qualifier */
                     byte qualifier_value = Qualifier.getByteValue(qualifier.toString());
                     obs.addObservation(time, dpth, qualifier_value, new VariableParams(StandardVariable.USGS_QC_FLAG, DataType.BYTE));
                 }
                 /* If the group has waterSurface, add a the datum variable */
                 if (hasWaterSurface) {
                     obs.addScalarVariable(new VariableParams(VariableParams.StandardVariable.RIVER_WATER_SURFACE_REF_DATUM_ALTITUDE, DataType.FLOAT), 0f);
                 }
             }
             obs.addAttributes(globalAttributes);
 
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
      * Parses the String data from the given reader into a list of observation groups.<br />
      * <br />
      * <b>Note:</b><br />
      * The given reader is guaranteed to return from this method in a <i>closed</i> state.
      * 
      * @param rdr
      *            a <code>Reader</code> object linked to a stream of USGS ascii data from the Daily Values Service
      * @return a List of IObservationGroup objects if observations are parsed, otherwise this list will be empty
      */
     public IObservationGroup wsIDV_parseObservations(Reader rdr) {
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
             String siteName = xpathSafeSelectValue(root, "//ns1:sourceInfo/ns1:siteName", null);
             String locationParam = xpathSafeSelectValue(root, "//ns1:queryInfo/ns1:criteria/ns1:locationParam", null);
             locationParam = locationParam.substring(locationParam.indexOf(":") + 1, locationParam.indexOf("/"));
 //            String variableName = xpathSafeSelectValue(root, "//ns1:variable/ns1:variableName", null);
 //            String dataType = xpathSafeSelectValue(root, "//ns1:variable/ns1:dataType", null);
             String title = siteName + " (" + locationParam + ") - Daily Values";// + dataType + " " + variableName;
             title = title.replace(",", "").replace(".", "");
             globalAttributes.put("title", title);
             globalAttributes.put("institution", "USGS NWIS");
 
             /* history */
             globalAttributes.put("history", "Converted from WaterML1.0 to OOI CDM by " + UsgsAgent.class.getName());
 
             /* references */
             globalAttributes.put("references", "http://waterservices.usgs.gov/rest/USGS-DV-Service.html (interim)");
 
             /* source */
             globalAttributes.put("source", "Daily Values Webservice (http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?)");
 
             /* conventions - from schema */
 //            globalAttributes.put("Conventions", "CF-1.5");
 
             /* Data URL */
             /* CAN'T have this because it changes every update and we don't have a way of merging attributes across multiple updates */
 //            globalAttributes.put("data_url", data_url);
 
 
 
             /** Get a list of provided time series */
             List<?> timeseriesList = XPath.selectNodes(root, ".//ns1:timeSeries");
 //            List<?> timeseriesList = XPath.selectNodes(doc, XPATH_ELEMENT_TIME_SERIES);
 //            List<?> timeseriesList = XPath.selectNodes(doc, ".//timeSeries");
 //            List<?> timeseriesList = root.getChildren("timeSeries", ns);
 
 //            System.out.println("Total timeseries in doc: " + timeseriesList.size());
 
 
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
                     String qualifier = ((org.jdom.Attribute) XPath.selectSingleNode(next, XPATH_ATTRIBUTE_QUALIFIERS)).getValue();
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
                     /* DON'T EVER CONVERT - Only convert data if we are dealing with Steamflow) */
 //                    if (name == VariableParams.RIVER_STREAMFLOW) {
 //                        data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value) * CONVERT_FT3_TO_M3); /* convert from (f3 s-1) --> (m3 s-1) */
 //                    } else {
 //                        data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value));
 //                    }
                     data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value));
                     dpth = 0;
 
 
                     /* Add the observation data */
                     obs.addObservation(time, dpth, data, new VariableParams(name, DataType.FLOAT));
 
                     /* Add the data observation qualifier */
                     byte qualifier_value = Qualifier.getByteValue(qualifier.toString());
                     obs.addObservation(time, dpth, qualifier_value, new VariableParams(StandardVariable.USGS_QC_FLAG, DataType.BYTE));
 
                 }
 
 
 //                /** Grab attributes */
 //                Map<String, String> tsAttributes = new TreeMap<String, String>();
 //
 //
 //                /* Extract timeseries-specific attributes */
 //                String sitename = xpathSafeSelectValue(nextTimeseries, "//ns1:siteName", "[n/a]");
 //                String network = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode/@network", "[n/a]");
 //                String agency = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode/@agencyCode", "[n/a]");
 //                String sCode = xpathSafeSelectValue(nextTimeseries, "//ns1:siteCode", "[n/a]");
 //                tsAttributes.put("institution", new StringBuilder().append(sitename).append(" (network:").append(network).append("; agencyCode:").append(agency).append("; siteCode:").append(sCode).append(";)").toString());
 //
 //                String method = xpathSafeSelectValue(nextTimeseries, ".//ns1:values//ns1:methodDescription", "[n/a]");
 //                tsAttributes.put("source", method);
 //
 //
 //
 //                /** Add global and timeseries attributes */
 //                obs.addAttributes(tsAttributes);
 
 
 
             }
 
             obs.addAttributes(globalAttributes);
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
      * Parses the String data from the given reader into a list of observation groups.<br />
      * <br />
      * <b>Note:</b><br />
      * The given reader is guaranteed to return from this method in a <i>closed</i> state.
      * 
      * @param rdr
      *            a <code>Reader</code> object linked to a stream of USGS ascii data from the Waterservices Service
      * @return a List of IObservationGroup objects if observations are parsed, otherwise this list will be empty
      */
     public IObservationGroup wsDV_parseObservations(Reader rdr) {
         /* TODO: Fix exception handling in this method, it is too generic; try/catch blocks should be as confined as possible */
 
         /** XPATH queries */
         final String XPATH_ELEMENT_TIME_SERIES = ".//ns1:timeSeries";
         final String XPATH_ELEMENT_SITE_CODE = "./ns1:sourceInfo/ns1:siteCode";
         final String XPATH_ATTRIBUTE_AGENCY_CODE = "./ns1:sourceInfo/ns1:siteCode/@agencyCode";
         final String XPATH_ELEMENT_LATITUDE = "./ns1:sourceInfo/ns1:geoLocation/ns1:geogLocation/ns1:latitude"; /* NOTE: geogLocation is (1..*) */
 
         final String XPATH_ELEMENT_LONGITUDE = "./ns1:sourceInfo/ns1:geoLocation/ns1:geogLocation/ns1:longitude"; /* NOTE: geogLocation is (1..*) */
 
         final String XPATH_ELEMENT_VALUES = "./ns1:values";
         final String XPATH_ELEMENT_VALUE = "./ns1:value";
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
 //            globalAttributes.put("title", "USGS rivers data timeseries.  Requested from \"" + queryUrl + "\"");
             String siteName = xpathSafeSelectValue(root, "//ns1:sourceInfo/ns1:siteName", null);
             String locationParam = xpathSafeSelectValue(root, "//ns1:queryInfo/ns1:criteria/ns1:locationParam", null);
             locationParam = locationParam.substring(locationParam.indexOf(":") + 1, locationParam.indexOf("]"));
             String variableName = xpathSafeSelectValue(root, "//ns1:variable/ns1:variableName", null);
             int cut = variableName.indexOf(",");
             if (cut >= 1) {
                 variableName = variableName.substring(0, cut);
             }
             String title = siteName + " (" + locationParam + ") - Daily Value";// + variableName;
             title = title.replace(",", "").replace(".", "");
             globalAttributes.put("title", title);
             globalAttributes.put("institution", "USGS NWIS");
 
             /* history */
             globalAttributes.put("history", "Converted from WaterML1.1 to OOI CDM by " + UsgsAgent.class.getName());
 
             /* references */
 
             globalAttributes.put("references", "http://waterservices.usgs.gov/rest/DV-Service.html");
 
             /* source */
            globalAttributes.put("source", "Instantaneous Values Webservice (http://waterservices.usgs.gov/mwis/dv?)");
 
             /* conventions - from schema */
 //            globalAttributes.put("Conventions", "CF-1.5");
 
             /* Data URL */
             /* CAN'T have this because it changes every update and we don't have a way of merging attributes across multiple updates */
 //            globalAttributes.put("data_url", data_url);
 
 
             /** Get a list of provided time series */
             List<?> timeseriesList = XPath.selectNodes(doc, XPATH_ELEMENT_TIME_SERIES);
 
             /** Build an observation group for each unique sitecode */
             Object nextTimeseries = null;
             Iterator<?> iterTimeseries = timeseriesList.iterator();
             boolean hasWaterSurface;
             while (iterTimeseries.hasNext()) {
                 hasWaterSurface = false;
 
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
                 VariableParams name = getDataNameForVariableCode(variableCode);
                 /* Check to see if this is the waterSurface var */
                 hasWaterSurface = (!hasWaterSurface) ? name == VariableParams.StandardVariable.RIVER_WATER_SURFACE_HEIGHT.getVariableParams() : hasWaterSurface;
 
                 /* May be multiple sets of values for a "variable" (i.e. middle, bottom, surface) */
                 List<?> valuesList = XPath.selectNodes(nextTimeseries, XPATH_ELEMENT_VALUES);
                 Iterator<?> valuesIter = valuesList.iterator();
                 Object valuesSet = null;
                 Pattern pattern = Pattern.compile("\\((.*)\\)");
                 Matcher match = null;
                 while (valuesIter.hasNext()) {
                     String varNameSuffix = "";
                     String longNameSuffix = "";
                     valuesSet = valuesIter.next();
                     if (valuesSet == null) {
                         continue;
                     }
                     Object method = XPath.selectSingleNode(valuesSet, "./ns1:method/ns1:methodDescription");
                     if (method != null) {
                         String methString = ((Element) method).getTextTrim();
                         if (methString != null & !methString.isEmpty()) {
                             longNameSuffix = methString;
                             match = pattern.matcher(methString);
                             if(match.find()) {
                                 varNameSuffix = match.group(1);
                             } else {
                                 varNameSuffix = methString;
                             }
                             varNameSuffix = varNameSuffix.toLowerCase().replace(" ", "_");
 //                            varNameSuffix = methString.substring(methString.indexOf("(") + 1, methString.indexOf(")"));
                         }
                     }
 //                }
 
                     /** Add each timeseries value (observation) to the observation group */
                     /* Get a list of each observation */
 //                List<?> observationList = XPath.selectNodes(nextTimeseries, XPATH_ELEMENT_VALUE);
                     List<?> observationList = XPath.selectNodes(valuesSet, XPATH_ELEMENT_VALUE);
 
 
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
                         String qualifier = ((org.jdom.Attribute) XPath.selectSingleNode(next, XPATH_ATTRIBUTE_QUALIFIERS)).getValue();
                         datetime = ((org.jdom.Attribute) XPath.selectSingleNode(next, XPATH_ATTRIBUTE_DATETIME)).getValue();
                         String value = ((Element) next).getTextTrim();
 //                    datetime = datetime.replaceAll("\\:", "").concat("Z");
 
 
                         /* Convert observation data */
                         int time = 0;
                         float data = 0;
                         float dpth = 0;
 
                         time = (int) (inSdf.parse(datetime).getTime() * 0.001);
                         // data = Float.parseFloat(value);
 
                         /* DON'T EVER CONVERT - Only convert data if we are dealing with Steamflow */
 //                    if (name == VariableParams.RIVER_STREAMFLOW) {
 //                        data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value) * CONVERT_FT3_TO_M3); /* convert from (f3 s-1) --> (m3 s-1) */
 //                    } else {
 //                        data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value));
 //                    }
 
                         data = (noDataString.equals(value)) ? (Float.NaN) : (float) (Double.parseDouble(value));
                         dpth = 0;
 
                         /* Add the observation data */
 //                    obs.addObservation(time, dpth, data, new VariableParams(name, DataType.FLOAT));
                         String vName = (!varNameSuffix.isEmpty()) ? name.getShortName().concat("_").concat(varNameSuffix) : name.getShortName();
                         String lName = (!longNameSuffix.isEmpty()) ? name.getDescription().concat(" ").concat(longNameSuffix) : name.getDescription();
                         obs.addObservation(time, dpth, data, new VariableParams(name.getStandardName(), vName, lName, name.getUnits(), DataType.FLOAT));
 
                         /* Add the data observation qualifier */
                         byte qualifier_value = Qualifier.getByteValue(qualifier.toString());
                         obs.addObservation(time, dpth, qualifier_value, new VariableParams(StandardVariable.USGS_QC_FLAG, DataType.BYTE));
                     }
                 }
                 /* If the group has waterSurface, add a the datum variable */
                 if (hasWaterSurface) {
                     obs.addScalarVariable(new VariableParams(VariableParams.StandardVariable.RIVER_WATER_SURFACE_REF_DATUM_ALTITUDE, DataType.FLOAT), 0f);
                 }
             }
             obs.addAttributes(globalAttributes);
 
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
             result = VariableParams.StandardVariable.WATER_TEMPERATURE.getVariableParams();
         } else if ("00060".equals(variableCode)) {
             result = VariableParams.StandardVariable.RIVER_STREAMFLOW.getVariableParams();
         } else if ("00065".equals(variableCode)) {
             result = VariableParams.StandardVariable.RIVER_WATER_SURFACE_HEIGHT.getVariableParams();
         } else if ("00045".equals(variableCode)) {
             result = VariableParams.StandardVariable.RIVER_PRECIPITATION.getVariableParams();
         } else if ("00095".equals(variableCode)) {
             result = VariableParams.StandardVariable.USGS_SEA_WATER_CONDUCTIVITY.getVariableParams();
         } else {
             throw new IllegalArgumentException("Given variable code is not known: " + variableCode);
         }
 
         return result;
     }
 
     /**
      * Converts the given list of <code>IObservationGroup</code>s to a {@link NetcdfDataset}, breaks that dataset into manageable sections
      * and sends those data "chunks" to the ingestion service.
      * 
      * @param obsList
      *            a group of observations as a list of <code>IObservationGroup</code> objects
      *            
      * @return TODO:
      * 
      * @see #obs2Ncds(IObservationGroup...)
      * @see #sendNetcdfDataset(NetcdfDataset, String)
      * @see #sendNetcdfDataset(NetcdfDataset, String, boolean)
      */
     @Override
     public String[] processDataset(IObservationGroup... obsList) {
         List<String> ret = new ArrayList<String>();
         NetcdfDataset ncds = null;
         try {
             ncds = obs2Ncds(obsList);
             if (ncds != null) {
                 /* Send the dataset via the send dataset method of AbstractDatasetAgent */
                 ret.add(this.sendNetcdfDataset(ncds, "ingest"));
             } else {
                 /* Send the an error via the send dataset method of AbstractDatasetAgent */
                 String err = "Abort from this update:: The returned NetcdfDataset is null";
                 this.sendDataErrorMsg(StatusCode.AGENT_ERROR, err);
                 ret.add(err);
             }
         } catch (IonException ex) {
             ret.add(AgentUtils.getStackTraceString(ex));
         }
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
 
         boolean dailyValues = false;
         boolean makeRutgersSamples = false;
         boolean makeUHSamples = false;
         boolean makeMetadataTable = false;
         boolean manual = true;
         if (makeRutgersSamples) {
             generateRutgersSamples(dailyValues);
         }
         if (makeUHSamples) {
             generateUHSamples(dailyValues);
         }
         if (makeMetadataTable) {
             generateRutgersMetadata(dailyValues);
         }
         if (manual) {
             net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
             cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
             cBldr.setIsInitial(true);
             cBldr.setBaseUrl("http://waterservices.usgs.gov/nwis/iv?");
             int switcher = 4;
             try {
                 switch (switcher) {
                     case 1://test temp
 //                    cBldr.setStartTime("2011-2-20T00:00:00Z");
 //                    cBldr.setEndTime("2011-4-19T00:00:00Z");
                         //test temp
                         //                    cBldr.setStartTime("2011-2-20T00:00:00Z");
                         //                    cBldr.setEndTime("2011-4-19T00:00:00Z");
                         cBldr.setStartDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-2-20T00:00:00Z").getTime());
                         cBldr.setEndDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-4-19T00:00:00Z").getTime());
                         cBldr.addProperty("00010");
 //                    cBldr.addStationId("01463500");
                         cBldr.addStationId("01646500");
                         break;
                     case 2://test discharge
                         cBldr.setStartDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-2-20T00:00:00Z").getTime());
                         cBldr.setEndDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-4-19T00:00:00Z").getTime());
                         cBldr.addProperty("00060");
 //                    cBldr.addStationId("01463500");
                         cBldr.addStationId("01646500");
                         break;
                     case 3://test temp & discharge
                         cBldr.setStartDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-2-10T00:00:00Z").getTime());
                         cBldr.setEndDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-2-11T00:00:00Z").getTime());
                         cBldr.addProperty("00010");
                         cBldr.addProperty("00060");
 //                    cBldr.addStationId("01463500");
                         cBldr.addStationId("01646500");
                         break;
                     case 4:
 //                        cBldr.setBaseUrl("http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?");
                         cBldr.setBaseUrl("http://waterservices.usgs.gov/nwis/dv?");
                         cBldr.setStartDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-01-01T00:00:00Z").getTime());
 //                    cBldr.setStartTime("2011-02-01T00:00:00Z");
                         cBldr.setEndDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-08-07T00:00:00Z").getTime());
                         cBldr.addProperty("00010");
                         cBldr.addProperty("00060");
                         cBldr.addProperty("00065");//gauge height
                         cBldr.addProperty("00045");//precip
                         cBldr.addProperty("00095");//?? 
                         cBldr.addStationId("01463500");
 //                        cBldr.addStationId("01646500");
                         break;
                     case 5://test all supported parameters
                         cBldr.setStartDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-05-12T00:00:00Z").getTime());
                         cBldr.setEndDatetimeMillis(AgentUtils.ISO8601_DATE_FORMAT.parse("2011-05-13T00:00:00Z").getTime());
                         cBldr.addProperty("00010");
                         cBldr.addProperty("00060");
                         cBldr.addProperty("00065");//gauge height
                         cBldr.addProperty("00045");//precip
                         cBldr.addProperty("00095");//?? 
 //                        cBldr.addStationId("01491000");
                         cBldr.addStationId("212359157502601");
                         break;
                 }
             } catch (ParseException ex) {
                 throw new IOException("Error parsing time strings", ex);
             }
 //            cBldr.setStartTime("2011-01-29T00:00:00Z");
 //            cBldr.setEndTime("2011-01-31T00:00:00Z");
 //            cBldr.addProperty("00010");
 //            cBldr.addProperty("00060");
 //            cBldr.addAllStationId(java.util.Arrays.asList(new String[] {"01184000", "01327750", "01357500", "01389500", "01403060", "01463500", "01578310", "01646500", "01592500", "01668000", "01491000", "02035000", "02041650", "01673000", "01674500", "01362500", "01463500", "01646500" }));
 
             net.ooici.core.container.Container.Structure struct = AgentUtils.getUpdateInitStructure(GPBWrapper.Factory(cBldr.build()));
 //            runAgent(struct, AgentRunType.TEST_WRITE_OOICDM);
             runAgent(struct, AgentRunType.TEST_WRITE_NC);
         }
     }
 
     private static void generateRutgersMetadata(boolean dailyValues) throws IOException {
         /** For each of the "R1" netcdf datasets (either local or remote)
          *
          * 1. get the last timestep of the data
          * 2. get the list of global-attributes
          * 3. build a delimited string with the following structure:
          *      attribute_1, attribute_2, attribute_3, ..., attribute_n
          *      value_1, value_2, value_3, ..., value_n
          *
          */
 //        String[] datasetList = new String[]{"http://nomads.ncep.noaa.gov:9090/dods/nam/nam20110303/nam1hr_00z",
 //                                            "http://thredds1.pfeg.noaa.gov/thredds/dodsC/satellite/GR/ssta/1day",
 //                                            "http://tashtego.marine.rutgers.edu:8080/thredds/dodsC/cool/avhrr/bigbight/2010"};
         Map<String, Map<String, String>> datasets = new TreeMap<String, Map<String, String>>(); /* Maps dataset name to an attributes map */
         List<String> metaLookup = new ArrayList<String>();
 
         /* Front-load the metadata list with the OOI required metadata */
         metaLookup.add("title");
         metaLookup.add("institution");
         metaLookup.add("source");
         metaLookup.add("history");
         metaLookup.add("references");
         metaLookup.add("Conventions");
         metaLookup.add("summary");
         metaLookup.add("comment");
         metaLookup.add("data_url");
         metaLookup.add("ion_time_coverage_start");
         metaLookup.add("ion_time_coverage_end");
         metaLookup.add("ion_geospatial_lat_min");
         metaLookup.add("ion_geospatial_lat_max");
         metaLookup.add("ion_geospatial_lon_min");
         metaLookup.add("ion_geospatial_lon_max");
         metaLookup.add("ion_geospatial_vertical_min");
         metaLookup.add("ion_geospatial_vertical_max");
         metaLookup.add("ion_geospatial_vertical_positive");
 
         /* For now, don't add anything - this process will help us figure out what needs to be added *//* Generates samples for near-realtime high-resolution data */
         String baseURL = "http://waterservices.usgs.gov/nwis/iv?";
         long sTime, eTime;
         try {
             sTime = AgentUtils.ISO8601_DATE_FORMAT.parse("2011-03-01T00:00:00Z").getTime();
             eTime = AgentUtils.ISO8601_DATE_FORMAT.parse("2011-03-10T00:00:00Z").getTime();
 
             /* Generates samples for "historical" low-resolution data */
             baseURL = "http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?";
             sTime = AgentUtils.ISO8601_DATE_FORMAT.parse("2003-01-01T00:00:00Z").getTime();
             eTime = AgentUtils.ISO8601_DATE_FORMAT.parse("2011-03-17T00:00:00Z").getTime();
         } catch (ParseException ex) {
             throw new IOException("Error parsing time string(s)", ex);
         }
         String prefix = (baseURL.endsWith("NWISQuery/GetDV1?")) ? "USGS-DV " : "USGS-WS ";
 
         String[] disIds = new String[]{"01184000", "01327750", "01357500", "01389500", "01403060", "01463500", "01578310", "01646500", "01592500", "01668000", "01491000", "02035000", "02041650", "01673000", "01674500"};
         String[] disNames = new String[]{"Connecticut", "Hudson", "Mohawk", "Passaic", "Raritan", "Delaware", "Susquehanna", "Potomac", "Patuxent", "Rappahannock", "Choptank", "James", "Appomattox", "Pamunkey", "Mattaponi"};
         String[] tempIds = new String[]{"01362500", "01463500", "01646500"};
         String[] tempNames = new String[]{"Hudson", "Delware", "Potomac"};
 
         String dsName;
         String[] resp;
         for (int i = 0; i < disIds.length; i++) {
             dsName = prefix + disNames[i] + "[" + disIds[i] + "]";
             try {
                 net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
                 cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
                 cBldr.setBaseUrl(baseURL);
                 cBldr.setStartDatetimeMillis(sTime);
                 cBldr.setEndDatetimeMillis(eTime);
                 cBldr.addProperty("00060");
                 cBldr.addStationId(disIds[i]);
                 net.ooici.core.container.Container.Structure struct = AgentUtils.getUpdateInitStructure(GPBWrapper.Factory(cBldr.build()));
                 resp = runAgent(struct, AgentRunType.TEST_NO_WRITE);
             } catch (Exception e) {
                 e.printStackTrace();
                 datasets.put(dsName + " (FAILED)", null);
                 continue;
             }
             Map<String, String> dsMeta = NcDumpParse.parseToMap(resp[0]);
             datasets.put(dsName, dsMeta);
 
 
             /* TODO: Eventually we can make this loop external and perform a sort beforehand.
              *       this sort would frontload attributes which are found more frequently
              *       across multiple datasets
              */
             for (String key : dsMeta.keySet()) {
                 if (!metaLookup.contains(key)) {
                     metaLookup.add(key);
                 }
             }
         }
 
         for (int i = 0; i < tempIds.length; i++) {
             dsName = prefix + disNames[i] + "[" + disIds[i] + "]";
             try {
                 net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
                 cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
                 cBldr.setBaseUrl(baseURL);
                 cBldr.setStartDatetimeMillis(sTime);
                 cBldr.setEndDatetimeMillis(eTime);
                 cBldr.addProperty("00010");
                 cBldr.addStationId(disIds[i]);
                 net.ooici.core.container.Container.Structure struct = AgentUtils.getUpdateInitStructure(GPBWrapper.Factory(cBldr.build()));
                 resp = runAgent(struct, AgentRunType.TEST_NO_WRITE);
             } catch (Exception e) {
                 e.printStackTrace();
                 datasets.put(dsName + " (FAILED)", null);
                 continue;
             }
             Map<String, String> dsMeta = NcDumpParse.parseToMap(resp[0]);
             datasets.put(dsName, dsMeta);
 
 
             /* TODO: Eventually we can make this loop external and perform a sort beforehand.
              *       this sort would frontload attributes which are found more frequently
              *       across multiple datasets
              */
             for (String key : dsMeta.keySet()) {
                 if (!metaLookup.contains(key)) {
                     metaLookup.add(key);
                 }
             }
         }
 
         /** Write the CSV output */
         String NEW_LINE = System.getProperty("line.separator");
         StringBuilder sb = new StringBuilder();
 
         /* TODO: Step 1: add header data here */
         sb.append("Dataset Name");
         for (String metaName : metaLookup) {
             sb.append("|");
             sb.append(metaName);
 //            sb.append('"');
 //            sb.append(metaName.replaceAll(Pattern.quote("\""), "\"\""));
 //            sb.append('"');
         }
 
         /* Step 2: Add each row of data */
         for (String ds : datasets.keySet()) {
             Map<String, String> dsMeta = datasets.get(ds);
             sb.append(NEW_LINE);
             sb.append(ds);
 //            sb.append('"');
 //            sb.append(ds.replaceAll(Pattern.quote("\""), "\"\""));
 //            sb.append('"');
             String metaValue = null;
             for (String metaName : metaLookup) {
                 sb.append("|");
                 if (null != dsMeta && null != (metaValue = dsMeta.get(metaName))) {
                     sb.append(metaValue);
                     /* To ensure correct formatting, change all existing double quotes
                      * to two double quotes, and surround the whole cell value with
                      * double quotes...
                      */
 //                    sb.append('"');
 //                    sb.append(metaValue.replaceAll(Pattern.quote("\""), "\"\""));
 //                    sb.append('"');
                 }
             }
 
         }
 
         System.out.println(NEW_LINE + NEW_LINE + "********************************************************");
         System.out.println(sb.toString());
         System.out.println(NEW_LINE + "********************************************************");
     }
 
     private static void generateRutgersSamples(boolean dailyValues) throws IOException {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'");
         sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         Date now = new Date();
 
         try {
             now = AgentUtils.ISO8601_DATE_FORMAT.parse("2011-04-29T00:00:00Z");
         } catch (ParseException ex) {
             throw new IOException("Error parsing time string", ex);
         }
 
         /* Generates samples for near-realtime high-resolution data */
         String baseURL = "http://waterservices.usgs.gov/nwis/iv?";
         long sTime = now.getTime() - 2592000000l;//start 30 days before
         long eTime = now.getTime();
 
         if (dailyValues) {
             /* Generates samples for "historical" low-resolution data */
             baseURL = "http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?";
             try {
                 sTime = AgentUtils.ISO8601_DATE_FORMAT.parse("2010-01-01T00:00:00Z").getTime();
             } catch (ParseException ex) {
                 throw new IOException("Error parsing time string", ex);
             }
         }
         String[] disIds = new String[]{"01184000", "01327750", "01357500", "01389500", "01403060", "01463500", "01578310", "01646500", "01592500", "01668000", "01491000", "02035000", "02041650", "01673000", "01674500"};
 //        String[] disNames = new String[]{"Connecticut", "Hudson", "Mohawk", "Passaic", "Raritan", "Delaware", "Susquehanna", "Potomac", "Patuxent", "Rappahannock", "Choptank", "James", "Appomattox", "Pamunkey", "Mattaponi"};
         String[] tempIds = new String[]{"01362500", "01463500", "01646500"};
 //        String[] tempNames = new String[]{"Hudson", "Delware", "Potomac"};
 
         String[] allIds = new String[]{"01184000", "01327750", "01357500", "01362500", "01389500", "01403060", "01463500", "01578310", "01646500", "01592500", "01668000", "01491000", "02035000", "02041650", "01673000", "01674500"};
 
         for (int i = 0; i < allIds.length; i++) {
             net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
             cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
             cBldr.setBaseUrl(baseURL);
             cBldr.setStartDatetimeMillis(sTime);
             cBldr.setEndDatetimeMillis(eTime);
             cBldr.addProperty("00010").addProperty("00060").addProperty("00065").addProperty("00045").addProperty("00095");
             cBldr.addStationId(allIds[i]);
 
             net.ooici.core.container.Container.Structure struct = AgentUtils.getUpdateInitStructure(GPBWrapper.Factory(cBldr.build()));
 
             String[] res = runAgent(struct, AgentRunType.TEST_WRITE_NC);
 //            String[] res = runAgent(struct, AgentRunType.TEST_WRITE_OOICDM);
         }
 
 //        for (int i = 0; i < disIds.length; i++) {
 //            net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
 //            cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
 //            cBldr.setBaseUrl(baseURL);
 //            cBldr.setStartTime(sTime);
 //            cBldr.setEndTime(eTime);
 //            cBldr.addProperty("00060");
 //            cBldr.addStationId(disIds[i]);
 //            String[] res = runAgent(cBldr.build(), AgentRunType.TEST_WRITE_NC);
 ////            NetcdfDataset dsout = null;
 ////            try {
 ////                dsout = NetcdfDataset.openDataset("ooici:" + res[0]);
 ////                ucar.nc2.FileWriter.writeToFile(dsout, output_prefix + disNames[i] + "_discharge.nc");
 ////            } catch (IOException ex) {
 ////                log.error("Error writing netcdf file", ex);
 ////            } finally {
 ////                if (dsout != null) {
 ////                    dsout.close();
 ////                }
 ////            }
 //        }
 //
 //        for (int i = 0; i < tempIds.length; i++) {
 //            net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
 //            cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
 //            cBldr.setBaseUrl(baseURL);
 //            cBldr.setStartTime(sTime);
 //            cBldr.setEndTime(eTime);
 //            cBldr.addProperty("00010");
 //            cBldr.addStationId(tempIds[i]);
 //            String[] res = runAgent(cBldr.build(), AgentRunType.TEST_WRITE_NC);
 ////            NetcdfDataset dsout = null;
 ////            try {
 ////                dsout = NetcdfDataset.openDataset("ooici:" + res[0]);
 ////                ucar.nc2.FileWriter.writeToFile(dsout, output_prefix + tempNames[i] + "_temp.nc");
 ////            } catch (IOException ex) {
 ////                log.error("Error writing netcdf file", ex);
 ////            } finally {
 ////                if (dsout != null) {
 ////                    dsout.close();
 ////                }
 ////            }
 //        }
 
         System.out.println("******FINISHED******");
     }
 
     private static void generateUHSamples(boolean dailyValues) throws IOException {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'");
         sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         Date now = new Date();
 
         /* Generates samples for near-realtime high-resolution data */
         String baseURL = "http://waterservices.usgs.gov/nwis/iv?";
         long sTime = now.getTime() - 86400000;//start 1 day before
         long eTime = now.getTime();
 
         if (dailyValues) {
             /* Generates samples for "historical" low-resolution data */
             baseURL = "http://interim.waterservices.usgs.gov/NWISQuery/GetDV1?";
             try {
                 sTime = AgentUtils.ISO8601_DATE_FORMAT.parse("2010-01-01T00:00:00Z").getTime();
             } catch (ParseException ex) {
                 throw new IOException("Error parsing time string", ex);
             }
         }
 
         String[] allIds = new String[]{"16211600", "16212800", "16213000", "16226200", "16226400", "16229000", "16238000", "16240500", "16242500", "16244000", "16247100", "211747157485601", "212359157502601", "212428157511201"};
 
         for (int i = 0; i < allIds.length; i++) {
             net.ooici.services.sa.DataSource.EoiDataContextMessage.Builder cBldr = net.ooici.services.sa.DataSource.EoiDataContextMessage.newBuilder();
             cBldr.setSourceType(net.ooici.services.sa.DataSource.SourceType.USGS);
             cBldr.setBaseUrl(baseURL);
             cBldr.setStartDatetimeMillis(sTime);
             cBldr.setEndDatetimeMillis(eTime);
             cBldr.addProperty("00010").addProperty("00060").addProperty("00065").addProperty("00045").addProperty("00095");
             cBldr.addStationId(allIds[i]);
 
             net.ooici.core.container.Container.Structure struct = AgentUtils.getUpdateInitStructure(GPBWrapper.Factory(cBldr.build()));
 //            String[] res = runAgent(struct, AgentRunType.TEST_WRITE_NC);
             String[] res = runAgent(struct, AgentRunType.TEST_WRITE_OOICDM);
         }
 
         System.out.println("******FINISHED******");
     }
 
     private static String[] runAgent(net.ooici.core.container.Container.Structure struct, AgentRunType agentRunType) throws IOException {
         net.ooici.eoi.datasetagent.IDatasetAgent agent = net.ooici.eoi.datasetagent.AgentFactory.getDatasetAgent(net.ooici.services.sa.DataSource.SourceType.USGS);
         agent.setAgentRunType(agentRunType);
 
         java.util.HashMap<String, String> connInfo = null;
         try {
             connInfo = IonUtils.parseProperties();
         } catch (IOException ex) {
             log.error("Error parsing \"ooici-conn.properties\" cannot continue.", ex);
             System.exit(1);
         }
         String[] result = agent.doUpdate(struct, connInfo);
         log.debug("Response:");
         for (String s : result) {
             log.debug(s);
         }
         return result;
     }
 }
