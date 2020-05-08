 package teamk.hw4.controller;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.media.opengl.GL2;
 
 import teamk.hw4.model.*;
 import teamk.hw4.model.geometry.TKAbstractGeometryObject;
 import teamk.hw4.model.geometry.TKPlane;
 import teamk.hw4.model.geometry.TKSphere;
 import teamk.hw4.model.material.TKSimpleColorMaterial;
 import teamk.hw4.model.material.TKSimpleMirrorMaterial;
 import teamk.hw4.model.uvmapper.TKSimpleUVMapper;
 import teamk.hw4.utils.math.*;
 
 
 /**
  * A concrete TKScene class that draws the specific scene for HW4
  * 
  * @author Yi Qiao, Tyler Laracuente, Ian O'Connor
  *
  */
 public class TKRayTraceScene extends TKScene {
 	
 	private TKVector3 lightLocation; 	/**< The location of the light source */
 	
 	// The location of the camera
 	private TKVector3 cameraLocation;
 	
 	private double[][][] rayTraceBuffer;
 	
 	private int xScanDensity;
 	private int yScanDensity;
 	
 	private double windowWidth;
 	private double windowHeight;
 
 	private double[] ambientColor = new double[] {0.25, 0.25, 0.25, 1.0};
 	private double[] specularColor = new double[] {1.0, 1.0, 1.0, 1.0};
 	private double ambientFactor = 0.5;
 	private double specularFactor = 0.1;
 	
 	/** The objects in the scene */
 	private List<TKAbstractGeometryObject> objects = new LinkedList<TKAbstractGeometryObject>();	
 	
 	public TKRayTraceScene() {
 		
 		// Set scan densities
 		xScanDensity = 500;
 		yScanDensity = 500;
 		rayTraceBuffer = new double[xScanDensity][yScanDensity][4];
 
 		// Set window height and width
 		windowWidth = 800;
 		windowHeight = 800;
 
 		// Set camera and light locations
 		cameraLocation = new TKVector3(250.0, 250.0, 200.0);
 		lightLocation = new TKVector3(500.0, 300.0, 200.0);
 		
 		// Create a green sphere at (250, 200, -10) with radius 50
 		TKAbstractGeometryObject greenSphere = new TKSphere(new TKVector3(250.0, 200.0, -10.0), 50.0);
 		greenSphere.setUVMapper(new TKSimpleUVMapper());
 		greenSphere.setMaterial(new TKSimpleColorMaterial(0.0, 1.0, 0.0, 1.0));
 		objects.add(greenSphere);
 		
 		// Create a randomly colored sphere at
 		TKAbstractGeometryObject randSphere = new TKSphere(new TKVector3(400.0, 300.0, -10.0), 60.0);
 		randSphere.setUVMapper(new TKSimpleUVMapper());
 		randSphere.setMaterial(new TKSimpleColorMaterial(Math.random(), Math.random(), Math.random(), 1.0));
 		objects.add(randSphere);
 		
 		// Create first mirrored sphere
 		TKAbstractGeometryObject mirrorSphereA = new TKSphere(new TKVector3(100.0, 100.0, -20.0), 60.0);
 		mirrorSphereA.setUVMapper(new TKSimpleUVMapper());
 		mirrorSphereA.setMaterial(new TKSimpleMirrorMaterial());
 		objects.add(mirrorSphereA);
 		
 		// Create second mirrored sphere
		TKAbstractGeometryObject mirrorSphereB = new TKSphere(new TKVector3(300.0, 50.0, -10.0), 60.0);
 		mirrorSphereB.setUVMapper(new TKSimpleUVMapper());
 		mirrorSphereB.setMaterial(new TKSimpleMirrorMaterial());
 		objects.add(mirrorSphereB);
 
 		// Create a plane in background
 		TKAbstractGeometryObject redPlane = new TKPlane(0.0, 1.0, 1.0, 200.0);
 		redPlane.setMaterial(new TKSimpleColorMaterial(1.0, 0.0, 0.0, 1.0));
 		objects.add(redPlane);
 		
 		// Create a checkered plane
 		TKAbstractGeometryObject bluePlane = new TKPlane(200.0, 200.0, 200.0, 300.0);
 		bluePlane.setMaterial(new TKSimpleColorMaterial(0.0, 0.0, 1.0, 1.0));
 		objects.add(bluePlane);
 		
 	}
 
 	@Override
 	public void render(GL2 gl) {
 		gl.glPointSize((float)Math.max(windowWidth / xScanDensity, windowHeight / yScanDensity));
 		gl.glBegin(GL2.GL_POINTS);
 		for (int i = 0; i < xScanDensity; i++) {
 			for (int j = 0; j < yScanDensity; j++) {
 				if (rayTraceBuffer[i][j] != null) {
 					gl.glColor4d(
 							rayTraceBuffer[i][j][0],
 							rayTraceBuffer[i][j][1],
 							rayTraceBuffer[i][j][2],
 							rayTraceBuffer[i][j][3]);
 					gl.glVertex3d(i * 500.0 / xScanDensity, j * 500.0 / yScanDensity, 0);
 				}
 			}
 		}
 		gl.glEnd();
 	}
 
 	@Override
 	public void updateAnimation(long timeElapsed) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	/**
 	 * Calculates the light vector that starts from a given point, and points to the light source
 	 * 
 	 * This function is not part of TKITraceable because they are the same for all objects
 	 * 
 	 * @param p   The point at which the light vector starts
 	 * @return    The light vector
 	 */
 	public TKVector3 lightVectorAtPoint(TKVector3 p) {
 		return lightLocation.sub(p).getNormalized();
 	}
 }
