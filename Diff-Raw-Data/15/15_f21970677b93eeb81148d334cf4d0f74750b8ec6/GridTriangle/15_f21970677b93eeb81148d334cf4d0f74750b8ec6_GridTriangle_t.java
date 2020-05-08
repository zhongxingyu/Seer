 package triangleeditor;
 import org.jbox2d.dynamics.Body;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 
 /**
  * Represents a triangle given 3 points
  * @author onedayitwillmake
  */
 public class GridTriangle {
 	public PVector	A;
 	public PVector	B;
 	public PVector	C;
 	public PVector	centroid;
 	public PVector	origin;
 	
 	private Boolean	_isActive;
 	private Body	_body;
 	
 	
 	private PApplet	app;
 	
 	public GridTriangle( PVector a, PVector b, PVector c, PVector originPoint, PApplet appRef) {
 		A = a;
 		B = b;
 		C = c;
 		app = appRef;
 		_isActive = false;
 		
 		
 		origin = originPoint;
 		computeCentroid();
 	}
 	
 	/**
 	 * Given a point X,Y will return if it is contained within this triangle
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public Boolean containsPoint(int x, int y) {
 		PVector p = new PVector(x, y);
 		PVector v1 = PVector.sub(p, A);
 		v1.normalize();
 		PVector v2 = PVector.sub(p, B);
 		v2.normalize();
 		PVector v3 = PVector.sub(p, C);
 		v3.normalize();
         double total_angles = Math.acos(v1.dot(v2));
         
         total_angles += Math.acos(v2.dot(v3));
         total_angles += Math.acos(v3.dot(v1));
         return (PApplet.abs((float) total_angles - PApplet.TWO_PI) <= 0.01f);
 	}
 	
 	public void draw() {
 		if(!_isActive) { 
 			return;
 		}
 		
 		app.triangle(A.x, A.y, B.x, B.y, C.x, C.y);
 		
 		Boolean shouldDrawCenter = true;
 		if(shouldDrawCenter) {
 			// Draw center
 			app.fill( 255 );
 			app.ellipseMode(PApplet.CENTER);
 			app.ellipse(centroid.x, centroid.y, 2, 2);
 			
 		}
 		
 //		rotate(1);
 	}
 	
 	/**
 	 * Rotates the triangle about it's origin by angle
 	 * @param angle	Angle in degrees for rotation, from current rotation. e.g. 90+90 will equal 180 degrees rotation
 	 */
 	public void rotate( float angle ) {
 		PVector[] allPoints = {A,B,C};
 		PVector o = origin;
 		angle *= PApplet.DEG_TO_RAD;
 		
 		for(int i = 0; i < 3; i++) {
 			PVector p = new PVector(allPoints[i].x, allPoints[i].y);
 			PVector np = new PVector(0,0);
 			p.x += (0 - o.x);
 			p.y += (0 - o.y);
 			np.x = (float) ((p.x * Math.cos(angle)) - (p.y * Math.sin(angle)));
 			np.y = (float) (Math.sin(angle) * p.x + Math.cos(angle) * p.y);
 			np.x += (0 + o.x);
 			np.y += (0 + o.y);
 	        
 			allPoints[i].set(np);
 		}
 		
 		computeCentroid();
 	}
 	
 	/**
 	 * Calculates the centroid of this triangle
 	 */
 	private void computeCentroid() {
 		centroid = new PVector();
 		centroid.x = (A.x + B.x + C.x) / 3.0f;
 		centroid.y = (A.y + B.y + C.y) / 3.0f;
 	}
 	
 	public PVector[] getPoints( Boolean clockWise ) {
		if(C.x < A.x) {
			PVector[] CBA = {C,B,A};
			return CBA;
		} else {
			PVector[] ABC = {A,B,C};
			return ABC;
		}
 	}
 	
 	/**
 	 * If a triangle is active that means it has been clicked, but that will mean something else later
 	 * @return
 	 */
 	public Boolean get_isActive() { return _isActive; }
 	public void set_isActive(Boolean _isActive) { this._isActive = _isActive; }
 
 	/**
 	 * Get/Set the Box2D body
 	 * @return
 	 */
 	public Body get_body() { return _body; }
 	public void set_body(Body _body) {this._body = _body; }	
 	
 }
