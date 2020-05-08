 package com.myproject.bookexchange.dao;
 
 import java.util.List;
 
 import org.springframework.data.mongodb.core.MongoOperations;
 
 public interface IGenericDAO<T> {
   
   public void deleteEntity(T entity);
   public long countEntities();
   public T addOrUpdateEntity(T entity);
   public List<T> getAllEntities();
   public void setMongo(MongoOperations mongo);
 }
