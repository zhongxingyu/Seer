 /* Copyright (C) 2013, Cameron White
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its contributors
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 package edu.pdx.cawhite.coordinates;
 
 import edu.pdx.cawhite.iterators.IteratorException;
 import edu.pdx.cawhite.math.ColumnMatrix;
 import edu.pdx.cawhite.math.LengthException;
 import edu.pdx.cawhite.math.Matrix;
 import edu.pdx.cawhite.math.MatrixSizeException;
 import edu.pdx.cawhite.math.RowMatrix;
 import edu.pdx.cawhite.math.Vector;
 import edu.pdx.cawhite.math.Matrix.Iterator;
 
 /**
  * @author Cameron Brandon White
  */
 public class Functions3D {
     
     /**
      * @param columnMatrix  The matrix to translate.
      * @param x             The amount to translate in the x direction.
      * @param y             The amount to translate in the y direction.
      * @param z             The amount to translate in the z direction.
      * @return              The translated matrix.
      */
     public static ColumnMatrix translate(
             ColumnMatrix columnMatrix, Double x, Double y, Double z) 
             throws MatrixSizeException {
 
         RowMatrix translationMatrix = TransformationMatrices3D.translationMatrix(x, y, z);
         return translationMatrix.mul(columnMatrix);
     }
 
     /**
      * @param vector    The vector to translate.
      * @param x         The amount to translate in the x direction.
      * @param y         The amount to translate in the y direction.
      * @param z         The amount to translate in the z direction.
      * @return          The translated vector.
      */
     public static Vector translate(
             Vector vector, Double x, Double y, Double z) 
             throws LengthException {
 
         return new Vector(new double[] {vector.get(0) + x, vector.get(1) + y, vector.get(2) + z});
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param theta            The amount to rotate.
      * @return                 The new matrix translated. 
      */
     public static ColumnMatrix rotateX(
             ColumnMatrix columnMatrix, Double theta) 
             throws MatrixSizeException {
         
         RowMatrix rotationMatrix = TransformationMatrices3D.xRotationMatrix(theta);
         return rotationMatrix.mul(columnMatrix);
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param theta            The amount to rotate.
      * @return                 The new matrix translated. 
      */
     public static Vector rotateX(Vector vector, Double theta) 
             throws LengthException {
         
         RowMatrix rotationMatrix = TransformationMatrices3D.xRotationMatrix(theta);
         return rotationMatrix.mul(vector);
     }
 
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param theta            The amount to rotate.
      * @return                 The new matrix translated. 
      */
     public static ColumnMatrix rotateY(
             ColumnMatrix columnMatrix, Double theta) 
             throws MatrixSizeException {
 
         RowMatrix rotationMatrix = TransformationMatrices3D.yRotationMatrix(theta);
         return rotationMatrix.mul(columnMatrix);
     }
 
     /**
      * @param coordinateMatrix  The matrix to translate.
      * @param theta             The amount to rotate.
      * @return                  The new matrix translated. 
      */
     public static Vector rotateY(Vector vector, Double theta) 
             throws LengthException {
        
         RowMatrix rotationMatrix = TransformationMatrices3D.yRotationMatrix(theta);
         return rotationMatrix.mul(vector);
     }
 
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param theta            The amount to rotate.
      * @return                 The new matrix translated. 
      */
     public static ColumnMatrix rotateZ(
             ColumnMatrix columnMatrix, Double theta) 
             throws MatrixSizeException {
         
         RowMatrix rotationMatrix = TransformationMatrices3D.zRotationMatrix(theta);
         return rotationMatrix.mul(columnMatrix);
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param theta            The amount to rotate.
      * @return The new matrix translated. 
      */
     public static Vector rotateZ(Vector vector, Double theta) 
             throws LengthException {
         
         RowMatrix rotationMatrix = TransformationMatrices3D.zRotationMatrix(theta);
         return rotationMatrix.mul(vector);
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param p1               point 1 on the line.
      * @param p2               point 2 on the line.
      * @param t                The amount to rotate.
      * @return The new matrix translated. 
      */
     public static ColumnMatrix rotateLine(
             ColumnMatrix columnMatrix, 
             Vector point1, Vector point2, Double t) 
             throws MatrixSizeException {
 
         RowMatrix rotationMatrix = TransformationMatrices3D.lineRotationMatrix(point1, point2, t);
         return rotationMatrix.mul(columnMatrix);
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param p1               point 1 on the line.
      * @param p2               point 2 on the line.
      * @param t                The amount to rotate.
      * @return The new matrix translated. 
      */
     public static Vector rotateLine(
             Vector vector, Vector point1, Vector point2, Double t) 
             throws LengthException {
 
         RowMatrix rotationMatrix = TransformationMatrices3D.lineRotationMatrix(point1, point2, t);
         return rotationMatrix.mul(vector);
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param distance         The distance
      * @return                 The new matrix projected. 
      */
     public static ColumnMatrix project(
             ColumnMatrix columnMatrix, Double distance) 
             throws MatrixSizeException {
 
         RowMatrix projectionMatrix = TransformationMatrices3D.projectionMatrix(distance);
         return project(columnMatrix, projectionMatrix);
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param point1           The first point on the line.
      * @param point2           The second point on the line.
      * @return                 The new matrix projected. 
      */
     public static ColumnMatrix project(
             ColumnMatrix columnMatrix, 
             Vector point1, Vector point2, Double distance) 
             throws MatrixSizeException {
 
         RowMatrix projectionMatrix = TransformationMatrices3D.projectionMatrix(point1, point2, distance);
         return project(columnMatrix, projectionMatrix);
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param distance         The distance
      * @return                 The new matrix projected. 
      */
     public static Vector project(Vector vector, Double distance) 
             throws LengthException {
 
         RowMatrix projectionMatrix = TransformationMatrices3D.projectionMatrix(distance);
         return project(vector, projectionMatrix);
     }
     
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param distance         The distance
      * @return                 The new matrix projected. 
      */
     protected static Vector project(
             Vector vector, RowMatrix projectionMatrix) 
             throws LengthException {
 
         Vector newVector = projectionMatrix.mul(vector);
 
         return normalizeProjectedVector(newVector);
     }
 
     /**
      * @param coordinateMatrix The matrix to translate.
      * @param distance         The distance
      * @return                 The new matrix projected. 
      */
     protected static ColumnMatrix project(
         ColumnMatrix columnMatrix, RowMatrix projectionMatrix) 
         throws MatrixSizeException {
 
         // Create the new matrix
         int numberOfRows = columnMatrix.getNumberOfRows();
         int numberOfColumns = columnMatrix.getNumberOfColumns();
         ColumnMatrix newColumnMatrix = new ColumnMatrix(numberOfRows, numberOfColumns);
 
         Matrix.Iterator matrixIterator = columnMatrix.new Iterator();
 
        do {
             try {
 
                 Vector vector = matrixIterator.get();
                 Vector newVector = project(vector, projectionMatrix);
                 matrixIterator.set(newVector);
                 matrixIterator.next();
 
             } catch (IteratorException e) {
                 break;
             } catch (LengthException e) {
                 throw new MatrixSizeException();
             }
 
        } while (matrixIterator.hasNext());
 
         return newColumnMatrix;
     }
     
     public static CoordinateMatrix line(
             Coordinate p, Coordinate d, 
             double tStart, double tEnd, double tStep) 
             throws LengthException {
 
         assert tEnd > tStart;
         assert tStep > 0.0;
 
         int numberOfRows = 4;
         int numberOfColumns = (int) ((tEnd - tStart) / tStep);
 
         if ((p.getLength() != numberOfRows) || 
            (d.getLength() == numberOfRows)) 
             throw new LengthException("Coordinate must be of length 4");
         
         CoordinateMatrix newMatrix = new CoordinateMatrix(numberOfRows,
                                                           numberOfColumns);
 
         CoordinateMatrix.Iterator matrixIterator = newMatrix.new Iterator();
 
         for (Double i = tStart; i <= tEnd; i += tStep) {
             try {
                 matrixIterator.set(p.add(d.mul(i)));
                 matrixIterator.next();
             } catch (IteratorException e) {
                 assert false : "Fatal programming error";
             } catch (LengthException e) {
                 assert false : "Fatal programming error";
             }
         }
 
         return newMatrix;
     }
     
     public static CoordinateMatrix plane(
             Coordinate p, 
             Coordinate d1, Double uStart, Double uEnd, Double uStep,
             Coordinate d2, Double vStart, Double vEnd, Double vStep) 
             throws LengthException {
 
         assert uEnd > uStart;
         assert vEnd > vStart;
         assert uStep > 0.0;
         assert vStep > 0.0;
 
         int numberOfRows = 4;
         int numberOfColumns = (int) Math.pow(((uEnd - uStart) / uStep), 
                                              ((vEnd - vStart) / vStep));
 
         if ((p.getLength() != numberOfRows) || 
            (d1.getLength() == numberOfRows) ||
            (d2.getLength() == numberOfRows)) 
             throw new LengthException("Coordinate must be of length 4");
 
         CoordinateMatrix newMatrix = new CoordinateMatrix(numberOfRows,
                                                           numberOfColumns);
 
         CoordinateMatrix.Iterator matrixIterator = newMatrix.new Iterator();
 
         for (Double i = uStart; i <= uEnd; i += uStep) {
             for (Double j = vStart; j <= vEnd; j += vStep) {
                 try {
                     matrixIterator.set(p.add(d1.mul(i).add(d2.mul(j))));
                     matrixIterator.next();
                 } catch (IteratorException e) {
                     assert false : "Fatal programming error";
                 } catch (LengthException e) {
                     assert false : "Fatal programming error";
                 }
             }
         }
 
         return newMatrix;
     }
 
     public static CoordinateMatrix cube(Coordinate point, Double length, Double step) {
 
         Coordinate d1 = null, d2 = null, d3 = null;
 
         int numberOfRows = 4;
         int numberOfColumns = (int) Math.pow(Math.pow((length / step), (length / step)), (length / step));
 
         CoordinateMatrix newMatrix = new CoordinateMatrix(numberOfRows,
                                                   numberOfColumns);
 
         try {
             d1 = new Coordinate(new double[] {1.0, 0.0, 0.0});
             d2 = new Coordinate(new double[] {0.0, 1.0, 0.0});
             d3 = new Coordinate(new double[] {0.0, 0.0, 1.0});
         } catch (LengthException e) {
             assert false : "Fatal programming error";
         }
 

         CoordinateMatrix.Iterator matrixIterator = newMatrix.new Iterator();
 
         for (Double i = 0.0; i <= length; i += step) {
             for (Double j = 0.0; j <= length; j += step) {
                 for (Double k = 0.0; k <= length; k += step) {
                     try {
                         matrixIterator.set(point.add(d1.mul(i).add(d2.mul(j)).add(d3.mul(k))));
                         matrixIterator.next();
                     } catch (IteratorException e) {
                         assert false : "Fatal programming error";
                     } catch (LengthException e) {
                         assert false : "Fatal programming error";
                     }
                 }
             }
         }
         return newMatrix;
     }
     /**
      *
      */
     protected static Vector normalizeProjectedVector(Vector vector) {
         vector.set(0, vector.get(0) / vector.get(3) );
         vector.set(1, vector.get(1) / vector.get(3) );
         vector.set(3, 1.0);
         return vector;
     }
 }
