 package uct.snapvote.util;
 
 /**
  * Created by Ben on 2013/09/02.
  */
 public class SobelAngleClassifier {
 
     static final int MAX = 1020;
    static final int RES = 1; // power of 2!
    static final int BUCKET = (MAX*2) >> RES;
 
     private static byte[] angles;
     private static byte[] hypclass;
 
     public static void prepare()
     {
         angles = new byte[BUCKET * BUCKET];
         hypclass = new byte[BUCKET * BUCKET];
 
         for(int y=0;y<BUCKET;y++)
         {
             int offset = y*BUCKET;
 
             for(int x=0;x<BUCKET;x++)
             {
                 int index = offset + x;
 
                int gx = x<<RES-MAX;
                int gy = y<<RES-MAX;
 
                 int degrees = (int)(Math.atan2(gy, gx)*180/Math.PI);
                degrees = (degrees + 180) % 180;
 
                 byte acat = (byte)0;
                 if (degrees < 45) acat = (byte)0;
                 else if (degrees < 90) acat = (byte)45;
                 else if (degrees < 135) acat = (byte)90;
                 else if (degrees < 180) acat = (byte)135;
 
                 angles[index] = acat;
 
                 int gm = (int)Math.sqrt(gy*gy + gx*gx);
                 if(gm > 255) gm = 255;
 
                 hypclass[index] = (byte)gm;
 
             }
         }
     }
 
     public static byte atan2cat(int gy, int gx)
     {
 
         gx+=MAX;
 
         gx=gx>>RES;
 
         gy+=MAX;
 
         gy=gy>>RES;
 
         int index = gy*BUCKET + gx;
 
         return angles[index];
     }
 
     public static byte mag(int gy, int gx)
     {
 
         gx+=MAX;
 
         gx=gx>>RES;
 
         gy+=MAX;
 
         gy=gy>>RES;
 
         int index = gy*BUCKET + gx;
 
         return hypclass[index];
     }
 
 
 }
