 package org.motechproject.quartz;
 
 import org.apache.log4j.Logger;
 import org.ektorp.ComplexKey;
 import org.ektorp.CouchDbConnector;
 import org.ektorp.DocumentNotFoundException;
 import org.ektorp.support.CouchDbRepositorySupport;
 import org.ektorp.support.View;
 import org.quartz.Calendar;
 import org.quartz.JobKey;
 import org.quartz.JobPersistenceException;
 import org.quartz.ObjectAlreadyExistsException;
 import org.quartz.Trigger;
 import org.quartz.TriggerKey;
 import org.quartz.impl.matchers.GroupMatcher;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static ch.lambdaj.Lambda.extract;
 import static ch.lambdaj.Lambda.on;
 
 public class CouchDbTriggerStore extends CouchDbRepositorySupport<CouchDbTrigger> {
 
     private Logger logger = Logger.getLogger(CouchDbTriggerStore.class);
 
     protected CouchDbTriggerStore(CouchDbConnector db) {
         super(CouchDbTrigger.class, db);
         initStandardDesignDocument();
     }
 
     // TODO: check conflict?
     public void updateTriggers(List<CouchDbTrigger> newTriggers) {
         if (logger.isDebugEnabled()) {
             for (CouchDbTrigger trigger : newTriggers) {
                 logger.debug(String.format("Document ID : %s; Revision: %s", trigger.getId(), trigger.getRevision()));
             }
         }
         db.executeBulk(newTriggers);
     }
 
     public void storeTrigger(CouchDbTrigger newTrigger, boolean replaceExisting) throws JobPersistenceException {
         CouchDbTrigger existingTrigger = getTriggerByKey(newTrigger.getKey());
         if (existingTrigger == null) {
             db.create(newTrigger);
             return;
         }
         if (replaceExisting) {
             if (!(existingTrigger.getJobName().equals(newTrigger.getJobKey().getName()) && existingTrigger.getJobGroup().equals(newTrigger.getJobKey().getGroup()))) {
                 throw new JobPersistenceException("New trigger is not related to the same job as the old trigger.");
             }
             newTrigger.setName(existingTrigger.getName());
             newTrigger.setGroup(existingTrigger.getGroup());
             newTrigger.setRevision(existingTrigger.getRevision());
             if (logger.isDebugEnabled()) {
                 logger.debug(String.format("Document ID : %s; Revision: %s", newTrigger.getId(), newTrigger.getRevision()));
             }
             db.update(newTrigger);
         } else {
             throw new ObjectAlreadyExistsException("trigger already exists " + newTrigger.getKey());
         }
     }
 
     public boolean removeTrigger(TriggerKey triggerKey) throws JobPersistenceException {
         logger.info("removeTrigger: " + triggerKey + "[" + Thread.currentThread().getId() + "]");
         CouchDbTrigger trigger = getTriggerByKey(triggerKey);
 
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Document ID : %s; Revision: %s", trigger.getId(), trigger.getRevision()));
        }
 
         if (trigger == null) {
             return false;
         }
         db.delete(trigger);
         return true;
     }
 
     public boolean replaceTrigger(TriggerKey triggerKey, CouchDbTrigger newTrigger) throws JobPersistenceException {
         CouchDbTrigger existingTrigger = getTriggerByKey(triggerKey);
 
         if (logger.isDebugEnabled()) {
             logger.debug(String.format("Document ID : %s; Revision: %s", existingTrigger.getId(), existingTrigger.getRevision()));
         }
 
        if (existingTrigger == null) {
            return false;
        }
         if (!(existingTrigger.getJobName().equals(newTrigger.getJobKey().getName()) && existingTrigger.getJobGroup().equals(newTrigger.getJobKey().getGroup()))) {
             throw new JobPersistenceException("New trigger is not related to the same job as the old trigger.");
         }
         newTrigger.setName(existingTrigger.getName());
         newTrigger.setGroup(existingTrigger.getGroup());
         newTrigger.setRevision(existingTrigger.getRevision());
         db.update(newTrigger);
         return true;
     }
 
     public CouchDbTrigger retrieveTrigger(TriggerKey triggerKey) throws JobPersistenceException {
         return getTriggerByKey(triggerKey);
     }
 
     public CouchDbTrigger getTriggerByKey(TriggerKey triggerKey) throws JobPersistenceException {
         try {
             return get(CouchDbTrigger.toId(triggerKey.getGroup(), triggerKey.getName()));
         } catch (DocumentNotFoundException e) {
             return null;
         }
     }
 
     @View(name = "by_triggerkey", map = "function(doc) { if (doc.type === 'CouchDbTrigger') emit([doc.trigger_name, doc.trigger_group], doc._id); }")
     public List<CouchDbTrigger> getTriggersByKeys(List<TriggerKey> triggerKeys) throws JobPersistenceException {
         List<ComplexKey> keys = new ArrayList<ComplexKey>();
         for (TriggerKey triggerKey : triggerKeys) {
             keys.add(ComplexKey.of(triggerKey.getName(), triggerKey.getGroup()));
         }
         return db.queryView(createQuery("by_triggerkey").includeDocs(true).keys(keys), type);
     }
 
     public boolean checkExists(TriggerKey triggerKey) throws JobPersistenceException {
         return getTriggerByKey(triggerKey) != null;
     }
 
     @View(name = "by_jobkey", map = "function(doc) { if (doc.type === 'CouchDbTrigger') emit([doc.job_name, doc.job_group], doc._id); }")
     public List<CouchDbTrigger> findByJob(JobKey jobKey) {
         return db.queryView(createQuery("by_jobkey").key(ComplexKey.of(jobKey.getName(), jobKey.getGroup())).includeDocs(true), type);
     }
 
     @View(name = "all_triggers", map = "function(doc) { if (doc.type === 'CouchDbTrigger') emit(doc._id, doc._id); }")
     public List<CouchDbTrigger> getAll() {
         return db.queryView(createQuery("all_triggers").includeDocs(true), type);
     }
 
     public int getNumberOfTriggers() {
         return getAll().size();
     }
 
     public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) {
         Set<TriggerKey> matchedKeys = new HashSet<TriggerKey>();
         for (CouchDbTrigger couchdbTrigger : getAll()) {
             if (matcher.isMatch(couchdbTrigger.getKey())) {
                 matchedKeys.add(couchdbTrigger.getKey());
             }
         }
         return matchedKeys;
     }
 
     @View(name = "by_triggerGroupName", map = "function(doc) { if (doc.type === 'CouchDbTrigger') emit(doc.group, doc._id); }")
     public List<String> getTriggerGroupNames() {
         return new ArrayList<String>(new HashSet<String>(extract(db.queryView(createQuery("by_triggerGroupName").includeDocs(true), type), on(CouchDbTrigger.class).getGroup())));
     }
 
     public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws JobPersistenceException {
         CouchDbTrigger trigger = getTriggerByKey(triggerKey);
         if (trigger == null) {
             return null;
         }
         return trigger.getState().getQuartzTriggerState();
     }
 
     @View(name = "by_nextFireTime", map = "function(doc) { if (doc.type === 'CouchDbTrigger' && doc.state === 'WAITING') emit(doc.next_fire_time, doc._id); }")
     public List<CouchDbTrigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) throws JobPersistenceException {
         logger.info("by_nextFireTime");
         return db.queryView(createQuery("by_nextFireTime").startKey(new Date(0)).endKey(new Date(noLaterThan + timeWindow)).limit(maxCount).includeDocs(true), CouchDbTrigger.class);
     }
 
     public void releaseAcquiredTrigger(CouchDbTrigger couchdbTrigger) throws JobPersistenceException {
         if (log.isInfoEnabled()) {
             log.info("releaseAcquiredTrigger:" + couchdbTrigger);
         }
         couchdbTrigger.setState(CouchDbTriggerState.WAITING);
         replaceTrigger(couchdbTrigger.getKey(), couchdbTrigger);
 
     }
 
     @View(name = "by_calendarName", map = "function(doc) { if (doc.type == 'CouchDbTrigger') emit(doc.calendar_name, doc._id);}")
     public List<CouchDbTrigger> findByCalendarName(String calName) {
         return db.queryView(createQuery("by_calendarName").key(calName).includeDocs(true), CouchDbTrigger.class);
     }
 
     List<CouchDbTrigger> triggersFired(List<CouchDbTrigger> triggers, Map<String, Calendar> calendarMap) throws JobPersistenceException {
         if (logger.isInfoEnabled()) {
             logger.info("triggersFired: Releasing triggers " + triggers.size());
             logger.info(triggers);
         }
         for (CouchDbTrigger trigger : triggers) {
             trigger.triggered(calendarMap.get(trigger.getKey()));
             trigger.setState(CouchDbTriggerState.WAITING);
             log.info("Updating trigger back to waiting state " + trigger);
         }
         updateTriggers(triggers);
         return triggers;
     }
 
     public void updateTriggerState(TriggerKey triggerKey, String state) {
         ///getTriggerState()    todo
     }
 
     public void removeAll() {
         try {
             for (CouchDbTrigger doc : getAll()) {
                 db.delete(db.get(CouchDbTrigger.class, doc.getId()));
             }
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
         }
     }
 }
