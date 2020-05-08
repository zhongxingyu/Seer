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
 		
		hNear = (float) (2 * Math.tan(Math.toRadians(CFG.FOV)) * Game.zNear);
 		wNear = hNear * ratio;
 		
		hFar = (float) (2 * Math.tan(Math.toRadians(CFG.FOV)) * CFG.RENDER_DISTANCE);
 		wFar = hFar * ratio;
 	}
 	
 	public static PickingRay getPickingRay(float mouseX, float mouseY)
 	{
		float displayNearRatio = wNear / Display.getWidth();
		float displayFarRatio = wFar / Display.getWidth();
 		
		float x = mouseX - (Display.getWidth() / 2);
		float y = mouseY - (Display.getWidth() / 2);
 		
 		
 		Vector3f near = new Vector3f(x * displayNearRatio, y * displayNearRatio, Game.zNear);
		Vector3f far = new Vector3f(x * displayFarRatio, y * displayFarRatio, CFG.RENDER_DISTANCES[CFG.RENDER_DISTANCE]);
 		
 		near = MathHelper.rotateVectorByCameraRotation(near);
 		far = MathHelper.rotateVectorByCameraRotation(far);
 		
 		near.translate(Game.camera.position.x, Game.camera.position.y, Game.camera.position.z);
 		far.translate(Game.camera.position.x, Game.camera.position.y, Game.camera.position.z);
 		
 		return new PickingRay(near, far);
 	}
 }
