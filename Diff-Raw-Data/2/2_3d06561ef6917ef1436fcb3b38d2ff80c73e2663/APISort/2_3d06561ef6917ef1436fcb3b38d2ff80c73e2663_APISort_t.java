 package com.testdroid.api;
 
 import com.testdroid.api.model.*;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.lang.StringUtils;
 
 /**
  *
  * @author kajdus
  */
 public class APISort {
     /**
      * Sort type used for fetching collections from API.
      */
     public static enum Type {
         ASC, DESC;
         public String getURLValue() {
             switch(this) {
                 case DESC: return "d";
                 case ASC: default: return "a";
             }
         }
         
         public static Type fromURLValue(String urlValue) {
             for(Type t: Type.values()) {
                 if(t.getURLValue().equals(urlValue)) {
                     return t;
                 }
             }
             return null;
         }
     }
     
     public static enum Column {
         DEVICE_CREDITS_PRICE(APIDevice.class, "creditsPrice"),
         DEVICE_NAME(APIDevice.class, "name"),
         DEVICE_OS_TYPE(APIDevice.class, "osType"),
         DEVICE_GROUP_ID(APIDeviceGroup.class, "id"),
         DEVICE_GROUP_NAME(APIDeviceGroup.class, "displayName"),
         DEVICE_GROUP_OS_TYPE(APIDeviceGroup.class, "osType"),
         DEVICE_PROPERTY_NAME(APIDeviceProperty.class, "displayName"),
         DEVICE_PROPERTY_GROUP_NAME(APIDeviceProperty.class, "propertyGroupName"),
         DEVICE_RUN_CREATED(APIDeviceRunState.class, "createTime"),
         DEVICE_RUN_DEVICE(APIDeviceRun.class, "device.userName"),
         DEVICE_RUN_END_TIME(APIDeviceRun.class, "endTime"),
         DEVICE_RUN_ID(APIDeviceRun.class, "id"),
         DEVICE_RUN_RUN_STATUS(APIDeviceRun.class, "runStatus"),
         DEVICE_RUN_STATE_FINISHED(APIDeviceRunState.class, "finishTimeMS"),
         DEVICE_RUN_STATE_ID(APIDeviceRunState.class, "id"),
         DEVICE_RUN_STATE_STARTED(APIDeviceRunState.class, "startTimeMS"),
         DEVICE_RUN_STATE_STATUS(APIDeviceRunState.class, "status"),
         NOTIFICATION_EMAIL_EMAIL(APINotificationEmail.class, "email"),
         NOTIFICATION_EMAIL_ID(APINotificationEmail.class, "id"),
        NOTIFICATION_EMAIL_PROJECT_NAME(APINotificationEmail.class, "p.name", NameType.ABSOLUTE),
         PROJECT_DESCRIPTION(APIProject.class, "description"),
         PROJECT_ID(APIProject.class, "id"),
         PROJECT_NAME(APIProject.class, "name"),
         PROJECT_TYPE(APIProject.class, "type"),
         PROJECT_JOB_CONFIG_ID(APIProjectJobConfig.class, "id"),
         PROJECT_JOB_CONFIG_TYPE(APIProjectJobConfig.class, "type"),
         PROJECT_JOB_CONFIG_VERSION(APIProjectJobConfig.class, "version"),
         PROJECT_SHARING_ID(APIProjectSharing.class, "id"),
         PROJECT_SHARING_USER_EMIAL(APIProjectSharing.class, "userEmail"),
         RECORDER_ONLINE_SESSION_ID(APIRecorderOnlineSession.class, "id"),
         RECORDER_ONLINE_SESSION_END_TIME(APIRecorderOnlineSession.class, "endTime"),
         RECORDER_ONLINE_SESSION_START_TIME(APIRecorderOnlineSession.class, "startTime"),
         RECORDER_ONLINE_SESSION_UPDATE_TIME(APIRecorderOnlineSession.class, "updateTime"),
         SCREENSHOT_ID(APIScreenshot.class, "id"),
         SCREENSHOT_ORIGINAL_NAME(APIScreenshot.class, "originalName"),
         SCREENSHOT_TYPE(APIScreenshot.class, "type"),
         SOFTWARE_API_LEVEL(APISoftwareVersion.class, "apiLevel"),
         SOFTWARE_ID(APISoftwareVersion.class, "id"),
         SOFTWARE_RELEASE_VERSION(APISoftwareVersion.class, "releaseVersion"),
         TAG_ID(APITag.class, "id"),
         TAG_NAME(APITag.class, "name"),
         TEST_CASE_RUN_CREATE_TIME(APITestCaseRun.class, "createTime"),
         TEST_CASE_RUN_ID(APITestCaseRun.class, "id"),
         TEST_CASE_RUN_RESULT(APITestCaseRun.class, "result"),
         TEST_RUN_CREATED(APITestRun.class, "createTime"),
         TEST_RUN_ID(APITestRun.class, "id"),
         TEST_RUN_NAME(APITestRun.class, "userName"),
         TEST_RUN_TAG(APITestRun.class, "t.name", NameType.ABSOLUTE),
         TEST_RUN_PARAMETER_ID(APITestRunParameter.class, "id"),
         TEST_RUN_PARAMETER_KEY(APITestRunParameter.class, "key"),
         USER_COUNTRY(APIUser.class, "localeCountry"),
         USER_EMAIL(APIUser.class, "email"),
         USER_ID(APIUser.class, "id"),
         USER_NAME(APIUser.class, "name");
         
         public static enum NameType { RELATIVE, ABSOLUTE }
         
         private Class<? extends APIEntity> supportedClass;
         private String name;
         private NameType type = NameType.RELATIVE;
         private Column(Class<? extends APIEntity> supportedClass, String name) { this.supportedClass = supportedClass; this.name = name; }
         private Column(Class<? extends APIEntity> supportedClass, String name, NameType type) { this(supportedClass, name); this.type = type; }
         
         public String getName() {
             return name;
         }
         
         public NameType getNameType() {
             return type;
         }
         
         public static Column fromColumnName(Class<? extends APIEntity> type, String name) {
             if(name == null) {
                 return null;
             }
             for(Column sc: get(type)) {
                 if(name.equals(sc.getName())) {
                     return sc;
                 }
             }
             return null;
         }
         
         public static List<Column> get(Class<? extends APIEntity> type) {
             List<Column> result = new ArrayList<Column>();
             for(Column sc: values()) {
                 if(sc.supportedClass.isAssignableFrom(type)) {
                     result.add(sc);
                 }
             }
             return result;
         }
         
     }
     
     public static class SortItem {    
         private Column column;
         private Type type;
 
         public SortItem(Column column, Type sortType) {
             this.column = column;
             this.type = sortType;
         }
 
         public Column getColumn() {
             return column;
         }
 
         public Type getType() {
             return type;
         }
 
     }
     
     private SortItem[] items;
     private APISort(Class<? extends APIEntity> type, SortItem... items) {
         if(items != null && items.length > 0) {
             List<SortItem> itemList = new ArrayList<SortItem>();
             for(SortItem si: items) {
                 if(si.column.supportedClass.equals(type)) {
                     itemList.add(si);
                 }
             }
             this.items = itemList.toArray(new SortItem[itemList.size()]);
         }
         else {
             this.items = new SortItem[0];
         }
     }
     
     public SortItem[] getItems() {
         return items;
     }
     
     public void setItems(SortItem... items) {
         this.items = items;
     }
     
     public boolean isEmpty() {
         return items == null || items.length == 0;
     }
     
     public static APISort create(Class<? extends APIEntity> type, SortItem... items) {
         return new APISort(type, items);
     }
     
     public String serialize() {
         List<String> resultItems = new ArrayList<String>();
         for(SortItem item: items) {
             resultItems.add(String.format("%s_%s", item.column.getName(), item.type.getURLValue()));
         }
         return StringUtils.join(resultItems, ":");
     }
     
     public static APISort deserialize(Class<? extends APIEntity> type, String value) {
         if(StringUtils.isBlank(value)) {
             return new APISort(type);
         }
         List<SortItem> items = new ArrayList<SortItem>();
         String[] resultItems = value.split(":");
         for(String stringItem: resultItems) {
             String[] sortItemValues = stringItem.split("_");
             if(sortItemValues != null && sortItemValues.length > 1) {
                 Column sc = Column.fromColumnName(type, sortItemValues[0]);
                 Type st = Type.fromURLValue(sortItemValues[1]);
                 if(sc != null && st != null) {
                     items.add(new SortItem(sc, st));
                 }
             }
         }
         return new APISort(type, items.toArray(new SortItem[items.size()]));
     }
 
 }
