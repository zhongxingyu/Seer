 package com.razh.tiling;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes.Usage;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.Vector3;
 
 /**
  * Factory class for generating geometry for various MeshActors.
  */
 public class Geometry {
 	private static Mesh mBillboard;
 	private static Mesh mTriangularBipyramid;
 	private static Mesh mOctahedron;
 	private static Mesh mHexagonalBipyramid;
 	private static Mesh mOctagonalBipyramid;
 
 	public static Mesh createBillboard() {
 		if (mBillboard == null) {
 			mBillboard = new Mesh(Mesh.VertexDataType.VertexBufferObject,
 	                     true, 4, 4,
 	                     new VertexAttribute(Usage.Position, 3,
 	                                         ShaderProgram.POSITION_ATTRIBUTE));
 
 			mBillboard.setVertices(new float[]{-0.5f, -0.5f, 0.0f,
 			                                    0.5f, -0.5f, 0.0f,
 			                                   -0.5f,  0.5f, 0.0f,
 			                                    0.5f,  0.5f, 0.0f});
 			mBillboard.setIndices(new short[]{0, 1, 2, 3});
 		}
 
 		return mBillboard;
 	}
 
 	public static Mesh createTriangularBipyramid() {
 		if (mTriangularBipyramid == null) {
 			mTriangularBipyramid = createBipyramid(3);
 		}
 
 		return mTriangularBipyramid;
 	}
 
 	public static Mesh createOctahedron() {
 		if (mOctahedron == null) {
 			mOctahedron = createBipyramid(4);
 		}
 
 		return mOctahedron;
 	}
 
 	public static Mesh createHexagonalBipyramid() {
 		if (mHexagonalBipyramid == null) {
 			mHexagonalBipyramid = createBipyramid(6);
 		}
 
 		return mHexagonalBipyramid;
 	}
 
 	public static Mesh createOctagonalBipyramid() {
 		if (mOctagonalBipyramid == null) {
 			mOctagonalBipyramid = createBipyramid(8);
 		}
 
 		return mOctagonalBipyramid;
 	}
 
 	public static Mesh createBipyramid(int subdivisions) {
 		if (subdivisions < 3) {
 			return null;
 		}
 
 		// For normals to work, each face must have its own separate set of vertices.
 		// Two sets of faces with three vertices each (first 6).
 		int numVertices = subdivisions * 6;
 		// Three vertices per face. Two faces per side (bipyramid).
 		int numIndices = subdivisions * 6;
 		Mesh mesh = new Mesh(Mesh.VertexDataType.VertexBufferObject,
		                     true, numVertices, numIndices,
		                     VertexAttribute.Position(),
		                     VertexAttribute.Normal());
 
 		// Array of unique vertices, with the top vertex at 0, and bottom vertex at end.
 		float[] shapeVertices = calculateVertices(subdivisions);
 		// Three vertex components and three normal components.
 		float[] vertices = new float[numVertices * 6];
 		short[] indices = new short[numIndices];
 
 		int vtxIndex = 0;
 		int idxIndex = 0;
 
 		// Push the generated vertices such that each face has its own set of three
 		// vertices and each vertex has its own normal.
 		// Reset vtxIndex.
 		vtxIndex = 0;
 		float ax, ay, az, bx, by, bz, cx, cy, cz;
 		float nx, ny, nz;
 		Vector3 normal;
 		for (int i = 0; i < subdivisions; i++) {
 			// Top face.
 			// Vertex 0.
 			ax = shapeVertices[0];
 			ay = shapeVertices[1];
 			az = shapeVertices[2];
 			// Vertex 1.
 			bx = shapeVertices[3 * (i + 1)];
 			by = shapeVertices[3 * (i + 1) + 1];
 			bz = shapeVertices[3 * (i + 1) + 2];
 			// Vertex 2.
 			cx = shapeVertices[3 * ((i + 1) % subdivisions + 1)];
 			cy = shapeVertices[3 * ((i + 1) % subdivisions + 1) + 1];
 			cz = shapeVertices[3 * ((i + 1) % subdivisions + 1) + 2];
 
 			// Normals.
 			normal = calculateFaceNormal(ax, ay, az,
 			                             bx, by, bz,
 			                             cx, cy, cz);
 			nx = normal.x;
 			ny = normal.y;
 			nz = normal.z;
 
 			// Vertex 0.
 			vertices[vtxIndex++] = ax;
 			vertices[vtxIndex++] = ay;
 			vertices[vtxIndex++] = az;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 
 			// Vertex 1.
 			vertices[vtxIndex++] = bx;
 			vertices[vtxIndex++] = by;
 			vertices[vtxIndex++] = bz;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 
 			// Vertex 2.
 			vertices[vtxIndex++] = cx;
 			vertices[vtxIndex++] = cy;
 			vertices[vtxIndex++] = cz;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 
 			// Bottom face.
 			// Vertex 0.
 			ax = shapeVertices[3 * (subdivisions + 1)];
 			ay = shapeVertices[3 * (subdivisions + 1) + 1];
 			az = shapeVertices[3 * (subdivisions + 1) + 2];
 
 			// Normals.
 			normal = calculateFaceNormal(ax, ay, az,
 			                             cx, cy, cz,
 			                             bx, by, bz);
 			nx = normal.x;
 			ny = normal.y;
 			nz = normal.z;
 
 			// Vertex 0.
 			vertices[vtxIndex++] = ax;
 			vertices[vtxIndex++] = ay;
 			vertices[vtxIndex++] = az;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 
 			// Vertex 1.
 			vertices[vtxIndex++] = cx;
 			vertices[vtxIndex++] = cy;
 			vertices[vtxIndex++] = cz;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 
 			// Vertex 2.
 			vertices[vtxIndex++] = bx;
 			vertices[vtxIndex++] = by;
 			vertices[vtxIndex++] = bz;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 		}
 
 		for (short i = 0; i < numIndices; i++) {
 			indices[idxIndex++] = i;
 		}
 
 		mesh.setVertices(vertices);
 		mesh.setIndices(indices);
 
 		return mesh;
 	}
 
 	public static Mesh createBicolorBipyramid(int subdivisions, Color color0, Color color1) {
 		if (subdivisions < 3) {
 			return null;
 		}
 
 		// For normals to work, each face must have its own separate set of vertices.
 		// Two sets of faces with three vertices each and one color index.
 		int numVertices = subdivisions * 6;
 		// Three vertices per face. Two faces per side (bipyramid).
 		int numIndices = subdivisions * 6;
 		Mesh mesh = new Mesh(Mesh.VertexDataType.VertexBufferObject,
		                     true, numVertices, numIndices,
		                     VertexAttribute.Position(),
		                     VertexAttribute.Normal(),
		                     new VertexAttribute(Usage.Generic, 3,
		                                         ShaderProgram.COLOR_ATTRIBUTE));
 
 		// Array of unique vertices, with the top vertex at 0, and bottom vertex at end.
 		float[] shapeVertices = calculateVertices(subdivisions);
 		// Three vertex components, three normal components, and three color components.
 		float[] vertices = new float[numVertices * 9];
 		short[] indices = new short[numIndices];
 
 		int vtxIndex = 0;
 		int idxIndex = 0;
 
 		float r0, g0, b0, r1, g1, b1;
 		r0 = color0.r;
 		g0 = color0.g;
 		b0 = color0.b;
 
 		r1 = color1.r;
 		g1 = color1.g;
 		b1 = color1.b;
 
 		// Push the generated vertices such that each face has its own set of three
 		// vertices and each vertex has its own normal.
 		// Reset vtxIndex.
 		vtxIndex = 0;
 		float ax, ay, az, bx, by, bz, cx, cy, cz;
 		float nx, ny, nz;
 		Vector3 normal;
 		for (int i = 0; i < subdivisions; i++) {
 			// Top face.
 			// Vertex 0.
 			ax = shapeVertices[0];
 			ay = shapeVertices[1];
 			az = shapeVertices[2];
 			// Vertex 1.
 			bx = shapeVertices[3 * (i + 1)];
 			by = shapeVertices[3 * (i + 1) + 1];
 			bz = shapeVertices[3 * (i + 1) + 2];
 			// Vertex 2.
 			cx = shapeVertices[3 * ((i + 1) % subdivisions + 1)];
 			cy = shapeVertices[3 * ((i + 1) % subdivisions + 1) + 1];
 			cz = shapeVertices[3 * ((i + 1) % subdivisions + 1) + 2];
 
 			// Normals.
 			normal = calculateFaceNormal(ax, ay, az,
 			                             bx, by, bz,
 			                             cx, cy, cz);
 			nx = normal.x;
 			ny = normal.y;
 			nz = normal.z;
 
 			// Vertex 0.
 			vertices[vtxIndex++] = ax;
 			vertices[vtxIndex++] = ay;
 			vertices[vtxIndex++] = az;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 			// Color.
 			vertices[vtxIndex++] = r0;
 			vertices[vtxIndex++] = g0;
 			vertices[vtxIndex++] = b0;
 
 			// Vertex 1.
 			vertices[vtxIndex++] = bx;
 			vertices[vtxIndex++] = by;
 			vertices[vtxIndex++] = bz;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 			// Color.
 			vertices[vtxIndex++] = r0;
 			vertices[vtxIndex++] = g0;
 			vertices[vtxIndex++] = b0;
 
 			// Vertex 2.
 			vertices[vtxIndex++] = cx;
 			vertices[vtxIndex++] = cy;
 			vertices[vtxIndex++] = cz;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 			// Color.
 			vertices[vtxIndex++] = r0;
 			vertices[vtxIndex++] = g0;
 			vertices[vtxIndex++] = b0;
 
 			// Bottom face.
 			// Vertex 0.
 			ax = shapeVertices[3 * (subdivisions + 1)];
 			ay = shapeVertices[3 * (subdivisions + 1) + 1];
 			az = shapeVertices[3 * (subdivisions + 1) + 2];
 
 			// Normals.
 			normal = calculateFaceNormal(ax, ay, az,
 			                             cx, cy, cz,
 			                             bx, by, bz);
 			nx = normal.x;
 			ny = normal.y;
 			nz = normal.z;
 
 			// Vertex 0.
 			vertices[vtxIndex++] = ax;
 			vertices[vtxIndex++] = ay;
 			vertices[vtxIndex++] = az;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 			// Color.
 			vertices[vtxIndex++] = r1;
 			vertices[vtxIndex++] = g1;
 			vertices[vtxIndex++] = b1;
 
 			// Vertex 1.
 			vertices[vtxIndex++] = cx;
 			vertices[vtxIndex++] = cy;
 			vertices[vtxIndex++] = cz;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 			// Color.
 			vertices[vtxIndex++] = r1;
 			vertices[vtxIndex++] = g1;
 			vertices[vtxIndex++] = b1;
 
 			// Vertex 2.
 			vertices[vtxIndex++] = bx;
 			vertices[vtxIndex++] = by;
 			vertices[vtxIndex++] = bz;
 			// Normal.
 			vertices[vtxIndex++] = nx;
 			vertices[vtxIndex++] = ny;
 			vertices[vtxIndex++] = nz;
 			// Color.
 			vertices[vtxIndex++] = r1;
 			vertices[vtxIndex++] = g1;
 			vertices[vtxIndex++] = b1;
 		}
 
 		for (short i = 0; i < numIndices; i++) {
 			indices[idxIndex++] = i;
 		}
 
 		mesh.setVertices(vertices);
 		mesh.setIndices(indices);
 
 		return mesh;
 	}
 
 	/**
 	 * Returns the perimeter vertices which define the shape.
 	 */
 	public static float[] calculateVertices(int subdivisions) {
 		if (subdivisions < 3) {
 			return null;
 		}
 
 		float[] shapeVertices = new float[(subdivisions + 2) * 3];
 
 		// Generate vertices in reverse order (as counterclockwise is front-facing).
 		float subdivAngle = (float) -(Math.PI * 2 / subdivisions);
 		int vtxIndex = 0;
 
 		// Generate the vertices which comprise the shape.
 		// Top vertex.
 		shapeVertices[vtxIndex++] = 0.0f;
 		shapeVertices[vtxIndex++] = 0.0f;
 		shapeVertices[vtxIndex++] = 1.0f;
 
 		// Side vertices.
 		for (int i = 0; i < subdivisions; i++) {
 			shapeVertices[vtxIndex++] = (float) Math.sin(i * subdivAngle);
 			shapeVertices[vtxIndex++] = (float) Math.cos(i * subdivAngle);
 			shapeVertices[vtxIndex++] = 0.0f;
 		}
 
 		// Bottom vertex.
 		shapeVertices[vtxIndex++] = 0.0f;
 		shapeVertices[vtxIndex++] = 0.0f;
 		shapeVertices[vtxIndex++] = -1.0f;
 
 		return shapeVertices;
 	}
 
 	public static float[] calculateVertices2D(int subdivisions) {
 		if (subdivisions < 3) {
 			return null;
 		}
 
 		float[] shapeVertices = new float[subdivisions * 2];
 
 		// Generate vertices in reverse order (as counterclockwise is front-facing).
 		float subdivAngle = (float) -(Math.PI * 2 / subdivisions);
 		int vtxIndex = 0;
 
 		// Generate the vertices which comprise the shape.
 		// Side vertices.
 		for (int i = 0; i < subdivisions; i++) {
 			shapeVertices[vtxIndex++] = (float) Math.sin(i * subdivAngle);
 			shapeVertices[vtxIndex++] = (float) Math.cos(i * subdivAngle);
 		}
 
 		return shapeVertices;
 	}
 
 	public static Vector3 calculateFaceNormal(float ax, float ay, float az,
 	                                          float bx, float by, float bz,
 	                                          float cx, float cy, float cz) {
 		// Cross the vector from CB with that of AB and normalize.
 		return new Vector3(cx - bx, cy - by, cz - bz)
 		              .crs(ax - bx, ay - by, az - bz)
 		              .nor();
 	}
 
 }
