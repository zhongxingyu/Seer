 package de.codesourcery.engine.raytracer;
 
import java.awt.*;
import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
import java.io.InputStream;
 import java.text.DecimalFormat;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 public class RayTracerDemo {
 
 	private static final Dimension IMAGE_SIZE = new Dimension(800,800);
 
 	private static final Vector4 DEFAULT_EYE_POSITION = new Vector4( -50.0,-250.0,1900.0 );
 
 	public static final float ANGLE_INCREMENT = 4;
 	
 	public static final float COORD_INCREMENT = 50;
 	
 	public static void main(String[] args) throws Exception {
 
 		final Vector4 eyePosition = new Vector4(DEFAULT_EYE_POSITION);
 		final Camera camera = new Camera();
 		camera.setEyePosition( eyePosition , 0 , 0 );
 
 		final Scene scene = new Scene(camera);
 
 		/**********
 		 * LIGHTS
 		 **********/
 		 // scene.addObject( new PointLightsource( new Vector4( 0,500,500 ) , Color.RED) );
 		scene.addObject( new PointLightsource( new Vector4( -50, 800 , 600 ) , Color.WHITE ) );
 //		scene.addObject( new PointLightsource( new Vector4( -200,0,0 ) , Color.WHITE ) ); 
 
 		/**********
 		 * OBJECTS
 		 **********/
 		final Sphere sphere1 = new Sphere( "sphere #1", new Vector4( -200 ,    -500 , -100 ) , 100 );
 		final Sphere sphere2 = new Sphere( "sphere #2", new Vector4(  250 ,    -500 , -300 ) , 100 );	
 		final Sphere sphere3 = new Sphere( "sphere #3", new Vector4( 50 , -600 , -150 ) , 100 );
 
 		final Plane horizontalPlane = new Plane( "horizontal plane", new Vector4( 0, -600,    0 ) , new Transformation(AffineTransform.rotate(180, 0, 0)) ); // horizontal plane
 		final Plane verticalPlane = new Plane( "vertical plane", new Vector4( 0,    0, -1000 ) , new Transformation() ); // vertical plane
 		final Plane leftPlane = new Plane( "left plane", new Vector4( -300,    0, 0 ) , new Vector4( 100 ,   0 , 0 ) ); // left plane    
 
 		// p4.material.diffuseColor = new Vector4(0,0,0.2);
		
		try ( InputStream in = RayTracerDemo.class.getResourceAsStream( "/checkers.png" ) )
		{
			horizontalPlane.material.texture = Texture.load( in );
		} 
 
 		final AxisAlignedCube cube = new AxisAlignedCube( "cube #1",  new Vector4( -100 , -500 , -600 ) , 200,400,600 );
 
 		final AxisAlignedCube cube2 = new AxisAlignedCube( "cube #2",  new Vector4( 500 , -500 , -200 ) , 200,400,600 );        
 		cube2.transformation( new Transformation( AffineTransform.rotate( 0, 105 , 0 ) , AffineTransform.translate( 1000 , -500 , -300 ) ) );
 
 		//        leftPlane.material.reflectivity(0.5);
 		//        cube1.material.refractionIndex=1.51;
 		//        sphere2.material.glossiness = 0.5;
 
 		sphere3.material.diffuseColor = new Vector4(1,0,0);
 		leftPlane.material.diffuseColor = new Vector4(0,0,1);
 		verticalPlane.material.diffuseColor = new Vector4(0,0.8,0);
 		cube.material.diffuseColor = new Vector4(0.8,0.8,0.3);    
 
 		//        cube2.material.refractionIndex = 1.51;
 		sphere1.material.refractionIndex = 1.31;
 		sphere1.material.isRefractive = true;
 		sphere2.material.diffuseColor = new Vector4(0,0.8,0);		
 		sphere2.material.specularColor = new Vector4(1.0,1.0,1.0);
 		sphere2.material.reflectivity(1.0);
 		//        cube.material.reflectivity(1.0);
 		cube2.material.reflectivity(0.1);
 
 		scene.addObject(cube2);
 		scene.addObject( cube );          
 		scene.addObject( sphere1 ); 
 		scene.addObject( sphere2 ); 
 //		scene.addObject( sphere3 );	
 		scene.addObject( horizontalPlane ); // horizontal
 		        scene.addObject( leftPlane ); // left
 		        scene.addObject( verticalPlane ); // vertical
 
 		final Raytracer tracer = new Raytracer( scene );
 
 		final RenderPanel panel = new RenderPanel(tracer);
 
 		panel.setMinimumSize( IMAGE_SIZE );
 		panel.setPreferredSize( IMAGE_SIZE );		
 
 		final JFrame frame = new JFrame("Tracer V0.1");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
 		frame.getContentPane().setLayout( new GridBagLayout() );
 
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.gridheight=GridBagConstraints.REMAINDER;
 		constraints.gridwidth=GridBagConstraints.REMAINDER;
 		constraints.weightx=1.0;
 		constraints.weighty=1.0;
 
 		frame.getContentPane().add( panel , constraints );
 		frame.pack();
 		frame.setVisible( true );
 		frame.repaint();
 
 		frame.addKeyListener( panel.keyAdapter );
 	}
 
 	protected static final class RenderPanel extends JPanel {
 
 		private final Object IMAGE_LOCK = new Object();
 
 		// @GuardedBy( IMAGE_LOCK )
 		private final AtomicBoolean isCalculating = new AtomicBoolean(false);
 
 		// @GuardedBy( IMAGE_LOCK )        
 		private BufferedImage image = null; 
 
 		public final Raytracer tracer;
 
 		private double bestRaysPerSecond = 0;
 		
 		private final KeyAdapter keyAdapter =  new KeyAdapter() 
 		{
 			@Override
 			public void keyPressed(KeyEvent e) 
 			{
 				synchronized( IMAGE_LOCK ) 
 				{
 					if ( isCalculating.get() ) {
 						return;
 					}
 					handleKeyPress( e );
 				}
 			}
 
 			private void handleKeyPress(KeyEvent e) 
 			{
 				final Camera camera = tracer.scene.camera;
 				switch(e.getKeyCode() ) 
 				{
 					case KeyEvent.VK_ENTER:
 						camera.setEyePosition( DEFAULT_EYE_POSITION , 0 , 0 );
 						break;
 					case KeyEvent.VK_SPACE:
 						if ( tracer.getSamplesPerPixel() == 1 ) {
 							tracer.setSamplesPerPixel(16); 
 						} else {
 							tracer.setSamplesPerPixel(1);
 						}                               
 						break;
 					case KeyEvent.VK_PLUS:
 						camera.moveUp( COORD_INCREMENT );                             
 						break;
 					case KeyEvent.VK_MINUS:
 						camera.moveDown( COORD_INCREMENT );
 						break;
 					case KeyEvent.VK_W:
 						camera.moveForward(COORD_INCREMENT);                              
 						break;
 					case KeyEvent.VK_S:
 						camera.moveBackward(COORD_INCREMENT);
 						break;
 					case KeyEvent.VK_A:
 						camera.strafeLeft(COORD_INCREMENT);
 						break;
 					case KeyEvent.VK_D:
 						camera.strafeRight(COORD_INCREMENT);                              
 						break;
 					case KeyEvent.VK_Q:
 						camera.rotate(-ANGLE_INCREMENT,0);
 						break;
 					case KeyEvent.VK_E:
 						camera.rotate(ANGLE_INCREMENT,0);
 						break;  
 					case KeyEvent.VK_Y:
 						camera.rotate(0,-ANGLE_INCREMENT);
 						break;
 					case KeyEvent.VK_C:
 						camera.rotate(0,ANGLE_INCREMENT);                                
 						break;  
 					case KeyEvent.VK_HOME:
 						try 
 						{
 							final File file = new File("/tmp/trace.png");
 							saveImage( file );
 							System.out.println("Image saved to "+file.getAbsolutePath());
 						} 
 						catch (IOException e1) 
 						{
 							System.err.println("Saving image failed");
 						}
 						break;
 					default:
 						return;
 				}
 				recalculate();                        
 			}
 		};
 
 		public RenderPanel(final Raytracer tracer) 
 		{
 			this.tracer = tracer;
 			addMouseListener( new MouseAdapter() 
 			{
 				@Override
 				public void mouseClicked(MouseEvent e)
 				{
 					if ( e.getButton() == MouseEvent.BUTTON1 ) 
 					{
 						Point point = e.getPoint();
 						System.out.println("CLICKED: "+tracer.getObjectAt( getWidth() , getHeight() , point) );
 					} 
 					else if ( e.getButton() == MouseEvent.BUTTON3 ) 
 					{
 						// map view coordinates to points on view plane
 						final Vector4 pointOnViewPlane = tracer.screenToPointOnViewPlane( getWidth() , getHeight() , e.getPoint().x , e.getPoint().y );
 						final Plane viewPlane = tracer.getViewPlane();
 						synchronized( IMAGE_LOCK ) 
 						{
 							if ( image != null ) {
 								System.out.println("Tracing single ray");					            
 								tracer.tracePrimaryRay( viewPlane , new Vector4(0,0,0) , pointOnViewPlane , image , true );
 								getGraphics().drawImage( image , 0 , 0 , getWidth() , getHeight(), null );					            
 							} else {
 								System.out.println("No image ?");
 							}
 						}
 					}
 				}
 			});			    
 		}
 
 		public void saveImage(File file) throws IOException 
 		{
 			BufferedImage tmp;
 			do {
 				synchronized( IMAGE_LOCK ) 
 				{
 					tmp = image;
 				}
 
 				if ( tmp != null ) {
 					break;
 				}
 
 				trace( getWidth() , getHeight() , true );
 
 				synchronized( IMAGE_LOCK ) 
 				{
 					tmp = image;
 				}
 				if ( tmp == null ) {
 					System.err.println("No image ???");
 					return;
 				}				
 			} while( true );
 
 			ImageIO.write( tmp , "PNG" , file );
 		}
 
 		public void recalculate() 
 		{
 			trace(getWidth() , getHeight() , false );
 		}
 		
 		private void trace(final int w,final int h,boolean waitForCompletion) 
 		{
 			synchronized( IMAGE_LOCK) 
 			{
 				if ( ! isCalculating.compareAndSet( false, true ) )
 				{
 					return;
 				}
 			}
 
 			final CountDownLatch latch = new CountDownLatch(1);
 			final Thread tracerThread = new Thread() 
 			{
 				public void run() 
 				{
 					System.out.print("Tracing "+w+" x "+h+" ... ");	
 					long time = -System.currentTimeMillis();
 					double raysPerSecond = 0;
 					try 
 					{
 						final BufferedImage newImage = tracer.trace( w , h );
 						time += System.currentTimeMillis();
 						synchronized( IMAGE_LOCK ) 
 						{
 							long rays = tracer.getSamplesPerPixel()*w*h;							
 							raysPerSecond = rays / (time/1000.0);		
 							if ( raysPerSecond > bestRaysPerSecond ) {
 								bestRaysPerSecond = raysPerSecond;
 							}
 							image = newImage;
 						}
 
 						SwingUtilities.invokeAndWait( new Runnable() 
 						{
 							public void run() 
 							{
 								RenderPanel.this.repaint();										
 							}
 						}
 								);								
 					} catch (Exception e) {
 						e.printStackTrace();
 					} 
 					finally 
 					{
 						final DecimalFormat DF = new DecimalFormat("###,###,###,##0");
 						System.out.println( time+" millis | "+DF.format(raysPerSecond)+" primary rays/s , best: "+DF.format(bestRaysPerSecond)+" primary rays/s ]");
 						latch.countDown();		
 						synchronized( IMAGE_LOCK) 
 						{
 							isCalculating.set( false );
 						}
 					}
 				}
 			};
 			tracerThread.start();
 
 			if ( waitForCompletion ) 
 			{
 				try 
 				{
 					latch.await();
 				} catch (InterruptedException e) {
 					Thread.currentThread().interrupt();
 				}
 			}					
 		}
 		@Override
 		public void paint(Graphics g) 
 		{
 			super.paint(g);
 
 			final int w = getWidth();
 			final int h = getHeight();
 
 			final BufferedImage img;
 			synchronized( IMAGE_LOCK ) 
 			{
 				img = image;
 			}
 
 			if ( img == null || img.getWidth() != w || img.getHeight() != h ) 
 			{
 				trace(w,h,false);
 			} 
 			else 
 			{
 				g.drawImage( img , 0 , 0 , w , h , null );
 			}
 		}
 	}
 }
