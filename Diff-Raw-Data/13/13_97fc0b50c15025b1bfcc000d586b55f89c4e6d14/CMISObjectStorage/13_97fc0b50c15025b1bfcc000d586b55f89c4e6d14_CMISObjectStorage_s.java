 package pt.inevo.encontra.storage;
 
 import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
 import org.apache.commons.io.IOUtils;
 import org.apache.chemistry.opencmis.client.api.*;
 import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
 import org.apache.chemistry.opencmis.commons.PropertyIds;
 import org.apache.chemistry.opencmis.commons.SessionParameter;
 import org.apache.chemistry.opencmis.commons.data.ContentStream;
 import org.apache.chemistry.opencmis.commons.enums.BindingType;
 import org.apache.chemistry.opencmis.commons.enums.VersioningState;
 import pt.inevo.encontra.query.criteria.StorageCriteria;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.*;
 
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.*;
 
 /**
  * Abstract implementation of generic DAO.
  *
  * @param <T> entity type, it must implements at least <code>IEntity</code>
  * @see IEntity
  */
 public class CMISObjectStorage<T extends CmisObject> implements ObjectStorage<String, T> {
 
     private CriteriaBuilder builder;
     private Class<T> clazz;
     private static Session session;
     private Folder cmisObjectStorage;
 
     /**
      * Default constructor. Use for extend this class.
      */
     @SuppressWarnings(value = "unchecked")
     public CMISObjectStorage(Map<String, String> parameters) {
 
         Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
 
         if (types[0] instanceof ParameterizedType) {
             // If the class has parameterized types, it takes the raw type.
             ParameterizedType type = (ParameterizedType) types[0];
             clazz = (Class<T>) type.getRawType();
         } else {
             clazz = (Class<T>) types[0];
         }
         init(parameters);
     }
 
     /**
      * Constructor with given {@link IEntity} implementation. Use for creating DAO without extending
      * this class.
      *
      * @param clazz class with will be accessed by DAO methods
      */
     @SuppressWarnings(value = "unchecked")
     public CMISObjectStorage(Class<T> clazz, Map<String, String> parameters) {
         this.clazz = clazz;
         init(parameters);
     }
 
     /**
      * Creates a folder to hold the CmisObjects that will be created in the CMIS.
      */
     private void init(Map<String, String> parameters) {
         // default factory implementation
         SessionFactory factory = SessionFactoryImpl.newInstance();
                // create session
         session = factory.createSession(parameters);
         cmisObjectStorage = session.getRootFolder();
 
         String queryString = "SELECT  * FROM cmis:folder WHERE " + PropertyIds.NAME + "='CMISObjectStorage'";
         ItemIterable<QueryResult> result = session.query(queryString, false);
         long numberResults = result.getTotalNumItems();
         if (numberResults == -1) {
             // (minimal set: name and object type id)
             Map<String, Object> properties = new HashMap<String, Object>();
             properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
             properties.put(PropertyIds.NAME, "CMISObjectStorage");
 
             // create the folder
             cmisObjectStorage = cmisObjectStorage.createFolder(properties);
         } else {
             String storageType = "cmis:folder";
             // get the query name of cmis:objectId
             ObjectType type = session.getTypeDefinition(storageType);
             PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
             String objectIdQueryName = objectIdPropDef.getQueryName();
 
             Iterator<QueryResult> it = result.iterator();
             QueryResult qr = it.next();
             String objectId = qr.getPropertyValueByQueryName(objectIdQueryName);
             ObjectId id = session.createObjectId(objectId);
             cmisObjectStorage = (Folder) session.getObject(id);
         }
     }
 
     @SuppressWarnings(value = "unchecked")
     @Override
     public T get(String id) {
         ObjectId objId = session.createObjectId(id);
         Document document = (Document)session.getObject(objId);
         return getObject(id, document);
     }
 
     /**
      * Given a document it retrieved the respective CmisObject / IEntity
      * @param id
      * @param document
      * @return
      */
     private T getObject(String id, Document document) {
 
         InputStream contentStream = document.getContentStream().getStream();
         try {
             byte [] bytes = IOUtils.toByteArray(contentStream);
             T object = clazz.newInstance();
             object.setContent(bytes);
             object.setId(id);
 
             return object;
         } catch (IOException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (InstantiationException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     @Override
     public boolean validate(String id, StorageCriteria criteria){
         ObjectId objId = session.createObjectId(id);
         String queryString = "SELECT * FROM cmis:document WHERE cmis:objectId='" + objId.getId() + "' AND " + criteria.getCriteria();
         ItemIterable<QueryResult> results = session.query(queryString, false);
         //if results >= 0 then the id is valid
         if (results.getTotalNumItems() > 0)
             return true;
 
         return false;
     }
 
     @Override
     public List<String> getValidIds(StorageCriteria criteria){
         List<String> ids = new ArrayList<String>();
         String storageType = "cmis:document";
 
         // get the query name of cmis:objectId
         ObjectType type = session.getTypeDefinition(storageType);
         PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
         String objectIdQueryName = objectIdPropDef.getQueryName();
 
         String queryString = "SELECT " + objectIdQueryName + " FROM " + type.getQueryName() + " WHERE " + criteria.getCriteria();
 
         // execute query
         ItemIterable<QueryResult> results = session.query(queryString, false);
 
        for (QueryResult qResult : results) {
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            ids.add(objectId);
            //Retrieve the object
            //Document doc = (Document) session.getObject(session.createObjectId(objectId));
         }
         return ids;
     }
 
     @Override
     public T save(final T object) {
         //saving the object on the Cmis
         // properties
         Map<String, Object> properties = new HashMap<String, Object>();
         properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
         properties.put(PropertyIds.NAME, "dummyName");
 
         //set the object properties
         Map<String, Object> objectProperties = object.getProperties();
         Set<Map.Entry<String, Object>> entries = objectProperties.entrySet();
         for (Map.Entry<String, Object> entry : entries) {
             properties.put(entry.getKey(), entry.getValue());
         }
 
         byte [] content = object.getContent();
         InputStream stream = new ByteArrayInputStream(content);
         ContentStream contentStream = session.getObjectFactory().createContentStream("dummyName", content.length, "application/x-binary", stream);
 
         //get the folder id
         ObjectId folderId = session.createObjectId(cmisObjectStorage.getId());
 
         ObjectId objectId = session.createDocument(properties, folderId, contentStream, VersioningState.MAJOR);
         String id = objectId.getId();
 
         //update the name of the newly saved cmis object
         org.apache.chemistry.opencmis.client.api.CmisObject cmisObject = session.getObject(objectId);
         Map<String, Object> updateproperties = new HashMap<String, Object>();
         updateproperties.put(PropertyIds.NAME, id);
         cmisObject.updateProperties(updateproperties);
 
         object.setId(id);
 
         return object;
     }
 
     @Override
     public void save(final T... objects) {
         //save all the objects, one by one
         for (T object : objects) {
             save(object);
         }
     }
 
     @Override
     public void delete(final T object)  {
         ObjectId objId = session.createObjectId(object.getId());
         org.apache.chemistry.opencmis.client.api.CmisObject cmisObject = session.getObject(objId);
         cmisObject.delete(true);
     }
 
     public void refresh(final T entity) {
         //TO DO
         return;
     }
 
     public void flushAndClear() {
         //TO DO
         return;
     }
 }
