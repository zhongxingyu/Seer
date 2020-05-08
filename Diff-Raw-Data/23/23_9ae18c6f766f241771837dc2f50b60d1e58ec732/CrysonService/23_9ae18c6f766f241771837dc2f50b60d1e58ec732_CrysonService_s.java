 /*
   Cryson
   
   Copyright 2011-2012 Björn Sperber (cryson@sperber.se)
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   
   http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
 
 package se.sperber.cryson.service;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import org.hibernate.SessionFactory;
 import org.hibernate.annotations.Type;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
 import org.springframework.core.type.filter.AnnotationTypeFilter;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import se.sperber.cryson.exception.CrysonEntityNotFoundException;
 import se.sperber.cryson.listener.ListenerNotificationBatch;
 import se.sperber.cryson.repository.CrysonRepository;
 import se.sperber.cryson.security.Restrictable;
 import se.sperber.cryson.serialization.CrysonSerializer;
 import se.sperber.cryson.serialization.ReflectionHelper;
 import se.sperber.cryson.serialization.UnauthorizedEntity;
 
 import javax.annotation.PostConstruct;
 import javax.persistence.Entity;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.util.*;
 
 @Transactional
 @Service
 public class CrysonService {
 
   @Autowired
   private CrysonRepository crysonRepository;
 
   @Autowired
   private CrysonSerializer crysonSerializer;
 
   @Autowired
   private ReflectionHelper reflectionHelper;
   
   @Autowired
   private SessionFactory sessionFactory;
 
   @Value("${cryson.model.package}")
   private String modelsPackage;
   
   private Map<String, Class<?>> entityClassesBySimpleName;
 
   private Map<Class<?>, Integer> classInsertionOrder;
 
   private String cachedDefinitions;
 
   @PostConstruct
   public void findEntityClasses() {
     ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
     provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
     Set<BeanDefinition> components = provider.findCandidateComponents(modelsPackage);
     Map<String, Class<?>> result = new HashMap<String, Class<?>>();
     for (BeanDefinition component : components)
     {
       try {
         Class klazz = Class.forName(component.getBeanClassName());
         result.put(klazz.getSimpleName(), klazz);
       } catch (ClassNotFoundException e) {}
     }
     entityClassesBySimpleName = result;
 
     classInsertionOrder = calculateClassInsertionOrder(entityClassesBySimpleName.values());
   }
 
   public Response getEntityDefinition(String entityName) throws Exception {
     return Response.ok().entity(crysonSerializer.serializeWithoutAugmentation(getEntityDefinitionMap(entityName))).build();
   }
 
   public Response getEntityDefinitions() throws Exception {
     if (cachedDefinitions == null) {
       Map<String, Map<String, String>> entityDefinitions = new HashMap<String, Map<String, String>>();
       for(String entityClassName : entityClassesBySimpleName.keySet()) {
         entityDefinitions.put(entityClassName, getEntityDefinitionMap(entityClassName));
       }
       cachedDefinitions = crysonSerializer.serializeWithoutAugmentation(entityDefinitions);
     }
     return Response.ok().entity(cachedDefinitions).build();
   }
 
   private Map<String, String> getEntityDefinitionMap(String entityName) throws ClassNotFoundException {
     Map<String, String> entityDefinition = new HashMap<String, String>();
 
     Class klazz = entityClass(entityName);
     Set<Field> fields = reflectionHelper.getAllDeclaredFields(klazz);
 
     for(Field field : fields) {
       String fieldClassName = field.getType().getSimpleName();
       if (field.isAnnotationPresent(Type.class)) {
         fieldClassName = "UserType_" + fieldClassName;
       } else if (field.getGenericType() instanceof ParameterizedType) {
         fieldClassName = ((Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0]).getSimpleName();
       }
       entityDefinition.put(field.getName(), fieldClassName);
     }
 
     Set <Method> transientMethods = reflectionHelper.getAllDeclaredVirtualAttributeGetters(klazz);
     for (Method method : transientMethods) {
       if (method.getGenericReturnType() instanceof ParameterizedType) {
         String className = ((Class) ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0]).getSimpleName();
         entityDefinition.put(reflectionHelper.getAttributeNameFromGetterName(method.getName()), className);
       } else {
         entityDefinition.put(reflectionHelper.getAttributeNameFromGetterName(method.getName()), method.getReturnType().getSimpleName());
       }
     }
 
     entityDefinition.put("crysonEntityClass", "String");
 
     return entityDefinition;
   }
 
   public Response getEntityById(String entityName, Long id, Set<String> associationsToFetch) {
     Object entity = crysonRepository.findById(qualifiedEntityClassName(entityName), id, associationsToFetch);
     if (entity != null) {
       return Response.ok(crysonSerializer.serialize(entity, associationsToFetch)).build();
     } else {
       throw new CrysonEntityNotFoundException("Not found", null);
     }
   }
 
   public Response getEntitiesByIds(final String entityName, List<Long> ids, Set<String> associationsToFetch) {
     final List<Object> entities = crysonRepository.findByIds(qualifiedEntityClassName(entityName), ids, associationsToFetch);
     return Response.ok(crysonSerializer.serialize(entities, associationsToFetch)).build();
   }
 
   public Response getEntitiesByExample(String entityName, String exampleJson, Set<String> associationsToFetch) throws Exception {
     Class entityClass = entityClass(entityName);
     Object exampleEntity = crysonSerializer.deserialize(exampleJson, entityClass, null);
     List<Object> entities = crysonRepository.findByExample(qualifiedEntityClassName(entityName), exampleEntity, associationsToFetch);
     return Response.ok(crysonSerializer.serialize(entities, associationsToFetch)).build();
   }
 
   public Response getAllEntities(String entityName, Set<String> associationsToFetch) {
     List<Object> entities = crysonRepository.findAll(qualifiedEntityClassName(entityName), associationsToFetch);
     return Response.ok(crysonSerializer.serialize(entities, associationsToFetch)).build();
   }
 
   public Response getEntitiesByNamedQuery(String queryName, MultivaluedMap<String, String> queryParameters, Set<String> associationsToFetch) {
     List<Object> entities = crysonRepository.findByNamedQuery(queryName, queryParameters);
     return Response.ok(crysonSerializer.serialize(entities, associationsToFetch)).build();
   }
 
   public Response getEntitiesByNamedQueryJson(String queryName, Set<String> associationsToFetch, JsonElement parameters) {
     List<Object> entities = crysonRepository.findByNamedQueryJson(queryName, parameters);
     return Response.ok(crysonSerializer.serialize(entities, associationsToFetch)).build();
   }
 
   public Response createEntity(String entityName, String json, ListenerNotificationBatch listenerNotificationBatch) throws Exception {
     Class entityClass = entityClass(entityName);
     Object entity = crysonSerializer.deserialize(json, entityClass, null);
     crysonRepository.persist(entity);
     crysonRepository.refresh(entity);
     listenerNotificationBatch.entityCreated(entity);
     return Response.ok(crysonSerializer.serialize(entity)).build();
   }
 
 
   public void validatePermissions(JsonElement committedEntities) throws Exception {
     JsonArray updatedEntities = committedEntities.getAsJsonObject().get("updatedEntities").getAsJsonArray();
     for(JsonElement updatedEntityElement : updatedEntities) {
       Object entity = crysonSerializer.deserialize(updatedEntityElement, entityClass(updatedEntityElement), new HashMap<Long, Long>());
       if (entity instanceof Restrictable) {
         Object originalEntity = crysonRepository.ensureReadableAndWritable(qualifiedEntityClassName(updatedEntityElement.getAsJsonObject().get("crysonEntityClass").getAsString()),
                 updatedEntityElement.getAsJsonObject().get("id").getAsLong());
         sessionFactory.getCurrentSession().evict(originalEntity);
       }
     }
   }
 
   public JsonObject commit(JsonElement committedEntities, ListenerNotificationBatch listenerNotificationBatch, List<Object> refreshedPersistedEntities, List<Object> updatedPersistedEntities) throws Exception {
     Map<Long, Long> replacedTemporaryIds = new HashMap<Long, Long>();
     JsonArray persistedEntities = committedEntities.getAsJsonObject().get("persistedEntities").getAsJsonArray();
     Collection<JsonElement> sortedPersistedEntities = topologicallySortPersistedEntities(persistedEntities);
     for(JsonElement persistedEntityElement : sortedPersistedEntities) {
       Object entity = crysonSerializer.deserialize(persistedEntityElement, entityClass(persistedEntityElement), replacedTemporaryIds);
       Long temporaryId = reflectionHelper.getPrimaryKey(entity);
       if (temporaryId < 0) {
         reflectionHelper.setPrimaryKey(entity, null);
       }
       crysonRepository.persist(entity);
       refreshedPersistedEntities.add(entity);
       Long replacementId = reflectionHelper.getPrimaryKey(entity);
       replacedTemporaryIds.put(temporaryId, replacementId);
     }
 
    JsonArray deletedEntities = committedEntities.getAsJsonObject().get("deletedEntities").getAsJsonArray();
    for(JsonElement deletedEntityElement : deletedEntities) {
      Object entity = crysonSerializer.deserialize(deletedEntityElement, entityClass(deletedEntityElement), replacedTemporaryIds);
      crysonRepository.delete(entity);
      listenerNotificationBatch.entityDeleted(entity);
    }

     JsonArray updatedEntities = committedEntities.getAsJsonObject().get("updatedEntities").getAsJsonArray();
     for(JsonElement updatedEntityElement : updatedEntities) {
       Object entity = crysonSerializer.deserialize(updatedEntityElement, entityClass(updatedEntityElement), replacedTemporaryIds);
       patchOneToOnes(entity);
       Object updatedEntity = crysonRepository.update(entity);
       updatedPersistedEntities.add(updatedEntity);
     }
 
     JsonObject responseJsonObject = new JsonObject();
     responseJsonObject.add("replacedTemporaryIds", crysonSerializer.serializeToTreeWithoutAugmentation(replacedTemporaryIds));
 
     return responseJsonObject;
   }
 
   private void patchOneToOnes(Object entity) throws Exception {
     List<Field> oneToOneFields = reflectionHelper.getOneToOneFields(entity);
     for(Field field : oneToOneFields) {
       Object associatedEntity = field.get(entity);
       if (associatedEntity != null) {
         field.set(entity, crysonRepository.findById(crysonSerializer.getEntityClassName(associatedEntity), reflectionHelper.getPrimaryKey(associatedEntity), Collections.EMPTY_SET));
       }
     }
   }
 
   public void refreshEntities(JsonObject responseJsonObject, ListenerNotificationBatch listenerNotificationBatch, List<Object> persistedEntities, List<Object> updatedEntities) {
     List<Object> refreshedPersistedEntities = new ArrayList<Object>(persistedEntities.size());
     List<Object> refreshedUpdatedEntities = new ArrayList<Object>(updatedEntities.size());
 
     for (Object persistedEntity : persistedEntities) {
       Object refreshedPersistedEntity = crysonRepository.findById(crysonSerializer.getEntityClassName(persistedEntity), reflectionHelper.getPrimaryKey(persistedEntity), Collections.EMPTY_SET);
       refreshedPersistedEntities.add(refreshedPersistedEntity);
       listenerNotificationBatch.entityCreated(refreshedPersistedEntity);
     }
 
     for (Object updatedEntity : updatedEntities) {
       Object refreshedUpdatedEntity = crysonRepository.findById(crysonSerializer.getEntityClassName(updatedEntity), reflectionHelper.getPrimaryKey(updatedEntity), Collections.EMPTY_SET);
       refreshedUpdatedEntities.add(refreshedUpdatedEntity);
       listenerNotificationBatch.entityUpdated(refreshedUpdatedEntity);
     }
 
     responseJsonObject.add("persistedEntities", crysonSerializer.serializeToTree(refreshedPersistedEntities, Collections.<String>emptySet()));
     responseJsonObject.add("updatedEntities", crysonSerializer.serializeToTree(refreshedUpdatedEntities, Collections.<String>emptySet()));
   }
 
   private Collection<JsonElement> topologicallySortPersistedEntities(JsonArray persistedEntities) {
     List<JsonElement> result = new ArrayList<JsonElement>(persistedEntities.size());
 
     for(JsonElement persistedEntity : persistedEntities) {
       result.add(persistedEntity);
     }
 
     Collections.sort(result, new Comparator<JsonElement>() {
       public int compare(JsonElement a, JsonElement b) {
         try {
           int aInsertionOrder = classInsertionOrder.get(entityClass(a));
           int bInsertionOrder = classInsertionOrder.get(entityClass(b));
           return ((aInsertionOrder < bInsertionOrder) ? -1 : ((aInsertionOrder > bInsertionOrder) ? 1 : 0));
         } catch (ClassNotFoundException e) {
           throw new RuntimeException("Failed to find class for entity", e);
         }
       }
     });
 
     return result;
   }
 
   private Map<Class<?>, Integer> calculateClassInsertionOrder(Collection<Class<?>> classes) {
     Map<Class<?>, List<Class<?>>> classOwnerships = new HashMap<Class<?>, List<Class<?>>>();
     Set<Class<?>> nonOwningClasses = new HashSet<Class<?>>();
     for(Class<?> klazz : classes) {
       List<Class<?>> ownerships = new ArrayList<Class<?>>(reflectionHelper.ownedAssociationFields(klazz).values());
       classOwnerships.put(klazz, ownerships);
       if (ownerships.size() == 0) {
         nonOwningClasses.add(klazz);
       }
     }
 
     Map<Class<?>, List<Class<?>>> classOwners = new HashMap<Class<?>, List<Class<?>>>();
     for(Class<?> klazz : classes) {
       List<Class<?>> owners = new ArrayList<Class<?>>();
       for(Class<?> otherKlazz : classes) {
         if (isOwnedBy(classOwnerships, klazz, otherKlazz)) {
           owners.add(otherKlazz);
         }
       }
       classOwners.put(klazz, owners);
     }
 
     List<Class<?>> result = new ArrayList<Class<?>>();
     Set<Class<?>> visitedClasses = new HashSet<Class<?>>();
     for(Class<?> nonOwningClass : nonOwningClasses) {
       topologicalSort(nonOwningClass, classOwners, result, visitedClasses);
     }
 
     Collections.reverse(result);
 
     Map<Class<?>, Integer> classInsertionOrder = new HashMap<Class<?>, Integer>();
 
     for(int ix = 0;ix < result.size();ix++) {
       classInsertionOrder.put(result.get(ix), ix);
     }
 
     return classInsertionOrder;
   }
 
   private boolean isOwnedBy(Map<Class<?>, List<Class<?>>> classOwnerships, Class<?> klazz, Class<?> otherKlazz) {
     Class currentKlazz = klazz;
     while(currentKlazz != Object.class) {
       if (classOwnerships.get(otherKlazz).contains(currentKlazz)) {
         return true;
       }
       currentKlazz = currentKlazz.getSuperclass();
     }
     return false;
   }
 
   private void topologicalSort(Class<?> klazz, Map<Class<?>, List<Class<?>>> classOwners, List<Class<?>> result, Set<Class<?>> visitedClasses) {
     if (!visitedClasses.contains(klazz)) {
       visitedClasses.add(klazz);
       for(Class<?> owningClass : classOwners.get(klazz)) {
         topologicalSort(owningClass, classOwners, result, visitedClasses);
       }
       result.add(klazz);
     }
   }
 
   public Response commitEntity(Object entity, ListenerNotificationBatch listenerNotificationBatch) {
     crysonRepository.update(entity);
     listenerNotificationBatch.entityUpdated(entity);
     return Response.ok().build();
   }
 
   private String qualifiedEntityClassName(String entityName) {
     Class<?> klazz = entityClassesBySimpleName.get(entityName);
     return klazz.getName();
   }
 
   private Class entityClass(String entityName) throws ClassNotFoundException {
     return entityClassesBySimpleName.get(entityName);
   }
 
   private Class entityClass(JsonElement entityElement) throws ClassNotFoundException {
     return entityClass(entityElement.getAsJsonObject().get("crysonEntityClass").getAsString());
   }
 
   void setCrysonSerializer(CrysonSerializer crysonSerializer) {
     this.crysonSerializer = crysonSerializer;
   }
 
   void setCrysonRepository(CrysonRepository crysonRepository) {
     this.crysonRepository = crysonRepository;
   }
 
   void setEntityClassesBySimpleName(Map<String, Class<?>> entityClassesBySimpleName) {
     this.entityClassesBySimpleName = entityClassesBySimpleName;
   }
 }
