 package bjornno.javabatch;
 
 
 public class BatchFileScanScheduler implements Runnable {
 
     private BatchFileReciever reciever;
     private String dir;
 
     public BatchFileScanScheduler(BatchFileReciever reciever, String dir) {
         this.reciever = reciever;
         this.dir = dir;
     }
 
     public void run() {
         BatchFileReader bfrdr = new BatchFileReader(dir);
         while (true) {
            reciever.recieve(bfrdr.getOneFile());
             try {
                 Thread.sleep(5000);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 }
