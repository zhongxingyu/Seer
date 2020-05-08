 package com.myproject.bookexchange.dao.impl;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.bson.types.ObjectId;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.query.Query;
 import com.myproject.bookexchange.dao.IGenericDAO;
 import com.myproject.bookexchange.domain.DomainObject;
 
 public abstract class GenericMongoDAO<T extends DomainObject> implements IGenericDAO<T> {
   
   protected MongoOperations mongo;
   
   protected Class<T> persistentClass;
   
   protected Log log = LogFactory.getLog(getClass());
   
   public GenericMongoDAO(Class<T> persistentClass) {
     super();
     this.persistentClass = persistentClass;
   }
     
   public abstract void beforeEntityUpdateAdd(T entity);
   
   public abstract void beforeEntityDelete(T entity);
   
   @Override
   public T addOrUpdateEntity(T entity) {
     log.info("Adding or updating entity for class: " + persistentClass);
     beforeEntityUpdateAdd(entity);
     mongo.save(entity);
     return entity;
   }
   
   @Override
   public void deleteEntity(T entity) {
     log.info("Deleting entity for class: " + persistentClass);
     beforeEntityDelete(entity);
     mongo.remove(entity.getId());
   }
   
   @Override
   public long countEntities() {
     log.info("Getting count of entities of class: " + persistentClass);
     return mongo.count(new Query(), persistentClass);
   }
   
   @Override
   public List<T> getAllEntities() {
     log.info("Getting all entities of class: " + persistentClass);
     return mongo.findAll(persistentClass); 
   }
   
   public T getEntityById(ObjectId id) {
     log.info("Getting entity of class: " + persistentClass + " by id");
     return mongo.findById(id, persistentClass);
   }
 
   @Override
   public void setMongo(MongoOperations mongo) {
     this.mongo = mongo;
   }
 }
