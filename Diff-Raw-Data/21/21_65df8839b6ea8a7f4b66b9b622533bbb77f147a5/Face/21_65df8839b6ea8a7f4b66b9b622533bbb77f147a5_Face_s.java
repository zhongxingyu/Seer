 package meshes;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Point4f;
 import javax.vecmath.Vector3f;
 
 import myutils.MyMath;
 /**
  * Implementation of a face for the {@link HalfEdgeStructure}
  *
  */
 public class Face extends HEElement {
 
 	//an adjacent edge, which is positively oriented with respect to the face.
 	private HalfEdge anEdge;
 	
 	public Face(){
 		anEdge = null;
 	}
 
 	public void setHalfEdge(HalfEdge he) {
 		this.anEdge = he;
 	}
 
 	public HalfEdge getHalfEdge() {
 		return anEdge;
 	}
 	
 	/**
 	 * Only works for triangles as faces
 	 */
 	public float getArea() {
 		Vector3f cross = new Vector3f();
 		cross.cross(anEdge.asVector(), anEdge.next.asVector());
 		return cross.length()/2;
 	}
 	
 	public Matrix4f getQuadricErrorMatrix() {
 		Matrix4f ppT = new Matrix4f();
		Point4f p = new Point4f(anEdge.start().getPos());
		p.w = - normal().dot(new Vector3f(anEdge.start().getPos()));
 		ppT.m00 = p.x*p.x; ppT.m01 = p.x*p.y; ppT.m02 = p.x*p.z; ppT.m03 = p.x*p.w;
 		ppT.m10 = p.y*p.x; ppT.m11 = p.y*p.y; ppT.m12 = p.y*p.z; ppT.m13 = p.y*p.w;
 		ppT.m20 = p.z*p.x; ppT.m21 = p.z*p.y; ppT.m22 = p.z*p.z; ppT.m23 = p.z*p.w;
 		ppT.m30 = p.w*p.x; ppT.m31 = p.w*p.y; ppT.m32 = p.w*p.z; ppT.m33 = p.w*p.w;
 		return ppT;
 	}
 	
 	/**
 	 * @throws NoSuchElementException if v is not a vertex of this face.
 	 * @param v, a Vertex, as in sketch
 	 * @return
 	 */
 	public float getMixedVoronoiCellArea(Vertex p) {
 		Iterator<HalfEdge> iter = new IteratorFE(anEdge);
 		HalfEdge pointingToP = iter.next();
 		while(pointingToP.incident_v != p){
 			pointingToP = iter.next();
 		}
 		float angleAtP = pointingToP.getIncidentAngle();
 		float voronoiCellArea;
 		if (!isObtuse()) { // non-obtuse
 			HalfEdge PR = pointingToP.getOpposite();
 			HalfEdge PQ = pointingToP.getNext();
 			float areaPR = PR.lengthSquared()*MyMath.cot(PQ.getIncidentAngle());
 			float areaPQ = PQ.lengthSquared() * MyMath.cot(PQ.getNext().getIncidentAngle());
 			voronoiCellArea = 1/8f * ( areaPR + areaPQ ); 
 		} else if (angleAtP > Math.PI/2) { // obtuse at P
 			voronoiCellArea = getArea()/2;
 		} else { // else
 			voronoiCellArea = getArea()/4;
 		}
 		return voronoiCellArea;
 	}
 	
 	/**
 	 * An obtuse angle is between 90 and 180 degree (aka pi/2 an pi radian).
 	 * @return true, if there is at least one obtuse (stumpf) angle in this face
 	 */
 	public boolean isObtuse() {
 		for(Iterator<HalfEdge> iter = iteratorFE(); iter.hasNext();) {
 			float angle = iter.next().getIncidentAngle();
 			if (angle > Math.PI/2 && angle < Math.PI)
 				return true;
 		}
 		return false;
 		
 	}
 	/**
 	 * Iterate over the vertices of the face.
 	 * @return
 	 */
 	public Iterator<Vertex> iteratorFV(){
 		return new IteratorFV(anEdge);
 	}
 	
 	/**
 	 * Iterate over the adjacent edges
 	 * @return
 	 */
 	public Iterator<HalfEdge> iteratorFE(){
 		return new IteratorFE(anEdge);
 	}
 	
 	public String toString(){
 		if(anEdge == null){
 			return "f: not initialized";
 		}
 		String s = "f: [";
 		Iterator<Vertex> it = this.iteratorFV();
 		while(it.hasNext()){
 			s += it.next().toString() + ",";
 		}
 		s = s.substring(0, s.length()-1);
 		s+= "]";
 		return s;
 		
 	}
 	
 	
 	abstract class IteratorF {
 		HalfEdge start, current;
 
 		public void remove() {
 			//we don't support removing through the iterator.
 			throw new UnsupportedOperationException();
 		}
 
 		/**
 		 * return the face this iterator iterates around
 		 * @return
 		 */
 		public Face face() {
 			return start.incident_f;
 		}
 		
 		public boolean hasNext() {
 			return current == null || current.next != start;
 		}
 	}
 	
 	/**
 	 * Iterator to iterate over the vertices on a face
 	 * @author Alf
 	 *
 	 */
 	public final class IteratorFV extends IteratorF implements Iterator<Vertex>  {	
 
 		public IteratorFV(HalfEdge anEdge) {
 			start = anEdge;
 			current = null;
 		}
 
 		@Override
 		public Vertex next() {
 			//make sure eternam iteration is impossible
 			if(!hasNext()){
 				throw new NoSuchElementException();
 			}
 
 			//update what edge was returned last
 			current = (current == null?
 						start:
 						current.next);
 			return current.incident_v;
 		}
 	}
 
 	public final class IteratorFE extends IteratorF implements Iterator<HalfEdge> {
 		
 		public IteratorFE(HalfEdge anEdge) {
 			start = anEdge;
 			current = null;
 		}
 
 		@Override
 		public HalfEdge next() {
 			//make sure eternam iteration is impossible
 			if(!hasNext()){
 				throw new NoSuchElementException();
 			}
 
 			//update what edge was returned last
 			current = (current == null?
 						start:
 						current.next);
 			return current;
 		}
 	}
 
 	public Vector3f normal() {
 		Iterator<HalfEdge> iter = iteratorFE();
 		Vector3f n = new Vector3f();
 		n.cross(iter.next().asVector(), iter.next().asVector());
 		n.normalize();
 		return n;
 	}
 }
