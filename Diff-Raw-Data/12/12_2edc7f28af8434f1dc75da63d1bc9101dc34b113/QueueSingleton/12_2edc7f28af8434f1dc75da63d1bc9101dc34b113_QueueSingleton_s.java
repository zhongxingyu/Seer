 /*
  * Created by Andrey Markelov 29-08-2012.
  * Copyright Mail.Ru Group 2012. All rights reserved.
  */
 package ru.mail.jira.plugins.mrimsender;
 
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.TimeUnit;
 import ru.mail.jira.plugins.mrimsender.mrim.MRIMPackageFactory;
 
 /**
  * This is m-agent data queue.
  * 
  * @author Andrey Markelov
  */
 public class QueueSingleton
 {
     /**
      * Singleton instance.
      */
     private static QueueSingleton instance = new QueueSingleton();
 
     /**
      * Get singleton instance.
      */
     public static QueueSingleton getInstance()
     {
         return instance;
     }
 
     /**
      * MRIM data queue.
      */
     private LinkedBlockingDeque<DataKeeper> queue;
 
     /**
      * Private constructor.
      */
     private QueueSingleton()
     {
        queue = new LinkedBlockingDeque<DataKeeper>(100);
     }
 
     /**
      * Get data from queue.
      */
     public DataKeeper poll()
     throws InterruptedException
     {
        return queue.poll(5, TimeUnit.SECONDS);
     }
 
     /**
      * Put data to queue.
      */
     public void put(DataKeeper dk)
     {
         queue.push(dk);
     }
 
     /**
      * Put ping data for test.
      */
     public void putPing()
     {
         queue.push(new DataKeeper(MRIMPackageFactory.createPingPackage(0, 0, 0), DataKeeper.PING));
     }
 }
