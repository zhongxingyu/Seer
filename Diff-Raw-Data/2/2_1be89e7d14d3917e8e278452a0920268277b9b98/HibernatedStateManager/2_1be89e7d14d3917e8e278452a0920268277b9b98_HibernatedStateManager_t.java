 /*
  * #%L
  * Workflow state monitor
  * %%
  * Copyright (C) 2012 The State and University Library, Denmark
  * %%
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
  * #L%
  */
 package dk.statsbiblioteket.medieplatform.workflowstatemonitor;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /** A state manager backed by a hibernated database. */
 public class HibernatedStateManager implements StateManager {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     @Override
     public List<State> addState(String entityName, State state, List<String> preservedStates) {
         try {
             List<State> result = new ArrayList<State>();
             log.trace("Enter addState(entityName='{}',state='{}',preservedStates='{}')",
                       new Object[]{entityName, state, preservedStates});
             Session session = HibernateUtil.getSessionFactory().openSession();
             try {
                 session.beginTransaction();
 
                 // Get or create entity in database
                 Query entityQuery = session.createQuery("from Entity where name = :entityName");
                 entityQuery.setParameter("entityName", entityName);
                 Entity entity = (Entity) entityQuery
                         .uniqueResult();
                 if (entity == null) {
                     entity = new Entity();
                     entity.setName(entityName);
                     session.save(entity);
                 }
 
                 // Set entity and date in state
                 state.setEntity(entity);
                 if (state.getDate() == null) {
                     state.setDate(new Date());
                 }
 
                 // See if we have a preserved state now
                 State preservedState = null;
                 if (preservedStates != null && !preservedStates.isEmpty()) {
                    Query preservedStateQuery = session.createQuery("from State s where s.entity.name=:entityName AND s.date = (SELECT MAX(s2.date) FROM State s2 WHERE s.entity.id = s2.entity.id) AND s.stateName IN :preservedStates");
                     preservedStateQuery.setParameterList("preservedStates", preservedStates);
                     preservedStateQuery.setParameter("entityName", entityName);
                     preservedState = (State) preservedStateQuery.uniqueResult();
                 }
 
                 // Save the given state
                 session.save(state);
                 result.add(state);
 
                 // If there was a preserved state, readd it with the current date
                 if (preservedState != null) {
                     State represervedState = new State();
                     represervedState.setDate(new Date());
                     represervedState.setMessage(preservedState.getMessage());
                     represervedState.setComponent(preservedState.getComponent());
                     represervedState.setEntity(preservedState.getEntity());
                     represervedState.setStateName(preservedState.getStateName());
                     session.save(represervedState);
                     result.add(represervedState);
                 }
 
                 session.getTransaction().commit();
                 log.debug("Added state '{}'", state);
             } catch (RuntimeException e) {
                 Transaction transaction = session.getTransaction();
                 if (transaction != null && transaction.isActive()) {
                     transaction.rollback();
                 }
                 throw e;
             } finally {
                 if (session.isOpen()) {
                     session.close();
                 }
             }
             log.trace("Exit addState(entityName='{}',state='{}',preservedStates='{} -> {}')",
                       new Object[]{entityName, state, preservedStates, result});
             return result;
         } catch (RuntimeException e) {
             log.error("Failed addState(entityName='{}',state='{}',preservedStates='{}')",
                       new Object[]{entityName, state, preservedStates, e});
             throw e;
         }
     }
 
     @Override
     public List<Entity> listEntities() {
         try {
             log.trace("Enter listEntities()");
             Session session = HibernateUtil.getSessionFactory().openSession();
             List<Entity> entities;
             try {
                 session.beginTransaction();
 
                 entities = session.createQuery("from Entity").list();
                 session.getTransaction().commit();
             } finally {
                 if (session.isOpen()) {
                     session.close();
                 }
             }
             log.trace("Exit listEntities()->entities='{}'", entities.toString());
             return entities;
         } catch (RuntimeException e) {
             log.error("Failed listEntities(): '{}'", e);
             throw e;
         }
     }
 
     @Override
     public List<State> listStates(String entityName, boolean onlyLast, List<String> includes, List<String> excludes,
                                   Date startDate, Date endDate) {
         try {
             log.trace("Enter listStates(entityName='{}')", entityName);
             List<State> states = queryStates(entityName, onlyLast, includes, excludes, startDate, endDate);
             log.trace("Exit listStates(entityName='{}')->states='{}'", entityName, states);
             return states;
         } catch (RuntimeException e) {
             log.error("Failed listStates(entityName='{}')", entityName, e);
             throw e;
         }
     }
 
     @Override
     public List<State> listStates(boolean onlyLast, List<String> includes, List<String> excludes, Date startDate,
                                   Date endDate) {
         try {
             log.trace("Enter listStates(onlyLast='{}', includes='{}', excludes='{}')",
                       new Object[]{onlyLast, includes, excludes});
             List<State> states = queryStates(null, onlyLast, includes, excludes, startDate, endDate);
             log.trace("Exit listStates(onlyLast='{}', includes='{}', excludes='{}') -> states='{}'",
                       new Object[]{onlyLast, includes, excludes, states});
             return states;
         } catch (RuntimeException e) {
             log.error("Failed listStates(onlyLast='{}', includes='{}', excludes='{}')",
                       new Object[]{onlyLast, includes, excludes, e});
             throw e;
         }
     }
 
     private List<State> queryStates(String entityName, boolean onlyLast, List<String> includes, List<String> excludes,
                                     Date startDate, Date endDate) {
 
         Session session = HibernateUtil.getSessionFactory().openSession();
         List<State> states;
         try {
             session.beginTransaction();
             states = buildQuery(session, entityName, onlyLast, includes, excludes, startDate, endDate).list();
             session.getTransaction().commit();
         } finally {
             if (session.isOpen()) {
                 session.close();
             }
         }
         return states;
     }
 
     private Query buildQuery(Session session, String entityName, boolean onlyLast, List<String> includes,
                              List<String> excludes, Date startDate, Date endDate) {
         StringBuilder query = new StringBuilder();
         Map<String, Object> parameters = new HashMap<String, Object>();
         if (entityName != null) {
             initNextClause(query);
             query.append("s.entity.name = :entityName");
             parameters.put("entityName", entityName);
         }
 
         if (onlyLast) {
             initNextClause(query);
             query.append("s.date = (SELECT MAX(s2.date) FROM State s2 WHERE s.entity.id = s2.entity.id)");
         }
 
         if (includes != null && includes.size() != 0) {
             initNextClause(query);
             query.append("s.stateName IN (:includes)");
             parameters.put("includes", includes);
         }
 
         if (excludes != null && excludes.size() != 0) {
             initNextClause(query);
             query.append("NOT s.stateName IN (:excludes)");
             parameters.put("excludes", excludes);
         }
 
         if (startDate != null) {
             initNextClause(query);
             query.append("s.date >= :startDate");
             parameters.put("startDate", startDate);
         }
 
         if (endDate != null) {
             initNextClause(query);
             query.append("s.date < :endDate");
             parameters.put("endDate", endDate);
         }
 
         log.debug("Query: '{}' Parameters: '{}'", query, parameters);
         Query sessionQuery = session.createQuery("SELECT s FROM State s " + query.toString() + " ORDER BY s.date DESC");
         for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
             if (parameter.getValue() instanceof Collection) {
                 sessionQuery.setParameterList(parameter.getKey(), (Collection) parameter.getValue());
             } else {
                 sessionQuery.setParameter(parameter.getKey(), parameter.getValue());
             }
         }
         return sessionQuery;
     }
 
     private void initNextClause(StringBuilder query) {
         if (query.length() > 0) {
             query.append(" AND ");
         } else {
             query.append("WHERE ");
         }
     }
 }
