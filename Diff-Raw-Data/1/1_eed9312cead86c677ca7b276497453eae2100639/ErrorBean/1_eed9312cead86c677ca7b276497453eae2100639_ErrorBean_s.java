 package gov.usgs.gdp.bean;
 
 import gov.usgs.gdp.helper.PropertyFactory;
 
 import java.security.InvalidParameterException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 
 @XStreamAlias("error")
 public class ErrorBean implements XmlBean {
     // Check properties file to read/change message for these codes
 
     public final static int ERR_NO_COMMAND = 0;
     public final static int ERR_USER_DIR_CREATE = 1;
     public final static int ERR_FILE_UPLOAD = 2;
     public final static int ERR_FILE_LIST = 3;
     public final static int ERR_FILE_NOT_FOUND = 4;
     public final static int ERR_ATTRIBUTES_NOT_FOUND = 5;
     public final static int ERR_FEATURES_NOT_FOUND = 6;
     public final static int ERR_ERROR_WHILE_CONNECTING = 7;
     public final static int ERR_MISSING_PARAM = 8;
     public final static int ERR_MISSING_DATASET = 9;
     public final static int ERR_PORT_INCORRECT = 10;
     public final static int ERR_MISSING_THREDDS = 11;
     public final static int ERR_MISSING_TIMERANGE = 12;
     public final static int ERR_PARSE_TIMERANGE = 13;
     public final static int ERR_INVALID_URL = 14;
     public final static int ERR_PROTOCOL_VIOLATION = 15;
     public final static int ERR_TRANSPORT_ERROR = 16;
     public final static int ERR_OUTFILES_UNAVAILABLE = 17;
     public final static int ERR_BOX_NO_INTERSECT_GRID = 18;
     public final static int ERR_EMAIL_ERROR = 19;
     public final static int ERR_EMAIL_ERROR_INCORRECT_ADDRESS = 20;
     public final static Map<Integer, String> ERROR_MESSAGES;
     @XStreamAlias("code")
     @XStreamAsAttribute
     private Integer errorNumber;
     @XStreamAlias("message")
     private String errorMessage;
     @XStreamAlias("exception")
     private Exception exception;
     @XStreamAlias("error-created")
     private Date errorCreated;
     @XStreamAlias("error-class")
     private String errorClass;
 
     static {
         ERROR_MESSAGES = new TreeMap<Integer, String>();
         List<String> errorMessageList = PropertyFactory.getValueList("error.message");
         for (int errorListIndex = 0; errorListIndex < errorMessageList.size(); errorListIndex++) {
             ERROR_MESSAGES.put(Integer.valueOf(errorListIndex), errorMessageList.get(errorListIndex));
         }
     }
 
     public ErrorBean() {
         // Default constructor
     }
 
     public ErrorBean(int errorNumberParam) throws InvalidParameterException {
         this(Integer.valueOf(errorNumberParam), ErrorBean.ERROR_MESSAGES.get(Integer.valueOf(errorNumberParam)));
     }
 
     public ErrorBean(String errorMessageParam) {
         this(Integer.valueOf(-1), errorMessageParam);
     }
 
     public ErrorBean(Integer errorNumberParam, String errorMessageParam) {
         this(errorNumberParam, errorMessageParam, null);
     }
 
     public ErrorBean(int errorNumberParam, Exception stackTrace) {
         this(Integer.valueOf(errorNumberParam), ErrorBean.ERROR_MESSAGES.get(Integer.valueOf(errorNumberParam)), stackTrace, null);
     }
 
     public ErrorBean(Integer errorNumberParam, String errorMessageParam, Exception stackTrace) {
         this(errorNumberParam, errorMessageParam, stackTrace, null);
     }
 
     public ErrorBean(Integer errorNumberParam, String errorMessageParam, Exception stackTrace, String errorClassParam) {
         setErrorMessage(errorMessageParam);
         setErrorNumber(errorNumberParam);
         setException(stackTrace);
         setErrorClass(errorClassParam);
         setErrorCreated(new Date());
     }
 
     @Override
     public String toXml() {
         XStream xstream = new XStream();
         xstream.processAnnotations(ErrorBean.class);
         StringBuffer sb = new StringBuffer();
         String result = "";
         sb.append(xstream.toXML(this));
         result = sb.toString();
         return result;
     }
 
     public void setErrorMessage(String errorMessageParam) {
         this.errorMessage = errorMessageParam;
     }
 
     public String getErrorMessage() {
         return this.errorMessage;
     }
 
     public void setException(Exception stackTrace) {
         this.exception = stackTrace;
     }
 
     public Exception getException() {
         return this.exception;
     }
 
     public void setErrorCreated(Date errorCreatedParam) {
         this.errorCreated = errorCreatedParam;
     }
 
     public Date getErrorCreated() {
         return this.errorCreated;
     }
 
     public void setErrorClass(String errorClassParam) {
         this.errorClass = errorClassParam;
     }
 
     @Override
     public String toString() {
         return "ErrorBean [errorClass=" + this.errorClass + ", errorCreated="
                 + this.errorCreated + ", errorMessage=" + this.errorMessage
                 + ", errorNumber=" + this.errorNumber + ", exception=" + this.exception
                 + "]";
     }
 
     public String getErrorClass() {
         return this.errorClass;
     }
 
     public void setErrorNumber(Integer errorNumberParam) {
         this.errorNumber = errorNumberParam;
     }
 
     public Integer getErrorNumber() {
         return this.errorNumber;
     }
 }
