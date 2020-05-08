 package meshes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 
 import javax.vecmath.Point2f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Tuple3f;
 import javax.vecmath.Vector3f;
 
 import meshes.exception.DanglingTriangleException;
 import meshes.exception.MeshNotOrientedException;
 
 
 /**
  * An implementation of a half-edge Structure.
  * This particular implementation can only handle orientable 
  * Manifold meshes with or without borders. This structure explicitely 
  * stores a list of {@link HalfEdge}s, {@link Face}s and {@link Vertex}s.
  * 
  * <p>
  * Every half-edge has an opposite, next and prev, but on boundary edges 
  * one edge of the half-edge pair will have a null face.
  * </p>
  * <p>
  * Initialize the HalfEdgeStructure with the {@link #init(WireframeMesh)} method
  * </p>
  * 
  * @author bertholet
  *
  */
 
 public class HalfEdgeStructure {
 	
 	private ArrayList<HalfEdge> edges;
 	ArrayList<Face> faces;
 	ArrayList<Vertex> vertices;
 	public ArrayList<Point2f> texCoords;
 	
 	public HalfEdgeStructure(){
 		faces = new ArrayList<Face>();
 		edges = new ArrayList<HalfEdge>();
 		vertices = new ArrayList<Vertex>();
 	}
 	
 	public <T extends Tuple3f> void setVerticesTo(ArrayList<T> pos) {
 		for(Vertex v : getVertices()){
 			v.getPos().set(pos.get(v.index));
 		}
 	}
 		
 	public HalfEdgeStructure(HalfEdgeStructure hs) {
 		faces = new ArrayList<Face>();
 		edges = new ArrayList<HalfEdge>();
 		vertices = new ArrayList<Vertex>();
 		this.texCoords = new ArrayList<>();
 		for(Point2f p: hs.texCoords) {
 			texCoords.add(new Point2f(p));
 		}
 		HashMap<Face, Face> f2f = new HashMap<>();
 		for(Face f: hs.getFaces()){
 			Face newFace = new Face();
 			faces.add(newFace);
 			f2f.put(f, newFace);
 		}
 		HashMap<Vertex, Vertex> v2v = new HashMap<>();
 		for(Vertex v: hs.getVertices()){
 			Vertex newV = new Vertex(new Point3f(v.getPos()), new Point2f(v.tex));
 			vertices.add(newV);
 			v2v.put(v, newV);
 		}
 		HashMap<HalfEdge, HalfEdge> e2e = new HashMap<>();
 		for(HalfEdge e: hs.getHalfEdges()){
 			
 			HalfEdge newE = new HalfEdge(f2f.get(e.getFace()), v2v.get(e.end()));
 			e2e.put(e, newE);
 			edges.add(newE);
 		}
 		
 		//interlink everything: faces->edge
 		for(Face f : hs.getFaces()){
 			Face newFace = f2f.get(f);
 			newFace.setHalfEdge(e2e.get(f.getHalfEdge()));
 		}
 		
 		//vertex-> edge
 		for(Vertex v : hs.getVertices()){
 			Vertex newV = v2v.get(v);
 			newV.setHalfEdge(e2e.get(v.getHalfEdge()));
 		}
 		
 		//edge -> edge
 		for(HalfEdge e: hs.getHalfEdges()){
 			HalfEdge newHE = e2e.get(e);
 			newHE.setNext(e2e.get(e.next));
 			newHE.setPrev(e2e.get(e.prev));
 			newHE.setOpposite(e2e.get(e.opposite));
 		}
 		
 		this.enumerateVertices();
 	}
 
 
 	public ArrayList<Vertex> getVertices() {
 		return vertices;
 	}
 	
 	public ArrayList<HalfEdge> getHalfEdges() {
 		return edges;
 	}
 	
 	public ArrayList<Face> getFaces() {
 		return faces;
 	}
 	
 	/**
 	 * Get an Iterator over all Vertices
 	 * @return
 	 */
 	public Iterator<Vertex> iteratorV(){
 		return vertices.iterator();
 	}
 	
 	/**
 	 * Get an Iterator over all Edges
 	 * @return
 	 */
 	public Iterator<HalfEdge> iteratorE(){
 		return edges.iterator();
 	}
 	
 	/**
 	 * Get an Iterator over all Faces
 	 * @return
 	 */
 	public Iterator<Face> iteratorF(){
 		return faces.iterator();
 	}
 	
 	
 	/**
 	 * Create a Halfedge Structure from a wireframe mesh described by the vertex Positions verts and fcs; this
 	 * method throws a MeshNotOrientedException if the input mesh is not oriented consistently.
 	 * 
 	 * Runs in O(n*m), where n is the number of faces and m is the average number of vertices per face
 	 * @param verts
 	 * @param fcs
 	 * @param v_per_face
 	 * @throws MeshNotOrientedException
 	 * @throws DanglingTriangleException when dangling Triangles are detected
 	 */
 	public void init(WireframeMesh m) throws MeshNotOrientedException, DanglingTriangleException{
 		
 		//add all vertices
 		Iterator<Point2f> iter = m.texCoords.iterator();
 		for(Point3f v: m.vertices){
			vertices.add(new Vertex(v, iter.next()));
 		}
 		this.enumerateVertices();
 		this.texCoords = m.texCoords;
 
 		// local vars... 
 		int j;
 		Point2i key;
 		HalfEdge he = null; 
 		HalfEdge first_he = null;
 		HalfEdge prev_he = null;
 		HalfEdge next_he;
 		
 		HashMap<Point2i, HalfEdge> edgeTable = new HashMap<Point2i, HalfEdge>(m.faces.size() + m.vertices.size());
 		
 		// for every face:
 		int[] fc;
 		for(int i = 0; i < m.faces.size(); i++){
 			
 			//add the face
 			faces.add(new Face());
 			
 			fc = m.faces.get(i);
 			//add all adjascent edges
 			for(j = 0; j < fc.length; j++){
 				
 				key = new Point2i(fc[j], fc[(j+1)%fc.length]);
 				
 				//check well-orientedness:
 				//every edge is found once if the mesh is oriented consistently
 				if(edgeTable.containsKey(key)){
 					throw new MeshNotOrientedException();
 				}
 				
 				//create a new half edge
 				he = new HalfEdge(faces.get(faces.size() -1), //the face that just was added
 						vertices.get(fc[(j+1)%fc.length])); //the vertex the edge is pointing to
 				
 							
 				//link between previous halfedge and the new halfedge
 				if(j == 0){
 					first_he = he;
 				}
 				else{
 					prev_he.setNext(he);
 					he.setPrev(prev_he);
 				}
 				//store the he for opposite he linkage
 				edgeTable.put(key, he);
 				
 				//add the edge to vertex its outgoing, 
 				//every vertex has to know an arbitrary outgoing halfedge.
 				vertices.get(fc[j]).setHalfEdge(he);
 				
 				//interlink the halfedge with its opposite halfedge if it exists.
 				key = new Point2i(key.y, key.x);
 				if(edgeTable.containsKey(key)){
 					he.setOpposite(edgeTable.get(key));
 					edgeTable.get(key).setOpposite(he);
 				}
 				//update prev_he
 				prev_he = he;
 			}
 			
 			
 			//close the circle
 			he.setNext(first_he);
 			first_he.setPrev(he);
 			//add a he to the face
 			faces.get(faces.size() -1).setHalfEdge(first_he);
 		}
 		
 		edges.addAll(edgeTable.values());
 
 		//finally: treat boundaries
 		LinkedHashMap<Vertex,HalfEdge> boundaryEdges = new LinkedHashMap<Vertex, HalfEdge>();
 		boolean dangling = false;
 		
 		//generate and interlink the opposite boundary halfEdges
 		for(HalfEdge e: edges){
 			if(e.opposite == null){
 				he = new HalfEdge(null, e.getPrev().end());
 				
 				//throw an error later because the datastructure will be disfunctional on boundaries:
 				// multiple boundary edges start at this vertex, so
 				if(boundaryEdges.containsKey(e.end())){
 					dangling = true;
 				}
 				//register them under the vertex 'he.getStart()'
 				boundaryEdges.put(e.end(), he);
 				he.setOpposite(e);
 				e.setOpposite(he);
 				
 			}
 		}
 		
 		// interlink previous and next boundary Edges
 		for(HalfEdge e: boundaryEdges.values()){
 			next_he = boundaryEdges.get(e.end());
 			e.setNext(next_he);
 			next_he.setPrev(e);
 		}
 		
 		//add all the boundary Edges
 		edges.addAll(boundaryEdges.values());
 		
 		if(dangling){
 			throw new DanglingTriangleException();
 		}
 		
 	}
 
 
 /**
  * Assign consecutive values 0...vertices.size()-1 to the Vertex.index fields.
  */
 	public void enumerateVertices() {
 		int idx =0;
 		for(Vertex v: vertices){
 			v.index= idx++;
 		}
 	}
 
 	public float getVolume() {
 		float sum = 0;
 		for (Face f: getFaces()) {
 			Iterator<Vertex> iter = f.iteratorFV();
 			Vector3f p1 = new Vector3f(iter.next().getPos());
 			Vector3f p2 = new Vector3f(iter.next().getPos());
 			Vector3f p3 = new Vector3f(iter.next().getPos());
 			Vector3f cross = new Vector3f();
 			cross.cross(p2, p3);
 			sum += p1.dot(cross);
 		}
 		return sum/6;
 	}
 	
 
 	public float getSurfaceArea() {
 		float sum = 0;
 		for (Face f: getFaces()) {
 			sum += f.getArea();
 		}
 		return sum;
 	}
 	
 	public ArrayList<Point3f> getVerticesAsPointArray() {
 		ArrayList<Point3f> t = new ArrayList<Point3f>();
 		for(Vertex v : getVertices()){
 			t.add(new Point3f(v.getPos()));
 		}
 		return t;
 	}
 }
