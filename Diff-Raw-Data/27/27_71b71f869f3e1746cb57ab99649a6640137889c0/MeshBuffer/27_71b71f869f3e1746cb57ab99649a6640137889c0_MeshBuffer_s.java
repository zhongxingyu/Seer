 package de.tum.in.cindy3dplugin.jogl.primitives.renderers;
 
 import java.nio.DoubleBuffer;
 import java.nio.IntBuffer;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import de.tum.in.cindy3dplugin.Cindy3DViewer.NormalType;
 import de.tum.in.cindy3dplugin.jogl.Util;
 import de.tum.in.cindy3dplugin.jogl.primitives.Mesh;
 
 /**
  * Holds vertex and index buffers belonging to a certain mesh object.
  */
 public class MeshBuffer {
 	/**
 	 * Vertex buffer id of the mesh.
 	 */
 	private int vertexBuffer;
 	/**
 	 * Index buffer id of the mesh.
 	 */
 	private int indexBuffer;
 	/**
 	 * Indicates if mesh has an index buffer.
 	 */
 	private boolean hasIndexBuffer;
 	/**
 	 * Number of indices in the index buffer
 	 */
 	private int indexCount;
 	/**
 	 * Number of vertices in the vertex buffer
 	 */
 	private int vertexCount;
 
 	/**
 	 * Builds vertex buffer and index buffer for a given mesh.
 	 * 
 	 * @param gl
 	 *            GL handle
 	 * @param m
 	 *            mesh
 	 */
 	public MeshBuffer(GL gl, Mesh m) {
 		createBuffers(gl, m);
 	}
 
 	/**
 	 * Creates vertex and index buffer for a given mesh.
 	 * 
 	 * @param gl
 	 *            GL handle
 	 * @param m
 	 *            mesh
 	 */
 	private void createBuffers(GL gl, Mesh m) {
 		double[][] positions = m.getPositions();
 		double[][] normals = m.getNormals();
 		
 		if (m.getNormalType() == NormalType.PER_VERTEX) {
 			vertexCount = m.getColumnCount() * m.getRowCount();
 			indexCount = m.getFaceCount() * 3;
 			hasIndexBuffer = true;
 		} else {
 			vertexCount = m.getFaceCount() * 3;
 			indexCount = 0;
 			hasIndexBuffer = false;
 		}
 
 		// Position (3 doubles) + normal (3 doubles)
 		DoubleBuffer vertices = DoubleBuffer.allocate(vertexCount * (3 + 3));
 
 		IntBuffer indices = null;
 		if (m.getNormalType() == NormalType.PER_VERTEX) {
 			indices = IntBuffer.allocate(indexCount);
 			for (int i = 0; i < vertexCount; ++i) {
 				vertices.put(positions[i]);
 				vertices.put(normals[i]);
 			}
 		}
 
 		// Iterate over all grid cells (each of which consists of two faces)
 		int faceIndex = 0;
 		for (int row = 0; row < m.getRowMax(); ++row) {
 			for (int column = 0; column < m.getColumnMax(); ++column) {
 				/*
 				 *    v1----v2
 				 *    |    / |
 				 *    |f1 /  |
 				 *    |  / f2|
 				 *    | /    |
 				 *    v3----v4
 				 */
 				int rowPlus1 = (row + 1) % m.getRowCount();
 				int columnPlus1 = (column + 1) % m.getColumnCount();
 
 				int v1Index = m.getVertexIndex(row, column);
 				int v2Index = m.getVertexIndex(row, columnPlus1);
 				int v3Index = m.getVertexIndex(rowPlus1, column);
 				int v4Index = m.getVertexIndex(rowPlus1, columnPlus1);
 
 				if (m.getNormalType() == NormalType.PER_VERTEX) {
 					// f1
 					indices.put(v1Index);
 					indices.put(v2Index);
 					indices.put(v3Index);
 
 					// f2
 					indices.put(v2Index);
 					indices.put(v4Index);
 					indices.put(v3Index);
 				} else {
 					// f1
 					int f1Index = faceIndex++;
 					vertices.put(positions[v1Index]);
 					vertices.put(normals[f1Index]);
 					vertices.put(positions[v2Index]);
 					vertices.put(normals[f1Index]);
 					vertices.put(positions[v3Index]);
 					vertices.put(normals[f1Index]);
 
 					// f2
 					int f2Index = faceIndex++;
 					vertices.put(positions[v2Index]);
 					vertices.put(normals[f2Index]);
 					vertices.put(positions[v4Index]);
 					vertices.put(normals[f2Index]);
 					vertices.put(positions[v3Index]);
 					vertices.put(normals[f2Index]);
 				}
 			}
 		}
 
 		vertices.flip();
 		int tmp[] = new int[1];
 		gl.glGenBuffers(1, tmp, 0);
 		vertexBuffer = tmp[0];
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.capacity()
 				* Util.BYTES_PER_DOUBLE, vertices, GL.GL_STATIC_DRAW);
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
 
 		if (hasIndexBuffer) {
 			indices.flip();
 			gl.glGenBuffers(1, tmp, 0);
 			indexBuffer = tmp[0];
 			gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
 			gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indices.capacity()
 					* Util.BYTES_PER_INT, indices, GL.GL_STATIC_DRAW);
 			gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
 		} else {
 			indexBuffer = 0;
 		}
 	}
 	
 	/**
 	 * Renders the mesh represented by the vertex and index buffer.
 	 * 
 	 * @param gl
 	 *            GL handle
 	 */
 	public void render(GL gl) {
 		GL2 gl2 = gl.getGL2();
 
 		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBuffer);
 		gl2.glVertexPointer(3, GL2.GL_DOUBLE, 6 * Util.BYTES_PER_DOUBLE, 0);
 		gl2.glNormalPointer(GL2.GL_DOUBLE, 6 * Util.BYTES_PER_DOUBLE,
 				3 * Util.BYTES_PER_DOUBLE);
 
 		if (hasIndexBuffer) {
 			gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
 			gl2.glDrawElements(GL2.GL_TRIANGLES, indexCount,
 					GL2.GL_UNSIGNED_INT, 0);
 		} else {
 			gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, vertexCount);
 		}
 	}
 
 	/**
	 * Explicitely releases the buffers on the GPU.
 	 * 
 	 * @param gl
 	 *            GL handle
 	 */
 	public void dispose(GL2 gl) {
 		gl.glDeleteBuffers(2, new int[] { vertexBuffer, indexBuffer }, 0);
 	}
 }
