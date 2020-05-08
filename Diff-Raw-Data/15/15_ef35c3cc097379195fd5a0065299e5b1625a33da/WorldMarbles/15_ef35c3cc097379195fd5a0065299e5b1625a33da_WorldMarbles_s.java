 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 public class WorldMarbles extends WorldGenerator{
 
 	World[] worlds;
 
 	final static double step = 1.0 / 24.0;
 
 	final static double back = -10;
 	final static double front = 0;
 	final static double left = -2.25;
 	final static double right = 2.25;
 	final static double bottom = 0.0;
 	final static double top = 3.0;
 
 	final static double diminish = 0.9; // How much speed drops when a wall is hit
 
 
 	/**
 	 * Generates worlds of marbles to render
 	 * 
 	 * @param seconds
 	 *            Number of seconds in animation
 	 */
 	public WorldMarbles( int seconds ){
 		this( seconds, -1, false );
 	}
 
 
 	/**
 	 * Generates worlds of marbles to render
 	 * 
 	 * @param seconds
 	 *            Number of seconds in animation
 	 * @param colorful
 	 *            True to use colorful marbles, false to use transparent ones
 	 */
 	public WorldMarbles( int seconds, boolean colorful ){
 		this( seconds, -1, colorful );
 	}
 
 	/**
 	 * Generates worlds of marbles to render
 	 * 
 	 * @param seconds
 	 *            Number of seconds in animation
 	 * @param marbles
 	 *            Number of marbles to generate
 	 */
 	public WorldMarbles( int seconds, int marbles ){
 		this( seconds, marbles, false );
 	}
 
 
 	/**
 	 * Generates worlds of marbles to render
 	 * 
 	 * @param seconds
 	 *            Number of seconds in animation
 	 * @param marbles
 	 *            Number of marbles to generate
 	 * @param colorful
 	 *            True to use colorful marbles, false to use transparent ones
 	 */
 	public WorldMarbles( int seconds, int marbles, boolean colorful ){
 		worlds = new World[24 * seconds];
 		Random r = new Random();
 
 		// Marble x,y,z positions
 		double posx[] = new double[]{ -0.4, 0.25, 0.0 };
 		double posy[] = new double[]{ 1.2, 0.95, 2.0 };
 		double posz[] = new double[]{ -1.5, -1.0, -7.0 };
 
 		// Marble radii
 		double radii[] = new double[]{ 0.4, 0.466, 0.2 };
 
 		// Marble x,y,z speeds
 		double spdx[] = new double[]{ 0, 0, 0 };
 		double spdy[] = new double[]{ -5, 0, -10 };
 		double spdz[] = new double[]{ 0, 0, 0 };
 
 		// Marble colors
 		Color colors[] = new Color[]{
 				new Color( 178.5, 178.5, 178.5 ),
 				new Color( 50.0, 50.0, 50.0 ),
 				new Color( 255.0, 0.0, 0.0 ) };
 
 		// Marble kr and kt values
 		double ka[] = new double[]{ 0.2, 0.2, 0.2 };
 		double kd[] = new double[]{ 0.2, 0.2, 0.2 };
 		double ks[] = new double[]{ 0.4, 0.4, 0.4 };
 		double kr[] = new double[]{ 1.0, 0.0, 0.0 };
 		double kt[] = new double[]{ 0.0, 1.0, 1.0 };
 		int ke[]    = new int[]{ 20, 50, 100 };
 
 		if( marbles > 0 ){
 			// Generates random marbles
 			posx = new double[marbles];
 			posy = new double[marbles];
 			posz = new double[marbles];
 			radii = new double[marbles];
 			spdx = new double[marbles];
 			spdy = new double[marbles];
 			spdz = new double[marbles];
 			colors = new Color[marbles];
 			ka = new double[marbles];
 			kd = new double[marbles];
 			ks = new double[marbles];
 			ke = new int[marbles];
 			kr = new double[marbles];
 			kt = new double[marbles];
 			for( int i = 0; i < marbles; i++ ){
 				radii[i] = (r.nextDouble() / 3 + 0.1);
 				posx[i] = r.nextDouble() * (right - left - 2 * radii[i]) + left + radii[i];
 				posy[i] = r.nextDouble() * (top - bottom - 2 * radii[i]) + bottom + radii[i];
 				posz[i] = r.nextDouble() * (front - back - 2 * radii[i]) + back + radii[i];
				spdx[i] = (r.nextDouble() * 5) - 5;
				spdy[i] = (r.nextDouble() * 5) - 5;
				spdz[i] = (r.nextDouble() * 5) - 5;
 				ka[i] = r.nextDouble() / 2 + 0.1;
 				kd[i] = r.nextDouble() / 2 + 0.1;
 				ks[i] = r.nextDouble() / 2 + 0.1;
 				ke[i] = r.nextInt( 80 ) + 20;
 				if( colorful ){
 					colors[i] = new Color( r.nextDouble() * 500, r.nextDouble() * 500, r.nextDouble() * 500 );
 					kr[i] = r.nextDouble() / 4;
 					// kr[i] = 0;
 					kt[i] = r.nextDouble() / 4;
 					// kt[i] = 0;
 				}else{
 					colors[i] = new Color( r.nextDouble() * 50, r.nextDouble() * 50, r.nextDouble() * 50 );
 					kr[i] = 0;
 					kt[i] = 1;
 				}
 			}
 		}
 
 		// Loop to generate each world scene
 		int shaderIndex = r.nextInt( 4 );
 		for( int i = 0; i < worlds.length; i++ ){
 			World world = new World();
 			worlds[i] = world;
 
 			// Set up spheres
 			Point3d centers[] = new Point3d[posx.length];
 			for( int k = 0; k < centers.length; k++ ){
 				centers[k] = new Point3d( posx[k], posy[k], posz[k] );
 			}
 
 			// Set up floor
 			ArrayList<Point3d> triAVertices = new ArrayList<Point3d>();
 			ArrayList<Point3d> triBVertices = new ArrayList<Point3d>();
 			ArrayList<Point3d> triCVertices = new ArrayList<Point3d>();
 			ArrayList<Point3d> triDVertices = new ArrayList<Point3d>();
 
 			Point3d PLANEVERTLF = new Point3d( left, bottom, front );
 			Point3d PLANEVERTRF = new Point3d( right, bottom, front );
 			Point3d PLANEVERTRR = new Point3d( right, bottom, back );
 			Point3d PLANEVERTLR = new Point3d( left, bottom, back );
 
 			Point3d LEFTPLANEVERTLFB = new Point3d( left, bottom, front );
 			Point3d LEFTPLANEVERTRRB = new Point3d( left, bottom, back );
 			Point3d LEFTPLANEVERTLFT = new Point3d( left, top, front );
 			Point3d LEFTPLANEVERTRRT = new Point3d( left, top, back );
 
 			triAVertices.add( PLANEVERTLF );
 			triAVertices.add( PLANEVERTRF );
 			triAVertices.add( PLANEVERTRR );
 
 			triBVertices.add( PLANEVERTLR );
 			triBVertices.add( PLANEVERTRR );
 			triBVertices.add( PLANEVERTLF );
 
 			triCVertices.add( LEFTPLANEVERTLFB );
 			triCVertices.add( LEFTPLANEVERTRRB );
 			triCVertices.add( LEFTPLANEVERTLFT );
 
 			triDVertices.add( LEFTPLANEVERTLFT );
 			triDVertices.add( LEFTPLANEVERTRRB );
 			triDVertices.add( LEFTPLANEVERTRRT );
 
 			// Set up light
 			Point3d LIGHTCENTER = new Point3d( 0.3, 2.75, 1.0 );
 
 			// Add objects to world
 			for( int k = 0; k < centers.length; k++ ){
 				world.add( new Sphere( centers[k], radii[k], colors[k], ka[k], kd[k], ks[k], ke[k], kr[k], kt[k] ) );
 				// world.add( new Sphere( centers[k], radii[k], colors[k], kr[k], kt[k] ) );
 			}
			world.add( new Triangle( triAVertices, new Vector3d( 0, 1, 0 ), new Color( 0, 0, 0 ), shaderIndex ) );
			world.add( new Triangle( triBVertices, new Vector3d( 0, 1, 0 ), new Color( 0, 0, 0 ), shaderIndex ) );
			world.add( new Triangle( triCVertices, new Vector3d( 0, 1, 0 ), new Color( 0, 0, 0 ), shaderIndex ) );
			world.add( new Triangle( triDVertices, new Vector3d( 0, 1, 0 ), new Color( 0, 0, 0 ), shaderIndex ) );
 
 			world.add( new PointLight( LIGHTCENTER, new Color( 255.0, 255.0, 255.0 ) ) );
 
 			// Update object positions
 			for( int k = 0; k < centers.length; k++ ){
 
 				// Update ypos
 				spdy[k] -= 9.8 * step;
 				posy[k] += spdy[k] * step;
 				if( posy[k] + radii[k] > top ){
 					spdy[k] = -spdy[k] * diminish;
 					posy[k] = 2 * top - 2 * radii[k] - posy[k];
 				}else if( posy[k] - radii[k] < bottom ){
 					posy[k] -= spdy[k] * step; // Undo previous move
 
 					// Compute drop
 					double d = posy[k] - radii[k] - bottom;
 					double vf = -Math.sqrt( spdy[k] * spdy[k] - 2 * -9.8 * d );
 					double t = (vf - spdy[k]) / -9.8;
 
 					// Compute rebound
 					t = step - t;
 					spdy[k] = -vf * diminish;
 					vf = spdy[k] + -9.8 * t;
 					d = (spdy[k] + vf) / 2 * t;
 
 					// Set new pos and speed
 					posy[k] = bottom + radii[k] + d;
 					spdy[k] = vf;
 
 					// Check threshold to stop bouncing
 					if( posy[k] - radii[k] - bottom < .02 && spdy[k] < .5 ){
 						posy[k] = bottom + radii[k];
 						spdy[k] = 0;
 					}
 				}
 
 				// Update xpos
 				posx[k] += spdx[k] * step;
 				if( posx[k] + radii[k] > right ){
 					spdx[k] = -spdx[k] * diminish;
 					// posx[k] = right - radii[k] - posx[k] - radii[k] + right;
 					posx[k] = 2 * right - 2 * radii[k] - posx[k];
 				}else if( posx[k] - radii[k] < left ){
 					spdx[k] = -spdx[k] * diminish;
 					// posx[k] = left + radii[k] - posx[k] + radii[k] + left;
 					posx[k] = 2 * left + 2 * radii[k] - posx[k];
 				}
 
 				// Update zpos
 				posz[k] += spdz[k] * step;
 				if( posz[k] + radii[k] > front ){
 					spdz[k] = -spdz[k] * diminish;
 					posz[k] = 2 * front - 2 * radii[k] - posz[k];
 				}else if( posz[k] - radii[k] < back ){
 					spdz[k] = -spdz[k] * diminish;
 					posz[k] = 2 * back + 2 * radii[k] - posz[k];
 				}
 			}
 		}
 	}
 
 	public World[] getWorlds(){
 		return worlds;
 	}
 }
