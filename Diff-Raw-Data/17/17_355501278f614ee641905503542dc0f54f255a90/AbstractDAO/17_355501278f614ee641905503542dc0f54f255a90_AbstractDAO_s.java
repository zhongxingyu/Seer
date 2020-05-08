 /*
  * This is a common dao with basic CRUD operations and is not limited to any 
  * persistent layer implementation
  * 
  * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.dao.impl.hibernate;
 
 import com.smartitengineering.dao.common.queryparam.BasicCompoundQueryParameter;
 import com.smartitengineering.dao.common.queryparam.CompositionQueryParameter;
 import com.smartitengineering.dao.common.queryparam.OperatorType;
 import com.smartitengineering.dao.common.queryparam.QueryParameter;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWith2Values;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithOperator;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithPropertyName;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithValue;
 import com.smartitengineering.dao.common.queryparam.QueryParameterWithValues;
 import com.smartitengineering.dao.common.queryparam.SimpleNameValueQueryParameter;
 import com.smartitengineering.dao.common.queryparam.StringLikeQueryParameter;
 import com.smartitengineering.dao.common.queryparam.ValueOnlyQueryParameter;
 import com.smartitengineering.domain.PersistentDTO;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 
 import java.util.Map;
 import java.util.WeakHashMap;
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Session;
 import org.hibernate.criterion.AggregateProjection;
 import org.hibernate.criterion.Conjunction;
 import org.hibernate.criterion.CountProjection;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Junction;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projection;
 import org.hibernate.criterion.ProjectionList;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.PropertyProjection;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 public abstract class AbstractDAO<Template extends PersistentDTO>
     extends HibernateDaoSupport
     implements Serializable {
 
     private Map<Criteria, ProjectionList> projections =
         new WeakHashMap<Criteria, ProjectionList>();
 
     protected void createEntity(Template... entities) {
         if (entities == null) {
             throw new IllegalArgumentException();
         }
         Session session;
         boolean customSession = false;
         try {
             session = getSessionFactory().getCurrentSession();
         }
         catch (Exception ex) {
             session = getSessionFactory().openSession();
             customSession = true;
         }
         try {
             for (Template entity : entities) {
                 session.save(entity);
             }
         }
         finally {
             if (session != null) {
                 session.flush();
                 if (customSession && session.isOpen()) {
                     session.close();
                 }
             }
         }
     }
 
     protected void updateEntity(Template... entities) {
         if (entities == null) {
             throw new IllegalArgumentException();
         }
         Session session;
         boolean customSession = false;
         try {
             session = getSessionFactory().getCurrentSession();
         }
         catch (Exception ex) {
             session = getSessionFactory().openSession();
             customSession = true;
         }
         try {
             for (Template entity : entities) {
                 session.update(entity);
             }
         }
         finally {
             if (session != null) {
                 session.flush();
                 if (customSession && session.isOpen()) {
                     session.close();
                 }
             }
         }
     }
 
     protected void deleteEntity(Template... entities) {
         if (entities == null) {
             throw new IllegalArgumentException();
         }
         Session session;
         boolean customSession = false;
         try {
             session = getSessionFactory().getCurrentSession();
         }
         catch (Exception ex) {
             session = getSessionFactory().openSession();
             customSession = true;
         }
         try {
             for (Template entity : entities) {
                 session.delete(entity);
             }
         }
         finally {
             if (session != null) {
                 session.flush();
                 if (customSession && session.isOpen()) {
                     session.close();
                 }
             }
         }
     }
 
     protected Template readSingle(Class entityClass,
                                   Hashtable<String, QueryParameter> parameter) {
         return readSingle(entityClass, parameter.values().toArray(
             new QueryParameter[0]));
     }
 
     protected Object readOther(Class entityClass,
                                Hashtable<String, QueryParameter> parameter) {
         return readOther(entityClass, parameter.values().toArray(
             new QueryParameter[0]));
     }
 
     protected List<? extends Object> readOtherList(Class entityClass,
                                                    Hashtable<String, QueryParameter> parameter) {
         return readOtherList(entityClass, parameter.values().toArray(
             new QueryParameter[0]));
     }
 
     protected List<Template> readList(Class entityClass,
                                       Hashtable<String, QueryParameter> parameter) {
         return readList(entityClass, parameter.values().toArray(
             new QueryParameter[0]));
     }
 
     protected Template readSingle(Class entityClass,
                                   List<QueryParameter> parameter) {
         return readSingle(entityClass, parameter.toArray(new QueryParameter[0]));
     }
 
     protected Object readOther(Class entityClass,
                                List<QueryParameter> parameter) {
         return readOther(entityClass, parameter.toArray(new QueryParameter[0]));
     }
 
     protected List<? extends Object> readOtherList(Class entityClass,
                                                    List<QueryParameter> parameter) {
         return readOtherList(entityClass, parameter.toArray(
             new QueryParameter[0]));
     }
 
     protected List<Template> readList(Class entityClass,
                                       List<QueryParameter> parameter) {
         return readList(entityClass, parameter.toArray(new QueryParameter[0]));
     }
 
     protected Template readSingle(Class entityClass,
                                   QueryParameter... parameter) {
         Session session;
         boolean customSession = false;
         try {
             session = getSessionFactory().getCurrentSession();
         }
         catch (Exception ex) {
             session = getSessionFactory().openSession();
             customSession = true;
         }
         try {
             Criteria criteria = simpleSearchCriteria(session, entityClass,
                 parameter);
             return (Template) criteria.uniqueResult();
         }
         catch (Exception e) {
             throw new IllegalArgumentException(e);
         }
         finally {
             if (session != null) {
                 if (customSession && session.isOpen()) {
                     session.close();
                 }
             }
         }
     }
 
     protected Object readOther(Class entityClass,
                                QueryParameter... parameter) {
         Session session;
         boolean customSession = false;
         try {
             session = getSessionFactory().getCurrentSession();
         }
         catch (Exception ex) {
             session = getSessionFactory().openSession();
             customSession = true;
         }
         try {
             Criteria criteria = simpleSearchCriteria(session, entityClass,
                 parameter);
             return criteria.uniqueResult();
         }
         catch (Exception e) {
             throw new IllegalArgumentException(e);
         }
         finally {
             if (session != null) {
                 if (customSession && session.isOpen()) {
                     session.close();
                 }
             }
         }
     }
 
     protected List<? extends Object> readOtherList(Class entityClass,
                                                    QueryParameter... parameter) {
         Session session;
         boolean customSession = false;
         try {
             session = getSessionFactory().getCurrentSession();
         }
         catch (Exception ex) {
             session = getSessionFactory().openSession();
             customSession = true;
         }
         try {
             Criteria criteria = simpleSearchCriteria(session, entityClass,
                 parameter);
             return criteria.list();
         }
         catch (Exception e) {
             throw new IllegalArgumentException(e);
         }
         finally {
             if (session != null) {
                 if (customSession && session.isOpen()) {
                     session.close();
                 }
             }
         }
     }
 
     protected List<Template> readList(Class entityClass,
                                       QueryParameter... parameter) {
         Session session;
         boolean customSession = false;
         try {
             session = getSessionFactory().getCurrentSession();
         }
         catch (Exception ex) {
             session = getSessionFactory().openSession();
             customSession = true;
         }
         try {
             Criteria criteria = simpleSearchCriteria(session, entityClass,
                 parameter);
             return criteria.list();
         }
         catch (Exception e) {
             throw new IllegalArgumentException(e);
         }
         finally {
             if (session != null) {
                 if (customSession && session.isOpen()) {
                     session.close();
                 }
             }
         }
     }
 
     protected Criteria simpleSearchCriteria(Session session,
                                             Class queryClass,
                                             QueryParameter... parameter) {
         Criteria criteria = session.createCriteria(queryClass);
         for (QueryParameter param : parameter) {
             String propertyName = getPropertyName(param);
             processCriteria(criteria, propertyName, param);
         }
         return criteria;
     }
 
     @SuppressWarnings("unchecked")
     private void processCriteria(Criteria criteria,
                                  String element,
                                  QueryParameter parameter) {
         switch (parameter.getParameterType()) {
             case PARAMETER_TYPE_PROPERTY: {
                 criteria.add(getCriterion(element, parameter));
                 return;
             }
             case PARAMETER_TYPE_ORDER_BY: {
                 final Order order;
                 SimpleNameValueQueryParameter<com.smartitengineering.dao.common.queryparam.Order> queryParameter =
                     (SimpleNameValueQueryParameter<com.smartitengineering.dao.common.queryparam.Order>) parameter;
                 com.smartitengineering.dao.common.queryparam.Order requestedOrder =
                     queryParameter.getValue();
                 switch (requestedOrder) {
                     case ASC: {
                         order = Order.asc(element);
                         break;
                     }
                     case DESC: {
                         order = Order.desc(element);
                         break;
                     }
                     default: {
                         order = null;
                         break;
                     }
                 }
                 if (order != null) {
                     criteria.addOrder(order);
                 }
                 return;
             }
             case PARAMETER_TYPE_MAX_RESULT: {
                 ValueOnlyQueryParameter<Integer> queryParameter =
                     (ValueOnlyQueryParameter<Integer>) parameter;
                 criteria.setMaxResults(queryParameter.getValue());
                 return;
             }
             case PARAMETER_TYPE_FIRST_RESULT: {
                 ValueOnlyQueryParameter<Integer> queryParameter =
                     (ValueOnlyQueryParameter<Integer>) parameter;
                 criteria.setFirstResult(queryParameter.getValue());
                 return;
             }
             case PARAMETER_TYPE_DISJUNCTION: {
                 processDisjunction(criteria, parameter);
                 return;
             }
             case PARAMETER_TYPE_CONJUNCTION: {
                 processConjunction(criteria, parameter);
                 return;
             }
             case PARAMETER_TYPE_NESTED_PROPERTY: {
                 processNestedParameter(criteria, element, parameter);
                 return;
             }
             case PARAMETER_TYPE_COUNT: {
                 final Projection countProjection = Projections.count(element);
                 setProjection(criteria, countProjection);
                 return;
             }
             case PARAMETER_TYPE_ROW_COUNT: {
                 final Projection rowCount = Projections.rowCount();
                 setProjection(criteria, rowCount);
                 return;
             }
             case PARAMETER_TYPE_SUM: {
                 final AggregateProjection sum = Projections.sum(element);
                 setProjection(criteria, sum);
                 return;
             }
             case PARAMETER_TYPE_MAX: {
                 final AggregateProjection max = Projections.max(element);
                 setProjection(criteria, max);
                 return;
             }
             case PARAMETER_TYPE_MIN: {
                 final AggregateProjection min = Projections.min(element);
                 setProjection(criteria, min);
                 return;
             }
             case PARAMETER_TYPE_AVG: {
                 final AggregateProjection avg = Projections.avg(element);
                 setProjection(criteria, avg);
                 return;
             }
             case PARAMETER_TYPE_GROUP_BY: {
                 final PropertyProjection groupProperty =
                     Projections.groupProperty(element);
                 setProjection(criteria, groupProperty);
                 return;
             }
             case PARAMETER_TYPE_COUNT_DISTINCT: {
                 final CountProjection countDistinct =
                     Projections.countDistinct(element);
                 setProjection(criteria, countDistinct);
                 return;
             }
             case PARAMETER_TYPE_DISTINCT_PROP: {
                 final Projection distinct =
                     Projections.distinct(Projections.property(element));
                 setProjection(criteria, distinct);
                 return;
             }
             case PARAMETER_TYPE_UNIT_PROP: {
                 final PropertyProjection property =
                     Projections.property(element);
                 setProjection(criteria, property);
                 return;
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     private void processNestedParameter(Criteria criteria,
                                         String element,
                                         QueryParameter parameter) {
         FetchMode mode;
         CompositionQueryParameter queryParameter =
             (CompositionQueryParameter) parameter;
         switch (queryParameter.getFetchMode()) {
             case EAGER:
                 mode = FetchMode.EAGER;
                 break;
             case SELECT:
                 mode = FetchMode.SELECT;
                 break;
             case JOIN:
                 mode = FetchMode.JOIN;
                 break;
             case LAZY:
                 mode = FetchMode.LAZY;
                 break;
             default:
             case DEFAULT:
                 mode = FetchMode.DEFAULT;
                 break;
         }
         criteria.setFetchMode(element, ((mode == null) ? FetchMode.JOIN : mode));
         Collection<QueryParameter> nestedParameters = queryParameter.
             getNestedParameters();
         if (nestedParameters == null || nestedParameters.size() <= 0) {
             return;
         }
         Criteria nestedCriteria = criteria.createCriteria(element);
         for (QueryParameter nestedQueryParameter : nestedParameters) {
             processCriteria(nestedCriteria,
                 getPropertyName(nestedQueryParameter), nestedQueryParameter);
         }
     }
 
     @SuppressWarnings("unchecked")
     private void processCriterion(Junction criterion,
                                   String element,
                                   QueryParameter parameter) {
         switch (parameter.getParameterType()) {
             case PARAMETER_TYPE_PROPERTY: {
                 criterion.add(getCriterion(element, parameter));
                 return;
             }
             case PARAMETER_TYPE_CONJUNCTION: {
                 processConjunction(criterion, parameter);
                 break;
             }
             case PARAMETER_TYPE_DISJUNCTION: {
                 processDisjunction(criterion, parameter);
                 break;
             }
 
         }
     }
 
     @SuppressWarnings("unchecked")
     private void processDisjunction(Criteria criteria,
                                     QueryParameter parameter) {
         Disjunction disjunction = Expression.disjunction();
         workOnNestedParams(parameter, disjunction);
         criteria.add(disjunction);
     }
 
     @SuppressWarnings("unchecked")
     private void processConjunction(Criteria criteria,
                                     QueryParameter parameter) {
         Conjunction conjunction = Expression.conjunction();
         workOnNestedParams(parameter, conjunction);
         criteria.add(conjunction);
     }
 
     @SuppressWarnings("unchecked")
     private void processDisjunction(Junction junction,
                                     QueryParameter parameter) {
         Disjunction disjunction = Expression.disjunction();
         workOnNestedParams(parameter, disjunction);
         junction.add(disjunction);
     }
 
     @SuppressWarnings("unchecked")
     private void processConjunction(Junction junction,
                                     QueryParameter parameter) {
         Conjunction conjunction = Expression.conjunction();
         workOnNestedParams(parameter, conjunction);
         junction.add(conjunction);
     }
 
     private Criterion getCriterion(String element,
                                    QueryParameter queryParamemter) {
         OperatorType operator = getOperator(queryParamemter);
         Object parameter = getValue(queryParamemter);
         switch (operator) {
             case OPERATOR_EQUAL: {
                 return Expression.eq(element, parameter);
             }
             case OPERATOR_LESSER: {
                 return Expression.lt(element, parameter);
             }
             case OPERATOR_LESSER_EQUAL: {
                 return Expression.le(element, parameter);
             }
             case OPERATOR_GREATER: {
                 return Expression.gt(element, parameter);
             }
             case OPERATOR_GREATER_EQUAL: {
                 return Expression.ge(element, parameter);
             }
             case OPERATOR_NOT_EQUAL: {
                 return Expression.ne(element, parameter);
             }
             case OPERATOR_IS_NULL: {
                 return Expression.isNull(element);
             }
             case OPERATOR_IS_NOT_NULL: {
                 return Expression.isNotNull(element);
             }
             case OPERATOR_IS_EMPTY: {
                 return Expression.isEmpty(element);
             }
             case OPERATOR_IS_NOT_EMPTY: {
                 return Expression.isNotEmpty(element);
             }
             case OPERATOR_STRING_LIKE: {
                 MatchMode hibernateMatchMode;
                 com.smartitengineering.dao.common.queryparam.MatchMode matchMode =
                     getMatchMode(queryParamemter);
                 if (matchMode == null) {
                     matchMode =
                         com.smartitengineering.dao.common.queryparam.MatchMode.EXACT;
                 }
                 switch (matchMode) {
                     case END:
                         hibernateMatchMode = MatchMode.END;
                         break;
                     case EXACT:
                         hibernateMatchMode = MatchMode.EXACT;
                         break;
                     case START:
                         hibernateMatchMode = MatchMode.START;
                         break;
                     default:
                     case ANYWHERE:
                         hibernateMatchMode = MatchMode.ANYWHERE;
                         break;
                 }
                 return Expression.like(element, parameter.toString(),
                     hibernateMatchMode);
             }
             case OPERATOR_BETWEEN: {
                 parameter = getFirstParameter(queryParamemter);
                 Object parameter2 = getSecondParameter(queryParamemter);
                 return Expression.between(element, parameter, parameter2);
             }
             case OPERATOR_IS_IN: {
                 Collection inCollectin =
                     ((QueryParameterWithValues) queryParamemter).getValues();
                 return Restrictions.in(element, inCollectin);
             }
             case OPERATOR_IS_NOT_IN: {
                 Collection inCollectin =
                     ((QueryParameterWithValues) queryParamemter).getValues();
                 return Restrictions.not(Restrictions.in(element, inCollectin));
             }
         }
         return null;
     }
 
     private String getPropertyName(
         QueryParameter param) {
         final String propertyName;
 
         if (param instanceof QueryParameterWithPropertyName) {
             propertyName =
                 ((QueryParameterWithPropertyName) param).getPropertyName();
         }
         else {
             propertyName = "";
         }
 
         return propertyName;
     }
 
     private void setProjection(Criteria criteria,
                                final Projection projection) {
         ProjectionList currentProjections = projections.get(
             criteria);
         if (currentProjections == null) {
             currentProjections = Projections.projectionList();
             projections.put(criteria, currentProjections);
             criteria.setProjection(currentProjections);
         }
 
         currentProjections.add(projection);
     }
 
     private void workOnNestedParams(QueryParameter parameter,
                                     Junction criterion) {
         BasicCompoundQueryParameter queryParameter =
             (BasicCompoundQueryParameter) parameter;
         Collection<QueryParameter> nestedParameters =
             queryParameter.getNestedParameters();
         for (QueryParameter nestedParam : nestedParameters) {
             processCriterion(criterion, getPropertyName(nestedParam),
                 nestedParam);
         }
 
     }
 
     private OperatorType getOperator(QueryParameter queryParamemter) {
         QueryParameterWithOperator parameterWithOperator =
             (QueryParameterWithOperator) queryParamemter;
         return parameterWithOperator.getOperatorType();
     }
 
     private Object getValue(QueryParameter queryParamemter) {
         Object value;
         if(queryParamemter instanceof QueryParameterWithValue) {
             value = ((QueryParameterWithValue) queryParamemter).getValue();
         }
         else {
             value = null;
         }
         if (value == null) {
             value = "";
         }
         return value;
     }
 
     private Object getSecondParameter(QueryParameter queryParamemter) {
         if (queryParamemter instanceof QueryParameterWith2Values) {
             return ((QueryParameterWith2Values) queryParamemter).getSecondValue();
         }
         else {
             return "";
         }
     }
 
     private Object getFirstParameter(QueryParameter queryParamemter) {
         if (queryParamemter instanceof QueryParameterWith2Values) {
             return ((QueryParameterWith2Values) queryParamemter).getFirstValue();
         }
         else {
             return "";
         }
     }
 
     private com.smartitengineering.dao.common.queryparam.MatchMode getMatchMode(
         QueryParameter queryParamemter) {
         return ((StringLikeQueryParameter) queryParamemter).getMatchMode();
     }
 }
