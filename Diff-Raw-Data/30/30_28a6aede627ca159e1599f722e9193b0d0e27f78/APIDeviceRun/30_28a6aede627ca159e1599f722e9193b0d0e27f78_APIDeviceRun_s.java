 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIList;
 import com.testdroid.api.APIListResource;
 import com.testdroid.api.APISort;
 import java.io.InputStream;
 import java.util.Date;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement
 public class APIDeviceRun extends APIEntity {
     private Date runTime;
     private APIDevice device;
     private Integer testCaseSuccessNo;
     private Integer testCaseAllNo;
     private Integer testCaseCount;
     private APISoftwareVersion softwareVersion;
     private Date createTime;
     private Date startTime; 
     private APIDeviceRunState currentState;
     private APIDeviceRunState interruptedByState;
 
     public APIDeviceRun() {}
     public APIDeviceRun(Long id, Date runTime, APIDevice device, Integer testCaseSuccessNo, Integer testCaseAllNo, Integer testCaseCount, APISoftwareVersion softwareVersion, 
             Date createTime, Date startTime, APIDeviceRunState currentState, APIDeviceRunState interruptedByState) {
         super(id);
         this.runTime = runTime;
         this.device = device;
         this.testCaseSuccessNo = testCaseSuccessNo;
         this.testCaseAllNo = testCaseAllNo;
         this.testCaseCount = testCaseCount;
         this.softwareVersion = softwareVersion;
         this.createTime = createTime;
         this.startTime = startTime;
         this.currentState = currentState;
         this.interruptedByState = interruptedByState;
     }
 
     public Date getRunTime() {
         return runTime;
     }
 
     public void setRunTime(Date runTime) {
         this.runTime = runTime;
     }
 
     public APIDevice getDevice() {
         return device;
     }
 
     public void setDevice(APIDevice device) {
         this.device = device;
     }
 
     public Integer getTestCaseSuccessNo() {
         return testCaseSuccessNo;
     }
 
     public void setTestCaseSuccessNo(Integer testCaseSuccessNo) {
         this.testCaseSuccessNo = testCaseSuccessNo;
     }
 
     public Integer getTestCaseAllNo() {
         return testCaseAllNo;
     }
 
     public void setTestCaseAllNo(Integer testCaseAllNo) {
         this.testCaseAllNo = testCaseAllNo;
     }
 
     public Integer getTestCaseCount() {
         return testCaseCount;
     }
 
     public void setTestCaseCount(Integer testCaseCount) {
         this.testCaseCount = testCaseCount;
     }
 
     public APISoftwareVersion getSoftwareVersion() {
         return softwareVersion;
     }
 
     public void setSoftwareVersion(APISoftwareVersion softwareVersion) {
         this.softwareVersion = softwareVersion;
     }
 
     public Date getCreateTime() {
         return createTime;
     }
 
     public void setCreateTime(Date createTime) {
         this.createTime = createTime;
     }
 
     public Date getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Date startTime) {
         this.startTime = startTime;
     }
 
     public APIDeviceRunState getCurrentState() {
         return currentState;
     }
 
     public void setCurrentState(APIDeviceRunState currentState) {
         this.currentState = currentState;
     }
 
     public APIDeviceRunState getInterruptedByState() {
         return interruptedByState;
     }
 
     public void setInterruptedByState(APIDeviceRunState interruptedByState) {
         this.interruptedByState = interruptedByState;
     }
     
     private String getLogsURI() { return selfURI + "/logs"; };
     private String getScreenshotsURI() { return selfURI + "/screenshots"; };
     private String getJunitURI() { return selfURI + "/junit.xml"; };
     private String getPerformanceURI() { return selfURI + "/performance"; };
     private String getStatesURI() { return selfURI + "/states"; };
     
     public InputStream getLogs() throws APIException {
         return client.get(getLogsURI());
     }
     
     public InputStream getJunitXml() throws APIException {
         return client.get(getJunitURI());
     }
     
     public InputStream getPerformanceData() throws APIException {
         return client.get(getPerformanceURI());
     }
     
     public APIListResource<APIDeviceRunState> getDeviceRunStatesResource() throws APIException {
         return getListResource(getStatesURI(), APIDeviceRunState.class);
     }
     
     @JsonIgnore
     public APIListResource<APIDeviceRunState> getDeviceRunStatesResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getStatesURI(), offset, limit, search, sort, APIDeviceRunState.class);
     }
     
     public APIListResource<APIScreenshot> getScreenshotsResource() throws APIException {
         return getListResource(getScreenshotsURI(), APIScreenshot.class);
     }
     
     @JsonIgnore
     public APIListResource<APIScreenshot> getScreenshotsResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getScreenshotsURI(), offset, limit, search, sort, APIScreenshot.class);
     }
     
 }
