 package emblcmci.view3d;
 
 /** Calculates angular displacement of tracks. 
  * 
  * 20120328
  * 
  */
 
 import org.apache.commons.math.geometry.euclidean.threed.Vector3D;
 
 
 public class CalcAngularDisplacement {
 	
 	/** Calculates angular displacement vector towards a reference bar.
 	 * returned value is an DispVec class holding containing displacement vector and direction (1 or -1) 
 	 * 
 	 * @param osv track vector start point
 	 * @param oev track vector end point
 	 * @param ocv single point, center of the sphere
 	 * @return DispVec instance, holding a Vector3D, the net displacement vector towards a point and its direction
 	 * <br>
 	 * <br>	
 	 */
 	public AngularDisp calcAngularVector(Vector3D osv, Vector3D oev, Vector3D ocv){	
 		Vector3D pv = oev.subtract(osv);
 		Vector3D csv = osv.subtract(ocv);
 		Vector3D cev = oev.subtract(ocv);
 		Vector3D dv = null;
 		// prepare two vectors in the tangential plane
 		Vector3D av = csv.orthogonal();
 		Vector3D bv = Vector3D.crossProduct(csv, av);
 		Double ap = Vector3D.dotProduct(av, pv);
 		Double bp = Vector3D.dotProduct(bv, pv);
 		Double n = ap/Math.pow(av.getNorm(), 2);
 		Double m = bp/Math.pow(bv.getNorm(), 2);
 		dv = new Vector3D(n, av, m, bv);
		Double seangle = Vector3D.angle(csv, cev); 
 		AngularDisp ad = new AngularDisp(dv, osv, 1, seangle);
 		return ad;
 	}
 	/** A class representing single angular displacement vector. 
 	 * dv:	angular displacement vector
 	 * osv:	positional vector of starting point. 
 	 * 
 	 * @author Kota Miura
 	 *
 	 */
 	public class AngularDisp {
 		public final Vector3D dv;
 		public final int direc;
 		public final Vector3D osv;
 		public final Double seangle;
 		public AngularDisp(Vector3D dv, Vector3D osv, int direc, Double seangle){
 			this.dv = dv;
 			this.osv = osv;
 			this.direc = direc;
 			this.seangle = seangle;
 		}
 		
 	}
 
 }
