 package com.adaburrows.superpowers;
 
 import java.io.IOException;
 
 import android.util.Log;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.os.Bundle;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.ViewGroup.LayoutParams;
 import android.media.AudioTrack;
 import android.media.AudioManager;
 import android.media.AudioFormat;
 
 
 class AudioSynth implements Camera.PreviewCallback {
 
   // Member vars
   private static final String TAG = "AudioSynth";
 
   boolean mFinished;
   int[] mRGBData;
   int mImageWidth, mImageHeight;
   int[] mRedHistogram;
   int[] mGreenHistogram;
   int[] mBlueHistogram;
   double[] mBinSquared;
   AudioTrack mRedSynthesizer;
   AudioTrack mGreenSynthesizer;
   AudioTrack mBlueSynthesizer;
   private final int sampleRate = 44100;
   private final int maxSamples = 10 * 1470;
   private int numRedSamples;
   private int numGreenSamples;
   private int numBlueSamples;
   private double sample[];
   private short redGeneratedSnd[];
   private short greenGeneratedSnd[];
   private short blueGeneratedSnd[];
 
 
   // Constructor
   public AudioSynth(AudioTrack redSynthesizer, AudioTrack greenSynthesizer, AudioTrack blueSynthesizer) {
     mFinished = false;
     mRGBData = null;
     mRedSynthesizer = redSynthesizer;
     mGreenSynthesizer = greenSynthesizer;
     mBlueSynthesizer = blueSynthesizer;
     redGeneratedSnd = new short[maxSamples];
     greenGeneratedSnd = new short[maxSamples];
     blueGeneratedSnd = new short[maxSamples];
     mRedHistogram = new int[256];
     mGreenHistogram = new int[256];
     mBlueHistogram = new int[256];
     mBinSquared = new double[256];
     for (int bin = 0; bin < 256; bin++)
     {
       mBinSquared[bin] = ((double)bin) * bin;
     }
   }
 
   @Override
   public void onPreviewFrame(byte[] data, Camera camera) {
     // Only run if we're not finished.
     if ( mFinished )
       return;
 
     Camera.Parameters params = camera.getParameters();
     mImageWidth = params.getPreviewSize().width;
     mImageHeight = params.getPreviewSize().height;
     mRGBData = new int[mImageWidth * mImageHeight];
 
     // Convert from YUV to RGB
     decodeYUV420SP(mRGBData, data, mImageWidth, mImageHeight);
 
     // Calculate histogram
     calculateIntensityHistogram(mRGBData, mRedHistogram, 
       mImageWidth, mImageHeight, 0);
     calculateIntensityHistogram(mRGBData, mGreenHistogram, 
       mImageWidth, mImageHeight, 1);
     calculateIntensityHistogram(mRGBData, mBlueHistogram, 
       mImageWidth, mImageHeight, 2);
 
     // Calculate mean
     double imageRedMean = 0, imageGreenMean = 0, imageBlueMean = 0;
     double redHistogramSum = 0, greenHistogramSum = 0, blueHistogramSum = 0;
     for (int bin = 0; bin < 256; bin++) {
       imageRedMean += mRedHistogram[bin] * bin;
       redHistogramSum += mRedHistogram[bin];
       imageGreenMean += mGreenHistogram[bin] * bin;
       greenHistogramSum += mGreenHistogram[bin];
       imageBlueMean += mBlueHistogram[bin] * bin;
       blueHistogramSum += mBlueHistogram[bin];
     }
     imageRedMean /= redHistogramSum;
     imageGreenMean /= greenHistogramSum;
     imageBlueMean /= blueHistogramSum;
 
     // Test of all the above, creates lots of log messages!!!
     //Log.i(TAG, "Mean (R,G,B): " + String.format("%.4g", imageRedMean) + ", " + String.format("%.4g", imageGreenMean) + ", " + String.format("%.4g", imageBlueMean));
 
     // This is where you'll use imageRedMean, imageGreenMean, and imageBlueMean to
     // construct a waveform based on the aggregate channel luminosities.
 
     double maxFreq = 5200;
     double minFreq = 3900;
     double redVolume = imageRedMean / 255;
     double redFrequency = minFreq + ((maxFreq - minFreq) * redVolume);
     double greenVolume = imageGreenMean / 255;
     double greenFrequency = minFreq + ((maxFreq - minFreq) * greenVolume);
     double blueVolume = imageBlueMean / 255;
     double blueFrequency = minFreq + ((maxFreq - minFreq) * blueVolume);
     
     genTones(redFrequency, redVolume, greenFrequency, greenVolume, blueFrequency, blueVolume);
     playSound();
 
   }
 
   void genTones(double redFrequency, double redVolume, double greenFrequency, double greenVolume, double blueFrequency, double blueVolume){
     // Compute the red channel
     numRedSamples = computeSamples(redGeneratedSnd, redFrequency, redVolume);
 
     // Compute the green channel
     numGreenSamples = computeSamples(greenGeneratedSnd, greenFrequency, greenVolume);
 
     // Compute the blue channel
     numBlueSamples = computeSamples(blueGeneratedSnd, blueFrequency, blueVolume);
   }
 
   int computeSamples(short[] generatedSnd, double frequency, double volume) {
     //Log.i("samples", "frequency = " + frequency + " volume = " + volume);
    double period = 1.0 / frequency;
     double adjustedDuration = (int)((1.0/3)/period)*period;
     int numSamples = (int)(adjustedDuration * sampleRate);
     //Log.i("samples", "adjustedDuration = " + adjustedDuration + " numSamples = " + numSamples);
     sample = new double[numSamples];
 
     // fill out the blue array
     for (int i = 0; i < numSamples; ++i) {
         sample[i] = volume * Math.sin(2 * Math.PI * i / (sampleRate/frequency));
     }
 
     // convert to 16 bit pcm sound array
     // assumes the sample buffer is normalised.
     int idx = 0;
     for (final double dVal : sample) {
         // scale to maximum amplitude
         final short val = (short) ((dVal * 16384));
         generatedSnd[idx++] = val;
     }
     return numSamples;
   }
 
   void playSound() {
       mRedSynthesizer.write(redGeneratedSnd, 0, numRedSamples);
       mGreenSynthesizer.write(greenGeneratedSnd, 0, numGreenSamples);
       mBlueSynthesizer.write(blueGeneratedSnd, 0, numBlueSamples);
   }
 
 
   // Decode YUV420SP colorspace to RGB
   static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
     // Calculate frame size
     final int frameSize = width * height;
 
     // Lets do this with a lot of bit twiddling for some speed
     for (int j = 0, yp = 0; j < height; j++) {
       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
       for (int i = 0; i < width; i++, yp++) {
         // Get luminosity and set uv values
         int y = (0xff & ((int) yuv420sp[yp])) - 16;
         if (y < 0) y = 0;
         if ((i & 1) == 0) {
           v = (0xff & yuv420sp[uvp++]) - 128;
           u = (0xff & yuv420sp[uvp++]) - 128;
         }
 
         // Actual converstion equations
         int y1192 = 1192 * y;
         int r = (y1192 + 1634 * v);
         int g = (y1192 - 833 * v - 400 * u);
         int b = (y1192 + 2066 * u);
 
         // Must be within a certain range and we don't care about errors.
         if (r < 0) r = 0; else if (r > 262143) r = 262143;
         if (g < 0) g = 0; else if (g > 262143) g = 262143;
         if (b < 0) b = 0; else if (b > 262143) b = 262143;
 
         // Pack it into bytes
         rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
       }
     }
   }
 
   static public void calculateIntensityHistogram(int[] rgb, int[] histogram, int width, int height, int component) {
     // Zero histogram bins
     for (int bin = 0; bin < 256; bin++) {
       histogram[bin] = 0;
     }
 
     // RED
     if (component == 0) {
       for (int pix = 0; pix < width*height; pix += 3) {
         int pixVal = (rgb[pix] >> 16) & 0xff; // bitshift and mask to get red value
         histogram[ pixVal ]++;
       }
     }
 
     // GREEN
     else if (component == 1) {
       for (int pix = 0; pix < width*height; pix += 3) {
         int pixVal = (rgb[pix] >> 8) & 0xff; // bitshift and mask to get green value
         histogram[ pixVal ]++;
       }
     }
 
     // Must be BLUE
     else {
       for (int pix = 0; pix < width*height; pix += 3) {
         int pixVal = rgb[pix] & 0xff; // mask for blue value
         histogram[ pixVal ]++;
       }
     }
   }
 }
