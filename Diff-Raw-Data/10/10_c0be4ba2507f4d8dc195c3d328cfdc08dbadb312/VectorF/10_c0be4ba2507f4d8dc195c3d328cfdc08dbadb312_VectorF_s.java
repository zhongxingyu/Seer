 /**
  * Copyright (C) 2010 openecho
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
  **/
 package openecho.math;
 
 import openecho.math.random.FastRandom;
 
 /**
  *
  * @author openecho
  */
 public abstract class VectorF extends Vector {
      public VectorF(int n) {
         this(n, false);
     }
 
     public VectorF(int n, boolean mutable) {
         this.n = n;
         this.mutable = mutable;
     }
 
     public VectorF(Number[] data) {
         this(data, false);
     }
 
     public VectorF(Number[] data, boolean mutable) {
         n = data.length;
         this.mutable = mutable;
     }
 
     protected abstract void initData(Number[] data);
 
     protected abstract void initData(int i, Number data);
 
     public abstract Float[] getData();
 
     public abstract Float getData(int i);
 
     public abstract void setData(Number[] data);
 
     public abstract void setData(int i, Number data);
 
     public final Float magnitude() {
         VectorF a = this;
         double squaredSum = 0;
         for (int i = 0; i < n; i++) {
             squaredSum += Math.pow(a.getData(i), 2);
         }
         return (float) Math.sqrt(squaredSum);
     }
 
     @Override
     public final Float length() {
         return magnitude();
     }
 
     public abstract VectorF negative();
 
     public abstract VectorF normalise();
 
     public abstract VectorF add(Vector b);
 
     public abstract VectorF subtract(Vector b);
 
    public Double dot(Vector b) {
         if (n != b.n) {
             throw new RuntimeException("Vector dimensions are not equal.");
         }
        double dotProduct = 0;
         for (int i = 0; i < n; i++) {
             dotProduct += getData(i) * b.getData(i).doubleValue();
         }
         return dotProduct;
     }
 
     public abstract VectorF cross(Vector b);
 
     public abstract VectorF addScalar(Number v);
 
     public abstract VectorF subtractScalar(Number v);
 
     public abstract VectorF multiplyScalar(Number v);
 
     public abstract VectorF divideScalar(Number v);
 
     @Override
     public String toString() {
         String dataString = "{";
         for (int i = 0; i < n; i++) {
             dataString += getData(i) + ((i < n - 1) ? ", " : "");
         }
         dataString += "}";
         return String.format("%s %s", super.toString(), dataString);
     }
 
     public static VectorF empty(int i) {
         VectorD e = new ArrayVectorD(i);
         for(int j=0;j<i;j++) {
             e.setData(i, 0D);
         }
         return null;
     }
 
     public static VectorF random(int i) {
         VectorD r = new ArrayVectorD(i);
         for(int j=0;j<i;j++) {
            r.setData(i, FastRandom.getInstance().random());
         }
         return null;
     }
 
     public static VectorF zero() {
         return null;
     }
 }
