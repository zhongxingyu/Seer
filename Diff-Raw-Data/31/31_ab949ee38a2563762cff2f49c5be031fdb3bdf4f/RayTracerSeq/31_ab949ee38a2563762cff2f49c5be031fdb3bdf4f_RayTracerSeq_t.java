 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.io.File;
 
 public class RayTracerSeq {
 	final static Point3d CAMERACENTER = new Point3d(0.0, 1.0, 2.0);
 	final static Point3d CAMERALOOKAT = new Point3d(0.0, 0.0, -1.0); //Direction
 	final static Vector3d CAMERAUP = new Vector3d(0.0, 1.0, 0.0);
 	
 	private WorldGenerator genWorlds;
 	
 	public static void main(String[] args) throws Exception {
 		
		WorldGenerator gen = new WorldMarbleGrid( 10, WorldMarbleGrid.DIAGONAL, 5 );
//	    WorldGenerator gen = new WorldMarbleGrid( 10, WorldMarbleGrid.DIAGONAL );

		int frames = gen.getWorlds().length;
		
		RayTracerSeq rt = new RayTracerSeq( gen);

 		
 		rt.cleanup();
 	    long startTime = System.currentTimeMillis();
 		rt.render();
 		long runTime = System.currentTimeMillis() - startTime;
 		System.out.println("Time: " + runTime + "msec");
 		System.out.println( "Time / frame: " + ( runTime / frames ) );
 	
 		System.exit(0);
 		PlayMovie.main( new String[]{} );
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
 		
 		if( false ){
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
 				camera.render( worlds[i], new File( "render_" + String.format( "%1$04d" , i) + ".png" ), bar );
 				main.setValue( i+1 );
 			}
 	
 			bar.setString("Done!");
 			main.setString("Done!");
 		}else{
 			// No GUI
 			
 			// Render all worlds and save to file
 			for( int i = 0; i < worlds.length; i++ ){
 				camera.render( worlds[i], new File( "render_" + String.format( "%1$04d" , i) + ".png" ), null );
 			}
 		}
 
 	}
 
 } 
