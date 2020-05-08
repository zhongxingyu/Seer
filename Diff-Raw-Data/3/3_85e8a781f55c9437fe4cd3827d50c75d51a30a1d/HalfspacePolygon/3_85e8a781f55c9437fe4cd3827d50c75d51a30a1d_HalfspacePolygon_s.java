 package com.sastraxi.chisel.math;
 
 import com.badlogic.gdx.math.*;
 import com.badlogic.gdx.utils.Array;
 import com.sastraxi.chisel.map.Brush;
 
 import java.util.*;
 
 
 public class HalfspacePolygon {
 
 	// TODO: somehow let static classes like this be "tasks" to be taken on by worker threads.
 	// idea guy: Tasks.ConcurrentLinkedQueue<Runnable>
 
 	/**
 	 * Create a Matrix3 as if (a,b,c) were its columns.
 	 * @param a the first column (a.x is top-left, a.y is middle-left, a.z is bottom-left)
 	 * @param b the second column
 	 * @param c the third column
 	 * @return a new Matrix3
 	 */
 	private static Matrix3 createSideBySide(Vector3 a, Vector3 b, Vector3 c) {
 		Matrix3 mat = new Matrix3();
 		mat.val[Matrix3.M00] = a.x; mat.val[Matrix3.M01] = b.x; mat.val[Matrix3.M02] = c.x;
 		mat.val[Matrix3.M10] = a.y; mat.val[Matrix3.M11] = b.y; mat.val[Matrix3.M12] = c.y;
 		mat.val[Matrix3.M20] = a.z; mat.val[Matrix3.M21] = b.z; mat.val[Matrix3.M22] = c.z;
 		return mat;
 	}
 
 	/**
 	 * Take a list of planes that denote half-spaces and turn it into a list of vertices and faces.
 	 * This solves the Vertex enumeration problem and then goes one step further, turning the vertices
 	 * into faces based on the original planes (each plane becomes a face)
 	 *
 	 * This algorithm does not work with degenerate vertices (i.e. where a vertex lays on 4+ input planes).
 	 * I do not believe that Hammer creates such representations, and we wont\'t either.
 	 *
 	 * @param planes the list of planes.
 	 * @return a Brush that can be used to render the convex polytope.
 	 */
 	public static Brush toConvex(Array<Plane> planes) {
 
 		// 3 planes intersect at a point (vertex), as long as they are not visible from an outside face.
 		// 2 planes intersect at a line segment.
 		// 2 vertices that share 2 planes are an edge
 		// 1 face per plane
 
 		// todo optimize out inner-loop object creation w/object pool
 
 		Array<Vector3> vertices = new Array<Vector3>();
 		Array<Array<Integer>> vertexList = new Array<Array<Integer>>();
 		Array<Array<int[]>> edgeList = new Array<Array<int[]>>();
 
 		// create arrays
 		for (int x = 0; x < planes.size; ++x) {
 			edgeList.add(new Array<int[]>());
 			for (int y = 0; y < planes.size; ++y) {
 				vertexList.add(new Array<Integer>());
 			}
 		}
 
 		// gather [plane_index, plane_index] -> ArrayList<Integer>
 		for (int a = 0; a < planes.size; ++a) {
 			Plane plane_a = planes.get(a);
 
 			for (int b = 0; b < a; ++b) {
 				Plane plane_b = planes.get(b);
 
 				for (int c = 0; c < b; ++c) {
 					Plane plane_c = planes.get(c);
 
 					// here I used the following:
 					// http://geomalgorithms.com/a05-_intersect-1.html#Intersection-of-3%20Planes
 
 					Vector3 cp_a = plane_b.normal.cpy().crs(plane_c.normal);
 
 					float determinant = plane_a.normal.dot(cp_a);
 					if (Math.abs(determinant) < LocalMath.EPSILON) {
 						// the planes do not intersect.
 						// System.out.println("No intersection: " + plane_a + " |\t" + plane_b + " |\t" + plane_c);
 						// System.out.println("cp_a -> " + cp_a);
 						// System.out.println("plane_a.normal -> " + plane_a.normal);
 						continue;
 					}
 
 					Vector3 cp_b = plane_c.normal.cpy().crs(plane_a.normal);
 					Vector3 cp_c = plane_a.normal.cpy().crs(plane_b.normal);
 
 					// the point where all planes intersect
 					Vector3 intersection = new Vector3(0f, 0f, 0f)
 							.add(cp_a.scl(-plane_a.getD()))
 							.add(cp_b.scl(-plane_b.getD()))
 							.add(cp_c.scl(-plane_c.getD()))
 							.scl(1f / determinant);
 
 					// add this vertex to the master list
 					int v_i = vertices.size;
 					vertices.add(intersection);
 					System.out.println(intersection);
 
 					// add to per-edge vertex lists
 					vertexListPush(vertexList, a * planes.size + b, v_i);
 					vertexListPush(vertexList, a * planes.size + c, v_i);
 					vertexListPush(vertexList, b * planes.size + c, v_i);
 				}
 			}
 		}
 
 		// try to discard each vertex with each plane
 		for (int a = 0; a < planes.size; ++a) {
 			Plane plane_a = planes.get(a);
 			for (int i = 0; i < vertices.size; ++i) {
 				Vector3 vertex = vertices.get(i);
 				if (vertex == null) continue;
 
 				if (plane_a.distance(vertex) > LocalMath.EPSILON) {
 					// vertex on plane side; that is, an outward face on the polyhedron can see this point.
 					// however, this would make the polyhedron concave, so discard the point.
 					vertices.set(i, null);
 				}
 			}
 		}
 
 		// gather edges
 		for (int a = 0; a < planes.size; ++a) {
 			Plane plane_a = planes.get(a);
 
 			for (int b = 0; b < a; ++b) {
 				Plane plane_b = planes.get(b);
 
 				Array<Integer> lineVertices = vertexList.get(a * planes.size + b);
 
 				// remove all vertices that have been discarded in the previous nested loop (impl.: null)
 				// todo could do this a lot faster by adding to edge[] things that aren't null and asserting there are no more non-nulls in the list
 				for (Iterator<Integer> x = lineVertices.iterator(); x.hasNext();) {
 					int v_i = x.next();
 					if (vertices.get(v_i) == null) {
 						x.remove();;
 					}
 				}
 
 				if (lineVertices.size == 0) continue; // the two planes don't meet (are parallel)
 				if (lineVertices.size == 1) {
 					System.out.println(lineVertices.get(0));
 					continue;
 				}
 				assert(lineVertices.size == 2);
 
 				int[] edge = new int[] {lineVertices.get(LocalMath.EDGE_START),
 						                lineVertices.get(LocalMath.EDGE_END)};
 
 				// add to per-plane edge lists
 				edgeList.get(a).add(edge);
 				edgeList.get(b).add(edge);
 
 			}
 		}
 
 		// gather faces
 		Array<Face> faces = new Array<Face>();
 		for (int a = 0; a < planes.size; ++a) {
 
 			// make sure we have a closed set of edges
 			Array<int[]> edges = edgeList.get(a);
 			assert(new Face(edges).isClosed());
 
 			// sort edges; pick whatever is first to start with and add adjoining until we get back to the start.
 			// claim: because we have asserted the edge loop is closed, this loop will eventually finish
 			Array<int[]> sortedEdges = new Array<int[]>();
			int i = 0;
 			sortedEdges.add(edges.get(0));
 			while (sortedEdges.size < edges.size) {
 				if (edges.get(i)[LocalMath.EDGE_START] == sortedEdges.peek()[LocalMath.EDGE_END]) {
 					sortedEdges.add(edges.get(i));
 				} else if (edges.get(i)[LocalMath.EDGE_END] == sortedEdges.peek()[LocalMath.EDGE_END]) {
 					// in opposite order; flip it
 					sortedEdges.add(new int[] {
 						edges.get(i)[LocalMath.EDGE_END],
 						edges.get(i)[LocalMath.EDGE_START]
 					});
 				}
 				i = (i + 1) % edges.size;
 			}
 
 			// create the face
 			Face candidate = new Face(sortedEdges);
 			assert(candidate.isClosed());
 			assert(candidate.isOrdered()); // what we just did
 			faces.add(candidate);
 		}
 
 		return new Brush(vertices, faces);
 	}
 
 	private static void vertexListPush(Array<Array<Integer>> vertexList, int i, int v_i) {
 		Array<Integer> l = vertexList.get(i);
 		if (l == null) {
 			l = new Array<Integer>();
 			vertexList.set(i, l);
 		}
 		l.add(v_i);
 	}
 
 	public static ArrayList<Plane> fromConvex(Brush brush) {
 		// todo take each face and turn it into a cutting plane
 		// if any faces aren't convex throw a NonConvexFaceException
 		return null;
 	}
 
 }
