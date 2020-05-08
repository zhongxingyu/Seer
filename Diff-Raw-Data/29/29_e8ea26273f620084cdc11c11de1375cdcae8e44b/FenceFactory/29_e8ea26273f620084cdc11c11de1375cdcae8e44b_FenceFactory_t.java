 package org.sanchome.shy.engine.entity.tool;
 
 import org.sanchome.shy.engine.entity.Fence;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.Node;
 
 public class FenceFactory {
 	
 	private AssetManager assetManager;
 	private Camera camera;
 	private Node rootNode;
 	private BulletAppState bulletAppState;
 	
 	private static final float FENCE_LENGTH = 3.3f*2.0f;
 	
 	private FenceFactory() {};
 	
 	private FenceFactory(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
 		this.assetManager = assetManager;
 		this.camera = camera;
 		this.rootNode = rootNode;
 		this.bulletAppState = bulletAppState;
 	}
 	
 	public static FenceFactory getPencil(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
 		return new FenceFactory(assetManager, camera, rootNode, bulletAppState);
 	}
 	
 	public void drawLine(float startX, float startZ, float endX, float endZ) {
 		Vector2f start = new Vector2f(startX, startZ);
 		Vector2f end   = new Vector2f(endX, endZ);
 		
 		float distance = start.distance(end);
 		
 		// Floors fenceNumber to compute a right rounded fenceStep
 		// Remove an half fence length at start and an half fence length at end. Total: 1 fence length less. 
 		int fenceNumber = (int)FastMath.floor(distance / FENCE_LENGTH) - 1;
 
 		// Compute the fenceStep now
 		float fenceStep = distance / fenceNumber;
 		
 		Vector2f vStep = end.subtract(start).normalizeLocal().multLocal(fenceStep);
 		
 		// Cause fence local origin is at the middle bottom of itself:
 		// -^----^----^-
 		//  |    |    |
 		// -+----+----+-
 		//  '    0    '
 		// Starting at : start + vStep*0.5
 		// Ending at   : end - vStep*0.5
 		Vector2f currentPosition = start.clone().addLocal(vStep.mult(0.5f));
 		for(int i=0; i<fenceNumber; i++) {
 			// Create a fence at currentPosition
 			Fence f = new Fence();
 			f.init(assetManager, camera, rootNode, bulletAppState);
 			f.setPosition(currentPosition.x, 0.0f, currentPosition.y);
 			// Where we go for the next step
 			f.setYRotation(vStep.getAngle());
			System.out.println("fence angle:"+vStep.getAngle());
 			currentPosition.addLocal(vStep);
 		}
 	}
 	
 	public void drawRectangle(float centerX, float centerZ, float widthX, float widthZ) {
 		drawLine(centerX-widthX/2.0f, centerZ-widthZ/2.0f, centerX+widthX/2.0f, centerZ-widthZ/2.0f);
 		drawLine(centerX+widthX/2.0f, centerZ-widthZ/2.0f, centerX+widthX/2.0f, centerZ+widthZ/2.0f);
 		drawLine(centerX+widthX/2.0f, centerZ+widthZ/2.0f, centerX-widthX/2.0f, centerZ+widthZ/2.0f);
 		drawLine(centerX-widthX/2.0f, centerZ+widthZ/2.0f, centerX-widthX/2.0f, centerZ-widthZ/2.0f);
 	}
 	
 	public void drawCircle(float centerX, float centerZ, float radius) {
 		float perimetre = FastMath.TWO_PI * radius;
 		int fenceNumber = (int)FastMath.floor(perimetre / FENCE_LENGTH) - 1;
 		float fenceAngularStep = FastMath.TWO_PI / fenceNumber;
 		for(float alpha=0.0f; alpha<FastMath.TWO_PI; alpha += fenceAngularStep) {
 			float fenceX = centerX + radius * FastMath.cos(alpha);
 			float fenceZ = centerZ + radius * FastMath.sin(alpha); 
 			Fence f = new Fence();
 			f.init(assetManager, camera, rootNode, bulletAppState);
 			f.setPosition(fenceX, 0.0f, fenceZ);
 			// Where we go for the next step
 			f.setYRotation(alpha - FastMath.HALF_PI);
 		}
 	}
	
	public void fenceAngleTest() {
		float alpha = 0.0f;
		for(float fenceX = -150.0f; fenceX<250.0; fenceX+=FENCE_LENGTH) {
			Fence f = new Fence();
			f.init(assetManager, camera, rootNode, bulletAppState);
			f.setPosition(fenceX, 0.0f, -10.0f);
			// Where we go for the next step
			f.setYRotation(alpha);
			alpha += FastMath.QUARTER_PI/2.0f;
		}
	}
 }
