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
 
 package se.sperber.cryson.repository;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.Iterables;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.transform.DistinctRootEntityResultTransformer;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.access.prepost.PostAuthorize;
 import org.springframework.security.access.prepost.PostFilter;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Repository;
 import se.sperber.cryson.exception.CrysonValidationFailedException;
 import se.sperber.cryson.security.Restrictable;
 import se.sperber.cryson.serialization.ReflectionHelper;
 import se.sperber.cryson.serialization.UnauthorizedEntity;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.ValidatorFactory;
 import javax.ws.rs.core.MultivaluedMap;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 @Repository
 public class CrysonRepository {
 
   @Autowired
   private SessionFactory sessionFactory;
 
   @Autowired
   private ReflectionHelper reflectionHelper;
 
   @Value("${cryson.validation.enabled}")
   private boolean validationsEnabled;
 
   private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
 
   public Object findById(String entityClassName, Long id, Set<String> associationsToFetch) {
     Criteria criteria = sessionFactory.getCurrentSession()
             .createCriteria(entityClassName)
             .add(Restrictions.eq("id", id))
             .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
             .setCacheable(true);
 
     setFetchModeForAssociations(criteria, associationsToFetch);
 
     Object foundEntity = criteria.uniqueResult();
     return getEntityOrUnauthorizedEntity(foundEntity);
   }
 
   private Object getEntityOrUnauthorizedEntity(Object entity) {
     if (entity instanceof Restrictable) {
       if (((Restrictable) entity).isReadableBy(SecurityContextHolder.getContext().getAuthentication())) {
         return entity;
       } else {
         return new UnauthorizedEntity(entity.getClass().getSimpleName(), reflectionHelper.getPrimaryKey(entity));
       }
     }
     return entity;
   }
 
   public List findByIds(final String entityClassName, List<Long> ids, Set<String> associationsToFetch) {
     Criteria criteria = sessionFactory.getCurrentSession()
             .createCriteria(entityClassName)
             .add(Restrictions.in("id", ids))
             .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
             .setCacheable(true);
 
     setFetchModeForAssociations(criteria, associationsToFetch);
 
     List<Object> foundEntities = criteria.list();
 
     return FluentIterable.from(foundEntities).transform(new Function<Object, Object>() {
       public Object apply(Object object) {
         return getEntityOrUnauthorizedEntity(object);
       }
     }).toList();
   }
 
   @PostFilter("hasPermission(filterObject, 'read')")
   public List findByExample(String entityClassName, Object exampleEntity, Set<String> associationsToFetch) {
     Criteria criteria = sessionFactory.getCurrentSession()
             .createCriteria(entityClassName)
             .add(Example.create(exampleEntity).enableLike(MatchMode.EXACT))
             .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
             .setCacheable(true);
 
     setFetchModeForAssociations(criteria, associationsToFetch);
 
     return criteria.list();
   }
 
   @PostFilter("hasPermission(filterObject, 'read')")
   public List findAll(String entityClassName, Set<String> associationsToFetch) {
     Criteria criteria = sessionFactory.getCurrentSession()
             .createCriteria(entityClassName)
             .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
             .setCacheable(true);
 
     setFetchModeForAssociations(criteria, associationsToFetch);
 
     return criteria.list();
   }
 
   @PostFilter("hasPermission(filterObject, 'read')")
   public List<Object> findByNamedQuery(String queryName, MultivaluedMap<String,String> queryParameters) {
     Query query = sessionFactory.getCurrentSession().getNamedQuery(queryName)
             .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
             .setCacheable(true);
 
     for(String parameterName : queryParameters.keySet()) {
       List<String> parameters = queryParameters.get(parameterName);
       if (parameters.size() > 1) {
         query.setParameterList(parameterName, parameters);
       } else {
         query.setParameter(parameterName, parameters.get(0));
       }
     }
 
     return query.list();
   }
 
   @PostFilter("hasPermission(filterObject, 'read')")
   public List<Object> findByNamedQueryJson(String queryName, JsonElement parameters) {
     Query query = sessionFactory.getCurrentSession().getNamedQuery(queryName)
             .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
             .setCacheable(true);
 
     JsonObject parameterMap = parameters.getAsJsonObject();
     for (Map.Entry<String, JsonElement> element : parameterMap.entrySet()) {
       String key = element.getKey();
       JsonElement value = element.getValue();
       if (value.isJsonArray()) {
         JsonArray parametersArray = value.getAsJsonArray();
         List<String> parameterList = new ArrayList<String>();
         for (JsonElement arrayElement : parametersArray) {
           parameterList.add(arrayElement.getAsString());
         }
         query.setParameterList(key, parameterList);
       } else if (value.isJsonPrimitive()) {
         query.setParameter(key, value.getAsString());
       }
     }
 
     return query.list();
   }
 
   @PostAuthorize("hasPermission(#entity, 'write')")
   public void persist(Object entity) {
     if (validationsEnabled) {
       throwConstraintViolations(validatorFactory.getValidator().validate(entity));
     }
     sessionFactory.getCurrentSession().save(entity);
     sessionFactory.getCurrentSession().flush();
     if (entity instanceof Restrictable) {
       sessionFactory.getCurrentSession().refresh(entity);
     }
   }
 
   @PostAuthorize("hasPermission(#entity, 'read')")
   public void refresh(Object entity) {
     sessionFactory.getCurrentSession().refresh(entity);
   }
 
   @PostAuthorize("hasPermission(returnObject, 'write')")
   public Object update(Object entity) {
     if (validationsEnabled) {
       throwConstraintViolations(validatorFactory.getValidator().validate(entity));
     }
     Object mergedEntity = sessionFactory.getCurrentSession().merge(entity);
     if (entity instanceof Restrictable) {
       sessionFactory.getCurrentSession().flush();
       sessionFactory.getCurrentSession().refresh(mergedEntity);
     }
     return mergedEntity;
   }
 
   @PostAuthorize("hasPermission(#entity, 'write')")
   public void delete(Object entity) {
    Object persistentEntity = sessionFactory.getCurrentSession().get(entity.getClass(), reflectionHelper.getPrimaryKey(entity));
    sessionFactory.getCurrentSession().delete(persistentEntity);
   }
 
   @PostAuthorize("hasPermission(returnObject, 'read') and hasPermission(returnObject, 'write')")
   public Object ensureReadableAndWritable(String entityClassName, long id) {
     Criteria criteria = sessionFactory.getCurrentSession()
             .createCriteria(entityClassName)
             .add(Restrictions.eq("id", id))
             .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
             .setCacheable(true);
 
     return criteria.uniqueResult();
   }
 
   private void throwConstraintViolations(Set<ConstraintViolation<Object>> constraintViolations) {
     if (constraintViolations.size() > 0) {
       StringBuilder violationMessages = new StringBuilder();
       for(ConstraintViolation<Object> constraintViolation : constraintViolations) {
         violationMessages.append(constraintViolation.getRootBeanClass().getSimpleName()).append(" ");
         violationMessages.append(constraintViolation.getPropertyPath()).append(" ");
         violationMessages.append(constraintViolation.getMessage()).append("\n");
       }
       throw new CrysonValidationFailedException(violationMessages.toString(), constraintViolations);
     }
   }
 
   private void setFetchModeForAssociations(Criteria criteria, Set<String> associationsToFetch) {
     for(String associationToFetch : associationsToFetch) {
       criteria.setFetchMode(associationToFetch, FetchMode.JOIN);
     }
   }
 
   void setSessionFactory(SessionFactory sessionFactory) {
     this.sessionFactory = sessionFactory;
   }
 
   void setReflectionHelper(ReflectionHelper reflectionHelper) {
     this.reflectionHelper = reflectionHelper;
   }
 }
