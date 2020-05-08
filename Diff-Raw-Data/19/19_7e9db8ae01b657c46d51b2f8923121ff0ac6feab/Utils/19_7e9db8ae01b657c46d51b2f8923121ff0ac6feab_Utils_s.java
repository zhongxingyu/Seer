 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.albaniliu.chuangxindemo.util;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Matrix;
 
 public class Utils {
     private static final int UNCONSTRAINED = -1;
 
     public static int dip2px(Context context, float dipValue) {
         final float scale = context.getResources().getDisplayMetrics().density;
         return (int) (dipValue * scale + 0.5f);
     }
     
     public static Bitmap rotate(Bitmap b, int degrees) {
         if (degrees != 0 && b != null) {
             Matrix m = new Matrix();
             m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
             try {
                 Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                 if (b != b2) {
                     b.recycle();
                     b = b2;
                 }
             } catch (OutOfMemoryError ex) {
                 // We have no memory to rotate. Return the original bitmap.
             }
         }
         return b;
     }
 
     public static final Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
         int srcWidth = bitmap.getWidth();
         int srcHeight = bitmap.getHeight();
         int width = maxSize;
         int height = maxSize;
         boolean needsResize = false;
         if (srcWidth > srcHeight) {
             if (srcWidth > maxSize) {
                 needsResize = true;
                 height = ((maxSize * srcHeight) / srcWidth);
             }
         } else {
             if (srcHeight > maxSize) {
                 needsResize = true;
                 width = ((maxSize * srcWidth) / srcHeight);
             }
         }
         if (needsResize) {
             Bitmap retVal = Bitmap.createScaledBitmap(bitmap, width, height, true);
             return retVal;
         } else {
             return bitmap;
         }
     }
 
     private static final long POLY64REV = 0x95AC9329AC4BC9B5L;
     private static final long INITIALCRC = 0xFFFFFFFFFFFFFFFFL;
 
     private static boolean init = false;
     private static long[] CRCTable = new long[256];
 
     /**
      * A function thats returns a 64-bit crc for string
      * 
      * @param in
      *            : input string
      * @return 64-bit crc value
      */
     public static final long Crc64Long(String in) {
         if (in == null || in.length() == 0) {
             return 0;
         }
         // http://bioinf.cs.ucl.ac.uk/downloads/crc64/crc64.c
         long crc = INITIALCRC, part;
         if (!init) {
             for (int i = 0; i < 256; i++) {
                 part = i;
                 for (int j = 0; j < 8; j++) {
                     int value = ((int) part & 1);
                     if (value != 0)
                         part = (part >> 1) ^ POLY64REV;
                     else
                         part >>= 1;
                 }
                 CRCTable[i] = part;
             }
             init = true;
         }
         int length = in.length();
         for (int k = 0; k < length; ++k) {
             char c = in.charAt(k);
             crc = CRCTable[(((int) crc) ^ c) & 0xff] ^ (crc >> 8);
         }
         return crc;
     }
 
     /*
      * Compute the sample size as a function of minSideLength
      * and maxNumOfPixels.
      * minSideLength is used to specify that minimal width or height of a
      * bitmap.
      * maxNumOfPixels is used to specify the maximal size in pixels that is
      * tolerable in terms of memory usage.
      *
      * The function returns a sample size based on the constraints.
      * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
      * which indicates no care of the corresponding constraint.
      * The functions prefers returning a sample size that
      * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
      *
      * Also, the function rounds up the sample size to a power of 2 or multiple
      * of 8 because BitmapFactory only honors sample size this way.
      * For example, BitmapFactory downsamples an image by 2 even though the
      * request is 3. So we round up the sample size to avoid OOM.
      */
     public static int computeSampleSize(BitmapFactory.Options options,
             int minSideLength, int maxNumOfPixels) {
         int initialSize = computeInitialSampleSize(options, minSideLength,
                 maxNumOfPixels);
 
         int roundedSize;
         if (initialSize <= 8 ) {
             roundedSize = 1;
             while (roundedSize < initialSize) {
                 roundedSize <<= 1;
             }
         } else {
             roundedSize = (initialSize + 7) / 8 * 8;
         }
 
         return roundedSize;
     }
 
     public static int computeInitialSampleSize(BitmapFactory.Options options,
             int minSideLength, int maxNumOfPixels) {
         double w = options.outWidth;
         double h = options.outHeight;
 
         int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                 (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
         int upperBound = (minSideLength == UNCONSTRAINED) ? 128 :
                 (int) Math.min(Math.floor(w / minSideLength),
                 Math.floor(h / minSideLength));
 
         if (upperBound < lowerBound) {
             // return the larger one when there is no overlapping zone.
             return lowerBound;
         }
 
         if ((maxNumOfPixels == UNCONSTRAINED) &&
                 (minSideLength == UNCONSTRAINED)) {
             return 1;
         } else if (minSideLength == UNCONSTRAINED) {
             return lowerBound;
         } else {
             return upperBound;
         }
     }
 
     private static final int MAX_PIX = 1 * 1024 * 1024;
     public static Bitmap createBitmapByFilePath(String path, int maxWidth) {
         Options opts = new Options();
         opts.inJustDecodeBounds = true;
         try {
             BitmapFactory.decodeFile(path, opts);
         } catch (OutOfMemoryError e) {
             return null;
         }
         if (opts.outWidth <= 0 || opts.outHeight <= 0)
             return null;
         int sampleSize = 1;
         while (opts.outWidth * opts.outHeight > MAX_PIX * sampleSize * sampleSize
                 || opts.outWidth > maxWidth * 2 * sampleSize) {
             sampleSize <<= 1;
             if (sampleSize <= 0) {
                 return null;
             }
         }
         int w = sampleSize == 1 ? maxWidth : opts.outWidth / sampleSize;
         int h = w * opts.outHeight / opts.outWidth;
 
         opts = new Options();
         opts.outHeight = h;
         opts.outWidth = w;
         opts.inJustDecodeBounds = false;
         opts.inSampleSize = sampleSize;
         opts.inPreferredConfig = Config.ARGB_8888;
         Bitmap raw = null;
         Bitmap org = null;
         try {
             org = BitmapFactory.decodeFile(path, opts);
             raw = Bitmap.createScaledBitmap(org, w, h, true);
             if (raw != org) {
                 org.recycle();
             }
         } catch (OutOfMemoryError e) {
             if (org != null) {
                 return org;
             }
         }
         return raw;
     }
 }
