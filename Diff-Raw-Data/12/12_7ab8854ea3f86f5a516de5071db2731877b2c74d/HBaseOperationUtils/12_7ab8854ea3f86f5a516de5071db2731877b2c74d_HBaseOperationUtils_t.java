 package com.impetus.kundera.ycsb.utils;
 
 import java.io.IOException;
 
 import javax.persistence.PersistenceException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 
 import common.Logger;
 
 public final class HBaseOperationUtils
 {
 
     // private final Configuration config = HBaseConfiguration.create(); // new
     // HBaseConfiguration();
 
     private HBaseAdmin admin;
 
     private static Logger logger = Logger.getLogger(HBaseOperationUtils.class);
 
     public HBaseOperationUtils()
     {
         try
         {
             Configuration config = HBaseConfiguration.create();
             admin = new HBaseAdmin(config);
         }
         catch (Exception e)
         {
             throw new PersistenceException(e);
         }
     }
 
     public void deleteTable(String name) throws IOException
     {
         // TableDescriptors desc = admin.getTableDescriptor(name.getBytes());
         // desc.remove(arg0)
         admin.deleteTable(name);
     }
 
     /**
      * Start HBase server.
      * 
      * @param runtime
      *            the runtime
      * @return the process
      * @throws IOException
      *             Signals that an I/O exception has occurred.
      * @throws InterruptedException
      */
     public void startHBaseServer(Runtime runtime, String startHBaseServerCommand) throws IOException,
             InterruptedException
     {
         logger.info("Starting hbase server ...........");
        runtime.exec(startHBaseServerCommand);
        Thread.sleep(10000);
         logger.info("started..............");
     }
 
     /**
      * Stop HBase server.
      * 
      * @param runtime
      *            the runtime
      * @throws IOException
      *             Signals that an I/O exception has occurred.
      * @throws InterruptedException
      */
     public void stopHBaseServer(String stopHBaseServerCommand, Runtime runtime) throws IOException,
             InterruptedException
     {
         logger.info("Stoping hbase server..");
         runtime.exec(stopHBaseServerCommand).waitFor();
         logger.info("stopped..............");
     }
 
     public static void main(String[] args)
     {
         HBaseOperationUtils utils = new HBaseOperationUtils();
         Runtime runtime = Runtime.getRuntime();
         String startHBaseServercommand = "/home/impadmin/software/hbase-0.94.3/bin/start-hbase.sh";
         String stopHBaseServercommand = "/home/impadmin/software/hbase-0.94.3/bin/stop-hbase.sh";
         try
         {
             utils.startHBaseServer(runtime, startHBaseServercommand);
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         try
         {
             utils.stopHBaseServer(stopHBaseServercommand, runtime);
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
