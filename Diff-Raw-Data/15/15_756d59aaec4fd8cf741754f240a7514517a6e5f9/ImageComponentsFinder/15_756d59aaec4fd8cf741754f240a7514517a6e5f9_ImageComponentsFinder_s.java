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
 
 import ru.aifgi.recognizer.api.Rectangle;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author aifgi
  */
 
 public class ImageComponentsFinder {
     private final double[][] myImage;
     private final int[][] myMask;
 
     public ImageComponentsFinder(final double[][] image) {
         myImage = image;
         myMask = new int[myImage.length][myImage[0].length];
         computeMask();
     }
 
     private void computeMask() {
         final int width = myImage.length;
         final int height = myImage[0].length;
         int count = 0;
         for (int i = 0; i < width; ++i) {
             final int[] line = myMask[i];
             for (int j = 0; j < height; ++j) {
                 if (myImage[i][j] != 0) {
                     final int value = findMinimumNeighbour(i, j);
                     line[j] = (value == 0) ? ++count : value;
                 }
             }
         }
 
         // TODO: count is very big. Better performance?
         final int[] mergedComponents = new int[count + 1];
         for (int i = 0; i < width; ++i) {
             final int[] line = myMask[i];
             for (int j = 0; j < height; ++j) {
                 int t = line[j];
                 final int component = mergedComponents[t];
                 if (component != 0) {
                     line[j] = component;
                 }
                 else {
                     final int minimumNeighbour = findMinimumNeighbour(i, j);
                     if (minimumNeighbour != 0) {
                         line[j] = minimumNeighbour;
                         mergedComponents[t] = minimumNeighbour;
                     }
                 }
             }
         }
     }
 
     private int findMinimumNeighbour(final int x, final int y) {
        int res = 0;
         for (int i = -1; i <= 1; ++i) {
             for (int j = -1; j <= 1; ++j) {
                 final int value = getValue(x + i, y + j);
                 if (value != 0) {
                     res = Math.min(res, value);
                 }
             }
         }
        return res;
     }
 
     private int getValue(final int x, final int y) {
        if (x < 0 || y < 0) {
             return 0;
         }
         return myMask[x][y];
     }
 
     public int[][] getConnectedComponentsMask() {
         return myMask;
     }
 
     public Collection<Rectangle> getWords() {
         return Collections.unmodifiableCollection(mergeComponentsToWords());
     }
 
     private List<Rectangle> mergeComponentsToWords() {
         return null;  //To change body of created methods use File | Settings | File Templates.
     }
 }
