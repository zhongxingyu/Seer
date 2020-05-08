 package ru.aifgi.recognizer.view;
 
 /*
  * Copyright 2012 Alexey Ivanov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import ru.aifgi.recognizer.api.ImageWrapper;
 
 import java.awt.image.BufferedImage;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * @author aifgi
  */
 
 public class BufferedImageWrapper implements ImageWrapper {
     private final BufferedImage myBufferedImage;
     private final Lock myCacheLock = new ReentrantLock();
     private double[][] myBrightnessesCache;
 
     public BufferedImageWrapper(final BufferedImage bufferedImage) {
         myBufferedImage = bufferedImage;
     }
 
     @Override
     public int getWidth() {
         return myBufferedImage.getWidth();
     }
 
     @Override
     public int getHeight() {
         return myBufferedImage.getHeight();
     }
 
     @Override
     public double[][] getBrightnesses() {
         if (myBrightnessesCache == null) {
             if (myCacheLock.tryLock()) {
                 try {
                     computeBrightnesses();
                 }
                 finally {
                     myCacheLock.unlock();
                 }
             }
         }
         return myBrightnessesCache;
     }
 
     private void computeBrightnesses() {
         final int width = myBufferedImage.getWidth();
         final int height = myBufferedImage.getHeight();
         myBrightnessesCache = new double[width][height];
         for (int i = 0; i < width; ++i) {
             for (int j = 0; j < height; ++j) {
                 myBrightnessesCache[i][j] = getBrightness(i, j);
             }
         }
     }
 
     private double getBrightness(final int x, final int y) {
         final int rgb = myBufferedImage.getRGB(x, y);
         final int red = getRed(rgb);
         final int green = getGreen(rgb);
         final int blue = getBlue(rgb);
        return 0.299 * red + 0.587 * green + 0.114 * blue;
     }
 
     private static int getRed(final int rgb) {
         return doGet(rgb, 16);
     }
 
     private static int getGreen(final int rgb) {
         return doGet(rgb, 8);
     }
 
     private static int getBlue(final int rgb) {
         return doGet(rgb, 0);
     }
 
     private static int doGet(final int rgb, final int s) {
         return (rgb >> s) & 0xFF;
     }
 }
