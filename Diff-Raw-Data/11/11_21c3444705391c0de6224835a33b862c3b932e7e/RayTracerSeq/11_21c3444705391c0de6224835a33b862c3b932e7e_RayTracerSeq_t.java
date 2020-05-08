 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.io.File;
 import java.util.Random;
 
 public class RayTracerSeq {
 	final static Point3d CAMERACENTER = new Point3d(0.0, 1.0, 2.0);
 	final static Point3d CAMERALOOKAT = new Point3d(0.0, 0.0, -1.0); //Direction
 	final static Vector3d CAMERAUP = new Vector3d(0.0, 1.0, 0.0);
 	
 	private static boolean GUI = false;
 	private static boolean PLAYER = true;
 	
 	private WorldGenerator genWorlds;
 	
 	public static void main(String[] args) throws Exception {
 		int seed = new Random().nextInt();
 		WorldGenerator wg = null;
 		int frames = 5*24;
 		int marbles = 5;
 		String world = "marbles";
 		
 		if( args.length == 1 && ( args[0].contains( "help" ) || args[0].equals( "-h" ) ) ){
 			usage();
 		}
 		
 		for( int i = 0; i < args.length; i++ ){
 			args[i] = args[i].toLowerCase();
 			
 			try{
 				if( args[i].startsWith( "seed" ) ){
 					seed = Integer.parseInt( args[i].substring( 5 ) );
 					
 				}else if( args[i].startsWith( "frames" ) ){
 					frames = Integer.parseInt( args[i].substring( 7 ) );
 					
 				}else if( args[i].startsWith( "seconds" ) ){
 					frames = Integer.parseInt( args[i].substring( 8 ) ) * 24;
 					
 				}else if( args[i].startsWith( "world" ) ){
 					world = args[i].substring( 6 );
 					if( world.startsWith( "world" ) ){
 						world = world.substring( 5 );
 					}
 					
 				}else if( args[i].startsWith( "marbles" ) ){
 					marbles = Integer.parseInt( args[i].substring( 8 ) );
 					
 				}else if( args[i].startsWith( "gui" ) ){
 					try{
 						GUI = Integer.parseInt( args[i].substring( 4 ) ) != 0;
 					}catch( Exception e ){
 						try{
 							GUI = Boolean.parseBoolean( args[i].substring( 4 ) );
 						}catch( Exception ex ){
 							System.err.println( "Argument: \"" + args[i].substring( 4 ) + "\" for 'gui' unknown. Ignored." );
 						}
 					}
 					
 				}else if( args[i].startsWith( "player" ) ){
 					try{
 						PLAYER = Integer.parseInt( args[i].substring( 7 ) ) != 0;
 					}catch( Exception e ){
 						try{
 							PLAYER = Boolean.parseBoolean( args[i].substring( 7 ) );
 						}catch( Exception ex ){
 							System.err.println( "Value: \"" + args[i].substring( 7 ) + "\" for 'player' unknown. Ignored." );
 						}
 					}
 				}else{
 					System.err.println( "Argument: \"" + args[i] + "\" unknown. Ignored." );
 				}
 			}catch( Exception e ){
 				System.err.println( "Argument: " + args[i] + " not understood. Ignored." );
 			}
 		}
 		
 		if( world.equals( "marbles" ) ){
 			wg = new WorldMarbles( frames, marbles, true, seed );
 		}else if( world.equals( "antimatter" ) ){
 			wg = new WorldAntimatter( frames, marbles, seed );
 		}else if( world.equals( "collide" ) ){
 			wg = new WorldCollide( frames, marbles, true, seed );
 		}else if( world.equals( "marblegrid" ) ){
 			wg = new WorldMarbleGrid( marbles, WorldMarbleGrid.DIAGONAL, frames );
 		}else{
 			usage();
 		}
 		
		RayTracerSeq rt = new RayTracerSeq( wg );
 
 		
 		rt.cleanup();
 	    long startTime = System.currentTimeMillis();
 		rt.render();
 		long runTime = System.currentTimeMillis() - startTime;
 		System.out.println("Time: " + runTime + "msec");
 		System.out.println( "Time / frame: " + ( runTime / frames ) );
 	
 		if( PLAYER ){
 			PlayMovie.main( new String[]{} );
 		}
 	}
 	
 	private static void usage(){
 		System.err.println( "Usage: java RayTracerSeq [seed=<int>] [world=<generator>] [frames=<int>] [seconds=<int>] [marbes=<int>] [player=<boolean>] [gui=<boolean>]" );
 		System.exit( 1 );
 	}
 	
 	/**
 	 * Create a ray tracer using the given world generator
 	 * 
 	 * @param genWorlds World generator
 	 */
 	public RayTracerSeq( WorldGenerator genWorlds ){
 		this.genWorlds = genWorlds;
 	}
 	
 	public void cleanup(){
 		// Erase previous render
 		File[] files = new File( "." ).listFiles();
 		try{
 			for( int i = 0; i < files.length; i++ ){
 				if( files[i].getName().endsWith( ".png" ) ){
 					files[i].delete();
 				}
 			}
 		}catch( Exception e ){
 			System.exit( 1 );
 		}
 	}
 	
 	/**
 	 * Renders the Worlds produced by the world Generator
 	 * 
 	 * @throws Exception
 	 */
 	public void render() throws Exception{
 		//Camera
 		final Camera camera = new Camera(CAMERACENTER, CAMERALOOKAT, CAMERAUP);
 		
 		final World[] worlds = genWorlds.getWorlds();
 		
 		long[][] times = new long[worlds.length][];
 		
 		if( GUI ){
 			// Use GUI
 		
 			// Setup progress window
 			JFrame frame = new JFrame("Rendering...");
 			frame.setBounds(0, 0, 300, 100);
 			frame.setLayout( new BorderLayout() );
 			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			
 			// Create progress bars
 			final JProgressBar main = new JProgressBar(0, worlds.length);
 			final JProgressBar bar = new JProgressBar();
 			main.setValue(0);
 			bar.setValue(0);
 			main.setStringPainted(true);
 			bar.setStringPainted(true);
 			main.setString("Rendering");
 			
 			// Setup left panel, with progress bar labels
 			JPanel left = new JPanel();
 			left.setLayout( new GridLayout(2,1) );
 			left.add( new JLabel("Total:" ) );
 			left.add( new JLabel("Frame:" ) );
 			
 			// Setup right panel, with progress bars
 			JPanel right = new JPanel();
 			right.setLayout( new GridLayout(2,1) );
 			right.add( main );
 			right.add( bar );
 			
 			// Finalize layout and display
 			frame.add( left, BorderLayout.WEST );
 			frame.add( right, BorderLayout.CENTER );
 			frame.setVisible(true);
 			
 			// Render all worlds and save to file
 			for( int i = 0; i < worlds.length; i++ ){
 				bar.setString( "Frame " + i );
 				bar.setValue(0);
 				times[i] = camera.render( worlds[i], new File( "render_" + String.format( "%1$04d" , i) + ".png" ), bar );
 				main.setValue( i+1 );
 			}
 	
 			bar.setString("Done!");
 			main.setString("Done!");
 		}else{
 			// No GUI
 			
 			// Render all worlds and save to file
 			for( int i = 0; i < worlds.length; i++ ){
 				times[i] = camera.render( worlds[i], new File( "render_" + String.format( "%1$04d" , i) + ".png" ), null );
 			}
 		}
 		
 		int renderTime = 0;
 		int ioTime = 0;
 		int ttlTime = 0;
 		for( int i = 0; i < times.length; i++ ){
 			renderTime += times[i][0];
 			ioTime += times[i][1];
 			ttlTime += times[i][0] + times[i][1];
 		}
 		
 		renderTime /= times.length;
 		ioTime /= times.length;
 		ttlTime /= times.length;
 		
 		System.out.println( "Average Frame Render Time: " + renderTime );
 		System.out.println( "Average Frame I/O Time   : " + ioTime );
 		System.out.println( "Average Frame Total Time : " + ttlTime );
 
 	}
 
 } 
