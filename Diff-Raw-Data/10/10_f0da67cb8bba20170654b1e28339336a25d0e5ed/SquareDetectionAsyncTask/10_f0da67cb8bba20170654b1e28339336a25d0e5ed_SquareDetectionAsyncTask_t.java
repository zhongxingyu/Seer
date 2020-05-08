 package uct.snapvote;
 
 import android.content.ContentResolver;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapRegionDecoder;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.Guard;
 import java.util.BitSet;
 
 import uct.snapvote.filter.BlobDetectorFilter;
 import uct.snapvote.filter.PeakFindTRF;
 import uct.snapvote.filter.GaussianTRF;
 import uct.snapvote.filter.SobelTRF;
 import uct.snapvote.filter.ThreadedBaseRegionFilter;
 import uct.snapvote.util.DebugTimer;
 
 /**
  * Created by Ben on 8/4/13.
  */
 public class SquareDetectionAsyncTask extends AsyncTask<String, String, Integer> {
 
     ProcessActivity processActivity;
     int gaussianBlurRadius, cannyPeakLow, cannyPeakHigh;
 
     public SquareDetectionAsyncTask(ProcessActivity processActivity) {
         this.processActivity = processActivity;
     }
 
 
     @Override
     protected Integer doInBackground(String... strings) {
 
         loadPreferences();
 
         try {
 
             ImageByteBuffer buffer1 = readGrayscale(processActivity.imageUri);
             ImageByteBuffer buffer2 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());
             ImageByteBuffer buffer3 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());
 
             DebugTimer totalTimer = new DebugTimer();
             DebugTimer debugTimer = new DebugTimer();
 
             // Gaussian blur
             blur(buffer1, buffer2, gaussianBlurRadius);
             publishProgress("1", "Blur: " + debugTimer.toString()); debugTimer.restart();
 
             // Sobel filter
             sobelFilter(buffer2, buffer1, buffer3);
             publishProgress("1", "Sobel: " + debugTimer.toString()); debugTimer.restart();
 
             BitSet visitedPixels = new BitSet(buffer1.getHeight() * buffer1.getWidth());
 
             buffer2 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());
 
             // Canny edge detection
             // In-place editing, don't need another buffer.
             peakFilter(buffer1, buffer2, buffer3, visitedPixels, cannyPeakLow, cannyPeakHigh);
             publishProgress("1", "Peaked: " + debugTimer.toString()); debugTimer.restart();
 
             buffer1 = null;
             buffer3 = null;
             System.gc();
 
             BlobDetectorFilter bdf = new BlobDetectorFilter(buffer2, visitedPixels, buffer2.getWidth(), buffer2.getHeight());
             bdf.run();
             publishProgress("1", "Blob label: " + debugTimer.toString()); debugTimer.restart();
             publishProgress("1", "Total Load & Process Time: " + totalTimer.toString());
 
             // save to sdcard in order to debug
             Bitmap testImage = buffer2.createBitmap();
             publishProgress("1", "Bitmap: " + debugTimer.toString()); debugTimer.restart();
 
             ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             testImage.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
             File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
             File f = new File(path, "test.jpg");
             f.createNewFile();
             FileOutputStream fo = new FileOutputStream(f);
             fo.write(bytes.toByteArray());
             fo.close();
             publishProgress("1", "Save: " + debugTimer.toString()); debugTimer.restart();
             Log.d("uct.snapvote", "Saved image to "+f.getAbsolutePath());
 
         } catch (IOException e) {
 
         }
         return 0;
     }
 
     private void loadPreferences(){
         SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(processActivity);
 
         gaussianBlurRadius = Integer.parseInt(mySharedPreferences.getString("gaussian_blur_radius", "2"));
         cannyPeakLow = Integer.parseInt(mySharedPreferences.getString("canny_peak_low", "70"));
         cannyPeakHigh = Integer.parseInt(mySharedPreferences.getString("canny_peak_high", "150"));
 
         Log.d("uct.snapvote", "Preferences loaded.");
     }
 
     private ImageByteBuffer readGrayscale(String datastr) throws IOException {
         // first get content uri of image on phone
         Uri contentURI = Uri.parse(datastr);
         ContentResolver cr = processActivity.getContentResolver();
         // open file stream
         InputStream in = cr.openInputStream(contentURI);
 
         // read image attributes
         BitmapFactory.Options preOptions = new BitmapFactory.Options();
         preOptions.inJustDecodeBounds = true;
         BitmapFactory.decodeStream(in,null,preOptions);
         int imageHeight = preOptions.outHeight;
         int imageWidth = preOptions.outWidth;
 
 
         long t1 = System.currentTimeMillis();
 
         // hurrah now we know how big our grayscale byte buffer is!
         ImageByteBuffer gbuffer = new ImageByteBuffer(imageWidth, imageHeight);
 
         publishProgress("1", String.format("Dimensions read - %dx%d", imageWidth, imageHeight) );
 
         // reset input stream
         in = cr.openInputStream(contentURI);
         BitmapRegionDecoder regDec = BitmapRegionDecoder.newInstance(in, false);
 
         int stripHeight = 500;
 
        int num_layers = imageHeight /  stripHeight;
         int rem_layer = imageHeight % stripHeight;

        if (stripHeight > imageHeight)
        {
            num_layers = 0;
            rem_layer = imageHeight;
        }

         Bitmap strip;
 
         BitmapFactory.Options postOptions = new BitmapFactory.Options();
         preOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
 
         for(int i=0;i<num_layers;i++) {
             Rect r = new Rect(0, i* stripHeight,imageWidth, (i+1)* stripHeight);
             strip= regDec.decodeRegion(r, postOptions);
             int[] pixelData = new int[ stripHeight*imageWidth];
             strip.getPixels(pixelData, 0, imageWidth, 0,0,imageWidth,  stripHeight);
 
             for(int y=0;y< stripHeight;y++)
                 for(int x = 0;x<imageWidth;x++) {
                     int pix = pixelData[y*imageWidth+x];
                     int g = ((pix & 0xFF) + ((pix >> 8) & 0xFF) + ((pix >> 16) & 0xFF))/3;
                     gbuffer.set(x, i* stripHeight+y, (byte)g);
                 }
         }
         Rect r = new Rect(0, imageHeight-rem_layer,imageWidth, imageHeight);
         strip = regDec.decodeRegion(r, postOptions);
         int[] pixelData = new int[rem_layer*imageWidth];
         strip.getPixels(pixelData, 0, imageWidth, 0,0,imageWidth, rem_layer);
 
         for(int y=0;y<rem_layer;y++)
             for(int x = 0;x<imageWidth;x++) {
                 int pix = pixelData[y*imageWidth+x];
                 int g = ((pix & 0xFF) + ((pix >> 8) & 0xFF) + ((pix >> 16) & 0xFF))/3;
                 gbuffer.set(x, imageHeight-rem_layer+y, (byte)g);
             }
 
         long t2 = System.currentTimeMillis();
 
         publishProgress("1", String.format("Image loaded time: %d.%ds", (t2-t1) / 1000, (t2-t1) % 1000) );
         return gbuffer;
     }
 
     public class Rectangle
     {
         int x1, y1;
         int x2, y2;
     }
 
     private Rectangle[] splitRegion(int width, int height, int num, int border) {
         int subwidth = width / num;
         Rectangle[] o = new Rectangle[num];
         for(int i=0;i<num;i++)
         {
             o[i] = new Rectangle();
             o[i].y1 = border;
             o[i].y2 = height - border;
             o[i].x1 = i * subwidth;
             o[i].x2 = o[i].x1 + subwidth;
 
             if(i==0) o[i].x1 += border;
             if(i==num-1) o[i].x2 = width - border;
         }
         return o;
     }
 
     private void runTRFs(ThreadedBaseRegionFilter[] trfs) {
         // initialise and start threads
         Thread[] threads = new Thread[trfs.length];
         for(int i=0;i<trfs.length;i++)
         {
             threads[i] = new Thread(trfs[i]);
             threads[i].start();
         }
 
         // wait for completion
         for(int i=0;i<trfs.length;i++)
         {
             try { threads[i].join(); } catch (InterruptedException e) {  }
         }
     }
 
 
     private void blur(ImageByteBuffer source, ImageByteBuffer destination, int blurRadius) {
         int numthreads = 4;
 
         Rectangle[] regions = splitRegion(source.getWidth(), source.getHeight(), numthreads, blurRadius);
 
         GaussianTRF[] trfs = new GaussianTRF[numthreads];
         for(int i=0;i<numthreads;i++)
         {
             trfs[i] = new GaussianTRF(source, destination, regions[i].x1, regions[i].y1, regions[i].x2, regions[i].y2, blurRadius);
         }
 
         runTRFs(trfs);
     }
 
     private void sobelFilter(ImageByteBuffer source, ImageByteBuffer destination, ImageByteBuffer dirDataOutput) {
         int numthreads = 4;
 
         Rectangle[] regions = splitRegion(source.getWidth(), source.getHeight(), numthreads, 1);
 
         SobelTRF[] trfs = new SobelTRF[numthreads];
         for(int i=0;i<numthreads;i++)
         {
             trfs[i] = new SobelTRF(source, destination, regions[i].x1, regions[i].y1, regions[i].x2, regions[i].y2, dirDataOutput);
         }
 
         runTRFs(trfs);
     }
 
     private void peakFilter(ImageByteBuffer source, ImageByteBuffer destination, ImageByteBuffer dirDataInput, BitSet visitpixels, int peakLow, int peakHigh) {
 
 
 
         int numthreads = 4;
 
         Rectangle[] regions = splitRegion(source.getWidth(), source.getHeight(), numthreads, 1);
 
         boolean[][] peakList = new boolean[source.getHeight()][source.getWidth()];
 
         PeakFindTRF[] trfs = new PeakFindTRF[numthreads];
         for(int i=0;i<numthreads;i++)
         {
             trfs[i] = new PeakFindTRF(source, regions[i].x1, regions[i].y1, regions[i].x2, regions[i].y2, dirDataInput, peakLow, peakHigh, peakList);
         }
 
         runTRFs(trfs);
 
 
         Log.d("uct.snapvote", "Identified first peaks.");
 
         boolean more;
         do{
             more = false;
 
             for(int y = 1; y < source.getHeight(); y++){
                 for(int x = 1; x < source.getWidth(); x++){
                     boolean peak = peakList[y][x];
 
                     if(peak){
                         peakList[y][x] = false;
 
                         for(int p = -1; p < 1; p++){
                             for(int q = -1; q < 1; q++){
 
                                 if(q == 0 && p == 0)
                                     continue;
 
                                 byte neighbour = source.get(x+p, y+q);
 
                                 if(neighbour != 0 && neighbour != (byte) 255){
                                     source.set(x+p, y+q, (byte) 255);
                                     peakList[y+p][x+q] = true;
                                     more = true;
                                 }
                             }
                         }
                     }
                 }
             }
         }while(more);
 
         // Clear out remaining potential peaks that have lost their potential ;(
         for(int y = 1; y < source.getHeight()-1; y++){
             for(int x = 1; x < source.getWidth()-1; x++){
 
                 byte pixel = source.get(x, y);
 
                 // Expand any peaks out by 1px to fill small gaps.
                 if(pixel == (byte)255){
                     visitpixels.set((y-1) * source.getWidth() +x -1, (y-1) * source.getWidth() +x +1);
                     visitpixels.set((y) * source.getWidth() +x -1, (y) * source.getWidth() +x +1);
                     visitpixels.set((y+1) * source.getWidth() +x -1, (y+1) * source.getWidth() +x +1);
 
                     destination.set(x-1, y-1, (byte)60); destination.set(x, y-1, (byte)60); destination.set(x+1, y-1, (byte)60);
                     destination.set(x-1, y, (byte)60); destination.set(x, y, (byte)60); destination.set(x+1, y, (byte)60);
                     destination.set(x-1, y+1, (byte)60); destination.set(x, y+1, (byte)60); destination.set(x+1, y+1, (byte)60);
                 }
             }
         }
     }
 
 
     @Override
     protected void onProgressUpdate(String... values) {
         processActivity.pbMainProgress.incrementProgressBy(Integer.parseInt(values[0]));  // hax ;)
         if (values.length > 1) {
             processActivity.tvConsole.append(values[1] + "\n");
         }
     }
 }
