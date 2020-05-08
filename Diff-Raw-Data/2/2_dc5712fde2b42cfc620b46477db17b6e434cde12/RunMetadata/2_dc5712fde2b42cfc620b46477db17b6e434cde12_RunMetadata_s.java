 package gov.usgs.cida.watersmart.parse;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import gov.usgs.cida.watersmart.parse.CreateDSGFromZip.ModelType;
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class RunMetadata {
     
     private static final Logger LOG = LoggerFactory.getLogger(RunMetadata.class);
     private static final String UPLOAD_EXTENSION = ".zip";
     
     private ModelType type;
     private String modelId;
     private String name;
     private String modelVersion;
     private String runIdent;
     private DateTime creationDate;
     private String scenario;
     private String comments;
     private String email;
     private String wfsUrl;
     private String layerName;
     private String commonAttribute;
     
     private static final Map<String, String> XPATH_MAP = Maps.newLinkedHashMap();
     private static final String XPATH_SUBSTITUTION_SCENARIO = "{scenario}";
     private static final String XPATH_SUBSTITUTION_MODEL_VERSION = "{modelVersion}";
     private static final String XPATH_SUBSTITUTION_RUN_IDENTIFIER = "{runIdentifier}";
         
     private static final String UPDATE_XPATH_TEMPLATE = "gmd:identificationInfo/srv:SV_ServiceIdentification[@id='ncSOS']/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString[text()='" +
                                XPATH_SUBSTITUTION_SCENARIO + "']/../../gmd:edition/gco:CharacterString[text()='" + 
                                XPATH_SUBSTITUTION_MODEL_VERSION + "." + XPATH_SUBSTITUTION_RUN_IDENTIFIER + 
                                "']/../../../..";
     
     private static final List<DateTimeFormatter> dateInputFormats = Lists.newArrayList();
     static {
         dateInputFormats.add(
             new DateTimeFormatterBuilder()
             .appendMonthOfYear(1)
             .appendLiteral('/')
             .appendDayOfMonth(1)
             .appendLiteral('/')
             .appendYear(4, 4)
             .toFormatter());
         
         dateInputFormats.add(
             new DateTimeFormatterBuilder()
             .appendMonthOfYear(1)
             .appendLiteral('-')
             .appendDayOfMonth(1)
             .appendLiteral('-')
             .appendYear(4, 4)
             .toFormatter());
         
         dateInputFormats.add(
             ISODateTimeFormat.dateTimeParser());
     }
 
     
     static {
         XPATH_MAP.put("name", "/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString");
         XPATH_MAP.put("date", "/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime");
         XPATH_MAP.put("email", "/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");
         XPATH_MAP.put("comments", "/gmd:abstract/gco:CharacterString");
         XPATH_MAP.put("scenario", "/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
         // Edition is modelVersion and runIdentifier, need to be changed at same time
         XPATH_MAP.put("edition", "/gmd:citation/gmd:CI_Citation/gmd:edition/gco:CharacterString");
     }
     
     public RunMetadata() {
         type = null;
         modelId = null;
         name = null;
         modelVersion = null;
         runIdent = null;
         creationDate = null;
         scenario = null;
         comments = null;
         email = null;
         wfsUrl = null;
         layerName = null;
         commonAttribute = null;
     }
     
     public RunMetadata(ModelType type, String modelId, String name, String modelVersion, String runIdent, String creationDate, String scenario, String comments, String email, String wfsUrl, String layerName, String commonAttribute) {
         this.type = type;
         this.modelId = modelId;
         this.name = name;
         this.modelVersion = modelVersion;
         this.runIdent = runIdent;
         this.creationDate = parseDate(creationDate);
         this.scenario = scenario;
         this.comments = comments;
         this.email = email;
         this.wfsUrl = wfsUrl;
         this.layerName = layerName;
         this.commonAttribute = commonAttribute;
     }
     
     public boolean isFilledIn() {
         return (type != null &&
                 creationDate != null &&
                 StringUtils.isNotBlank(modelId) &&
                 StringUtils.isNotBlank(name) &&
                 StringUtils.isNotBlank(modelVersion) &&
                 StringUtils.isNotBlank(runIdent) &&
                 StringUtils.isNotBlank(scenario) &&
                 StringUtils.isNotBlank(email) &&
                 StringUtils.isNotBlank(wfsUrl) &&
                 StringUtils.isNotBlank(layerName) &&
                 StringUtils.isNotBlank(commonAttribute) &&
                 comments != null);
     }
     
     /**
      * Tries to set value corresponding to the item
      * @param item file form field item
      * @return true if item is set, false if not found
      */
     public boolean set(FileItem item) {
         String param = item.getFieldName().toLowerCase();
         if ("modeltype".equals(param)) {
             ModelType mt = ModelType.valueOf(item.getString());
             setType(mt);
             return true;
         }
         if ("modelid".equals(param)) {
             setModelId(item.getString());
             return true;
         }
         if("name".equals(param)) {
             setName(item.getString());
             return true;
         }
         if ("modelversion".equals(param)) {
             setModelVersion(item.getString());
             return true;
         }
         if ("runident".equals(param)) {
             setRunIdent(item.getString());
             return true;
         }
         if ("creationdate".equals(param)) {
             setCreationDate(item.getString());
             return true;
         }
         if ("scenario".equals(param)) {
             setScenario(item.getString());
             return true;
         }
         if ("comments".equals(param)) {
             setComments(item.getString());
             return true;
         }
         if ("email".equals(param)) {
             setEmail(item.getString());
             return true;
         }
         if ("wfsurl".equals(param)) {
             setWfsUrl(item.getString());
             return true;
         }
         if ("layer".equals(param)) {
             setLayerName(item.getString());
             return true;
         }
         if ("commonattr".equals(param)) {
             setCommonAttribute(item.getString());
             return true;
         }
         return false;
     }
     
     public File getFile(String dirPath) throws UnsupportedOperationException {
         if (!isFilledIn()) {
             String err = "Call to getFile before all fields are set";
             LOG.debug(err);
             throw new UnsupportedOperationException(err);
         }
         
         if (StringUtils.isNotBlank(dirPath)) {
             File dir = new File(dirPath);
             if (dir.exists() && dir.canWrite()) {
                 String file = getFileName() + UPLOAD_EXTENSION;
                 return new File(dirPath + File.separator + file);
             }
         }
         // Did not return, must have failed
         String err = "Must pass in a valid directory in which to create file";
         LOG.debug(err);
         throw new IllegalArgumentException(err);
     }
     
     public String getFileName() {
         StringBuilder str = new StringBuilder();
         str.append(getTypeString())
            .append("-")
            .append(scenario)
            .append("-")
            .append(getEditionString());
         return str.toString();
     }
 
     public String getComments() {
         return comments;
     }
 
     public void setComments(String comments) {
         this.comments = comments;
     }
 
     public String getCreationDate() {
         return creationDate.toString(ISODateTimeFormat.dateTimeNoMillis());
     }
 
     public void setCreationDate(String creationDate) {
         this.creationDate = parseDate(creationDate);
     }
 
     /**
      * Uses predefined date parsers to create DateTime object
      * @param date parsed Date
      * @return parsed date or null if invalid input date
      */
     private static DateTime parseDate(String date) {
         DateTime parsedDate = null;
         for (DateTimeFormatter dtf : dateInputFormats) {
             try {
                parsedDate = dtf.parseDateTime(date);
                 break; // throws exception if parse fails
             }
             catch (IllegalArgumentException ex) {
                 // try again
             }
         }
         return parsedDate;
     }
     
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getModelVersion() {
         return modelVersion;
     }
 
     public void setModelVersion(String modelVersion) {
         this.modelVersion = modelVersion;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getRunIdent() {
         return runIdent;
     }
 
     public void setRunIdent(String runIdent) {
         this.runIdent = runIdent;
     }
 
     public String getScenario() {
         return scenario;
     }
 
     public void setScenario(String scenario) {
         this.scenario = scenario;
     }
 
     public ModelType getType() {
         return type;
     }
     
     public String getTypeString() {
         return getType().toString().toLowerCase();
     }
 
     public void setType(ModelType type) {
         this.type = type;
     }
     
     public String getCommonAttribute() {
         return commonAttribute;
     }
 
     public void setCommonAttribute(String commonAttr) {
         this.commonAttribute = commonAttr;
     }
 
     public String getLayerName() {
         return layerName;
     }
 
     public void setLayerName(String layerName) {
         this.layerName = layerName;
     }
 
     public String getWfsUrl() {
         return wfsUrl;
     }
 
     public void setWfsUrl(String wfsUrl) {
         this.wfsUrl = wfsUrl;
     }
 
     /**
      * @return the modelId
      */
     public String getModelId() {
         return modelId;
     }
 
     /**
      * @param modelId the modelId to set
      */
     public void setModelId(String modelId) {
         this.modelId = modelId;
     }
     
     public String getEditionString() {
         return modelVersion + "." + runIdent;
     }
     
     public String get(String var) {
         if ("edition".equals(var)) {
             return getEditionString();
         } else if ("name".equals(var)) {
             return getName();
         } else if ("scenario".equals(var)) {
             return getScenario();
         } else if ("date".equals(var)) {
             return getCreationDate();
         } else if ("comments".equals(var)) {
             return getComments();
         } else if ("email".equals(var)) {
             return getEmail();
         } else {
             throw new IllegalArgumentException("This is not a thing");
         }
     }
     
     public Map<String, String> getUpdateMap(RunMetadata oldMetadata) {
         String updateXpath = UPDATE_XPATH_TEMPLATE
                 .replace(XPATH_SUBSTITUTION_SCENARIO, oldMetadata.getScenario())
                 .replace(XPATH_SUBSTITUTION_MODEL_VERSION, oldMetadata.getModelVersion())
                 .replace(XPATH_SUBSTITUTION_RUN_IDENTIFIER, oldMetadata.getRunIdent());
         
         Map<String, String> propsMap = Maps.newLinkedHashMap();
         for (String key : XPATH_MAP.keySet()) {
             if ("edition".equals(key)) {
                 updateXpath = UPDATE_XPATH_TEMPLATE
                 .replace(XPATH_SUBSTITUTION_SCENARIO, getScenario()) // scenario has already been changed, maybe
                 .replace(XPATH_SUBSTITUTION_MODEL_VERSION, oldMetadata.getModelVersion())
                 .replace(XPATH_SUBSTITUTION_RUN_IDENTIFIER, oldMetadata.getRunIdent());
             }
             
             propsMap.put(updateXpath + XPATH_MAP.get(key), this.get(key));
         }
         return propsMap;
     }
 }
