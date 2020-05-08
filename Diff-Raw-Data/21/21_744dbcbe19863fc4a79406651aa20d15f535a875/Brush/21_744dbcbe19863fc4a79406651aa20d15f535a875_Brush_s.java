 package com.sastraxi.chisel.map;
 
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g3d.Material;
 import com.badlogic.gdx.graphics.g3d.Renderable;
 import com.badlogic.gdx.graphics.g3d.RenderableProvider;
 import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
 import com.badlogic.gdx.math.Plane;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Pool;
 import com.sastraxi.chisel.image.ColourManager;
 import com.sastraxi.chisel.math.Face;
 import com.sastraxi.chisel.math.LocalMath;
 
 import java.util.Arrays;
 
 public class Brush implements RenderableProvider {
 
 	public static float RESOLUTION = 0.00001f;
 
 	// actual data
 	private final Array<Face> faces;
 	private final Array<Vector3> vertices;
 	private final Color colour;
 
 	/**
 	 * A brush is not allowed to contain vertices that do not exist on any faces.
 	 */
 	public Brush(Array<Vector3> vertices, Array<Face> faces) {
 		this.vertices = vertices;
 		this.faces = faces;
 		this.colour = ColourManager.next();
 
 		// assert face correctness: planar + convex
 		for (Face face: this.faces) {
 			assert(face.arity() >= 3);
 			assert(face.isConvex(this.vertices));
 		}
 	}
 
 	// cache
 	private Mesh _mesh;
 	private boolean validMesh = false;
 
 	private Mesh getMesh() {
 		if (!validMesh) {
 			if (_mesh != null) _mesh.dispose();
 			_mesh = generateMesh();
 			validMesh = true;
 		}
 		return _mesh;
 	}
 
 	/**
 	 * Generates a non-smooth mesh (think: D&D dice) that represents this Brush,
 	 * given its geometry.
 	 */
 	private Mesh generateMesh() {
 
 		// each face has (edges - 2) triangles.
 		int n_indices = 0, n_vertices = 0;
 		for (Face face: faces) {
 			n_vertices += face.arity();
 			n_indices += (face.arity() - 2) * 3;
 		}
 
 		int i = 0, v = 0;
 		short[] indices = new short[n_indices]; // each triangle has 3 vertices.
 		float[] verts = new float[n_vertices * 6]; // each vertex has a number of attributes; see below*
 		for (Face face: faces) {
 
 			Vector3 normal = face.getNormal(this.vertices).nor();
 
 			// assemble vertices
 			int v_start = v / 6;
 			for (int t = 0; t < face.arity(); ++t) {
 				Vector3 p = this.vertices.get(face.getEdges().get(t)[LocalMath.EDGE_START]);
 				verts[v++] = p.x; verts[v++] = p.y; verts[v++] = p.z;
 				verts[v++] = normal.x; verts[v++] = normal.y; verts[v++] = normal.z;
 			}
 
 			// assemble indices (triangle fan)
 			for (int t = 0; t < face.arity() - 2; ++t) {
 				indices[i++] = (short) (v_start);
 				indices[i++] = (short) (v_start+t+1);
 				indices[i++] = (short) (v_start+t+2);
 			}
 
 		}
 
 		// the total number of components in the VertexAttributes attached
 		// must match the multiplier in the verts[] definition above*
 		Mesh mesh = new Mesh(true, n_vertices, n_indices,
 				new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
 				new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"));
 
 		System.out.println("Generated Mesh:");
 		System.out.println(Arrays.toString(verts));
 		System.out.println(Arrays.toString(indices));
 
 		mesh.setVertices(verts);
 		mesh.setIndices(indices);
 
 		return mesh;
 	}
 
 	private Mesh generateWireframeMesh() {
 		return null;
 	}
 
 	public void populate(Renderable r) {
 		r.mesh = getMesh();
 		r.material = new Material(ColorAttribute.createDiffuse(colour));
 		r.primitiveType = GL10.GL_TRIANGLES;
 		r.meshPartSize = r.mesh.getNumIndices();
 	}
 
 	public void invalidateMesh() {
 		this.validMesh = false;
 	}
 
 	@Override
 	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
 		Renderable r = pool.obtain();
 		populate(r);
 		renderables.add(r);
 	}
 
 	/**
 	 * Does not modify the Brush it is called on.
 	 *
 	 * @param plane the splitting plane.
 	 * @param returnPositive return the new convex brush on the positive side of the plane (with normal).
 	 * @param returnNegative return the new convex brush on the negative side of the plane.
 	 *
 	 * @return a brush array. Get the brushes by e.g. carve(plane, true, false)[Brush.POSITIVE_SIDE]
 	 */
 	public Brush[] carve(Plane plane, boolean returnPositive, boolean returnNegative) {
 		// todo for each face, the plane either:
 		// a) misses the face;
 		// b) glances the face (some edge of the face is on the plane)
 		// c) splits the face.
 		return null;
 	}
 
 	public Array<Face> getFaces() {
 		return faces;
 	}
 
 	public Array<Vector3> getVertices() {
 		return vertices;
 	}
 
 	/**
 	 * Compares the list of Faces and vertices differently (topologically).
 	 * 1. Find a mapping that is one-to-one and onto from the vertex
 	 *    indices of this brush to the vertex indices of the other.
 	 * 2. Re-write our Face edge lists in terms of the other brush's vertex indices.
 	 * 3. Require that each re-written Face from our faces topologically
 	 *    equals some face in the other object.
 	 * @param o the object to compare to.
 	 * @return true, if both Brush objects represent the same geometry, false otherwise.
 	 */
 	@Override
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (o == null || getClass() != o.getClass()) return false;
 
 		Brush other  = (Brush) o;
 
 		if (other.vertices.size != vertices.size) return false;
 		if (other.faces.size != faces.size) return false;
 
 		// first map vertices, _map[i] -> the index of i in other
 		int[] _map = new int[vertices.size];
 		for (int i = 0; i < vertices.size; ++i) {
 			Vector3 v_this = this.vertices.get(i);
 
 			for (int j = 0; j < vertices.size; ++j) {
 				Vector3 v_other = other.vertices.get(j);
 
 				if (v_this.equals(v_other)) {
 					_map[i] = j;
 				}
 			}
 		}
 
 		// create expected faces (based on mapping),
 		// then look for them in the other object
 		for (int i = 0; i < faces.size; ++i) {
 
 			Array<int[]> expectedFaceEdges = new Array<int[]>();
 			for (int[] edge: faces.get(i).getEdges()) {
 				expectedFaceEdges.add(new int[] {
 					_map[edge[LocalMath.EDGE_START]],
 					_map[edge[LocalMath.EDGE_END]]
 				});
 			}
 
 			Face expectedFace = new Face(expectedFaceEdges);
 			if (!other.faces.contains(expectedFace, false)) {
 				System.out.println("Couldn't find " + expectedFace.toString2(vertices));
 				return false;
 			}
 
 		}
 
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int result = faces.hashCode();
 		result = 31 * result + vertices.hashCode();
 		return result;
 	}
 
 	@Override
 	public String toString() {
 	    StringBuilder b = new StringBuilder();
 		b.append("Brush(" + faces.size + "):\n");
 		for (Face face: faces) {
 			b.append(" - ");
 			b.append(face.toString(vertices));
 			b.append("\n");
 		}
 		return b.toString();
 	}
 }
