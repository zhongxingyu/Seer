 package assignment5;
 
 import glWrapper.GLHalfedgeStructure;
 import glWrapper.GLWireframeMesh;
 
 import java.util.ArrayList;
 
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import com.jogamp.opengl.math.FloatUtil;
 
 import meshes.HalfEdgeStructure;
 import meshes.Vertex;
 import meshes.WireframeMesh;
 import meshes.reader.ObjReader;
 import openGL.MyDisplay;
 import openGL.objects.Transformation;
 
 
 /**
  * Some convenience methods for the visualization of
  * epsilon-isosurfaces of quadratic forms.
  * @author Alf
  *
  */
 public class Assignment5_vis {
 
	public static final float epsilon = 0.04f;
 	
 	public static void main(String[] args) throws Exception{
 		WireframeMesh wf = ObjReader.read("objs/bunny_ear.obj", true);
 		HalfEdgeStructure hs = new HalfEdgeStructure();
 		hs.init(wf);
 		GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
 		glHs.configurePreferredShader("shaders/trimesh_flat.vert",
 				"shaders/trimesh_flat.frag", 
 				"shaders/trimesh_flat.geom");
 		QSlim qs = new QSlim(hs);
 		MyDisplay d = new MyDisplay();
 		d.addToDisplay(glHs);
 		for (Vertex v: qs.hm.keySet()) {
 			Matrix3f m = new Matrix3f();
 			qs.hm.get(v).getRotationScale(m);
 			float[] eVals = eigenValues(m);
 			Vector3f[] eVecs = new Vector3f[3];
 			for (int i = 0; i <3; i++) {
 				float eig = eVals[i];
 				eVals[i] = epsilon/FloatUtil.sqrt(eig);
 				eVecs[i] = eigenVector(m, eig);
 				
 			}
 			d.addToDisplay(ellipsoid(v.getPos(), eVecs[0], eVals[0], eVecs[1], eVals[1], eVecs[2], eVals[2]));
 		}
 	}
 
 	
 	
 	/**
 	 * This method is a hack to compute eigenvectors that will often work but will fail
 	 * in various cases. it can't handle if the eigenspace is not 1 dimensional, or if
 	 * zero's pop up during the computations. But it will do for the simple bunny_ear.obj 
 	 * visualizations.
 	 * @param m
 	 * @param eig
 	 * @return
 	 */
 	private static Vector3f eigenVector(Matrix3f m, float eig) {
 		Matrix3f mat = new Matrix3f(m);
 		mat.m00 -= eig; mat.m11 -= eig; mat.m22 -= eig;
 		
 		
 		if(mat.m00 != 0){
 			if(mat.m10 != 0){
 				float c = mat.m10/mat.m00;
 				mat.m10 = 0;
 				mat.m11 -= c*mat.m01;
 				mat.m12 -= c* mat.m02;
 			}
 			if(mat.m20 != 0){
 				float c = mat.m20/mat.m00;
 				mat.m20 = 0;
 				mat.m21 -= c*mat.m01;
 				mat.m22 -= c* mat.m02;
 			}
 		}
 		if(mat.m11 != 0){
 			if(mat.m21 != 0){
 				float c = mat.m21/mat.m11;
 				mat.m21 = 0; 
 				mat.m22 -= c*mat.m12;
 			}
 			if(mat.m01 != 0){
 				float c = mat.m01/mat.m11;
 				mat.m01 = 0; 
 				mat.m02 -= c*mat.m12;
 			}
 		}
 		
 		Vector3f ev = new Vector3f();
 		ev.z = 1;
 		
 		if(Math.abs(mat.m02) > 1e-4){
 			ev.x = - ev.z *mat.m02/mat.m00;
 		}
 
 		
 		if(Math.abs(mat.m12) > 1e-4){
 			ev.y = -ev.z * mat.m12/mat.m11;
 		}
 
 		ev.normalize();
 		return ev;
 	}
 
 	
 	/**
 	 * Constructs an ellipsoid with the given axes and the
 	 * given radia.
 	 * @param center
 	 * @param v0
 	 * @param l0
 	 * @param v1
 	 * @param l1
 	 * @param v2
 	 * @param l2
 	 * @return
 	 */
 	private static GLWireframeMesh ellipsoid(Point3f center, 
 			Vector3f v0, float l0, 
 			Vector3f v1, float l1, 
 			Vector3f v2, float l2) {
 		
 		int numPhi = 10;
 		ArrayList<Point3f> sphr = new ArrayList<>();
 		float Pi = (float) Math.PI;
 		int numPsi = 10;
 		for(float psi = -Pi/2; psi < Pi/2 + Pi/(2*numPsi) ; psi+=Pi/numPsi){
 			for(float phi = 0; phi < 2*Pi -Pi/(2*numPhi) ; phi+=Pi/numPhi){
 				
 				Point3f p = new Point3f((float) (Math.cos(phi)* Math.cos(psi)),
 						(float) (Math.sin(phi)* Math.cos(psi)),
 						(float) (Math.sin(psi)));
 				
 				sphr.add(new Point3f(
 						center.x + l0 *p.x * v0.x + l1*p.y * v1.x + l2*p.z * v2.x,
 						center.y +l0*p.x * v0.y + l1*p.y * v1.y + l2*p.z * v2.y,
 						center.z +l0*p.x * v0.z + l1*p.y * v1.z + l2*p.z * v2.z));
 
 			}
 		}
 		
 		WireframeMesh wf = new WireframeMesh();
 		wf.vertices = sphr;
 		int nphi = 2*numPhi;
 		int npsi = numPsi + 1;
 		for(int i = 0; i < npsi ; i++){
 			for(int j = 0; j < nphi ; j++){
 				int[] fc = {i*nphi + j, i*nphi+ (j + 1)%(nphi), (i+1)*nphi + j};
 				int[] fc2 = {(i+1)*nphi + j, i*nphi+ (j + 1)%(nphi),(i+1)*nphi+ (j + 1)%(nphi)};
 				wf.faces.add(fc);
 				wf.faces.add(fc2);
 			}
 		}
 		
 		return new GLWireframeMesh(wf);
 	}
 	
 	
 	/**
 	 * Compute the 3 Eigenvalues of a symmetric 3x3 matrix m. This nice algorithm
 	 * is taken from wikipedia: http://en.wikipedia.org/wiki/Eigenvalue_algorithm
 	 * @param m
 	 * @param evs
 	 */
 	public static float[] eigenValues(Matrix3f m){
 		float eig1, eig2, eig3;
 		float[] evs = new float[3];
 		float p1 = m.m01* m.m01 + m.m02*m.m02 + m.m12*m.m12;
 		if (p1 == 0) {
 		  // A is diagonal.
 		   eig1 = m.m00;
 		   eig2 = m.m11;
 		   eig3 = m.m22;
 		}
 		else{
 		   float q = (m.m00+ m.m11 + m.m22)/3;
 		   float p2 = (m.m00 - q)*(m.m00 - q) + (m.m11 - q)*(m.m11 - q)
 				   + (m.m22 - q)*(m.m22 - q) + 2 * p1;
 		   float p = (float) Math.sqrt(p2 / 6);
 		  // B = (1 / p) * (A - q * I)       // I is the identity matrix
 		   Matrix3f B = new Matrix3f(m);
 		   B.m00-= q; B.m11-= q; B.m22 -= q;
 		  B.mul(1.f/p);
 		   
 		   float r = B.determinant() / 2;
 		 
 		   // In exact arithmetic for a symmetric matrix  -1 <= r <= 1
 		   // but computation error can leave it slightly outside this range.
 		  double phi;
 		   if (r <= -1) 
 		      phi = Math.PI / 3;
 		   else if (r >= 1)
 		      phi = 0;
 		   else
 		      phi = Math.acos(r) / 3;
 		 
 		   // the eigenvalues satisfy eig3 <= eig2 <= eig1
 		   eig1 = q + 2 * p * (float) Math.cos(phi);
 		   eig3 = q - p * (float)( Math.cos(phi) + Math.sqrt(3)*Math.sin(phi) );
 		   eig2 = 3 * q - eig1 - eig3;     // since trace(A) = eig1 + eig2 + eig3
 		}
 		
 		evs[0] = eig1;
 		evs[1] = eig2;
 		evs[2] = eig3;
 		return evs;
 	}
 }
