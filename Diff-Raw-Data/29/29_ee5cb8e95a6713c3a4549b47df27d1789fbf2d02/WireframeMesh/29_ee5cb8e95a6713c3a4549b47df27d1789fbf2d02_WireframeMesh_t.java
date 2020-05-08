 package meshes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import javax.vecmath.Point2f;
 import javax.vecmath.Point3f;
 
 /**
  * A Wireframe Mesh represents a mesh as a list of vertices and a list of faces.
  * Very lightweight representation.
  * @author bertholet
  *
  */
 public class WireframeMesh {
 
 	public ArrayList<Point3f> vertices = new ArrayList<>();
 	public ArrayList<int[]> faces = new ArrayList<>();
 	public ArrayList<Point2f> texCoords = new ArrayList<>();
 	
 	public WireframeMesh(HalfEdgeStructure hs) {
 		hs.enumerateVertices();
 		
 		for(Vertex v : hs.getVertices()){
 			vertices.add(new Point3f(v.getPos()));
 			texCoords.add(new Point2f(v.tex));
 		}
 		for(Face f : hs.getFaces()){
 			Iterator<Vertex> iter = f.iteratorFV();
 			int[] fc = { iter.next().index,
 					iter.next().index,
 					iter.next().index };
 			faces.add(fc);
 		}
 	
 	}
 	
 	public WireframeMesh(){
 	}
 
 	private int[] currentFace = new int[3];
 	private int currentFaceIdx = 0;
	public boolean hasTexture;
 	
 	/**
 	 * Zomfg this is way to complicated for what I'm trying to do... I could just go on gl-level...
 	 * @param idx
 	 */
 	public void addIndex(Integer idx) {
 		currentFace[currentFaceIdx++] = idx;
 		if (currentFaceIdx == 3) {
 			if (!isDegenerated(currentFace))
 				faces.add(currentFace);
 			//else System.out.println("prevented degenerated Face: " + Arrays.toString(currentFace)); //verbosity for checking implementation
 			currentFace = new int[3];
 			currentFaceIdx = 0;
 		}
 	}
 	private boolean isDegenerated(int[] face) {
 		return face[0] == face[2] || face[0] == face[1] || face[1] == face[2];
 	}
 }
