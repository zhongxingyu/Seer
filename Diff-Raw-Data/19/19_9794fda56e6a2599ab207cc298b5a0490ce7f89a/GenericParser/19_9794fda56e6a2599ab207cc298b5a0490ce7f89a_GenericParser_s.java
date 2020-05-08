 package com.asascience.edc.sos.parsers;
 
 import com.asascience.edc.sos.SosServer;
 import cern.colt.Timer;
 import com.asascience.edc.Configuration;
 import java.beans.PropertyChangeEvent;
 import java.util.Date;
 import java.util.List;
 
 import org.jdom.Document;
 import com.asascience.edc.sos.SensorContainer;
 import com.asascience.edc.sos.map.SensorPoint;
 import com.asascience.edc.sos.VariableContainer;
 import com.asascience.edc.sos.requests.ResponseFormat;
 import gov.nasa.worldwind.render.PointPlacemark;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.xpath.XPath;
 import ucar.unidata.geoloc.LatLonPointImpl;
 import ucar.unidata.geoloc.LatLonRect;
 import org.apache.commons.lang.time.DateUtils;
 
 /**
  * GenericParser.java
  * 
  * @author Kyle Wilcox <kwilcox@asascience.com>
  */
 public class GenericParser implements PropertyChangeListener, SosParserInterface {
 
   protected List<SensorContainer> sensorList;
   protected List<VariableContainer> variableList;
   protected List<ResponseFormat> responseFormatList;
   protected double[] NESW;
   protected Date startTime;
   protected Date endTime;
   protected String[] dateFormats;
   protected String sosURL;
   protected String homeDir;
   protected Document getCapDoc;
   protected String type;
   protected double timeInterval;
   protected int numTimeSteps;
   protected PropertyChangeSupport pcs;
   protected int displayType;
 
   public GenericParser(Document xmlDoc) {
     type = "Generic";
     pcs = new PropertyChangeSupport(this);
     getCapDoc = xmlDoc;
   }
 
   public void process() {
     // Sensors must go first
     parseSensors();
     parseVariables();
   }
 
   public String getType() {
     return type;
   }
 
   public List<SensorContainer> getSensors() {
     return sensorList;
   }
 
   public List<VariableContainer> getVariables() {
     return variableList;
   }
 
   public List<ResponseFormat> getResponseFormats() {
     return responseFormatList;
   }
 
   public void setPanelType(int type) {
     displayType = type;
   }
 
   public void setResponseFormats(List<SensorContainer> sensors) {
     List<ResponseFormat> formats = new ArrayList<ResponseFormat>();
     for (SensorContainer sp : sensors) {
       for (String f : sp.getResponseFormats()) {
         ResponseFormat format = new ResponseFormat(f);
         if (!formats.contains(format)) {
           formats.add(format);
           // Does this format supply some post-processing formats?
           for (ResponseFormat r : format.getChildFormats()) {
             formats.add(r);
           }
         }
       }
     }
     // This makes the list unique
     responseFormatList = new ArrayList<ResponseFormat>(new HashSet(formats));
 
     if (displayType == Configuration.DisplayType.ESRI) {
       ArrayList<ResponseFormat> removals = new ArrayList<ResponseFormat>();
       for (ResponseFormat r : responseFormatList) {
         if (!r.getName().contains("ARC")) {
           removals.add(r);
         }
       }
       responseFormatList.removeAll(removals);
     }
 
     pcs.firePropertyChange("message", null, "Found " + responseFormatList.size() + " unique response formats");
   }
   
   public void parseWorldwindResponseFormats(List<PointPlacemark> selectedPoints) {
     List<SensorContainer> scs = new ArrayList<SensorContainer>();
     for (PointPlacemark sp : selectedPoints) {
       scs.add((SensorContainer)sp.getValue("sensor"));
     }
     setResponseFormats(scs);
   }
   
   public void parseResponseFormats(List<SensorPoint> selectedPoints) {
     List<SensorContainer> scs = new ArrayList<SensorContainer>();
     for (SensorPoint sp : selectedPoints) {
       scs.add(sp.getSensor());
     }
     setResponseFormats(scs);
   }
 
   public void parseVariables() {
     // Make a unique list of VariableContainers
     List<VariableContainer> thisList = new ArrayList();
     pcs.firePropertyChange("message", null, "Parsing out unique variables");
     Timer stopwatch = new Timer();
     stopwatch.start();
     
     boolean found = false;
     for (SensorContainer sensor : sensorList) {
       for (VariableContainer variable : sensor.getVarList()) {
         if (thisList.isEmpty()) {
           thisList.add(variable);
         } else {
           found = false;
           for (VariableContainer varInList : thisList) {
             if (variable.compareTo(varInList) == 0) {
               found = true;
               break;
             }
           }
           if (!found) {
             thisList.add(variable);
           }
         }
       }
     }
     variableList = thisList;
     pcs.firePropertyChange("message", null, "Found " + variableList.size() + " unique variables in " + stopwatch.seconds() + " seconds");
   }
   
   public void parseSensors() {
     pcs.firePropertyChange("message", null, "Parsing for Offerings...");
     Timer stopwatch = new Timer();
 
     // Start Timing
     stopwatch.start();
 
     sensorList = getCapXPath(getCapDoc);
 
     if (sensorList != null) {
       // See if this service has a network all sensor
       // (http://sdf.ndbc.noaa.gov/sos/server.php?request=GetCapabilities&service=SOS)
       if (sensorList.get(0).getName().equals("network-all")) {
         SensorContainer NetworkAll = sensorList.remove(0);
         // Set min and max location Data
         NESW = NetworkAll.getNESW();
         // Set min and max Time
         startTime = NetworkAll.getStartTime();
         endTime = NetworkAll.getEndTime();
       } else {
         // Set default values for NESW
         NESW = new double[4];
         NESW[0] = -90.;
         NESW[1] = -180.;
         NESW[2] = 90.;
         NESW[3] = 180.;
         // Set default values for Time;
         startTime = new Date();
         startTime.setYear(300); // set a way future start time...
 
         endTime = new Date();
         endTime.setYear(0); // set an old end time
 
         for (SensorContainer sensor : sensorList) {
           NESW[0] = Math.max(sensor.getNESW()[0], NESW[0]);
           NESW[1] = Math.max(sensor.getNESW()[1], NESW[1]);
           NESW[2] = Math.min(sensor.getNESW()[2], NESW[2]);
           NESW[3] = Math.min(sensor.getNESW()[3], NESW[3]);
 
           startTime = startTime.before(sensor.getStartTime()) ? startTime : sensor.getStartTime();
           endTime = endTime.after(sensor.getStartTime()) ? endTime : sensor.getEndTime();
         }
 
         // Make sure the year is at least 1990
         Date minTime = new Date();
         minTime.setYear(90);
         minTime.setDate(1);
         minTime.setHours(00);
         minTime.setMinutes(00);
         minTime.setMonth(1);
         minTime.setSeconds(00);
 
         if (startTime.before(minTime)) {
           startTime = minTime;
           pcs.firePropertyChange("message", null, "======= Date Error =======");
           pcs.firePropertyChange("message", null, "Observation Data before 1990 is not available through this client at this time");
           pcs.firePropertyChange("message", null, "==========================");
         }
       }
       float parseTime = stopwatch.seconds();
       pcs.firePropertyChange("message", null, "Seconds to parse SOS capabilities: " + parseTime);
       pcs.firePropertyChange("message", null, "Found " + sensorList.size() + " valid sensors.");
     }
   }
 
   @SuppressWarnings("unchecked")
   public List getCapXPath(Document doc) {
     List<SensorContainer> sensorList = null;
 
     String XPATH_GETEACH_OBS_OFFERING = "//*[local-name()='ObservationOffering']";
     String XPATH_GETEACH_OBS_ATTRIBUTE = "./@gml:id";
     String XPATH_GETEACH_LOWER_CORNER = ".//gml:lowerCorner";
     String XPATH_GETEACH_UPPER_CORNER = ".//gml:upperCorner";
     String XPATH_GETEACH_DESCRIPTION = ".//gml:description";
     String XPATH_GETEACH_PROCEDURE = ".//*[local-name()='procedure']";
     
     String XPATH_GETEACH_PARAMETER = "//*[local-name()='Parameter'][@name='observedProperty']/*[local-name()='AllowedValues']/*[local-name()='Value']";
     
     String XPATH_GETEACH_XLINK_ATTRIBUTE = "./@xlink:href";
     String XPATH_GETEACH_GML_NAME = ".//gml:name";
     String XPATH_GETEACH_PROPERTY = ".//*[local-name()='observedProperty']";
     String XPATH_GETEACH_BEGINPOSITION = ".//gml:beginPosition";
     String XPATH_GETEACH_ENDPOSITION = ".//gml:endPosition";
     String XPATH_GETEACH_INDETERMINATEPOSITION = "./@indeterminatePosition";
     String XPATH_GETEACH_RESPONSEFORMAT = ".//*[local-name()='responseFormat']";
 
     XPath XPATH_Obs_Offering = null;
     XPath XPATH_Obs_Attribute = null;
 
     XPath XPATH_Lower_Corner = null;
     XPath XPATH_Upper_Corner = null;
 
     XPath XPATH_Description = null;
     XPath XPATH_Procedure = null;
     XPath XPATH_Parameter = null;
     XPath XPATH_Xlink_Attribute = null;
     XPath XPATH_Gml_Name = null;
     XPath XPATH_Property = null;
 
     XPath XPATH_BeginPosition = null;
     XPath XPATH_EndPosition = null;
     XPath XPATH_IndeterminatePosition = null;
 
     XPath XPATH_ResponseFormat = null;
 
     try {
 
       XPATH_Obs_Offering = XPath.newInstance(XPATH_GETEACH_OBS_OFFERING);
       XPATH_Obs_Attribute = XPath.newInstance(XPATH_GETEACH_OBS_ATTRIBUTE);
 
       XPATH_Lower_Corner = XPath.newInstance(XPATH_GETEACH_LOWER_CORNER);
       XPATH_Upper_Corner = XPath.newInstance(XPATH_GETEACH_UPPER_CORNER);
 
       XPATH_Description = XPath.newInstance(XPATH_GETEACH_DESCRIPTION);
       XPATH_Procedure = XPath.newInstance(XPATH_GETEACH_PROCEDURE);
       
       XPATH_Parameter = XPath.newInstance(XPATH_GETEACH_PARAMETER);
       
       XPATH_Xlink_Attribute = XPath.newInstance(XPATH_GETEACH_XLINK_ATTRIBUTE);
 
       XPATH_Gml_Name = XPath.newInstance(XPATH_GETEACH_GML_NAME);
 
       XPATH_Property = XPath.newInstance(XPATH_GETEACH_PROPERTY);
 
       XPATH_BeginPosition = XPath.newInstance(XPATH_GETEACH_BEGINPOSITION);
       XPATH_EndPosition = XPath.newInstance(XPATH_GETEACH_ENDPOSITION);
       XPATH_IndeterminatePosition = XPath.newInstance(XPATH_GETEACH_INDETERMINATEPOSITION);
 
       XPATH_ResponseFormat = XPath.newInstance(XPATH_GETEACH_RESPONSEFORMAT);

     } catch (JDOMException e1) {
       e1.printStackTrace();
     }
 
     // Get the list of variables in this service
     List parameterObjects = null;
     try {
       parameterObjects = XPATH_Parameter.selectNodes(doc);
       int cnt = parameterObjects.size();
       int index = 0;
       for (Object o : parameterObjects) {
         if (!(o instanceof Element)) {
           System.out.println("xpathGetCapabilities: Unknown Instance of Parameter (Property Names)!");
           return null;
         }
         Element names = (Element) o;
         index = index + 1;
       }
     } catch (JDOMException e) {
       System.out.println("xpathGetCapabilities: XPATH error reading Parameter names!");
       e.printStackTrace();
       return null;
     } catch (NullPointerException enull) {
       enull.printStackTrace();
       System.out.println("xpathGetCapabilities: error reading Parameter names - returned null!");
       return null;
     }
 
     // Get the list of observations and parse it:
     List obsList = null;
     try {
       obsList = XPATH_Obs_Offering.selectNodes(doc);
 
     } catch (JDOMException e) {
       System.out.println("xpathGetCapabilities: error reading ObservationOffering!");
       e.printStackTrace();
     }
 
     if (obsList == null || obsList.size() <= 0) {
       System.out.println("xpathGetCapabilities: obsList is null or empty!");
       return sensorList;
     }
 
     sensorList = new ArrayList<SensorContainer>();
     // Instantiate double array here and copy inside the setter...
     double[] NESW = new double[4];
 
     // Create DateFormats
     dateFormats = new String[12];
     dateFormats[0] = "yyyy-MM-dd'T'HH:mm:ss'.'S'Z'";// Zulu with Millis
     dateFormats[1] = "yyyy-MM-dd'T'HH:mm:ss'.'SZ";  // Millis, RFC822 zone
     dateFormats[2] = "yyyy-MM-dd'T'HH:mm:ss'.'Sz";  // Millis, long zone
     dateFormats[3] = "yyyy-MM-dd'T'HH:mm:ss'.'S";   // Millis, no zone
     dateFormats[4] = "yyyy-MM-dd'T'HH:mm:ssZ";      // ISO8601 long, RFC822 zone
     dateFormats[5] = "yyyy-MM-dd'T'HH:mm:ssz";      // ISO8601 long, long zone
     dateFormats[6] = "yyyy-MM-dd'T'HH:mm:ss'Z'";    // Zulu
     dateFormats[7] = "yyyy-MM-dd'T'HH:mm:ss";       // No 'Z', No zone
     dateFormats[8] = "yyyy-MM-dd'T'HH:mmZ";         // No seconds, RFC822 zone
     dateFormats[9] = "yyyy-MM-dd'T'HH:mmz";         // No seconds, long zone
     dateFormats[10] = "yyyy-MM-dd'T'HH:mm";         // No seconds, no zone
     dateFormats[11] = "yyyyMMddHHmmssZ";            // ISO8601 short
 
     for (Object o : obsList) {
 
       if (!(o instanceof Element)) {
         System.out.println("xpathGetCapabilities: Unknown Instance of ObservationOffering!");
         continue;
       }
 
       Element obsOffering = (Element) o;
 
       SensorContainer sensor = new SensorContainer();
 
       // GET the gmlId of the Observation
       try {
         Attribute gmlId = (Attribute) XPATH_Obs_Attribute.selectSingleNode(obsOffering);
         sensor.setName(gmlId.getValue());
         // gmlId = (Attribute)
         // XPath.selectSingleNode(obsOffering,"./@gml:id");
       } catch (JDOMException e1) {
         System.out.println("Can not find gmlId: JDOM exception");
         e1.printStackTrace();
       } catch (NullPointerException enull) {
         enull.printStackTrace();
         System.out.println("Can not find gmlId Attributre: returned null attribute");
       }
 
       // Get Upper Corner:
       double[] ll = null;
       try {
         Element lowerCorner = (Element) XPATH_Lower_Corner.selectSingleNode(obsOffering);
         ll = SosServer.parseBnds(lowerCorner.getValue());
       } catch (JDOMException e) {
         e.printStackTrace();
         System.out.println("Can not find Lower Corner: JDOM exception");
       } catch (NullPointerException enull) {
         enull.printStackTrace();
         System.out.println("Can not find Lower Corner: returned null lowerCorner");
       }
 
       // Get Upper Corner:
       double[] ur = null;
       try {
         Element upperCorner = (Element) XPATH_Upper_Corner.selectSingleNode(obsOffering);
         // upperCorner = (Element)
         // XPath.selectSingleNode(obsOffering,XPATH_GETEACH_UPPER_CORNER);
         ur = SosServer.parseBnds(upperCorner.getValue());
 
       } catch (JDOMException e) {
         e.printStackTrace();
         System.out.println("Can not find Upper Corner: JDOM exception");
       } catch (NullPointerException enull) {
         System.out.println("Can not find Upper Corner: returned null upperCorner");
       }
 
       if (ll != null && ur != null) {
         NESW[0] = ur[0];
         NESW[1] = ur[1];
         NESW[2] = ll[0];
         NESW[3] = ll[1];
 
         sensor.setNESW(NESW);
       }
 
       // Get Description
       try {
         Element description = (Element) XPATH_Description.selectSingleNode(obsOffering);
         if (description != null) {
           sensor.setDescription(description.getValue());
         }
       } catch (JDOMException e) {
         e.printStackTrace();
         System.out.println("Can not find Description: JDOM exception");
       } catch (NullPointerException enull) {
         // enull.printStackTrace();
         System.out.println("Can not find Description: returned null description");
       }
 
       // Get gml:Name
       try {
         Element name = (Element) XPATH_Gml_Name.selectSingleNode(obsOffering);
         sensor.setGmlName(name.getValue());
       } catch (JDOMException e) {
         e.printStackTrace();
         System.out.println("Can not find Description: JDOM exception");
         sensor = null;
         continue;
       } catch (NullPointerException enull) {
         // enull.printStackTrace();
         System.out.println("Can not find Description: returned null description");
         sensor = null;
         continue;
       }
 
       // Get BeginPosition
       Element begin = null;
       try {
         begin = (Element) XPATH_BeginPosition.selectSingleNode(obsOffering);
        sensor.setStartTime(DateUtils.parseDate(begin.getValue().trim(), dateFormats));
       } catch (JDOMException e) {
         System.out.println("Can not find BeginPosition: JDOM exception");
         sensor = null;
         continue;
       } catch (NullPointerException enull) {
         System.out.println("Can not find BeginPosition: returned null beginPosition");
         sensor = null;
         continue;
       } catch (ParseException e) {
         System.out.println("================= Can not parse beginPosition ==================");
         System.out.println(e.getLocalizedMessage());
         sensor = null;
         continue;
       }
 
       // Get endPosition
       Element ending = null;
       String endString = null;
       try {
         ending = (Element) XPATH_EndPosition.selectSingleNode(obsOffering);
         Attribute indPos = (Attribute) XPATH_IndeterminatePosition.selectSingleNode(ending);
         endString = ending.getValue().trim();
         if (endString != null && endString.length() > 0) {
           sensor.setEndTime(DateUtils.parseDate(endString, dateFormats));
         } else if (indPos.getValue() != null
                 && (indPos.getValue().equals("now") || indPos.getValue().equals("unknown"))) {
           sensor.setEndTime(new Date()); // Defaults to now
         }
       } catch (JDOMException e) {
         System.out.println("Can not find EndPosition: JDOM exception");
         sensor = null;
         continue;
       } catch (NullPointerException enull) {
         System.out.println("Can not find EndPosition: returned null endPosition");
         sensor = null;
         continue;
       } catch (ParseException e) {
         System.out.println("================== Can not parse EndPosition ===================");
         System.out.println(e.getLocalizedMessage());
         sensor = null;
         continue;
       }
 
       // Get Procedure
       List procedureList = null;
       try {
         procedureList = XPATH_Procedure.selectNodes(obsOffering);
 
       } catch (JDOMException e) {
         System.out.println("xpathGetCapabilities: error reading procedureList!");
         e.printStackTrace();
       }
 
       // Get ResponseFormats
       ArrayList<String> responseFormats = new ArrayList<String>();
       List responseFormatList = null;
       try {
         responseFormatList = XPATH_ResponseFormat.selectNodes(obsOffering);
       } catch (JDOMException e) {
         System.out.println("xpathGetCapabilities: error reading responseFormat!");
         e.printStackTrace();
         sensor = null;
         continue;
       }
       int cnt, ind;
       cnt = responseFormatList.size();
       for (ind = 0; ind < cnt; ind++) {
         Element format = (Element) responseFormatList.get(ind);
         responseFormats.add(format.getValue().trim());
       }
       sensor.setResponseFormats(responseFormats);
 
       // Get Properties (variables)
       List propertyList = null;
       try {
         propertyList = XPATH_Property.selectNodes(obsOffering);
 
       } catch (JDOMException e) {
         System.out.println("xpathGetCapabilities: error reading procedureList!");
         e.printStackTrace();
         sensor = null;
         continue;
       }
 
       cnt = propertyList.size();
       for (ind = 0; ind < cnt; ind++) {
 
         VariableContainer var = null;
         Element procedure = null;
         Element property = null;
         try {
           procedure = (Element) procedureList.get(ind);
         } catch (ClassCastException e) {
           System.out.println("xpathGetCapabilities: error casting procedure and properties!");
           continue;
         } catch (IndexOutOfBoundsException ee) {
           //System.out.println("xpathGetCapabilities: More properties than procedures!");
         }
 
         try {
           property = (Element) propertyList.get(ind);
         } catch (ClassCastException e) {
           System.out.println("xpathGetCapabilities: error casting procedure and properties!");
           continue;
         }
 
         // Escape if there is no property - that means there is nothing
         // to do...
         if (property != null) {
           var = new VariableContainer();
         } else {
           continue;
         }
 
         try {
           Attribute xlink = (Attribute) XPATH_Xlink_Attribute.selectSingleNode(property);
 
           String Name = xlink.getValue();
           var.setProperty(Name);
 
           Character divider;
           if (Name.contains("http://")) {
             Name = Name.substring(Name.lastIndexOf('/') + 1);
           }
 
           if (Name.contains(":")) {
             divider = ':';
           } else if (Name.contains("#")) {
             divider = '#';
           } else {
             divider = ' ';
           }
 
           if (!divider.equals(' ')) {
             var.setName(Name.substring(Name.lastIndexOf(divider) + 1));
           } else {
             var.setName(Name);
           }
 
         } catch (JDOMException e) {
           e.printStackTrace();
           System.out.println("Can not find Xlink Attribute: JDOM exception");
           var = null;
         } catch (NullPointerException enull) {
           // enull.printStackTrace();
           System.out.println("Can not find Xlink Attribute: returned null Attribute");
           var = null;
         }
 
         // Only add the procedure if it exists...
         if (procedure != null) {
           try {
             Attribute xlink = (Attribute) XPATH_Xlink_Attribute.selectSingleNode(procedure);
             var.setProcedure(xlink.getValue());
 
           } catch (JDOMException e) {
             e.printStackTrace();
             System.out.println("Can not find Xlink Attribute: JDOM exception");
             var = null;
             sensor = null;
             continue;
           } catch (NullPointerException enull) {
             // enull.printStackTrace();
             System.out.println("Can not find Xlink Attribute: returned null Attribute");
             var = null;
             sensor = null;
             continue;
           }
         }
 
         if (var != null) {
           sensor.addVariable(var);
         }
       }
       // Add the Sensor to the list
       sensorList.add(sensor);
     }
     return sensorList;
   }
 
   public void setURL(String URL) {
     sosURL = URL;
   }
 
   public LatLonRect getBBOX() {
     LatLonPointImpl uL = new LatLonPointImpl(NESW[0], NESW[3]);
     LatLonPointImpl lR = new LatLonPointImpl(NESW[2], NESW[1]);
     LatLonRect llr = new LatLonRect(uL, lR);
     return llr;
   }
 
   public Date getStartTime() {
     return startTime;
   }
 
   public Date getEndTime() {
     return endTime;
   }
 
   public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
     pcs.addPropertyChangeListener(l);
   }
 
   public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
     pcs.removePropertyChangeListener(l);
   }
 
   public void propertyChange(PropertyChangeEvent evt) {
     pcs.firePropertyChange(evt);
   }
 }
