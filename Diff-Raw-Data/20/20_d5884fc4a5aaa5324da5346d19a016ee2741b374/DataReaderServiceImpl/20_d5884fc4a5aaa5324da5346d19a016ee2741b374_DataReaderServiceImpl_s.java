 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.orm;
 
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.util.StringUtils;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.orm.query.*;
 import org.web4thejob.orm.query.Criterion;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
 
 /* package */class DataReaderServiceImpl implements DataReaderService {
     // ------------------------------ FIELDS ------------------------------
 
 
     @Autowired
     private SessionFactory sessionFactory;
 
     // ------------------------ INTERFACE METHODS ------------------------
 
     // --------------------- Interface DataReaderService ---------------------
 
     @Override
     @SuppressWarnings("unchecked")
     public <E extends Entity> E findById(Class<E> entityType, Serializable id) {
         return (E) sessionFactory.getCurrentSession().get(entityType, id);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <E extends Entity> List<E> findByQuery(Query query) {
         Criteria criteria = toDetachedCriteria(query).getExecutableCriteria(sessionFactory.getCurrentSession())
                 .setCacheable(query.isCached());
         if (StringUtils.hasText(query.getCacheRegion())) {
             criteria.setCacheRegion(query.getCacheRegion());
         }
         return criteria.list();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <E extends Entity> E findFirstByQuery(Query query) {
         Criteria criteria = toDetachedCriteria(query).getExecutableCriteria(sessionFactory.getCurrentSession())
                 .setMaxResults(1).setCacheable(query.isCached());
         if (StringUtils.hasText(query.getCacheRegion())) {
             criteria.setCacheRegion(query.getCacheRegion());
         }
         final List<E> list = criteria.list();
         if (list.size() > 0) {
             return list.get(0);
         }
         return null;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <E extends Entity> E findUniqueByQuery(Query query) {
         Criteria criteria = toDetachedCriteria(query).getExecutableCriteria(sessionFactory.getCurrentSession())
                 .setMaxResults(2).setCacheable(query.isCached());
         if (StringUtils.hasText(query.getCacheRegion())) {
             criteria.setCacheRegion(query.getCacheRegion());
         }
         final List<E> list = criteria.list();
         if (list.size() == 0) {
             return null;
         } else if (list.size() == 1) {
             return list.get(0);
         } else {
             throw new DataIntegrityViolationException("expecting unique result but got many");
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <E extends Entity> E get(Class<E> entityType, Serializable id) {
         return (E) sessionFactory.getCurrentSession().get(entityType, id);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <E extends Entity> List<E> getAll(Class<E> entityType) {
        return DetachedCriteria.forClass(entityType).getExecutableCriteria(sessionFactory.getCurrentSession()).list();
     }
 
     @Override
     public <E extends Entity> E getOne(Class<E> entityType) {
         return findFirstByQuery(ContextUtil.getBean(EntityFactory.class).buildQuery(entityType));
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <E extends Entity> E refresh(E entity) {
         sessionFactory.getCurrentSession().refresh(entity);
         return entity;
     }
 
     @Override
     public void evictCache() {
         if (sessionFactory.getCache() != null) {
             sessionFactory.getCache().evictCollectionRegions();
             sessionFactory.getCache().evictEntityRegions();
             sessionFactory.getCache().evictNaturalIdRegions();
             sessionFactory.getCache().evictQueryRegions();
         }
     }
 
     // -------------------------- OTHER METHODS --------------------------
 
     private DetachedCriteria toDetachedCriteria(Query query) {
         return toDetachedCriteria(query, "this");
     }
 
     private DetachedCriteria toDetachedCriteria(Query query, String alias) {
         boolean hasOneToManyAssociation = false;
         DetachedCriteria detachedCriteria;
         if (alias != null) {
             detachedCriteria = DetachedCriteria.forClass(query.getTargetType(), alias);
         } else {
             detachedCriteria = DetachedCriteria.forClass(query.getTargetType());
         }
 
         final Map<String, String> aliases = new HashMap<String, String>();
         for (final Criterion w4tjCriterion : query.getCriteria()) {
             if (w4tjCriterion.getCondition() != null && (w4tjCriterion.getCondition().getOperandsNo() == 0 ||
                     (w4tjCriterion.getValue() != null && StringUtils.hasText(w4tjCriterion.getValue().toString())))) {
                 if (!hasOneToManyAssociation) {
                     hasOneToManyAssociation = w4tjCriterion.getPropertyPath().hasOneToManySteps();
                 }
 
                 org.hibernate.criterion.Criterion hibCriterion;
 
                 if (w4tjCriterion.isLocal()) {
                     hibCriterion = toHibernateCriterion(w4tjCriterion, detachedCriteria.getAlias());
                 } else {
                     String aliasPath = null;
                     for (final PropertyMetadata propertyMetadata : w4tjCriterion.getPropertyPath().getSteps()) {
                         if (propertyMetadata.equals(w4tjCriterion.getPropertyPath().getLastStep())) {
                             break;
                         } else if (propertyMetadata.equals(w4tjCriterion.getPropertyPath().getFirstStep())) {
                             aliasPath = propertyMetadata.getName();
                         } else {
                             aliasPath += "." + propertyMetadata.getName();
                         }
 
                         buildAlias(detachedCriteria, aliases, aliasPath);
                     }
 
                     hibCriterion = toHibernateCriterion(w4tjCriterion, aliases.get(aliasPath));
                 }
 
                 detachedCriteria = detachedCriteria.add(hibCriterion);
             }
         }
 
         if (!query.getSubqueries().isEmpty()) {
             String masterId = detachedCriteria.getAlias() + "." + ContextUtil.getMRS().getEntityMetadata(query
                     .getTargetType()).getIdentifierName();
             int subqindex = 0;
             for (Subquery subquery : query.getSubqueries()) {
                 subqindex += 1;
                 detachedCriteria = detachedCriteria.add(toHibernateSubcriterion(masterId, String.valueOf(subqindex),
                         subquery));
             }
         }
 
         for (final OrderBy orderBy : query.getOrderings()) {
             PathMetadata pathMetadata = ContextUtil.getMRS().getPropertyPath(query.getTargetType(),
                     StringUtils.delimitedListToStringArray(orderBy.getProperty(), Path.DELIMITER));
 
             String property;
             if (pathMetadata.isMultiStep()) {
                 String aliasPath = null;
 
                 if (!hasOneToManyAssociation) {
                     hasOneToManyAssociation = pathMetadata.hasOneToManySteps();
                 }
 
                 for (PropertyMetadata propertyMetadata : pathMetadata.getSteps()) {
                     if (propertyMetadata.equals(pathMetadata.getLastStep())) {
                         break;
                     } else if (propertyMetadata.equals(pathMetadata.getFirstStep())) {
                         aliasPath = propertyMetadata.getName();
                     } else {
                         aliasPath += "." + propertyMetadata.getName();
                     }
 
                     buildAlias(detachedCriteria, aliases, aliasPath);
                 }
                 property = aliases.get(aliasPath) + "." + pathMetadata.getLastStep().getName();
             } else {
                 property = orderBy.getProperty();
             }
 
 
             if (orderBy.isDescending()) {
                 detachedCriteria = detachedCriteria.addOrder(Order.desc(property));
             } else {
                 detachedCriteria = detachedCriteria.addOrder(Order.asc(property));
             }
         }
 
         if (hasOneToManyAssociation) {
             detachedCriteria = detachedCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
         }
 
         return detachedCriteria;
     }
 
     private String buildAlias(DetachedCriteria detachedCriteria, Map<String, String> aliases, String aliasPath) {
         if (!aliases.containsKey(aliasPath)) {
             String alias = aliasPath.replaceAll("\\.", "_");
             aliases.put(aliasPath, alias);
             detachedCriteria.createAlias(aliasPath, alias);
         }
 
         return aliases.get(aliasPath);
     }
 
     private org.hibernate.criterion.Criterion toHibernateCriterion(Criterion criterion, String alias) {
         org.hibernate.criterion.Criterion hibernate_criterion;
         String propertyName = criterion.getPropertyPath().getLastStep().getName();
         if (alias != null) {
             propertyName = alias + "." + propertyName;
         }
 
         if (criterion.getCondition().equals(Condition.EQ)) {
             hibernate_criterion = Property.forName(propertyName).eq(criterion.getValue());
         } else if (criterion.getCondition().equals(Condition.NE)) {
             hibernate_criterion = Property.forName(propertyName).ne(criterion.getValue());
         } else if (criterion.getCondition().equals(Condition.GT)) {
             hibernate_criterion = Property.forName(propertyName).gt(criterion.getValue());
         } else if (criterion.getCondition().equals(Condition.GTE)) {
             hibernate_criterion = Property.forName(propertyName).ge(criterion.getValue());
         } else if (criterion.getCondition().equals(Condition.LT)) {
             hibernate_criterion = Property.forName(propertyName).lt(criterion.getValue());
         } else if (criterion.getCondition().equals(Condition.LTE)) {
             hibernate_criterion = Property.forName(propertyName).le(criterion.getValue());
         } else if (criterion.getCondition().equals(Condition.NL)) {
             hibernate_criterion = Property.forName(propertyName).isNull();
         } else if (criterion.getCondition().equals(Condition.NNL)) {
             hibernate_criterion = Property.forName(propertyName).isNotNull();
         } else if (criterion.getCondition().equals(Condition.SW)) {
             hibernate_criterion = Property.forName(propertyName).like(criterion.getValue().toString(), MatchMode.START);
         } else if (criterion.getCondition().equals(Condition.NSW)) {
             hibernate_criterion = Restrictions.not(Property.forName(propertyName).like(criterion.getValue().toString
                     (), MatchMode.START));
         } else if (criterion.getCondition().equals(Condition.CN)) {
             hibernate_criterion = Property.forName(propertyName).like(criterion.getValue().toString(),
                     MatchMode.ANYWHERE);
         } else if (criterion.getCondition().equals(Condition.NCN)) {
             hibernate_criterion = Restrictions.not(Property.forName(propertyName).like(criterion.getValue().toString
                     (), MatchMode.ANYWHERE));
         } else if (criterion.getCondition().equals(Condition.EW)) {
             hibernate_criterion = Property.forName(propertyName).like(criterion.getValue().toString(), MatchMode.END);
         } else if (criterion.getCondition().equals(Condition.NEW)) {
             hibernate_criterion = Restrictions.not(Property.forName(propertyName).like(criterion.getValue().toString
                     (), MatchMode.END));
         } else if (criterion.getCondition().equals(Condition.IN)) {
             hibernate_criterion = Restrictions.in(propertyName, (Collection) criterion.getValue());
         } else if (criterion.getCondition().equals(Condition.NIN)) {
             hibernate_criterion = Restrictions.not(Restrictions.in(propertyName, (Collection) criterion.getValue()));
         } else if (criterion.getCondition().equals(Condition.EX) || criterion.getCondition().equals(Condition.NEX)) {
             EntityMetadata target = criterion.getPropertyPath().getLastStep().getAssociatedEntityMetadata();
             Subquery subquery;
             if (criterion.getCondition().equals(Condition.EX)) {
                 subquery = new Subquery(Subquery.SubqueryType.TYPE_EXISTS, target);
             } else {
                 subquery = new Subquery(Subquery.SubqueryType.TYPE_NOT_EXISTS, target);
             }
 
             for (PropertyMetadata propertyMetadata : target.getPropertiesMetadata()) {
                 if (criterion.getPropertyPath().getLastStep().isAssociatedWith(propertyMetadata)) {
                     subquery.addCriterion(ContextUtil.getMRS().getPropertyPath(target.getEntityType(),
                             new Path(propertyMetadata.getName())), Condition.EQ, Subquery.MASTER_ID_PLACEHOLDER);
                     break;
                 }
             }
             if (subquery.getCriteria().isEmpty()) {
                 throw new RuntimeException("subquery failed: " + criterion.toString());
             }
 
             hibernate_criterion = toHibernateSubcriterion(alias + "." + criterion.getPropertyPath().getLastStep()
                     .getName(), alias, subquery);
         } else {
             throw new RuntimeException("unknown condition encountered: " + criterion.getCondition().toString());
         }
 
         return hibernate_criterion;
     }
 
     private org.hibernate.criterion.Criterion toHibernateSubcriterion(String masterId, String subqueryAlias,
                                                                       Subquery subquery) {
         String alias = "subq_" + subqueryAlias;
 
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(subquery.getTarget().getEntityType(), alias);
         for (Criterion criterion : subquery.getCriteria()) {
             if (Subquery.MASTER_ID_PLACEHOLDER == criterion.getValue()) {
                 String subProperty = alias + "." + criterion.getPropertyPath().getPath();
                 detachedCriteria = detachedCriteria.add(Property.forName(subProperty).eqProperty(masterId));
             } else if (org.hibernate.criterion.Property.class.isInstance(criterion.getValue())) {
                 detachedCriteria = detachedCriteria.add(((org.hibernate.criterion.Property) criterion.getValue())
                         .eqProperty(masterId));
             } else {
                 detachedCriteria = detachedCriteria.add(toHibernateCriterion(criterion, alias));
             }
         }
 
         if (subquery.getSubqueryType() == Subquery.SubqueryType.TYPE_EXISTS) {
             return Subqueries.exists(detachedCriteria.setProjection(Projections.property(alias + "." + subquery
                     .getTarget().getIdentifierName())));
         } else {
             return Subqueries.notExists(detachedCriteria.setProjection(Projections.property(alias + "." + subquery
                     .getTarget().getIdentifierName())));
         }
     }
 }
