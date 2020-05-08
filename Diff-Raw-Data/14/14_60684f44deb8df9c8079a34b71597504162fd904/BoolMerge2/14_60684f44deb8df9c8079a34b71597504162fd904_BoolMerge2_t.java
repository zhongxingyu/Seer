 package net.untoldwind.moredread.model.op.bool.blebopf;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 public class BoolMerge2 {
 	BoolMesh mesh;
 	int firstVertex;
 
 	BoolMerge2() {
 	}
 
 	void mergeFaces(final BoolMesh m, final int firstVertex) {
 		this.mesh = m;
 		this.firstVertex = firstVertex;
 
 		cleanup();
 
 		// Merge faces
 		mergeFaces();
 	}
 
 	/**
 	 * Runs through mesh and makes sure vert/face/edge data is consistent. Most
 	 * importantly: (1) mark edges which are no longer used (2) remove broken
 	 * faces from edges (3) remove faces from mesh which have a single edge
 	 * belonging to no other face (non-manifold edges)
 	 */
 
 	void cleanup() {
 		final Collection<BoolEdge> edges = mesh.getEdges();
 		for (final BoolEdge edge : edges) {
 			final List<BoolFace> faces = new ArrayList<BoolFace>(edge
 					.getFaces());
 
 			for (final BoolFace f : faces) {
 				if (f.getTAG() != BoolTag.UNCLASSIFIED) {
 					edge.removeFace(f);
 				}
 			}
 			if (edge.getFaces().size() == 0) {
 				edge.setUsed(false);
 			}
 		}
 
 		final List<BoolVertex> vertices = mesh.getVertices();
 
 		for (final BoolVertex vertex : vertices) {
 			if (vertex.getTAG() != BoolTag.BROKEN) {
 				final List<BoolEdge> vEdges = new ArrayList<BoolEdge>(vertex
 						.getEdges());
 
 				for (final BoolEdge edge : vEdges) {
 					if (!edge.isUsed()) {
 						vertex.removeEdge(edge);
 					}
 				}
 				if (vertex.getEdges().size() == 0) {
 					vertex.setTAG(BoolTag.BROKEN);
 				}
 			}
 		}
 
 		// clean_nonmanifold( m_mesh );
 	}
 
 	/**
 	 * Simplifies a mesh, merging its faces.
 	 */
 	boolean mergeFaces() {
 		final List<BoolVertex> mergeVertices = new ArrayList<BoolVertex>();
 		final List<BoolVertex> vertices = mesh.getVertices();
 		final Iterator<BoolVertex> it = vertices.iterator();
 
 		// Advance to first mergeable vertex
 		for (int i = 0; it.hasNext() && i < firstVertex; i++) {
 			it.next();
 		}
 
 		// Add unbroken vertices to the list
 		while (it.hasNext()) {
 			final BoolVertex v = it.next();
 			if (v.getTAG() != BoolTag.BROKEN) {
 				mergeVertices.add(v);
 			}
 		}
 
 		// Merge faces with that vertices
 		return mergeFaces(mergeVertices);
 	}
 
 	private boolean mergeFaces(final List<BoolVertex> mergeVertices) {
 		// Check size > 0!
 		if (mergeVertices.size() == 0) {
 			return false;
 		}
 		boolean didMerge = false;
 
 		for (final BoolVertex vert : mergeVertices) {
 			final List<List<BoolFace>> facesByOriginalFace = new ArrayList<List<BoolFace>>();
 
 			if (vert.getTAG() != BoolTag.BROKEN) {
 				getFaces(facesByOriginalFace, vert);
 				switch (facesByOriginalFace.size()) {
 				case 0:
 					// vert has no unbroken faces (so it's a new BROKEN vertex)
 					freeVerts(vert);
 					vert.setTAG(BoolTag.BROKEN);
 					break;
 				case 2: {
 					final List<BoolFace> ff = facesByOriginalFace.get(0);
 					final List<BoolFace> fb = facesByOriginalFace.get(1);
					final BoolEdge[] eindexs = new BoolEdge[vert.getEdges()
							.size()];
 					int ecount = 0;
 
 					for (final BoolEdge edge : vert.getEdges()) {
 						final List<BoolFace> faces = edge.getFaces();
 
 						if (faces.size() == 2) {
 							final BoolFace f0 = faces.get(0);
 							final BoolFace f1 = faces.get(1);
 							if (f0.getOriginalFace() != f1.getOriginalFace()) {
 								eindexs[ecount++] = edge;
 							}
 						}
 					}
 					if (ecount == 2) {
 						final BoolEdge edge = eindexs[0];
 						BoolVertex N = edge.getVertex1();
 						if (N == vert) {
 							N = edge.getVertex2();
 						}
 
 						mergeVertex(ff, vert, N);
 						mergeVertex(fb, vert, N);
 						// now remove v and its edges
 						vert.setTAG(BoolTag.BROKEN);
 						for (final BoolEdge tedge : vert.getEdges()) {
 							tedge.setUsed(false);
 						}
 						didMerge = true;
 					}
 				}
 					break;
 				default:
 					break;
 				}
 			}
 		}
 		return didMerge;
 	}
 
 	/**
 	 * remove edges from vertices when the vertex is removed
 	 */
 	void freeVerts(final BoolVertex vert) {
 		final List<BoolEdge> edges = new ArrayList<BoolEdge>(vert.getEdges());
 		BoolVertex other;
 
 		for (final BoolEdge edge : edges) {
 			if (edge.getVertex1() != vert) {
 				other = edge.getVertex1();
 			} else {
 				other = edge.getVertex2();
 			}
 
 			other.removeEdge(edge);
 			vert.removeEdge(edge);
 		}
 	}
 
 	void mergeVertex(final List<BoolFace> faces, final BoolVertex v1,
 			final BoolVertex v2) {
 		for (final BoolFace face : faces) {
 			if (face.size() == 3) {
 				mergeVertex3(face, v1, v2);
 			} else {
 				mergeVertex4(face, v1, v2);
 			}
 			face.setTAG(BoolTag.BROKEN);
 		}
 	}
 
 	/*
 	 * Remove a face from the mesh and from each edges's face list
 	 */
 
 	static void deleteFace(final BoolMesh m, final BoolFace face) {
 		BoolVertex l2 = face.getVertex(0);
 
		for (int i = face.size() - 1; i > 0; i--) {
 			final List<BoolEdge> edges = l2.getEdges();
 			final BoolVertex l1 = face.getVertex(i);
 
 			for (final BoolEdge edge : edges) {
 				if ((edge.getVertex1() == l1 && edge.getVertex2() == l2)
 						|| (edge.getVertex1() == l2 && edge.getVertex2() == l1)) {
 					final List<BoolFace> efs = new ArrayList<BoolFace>(edge
 							.getFaces());
 					for (final BoolFace ef : efs) {
 						if (efs == face) {
 							edge.removeFace(ef);
 							break;
 						}
 					}
 					break;
 				}
 			}
 			l2 = l1;
 		}
 		face.setTAG(BoolTag.BROKEN);
 	}
 
 	void mergeVertex3(final BoolFace face, final BoolVertex v1,
 			final BoolVertex v2) {
 		final BoolVertex neigbours[] = face.getNeighbours(v1);
 		final BoolVertex next = neigbours[0], prev = neigbours[1];
 
 		// if new vertex is not already in the tri, make a new tri
 		if (prev != v2 && next != v2) {
 			mesh.addFace(new BoolFace(prev, v2, next, face.getPlane(), face
 					.getOriginalFace()));
 
 		}
 		deleteFace(mesh, face);
 	}
 
 	void mergeVertex4(final BoolFace face, final BoolVertex v1,
 			final BoolVertex v2) {
 		final BoolVertex neigbours[] = face.getNeighbours(v1);
 		final BoolVertex next = neigbours[0], prev = neigbours[1], opp = neigbours[2];
 
 		// if new vertex is already in the quad, replace quad with new tri
 		if (prev == v2 || next == v2) {
 			mesh.addFace(new BoolFace(prev, next, opp, face.getPlane(), face
 					.getOriginalFace()));
 		}
 		// otherwise make a new quad
 		else {
 			mesh.addFace(new BoolFace(prev, v2, next, face.getPlane(), face
 					.getOriginalFace()));
 			mesh.addFace(new BoolFace(next, v2, opp, face.getPlane(), face
 					.getOriginalFace()));
 		}
 		deleteFace(mesh, face);
 	}
 
 	/**
 	 * Creates a list of lists L1, L2, ... LN where LX = mesh faces with vertex
 	 * v that come from the same original face
 	 * 
 	 * @param facesByOriginalFace
 	 *            list of faces lists
 	 * @param v
 	 *            vertex index
 	 */
 	void getFaces(final List<List<BoolFace>> facesByOriginalFace,
 			final BoolVertex v) {
 		// Get edges with vertex v
 		final List<BoolEdge> edges = v.getEdges();
 
 		for (final BoolEdge edge : edges) {
 			// For each edge, add its no broken faces to the output list
 			final List<BoolFace> faces = edge.getFaces();
 
 			for (final BoolFace face : faces) {
 				if (face.getTAG() != BoolTag.BROKEN) {
 					boolean found = false;
 					// Search if we already have created a list for the
 					// faces that come from the same original face
 					for (final List<BoolFace> facesByOriginalFaceX : facesByOriginalFace) {
 						if (facesByOriginalFaceX.get(0).getOriginalFace() == face
 								.getOriginalFace()) {
 							// Search that the face has not been added to the
 							// list before
 							for (int i = 0; i < facesByOriginalFaceX.size(); i++) {
 								if (facesByOriginalFaceX.get(i) == face) {
 									found = true;
 									break;
 								}
 							}
 							if (!found) {
 								// Add the face to the list
 								if (face.getTAG() == BoolTag.OVERLAPPED) {
 									facesByOriginalFaceX.add(0, face);
 								} else {
 									facesByOriginalFaceX.add(face);
 								}
 								found = true;
 							}
 							break;
 						}
 					}
 					if (!found) {
 						// Create a new list and add the current face
 						final List<BoolFace> facesByOriginalFaceX = new ArrayList<BoolFace>();
 						facesByOriginalFaceX.add(face);
 						facesByOriginalFace.add(facesByOriginalFaceX);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates a list of lists L1, L2, ... LN where LX = mesh faces with vertex
 	 * v that come from the same original face and without any of the vertices
 	 * that appear before v in vertices
 	 * 
 	 * @param facesByOriginalFace
 	 *            list of faces lists
 	 * @param vertices
 	 *            vector with vertices indexs that contains v
 	 * @param v
 	 *            vertex index
 	 */
 	void getFaces(final List<List<BoolFace>> facesByOriginalFace,
 			final List<BoolVertex> vertices, final BoolVertex v) {
 		final List<BoolEdge> edges = v.getEdges();
 
 		for (final BoolEdge edge : edges) {
 			// Foreach edge, add its no broken faces to the output list
 			final List<BoolFace> faces = edge.getFaces();
 
 			for (final BoolFace face : faces) {
 				if (face.getTAG() != BoolTag.BROKEN) {
 					boolean found = false;
 
 					for (final BoolVertex vertex : vertices) {
 						if (face.containsVertex(vertex)) {
 							found = true;
 							break;
 						}
 					}
 
 					if (!found) {
 						// Search if we already have created a list with the
 						// faces that come from the same original face
 						for (final List<BoolFace> facesByOriginalFaceX : facesByOriginalFace) {
 							if (facesByOriginalFaceX.get(0).getOriginalFace() == face
 									.getOriginalFace()) {
 								// Search that the face has not been added to
 								// the list before
 								for (int i = 0; i < facesByOriginalFaceX.size(); i++) {
 									if (facesByOriginalFaceX.get(i) == face) {
 										found = true;
 										break;
 									}
 								}
 								if (!found) {
 									if (face.getTAG() == BoolTag.OVERLAPPED) {
 										facesByOriginalFaceX.add(0, face);
 									} else {
 										facesByOriginalFaceX.add(face);
 									}
 									found = true;
 								}
 							}
 						}
 						if (!found) {
 							final List<BoolFace> facesByOriginalFaceX = new ArrayList<BoolFace>();
 							facesByOriginalFaceX.add(face);
 							facesByOriginalFace.add(facesByOriginalFaceX);
 						}
 					}
 				}
 			}
 		}
 	}
 }
