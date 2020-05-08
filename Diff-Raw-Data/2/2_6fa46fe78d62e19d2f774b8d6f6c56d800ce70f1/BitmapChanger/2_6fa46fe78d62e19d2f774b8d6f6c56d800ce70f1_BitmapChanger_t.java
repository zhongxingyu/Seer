 package com.group7.dragonwars.engine;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 
 public class BitmapChanger {
 
     private BitmapChanger() {}
 
     public static Bitmap changeColour(final Bitmap templateBitmap,
                                       final Integer originalColour,
                                       final Integer replacementColour) {
         Bitmap replacement = Bitmap.createBitmap(templateBitmap);
         Integer allPxs = replacement.getHeight() * replacement.getWidth();
 
         int[] allpixels = new int[allPxs];
 
        replacement.getPixels(allpixels, 0, replacement.getWidth(), 0, 0,
                               replacement.getWidth(),replacement.getHeight());
 
         for (Integer i = 0; i < allPxs; i++) {
             if (allpixels[i] == originalColour) {
                 allpixels[i] = replacementColour;
             }
         }
 
         replacement.setPixels(allpixels, 0, replacement.getWidth(), 0, 0,
                               replacement.getWidth(), replacement.getHeight());
 
         return replacement;
     }
 }
