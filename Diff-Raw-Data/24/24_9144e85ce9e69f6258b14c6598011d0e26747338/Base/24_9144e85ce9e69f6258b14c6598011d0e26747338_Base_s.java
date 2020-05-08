 package com.sunnyd;
 
 import com.sunnyd.annotations.*;
 import com.sunnyd.database.Manager;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 public class Base {
 
     @tableAttr
     private Integer id;
 
     private Boolean updateFlag = false;
     
     public Base(){
         
     }
 
     public Base(HashMap<String, Object> HM) {
         this.setId((Integer) HM.get("id"));
 
         // Get Caller ClassName
         Class<?> classObject = this.getClass();
 
         // Get all object fields that is in the table
         Field[] fields = Base.getTableField(this.getClass());
         for (Field field : fields) {
             String fieldName = field.getName();
             Class<?> fieldType = field.getType();
             if (HM.containsKey(fieldName)) {
                 Object value = HM.get(fieldName);
                 String capitalizeField = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
 
                 java.lang.reflect.Method method;
                 try {
                     method = classObject.getDeclaredMethod("set" + capitalizeField, fieldType);
                     method.invoke(this, fieldType.cast(value));
                 } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException e) {
                     e.printStackTrace();
                     break;// If method does not have setMethod then it is
                           // not a db Attribute
                 }
 
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     public static <T> T find(int id) {
         // Since this is a static method, to get caller of method we must look
         // in the stack trace to get class
         StackTraceElement[] ste = Thread.currentThread().getStackTrace();
         // At this point stack should look like this
         // [java.lang.Thread.getStackTrace(Unknown Source),
         // com.sunnyd.Base.find(Base.java:79),
         // com.sunnyd.models.Person.main(Person.java:20), .....so on]
         // TODO:Need a better solution than stack to get caller class
         String className = ste[2].getClassName();
         try {
             String tableName = getClassTableName(className);
             HashMap<String, Object> HM = Manager.find(id, tableName);
             if (HM == null) {
                 return null;
             }
             return (T) Class.forName(className).getConstructor(HashMap.class).newInstance(HM);
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static <T> T findAll() {
         return null;
     }
 
     public boolean update() {
         if (getUpdateFlag()) {
             HashMap<String, Object> updateAttributes = new HashMap<String, Object>();
             Field[] classFields = this.getClass().getDeclaredFields();
             for (Field field : classFields) {
                 Annotation tableAttr = field.getAnnotation(tableAttr.class);
                 if (tableAttr != null) {
                     try {
                         String fieldName = field.getName();
                         java.lang.reflect.Method method;
                         method = this.getClass().getDeclaredMethod("get" + capitalize(fieldName));
                         updateAttributes.put(fieldName, method.invoke(this));
                     } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException
                             | SecurityException | InvocationTargetException e) {
                         e.printStackTrace();
                     }
                 }
             }
 
             this.setUpdateFlag(false);
             return Manager.update(this.getId(), getTableName(), updateAttributes);
         }
         return false;
     }
 
     public Boolean Destroy() {
         return Manager.destroy(this.getId(), getTableName());
     }
 
     public boolean save() {
        if(this.getId()!=null && this.getId() != 0){
             HashMap<String, Object> attrToPersist = Base.getTableAttributeNameAndValue(this);
             int id = Manager.save(getClassTableName(this.getClassName()), attrToPersist);
             if(id != 0){
                 this.setId(id);
                 return true;
             }
         }
         return false;
     }
 
     /****** MUTATOR ****************************************************/
     // Common Mutator in all child class.
 
     public Integer getId() {
         return this.id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Boolean getUpdateFlag() {
         return this.updateFlag;
     }
 
     public void setUpdateFlag(Boolean flag) {
         this.updateFlag = flag;
     }
 
     /******** Private ***********************************************/
     private String getClassName() {
         return this.getClass().getName();
     }
 
     private static String getClassTableName(String className) {
         String name = null;
         try {
             name = Class.forName(className).getDeclaredField("tableName").get(null).toString();
         } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException
                 | ClassNotFoundException e) {
             e.printStackTrace();
         }
         return name;
     }
 
     private String getTableName() {
         try {
             return this.getClass().getDeclaredField("tableName").get(null).toString();
         } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     private static String capitalize(String s) {
         return s.substring(0, 1).toUpperCase() + s.substring(1);
     }
     
     private static HashMap<String, Object> getTableAttributeNameAndValue(Object classObject){
         Field[] classFields = classObject.getClass().getDeclaredFields();
         HashMap<String, Object> tableAttributes = new HashMap<String,Object>();
         for(int i=0; i<classFields.length; i++){
             Field field = classFields[i];
             Annotation attrAnnotation = field.getAnnotation(tableAttr.class);
             if (attrAnnotation != null) {
                 try {
                     String fieldName = field.getName();
                     java.lang.reflect.Method method;
                     Object value;
                         method = classObject.getClass().getDeclaredMethod("get" + capitalize(fieldName));
                         value = method.invoke(classObject);
                     tableAttributes.put(field.getName(), value);
                 } catch (IllegalArgumentException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
                     e.printStackTrace();
                 }
             }
         }
         return tableAttributes;
     }
     
     private static Field[] getTableField(Class<?> classObject){
         Field[] classFields = classObject.getDeclaredFields();
         List<Field> tableAttributes = new ArrayList<Field>();
         for(int i=0; i<classFields.length; i++){
             Field field = classFields[i];
             Annotation attrAnnotation = field.getAnnotation(tableAttr.class);
             if (attrAnnotation != null) {
                 try {
                     tableAttributes.add(field);
                 } catch (IllegalArgumentException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
         return tableAttributes.toArray(new Field[tableAttributes.size()]);
     }
 }
