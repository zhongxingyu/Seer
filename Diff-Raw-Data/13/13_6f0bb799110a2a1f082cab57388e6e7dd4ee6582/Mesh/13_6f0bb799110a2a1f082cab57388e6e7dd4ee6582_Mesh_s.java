 package cs5625.deferred.scenegraph;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.HashMap;
 
 import javax.media.opengl.GL2;
 import javax.vecmath.GMatrix;
 import javax.vecmath.Matrix3d;
 import javax.vecmath.GVector;
 import javax.vecmath.Vector3f;
 
 import com.jogamp.common.nio.Buffers;
 
 import cs5625.deferred.materials.BlinnPhongMaterial;
 import cs5625.deferred.materials.Material;
 import cs5625.deferred.misc.OpenGLResourceObject;
 
 /**
  * Mesh.java
  * 
  * The Mesh abstract class represents a mesh of n-gons, where n is specified by the subclass.
  * 
  * Written for Cornell CS 5625 (Interactive Computer Graphics).
  * Copyright (c) 2012, Computer Science Department, Cornell University.
  * 
  * @author Asher Dunn (ad488)
  * @date 2012-04-06
  */
 public abstract class Mesh implements OpenGLResourceObject
 {
 	protected static float EPS = 1e-7f;
 	
 	/* Material and name of this mesh. */
 	private Material mMaterial = new BlinnPhongMaterial();
 	private String mName = "";
 	
 	/* Buffers to hold vertex and polygon index data. Buffer formats are 
 	 * described in the comments for the getter and setter methods, 
 	 * farther down the file. */
 	protected FloatBuffer mVertexData, mNormalData, mTexCoordData;
 	protected IntBuffer mPolygonData, mEdgeData;
 	
 	/**
 	 * Map of generic vertex attribute name -> generic vertex attribute buffer. The number of elements in 
 	 * each buffer must match the number of vertices; each buffer's dimensionality (float, vec2, vec3, vec4) 
 	 * will be inferred based on its size.
 	 */
 	public HashMap<String, FloatBuffer> vertexAttribData = new HashMap<String, FloatBuffer>();
 	
 	/**
 	 * Implemented by subclasses to specify how many vertices per polygon this type of mesh has.
 	 */
 	public abstract int getVerticesPerPolygon();
 	
 	/**
 	 * Implemented by subclasses to calculate surface tangent vectors based on the vertices, 
 	 * normals, and texture coordinates of this mesh.
 	 * 
 	 * The output is a 4-vector for each vertex, storing the handedness in the w component. Bitangents 
 	 * can be computed from normals and tangents as `cross(normal, tangent.xyz) * tangent.w`. 
 	 */
 	public abstract FloatBuffer calculateTangentVectors();
 	
 	/**
 	 * Computes the tangent and bitangent vectors of triangle with vertex indices (i1, i2, i3). 
 	 * using the idea described in  http://www.terathon.com/code/tangent.html
 	 * 
 	 * The computed tangent vectors should be accumulated in the array tan1[], and the
 	 * computed bitangent vectors should be accumulated in the array tan2[].	 
 	 */
 	protected void computeAndAccumulateTangentVectors(int i1, int i2, int i3, float tan1[], float[] tan2) {
 		/* Get vertex coordinates of this triangle. */
 		Vector3f p0 = new Vector3f(mVertexData.get(3 * i1 + 0), mVertexData.get(3 * i1 + 1), mVertexData.get(3 * i1 + 2));
 		Vector3f p1 = new Vector3f(mVertexData.get(3 * i2 + 0), mVertexData.get(3 * i2 + 1), mVertexData.get(3 * i2 + 2));
 		Vector3f p2 = new Vector3f(mVertexData.get(3 * i3 + 0), mVertexData.get(3 * i3 + 1), mVertexData.get(3 * i3 + 2));		
 
 		/* Get texture coordinates of this triangle. */
 		float u1 = mTexCoordData.get(2 * i1 + 0);
 		float v1 = mTexCoordData.get(2 * i1 + 1);
 
 		float u2 = mTexCoordData.get(2 * i2 + 0);
 		float v2 = mTexCoordData.get(2 * i2 + 1);
 
 		float u3 = mTexCoordData.get(2 * i3 + 0);
 		float v3 = mTexCoordData.get(2 * i3 + 1);			
 		
 		/* Get positions of vertices relative to the first vertex. */
 		Vector3f q1 = new Vector3f();
 		Vector3f q2 = new Vector3f();
 		
 		q1.sub(p1, p0);
 		q2.sub(p2, p0);
 
 		/* TODO PA1: Construct the Q matrix */
 		GMatrix matQ = new GMatrix(2, 3);
 		double[] temp1 = {q1.x, q1.y, q1.z};
 		double[] temp2 = {q2.x, q2.y, q2.z};
 		matQ.setRow(0, temp1);
 		matQ.setRow(1, temp2);
 		
 		
 		
 		/* Get texture coordinates relative to the first vertex. */
         float s1 = u2 - u1;
         float s2 = u3 - u1;
         float t1 = v2 - v1;
         float t2 = v3 - v1;
         
         /* This can happen because of (1) missing texture
          * (2) broken texture coordinates, so we skip it. */
         if (Math.abs(s1 * t2 - s2 * t1) < EPS) {
         	return;
         }
 
 		/* TODO PA1: Construct the inverse (s,t) matrix, and compute 
 		 * tangent and bitangent vectors as explained on Lengyel's site. */       
 		GVector tangent = new GVector(3);
 		GVector bi_tangent = new GVector(3);
 		
 		GMatrix matST = new GMatrix(2,2);
 		double[] temp3 = {s1, t1};
 		double[] temp4 = {s2, t2};
 		matST.setRow(0, temp3);
 		matST.setRow(1, temp4);
 		matST.invert();
 		
		matST.mul(matQ);
		matST.getRow(0, tangent);
		matST.getRow(1, bi_tangent);
 		
 		
 		/* Accumulate into temporary arrays. */
 		tan1[3 * i1 + 0] += tangent.getElement(0);
 		tan1[3 * i1 + 1] += tangent.getElement(1);
 		tan1[3 * i1 + 2] += tangent.getElement(2);
 		
 		tan1[3 * i2 + 0] += tangent.getElement(0);
 		tan1[3 * i2 + 1] += tangent.getElement(1);
 		tan1[3 * i2 + 2] += tangent.getElement(2);
 
 		tan1[3 * i3 + 0] += tangent.getElement(0);
 		tan1[3 * i3 + 1] += tangent.getElement(1);
 		tan1[3 * i3 + 2] += tangent.getElement(2);
 
 		tan2[3 * i1 + 0] += bi_tangent.getElement(0);
 		tan2[3 * i1 + 1] += bi_tangent.getElement(1);
 		tan2[3 * i1 + 2] += bi_tangent.getElement(2);
 		
 		tan2[3 * i2 + 0] += bi_tangent.getElement(0);
 		tan2[3 * i2 + 1] += bi_tangent.getElement(1);
 		tan2[3 * i2 + 2] += bi_tangent.getElement(2);
 
 		tan2[3 * i3 + 0] += bi_tangent.getElement(0);
 		tan2[3 * i3 + 1] += bi_tangent.getElement(1);
 		tan2[3 * i3 + 2] += bi_tangent.getElement(2);
 	}
 	
 	/**
 	 * Averages and normalizes all tangent vectors, previously accumulated by computeAndAccumulateTangentVectors()
 	 * for every triangle. The input arrays tan1 and tan2 should be the same arrays, passed to the above function.
 	 * 	 
 	 * The output is a 4-vector for each vertex, storing the handedness in the w component. Bitangents 
 	 * can be computed from normals and tangents as `cross(normal, tangent.xyz) * tangent.w`. 	 
 	 */
 	protected FloatBuffer averageAndNormalizeAllTangentVectors(float tan1[], float[] tan2) {
 		/* Allocate result buffer and loop over vertices. */
 		FloatBuffer result = Buffers.newDirectFloatBuffer(4 * getVertexCount());
 		
 		for (int vIndex = 0; vIndex < getVertexCount(); ++vIndex)
 		{
 			/* Get vertex normal. */
 			Vector3f normal = new Vector3f(mNormalData.get(3 * vIndex + 0), mNormalData.get(3 * vIndex + 1), mNormalData.get(3 * vIndex + 2));
 			normal.normalize();
 			
 			/* Get tentative tangent and bitangent vectors at this vertex. */
 			Vector3f tangent = new Vector3f(tan1[3 * vIndex + 0], tan1[3 * vIndex + 1], tan1[3 * vIndex + 2]);
 			Vector3f bitangent = new Vector3f(tan2[3 * vIndex + 0], tan2[3 * vIndex + 1], tan2[3 * vIndex + 2]);
 						
 			/* If the tangent is the zero vector, then there were no valid texture coordinates,
 			 * so we generate a tangent space starting from an arbitrary vector: e.g. (1, 0, 0) */			
 			if (tangent.length() < EPS) {
 				/* This computes 'n cross (1, 0, 0)' */
 				tangent = new Vector3f(0, normal.z, -normal.y);				
 				
 				/* If this coincides with the normal, pick (0, 1, 0) */
 				if (tangent.length() < EPS) {
 					/* This computes 'n cross (0, 1, 0)' */
 					tangent = new Vector3f(-normal.z, 0, normal.x);				
 				}
 				
 				/* Make sure this is all zeros as well */
 				bitangent = new Vector3f(0, 0, 0);
 			}
 			
 			tangent.normalize();
 			bitangent.normalize();
 			
 			/* TODO PA1: Orthogonalize and normalize (aka. create orthonormal basis), based on
 			 * the current normal and tangent vectors, as explained on Lengyel's site. */   
 			Vector3f tangent_orth = new Vector3f();
 			Vector3f temp = new Vector3f();
 			temp.set(normal);
 			double dot = temp.dot(tangent);
 			temp.scale((float) dot);
 			tangent_orth.sub(tangent, temp);
 			
 			Vector3f bitangent_orth = new Vector3f();
 			Vector3f temp2 = new Vector3f();
 			temp2.set(tangent_orth);
 			double dot2 = temp2.dot(bitangent);
 			temp2.scale((float) dot2);
 			temp2.scale(1 / temp2.lengthSquared());
 			Vector3f temp3 = new Vector3f();
 			temp3.set(normal);
 			double dot3 = temp3.dot(bitangent);
 			temp3.scale((float) dot3);
 			Vector3f temp4 = new Vector3f();
 			temp4.sub(temp3, temp2);
 			bitangent_orth.sub(bitangent, temp3);
 			
 			Matrix3d mat = new Matrix3d((double)tangent_orth.x,
 										(double)tangent_orth.y,
 										(double)tangent_orth.z,
 										(double)bitangent_orth.x,
 										(double)bitangent_orth.y,
 										(double)bitangent_orth.z,
 										(double)normal.x,
 										(double)normal.y,
 										(double)normal.z);
 			
 			double determinant = mat.determinant();
 			
 			tangent_orth.normalize();
 			bitangent_orth.normalize();
 			
 			/* TODO PA1: Compute handedness of bitangent, as explained on Lengyel's site. */			
 			float handedness = 1.0f;
 			handedness = (float) (determinant / Math.abs(determinant));
 			
 			
 			/* Store the normalized result in the first 3 components, and the handedness in the last one */		
 			result.put(4 * vIndex + 0, tangent_orth.x);
 			result.put(4 * vIndex + 1, tangent_orth.y);
 			result.put(4 * vIndex + 2, tangent_orth.z);
 			result.put(4 * vIndex + 3, handedness);			
 		}
 		
 		return result;
 	}
 	
 	
 	
 	/**
 	 * Creates a shallow copy of the given mesh (it will share references to all member data).
 	 * This allows us to do useful things like create many instances of some object with
 	 * different names and materials but only one set of float buffers for all instances.
 	 */
 	public abstract Mesh clone();
 
 	/**
 	 * Returns the name of this mesh, which can be specified by a model file or set in code.
 	 * Meshes can be retrieved by name out of a `Geometry` object. 
 	 */
 	public String getName()
 	{
 		return mName;
 	}
 	
 	/**
 	 * Sets the name of this mesh.
 	 */
 	public void setName(String name)
 	{
 		mName = name;
 	}
 
 	/**
 	 * Returns the material used to render this mesh.
 	 */
 	public Material getMaterial()
 	{
 		return mMaterial;
 	}
 
 	/**
 	 * Sets the material used to render this mesh. Must not be null.
 	 */
 	public void setMaterial(Material mat)
 	{
 		mMaterial = mat;
 	}
 	
 	/**
 	 * Returns the number of vertices in this mesh.
 	 * 
 	 * This is computed from the size of the vertex data buffer.
 	 */
 	public int getVertexCount()
 	{
 		if (mVertexData == null)
 		{
 			return 0;
 		}
 		else
 		{
 			return mVertexData.capacity() / 3;
 		}
 	}
 	
 	/**
 	 * Returns vertex data buffer. Format is 3 floats per vertex, tightly 
 	 * packed: {x1, y1, z1, x2, y2, z2, ...}.
 	 */
 	public FloatBuffer getVertexData()
 	{
 		return mVertexData;
 	}
 
 	/**
 	 * Sets the vertex data buffer. Format must be 3 floats per vertex, tightly 
 	 * packed: {x1, y1, z1, x2, y2, z2, ...}.
 	 */
 	public void setVertexData(FloatBuffer vertices)
 	{
 		mVertexData = vertices;
 	}
 
 	/**
 	 * Returns normal data buffer. Format is 3 floats per normal, tightly 
 	 * packed: {x1, y1, z1, x2, y2, z2, ...}.
 	 */
 	public FloatBuffer getNormalData()
 	{
 		return mNormalData;
 	}
 
 	/**
 	 * Sets normal data buffer. Format must be 3 floats per normal, tightly 
 	 * packed: {x1, y1, z1, x2, y2, z2, ...}.
 	 */
 	public void setNormalData(FloatBuffer normals)
 	{
 		mNormalData = normals;
 	}
 
 	/**
 	 * Returns texture coordinate data buffer. Format is 2 floats per texcoord, 
 	 * tightly packed: {u1, v1, u2, v2, ...}.
 	 */
 	public FloatBuffer getTexCoordData()
 	{
 		return mTexCoordData;
 	}
 	
 	/**
 	 * Sets texture coordinate data buffer. Format is 2 floats per texcoord, 
 	 * tightly packed: {u1, v1, u2, v2, ...}.
 	 */
 	public void setTexCoordData(FloatBuffer texcoords)
 	{
 		mTexCoordData = texcoords;
 	}
 
 	/**
 	 * Returns the number of polygons in this mesh.
 	 * 
 	 * This is calculated based on the size of the polygon index buffer and the size of 
 	 * polygons from the subclass.
 	 */
 	public int getPolygonCount()
 	{
 		if (mPolygonData == null)
 		{
 			return 0;
 		}
 		else
 		{
 			return mPolygonData.capacity() / getVerticesPerPolygon();
 		}
 	}
 	
 	/**
 	 * Returns polygon index buffer. Format is `getVerticesPerPolygon()` ints per polygon 
 	 * specifying the vertex indices of that polygon in counterclockwise winding order. 
 	 * For example, for triangles: {i11, i12, i13, i21, i22, i23, ...}.
 	 */
 	public IntBuffer getPolygonData()
 	{
 		return mPolygonData;
 	}
 	
 	/**
 	 * Sets the polygon index buffer. Format Must be `getVerticesPerPolygon()` ints per polygon
 	 * specifying the vertices of that polygon.
 	 */
 	public void setPolygonData(IntBuffer polys)
 	{
 		mPolygonData = polys;
 	}
 
 	/**
 	 * Returns the edge index buffer. Format is the same as the polygon buffer (with only
 	 * 2 indices per edge, of course). The edge buffer is not automatically initialized to all 
 	 * edges in the mesh; it might contain a subset of edges, depending on the application. 
 	 */
 	public IntBuffer getEdgeData()
 	{
 		return mEdgeData;
 	}
 	
 	/**
 	 * Sets edge index buffer. Format is the same as the polygon index buffer (with
 	 * only 2 indices per edge, of course).  
 	 */
 	public void setEdgeData(IntBuffer edges)
 	{
 		mEdgeData = edges;
 	}
 	
 	/** 
 	 * Releases OpenGL resources owned by this mesh or its material.
 	 */
 	public void releaseGPUResources(GL2 gl)
 	{
 		mMaterial.releaseGPUResources(gl);
 	}
 }
