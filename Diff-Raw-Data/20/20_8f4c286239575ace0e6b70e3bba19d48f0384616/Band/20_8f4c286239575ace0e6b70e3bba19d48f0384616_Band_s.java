 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adp.a2.dateiverwaltung;
 
 import adp.a2.Util;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Domani
  */
 public class Band {
     
     //TODO output / FileOutputStrean rausnehmen
     
     int name;
     private File f;
     private RandomAccessFile rFile;
     //private FileOutputStream output;
     private Run aktRun;
     private List<Run> runQueue;
     private String path;
 
 
     public Band(int name,String path){
         this.name = name;
         this.f = new File(path);
         try 
         {
             this.rFile = new RandomAccessFile(f, "rw");
         } 
         catch (FileNotFoundException ex) 
         {
             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         this.path = path;
         
         runQueue = new ArrayList();
         
         if(!f.exists())
         {
             try 
             {
                 f.createNewFile();
             } 
             catch (IOException ex) 
             {
                 Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
        /*
        try 
        {
            output = new FileOutputStream(f, true);
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
        }
         * */
          
     }
     
     public boolean leer(){
         return runQueue.isEmpty() && aktRun == null;
     }
     
     public void addRun()
     {
         aktRun = new Run(0, f.length());
         runQueue.add(aktRun);
     }
     
     public void endRun()
     {
         aktRun = null;
     }
   
     /**
      * @param number 
      */
     public void addNumber(int number) 
     {
         try 
         {
             aktRun.size++;
             rFile.seek(aktRun.endRun);
             rFile.write(Util.intToByte(number));
             aktRun.calcEndPosition();
 
         } 
         catch (IOException ex) 
         {
             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public long size()
     {
         int x = 0;
         for(Run r : runQueue)
         {
            x+= r.size;
         }
         if(aktRun != null) x += aktRun.size;
         return x;
     }
     
     public int getRunCount()
     {
         return runQueue.size() + ((aktRun != null) ? 1 : 0);
     }
     
     private void setNextRun()
     {
         aktRun = (runQueue.size() > 0 ) ? runQueue.remove(0) : null;
     }
     
     public int getNumber()
     {
         return getNumber(true);
     }
 
     /**
      * @param countNumber
      * @return 
      */
     public int getNumber(boolean deleteReadedNumbers)
     {
         if(runFinished()) setNextRun();
         if(aktRun == null) return -1;
         try 
         {
             rFile.seek(aktRun.position);
             byte[] buffer = new byte[4];
             for(int i = 0; i < 4; ++i)
             {
                 buffer[i] = rFile.readByte();
             }
             
             if(deleteReadedNumbers) 
             {
                 aktRun.size--;
                 aktRun.position += 4;
             }
             
             return Util.byteAryToInt(buffer);
         } 
         catch (IOException ex) 
         {
             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
         }
         return -1;
     }
     
     public int getRunSize()
     {
         if(aktRun == null) setNextRun();
        return (aktRun.size == 0) ? 0 : aktRun.size;
     }
     
     public void skipRun()
     {
         aktRun = null;
         setNextRun();
     }
     
     public boolean runFinished()
     {
         if(aktRun == null && runQueue.size() > 0) setNextRun();
         
         if(aktRun != null && aktRun.size == 0)
         {
             setNextRun();
             return true;
         }
         else if(aktRun == null) return true;
         return false;
     }
     
     public void printBand()
     {
         System.out.println("Band " + name + " - [" + getRunCount() + "]");
         int counter = 0;
        List<Run> _tmpQueue = runQueue;
         if(aktRun != null) _tmpQueue.add(aktRun);
         for(Run r : _tmpQueue)
         {
 
             long runPos = r.position;
             int runSize = r.size;
             int[] runNumbers = new int[runSize];
             for(int a = 0; a < runSize; ++a)
             {
                 try 
                 {
                     rFile.seek(runPos);
                     byte[] buffer = new byte[4];
                     for(int i = 0; i < 4; ++i)
                     {
                         buffer[i] = rFile.readByte();
                     }
                     runNumbers[a] = Util.byteAryToInt(buffer);
                     
                     runPos += 4;
                 } 
                 catch (IOException ex) 
                 {
                     Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             
             System.out.println("Run " + counter++ + " " + Arrays.toString(runNumbers));
         }
     }
     
     public void clearBand()
     {
         runQueue.clear();
         try 
         {           
             f.delete();
             
             
             f = null;
 
             
             f = new File(path);
             f.createNewFile();
             
         } 
         catch (IOException ex) 
         {
             Logger.getLogger(Band.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     class Run
     {
         public int size;
         public long position;
         public long endRun;
         
         Run(int size, long position)
         {
             this.size = size;
             this.position = position;
             calcEndPosition();
         }
         
         public void calcEndPosition()
         {
             this.endRun = position + (4 * size);
         }
     }
 }
