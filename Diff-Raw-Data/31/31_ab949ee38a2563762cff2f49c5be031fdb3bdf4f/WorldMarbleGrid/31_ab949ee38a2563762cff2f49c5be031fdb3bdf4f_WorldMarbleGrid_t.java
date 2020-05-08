 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 /**
  * Creates a grid of super balls which are just dropped
  */
 public class WorldMarbleGrid extends WorldGenerator{
 
 	final static double step = 1.0 / 24.0;
 
 	final static double back = -10;
 	final static double front = 0;
 	final static double left = -2.25;
 	final static double right = 2.25;
 	final static double bottom = 0.0;
 	final static double top = 3.0;
 	
 	final static double diminish = 0.9;
 	
 	final static int ROWS = 0;
 	final static int COLUMNS = 1;
 	final static int DIAGONAL = 2;
	
	static int frames;
 
 	World[] worlds;
 	
	public WorldMarbleGrid( int marbles, int style ){
		this( marbles, style, Integer.MAX_VALUE );
	}
	
 	/**
 	 * Makes a grid of superballs
 	 * @param marbles Sqrt of number of marbles to draw
 	 * @param style   Marble layout style [ROWS, COLUMNS, DIAGONAL]
 	 */
	public WorldMarbleGrid( int marbles, int style, int frameCount ){
		frames = frameCount;
 		LinkedList<World> worldList = new LinkedList<World>(); 
 		
 		double centerx = 0;
 		double centerz = -2;
 		double centery = 1;
 		
 		double radius = 0.1;
 		
 		Color[] colors = new Color[marbles*marbles];
 		double ka = 0.2;
 		double kd = 0.2;
 		double ks = 0.4;
 		double kr = 0.0;
 		double kt = 0.5;
 		int ke = 50;
 
 		// Marble x,y,z positions
 		double posx[] = new double[marbles*marbles];
 		double posy[] = new double[marbles*marbles];
 		double posz[] = new double[marbles*marbles];
 		
 		double spdy[] = new double[marbles*marbles];
 		
 		// Generate coordinates for doing a diagonal layout
 		int[][] diag = new int[marbles][marbles];
 		int row = 1;
 		int r = 0;
 		int c = 0;
 		for( int count = 0; count < marbles * marbles; ){
 			for( int i = 0; i < row; i++ ){
 //				diag[r][c] = count++;
 				count++;
 				diag[r][c] = row;
 				r++;
 				c--;
 			}
 			if( count < marbles * marbles / 2 ){
 				r = 0;
 				c = row;
 				row++;
 			}else{
 				row--;
 				r = marbles - row;
 				c = marbles - 1;
 			}
 		}
 		
 		// Generate marble starting coordinates
 		for( int i = 0; i < marbles; i++ ){
 			for( int k = 0; k < marbles; k++ ){
 				posx[i*marbles+k] = ((marbles-1)*2*radius) / 2.0 - ( k * 2*radius ) + centerx;
 				posz[i*marbles+k] = -((marbles-1)*2*radius) / 2.0 - ( i * 2*radius ) + centerz;
 				if( style == ROWS )
 					posy[i*marbles+k] = ((marbles-1)*2*radius) / 2.0 - ( i*marbles+k) * -(radius/2) + centery;
 				if( style == COLUMNS )
 					posy[k*marbles+i] = ((marbles-1)*2*radius) / 2.0 - ( i*marbles+k) * -(radius/2) + centery;
 				if( style == DIAGONAL )
 					posy[k*marbles+i] = ((marbles-1)*2*radius) / 2.0 - ( diag[i][k] ) * -(radius/2) + centery;
 				spdy[i*marbles+k] = 0.0;
 				
 //				colors[i*marbles+k] = new Color(192,192,192); // Silver
 				colors[i*marbles+k] = new Color(255,20,147); // Pink
 			}
 		}
 		
 		// Render worlds until all marbles have stopped moving
 		boolean done = false;
		while( !done && worldList.size() < frameCount ){
 			World world = new World();
 			worldList.add( world );
 			
 
 			// Set up spheres
 			Point3d centers[] = new Point3d[posx.length];
 			for( int k = 0; k < centers.length; k++ ){
 				centers[k] = new Point3d( posx[k], posy[k], posz[k] );
 			}
 			
 
 			// Set up floor
 			ArrayList<Point3d> triAVertices = new ArrayList<Point3d>();
 			ArrayList<Point3d> triBVertices = new ArrayList<Point3d>();
 
 			Point3d PLANEVERTLF = new Point3d( left, bottom, front );
 			Point3d PLANEVERTRF = new Point3d( right, bottom, front );
 			Point3d PLANEVERTRR = new Point3d( right, bottom, back );
 			Point3d PLANEVERTLR = new Point3d( left, bottom, back );
 
 			triAVertices.add( PLANEVERTLF );
 			triAVertices.add( PLANEVERTRF );
 			triAVertices.add( PLANEVERTRR );
 
 			triBVertices.add( PLANEVERTLR );
 			triBVertices.add( PLANEVERTRR );
 			triBVertices.add( PLANEVERTLF );
 
 			// Set up light
 			Point3d LIGHTCENTER = new Point3d( 0.3, 2.75, 1.0 );
 
 			// Add objects to world
 			for( int k = 0; k < centers.length; k++ ){
 				world.add( new Sphere( centers[k], radius, colors[k], ka, kd, ks, ke, kr, kt ) );
 			}
 			world.add( new Triangle( triAVertices, new Vector3d( 0, 1, 0 ), new Color( 0, 0, 0 ), 1 ) );
 			world.add( new Triangle( triBVertices, new Vector3d( 0, 1, 0 ), new Color( 0, 0, 0 ), 1 ) );
 
 			world.add( new PointLight( LIGHTCENTER, new Color( 255.0, 255.0, 255.0 ) ) );
 			
 			// Update object positions
 			done = true;
 			for( int k = 0; k < centers.length; k++ ){
 				
 				// Check if every marble has finished bouncing
 				if( spdy[k] != 0 || posy[k] != bottom + radius ){
 					done = false;
 				}
 				
 				// Compute drop distance
 				double d = spdy[k] * step + -9.8 * step * step / 2;
 				
 				if( posy[k] + d - radius < bottom ){
 					// Will fall below floor, compute rebound
 				
 					// Compute drop
 					d = posy[k] - radius - bottom;
 					double vf = -Math.sqrt( spdy[k] * spdy[k] - 2 * -9.8 * d );
 					double t = (vf - spdy[k]) / -9.8;
 
 					// Compute rebound
 					t = step - t;
 					spdy[k] = -vf * diminish;
 					vf = spdy[k] + -9.8 * t;
 					d = (spdy[k] + vf) / 2 * t;
 
 					// Set new pos and speed
 					posy[k] = bottom + radius + d;
 					spdy[k] = vf;
 
 					// Check threshold to stop bouncing
 					if( posy[k] - radius - bottom < .02 && spdy[k] < .5 ){
 						posy[k] = bottom + radius;
 						spdy[k] = 0;
 					}
 				}else{
 					// Will not fall below floor
 					spdy[k] = spdy[k] + -9.8 * step;
 					posy[k] += d;
 				}
 			}
 		}
 		
 		worlds = worldList.toArray( new World[0] );
 	}
 
 	public World[] getWorlds(){
 		return worlds;
 	}
 }
