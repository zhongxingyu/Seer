 package projectrts.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import projectrts.controller.InGameState;
 import projectrts.model.IGame;
 import projectrts.model.world.INode;
 import projectrts.view.controls.MoveControl;
 import projectrts.view.controls.NodeControl;
 import projectrts.view.spatials.AbstractSpatial;
 import projectrts.view.spatials.BarracksSpatial;
 import projectrts.view.spatials.HeadquarterSpatial;
import projectrts.view.spatials.NodeSpatial;
 import projectrts.view.spatials.RangedSpatial;
 import projectrts.view.spatials.ResourceSpatial;
 import projectrts.view.spatials.SelectSpatial;
 import projectrts.view.spatials.SpatialFactory;
 import projectrts.view.spatials.WallSpatial;
 import projectrts.view.spatials.WarriorSpatial;
 import projectrts.view.spatials.WorkerSpatial;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 
 /**
  * The in-game view, creating and managing the scene.
  * 
  * @author Markus Ekstrm
  * 
  */
 public class GameView {
 	private final SimpleApplication app;
 	private final IGame game;
 	private final Node debug = new Node("debug"); // The node for the debugging
 													// graphics
 	private Node mouseEffects = new Node("mouseEffects"); // The node for
 															// mouseEffects
 	private final static float mod = InGameState.MODEL_TO_WORLD; // The modifier
 																	// value for
 																	// converting
 																	// lengths
 																	// between
 																	// model and
 																	// world.
 	private EntityHandler entityHandler;
 	private TerrainHandler terrainHandler;
 
 	private boolean debugNodes = false;
 
 	static {
 		try {
 			// Initialize the control classes.
 			Class.forName(MoveControl.class.getName());
 			Class.forName(NodeControl.class.getName());
 
 			// Initialize the spatial classes.
 			Class.forName(WarriorSpatial.class.getName());
 			Class.forName(RangedSpatial.class.getName());
 			Class.forName(WorkerSpatial.class.getName());
 			Class.forName(HeadquarterSpatial.class.getName());
 			Class.forName(BarracksSpatial.class.getName());
 			Class.forName(ResourceSpatial.class.getName());
 			Class.forName(SelectSpatial.class.getName());
 			Class.forName(NodeSpatial.class.getName());
 			Class.forName(WallSpatial.class.getName());
 		} catch (ClassNotFoundException any) {
 			any.printStackTrace();
 		}
 	}
 
 	// TODO Markus: Add javadoc
 	public GameView(SimpleApplication app, IGame game) {
 		this.app = app;
 		this.game = game;
 		entityHandler = new EntityHandler(app, game);
 		terrainHandler = new TerrainHandler(app);
 
 	}
 
 	/**
 	 * Initializes the scene.
 	 * 
 	 * @param entitiesList
 	 *            A list containing the initial entities.
 	 * @param controlList
 	 *            The controls for the initial entities.
 	 */
 	public void initialize() {
 		terrainHandler.initialize();
 		entityHandler.initialize();
 		initializeDebug();
 		initializeMouseEffects();
 	}
 
 	private void initializeDebug() {
 		if (debugNodes) {
 			integrateNodes(game.getWorld().getNodes());
 		}
 
 		this.app.getRootNode().attachChild(debug);
 	}
 
 	private void initializeMouseEffects() {
 
 		// Attach the entities node to the root node, connecting it to the
 		// world.
 		this.app.getRootNode().attachChild(mouseEffects);
 	}
 
 	private void integrateNodes(INode[][] nodes) {
 		Box[][] nodeShapes = new Box[nodes.length][];
 
 		for (int i = 0; i < nodes.length; i++) {
 			nodeShapes[i] = new Box[nodes[i].length];
 			for (int j = 0; j < nodes[i].length; j++) {
 				nodeShapes[i][j] = new Box(new Vector3f((float) nodes[i][j]
 						.getPosition().getX() * mod, -(float) nodes[i][j]
 						.getPosition().getY() * mod, 1), (0.1f * mod) / 2,
 						(0.1f * mod) / 2, 0);
 
 				AbstractSpatial nodeSpatial = SpatialFactory.createNodeSpatial(
 						NodeSpatial.class.getSimpleName(), nodes[i][j]
 								.getClass().getSimpleName(), nodeShapes[i][j],
 						nodes[i][j]);
 				debug.attachChild(nodeSpatial);
 			}
 		}
 	}
 
 	public void drawNodes(List<projectrts.model.world.INode> coveredNodes) {
 
 		List<INode> oldNodes = getNodes(mouseEffects.getChildren());
 
 		for (INode node : coveredNodes) {
 			if (!oldNodes.contains(node)) {
 				addNodeSpatial(node);
 			}
 		}
 
 		for (INode node : oldNodes) {
 			if (!coveredNodes.contains(node)) {
 				removeNodeSpatial(node);
 			}
 		}
 
 	}
 
 	private List<INode> getNodes(List<Spatial> coveredNodes) {
 		List<INode> output = new ArrayList<INode>();
 		NodeSpatial dSpatial;
 		for (Spatial spatial : coveredNodes) {
 			if (spatial instanceof NodeSpatial) {
 				dSpatial = (NodeSpatial) spatial;
 				output.add(dSpatial.getNode());
 
 			}
 
 		}
 		return output;
 	}
 
 	private void removeNodeSpatial(INode node) {
 		NodeSpatial dSpatial;
 		for (Spatial spatial : mouseEffects.getChildren()) {
 
 			if (spatial instanceof NodeSpatial) {
 				dSpatial = (NodeSpatial) spatial;
 				if (node.equals(dSpatial.getNode())) {
 					mouseEffects.detachChild(dSpatial);
 				}
 			}
 		}
 	}
 
 	private void addNodeSpatial(INode node) {
 		Box nodeBox = new Box(new Vector3f((float) node.getPosition().getX()
 				* mod, -(float) node.getPosition().getY() * mod, 1),
 				(1f * mod) / 2, (1f * mod) / 2, 0);
 		AbstractSpatial nodeSpatial = SpatialFactory.createNodeSpatial(
 				NodeSpatial.class.getSimpleName(), node.getClass()
 						.getSimpleName(), nodeBox, node);
 		mouseEffects.attachChild(nodeSpatial);
 	}
 
 	public void drawSelected() {
 		entityHandler.drawSelected();
 	}
 
 	public void clearNodes() {
 		mouseEffects.detachAllChildren();
 	}
 }
