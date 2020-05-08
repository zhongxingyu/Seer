 package math.matrices;
 
 import static math.Math.*;
 
 /**
  * A matrix database that can generate many useful matrices.
  */
 public class MatrixDB {
 	/**
 	 * Generates a rotation matrix to rotate a vector around the origin around
 	 * the x-axis at a specific angle.
 	 * 
 	 * @param a
 	 *            The angle in radians.
 	 * @return Returns a 3x3 matrix.
 	 */
 	public static Matrix generate3DXRotationMatrix(float a) {
 		return Matrix.mat(new float[][] { new float[] { 1, 0, 0 },
 				new float[] { 0, cos(a), sin(a) }, new float[] { 0, -sin(a), cos(a) } });
 	}
 
 	/**
 	 * Generates a rotation matrix to rotate a vector around the origin around
 	 * the y-axis at a specific angle.
 	 * 
 	 * @param a
 	 *            The angle in radians.
 	 * @return Returns a 3x3 matrix.
 	 */
 	public static Matrix generate3DYRotationMatrix(float a) {
 		return Matrix.mat(new float[][] { new float[] { cos(a), 0, sin(a) },
 				new float[] { 0, 1, 0 }, new float[] { -sin(a), 0, cos(a) } });
 	}
 
 	/**
 	 * Generates a rotation matrix to rotate a vector around the origin around
 	 * the z-axis at a specific angle.
 	 * 
 	 * @param a
 	 *            The angle in radians.
 	 * @return Returns a 3x3 matrix.
 	 */
 	public static Matrix generate3DZRotationMatrix(float a) {
 		return Matrix.mat(new float[][] { new float[] { cos(a), sin(a), 0 },
 				new float[] { -sin(a), cos(a), 0 }, new float[] { 0, 0, 1 } });
 	}
 
 	/**
 	 * Generates a rotation matrix to rotate a vector around its origin at a
 	 * specific angle.
 	 * 
 	 * @param a
 	 *            The angle in radians.
 	 * @return Returns a 2x2 matrix.
 	 */
 	public static Matrix generate2DRotationMatrix(float a) {
 		return Matrix.mat(new float[][] { new float[] { cos(a), sin(a) },
 				new float[] { -sin(a), cos(a) } });
 	}
 
 	/**
 	 * Generates a perspective projection matrix.
 	 * 
 	 * @param fovy
	 *            Specifies the field of view angle, in radians, in the y
 	 *            direction.
 	 * @param aspect
 	 *            Specifies the aspect ratio that determines the field of view
 	 *            in the x direction. The aspect ratio is the ratio of x (width)
 	 *            to y (height).
 	 * @param zNear
 	 *            Specifies the distance from the viewer to the near clipping
 	 *            plane (always positive).
 	 * @param zFar
 	 *            Specifies the distance from the viewer to the far clipping
 	 *            plane (always positive).
 	 * @return Returns a 4x4 matrix.
 	 */
 	public static Matrix generatePerspectiveMatrix(float fovy, float aspect, float zNear, float zFar) {
 		float f = 1.0f / tan(fovy / 2f);
 		float f1 = zNear - zFar;
 		return Matrix.mat(new float[][] { new float[] { f / aspect, 0f, 0f, 0f },
 				new float[] { 0f, f, 0f, 0f }, new float[] { 0f, 0f, (zFar + zNear) / f1, -1f },
 				new float[] { 0f, 0f, (2 * zFar * zNear) / f1, 0f } });
 	}
 
 	/**
 	 * Generates a matrix describing a translation.
 	 * 
 	 * @param direction
 	 *            The translation vector.
 	 * @return Returns a 4x4 matrix.
 	 */
 	public static Matrix generateTranslationMatrix(Vector3 direction) {
 		return Matrix.mat(new float[][] { new float[] { 1f, 0f, 0f, 0f },
 				new float[] { 0f, 1f, 0f, 0f }, new float[] { 0f, 0f, 1f, 0f },
 				new float[] { direction.getX(), direction.getY(), direction.getZ(), 1f } });
 	}
 
 	/**
 	 * Generates a matrix describing a rotation around a specific axis.
 	 * 
 	 * @param rotation
 	 *            Specifies the angle of rotation, in radians.
 	 * @param axis
 	 *            Specifies the axis, around which the rotation will be
 	 *            performed.
 	 * @return Returns a 4x4 matrix.
 	 */
 	public static Matrix generateRotationMatrix(float rotation, Vector3 axis) {
 		Vector3 normAxis = (Vector3) axis.normalize();
 		float s = sin(rotation);
 		float c = cos(rotation);
 		float x = normAxis.getX();
 		float y = normAxis.getY();
 		float z = normAxis.getZ();
 		return Matrix.mat(new float[][] {
 				new float[] { x * x * (1f - c) + c, y * x * (1f - c) + z * s,
 						x * z * (1f - c) - y * s, 0f },
 				new float[] { x * y * (1f - c) - z * s, y * y * (1f - c) + c,
 						y * z * (1f - c) + x * s, 0f },
 				new float[] { x * z * (1f - c) + y * s, y * z * (1f - c) - x * s,
 						z * z * (1f - c) + c, 0f }, new float[] { 0f, 0f, 0f, 1f } });
 	}
 
 	/**
 	 * Generates a matrix describing a scale transformation.
 	 * 
 	 * @param scale
 	 *            The scale factor in x, y and z direction.
 	 * @return Returns a 4x4 matrix.
 	 */
 	public static Matrix generateScaleMatrix(Vector3 scale) {
 		return Matrix.mat(new float[][] { new float[] { scale.getX(), 0f, 0f, 0f },
 				new float[] { 0f, scale.getY(), 0f, 0f }, new float[] { 0f, 0f, scale.getZ(), 0f },
 				new float[] { 0f, 0f, 0f, 1f } });
 	}
 
 	/**
 	 * Generates a orthogonal projection matrix.
 	 * 
 	 * @param left
 	 *            Specify the coordinates for the left vertical clipping plane.
 	 * @param right
 	 *            Specify the coordinates for the right vertical clipping plane.
 	 * @param bottom
 	 *            Specify the coordinates for the bottom horizontal clipping
 	 *            plane.
 	 * @param top
 	 *            Specify the coordinates for the top horizontal clipping plane.
 	 * @param near
 	 *            Specify the distances to the nearer depth clipping plane. This
 	 *            value is negative if the plane is to be behind the viewer.
 	 * @param far
 	 *            Specify the distances to the farther depth clipping plane.
 	 *            This value is negative if the plane is to be behind the
 	 *            viewer.
 	 * @return Return a 4x4 matrix.
 	 */
 	public static Matrix generateOrthogonalMatrix(float left, float right, float bottom, float top,
 			float near, float far) {
 		float tx = -((right + left) / (right - left));
 		float ty = -((top + bottom) / (top - bottom));
 		float tz = -((far + near) / (far - near));
 		return Matrix.mat(new float[][] {
 				new float[] { 2f / (right - left), 0f, 0f, 2f / (right - left) },
 				new float[] { 0f, 2f / (top - bottom), 0f, 0f },
 				new float[] { 0f, 0f, -2f / (far - near), 0f }, new float[] { tx, ty, tz, 1f } });
 	}
 
 	/**
 	 * Generates a matrix describing a view transformation.
 	 * 
 	 * @param eye
 	 *            Specifies the position of the eye point.
 	 * @param center
 	 *            Specifies the position of the reference point.
 	 * @param up
 	 *            Specifies the direction of the up vector.
 	 * @return Returns a 4x4 matrix.
 	 */
 	public static Matrix generateLookAtMatrix(Vector3 eye, Vector3 center, Vector3 up) {
 		Vector3 f = (Vector3) ((Vector3) center.subtract(eye)).normalize();
 		Vector3 normUp = (Vector3) up.normalize();
 		Vector3 s = f.cross(normUp);
 		Vector3 u = s.cross(f);
 		Matrix m = Matrix
 				.mat(new float[][] { new float[] { s.getX(), u.getX(), -f.getX(), 0f },
 						new float[] { s.getY(), u.getY(), -f.getY(), 0f },
 						new float[] { s.getZ(), u.getZ(), -f.getZ(), 0f },
 						new float[] { 0f, 0f, 0f, 1f } });
 		return generateTranslationMatrix((Vector3) eye.multiply(-1f).multiply(m));
 	}
 
 	/**
 	 * Generates an identity matrix.
 	 * 
 	 * @param size
 	 *            Returns a 4x4 matrix.
 	 * @return
 	 */
 	public static Matrix generateIdentityMatrix(int size) {
 		float[][] values = new float[size][size];
 		for (int i = 0; i < size; i++) {
 			values[i][i] = 1;
 		}
 		return Matrix.mat(values);
 	}
 }
