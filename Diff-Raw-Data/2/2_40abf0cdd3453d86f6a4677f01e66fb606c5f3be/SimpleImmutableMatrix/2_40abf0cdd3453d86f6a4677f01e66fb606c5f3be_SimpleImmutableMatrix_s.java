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
 
 /**
  * Simple Matrix Implementation. This version is immutable.
  *
  * Holds and m-by-n matrix where,
  *
  * <pre>
  *         n1  n2  n3  ... nN
  *      +---------------------
  *   m1 | a11 a12 a13 ... aN1
  *   m2 | a21 a22 a23 ... aN1
  *   m3 | a31 a32 a33 ... aN1
  *   .. | ... ... ...     ...
  *   mN | aM1 aM2 aM3 ... aMN
  * </pre>
  *
  * @author openecho
  * @version 1.0.0
  */
 public class SimpleImmutableMatrix {
     /**
      * m dimension
      */
     private final int m;
     /**
      * n dimension
      */
     private final int n;
     /**
      * matrix data
      */
     private final double[][] data;
 
     public SimpleImmutableMatrix(int m, int n) {
         this.m = m;
         this.n = n;
         data = new double[m][n];
     }
 
     public SimpleImmutableMatrix(double[][] data) {
         m = data.length;
         n = data[0].length;
         this.data = new double[m][n];
         for (int i = 0; i < m; i++) {
             System.arraycopy(data[i], 0, this.data[i], 0, n);
         }
     }
 
     public int getM() {
         return m;
     }
 
     public int getN() {
         return n;
     }
 
     public double[][] getData() {
         double[][] data = new double[m][n];
         for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, n);
         }
         return data;
     }
 
     public double[] getRow(int i) {
         if(i>=m) {
             throw new IndexOutOfBoundsException();
         }
         return data[i];
     }
 
     public double[] getColumn(int i) {
         if(i>=n) {
             throw new IndexOutOfBoundsException();
         }
         double[] result = new double[m];
         for(int j=0;j<m;j++) {
             result[j]=data[j][i];
         }
         return result;
     }
 
     public boolean equals(SimpleImmutableMatrix b) {
         SimpleImmutableMatrix a = this;
         for(int i=0;i<m;i++) {
             for(int j=0;j<n;j++) {
                 if(a.data[i][j]!=b.data[i][j]) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     public SimpleImmutableMatrix add(SimpleImmutableMatrix b) {
         SimpleImmutableMatrix a = this;
         if(a.m != b.m || a.n != b.n) {
             throw new RuntimeException("Matrix dimensions are not equal.");
         }
         SimpleImmutableMatrix c = new SimpleImmutableMatrix(m,n);
         for(int i=0;i<m;i++) {
             for(int j=0;j<n;j++) {
                 c.data[i][j]=a.data[i][j]+b.data[i][j];
             }
         }
         return c;
     }
 
     public SimpleImmutableMatrix subtract(SimpleImmutableMatrix b) {
         SimpleImmutableMatrix a = this;
         if(a.m != b.m || a.n != b.n) {
             throw new RuntimeException("Matrix dimensions are not equal.");
         }
         SimpleImmutableMatrix c = new SimpleImmutableMatrix(m,n);
         for(int i=0;i<m;i++) {
             for(int j=0;j<n;j++) {
                 c.data[i][j]=a.data[i][j]-b.data[i][j];
             }
         }
         return c;
     }
 
     public SimpleImmutableMatrix multiply(SimpleImmutableMatrix b) {
         SimpleImmutableMatrix a = this;
         if(a.n != b.m) {
             throw new RuntimeException("Matrix dimensions are not incorrect.");
         }
         SimpleImmutableMatrix c = new SimpleImmutableMatrix(a.m, b.n);
         for(int i=0;i<c.m;i++) {
             for(int j=0;j<c.n;j++) {
                 for(int k=0;k<a.n;k++) {
                     c.data[i][j] += (a.data[i][k]*b.data[k][j]);
                 }
             }
         }
         return c;
     }
 
     public SimpleImmutableMatrix addScalar(double v) {
         SimpleImmutableMatrix a = this;
         SimpleImmutableMatrix c = new SimpleImmutableMatrix(m, n);
         for(int i=0;i<m;i++) {
             for(int j=0;j<n;j++) {
                 c.data[i][j]=a.data[i][j]+v;
             }
         }
         return c;
     }
 
     public SimpleImmutableMatrix subtractScalar(double v) {
         SimpleImmutableMatrix a = this;
         SimpleImmutableMatrix c = new SimpleImmutableMatrix(m, n);
         for(int i=0;i<m;i++) {
             for(int j=0;j<n;j++) {
                 c.data[i][j]=a.data[i][j]-v;
             }
         }
         return c;
     }
 
     public SimpleImmutableMatrix multiplyScalar(double v) {
         SimpleImmutableMatrix a = this;
         SimpleImmutableMatrix c = new SimpleImmutableMatrix(m, n);
         for(int i=0;i<m;i++) {
             for(int j=0;j<n;j++) {
                 c.data[i][j]=a.data[i][j]*v;
             }
         }
         return c;
     }
 
     public SimpleImmutableMatrix divideScalar(double v) {
         if(v==0) {
             throw new RuntimeException("Divide by Zero");
         }
         SimpleImmutableMatrix a = this;
         SimpleImmutableMatrix c = new SimpleImmutableMatrix(m, n);
         for(int i=0;i<m;i++) {
             for(int j=0;j<n;j++) {
                 c.data[i][j]=a.data[i][j]/v;
             }
         }
         return c;
     }
 
     @Override
     public String toString() {
         String dataString = "{";
         for(int i=0;i<m;i++) {
             dataString+="{";
             for(int j=0;j<n;j++) {
                 dataString += data[i][j]+((j<n-1)?",":"");
             }
             dataString+="}"+((i<m-1)?",":"");
         }
         dataString+="}";
         return String.format("%s %s", super.toString(),dataString);
     }
 
     public static SimpleImmutableMatrix transpose(SimpleImmutableMatrix a) {
         SimpleImmutableMatrix t = new SimpleImmutableMatrix(a.n, a.m);
         for (int i = 0; i < a.m; i++) {
             for (int j = 0; j < a.n; j++) {
                 t.data[j][i] = a.data[i][j];
             }
         }
         return t;
     }
 
     public static SimpleImmutableMatrix identity(int n) {
         SimpleImmutableMatrix i = new SimpleImmutableMatrix(n, n);
         for (int j = 0; j < n; j++) {
             i.data[j][j] = 1;
         }
         return i;
     }
 
     public static SimpleImmutableMatrix random(int m, int n) {
         SimpleImmutableMatrix r = new SimpleImmutableMatrix(m, n);
         for (int i = 0; i < m; i++) {
             for (int j = 0; j < n; j++) {
                 r.data[i][j] = Math.random();
             }
         }
         return r;
     }
 
     public static SimpleImmutableMatrix random(int m, int n, double lowerBound, double higherBound) {
         SimpleImmutableMatrix r = new SimpleImmutableMatrix(m, n);
         for (int i = 0; i < m; i++) {
             for (int j = 0; j < n; j++) {
                 r.data[i][j] = (Math.random()*(higherBound-lowerBound))+lowerBound;
             }
         }
         return r;
     }
 
     public static SimpleImmutableMatrix generate(int m, int n, double v) {
         SimpleImmutableMatrix g = new SimpleImmutableMatrix(m, n);
         for (int i = 0; i < m; i++) {
             for (int j = 0; j < n; j++) {
                 g.data[i][j] = v;
             }
         }
         return g;
     }
 
     public static SimpleImmutableMatrix oneMatrix(int m, int n) {
         return generate(m,n,1D);
     }
 }
