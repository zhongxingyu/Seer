 package com.sunnyd;
 
 import com.sunnyd.annotations.*;
 import com.sunnyd.database.Manager;
 import com.sunnyd.models.Document;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.apache.commons.lang3.StringUtils;
 
 /*****
  * 
  * 
  * @author Mike All Children isa relation/children of a model primary key is the
  *         parent's primary key
  */
 
 public class Base implements IModel {
 
     @ActiveRecordField
     private Integer id;
 
     @ActiveRecordField
     private Date creationDate;
 
     @ActiveRecordField
     private Date lastModifiedDate;
 
     private Boolean updateFlag = false;
 
     public Base() {
     }
 
     public Base(HashMap<String, Object> HM) {
         id = (Integer) HM.get("id");
         creationDate = (Date) HM.get("creationDate");
         lastModifiedDate = (Date) HM.get("lastModifiedDate");
 
         // Get Caller ClassName
         Class<?> classObject = this.getClass();
 
         // Set Attribute
         Base.setAttributes(classObject, this, HM);
 
         // Set updateDateFlag to false after setter methods
         this.setUpdateFlag(false);
     }
 
     @SuppressWarnings("unchecked")
     public static <T> T find(int id) {
         // Since this is a static method, to get caller of method we must look
         // in stack
         // At this point stack should look like this:
         // [java.lang.Thread.getStackTrace(Unknown Source),
         // com.sunnyd.Base.find(Base.java:79),
         // com.sunnyd.models.Person.main(Person.java:20), .....so on]
         // TODO:Need a better solution than stack to get caller class
         StackTraceElement[] ste = Thread.currentThread().getStackTrace();
         String className = ste[2].getClassName();  
         return find(id, className);
     }
     
     @SuppressWarnings("unchecked")
     public static <T> T find(int id, String className){
         try {
             // Get class attribute from database
             String tableName = BaseHelper.getClassTableName(className);
             HashMap<String, Object> HM = Manager.find(id, tableName);

             // Get inherited values from parent table
             HashMap<String, Object> parentDatas = BaseHelper.getSuperDatas((Integer) HM.get("id"),
                     Class.forName(className));
 
             if (parentDatas != null) {
                 // Merge parent's table data's into map
                 HM.putAll(parentDatas);
             }
             return (T) Class.forName(className).getConstructor(HashMap.class).newInstance(HM);
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     
     @Deprecated
     @SuppressWarnings("unchecked")
     // test find for hasOne
     public static <T> T find_hasOne(int id) {
         // Since this is a static method, to get caller of method we must look
         // in stack
         // At this point stack should look like this:
         // [java.lang.Thread.getStackTrace(Unknown Source),
         // com.sunnyd.Base.find(Base.java:79),
         // com.sunnyd.models.Person.main(Person.java:20), .....so on]
         // TODO:Need a better solution than stack to get caller class
         StackTraceElement[] ste = Thread.currentThread().getStackTrace();
         String className = ste[2].getClassName();
         try {
             // Get class attribute from database
             String tableName = BaseHelper.getClassTableName(className);
             HashMap<String, Object> HM = Manager.find(id, tableName);
 
             // Get inherited values from parent table
             HashMap<String, Object> parentDatas = BaseHelper.getSuperDatas((Integer) HM.get("id"),
                     Class.forName(className));
 
             if (parentDatas != null) {
                 // Merge parent's table data's into map
                 HM.putAll(parentDatas);
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
         if (this.getUpdateFlag()) {
             boolean allUpdated = update(this.getClass(), this);
             if (allUpdated) {
                 this.setUpdateFlag(false);
                 return allUpdated;
             }
         }
         return false;
     }
 
     private static boolean update(Class<?> classObject, Object instanceObject) {
         HashMap<String, Object> updateAttributes = BaseHelper.getTableFieldNameAndValue(classObject, instanceObject);
         if (classObject.getAnnotation(ActiveRecordInheritFrom.class) != null) {
             boolean updated = Base.update(classObject.getSuperclass(), instanceObject);
             if (!updated) {
                 System.out.println("Could not update " + ((Base) instanceObject).getId() + " " + classObject.getName());
                 return false;
             }
         }
         return Manager.update(((Base) instanceObject).getId(), BaseHelper.getClassTableName(classObject),
                 updateAttributes);
     }
 
     public Boolean Destroy() {
         boolean success = destroyHierarchy(this.getClass(), this.getId());
         if (success) {
             id = null;
         }
         return success;
     }
 
     // Delete Parent Data after child has been deleted
     private static boolean destroyHierarchy(Class<?> classObject, Integer id) {
         String tableName = BaseHelper.getClassTableName(classObject);
         boolean success = Manager.destroy(id, tableName);
         if (classObject.getAnnotation(ActiveRecordInheritFrom.class) != null) {
             success = Base.destroyHierarchy(classObject.getSuperclass(), id);
         }
         return success;
     }
 
     public boolean save() {
         int newId = 0;
         if (this.getId() == null) {
             newId = save(this.getClass(), this);
             if (newId != 0) {
                 id = newId;
                 return true;
             }
         }
         return false;
     }
 
     private static Integer save(Class<?> classObject, Object objectInstance) {
         HashMap<String, Object> attrToPersist = BaseHelper.getTableFieldNameAndValue(classObject, objectInstance);
         // System.out.println(classObject.getName());
         int id = 0;
         if (classObject.getAnnotation(ActiveRecordInheritFrom.class) != null) {
             id = Base.save(classObject.getSuperclass(), objectInstance);
             // System.out.println(id);
         }
         if (id != 0) {
             attrToPersist.put("id", id);
             // System.out.println(Arrays.asList(attrToPersist).toString());
         }
         return Manager.save(BaseHelper.getClassTableName(classObject), attrToPersist);
     }
     
     /*********************************Relations************************************************/
     public void initRelation(String attributeName) {
         try {
             Field relation = this.getClass().getDeclaredField(attributeName);
             Annotation[] relationAnnotations = relation.getAnnotations();
             
             //LazyLoading
             relation.setAccessible(true);
             if(relationLoaded(relation)){
                 return;
             }
             
             switch (relationAnnotations[0].annotationType().getSimpleName()) {
             case "ActiveRelationHasMany":
                 relationHasMany(relation, null);
                 break;
             case "ActiveRelationHasOne":
                 relationHasOne(relation);
                 break;
                 
             case "ActiveRelationManyToMany":
                 try {
                     relationHasMany(relation, relationAnnotations[0].getClass().getMethod("relationTable").invoke(relationAnnotations[0]).toString());
                 } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                         | NoSuchMethodException e) {
                     e.printStackTrace();
                 }
                 break;
             default:
                 break;
             }
 
         } catch (NoSuchFieldException | SecurityException | NegativeArraySizeException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     private boolean relationLoaded(Field relation){
         try {
             if(relation.get(this) != null){
                 System.out.println("alreadyInitlized");
                 return true;
             }
         } catch (IllegalArgumentException | IllegalAccessException e2) {
             // TODO Auto-generated catch block
             e2.printStackTrace();
         }  
         return false;
     }
     private void relationHasOne(Field relation){       
         String relationCanonicalClassName = relation.getType().getCanonicalName();
         Object relationObject = null;
         try {
             Method findMethod = Class.forName(relationCanonicalClassName).getMethod("find", int.class, String.class);
            relationObject = findMethod.invoke(null, this.getId(), relationCanonicalClassName);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
       
         relation.setAccessible(true);
         try {
             relation.set(this, relationObject);
         } catch (IllegalArgumentException | IllegalAccessException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
 
     }
     
     private void relationHasMany(Field relation, String relationTableName){
         String relationCanonicalClassName = relation.getType().getComponentType().getCanonicalName();
         String relationSimpleClassName = relation.getType().getComponentType().getSimpleName();
         String simpleClassName = this.getClass().getSimpleName();
         
         relationTableName = relationTableName == null ? BaseHelper.getClassTableName(relationSimpleClassName.toLowerCase()) : relationTableName;
        
         Class<?> relationClass = null;
         try {
             relationClass = Class.forName(relationCanonicalClassName);
         } catch (ClassNotFoundException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         
         //Condition use current object ID for has many query
         HashMap<String, Object> condition = new HashMap<String, Object>();
         condition.put(simpleClassName.toLowerCase() + "Id", this.getId());
 
         //Get results
         ArrayList<HashMap<String, Object>> results = Manager.findAll(
                 relationTableName, condition);
 
         //Following: create instance of relation class and add to array
         int size = results.size();
         Object[] collection = (Object[]) Array.newInstance(relationClass, size);
         for (int i = 0; i < size; i++) {
             try {
                 collection[i] = relationClass.getConstructor(HashMap.class).newInstance(results.get(i));
             } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                     | InvocationTargetException | NoSuchMethodException e) {
                 e.printStackTrace();
             }
         }
         
         relation.setAccessible(true);
         try {
             relation.set(this, collection);
         } catch (IllegalArgumentException | IllegalAccessException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
 
     /****** MUTATOR ****************************************************/
     // Common Mutator in all child class.
 
     public Integer getId() {
         return this.id;
     }
 
     private void setId(Integer id) {
         this.id = id;
     }
 
     public Boolean getUpdateFlag() {
         return this.updateFlag;
     }
 
     public void setUpdateFlag(Boolean flag) {
         this.updateFlag = flag;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getLastModifiedDate() {
         return lastModifiedDate;
     }
 
     public void setLastModifiedDate(Date lastModifiedDate) {
         this.lastModifiedDate = lastModifiedDate;
     }
 
     /******** Private ***********************************************/
     private String getClassName() {
         return this.getClass().getName();
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
 
     public static void setAttributes(Class<?> classObject, Object instanceObject, HashMap<String, Object> data) {
         // Get all table attribute from this class
         Field[] fields = BaseHelper.getTableField(classObject);
         for (Field field : fields) {
             String fieldName = field.getName();
             Class<?> fieldType = field.getType();
 
             if (data.containsKey(fieldName)) {
                 Object value = data.get(fieldName);
                 String capitalizeField = StringUtils.capitalize(fieldName);
                 // System.out.println("+pppppppppppppppppppppppppppppppppppppp"+capitalizeField);
                 java.lang.reflect.Method method;
                 try {
                     method = classObject.getDeclaredMethod("set" + capitalizeField, fieldType);
                     method.invoke(instanceObject, fieldType.cast(value));
                 } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException e) {
                     e.printStackTrace();
                     break;// If method does not have setMethod then it is
                           // not a db Attribute
                 }
 
             }
         }
 
         // Verify if model inherit another model
         if (classObject.getAnnotation(ActiveRecordInheritFrom.class) != null) {
             setAttributes(classObject.getSuperclass(), instanceObject, data);
         }
 
     }
 
 }
