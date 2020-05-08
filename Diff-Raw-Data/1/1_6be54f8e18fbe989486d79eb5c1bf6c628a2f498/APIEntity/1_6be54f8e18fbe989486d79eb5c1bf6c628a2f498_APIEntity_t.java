 package com.testdroid.api;
 
 import com.testdroid.api.model.*;
 import com.testdroid.api.model.AndroidFiles;
 import com.testdroid.api.model.IOSFiles;
 import com.testdroid.api.model.RemoteControlFiles;
 import com.testdroid.api.model.UIAutomatorFiles;
 import java.io.File;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlSeeAlso;
 import javax.xml.bind.annotation.XmlTransient;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonView;
 
 /**
  *
  * @author Łukasz Kajda <lukasz.kajda@bitbar.com>
  */
 @XmlRootElement
 @XmlSeeAlso({
     APIArray.class,
     APIDevice.class,
     APIDeviceGroup.class,
     APIDeviceProperty.class,
     APIDeviceRun.class,
     APIDeviceRunState.class,
    APIExceptionMessage.class,
     APIFiles.class,
     APIFiles.AndroidAppFile.class,
     APIFiles.AndroidTestFile.class,
     APIFiles.APIFile.class,
     APIFiles.CalabashTestFile.class,
     APIFiles.DataFile.class,
     APIFiles.IOSAppFile.class,
     APIFiles.IOSTestFile.class,
     APIFiles.UIAutomatorTestFile.class,
     APILabelGroup.class,
     APIList.class,
     APINotificationEmail.class,
     APIProject.class,
     APIProjectJobConfig.class,
     APIProjectSharing.class,
     APIRecorderOnlineSession.class,
     APIRole.class,
     APIScreenshot.class,
     APISoftwareVersion.class,
     APITag.class,
     APITestCaseRun.class,
     APITestCaseRunStep.class,
     APITestRun.class,
     APITestRunConfig.class,
     APITestRunParameter.class,
     APIUser.class,
     AndroidFiles.class, 
     CalabashFiles.class, 
     IOSFiles.class, 
     RecorderOnlineFiles.class,
     RemoteControlFiles.class, 
     UIAutomatorFiles.class
     })
 public abstract class APIEntity {
 
     private static final DateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
     private static final String ENCODING = "UTF-8";
 
     private static final HashMap<Class, JAXBContext> contextMap = new HashMap<Class, JAXBContext>();
 
     protected APIClient client;
     protected String selfURI;
     protected Long id;
     protected Class<? extends APIView> view;
     
     public APIEntity() {
     }
 
     public APIEntity(Long id) {
         this.id = id;
     }
 
     /**
      * Returns ID of entity if such ID exists. Usually it does not exist for
      * lists. Please use
      * <code>hasId()</code> method to check if ID exists.
      */
     @JsonView(APIView.class)
     public Long getId() {
         return this.id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @JsonIgnore
     protected <T extends APIEntity> APIResource<T> getResource(String uri, Class<T> type) throws APIException {
         checkClient(client);
         return new APIResource<T>(client, uri, type);
     }
 
     @JsonIgnore
     protected <T extends APIEntity> APIListResource<T> getListResource(String uri, Class<T> type) throws APIException {
         checkClient(client);
         return new APIListResource<T>(client, uri, type);
     }
 
     @JsonIgnore
     protected <T extends APIEntity> APIListResource<T> getListResource(String uri, APIQueryBuilder queryBuilder, Class<T> type) throws APIException {
         checkClient(client);
         return new APIListResource<T>(client, queryBuilder.build(uri), type);
     }
     
     @JsonIgnore
     protected <T extends APIEntity> APIListResource<T> getListResource(String uri, long offset, long limit, String search, APISort sort, Class<T> type) throws APIException {
         checkClient(client);
         if(limit <= 0) {
             limit = 10;
         }
         return new APIListResource<T>(client, uri, offset, limit, search, sort, type);
     }
 
     @JsonIgnore
     protected <T extends APIEntity> T postResource(String uri, Object body, Class<T> type) throws APIException {
         checkClient(client);
         return client.post(uri, body, type);
     }
 
     @JsonIgnore
     protected <T extends APIEntity> T postFile(String uri, File file, String contentType, Class<T> type) throws APIException {
         checkClient(client);
         return client.postFile(uri, contentType, file, type);
     }
 
     @JsonIgnore
     protected void deleteResource(String uri) throws APIException {
         checkClient(client);
         client.delete(uri);
     }
     
     @JsonIgnore
     protected InputStream getFile(String uri) throws APIException {
         checkClient(client);
         return client.get(uri);
     }
 
     @JsonIgnore
     public void refresh() throws APIException {
         checkClient(client);
         clone(client.get(selfURI, getClass()));
     }
     
     @JsonIgnore
     private void checkClient(APIClient client) throws APIException {
         if (client == null) {
             throw new APIException("Missing API client");
         }
     }
 
     @JsonIgnore
     public static String encodeURL(String name) {
         try {
             if (name == null) {
                 return "";
             }
             return URLEncoder.encode(name, ENCODING);
         } catch (UnsupportedEncodingException ex) { }
 
         return name;
     }
     
     @JsonIgnore
     public static String decodeURL(String name) {
         try {
             return URLDecoder.decode(name, ENCODING);
         } catch (UnsupportedEncodingException ex) {
         }
         return name;
     }
 
     @JsonIgnore
     public static String format(Date date) {
         return API_DATE_FORMAT.format(date);
     }
 
     @JsonIgnore
     public static String formatAndEncodeURL(Date date) {
         return format(date);
     }
 
     @JsonIgnore
     public boolean hasView() {
         return view != null;
     }
     
     @JsonIgnore
     @XmlTransient
     public Class<? extends APIView> getView() {
         return view;
     }
     
     @JsonIgnore
     public void setView(Class<? extends APIView> view) {
         this.view = view;
     }
     
     @JsonIgnore
     public String toXML() {
         try {
             JAXBContext context = getJAXBContext(this.getClass());
             Marshaller marshaller = context.createMarshaller();
             StringWriter writer = new StringWriter();
             marshaller.marshal(this, writer);
             return writer.toString();
         } catch (JAXBException ex) {
             Logger.getLogger(APIEntity.class.getName()).log(Level.SEVERE, null, ex);
         }
         return "";
     }
 
     @JsonIgnore
     public static <T extends APIEntity> T fromXML(String xml, Class<T> type) {
         try {
             JAXBContext context = getJAXBContext(type);
             Unmarshaller unmarshaller = context.createUnmarshaller();
             return (T) unmarshaller.unmarshal(new StringReader(xml));
         } catch (JAXBException ex) {
             Logger.getLogger(APIEntity.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     @JsonIgnore
     public static JAXBContext getJAXBContext(Class type) throws JAXBException {
         JAXBContext context = contextMap.get(type);
         if(context == null) {
             context = JAXBContext.newInstance(type);
             contextMap.put(type, context);
             return context;
         } else {
             return context;
         }
     }
 
     @JsonIgnore
     protected abstract <T extends APIEntity> void clone(T from);
     
     @JsonIgnore
     protected <T extends APIEntity> void cloneBase(T from) {
         this.id = from.id;
         this.view = from.view;
     }
     
     @JsonIgnore
     public static String createUri(String preUri, String postUri) {
         if(preUri.contains("?")) {
             String[] parts = preUri.split("\\?");
             return String.format("%s%s", parts[0], postUri);
         } else {
             return String.format("%s%s", preUri, postUri);
         }
     }
 }
