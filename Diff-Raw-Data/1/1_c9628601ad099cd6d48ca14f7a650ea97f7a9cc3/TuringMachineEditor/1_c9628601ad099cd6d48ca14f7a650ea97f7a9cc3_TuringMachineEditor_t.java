 package gui.turing;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Observable;
 import java.util.UUID;
 import java.util.Observer;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import machine.Simulation;
 import machine.turing.*;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 
 import machine.turing.Edge;
 import machine.turing.State;
 import machine.turing.TuringMachine;
 
 import com.mxgraph.swing.mxGraphComponent;
 
 import com.mxgraph.view.mxCellState;
 import com.mxgraph.view.mxGraphSelectionModel;
 import com.mxgraph.view.mxGraph;
 import com.mxgraph.view.mxStylesheet;
 import com.mxgraph.model.*;
 
 import com.mxgraph.util.mxConstants;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxPoint;
 
 
 
 import gui.MachineEditor;
 
 public class TuringMachineEditor extends MachineEditor implements KeyListener, ItemListener, ActionListener, MouseListener, Observer{
 	private static final long serialVersionUID = 7647012826073382156L;
 	private int GRID_SIZE = 50;
 	private final int WIDTH = 50;
 	private final int HEIGHT = 50;
 	private boolean initialized = false;
 	private final TuringMachine machine;
 	private mxCell selectedState = null;
 	private mxCell selectedEdge = null;
 	private StateList graphicalStates = null;
 	private StateList graphicalTextboxes = null;
 	private EdgeList graphicalEdges = null;
 	private StateList graphicalFrames = null;
 
 
 	protected JPanel jPanelLeft = null;
 	protected JPanel jPanelGraph = null;
 	protected mxGraph graph = null;
 	protected JSplitPane jSplitPaneHorizontal = null;
 	protected JPanel jPanelToolBox = null;
 	protected JPanel jPanelProperties = null;
 	protected ToolBox toolBox = new ToolBox();
 
 	private JMenu editMenu;
 	private JMenu viewMenu;
 	private JMenuItem selectAllAction;
 	private JMenuItem copyAction;
 	private JMenuItem cutAction;
 	private JMenuItem pasteAction;
 	private JCheckBoxMenuItem gridToggleAction;
 
 	private boolean gridEnabled = true;
 
 	/**
 	 * 
 	 * @author Philipp
 	 * Nested class to extend ArrayList<mxCell> to find mxCells with specified State Object
 	 */
 	class StateList extends ArrayList<mxCell>{
 		private static final long serialVersionUID = 4590100471318084729L;
 		public StateList(){
 			super();
 		}
 
 		public StateList(int size){
 			super(size);
 		}
 		/**
 		 * Method to find mxCell with specified value of type State
 		 * @param state
 		 * @return mxCell
 		 */
 		mxCell getMxCell(State state){							
 			for (int i = 0; i < this.size(); i++) {
 				if(this.get(i).getValue().equals((Object) state)){
 					return this.get(i);
 				}
 			}
 			return null;
 		}
 	}
 
 	class EdgeList extends ArrayList<mxCell>{
 		private static final long serialVersionUID = -6540044275767431408L;
 		public EdgeList(){
 			super();
 		}
 		public EdgeList(int size){
 			super(size);
 		}
 
 		mxCell getMxCell(State source, State target){
 			for (int i = 0; i < this.size(); i++) {
 				if((this.get(i).getSource().getValue().equals((Object) source)) && (this.get(i).getTarget().getValue().equals((Object) target))){
 					return this.get(i);
 				}
 			}
 			return null;
 		}
 
 	}
 	public TuringMachineEditor(final TuringMachine machine) {
 		super();
 		this.machine = machine;
 
 		this.initEditor();
 
 		this.graphicalStates = new StateList(machine.getStates().size());
 		this.graphicalTextboxes = new StateList();
 		this.graphicalEdges = new EdgeList(machine.getEdges().size());
 		this.graphicalFrames = new StateList();
 
 		//create left panel
 		this.jPanelLeft = new JPanel();
 		this.jPanelLeft.setLayout(new BorderLayout());
 		this.jPanelToolBox = new JPanel();
 		this.jPanelProperties = new JPanel();
 		this.jPanelLeft.add(this.jPanelToolBox, BorderLayout.NORTH);
 		this.jPanelLeft.add(this.jPanelProperties, BorderLayout.CENTER);
 		this.jPanelProperties.setLayout(new BorderLayout());
 		this.jPanelToolBox.setLayout(new BorderLayout());
 
 		//create main graph panel
 		this.jPanelGraph = new JPanel();
 		this.jPanelGraph.setLayout(new BorderLayout());
 
 		//create split pane
 		this.jSplitPaneHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				this.jPanelLeft, this.jPanelGraph);
 		this.jSplitPaneHorizontal.setOneTouchExpandable(true);
 		this.jSplitPaneHorizontal.setDividerLocation(250);
 		this.jPanelLeft.setMinimumSize(new Dimension(200, 100));
 		this.jPanelGraph.setMinimumSize(new Dimension(200, 100));
 		this.setLayout(new BorderLayout());
 		this.add(this.jSplitPaneHorizontal, BorderLayout.CENTER);
 		jPanelToolBox.add(toolBox);
 
 		//create the graph
 		this.graph = new mxGraph();
 		this.graph.setAllowDanglingEdges(false);
 		this.graph.setAllowLoops(true);
 		this.graph.setAutoSizeCells(true);
 		this.graph.setCellsResizable(true);
 		this.graph.setCellsEditable(false);
 		this.graph.setAllowNegativeCoordinates(true);
 		this.graph.setSplitEnabled(false);
 		//		this.graph.setDefaultLoopStyle(null);
 
 		this.graph.addListener(mxEvent.MOVE_CELLS, new mxIEventListener() {
 			@Override
 			public void invoke(Object obj, mxEventObject e) {
 				for(Object cellObj: (Object[]) e.getProperty("cells")){
 					mxCell cell = (mxCell) cellObj;
 					if(cell.isVertex()){
 						if(cell.getValue() instanceof State) {
 							int x = (int) cell.getGeometry().getX();
 							int y = (int) cell.getGeometry().getY();
 							((State)cell.getValue()).setXcoord((int)cell.getGeometry().getX());
 							((State)cell.getValue()).setYcoord((int)cell.getGeometry().getY());
 							x = (int) Math.ceil(x / GRID_SIZE);
 							y = (int) Math.ceil(y / GRID_SIZE);
 							graph.getModel().beginUpdate();
 							try {
 								cell.setGeometry(new mxGeometry(x * GRID_SIZE, y * GRID_SIZE, cell.getGeometry().getWidth(), cell.getGeometry().getHeight()));
 								graph.repaint();
 							}
 							finally {
 								graph.getModel().endUpdate();
 							}
 						}
 						else if(cell.getValue() instanceof Textbox) {
 							((Textbox)cell.getValue()).setX((int)cell.getGeometry().getX());
 							((Textbox)cell.getValue()).setY((int)cell.getGeometry().getY());
 						}
 						else if(cell.getValue() instanceof Frame) {
 							((Frame)cell.getValue()).setX((int)cell.getGeometry().getX());
 							((Frame)cell.getValue()).setY((int)cell.getGeometry().getY());
 						}
 					}
 				}
 			}
 		});
 
 		this.graph.getSelectionModel().addListener(mxEvent.CHANGE, new mxIEventListener() {
 			@Override
 			public void invoke(Object obj, mxEventObject e) {
 				mxGraphSelectionModel model = (mxGraphSelectionModel) obj;
 				mxCell cell = (mxCell) model.getCell();
 				if (cell == null) {
 					displayProperties();
 				}
 				else if(cell.isVertex()){
 					if(cell.getValue() instanceof State)
 						displayProperties((State) cell.getValue(), graph.getView().getState(cell));
 					else if(cell.getValue() instanceof Textbox)
 						displayProperties((Textbox) cell.getValue());
 					else if(cell.getValue() instanceof Frame)
 						displayProperties();					
 				} 
 				else if (cell.isEdge()) {
 					displayProperties((Edge) cell.getValue());
 				}
 			}
 		});
 
 		this.graph.addListener(mxEvent.CELL_CONNECTED, new mxIEventListener() {
 			@Override
 			public void invoke(Object obj, mxEventObject e) {
 				if(initialized){
 					mxCell graphEdge = (mxCell) e.getProperty("edge");
 					mxICell source = ((mxCell) graphEdge).getSource();
 					mxICell target = ((mxCell) graphEdge).getTarget();
 					if(source != null && target != null) {
 						Edge edge = new Edge((State) (graphEdge.getSource().getValue()),(State)(graphEdge.getTarget().getValue()),new ArrayList<Transition>());
 						graphEdge.setValue(edge);
 						machine.getEdges().add(edge);
						graphicalEdges.add(graphEdge);
 						graph.refresh();
 						graph.repaint();
 					}
 				}
 			}
 		});
 		
 		this.graph.addListener(mxEvent.CELLS_RESIZED, new mxIEventListener() {
 			@Override
 			public void invoke(Object obj, mxEventObject e) {
 				mxGraphSelectionModel model = ((mxGraph) obj).getSelectionModel();
 				mxCell cell = (mxCell) model.getCell();
 
 				if(cell.isVertex()) {
 					mxGeometry g = cell.getGeometry();
 					if(cell.getValue() instanceof Textbox) {
 						Textbox textbox  = (Textbox) cell.getValue();
 						textbox.setWidth((int) g.getWidth());
 						textbox.setHeight((int) g.getHeight());
 					}
 					else if(cell.getValue() instanceof Frame) {
 						Frame frame = (Frame) cell.getValue();
 						frame.setWidth((int) g.getWidth());
 						frame.setHeight((int) g.getHeight());
 					}
 				}
 			}
 		});
 
 		// set style
 		mxStylesheet stylesheet = graph.getStylesheet();
 		initStyles(stylesheet);
 		
 		this.drawGraph();
 
 		mxGraphComponent graphComponent = new mxGraphComponent(graph);
 		graphComponent.addKeyListener(this);
 		graphComponent.getGraphControl().addMouseListener(this);
 		this.jPanelGraph.add(graphComponent, BorderLayout.CENTER);
 		initialized = true;
 
 		displayProperties();
 	}
 
 	/**
 	 * Initializes the editor
 	 */
 	public void initEditor() {
 		editMenu = new JMenu("Edit");
 		viewMenu = new JMenu("View");
 		selectAllAction = new JMenuItem("Select all");
 		copyAction = new JMenuItem("Copy");
 		cutAction = new JMenuItem("Cut");
 		pasteAction = new JMenuItem("Paste");
 		gridToggleAction = new JCheckBoxMenuItem("Grid enabled");
 		gridToggleAction.setSelected(true);
 
 		editMenu.add(copyAction);
 		editMenu.add(cutAction);
 		editMenu.add(pasteAction);
 		editMenu.add(new JSeparator());
 		editMenu.add(selectAllAction);
 		viewMenu.add(gridToggleAction);
 
 		this.getMenus().add(editMenu);
 		this.getMenus().add(viewMenu);
 
 		copyAction.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		cutAction.setAccelerator(KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		pasteAction.setAccelerator(KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		selectAllAction.setAccelerator(KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 
 		copyAction.addActionListener(this);
 		cutAction.addActionListener(this);
 		pasteAction.addActionListener(this);
 		selectAllAction.addActionListener(this);
 		gridToggleAction.addItemListener(this);
 	}
 
 	private void displayProperties() {
 		PropertiesTuringMachine propertiesMachine = new PropertiesTuringMachine(machine);
 		jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesMachine, BorderLayout.PAGE_START);
 		jPanelProperties.validate();
 	}
 
 	private void displayProperties(Edge edge) {
 		PropertiesEdge propertiesEdge = new PropertiesEdge(this.machine.getNumberOfTapes(), edge, graph);
 		jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesEdge, BorderLayout.CENTER);
 		jPanelProperties.validate();
 	}
 
 	private void displayProperties(State state, mxCellState mxState) {
 		PropertiesState propertiesState = new PropertiesState(state,graph, mxState);
 		this.jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesState, BorderLayout.PAGE_START);
 		jPanelProperties.validate();
 	}
 
 	private void displayProperties(Textbox textbox) {
 		PropertiesTextbox propertiesTextbox = new PropertiesTextbox(textbox, graph);
 		this.jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesTextbox, BorderLayout.PAGE_START);
 		jPanelProperties.validate();
 	}
 
 	private void drawGraph(){
 		ArrayList<State> states = this.machine.getStates();
 		ArrayList<Edge> edges = this.machine.getEdges();
 		ArrayList<Textbox> textboxes = this.machine.getTextboxes();
 		ArrayList<Frame> frames = this.machine.getFrames();
 
 		//load graphical states
 		graph.getModel().beginUpdate();
 		try	{
 			for (int i = 0;  i < states.size(); i++){
 				int x = states.get(i).getXcoord();
 				int y = states.get(i).getYcoord();
 				x = (int) Math.ceil(x / GRID_SIZE);
 				y = (int) Math.ceil(y / GRID_SIZE);
 				graphicalStates.add(i, (mxCell) graph.insertVertex(graph.getDefaultParent(), null, 
 						states.get(i), x * GRID_SIZE, y * GRID_SIZE, 
 						states.get(i).getWidth(), states.get(i).getHeight(),
 						(states.get(i).isFinalState() && states.get(i).isStartState() ? "FINALSTART" :
 							(states.get(i).isFinalState() ? "FINAL" : 
 								((states.get(i).isStartState() ? "START" : 
 										"CIRCLE"))))));
 			}
 			//insert graphical Edges
 			Edge currentEdge = null;
 			Object v1 = null;
 			Object v2 = null;
 			for (int i = 0; i < edges.size(); i++){
 				currentEdge = edges.get(i);
 				v1 = graphicalStates.getMxCell(currentEdge.getFrom());
 				v2 = graphicalStates.getMxCell(currentEdge.getTo());
 				mxCell edge = (mxCell) graph.insertEdge(graph.getDefaultParent(), null, currentEdge, v1, v2);
 				edge.getGeometry().setX(currentEdge.getPosLabelX());
 				edge.getGeometry().setY(currentEdge.getPosLabelY());
 				
 				//set via control points
 				ArrayList<mxPoint> points = new ArrayList<mxPoint>();
 				for (Point p : currentEdge.getVia()) {
 					points.add(new mxPoint(p.getX(),p.getY()));
 				}
 				edge.getGeometry().setPoints(points);
 				
 				graphicalEdges.add(i,edge);
 			}
 
 			for (int i = 0;  i < textboxes.size(); i++){
 				int x = textboxes.get(i).getX();
 				int y = textboxes.get(i).getY();
 				int width = textboxes.get(i).getWidth();
 				int height = textboxes.get(i).getHeight();
 				mxCell mxTextbox = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, 
 						textboxes.get(i), x, y, width, height,"TEXTBOX");
 				mxTextbox.setConnectable(false);
 				graphicalTextboxes.add(i,mxTextbox);
 			}
 
 			for (int i = 0;  i < frames.size(); i++){
 				int x = frames.get(i).getX();
 				int y = frames.get(i).getY();
 				int width = frames.get(i).getWidth();
 				int height = frames.get(i).getHeight();
 				mxCell mxFrame= (mxCell) graph.insertVertex(graph.getDefaultParent(), null, 
 						frames.get(i), x, y, width, height,"FRAME");
 				mxFrame.setConnectable(false);
 				graphicalTextboxes.add(i,mxFrame);
 			}
 		} finally {
 			graph.getModel().endUpdate();
 			graph.refresh();
 		}
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		if (e.getSource() == gridToggleAction) {
 			gridEnabled = gridToggleAction.isSelected();
 			if (gridEnabled) {
 				this.GRID_SIZE = 50;
 			}
 			else {
 				this.GRID_SIZE = 1;
 			}
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == copyAction) {
 			JOptionPane.showMessageDialog(null, "Not implemented yet!");
 		}
 		else if (e.getSource() == cutAction) {
 			JOptionPane.showMessageDialog(null, "Not implemented yet!");
 		}
 		else if (e.getSource() == pasteAction) {
 			JOptionPane.showMessageDialog(null, "Not implemented yet!");
 		}
 		else if (e.getSource() == selectAllAction) {
 			graph.selectAll();
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (toolBox.getClicked() != null) {
 			int x = e.getX();
 			int y = e.getY();
 			int xGrid = (int) Math.ceil(x / GRID_SIZE);
 			int yGrid = (int) Math.ceil(y / GRID_SIZE);
 			graph.getModel().beginUpdate();
 			try	{
 				if (toolBox.getClicked().equals("State")) {
 					State state = new State(UUID.randomUUID().toString(), "New...", false, false);
 					state.setXcoord(x);
 					state.setYcoord(y);
 					state.setWidth(this.WIDTH);
 					state.setHeight(this.HEIGHT);
 					this.machine.getStates().add(state);
 					graphicalStates.add((mxCell) graph.insertVertex(graph.getDefaultParent(), null, state, 
 							xGrid * GRID_SIZE, yGrid * GRID_SIZE, WIDTH, HEIGHT, "CIRCLE"));
 					this.graph.refresh();
 					toolBox.setClicked(null);
 					this.graph.setSelectionCell(graphicalStates.get(graphicalStates.size()-1));
 				}
 				else if (toolBox.getClicked().equals("Frame")) {
 					Frame frame = new Frame(x, y, this.WIDTH, this.HEIGHT);
 					this.machine.getFrames().add(frame);
 					mxCell mxFrame = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, frame, x, y, WIDTH, HEIGHT, "FRAME");
 					mxFrame.setConnectable(false);
 					graphicalFrames.add(mxFrame);
 					this.graph.refresh();
 					toolBox.setClicked(null);
 					this.graph.setSelectionCell(graphicalFrames.get(graphicalFrames.size()-1));
 				}
 				else if (toolBox.getClicked().equals("Text")) {
 					Textbox textbox = new Textbox("", x, y, this.WIDTH, this.HEIGHT);
 					this.machine.getTextboxes().add(textbox);
 					mxCell mxTextbox = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, textbox, x, y, WIDTH, HEIGHT, "TEXTBOX");
 					mxTextbox.setConnectable(false);
 					graphicalTextboxes.add(mxTextbox);
 					this.graph.refresh();
 					toolBox.setClicked(null);
 					this.graph.setSelectionCell(graphicalTextboxes.get(graphicalTextboxes.size()-1));
 				}
 			} finally {
 				graph.getModel().endUpdate();
 			}
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		mxCell mxEdge = (mxCell) graph.getSelectionCell();
 		if(mxEdge != null && mxEdge.isEdge()) {
 			mxGeometry g = mxEdge.getGeometry();
 			Edge edge = (Edge) mxEdge.getValue();
 			edge.setPosLabelX((int) g.getX());
 			edge.setPosLabelY((int) g.getY());
 			
 			edge.getVia().clear();
 			for (mxPoint p : mxEdge.getGeometry().getPoints()) {
 				edge.getVia().add(new Point((int)p.getX(), (int)p.getY()));
 			}
 			graph.refresh();
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
 			Object[] deletedCells = this.graph.removeCells();
 			for (int i = 0; i < deletedCells.length; i++) {
 				mxCell currentCell = (mxCell) deletedCells[i];
 				if (currentCell.isEdge()) {
 					Edge edge = (Edge) currentCell.getValue();
 					for (int j = 0; j < this.machine.getEdges().size(); j++) {
 						if (this.machine.getEdges().get(j) == edge) {
 							this.machine.getEdges().remove(j);
 						}
 					}
 				}
 				else if(currentCell.isVertex()) {
 					if(currentCell.getValue() instanceof State) {
 						State state = (State) currentCell.getValue();
 						for (int j = 0; j < this.machine.getStates().size(); j++) {
 							if (this.machine.getStates().get(j) == state) {
 								this.machine.getStates().remove(j);
 							}
 						}
 					}
 					else if(currentCell.getValue() instanceof Textbox) {
 						Textbox textbox = (Textbox) currentCell.getValue();
 						for(int k = 0; k < this.machine.getTextboxes().size(); k++) {
 							if(this.machine.getTextboxes().get(k) == textbox)
 								this.machine.getTextboxes().remove(k);
 						}
 					}
 					else if(currentCell.getValue() instanceof Frame) {
 						Frame frame = (Frame) currentCell.getValue();
 						for(int l = 0; l < this.machine.getFrames().size(); l++) {
 							if(this.machine.getFrames().get(l) == frame)
 								this.machine.getFrames().remove(l);
 						}
 					}
 				}
 			}
 			displayProperties();
 		} else if (!graph.isSelectionEmpty()){
 			int dx = 0, 
 				dy = 0;
 			if (e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN){
 				dy = GRID_SIZE;
 			} else if (e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_UP)	{
 				dy = -GRID_SIZE;
 			} else if (e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_LEFT) {
 				dx = -GRID_SIZE;
 			} else if (e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
 				dx = GRID_SIZE;
 			}
 				
 			graph.moveCells(graph.getSelectionCells(), dx, dy);
 			
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 	}
 
 	@Override
 	public void update(Observable observable, Object obj) {
 		System.out.println("Notified");
 
 		if(obj instanceof State){
 			System.out.println("is State");
 			if (selectedState != null){
 				if(selectedState.getStyle()=="FINAL_SELECTED"){
 					selectedState.setStyle("FINAL");
 				} else {
 					selectedState.setStyle("CIRCLE");
 				}
 			}
 			selectedState = graphicalStates.getMxCell((State)obj);
 
 			if (((State)obj).isFinalState()) {
 				selectedState.setStyle("FINAL_SELECTED");
 			} else {
 				selectedState.setStyle("CIRCLE_SELECTED");
 			}
 		}
 		if(obj instanceof Edge){
 			System.out.println("is Edge");
 			if (selectedEdge != null){
 				if(selectedEdge.getStyle()=="EDGE_SELECTED"){
 					selectedEdge.setStyle("EDGE");
 				}
 			}
 			selectedEdge = graphicalEdges.getMxCell(((Edge)obj).getFrom(), ((Edge)obj).getTo());
 			selectedEdge.setStyle("EDGE_SELECTED");
 		}
 		if(obj instanceof Simulation.simulationState){
 			System.out.println("is simulation State");
 			if (((Simulation.simulationState)obj)==Simulation.simulationState.ABORTED){
 				if(selectedState.getStyle()=="FINAL_SELECTED"){
 					selectedState.setStyle("FINAL");
 				} else {
 					selectedState.setStyle("CIRCLE");
 				}
 			} else if (((Simulation.simulationState)obj)==Simulation.simulationState.FINISHED){
 				selectedEdge.setStyle("EDGE");
 			}
 		}
 		graph.refresh();			
 		graph.repaint();
 
 	}
 	
 	public void initStyles(mxStylesheet stylesheet) {
 		Hashtable<String, Object> styleCircle = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleStart = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleFinal = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleFinalStart = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleTextbox = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleEdge = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleFrame = new Hashtable<String, Object>();
 
 		Hashtable<String, Object> styleSelectedCircle = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleSelectedFinal = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleSelectedStart = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleSelectedFinalStart = new Hashtable<String, Object>();
 		Hashtable<String, Object> styleSelectedEdge = new Hashtable<String, Object>();
 		
 		styleStart.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
 		styleStart.put(mxConstants.STYLE_STROKEWIDTH, 2);
 		stylesheet.putCellStyle("START", styleStart);
 		
 		styleCircle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
 		stylesheet.putCellStyle("CIRCLE", styleCircle);
 		
 		styleFinal.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_ELLIPSE);
 		stylesheet.putCellStyle("FINAL", styleFinal);
 		
 		styleFinalStart.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_ELLIPSE);
 		styleFinalStart.put(mxConstants.STYLE_STROKEWIDTH, 2);
 		stylesheet.putCellStyle("FINALSTART", styleFinalStart);
 		
 		styleTextbox.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
 		styleTextbox.put(mxConstants.STYLE_FILLCOLOR, "#FBFF8B");
 		styleTextbox.put(mxConstants.STYLE_STROKECOLOR, "#FBFF8B");
 		styleTextbox.put(mxConstants.STYLE_SHADOW, true);
 		stylesheet.putCellStyle("TEXTBOX", styleTextbox);
 		stylesheet.putCellStyle("EDGE", styleEdge);
 
 		styleFrame.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
 		styleFrame.put(mxConstants.STYLE_FILLCOLOR, "none");
 		styleFrame.put(mxConstants.STYLE_STROKECOLOR, "black");
 		styleFrame.put(mxConstants.STYLE_DASHED, true);
 		stylesheet.putCellStyle("FRAME", styleFrame);
 		
 		styleSelectedCircle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
 		styleSelectedCircle.put(mxConstants.STYLE_FILLCOLOR, "yellow");
 		stylesheet.putCellStyle("CIRCLE_SELECTED", styleSelectedCircle);
 		
 		styleSelectedFinal.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_ELLIPSE);
 		styleSelectedFinal.put(mxConstants.STYLE_FILLCOLOR, "yellow");
 		stylesheet.putCellStyle("FINAL_SELECTED", styleSelectedFinal);
 		
 		styleSelectedStart.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
 		styleSelectedStart.put(mxConstants.STYLE_FILLCOLOR, "yellow");
 		styleSelectedStart.put(mxConstants.STYLE_STROKEWIDTH, 2);
 		stylesheet.putCellStyle("FINAL_SELECTED", styleSelectedStart);
 		
 		styleSelectedFinalStart.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_ELLIPSE);
 		styleSelectedFinalStart.put(mxConstants.STYLE_FILLCOLOR, "yellow");
 		styleSelectedFinalStart.put(mxConstants.STYLE_STROKEWIDTH, 2);
 		stylesheet.putCellStyle("FINAL_SELECTED", styleSelectedFinalStart);
 		
 		styleSelectedEdge.put(mxConstants.STYLE_STROKECOLOR, "yellow");
 		stylesheet.putCellStyle("EDGE_SELECTED", styleSelectedEdge);
 	}
 }
