 /**
  * Copyright (C) 2010 cbrew <cbrew@acm.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package edu.osu.nlp.chartparser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The extent of a tree is represented as list of pairs
  * with each pair corresponding to the left and right edge
  * of a layer of the tree.
  * @author cbrew
  */
 public final class Extent {
     /**
      * The layers of the tree.
      */
     private List<ExtentPair> elements;
 
     /**
      * this constructor prevents default constructor
      * from becoming available.
      */
     private Extent() { }
     /**
      * Private constructor for creating new extents.
      * @param elems the elements of the extent.
      */
     private Extent(final List<ExtentPair> elems) {
         elements = elems;
     }
 
     /**
      * the elements of the extent.
      */
     class ExtentPair {
         /**
          * left edge of extent.
          */
         private double left;
         /**
          * right edge of extent.
          */
         private double right;
 
         /**
          * constructor.
          * @param l the left edge
          * @param r the right edge
          */
         ExtentPair(final double l, final double r) { left = l; right = r; }
 
         /**
          * @return the left
          */
         public double getLeft() {
             return left;
         }
 
         /**
          * @return the right
          */
         public double getRight() {
             return right;
         }
 
     }
 
     /**
      * shift an extent horizontally.
      * @param e  the extent to shift
      * @param shift the amount to shift
      * @return a shifted version of the extent
      */
     Extent move(final Extent e, final double shift) {
         ArrayList<ExtentPair> r = new ArrayList<ExtentPair>();
         for (ExtentPair p : e.elements) {
             r.add(new ExtentPair(
                         p.getLeft() + shift,
                         p.getRight() + shift));
         }
 
         return new Extent(r);
     }
     /**
      * Merge the extent with another one to its right.
      * Note that you have to be careful to handle case of
      * mismatched heights, which lead to trailing elements in one or the
      * other of the lists.
      * @param r right extent
      * @return the merged extent
      */
     Extent merge(final Extent r) {
         Extent l = this;
         ArrayList<ExtentPair> res = new ArrayList<ExtentPair>();
         int numShared = Math.min(l.elements.size(), r.elements.size());
 
         for (int i = 0; i < numShared; i++) {
             // add shared layers with appropriate left and right edges
             res.add(new ExtentPair(
                         l.elements.get(i).getLeft(),
                         r.elements.get(i).getRight()));
         }
 
         // add any trailing elements from l
         res.addAll(numShared, l.elements);
         // add any trailing elements from r
         // either this or the last call will wind
         // up doing nothing.
         res.addAll(numShared, r.elements);
         return new Extent(res);
     }
 
     /**
      * Merge a list of extents.
      * @param extents the list of extents to merge
      * @return the merged extent
      */
     Extent merge(final List<Extent> extents) {
         Extent r = new Extent(new ArrayList<ExtentPair>());
         for (Extent e : extents) {
             r = r.merge(e);
         }
         return r;
     }
 
     /**
      * Find out how close a tree may be placed to right
      * of this one.
     * @param e the extent that is needed.
      * @return size of gap
      */
 
    double fit(final Extent e) {
         double gap = 0.0;
         List<ExtentPair> l = this.elements;
         List<ExtentPair> r = e.elements;
 
         int numShared = Math.min(l.size(), r.size());
 
         for (int i = 0; i < numShared; i++) {
            gap = Math.max(gap, l.get(i).getRight()
                    + TreeLayout.HGAP - r.get(i).getLeft());
         }
 
         return gap;
     }
 }
