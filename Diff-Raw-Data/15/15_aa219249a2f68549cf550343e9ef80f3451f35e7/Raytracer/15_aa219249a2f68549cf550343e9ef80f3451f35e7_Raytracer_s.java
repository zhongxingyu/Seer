 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 
 public class Raytracer extends PApplet {
 	private static final long serialVersionUID = 1L;
 	private static final String FILE_PATH = "res/test.txt";
 
 	private Camera camera;
 	private RTInterpreter interpreter;
 	
 	private int by, ty;
 	private int pixelValues[][];
 
     public static void main(String[] args) {
         PApplet.main(new String[] { Raytracer.class.getName() });
     }
 	
 	public void setup() {
 		//noLoop();
 
 		ArrayList<String> lines = null;
 		try {
 			FileParser parser = new FileParser( FILE_PATH );
 			lines = parser.returnLines();
 		} catch ( FileNotFoundException e ) {
 			e.printStackTrace();
 			exit();
 		}
 		if ( lines != null ) { 
 			interpreter = new RTInterpreter( lines, this );
 			if ( interpreter != null ) {
 				size( interpreter.width, interpreter.height, OPENGL );
 				background( interpreter.background.getRGB() );
 				camera = interpreter.camera;
 			}
 			// No interpreter exit app
 			else exit();
 		}
 		// No lines read from file exit app
 		else exit();
 	}
 	
 	public void calculatePixelValues() {
 		pixelValues = new int[width][height];
 		
 		float dist = 0.5f / tan( camera.fov / 2 );
 		PVector d = PVector.mult( camera.forward, dist );
 		
 		// Loop through every pixel
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
				PVector dir = PVector.add( PVector.add( d,
								PVector.mult( camera.up, ( 0.5f - ((float) y) / ( height - 1 ) ) ) ),
								PVector.mult( camera.right, ( 0.5f -  ((float) x) / ( width - 1 ) ) ) );
 				
 				Ray r = new Ray( camera.pos.get(), dir );
 				
 				// Find the closest intersection point
 				Primitive closestPrimitive = null;
 				Float closestT = null;
 				for (Primitive p : interpreter.primitives) {
 					Float t = p.tIntersect(r);
 					if (t != null && (closestT == null || t < closestT)) {
 						closestT = t;
 						closestPrimitive = p;
 					}
 				}
 				
 				// Get the color from the closest intersection point
 				if (closestT == null) {
 					pixelValues[x][y] = interpreter.background.getRGB();
 				} else {
 					PVector intersect = new PVector(
 							 r.o.x + closestT * r.d.x,
 							 r.o.y + closestT * r.d.y,
 							 r.o.z + closestT * r.d.z);
 					pixelValues[x][y] = closestPrimitive.getColor(intersect, camera.pos, interpreter.lights).getRGB();
 				}
 			}
 		}
 	}
 	
 	public void draw() {
 		background(interpreter.background.getRGB());
 		
 		if (pixelValues == null) {
 			camera.active();
 			calculatePixelValues();
 			by = ty = height / 2;
 		}
 		
 		loadPixels();
 		for (int y = by; y < ty; y++) {
 			for (int x = 0; x < width; x++) {
 				int i = (int) ( y * width + x );
 				pixels[i] = pixelValues[x][y];
 			}
 		}
 		updatePixels();
 
 		by--;
 		ty++;
 		if (ty >= height) {
 			if (by < 0) {
 				noLoop(); 
 				save(interpreter.outputFileName);
 			}
 			ty = height - 1;
 		}
 	}
 }
