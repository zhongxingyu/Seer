 public class FunkyAnimator {
 
 	private FunkyScene Scene;
 
 	public FunkyAnimator(FunkyScene scene) {
 		this.Scene = scene;
 	}
 
 	public void tick() {
 		int i;
 		FunkyBaseObject obj;
 		if (Scene.objectsNumber > 0)
 			for (i = 0; i < Scene.objectsNumber; i++) {
 				obj = Scene.objects[i];
 				obj.move();
				if (obj.coords.x > Scene.height || obj.coords.x < 0) obj.force.bounceX();
 				if (obj.coords.y > Scene.height || obj.coords.y < 0) obj.force.bounceY();
 			}
 		Scene.tick();
 	}
 }
