 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import edu.rit.pj.BarrierAction;
 import edu.rit.pj.IntegerForLoop;
 import edu.rit.pj.IntegerSchedule;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelTeam;
 import edu.rit.pj.reduction.ObjectOp;
 import edu.rit.pj.reduction.SharedObject;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.io.File;
 import java.util.Random;
 
 public class RayTracerSmp{
 	final static Point3d CAMERACENTER = new Point3d( 0.0, 1.0, 2.0 );
 	final static Point3d CAMERALOOKAT = new Point3d( 0.0, 0.0, -1.0 ); // Direction
 	final static Vector3d CAMERAUP = new Vector3d( 0.0, 1.0, 0.0 );
 	
 	final static int THREADS = ParallelTeam.getDefaultThreadCount();
 	
 	private static boolean GUI = false;
 	private static boolean PLAYER = true;
 	
 	private WorldGenerator genWorlds;
 
 	public static void main( String[] args ) throws Exception{
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
 		
 		RayTracerSmp rt = new RayTracerSmp( wg );
 		
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
 		System.err.println( "Usage: java RayTracerSmp [seed=<int>] [world=<generator>] [frames=<int>] [seconds=<int>] [marbes=<int>] [player=<boolean>] [gui=<boolean>]" );
 		System.exit( 1 );
 	}
 
 	/**
 	 * Create a ray tracer using the given world generator
 	 * 
 	 * @param genWorlds World generator
 	 */
 	public RayTracerSmp( WorldGenerator genWorlds ){
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
 		// Setup worlds to render
 		final World[] worlds = genWorlds.getWorlds();
 
 		final JProgressBar main;// = new JProgressBar( 0, worlds.length );
 		final JProgressBar[] bars;// = new JProgressBar[THREADS];
 		
 		if( GUI ){
 			// Setup progress window
 			JFrame frame = new JFrame( "Rendering..." );
 			frame.setBounds( 0, 0, 300, 50 * THREADS + 50 );
 			frame.setLayout( new BorderLayout() );
 			frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 	
 			// Create progress bars
 			main = new JProgressBar( 0, worlds.length );
 			bars = new JProgressBar[THREADS];
 			for( int i = 0; i < bars.length; i++ ){
 				bars[i] = new JProgressBar();
 				bars[i].setValue( 0 );
 				bars[i].setStringPainted( true );
 			}
 			main.setValue( 0 );
 			main.setStringPainted( true );
 			main.setString( "Rendering" );
 	
 			// Setup left panel, with progress bar labels
 			JPanel left = new JPanel();
 			left.setLayout( new GridLayout( THREADS + 1, 1 ) );
 			left.add( new JLabel( "Total:" ) );
 			for( int i = 0; i < bars.length; i++ ){
 				left.add( new JLabel( "Frame:" ) );
 			}
 	
 			// Setup right panel, with progress bars
 			JPanel right = new JPanel();
 			right.setLayout( new GridLayout( THREADS + 1, 1 ) );
 			right.add( main );
 			for( int i = 0; i < bars.length; i++ ){
 				right.add( bars[i] );
 			}
 	
 			// Finalize layout and display
 			frame.add( left, BorderLayout.WEST );
 			frame.add( right, BorderLayout.CENTER );
 			frame.setVisible( true );
 		}else{
 			main = null;
 			bars = null;
 		}
 
 		final SharedObject<JProgressBar> sharedMain = new SharedObject<JProgressBar>( main );
 
 		final long[][] times = new long[worlds.length][];
 		
 		new ParallelTeam().execute( new ParallelRegion(){
 			public void run() throws Exception{
 				final ObjectOp<JProgressBar> op = new ObjectOp<JProgressBar>(){
 					public JProgressBar op( JProgressBar arg0, JProgressBar arg1 ){
 						arg0.setValue( arg0.getValue() + 1 );
 						return arg0;
 					}
 				};
 
 				execute( 0, worlds.length - 1, new IntegerForLoop(){
 
 					public IntegerSchedule schedule()
 					{
 						return IntegerSchedule.guided();
 					}
 
 					public void run( int low, int high ) throws Exception{
 						Camera camera = new Camera( CAMERACENTER, CAMERALOOKAT, CAMERAUP );
 						int index = getThreadIndex();
 
 						// Render all worlds and save to file
 						if( GUI ){
 							for( int i = low; i <= high; i++ ){
 								bars[index].setString( "Frame " + i );
 								bars[index].setValue( 0 );
 								times[i] = camera.render( worlds[i], new File( "render_" + String.format( "%1$04d", i ) + ".png" ), bars[index] );
 								sharedMain.reduce( null, op );
 							}
 						}else{
 							for( int i = low; i <= high; i++ ){
 								times[i] = camera.render( worlds[i], new File( "render_" + String.format( "%1$04d", i ) + ".png" ), null );
 							}
 						}
 
 						if( GUI ){
 							bars[index].setString( "Done!" );
 						}
 					}
 				},
 						BarrierAction.NO_WAIT );
 			}
 		} );
 
 		if( GUI ){
 			main.setString( "Done!" );
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
