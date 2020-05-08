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
  * Performance optimised 3 dimensional float matrix. This class can be mutable
  * or immutable.
  *
  * @author openecho
  */
 public class Matrix3F extends MatrixF {
     float m00, m01, m02,
           m10, m11, m12,
           m20, m21, m22 = 0;
 
     
     public Matrix3F(float m00, float m01, float m02,
             float m10, float m11, float m12, 
             float m20, float m21, float m22) {
         super(3, 3, false);
         this.m00 = m00;
         this.m01 = m01;
         this.m02 = m02;
         this.m10 = m10;
         this.m11 = m11;
         this.m12 = m12;
         this.m20 = m20;
         this.m21 = m21;
         this.m22 = m22;
     }
 
     public Matrix3F(Float[][] data) {
         this(data, false);
     }
     
     public Matrix3F(Float[][] data, boolean mutable) {
         super(data, mutable);
         m = data.length;
         if(m > 0) {
             n = data[0].length;
         } else {
             throw new IllegalArgumentException("data dimensions must be > 0");
         }
        if(m != 3 || n != 3) {
             throw new IllegalArgumentException("data dimensions must be = 3");
         }
         m00 = data[0][0];
         m01 = data[0][1];
         m02 = data[0][2];
         m10 = data[1][0];
         m11 = data[1][1];
         m12 = data[1][2];
         m20 = data[2][0];
         m21 = data[2][1];
         m22 = data[2][2];
     }
 
     public Matrix3F() {
         this(true);
     } 
         
     public Matrix3F(boolean mutable) {
         super(3, 3, mutable);
     }
 
     @Override
     protected void initData(Number[][] data) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     protected void initData(int i, int j, Number data) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Float[][] getData() {
         return new Float[][] {{m00, m01, m02}, {m10, m11, m12}, {m20, m21, m22}};
     }
 
     @Override
     public Float getData(int i, int j) {
         if(i > 2 || j > 2) {
             throw new IllegalArgumentException("i and j must be < 2");
         }
         return getData()[i][j];
     }
 
     @Override
     public void setData(Number[][] data) {
         m = data.length;
         if(m > 0) {
             n = data[0].length;
         } else {
             throw new IllegalArgumentException("data dimensions must be > 0");
         }
         if(m != 2 || n != 2) {
             throw new IllegalArgumentException("data dimensions must be = 3");
         }
         m00 = data[0][0].floatValue();
         m01 = data[0][1].floatValue();
         m02 = data[0][2].floatValue();
         m10 = data[1][0].floatValue();
         m11 = data[1][1].floatValue();
         m12 = data[1][2].floatValue();
         m20 = data[2][0].floatValue();
         m21 = data[2][1].floatValue();
         m22 = data[2][2].floatValue();
     }
 
     @Override
     public void setData(int i, int j, Number data) {
         if(i > 2 || j > 2) {
             throw new IllegalArgumentException("i and j must be <= 2");
         }
         if(i == 0 && j == 0) {
             m00 = data.floatValue();
         } else if(i == 0 && j == 1) {
             m01 = data.floatValue();
         } else if(i == 0 && j == 2) {
             m02 = data.floatValue();
         } else if(i == 1 && j == 0) {
             m10 = data.floatValue();
         } else if(i == 1 && j == 1) {
             m11 = data.floatValue();
         } else if(i == 1 && j == 2) {
             m12 = data.floatValue();
         } else if(i == 2 && j == 0) {
             m20 = data.floatValue();
         } else if(i == 2 && j == 1) {
             m21 = data.floatValue();
         } else if(i == 2 && j == 2) {
             m22 = data.floatValue();
         }
     }
 
     @Override
     public Float[] getRow(int i) {
         if(i > 2) {
             throw new IllegalArgumentException("i must be <= 2");
         }
         return getData()[i];
     }
 
     @Override
     public final Matrix3F add(Matrix b) {
         if (m != b.m || n != b.n) {
             throw new RuntimeException("Matrix dimensions are not equal.");
         }
         if(mutate) {
             m00 += b.getData(0, 0).floatValue();
             m01 += b.getData(0, 1).floatValue();
             m02 += b.getData(0, 2).floatValue();
             m10 += b.getData(1, 0).floatValue();
             m11 += b.getData(1, 1).floatValue();
             m12 += b.getData(1, 2).floatValue();
             m20 += b.getData(2, 0).floatValue();
             m21 += b.getData(2, 1).floatValue();
             m22 += b.getData(2, 2).floatValue();
             return this;
         } else {
             return new Matrix3F(m00 + b.getData(0, 0).floatValue(), m01 + b.getData(0, 1).floatValue(), m02 + b.getData(0, 2).floatValue(),
                     m10 + b.getData(1, 0).floatValue(), m11 + b.getData(1, 1).floatValue(), m12 + b.getData(1, 2).floatValue(),
                     m20 + b.getData(2, 0).floatValue(), m21 + b.getData(2, 1).floatValue(), m22 + b.getData(2, 2).floatValue());
         }
     }
 
     public final Matrix3F add3F(Matrix3F b) {
         if (m != b.m || n != b.n) {
             throw new RuntimeException("Matrix dimensions are not equal.");
         }
         if(mutate) {
             m00 += b.m00;
             m01 += b.m01;
             m02 += b.m02;
             m10 += b.m10;
             m11 += b.m11;
             m12 += b.m12;
             m20 += b.m20;
             m21 += b.m21;
             m22 += b.m22;
             return this;
         } else {
             return new Matrix3F(m00 + b.m00, m01 + b.m01, m02 + b.m02,
                     m10 + b.m10, m11 + b.m11, m12 + b.m12,
                     m20 + b.m20, m21 + b.m21, m22 + b.m22);
         }
     }
 
     @Override
     public final Matrix3F subtract(Matrix b) {
         if (m != b.m || n != b.n) {
             throw new RuntimeException("Matrix dimensions are not equal.");
         }
         if(mutate) {
             m00 -= b.getData(0, 0).floatValue();
             m01 -= b.getData(0, 1).floatValue();
             m02 -= b.getData(0, 2).floatValue();
             m10 -= b.getData(1, 0).floatValue();
             m11 -= b.getData(1, 1).floatValue();
             m12 -= b.getData(1, 2).floatValue();
             m20 -= b.getData(2, 0).floatValue();
             m21 -= b.getData(2, 1).floatValue();
             m22 -= b.getData(2, 2).floatValue();
             return this;
         } else {
             return new Matrix3F(m00 - b.getData(0, 0).floatValue(), m01 - b.getData(0, 1).floatValue(), m02 - b.getData(0, 2).floatValue(),
                     m10 - b.getData(1, 0).floatValue(), m11 - b.getData(1, 1).floatValue(), m12 - b.getData(1, 2).floatValue(),
                     m20 - b.getData(2, 0).floatValue(), m21 - b.getData(2, 1).floatValue(), m22 - b.getData(2, 2).floatValue());
         }
     }
 
     public final Matrix3F subtract3F(Matrix3F b) {
         if (m != b.m || n != b.n) {
             throw new RuntimeException("Matrix dimensions are not equal.");
         }
         if(mutate) {
             m00 -= b.m00;
             m01 -= b.m01;
             m02 -= b.m02;
             m10 -= b.m10;
             m11 -= b.m11;
             m12 -= b.m12;
             m20 -= b.m20;
             m21 -= b.m21;
             m22 -= b.m22;
             return this;
         } else {
             return new Matrix3F(m00 - b.m00, m01 - b.m01, m02 - b.m02,
                     m10 - b.m10, m11 - b.m11, m12 - b.m12,
                     m20 - b.m20, m21 - b.m21, m22 - b.m22);
         }
     }
 
     @Override
     public MatrixF multiply(Matrix b) {
         /**
          * TODO: Optimise
          */
         if (n != b.m) {
             throw new RuntimeException("Matrix dimensions are incorrect.");
         }
         RowArrayMatrixF c = new RowArrayMatrixF(m, b.n);
         for(int i=0;i<c.m;i++) {
             for(int j=0;j<c.n;j++) {
                 for(int k=0;k<n;k++) {
                     c.data[i][j] += (getData(i, j)*b.getData(k, j).floatValue());
                 }
             }
         }
         return c;
     }
 
     public Matrix3F multiply3F(Matrix3F b) {
         /**
          * TODO: Optimise
          */
         if (n != b.m) {
             throw new RuntimeException("Matrix dimensions are incorrect.");
         }
         Matrix3F c = new Matrix3F();
         for(int i=0;i<c.m;i++) {
             for(int j=0;j<c.n;j++) {
                 for(int k=0;k<n;k++) {
                     c.setData(i, j, getData(i, j) * b.getData(k, j).floatValue());
                 }
             }
         }
         return c;
     }
 
     @Override
     public Matrix3F transpose() {
         if(mutate) {
             /**
              * TODO: Figure out transpose.
              */
             Matrix3F t = new Matrix3F();
             for (int i = 0; i < m; i++) {
                 for (int j = 0; j < n; j++) {
                     t.setData(j, i, getData(i,j));
                 }
             }
             return t;
         } else {
             Matrix3F t = new Matrix3F();
             for (int i = 0; i < m; i++) {
                 for (int j = 0; j < n; j++) {
                     t.setData(j, i, getData(i,j));
                 }
             }
             return t;
         }
     }
 
     @Override
     public final Matrix3F addScalar(Number v) {
         float f = v.floatValue();
         return addScalar3F(f);
     }
 
     public final Matrix3F addScalar3F(float f) {
         if (mutate) {
             m00 = m00+f;
             m01 = m01+f;
             m02 = m02+f;
             m10 = m10+f;
             m11 = m11+f;
             m12 = m12+f;
             m20 = m20+f;
             m21 = m21+f;
             m22 = m22+f;
             return this;
         } else {
             return new Matrix3F(m00+f,m01+f,m02+f,
                     m10+f,m11+f,m12+f,
                     m20+f,m21+f,m22+f);
         }
     }
 
     @Override
     public final Matrix3F subtractScalar(Number v) {
         float f = v.floatValue();
         return subtractScalar3F(f);
     }
 
     public final Matrix3F subtractScalar3F(float f) {
         if (mutate) {
             m00 = m00-f;
             m01 = m01-f;
             m02 = m02-f;
             m10 = m10-f;
             m11 = m11-f;
             m12 = m12-f;
             m20 = m20-f;
             m21 = m21-f;
             m22 = m22-f;
             return this;
         } else {
             return new Matrix3F(m00-f,m01-f,m02-f,
                     m10-f,m11-f,m12-f,
                     m20-f,m21-f,m22-f);
         }
     }
 
     @Override
     public final Matrix3F multiplyScalar(Number v) {
         float f = v.floatValue();
         return multiplyScalar3F(f);
     }
 
     public Matrix3F multiplyScalar3F(float f) {
         if (mutate) {
             m00 = m00*f;
             m01 = m01*f;
             m02 = m02*f;
             m10 = m10*f;
             m11 = m11*f;
             m12 = m12*f;
             m20 = m20*f;
             m21 = m21*f;
             m22 = m22*f;
             return this;
         } else {
             return new Matrix3F(m00*f,m01*f,m02*f,
                     m10*f,m11*f,m12*f,
                     m20*f,m21*f,m22*f);
         }
     }
 
     @Override
     public final Matrix3F divideScalar(Number v) {
         if (v.intValue() == 0) {
             throw new RuntimeException("Divide By Zero.");
         }
         float f = v.floatValue();
         return divideScalar3F(f);
     }
 
     public final Matrix3F divideScalar3F(float f) {
         if (f == 0F) {
             throw new RuntimeException("Divide By Zero.");
         }
         if (mutate) {
             m00 = m00/f;
             m01 = m01/f;
             m02 = m02/f;
             m10 = m10/f;
             m11 = m11/f;
             m12 = m12/f;
             m20 = m20/f;
             m21 = m21/f;
             m22 = m22/f;
             return this;
         } else {
             return new Matrix3F(m00/f,m01/f,m02/f,
                     m10/f,m11/f,m12/f,
                     m20/f,m21/f,m22/f);
         }
     }
 }
