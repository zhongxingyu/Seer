 package com.example.testing;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.lang.Object;
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.ImageFormat;
 import android.graphics.Rect;
 import android.graphics.YuvImage;
 import android.graphics.drawable.BitmapDrawable;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.ImageView;
 
 public class ARCameraPreview extends SurfaceView implements
         SurfaceHolder.Callback {
     // Definitions:
     private SurfaceHolder mHolder; // Tinem minte Suprafata de randare
     private Camera mCamera; // Tinem minte variabila de camera
     byte[] cb; //
     public Bitmap bitmap = null; // Tinem minte buffer-ul pt afisare
     AnimEvents surface_2 = null, surface_3 = null, surface_4 = null,
             surface_5 = null;
     int displayX, displayY; // DisplayX DisplayY
     private int[] cameraBuffer = new int[4 * 240 * 320]; // Buffer de camera
 
     public ARCameraPreview(Context context, ArrayList<AnimEvents> var,
             int dispX, int dispY) {
         // Constructorul asteapta contextul de la camera
         super(context); //
 
         surface_2 = var.get(0);
         surface_3 = var.get(1);
         surface_4 = var.get(2);
         surface_5 = var.get(3);
 
         cb = new byte[10 * 1000 * 1000];
         mHolder = getHolder(); // Obtin instanta de SURFACE HOLDER
         mHolder.addCallback(this); // Inregistrez callback-ul la suprafata de
                                    // desenat
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Setez
                                                                   // Bufferele
                                                                   // de memorie
         displayX = dispX;
         displayY = dispY;
     }
 
     @SuppressLint("NewApi")
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width,
             int height) {
         // mCamera.setDisplayOrientation(180);
         // Apelat cand suprafata a fost initializata coomplet
         mCamera.stopPreview(); // Opresc preview-ul la camera
         final Camera.Parameters p = mCamera.getParameters(); // Creez parametrii
                                                              // noi pentru
                                                              // camera
         p.setPreviewSize(displayY, displayX); // Adjustez rezolutia camerei
                                               // ----> REZOLUTIE MAI MICA
                                               // FRAMERATE RIDICAT !
         mCamera.setParameters(p); // Setez parametrii
         mCamera.startPreview(); // Camera trimite frame-uri catre suprafata
         mCamera.addCallbackBuffer(cb); // Adaug buffer de callback
         mCamera.setPreviewCallback(new PreviewCallback() { // Initializez
                                                            // functia de
                                                            // callback
 
             @Override
             public void onPreviewFrame(byte[] data, Camera camera) { // Procedura
                                                                      // de
                                                                      // callback
                 //
                 // Log.i("testing", "camera framerate"); //
                 //
                 if (data == null) { // Daca nu primim date
                     Log.e("null data ", "err getting data"); //
                 }
                 //
                 int imageFormat = p.getPreviewFormat(); // Memoreaza variabila
                                                         // de format
 
                 switch (imageFormat) {
 
                 case ImageFormat.NV21: {
 
                     YuvImage yuvImage = new YuvImage(data, imageFormat,
                             displayY, displayX, null); // Din NV21 -> RAW argb
                                                        // 8888
                     YUV_NV21_TO_RGB(cameraBuffer, yuvImage.getYuvData(), 320,
                             240); //
                     bitmap = Bitmap.createBitmap(cameraBuffer, 320, 240,
                             Bitmap.Config.ARGB_8888); //
                 }
                     break;
 
                 case ImageFormat.JPEG: {
                     bitmap = BitmapFactory
                             .decodeByteArray(data, 0, data.length);
                     Log.i("formatul utilizat", "JPEG");
                 }
                     break;
 
                 case ImageFormat.RGB_565: {
                     bitmap = BitmapFactory
                             .decodeByteArray(data, 0, data.length);
                     Log.i("formatul utilizat", "RGB_565");
                 }
                     break;
 
                 }
                 if (surface_2 != null) { // Randeaza Bitmap-ul intr un context
 
                     surface_2.setImageBitmap(surface_2.applyFilter(bitmap)); // ImageView
                     surface_3.setImageBitmap(surface_3.applyFilter(bitmap));
                     surface_4.setImageBitmap(surface_4.applyFilter(bitmap));
                     surface_5.setImageBitmap(surface_5.applyFilter(bitmap));
 
                 } //
                 else { //
                     Log.e("err", "null");
                 }
             }
         });
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         // E apelat odata ce interfata a fost initiaizata
         mCamera = Camera.open(); // Obtin instanta la camera
         try {
             mCamera.setPreviewDisplay(holder); // Initilaizez Suprafata de
                                                // randare
         } catch (IOException exception) { //
             mCamera.release(); //
             mCamera = null; //
         } //
           //
     } //
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         // Apelat cand suprafata de randare se distruge
         mCamera.stopPreview(); // Oprim camera sa mai trimita frame-uri catre
                                // Suprafata
        mCamera.release(); // Eliberam camera
     } //
 
     public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width,
             int height) {
         final int frameSize = width * height;
 
         final int ii = 0;
         final int ij = 0;
         final int di = +1;
         final int dj = +1;
 
         int a = 0;
         for (int i = 0, ci = ii; i < height; ++i, ci += di) {
             for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                 int y = (0xff & ((int) yuv[ci * width + cj]));
                 int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width
                         + (cj & ~1) + 0]));
                 int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width
                         + (cj & ~1) + 1]));
                 y = y < 16 ? 16 : y;
 
                 int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                 int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                 int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));
 
                 r = r < 0 ? 0 : (r > 255 ? 255 : r);
                 g = g < 0 ? 0 : (g > 255 ? 255 : g);
                 b = b < 0 ? 0 : (b > 255 ? 255 : b);
 
                 argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
             }
         }
     }
 }
