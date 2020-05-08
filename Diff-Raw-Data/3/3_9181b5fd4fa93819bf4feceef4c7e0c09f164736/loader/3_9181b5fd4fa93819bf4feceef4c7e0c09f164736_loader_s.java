 
 package com.marklogic.community;
 
 import java.util.Random;
 import java.util.Hashtable;
 
 import com.marklogic.client.DatabaseClientFactory.Authentication;
 import com.marklogic.client.DatabaseClientFactory;
 import com.marklogic.client.DatabaseClient;
 import com.marklogic.client.document.XMLDocumentManager;
 import com.marklogic.client.io.DocumentMetadataHandle;
 import com.marklogic.client.io.DocumentMetadataHandle.DocumentCollections;
 import com.marklogic.client.io.JAXBHandle;
 import com.marklogic.client.Transaction;
 
 import javax.xml.bind.JAXBContext;
 import java.util.ArrayList;
 import java.text.DecimalFormat;
 
 import com.marklogic.community.thing;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class loader {
 
     static class MyRunnable implements Runnable {
 
         private int start, end;
 
         MyRunnable(int s, int e) {
             this.start = s;
             this.end = e;
         }
 
         @Override
         public void run() {
 
             Transaction transaction = client.openTransaction();
 
             Hashtable<Class, JAXBHandle> table = new Hashtable<Class, JAXBHandle>();
         
             while (start < end) {
                 Object t = list.get(start);
                 try {
         
                     String uri = "/" + Math.abs(gen.nextInt()) + ".xml";
 
                     JAXBHandle handle = table.get(t.getClass());
         
                     if (handle == null) {
                         JAXBContext docContext = JAXBContext.newInstance(t.getClass());
                         handle = new JAXBHandle(docContext);
                         table.put(t.getClass(), handle);
                     }
         
                     if (!nodb) {
                         if (docsPerTrans == 0) {
                             docMgr.write(uri, dataMetadataHandle, handle.with(t));
                         } else {
                             docMgr.write(uri, dataMetadataHandle, handle.with(t), transaction);
                             counter++;
                             if (counter == docsPerTrans) {
                                 transaction.commit();
                                 transaction = client.openTransaction();
                                 counter = 0;
                             }
                         }
                     }
 
                     start++;
         
                 } catch (Exception e) {
                     transaction.rollback();
                     e.printStackTrace();
                     break;
                 }
             }
 
             if (counter > 0) {
                 transaction.commit();
             }
         }
     }
 
     static public int T = 8;
     static public int N = 2000;
    static public int counter = 0;
     static public int docsPerTrans = 200;
     static public Boolean nodb = false;
     
     static public ArrayList list = new ArrayList();
     static public XMLDocumentManager docMgr;
     static public DocumentMetadataHandle dataMetadataHandle;
     static public Random gen = new Random();
     static public DatabaseClient client;
 
     public static void main(String[] args) throws Exception {
 
         client = DatabaseClientFactory.newClient(
             "localhost", 7006,
             "admin", "adm1n",
             Authentication.DIGEST
         );
 
         ExecutorService executor = Executors.newFixedThreadPool(T);
 
         for(int i = 0; i < N; i++) {
             list.add(thing.random());
         }
 
         docMgr = client.newXMLDocumentManager();
         String collection = "stuff";
 
         dataMetadataHandle = new DocumentMetadataHandle();
 
         DocumentMetadataHandle.DocumentCollections dataCollections = dataMetadataHandle.getCollections();
         dataCollections.add(collection);
         dataMetadataHandle.setCollections(dataCollections);
 
 
         int perT = N/T;
         int start = 0;
 
         long startTime = System.nanoTime();
 
         for (int i = 0; i < T; i++) {
             Runnable worker = new MyRunnable(start, start + perT);
             executor.execute(worker);
             start = start + perT;
         }
 
         executor.shutdown();
 
         while (!executor.isTerminated()) {
         }
 
         double secs = (double)(System.nanoTime() - startTime) / (double)(1000000000);
 
         System.out.println("Config: nodb: " + nodb + ", docsPerTrans: " + docsPerTrans + ", Num Threads: " +  T + ", docsPerThread: " + perT);
         System.out.println("" + N + " things stored took " + new DecimalFormat("#.##").format(secs) + " secs");
         double rate = (double) N / secs;
         System.out.println("That is " + new DecimalFormat("#.##").format(rate) + " docs per second");
     }
 }
