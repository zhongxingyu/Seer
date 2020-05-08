 package GUI;
 
 import Utility.*;
 import World.*;
 
 public class Run {
 
 	/**
 	 * The main class of the raytracer.
 	 */
 
 	public static void main(String[] args) {
		
 		World myWorld = new TestScene();
 		RGBColor[][] rendered_image = myWorld.render_scene();
 		new GUI(rendered_image);
		
 	}
 
 }
