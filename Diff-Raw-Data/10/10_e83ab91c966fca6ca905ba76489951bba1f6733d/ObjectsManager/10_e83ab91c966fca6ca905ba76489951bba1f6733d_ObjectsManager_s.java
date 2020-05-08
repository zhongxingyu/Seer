 package ru.terraobjects.manager;
 
 import ru.terraobjects.entity.ObjectFields;
 import ru.terraobjects.entity.TObject;
 import ru.terraobjects.entity.controller.ObjectFieldsJpaController;
 import ru.terraobjects.entity.controller.TObjectJpaController;
 import ru.terraobjects.entity.controller.exceptions.IllegalOrphanException;
 import ru.terraobjects.entity.controller.exceptions.NonexistentEntityException;
 
 import javax.persistence.*;
 import java.lang.reflect.Field;
 import java.util.*;
 
 /**
  * Date: 29.05.14
  * Time: 16:27
  */
 public class ObjectsManager<T> {
     private EntityManagerFactory emf = Persistence.createEntityManagerFactory("to2pu");
     private TObjectJpaController objectJpaController = new TObjectJpaController(emf);
     private ObjectFieldsJpaController objectFieldsJpaController = new ObjectFieldsJpaController(emf);
 
     public ObjectsManager() {
     }
 
     public void saveObject(TObject tObject) throws Exception {
         objectJpaController.create(tObject);
     }
 
     public void saveOrUpdate(T entity) throws Exception {
         String name = entity.getClass().getName();
         GenerationType generationType = GenerationType.IDENTITY;
         Object id = null;
         String idFieldName = "";
         TObject object = null;
         for (Field field : entity.getClass().getDeclaredFields()) {
             if (field.getAnnotation(Id.class) != null)
                 try {
                     if (!field.isAccessible())
                         field.setAccessible(true);
                     id = field.get(entity);
                     idFieldName = field.getName();
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 }
         }
 
         if (id != null)
             object = findByValue(id);
 
         if (object == null) {
             object = new TObject();
             object.setId(0);
             object.setParent(0);
             object.setName(name);
             object.setVersion(0);
             object.setUpdated(new Date());
            object.setObjectFieldsList(new ArrayList<>());
             objectJpaController.create(object);
             final List<ObjectFields> objectFieldsList = new ArrayList<>();
             for (Field field : entity.getClass().getDeclaredFields()) {
                 if (!field.isAccessible())
                     field.setAccessible(true);
                 ObjectFields newObjectField = new ObjectFields(0, field.getName(), field.getType().getSimpleName().toLowerCase());
                 newObjectField.setObjectId(object);
                 try {
                     String fieldType = field.getType().getSimpleName().toLowerCase();
                     switch (fieldType) {
                         case "integer": {
                             if (idFieldName.equalsIgnoreCase(field.getName())) {
                                 Integer newId = 0;
                                 if (generationType.equals(GenerationType.IDENTITY)) {
                                     newId = objectJpaController.getCountByName(name).intValue() + 1;
                                     newObjectField.setIntval(newId);
                                     field.set(entity, newId);
                                 }
                             } else
                                 newObjectField.setIntval((Integer) field.get(entity));
                         }
                         break;
                         case "long": {
                             if (idFieldName.equalsIgnoreCase(field.getName())) {
                                 Long newId = 0l;
                                 if (generationType.equals(GenerationType.IDENTITY)) {
                                     newId = objectJpaController.getCountByName(name) + 1;
                                     newObjectField.setLongval(newId);
                                     field.set(entity, newId);
                                 }
                             } else
                                 newObjectField.setLongval((Long) field.get(entity));
                         }
                         break;
                         case "double": {
                             newObjectField.setFloatval((Double) field.get(entity));
                         }
                         break;
                         case "date": {
                             newObjectField.setDateval((Date) field.get(entity));
                         }
                         break;
                         case "string": {
                             newObjectField.setStrval((String) field.get(entity));
                         }
                         break;
                     }
 
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 }
                 objectFieldsJpaController.create(newObjectField);
                 objectFieldsList.add(newObjectField);
             }
 
             object.setObjectFieldsList(objectFieldsList);
         }
         objectJpaController.edit(object);
     }
 
     private TObject findByValue(Object value) {
         ObjectFields objectField = objectFieldsJpaController.findByValueSingle(value);
         if (objectField != null)
             return objectField.getTObject();
         return null;
     }
 
     public List<T> load(Class<T> loadClass, String fieldName, Object value) {
         List<ObjectFields> fields = objectFieldsJpaController.findByObjectNameAndFieldValue(loadClass.getName(), fieldName, value);
         if (fields == null)
             return null;
         List<T> ret = new ArrayList<T>();
         for (ObjectFields of : fields) {
             ret.add(loadEntityFromObject(loadClass, of.getTObject()));
         }
         return ret;
     }
 
     public T load(Class<T> loadClass, Object value) {
         ObjectFields field = objectFieldsJpaController.findByValueSingle(value);
         if (field == null)
             return null;
         return loadEntityFromObject(loadClass, field.getTObject());
     }
 
     public List<TObject> load(String name, int page, int perpage, boolean all) {
         return objectJpaController.findByName(name);
     }
 
     public Long getCount(Object value, String field) {
         return objectFieldsJpaController.getCountByValue(value, field);
     }
 
 
     public void remove(T entity) throws NonexistentEntityException {
         Object id = null;
         String idFieldName = "";
         for (Field field : entity.getClass().getDeclaredFields()) {
             if (field.getAnnotation(Id.class) != null)
                 try {
                     if (!field.isAccessible())
                         field.setAccessible(true);
                     id = field.get(entity);
                     idFieldName = field.getName();
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 }
         }
 
         TObject object = objectJpaController.findTObject((Integer) id);
         if (object != null) {
            List<ObjectFields> fields = objectFieldsJpaController.findByTObject(object);
            if (fields != null)
                for (ObjectFields objectFields : fields)
                 objectFieldsJpaController.destroy(objectFields.getId());
             try {
                 objectJpaController.destroy(object.getId());
             } catch (IllegalOrphanException e) {
                 e.printStackTrace();
             } catch (NonexistentEntityException e) {
                 e.printStackTrace();
             }
         } else
             throw new NonexistentEntityException("Unable to find entity by id = " + id);
     }
 
     public List<T> list(Class<T> targetClass, int page, int perpage, boolean all) {
         final List<T> ret = new ArrayList<>();
         for (TObject tobject : objectJpaController.findByName(targetClass.getName())) {
             ret.add(loadEntityFromObject(targetClass, tobject));
         }
         return ret;
     }
 
     private T loadEntityFromObject(Class<T> loadClass, TObject object) {
         T ret = null;
         try {
             ret = loadClass.newInstance();
 
             for (ObjectFields of : object.getObjectFieldsList()) {
                 Field f = loadClass.getDeclaredField(of.getName());
                 if (!f.isAccessible())
                     f.setAccessible(true);
                 f.set(ret, objectFieldsJpaController.getValue(of));
             }
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (NoSuchFieldException e) {
             e.printStackTrace();
         }
 
         return ret;
     }
 
     public Integer createObject(String name) {
         TObject tObject = new TObject();
         tObject.setId(0);
         tObject.setParent(0);
         tObject.setName(name);
         tObject.setVersion(0);
         tObject.setUpdated(new Date());
        tObject.setObjectFieldsList(new ArrayList<>());
         objectJpaController.create(tObject);
         return tObject.getId();
     }
 
     public void updateObjectFields(Integer id, Map<String, String> fields) throws PersistenceException {
         TObject tObject = objectJpaController.findTObject(id);
         if (tObject == null)
             throw new PersistenceException("Unable to find object " + id);
 
         for (String name : fields.keySet()) {
             Object value = fields.get(name);
             String fieldType = value.getClass().getSimpleName().toLowerCase();
             ObjectFields newObjectField = new ObjectFields(0, name, fieldType);
             newObjectField.setObjectId(tObject);
 
             switch (fieldType) {
                 case "integer":
                     newObjectField.setIntval((Integer) value);
                     break;
                 case "long":
                     newObjectField.setLongval((Long) value);
                     break;
                 case "double":
                     newObjectField.setFloatval((Double) value);
                     break;
                 case "date":
                     newObjectField.setDateval((Date) value);
                     break;
                 case "string":
                     newObjectField.setStrval((String) value);
                     break;
             }
             objectFieldsJpaController.create(newObjectField);
         }
     }
 
     public Map<String, String> getObjectFieldValues(Integer id) throws PersistenceException {
         TObject tObject = objectJpaController.findTObject(id);
         if (tObject == null)
             throw new PersistenceException("Unable to find object " + id);
 
         Map<String, String> ret = new HashMap<>();
         for (ObjectFields objectField : tObject.getObjectFieldsList())
             ret.put(objectField.getName(), (String) objectFieldsJpaController.getValue(objectField));
 
         return ret;
     }
 
     public TObject findById(Integer id) {
         TObject ret = objectJpaController.findTObject(id);
         return ret;
     }
 }
