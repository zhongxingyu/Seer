 package cube;
 
 /**
  * Used to apply changes on the Cube due to pressed keys on the keyboard.
 * @author Björn Tegelund
  */
 public class CubeController {
 	private Cube c;
	private float targetRotY, targetRotZ, targetRotX;
 	
 	/**
 	 * Creates a new CubeController to be used to apply changes on the Cube due to pressed keys on the keyboard.
 	 * @param c The Cube which should be affected.
 	 */
 	public CubeController(Cube c) {
 		this.c = c;
 	}
 	
 	/**
 	 * Applies all the current changes. This method is used to give the rotation of the Cube a smoother animation.
 	 * TODO Tidy this up
 	 */
 	public void runAnimation() {
		if(Math.abs(targetRotY) > 0.015f || Math.abs(targetRotZ) > 0.015f || Math.abs(targetRotX) > 0.015f) {
 			if(Math.abs(targetRotY) > 0.015f) {
 				c.setRotY(c.getRotY() + (c.getRotY() - (c.getRotY() + targetRotY)) * 0.1f);
 				targetRotY /= 1.1;
 			}
 			if(Math.abs(targetRotZ) > 0.015f) {
 				c.setRotZ(c.getRotZ() + (c.getRotZ() - (c.getRotZ() + targetRotZ)) * 0.1f);
 				targetRotZ /= 1.1;
 			}
			if(Math.abs(targetRotX) > 0.015f) {
				c.setRotX(c.getRotX() + (c.getRotX() - (c.getRotX() + targetRotX)) * 0.1f);
				targetRotX /= 1.1;
			}
 		}
 		else {
 			c.setRotY(c.getRotY() + targetRotY);
 			c.setRotZ(c.getRotZ() + targetRotZ);
			c.setRotX(c.getRotX() + targetRotX);
 			if(Math.abs(targetRotY) != 0.015f) {
 				targetRotY = 0f;
 			}
			if(Math.abs(targetRotX) != 0.015f) {
				targetRotX = 0f;
			}
 			if(Math.abs(targetRotZ) != 0.015f) {
 				targetRotZ = 0f;
 			}
 		}
 	}
 
 	public void rotateY (float tarY) {
 		targetRotY = tarY;
 	}
 	public void rotateZ (float tarZ) {
 		targetRotZ = tarZ;
 	}
	public void rotateX (float tarX) {
		targetRotX = tarX;
	}
 }
