 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.telefonica.claudia.collectdplugin;
 
 import com.telefonica.tcloud.collectorinterfaces.CollectdName2FQNMap;
 import com.telefonica.tcloud.collectorinterfaces.MonPersistence;
 import com.telefonica.tcloud.collectorinterfaces.MonPersistenceFactory;
 import com.telefonica.tcloud.collectorinterfaces.MonPublisher;
 import com.telefonica.tcloud.collectorinterfaces.MonPublisherFactory;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.collectd.api.Collectd;
 import org.collectd.api.CollectdConfigInterface;
 import org.collectd.api.CollectdFlushInterface;
 import org.collectd.api.CollectdShutdownInterface;
 import org.collectd.api.CollectdWriteInterface;
 import org.collectd.api.OConfigItem;
 import org.collectd.api.OConfigValue;
 import org.collectd.api.ValueList;
 
 /**
  * This is the main class of the plugin
  * 
  * @author jomar
  */
 
 public class ClaudiaCollector implements CollectdConfigInterface, 
         CollectdWriteInterface,CollectdFlushInterface ,
         CollectdShutdownInterface {
     private FileWriter fw=null;
     private MonPersistence persistence=null;
     private MonPublisher publishService=null;
     
     private MeasureTypeTable measureTypes=null;
     private CollectdName2FQNMap conversor=null;
     private MeasuresFilter filter=null;
     private HostFilter hostFilter=null;
     @SuppressWarnings("LeakingThisInConstructor")
     public ClaudiaCollector() throws IOException {
         //fw=new FileWriter("/tmp/datos.txt");
         //fw.write("Antes");
         measureTypes=new MeasureTypeTable();
         //fw.write("Después:");
         //fw.close();
         Collectd.registerWrite("ClaudiaCollector",this);
         Collectd.registerConfig("ClaudiaCollector", this);
         Collectd.registerFlush("ClaudiaCollector", this);
         Collectd.registerShutdown("ClaudiaCollector", this);
         
     }
     
     
     public int write(ValueList vl) {
         String host=vl.getHost();
         String plugin=vl.getPlugin();
         String pluginInstance=vl.getPluginInstance();
         String type=vl.getType();
         String typeInstance=vl.getTypeInstance();
           
         if (hostFilter!=null && !hostFilter.accept(host)) return 0;
         if (filter!=null && !filter.accept(plugin,pluginInstance)) 
             return 0;
           
        String fqn=null;
        
        Iterator<MeasureType> itMT=measureTypes.getMeauresTypeFromDataSources(
                   type,typeInstance,
                   vl.getDataSet().getDataSources()).iterator();
           
        for (Number n: vl.getValues()) {
               MeasureType measureType=itMT.next();
               Date date=new Date(vl.getTime());
               if (measureType==null) continue;
               // only get fqn if the measure is used.
               if (fqn==null) {
                   fqn=conversor.collectd2FQN(host,plugin,
                       pluginInstance,type,typeInstance);
                   if (fqn==null) return 0;
               }
              if (persistence!=null) try {
        
                 persistence.insertData(date, fqn,
                         measureType.getMeasureType(),measureType.getMeasureUnit()
                         ,n);
              } catch (SQLException ex) {
                 Collectd.logError("error saving data to database "+ex.toString());
              }
               if (publishService!=null)
                 publishService.publish(date, fqn, measureType.getMeasureType(),
                       measureType.getMeasureUnit(), n);
         }
         return 0;
     }
     
     public int flush(Number timeout,String identifier) {
         return 0;
     }
     
     public int shutdown() {
         if (persistence!=null) {
           persistence.shutdown();
           persistence=null;
         }
         if (publishService!=null) {
             publishService.shutdown();
             publishService=null;
         }
         conversor.shutdown();
         return 0;
         
     }
 
     private LinkedHashMap<String,String[]> convertConfig(OConfigItem item) {
         LinkedHashMap<String,String[]> hashMap=new LinkedHashMap<String,String[]>();
         String className=null;
         for(OConfigItem item2 : item.getChildren()) {
                List<OConfigValue> valuesList=item2.getValues();
                String values[]=new String[valuesList.size()];
                int i=0;
                for(OConfigValue value: valuesList)
                             values[i++]=value.toString();
                hashMap.put(item2.getKey(), values);
          }
          return hashMap;
 
     }
     public int config(OConfigItem oci) {
         
         for (OConfigItem item : oci.getChildren()) {
             
             if (item.getKey().equalsIgnoreCase("persistence")) {
                 LinkedHashMap<String,String[]> hashmap=convertConfig(item);
                 if (hashmap!=null) {
                   String className[]=hashmap.get("classname");
                   if (className!=null && className.length==1)
                       persistence=MonPersistenceFactory.getPersistence(className[0],
                               hashmap);
                 }
 
             } else if (item.getKey().equalsIgnoreCase("filter")) {
                 String fileName=item.getValues().get(0).getString();
                 try {
                    filter=new MeasuresFilter(fileName);
                 } catch (Exception ex) {
                   Collectd.logError("Problem reading filter file: "+fileName);
                   return -1;
                 } 
             } else if (item.getKey().equalsIgnoreCase("hostFilter")) {
                 String fileName=item.getValues().get(0).getString();
                 try {
                    hostFilter=new HostFilter(fileName);
                 } catch (Exception ex) {
                   Collectd.logError("Problem reading hostFilter file: "+fileName);
                   
                 } 
             } else if (item.getKey().equalsIgnoreCase("namemapper")) { 
                 for(OConfigItem item2 : item.getChildren()) {
                     String className=null;
                     if (item2.getKey().equalsIgnoreCase("classname")) try {
                       className=item2.getValues().get(0).getString();
                       conversor=
                           Class.forName(className).asSubclass(
                                 CollectdName2FQNMap.class).newInstance();
                       LinkedHashMap<String,String[]> hashmap=convertConfig(item);
                       conversor.setConfig(hashmap);
                       break;
                     } catch (Exception e) {
                         Collectd.logError("Problem creating instance of "+
                                 "namemapper plugin "+className);
                         return -1;
                     } 
                 }   
                 
             } else if (item.getKey().equalsIgnoreCase("publishservice")) {
                 LinkedHashMap<String,String[]> hashmap=convertConfig(item);
                 if (hashmap!=null) {
                   String className[]=hashmap.get("classname");
                   if (className!=null && className.length==1)
                       publishService=MonPublisherFactory.getPublisher(className[0],
                               hashmap);
                 }
             }
       
         }
         
         if (conversor==null) 
                     conversor=new CollectdName2FQNDefaultImpl();
         conversor.setMonPersistence(persistence);
         return 0;
         
     }
     
   
     
 }
