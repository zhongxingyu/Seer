 package de.tum.in.cindy3dplugin.jogl.primitives.renderers.fixedfunc;
 
 import java.nio.DoubleBuffer;
 import java.nio.IntBuffer;
 
 import javax.media.opengl.GL2;
 
 import de.tum.in.cindy3dplugin.jogl.Util;
 
 /**
  * Represents a single mesh used in a level of detail based rendering
  */
 public class LODMesh {
 	/**
 	 * Vertex buffer of the mesh.
 	 */
 	private int vertexBuffer;
 	/**
 	 * Index buffer of the mesh.
 	 */
 	private int indexBuffer;
 	/**
 	 * Number of faces of the mesh.
 	 */
 	private int faceCount;
 	/**
 	 * Dimension of the vertex position.
 	 */
 	private int vertexPositionDimension;
 	/**
 	 * Length of the longest edge in the mesh.
 	 */
 	private double maxEdgeLength;
 	/**
 	 * Indicates if normals are included in the mesh representation.
 	 */
 	private boolean hasNormals;
 	/**
 	 * Intermediate buffer storing vertex information.
 	 */
 	private DoubleBuffer vertices;
 	/**
 	 * Intermediate buffer storing index information.
 	 */
 	private IntBuffer indices;
 
 	/**
 	 * Creates a new empty mesh with fixed vertex and face count. Mesh
	 * information can be filled by calling {@link #putVertex(double[]) and
 	 * 
 	 * @link #putFace(int, int, int)}.
 	 * 
 	 * @param vertexPositionDimension
 	 *            dimension of the vertex position
 	 * @param vertexCount
 	 *            number of vertices
 	 * @param faceCount
 	 *            number of faces
 	 * @param hasNormals
 	 *            <code>true</code> if the vertex normals are provided and
 	 *            <code>false</false> if the currently set normal should be used during rendering
 	 */
 	public LODMesh(int vertexPositionDimension, int vertexCount, int faceCount,
 			boolean hasNormals) {
 		this.vertexPositionDimension = vertexPositionDimension;
 		this.faceCount = faceCount;
 		this.hasNormals = hasNormals;
 		if (hasNormals) {
 			this.vertices = DoubleBuffer.allocate(vertexCount
 					* (vertexPositionDimension + 3));
 		} else {
 			this.vertices = DoubleBuffer.allocate(vertexCount
 					* vertexPositionDimension);
 		}
 		this.indices = IntBuffer.allocate(faceCount * 3);
 		this.maxEdgeLength = 0;
 	}
 
 	/**
 	 * Adds a vertex, consisting of a position only, to the mesh vertices. The
 	 * length of <code>position</code> has to match
 	 * {@link #vertexPositionDimension}. Additionally, this method ought to be
 	 * called only if the mesh was created without normal support. Otherwise use
 	 * {@link #putVertex(double[], double[])} for adding vertex information.
 	 * 
 	 * @param position
 	 *            vertex position to be added
 	 */
 	public void putVertex(double[] position) {
 		if (hasNormals) {
 			throw new UnsupportedOperationException("Vertex normal expected.");
 		}
 		if (position.length != vertexPositionDimension) {
 			throw new IllegalArgumentException("Wrong position dimension.");
 		}
 		
 		vertices.put(position);
 	}
 
 	/**
 	 * Adds a vertex, consisting of position and normal, to the mesh vertices.
 	 * The length of <code>position</code> has to match
 	 * {@link #vertexPositionDimension} and <code>normal</code> needs to have
 	 * length 3. Additionally, this method ought to be called only if the mesh
 	 * was created with normal support. Otherwise use
 	 * {@link #putVertex(double[])} for adding vertex information.
 	 * 
 	 * @param position
 	 *            vertex position to be added
 	 * @param normal
 	 *            vertex normal to be added
 	 */
 	public void putVertex(double[] position, double[] normal) {
 		if (!hasNormals) {
 			throw new UnsupportedOperationException("Unexpected vertex normal.");
 		}
 		vertices.put(position);
 		vertices.put(normal);
 	}
 
 	/**
 	 * Adds a triangle face to the mesh. The orientation of the resulting
 	 * triangle is specified by the order of indices.
 	 * 
 	 * @param i0
 	 *            vertex id of the first triangle vertex
 	 * @param i1
 	 *            vertex id of the second triangle vertex
 	 * @param i2
 	 *            vertex id of the third triangle vertex
 	 */
 	public void putFace(int i0, int i1, int i2) {
 		indices.put(i0);
 		indices.put(i1);
 		indices.put(i2);
 	}
 
 	/**
 	 * Creates vertex and index buffer from the added vertex and index data.
 	 * 
 	 * After adding vertex and index information calling
 	 * {@link #putVertex(double[])}, {@link #putVertex(double[], double[])} and
 	 * {@link #putFace(int, int, int)}, the mesh representation is finished by
 	 * calling this method. The mesh cannot be changed after this method is
 	 * called.
 	 * 
 	 * @param gl
 	 *            GL handle
 	 */
 	public void finish(GL2 gl) {
 		int[] tmp = new int[2];
 		gl.glGenBuffers(2, tmp, 0);
 		vertexBuffer = tmp[0];
 		indexBuffer = tmp[1];
 
 		vertices.flip();
 		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBuffer);
 		gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.capacity()
 				* Util.SIZEOF_DOUBLE, vertices, GL2.GL_STATIC_DRAW);
 		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
 
 		indices.flip();
 		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
 		gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indices.capacity()
 				* Util.SIZEOF_INT, indices, GL2.GL_STATIC_DRAW);
 		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
 
 		calculateMaxEdgeLength();
 
 		vertices = null;
 		indices = null;
 	}
 
 	/**
 	 * Calculates the length of the longest edge in the mesh.
 	 */
 	private void calculateMaxEdgeLength() {
 		maxEdgeLength = 0;
 
 		int stride = vertexPositionDimension;
 		if (hasNormals) {
 			stride += 3;
 		}
 		
 		for (int i = 0; i < indices.limit(); i += 3) {
 			int i0 = indices.get(i);
 			int i1 = indices.get(i + 1);
 			int i2 = indices.get(i + 2);
 			double[][] v = new double[3][vertexPositionDimension];
 			vertices.position(i0 * stride);
 			vertices.get(v[0], 0, vertexPositionDimension);
 			vertices.position(i1 * stride);
 			vertices.get(v[1], 0, vertexPositionDimension);
 			vertices.position(i2 * stride);
 			vertices.get(v[2], 0, vertexPositionDimension);
 
 			for (int j = 0; j < 3; ++j) {
 				double edgeLengthSq = 0;
 				double[] v0 = v[j];
 				double[] v1 = v[(j + 1) % 3];
 				for (int k = 0; k < vertexPositionDimension; ++k) {
 					edgeLengthSq += Math.pow(v0[k] - v1[k], 2);
 				}
 				maxEdgeLength = Math.max(maxEdgeLength,
 						Math.sqrt(edgeLengthSq));
 			}
 		}
 	}
 
 	/**
 	 * Disposes vertex and index buffers.
 	 * 
 	 * @param gl2
 	 *            GL handle
 	 */
 	public void dispose(GL2 gl2) {
 		gl2.glDeleteBuffers(2, new int[] { vertexBuffer, indexBuffer }, 0);
 	}
 	
 	public boolean isSufficient(double scale, double allowedWorldSpaceError) {
 		double worldSpaceError = scale * maxEdgeLength;
 		return worldSpaceError <= allowedWorldSpaceError;
 	}
 	
 	/**
 	 * @return length of the longest edge in the mesh
 	 */
 	public double getMaxEdgeLength() {
 		return maxEdgeLength;
 	}
 
 	/**
 	 * Renders the mesh. Vertex and index buffer must be created by calling
 	 * {@link #finish(GL2)} before the mesh can be rendered. If the mesh was
 	 * created with {@link #hasNormals} set to <code>true</code>, the specified
 	 * normals are used for shading. If {@link #hasNormals} was set to
 	 * <code>false</code>, no assumption about the normals are made, which means
 	 * that neither state changes concerning the normal are made nor any normals
 	 * are set. Normals set explicitly before calling this rendering method are
 	 * kept and used for shading.
 	 * 
 	 * @param gl2
 	 *            GL handle
 	 */
 	public void render(GL2 gl2) {
 		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBuffer);
 
 		int stride;
 		if (hasNormals) {
 			stride = Util.SIZEOF_DOUBLE * (vertexPositionDimension + 3);
 		} else {
 			stride = Util.SIZEOF_DOUBLE * vertexPositionDimension;
 		}
 
 		gl2.glVertexPointer(vertexPositionDimension, GL2.GL_DOUBLE, stride, 0);
 		
 		if (hasNormals) {
 			gl2.glNormalPointer(GL2.GL_DOUBLE, stride,
 					vertexPositionDimension * Util.SIZEOF_DOUBLE);
 		}
 		gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
 		gl2.glDrawElements(GL2.GL_TRIANGLES, faceCount * 3,
 				GL2.GL_UNSIGNED_INT, 0);
 		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
 		gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
 	}
 }
