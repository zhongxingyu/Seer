 package de.widemeadows.projectcore.math;
 
import android.opengl.GLU;
 import org.jetbrains.annotations.NotNull;
 
import javax.microedition.khronos.opengles.GL10;

 /**
  * Work in progress
  */
@Deprecated
 public final class Frustum {
 
 	/**
	 * Das Aspektverhältnis
	 */
//	private float _aspectRatio;

	/**
 	 * Entfernung der nahen Plane
 	 */
 	private float _nearDistance;
 	
 	/**
 	 * Entfernung der fernen Plane
 	 */
 	private float _farDistance;
 	
 	/**
 	 * Sichtwinkel in Grad
 	 */
 	private float _fieldOfView;
 
     /**
      * Der inverse Zoomfaktor (1/Zoomfaktor)
      * @see #_zoomFactor
      */
     private float _inverseZoomFactor = 1.0f;
 
     /**
      * Der Zoomfaktor
      * @see #_inverseZoomFactor
      */
     private float _zoomFactor = 1.0f;
 
 	/**
 	 * Aspektverhältnis
 	 */
 	private float _ratio;
 	
 	/**
 	 * Der Tangens des Blickwinkels
 	 */
 	private float _tangens;
 	
 	/**
 	 * Höhe der near plane
 	 */
 	private float _height;
 	
 	/**
 	 * Breite der near plane
 	 */
 	private float _width;
 	
 	/**
 	 * Kameraposition, ...
 	 */
 	@NotNull
 	private Vector3 eye, target, _up;
 
 	/**
 	 * Kamerareferenzvektoren
 	 */
     @NotNull
     private Vector3 _X,_Y,_Z;
 	
 	/**
 	 * Kugel-Faktoren
 	 */
 	private float _sphereFactorY, _sphereFactorX;
 
     /**
      * Bezieht die Kameraposition
      *
      * @return Die Position
      */
     @NotNull
     public Vector3 getCameraPosition() {
         return eye;
     }
 
     /**
      * Bezieht das Kameraziel
      *
      * @return Das Ziel
      */
     @NotNull
     public Vector3 getCameraTarget() {
         return target;
     }
 
 	/**
 	 * Liefert den Aufwärtsvektor der Kamera
 	 * @return Der Aufwärtsvektor
 	 */
 	@NotNull
 	public final Vector3 getCameraUpVector() {
 		return _up;
 	}
 
 	/**
 	 * Liefert den Rechtsvektor der Kamera
 	 *
 	 * @return Der Rechtsvektor
 	 */
 	@NotNull
 	public final Vector3 getCameraRightVector() {
 		return eye;
 	}
 
 	/**
 	 * Liefert den Augenvektor der Kamera
 	 *
 	 * @return Der Augenvektor
 	 */
 	@NotNull
 	public final Vector3 getCameraEyeVector() {
 		return target;
 	}
 
 	/**
 	 * Erzeugt einen neuen Sichtkegel
 	 * @param aspectRatio Das Apsektverh�ltnis
 	 * @param fov Das Blickfeld in Radians
 	 * @param nearDistance Die Entfernung zur nahen Ebene
 	 * @param farDistance Die Entfernung zur fernen Ebene
 	 */
 	public Frustum(float aspectRatio, float fov, float nearDistance, float farDistance) {
 
 		this(aspectRatio, fov, 1.0f, nearDistance, farDistance);
 	}
 
     /**
      * Erzeugt einen neuen Sichtkegel
      *
      * @param aspectRatio  Das Apsektverhältnis
      * @param fov          Das Blickfeld in Radians
      * @param zoomFactor   Der Zoomfaktor
      * @param nearDistance Die Entfernung zur nahen Ebene
      * @param farDistance  Die Entfernung zur fernen Ebene
      */
     public Frustum(float aspectRatio, float fov, float zoomFactor, float nearDistance, float farDistance) {
 
         set(aspectRatio, fov, zoomFactor, nearDistance, farDistance);
     }
 
     /**
      * Setzt die Parameter des Sichtkegels
      *
      * @param aspectRatio  Das Apsektverhältnis
      * @param fov          Das Blickfeld in Radians
      * @param nearDistance Die Entfernung zur nahen Ebene
      * @param farDistance  Die Entfernung zur fernen Ebene
      */
     public void set(float aspectRatio, float fov, float nearDistance, float farDistance) {
         set(aspectRatio, fov, 1.0f, nearDistance, farDistance);
     }
 
 	/**
 	 * Setzt die Parameter des Sichtkegels
      *
 	 * @param aspectRatio Das Apsektverhältnis
 	 * @param fov Das Blickfeld in Radians
      * @param zoomFactor Der Zoomfaktor
 	 * @param nearDistance Die Entfernung zur nahen Ebene
 	 * @param farDistance Die Entfernung zur fernen Ebene
 	 */
 	public void set(float aspectRatio, float fov, float zoomFactor, float nearDistance, float farDistance) {
         assert aspectRatio > 0;
         assert fov > 0;
         assert zoomFactor > 0;
         assert nearDistance > 0;
         assert farDistance > nearDistance;
 
//		_aspectRatio = aspectRatio;
 		_ratio = aspectRatio;
 		_nearDistance = nearDistance;
 		_farDistance = farDistance;
 		_fieldOfView = fov;
         setZoomFactorWithoutUpdatingPlanes(zoomFactor);
 
 		calculatePlaneSizes();
 	}
 
     /**
      * Setzt den Zoomfaktor, ohne die Planes zu aktualisieren
      * @param zoomFactor Der Zoomfaktor
      * @see #calculatePlaneSizes()
      */
     private void setZoomFactorWithoutUpdatingPlanes(float zoomFactor) {
         assert zoomFactor > 0;
 
         _inverseZoomFactor = 1.0f / zoomFactor;
         _zoomFactor = zoomFactor;
     }
 
     /**
      * Setzt das horizontale Blickfeld und berechnet den Sichtkegel neu
      *
      * @param fov        Das Sichtfeld in Radians
      * @param resetZoom  Gibt an, ob der Zoomfaktor zurückgesetzt werden soll
      * @see #setHorizontalFieldOfView(float, float) 
      */
     public void setHorizontalFieldOfView(float fov, boolean resetZoom) {
         assert fov > 0;
 
         if (resetZoom) setZoomFactorWithoutUpdatingPlanes(1.0f);
         _fieldOfView = fov;
 
         calculatePlaneSizes();
     }
 
     /**
      * Setzt das horizontale Blickfeld und berechnet den Sichtkegel neu
      *
      * @param fov Das Sichtfeld in Radians
      * @param zoomFactor Der Zoomfaktor
      */
     public void setHorizontalFieldOfView(float fov, float zoomFactor) {
         assert fov > 0;
         assert zoomFactor > 0;
 
         _fieldOfView = fov;
         setZoomFactorWithoutUpdatingPlanes(zoomFactor);
         
         calculatePlaneSizes();
     }
 
     /**
      * Liefert das horizontale Sichtfeld
      *
      * @return Winkel in Radians
      * @see #getZoomFactor()
      * @see #getHorizontalFieldOfViewEffective() 
      */
     public float getHorizontalFieldOfView() {
         return _fieldOfView;
     }
 
     /**
      * Liefert den Zoomfaktor
      *
      * @return Der Zoomfaktor
      * @see #getHorizontalFieldOfView()
      */
     public float getZoomFactor() {
         return _zoomFactor;
     }
 
     /**
      * Liefert den inversen Zoomfaktor
      *
      * @return Der inverse Zoomfaktor
      * @see #getHorizontalFieldOfView()
      */
     public float getInverseZoomFactor() {
         return _inverseZoomFactor;
     }
 
     /**
      * Liefert den Winkel des horizontalen Sichtbereiches unter Beachtung des Zoomfaktors
      *
      * @return Der Winkel in Radians
      * @see #getHorizontalFieldOfView()
      */
     public float getHorizontalFieldOfViewEffective() {
         return getHorizontalFieldOfView() * getInverseZoomFactor();
     }
 
 	/**
 	 * Liefert das Aspektverhältnis
 	 *
 	 * @return Das Aspektverhältnis
 	 */
 	public float getAspectRatio() {
 		return _ratio;
 	}
 
 	/**
 	 * Berechnet die Größen der nahen und fernen Plane
 	 */
 	private void calculatePlaneSizes() {
 		_tangens = (float)Math.tan(_fieldOfView * _inverseZoomFactor); // TODO: Noch weiter vorberechnen?
 		_height = _nearDistance * _tangens;
 		_width = _height * _ratio; 
 		
 		_sphereFactorY = 1.0f/(float)Math.cos(_fieldOfView);
 
 		// compute half of the the horizontal field of view and sphereFactorX 
 		float anglex = (float)Math.atan(_tangens*_ratio);
 		_sphereFactorX = 1.0f/(float)Math.cos(anglex);
 	}
 
 	/**
 	 * Setzt die Kameraparameter
 	 * @param position Die Position der Kamera
 	 * @param lookAt Der Punkt, auf den die Kamera blickt
 	 * @param up Der Hoch-Vektor der Kamera
 	 */
 	public void setCamera(@NotNull Vector3 position, @NotNull Vector3 lookAt, @NotNull Vector3 up) {
 		// http://www.lighthouse3d.com/opengl/viewfrustum/index.php?gimp
 
         // TODO: cc.set, wenn nicht null
 		eye = position.clone();
 		target = lookAt.clone();
 		_up = up.clone();
 		
 		// compute the Z axis of the camera referential
 		// this axis points in the same direction from 
 		// the looking direction
 		_Z = lookAt.sub(position);
 		_Z.normalize();
 
 		// X axis of camera with given "up" vector and Z axis
 		_X = _Z.cross(up);
 		_X.normalize();
 
 		// the real "up" vector is the dot product of X and Z
 		_Y = _X.cross(_Z);
 	}
 
 	/**
 	 * Ermittelt, ob ein Punkt im Sichtfeld liegt
 	 * @param point Der Punkt
 	 * @return Testergebnis
 	 */
 	public boolean intersects(@NotNull Vector3 point) {
 
 		float pcz,pcx,pcy,aux;
 
 		// compute vector from camera position to p
 		Vector3 v = point.sub(eye);
 
 		// compute and test the Z coordinate
 		pcz = v.dot(_Z);
 		if (pcz > _farDistance || pcz < _nearDistance)
 			return false; //return Intersection.OUTSIDE;
 
 		// compute and test the Y coordinate
 		pcy = v.dot(_Y);
 		aux = pcz * _tangens;
 		if (pcy > aux || pcy < -aux)
 			return false; // return Intersection.OUTSIDE;
 			
 		// compute and test the X coordinate
 		pcx = v.dot(_X);
 		aux = aux * _ratio;
 		if (pcx > aux || pcx < -aux)
 			return false; // return Intersection.OUTSIDE;
 
 		return true; // return Intersection.INSIDE;
 	}
 
 	/**
 	 * Ermittelt, ob eine Kugel vom Sichtfeld eingeschlossen ist
 	 * @param sphere Die Kugel
 	 * @return Testergebnis
 	 */
 	public boolean intersects(@NotNull Sphere sphere) {
 
 		boolean result = true; // Intersection result = Intersection.INSIDE;
 
 		Vector3 position = sphere.getPosition();
 		float radius = sphere.getRadius();
 		
 		float d;
 		float az,ax,ay;
 
 		Vector3 v = position.sub(eye);
 
 		az = v.dot(_Z);
 		if (az > _farDistance + radius || az < _nearDistance-radius)
 			return false; // return(Intersection.OUTSIDE);
 		if (az > _farDistance - radius || az < _nearDistance+radius)
 			result = true; // result = Intersection.INTERSECTS;
 
 		ay = v.dot(_Y);
 		d = _sphereFactorY * radius;
 		az *= _tangens;
 		if (ay > az+d || ay < -az-d)
 			return false; // return(Intersection.OUTSIDE);
 		if (ay > az-d || ay < -az+d)
 			result = true; // result = Intersection.INTERSECTS;
 
 		ax = v.dot(_X);
 		az *= _ratio;
 		d = _sphereFactorX * radius;
 		if (ax > az+d || ax < -az-d)
 			return false; // return(Intersection.OUTSIDE);
 		if (ax > az-d || ax < -az+d)
 			result = true; // result = Intersection.INTERSECTS;
 
 		return result;
 	}
 
	/**
	 * Wendet das Frustum auf die OpenGL-Instanz an
	 * @param gl Die OpenGL-Instanz
	 */
	public void apply(@NotNull GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float zoomDingsFoo = _fieldOfView * _inverseZoomFactor;
        GLU.gluPerspective(gl, zoomDingsFoo, _ratio, _nearDistance, _farDistance);
		GLU.gluLookAt(gl, eye.x, eye.y, eye.z, target.x, target.y, target.z, _up.x, _up.y, _up.z);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
	}

     public void setAspectRatio(float aspectRatio) {
         _ratio = aspectRatio;
     }
 }
