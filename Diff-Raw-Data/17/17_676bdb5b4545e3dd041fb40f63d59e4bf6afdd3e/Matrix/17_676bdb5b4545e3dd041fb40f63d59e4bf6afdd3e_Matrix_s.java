 /********************************************************************************
  *                                                                              *
  *  This file is part of the Tiny Math Library.                                 *
  *                                                                              *
  *  Copyright 2012 Chris Foster                                                 *
  *                                                                              *
  ********************************************************************************/
 
 package math;
 
 import java.io.Serializable;
 
 public class Matrix implements Serializable
 {
 	private static final long serialVersionUID = -8921412792080681557L;
 
 	public float[][] m;
 
 	private void setIdentity()
 	{
 		this.m[0][0] = 1.0f; this.m[0][1] = 0.0f; this.m[0][2] = 0.0f; this.m[0][3] = 0.0f;
 		this.m[1][0] = 0.0f; this.m[1][1] = 1.0f; this.m[1][2] = 0.0f; this.m[1][3] = 0.0f;
 		this.m[2][0] = 0.0f; this.m[2][1] = 0.0f; this.m[2][2] = 1.0f; this.m[2][3] = 0.0f;
 		this.m[3][0] = 0.0f; this.m[3][1] = 0.0f; this.m[3][2] = 0.0f; this.m[3][3] = 1.0f;
 	}
 
 	public Matrix()
 	{
 		this.m = new float[4][4];
 		this.setIdentity();
 	}
 
 	public Matrix(float _00, float _01, float _02, float _03,
 				  float _10, float _11, float _12, float _13,
 				  float _20, float _21, float _22, float _23,
 				  float _30, float _31, float _32, float _33)
 	{
 		this();
 		this.m[0][0] = _00; this.m[0][1] = _01; this.m[0][2] = _02; this.m[0][3] = _03;
 		this.m[1][0] = _10; this.m[1][1] = _11; this.m[1][2] = _12; this.m[1][3] = _13;
 		this.m[2][0] = _20; this.m[2][1] = _21; this.m[2][2] = _22; this.m[2][3] = _23;
 		this.m[3][0] = _30; this.m[3][1] = _31; this.m[3][2] = _32; this.m[3][3] = _33;
 	}
 
 	public Matrix(Matrix b)
 	{
 		this.m = b.m.clone();
 
 		for (int row = 0; row < b.m.length; row++)
 			this.m[row] = b.m[row].clone();
 	}
 
 	public Matrix transpose()
 	{
 		this.m = this.transposed().m;
 		return this;
 	}
 
 	public Matrix transposed()
 	{
 		return new Matrix(this.m[0][0], this.m[1][0], this.m[2][0], this.m[3][0],
 						  this.m[0][1], this.m[1][1], this.m[2][1], this.m[3][1],
 						  this.m[0][2], this.m[1][2], this.m[2][2], this.m[3][2],
 						  this.m[0][3], this.m[1][3], this.m[2][3], this.m[3][3]);
 	}
 
 	public Matrix invert()
 	{
 		this.m = this.inverted().m;
 		return this;
 	}
 
 	public Matrix inverted()
 	{
 		Matrix result = new Matrix();
 		Matrix temp = this.clone();
 
 		for (int firstIndex = 0; firstIndex < 4; firstIndex++)
 		{
 			float max = Math.abs(this.m[firstIndex][firstIndex]);
 			int maxRow = firstIndex; // This will contain the row index for the current column that contains the maximum
 
 			for (int row = firstIndex + 1; row < 4; row++)
 				if (Math.abs(temp.m[row][firstIndex]) > max)
 				{
 					max = Math.abs(temp.m[row][firstIndex]);
 					maxRow = row;
 				}
 
 			if (max < Constants.FLT_EPSILON)
 				// The matrix is uninvertible
 				return new Matrix();
 
 			if (maxRow != firstIndex)
 				for (int index = 0; index < 4; index++)
 				{
 					float t = temp.m[maxRow][index];
 					temp.m[maxRow][index] = temp.m[firstIndex][index];
 					temp.m[firstIndex][index] = t;
 
 					t = result.m[maxRow][index];
 					result.m[maxRow][index] = result.m[firstIndex][index];
 					result.m[firstIndex][index] = t;
 				}
 
 			float signedMax = temp.m[firstIndex][firstIndex];
 			for (int index = 0; index < 4; index++)
 			{
 				temp.m[firstIndex][index] /= signedMax;
 				result.m[firstIndex][index] /= signedMax;
 			}
 
 			for (int row = 0; row < 4; row++)
 				if (row != firstIndex)
 				{
 					float rowValue = temp.m[row][firstIndex];
 					for (int index = 0; index < 4; index++)
 					{
 						temp.m[row][index] -= temp.m[firstIndex][index] * rowValue;
 						result.m[row][index] -= result.m[firstIndex][index] * rowValue;
 					}
 				}
 		}
 		return result;
 	}
 
 	public Matrix normalize()
 	{
 		Vector x = this.xAxis().normalized();
 		Vector y = this.yAxis().normalized();
 		Vector z = this.zAxis().normalized();
 
 		this.m[0][0] = x.x; this.m[0][1] = y.x; this.m[0][2] = z.x;
 		this.m[1][0] = x.y; this.m[1][1] = y.y; this.m[1][2] = z.y;
 		this.m[2][0] = x.z; this.m[2][1] = z.y; this.m[2][2] = z.z;
 
 		return this;
 	}
 
 	public Matrix normalized()
 	{
 		Vector x = this.xAxis().normalized();
 		Vector y = this.yAxis().normalized();
 		Vector z = this.zAxis().normalized();
 
		return new Matrix(x.x, y.x, z.x, this.m[0][3],
						  x.y, y.y, z.y, this.m[1][3],
						  x.z, y.z, z.z, this.m[2][3],
 						  this.m[3][0], this.m[3][1], this.m[3][2], this.m[3][3]);
 	}
 
 	public Vector xAxis()
 	{
		return new Vector(this.m[0][0], this.m[1][0], this.m[2][0]);
 	}
 
 	public Vector yAxis()
 	{
		return new Vector(this.m[0][1], this.m[1][1], this.m[2][1]);
 	}
 
 	public Vector zAxis()
 	{
		return new Vector(this.m[0][2], this.m[1][2], this.m[2][2]);
 	}
 
 	public void setAxis(int axis, Vector value)
 	{
 		if (axis < 0 || axis >= 3)
 			return;
 		for (int i = 0; i < 3; i++)
 			this.m[axis][i] = value.get(i);
 	}
 
 	public Matrix rotationPart()
 	{
 		Matrix normalizedMatrix = this.normalized().as3x3();
 		if (normalizedMatrix.isNegative())
 		{
 			normalizedMatrix.setAxis(0, normalizedMatrix.xAxis().multiply(-1.0f));
 			normalizedMatrix.setAxis(1, normalizedMatrix.yAxis().multiply(-1.0f));
 			normalizedMatrix.setAxis(2, normalizedMatrix.zAxis().multiply(-1.0f));
 		}
 
 		return normalizedMatrix;
 	}
 
 	public Vector scalePart()
 	{
 		Matrix scaleMatrix = this.as3x3().multiply(this.rotationPart().inverted());
 		return new Vector(scaleMatrix.m[0][0], scaleMatrix.m[1][1], scaleMatrix.m[2][2]);
 	}
 
 	public Vector translationPart()
 	{
 		return new Vector(this.m[0][3], this.m[1][3], this.m[2][3]);
 	}
 
 	public Matrix as3x3()
 	{
 		return new Matrix(this.m[0][0], this.m[0][1], this.m[0][2], 0.0f,
 						  this.m[1][0], this.m[1][1], this.m[1][2], 0.0f,
 						  this.m[2][0], this.m[2][1], this.m[2][2], 0.0f,
 						  0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	public Matrix multiply(Matrix b)
 	{
 		Matrix result = new Matrix();
 
 		result.m[0][0] = this.m[0][0] * b.m[0][0] + this.m[0][1] * b.m[1][0] + this.m[0][2] * b.m[2][0] + this.m[0][3] * b.m[3][0];
 		result.m[1][0] = this.m[1][0] * b.m[0][0] + this.m[1][1] * b.m[1][0] + this.m[1][2] * b.m[2][0] + this.m[1][3] * b.m[3][0];
 		result.m[2][0] = this.m[2][0] * b.m[0][0] + this.m[2][1] * b.m[1][0] + this.m[2][2] * b.m[2][0] + this.m[2][3] * b.m[3][0];
 		result.m[3][0] = this.m[3][0] * b.m[0][0] + this.m[3][1] * b.m[1][0] + this.m[3][2] * b.m[2][0] + this.m[3][3] * b.m[3][0];
 
 		result.m[0][1] = this.m[0][0] * b.m[0][1] + this.m[0][1] * b.m[1][1] + this.m[0][2] * b.m[2][1] + this.m[0][3] * b.m[3][1];
 		result.m[1][1] = this.m[1][0] * b.m[0][1] + this.m[1][1] * b.m[1][1] + this.m[1][2] * b.m[2][1] + this.m[1][3] * b.m[3][1];
 		result.m[2][1] = this.m[2][0] * b.m[0][1] + this.m[2][1] * b.m[1][1] + this.m[2][2] * b.m[2][1] + this.m[2][3] * b.m[3][1];
 		result.m[3][1] = this.m[3][0] * b.m[0][1] + this.m[3][1] * b.m[1][1] + this.m[3][2] * b.m[2][1] + this.m[3][3] * b.m[3][1];
 
 		result.m[0][2] = this.m[0][0] * b.m[0][2] + this.m[0][1] * b.m[1][2] + this.m[0][2] * b.m[2][2] + this.m[0][3] * b.m[3][2];
 		result.m[1][2] = this.m[1][0] * b.m[0][2] + this.m[1][1] * b.m[1][2] + this.m[1][2] * b.m[2][2] + this.m[1][3] * b.m[3][2];
 		result.m[2][2] = this.m[2][0] * b.m[0][2] + this.m[2][1] * b.m[1][2] + this.m[2][2] * b.m[2][2] + this.m[2][3] * b.m[3][2];
 		result.m[3][2] = this.m[3][0] * b.m[0][2] + this.m[3][1] * b.m[1][2] + this.m[3][2] * b.m[2][2] + this.m[3][3] * b.m[3][2];
 
 		result.m[0][3] = this.m[0][0] * b.m[0][3] + this.m[0][1] * b.m[1][3] + this.m[0][2] * b.m[2][3] + this.m[0][3] * b.m[3][3];
 		result.m[1][3] = this.m[1][0] * b.m[0][3] + this.m[1][1] * b.m[1][3] + this.m[1][2] * b.m[2][3] + this.m[1][3] * b.m[3][3];
 		result.m[2][3] = this.m[2][0] * b.m[0][3] + this.m[2][1] * b.m[1][3] + this.m[2][2] * b.m[2][3] + this.m[2][3] * b.m[3][3];
 		result.m[3][3] = this.m[3][0] * b.m[0][3] + this.m[3][1] * b.m[1][3] + this.m[3][2] * b.m[2][3] + this.m[3][3] * b.m[3][3];
 
 		return result;
 	}
 
 	public Ray multiply(Ray b)
 	{
 		float maxTime = b.maxTime;
 		if (maxTime < Float.POSITIVE_INFINITY)
 		{
 			Vector range = b.at(maxTime).subtract(b.origin);
 			maxTime = this.as3x3().multiply(range).length();
 		}
 
 		float minTime = b.minTime;
 		if (minTime < Float.POSITIVE_INFINITY)
 		{
 			Vector range = b.at(minTime).subtract(b.origin);
 			minTime = this.as3x3().multiply(range).length();
 		}
 
 		return new Ray(this.multiply(b.origin), this.as3x3().multiply(b.direction), minTime, maxTime);
 	}
 
 	public Vector multiply(Vector b)
 	{
 		return new Vector(this.m[0][0] * b.x + this.m[0][1] * b.y + this.m[0][2] * b.z + this.m[0][3] * 1.0f,
 						  this.m[1][0] * b.x + this.m[1][1] * b.y + this.m[1][2] * b.z + this.m[1][3] * 1.0f,
 						  this.m[2][0] * b.x + this.m[2][1] * b.y + this.m[2][2] * b.z + this.m[2][3] * 1.0f);
 	}
 
 	public static Matrix rotate(float radiansX, float radiansY, float radiansZ)
 	{
 		Matrix xRotation = new Matrix();
 		if (Math.abs(radiansX) > Constants.FLT_EPSILON)
 			xRotation = new Matrix(1.0f, 0.0f, 0.0f, 0.0f,
 								   0.0f, (float)Math.cos(radiansX), -(float)Math.sin(radiansX), 0.0f,
 								   0.0f, (float)Math.sin(radiansX), (float)Math.cos(radiansX), 0.0f,
 								   0.0f, 0.0f, 0.0f, 1.0f);
 
 		Matrix yRotation = new Matrix();
 		if (Math.abs(radiansY) > Constants.FLT_EPSILON)
 			yRotation = new Matrix((float)Math.cos(radiansY), 0.0f, (float)Math.sin(radiansY), 0.0f,
 								   0.0f, 1.0f, 0.0f, 0.0f,
 								   -(float)Math.sin(radiansY), 0.0f, (float)Math.cos(radiansY), 0.0f,
 								   0.0f, 0.0f, 0.0f, 1.0f);
 
 		Matrix zRotation = new Matrix();
 		if (Math.abs(radiansZ) > Constants.FLT_EPSILON)
 			zRotation = new Matrix((float)Math.cos(radiansZ), -(float)Math.sin(radiansZ), 0.0f, 0.0f,
 								   (float)Math.sin(radiansZ), (float)Math.cos(radiansZ), 0.0f, 0.0f,
 								   0.0f, 0.0f, 1.0f, 0.0f,
 								   0.0f, 0.0f, 0.0f, 1.0f);
 
 		return zRotation.multiply(yRotation).multiply(xRotation);
 	}
 
 	public static Matrix scale(float scaleX, float scaleY, float scaleZ)
 	{
 		return new Matrix(scaleX, 0.0f, 0.0f, 0.0f,
 						  0.0f, scaleY, 0.0f, 0.0f,
 						  0.0f, 0.0f, scaleZ, 0.0f,
 						  0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	public static Matrix scale(Vector scaleXYZ)
 	{
 		return scale(scaleXYZ.x, scaleXYZ.y, scaleXYZ.z);
 	}
 
 	public static Matrix translate(float translationX, float translationY, float translationZ)
 	{
 		return new Matrix(1.0f, 0.0f, 0.0f, translationX,
 						  0.0f, 1.0f, 0.0f, translationY,
 						  0.0f, 0.0f, 1.0f, translationZ,
 						  0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	public static Matrix translate(Vector translationXYZ)
 	{
 		return translate(translationXYZ.x, translationXYZ.y, translationXYZ.z);
 	}
 
 	private boolean isNegative()
 	{
 		Vector z = this.xAxis().cross(this.yAxis());
 		return z.dot(this.zAxis()) < 0.0f;
 	}
 
 	@Override
 	public boolean equals(Object b)
 	{
 		if (this == b)
 			return true;
 		if (b == null)
 			return false;
 		if (this.getClass() != b.getClass())
 			return false;
 
 		Matrix c = (Matrix)b;
 
 		for (int row = 0; row < 4; row++)
 			for (int column = 0; column < 4; column++)
 				if (Math.abs(this.m[row][column] - c.m[row][column]) > Constants.FLT_EPSILON)
 					return false;
 		return true;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		int hash = 15;
 		for (int row = 0; row < 4; row++)
 			for (int column = 0; column < 4; column++)
 				hash = 17 * hash + new Float(this.m[row][column]).hashCode();
 		return hash;
 	}
 
 	@Override
 	public Matrix clone()
 	{
 		return new Matrix(this);
 	}
 
 	@Override
 	public String toString()
 	{
 		return String.format("[ %f, %f, %f, %f ]\n[ %f, %f, %f, %f ]\n[ %f, %f, %f, %f ]\n[ %f, %f, %f, %f ]",
 				this.m[0][0], this.m[0][1], this.m[0][2], this.m[0][3],
 				this.m[1][0], this.m[1][1], this.m[1][2], this.m[1][3],
 				this.m[2][0], this.m[2][1], this.m[2][2], this.m[2][3],
 				this.m[3][0], this.m[3][1], this.m[3][2], this.m[3][3]);
 	}
 
 }
