 package tesseract.generators;
 
 import javax.vecmath.Vector3f;
 
 import tesseract.World;
 import tesseract.objects.PhysicalObject;
 import tesseract.objects.Sphere;
 
 /**
  * Generate a sphere field.
  * 
  * @author jesse
  */
 public class SphereField extends MenuItem {
 
 	private static final int FIELD_SIZE = 4;
 	
	private static final float SPHERE_SIZE = 0.15f;
 	
 	public SphereField(World theWorld) {
 		super("Sphere Field", theWorld);
 	}
 
 	/**
 	 * Generate the field.
 	 * 
 	 * @param theWorld Where to put them
 	 */
 	public void generate(final World theWorld) {
		final float start = 1.1f * 0.5f * FIELD_SIZE * SPHERE_SIZE; 
 		
		for (float x = -start; x <= +start; x += SPHERE_SIZE * 1.1f) {
			for (float y = -start; y <= +start; y += SPHERE_SIZE * 1.1f) {
				for (float z = -start; z <= +start; z += SPHERE_SIZE * 1.1f) {
 					PhysicalObject s = new Sphere(SPHERE_SIZE, new Vector3f(x, y, z));
 					theWorld.addObject(s);
 					
 				}
 			}
 			
 		}
 	}
 }
