 package com.pms.service.dbhelper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.bson.types.ObjectId;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.QueryBuilder;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.util.ApiUtil;
 
 public class DBQueryUtil {
 
     private static Logger logger = LogManager.getLogger(DBQueryUtil.class);
     
     public static DBObject buildQueryObject(Map<String, Object> parameters, boolean and, boolean mergeClientNameQuery) {
 
         QueryBuilder builder = new QueryBuilder();
         Map<String, Object> cloneParameters = generateQueryFields(parameters, mergeClientNameQuery);
 
         
         Set<String> keys = cloneParameters.keySet();
         for (String key : keys) {
             Object value = cloneParameters.get(key);
 
 //            if (value == null) {// Comment by shihua , for condition is null
 //                continue;
 //            }
             DBObject object = new BasicDBObject();
             if (value instanceof DBQuery) {
                 QueryBuilder childBuilder = new QueryBuilder();
                 childBuilder.put(key);
                 DBQuery dbQuery = (DBQuery) value;
                 if (dbQuery.getOperation() == DBQueryOpertion.IN) {
                     BasicDBList dbList = new BasicDBList();
                     List<Object> arrayValues = new ArrayList<Object>();
                     if(dbQuery.getValue() instanceof String){  
                         String[] s = new String[] {(String) dbQuery.getValue()};
                         for(String v: s){
                             arrayValues.add(v);
                         }
                     }else if(dbQuery.getValue() instanceof String[]){
                         String[] s = (String[]) (dbQuery.getValue());
                         for(String v: s){
                             arrayValues.add(v);
                         }
                     }else{
                          arrayValues = (List<Object>) dbQuery.getValue();
                     }
                     for (Object aValue : arrayValues) {
                         if (key == ApiConstants.MONGO_ID) {
                             dbList.add(new ObjectId(aValue.toString()));
                         } else {
                             dbList.add(aValue);
                         }
                     }
                     childBuilder.in(dbList);
                     object = childBuilder.get();
 
                 } else if (dbQuery.getOperation() == DBQueryOpertion.NOT_IN){
                     BasicDBList dbList = new BasicDBList();
                    List<Object> arrayValues = (List<Object>) dbQuery.getValue();
                     for (Object aValue : arrayValues) {
                         if (key == ApiConstants.MONGO_ID) {
                             dbList.add(new ObjectId(aValue.toString()));
                         } else {
                             dbList.add(aValue);
                         }
                     }
                     childBuilder.notIn(dbList);
                     object = childBuilder.get();
 
                 } else if (dbQuery.getOperation() == DBQueryOpertion.NOT_NULL) {
                     DBObject notNull = new BasicDBObject("$ne", null);
                     object = new BasicDBObject(key, notNull);
                 } else if (dbQuery.getOperation() == DBQueryOpertion.NOT_EQUALS) {
                     DBObject notEquals = new BasicDBObject("$ne", dbQuery.getValue());
                     object = new BasicDBObject(key, notEquals);
                 } else if (dbQuery.getOperation() == DBQueryOpertion.LARGER_THAN) {
                     DBObject notEquals = new BasicDBObject("$gt", dbQuery.getValue());
                     object = new BasicDBObject(key, notEquals);
                 } else if (dbQuery.getOperation() == DBQueryOpertion.LESS_THAN) {
                     DBObject notEquals = new BasicDBObject("$lt", dbQuery.getValue());
                     object = new BasicDBObject(key, notEquals);
                 } else if (dbQuery.getOperation() == DBQueryOpertion.GREATER_THAN_EQUALS) {
                     DBObject largerThanEquals = new BasicDBObject("$gte", dbQuery.getValue());
                     object = new BasicDBObject(key, largerThanEquals);
                 } else if (dbQuery.getOperation() == DBQueryOpertion.LESS_THAN_EQUAILS) {
                     DBObject lessThanEquals = new BasicDBObject("$lte", dbQuery.getValue());
                     object = new BasicDBObject(key, lessThanEquals);
                 } else if (dbQuery.getOperation() == DBQueryOpertion.CASE_INSENSITIVE) {
                     String matchString = "^" + dbQuery.getValue().toString() + "$";
                     Pattern pattern = Pattern.compile(matchString, Pattern.CASE_INSENSITIVE);
                     object = new BasicDBObject(key, pattern);
                 } else if (dbQuery.getOperation() == DBQueryOpertion.BETWEEN_AND) {
                     DBObject betweenAnd = new BasicDBObject();
                     Object[] ta = (Object[]) dbQuery.getValue();
                     Object floor = ta[0];
                     Object top = ta[1];
                     betweenAnd.put("$gte", floor);
                     betweenAnd.put("$lt", top);
                     object.put(key, betweenAnd);
                 } else if (dbQuery.getOperation() == DBQueryOpertion.LIKE) {
 
                     Pattern regex = Pattern.compile(dbQuery.getValue().toString(), Pattern.CASE_INSENSITIVE);
                     QueryBuilder temp = new QueryBuilder();
                     temp.put(key).regex(regex);
                     object = temp.get();
                 }
             } else if (value instanceof DBObject) {
                 object = (DBObject) value;
             } else if (value instanceof String) {
                 String matchString = "^" + value + "$";
                 Pattern pattern = Pattern.compile(matchString, Pattern.CASE_INSENSITIVE);
                 object = new BasicDBObject(key, pattern);
             } else {
                 object.put(key, value);
             }
 
             if (and) {
                 builder.and(object);
             } else {
                 builder.or(object);
             }
 
         }
         DBObject query = builder.get();
         logger.info("Query object: " + query.toMap().toString());
 
         return query;
     
     }
 
     public static DBObject buildQueryObject(Map<String, Object> parameters, boolean and) {        
         return buildQueryObject(parameters, and, false);
     }
     
     public static Map<String, Object> generateQueryFields(Map<String, Object> parameters) {
         
         return generateQueryFields(parameters, false);
     }
 
     public static Map<String, Object> generateQueryFields(Map<String, Object> parameters, boolean mergeClientNameQuery) {
         Map<String, Object> clone = new HashMap<String, Object>();
 
         List<String> ignoreKyes = getDbQueryIgnoreKeys();
         if (parameters != null) {
             Set<String> keys = parameters.keySet();
             for (String key : keys) {
                 if (!ignoreKyes.contains(key)) {
                     Object object = parameters.get(key);
                     if (key.equalsIgnoreCase(ApiConstants.MONGO_ID) && !ApiUtil.isEmpty(object)) {
                         if (object instanceof DBQuery) {
                             clone.put(key, object);
                         } else {
                             try {
                                 clone.put(key, new ObjectId(object.toString()));
                             } catch (IllegalArgumentException e) {
                                 logger.error("Illegal Object id: ".concat(object.toString()), e);
                                 clone.put(key, object);
                             }
                         }
                     } else {
                         clone.put(key, object);
                     }
                 }
             }
         }
 
         return clone;
     }
 
     private static List<String> getDbQueryIgnoreKeys() {
         // remove fields that not need save to data base
         List<String> ignoreKyes = new ArrayList<String>();
         ignoreKyes.add(ApiConstants.LIMIT);
         ignoreKyes.add(ApiConstants.LIMIT_START);
         ignoreKyes.add(ApiConstants.LIMIT_KEYS);
         ignoreKyes.add(ApiConstants.DB_QUERY_ORDER_BY);
         ignoreKyes.add("token");
         
         ignoreKyes.add(ApiConstants.PAGE);
         ignoreKyes.add(ApiConstants.PAGE_SIZE);
         ignoreKyes.add(ApiConstants.SKIP);
         ignoreKyes.add(ApiConstants.TAKE);
         return ignoreKyes;
     }
     
     
     public static Map<String, Object> clearParameterFields(Map<String, Object> parameters){
         Map<String, Object> clone = new HashMap<String, Object>();
         List<String> ignoreKyes = getDbQueryIgnoreKeys();
         ignoreKyes.add(ApiConstants.PATH_INFO);
         ignoreKyes.add(ApiConstants.IP_ADDRESS);
         ignoreKyes.add(ApiConstants.USER_AGENT);
         ignoreKyes.add("accept-encoding");
         if (parameters != null) {
             Set<String> keys = parameters.keySet();
             for (String key : keys) {
                 if (!ignoreKyes.contains(key)) {
                     clone.put(key, parameters.get(key));
                 }
             }
         }
         
         return clone;
     }
     
     
 }
