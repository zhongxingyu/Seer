 package org.springframework.mongo.core.support;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import org.bson.types.ObjectId;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mongo.core.CursorMapper;
 import org.springframework.mongo.core.MappableMongoOperations;
 import org.springframework.mongo.core.MongoOperations;
 import org.springframework.mongo.object.MappableDataObject;
 import org.springframework.util.Assert;
 
 import java.lang.reflect.Field;
 import java.util.*;
 
 import static org.springframework.mongo.support.MongoUtil.ID;
 import static org.springframework.mongo.support.MongoUtil.expectOneUpdate;
import static org.springframework.mongo.support.MongoUtil.withId;
 
 /**
  * TODO: comment
  *
  * @author Alexander Shabanov
  */
 public class MappableMongoTemplate implements MappableMongoOperations {
     @Autowired
     private MongoOperations mo;
 
     private Map<Class<?>, CachedMappableLayout> classToLayoutCache = new HashMap<Class<?>, CachedMappableLayout>();
 
     @Override
     public MongoOperations getMongoOperations() {
         return mo;
     }
 
     @Override
     public String insert(MappableDataObject object) {
         Assert.notNull(object, "Object can not be null");
         return mo.insert(getLayout(object.getClass()).collectionName, toDBObject(object));
     }
 
     @Override
     public void update(MappableDataObject object) {
         final DBObject query = new BasicDBObject(ID, getNonNullIdValue(object));
         expectOneUpdate(mo.update(getLayout(object.getClass()).collectionName, query, toDBObject(object)));
     }
 
     @Override
     public <T extends MappableDataObject> T getById(String id, final Class<T> resultClass) {
         final CachedMappableLayout layout = getLayout(resultClass);
         return mo.queryForObject(layout.collectionName, new CursorMapper<T>() {
             @Override
             public T mapCursor(DBObject cursor, int rowNum) {
                 try {
                     final Object instance = resultClass.newInstance();
                     for (final Field field : layout.fields) {
                         final String fieldName = field.getName();
                         field.set(instance, coerceFromDBValue(fieldName, cursor.get(fieldName)));
                     }
                     return resultClass.cast(instance);
                 } catch (InstantiationException e) {
                     throw new IllegalStateException(e);
                 } catch (IllegalAccessException e) {
                     throw new IllegalStateException(e);
                 }
             }
        }, withId(id));
     }
 
     //
     // Private
     //
 
     private Object getNonNullIdValue(MappableDataObject dataObject) {
         final CachedMappableLayout layout = getLayout(dataObject.getClass());
         try {
             final Object idVal = coerceToDBValue(ID_FIELD, layout.idField.get(dataObject));
             if (idVal == null) {
                 throw new IllegalStateException("No ID value for object=" + dataObject);
             }
             return idVal;
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         }
     }
 
     private BasicDBObject toDBObject(MappableDataObject dataObject) {
         try {
             final BasicDBObject result = new BasicDBObject();
             for (final Field field : getLayout(dataObject.getClass()).fields) {
                 final String fieldName = field.getName();
                 if (ID_FIELD.equals(fieldName)) {
                     continue; // omit ID field
                 }
                 result.put(fieldName, coerceToDBValue(fieldName, field.get(dataObject)));
             }
             return result;
         } catch (IllegalAccessException e) {
             throw new IllegalStateException("Can't get field value", e);
         }
     }
 
     private Object coerceFromDBValue(String fieldName, Object dbValue) {
         return dbValue; // TODO: another IDs
     }
 
     private Object coerceToDBValue(String fieldName, Object realValue) {
         if (ID_FIELD.equals(fieldName)) {
             if (realValue == null) {
                 return null;
             }
 
             if (realValue instanceof String) {
                 return new ObjectId(realValue.toString(), false);
             } else {
                 throw new IllegalStateException("ID field is not of String type");
             }
         }
         return realValue;
     }
 
     private static final class CachedMappableLayout {
         public final List<Field> fields;
         public final Field idField;
         public final String collectionName;
 
         public CachedMappableLayout(Class<?> objectClass) {
             this.fields = Collections.unmodifiableList(Arrays.asList(objectClass.getFields()));
             this.collectionName = objectClass.getSimpleName();
 
             Field idField = null;
             for (final Field field : fields) {
                 if (ID_FIELD.equals(field.getName())) {
                     Assert.state(idField == null, "Multiple ID fields");
                     idField = field;
                 }
             }
             this.idField = idField;
         }
     }
 
     private CachedMappableLayout getLayout(Class<?> objectClass) {
         synchronized (this) {
             CachedMappableLayout layout = classToLayoutCache.get(objectClass);
             if (layout == null) {
                 layout = new CachedMappableLayout(objectClass);
                 classToLayoutCache.put(objectClass, layout);
             }
             return layout;
         }
     }
 }
