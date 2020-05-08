 package se2.e.engine3d.j3d;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.GraphicsConfiguration;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.Group;
 import javax.media.j3d.Node;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.media.j3d.View;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import se2.e.engine3d.Engine3D;
 import se2.e.engine3d.Engine3DListener;
 import se2.e.engine3d.GeometryAndAppearanceLoader;
 import se2.e.engine3d.j3d.animations.RuntimeAnimation;
 import se2.e.engine3d.j3d.animations.RuntimeAnimationFactory;
 import se2.e.engine3d.j3d.animations.RuntimeAnimationListener;
 import se2.e.geometry.Geometry;
 import se2.e.simulator.runtime.petrinet.RuntimeToken;
 import animations.Animation;
 import appearance.AppearanceModel;
 
 import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
 import com.sun.j3d.utils.geometry.Primitive;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.Viewer;
 import com.sun.j3d.utils.universe.ViewingPlatform;
 
 /**
  * The Class J3DEngine that is an implementation of a {@link Engine3D} using Java 3D library.
  * 
  * @author cosmin
  */
 public class J3DEngine extends JFrame implements Engine3D, ActionListener, RuntimeAnimationListener {
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = 5165791727088692312L;
 
 	/** The universe. */
 	private SimpleUniverse universe;
 
 	/** The start button. */
 	private JButton btnStart;
 
 	/** The stop button. */
 	private JButton btnStop;
 
 	/** The pause button. */
 	private JButton btnPause;
 
 	/** The animation progress listener. */
 	private Engine3DListener engineListener = null;
 
 	/** The scene branch group. */
 	private BranchGroup sceneRoot;
 
 	/** The geometry. */
 	private GeometryAndAppearanceLoader loader;
 
 	/** The geometry node factory. */
 	private J3DNodeFactory nodeFactory;
 
 	/** The logger. */
 	private Logger log = Logger.getLogger("J3DEngine");
 
 	/** The token representations. */
 	private HashMap<RuntimeToken, DynamicBranch> tokenRepresentations;
 
 	/** The place representations. */
 	private HashMap<String, DynamicBranch> placeRepresentations;
 
 	/** The canvas. */
 	private Canvas3D canvas;
 
 	/** The paused. */
 	private boolean paused = false;
 
	/** The running. */
	private boolean running = false;

	/** The canvas panel. */
 	private JPanel canvasPanel;
 
 	/**
 	 * Instantiates a new Java 3D engine.
 	 * 
 	 * @throws HeadlessException the headless exception
 	 * @author cosmin
 	 */
 	public J3DEngine() throws HeadlessException {
 		// Setup a SimpleUniverse by referencing a Canvas3D
 		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
 		canvas = new Canvas3D(config);
 		Container cp = this.getContentPane();
 		cp.setLayout(new BorderLayout());
 		cp.add(canvas, BorderLayout.CENTER);
 
 		canvasPanel = new JPanel();
 		getContentPane().add(canvasPanel, BorderLayout.NORTH);
 
 		btnStart = new JButton("Start");
 		btnStart.addActionListener(this);
 		canvasPanel.add(btnStart);
 
 		btnStop = new JButton("Stop");
 		btnStop.addActionListener(this);
 		btnStop.setEnabled(false);
 		canvasPanel.add(btnStop);
 
 		btnPause = new JButton("Pause");
 		btnPause.addActionListener(this);
 		btnPause.setEnabled(false);
 		canvasPanel.add(btnPause);
 
 		// Configure this JFrame
 		this.setLocation(200, 200);
 		this.setSize(640, 480);
 		this.setTitle("Petri Net Simulator");
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
 		// Show the JFrame
 		this.setVisible(true);
 	}
 
 	@Override
 	public void dispose() {
 		log.info("Disposing window...");
 		cleanupFull();
 
 		super.dispose();
 	}
 
 	/**
 	 * Creates the universe and set up the viewer.
 	 * 
 	 * @param canvas the canvas
 	 * @return the simple universe
 	 * @author cosmin
 	 */
 	private SimpleUniverse createUniverse(Canvas3D canvas) {
 		// Manually create the viewing platform so that we can customize it
 		ViewingPlatform viewingPlatform = new ViewingPlatform();
 
 		// Set the view position back far enough so that we can see thingsDid not find
 		TransformGroup viewTransform = viewingPlatform.getViewPlatformTransform();
 		Transform3D t3d = new Transform3D();
 		// Compute initial values for viewer
 		double xCenter = (loader.maxX + loader.minX) / 2;
 		double yCenter = (loader.maxY + loader.minY) / 2;
 		double zHeight = ((loader.maxX - loader.minX) + (loader.maxY - loader.minY));
 		// double zHeight=1500;
 		t3d.lookAt(new Point3d(xCenter, yCenter, zHeight), new Point3d(xCenter, yCenter, 0), new Vector3d(0, 1, 0));
 		t3d.invert();
 		viewTransform.setTransform(t3d);
 
 		// Set the activation radius
 		viewingPlatform.getViewPlatform().setActivationRadius((float) (2 * zHeight));
 
 		// Set back clip distance so things don't disappear
 		Viewer viewer = new Viewer(canvas);
 		View view = viewer.getView();
 		view.setBackClipDistance(2 * zHeight);
 
 		// Set the Orbit Behavior to allow moving through the universe
 		OrbitBehavior ob = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ROTATE | OrbitBehavior.STOP_ZOOM);
 		ob.setTransFactors(10.0d, 10.0d);
 		ob.setZoomFactor(10.0d);
 		ob.setRotFactors(0.2d, 0.2d);
 		ob.setSchedulingBounds(new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 2000));
 		viewingPlatform.setViewPlatformBehavior(ob);
 
 		return new SimpleUniverse(viewingPlatform, viewer);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3D#init(java.lang.Object, java.lang.Object)
 	 * 
 	 * @author cosmin, marius (some fixes after update of loader and dynamicBranchFactory class)
 	 */
 	@Override
 	public void init(Geometry geometry, AppearanceModel appearance, Set<String> inputPlacesLabels, double trackWidth) {
 
 		// Load the geometry and appearance
 		this.loader = new GeometryAndAppearanceLoader(geometry, appearance);
 		this.nodeFactory = new J3DNodeFactory(loader, this, canvas, trackWidth);
 
 		this.placeRepresentations = new HashMap<String, DynamicBranch>();
 
 		// Load the universe
 		universe = createUniverse(canvas);
 
 		// Create the initial scene and add it to the graph
 		sceneRoot = createSceneGraph(inputPlacesLabels);
 		universe.addBranchGraph(sceneRoot);
 
 		// Initialize other objects
 		tokenRepresentations = new HashMap<RuntimeToken, DynamicBranch>();
 
 		log.info("J3D Engine initialized...");
 	}
 
 	/**
 	 * Creates the scene graph (content branch) and add the static objects (e.g. {@link Place}s representations).
 	 * 
 	 * @param inputPlacesLabels the input places labels
 	 * @return the branch group
 	 * @author cosmin
 	 */
 	public BranchGroup createSceneGraph(Set<String> inputPlacesLabels) {
 		// Create the root node of the content branch
 		BranchGroup rootNode = new BranchGroup();
 
 		// Add representations for the tracks
 		Group trackGroup = new Group();
 		for (String label : loader.getTrackLabels()) {
 
 			// Create the node corresponding to tracks and add it to the scene graph
 			Node geometryNode = nodeFactory.getGeometryTransformGroup(label);
 			if (geometryNode != null)
 				trackGroup.addChild(geometryNode);
 		}
 		rootNode.addChild(trackGroup);
 
 		// Compile to perform optimizations on this content branch.
 		rootNode.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
 		rootNode.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		rootNode.compile();
 
 		// Add representations for the simple positions
 		for (String label : loader.getSimplePositionLabels()) {
 			// Create the node corresponding to simple position and add it to the scene graph
 			DynamicBranch inputPlaceBranch = null; // nodeFactory.getGeometryBranch(label, true);
 			System.out.println("Label: " + label);
 			System.out.println(inputPlacesLabels);
 			// The node corresponds to an interactive input place
 			if (inputPlacesLabels.contains(label)) {
 				log.info("Setting up a new interactive input place: " + label);
 				inputPlaceBranch = nodeFactory.getGeometryBranch(label, true);
 			} // The node doesn't correspond to an interactive input place
 			else {
 				log.info("Setting up a new simple position: " + label);
 				inputPlaceBranch = nodeFactory.getGeometryBranch(label, false);
 			}
 			// Add the branch to the scene graph
 			if (inputPlaceBranch != null) {
 				inputPlaceBranch.setAttachedToRoot(true);
 				rootNode.addChild(inputPlaceBranch.getBranchGroup());
 				placeRepresentations.put(label, inputPlaceBranch);
 			}
 		}
 
 		return rootNode;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 * 
 	 * @author cosmin
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		// If the user clicked the Start Button
 		if (e.getSource() == btnStart) {
 			log.info("Starting engine 3D...");
 			paused = false;
			running = true;
 			if (this.engineListener != null) {
 				engineListener.onStartSimulation();
 				btnStart.setEnabled(false);
 				btnPause.setEnabled(true);
 				btnStop.setEnabled(true);
 			}
 		}
 
 		// If the user clicked the Pause/Resume button
 		if (e.getSource() == btnPause) {
 			log.info("Pause/Resume button clicked...");
 			paused = !paused;
			running = !running;
 			if (paused)
 				btnPause.setText("Resume");
 			else
 				btnPause.setText("Pause");
 		}
 
 		// If the user clicked the Stop button
 		if (e.getSource() == btnStop) {
 			log.info("Stop button clicked...");
 			btnStop.setEnabled(false);
 			btnPause.setEnabled(false);
 			btnStart.setEnabled(true);
 			btnPause.setText("Pause");
			running = false;
 			cleanupOnStop();
 		}
 	}
 
 	/**
 	 * Cleanup on stop.
 	 * 
 	 * @author cosmin
 	 */
 	private void cleanupOnStop() {
 
 		for (DynamicBranch branch : tokenRepresentations.values())
 			branch.getBranchGroup().detach();
 
 		this.tokenRepresentations.clear();
 
 	}
 
 	/**
 	 * Cleanup full.
 	 * 
 	 * @author cosmin
 	 */
 	private void cleanupFull() {
 		cleanupOnStop();
 
 		Viewer.clearViewerMap();
 		Primitive.clearGeometryCache();
 		canvasPanel.removeAll();
 		canvasPanel = null;
 
 		sceneRoot = null;
 		canvas = null;
 		universe.cleanup();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3D#startAnimation(petrinet.Token, petrinet.Place)
 	 * 
 	 * @author cosmin
 	 */
 	@Override
 	public void startAnimation(RuntimeToken token, Animation animation, String geometryLabel) {
 		log.info("Starting new animation for token " + token.getLabel() + " on " + geometryLabel + ": " + animation);
 		// Get any existing branch representation of the token
 		DynamicBranch branch = tokenRepresentations.get(token);
 		if (branch != null)
 			log.info("Found dynamic branch for token " + token);
 		else
 			log.info("Existing dynamic branch not found for token " + token);
 
 		// Build the RuntimeAnimation
 		RuntimeAnimationFactory.buildRuntimeAnimation(branch, animation, token, this, this, geometryLabel);
 		// There is no need to attach the animation to the root, as it will attach by itself
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3D#setAnimationProgressListener(se2.e.engine3d.AnimationProgressListener)
 	 * 
 	 * @author cosmin
 	 */
 	@Override
 	public void setEngine3DListener(Engine3DListener listener) {
 		this.engineListener = listener;
 	}
 
 	/**
 	 * Gets the scene root.
 	 * 
 	 * @return the scene root
 	 */
 	public BranchGroup getSceneRoot() {
 		return sceneRoot;
 	}
 
 	/**
 	 * Gets the geometry and appearance loader.
 	 * 
 	 * @return the geometry and appearance loader
 	 */
 	public GeometryAndAppearanceLoader getGeometryAndAppearanceLoader() {
 		return loader;
 	}
 
 	/**
 	 * Gets the node factory.
 	 * 
 	 * @return the node factory
 	 */
 	public J3DNodeFactory getNodeFactory() {
 		return nodeFactory;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.j3d.animations.RuntimeAnimationListener#animationFinished(se2.e.simulator.runtime.petrinet.
 	 * RuntimeToken)
 	 */
 	public void animationFinished(RuntimeToken token) {
 		log.info("Animation finished for: " + token);
 		// Notify the engine listener
 		engineListener.onAnimationFinished(token);
 	}
 
 	/**
 	 * Method called when an user has interacted with an Interactive Input place.
 	 * 
 	 * @param geomLabel the geom label
 	 */
 	public void userInteraction(String geomLabel) {
		// Notify the engine listener if not paused or stopped
		if (running) {
			log.info("User interaction on: " + geomLabel);
			engineListener.onUserInteraction(geomLabel);
		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3D#destroyRepresentation(se2.e.simulator.runtime.petrinet.RuntimeToken)
 	 * 
 	 * @author cosmin
 	 */
 	@Override
 	public void destroyRepresentation(RuntimeToken token) {
 		log.info("Destroying token representation for: " + token);
 		DynamicBranch tokenBranch = tokenRepresentations.get(token);
 		if (tokenBranch != null) {
 			if (tokenBranch.isAttachedToRoot()) {
 				tokenBranch.getBranchGroup().detach();
 				tokenBranch.setAttachedToRoot(false);
 			}
 			tokenRepresentations.remove(token);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.j3d.animations.RuntimeAnimationListener#isPaused()
 	 */
 	@Override
 	public boolean isPaused() {
 		return paused;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.j3d.animations.RuntimeAnimationListener#attachToRoot(se2.e.engine3d.j3d.DynamicBranch)
 	 */
 	@Override
 	public void attachToRoot(RuntimeAnimation<?> animation) {
 		// Attach the Animation branch to the Scene graph, if needed
 		if (animation.getTargetBranch() != null) {
 			sceneRoot.addChild(animation.getTargetBranch().getBranchGroup());
 		}
 		tokenRepresentations.put(animation.getToken(), animation.getTargetBranch());
 	}
 
 	/**
 	 * Gets the place representation for a given label.
 	 * 
 	 * @param label the label
 	 * @return the place representation
 	 */
 	public DynamicBranch getPlaceRepresentation(String label) {
 		return placeRepresentations.get(label);
 	}
 
 	/**
 	 * Attach a place representation to the root.
 	 * 
 	 * @param label the label
 	 * @param branch the branch
 	 */
 	public void attachPlaceRepresentation(String label, DynamicBranch branch) {
 		if (branch != null) {
 			sceneRoot.addChild(branch.getBranchGroup());
 			branch.setAttachedToRoot(true);
 			placeRepresentations.put(label, branch);
 		}
 	}
 }
