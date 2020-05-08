 package com.jff.engine3d.model;
 
 public class CameraMain {
 
     public static void main(String args[]) {
        testAngle();
 
 
     }
 
     private static void testAngle() {
         System.out.println("go");
         Point3D up = new Point3D(0, 1, 0);
         Point3D from;
         Point3D to;
 
         Point3D point3D;
         Point3D cameraPoint3D;
 
         from = new Point3D(200, 100, 100);
        to = new Point3D(198, 99, 100);
 
         System.out.println();
         System.out.println("from " + from);
         System.out.println("to " + to);
         System.out.println();
 
 
        point3D = new Point3D(0, 0, 0);
         cameraPoint3D = convert2(from, to, point3D);
         System.out.println("before " + point3D);
         System.out.println("after " + cameraPoint3D);
 
 
         from = new Point3D(200, 100, 100);
        to = new Point3D(100, 100, 100);
 
         System.out.println();
         System.out.println("from " + from);
         System.out.println("to " + to);
         System.out.println();
 
 
        point3D = new Point3D(0, 0, 0);
         cameraPoint3D = convert2(from, to, point3D);
         System.out.println("before " + point3D);
         System.out.println("after " + cameraPoint3D);
     }
 
     private static void testZ() {
         System.out.println("go");
 
         Point3D from;
         Point3D to;
 
         Point3D point3D;
         Point3D cameraPoint3D;
 
         from = new Point3D(200, 100, 100);
         to = new Point3D(100, 100, 100);
 
         System.out.println();
         System.out.println("from " + from);
         System.out.println("to " + to);
         System.out.println();
 
 
         point3D = new Point3D(0, 100, 0);
         cameraPoint3D = convert2(from, to, point3D);
         System.out.println("before " + point3D);
         System.out.println("after " + cameraPoint3D);
 
         point3D = new Point3D(300, 100, 0);
         cameraPoint3D = convert2(from, to, point3D);
         System.out.println("before " + point3D);
         System.out.println("after " + cameraPoint3D);
 
 
     }
 
     public static Point3D convert2(Point3D eye, Point3D lookAt, Point3D point3D) {
 
         double xe;
         double ye;
         double ze;
 
         double xw = point3D.x;
         double yw = point3D.y;
         double zw = point3D.z;
 
 
         xw -= eye.x;
         yw -= eye.y;
         zw -= eye.z;
 
         double tx = lookAt.x - eye.x;
         double ty = lookAt.y - eye.y;
         double tz = lookAt.z - eye.z;
 
         Point3D point3D1 = new Point3D(tx, ty, tz);
         System.out.println("translated " + point3D1);
         SpherePoint3D spherePoint3D = SpherePoint3D.fromCartesian(point3D1);
         System.out.println(spherePoint3D);
 
         double theta = spherePoint3D.theta;
         double phi = spherePoint3D.phi;
         double ro = spherePoint3D.r;
//
//        System.out.println("theta" + theta);
//        System.out.println("phi" + theta);
 
         xe = -xw * Math.sin(theta) + yw * Math.cos(theta);
         ye = -xw * Math.cos(phi) * Math.cos(theta) - yw * Math.cos(phi) * Math.sin(theta) + zw * Math.sin(phi);
         ze = -xw * Math.sin(phi) * Math.cos(theta) - yw * Math.sin(phi) * Math.sin(theta) - zw * Math.cos(phi);
 
         ze *= -1;
//
//        if(Double.compare(theta, Math.PI) >= 0 || theta <0) {
//
//            xe *=-1;
//           ye *=-1;
//        }
 
 
         return new Point3D(xe, ye, ze);
     }
 
     public static Point3D convert(Point3D eye, Point3D lookAt, Point3D up, Point3D point3D) {
 
 
         Matrix mR = new Matrix(4);
         Matrix mT = new Matrix(4);
         Matrix m = new Matrix(4);
 
 
         Vector vEye = convertPoint3d(eye);
         Vector vLookAt = convertPoint3d(lookAt);
         Vector vUp = convertPoint3d(up);
 
         double[] z = {lookAt.x - eye.x, lookAt.y - eye.y, lookAt.z - eye.z, 1};
 //        double[] z = {-from.x + eye.x, -from.y + eye.y, -from.z + eye.z, 1};
 //        double[] z = {eye.x, eye.y, eye.z, 1};
 //        double[] z = {from.x , from.y , from.z , 1};
         //Vector vZ = new Vector(z);
         Vector vZ = new Vector(vLookAt.vector);
         Vector vX = new Vector(crossproduct(vZ.vector, vUp.vector));
         Vector vY = new Vector(crossproduct(vX.vector, vZ.vector));
 
 
         vX.normalize();
         vZ.normalize();
         vY.normalize();
 
         System.out.println(vX);
         System.out.println("vX");
         System.out.println(vY);
         System.out.println("uY");
         System.out.println(vZ);
         System.out.println("vZ");
 
         mR.matrix[0][0] = vX.vector[0];
         mR.matrix[0][1] = vX.vector[1];
         mR.matrix[0][2] = vX.vector[2];
         mR.matrix[0][3] = 0;
 
         mR.matrix[1][0] = vY.vector[0];
         mR.matrix[1][1] = vY.vector[1];
         mR.matrix[1][2] = vY.vector[2];
         mR.matrix[1][3] = 0;
 
         mR.matrix[2][0] = vZ.vector[0];
         mR.matrix[2][1] = vZ.vector[1];
         mR.matrix[2][2] = vZ.vector[2];
         mR.matrix[2][3] = 0;
 
         mR.matrix[3][0] = 0;
         mR.matrix[3][1] = 0;
         mR.matrix[3][2] = 0;
         mR.matrix[3][3] = 1;
 
 
         mT.matrix[0][0] = 1;
         mT.matrix[0][1] = 0;
         mT.matrix[0][2] = 0;
         mT.matrix[0][3] = -eye.x;
 
         mT.matrix[1][0] = 0;
         mT.matrix[1][1] = 1;
         mT.matrix[1][2] = 0;
         mT.matrix[1][3] = -eye.y;
 
         mT.matrix[2][0] = 0;
         mT.matrix[2][1] = 0;
         mT.matrix[2][2] = 1;
         mT.matrix[2][3] = eye.z;
 
         mT.matrix[3][0] = 0;
         mT.matrix[3][1] = 0;
         mT.matrix[3][2] = 0;
         mT.matrix[3][3] = 1;
 
 
         mR = mR.transpose();
         mT = mT.transpose();
 
         m = new Matrix(multiply(mR.matrix, mT.matrix));
         System.out.println(mR);
         System.out.println("mR");
         System.out.println(mT);
         System.out.println("mT");
         System.out.println(m);
         System.out.println("m");
 
 
         Vector startVector = convertPoint3d(point3D);
         System.out.println();
         System.out.println(startVector);
 //        System.out.println(new Vector(multiply(mT.matrix, startVector.vector)));
 //        System.out.println(new Vector(multiply(mR.matrix, startVector.vector)));
 //        System.out.println(new Vector(multiply(m.matrix, startVector.vector)));
 
         System.out.println(new Vector(multiply(startVector.vector, mT.matrix)));
         System.out.println(new Vector(multiply(startVector.vector, mR.matrix)));
         System.out.println(new Vector(multiply(startVector.vector, m.matrix)));
 
 
         System.out.println();
         double[] multiply = multiply(startVector.vector, mT.matrix);
         double[] multiply1 = multiply(multiply, mR.matrix);
         System.out.println(new Vector(multiply1));
 
         Point3D cameraPoint3D = new Point3D(multiply1[0], multiply1[1], multiply1[2]);
 
         return cameraPoint3D;
     }
 
     public static Vector convertPoint3d(Point3D point3D) {
 
         Vector vector = new Vector(4);
 
         vector.vector[0] = point3D.x;
         vector.vector[1] = point3D.y;
         vector.vector[2] = point3D.z;
         vector.vector[3] = 1;
 
         return vector;
     }
 
     public static double[] crossproduct(double v1[], double v2[]) {
         double c[] = new double[3];
         double d1, d2, d3;
         int i;
         d1 = (v1[1] * v2[2]) - (v1[1] * v2[2]);
         d2 = (v1[2] * v2[0]) - (v1[0] * v2[2]);
         d3 = (v1[0] * v2[1]) - (v1[1] * v2[0]);
         c[0] = d1;
         c[1] = d2;
         c[2] = d3;
         return (c);
     }
 
 
     // return x^T y
     public static double dot(double[] x, double[] y) {
         if (x.length != y.length) throw new RuntimeException("Illegal vector dimensions.");
         double sum = 0.0;
         for (int i = 0; i < x.length; i++)
             sum += x[i] * y[i];
         return sum;
     }
 
 
     // return C = A * B
     public static double[][] multiply(double[][] A, double[][] B) {
         int mA = A.length;
         int nA = A[0].length;
         int mB = B.length;
         int nB = A[0].length;
         if (nA != mB) throw new RuntimeException("Illegal matrix dimensions.");
         double[][] C = new double[mA][nB];
         for (int i = 0; i < mA; i++)
             for (int j = 0; j < nB; j++)
                 for (int k = 0; k < nA; k++)
                     C[i][j] += (A[i][k] * B[k][j]);
         return C;
     }
 
     // matrix-vector multiplication (y = A * x)
     public static double[] multiply(double[][] A, double[] x) {
         int m = A.length;
         int n = A[0].length;
         if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
         double[] y = new double[m];
         for (int i = 0; i < m; i++)
             for (int j = 0; j < n; j++)
                 y[i] += (A[i][j] * x[j]);
         return y;
     }
 
 
     // vector-matrix multiplication (y = x^T A)
     public static double[] multiply(double[] x, double[][] A) {
         int m = A.length;
         int n = A[0].length;
         if (x.length != m) throw new RuntimeException("Illegal matrix dimensions.");
         double[] y = new double[n];
         for (int j = 0; j < n; j++)
             for (int i = 0; i < m; i++)
                 y[j] += (A[i][j] * x[i]);
         return y;
     }
 }
