 package org.lazydog.repository.ldap;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.lazydog.repository.Criteria;
 import org.lazydog.repository.Entity;
 import org.lazydog.repository.Repository;
 import org.lazydog.repository.ldap.internal.Configuration;
 import org.lazydog.repository.ldap.internal.ConfigurationException;
 import org.lazydog.repository.ldap.internal.CriteriaImpl;
 import org.lazydog.repository.ldap.internal.Directory;
 import org.lazydog.repository.ldap.internal.DirectoryException;
 import org.lazydog.repository.ldap.internal.EntityFactoryException;
 import org.lazydog.repository.ldap.internal.FetchType;
 import static org.lazydog.repository.ldap.internal.Validator.*;
 
 
 /**
  * Abstract repository using the Java Naming and Directory Interface.
  *
  * @author  Ron Rickard
  */
 public abstract class AbstractRepository implements Repository {
 
     private Configuration configuration;
     private Directory directory;
 
     /**
      * Constructor.
      * 
      * @throws  RepositoryException  if unable to initialize the configuration.
      */
     public AbstractRepository() {
 
         try {
 
             // Initialize the configuration and get the directory.
             this.configuration = Configuration.newInstance();
             this.directory = this.configuration.getDirectory();
         }
         catch (ConfigurationException e) {
             throw new RepositoryException("Unable to initialize the configuration.", e, null);
         }
     }
     
     /**
      * Convert the attribute name-attribute value map to a property name-property value map.
      * 
      * @param  entityClass   the entity class.
      * @param  attributeMap  the attribute name-attribute value map.
      * 
      * @return  a property name-property value map.
      * 
      * @throws  EntityFactoryException  if unable to convert the attribute name-attribute value map to a property name-property value map.
      */
     private Map<String,Object> convertAttributeMapToPropertyMap(final Class<?> entityClass, final Map<String,Set<String>> attributeMap) throws EntityFactoryException {
 
         // Initialize.
         Map<String, Object> propertyMap = new HashMap<String, Object>();
 
         // Get the attribute names.
         Set<String> attributeNames = attributeMap.keySet();
 
         // Loop through the attribute names.
         for (String attributeName : attributeNames) {
 
             // Get the attribute values, property name, and property value.
             Set<String> attributeValues = attributeMap.get(attributeName);
             String propertyName = this.configuration.getPropertyName(entityClass, attributeName);
             Object propertyValue = this.getPropertyValue(entityClass, attributeValues, propertyName);
 
             // Check if there is a property value.
             if (propertyValue != null) {
 
                 // Put the property name-property value in the map.
                 propertyMap.put(propertyName, propertyValue);
             }
         }
 
         return propertyMap;
     }
 	
     /**
      * Convert the property name-property value map to an attribute name-attribute value map.
      * 
      * @param  entityClass  the entity class.
      * @param  propertyMap  the property name-property value map.
      * 
      * @return  an attribute name-attribute value map.
      * 
      * @throws  EntityFactoryException  if unable to convert the property name-property value map to an attribute name-attribute value map.
      */
     private Map<String,Set<String>> convertPropertyMapToAttributeMap(final Class<?> entityClass, final Map<String,Object> propertyMap) throws EntityFactoryException {
 
         // Initialize.
         Map<String,Set<String>> attributeMap = new HashMap<String,Set<String>>();
 
         // Get the property names.
         Set<String> propertyNames = propertyMap.keySet();
 
         // Loop through the property names.
         for (String propertyName : propertyNames) {
 
             // Get the property value, attribute name, and attribute values.
             Object propertyValue = propertyMap.get(propertyName);
             String attributeName = this.configuration.getAttributeName(entityClass, propertyName);
             Set<String> attributeValues = this.getAttributeValues(entityClass, propertyValue, propertyName);
 
             // Check if there are attribute values.
             if (attributeValues.size() > 0) {
 
                 // Put the attribute name-attribute value in the map.
                 attributeMap.put(attributeName, attributeValues);
             }
         }
 
         return attributeMap;
     }
     
     /**
      * Create the entity.
      * 
      * @param  entityClass   the entity class.
      * @param  id            the ID.
      * @param  attributeMap  the attribute name-attribute value map.
      * 
      * @return  the entity.
      * 
      * @throws  EntityFactoryException  if unable to create the entity.
      */
     private <T,U> T createEntity(final Class<T> entityClass, final U id, final Map<String,Set<String>> attributeMap) throws EntityFactoryException {
 
         // Convert the attribute name-attribute value map to the property name-property value map.
         Map<String,Object> propertyMap = this.convertAttributeMapToPropertyMap(entityClass, attributeMap);
         propertyMap.put("id", id);
 
         // Create the entity class.
         return this.configuration.getEntityFactory(entityClass).createEntity(propertyMap);
     }
 	
     /**
      * Create the entity.
      * 
      * @param  entityClass  the entity class.
      * @param  id           the ID.
      * @param  fetchType    the fetch type.
      * 
      * @return  the entity.
      * 
      * @throws  EntityFactoryException  if unable to create the entity.
      */
     private <T,U> T createEntity(final Class<T> entityClass, final U id, final FetchType fetchType) throws EntityFactoryException {
         return (fetchType == FetchType.EAGER) ? this.find(entityClass, id) : this.createLazyEntity(entityClass, id);
     }
 	
     /**
      * Create the entity lazily.
      *
      * @param  entityClass  the entity class.
      * @param  id           the ID.
      * 
      * @return  the entity.
      * 
      * @throws  EntityFactoryException  if unable to create the entity.
      */
     private <T,U> T createLazyEntity(final Class<T> entityClass, final U id) throws EntityFactoryException {
 
         // Only add the ID property to the entity.
         Map<String,Object> propertyValues = new HashMap<String,Object>();
         propertyValues.put("id", id);
 
         return this.configuration.getEntityFactory(entityClass).createEntity(propertyValues);
     }
     
     /**
      * Find the entity.
      *
      * @param  entityClass  the entity class.
      * @param  id           the ID.
      *
      * @return  the entity.
      * 
      * @throws  IllegalArgumentException  if the entity class or ID is null or invalid.
      * @throws  RepositoryException       if unable to find the entity.
      */
     @Override
     public <T,U> T find(final Class<T> entityClass, final U id) {
 		
         validEntityClass(entityClass, this.configuration.getEntityClasses());
         validId(id);
 
         // Initialize the entity.
         T entity = null;
 
         try {
 
             // Get the attribute name-attribute value map for the entity identified by ID.
             Map<String,Set<String>> attributeMap = this.directory.getAttributeMap((String)id, this.configuration.getAttributeNames(entityClass));
 
             // Check if there are attribute name-attribute value pairs.
             if (attributeMap.size() > 0) {
 
                 // Create the entity.
                 entity = this.createEntity(entityClass, id, attributeMap);
             }
         }
         catch (DirectoryException e) {
             throw new RepositoryException(
                     "Unable to find the entity " + entityClass + " identified by id '" + id + "'.", 
                     e, entityClass);
         }
         catch (EntityFactoryException e) {
             throw new RepositoryException(
                     "Unable to find the entity " + entityClass + " identified by id '" + id + "'.", 
                     e, entityClass);
         }
 
         return entity;
     }
 
     /**
      * Find the entity.
      *
      * @param  entityClass  the entity class.
      * @param  criteria     the criteria.
      *
      * @return  the entity.
      * 
      * @throws  IllegalArgumentException  if the entity class or criteria is null or invalid.
      * @throws  RepositoryException       if unable to find the entity.
      */
     @Override
     public <T> T find(final Class<T> entityClass, final Criteria<T> criteria) {
 		
         // Find the list of entities.
         List<T> entities = this.findList(entityClass, criteria);
 
         // Check if more than one entity was found.
         if (entities.size() > 1) {
             throw new RepositoryException(
                     "More than one entity " + entityClass + " with criteria '" + criteria + "' found.", 
                     entityClass);
         }
 
         return entities.get(0);
     }
 
     /**
      * Find the list of entities.
      *
      * @param  entityClass  the entity class.
      *
      * @return  the list of entities or an empty list if no entities were found.
      * 
      * @throws  IllegalArgumentException  if the entity class is null or invalid.
      * @throws  RepositoryException       if unable to find the list of entities.
      */
     @Override
     public <T> List<T> findList(final Class<T> entityClass) {
         return this.findList(entityClass, this.getCriteria(entityClass));
     }
 
     /**
      * Find the list of entities.
      *
      * @param  entityClass  the entity class.
      * @param  criteria     the criteria.
      * 
      * @return  the list of entities or an empty list if no entities were found.
      * 
      * @throws  IllegalArgumentException  if the entity class or criteria is null or invalid.
      * @throws  RepositoryException       if unable to find the list of entities.
      */
     @Override
     public <T> List<T> findList(final Class<T> entityClass, final Criteria<T> criteria) {
 		
         validEntityClass(entityClass, this.configuration.getEntityClasses());
         validCriteria(criteria);
 
         // Initialize the list of entities.
         List<T> entities = new ArrayList<T>();
 
         try {
 
             // Get the attribute name-attribute value maps for the entity identified by ID.
             Map<String,Map<String,Set<String>>> attributeMaps = this.directory.getAttributeMaps(
                     ((CriteriaImpl<T>)criteria).getFilter(),
                     ((CriteriaImpl<T>)criteria).getSearchBase(),
                     ((CriteriaImpl<T>)criteria).getSearchScope(),
                     this.configuration.getAttributeNames(entityClass));
 
             // Check if there are attribute name-attribute value maps.
             if (attributeMaps.size() > 0) { 
 
                 // Get the IDs.
                 Set<String> ids = attributeMaps.keySet();
 
                 // Loop through the IDs.
                 for (String id : ids) {
 
                     // Get the attribute map.
                     Map<String,Set<String>> attributeMap = attributeMaps.get(id);
 
                     // Create the entity.
                     T entity = this.createEntity(entityClass, id, attributeMap);
 
                     // Add the entity to the list of entities.
                     entities.add(entity);
                 }
             }
         }
         catch (DirectoryException e) {
             throw new RepositoryException(
                     "Unable to find the entities " + entityClass + " with criteria '" + criteria + "'.", 
                     e, entityClass);
         }
         catch (EntityFactoryException e) {
             throw new RepositoryException(
                     "Unable to find the entities " + entityClass + " with criteria '" + criteria + "'.", 
                     e, entityClass);
         }
 
         return entities;
     }
 
     /**
      * Get the attribute values from the property value.
      * 
      * @param  entityClass    the entity class.
      * @param  propertyValue  the property value.
      * @param  propertyName   the property name.
      * 
      * @return  the attribute values.
      * 
      * @throws  EntityFactoryException  if unable to get the attribute values.
      */
     @SuppressWarnings("unchecked")
     private Set<String> getAttributeValues(final Class<?> entityClass, final Object propertyValue, final String propertyName) throws EntityFactoryException {
 
         // Initialize the attribute values.
         Set<String> attributeValues = new HashSet<String>();
 
         // check if there is a property value.
         if (propertyValue != null) {
 			
             // Get the property type for the property name.
             Class<?> propertyType = this.configuration.getEntityFactory(entityClass).getAccessorReturnType(propertyName);
 
             // Check if the property is an entity.
             if (Entity.class.isAssignableFrom(propertyType) && this.configuration.isEntityType(entityClass, propertyName)) {
 
                 // Set the attribute value to the entity.
                 attributeValues.add((String)((Entity)propertyValue).getId());
             }
 			
             // Check if the property is an integer.
             else if (propertyType == Integer.class && !this.configuration.isEntityType(entityClass, propertyName)) {
 
                 // Set the attribute value to the integer.
                 attributeValues.add(((Integer)propertyValue).toString());
             }
 			
             // Check if the property is a string.
             else if (propertyType == String.class && !this.configuration.isEntityType(entityClass, propertyName) && !propertyValue.equals("")) {
 
                 // Set the attribute value to the string.
                 attributeValues.add((String)propertyValue);
             }
 			
             // Check if the property is a set of entities.
             else if (propertyType == Set.class && this.configuration.isEntityType(entityClass, propertyName)) {
 
                 // Set the attribute value to the set of entities.
                 for (Entity value : (Set<Entity>)propertyValue) {
                     attributeValues.add((String)value.getId());
                 }
             }
 			
             // Check if the property is a set of strings.
             else if (propertyType == Set.class && !this.configuration.isEntityType(entityClass, propertyName)) {
 
                 // Set the attribute value to the set of strings.
                 for (String value : (Set<String>)propertyValue) {
                         attributeValues.add(value);
                 }
             }
         }
 		
         return attributeValues;
     }
 		
     /**
      * Get the criteria.
      * 
      * @param  entityClass  the entity class.
      * 
      * @return  the criteria.
      * 
      * @throws  IllegalArgumentException  if the entity class is null or invalid.
      */
     @Override
     public <T> Criteria<T> getCriteria(final Class<T> entityClass) {
 		
         validEntityClass(entityClass, this.configuration.getEntityClasses());
         return new CriteriaImpl<T>(
                 this.configuration.getObjectClassValues(entityClass),
                 this.configuration.getPropertyAttributeMap(entityClass),
                 this.configuration.getSearchBase(entityClass),
                 this.configuration.getSearchScope(entityClass));
     }
 
     /**
      * Get the property value from the attribute values.
      * 
      * @param  entityClass      the entity class.
      * @param  attributeValues  the attribute values.
      * @param  propertyName     the property name.
      * 
      * @return  the property value.
      * 
      * @throws  EntityFactoryException  if unable to get the property value.
      */
     @SuppressWarnings("unchecked")
     private Object getPropertyValue(final Class<?> entityClass, final Set<String> attributeValues, final String propertyName) throws EntityFactoryException {
 		
         // Initialize the property value.
         Object propertyValue = null;
 
         // Check if there are attribute values.
         if (attributeValues.size() > 0) {
 			
             // Get the property type for the property name.
             Class<?> propertyType = this.configuration.getEntityFactory(entityClass).getMutatorParameterType(propertyName);
 
             // Initialize the attribute value list.
             List<String> attributeValueList = new ArrayList<String>(attributeValues);
 
             // Check if the property is an entity.
             if (Entity.class.isAssignableFrom(propertyType) && this.configuration.isEntityType(entityClass, propertyName)) {
 				
                 // Get the class for the entity.
                 Class<?> targetEntityClass = this.configuration.getTargetEntityClass(entityClass, propertyName);
 
                 // Determine if the entity will be fetched eagerly or lazily.
                 FetchType fetchType = this.configuration.getFetchType(entityClass, propertyName);
 
                 // Set the property value to the entity.
                 propertyValue = this.createEntity(targetEntityClass, attributeValueList.get(0), fetchType);
             }
 			
             // Check if the property is an integer.
             else if (propertyType == Integer.class && !this.configuration.isEntityType(entityClass, propertyName)) {
 
                 // Set the property value to the integer.
                 propertyValue = Integer.parseInt(attributeValueList.get(0));
             }
 			
             // Check if the property is a string.
             else if (propertyType == String.class && !this.configuration.isEntityType(entityClass, propertyName)) {
 
                 // Set the property value to the string.
                 propertyValue = attributeValueList.get(0);
             }
 			
             // Check if the property is a set of entities.
             else if (propertyType == Set.class && this.configuration.isEntityType(entityClass, propertyName)) {
 				
                 // Get the class for the entity.
                 Class<?> targetEntityClass = this.configuration.getTargetEntityClass(entityClass, propertyName);
 
                 // Determine if the entity will be fetched eagerly or lazily.
                 FetchType fetchType = this.configuration.getFetchType(entityClass, propertyName);
 
                 // Initialize the property value.
                 propertyValue = new HashSet();
 
                 // Loop through the entities.
                 for (String attributeValue : attributeValues) {
 
                     // Add the entity to the set of entities.
                     ((HashSet)propertyValue).add(this.createEntity(targetEntityClass, attributeValue, fetchType));
                 }
             }
 			
             // Check if the property is a set of strings.
             else if (propertyType == Set.class && !this.configuration.isEntityType(entityClass, propertyName)) {
 				
                 // Initialize the property value.
                 propertyValue = new HashSet<String>();
 				
                 // Loop through the strings.
                 for (String attributeValue : attributeValues) {
 					
                     // Add the string to the set of strings.
                     ((HashSet<String>)propertyValue).add(attributeValue);
                 }
             }
         }
 		
         return propertyValue;
     }
 
     /**
      * Persist the entity.
      *
      * @param  entity  the entity.
      *
      * @return  the persisted entity.
      * 
      * @throws  IllegalArgumentException  if the entity is null or invalid.
      * @throws  RepositoryException       if unable to persist the entity.
      */
     @Override
     @SuppressWarnings("unchecked")
     public <T> T persist(final T entity) {
 		
         validEntity(entity, this.configuration.getEntityClasses());
 		
         try {
 
             // Get the ID for the entity.
             String id = (String)((Entity<?,?>)entity).getId();
 
             // Get the property names for the entity.
             Set<String> propertyNames = this.configuration.getPropertyNames(entity.getClass());
 
             // Get the property value-property name map.
             Map<String,Object> propertyMap = this.configuration.getEntityFactory((Class<T>)entity.getClass()).getPropertyMap(entity, propertyNames);
 
             // Convert the property name-property value map to the attribute name-attribute value map.
             Map<String,Set<String>> attributeMap = this.convertPropertyMapToAttributeMap(entity.getClass(), propertyMap);
 
             // Check if the entity has already been persisted.
             if (this.directory.entryExists(id)) {
 
                 // Update the entity.
                 this.directory.updateEntry(id, attributeMap, this.configuration.getReferentialIntegrityMap(entity.getClass()));
             }
 
             // Otherwise this is a new entity.
             else {
 
                 // Add the object class to the attribute name-attribute value map.
                 attributeMap.put("objectClass", this.configuration.getObjectClassValues(entity.getClass()));
 
                 // Add the entity.
                 this.directory.addEntry(id, attributeMap, this.configuration.getReferentialIntegrityMap(entity.getClass()));
             }
         }
         catch (DirectoryException e) {
             throw new RepositoryException(
                     "Unable to persist the entity " + entity.getClass() + " identified by id '" + ((Entity)entity).getId() + "'.", 
                     e, entity.getClass());
         }
         catch (EntityFactoryException e) {
             throw new RepositoryException(
                     "Unable to persist the entity " + entity.getClass() + " identified by id '" + ((Entity)entity).getId() + "'.", 
                     e, entity.getClass());
         }
 
         return entity;
     }
 
     /**
      * Persist the list of entities.
      *
      * @param  entities  the entities.
      *
      * @return  the persisted list of entities.
      * 
      * @throws  IllegalArgumentException  if the list of entities is null or invalid.
      * @throws  RepositoryException       if unable to persist the list of entities.
      */
     @Override
     public <T> List<T> persistList(final List<T> entities) {
 
         validList(entities);
 		
         // Initialize.
         List<T> persistedEntities = new ArrayList<T>();
 
         // Loop through the entities.
         for (T entity : entities) {
 
             // Persist the entity.
             T persistedEntity = this.persist(entity);
 
             // Add the persisted entity.
             persistedEntities.add(persistedEntity);
         }
 
         return persistedEntities;
     }
 
     /**
      * Remove the entity.
      *
      * @param  entityClass  the entity class.
      * @param  id           the ID.
      * 
      * @throws  IllegalArgumentException  if the entity class or ID is null or invalid.
      * @throws  RepositoryException       if unable to remove the entity.
      */
     @Override
     public <T,U> void remove(final Class<T> entityClass, final U id) {
 		
         validEntityClass(entityClass, this.configuration.getEntityClasses());
         validId(id);
 
         try {
 
             // Remove the entity.
             this.directory.removeEntry((String)id);
         }
         catch (DirectoryException e) {
             throw new RepositoryException(
                     "Unable to remove the entity " + entityClass + " identified by id '" + id + "'.", 
                     e, entityClass);
         }
     }
 
     /**
     * Remove the entities by the list of IDs.
      *
      * @param  entityClass  the entity class.
      * @param  ids          the IDs.
      * 
      * @throws  IllegalArgumentException  if the entity class or list of IDs is null or invalid.
      * @throws  RepositoryException       if unable to remove the entities.
      */
     @Override
     public <T,U> void removeList(final Class<T> entityClass, final List<U> ids) {
 
         validList(ids);
 
         // Loop through the IDs.
         for (U id : ids) {
 
             // Remove the entity.
             this.remove(entityClass, id);
         }
     }
 }
