 import java.awt.BorderLayout;
 import java.awt.RenderingHints.Key;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  *
  * @author Manu Bhadoria
  */
 public class videoToShots implements Runnable {
 
     static File file;
     FileInputStream fis;
     //FileOutputStream fos = new FileOutputStream("videoOutput.rgb");
     static int Height = 240;
     static int Width = 320;
     static long numFrames;
     long numShots;
     int shotLength = 15;
     int numBuckets = 10;
     int lowerThreshold = 2;
     int upperThreshold = 7;
     //HashMap<Integer, > keyFrameHashMap = new HashMap<Integer, >();
     static LinkedHashMap<Integer, shotInfo> shotHashMap = new LinkedHashMap<Integer, shotInfo>();
 
     public videoToShots(String fileName) throws FileNotFoundException {
         file = new File(fileName);
         this.fis = new FileInputStream(file);
         numFrames = file.length() / (Height * Width * 3);
         this.numShots = numFrames / 120;
     }
 
     @Override
     public void run() {
         }
     public void colorHistogram() {
         try {
             double yuvArray_1[] = new double[Height * Width * 3];
             double yuvArray_2[] = new double[Height * Width * 3];
             byte frameArray_1[] = new byte[Height * Width * 3];
             byte frameArray_2[] = new byte[Height * Width * 3];
             double entropy_prev, entropy_next, entropyDiff;
             int currShot = 0;
 
             fis.read(frameArray_1, 0, Height * Width * 3);
             yuvArray_1 = convertToYUV(frameArray_1);
             //fis.read(frameArray_2, 0, Height * Width * 3);
             //yuvArray_2 = convertToYUV(frameArray_2);
             //printImage(yuvArray_1);
             entropy_prev = computeEntropy(yuvArray_1);
             //Insert the shot info into HASH MAP
             insertIntoShotHashMap(0, (int)this.shotLength * 24, 0);
             currShot = 0;
             long shotDuration = currShot + (this.shotLength * 24);
             //fis.read(frameArray_2, 0, Height * Width * 3);
             for (int i = 1; i < numFrames; i++) {
                 //System.out.println("\t................Next frame...................... "+i);
                 //printImage(yuvArray_1);
                 fis.read(frameArray_2, 0, Height * Width * 3);
                 //first get YUV for RGB
                 yuvArray_2 = convertToYUV(frameArray_2);
                 //compute Entropy for this YUV Frame
                 entropy_next = computeEntropy(yuvArray_2);
                 //compute diff of entropy with prev so as to determine new shot, key frame or continuos frame
                 entropyDiff = computeDifferenceInEntropy(entropy_prev, entropy_next);
                 //System.out.println("prev: "+ entropy_prev +" next: "+ entropy_next+" Diff in Entropy: "+entropyDiff);
                 if ( entropyDiff > upperThreshold) {
                     //create a new shot and put this frame as key frame
                     if (i > shotDuration) {
                         insertIntoShotHashMap(i, (int)this.shotLength * 24, i);
                         currShot = i;
                         shotDuration = i + (this.shotLength * 24);
                     } else {
                         insertIntoShotHashMap(currShot,  (int)((i + (this.shotLength * 24) - shotDuration)), i);
                         shotDuration = i + (this.shotLength * 24);
                     }
                 } else if (entropyDiff >= lowerThreshold && entropyDiff <= upperThreshold) {
                     //create a new key frame but same shot
                     //     System.out.println("Value between Thresholds..." + i);
                     if (i > shotDuration) {
                         insertIntoShotHashMap(currShot, 1, i);
                         shotDuration++;
                     } else {
                         insertIntoShotHashMap(currShot, 0, i);
                     }
                 } else {
                     //    System.out.println("Value below lowerThreshold..." + i);
                     //add this frame to exisiting shot, neither a new key frame or not a new shot
                     if (i > shotDuration) {
                         insertIntoShotHashMap(currShot, 1, -1);
                         shotDuration++;
                     }
                 }
                 entropy_prev = entropy_next;
                 entropy_next = 0;
             }
             fis.close();
 //            printShotHashMap();
 //            shotInfo.writeShotHashMapToFile(0, 120);
 //            shotInfo.writeShotHashMapToFile(720, 240);
         } catch (IOException ex) {
             Logger.getLogger(videoToShots.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     //Convert RGB Frame to YUV
     public static double[] convertToYUV(byte frame[]) {
         double[][] rgbMultiplier = new double[][]{{0.2990, 0.5870, 0.1140}, {-0.1470, -0.2890, 0.4360}, {0.6150, -0.5140, -0.1000}};
         double yuvArray[] = new double[Height * Width * 3];
 
         int ind = 0;
         //double Y_array[][] = new double[Height][Width];
         //double U_array[][] = new double[Height][Width];
         //double V_array[][] = new double[Height][Width];
 
         for (int y = 0; y < Height; y++) {
 
             for (int x = 0; x < Width; x++) {
 
                 int r = frame[ind] & 0xff;
                 int g = frame[ind + Height * Width] & 0xff;
                 int b = frame[ind + Height * Width * 2] & 0xff;
 
 
                 /*for (int i = 0; i < 1; i++) {
                     double temp = 0;
                     for (int j = 0; j < 3; j++) {
                         if (j == 0) {
                             temp = temp + rgbMultiplier[i][j] * (r);
                         } else {
                             if (j == 1) {
                                 temp = temp + rgbMultiplier[i][j] * (g);
                             } else {
                                 temp = temp + rgbMultiplier[i][j] * (b);
                             }
                         }
                     }
                     if (i == 0) {
                         Y_array[y][x] = temp;
                     } else {
                         if (i == 1) {
                             U_array[y][x] = temp;
                         } else {
                             V_array[y][x] = temp;
                         }
                     }
                 }*/
                 double temp = rgbMultiplier[0][0]*r+rgbMultiplier[0][1]*g+rgbMultiplier[0][2]*b;
                 //int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                 //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                 //img.setRGB(x,y,pix);
                 //Y_array[y][x] = temp;
                 yuvArray[ind] = temp;
                 ind++;
             }
         }
 
         /*int k = 0;
         for (int i = 0; i < Height; i++) {
             for (int j = 0; j < Width; j++) {
                 yuvArray[k] = Y_array[i][j];
                 yuvArray[k + Height * Width] = U_array[i][j];
                 yuvArray[k + Height * Width * 2] = V_array[i][j];
                 k++;
             }
         }*/
 
         return yuvArray;
     }
 
     public double computeEntropy(double yuvArray[]) {
         double entropy = 0;
         int index = 0;
         int histogramBucket[] = new int[numBuckets];
         int num = 255 / numBuckets;
         for (int i = 0; i < Height * Width; i++) {
             index = (int) (yuvArray[i] / num);
             if (index >= numBuckets) {
                 index = 9;
             }
             histogramBucket[index]++;
         }
         double prob[] = new double[numBuckets];
         for (int i = 0; i < numBuckets; i++) {
             prob[i] = ((double) histogramBucket[i] / (double)(Height * Width));
             //System.out.println("Prob is: "+prob[i]);
             if (prob[i] > 0) {
                 double temp = (double)(Math.log(prob[i])/Math.log(2));
                 //System.out.println("Temp is : "+temp);
                 entropy += ((-1) * prob[i]) * temp;
             }
             //System.out.println("entropy is: "+entropy);
         }
         return entropy;
     }
 
     public double computeDifferenceInEntropy(double entropy_prev, double entropy_next) {
         if (entropy_prev == 0) {
             return 0;
         }
         double diff;
         diff = 100 * Math.abs((entropy_next - entropy_prev) / entropy_prev);
         return diff;
     }
 
     //only prints YUV frame
     public static void printImage(double yuvArray[]) {
 
         BufferedImage img = new BufferedImage(Width, Height, BufferedImage.TYPE_BYTE_GRAY);
 
         int ind = 0;
         for (int y = 0; y < Height; y++) {
 
             for (int x = 0; x < Width; x++) {
 
                 byte a = 0;
                 byte r = (byte) yuvArray[ind];
                 byte g = (byte) yuvArray[ind];
                 byte b = (byte) yuvArray[ind];
 
                 int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                 //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                 //this.pixels[y][x] = (double)pix;
                 //System.out.println("Value : "+r);
                 img.setRGB(x, y, pix);
                 ind++;
             }
         }
 
         // Use a label to display the image
         JFrame frame = new JFrame();
         JLabel label = new JLabel(new ImageIcon(img));
         frame.getContentPane().add(label, BorderLayout.CENTER);
         frame.pack();
         frame.setVisible(true);
     }
 
     public void insertIntoShotHashMap(Integer key, int count, Integer keyFrameNum) {
         shotInfo obj = shotHashMap.get(key);
         if (obj == null) {
             shotInfo objTemp = new shotInfo();
             if ((count + key) > videoToShots.numFrames) {
                 count =  (int)(videoToShots.numFrames - key);
             }
             objTemp.numFrames = count;
//            objTemp.keyFrames.put(keyFrameNum, 1);
             shotHashMap.put(key, objTemp);
         } else {
             obj.numFrames += count;
             if ((obj.numFrames + key) > videoToShots.numFrames) {
                 obj.numFrames = (int) (videoToShots.numFrames - key);
             }
             if (keyFrameNum != -1) {
                 int temp = 0;
                 if(obj.keyFrames.containsKey(keyFrameNum)){
                     temp = obj.keyFrames.get(keyFrameNum);
//                  obj.keyFrames.put(keyFrameNum, temp+1);
                 }
                 else {
//                    obj.keyFrames.put(keyFrameNum, 1);
                 }
             }
             shotHashMap.put(key, obj);
         }
     }
 
     public static void printShotHashMap() throws IOException {
         //int count = 0;
         //for (Map.Entry<Integer, shotInfo> entry : shotHashMap.entrySet()) {
             //System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue().numFrames);
             //entry.getValue().addKeyToKeyFramesHashMap(-1);
             //entry.getValue().addKeyToKeyFramesHashMap(-2);
             //entry.getValue().addKeyToKeyFramesHashMap(-3);
             //entry.getValue().addKeyToKeyFramesHashMap(-4);
             //entry.getValue().addKeyToKeyFramesHashMap(-5);
         //}
         shotInfo.sortKeyFramesHashMapOnKey();
         //insertIntoShotHashMap(-1, 0, 0);
         //shotInfo.computeShotWeight();
         //shotInfo.sortShotHashMapOnWeight();
 
         for (Map.Entry<Integer, shotInfo> entry : shotHashMap.entrySet()) {
             System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue().numFrames + " Weight: "+entry.getValue().weight+" Num of Keyframes: "+entry.getValue().keyFrames.size());
             System.out.print("Key Frames are: ");
             for (Map.Entry<Integer, Integer> sub_entry : entry.getValue().keyFrames.entrySet()) {
                 System.out.print(sub_entry.getKey() + " ");
             }
             System.out.println();
         }
     }
 }
