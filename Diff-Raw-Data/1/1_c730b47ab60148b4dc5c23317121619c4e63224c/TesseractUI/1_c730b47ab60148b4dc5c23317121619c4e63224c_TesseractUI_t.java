 package tesseract;
 
 import java.awt.GraphicsConfiguration;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import javax.media.j3d.BoundingBox;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.Node;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import tesseract.forces.AirDrag;
 import tesseract.forces.CircularXY;
 import tesseract.forces.CircularXZ;
 import tesseract.forces.Force;
 import tesseract.forces.Gravity;
 import tesseract.forces.LinearOrigin;
 import tesseract.forces.QuadradicOrigin;
 import tesseract.newmenu.NewChainLinkMenuItem;
 import tesseract.newmenu.NewEllipsoidMenuItem;
 import tesseract.newmenu.NewIcosahedronMenuItem;
 import tesseract.newmenu.NewParticleEmitterMenuItem;
 import tesseract.newmenu.NewParticleMenuItem;
 import tesseract.newmenu.NewPlanarPolygonMenuItem;
 import tesseract.newmenu.NewSurfBoardMenuItem;
 import tesseract.newmenu.NewToroidMenuItem;
 import tesseract.objects.Box;
 import tesseract.objects.ChainLink2;
 import tesseract.objects.PhysicalObject;
 import tesseract.objects.Sphere;
 
 import com.sun.j3d.utils.picking.PickCanvas;
 import com.sun.j3d.utils.picking.PickResult;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 
 /**
  * This class is the main UI for the Tesseract Project.
  * 
  * @author Jesse Morgan
  */
 public class TesseractUI extends JFrame {
 	
 	/**
 	 * Generated serialVersionUID.
 	 */
 	private static final long serialVersionUID = 4097744746899308736L;
 	
 	/**
 	 * Update Rate.
 	 */
 	private static final int UPDATE_RATE = 30;
 	
 	/**
 	 * Measure of 1 unite of space in the world.
 	 */
 	private static final float UNIT = 1;
 
 	/**
 	 * Number of miliseconds in 1 second.
 	 */
 	private static final int MILISECONDS_IN_SECOND = 1000;
 	
 	/**
 	 * A reference to the world.
 	 */
 	private World myWorld;
 	
 	/**
 	 * The Canvas.
 	 */
 	private Canvas3D myCanvas;
 
 	/**
 	 * Camera TransformGroup.
 	 */
 	private TransformGroup cameraTG;
 	
 	/**
 	 * Camera position information.
 	 */
 	private double cameraXRotation, cameraYRotation, cameraDistance;
 	
 	/**
 	 * Object Menu Items.
 	 */
 	private JMenuItem[] myObjectMenuItems;
 	
 	/**
 	 * World Timer.
 	 */
 	private Timer myTimer;
 	
 	/**
 	 * Currently selected object.
 	 */
 	private PhysicalObject myCurrentObject;
 	
 	/**
 	 * UI Constructor.
 	 */
 	public TesseractUI() {
 		super("Tesseract Project");
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
 		
 		myWorld = new World(
 				new BoundingBox(new Point3d(-UNIT / 2, -UNIT / 2, -UNIT / 2), 
 						new Point3d(UNIT / 2, UNIT / 2, UNIT / 2)));
 		
 		myCurrentObject = null;
 		
 		myObjectMenuItems = new JMenuItem[] {
 				new NewParticleEmitterMenuItem(myWorld),
 				new NewParticleMenuItem(myWorld),
 				new NewPlanarPolygonMenuItem(myWorld),
 				new NewEllipsoidMenuItem(myWorld),
 				new NewIcosahedronMenuItem(myWorld),
 				new NewChainLinkMenuItem(myWorld),
 				new NewToroidMenuItem(myWorld),
 				new NewSurfBoardMenuItem(myWorld)
 		};
 		createMenu();
 		setupCanvas();
 		pack();
 		
 		// Maximize the windows
 		if (Toolkit.getDefaultToolkit().
 				isFrameStateSupported(JFrame.MAXIMIZED_BOTH)) {
 			setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
 		}
 		
 		// THIS IS WHERE OBJECTS ARE FORCED INTO EXISTANCE
 		// TODO: REMOVE TEST CODE
 		
 		// Lookie! Linked chainlinks!
 		//myWorld.addObject(new ChainLink2(new Vector3f(0.15f, 0, 0), 1));
 		//ChainLink2 o = new ChainLink2(new Vector3f(), 1);
 		//o.setRotation();
 		
 		//myWorld.addForce(new AirDrag());
 		
 		//World.addObject(new Box(0.18f, 0.1f, 0.25f, new Vector3f(0.1f, -0.10f, 0)));
 		//myWorld.addObject(new Box(0.18f, 0.25f, 0.1f, new Vector3f(-0.1f, 0, 0)));
 		PhysicalObject s = new Sphere(.05f, new Vector3f());
 		s.setAngularVelocity(new Vector3f(0, 2, 0));
 		myWorld.addObject(s);
 		
 		//myWorld.addObject(o);
 	}
 	
 	/**
 	 * Create the menu.
 	 */
 	private void createMenu() {
 		JMenuBar menuBar = new JMenuBar();
 		// Added by Steve: Fixes viewing menu problem with Canvas3D on both my windows machines
 		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
 		
 		//Simulator
 		JMenu simulationMenu = new JMenu("Simulation");
 		menuBar.add(simulationMenu);
 		// Simulator Start/Stop
 		JMenuItem runSim = new JCheckBoxMenuItem("Run Simulator", true);
 		runSim.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
 					myTimer.start();
 				
 				} else {
 					myTimer.stop();
 				}
 			}
 		});
 		simulationMenu.add(runSim);
 		
 		
 		//Objects
 		JMenu objectsMenu = new JMenu("Add Object");
 		for (JMenuItem item : myObjectMenuItems) {
 			objectsMenu.add(item);
 		}
 		menuBar.add(objectsMenu);
 		
 		//Forces
 		JMenu forcesMenu = new JMenu("Add Forces");
 		final JMenuItem gravity = new JCheckBoxMenuItem("Gravity", false);
 		gravity.addActionListener(new ActionListener() {
 				private Force me;
 				
 				// Constructor 
 				{
 					me = new Gravity();
 				}
 				
 				public void actionPerformed(ActionEvent e) {
 					if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
 						myWorld.addForce(me);
 					} else {
 						myWorld.removeForce(me);
 					}
 				}			
 			});
 		forcesMenu.add(gravity);
 
 		final JMenuItem circularXZ = new JCheckBoxMenuItem("Tangential force in the XZ plane", false);
 		circularXZ.addActionListener(new ActionListener() {
 				private Force me;
 				
 				// Constructor 
 				{
 					me = new CircularXZ(.5f);
 				}
 				
 				public void actionPerformed(ActionEvent e) {
 					if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
 						myWorld.addForce(me);
 					} else {
 						myWorld.removeForce(me);
 					}
 				}			
 			});
 		forcesMenu.add(circularXZ);
 		
 		final JMenuItem circularXY = new JCheckBoxMenuItem("Tangential force in the XY plane", false);
 		circularXY.addActionListener(new ActionListener() {
 				private Force me;
 				
 				// Constructor 
 				{
 					me = new CircularXY(.5f);
 				}
 				
 				public void actionPerformed(ActionEvent e) {
 					if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
 						myWorld.addForce(me);
 					} else {
 						myWorld.removeForce(me);
 					}
 				}			
 			});
 		forcesMenu.add(circularXY);
 		
 		final JMenuItem originLinear = new JCheckBoxMenuItem("Linear proportional force towards the origin", false);
 		originLinear.addActionListener(new ActionListener() {
 				private Force me;
 				
 				// Constructor 
 				{
 					me = new LinearOrigin(.5f);
 				}
 				
 				public void actionPerformed(ActionEvent e) {
 					if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
 						myWorld.addForce(me);
 					} else {
 						myWorld.removeForce(me);
 					}
 				}			
 			});
 		forcesMenu.add(originLinear);
 		
 		final JMenuItem originQuadradic = new JCheckBoxMenuItem("Quadratic proportional force towards the origin", false);
 		originQuadradic.addActionListener(new ActionListener() {
 				private Force me;
 				
 				// Constructor 
 				{
 					me = new QuadradicOrigin(.5f);
 				}
 				
 				public void actionPerformed(ActionEvent e) {
 					if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
 						myWorld.addForce(me);
 					} else {
 						myWorld.removeForce(me);
 					}
 				}			
 			});
 		forcesMenu.add(originQuadradic);
 		
 		final JMenuItem airDrag = new JCheckBoxMenuItem("Air Drag", false);
 		airDrag.addActionListener(new ActionListener() {
 				private Force me;
 				
 				// Constructor 
 				{
 					me = new AirDrag();
 				}
 				
 				public void actionPerformed(ActionEvent e) {
 					if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
 						myWorld.addForce(me);
 					} else {
 						myWorld.removeForce(me);
 					}
 				}			
 			});
 		forcesMenu.add(airDrag);
 		
 		menuBar.add(forcesMenu);
 		
 		// Add reset Simulator menu item
 		JMenuItem resetSim = new JMenuItem("Reset Simulator");
 		resetSim.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				myWorld.resetWorld();
 				gravity.setSelected(false);
 				circularXZ.setSelected(false);
 				circularXY.setSelected(false);
 				originLinear.setSelected(false);
 				originQuadradic.setSelected(false);
				airDrag.setSelected(false);
 			}
 		});
 		simulationMenu.add(resetSim);
 		
 		// Exit Menu Item
 		JMenuItem exit = new JMenuItem("Exit");
 		exit.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				// TODO: I feel this is the wrong way of exiting...
 				System.exit(0);
 			}
 		});
 		simulationMenu.add(exit);
 		
 		setJMenuBar(menuBar);
 	}
 	
 	/**
 	 * Create and show the UI.
 	 */
 	private void setupCanvas() {
 		GraphicsConfiguration config
 			= SimpleUniverse.getPreferredConfiguration();
 		
 		myCanvas = new Canvas3D(config);
 		
 		SimpleUniverse universe = new SimpleUniverse(myCanvas);
 		universe.getViewer().getView().setSceneAntialiasingEnable(true);
 
 		// Set the camera
 		cameraTG = universe.getViewingPlatform().getViewPlatformTransform();
 		cameraDistance = 3 * UNIT;
 		updateCamera();
 		
 		// Add the scene BG.
 		universe.addBranchGraph(myWorld.getScene());
 		
 		// Add the canvas to the frame.
 		add(myCanvas);
 		
 		// Test Picking
 		final PickCanvas pc = new PickCanvas(myCanvas, myWorld.getScene());
 		pc.setMode(PickCanvas.GEOMETRY);
 		
 		myCanvas.addMouseListener(new MouseAdapter() {
 			public void mousePressed(final MouseEvent e) {
 				pc.setShapeLocation(e);
 				PickResult r = pc.pickClosest();
 				
 				if (r != null) {
 					if (r.getObject().getUserData() instanceof PhysicalObject) {
 						myCurrentObject = 
 							(PhysicalObject) r.getObject().getUserData();
 						myCurrentObject.selected(true);
 						
 					} else {
 						// The PhysicalObject was not found in the selected
 						// object... Check parents.
 						for (int i = r.getSceneGraphPath().nodeCount() - 1;
 							i >= 0; i--) {
 							Node n = r.getSceneGraphPath().getNode(i); 
 							if (n.getUserData() instanceof PhysicalObject) {
 								myCurrentObject = 
 									(PhysicalObject) n.getUserData();
 								myCurrentObject.selected(true);
 								
 								break;
 							}
 						}
 					}
 				}
 			}
 
 			public void mouseReleased(final MouseEvent e) {
 				if (myCurrentObject != null) {
 					myCurrentObject.selected(false);
 				}
 				myCurrentObject = null;				
 			}
 		});
 		
 		// Event listener time
 		myCanvas.addMouseMotionListener(new MouseMotionAdapter() {
 			private MouseEvent lastDragEvent = null;
 			
 			public void mouseDragged(final MouseEvent e) {
 				if (lastDragEvent != null) { 
 					if (myCurrentObject != null) {
 						float scale = 0.001f;
 						
 						int xdiff = e.getX() - lastDragEvent.getX();
 						int ydiff = -e.getY() + lastDragEvent.getY();
 						
 						Point3f p = new Point3f(scale * xdiff, scale 
 								* ydiff, 0);
 						Transform3D t3d = new Transform3D();
 						t3d.rotX(cameraXRotation);
 						Transform3D tmp = new Transform3D();
 						tmp.rotY(cameraYRotation);
 						t3d.mul(tmp);
 						t3d.invert();
 						t3d.transform(p);
 						
 						if (e.isAltDown()) {
 							myCurrentObject.getOrientation().y += p.x;
 							myCurrentObject.getOrientation().x += p.y;
 							myCurrentObject.getOrientation().z += p.z;
 							myCurrentObject.getOrientation().w = 1;
 							myCurrentObject.getOrientation().normalize();
 							
 						} else {
 							myCurrentObject.getPosition().x += p.x;
 							myCurrentObject.getPosition().y += p.y;
 							myCurrentObject.getPosition().z += p.z;
 						}
 						
 						myCurrentObject.updateTranformGroup();
 						
 						
 					} else {
 						cameraXRotation += 
 							Math.toRadians(e.getY() - lastDragEvent.getY()) / 3;
 						
 						if (cameraXRotation > Math.PI / 2) {
 							cameraXRotation = Math.PI / 2;
 							
 						} else if (cameraXRotation < -Math.PI / 2) {
 							cameraXRotation = -Math.PI / 2;
 						}
 						
 						cameraYRotation += 
 							Math.toRadians(e.getX() - lastDragEvent.getX()) / 3;
 						
 						updateCamera();
 					}
 				}
 				
 				lastDragEvent = e;
 			}
 			
 			public void mouseMoved(final MouseEvent e) {
 				lastDragEvent = null;
 			}
 		});
 		
 		myCanvas.addMouseWheelListener(new MouseWheelListener() {
 			public void mouseWheelMoved(final MouseWheelEvent e) {
 				if (e.getWheelRotation() > 0) {
 					cameraDistance *= 1.05;
 				
 				} else if (e.getWheelRotation() < 0) {
 					cameraDistance *= 0.95;
 				}
 				
 				updateCamera();
 			}
 		});
 		
 		// Setup the timer.
  		myTimer = new Timer(MILISECONDS_IN_SECOND / UPDATE_RATE,
  		new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				myCanvas.stopRenderer();
 				myWorld.tick();
 				myCanvas.startRenderer();
 			}
 		});
  		
  		myTimer.start();
 		
 	}
 	
 	/**
 	 * Method to update the camera.
 	 */
 	private void updateCamera() {
 		Transform3D camera3D = new Transform3D();
 		camera3D.setTranslation(new Vector3f(0f, 0f, (float) -cameraDistance));
 		Transform3D tmp = new Transform3D();
 		tmp.rotX(cameraXRotation);
 		camera3D.mul(tmp);
 		tmp.rotY(cameraYRotation);
 		camera3D.mul(tmp);
 		camera3D.invert();
 		cameraTG.setTransform(camera3D);
 	}
 	
 	/**
 	 * Start up the program.
 	 * 
 	 * @param args Unused commandline arguments.
 	 */
 	public static void main(final String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new TesseractUI().setVisible(true);
 			}
 		});
 	}
 }
 
 
 
 /*
 JCheckBoxMenuItem cMenuItem = new JCheckBoxMenuItem("Enable Particle Emitters", enableEmitters);
 cMenuItem.addActionListener(new ActionListener() {
 	public void actionPerformed(ActionEvent e) {
 		enableEmitters = !enableEmitters;
 	}			
 });
 menu.add(cMenuItem);
 
 for (int i = 0; i < forces.length; i++) {
 	cMenuItem = new JCheckBoxMenuItem(forces[i].toString(), activeForces[i]);
 	cMenuItem.setActionCommand(i + "");
 	cMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			int index = Integer.parseInt(e.getActionCommand());
 			activeForces[index] = !activeForces[index];
 		}			
 	});
 	menu.add(cMenuItem);
 }
 */
