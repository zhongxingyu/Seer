 package lesson6;
 
 /**
  * Author: Ilya Varlamov aka privr@tnik
  * Date: 25.03.12
  * Time: 19:28
  */
 
 public class DataBase {
 
     Semaphore readLock;
     Semaphore readWriteLock;
 
     private int readerCount;
 
     public DataBase() {
         readLock = new Semaphore(1);
         readWriteLock = new Semaphore(1);
     }
 
     public void startRead() {
         readLock.P();
            readerCount++;
            if(readerCount == 1) {
                 readWriteLock.P();
             }
        readLock.V();
     }
 
     public void endRead() {
        readLock.P();
             readerCount--;
             if(readerCount == 0) {
               readWriteLock.V();
             }
         readLock.V();
     }
 
     public void startWrite() {
         readWriteLock.P();
     }
 
     public void endWrite() {
         readWriteLock.V();
     }
 
     public static void sleepTime() {
         try {
             Thread.sleep((int)(Math.random() * 1000));
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
 }
