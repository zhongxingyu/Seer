 package plugins.FreePublisher;
 
 import freenet.client.ClientMetadata;
 import freenet.client.HighLevelSimpleClient;
 import freenet.client.InsertBlock;
 import freenet.keys.FreenetURI;
 import freenet.support.api.Bucket;
 import freenet.support.io.ArrayBucket;
 import java.util.Date;
 
 /**
  *
  * @author zapu
  */
 public class UpdateTableJob implements Runnable
 {
     public UpdateTableJob(Publisher publisher)
     {
         this.publisher = publisher;
     }
 
     private final Publisher publisher;
 
     static public final int Interval = 10 * 60 * 1000;
     private boolean terminated = false;
     private boolean working = false;
     private boolean forced = false;
 
     public void run()
     {
         System.err.println("UpdateTableJob starts...");
 
         while(!terminated)
         {
             if(!forced)
             {
                 synchronized(this)
                 {
                     try
                     {
                         wait(Interval);
                     }
                     catch(InterruptedException e)
                     {
 
                     }
                 }
             }
 
             forced = false;
 
             if(terminated)
                 return;
 
             working = true;
             work();
             working = false;
         }
     }
 
     public synchronized void forceRun()
     {
         forced = true;
         notifyAll();
     }
 
     public synchronized void terminate()
     {
         terminated = true;
         notifyAll();
     }
 
     public boolean isWorking()
     {
         return working;
     }
 
     public boolean isForced()
     {
         return forced;
     }
 
     private void work()
     {
         System.err.println("UpdateTableJob work() " + new Date());
 
         publisher.identityLock.lock();
 
         if(publisher.eventTable == null || publisher.identity == null)
         {
             System.err.println("UpdateTableJob end work (identity not loaded) " + new Date());
            return;
         }
 
         if(!publisher.eventTable.isDirty())
         {
             System.err.println("UpdateTableJob end work (eventTable not dirty) " + new Date());
             return;
         }
 
         FreenetURI tableURI;
         ArrayBucket bucket = new ArrayBucket();
 
         try
         {
             tableURI = new FreenetURI("USK@" + publisher.identity.getPrivateKey() + "/events.xml/0");
 
             publisher.eventTable.outputEventTable(bucket.getOutputStream());
         }
         catch(Exception e)
         {
             System.err.println("Error in UpdateTableJob (preparing): " + e);
             return;
         }
         finally
         {
             publisher.identityLock.unlock();
         }
 
         HighLevelSimpleClient HLSL = publisher.getPR().getHLSimpleClient();
 
         try
         {
             HLSL.insert(new InsertBlock(bucket, new ClientMetadata("text/plain"), tableURI), false, null);
         }
         catch(Exception e)
         {
             System.err.println("Error in UpdateTableJob (inserting): " + e);
         }
 
         System.err.println("UpdateTableJob end work " + new Date());
     }
 }
