 // 
 //  Test3.java
 //  Driver for testing CPU bound (TestThread3a) and disk bound (TestThread3b)
 //  threads.
 //  Created by John Hildebrant on 2011-11-13.
 // 
 import java.util.Date;
 
 class Test3 extends Thread {
   private int pairs;
   
   public Test3(String[] args) {
     pairs = Integer.parseInt(args[0]);
   }
   
   public void run() {
    long startTime = new Date().getTime();q
     String[] strArray1 = SysLib.stringToArgs("TestThread3a");
     String[] strArray2 = SysLib.stringToArgs("TestThread3b");
     for (int i = 0; i < pairs; i++) {
       SysLib.exec(strArray1);
       SysLib.exec(strArray2);
     }
     for (int i = 0; i < pairs*2; i++) {
       SysLib.join();
     }
     long endTime = new Date().getTime();
     
     SysLib.cout("elapsed time = " + (endTime - startTime) + " msec.\n");
     SysLib.exit();
   }
 }
