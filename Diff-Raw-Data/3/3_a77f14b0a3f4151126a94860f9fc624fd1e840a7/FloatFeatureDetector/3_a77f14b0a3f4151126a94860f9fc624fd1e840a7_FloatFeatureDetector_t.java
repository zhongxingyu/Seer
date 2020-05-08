 /**
  * Copyright (c) 2013, Martin Pecka (peci1@seznam.cz)
  * All rights reserved.
  * Licensed under the following BSD License.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * 
  * Neither the name Martin Pecka nor the
  * names of contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package cz.cuni.mff.peckam.ais.detection;
 
 import java.awt.Point;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import cz.cuni.mff.peckam.ais.Ionogram;
 import cz.cuni.mff.peckam.ais.Product;
 
 /**
  * Floating point numbers feature detector.
  * 
  * @author Martin Pecka
  */
 public abstract class FloatFeatureDetector extends FeatureDetectorBase<Float>
 {
 
     @Override
     protected boolean canHaveFeatures(Product<Float, ?, ?> product)
     {
         final Float[][] data = product.getData();
         float sum = 0;
         for (int x = 0; x < data.length; x++) {
             for (int y = 0; y < data[0].length; y++) {
                 sum += data[x][y];
             }
         }
         final float mean = sum / (data.length * data[0].length);
         return mean >= 2.45216E-16;
     }
 
     @Override
     public DetectionResult detectFeatures(Product<Float, ?, ?> product)
     {
         final DetectionResult result = super.detectFeatures(product);
 
         if (product instanceof Ionogram) {
             final Ionogram iono = (Ionogram) product;
             if (iono.getAltitude() != null) {
                 final DetectedFeature echo = detectGroundEcho(iono, iono.getAltitude());
                 if (echo != null) {
                     result.addFeature(echo);
                     result.readProductData(iono);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * @param product The product.
      * @param altitude Altitude over ground in km.
      * @return The ground echo.
      */
     protected DetectedFeature detectGroundEcho(Ionogram product, float altitude)
     {
         final Float[][] data = product.getData();
         final float timeDelay = 2 * altitude / 300; // 300 for speed of light; timeDelay in ms
 
         if (timeDelay < Ionogram.MIN_DELAY_TIME || timeDelay > Ionogram.MAX_DELAY_TIME)
             return null;
 
         final int yPosition = product.getDataPosition(timeDelay, (float) Ionogram.MAX_FREQUENCY).x;
 
         float noEchoSum = 0, echoSum = 0;
         int noEchoCount = 0, echoCount = 0;
 
         for (int x = data.length / 2; x < data.length; x++) {
             for (int y = 0; y < data[0].length; y++) {
                 final float val = data[x][y];
                 if (y >= yPosition && y < yPosition + 20) {
                     echoSum += val;
                     echoCount++;
                 } else {
                     noEchoSum += val;
                     noEchoCount++;
                 }
             }
         }
 
         if (echoCount == 0 || noEchoCount == 0) // should not happen
             return null;
 
         final float echoMean = echoSum / echoCount;
         final float noEchoMean = noEchoSum / noEchoCount;
 
         if (echoMean > 2 * noEchoMean) {
             final List<Point> points = new LinkedList<>();
             int y = yPosition;
             final Float[] colKeys = product.getOriginalColumnKeys();
             for (int xx = colKeys.length - 1; xx >= 0; xx--) {
                 final int x = product.getDataPosition((float) Ionogram.MIN_DELAY_TIME,
                         (float) Math.min(colKeys[xx], Ionogram.MAX_FREQUENCY)).y;
                 if (x < data.length / 2)
                     break;
 
                 float max = 0;
                 int maxY = -1;
                 for (int yy = Math.min(y + 10, data[0].length - 1); yy >= y - 10; yy--) {
                     if (data[x][yy] > max) {
                         max = data[x][yy];
                         maxY = yy;
                     }
                 }
 
                 if (maxY < 0)
                     break;
 
                 y = maxY;
                 if (max >= echoMean)
                     points.add(new Point(x, y));
             }
 
            if (points.isEmpty())
                return null;

             Collections.reverse(points);
             return new GroundEcho(points.toArray(new Point[0]));
         }
 
         return null;
     }
 
 }
