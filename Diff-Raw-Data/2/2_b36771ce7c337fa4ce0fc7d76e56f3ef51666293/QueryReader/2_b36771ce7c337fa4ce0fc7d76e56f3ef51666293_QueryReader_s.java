 package com.xingcloud.util;
 
 import com.google.gson.Gson;
 import com.xingcloud.model.Enumeration;
 import com.xingcloud.model.Index;
 import org.apache.log4j.Logger;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: witwolf
  * Date: 4/25/13
  * Time: 3:29 PM
  * To change this template use File | Settings | File Templates.
  */
 public class QueryReader {
   private static Logger logger = Logger.getLogger(QueryReader.class);
   private Gson gson;
 
   public QueryReader() {
     gson = new Gson();
   }
 
   List<String> times = new ArrayList<String>() {
     {
       add("first_pay_time");
       add("register_time");
     }
   };
 
   public Map<String, Set<Index>> readQueryLog(int dateDistance) {
     Map<String, Map<Index, Integer>> indexesMap = new HashMap<String, Map<Index, Integer>>();
     String fileName = Utility.getQueryLogPath(dateDistance);
     File file = new File(fileName);
     logger.info("Read query log from file : " + fileName);
     String line = null;
     try {
       BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
       while ((line = bufferedReader.readLine()) != null) {
         Pair<String, Index> pair = parse(line);
         if (pair != null) {
           String project = pair.first;
           Index index = pair.second;
 
           Map<Index, Integer> indexes = indexesMap.get(project);
           if (indexes != null) {
             Integer value = indexes.get(index);
             if (value != null) {
               value += 1;
               indexes.put(index, value);
             } else {
               indexes.put(index, 1);
             }
           } else {
             indexes = new HashMap<Index, Integer>();
             indexes.put(index, 1);
             indexesMap.put(project, indexes);
           }
         }
 
       }
       bufferedReader.close();
     } catch (Exception e) {
       logger.error("Read query log from : " + fileName + " failed");
     }
     return Utility.extract(indexesMap);
   }
 
   public Map<String, Set<Index>> readQueryLog() {
     return readQueryLog(-1);
   }
 
   private Pair<String, Index> parse(String json) {
     try {
 
       String[] fileds = json.split(",");
       int length = fileds.length;
       String type = fileds[0];
       String project = fileds[1];
       String startDate = fileds[2];
       String endDate = fileds[3];
       String event = fileds[4];
       String segment = "";
       int coverRange = -DateUtil.getDateDistance(startDate, endDate);
       int coverRangeOrigin = 0;
       int distance = DateUtil.getDateDistance(endDate, DateUtil.getDateByDistance(-1));
       Index index = null;
       boolean first = true;
      if (distance != 0 && !("visit.*".equals(event) || "pay.*".equals(event)))
         return null;
 
       if ("GROUP".equals(type)) {
         String gbv = fileds[length - 1];
         Enumeration.GROUPBY_TYPE gbt = fileds[length - 2].equals("EVENT") ? Enumeration.GROUPBY_TYPE.EVENT : Enumeration.GROUPBY_TYPE.USER_PROPERTIES;
         for (int i = 5; i < length - 3; i++) {
           if (first) {
             first = false;
           } else {
             segment += ",";
           }
           segment += fileds[i];
         }
 
         index = new Index(event, coverRange, coverRangeOrigin, processSegment(distance, segment), gbt, gbv);
 
       } else {
         String interval = fileds[length - 1];
         for (int i = 5; i < length - 2; i++) {
           if (first) {
             first = false;
           } else {
             segment += ",";
           }
           segment += fileds[i];
         }
         Enumeration.INTERVAL intervalenum = Enumeration.INTERVAL.DAY;
         if ("DAY".equals(interval)) {
           intervalenum = Enumeration.INTERVAL.DAY;
         } else if ("MIN5".equals(interval)) {
           intervalenum = Enumeration.INTERVAL.MIN5;
         } else if ("WEEK".equals(interval)) {
           intervalenum = Enumeration.INTERVAL.WEEK;
         } else if ("PERIOD".equals(interval)) {
           intervalenum = Enumeration.INTERVAL.DAY;
         }
 
         index = new Index(intervalenum, event, coverRange, coverRangeOrigin, processSegment(distance, segment));
 
       }
 
       return new Pair<String, Index>(project, index);
 
     } catch (Exception e) {
       logger.error("Parse query to indexs failed ," + json);
     }
     return null;
 
   }
 
   private Object processSegment(int distance, String segmentStr) {
 
     if (!segmentStr.startsWith("{"))
       return segmentStr;
 
     String yesterday = DateUtil.getDateByDistance(-1);
     Map segment = gson.fromJson(segmentStr, Map.class);
     for (String str : times) {
       if (segment.containsKey(str)) {
         if (segment.get(str) instanceof Map) {
           Map<String, String> map = (Map) segment.get(str);
           if (map.containsKey("$gte") && map.containsKey("$lte")) {
             String start = map.get("$gte");
             String end = map.get("$lte");
             int innerDistance = DateUtil.getDateDistance(end, yesterday);
             int seDistance = DateUtil.getDateDistance(start, end);
             end = DateUtil.getDateByDistance(yesterday, distance - innerDistance);
             start = DateUtil.getDateByDistance(end, -seDistance);
             map.put("$gte", start);
             map.put("$lte", end);
 
           }
         }
       }
     }
     return segment;
 
   }
 
 }
