 package bjornno.javabatch;
 
 import java.io.Reader;
 
 
 public class BatchFileScanManager implements Runnable {
 
     private BatchFileReciever reciever;
     private String dir;
 
     public BatchFileScanManager(BatchFileReciever reciever, String dir) {
         this.reciever = reciever;
         this.dir = dir;
     }
 
     public void run() {
         System.out.println("Start scanning for files in: " + dir);
         BatchFileReader bfrdr = new BatchFileReader(dir);
         while (true) {
             Reader rdr = bfrdr.getOneFile();
             if (rdr != null) {
                 reciever.recieve(rdr);
             }
             try {
                Thread.currentThread().sleep(5000);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 }
