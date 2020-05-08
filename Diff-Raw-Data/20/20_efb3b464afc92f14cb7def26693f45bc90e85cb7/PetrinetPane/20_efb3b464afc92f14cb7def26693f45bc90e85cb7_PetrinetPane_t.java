 package gui2;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import javax.swing.JPanel;
 
 
 import petrinet.Arc;
 import petrinet.INode;
 
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
 import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
 import edu.uci.ics.jung.visualization.control.ScalingControl;
 import exceptions.EngineException;
 import gui2.EditorPane.EditorMode;
 
 import static gui2.Style.*;
 
 /** Pane for displaying petrinets */
 class PetrinetPane {
 
 	/** Internal JPanel for gui-layouting the petrinet */
 	private JPanel petrinetPanel;
 
 	/** Internal Panel for scrolling and zooming. This is added to petrinetPanel */
 	private GraphZoomScrollPane scrollPanel;
 
 	/** For zooming */
 	private ScalingControl scaler;
 
 	/** Internal JPanel for actually viewing and controlling the petrinet */
 	private VisualizationViewer<INode, Arc> visualizationViewer;
 
 	/** ID of currently displayed petrinet */
 	int currentPetrinetId = -1;
 
 	/** singleton instance of this pane */
 	private static PetrinetPane instance;
 
 	/** Returns the singleton instance for this pane */
 	public static PetrinetPane getInstance() {
 		return instance;
 	}
 
 	/** mouse click listener for the drawing panel */
 	private static class PetrinetMouseListener extends
 			PickingGraphMousePlugin<INode, Arc> implements MouseWheelListener {
 		
 		private static enum DragMode{
 			SCROLL, MOVENODE, NONE
 		}
 
 		private PetrinetPane petrinetPane;
 		
 		private DragMode dragMode = DragMode.NONE;
 
 		PetrinetMouseListener(PetrinetPane petrinetPane) {
 			this.petrinetPane = petrinetPane;
 		}
 
 		/** X-coordinate of begin of drag */
 		private int pressedX = 0;
 		/** Y-coordinate of begin of drag */
 		private int pressedY = 0;
 
 		/** Zoom petrinet on mouse wheel */
 		@Override
 		public void mouseWheelMoved(MouseWheelEvent e) {
 			Point point = new Point(e.getX(), e.getY());
 			System.out.println(point);
 			if (e.getWheelRotation() < 0) {
 				petrinetPane.scaler.scale(petrinetPane.visualizationViewer,
 						1.1f, point);
 			} else {
 				petrinetPane.scaler.scale(petrinetPane.visualizationViewer,
 						0.9f, point);
 			}
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			super.mousePressed(e); // mousePressedEvent in class PickingGraphMousePlugin selects nodes
 			EditorMode mode = EditorPane.getInstance().getCurrentMode();
 
 			if (mode == EditorMode.PICK) {
 				if (edge != null) {
 					AttributePane.getInstance().displayEdge(edge);
 					edge = null;
 				} else if (vertex != null) {
 					AttributePane.getInstance().displayNode(vertex);
 					vertex = null;
 				}
 			}
 		}
 		
 		/** Checks if something is clicked without changing selection */
 		private boolean isAnythingClicked(MouseEvent e){
 			Arc beforeEdge = edge;
 			INode beforeNode = vertex;
 			edge = null;
 			vertex = null;
 			super.mousePressed(e); 
 			
 			boolean result = edge != null || vertex != null;
 			edge = beforeEdge;
 			vertex = beforeNode;
 			
 			return result;
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			if(EditorPane.getInstance().getCurrentMode() == EditorMode.PICK &&
 					dragMode == DragMode.NONE){
 				pressedX = e.getX();
 				pressedY = e.getY();
 				//find >out: scrolling or moving? -> is something clicked?
 				if(isAnythingClicked(e)){
 					//something is clicked -> MOVENODE
 					dragMode = DragMode.MOVENODE;
 				}else{
 					//nothing is clicked -> SCROLL
 					dragMode = DragMode.SCROLL;
 				}
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			if (EditorPane.getInstance().getCurrentMode() == EditorMode.PICK &&
 					(e.getX() != pressedX || e.getY() != pressedY)) {
 				Point oldPoint = new Point(pressedX, pressedY);
 				Point newPoint = new Point(e.getX(), e.getY());
 				if(dragMode == DragMode.SCROLL){
 					/* Scrolling is realized by zooming out of old position and
 					 * zooming in to new position */
 					petrinetPane.scaler.scale(petrinetPane.visualizationViewer,
 							2f, oldPoint);
 					petrinetPane.scaler.scale(petrinetPane.visualizationViewer,
 							0.5f, newPoint);
 				}else if(dragMode == DragMode.MOVENODE){
 					PopUp.popUnderConstruction("Knoten verschieben");
 				}
 			}
 			dragMode = DragMode.NONE;
 		}
 	}
 
 	/* Static constructor that initiates the singleton */
 	static {
 		instance = new PetrinetPane();
 	}
 
 	/** Private default constructor */
 	private PetrinetPane() {
 		petrinetPanel = new JPanel();
 		petrinetPanel.setLayout(PETRINET_PANE_LAYOUT);
 		petrinetPanel.setBorder(PETRINET_BORDER);
 
 		visualizationViewer = initializeVisualizationViever();
 		scaler = new CrossoverScalingControl();
 		visualizationViewer.setBackground(Color.WHITE);
 		scrollPanel = new GraphZoomScrollPane(visualizationViewer);
 
 		petrinetPanel.add(scrollPanel);
 
 		visualizationViewer.addMouseListener(new PetrinetMouseListener(this));
 		visualizationViewer.addMouseWheelListener(new PetrinetMouseListener(
 				this));
 	}
 
 	private JPanel getPetrinetPanel() {
 		return petrinetPanel;
 	}
 
 	/**
 	 * Returns the graphLayout for the currently displayed petrinet
 	 * 
 	 * @return <tt>null</tt> if Error was thrown
 	 */
 	private AbstractLayout<INode, Arc> getCurrentLayout() {
 		try {
 			return MainWindow.getPetrinetManipulation().getJungLayout(
 					currentPetrinetId);
 		} catch (EngineException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private VisualizationViewer<INode, Arc> initializeVisualizationViever() {
 		currentPetrinetId = MainWindow.getPetrinetManipulation()
 				.createPetrinet();
 
 		VisualizationViewer<INode, Arc> visuServer = new VisualizationViewer<INode, Arc>(
 				getCurrentLayout());
 
 		return visuServer;
 	}
 
 	/** Adds the petrinet pane to the given JPanel (frame) */
 	public void addTo(JPanel frame) {
 		frame.add(getPetrinetPanel());
 	}
 }
