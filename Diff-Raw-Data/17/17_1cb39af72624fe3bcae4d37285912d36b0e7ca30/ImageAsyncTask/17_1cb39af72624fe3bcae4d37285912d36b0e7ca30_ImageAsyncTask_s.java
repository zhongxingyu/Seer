 package org.g_okuyama.capture;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.hardware.Camera.Size;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.provider.MediaStore.Images;
 import android.util.Log;
 
 public class ImageAsyncTask extends AsyncTask<Bitmap, Void, Bitmap> {
     public static final String TAG = "ContShooting";
 
     Context mContext = null;
     CameraPreview mCameraPreview;
     byte[] mData;
     Size mSize;
     
     ImageAsyncTask(Context context, CameraPreview camera, byte[] data, Size size){
         mContext = context;
         mData = data;
         mCameraPreview = camera;
         mSize = size;
     }
 
     @Override
     protected Bitmap doInBackground(Bitmap... bmp) {
     	//Bitmap retBmp;
         
         final int width = mSize.width;
         final int height = mSize.height;            
         int[] rgb = new int[(width * height)];
         decodeYUV420SP(rgb, mData, width, height);
         bmp[0].setPixels(rgb, 0, width, 0, 0, width, height);
         rgb = null;
         
         /*
         //]
         Matrix matrix = new Matrix();
         // ]pxw
         matrix.postRotate(90.0f);   
         retBmp = Bitmap.createBitmap(bmp[0], 0, 0, bmp[0].getWidth(), bmp[0].getHeight(), matrix, true);
         bmp[0].recycle();
         bmp[0] = null;
         */
         
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         bmp[0].compress(Bitmap.CompressFormat.JPEG, 100, out);
 
         savedata(out.toByteArray());
         bmp[0].recycle();
         bmp[0] = null;
 
         return bmp[0];
     }
 
     @Override
     protected void onPostExecute(Bitmap bmp) {
         mCameraPreview.countShoot();
     }
     
     // YUV420 to BMP 
     public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) { 
         final int frameSize = width * height; 
 
         for (int j = 0, yp = 0; j < height; j++) { 
             int uvp = frameSize + (j >> 1) * width, u = 0, v = 0; 
             for (int i = 0; i < width; i++, yp++) { 
                 int y = (0xff & ((int) yuv420sp[yp])) - 16; 
                 if (y < 0) y = 0; 
                 if ((i & 1) == 0) { 
                         v = (0xff & yuv420sp[uvp++]) - 128; 
                         u = (0xff & yuv420sp[uvp++]) - 128; 
                 } 
 
                 int y1192 = 1192 * y; 
                 int r = (y1192 + 1634 * v); 
                 int g = (y1192 - 833 * v - 400 * u); 
                 int b = (y1192 + 2066 * u); 
 
                 if (r < 0) r = 0; else if (r > 262143) r = 262143; 
                 if (g < 0) g = 0; else if (g > 262143) g = 262143; 
                 if (b < 0) b = 0; else if (b > 262143) b = 262143; 
 
                 rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff); 
             } 
         }
     }
     
     public void savedata(byte[] data){
         File mFile = null;
         if(mFile == null){
             mFile = new File(Environment.getExternalStorageDirectory(), "/ContShooting");
         }
 
         FileOutputStream fos = null;
         File savefile = null;
        String datastr = getCurrentDate();
         try{
             if(mFile.exists() == false){
                 mFile.mkdir();
             }
            savefile = new File(mFile.getPath(), datastr + ".jpg");
             fos = new FileOutputStream(savefile);
             fos.write(data);
             fos.flush();
             fos.close();
         }catch(IOException e){
             //Log.e(TAG, "IOException in savedata");
             if(fos != null){
                 try {
                     fos.close();
                 } catch (IOException e1) {
                     //do nothing
                 }
             }
             return;
         }
 
         //M[ւ̓o^
        ContentValues values = new ContentValues();
 
         values.put(Images.Media.MIME_TYPE, "image/jpeg");
         values.put(Images.Media.DATA, savefile.getAbsolutePath());
         values.put(Images.Media.SIZE, savefile.length());
         //values.put(Images.Media.TITLE,strFile);
         //values.put(Images.Media.DISPLAY_NAME,strFile);
        values.put(Images.Media.DATE_ADDED, datastr);
        values.put(Images.Media.DATE_TAKEN, datastr);
        values.put(Images.Media.DATE_MODIFIED, datastr);
         //values.put(Images.Media.DESCRIPTION,"");
         //values.put(Images.Media.LATITUDE,0.0);
         //values.put(Images.Media.LONGITUDE,0.0);
         //values.put(Images.Media.ORIENTATION,"");
         ((ContShooting)mContext).saveGallery(values);
     }
     
     
     String getCurrentDate(){
         Calendar cal1 = Calendar.getInstance();
         int year = cal1.get(Calendar.YEAR);
         int mon = cal1.get(Calendar.MONTH) + 1;
         int d = cal1.get(Calendar.DATE);
         int h = cal1.get(Calendar.HOUR_OF_DAY);
         int min = cal1.get(Calendar.MINUTE);
         int sec = cal1.get(Calendar.SECOND);
         int msec = cal1.get(Calendar.MILLISECOND);
         
         String month = Integer.toString(mon);
         if(month.length() == 1){
             month = "0" + month;
         }
         String day = Integer.toString(d);
         if(day.length() == 1){
             day = "0" + day;
         }
         String hour = Integer.toString(h);
         if(hour.length() == 1){
             hour = "0" + hour;
         }
         String minute = Integer.toString(min); 
         if(minute.length() == 1){
             minute = "0" + minute;
         }
         String second = Integer.toString(sec);
         if(second.length() == 1){
             second = "0" + second;
         }
         String millisecond = Integer.toString(msec);
         if(millisecond.length() == 1){
             millisecond = "00" + millisecond;
         }
         else if(millisecond.length() == 2){
             millisecond = "0" + millisecond;
         }            
 
         return Integer.toString(year) + month + day + hour + minute + second + millisecond;
     }
 }
