 package me.alanfoster.services.me.alanfoster.services.core.service;
 
 /**
  * @author Alan Foster
  * @version 1.0.0-SNAPSHOT
  */
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * @author Alan Foster
  * @version 1.0.0-SNAPSHOT
  */
 public interface IGenericService<T, K extends Serializable> {
     /* Create Operations */
    K create(T object);
 
     /* Read Operations */
    T get(K key);
    List<T> getAll();
 
     /* Update Operations */
    void update(T object);
 
     /* Delete Operations */
    void delete(T object);
    void delete(K key);
 }
