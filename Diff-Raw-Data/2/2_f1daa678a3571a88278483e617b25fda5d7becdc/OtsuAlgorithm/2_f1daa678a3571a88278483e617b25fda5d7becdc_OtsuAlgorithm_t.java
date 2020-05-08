 package ru.aifgi.recognizer.model.preprosessing;
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
 
 import com.google.common.math.DoubleMath;
 import ru.aifgi.recognizer.model.MathUtil;
 
 import java.math.RoundingMode;
 
 /**
  * @author aifgi
  */
 
 public class OtsuAlgorithm {
     private static int[] computeHistogram(final double[][] image) {
         final int[] histogram = new int[256];
         for (final double[] line : image) {
             for (final double value : line) {
                final int brightness = DoubleMath.roundToInt(value, RoundingMode.HALF_UP);
                 ++histogram[brightness];
             }
         }
         return histogram;
     }
 
     public static int thresholding(final double[][] image) {
         final int[] histogram = computeHistogram(image);
 
         final int length = histogram.length;
         int m = 0;
         int n = 0;
         for (int i = 0; i < length; ++i) {
             final int value = histogram[i];
             m += value;
             n += i * value;
         }
 
         double maxSigma = Double.MIN_VALUE;
         int threshold = 0;
         int brightnessesNumberSum = 0;
         int brightnessesSum = 0;
         for (int i = 0; i < length; ++i) {
             final int value = histogram[i];
             brightnessesNumberSum += value;
             brightnessesSum += i * value;
 
             final double probability = brightnessesNumberSum / (double) m;
             final double firstMeanValue = brightnessesSum / (double) brightnessesNumberSum;
             final double secondMeanValue = (n - brightnessesSum) / (double) (m - brightnessesNumberSum);
             final double sigma = probability * (1 - probability) * MathUtil.sqr(firstMeanValue - secondMeanValue);
             if (sigma > maxSigma) {
                 maxSigma = sigma;
                 threshold = i;
             }
         }
         return threshold;
     }
 
     private OtsuAlgorithm() {
     }
 }
