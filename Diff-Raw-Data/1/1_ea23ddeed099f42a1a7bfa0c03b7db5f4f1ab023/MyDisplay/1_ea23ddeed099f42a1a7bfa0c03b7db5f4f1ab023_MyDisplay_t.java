 package openGL;
 
 import glWrapper.GLWireframeMesh;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import meshes.WireframeMesh;
 import openGL.gl.GLDisplayable;
 import openGL.gl.GLRenderPanel;
 import openGL.gl.GLRenderer;
 import openGL.interfaces.RenderPanel;
 import openGL.interfaces.SceneManagerIterator;
 import openGL.objects.RenderItem;
 import openGL.objects.Shape;
 import openGL.objects.SimpleSceneManager;
 
 /**
  * A simple  displayer for anything that is {@link GLDisplayable}.
  * 
  * By default it provides zoom (mouse scrolling, hold shift for fast mode),
  * a trackball, near and far plane control 
  * (ctrl-scroll, alt-scroll), and an interface to switch on/off everything
  * on display.
  * @author bertholet
  *
  */
 public class MyDisplay extends JFrame implements ActionListener {
 
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2430061074691026492L;
 	RenderPanel renderPanel;
 	GLRenderer glRenderer;
 
 	SimpleSceneManager sceneManager;
 	TrackballListener trackball;
 	ZoomListener zoomListener;
 	
 	JPanel whatsOnDisplay;
 	
 	float angle;
 	
 	
 	public MyDisplay(){
 		super("Geometry Processing");
 		//the gl display
 		renderPanel = new SimpleRenderPanel();
 		
 		//for object, camera and frustum managament
 		sceneManager = new SimpleSceneManager();
 		whatsOnDisplay = new JPanel();
 		whatsOnDisplay.setLayout( new BoxLayout(whatsOnDisplay, BoxLayout.Y_AXIS));
 		updateWhatsOnDisplay();
 		
 		//add trackball and zoom support
 		trackball = new TrackballListener(this);
 		zoomListener = new ZoomListener(sceneManager, this);
 		renderPanel.getCanvas().addMouseListener(trackball);
 		renderPanel.getCanvas().addMouseMotionListener(trackball);
 		renderPanel.getCanvas().addMouseWheelListener(zoomListener);
 		
 		
 		//layout and stuff
 		this.setSize(700, 500);
 		this.setLocationRelativeTo(null); // center of screen
 		this.getContentPane().setLayout(new BorderLayout());
 		this.getContentPane().add(renderPanel.getCanvas(), BorderLayout.CENTER);
 		this.getContentPane().add(new JScrollPane(whatsOnDisplay), BorderLayout.EAST);
 
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setVisible(true); 
 	}
 
 	
 	/**
 	 * Update the list of objects on display.
 	 */
 	private void updateWhatsOnDisplay() {
 
 		whatsOnDisplay.removeAll();
 		whatsOnDisplay.add(new JLabel("On Display:"));
 		
 		SceneManagerIterator it = sceneManager.iterator();
 		RenderItem item;
 		while(it.hasNext()){
 			item = it.next();
 			JCheckBox box = new JCheckBox(
 					item.getShape().
 					getVertexData().toString(), 
 					item.getShape().isVisible());
 			box.addActionListener(item.getShape());
 			
 			box.addActionListener(this);
 			
 			whatsOnDisplay.add(box);
 		}
 	}
 
 	/**
 	 * Dump a {@link GLDisplayable} object to the display.
 	 * You can  use for example
 	 * {@link GLWireframeMesh} for {@link WireframeMesh}es or write your own
 	 * wrappers for different data structures
 	 * @param object
 	 */
 	public void addToDisplay(GLDisplayable object){
 		Shape s = new Shape(object);
 		sceneManager.addShape(s);
 		trackball.register(s);
 		this.updateWhatsOnDisplay();
 		this.updateDisplay();
 		this.invalidate();
 	}
 	
 	
 	/**
 	 * rerender display.
 	 */
 	public void updateDisplay(){
 		renderPanel.getCanvas().repaint();
		//whatsOnDisplay.validate();
 	}
 	
 	
 	/**
 	 * An extension of {@link GLRenderPanel} to provide
 	 * a JOGL call-back function for OpenGL initialization.
 	 */
 	private class SimpleRenderPanel extends GLRenderPanel {
 		/**
 		 * Initialization call-back. We initialize our renderer here.
 		 * 
 		 * @param r
 		 *            the render context that is associated with this render
 		 *            panel
 		 */
 		@Override
 		public void init(GLRenderer r) {
 			glRenderer = r;
 			glRenderer.setSceneManager(sceneManager);
 		}
 
 		@Override
 		public void reshape(int x, int y, int width, int height) {
 			sceneManager.getFrustum().update((0.f + width)/height);
 		}
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		this.updateDisplay();
 	}
 	
 }
