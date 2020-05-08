 package org.eqaula.glue.service;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import org.eqaula.glue.model.BussinesEntity;
 import org.eqaula.glue.model.BussinesEntityAttribute;
 import org.eqaula.glue.model.BussinesEntityAttribute_;
 import org.eqaula.glue.model.BussinesEntityType;
 import org.eqaula.glue.model.BussinesEntityType_;
 import org.eqaula.glue.model.BussinesEntity_;
 import org.eqaula.glue.model.GroupType;
 import org.eqaula.glue.model.Property;
 import org.eqaula.glue.util.PersistenceUtil;
 import org.eqaula.glue.util.QueryData;
 import org.eqaula.glue.util.QuerySortOrder;
 
 /*
  * Copyright 2012 jlgranda.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  *
  * @author jlgranda
  */
 public class BussinesEntityService extends PersistenceUtil<BussinesEntity> {
 
     private static final long serialVersionUID = 6569835981443699931L;
     private static org.jboss.solder.logging.Logger log = org.jboss.solder.logging.Logger.getLogger(BussinesEntityService.class);
 
     public BussinesEntityService() {
         super(BussinesEntity.class);
     }
 
     @Override
     public void setEntityManager(EntityManager em) {
         this.em = em;
     }
 
     public BussinesEntity findByName(final String name) {
 
         log.info("find BussinesEntity with name " + name);
 
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<BussinesEntity> query = builder.createQuery(BussinesEntity.class);
 
         Root<BussinesEntity> bussinesEntity = query.from(BussinesEntity.class);
 
         query.where(builder.equal(bussinesEntity.get(BussinesEntity_.name), name));
 
         return getSingleResult(query);
     }
 
     public BussinesEntity findByNameAndProfile(final String name, final Long profile) {
 
         log.info("find BussinesEntity with name " + name + " and profile " + profile);
 
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<BussinesEntity> query = builder.createQuery(BussinesEntity.class);
 
         Root<BussinesEntity> bussinesEntity = query.from(BussinesEntity.class);
 
         query.where(builder.equal(bussinesEntity.get(BussinesEntity_.name), name));
 
         return getSingleResult(query);
     }
 
     public List<BussinesEntity> find(int maxresults, int firstresult) {
         log.info("find BussinesEntity, max results " + maxresults + " next result " + firstresult);
 
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<BussinesEntity> query = builder.createQuery(BussinesEntity.class);
 
         Root<BussinesEntity> from = query.from(BussinesEntity.class);
         query.select(from).orderBy(builder.desc(from.get(BussinesEntity_.name)));
         return getResultList(query, maxresults, firstresult);
     }
 
     public Long count() {
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<Long> query = builder.createQuery(Long.class);
         Root<BussinesEntity> from = query.from(BussinesEntity.class);
 
         query.select(builder.count(from));
 
         return this.<Long>getTypedSingleResult(query);
 
     }
 
     public Long countBussinesEntity(Class entityClass) {
         return this.count(entityClass);
     }
 
     public List<BussinesEntity> find(int maxresults, int firstresult, GroupType type) {
         Map<String, Object> filters = new HashMap<String, Object>();
         filters.put(BussinesEntity_.type.getName(), type);
         QueryData<BussinesEntity> qData = find(firstresult, maxresults, BussinesEntity_.name.getName(), QuerySortOrder.ASC, filters);
 
         return qData.getResult();
     }
 
     public List<BussinesEntity> find(int maxresults, int firstresult, BussinesEntity author) {
         Map<String, Object> filters = new HashMap<String, Object>();
         filters.put(BussinesEntity_.author.getName(), author);
         QueryData<BussinesEntity> qData = find(firstresult, maxresults, BussinesEntity_.name.getName(), QuerySortOrder.ASC, filters);
 
         return qData.getResult();
     }
 
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void saveAttributes(BussinesEntity bussinesEntity, Map<Property, Object> attributes) {
         BussinesEntityAttribute attribute = null;
         for (Map.Entry<Property, Object> e : attributes.entrySet()) {
             attribute = new BussinesEntityAttribute();
             attribute.setName(e.getKey().getName());
             attribute.setValue((Serializable) e.getValue());
             attribute.setType(e.getKey().getType());
             bussinesEntity.addBussinesEntityAttribute(attribute);
         }
 
         super.save(bussinesEntity);
         em.flush();
     }
 
     public BussinesEntityType findBussinesEntityTypeByName(String name) {
 
 
         log.info("find BussinesEntityType with name " + name);
 
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<BussinesEntityType> query = builder.createQuery(BussinesEntityType.class);
 
         Root<BussinesEntityType> bussinesEntity = query.from(BussinesEntityType.class);
 
         query.where(builder.equal(bussinesEntity.get(BussinesEntityType_.name), name));
 
         return getSingleResult(query);
     }
 
     public Long getCountRequiredProperties(Long bussinesEntityId, String bussinesEntityTypeName) {
         Query q = em.createNamedQuery("BussinesEntityAttribute.countRequiredProperties", Long.class);
         q.setParameter("bussinesEntityId", bussinesEntityId);
         q.setParameter("bussinesEntityTypeName", bussinesEntityTypeName);
         Long value = 0L;
         System.out.println("BussinesEntityAttribute.countRequiredProperties [bussinesEntityId, bussinesEntityTypeName] ---> [" + bussinesEntityId + ", " + bussinesEntityTypeName + "] " + q.getResultList());
         List<Long> result = q.getResultList();
         for (Long r : result) {
             value = r;
         }
         return value;
     }
 
     public Long getCountCompletedRequiredProperties(Long bussinesEntityId, String bussinesEntityTypeName) {
         Query q = em.createNamedQuery("BussinesEntityAttribute.countCompletedRequiredProperties", Long.class);
         q.setParameter("bussinesEntityId", bussinesEntityId);
         q.setParameter("bussinesEntityTypeName", bussinesEntityTypeName);
         Long value = 0L;
         List<Long> result = q.getResultList();
         for (Long r : result) {
             value = r;
         }
         return value;
     }
 
     public List<BussinesEntity> findBussinesEntityForType(BussinesEntityType type) {
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<BussinesEntity> query = builder.createQuery(BussinesEntity.class);
         Root<BussinesEntity> bussinesEntity = query.from(BussinesEntity.class);
         query.where(builder.equal(bussinesEntity.get(BussinesEntity_.type), type));
         return getResultList(query);
 
     }
 
     public List<BussinesEntity> findBussinesEntityForProperty(Property property) {
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<BussinesEntity> query = builder.createQuery(BussinesEntity.class);
         Root<BussinesEntity> bussinesEntity = query.from(BussinesEntity.class);
         query.where(builder.equal(bussinesEntity.get(BussinesEntity_.property), property));
         return getResultList(query);
     }
     
     public List<BussinesEntityAttribute> findBussinesEntityAttributeForProperty(Property property) {
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<BussinesEntityAttribute> query = builder.createQuery(BussinesEntityAttribute.class);
         Root<BussinesEntityAttribute> bussinesEntity = query.from(BussinesEntityAttribute.class);
         query.where(builder.equal(bussinesEntity.get(BussinesEntityAttribute_.property), property));
         return getResultList(query);
     }
     
    public BussinesEntity findBussinesEntityForCode(final String code){
         CriteriaBuilder builder = getCriteriaBuilder();
         CriteriaQuery<BussinesEntity> query = builder.createQuery(BussinesEntity.class);
         Root<BussinesEntity> bussinesEntity = query.from(BussinesEntity.class);
         query.where(builder.equal(bussinesEntity.get(BussinesEntity_.code), code));        
         return getSingleResult(query);
     }
 }
