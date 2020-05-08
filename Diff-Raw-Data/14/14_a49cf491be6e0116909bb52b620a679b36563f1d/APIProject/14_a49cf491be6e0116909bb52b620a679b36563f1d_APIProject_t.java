 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIListResource;
 import com.testdroid.api.APISort;
 import com.testdroid.api.model.APIFiles.APIFile;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement
 public class APIProject extends APIEntity {
 
     @XmlType(name = "projectType")
     public static enum Type { 
         ANDROID, CTS, IOS, UIAUTOMATOR, REMOTECONTROL, RECORDERONLINE, CALABASH;
         public Class<? extends APIFiles> getFilesClass() {
             switch(this) {
                 case ANDROID: return AndroidFiles.class;
                 case CTS: return null;
                 case IOS: return IOSFiles.class;
                 case UIAUTOMATOR: return UIAutomatorFiles.class;
                 case REMOTECONTROL: return RemoteControlFiles.class;
                 case RECORDERONLINE: return RecorderOnlineFiles.class;
                 case CALABASH: return CalabashFiles.class;
                 default: return null;
             }
         }
     }
 
     private String name;
     private String description;
     private Type type;
     private boolean common;
     private Long sharedById;
 
     public APIProject() {
 
     }
     public APIProject(Long id, String name, String description, Type type, Long sharedById, boolean common) {
         super(id);
         this.name = name;
         this.description = description;
         this.type = type;
         this.sharedById = sharedById;
         this.common = common;
         jobConfig = new HashMap<APIProjectJobConfig.Type, APIProjectJobConfig>();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public boolean isCommon() {
         return common;
     }
 
     public void setCommon(boolean common) {
         this.common = common;
     }
 
     /**
      * Returns user ID sharing this project or null if project is owned or common.
      */
     public Long getSharedById() {
         return sharedById;
     }
 
     public void setSharedById(Long sharedById) {
         this.sharedById = sharedById;
     }
 
     private APITestRunConfig testRunConfig;
     private Map<APIProjectJobConfig.Type, APIProjectJobConfig> jobConfig;
     private APIFiles files;
     private byte[] icon;
 
     private String getConfigURI() { return selfURI + "/config"; };
     private String getJobConfigURI(APIProjectJobConfig.Type type) { return selfURI + "/job-configs/"+type.toString(); };
     private String getFilesURI() { return selfURI + "/files"; };
     private String getIconURI() { return selfURI + "/icon"; };
     private String getSharingsURI() { return selfURI + "/sharings"; };
     private String getTrendsURI() { return selfURI + "/trends"; };
     private String getReportsURI() { return selfURI + "/reports/%s"; };
     private String getRunsURI() { return selfURI + "/runs"; };
     private String getPublicDeviceGroupsURI() { return selfURI + "/public-device-groups"; }
     private String getUploadApplicationURI() { return selfURI + "/files/application"; }
     private String getUploadTestURI() { return selfURI + "/files/test"; }
     private String getUploadDataURI() { return selfURI + "/files/data"; }
     private String getNotificationsURI() { return selfURI + "/notifications"; }
 
     private String getCreateRunParameters(String testRunName) {
         return String.format("name=%s", encodeURL(testRunName));
     }
     
     @JsonIgnore
     public APITestRunConfig getTestRunConfig() throws APIException {
         if(testRunConfig == null) {
             testRunConfig = getResource(getConfigURI(), APITestRunConfig.class).getEntity();
         }
         return testRunConfig;
     }
     
     @JsonIgnore
     public APIProjectJobConfig getJobConfig(APIProjectJobConfig.Type type) throws APIException {
         if(jobConfig == null ) {
             jobConfig = new HashMap<APIProjectJobConfig.Type, APIProjectJobConfig>();
         }
         if(jobConfig.get(type) == null) {
             jobConfig.put(type, getResource(getJobConfigURI(type), APIProjectJobConfig.class).getEntity());
         }
         return jobConfig.get(type);
     }
     
     /**
      * Returns APIFiles entity about files uploaded to this project.
      * Depending on <code>type</code> it may be any subclass of <code>APIFiles</code> returned.
      */
     @JsonIgnore
     public <T extends APIFiles> T getFiles(Class<T> clazz) throws APIException {
         if(clazz == null || !clazz.isAssignableFrom(type.getFilesClass())) {
             throw new APIException("This project type does not have requested file types");
         }
         if(files == null) {
             files = getResource(getFilesURI(), clazz).getEntity();
         }
         return (T) files;
     }
     
     @JsonIgnore
     public byte[] getIcon() throws APIException, IOException {
         if(icon == null) {
             icon = IOUtils.toByteArray(getResource(getIconURI(), null).getStream());
         }
         return icon;
     }
     
     @JsonIgnore
     public APITestRun run() throws APIException {
         return postResource(getRunsURI(), null, APITestRun.class);
     }
     
     @JsonIgnore
     public APITestRun run(String testRunName) throws APIException {
         return postResource(getRunsURI(), getCreateRunParameters(testRunName), APITestRun.class);
     }
     
     public void update() throws APIException {
        String body = String.format("name=%s&description=%s", encodeURL(name), encodeURL(description));
         APIProject project = postResource(selfURI, body, APIProject.class);
         clone(project);
     }
     
     public void delete() throws APIException {
         deleteResource(selfURI);
     }
 
     @JsonIgnore
     public APIListResource<APITestRun> getTestRunsResource() throws APIException {
         return getListResource(getRunsURI(), APITestRun.class);
     }
     
     @JsonIgnore
     public APIListResource<APITestRun> getTestRunsResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getRunsURI(), offset, limit, search, sort, APITestRun.class);
     }
     
     @JsonIgnore
     public APIListResource<APIProjectSharing> getProjectsResource() throws APIException {
         return getListResource(getSharingsURI(), APIProjectSharing.class);
     }
     
     @JsonIgnore
     public APIListResource<APIProjectSharing> getProjectsResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getSharingsURI(), offset, limit, search, sort, APIProjectSharing.class);
     }
     
     @JsonIgnore
     public APIListResource<APIDeviceGroup> getPublicDeviceGroups() throws APIException {
         return getListResource(getPublicDeviceGroupsURI(), APIDeviceGroup.class);
     }
     
     @JsonIgnore
     public APIFiles.APIFile uploadApplication(File file, String contentType) throws APIException {
         return postFile(getUploadApplicationURI(), file, contentType, APIFile.class);
     }
 
     @JsonIgnore
     public APIFiles.APIFile uploadTest(File file, String contentType) throws APIException {
         return postFile(getUploadTestURI(), file, contentType, APIFile.class);
     }
     
     @JsonIgnore
     public APIFiles.APIFile uploadData(File file, String contentType) throws APIException {
         return postFile(getUploadDataURI(), file, contentType, APIFile.class);
     }
     
     @Override
     @JsonIgnore
     protected <T extends APIEntity> void clone(T from) {
         APIProject apiProject = (APIProject) from;
         cloneBase(from);
         this.common = apiProject.common;
         this.description = apiProject.description;
         this.files = apiProject.files;
         this.icon = apiProject.icon;
         this.jobConfig = apiProject.jobConfig;
         this.name = apiProject.name;
         this.sharedById = apiProject.sharedById;
         this.testRunConfig = apiProject.testRunConfig;
         this.type = apiProject.type;
     }
 }
