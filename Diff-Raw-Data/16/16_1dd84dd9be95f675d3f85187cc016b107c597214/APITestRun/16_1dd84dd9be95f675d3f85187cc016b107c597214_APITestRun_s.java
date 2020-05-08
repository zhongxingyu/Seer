 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIList;
 import com.testdroid.api.APIListResource;
 import com.testdroid.api.APISort;
 import java.util.Date;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement
 public class APITestRun extends APIEntity {
     
     @XmlType(namespace = "APITestRun", name = "APITestRunState")
     public static enum State { WAITING, RUNNING, FINISHED }
     
     private Integer number;
     private Date createTime;
     private String displayName;
     private Float executionRatio;
     private Float successRatio;
     private String startedByDisplayName;
     private State state;
 
     public APITestRun() {}
     public APITestRun(Long id, Integer number, Date createTime, String displayName, Float executionRatio, Float successRatio, String startedByDisplayName, State state) {
         super(id);
         this.number = number;
         this.createTime = createTime;
         this.displayName = displayName;
         this.executionRatio = executionRatio;
         this.successRatio = successRatio;
         this.startedByDisplayName = startedByDisplayName;
         this.state = state;
     }    
 
     public Integer getNumber() {
         return number;
     }
 
     public void setNumber(Integer number) {
         this.number = number;
     }
 
     public Date getCreateTime() {
         return createTime;
     }
 
     public void setCreateTime(Date createTime) {
         this.createTime = createTime;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public void setDisplayName(String displayName) {
         this.displayName = displayName;
     }
 
     public Float getExecutionRatio() {
         return executionRatio;
     }
 
     public void setExecutionRatio(Float executionRatio) {
         this.executionRatio = executionRatio;
     }
 
     public Float getSuccessRatio() {
         return successRatio;
     }
 
     public void setSuccessRatio(Float successRatio) {
         this.successRatio = successRatio;
     }
 
     public String getStartedByDisplayName() {
         return startedByDisplayName;
     }
 
     public void setStartedByDisplayName(String startedByDisplayName) {
         this.startedByDisplayName = startedByDisplayName;
     }
 
     public State getState() {
         return state;
     }
 
     public void setState(State state) {
         this.state = state;
     }
  
     private APIFiles files;
     private APITestRunConfig config;
     private String getFilesURI() { return selfURI + "/files"; };
     private String getConfigURI() { return selfURI + "/config"; };
     private String getTagsURI() { return selfURI + "/tags"; };
     private String getDeviceRunsURI() { return selfURI + "/device-runs"; };
     
     @JsonIgnore
     /**
      * Returns APIFiles entity about files uploaded to this project.
      * Depending on <code>type</code> it may be any subclass of <code>APIFiles</code> returned.
      */
     public <T extends APIFiles> T getFiles(Class<T> clazz) throws APIException {
         if(files == null) {
             files = getResource(getFilesURI(), clazz).getEntity();
         }
         return (T) files;
     }
     
     public void delete() throws APIException {
         deleteResource(selfURI);
     }
     
     @JsonIgnore
     public APITestRunConfig getConfig() throws APIException {
         if(config == null) {
             config = getResource(getConfigURI(), APITestRunConfig.class).getEntity();
         }
         return config;
     }
     
     public APITag addTag(String name) throws APIException {
         return postResource(getTagsURI(), String.format("name=%s", encodeURL(name)), APITag.class);
     }
     
     public APIListResource<APITag> getTagsResource() throws APIException {
         return getListResource(getTagsURI(), APITag.class);
     }
     
     @JsonIgnore
     public APIListResource<APITag> getTagsResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getTagsURI(), offset, limit, search, sort, APITag.class);
     }
 
     public APIListResource<APIDeviceRun> getDeviceRunsResource() throws APIException {
         return getListResource(getDeviceRunsURI(), APIDeviceRun.class);
     }
     
     @JsonIgnore
     public APIListResource<APIDeviceRun> getDeviceRunsResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getDeviceRunsURI(), offset, limit, search, sort, APIDeviceRun.class);
     }
     
 }
