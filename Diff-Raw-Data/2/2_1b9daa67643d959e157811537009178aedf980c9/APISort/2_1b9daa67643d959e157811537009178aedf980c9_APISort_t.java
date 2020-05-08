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
         USER_ID(APIUser.class, "id"), USER_EMAIL(APIUser.class, "email"), USER_NAME(APIUser.class, "name"), USER_COUNTRY(APIUser.class, "localeCountry"),
         PROJECT_ID(APIProject.class, "id"), PROJECT_NAME(APIProject.class, "name"), PROJECT_DESCRIPTION(APIProject.class, "description"),
         TEST_RUN_ID(APITestRun.class, "id"), TEST_RUN_CREATED(APITestRun.class, "createTime"), TEST_RUN_NAME(APITestRun.class, "userName"), TEST_RUN_TAG(APITestRun.class, "t.name", NameType.ABSOLUTE),
         TAG_ID(APITag.class, "id"), TAG_NAME(APITag.class, "name"), DEVICE_NAME(APIDevice.class, "userName"),
         DEVICE_RUN_ID(APIDeviceRun.class, "id"), DEVICE_RUN_DEVICE(APIDeviceRun.class, "device.userName"), DEVICE_RUN_CREATED(APIDeviceRunState.class, "createTime"), 
         DEVICE_RUN_END_TIME(APIDeviceRun.class, "endTime"), DEVICE_RUN_STATE_STARTED(APIDeviceRunState.class, "startTimeMS");
         
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
