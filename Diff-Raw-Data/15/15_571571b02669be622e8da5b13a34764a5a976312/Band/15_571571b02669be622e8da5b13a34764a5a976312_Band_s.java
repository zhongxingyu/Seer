 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adp.a2.dateiverwaltung;
 
 import adp.a2.Util;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Domani
  */
 public class Band {
 
     int name;
     private File f;
     private RandomAccessFile rFile;
 //    private Band.Run aktRun;
     private Map<Integer,Band.Run> runQueue;
     private List<Integer> freeRuns;
     private String path;
     private int runCount=-1;
     private static final int ANZAHL_ZAHLEN_IM_PUFFER = 4;
     private static final int BUFFER_SIZE = ANZAHL_ZAHLEN_IM_PUFFER * 4;
 
     public Band(int name, String path) {
         freeRuns = new ArrayList();
         this.name = name;
         this.f = new File(path);
         try {
             this.rFile = new RandomAccessFile(f, "rw");
         } catch (FileNotFoundException ex) {
             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         this.path = path;
 
         runQueue = new HashMap();
 
         if (!f.exists()) {
             try {
                 f.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public boolean leer() {
         return runQueue.isEmpty();
     }
 
     public int addRun(int runSize) {
         runQueue.put(++runCount,new Band.Run(runSize, f.length(), rFile));
         freeRuns.add(runCount);
         return runCount;
     }
     /**
      * @param number
      */
     public void addNumber(int number, int runId) {
         runQueue.get(runId).addNumber(number);   
     }
 
     public long size() {
         int x = 0;
         for (Band.Run r : runQueue.values()) {
             x += r.size;
         }
         return x;
     }
 
     public int getRunCount() {
         return runQueue.size();
     }
 
     public int getNumber(int runId) {
         if (runFinished(runId)) {
             return -1;
         }
         return runQueue.get(runId).getNumber();//aktRun.getNumber();
     }
 
     
     public int getRunSize(int runId) {
         return runQueue.get(runId).size;
     }
 
     public int skipRun(int runID) {//TODO Skip Run implementieren
         for (int i = runID; i<runCount;++i)
         {
             if(freeRuns.contains(i) &&runQueue.containsKey(i)){                            
                 freeRuns.remove(new Integer(i));
                 return i;
             }
         }
         return -1;
     }
     
     public int skipRun() {//TODO Skip Run implementieren
         return skipRun(0);
     }
     
 
     public boolean runFinished(int runId) {
         return runQueue.get(runId).runFinished();        
     }
 
     public void printBand() {
         System.out.println("Band " + name + " - [" + getRunCount() + "]");
         int counter = 0;
         List<Band.Run> _tmpQueue = new ArrayList();
         _tmpQueue.addAll(runQueue.values());
         for (Band.Run r : _tmpQueue) {
             long runPos = r.position;
             int runSize = r.size;
             int[] runNumbers = new int[runSize];
             for (int a = 0; a < runSize; ++a) {
                 try {
                     rFile.seek(runPos);
                     byte[] buffer = new byte[4];
                     for (int i = 0; i < 4; ++i) {
                         buffer[i] = rFile.readByte();
                     }
                     runNumbers[a] = Util.byteAryToInt(buffer);
                     runPos += 4;
                 } catch (IOException ex) {
                     Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             System.out.println("Run " + counter++ + " " + Arrays.toString(runNumbers));
         }
     }
 
     public void clearBand() {
         runQueue.clear();
         try {
             f.delete();
             f = null;
            f = new File(path);
             f.createNewFile();
         } catch (IOException ex) {
             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     class Run {
 
         public int size;//TODO size final machen
         public final int initialSize;
         public long position;
         public long endRun;
         private boolean linkerBuffer = true;
         private final RandomAccessFile rfile;
         private final List<Byte> buffer1 = new ArrayList<Byte>();
         private final List<Byte> buffer2 = new ArrayList<Byte>();
         
         public boolean runFinished()
         {
             if(position == endRun && buffer1.isEmpty() && buffer2.isEmpty())//TODO Jan dazu befragen!
                 return true;
             return false;
         }
 
         public int getNumber() {
             synchronized (buffer1) {
                 if (buffer1.isEmpty()) {
                     linkerBuffer = false;
                     readNumberFromFile(buffer1);
                 }
                 buffer1.notifyAll();
             }
             synchronized (buffer2) {
                 if (buffer2.isEmpty()) {
                     linkerBuffer = true;
                     readNumberFromFile(buffer2);
                 }
                 buffer2.notifyAll();
             }
             if (buffer1.isEmpty() && buffer2.isEmpty()) {
                 return -1;
             }
             byte[] bAry = new byte[4];
             if (linkerBuffer) {
                 synchronized (buffer1) {
                     for (int i = 0; i < 4; i++) {
                         bAry[i] = buffer1.remove(0);
                     }
                     buffer1.notifyAll();
                 }
             } else {
                 synchronized (buffer2) {
                     for (int i = 0; i < 4; i++) {
                         bAry[i] = buffer2.remove(0);
                     }
                     buffer2.notifyAll();
                 }
             }
             size--;
             position += 4;
             return Util.byteAryToInt(bAry);
         }
 
         private void readNumberFromFile(final List<Byte> buff) {
             new Thread() {
                 @Override
                 public void run() {
                    int buffSize = (size < BUFFER_SIZE) ? size : BUFFER_SIZE;
                    buffSize = (buffSize > (size - buffer1.size() - buffer2.size())) ? size - buffer1.size() - buffer2.size() : buffSize;
                     if (buffSize < 0) {
                         return;
                     }
                     synchronized (buff) {
                         while (!buff.isEmpty()) {
                             try {
                                 buff.wait();
                             } catch (InterruptedException ex) {
                                 Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                         byte bAry[] = new byte[buffSize];
                         try {
                             rfile.seek(position);
                             rfile.read(bAry);//TODO Schauen ob es wirklich so viele Zahlen liest!!
                         } catch (IOException ex) {
                             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         for (int i = 0; i < buffSize; i++) {
                             buff.add(bAry[i]);
                         }
                         buff.notifyAll();
                     }
                 }
             }.start();
         }
 
         public void addNumber(int number) {
            int buffSize = (size < BUFFER_SIZE) ? size : BUFFER_SIZE;
             synchronized (buffer1) {
                 if (buffer1.size() == BUFFER_SIZE) {
                     linkerBuffer = false;
                     writeNumberToFile(buffer1);                    
                 }
                 buffer1.notifyAll();
             }
             synchronized (buffer2) {
                 if (buffer2.size() == BUFFER_SIZE) {
                     linkerBuffer = true;
                     writeNumberToFile(buffer2);
                 }
                 buffer2.notifyAll();
             }
             byte[] bAry = Util.intToByte(number);
             if (linkerBuffer) {
                 synchronized (buffer1) {
                     while (buffer1.size()==BUFFER_SIZE) {
                         try {
                             buffer1.wait();
                         } catch (InterruptedException ex) {
                             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                     for (int i = 0; i < 4; i++) {
                         buffer1.add(bAry[i]);
                     }
                     buffer1.notifyAll();
                 }
             } else {
                 synchronized(buffer2)
                 {
                  while(buffer2.size()==BUFFER_SIZE)   {
                         try {
                             buffer2.wait();
                         } catch (InterruptedException ex) {
                             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
                         }
                  }                
                     for (int i = 0; i < 4; i++) {
                         buffer2.add(bAry[i]);
                     }
                 }
                 buffer2.notifyAll();
             }
             size++;
             calcEndPosition();
         }
 
         private void writeNumberToFile(final List<Byte> buff) {
             new Thread() {
                 //int buffSize = (size<BUFFER_SIZE)?size:BUFFER_SIZE;                
                 int buffSize = buff.size();
                 @Override
                 public void run() {
                     synchronized (buff) {
                         try {
                             while (buff.isEmpty()) {
                                 try {
                                     buff.wait();
                                 } catch (InterruptedException ex) {
                                     Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
                                 }
                             }
                             byte[] bAry = new byte[buffSize];
                             for (int i = 0; i < buffSize; i++) {
                                 bAry[i] = buff.remove(0);
                             }
                             rfile.seek(endRun - buffer1.size() - buffer2.size());
                             rfile.write(bAry);
                         } catch (IOException ex) {
                             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         buff.notifyAll();
                     }
                 }
             }.start();
         }
 
        Run(int size, long position, RandomAccessFile rFile) {
             this.rfile = rFile;
            this.size = size;
            this.initialSize=size;
             this.position = position;
             calcEndPosition();
         }
 
         public void calcEndPosition() {
             this.endRun = position + (4 * size);
         }
     }
 }
