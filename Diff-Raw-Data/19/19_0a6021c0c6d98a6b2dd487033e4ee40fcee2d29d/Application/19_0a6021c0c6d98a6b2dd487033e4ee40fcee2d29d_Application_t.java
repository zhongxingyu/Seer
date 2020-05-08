 package application;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GraphicsConfiguration;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.media.j3d.AmbientLight;
 import javax.media.j3d.Appearance;
 import javax.media.j3d.BoundingBox;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.ColoringAttributes;
 import javax.media.j3d.DirectionalLight;
 import javax.media.j3d.Light;
 import javax.media.j3d.PolygonAttributes;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.WindowConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 import javax.vecmath.Vector3f;
 
 import particles.ParticleSystem;
 import particles.behaviors.*;
 
 import com.sun.j3d.utils.geometry.Box;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 
 import forcefield.VectorField;
 
 public class Application {
 
 	// Physics updates per second (approximate).
 	private static final int UPDATE_RATE = 30;
 	
 	// Width of the extent in meters.
 	private static final float EXTENT_WIDTH = 20;
 	
 	// Magnitude of the force being applied.
 	private int forceMagnitude;
 	
 	// Coefficient of restitution.
 	private float coefficientOfRestitution = 1f;
 	
 	// Simple universe reference.
 	private SimpleUniverse simpleU;
 	
 	// Particle system
     private ParticleSystem particleSystem;
     private VectorField forceField;
     private static final int PARTICLE_COUNT = 50; //number of particles
     private static final int FIELD_DIVISIONS = 10; //divisions in the force field
     double DISTANCE = 15d;
 
     private Set<ForceBehavior> forceBehaviors = new HashSet<ForceBehavior>();
     private Set<CollisionBehavior> collisionBehaviors = new HashSet<CollisionBehavior>();
 
     ForceBehavior currentForceBehavior = null;
 
     BranchGroup trueScene;
     BranchGroup scene;
     BranchGroup forceFieldBranchGroup;
 
     /**
      * Main method.
      * 
      * @param args
      */
     public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				new Application().createAndShowGUI();
 			}
 		});
 	}
 
 	/**
 	 * Create and display the GUI, including menus and the scene graph. 
 	 */
 	private void createAndShowGUI() {
 		// Fix for background flickering on some platforms
 		System.setProperty("sun.awt.noerasebackground", "true");
 		
 		// Build the universe
 		GraphicsConfiguration config = SimpleUniverse
 				.getPreferredConfiguration();
 		final Canvas3D canvas = new Canvas3D(config);
 
 		simpleU = new SimpleUniverse(canvas);
 		simpleU.getViewingPlatform().setNominalViewingTransform();
 		simpleU.getViewer().getView().setSceneAntialiasingEnable(true);
 
 		// Scene graph
 		
 		// Add a scaling transform that resizes the virtual world to fit
 		// within the standard view frustum.
         trueScene = new BranchGroup();
 		TransformGroup worldScaleTG = new TransformGroup();
 		Transform3D t3D = new Transform3D();
 		t3D.setScale( 6 / EXTENT_WIDTH);
 		worldScaleTG.setTransform(t3D);
 		trueScene.addChild(worldScaleTG);
 		scene = new BranchGroup();
         scene.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
         scene.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
 		worldScaleTG.addChild(scene);
 
 		Light light = new AmbientLight(new Color3f(1f, 1f, 1f));
 		scene.addChild(light);
 		light = new DirectionalLight(new Color3f(1f, 1f, 1f), new Vector3f(-1f, -1f, -1f));
 		light.setInfluencingBounds(new BoundingBox());
 		
 		// View movement
 		Point3d focus = new Point3d();
         Point3d camera = new Point3d(0,0,1);
         Vector3d up = new Vector3d(0,1,0);
         TransformGroup lightTransform = new TransformGroup();
         TransformGroup curTransform = new TransformGroup();
         FlyCam fc = new FlyCam(simpleU.getViewingPlatform().getViewPlatformTransform(),focus,camera,up,DISTANCE, lightTransform, curTransform);
         fc.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));
         scene.addChild(fc);
 
 		// Extent
         Appearance app = new Appearance();
         PolygonAttributes polyAttribs = new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_NONE, 0);
         app.setPolygonAttributes(polyAttribs);
         app.setColoringAttributes(new ColoringAttributes(new Color3f(1.0f, 1.0f, 1.0f), ColoringAttributes.FASTEST));
         float dim = EXTENT_WIDTH / 2  - 0.02f;
 		Box extent = new Box(dim, dim, dim, app);
         extent.setPickable(false);
         scene.addChild(extent);
 
         //extent solid back
         app = new Appearance();
         polyAttribs = new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_FRONT, 0);
         app.setPolygonAttributes(polyAttribs);
         app.setColoringAttributes(new ColoringAttributes(new Color3f(0.5f, 0.5f, 0.5f), ColoringAttributes.FASTEST));
         dim = EXTENT_WIDTH / 2;
         extent = new Box(dim, dim, dim, app);
 		extent.setPickable(false);
 		scene.addChild(extent);
        particleSystem = new ParticleSystem(PARTICLE_COUNT,EXTENT_WIDTH / 2);
         scene.addChild(particleSystem);
         forceField = new VectorField(EXTENT_WIDTH/2f, FIELD_DIVISIONS);
         forceFieldBranchGroup = new BranchGroup();
         forceFieldBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
         forceFieldBranchGroup.addChild(forceField);
         scene.addChild(forceFieldBranchGroup);
         scene.setCapability(BranchGroup.ALLOW_DETACH);
         scene.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
 		
 		simpleU.addBranchGraph(trueScene);
         addBehaviors();
 
 		// Swing-related code
 		JFrame appFrame = new JFrame("Physics Engine - Project 2");
 		appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         canvas.setPreferredSize(new Dimension(800,600));
 		appFrame.add(canvas, BorderLayout.CENTER);
 		appFrame.add(buildControlPanel(), BorderLayout.SOUTH);
 		appFrame.pack();
         appFrame.setLocationRelativeTo(null); //center the window
         //disabled because it's super annoying for testing --david
 //		if (Toolkit.getDefaultToolkit().isFrameStateSupported(
 //				Frame.MAXIMIZED_BOTH)) {
 //			appFrame.setExtendedState(appFrame.getExtendedState()
 //					| Frame.MAXIMIZED_BOTH);
 //		}
 
         //simulation start
         new Timer(1000 / UPDATE_RATE, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 canvas.stopRenderer();
                 tick();
                 canvas.startRenderer();
             }
         }).start();
 		appFrame.setVisible(true);
 	}
 
     boolean forceFieldEnabled = true;
 
     /**
      * Hides or displays the Force Field. Enabled by default.
      * @param enabled On or off?
      */
     private void setForceFieldEnabled(final boolean enabled) {
         if(enabled) {
             if(!forceFieldEnabled) {
                 scene.addChild(forceFieldBranchGroup);
             }
             forceFieldEnabled = true;
         } else {
 //            scene.removeChild(forceFieldBranchGroup);
             forceFieldBranchGroup.detach();
             forceFieldEnabled = false;
         }
     }
 
     /**Helper method to add the behaviors to the scene**/
     private void addBehaviors() {
         forceBehaviors.add(new GravityBehavior(10));
         forceBehaviors.add(new WindBehavior(20));
         forceBehaviors.add(new WindBehavior(0,"Wind 2"));
         forceBehaviors.add(new WindBehavior(0,"Wind 3: Revelations"));
         forceBehaviors.add(new DragBehavior(0.01f));
 
         //collisionBehaviors.add(new CubeBoundingBehavior(EXTENT_WIDTH/2, coefficientOfRestitution));
         collisionBehaviors.add(new CubeBoundingWrapBehavior(EXTENT_WIDTH/2, coefficientOfRestitution));
 
         for(ForceBehavior fb : forceBehaviors) {
             particleSystem.addParticleForceBehavior(fb);
             forceField.addParticleForceBehavior(fb);
         }
 
         for(CollisionBehavior cb : collisionBehaviors) {
             particleSystem.addParticleCollisionBehavior(cb);
         }
     }
     /**Adds a single behavior to the scene and all appropriate systems**/
     private void addBehavior(ParticleBehavior particleBehavior) {
         if(particleBehavior instanceof ForceBehavior)
             forceBehaviors.add((ForceBehavior)particleBehavior);
         if(particleBehavior instanceof CollisionBehavior)
             collisionBehaviors.add((CollisionBehavior)particleBehavior);
         particleSystem.addBehavior(particleBehavior);
         forceField.addBehavior(particleBehavior);
 
     }
 
     /**Removes a single behavior from the scene and all appropriate systems**/
     private void removeBehavior(ParticleBehavior particleBehavior) {
         forceBehaviors.remove(particleBehavior);
         collisionBehaviors.remove(particleBehavior);
         particleSystem.removeBehavior(particleBehavior);
         forceField.removeBehavior(particleBehavior);
     }
 
     long old_time = System.currentTimeMillis();
     long new_time;
 
     /** Updates the system's simulation**/
     private void tick() {
         new_time = System.currentTimeMillis();
         float difference = (new_time - old_time)/ 1000f;
         particleSystem.update(difference);
         forceField.update(difference);
         for(ForceBehavior fb : forceBehaviors) {
             fb.update(difference);
         }
         for(CollisionBehavior cb : collisionBehaviors) {
             cb.update(difference);
         }
 
         old_time = new_time;
     }
 
     /** Builds the control panel **/
 	private final JPanel buildControlPanel() {
 		// Basic panel setups
 		JPanel controlPanel = new JPanel();
 		JPanel checkBoxPanel = new JPanel();
 		JPanel sliderPanel = new JPanel();
 		
 		GridLayout radioButtonGrid = new GridLayout(0, 1);
 		GridLayout sliderGrid = new GridLayout(0, 3);
 		checkBoxPanel.setLayout(radioButtonGrid);
 		sliderPanel.setLayout(sliderGrid);
 		
 		controlPanel.add(checkBoxPanel, BorderLayout.EAST);
 		controlPanel.add(sliderPanel, BorderLayout.WEST);
 
 		// Add controls for forces
 		
 		// Add control for force field
         JCheckBox forceFieldEnable = new JCheckBox();
         forceFieldEnable.addItemListener(new ItemListener() {
 
             @Override
             public void itemStateChanged(ItemEvent e) {
                 JCheckBox source = (JCheckBox) e.getSource();
                 if (source.isSelected()) {
                     setForceFieldEnabled(true);
                 } else {
                     setForceFieldEnabled(false);
                 }
             }
         });
      
         forceFieldEnable.setText("Force Field");
         forceFieldEnable.setSelected(true);
         checkBoxPanel.add(forceFieldEnable);
 		
         for(final ForceBehavior fb : forceBehaviors) {
         	// Checkboxes for behaviors
             JCheckBox behaviorEnable = new JCheckBox();
             behaviorEnable.addChangeListener(new ChangeListener() {
 				
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					JCheckBox source = (JCheckBox) e.getSource();
 					if (source.isSelected()) {
 						addBehavior(fb);
 					} else {
 						removeBehavior(fb);
 					}
 				}
 			});
          
             behaviorEnable.setText(fb.getName());
             behaviorEnable.setSelected(true);
             checkBoxPanel.add(behaviorEnable);
             
             // Sliders for magnitude of forces
             final float max = fb.getForceMaximum();
             final float min = fb.getForceMinimum();
             final float cur = fb.getForceMagnitude();     
             
     		final JSlider forceMagnitudeSlider = new JSlider();
             
 	        sliderPanel.add(new JLabel(fb.getName()));
 			sliderPanel.add(forceMagnitudeSlider);
 			final JLabel forceMagLabel = new JLabel("" + (int) cur);
 			sliderPanel.add(forceMagLabel);
 			
 			forceMagnitudeSlider.setMinorTickSpacing(1);
             forceMagnitudeSlider.setMaximum((int) (max * 100));
             forceMagnitudeSlider.setMinimum((int) (min * 100));
             forceMagnitudeSlider.setValue((int) (cur * 100));
             forceMagnitudeSlider.addChangeListener(new ChangeListener() {
             	
 				ForceBehavior associatedBehavior = fb;
 				
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					JSlider slider = (JSlider) e.getSource();
     				forceMagLabel.setText("" + Math.round(slider.getValue() / 100f));
                     associatedBehavior.setForceMagnitude(slider.getValue() / 100f);
                     forceField.resetMaxLength(); //clear the max length so it can adjust quickly
                     System.out.printf("Slider Changed: Min (%02.2f) Max (%02.2f) Cur (%02.2f)\n", min, max, cur);
 				}
 			});
             
         }
 		
     	JSlider coefficientOfRestitutionSlider = buildSlider(0, 100, (int)(coefficientOfRestitution*100));
 		sliderPanel.add(new JLabel("Coefficient of restitution"));
 		sliderPanel.add(coefficientOfRestitutionSlider);
 		final JLabel coefficientLabel = new JLabel("" + coefficientOfRestitutionSlider.getValue());
 		sliderPanel.add(coefficientLabel);
 		
 		ChangeListener coefficientListener = new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				JSlider source = (JSlider) e.getSource();
 				coefficientOfRestitution = source.getValue()/100f;
 				coefficientLabel.setText("" + source.getValue() + "%");
                 //update the coefficients
                 for(CollisionBehavior cb : collisionBehaviors)
                     cb.setCoefficientOfRestitution(coefficientOfRestitution);
 			}
 		};
 		coefficientOfRestitutionSlider.addChangeListener(coefficientListener);
		
		JSlider particleSlider = buildSlider(0, 100, PARTICLE_COUNT);
		sliderPanel.add(new JLabel("Particles"));
		sliderPanel.add(particleSlider);
		final JLabel particleCountLabel = new JLabel("" + particleSlider.getValue());
		sliderPanel.add(particleCountLabel);
		
		ChangeListener particleListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				coefficientOfRestitution = source.getValue();
				particleSystem.setParticleCount(source.getValue());
			}
		};
		particleSlider.addChangeListener(particleListener);
 		return controlPanel;
 	}
 
     /** Creates a slider **/
 	private final JSlider buildSlider(int min, int max, int value) {
 		JSlider slider = new JSlider(min, max, value);
 		slider.setMinorTickSpacing(1);
 		slider.setPaintTicks(true);
 		return slider;
 	}
 }
