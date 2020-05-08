 /**
  * 
  */
 package com.impetus.kundera.ycsb.runner;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.configuration.Configuration;
 
 import com.impetus.kundera.ycsb.utils.HBaseOperationUtils;
import com.impetus.kundera.ycsb.utils.HibernateCRUDUtils;
 import com.impetus.kundera.ycsb.utils.MailUtils;
 
 /**
  * @author vivek.mishra
  * 
  */
 public class HBaseRunner extends YCSBRunner
 {
 
     public HBaseRunner(String propertyFile, Configuration config)
     {
         super(propertyFile, config);
        crudUtils = new HibernateCRUDUtils();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.impetus.kundera.ycsb.runner.YCSBRunner#startServer(boolean,
      * java.lang.Runtime)
      */
     @Override
     protected void startServer(boolean performDelete, Runtime runTime)
     {
         if(performDelete)
         {/*
             HBaseOperationUtils utils = new HBaseOperationUtils();
             try
             {
                 utils.deleteTable(columnFamilyOrTable);
             }
             catch (IOException e)
             {
                 throw new RuntimeException("Error while deleting data,Caused by:" , e);
             }
             
         */}
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.impetus.kundera.ycsb.runner.YCSBRunner#stopServer(java.lang.Runtime)
      */
     @Override
     protected void stopServer(Runtime runTime)
     {
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.impetus.kundera.ycsb.runner.YCSBRunner#sendMail()
      */
     @Override
     protected void sendMail()
     {
         Map<String, Double> delta = new HashMap<String, Double>();
 
         double kunderaHBaseToNativeDelta = ((timeTakenByClient.get(clients[0]) - timeTakenByClient.get(clients[1]))
                 / timeTakenByClient.get(clients[1]) * 100);
         delta.put("kunderaHBaseToNativeDelta", kunderaHBaseToNativeDelta);
 
         if (kunderaHBaseToNativeDelta > 8.00)
         {
             MailUtils.sendMail(delta, runType, "hbase");
         }
     }
 
 }
