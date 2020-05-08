 package com.jweekend.dao.interfaces;
 
 import java.io.Serializable;
 import java.util.List;
 
 import com.jweekend.entity.DomainObject;
 
 /**
  * A generic DAO.
  * 
  * @author Richard Wilkinson - richard.wilkinson@jweekend.com
  * @author Ernesto Reinaldo Barreiro - ernesto.reinaldo@jweekend.com
  * 
  * @param <T> The type of the domain object.
  * @param <K> The type of the key.
  */
 public interface Dao<T extends DomainObject, K extends Serializable> {
 	
 	/**
 	 * Deletes an object of type T.
 	 * 
 	 * @param o
 	 */
 	public void delete(T o);
 
 	/**
 	 * Loads the object of type T whose primary key is id.
 	 * 
 	 * @param id The id.
 	 * @return An object of type T.
 	 */
 	public T load(K id);
 
 	/**
 	 * Saves an object of type T.
 	 * 
 	 * @param o
	 * @return T
 	 */
 	public T save(T o);
 
 	/**
 	 * Finds all elements of type T.
 	 * 
	 * @return {@link List}
 	 */
 	public List<T> findAll();
 
 	/**
 	 * Counts all elements of type T.
 	 * 
 	 * @return The number of elements.
 	 */
 	public int countAll();
 }
