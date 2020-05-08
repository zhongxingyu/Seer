 package com.imaginea.mongodb.utils;
 
 import com.imaginea.mongodb.exceptions.ApplicationException;
 import com.imaginea.mongodb.exceptions.ErrorCodes;
 import com.imaginea.mongodb.exceptions.InvalidMongoCommandException;
 import com.mongodb.*;
 import com.mongodb.util.JSON;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * User: venkateshr
  */
 public class QueryExecutor {
 
     //TODO need to change this implementation to non-static
     public static JSONObject executeQuery(DB db, DBCollection dbCollection, String command, String queryStr, String fields, int limit, int skip) throws JSONException, ApplicationException {
         StringTokenizer strtok = new StringTokenizer(fields, ",");
         DBObject keyObj = new BasicDBObject();
         while (strtok.hasMoreElements()) {
             keyObj.put(strtok.nextToken(), 1);
         }
         if (command.equals("find")) {
             return executeFind(dbCollection, queryStr, keyObj, limit, skip);
         }
         if (command.equals("count")) {
             return executeCount(dbCollection, queryStr);
         }
         if (command.equals("distinct")) {
             return executeDistinct(dbCollection, queryStr);
         }
         if (command.equals("update")) {
             return executeUpdate(dbCollection, queryStr);
         }
         if (command.equals("findAndModify")) {
             return executeFindAndModify(dbCollection, queryStr, keyObj);
         }
         if (command.equals("group")) {
             return executeGroup(dbCollection, queryStr);
         }
        if (command.equals("mapreduce")) {
             return executeMapReduce(dbCollection, queryStr, limit);
         }
         if(command.equals("insert")){
             return  executeInsert(dbCollection, queryStr);
         }
         throw new InvalidMongoCommandException(ErrorCodes.COMMAND_NOT_SUPPORTED, "Command is not yer supported");
     }
 
     public static JSONObject executeFind(DBCollection dbCollection, String queryStr, DBObject keyObj, int limit, int skip) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         DBCursor cursor = dbCollection.find(queryObj, keyObj).limit(limit).skip(skip);
         ArrayList<DBObject> dataList = new ArrayList<DBObject>();
         if (cursor.hasNext()) {
             while (cursor.hasNext()) {
                 dataList.add(cursor.next());
             }
         }
         JSONObject result = new JSONObject();
         result.put("documents", dataList);
         result.put("count", dbCollection.count(queryObj));
         return result;
     }
 
     public static JSONObject executeCount(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         long count = dbCollection.count(queryObj);
         return constructUneditableResult(new BasicDBObject("count", count));
     }
 
     public static JSONObject executeDistinct(DBCollection dbCollection, String queryStr) throws JSONException {
         List distinctValuesList = null;
         if (queryStr.startsWith("{")) {
             DBObject queryObj = (DBObject) JSON.parse(queryStr);
             String key = (String) queryObj.removeField("distinct");
             distinctValuesList = dbCollection.distinct(key, queryObj);
         } else {
             distinctValuesList = dbCollection.distinct(queryStr);
         }
         JSONObject result = new JSONObject();
         result.put("documents", distinctValuesList);
         result.put("count", distinctValuesList.size());
         return result;
     }
 
     public static JSONObject executeFindAndModify(DBCollection dbCollection, String queryStr, DBObject keyObj) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
 
         DBObject criteria = (DBObject) queryObj.get("query");
         DBObject sort = (DBObject) queryObj.get("sort");
         DBObject update = (DBObject) queryObj.get("update");
         keyObj = queryObj.get("fields") != null ? (DBObject) queryObj.get("") : keyObj;
         boolean returnNew = queryObj.get("new") != null ? (Boolean) queryObj.get("new") : false;
         boolean upsert = queryObj.get("upsert") != null ? (Boolean) queryObj.get("upsert") : false;
         boolean remove = queryObj.get("remove") != null ? (Boolean) queryObj.get("remove") : false;
 
         DBObject queryResult = dbCollection.findAndModify(criteria, keyObj, sort, remove, update, returnNew, upsert);
         return constructUneditableResult(queryResult);
     }
 
     public static JSONObject executeUpdate(DBCollection dbCollection, String queryStr) throws JSONException, InvalidMongoCommandException {
         String reconstrcutedUpadteQuery = "{updateQueryParams:[" + queryStr + "]}";
         DBObject queryObj = (DBObject) JSON.parse(reconstrcutedUpadteQuery);
 
         List queryParams = (List) queryObj.get("updateQueryParams");
         if (queryParams.size() < 2) {
             throw new InvalidMongoCommandException(ErrorCodes.COMMAND_ARGUMENTS_NOT_SUFFICIENT, "Requires atleast 2 params");
         }
         DBObject criteria = (DBObject) queryParams.get(0);
         DBObject updateByValuesMap = (DBObject) queryParams.get(1);
         boolean upsert = false;
         boolean multi = false;
         if (queryParams.size() > 2) {
             upsert = (Boolean) queryParams.get(2);
             if (queryParams.size() > 3) {
                 multi = (Boolean) queryParams.get(3);
             }
         }
         WriteResult updateResult = dbCollection.update(criteria, updateByValuesMap, upsert, multi);
         CommandResult commandResult = updateResult.getLastError();
         return constructUneditableResult(commandResult);
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
         return constructUneditableResult(groupQueryResult);
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
         return constructUneditableResult(mapReduceOutput.getCommandResult());
     }
 
     public static JSONObject executeCommand(DB db, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         CommandResult commandResult = db.command(queryObj);
         return constructUneditableResult(commandResult);
     }
 
     public static JSONObject executeInsert(DBCollection dbCollection, String queryStr) throws JSONException {
         DBObject queryObj = (DBObject) JSON.parse(queryStr);
         WriteResult writeResult = dbCollection.insert(queryObj);
         return constructUneditableResult(writeResult.getLastError());
     }
 
     public static JSONObject constructUneditableResult(DBObject dbObject) throws JSONException {
         List<DBObject> docs = new ArrayList<DBObject>();
         docs.add(dbObject);
         JSONObject result = new JSONObject();
         result.put("documents", docs);
         result.put("count", 1);
         return result;
     }
 }
