 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 //import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.FileNotFoundException;
 import java.io.BufferedInputStream;
 import java.io.File;
 
 /**
  *
  * @author Manu Bhadoria
  */
 public class videoSummarize {
 
     /**
      * @param args the command line arguments
      */
     static RandomAccessFile fos;
     
     public static void main(String[] args) throws FileNotFoundException {
 	try {
             // TODO code application logic here
             // get the command line parameters
             if (args.length < 3) {
                 System.err.println("Invalid number of arguments");
                 return;
             }
             String videoFileName = args[0];
             String audioFileName = args[1];
             double percentage = Double.parseDouble(args[2]);
 
             fos = new RandomAccessFile("/media/New Volume/576_sample/videoOutput.rgb", "rw");
             fos.setLength(0);
             
             //Thread videoShotThread = new Thread(new videoToShots(videoFileName));
             videoToShots vidShot = new videoToShots(videoFileName);
             vidShot.colorHistogram();
 
             AudioLevels al = new AudioLevels(audioFileName) ;
 //                motionDetection.motionDetetcionAlgo();
 //                System.out.println("Main exiting..");
 
//            al.shorten() ;
             al.summarize((int)percentage) ;
 //            videoToShots.printShotHashMap();
             try {
                 fos.close();
             } catch (Exception ex) {
             } 
 
         } catch (IOException ex) {
             Logger.getLogger(videoSummarize.class.getName()).log(Level.SEVERE, null, ex);
 	} 
     }
 }
