 package com.trendmicro.tme.mfr;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.protobuf.TextFormat;
 import com.trendmicro.codi.DataListener;
 import com.trendmicro.codi.DataObserver;
 import com.trendmicro.mist.proto.ZooKeeperInfo;
 
 public class BrokerFarm implements DataListener {
    private static Logger logger = LoggerFactory.getLogger(BrokerFarm.class);
     
     private HashMap<String, ZooKeeperInfo.Broker> allBrokers = new HashMap<String, ZooKeeperInfo.Broker>();
     private HashMap<String, ZooKeeperInfo.Loading> allBrokerLoadings = new HashMap<String, ZooKeeperInfo.Loading>();
     private DataObserver obs = null;
     private long lastUpdateTs = 0;
     
    // ////////////////////////////////////////////////////////////////////////////
    public static final String BRK_NODE = "/tme2/broker";
    
    public BrokerFarm() {
        obs = new DataObserver(BRK_NODE, this, true, 0);
         obs.start();
     }
     
     public long getLastUpdateTs() {
         return lastUpdateTs;
     }
     
     public ZooKeeperInfo.Broker getBrokerByHost(String hostname) {
         return allBrokers.get(hostname);
     }
     
     public int getBrokerCount() {
         return allBrokers.size();
     }
     
     public Map<String, ZooKeeperInfo.Loading> getAllLoading() {
         return allBrokerLoadings;
     }
     
     public Map<String, ZooKeeperInfo.Broker> getAllBrokers() {
         return allBrokers;
     }
     
     @Override
     public void onDataChanged(String parentPath, Map<String, byte[]> changeMap) {
         for(Entry<String, byte[]> ent : changeMap.entrySet()) {
             if(ent.getKey().length() == 0)
                 continue;
             else if(ent.getKey().endsWith(".lock"))
                 continue;
             
             String host = ent.getKey();
             boolean isLoading = ent.getKey().endsWith("loading");
             if(isLoading)
                 host = host.substring(0, host.lastIndexOf('/'));
             if(ent.getValue() == null) {
                 if(isLoading)
                     allBrokerLoadings.remove(host);
                 else
                     allBrokers.remove(host);
             }
             else {
                 try {
                     if(isLoading) {
                         ZooKeeperInfo.Loading.Builder builder = ZooKeeperInfo.Loading.newBuilder();
                         TextFormat.merge(new String(ent.getValue()), builder);
                         allBrokerLoadings.put(host, builder.build());
                     }
                     else {
                         ZooKeeperInfo.Broker.Builder builder = ZooKeeperInfo.Broker.newBuilder();
                         TextFormat.merge(new String(ent.getValue()), builder);
                         allBrokers.put(host, builder.build());
                     }
                 }
                 catch(Exception e) {
                     logger.error("Failed to parse broker node " + ent.getKey(), e);
                 }
             }
         }
         lastUpdateTs = new Date().getTime();
     }
 }
