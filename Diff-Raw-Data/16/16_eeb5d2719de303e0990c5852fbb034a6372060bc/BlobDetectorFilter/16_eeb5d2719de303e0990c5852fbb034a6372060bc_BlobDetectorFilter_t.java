 package uct.snapvote.filter;
 
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import uct.snapvote.ImageByteBuffer;
 
 /**
  * Created by Justin on 2013/08/11.
  */
 public class BlobDetectorFilter extends BaseRegionFilter {
 
     final char MAX_CHAR_VAL = 65535;
 
     public BlobDetectorFilter(ImageByteBuffer source){
         super(source, null);
     }
 
     static class Blob
     {
        public static final int MIN_MASS = 15;

         public int xMin;
         public int xMax;
         public int yMin;
         public int yMax;
         public int mass;
 
         public Blob(int xMin, int xMax, int yMin, int yMax, int mass)
         {
             this.xMin = xMin;
             this.xMax = xMax;
             this.yMin = yMin;
             this.yMax = yMax;
             this.mass = mass;
         }
 
         public String toString()
         {
             return String.format("X: %4d -> %4d, Y: %4d -> %4d, mass: %6d", xMin, xMax, yMin, yMax, mass);
         }
     }
 
     @Override
     public void run(){
 
         // this contains the labels for the pixels. 0 = no label
         char[] pixelLabels = new char[source.getHeight() * source.getWidth()];
 
         char[] labelTable = new char[Character.MAX_VALUE];
         char[] xMinTable = new char[Character.MAX_VALUE];
         char[] xMaxTable = new char[Character.MAX_VALUE];
         char[] yMinTable = new char[Character.MAX_VALUE];
         char[] yMaxTable = new char[Character.MAX_VALUE];
         char[] massTable = new char[Character.MAX_VALUE];
 
         char currentlabel = 1;
 
         // loop through all pixels
         for(int y=1;y<source.getHeight()-1;y++)
         {
             for(int x=1;x<source.getWidth()-1;x++)
             {
                 pixelLabels[y * source.getWidth() + x] = 0;
 
                 int px = source.get(x,y) & 0xFF;
 
                 // can this pixel be labeled?
                 if (px == 0)
                 {
                     // check top and left pixel labels
                     char north = pixelLabels[(y-1) * source.getWidth() + x];
                     char west = pixelLabels[y * source.getWidth() + (x-1)];
                     char northwest = pixelLabels[(y-1) * source.getWidth() + (x-1)];
                     char northeast = pixelLabels[(y-1) * source.getWidth() + (x+1)];
 
                     char min = Character.MAX_VALUE;
                     if(north > 0) min = north;
                     if(west > 0 && west < min) min = west;
                     if(northwest > 0 && northwest < min) min = northwest;
                     if(northeast > 0 && northeast < min) min = northeast;
 
                     // if neither were labeled
                     if (min == Character.MAX_VALUE)
                     {
                         // label the pixel with currentlabel
                         pixelLabels[y * source.getWidth() + x] = currentlabel;
                         labelTable[currentlabel] = currentlabel;
 
                         yMinTable[currentlabel] = (char)y;
                         yMaxTable[currentlabel] = (char)y;
 
                         xMinTable[currentlabel] = (char)x;
                         xMaxTable[currentlabel] = (char)x;
 
                         massTable[currentlabel] = 1;
 
                         // increment current label
                         currentlabel++;
                     }
                     else
                     {
                         pixelLabels[y * source.getWidth() + x] = min;
 
                         yMaxTable[min] = (char)y;
                         massTable[min]++;
 
                         if( x< xMinTable[min]) xMinTable[min] = (char)x;
                         if( x> xMaxTable[min]) xMaxTable[min] = (char)x;
 
 
                         if (north > 0)
                         {
                             labelTable[north] = min;
                         }
                         if (west > 0)
                         {
                            labelTable[west] = min;
                        }
                        if (northeast > 0)
                        {
                            labelTable[northeast] = min;
                        }
                        if (northwest > 0)
                        {
                            labelTable[northwest] = min;
                         }
                     }
                 }
             }
         }
 
         char maxlabelcount = currentlabel;
 
         List<Blob> blobs = new ArrayList<Blob>();
 
         // for each label, decay it
         for (int i = maxlabelcount-1; i >0;i--)
         {
             if (labelTable[i] != i)
             {
                 if( xMaxTable[i] > xMaxTable[labelTable[i]]) xMaxTable[labelTable[i]] = xMaxTable[i];
                 if( xMinTable[i] < xMinTable[labelTable[i]]) xMinTable[labelTable[i]] = xMinTable[i];
                 if( yMaxTable[i] > yMaxTable[labelTable[i]]) yMaxTable[labelTable[i]] = yMaxTable[i];
                 if( yMinTable[i] < yMinTable[labelTable[i]]) yMinTable[labelTable[i]] = yMinTable[i];
                 massTable[labelTable[i]] += massTable[i];
 
                 int l = i;
                 while (l != labelTable[l]) l = labelTable[l];
                 labelTable[i] = (char)l;
             }
            else if (massTable[i] >= Blob.MIN_MASS)
             {
                 Blob blob = new Blob(xMinTable[i], xMaxTable[i], yMinTable[i], yMaxTable[i], massTable[i]);
                 blobs.add(blob);
             }
         }
 
         for (int i = maxlabelcount-1; i >0;i--)
         {
             if (labelTable[i] != i)
             {
                 int l = i;
                 while (l != labelTable[l]) l = labelTable[l];
                 labelTable[i] = (char)l;
             }
         }
 
         char newlabel = 0;
         for(int i=0;i< maxlabelcount-1;i++)
         {
             if ( labelTable[i] == i) labelTable[i] = newlabel++;
             else labelTable[i] = labelTable[labelTable[i]];
         }
 
         byte [] colours = new byte[]{
                 (byte) 31,
                 (byte) 63,
                 (byte) 95,
                 (byte) 127,
                 (byte) 159,
                 (byte) 191,
                 (byte) 223
         };
 
         for(int y=1;y<source.getHeight()-1;y++)
         {
             for(int x=1;x<source.getWidth()-1;x++)
             {
                 char l = pixelLabels[y * source.getWidth() + x];
                 char rootlabel = labelTable[l];
 
                 source.set(x,y, colours[rootlabel % 7]);
             }
         }
 
         for(Blob b : blobs)
         {
             for(int y=b.yMin;y< b.yMax;y++)
             {
                 source.set(b.xMin,y,(byte)255);
                 source.set(b.xMax,y,(byte)255);
 
                 if( y ==b.yMin  || y == (b.yMax-1))
                 {
                     for(int x=b.xMin;x< b.xMax;x++)
                     {
                         source.set(x,y,(byte)255);
                     }
                 }
             }
         }
 
 
 
         //Log.d("uct.snapvote", "Labels: "+(int)count+".");
     }
 }
