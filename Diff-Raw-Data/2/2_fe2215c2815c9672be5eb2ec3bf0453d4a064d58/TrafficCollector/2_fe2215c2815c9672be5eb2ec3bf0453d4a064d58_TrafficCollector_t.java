 package net.paguo.trafshow.backend.snmp.summary.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Reyentenko
  */
 public class TrafficCollector {
     private Map<String, RouterSummaryTraffic> traffic = new HashMap<String, RouterSummaryTraffic>();
 
     public TrafficCollector(){}
 
     public void addTrafficRecord(TrafficRecord record){
         String key = record.getRouter() + "/" + record.getIface();
         RouterSummaryTraffic summaryTraffic = traffic.get(key);
        if (summaryTraffic == null){
             summaryTraffic = new RouterSummaryTraffic();
             summaryTraffic.setDate(record.getDatetime());
             traffic.put(key, summaryTraffic);
         }
         summaryTraffic.processRecord(record);
     }
 }
