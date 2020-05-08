 package cs5625.deferred.loop;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.*;
 
 import javax.vecmath.Point3f;
 
 import cs5625.deferred.datastruct.*;
 import cs5625.deferred.scenegraph.Mesh;
 import cs5625.deferred.scenegraph.Trimesh;
 
 /**
  * LoopSubdiv.java
  * 
  * Perform the subdivision in this class/package 
  * 
  * Written for Cornell CS 5625 (Interactive Computer Graphics).
  * Copyright (c) 2012, Computer Science Department, Cornell University.
  * 
  * @author Rohit Garg (rg534)
  * @date 2012-03-23
  */
 
 public class LoopSubdiv {
 	
 	private Mesh mMesh;
 	
 	public LoopSubdiv(EdgeDS edgeDS) {
 		// TODO PA5: Fill in this function to perform loop subdivision
 		
 		Trimesh oldMesh = (Trimesh) edgeDS.getMesh();
 		Set<Integer> vertexSet = edgeDS.getVertexIDs();
 		Set<Integer> edgeSet = edgeDS.getEdgeIDs();
 		Set<Integer> polygonSet = edgeDS.getPolygonIDs();
 		
 		// initiate new buffers for vertices (3 floats), normals (3 floats), texCoords (2 floats), crease edges / EdgeData (2 ints), triangles (3 ints)
 		int oldVertexCount = vertexSet.size();
 		int oldEdgeCount = edgeSet.size();
 		int oldPolygonCount = polygonSet.size();
 		
 		int newVertexCount = oldVertexCount + oldEdgeCount;
 		int newPolygonCount = oldPolygonCount * 4;
 		int newCreaseEdgesCount = oldMesh.getEdgeData().capacity(); // implicitly * 2 / 2
 		
 		FloatBuffer newVertices = FloatBuffer.allocate(newVertexCount*3);
 		FloatBuffer newNormals = FloatBuffer.allocate(newVertexCount*3); // do not need to touch
 		FloatBuffer newTexCoords = FloatBuffer.allocate(newVertexCount*2); // do not need to touch
 		
 		IntBuffer newCreaseEdges = IntBuffer.allocate(newCreaseEdgesCount*2);
 		IntBuffer newTriangles = IntBuffer.allocate(newPolygonCount*3);
 		
 		// looping over the edges to create new vertices
 		Iterator<Integer> edgeIterator = edgeSet.iterator();
 		
 		// new vertex buffer index needs explicit tracking, all other buffers can be tracked by their internal position
 		int newVertexIndex = 0;
 		
 		while (edgeIterator.hasNext()) {
 			int edgeID = edgeIterator.next();
 			EdgeData edge = edgeDS.getEdgeData(edgeID);
 			VertexData vertex0 = edgeDS.getVertexData(edge.getVertex0());
 			VertexData vertex1 = edgeDS.getVertexData(edge.getVertex1());
 			
 			Point3f newVertexPosition;
 			if (edgeDS.isCreaseEdge(edgeID)) {
 				// crease edge
 				newVertexPosition = newVertexCreaseBoundaryCase(vertex0, vertex1);
 			} else {
 				// get position of top vertex
 				ArrayList<Integer> topEdgesIDs = edgeDS.getOtherEdgesOfLeftFace(edgeID);
 				EdgeData firstEdge = edgeDS.getEdgeData(topEdgesIDs.get(0));
 				int topVertexIndex = (firstEdge.getVertex0() == edge.getVertex0()) ? firstEdge.getVertex1() : firstEdge.getVertex0();
 				VertexData topVertex = edgeDS.getVertexData(topVertexIndex);
 					
 				// get position of bottom vertex
 				ArrayList<Integer> bottomEdgesIDs = edgeDS.getOtherEdgesOfRightFace(edgeID);
 				if (bottomEdgesIDs.size() == 0) {
 					// boundary edge
 					newVertexPosition = newVertexCreaseBoundaryCase(vertex0, vertex1);
 				} else {
 					EdgeData firstEdgeBottom = edgeDS.getEdgeData(bottomEdgesIDs.get(0));
 					int bottomVertexIndex = (firstEdgeBottom.getVertex0() == edge.getVertex0()) ? firstEdgeBottom.getVertex1() : firstEdgeBottom.getVertex0();
 					VertexData bottomVertex = edgeDS.getVertexData(bottomVertexIndex);
 					newVertexPosition = newVertexRegularCase(vertex0, vertex1, topVertex, bottomVertex);
 				}
 			}
 			
 			// store the new vertex index for old index calculations
 			edge.setVertexIDNew(newVertexIndex);
 			
 			// store the new vertex position into newVertices
 			float[] newPositionArray = new float[3];
 			newVertexPosition.get(newPositionArray);
 			newVertices.put(newPositionArray);
 			newVertexIndex++;
 		}
 		
 		// loop over the old vertices to edit their position
 		Iterator<Integer> vertexIterator = vertexSet.iterator();
 		
 		while (vertexIterator.hasNext()) {
 			int vertexID = vertexIterator.next();
 			VertexData vertex = edgeDS.getVertexData(vertexID);
 			
 			// get surrounding new vertices
 			ArrayList<Integer> connectedEdges = vertex.getConnectedEdges();
 			int[] connectedVertexIDs = new int[connectedEdges.size()];
 			ArrayList<Integer> creaseEdgeIDs = new ArrayList<Integer>();
 			for (int i = 0; i < connectedEdges.size(); i++) {
 				int edgeID = connectedEdges.get(i);
 				connectedVertexIDs[i] = edgeDS.getEdgeData(edgeID).getNewVertexID();
 				if (edgeDS.isCreaseEdge(edgeID)) {
 					creaseEdgeIDs.add(edgeID);
 				}
 			}
 			
 			
 			
 			// calculate new position
 			Point3f newVertexPosition = oldVertexPositionShift(vertexID, connectedVertexIDs, newVertices, creaseEdgeIDs, edgeDS);
 			
 			// store the new vertex index for forming polygons
 			vertex.setNewVertexID(newVertexIndex);
 			
 			// store the new position for the current vertex into newVertices
 			float[] newPositionArray = new float[3];
 			newVertexPosition.get(newPositionArray);
 			newVertices.put(newPositionArray);
 			newVertexIndex++;
 		}
 		
 		// loop over edges again to store crease edges
 		edgeIterator = edgeSet.iterator();
 		while (edgeIterator.hasNext()) {
 			int edgeID = edgeIterator.next();
 			if (edgeDS.isCreaseEdge(edgeID)) {
 				EdgeData edge = edgeDS.getEdgeData(edgeID);
 				VertexData v0 = edgeDS.getVertexData(edge.getVertex0());
 				VertexData v1 = edgeDS.getVertexData(edge.getVertex1());
 				int v0New = v0.getNewVertexID();
 				int v1New = v1.getNewVertexID();
 				int newVertex = edge.getNewVertexID();
 				int[] creaseEdges = new int[] {v0New, newVertex, newVertex, v1New};
 				newCreaseEdges.put(creaseEdges);
 			}
 		}
 		
 		// loop over polygons to form 4 new triangles
 		Iterator<Integer> polygonIterator = polygonSet.iterator();
 		while (polygonIterator.hasNext()) {
 			int polygonID = polygonIterator.next();
 			PolygonData polygon = edgeDS.getPolygonData(polygonID);
 			ArrayList<Integer> edgeIDs = polygon.getAllEdges();
 			EdgeData e0 = edgeDS.getEdgeData(edgeIDs.get(0));
 			EdgeData e1 = edgeDS.getEdgeData(edgeIDs.get(1));
 			EdgeData e2 = edgeDS.getEdgeData(edgeIDs.get(2));
 			
 			// v0 is common point between e0 and e2, v1 is common point between e0 and e1, v2: e1 and e2
 			VertexData v0, v1, v2;
 			if (e0.getVertex0() == e2.getVertex0() || e0.getVertex0() == e2.getVertex1()) {
 				v0 = edgeDS.getVertexData(e0.getVertex0());
 			} else {
 				v0 = edgeDS.getVertexData(e0.getVertex1());
 			}
 			
 			if (e0.getVertex0() == e1.getVertex0() || e0.getVertex0() == e1.getVertex1()) {
 				v1 = edgeDS.getVertexData(e0.getVertex0());
 			} else {
 				v1 = edgeDS.getVertexData(e0.getVertex1());
 			}
 			
 			if (e1.getVertex0() == e2.getVertex0() || e1.getVertex0() == e2.getVertex1()) {
 				v2 = edgeDS.getVertexData(e1.getVertex0());
 			} else {
 				v2 = edgeDS.getVertexData(e1.getVertex1());
 			}
 
 			int[] tris = new int[] {v0.getNewVertexID(), e0.getNewVertexID(), e2.getNewVertexID(), // tri0: v0, e0, e2
 									v1.getNewVertexID(), e1.getNewVertexID(), e0.getNewVertexID(), // tri1: v1, e1, e0
 									v2.getNewVertexID(), e2.getNewVertexID(), e1.getNewVertexID(), // tri2: v2, e2, e1
 									e0.getNewVertexID(), e1.getNewVertexID(), e2.getNewVertexID()}; //tri3: e0, e1, e1
 			
 			newTriangles.put(tris);
 			
 		}
 		
 		Mesh newMesh = oldMesh.clone();
 		
 		// resetting the positions
 		newVertices.position(0);
 		newCreaseEdges.position(0);
 		newTriangles.position(0);
 		
 		newMesh.setVertexData(newVertices);
 		newMesh.setNormalData(newNormals);
 		newMesh.setTexCoordData(newTexCoords);
 		newMesh.setEdgeData(newCreaseEdges);
 		newMesh.setPolygonData(newTriangles);
 		this.mMesh = newMesh;
 	}
 	
 	private Point3f oldVertexPositionShift(int vertexID, int[] connectedVertexIDs, 
 			FloatBuffer newVertices, ArrayList<Integer> creaseEdgeIDs, EdgeDS edgeDS) {
 		VertexData vertex = edgeDS.getVertexData(vertexID);
 		
 		// more than 2 crease edges, don't move it
 		if (creaseEdgeIDs.size() > 2) {
 			return new Point3f(vertex.mData.getPosition());
 		}
 		
 		
 		float beta = 0.0f;
 		
 		// if exactly 2 creases, use beta = 1/8
 		if (creaseEdgeIDs.size() == 2) {
 			beta = 1f/8f;
 			EdgeData e0 = edgeDS.getEdgeData(creaseEdgeIDs.get(0));
 			EdgeData e1 = edgeDS.getEdgeData(creaseEdgeIDs.get(1));
 			int v0ID = e0.getNewVertexID();
 			int v1ID = e1.getNewVertexID();
 			
 			Point3f v0pos = new Point3f(newVertices.get(v0ID*3+0), newVertices.get(v0ID*3+1), newVertices.get(v0ID*3+2));
 			Point3f v1pos = new Point3f(newVertices.get(v1ID*3+0), newVertices.get(v1ID*3+1), newVertices.get(v1ID*3+2));
 			Point3f vOriginalPos = new Point3f(vertex.mData.getPosition());
 			
 			v0pos.scale(beta);
 			v1pos.scale(beta);
 			vOriginalPos.scale(6f/8f);
 			
 			Point3f newPosition = new Point3f();
 			newPosition.add(v0pos, v1pos);
 			newPosition.add(vOriginalPos);
 			
 			return newPosition;
 		}
 		
 		
 		// interior case
 		int n = connectedVertexIDs.length;
 		
 //		if (n == 3) {
 //			beta = 3f/16f;
 //		} else {
 //			beta = 3f/(8f*(float)n);
 //		}
 		
 		beta = (float) ((1f/n) * (5f/8f - Math.pow(3f/8f + (1f/4f)*Math.cos(2f*Math.PI/n), 2)));
 		
 		Point3f vOriginalPos = new Point3f(vertex.mData.getPosition());
 		Point3f newPosition = new Point3f();
 		
 		// scale original by 1-n*beta
 		vOriginalPos.scale(1f - (float)n * beta);
 		newPosition.set(vOriginalPos);
 		
 		// for each connected vertex, scale by beta
 		for (int i = 0; i < connectedVertexIDs.length; i++) {
 			int viID = connectedVertexIDs[i];
 			Point3f viPos = new Point3f(newVertices.get(viID*3+0), newVertices.get(viID*3+1), newVertices.get(viID*3+2));
 			viPos.scale(beta);
 			newPosition.add(viPos);
 		}
 		return newPosition;
 	}
 
 	private Point3f newVertexRegularCase(VertexData vertex0,
 			VertexData vertex1, VertexData topVertex, VertexData bottomVertex) {
 		Point3f v0pos = new Point3f(vertex0.mData.getPosition());
 		Point3f v1pos = new Point3f(vertex1.mData.getPosition());
 		Point3f vtpos = new Point3f(topVertex.mData.getPosition());
 		Point3f vbpos = new Point3f(bottomVertex.mData.getPosition());
 		
 		v0pos.scale(3f/8f);
 		v1pos.scale(3f/8f);
 		vtpos.scale(1f/8f);
 		vbpos.scale(1f/8f);
 		
 		Point3f newPosition = new Point3f();
 		newPosition.add(v0pos, v1pos);
 		newPosition.add(vtpos);
 		newPosition.add(vbpos);
 		return newPosition;
 	}
 
 	private Point3f newVertexCreaseBoundaryCase(VertexData vertex0,
 			VertexData vertex1) {
 		Point3f v0pos = new Point3f(vertex0.mData.getPosition());
 		Point3f v1pos = new Point3f(vertex1.mData.getPosition());
 		v0pos.scale(0.5f);
 		v1pos.scale(0.5f);
 		Point3f newPosition = new Point3f();
 		newPosition.add(v0pos, v1pos);
 		return newPosition;
 	}
 
 	public Mesh getNewMesh()
 	{
 		return this.mMesh;
 	}
 	
 }
