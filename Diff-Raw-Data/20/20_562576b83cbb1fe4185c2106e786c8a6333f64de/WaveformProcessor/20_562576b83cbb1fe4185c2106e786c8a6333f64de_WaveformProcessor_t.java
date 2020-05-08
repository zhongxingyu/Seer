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
 
 public class WaveformProcessor {
 
     private Resources resources;
 
     public WaveformProcessor(Context context) {
         this.resources = context.getResources();
     }
 
     public Bitmap process(Bitmap source) {
        int scaledWidth = resources.getDimensionPixelSize(R.dimen.waveform_width);
        int scaledHeight = resources.getDimensionPixelSize(R.dimen.waveform_height);
        Bitmap scaledSource = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, false);
        Bitmap targetWaveform = Bitmap.createBitmap(scaledWidth, scaledHeight,
                Bitmap.Config.ARGB_8888);
 
         Canvas canvas = new Canvas(targetWaveform);
         applyGradient(targetWaveform, canvas);
        canvas.drawBitmap(scaledSource, new Matrix(), null);
 
         replaceColor(targetWaveform, Color.rgb(220, 220, 220), Color.rgb(240, 240, 240),
                 Color.TRANSPARENT);
 
         return targetWaveform;
     }
 
     private void replaceColor(Bitmap targetWaveform, int sourceColorLow, int sourceColorHigh,
             int targetColor) {
         int length = targetWaveform.getWidth() * targetWaveform.getHeight();
         int[] pixels = new int[length];
         targetWaveform.getPixels(pixels, 0, targetWaveform.getWidth(), 0, 0,
                 targetWaveform.getWidth(), targetWaveform.getHeight());
         for (int i = 0; i < length; i++) {
             if (inColorRange(pixels[i], sourceColorLow, sourceColorHigh)) {
                 pixels[i] = targetColor;
             }
         }
         targetWaveform.setPixels(pixels, 0, targetWaveform.getWidth(), 0, 0,
                 targetWaveform.getWidth(), targetWaveform.getHeight());
     }
 
     private boolean inColorRange(int color, int colorLow, int colorHigh) {
         int red = Color.red(color);
         int green = Color.green(color);
         int blue = Color.blue(color);
         return red >= Color.red(colorLow) && red <= Color.red(colorHigh)
                 && green >= Color.green(colorLow) && green <= Color.green(colorHigh)
                 && blue >= Color.blue(colorLow) && blue <= Color.blue(colorHigh);
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
