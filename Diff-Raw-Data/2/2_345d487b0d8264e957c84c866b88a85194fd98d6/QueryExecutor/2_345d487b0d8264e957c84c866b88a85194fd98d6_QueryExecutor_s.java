 package com.imaginea.mongodb.utils;
 
 import com.imaginea.mongodb.exceptions.ApplicationException;
 import com.imaginea.mongodb.exceptions.DatabaseException;
 import com.imaginea.mongodb.exceptions.ErrorCodes;
 import com.imaginea.mongodb.exceptions.InvalidMongoCommandException;
 import com.mongodb.*;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.*;
 
 /**
  * User: venkateshr
  */
 public class QueryExecutor {
 
     public static JSONObject executeQuery(DB db, DBCollection dbCollection, String command, String queryStr, String fields, String sortByStr, int limit, int skip) throws JSONException, ApplicationException {
         StringTokenizer strtok = new StringTokenizer(fields, ",");
         DBObject keysObj = new BasicDBObject("_id", 1);
         while (strtok.hasMoreElements()) {
             keysObj.put(strtok.nextToken(), 1);
         }
         DBObject sortObj = (DBObject) JSON.parse(sortByStr);
         if (command.equals("count")) {
             return executeCount(dbCollection, queryStr);
         }
         if (command.equals("distinct")) {
             return executeDistinct(dbCollection, queryStr);
         }
         if (command.equals("find")) {
             return executeFind(dbCollection, queryStr, keysObj, sortObj, limit, skip);
         }
         if (command.equals("findOne")) {
             return executeFindOne(dbCollection, queryStr);
         }
         if (command.equals("findAndModify")) {
             return executeFindAndModify(dbCollection, queryStr, keysObj);
         }
         if (command.equals("group")) {
             return executeGroup(dbCollection, queryStr);
         }
         if (command.equals("insert")) {
             return executeInsert(dbCollection, queryStr);
         }
         if (command.equals("mapReduce")) {
             return executeMapReduce(dbCollection, queryStr, limit);
         }
         if (command.equals("update")) {
             return executeUpdate(dbCollection, queryStr);
         }
         if (command.equals("remove")) {
             return executeRemove(dbCollection, queryStr);
         }
         if (command.equals("stats")) {
             return executeStats(dbCollection);
         }
         if (command.equals("drop")) {
             return executeDrop(dbCollection);
         }
         if (command.equals("dropIndex")) {
             return executeDropIndex(dbCollection, queryStr);
         }
         if (command.equals("dropIndexes")) {
             return executeDropIndexes(dbCollection);
         }
         if (command.equals("ensureIndex")) {
             return executeEnsureIndex(dbCollection, queryStr);
         }
         if (command.equals("getIndexes")) {
             return executeGetIndexes(dbCollection);
         }
         throw new InvalidMongoCommandException(ErrorCodes.COMMAND_NOT_SUPPORTED, "Command is not yet supported");
     }
 
     public static JSONObject executeAggregate(DBCollection dbCollection, String queryStr) throws DatabaseException, JSONException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryStr + "]");
         if (queryObj instanceof List) {
             List<DBObject> listOfAggregates = (List) queryObj;
             int size = listOfAggregates.size();
             AggregationOutput aggregationOutput = dbCollection.aggregate(listOfAggregates.get(0), listOfAggregates.subList(1, size).toArray(new DBObject[size]));
             Iterator<DBObject> resultIterator = aggregationOutput.results().iterator();
             List<DBObject> results = dbCollection.getIndexInfo();
             while (resultIterator.hasNext()) {
                 results.add(resultIterator.next());
             }
             return constructResponse(false, results.size(), results);
         }
         throw new DatabaseException(ErrorCodes.INVALID_AGGREGATE_COMMAND, "Aggregate command is ill formed");
     }
 
     public static JSONObject executeCommand(DB db, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         CommandResult commandResult = db.command(queryObj);
         return constructResponse(false, commandResult);
     }
 
     public static JSONObject executeCount(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         long count = dbCollection.count(queryObj);
         return constructResponse(false, new BasicDBObject("count", count));
     }
 
     public static JSONObject executeDistinct(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryStr + "]");
         List distinctValuesList = null;
         if (queryObj.get("1") == null) {
             distinctValuesList = dbCollection.distinct((String) queryObj.get("0"));
         } else {
             distinctValuesList = dbCollection.distinct((String) queryObj.get("0"), (DBObject) queryObj.get("1"));
         }
         return constructResponse(false, distinctValuesList.size(), distinctValuesList);
     }
 
     public static JSONObject executeDrop(DBCollection dbCollection) throws JSONException {
         dbCollection.drop();
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("success", true);
         return jsonObject;
     }
 
     public static JSONObject executeDropIndex(DBCollection dbCollection, String queryStr) throws JSONException, DatabaseException {
         DBObject indexInfo = (DBObject) JSON.parse(queryStr);
         if (indexInfo == null) {
             throw new DatabaseException(ErrorCodes.INDEX_EMPTY, "Index is null");
         }
         dbCollection.dropIndex(indexInfo);
         return executeGetIndexes(dbCollection);
     }
 
     public static JSONObject executeDropIndexes(DBCollection dbCollection) throws JSONException {
         dbCollection.dropIndexes();
         return executeGetIndexes(dbCollection);
     }
 
     public static JSONObject executeEnsureIndex(DBCollection dbCollection, String queryStr) throws JSONException, DatabaseException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryStr + "]");
         DBObject keys = (DBObject) queryObj.get("0");
         if (keys == null) {
             throw new DatabaseException(ErrorCodes.KEYS_EMPTY, "Index Keys are null");
         }
         if (keys.equals("")) {
             throw new DatabaseException(ErrorCodes.KEYS_EMPTY, "Index keys are Empty");
         }
         DBObject options = (DBObject) queryObj.get("1");
         if (options != null) {
             dbCollection.ensureIndex(keys, options);
         } else {
             dbCollection.ensureIndex(keys);
         }
         return executeGetIndexes(dbCollection);
     }
 
     public static JSONObject executeFindOne(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse("[" + queryStr + "]");
         DBObject matchedRecord = dbCollection.findOne((DBObject) queryObj.get("0"), (DBObject) queryObj.get("1"));
         return constructResponse(true, matchedRecord);
     }
 
     public static JSONObject executeFind(DBCollection dbCollection, String queryStr, DBObject keysObj, DBObject sortObj, int limit, int skip) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         DBCursor cursor = dbCollection.find(queryObj, keysObj).sort(sortObj).skip(skip).limit(limit);
         ArrayList<DBObject> dataList = new ArrayList<DBObject>();
         if (cursor.hasNext()) {
             while (cursor.hasNext()) {
                 dataList.add(cursor.next());
             }
         }
         return constructResponse(true, dbCollection.count(queryObj), dataList);
     }
 
     public static JSONObject executeFindAndModify(DBCollection dbCollection, String queryStr, DBObject keysObj) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         DBObject criteria = (DBObject) queryObj.get("query");
         DBObject sort = (DBObject) queryObj.get("sort");
         DBObject update = (DBObject) queryObj.get("update");
         keysObj = queryObj.get("fields") != null ? (DBObject) queryObj.get("") : keysObj;
         boolean returnNew = queryObj.get("new") != null ? (Boolean) queryObj.get("new") : false;
         boolean upsert = queryObj.get("upsert") != null ? (Boolean) queryObj.get("upsert") : false;
         boolean remove = queryObj.get("remove") != null ? (Boolean) queryObj.get("remove") : false;
 
         DBObject queryResult = dbCollection.findAndModify(criteria, keysObj, sort, remove, update, returnNew, upsert);
         return constructResponse(false, queryResult);
     }
 
     public static JSONObject executeGetIndexes(DBCollection dbCollection) throws JSONException {
         List<DBObject> indexInfo = dbCollection.getIndexInfo();
         return constructResponse(false, indexInfo.size(), indexInfo);
     }
 
     public static JSONObject executeInsert(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         WriteResult writeResult;
         if (queryObj instanceof List) {
             writeResult = dbCollection.insert((List<DBObject>) queryObj);
         } else {
             writeResult = dbCollection.insert(queryObj);
         }
         return constructResponse(false, writeResult.getLastError());
     }
 
     public static JSONObject executeGroup(DBCollection dbCollection, String queryString) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryString);
 
         DBObject key = (DBObject) queryObj.get("key");
         DBObject cond = (DBObject) queryObj.get("cond");
         String reduce = (String) queryObj.get("reduce");
         DBObject initial = (DBObject) queryObj.get("initial");
         //There is no way to specify this.
         //DBObject keyf = (DBObject) queryObj.get("keyf");
         String finalize = (String) queryObj.get("finalize");
 
         DBObject groupQueryResult = dbCollection.group(key, cond, initial, reduce, finalize);
         return constructResponse(false, groupQueryResult);
     }
 
     public static JSONObject executeMapReduce(DBCollection dbCollection, String queryString, int limit) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryString);
 
         String map = (String) queryObj.get("map");
         String reduce = (String) queryObj.get("reduce");
         DBObject out = (DBObject) queryObj.get("out");
         DBObject query = (DBObject) out.get("query");
         MapReduceCommand.OutputType outputType = MapReduceCommand.OutputType.REPLACE;
         String outputCollection = null;
         if (out.get("replace") != null) {
             outputCollection = (String) out.get("replace");
             outputType = MapReduceCommand.OutputType.REPLACE;
         } else if (out.get("merge") != null) {
             outputCollection = (String) out.get("merge");
             outputType = MapReduceCommand.OutputType.INLINE;
         } else if (out.get("reduce") != null) {
             outputCollection = (String) out.get("reduce");
             outputType = MapReduceCommand.OutputType.INLINE;
         } else if (out.get("inline") != null) {
             outputType = MapReduceCommand.OutputType.INLINE;
         }
 
         MapReduceCommand mapReduceCommand = new MapReduceCommand(dbCollection, map, reduce, outputCollection, outputType, query);
         if (out != null) {
             mapReduceCommand.setFinalize((String) out.get("finalize "));
             mapReduceCommand.setLimit(limit);
             mapReduceCommand.setScope((Map) out.get("scope"));
             mapReduceCommand.setSort((DBObject) out.get("sort"));
             if (out.get("verbose") != null) {
                 mapReduceCommand.setVerbose((Boolean) out.get("verbose"));
             }
         }
 
         MapReduceOutput mapReduceOutput = dbCollection.mapReduce(mapReduceCommand);
         return constructResponse(false, mapReduceOutput.getCommandResult());
     }
 
     public static JSONObject executeRemove(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         WriteResult result = dbCollection.remove(queryObj);
         return constructResponse(false, result.getLastError());
     }
 
     public static JSONObject executeUpdate(DBCollection dbCollection, String queryStr) throws JSONException, InvalidMongoCommandException {
         String reconstructedUpdateQuery = "{updateQueryParams:[" + queryStr + "]}";
         DBObject queryObj = (DBObject) JSON.parse(reconstructedUpdateQuery);
 
         List queryParams = (List) queryObj.get("updateQueryParams");
         if (queryParams.size() < 2) {
             throw new InvalidMongoCommandException(ErrorCodes.COMMAND_ARGUMENTS_NOT_SUFFICIENT, "Requires atleast 2 params");
         }
         DBObject criteria = (DBObject) queryParams.get(0);
         DBObject updateByValuesMap = (DBObject) queryParams.get(1);
         boolean upsert = false, multi = false;
         if (queryParams.size() > 2) {
             upsert = (Boolean) queryParams.get(2);
             if (queryParams.size() > 3) {
                 multi = (Boolean) queryParams.get(3);
             }
         }
         WriteResult updateResult = dbCollection.update(criteria, updateByValuesMap, upsert, multi);
         CommandResult commandResult = updateResult.getLastError();
         return constructResponse(false, commandResult);
     }
 
     public static JSONObject executeStats(DBCollection dbCollection) throws JSONException {
         CommandResult stats = dbCollection.getStats();
         return constructResponse(false, stats);
     }
 
     private static JSONObject constructResponse(boolean isEditable, long size, List docs) throws JSONException {
         JSONObject result = new JSONObject();
         result.put("documents", docs);
        result.put("count", docs.size());
         result.put("editable", isEditable);
         return result;
     }
 
     private static JSONObject constructResponse(boolean isEditable, DBObject... dbObject) throws JSONException {
         List<DBObject> docs = Arrays.asList(dbObject);
         return constructResponse(isEditable, docs.size(), docs);
     }
 }
