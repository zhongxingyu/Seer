 package examples.TicketReadersWriters;
 
 import java.util.Random;
 
 import examples.util.DoneCounter;
 class Reader extends Thread {
     private DoneCounter doneCounter ;
     ReadersWritersMonitor monitor;
     int numRead;
     int maxReadTime;
     int delay;
 
     public Reader(ReadersWritersMonitor monitor, int numRead, int maxReadTime,
             DoneCounter doneCounter, int delay) {
         this.monitor = monitor;
         this.numRead = numRead;
         this.maxReadTime = maxReadTime;
         this.doneCounter = doneCounter;
        this.delay = delay;
     }
 
     public void run() {
         for(int i = 0; i < numRead; ++i) {
             monitor.startRead();
             // read
             try {
                 if (maxReadTime != 0) {
                     Thread.sleep((long) (Math.random() * maxReadTime) + 1);
                     //Thread.sleep(0, maxReadTime * 1000);
                 }
             }
             catch(InterruptedException e) {
                 
             }
             monitor.endRead();
             if (delay != 0) {
                 try {
                     if (delay >= 1000000) {
                         Thread.sleep(delay / 1000000, delay % 1000000);
                     } else {
                         Thread.sleep(0, delay);
                     }
                 } catch(InterruptedException e) {
                 }
             }
         }
         doneCounter.increment() ;
     }
 }
 
 class Writer extends Thread {
     private DoneCounter doneCounter ;
     ReadersWritersMonitor monitor; 
     int numWrite;
     int maxWriteTime;
    int delay;
 
     public Writer(ReadersWritersMonitor monitor, int numWrite, 
             int maxWriteTime, DoneCounter doneCounter, int delay) {
         this.monitor = monitor;
         this.numWrite = numWrite;
         this.maxWriteTime = maxWriteTime;
         this.doneCounter = doneCounter;
         this.delay = delay;
     }
     public void run() {
         for (int i = 0; i < numWrite; ++i) {
             monitor.startWrite();
             // write
             try {
                 if (maxWriteTime != 0) {
                     //Thread.sleep(0, maxWriteTime * 1000);
                    Thread.sleep((long) (Math.random() * maxWriteTime) + 1);
                 }
             }
             catch(InterruptedException e) {
                 
             }
             monitor.endWrite();
             if (delay != 0) {
                 try {
                     if (delay >= 1000000) {
                         Thread.sleep(delay / 1000000, delay % 1000000);
                     } else {
                         Thread.sleep(0, delay);
                     }
                 } catch(InterruptedException e) {
                 }
             }
         }
         doneCounter.increment() ;
     }
 }
 
 public class TestTicketReadersWriters {
     public static void main (String [] args)
     {
         int READERS = 10;
         int WRITERS = 10;
         int totalNumRead = 10; 
         int totalNumWrite = 10; 
         int maxReadTime = 100;
         int maxWriteTime = 100;
         int delay = 0;
 
         ReadersWritersMonitor monitor = null; 
         try {
             switch(args[0].charAt(0)) {
             case 'e':
                 monitor = new ExplicitReadersWritersMonitor();
                 break;
             default: 
                 monitor = new iMonitorReadersWriters(args[0].charAt(0));
                 break;
             }
             WRITERS = Integer.parseInt(args[1]);
             READERS = 5 * WRITERS;
             totalNumWrite = totalNumRead = Integer.parseInt(args[2]);
             maxReadTime = Integer.parseInt(args[3]);
             maxWriteTime = Integer.parseInt(args[4]);
            delay = Integer.parseInt(args[5]) * 1000;
         } catch (Exception e) { /* use defaults */ 
             e.printStackTrace();
             if(monitor == null) {
                 monitor = new ExplicitReadersWritersMonitor();
             }
         }
         DoneCounter doneCounter = new DoneCounter() ;
         doneCounter.set( READERS + WRITERS ) ;
         long startTime = System.currentTimeMillis();
 
 
         Reader[] r = new Reader[READERS];
         for( int k = 0 ; k < READERS; ++k ) {
             r[k] = new Reader(monitor, totalNumRead/READERS, maxReadTime, 
                     doneCounter, delay) ;
             r[k].start(); 
         }
 
         Writer[] w = new Writer[WRITERS];
         for( int k = 0 ; k < WRITERS; ++k ) {
             w[k] = new Writer(monitor, totalNumWrite/WRITERS, maxWriteTime, 
                     doneCounter, delay) ;
             w[k].start(); 
         }
 
         doneCounter.waitForDone() ;
         
         long execTime = System.currentTimeMillis() - startTime;
         System.out.println( execTime );
     }
 }
