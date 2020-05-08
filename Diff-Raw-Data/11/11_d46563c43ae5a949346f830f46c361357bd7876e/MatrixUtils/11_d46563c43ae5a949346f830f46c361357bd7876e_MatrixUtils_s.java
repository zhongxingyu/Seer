 /*
  * Copyright (c) 2013 George Weller
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package uk.co.zutty.glarena.util;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import static uk.co.zutty.glarena.util.MathUtils.cot;
 import static uk.co.zutty.glarena.util.MathUtils.degreesToRadians;
 
 /**
  * Utility methods to provide stock matrices.
  */
 public final class MatrixUtils {
 
     private static final Vector3f forward = new Vector3f();
     private static final Vector3f side = new Vector3f();
     private static final Vector3f up = new Vector3f();
     private static final Vector3f eye = new Vector3f();
 
     public static Matrix4f frustum(float width, float height, float fieldOfView, float near, float far) {
         Matrix4f frustum = new Matrix4f();
         float aspectRatio = width / height;
 
         float yScale = cot(degreesToRadians(fieldOfView / 2f));
         float xScale = yScale / aspectRatio;
         float frustumLength = far - near;
 
         frustum.m00 = xScale;
         frustum.m11 = yScale;
         frustum.m22 = -((far + near) / frustumLength);
        frustum.m23 = -1;
        frustum.m32 = -((2 * near * far) / frustumLength);
 
         return frustum;
     }
 
     public static Matrix4f lookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
         forward.set(centerX - eyeX, centerY - eyeY, centerZ - eyeZ);
         forward.normalise();
 
         up.set(upX, upY, upZ);
 
         Vector3f.cross(forward, up, side);
         side.normalise();
 
         Vector3f.cross(side, forward, up);
         up.normalise();
 
         Matrix4f matrix = new Matrix4f();
         matrix.m00 = side.x;
         matrix.m10 = side.y;
         matrix.m20 = side.z;
 
         matrix.m01 = up.x;
         matrix.m11 = up.y;
         matrix.m21 = up.z;
 
         matrix.m02 = -forward.x;
         matrix.m12 = -forward.y;
         matrix.m22 = -forward.z;
 
         eye.set(-eyeX, -eyeY, -eyeZ);
         matrix.translate(eye);
 
         return matrix;
     }
 }
