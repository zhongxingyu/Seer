 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 
 public class WorldAntimatter extends WorldGenerator{
 	private World[] worlds;
 	
     // Charge on an antiproton.
     static final double QP = 3.0;
 
     // Magnetic field strength.
     static final double B = 3.0;
 
     static final double QP_QP = QP * QP;
     static final double QP_B = QP * B;
     
 	
 	final static double back = -10;
 	final static double front = 0;
 	final static double left = -2.25;
 	final static double right = 2.25;
 	final static double bottom = 0.0;
 	final static double top = 3.0;
 	
 	Vector3d pos[];
 	Vector3d spd[];
 	Vector3d acc[];
 	Vector3d temp = new Vector3d();
 	// Total momentum.
 	static Vector3d totalMV = new Vector3d();
 	
 	double dt = 0.00001;
 	double one_half_dt_sqr = 0.5 * dt * dt;
 	
 	double posx[];
 	double posy[];
 	double posz[];
 	
 	double spdx[];
 	double spdz[];
 	
 	public WorldAntimatter( int seconds, int particles ){
 		this( seconds, particles, -1 );
 	}
 	
 	public WorldAntimatter( int seconds, int particles, int seed ){
 		LinkedList<World> worldList = new LinkedList<World>();
 		if( seed < 0 ){
 			seed = new Random().nextInt();
 		}
 		Random r = new Random( seed );
 		System.out.println( "WorldAntimatter( " + seconds + ", " + particles + ", " + seed + " )" );
 		
 		double centerx = 0;
 		double centerz = -2;
 		double centery = 0.2;
 		
 		double radius = 0.1;
 		
 		Color[] colors = new Color[particles*particles];
 		double ka = 0.2;
 		double kd = 0.2;
 		double ks = 0.4;
		double kr = 0.3;
 		double kt = 0.5;
 		int ke = 50;
 
 		// Marble x,y,z positions
 		posx = new double[particles];
 		posy = new double[particles];
 		posz = new double[particles];
 		
 		spdx = new double[particles];
 		spdz = new double[particles];
 		
 		pos = new Vector3d[particles];
 		spd = new Vector3d[particles];
 		acc = new Vector3d[particles];
 		
 		for( int i = 0; i < particles; i++ ){
 			posx[i] = r.nextDouble() * ( right - left ) + left;
 			posy[i] = centery;
 			posz[i] = r.nextDouble() * ( front - back ) + back;
 			System.out.println( posx[i] + " " + posy[i] + " " + posz[i] );
 //			pos[i] = new Vector3d( posx[i], posy[i], posz[i] );
 			pos[i] = new Vector3d( 0, posy[i], -5 );
 			
 			spdx[i] = r.nextDouble() * 5;
 			spdz[i] = r.nextDouble() * 5;
 			spd[i] = new Vector3d( spdx[i], 0, spdz[i] );
 //			spd[i] = new Vector3d( spdx[i], 0, spdz[i] );
 			
 			acc[i] = new Vector3d( 0, 0, 0 );
 			
 			colors[i] = new Color(255,20,147); // Pink
 		}
 		
 		// Set up floor data
 		Point3d PLANEVERTLF = new Point3d( left, bottom, front );
 		Point3d PLANEVERTRF = new Point3d( right, bottom, front );
 		Point3d PLANEVERTRR = new Point3d( right, bottom, back );
 		Point3d PLANEVERTLR = new Point3d( left, bottom, back );
 
 		ArrayList<Point3d> triAVertices = new ArrayList<Point3d>();
 		ArrayList<Point3d> triBVertices = new ArrayList<Point3d>();
 
 		triAVertices.add( PLANEVERTLF );
 		triAVertices.add( PLANEVERTRF );
 		triAVertices.add( PLANEVERTRR );
 
 		triBVertices.add( PLANEVERTLF );
		triBVertices.add( PLANEVERTRR );
		triBVertices.add( PLANEVERTLR );
 		
 		int frame = 0;
 		BufferedReader br = null;
 		try{
 			br = new BufferedReader( new FileReader("antiprotons") );
 		
 //			while( frame < 24 * seconds ){
 			while( br.ready() ){
 				World world = new World();
 				worldList.add( world );
 				
 				// Set up spheres
 				Point3d centers[] = new Point3d[posx.length];
 				for( int k = 0; k < centers.length; k++ ){
 					String[] vals = br.readLine().trim().split(" ");
 //					centers[k] = new Point3d( posx[k], posy[k], posz[k] );
 //					centers[k] = new Point3d( pos[k].x, pos[k].y, pos[k].z );
 					centers[k] = new Point3d( Double.parseDouble(vals[0]) - 5, 0.25, -Double.parseDouble(vals[1]) );
 				}
 				
 				// Set up light
 				Point3d LIGHTCENTER = new Point3d( 0.3, 2.75, 1.0 );
 		
 				// Add objects to world
 				for( int k = 0; k < centers.length; k++ ){
 					world.add( new Sphere( centers[k], radius, colors[k], ka, kd, ks, ke, kr, kt ) );
 				}
                world.add( new Triangle( triAVertices, new Color( 255, 255, 255 ), 0.2, 0.4, 0.6, 20, 0.6, 0.0, 1 ) );
                world.add( new Triangle( triBVertices, new Color( 255, 255, 255 ), 0.2, 0.4, 0.6, 20, 0.6, 0.0, 1 ) );
 		
 				world.add( new PointLight( LIGHTCENTER, new Color( 255.0, 255.0, 255.0 ) ) );
 				
 				
 //				for( int step = 0; step < 100; step++ ){
 //					// Update particle positions
 //					computeAcceleration( centers );
 //					step( centers );
 //				}
 //				
 //				// Compute total momentum.
 //				computeTotalMomentum( centers );
 		
 				
 				frame++;
 			}
 
 		}catch( Exception e ){
 			e.printStackTrace();
 			System.err.println( e.getMessage() );
 		}
 
 		worlds = worldList.toArray( new World[0] );
 	}
 	
 	private void computeAcceleration( Point3d[] spheres ){
 		// Accumulate forces between each pair of antiprotons, but not between an antiproton and itself.
 		for( int i = 0; i < spheres.length; ++ i ){
 			
 			Vector3d a_i = acc[i];
             Vector3d p_i = pos[i];
             
 		    for (int j = 0; j < i; ++ j){
 		    	temp = new Vector3d( p_i );
 		        temp.sub( pos[j] );
 		        
 		        double dsqr = temp.lengthSquared();
 		        temp.scale( QP_QP / ( dsqr * Math.sqrt(dsqr) ) );
 		        a_i.add( temp );
 		    }
 		    for (int j = i+1; j < spheres.length; ++ j){
 		        temp = new Vector3d( p_i );
 		        temp.sub( pos[j] );
 		        double dsqr = temp.lengthSquared();
 		        temp.scale( QP_QP / ( dsqr * Math.sqrt(dsqr) ) );
 		        a_i.add( temp );
 		    }
 		}
     }
 	
 	private void step( Point3d[] spheres ){
 		// Move all antiprotons.
 		for( int i = 0; i < spheres.length; ++ i ){
 			Vector3d a_i = acc[i];
 			Vector3d v_i = spd[i];
 			Vector3d p_i = pos[i];
 			
 			// Accumulate acceleration on antiproton from magnetic field.
 			temp = new Vector3d( v_i );
 			temp.scale( QP_B );
 			temp = new Vector3d( temp.z, temp.y, -temp.x ); // rotate 270
 			a_i.add( temp );
 			
 			// Update antiproton's position and velocity.
 			temp = new Vector3d( v_i );
 			temp.scale(dt);
 			p_i.add( temp );
 			
 			temp = new Vector3d( a_i );
 			temp.scale( one_half_dt_sqr );
 			p_i.add( temp );
 			
 			temp = new Vector3d( a_i );
 			temp.scale( dt );
 			v_i.add( temp );
 			
 			// Clear antiproton's acceleration for the next step.
 			a_i = new Vector3d();
 		}
 	}
 	
 	/**
      * Compute the total momentum for all the antiprotons. The answer is stored
      * in <TT>totalMV</TT>.
      */
     private void computeTotalMomentum( Point3d sphere[] ){
 		totalMV = new Vector3d();
 		for( int i = 0; i < sphere.length; ++ i ){
 			totalMV.add( spd[i] );
 		}
 	}
 
 
 	
 	private static double distance( double x, double y, double z, Point3d sphere ){
 		double dx = x - sphere.x;
 		double dy = y - sphere.y;
 		double dz = z - sphere.z;
 		return Math.sqrt( x*x + y*y + z*z );
 	}
 			
 	public World[] getWorlds() {
 		return worlds;
 	}
 
 }
