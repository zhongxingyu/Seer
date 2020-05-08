 package gui.turing;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
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
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 
 import tape.Tape;
 
 import machine.turing.Edge;
 import machine.turing.State;
 import machine.turing.TuringMachine;
 
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.swing.handler.mxRubberband;
 
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
 
 /**
  * This class implements the TuringMachineEditor
  * @author Nils Breyer, Nessa Baier, Philipp Neumann, Sven Schuster, David Wille
  *
  */
 
 public class TuringMachineEditor extends MachineEditor 
 implements KeyListener, ItemListener, ActionListener, MouseListener, Observer {
 
 	private static final long serialVersionUID = 7647012826073382156L;
 	private int GRID_SIZE = 50;
 	private final int WIDTH = 50;
 	private final int HEIGHT = 50;
 	private boolean initialized = false;
 	private TuringMachine machine;
 	private boolean inputWordWritten = false;
 	private mxCell selectedState = null;
 	private mxCell selectedEdge = null;
 	private mxCell lastSelectedEdge = null;
 	protected ArrayList<TuringMachineState> turingMachineStates = null;
 	protected int currentStateIndex = -1;
 	protected boolean undoing = false; 
 
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
 	private JMenuItem undoAction;
 	private JMenuItem redoAction;
 	private JMenuItem addViaAction;
 	private JMenuItem removeViaAction;
 	private JCheckBoxMenuItem gridToggleAction;
 
 	private boolean gridEnabled = true;
 
 	public TuringMachineEditor(final TuringMachine machine) {
 		super();
 		this.machine = machine;
 
 		this.initEditor();
 
 		this.turingMachineStates = new ArrayList<TuringMachineState>();
 
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
 		this.hideLeftSplitPane(false);
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
 
 		this.graph.addListener(mxEvent.CELLS_ADDED, new mxIEventListener() {
 			@Override
 			public void invoke(Object obj, mxEventObject e) {
 				for(Object cellObj: (Object[]) e.getProperty("cells")){
 					mxCell cell = (mxCell) cellObj;
 					if (cell.getValue() == null) {
 						if (cell.getStyle().equals("CIRCLE")) {
 							State state = new State(UUID.randomUUID().toString(), "New...", false, false);
 							state.setXcoord((int)cell.getGeometry().getX());
 							state.setYcoord((int)cell.getGeometry().getY());
 							state.setWidth(WIDTH);
 							state.setHeight(HEIGHT);
 							cell.setValue(state);
 							machine.getStates().add(state);
 							System.out.println("blubb:" + machine);
 							graph.refresh();
 							toolBox.setClicked(null);
 							graph.setSelectionCell(cell);
 							addUndoableEdit("State inserted");
 						}
 						if (cell.getStyle().equals("FRAME")) {
 							Frame frame = new Frame((int)cell.getGeometry().getX(), (int)cell.getGeometry().getY(), WIDTH, HEIGHT);
 							machine.getFrames().add(frame);
 							cell.setValue(frame);
 							cell.setConnectable(false);
 							graph.refresh();
 							toolBox.setClicked(null);
 							graph.setSelectionCell(cell);
 							addUndoableEdit("Frame inserted");
 						}
 						if (cell.getStyle().equals("TEXTBOX")) {
 							Textbox textbox = new Textbox("", (int)cell.getGeometry().getX(), (int)cell.getGeometry().getY(), WIDTH, HEIGHT);
 							machine.getTextboxes().add(textbox);
 							cell.setValue(textbox);
 							cell.setConnectable(false);
 							graph.refresh();
 							toolBox.setClicked(null);
 							graph.setSelectionCell(cell);
 							addUndoableEdit("Textbox inserted");
 						}
 						setResizable();
 					}
 				}
 			}
 		});
 
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
 							addUndoableEdit("State moved: " + ((State)cell.getValue()).getName());
 						}
 						else if(cell.getValue() instanceof Textbox) {
 							((Textbox)cell.getValue()).setX((int)cell.getGeometry().getX());
 							((Textbox)cell.getValue()).setY((int)cell.getGeometry().getY());
 							addUndoableEdit("Textbox moved");
 						}
 						else if(cell.getValue() instanceof Frame) {
 							((Frame)cell.getValue()).setX((int)cell.getGeometry().getX());
 							((Frame)cell.getValue()).setY((int)cell.getGeometry().getY());
 							addUndoableEdit("Frame moved");
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
 					if(cell.getValue() instanceof State) {
 						displayProperties((State) cell.getValue(), graph.getView().getState(cell));
 					}
 					else if(cell.getValue() instanceof Textbox) {
 						displayProperties((Textbox) cell.getValue());
 					}
 					else if(cell.getValue() instanceof Frame) {
 						displayProperties();
 					}
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
 					Edge oldEdge = null;
 					if (lastSelectedEdge != null) {
 						oldEdge = (Edge) lastSelectedEdge.getValue();
						lastSelectedEdge = null;
 					}
 					mxCell graphEdge = (mxCell) e.getProperty("edge");
 					mxICell source = ((mxCell) graphEdge).getSource();
 					mxICell target = ((mxCell) graphEdge).getTarget();
 					if(source != null && target != null) {
 						Edge edge = null;
 						if (oldEdge != null && oldEdge.getTransitions().size() != 0) {
 							edge = new Edge((State) (graphEdge.getSource().getValue()),(State)(graphEdge.getTarget().getValue()),oldEdge.getTransitions());
 							machine.getEdges().remove(oldEdge);
 						}
 						else {
 							edge = new Edge((State) (graphEdge.getSource().getValue()),(State)(graphEdge.getTarget().getValue()),new ArrayList<Transition>());
 						}
 						graphEdge.setValue(edge);
 						machine.getEdges().add(edge);
 						graph.refresh();
 						graph.repaint();
 					}
 					addUndoableEdit("Edge inserted");
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
 						addUndoableEdit("Textbox resized");
 					}
 					else if(cell.getValue() instanceof Frame) {
 						Frame frame = (Frame) cell.getValue();
 						frame.setWidth((int) g.getWidth());
 						frame.setHeight((int) g.getHeight());
 						addUndoableEdit("Frame resized");
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
 		new mxRubberband(graphComponent);
 
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
 		undoAction = new JMenuItem("Undo");
 		redoAction = new JMenuItem("Redo");
 		pasteAction = new JMenuItem("Paste");
 		addViaAction = new JMenuItem("Add via point");
 		removeViaAction = new JMenuItem("Remove via point");
 
 		gridToggleAction = new JCheckBoxMenuItem("Snap to grid");
 		gridToggleAction.setSelected(true);
 
 		addViaAction.setEnabled(false);
 		removeViaAction.setEnabled(false);
 
 		editMenu.add(undoAction);
 		editMenu.add(redoAction);
 		editMenu.addSeparator();
 		editMenu.add(copyAction);
 		editMenu.add(cutAction);
 		editMenu.add(pasteAction);
 		editMenu.addSeparator();
 		editMenu.add(selectAllAction);
 		editMenu.addSeparator();
 		editMenu.add(addViaAction);
 		editMenu.add(removeViaAction);
 
 		viewMenu.add(gridToggleAction);
 
 		this.getMenus().add(editMenu);
 		this.getMenus().add(viewMenu);
 
 		undoAction.setAccelerator(KeyStroke.getKeyStroke('Z', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		redoAction.setAccelerator(KeyStroke.getKeyStroke('Y', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		copyAction.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		cutAction.setAccelerator(KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		pasteAction.setAccelerator(KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		selectAllAction.setAccelerator(KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		addViaAction.setAccelerator(KeyStroke.getKeyStroke('T', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		removeViaAction.setAccelerator(KeyStroke.getKeyStroke('T', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
 
 		undoAction.addActionListener(this);
 		redoAction.addActionListener(this);
 		copyAction.addActionListener(this);
 		cutAction.addActionListener(this);
 		pasteAction.addActionListener(this);
 		selectAllAction.addActionListener(this);
 		addViaAction.addActionListener(this);
 		removeViaAction.addActionListener(this);
 
 		gridToggleAction.addItemListener(this);
 	}
 
 	/**
 	 * Displays the information about the Turing machine
 	 */
 	private void displayProperties() {
 		addViaAction.setEnabled(false);
 		removeViaAction.setEnabled(false);
 
 		PropertiesTuringMachine propertiesMachine = new PropertiesTuringMachine(machine);
 		jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesMachine, BorderLayout.PAGE_START);
 		jPanelProperties.validate();
 	}
 
 	/**
 	 * Displays the information of an edge
 	 * @param edge Selected edge
 	 */
 	private void displayProperties(Edge edge) {
 		addViaAction.setEnabled(true);
 		removeViaAction.setEnabled(true);
 
 		PropertiesEdge propertiesEdge = new PropertiesEdge(this.machine.getNumberOfTapes(), edge, graph);
 		jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesEdge, BorderLayout.CENTER);
 		jPanelProperties.validate();
 	}
 
 	/**
 	 * Displays the information of a state
 	 * @param state Selected state
 	 * @param mxState mxState of the selected state
 	 */
 	private void displayProperties(State state, mxCellState mxState) {
 		addViaAction.setEnabled(false);
 		removeViaAction.setEnabled(false);
 
 		PropertiesState propertiesState = new PropertiesState(state,graph, mxState);
 		this.jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesState, BorderLayout.PAGE_START);
 		jPanelProperties.validate();
 	}
 
 	/**
 	 * Displays the information of a textbox
 	 * @param textbox Selected textbox
 	 */
 	private void displayProperties(Textbox textbox) {
 		addViaAction.setEnabled(false);
 		removeViaAction.setEnabled(false);
 
 		PropertiesTextbox propertiesTextbox = new PropertiesTextbox(textbox, graph);
 		this.jPanelProperties.removeAll();
 		jPanelProperties.validate();
 		jPanelProperties.repaint();
 		jPanelProperties.add(propertiesTextbox, BorderLayout.PAGE_START);
 		jPanelProperties.validate();
 	}
 
 	/**
 	 * Draws the graph
 	 */
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
 				graph.insertVertex(graph.getDefaultParent(), null, 
 						states.get(i), x * GRID_SIZE, y * GRID_SIZE, 
 						states.get(i).getWidth(), states.get(i).getHeight(),
 						(states.get(i).isFinalState() && states.get(i).isStartState() ? "FINALSTART" :
 							(states.get(i).isFinalState() ? "FINAL" : 
 								((states.get(i).isStartState() ? "START" : 
 										"CIRCLE")))));
 			}
 			//insert graphical Edges
 			Edge currentEdge = null;
 			Object v1 = null;
 			Object v2 = null;
 			for (int i = 0; i < edges.size(); i++){
 				currentEdge = edges.get(i);
 				v1 = this.getStateCell(currentEdge.getFrom());
 				v2 = this.getStateCell(currentEdge.getTo());
 				mxCell edge = (mxCell) graph.insertEdge(graph.getDefaultParent(), null, currentEdge, v1, v2);
 				edge.getGeometry().setX(currentEdge.getPosLabel().getX());
 				edge.getGeometry().setY(currentEdge.getPosLabel().getY());
 
 				//set via control points
 				ArrayList<mxPoint> points = new ArrayList<mxPoint>();
 				for (Point p : currentEdge.getVia()) {
 					points.add(new mxPoint(p.getX(),p.getY()));
 				}
 				edge.getGeometry().setPoints(points);
 			}
 
 			for (int i = 0;  i < textboxes.size(); i++){
 				int x = textboxes.get(i).getX();
 				int y = textboxes.get(i).getY();
 				int width = textboxes.get(i).getWidth();
 				int height = textboxes.get(i).getHeight();
 				mxCell mxTextbox = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, 
 						textboxes.get(i), x, y, width, height,"TEXTBOX");
 				mxTextbox.setConnectable(false);
 			}
 
 			for (int i = 0;  i < frames.size(); i++){
 				int x = frames.get(i).getX();
 				int y = frames.get(i).getY();
 				int width = frames.get(i).getWidth();
 				int height = frames.get(i).getHeight();
 				mxCell mxFrame= (mxCell) graph.insertVertex(graph.getDefaultParent(), null, 
 						frames.get(i), x, y, width, height,"FRAME");
 				mxFrame.setConnectable(false);
 			}
 		} finally {
 			if(!initialized)
 				this.addUndoableEdit("Machine loaded");
 			this.updateUndoRedoMenu();
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
 		else if (e.getSource() == addViaAction) {
 			this.addVia();
 		}
 		else if(e.getSource() == redoAction) {
 			redo();
 		}
 		else if(e.getSource() == undoAction) {
 			undo();
 		}
 		else if (e.getSource() == removeViaAction) {
 			this.removeVia();
 		}
 	}
 
 	/**
 	 * Adds a via point to an edge
 	 */
 	private void addVia() {
 		if (this.graph.getSelectionCell() != null && ((mxCell)this.graph.getSelectionCell()).isEdge()) {
 			mxCell edge = (mxCell)this.graph.getSelectionCell();
 			List<mxPoint> points = edge.getGeometry().getPoints();
 			if (points == null) {
 				points = new ArrayList<mxPoint>();
 				edge.getGeometry().setPoints(points);
 			}
 
 			Point lastPoint;
 			if (points.size() > 0) {
 				lastPoint = new Point((int)points.get(points.size()-1).getX(), (int)points.get(points.size()-1).getY());
 			}
 			else {
 				lastPoint = new Point((int)edge.getSource().getGeometry().getCenterX(), (int)edge.getSource().getGeometry().getCenterY());
 			}
 			System.out.println("Last point: " + lastPoint.getX() + "," + lastPoint.getY());
 			System.out.println("Target point: " + edge.getTarget().getGeometry().getCenterX() + "," + edge.getTarget().getGeometry().getCenterY());
 
 			int x = (int)lastPoint.getX();
 			x += ((edge.getTarget().getGeometry().getCenterX() - lastPoint.getX())/2);
 			int y = (int)lastPoint.getY();
 			y += ((edge.getTarget().getGeometry().getCenterY() - lastPoint.getY())/2);
 			System.out.println("Mid point: " + x + "," + y);
 
 			points.add(new mxPoint(x,y));
 
 			Object[] selection = this.graph.getSelectionCells();
 			this.graph.setSelectionCells(new Object[0]);
 			this.graph.refresh();
 			this.graph.repaint();
 			this.graph.setSelectionCells(selection);
 			this.graph.refresh();
 			this.graph.repaint();
 		}
 	}
 
 	/**
 	 * Removes a via point from an edge
 	 */
 	private void removeVia() {
 		if (this.graph.getSelectionCell() != null && ((mxCell)this.graph.getSelectionCell()).isEdge()) {
 			mxCell edge = (mxCell)this.graph.getSelectionCell();
 			List<mxPoint> points = edge.getGeometry().getPoints();
 			if (points == null) {
 				points = new ArrayList<mxPoint>();
 				edge.getGeometry().setPoints(points);
 			}
 
 			if (points.size() > 0) {
 				points.remove(points.size()-1);
 			}
 
 			Object[] selection = this.graph.getSelectionCells();
 			this.graph.setSelectionCells(new Object[0]);
 			this.graph.refresh();
 			this.graph.repaint();
 			this.graph.setSelectionCells(selection);
 			this.graph.refresh();
 			this.graph.repaint();
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
 					graph.insertVertex(graph.getDefaultParent(), null, null, 
 							xGrid * GRID_SIZE, yGrid * GRID_SIZE, WIDTH, HEIGHT, "CIRCLE");
 					this.graph.refresh();
 					toolBox.setClicked(null);
 				}
 				else if (toolBox.getClicked().equals("Frame")) {
 					graph.insertVertex(graph.getDefaultParent(), null, null, x, y, WIDTH, HEIGHT, "FRAME");
 					this.graph.refresh();
 					toolBox.setClicked(null);
 				}
 				else if (toolBox.getClicked().equals("Text")) {
 					graph.insertVertex(graph.getDefaultParent(), null, null, x, y, WIDTH, HEIGHT, "TEXTBOX");
 					this.graph.refresh();
 					toolBox.setClicked(null);
 				}
 			} finally {
 				graph.getModel().endUpdate();
 			}
 		}
 		setResizable();
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
 			if (mxEdge.getGeometry().getPoints() != null) {
 				for (mxPoint p : mxEdge.getGeometry().getPoints()) {
 					edge.getVia().add(new Point((int)p.getX(), (int)p.getY()));
 				}
 			}
 
 			graph.refresh();
 			this.addUndoableEdit("Label moved");
 			lastSelectedEdge = mxEdge;
 		}
 		else {
 			lastSelectedEdge = null;
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
 							this.addUndoableEdit("Edge removed");
 						}
 					}
 				}
 				else if(currentCell.isVertex()) {
 					if(currentCell.getValue() instanceof State) {
 						State state = (State) currentCell.getValue();
 						for (int j = 0; j < this.machine.getStates().size(); j++) {
 							if (this.machine.getStates().get(j) == state) {
 								this.machine.getStates().remove(j);
 								this.addUndoableEdit("State removed");
 							}
 						}
 					}
 					else if(currentCell.getValue() instanceof Textbox) {
 						Textbox textbox = (Textbox) currentCell.getValue();
 						for(int k = 0; k < this.machine.getTextboxes().size(); k++) {
 							if(this.machine.getTextboxes().get(k) == textbox) {
 								this.machine.getTextboxes().remove(k);
 								this.addUndoableEdit("Textbox removed");
 							}
 						}
 					}
 					else if(currentCell.getValue() instanceof Frame) {
 						Frame frame = (Frame) currentCell.getValue();
 						for(int l = 0; l < this.machine.getFrames().size(); l++) {
 							if(this.machine.getFrames().get(l) == frame) {
 								this.machine.getFrames().remove(l);
 								this.addUndoableEdit("Frame removed");
 							}
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
 				} 
 				else if(selectedState.getStyle()=="START_SELECTED"){
 					selectedState.setStyle("START");
 				}
 				else if(selectedState.getStyle()=="FINALSTART_SELECTED") {
 					selectedState.setStyle("FINALSTART");
 				}
 				else {
 					selectedState.setStyle("CIRCLE");
 				}
 			}
 			selectedState = this.getStateCell(((State)obj));
 
 			if(inputWordWritten){
 				if (((State)obj).isFinalState() & ((State)obj).isStartState()) {
 					selectedState.setStyle("FINALSTART_SELECTED");
 				} 
 				else if(((State)obj).isFinalState()) {
 					selectedState.setStyle("FINAL_SELECTED");
 				}
 				else if(((State)obj).isStartState()) {
 					selectedState.setStyle("START_SELECTED");
 				}
 				else {
 					selectedState.setStyle("CIRCLE_SELECTED");
 				}
 			}
 		}
 		else if(obj instanceof Edge){
 			System.out.println("is Edge");
 			if (selectedEdge != null){
 				if(selectedEdge.getStyle()=="EDGE_SELECTED"){
 					selectedEdge.setStyle("EDGE");
 				}
 			}
 			selectedEdge = this.getEdgeCell(((Edge)obj).getFrom(), ((Edge)obj).getTo());
 			selectedEdge.setStyle("EDGE_SELECTED");
 		}
 		else if(obj instanceof Simulation.simulationState){
 			System.out.println("is simulation State");
 			if (((Simulation.simulationState)obj)==Simulation.simulationState.ABORTED){
 				if(selectedState.getStyle()=="FINAL_SELECTED"){
 					selectedState.setStyle("FINAL");
 				} 
 				else if(selectedState.getStyle()=="START_SELECTED") {
 					selectedState.setStyle("START");
 				}
 				else if(selectedState.getStyle()=="FINALSTART_SELECTED") {
 					selectedState.setStyle("FINALSTART");
 				}
 				else {
 					selectedState.setStyle("CIRCLE");
 				}
 				if(selectedEdge!= null){
 				selectedEdge.setStyle("EDGE");
 				}
 				inputWordWritten = false;
 				this.setEditable(true);
 			} else if (((Simulation.simulationState)obj)==Simulation.simulationState.FINISHED){
 				selectedEdge.setStyle("EDGE");
 				inputWordWritten = false;
 				this.setEditable(true);
 			}
 		}
 		else if (obj instanceof Tape.Event){
 			if((Tape.Event)obj==Tape.Event.INPUTFINISHED){ //FIXME: is this ok? seems like it is executed if any tape has finished, not all
 				inputWordWritten = true;
 				if (selectedState != null){
 					if(selectedState.getStyle()=="FINAL"){
 						selectedState.setStyle("FINAL_SELECTED");
 					} 
 					else if(selectedState.getStyle()=="START"){
 						selectedState.setStyle("START_SELECTED");
 					}
 					else if(selectedState.getStyle()=="FINALSTART") {
 						selectedState.setStyle("FINALSTART_SELECTED");
 					}
 				}
 			}
 		}
 		graph.refresh();			
 		graph.repaint();
 	}
 
 	@Override
 	public void setEditable(boolean editable) {
 		this.graph.setCellsMovable(editable);
 		this.graph.setCellsResizable(editable);
 		this.graph.setCellsDeletable(editable);
 		this.graph.setCellsSelectable(editable);
 		this.graph.clearSelection();
 		this.setEditMenuItemsSelectable(editable);
 		this.hideLeftSplitPane(!editable);
 	}
 	
 	public void setEditMenuItemsSelectable(boolean selectable) {
 		this.selectAllAction.setEnabled(selectable);
 		this.gridToggleAction.setEnabled(selectable);
 		this.copyAction.setEnabled(selectable);
 		this.cutAction.setEnabled(selectable);
 		this.pasteAction.setEnabled(selectable);
 		if (selectable) {
 			this.updateUndoRedoMenu();
 		}
 		else {
 			this.undoAction.setEnabled(selectable);
 			this.redoAction.setEnabled(selectable);
 			this.addViaAction.setEnabled(selectable);
 			this.removeViaAction.setEnabled(selectable);
 		}
 	}
 
 	/**
 	 * Hides the toolbar
 	 * @param hide true / false
 	 */
 	public void hideLeftSplitPane(boolean hide) {
 		if (hide) {
 			this.jSplitPaneHorizontal.setDividerLocation(0);
 			this.jSplitPaneHorizontal.setEnabled(false);
 			this.jPanelLeft.setMinimumSize(new Dimension(0, 0));
 		}
 		else {
 			this.jSplitPaneHorizontal.setDividerLocation(250);
 			this.jSplitPaneHorizontal.setEnabled(true);
 			this.jPanelLeft.setMinimumSize(new Dimension(200, 100));
 		}
 	}
 
 	public boolean canUndo() {
 		return this.currentStateIndex > 0;
 	}
 
 	public boolean canRedo() {
 		return this.currentStateIndex < this.turingMachineStates.size()-1;
 	}
 	
 	public void updateUndoRedoMenu() {
 		if (canUndo()) {
 			this.undoAction.setText("Undo " + this.turingMachineStates.get(this.currentStateIndex).getName());
 			this.undoAction.setEnabled(true);
 		}
 		else {
 			this.undoAction.setText("Undo");
 			this.undoAction.setEnabled(false);
 		}
 		if (canRedo()) {
 			this.redoAction.setText("Redo " + this.turingMachineStates.get(this.currentStateIndex+1).getName());
 			this.redoAction.setEnabled(true);
 		}
 		else {
 			this.redoAction.setText("Redo");
 			this.redoAction.setEnabled(false);
 		}
 	}
 
 	public void addUndoableEdit(String name) {
 		if(!undoing) {
 			System.out.println(name);
 			while(turingMachineStates.size()-1 > this.currentStateIndex)
 				turingMachineStates.remove(turingMachineStates.size()-1);
 			turingMachineStates.add(new TuringMachineState(name, (TuringMachine) machine.clone()));
 			this.currentStateIndex++;
 			System.out.println(currentStateIndex);
 			System.out.println(turingMachineStates.size());
 			System.out.println(turingMachineStates);
 		}
 		this.updateUndoRedoMenu();
 	}
 
 	public void undo() {
 		if(canUndo()) {
 			this.undoing = true;
 			graph.selectAll();
 			graph.removeCells(graph.getSelectionCells());
 			this.currentStateIndex--;
 			this.machine = this.turingMachineStates.get(this.currentStateIndex).getMachine();
 			this.drawGraph();
 			graph.refresh();
 			graph.repaint();
 			System.out.println(this.currentStateIndex);
 			System.out.println(turingMachineStates);
 			this.undoing = false;
 		}
 		this.updateUndoRedoMenu();
 	}
 
 	public void redo() {
 		if(canRedo()) {
 			this.undoing = true;
 			graph.selectAll();
 			graph.removeCells(graph.getSelectionCells());
 			this.currentStateIndex++;
 			this.machine = this.turingMachineStates.get(this.currentStateIndex).getMachine();
 			this.drawGraph();
 			this.undoing = false;
 		}
 		this.updateUndoRedoMenu();
 	}
 	
 	mxCell getStateCell(State state){							
 		for (Object cell : this.graph.getChildVertices(graph.getDefaultParent())) {
 			mxCell mxCell = (mxCell) cell;
 			if(mxCell.getValue().equals((Object) state)){
 				return mxCell;
 			}
 		}
 		return null;
 	}
 
 	mxCell getEdgeCell(State from, State to){							
 		for (Object cell : this.graph.getChildEdges(graph.getDefaultParent())) {
 			mxCell mxCell = (mxCell) cell;
 			if(mxCell.getSource().getValue() == from && mxCell.getTarget().getValue() == to){
 				return mxCell;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Initializes the styles
 	 * @param stylesheet Stylesheet that should be edited
 	 */
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
 		stylesheet.putCellStyle("START_SELECTED", styleSelectedStart);
 
 		styleSelectedFinalStart.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_ELLIPSE);
 		styleSelectedFinalStart.put(mxConstants.STYLE_FILLCOLOR, "yellow");
 		styleSelectedFinalStart.put(mxConstants.STYLE_STROKEWIDTH, 2);
 		stylesheet.putCellStyle("FINALSTART_SELECTED", styleSelectedFinalStart);
 
 		styleSelectedEdge.put(mxConstants.STYLE_STROKECOLOR, "yellow");
 		stylesheet.putCellStyle("EDGE_SELECTED", styleSelectedEdge);
 	}
 	
 	public void setResizable() {
 		mxCell cell = (mxCell) graph.getSelectionCell();
 		// set cells unresizable if state selected
 		if (cell != null && cell.getValue() instanceof State) {
 			this.graph.setCellsResizable(false);
 			this.graph.refresh();
 			this.graph.repaint();
 			this.graph.clearSelection();
 			this.graph.setSelectionCell(cell);
 		}
 		else {
 			this.graph.setCellsResizable(true);
 			this.graph.refresh();
 			this.graph.repaint();
 			this.graph.clearSelection();
 			this.graph.setSelectionCell(cell);
 		}
 	}
 }
