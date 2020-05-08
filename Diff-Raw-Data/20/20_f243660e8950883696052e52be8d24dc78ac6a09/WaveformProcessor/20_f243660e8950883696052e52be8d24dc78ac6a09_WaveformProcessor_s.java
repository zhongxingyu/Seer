 package com.github.kaeppler.sawtoothtapestry;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.LinearGradient;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Shader.TileMode;
 import android.util.DisplayMetrics;
 
 public class WaveformProcessor {
 
     private Resources resources;
     private int screenWidth;
 
     public WaveformProcessor(Context context) {
         this.resources = context.getResources();
 
         DisplayMetrics dm = resources.getDisplayMetrics();
         screenWidth = dm.widthPixels;
     }
 
     public Bitmap process(Bitmap source) {
         Bitmap scaledSource = Bitmap.createScaledBitmap(source, source.getWidth(),
                 source.getHeight(), true);
         Bitmap targetWaveform = Bitmap.createBitmap(scaledSource.getWidth(),
                 scaledSource.getHeight(), Bitmap.Config.ARGB_8888);
 
         Canvas canvas = new Canvas(targetWaveform);
         applyGradient(targetWaveform, canvas);
         canvas.drawBitmap(source, new Matrix(), null);
 
        replaceColor(targetWaveform, 0xffefefef, Color.TRANSPARENT);
 
         return targetWaveform;
     }
 
    private void replaceColor(Bitmap targetWaveform, int sourceColor, int targetColor) {
         int length = targetWaveform.getWidth() * targetWaveform.getHeight();
         int[] pixels = new int[length];
         targetWaveform.getPixels(pixels, 0, targetWaveform.getWidth(), 0, 0,
                 targetWaveform.getWidth(), targetWaveform.getHeight());
         for (int i = 0; i < length; i++) {
            if (pixels[i] == sourceColor) {
                 pixels[i] = targetColor;
             }
         }
         targetWaveform.setPixels(pixels, 0, targetWaveform.getWidth(), 0, 0,
                 targetWaveform.getWidth(), targetWaveform.getHeight());
     }
 
     private void applyGradient(Bitmap targetWaveform, Canvas canvas) {
         Paint gradientPaint = new Paint();
         int startColor = resources.getColor(R.color.soundcloud_orange_light);
         int centerColor = Color.WHITE;
         int endColor = resources.getColor(R.color.soundcloud_orange_dark);
         LinearGradient gradient = new LinearGradient(0, 0, 0, targetWaveform.getHeight(),
                 new int[] { startColor, startColor, centerColor, endColor, endColor }, new float[] {
                         0, 0.4f, 0.5f, 0.6f, 1 }, TileMode.REPEAT);
         gradientPaint.setShader(gradient);
         canvas.drawPaint(gradientPaint);
     }
 }
