 /*
  *    Copyright (c) 2010-2011 Manuel Polo (mrmx.org)
  *
  *    This program is free software: you can redistribute it and/or  modify
  *    it under the terms of the GNU Affero General Public License, version 3,
  *    as published by the Free Software Foundation.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Affero General Public License for more details.
  *
  *    You should have received a copy of the GNU Affero General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mongoste.core.impl.mongodb;
 
 import org.mongoste.core.AbstractStatsEngine;
 import org.mongoste.core.DuplicateEventException;
 import org.mongoste.core.StatsEngineException;
 import org.mongoste.core.TimeScope;
 import org.mongoste.model.StatEvent;
 import org.mongoste.model.StatAction;
 import org.mongoste.model.StatCounter;
 import org.mongoste.util.DateUtil;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 import com.mongodb.WriteConcern;
 import com.mongodb.WriteResult;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.TreeMap;
 import org.joda.time.DateTime;
 import org.joda.time.MutableDateTime;
 import org.mongoste.query.Query;
 import org.mongoste.query.QueryField;
 import org.mongoste.query.QueryFilter;
 
 /**
  * MongoDB stats engine implementation
  * @author mrmx
  */
 public class MongoStatsEngine extends AbstractStatsEngine {
     private static Logger log = LoggerFactory.getLogger(MongoStatsEngine.class);
 
     private Mongo mongo;
     private DB db;
     private DBCollection events;
     private Map<String,DBCollection> collectionMap;
     private Map<String,String> functionMap;
     private static final DBObject EMPTY_DOC = new BasicDBObject();
     private static final int ERROR_DUPKEY 			= 11000;
     private static final int ERROR_DUPKEY_INSERT	= 11001;
     
     public static final String DB_NAME              = "dbname";
     protected static final String DEFAULT_DB_NAME     = "mongoste";
     protected static final String EVENT_CLIENT_ID     = "_idc";
     protected static final String EVENT_TARGET        = "_idt";
     protected static final String EVENT_TARGET_TYPE   = "_idk";
     protected static final String EVENT_TARGET_OWNERS = "own";
     protected static final String EVENT_TARGET_TAGS   = "tags";
     protected static final String EVENT_ACTION        = "_ida";
     protected static final String EVENT_TIMESTAMP     = "ts";
     protected static final String EVENT_DATE          = "dt";
     protected static final String EVENT_METADATA      = "meta";
     protected static final String TARGET_YEAR         = "y";
     protected static final String TARGET_MONTH        = "m";
     protected static final String TOUCH_DATE          = "t";
     protected static final String ACTION_TARGET       = "target";
 
     protected static final String FIELD_TOTAL         = "total";
     protected static final String FIELD_COUNT         = "count";
     protected static final String FIELD_DAYS          = "days";
     protected static final String FIELD_HOURS         = "hours";
     protected static final String FIELD_HOUR          = "hour";
     protected static final String FIELD_META          = "meta";
 
     protected static final String COLLECTION_EVENTS          = "events";
     protected static final String COLLECTION_TARGETS         = "targets";
     protected static final String COLLECTION_COUNTERS        = "counters";
     protected static final String COLLECTION_TARGET_ACTIONS  = "actions";
     protected static final String COLLECTION_STATS           = "rstats";
 
     protected static final String FN_MAPPER_TARGETS      = "targetMapper";
     protected static final String FN_REDUCER_TARGETS     = "targetReducer";
     protected static final String FN_REDUCER_PLAIN       = "plainReducer";
 
     protected static final String METAKEY_IP             = "ip";
 
     protected static final TimeScope DEFAULT_TIMESCOPE_PRECISION = TimeScope.DAILY;
 
 	
     
     private boolean resetCollections = false; //For testing debug!!!!
     private boolean countEvents = true;
     private boolean keepEvents = true;
 
     public MongoStatsEngine() {
     }
     
     public void setKeepEvents(boolean keepEvents) {
         this.keepEvents = keepEvents;
     }
 
     public boolean isKeepEvents() {
         return keepEvents;
     }
 
     public void setResetCollections(boolean resetCollections) {
         this.resetCollections = resetCollections;
     }
 
     public boolean isResetCollections() {
         return resetCollections;
     }
 
     public void setCountEvents(boolean countEvents) {
         this.countEvents = countEvents;
     }
     
     public boolean isCountEvents() {
         return countEvents;
     }
     
     @Override
     public void init(Properties properties) throws StatsEngineException {
     	log.info("Mongo Stats Engine initialization: {}",properties);
         String host = properties.getProperty("host","localhost");
         int port = -1;
         try {
             port = Integer.parseInt(properties.getProperty("port","-1"));
             mongo = port != -1 ? new Mongo(host, port) : new Mongo(host);
         } catch (Throwable ex) {
             throw new StatsEngineException("Initializing mongo connection",ex);
         }
         String dbName = properties.getProperty(DB_NAME,DEFAULT_DB_NAME);
         try {
             db = mongo.getDB(dbName);
         }catch(Throwable ex) {
             throw new StatsEngineException("Getting db "+dbName,ex);
         }
         setKeepEvents(Boolean.valueOf(properties.getProperty("events.keep", "true")));
         setCountEvents(Boolean.valueOf(properties.getProperty("events.count", "true")));
         setTimeScopePrecision(properties.getProperty("precision", DEFAULT_TIMESCOPE_PRECISION.name()));
         initCollections();
         initFunctions();
     }
     
     @Override
     public void handleEvent(StatEvent event) throws StatsEngineException {
         checkEvent(event);
         if(isKeepEvents()) {
             saveEvent(event);
         }
         if(countEvents){
             //Count event
             if(countRawTarget(event)) {
                 //Global count event
                 countTarget(event);
                 //Count total actions/targets
                 countTargetActions(event);
             } 
         }
     }
 
     @Override
     public List<TimeScope> getSupportedTimeScopePrecision() {
         return Arrays.asList(TimeScope.MONTHLY,TimeScope.DAILY,TimeScope.HOURLY);
     }
 
     @Override
     public void buildStats(TimeScope scope,TimeScope groupBy) {
         TimeScope mapperScope = groupBy;
         if(mapperScope == null) {
             mapperScope = getTimeScopePrecision();
         }
         switch (mapperScope)  {
             case GLOBAL:
             case ANNUAL:
             case WEEKLY:            
                 mapperScope = getTimeScopePrecision();
         }
         String map = getFunction(FN_MAPPER_TARGETS,mapperScope);
         String red = getFunction(FN_REDUCER_TARGETS);
         DateTime now = DateUtil.getDateTimeUTC();
         String statsResultCollection = getScopeCollectionName(COLLECTION_STATS,now.toDate(), scope);
         DBObject queryTargets = EMPTY_DOC; //TODO
         try {            
             getTargetCollection().mapReduce(map, red, statsResultCollection, queryTargets);
         } catch (StatsEngineException ex) {
             log.error("Map reducing targets",ex);
         }
     }
 
 
 
     @Override
     public List<StatAction> getActions(Query query) throws StatsEngineException {
         List<StatAction> actions = new ArrayList<StatAction>();
         try {
             DBCollection targetActions = getTargetActionsCollection();
             DBObject queryDoc = EMPTY_DOC;
             QueryFilter filter = query.getFilter(QueryField.CLIENT_ID);
             if(filter != null && !filter.isEmpty())  {
                 queryDoc = MongoUtil.createDoc(EVENT_CLIENT_ID,filter.getValue());
             }
             DBCursor dbc = targetActions.find(queryDoc,MongoUtil.createDoc(EVENT_ACTION,1,FIELD_TOTAL,1,ACTION_TARGET,1));
             if(query.getMaxResults() != null) {
                 dbc.limit(query.getMaxResults());
             }
             DBObject resultAction,resultTargets,resultTarget;
             String actionName;
             Long count;
             StatAction action;
             while(dbc.hasNext()) {
                 resultAction = dbc.next();
                 actionName = String.valueOf(resultAction.get(EVENT_ACTION));
                 count = ((Number)resultAction.get(FIELD_TOTAL)).longValue();
                 action = new StatAction(actionName,count);
                 actions.add(action);
                 //Add targets
                 resultTargets = (DBObject) resultAction.get(ACTION_TARGET);
                 for(String targetName : resultTargets.keySet()) {
                     resultTarget = (DBObject) resultTargets.get(targetName);
                     count = ((Number)resultTarget.get(FIELD_COUNT)).longValue();
                     action.getTargets().add(new StatCounter(targetName, count));
                 }
             }
         }catch(Exception ex) {
             log.error("getActions",ex);
             throw new StatsEngineException("getActions", ex);
         }
         return actions;
     }
 
     @Override
     public List<StatCounter> getTopTargets(Query query) throws StatsEngineException {
         List<StatCounter> result = new ArrayList<StatCounter>();
         try {
             DBCollection counters = getCounterCollection();
             DBObject queryDoc = MongoUtil.createDoc(
                     EVENT_CLIENT_ID , getQueryValue(query,QueryField.CLIENT_ID),
                     EVENT_TARGET_TYPE,getQueryValue(query,QueryField.TARGET_TYPE)
             );
             String actionCountPath = createDotPath(
                     EVENT_ACTION , getQueryValue(query,QueryField.ACTION),
                     FIELD_COUNT
             );
             DBObject order = MongoUtil.createDoc(actionCountPath,getQueryOrder(query));
             log.debug("Ensuring index for {}",order);
             counters.ensureIndex(order);
             log.debug("Querying counters");
             DBCursor dbc = counters.find(queryDoc,MongoUtil.createDoc(EVENT_TARGET,1,EVENT_ACTION,1));
             Integer limit = query.getMaxResults();
             dbc = dbc.sort(order).limit(limit == null ? 10 : limit);
             BasicDBObject counter;
             String target;
             Long count;
             while(dbc.hasNext()) {
                 counter = (BasicDBObject) dbc.next();
                 target = String.valueOf(counter.get(EVENT_TARGET));
                 count = MongoUtil.getChildDBObject(counter,actionCountPath,2).getLong(FIELD_COUNT);
                 result.add(new StatCounter(target, count));
             }
         }catch(Exception ex) {
             log.error("getTopTargets",ex);
             throw new StatsEngineException("getTopTargets", ex);
         }
         return result;
     }
 
     @Override
     public Map<String,Long> getTargetActionCount(Query query) throws StatsEngineException {
         DBObject queryDoc = MongoUtil.createDoc(
             EVENT_CLIENT_ID , getQueryValue(query,QueryField.CLIENT_ID),
             EVENT_TARGET_TYPE,getQueryValue(query,QueryField.TARGET_TYPE)            
         );
         Object target = getQueryValue(query,QueryField.TARGET);
         if(target != null){
             queryDoc.put(EVENT_TARGET, target);
         }         
         Object owners = getQueryValue(query,QueryField.TARGET_OWNER);
         if(owners != null){
             queryDoc.put(EVENT_TARGET_OWNERS, owners);
         }
         Object tags = getQueryValue(query,QueryField.TARGET_TAGS);
         if(tags != null){
             queryDoc.put(EVENT_TARGET_TAGS, tags);
         }
         return getActionCount(queryDoc);
     }
 
     @Override
     public List<StatAction> getTargetStats(Query query) throws StatsEngineException {
         DBObject queryDoc = MongoUtil.createDoc(
             EVENT_CLIENT_ID , getQueryValue(query,QueryField.CLIENT_ID),
             EVENT_TARGET_TYPE,getQueryValue(query,QueryField.TARGET_TYPE),
             EVENT_TARGET,     getQueryValue(query,QueryField.TARGET)
         );
         QueryFilter actionFilter = query.getFilter(QueryField.ACTION);
         if(actionFilter != null) {
         	queryDoc.put(EVENT_ACTION,getQueryValue(query,QueryField.ACTION));
         }
         QueryFilter dateFromFilter = query.getFilter(QueryField.DATE_FROM);
         DateTime dtFrom = null;
         if(dateFromFilter != null) {            
             dtFrom = dateFromFilter.getDateTimeValue();
         }
         QueryFilter dateToFilter = query.getFilter(QueryField.DATE_TO);
         DateTime dtTo = null;
         if(dateToFilter != null) {            
             dtTo = dateToFilter.getDateTimeValue();
         }        
         if(dtFrom == null) {
             //Set to date
             queryDoc.put(EVENT_DATE,new BasicDBObject("$lte", dtTo.toDate()));            
         } else {
             if(dtTo == null) {
                 //Set from date
                 queryDoc.put(EVENT_DATE,new BasicDBObject("$gte", dtFrom.toDate()));
             } else {
                 if(dtTo.isBefore(dtFrom)) {
                     DateTime dt = dtFrom;
                     dtFrom = dtTo;
                     dtTo = dt;
                 }
                 //Set from-to date
                 queryDoc.put(EVENT_DATE,
                         new BasicDBObject("$gte", dtFrom.toDate()).append("$lte",dtTo.toDate())
                 );
             }
         }        
         //TODO getTargetStats(queryDoc,query.getPrecision() == TimeScope.DAILY) to handle day level
         //TODO or better: getTargetStats(queryDoc,query.getPrecision()) to handle hourly,daily,monthly (default) precision
         return getTargetStats(queryDoc);
     }
 
     private List<StatAction> getTargetStats(DBObject query) throws StatsEngineException {
         List<StatAction> result = new ArrayList<StatAction>();
         DBCursor dbc = null;
         try {
             log.debug("Querying targets");
             DBCollection targets = getTargetCollection();
             long t = System.currentTimeMillis();            
             DBObject fields = MongoUtil.createDoc(
                     EVENT_ACTION,1,
                     FIELD_COUNT,1,
                     EVENT_DATE,1
             );
             dbc = targets.find(query,fields);
             t = System.currentTimeMillis() - t;            
             if(t > 1000) {
                 log.warn("getTargetStats query: {}\n took {}s", debugTrim(query), t / 1000.0);
             }
             BasicDBObject resultDoc;
             Map<String,StatAction> actions = new HashMap<String,StatAction>();
             Map<String,Map<DateTime,StatCounter>> actionsDate = new HashMap<String,Map<DateTime,StatCounter>>();
             Map<DateTime,StatCounter> dateCount;
             StatAction action;
             StatCounter dateCounter;
             String actionName;
             Long count;
             MutableDateTime dateTime = DateUtil.getDateTimeUTC(true).toMutableDateTime();
             DateTime date;
             Date eventYearMonthTargetDate;
             int processed = 0;
             t = System.currentTimeMillis();
             while(dbc.hasNext()) {
                 resultDoc = (BasicDBObject) dbc.next();
                 actionName = resultDoc.getString(EVENT_ACTION);
                 count = resultDoc.getLong(FIELD_COUNT);
                 eventYearMonthTargetDate = (Date) resultDoc.get(EVENT_DATE);
                 dateTime.setDate(eventYearMonthTargetDate.getTime());
                 date = dateTime.toDateTime();
                 action = actions.get(actionName);
                 if(action == null) {
                     actions.put(actionName,action = new StatAction(actionName,0));
                 }
                 action.add(count);
                 dateCount = actionsDate.get(actionName);
                 if(dateCount == null) {
                     dateCount = new TreeMap<DateTime,StatCounter>();
                     actionsDate.put(actionName, dateCount);
                 }
                 dateCounter = dateCount.get(date);
                 if(dateCounter == null) {
                     dateCount.put(date, dateCounter = new StatCounter(actionName, 0, date.toDate()));
                 }
                 dateCounter.add(count);
                 processed++;
             }
             //Build result list
             for(Entry<String,StatAction> entry : actions.entrySet()) {
                 action = entry.getValue();
                 dateCount = actionsDate.get(action.getName());
                 List<StatCounter> targetList = action.getTargets();
                 for(Entry<DateTime,StatCounter> entryDate : dateCount.entrySet()) {
                     StatCounter counter = entryDate.getValue();
                     targetList.add(counter);
                 }
                 result.add(action);
             }
             t = System.currentTimeMillis() - t;
             //TODO add warning level to X ms:
             if(t > 1000) {
                 log.warn("getTargetStats query fetch: {}\n took {}s", debugTrim(query), t / 1000.0);
             } else {
                 log.info("getTargetStats processed {} results in {}ms", processed, t );
             }
         }catch(Exception ex) {
             log.error("getTargetStats",ex);
             if(ex instanceof StatsEngineException) {
                 throw (StatsEngineException)ex;
             }
             throw new StatsEngineException("getTargetStats", ex);
         } finally {
             MongoUtil.close(dbc);
         }
         return result;
     }
 
     private Map<String,Long> getActionCount(DBObject query) throws StatsEngineException {
         Map<String,Long> result = new HashMap<String, Long>();
         DBCursor dbc = null;
         try {
             log.debug("Querying counters");
             DBCollection counters = getCounterCollection();
             long t = System.currentTimeMillis();
             dbc = counters.find(query,MongoUtil.createDoc(EVENT_ACTION,1));
             t = System.currentTimeMillis() - t;
             if(t > 1000) {
                 log.warn("getActionCount query: {}\n took {}s", debugTrim(query), t / 1000.0);
             }
             BasicDBObject actionCounters,counter;
             String action;
             Long count;
             int processed = 0;
             t = System.currentTimeMillis();
             while(dbc.hasNext()) {
                 actionCounters = (BasicDBObject) dbc.next();
                 actionCounters = (BasicDBObject) actionCounters.get(EVENT_ACTION);
                 for(Map.Entry entry : actionCounters.entrySet()) {
                     action = entry.getKey().toString();
                     count = result.get(action);
                     if(count == null) {
                         count = 0L;
                     }
                     counter = (BasicDBObject) entry.getValue();
                     count += counter.getLong(FIELD_COUNT);
                     result.put(action,count);
                 }
                 processed++;
             }
             t = System.currentTimeMillis() - t;
             if(t > 1000) {
                 log.warn("getActionCount query fetch: {}\n took {}s", debugTrim(query), t / 1000.0);
             } else {
                 log.info("getActionCount processed {} results in {}ms", processed, t );
             }
         }catch(Exception ex) {
             log.error("getActionCount",ex);
             if(ex instanceof StatsEngineException) {
                 throw (StatsEngineException)ex;
             }
             throw new StatsEngineException("getActionCount", ex);
         } finally {
             MongoUtil.close(dbc);
         }
         return result;
     }
 
     @Override
     public void setTargetOwners(String clientId,String targetType,List<String> targets,List<String> owners) throws StatsEngineException {
         log.info("setTargetOwners for client: {} target type: {}\ntargets: {}\nowners: {}",new Object[]{clientId,targetType,targets,owners});
         //Find targets and upsert owners field
         BasicDBObject q = new BasicDBObject();
         q.put(EVENT_CLIENT_ID,clientId);
         q.put(EVENT_TARGET_TYPE,targetType);
         putSingleInDoc(q,EVENT_TARGET,targets);        
 
         BasicDBObject doc = new BasicDBObject();
         doc.put("$set", createSetOwnersTagsDoc(owners,null,false));
         
         WriteResult wsTargets = getTargetCollection().update(q,doc,true,true);
         WriteResult wsCounters = getCounterCollection().update(q,doc,true,true);
         //log.debug("setTargetOwners result: {}",wsTargets.getLastError());
     }
 
     @Override
     public void setTargetTags(String clientId,String targetType,String target,List<String> tags) throws StatsEngineException {
         log.info("setTargetTags for client: {} target type: {} target: {} tags: {}",new Object[]{clientId,targetType,target,tags});
         //Find targets and upsert tags field
         BasicDBObject q = new BasicDBObject();
         q.put(EVENT_CLIENT_ID,clientId);        
         q.put(EVENT_TARGET_TYPE,targetType);
         q.put(EVENT_TARGET,target);
 
         BasicDBObject doc = new BasicDBObject();
         doc.put("$set", createSetOwnersTagsDoc(null,tags,false));
         
         WriteResult wsTargets = getTargetCollection().update(q,doc,true,true);
         WriteResult wsCounters = getCounterCollection().update(q,doc,true,true);
         //log.debug("setTargetTags result: {}",ws.getLastError());
     }
 
 
     protected String getScopeCollectionName(String prefix,Date date, TimeScope timeScope) {
         StatEvent event = new StatEvent();
         event.setDate(date);
         return getScopeCollectionName(prefix,event,timeScope);
     }
 
     protected String getScopeCollectionName(String prefix,StatEvent event, TimeScope timeScope) {
         if(TimeScope.GLOBAL.equals(timeScope) || timeScope == null) {
             return prefix;
         }
         String name = prefix + "_" +  timeScope.getKey() + event.getYear();
         if(timeScope == TimeScope.WEEKLY) {
             name = name + "_" + event.getWeek();
         } else {
             if(timeScope == TimeScope.MONTHLY || timeScope == TimeScope.DAILY || timeScope == TimeScope.HOURLY ) {
                 name = name + "_" + event.getMonth();
             }
             if(timeScope == TimeScope.DAILY || timeScope == TimeScope.HOURLY ) {
                 name = name + "_" + event.getDay();
             }
             if(timeScope == TimeScope.HOURLY ) {
                 name = name + "_" + event.getHour();
             }
         }
         return name;
     }
 
     protected void checkEvent(StatEvent event) throws StatsEngineException {
         if(event == null) {
             throw new StatsEngineException("null event");
         }
         if(StringUtils.isBlank(event.getClientId())) {
             throw new StatsEngineException("empty event client-id");
         }
         if(StringUtils.isBlank(event.getAction())) {
             throw new StatsEngineException("empty event action");
         }
         if(StringUtils.isBlank(event.getTarget())) {
             throw new StatsEngineException("empty event target");
         }
         if(StringUtils.isBlank(event.getTargetType())) {
             throw new StatsEngineException("empty event target type");
         }
     }
 
     private void saveEvent(StatEvent event) throws StatsEngineException {
         BasicDBObject doc = new BasicDBObject();
         doc.put(EVENT_CLIENT_ID,event.getClientId());
         doc.put(EVENT_TARGET,event.getTarget());
         doc.put(EVENT_TARGET_TYPE,event.getTargetType());
         doc.put(EVENT_ACTION,event.getAction());
        doc.put(EVENT_DATE,event.getDate());        
        doc.put(EVENT_METADATA,new BasicDBObject(event.getMetadata()));
         WriteResult ws = events.insert(doc);
         log.debug("saveEvent result: {}",ws.getLastError());
     }
 
     @SuppressWarnings("finally")
 	private boolean countRawTarget(StatEvent event)  throws StatsEngineException {
     	boolean processed = false;    	
         try {
             BasicDBObject q = new BasicDBObject();
             q.put(EVENT_CLIENT_ID,event.getClientId());
             q.put(EVENT_TARGET,event.getTarget());
             q.put(EVENT_TARGET_TYPE,event.getTargetType());
             q.put(EVENT_ACTION,event.getAction());
             q.put(EVENT_DATE,event.getYearMonthDate().toDate());
             q.put(TARGET_YEAR, event.getYear());
             q.put(TARGET_MONTH, event.getMonth());
 
             BasicDBObject doc = new BasicDBObject();
 
             //BasicDBObject docSet = new BasicDBObject();
             doc.put("$addToSet", createAddToSetOwnersTagsDoc(event));
             BasicDBObject incDoc = new BasicDBObject();
             incDoc.put(FIELD_COUNT, 1); //Month count
             String metaBaseKey = "";
             TimeScope precision = getTimeScopePrecision();
             if(precision == TimeScope.DAILY || precision == TimeScope.HOURLY) {
                 String dayKey = createDotPath(FIELD_DAYS , event.getDay());
                 incDoc.put(createDotPath(dayKey ,FIELD_COUNT),1); //Day count
                 if(precision == TimeScope.HOURLY) {
                     String hourKey = createDotPath(dayKey ,FIELD_HOURS , event.getHour());
                     incDoc.put(createDotPath(hourKey,FIELD_COUNT), 1);//Hour count
                     metaBaseKey = hourKey;
                 } else {
                     metaBaseKey = dayKey;
                 }
             }            
             //Count metadata
             Map<String,Object> metadata = event.getMetadata();
             for(String metaKey : metadata.keySet()) {
                 incDoc.put(createDotPath(metaBaseKey ,FIELD_META , metaKey ,metaKeyValue(metaKey, metadata.get(metaKey) )),1);
             }
             doc.put("$inc",incDoc);
             DBCollection targets = getTargetCollection(event,TimeScope.GLOBAL);
             //TODO externalize write concern to configuration properties:
             WriteResult wr = targets.update(q,doc,true,true,WriteConcern.FSYNC_SAFE); 
             processed = wr.getN() > 0;
         }catch(MongoException ex) {
         	int errorCode = ex.getCode();
         	if(errorCode == ERROR_DUPKEY || errorCode == ERROR_DUPKEY_INSERT) {
         		throw new DuplicateEventException("Duplicate event " + event);
         	}    	
         	throw new StatsEngineException("countRawTarget failed",ex);
         } 
         return processed;        
     }
 
     private void countTarget(StatEvent event) throws StatsEngineException {
         BasicDBObject q = new BasicDBObject();
         q.put(EVENT_CLIENT_ID,event.getClientId());
         q.put(EVENT_TARGET,event.getTarget());
         q.put(EVENT_TARGET_TYPE,event.getTargetType());
         String actionKey = createDotPath(EVENT_ACTION,event.getAction());
 
         BasicDBObject doc = new BasicDBObject();
         doc.put("$addToSet", createAddToSetOwnersTagsDoc(event));
         BasicDBObject docSet = new BasicDBObject();
         docSet.put(createDotPath(actionKey,TOUCH_DATE), DateUtil.getDateTimeUTC().toDate());
         doc.put("$set", docSet);
         BasicDBObject incDoc = new BasicDBObject();
         incDoc.put(createDotPath(actionKey,FIELD_COUNT), 1); //Global count
         doc.put("$inc",incDoc);
         DBCollection targets = getCounterCollection(event,TimeScope.GLOBAL);
         WriteResult ws = targets.update(q,doc,true,false);
         //log.debug("countTarget result: {}",ws.getLastError());
     }
 
     private void countTargetActions(StatEvent event) throws StatsEngineException {
         BasicDBObject q = new BasicDBObject();
         q.put(EVENT_CLIENT_ID,event.getClientId());
         q.put(EVENT_ACTION,event.getAction());
         BasicDBObject doc = new BasicDBObject();
         BasicDBObject docSet = new BasicDBObject();
         docSet.put(TOUCH_DATE, DateUtil.getDateTimeUTC().toDate());
         //docSet.put(TARGET_DATE, event.getDate());
         doc.put("$set", docSet);
         BasicDBObject incDoc = new BasicDBObject();
         incDoc.put(FIELD_TOTAL,1);
         incDoc.put(createDotPath(ACTION_TARGET,event.getTargetType(),FIELD_COUNT) ,1);
         doc.put("$inc",incDoc);
         DBCollection targetActions = getTargetActionsCollection();
         WriteResult ws = targetActions.update(q,doc,true,false);
         //log.debug("countTarget actions result: {}",ws.getLastError());
     }
 
 
     private void initCollections() throws StatsEngineException {
         log.info("Initializing collections");
         collectionMap = new HashMap<String,DBCollection>();
         if(resetCollections) {
             MongoUtil.dropCollections(db);
         }
         try {
             log.info("Indexing {} collection",COLLECTION_EVENTS);            
             events = db.getCollection(COLLECTION_EVENTS);
             long t = System.currentTimeMillis();
             MongoUtil.createIndexes(events,
                 EVENT_CLIENT_ID,
                 EVENT_TARGET,
                 EVENT_TARGET_TYPE,
                 EVENT_DATE,
                 EVENT_METADATA
             );
             events.ensureIndex(MongoUtil.createDoc(
                     EVENT_CLIENT_ID,1,
                     EVENT_TARGET,1,
                     EVENT_TARGET_TYPE,1,
                     EVENT_ACTION,1,                    
                     EVENT_DATE,1,
                     EVENT_METADATA,1
                     ),"targetAction",true);
             log.info("Done indexing {} collection in {}ms",COLLECTION_EVENTS,System.currentTimeMillis()-t);
         }catch(MongoException ex) {
             throw new StatsEngineException("creating events indexes", ex);
         }
     }
 
     protected DBCollection getStatsCollection() throws StatsEngineException {
         return getStatsCollection((StatEvent)null, TimeScope.GLOBAL);
     }
 
     protected DBCollection getStatsCollection(StatEvent event, TimeScope timeScope) throws StatsEngineException {
         String name = getScopeCollectionName(COLLECTION_STATS, event, timeScope);
         DBCollection stats = collectionMap.get(name);
         if(stats == null) {
             stats = db.getCollection(name);
             if(stats.count() != 0) {
                 try {
                     MongoUtil.createIndexes(stats,"value.count","value.unique");
                 }catch(MongoException ex) {
                     throw new StatsEngineException("creating collection " + name + " indexes", ex);
                 }
             }
             collectionMap.put(name, stats);
         }
         return stats;
     }
 
     protected DBCollection getTargetCollection()  throws StatsEngineException {
         return getTargetCollection((StatEvent)null, TimeScope.GLOBAL);
     }
 
     protected DBCollection getTargetCollection(Date date, TimeScope timeScope) throws StatsEngineException {
         StatEvent event = new StatEvent();
         event.setDate(date);
         return getTargetCollection(event, timeScope);
     }
 
     protected DBCollection getTargetCollection(StatEvent event, TimeScope timeScope) throws StatsEngineException {
         String name = getScopeCollectionName(COLLECTION_TARGETS, event, timeScope);
         DBCollection target = collectionMap.get(name);
         if(target == null) {
             target = db.getCollection(name);
             try {
                 MongoUtil.createIndexes(target,
                     EVENT_CLIENT_ID,
                     EVENT_TARGET,
                     EVENT_TARGET_TYPE,
                     EVENT_TARGET_OWNERS,
                     EVENT_TARGET_TAGS,
                     EVENT_ACTION,
                     EVENT_DATE,
                     TARGET_YEAR,
                     TARGET_MONTH
                 );
                 target.ensureIndex(MongoUtil.createDoc(
                         EVENT_CLIENT_ID,1,
                         EVENT_TARGET,1,
                         EVENT_TARGET_TYPE,1,
                         TARGET_YEAR,1,
                         TARGET_MONTH,1
                         ),"targetYearMonth",false);
                 target.ensureIndex(MongoUtil.createDoc(
                         EVENT_CLIENT_ID,1,
                         EVENT_TARGET,1,
                         EVENT_TARGET_TYPE,1,
                         EVENT_ACTION,1,
                         TARGET_YEAR,1,
                         TARGET_MONTH,1
                         ),"targetActionYearMonth",true);
                 target.ensureIndex(MongoUtil.createDoc(
                         EVENT_CLIENT_ID,1,
                         EVENT_TARGET,1,
                         EVENT_TARGET_TYPE,1,
                         EVENT_ACTION,1,
                         EVENT_DATE,1
                         ),"targetActionDate",true);
                 target.ensureIndex(MongoUtil.createDoc(
                         EVENT_CLIENT_ID,1,
                         EVENT_TARGET,1,
                         EVENT_TARGET_TYPE,1,
                         EVENT_DATE,1
                         ),"targetDate",false);
             }catch(MongoException ex) {
                 throw new StatsEngineException("creating target " + name + " indexes", ex);
             }
             collectionMap.put(name, target);
         }
         return target;
     }
 
     protected DBCollection getCounterCollection() throws StatsEngineException {
         return getCounterCollection((StatEvent)null,TimeScope.GLOBAL);
     }
 
     protected DBCollection getCounterCollection(StatEvent event, TimeScope timeScope) throws StatsEngineException {
         String name = getScopeCollectionName(COLLECTION_COUNTERS, event, timeScope);
         DBCollection target = collectionMap.get(name);
         if(target == null) {
             target = db.getCollection(name);
             try {
                 MongoUtil.createIndexes(target,
                     EVENT_CLIENT_ID,
                     EVENT_TARGET,
                     EVENT_TARGET_TYPE,
                     EVENT_TARGET_OWNERS,
                     EVENT_TARGET_TAGS
                 );
                 target.ensureIndex(MongoUtil.createDoc(
                         EVENT_CLIENT_ID,1,
                         EVENT_TARGET,1,
                         EVENT_TARGET_TYPE,1,
                         EVENT_ACTION,1
                         ),"targetAction",true);
             }catch(MongoException ex) {
                 throw new StatsEngineException("creating target " + name + " indexes", ex);
             }
             collectionMap.put(name, target);
         }
         return target;
     }
 
     protected DBCollection getTargetActionsCollection() throws StatsEngineException {
         String name = COLLECTION_TARGET_ACTIONS;
         DBCollection target = collectionMap.get(name);
         if(target == null) {
             target = db.getCollection(name);
             try {
                 MongoUtil.createIndexes(target,EVENT_CLIENT_ID,EVENT_ACTION);
                 target.ensureIndex(MongoUtil.createDoc(
                         EVENT_CLIENT_ID,1,
                         EVENT_ACTION,1
                         ),"clientTargetActions",true);
             }catch(MongoException ex) {
                 throw new StatsEngineException("creating target " + name + " indexes", ex);
             }
             collectionMap.put(name, target);
         }
         return target;
     }
 
     protected int getQueryOrder(Query query) {
         return query.isOrderAscending() ? 1 : -1;
     }
 
     protected Object getQueryValue(Query query,QueryField field) {
         Object value = null;
         QueryFilter filter = query.getFilter(field);
         if(filter != null) {
             switch(filter.getOperation()) {
                 case IN:
                     value = new BasicDBObject("$in",filter.getValue());
                     break;
                 case EQ:
                 default:
                     value = filter.isEmpty() ? "" : filter.getValue();
                     break;
             }
         }
         return value;
     }
 
 
     protected void dropAllCollections() {
         MongoUtil.dropCollections(db);
     }
 
     protected DBObject debugTrim(DBObject dbo) {
         //TODO trim arrays
         return dbo;
     }
 
 
     private void initFunctions() throws StatsEngineException {
         addFunction(FN_MAPPER_TARGETS,TimeScope.MONTHLY);
         addFunction(FN_MAPPER_TARGETS,TimeScope.HOURLY);
         addFunction(FN_MAPPER_TARGETS,TimeScope.DAILY);
         addFunction(FN_REDUCER_TARGETS);        
         addFunction(FN_REDUCER_PLAIN);        
     }
 
     private void addFunction(String functionNamePrefix,TimeScope scopeSubfix) throws StatsEngineException {
         addFunction(getFunctionName(functionNamePrefix, scopeSubfix));
     }
 
     private void addFunction(String functionName) throws StatsEngineException {
         String functionBody = null;
         InputStream is = null;
         try {
             is = getClass().getResourceAsStream(functionName + ".js");
             if(is == null) {
                 log.error("Function {} not found",functionName);
                 return;
             }
             functionBody = IOUtils.toString(is);
         } catch (IOException ex) {
             throw new StatsEngineException("Loading function " + functionName);
         } finally {
             IOUtils.closeQuietly(is);
         }
         addFunction(functionName, functionBody);
     }
 
     private void addFunction(String functionName,String body) {
         MongoUtil.addDBFunction(db, functionName, body);
         if(functionMap == null) {
             functionMap = new HashMap<String, String>();
         }
         functionMap.put(functionName, body);
     }
 
     private String getFunctionName(String functionNamePrefix,TimeScope scopeSubfix) {
         return functionNamePrefix + scopeSubfix.getKey().toUpperCase();
     }
 
     private String getFunction(String functionNamePrefix,TimeScope scopeSubfix) {
         return getFunction(getFunctionName(functionNamePrefix, scopeSubfix));
     }
 
     private String getFunction(String functionName) {
         return functionMap.get(functionName);
     }
 
     private BasicDBObject createAddToSetOwnersTagsDoc(StatEvent event) {
         return createSetOwnersTagsDoc(event.getTargetOwners(), event.getTargetTags(),true);
     }
 
     private BasicDBObject createSetOwnersTagsDoc(List<String> owners,List<String> tags,boolean addToSet) {
         BasicDBObject doc = new BasicDBObject();
         if(owners != null) {
             doc.append(EVENT_TARGET_OWNERS, addToSet ? new BasicDBObject("$each",owners) : owners);
         }
         if(tags != null) {
             doc.append(EVENT_TARGET_TAGS, addToSet ? new BasicDBObject("$each",tags) : tags);
         }
         return doc;
     }
     
     private void putSingleInDoc(DBObject doc, String key, List values) {
         Object value = createSingleInDoc(values);
         if(value != null) {
             doc.put(key, value);
         }
     }
 
     private Object createSingleInDoc(List values) {
         Object result = null;
         if(values == null || values.isEmpty()) {
             return null;
         }
         if(values.size() == 1) {
            result = values.get(0);
         } else if(values.size() > 1) {
            result = new BasicDBObject("$in", values);
         }
         return result;
     }
 
     private String metaKeyValue(String key, Object value) {
        String resultKey = String.valueOf(value);
         if(METAKEY_IP.equals(key)) {
            String [] parts = resultKey.split("\\.");
             long ipValue = 0L;
             if(parts.length == 4) {
                 //IPv4?
                 try {
                     ipValue = Long.valueOf(parts[3]);
                     ipValue += Long.valueOf(parts[2]) << 8;
                     ipValue += Long.valueOf(parts[1]) << 16;
                     ipValue += Long.valueOf(parts[0]) << 24;
                     return String.valueOf(ipValue);
                 }catch(Exception ex) {
                     log.error("Parsing IPv4 metakey "+key+"="+value,ex);
                 }
             } else {
                 //IPv6?
                 //TODO
                 log.warn("Parsing IPv6 ?? metakey {}={}",key,value);
             }
             
         }
        //Avoid dot notation for json key
        resultKey = resultKey.replace(".", "_");

        return resultKey;
     }
 
 }
