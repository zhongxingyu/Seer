 package com.vloxlands.util.math;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.settings.CFG;
 
 /**
  * @author Dakror
  */
 public class PickingRay
 {
 	static float ratio;
 	static float wNear, hNear, wFar, hFar;
 	
 	public Vector3f start, end;
 	float length;
 	
 	public PickingRay(Vector3f n, Vector3f f)
 	{
 		start = n;
 		end = f;
 		length = Vector3f.sub(n, f, null).length();
 	}
 	
 	public static void update()
 	{
 		ratio = (float) Display.getWidth() / Display.getHeight();
 		
		hNear = (float) (Math.tan(Math.toRadians(CFG.FOV / 2)) * Game.zNear);
 		wNear = hNear * ratio;
 		
		hFar = (float) (Math.tan(Math.toRadians(CFG.FOV / 2)) * CFG.RENDER_DISTANCES[CFG.RENDER_DISTANCE]);
 		wFar = hFar * ratio;
 	}
 	
 	public static PickingRay getPickingRay(float mouseX, float mouseY)
 	{
		float displayNearRatio = wNear / Display.getWidth() * 2;
		float displayFarRatio = wFar / Display.getWidth() * 2;
 		
		float x = (Display.getWidth() / 2) - mouseX;
		float y = (Display.getHeight() / 2) - (Display.getHeight() - mouseY);
 		
 		
 		Vector3f near = new Vector3f(x * displayNearRatio, y * displayNearRatio, Game.zNear);
		Vector3f far = new Vector3f(x * displayFarRatio, y * displayFarRatio, (CFG.RENDER_DISTANCES[CFG.RENDER_DISTANCE] - 5));
 		
 		near = MathHelper.rotateVectorByCameraRotation(near);
 		far = MathHelper.rotateVectorByCameraRotation(far);
 		
 		near.translate(Game.camera.position.x, Game.camera.position.y, Game.camera.position.z);
 		far.translate(Game.camera.position.x, Game.camera.position.y, Game.camera.position.z);
 		
 		return new PickingRay(near, far);
 	}
 }
