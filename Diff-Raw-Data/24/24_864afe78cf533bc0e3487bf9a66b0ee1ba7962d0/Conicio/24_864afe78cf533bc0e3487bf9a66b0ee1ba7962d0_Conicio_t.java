 package conicio;
 
 import conicio.shapes.*;
 import conicio.util.*;
 import java.util.*;
 import java.io.*;
 import javax.imageio.*;
 import java.awt.image.*;
 
 public class Conicio {
 
 	public static void main(String[] args) {
 		if(args.length == 0) { 
 			System.err.println("ERROR: No scene specified"); 
 			return;	
 		}
 		
 		boolean visual = false;
 		boolean render = true;
 		for(String arg : args) {
 			if(arg.equals(  "-visual")) { visual = true;  }
 			if(arg.equals("-norender")) { render = false; }
 		}
 
 		try {
 			Class<?> sceneClass = Class.forName(args[args.length - 1]);
 			Scene<?> scene      = (Scene<?>)sceneClass.newInstance();
 		
 			Renderer<?> renderer = Renderer.create(scene);
 			
 			if(visual) {
 				Visualizer visualizer = new Visualizer(renderer);
 				new Thread(visualizer).start();
 			}
 			
 			long start = System.nanoTime();
 			renderer.run();
 			long elapsed = System.nanoTime() - start;
 			System.out.format("Render Time: %f(s)%n", elapsed / 1000000000.);
 			
 			if(render) {
 				String filename = String.format(
 					"img/render-%s.png", 
 					(new Date()).toString().replace(" ", "-")
 				);
 				BufferedImage image = new BufferedImage(
 					scene.xSize(), scene.ySize(), 
 					BufferedImage.TYPE_INT_RGB
 				);
 				image.getGraphics().drawImage(
 					renderer.result(), 
 					0, 0, scene.xSize(), scene.ySize(), 
 					null
 				);
 				ImageIO.write(image, "png", new File(filename));
 			}
 		}
 		catch(Exception e) {
			e.printStackTrace();
 		}
 	}
 }
