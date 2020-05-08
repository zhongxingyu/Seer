 package main.java.scalax.automata.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTextField;
 import com.mxgraph.canvas.mxGraphics2DCanvas;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.view.mxGraph;
 
 @SuppressWarnings("serial")
 public class GUI extends JFrame {
 	private static final int CELL_RADIUS = 80;
 	private final static int INITIAL_STATE = 1;
 	private final static int NORMAL_STATE = 2;
 	private final static int END_STATE = 4;
 
 	public mxGraph graph;
 	public Object root;
 	
 	private HashMap<String, String> initialState = new HashMap<String, String>();
 	private ArrayList<HashMap<String, String>> endStates = new ArrayList<HashMap<String, String>>();
 	private ArrayList<HashMap<String, String>> states = new ArrayList<HashMap<String, String>>();
 	private ArrayList<HashMap<String, String>> transitions = new ArrayList<HashMap<String, String>>();
 
 	private Component top;
 	private int nextInt = 1;
 	private boolean hasInitialState = false;
 	private JPopupMenu popup;
 	private Point popupPosition = new Point();
 	private mxGraphComponent graphComponent = null;
 	private JTextField alphabetField, testField;
 	private JLabel status;
 	
 	public GUI (String name) {
 		super(name);
 		top = getContentPane();
 	}
 	
 	public void initGUI(JFrame gui) {
 		JMenu menu;
 		JMenuItem menuItem;
 		JMenuBar menuBar = new JMenuBar();
 		JPanel sidePanel, statusBar, subPanel, statesPanel, transitionsPanel, infoPanel, buttonPanel;
 		JButton runButton, quitButton;
 
 	    //Create the pop-up menu.
 	    popup = new JPopupMenu();
 	    menuItem = new JMenuItem("Add state");
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// generate a new name for the cell
 				StringBuilder value = new StringBuilder("S_").append(nextInt);
 				addState(value.toString(), popupPosition.x, popupPosition.y, CELL_RADIUS, NORMAL_STATE);
 			}
 	    });
 	    popup.add(menuItem);
 	    
 	    menuItem = new JMenuItem("Remove");
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Object cell[] = {graphComponent.getCellAt(popupPosition.x, popupPosition.y)};
 				graph.removeCells(cell, true);
 			}
 	    });
 	    popup.add(menuItem);
 	    popup.addSeparator();
 	    
 	    menuItem = new JMenuItem("Change to Initial state");
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (hasInitialState) {
 					JOptionPane.showMessageDialog(top, "There can only be one initial state!\n" +
 							"Remove or change the existing initial state first.", "Warning", JOptionPane.WARNING_MESSAGE);
 				}
 				else {
 					hasInitialState = true;
 					Object cell = graphComponent.getCellAt(popupPosition.x, popupPosition.y);
 					graph.getModel().setStyle(cell, "shape=initialShape;perimeter=ellipsePerimeter");
 					graphComponent.refresh();
 				}
 			}
 	    });
 	    popup.add(menuItem);
 	    
 	    menuItem = new JMenuItem("Change to Normal state");
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				hasInitialState = false;
 				Object cell = graphComponent.getCellAt(popupPosition.x, popupPosition.y);
 				graph.getModel().setStyle(cell, "shape=ellipse;perimeter=ellipsePerimeter");
 				graphComponent.refresh();
 			}
 	    });
 	    popup.add(menuItem);
 	    
 	    menuItem = new JMenuItem("Change to End state");
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Object cell = graphComponent.getCellAt(popupPosition.x, popupPosition.y);
 				graph.getModel().setStyle(cell, "shape=doubleEllipse;perimeter=ellipsePerimeter");
 				graphComponent.refresh();
 			}
 	    });
 	    popup.add(menuItem);
 		
 		// menus
 		menu = new JMenu("File");
 		menu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("New");
 		menuItem.setMnemonic(KeyEvent.VK_N);
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				removeAllCells();
 			}
 	    });
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Open");
 		menuItem.setMnemonic(KeyEvent.VK_O);
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				removeAllCells();
 				loadAutomata();
 			}
 	    });
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem("Save");
 		menuItem.setMnemonic(KeyEvent.VK_S);
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				saveAutomata();
 			}
 	    });
 		menu.add(menuItem);
 		menu.addSeparator();
 		
 		menuItem = new JMenuItem("Quit");
 		menuItem.setMnemonic(KeyEvent.VK_Q);
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				quit();
 			}
 	    });
 		menu.add(menuItem);
 		
 		menu = new JMenu("Simulation");
 		menu.setMnemonic(KeyEvent.VK_S);
 		menuBar.add(menu);
 		
 		menuItem = new JMenuItem("Run");
 		menuItem.setMnemonic(KeyEvent.VK_R);
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				runSimulation();
 			}
 	    });
 		menu.add(menuItem);
 		
 		menu = new JMenu("Info");
 		menu.setMnemonic(KeyEvent.VK_I);
 		menuBar.add(menu);
 		
 		menuItem = new JMenuItem("About");
 		menuItem.setMnemonic(KeyEvent.VK_A);
 	    menuItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				displayAbout();
 			}
 	    });
 		menu.add(menuItem);
 		
 		statusBar = new JPanel();
 		sidePanel = new JPanel();
 		subPanel = new JPanel();
 		buttonPanel = new JPanel();
 		statesPanel = new JPanel();
 		transitionsPanel = new JPanel();
 		infoPanel = new JPanel();
 		
 		sidePanel.setLayout(new BorderLayout());
 		sidePanel.setBorder(BorderFactory.createEtchedBorder());
 		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
 		statesPanel.setLayout(new BorderLayout());
 		statesPanel.setBorder(BorderFactory.createTitledBorder("States:"));
 		transitionsPanel.setLayout(new BorderLayout());
 		transitionsPanel.setBorder(BorderFactory.createTitledBorder("Transitions:"));
 		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
 		
 		alphabetField = new JTextField();
 		alphabetField.setEditable(false);
 		testField = new JTextField();
 		infoPanel.add(new JLabel("Alphabet:"));
 		infoPanel.add(alphabetField);
 		infoPanel.add(new JLabel("Word to test:"));
 		infoPanel.add(testField);
 		
 		runButton = new JButton("Run");
 	    runButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				runSimulation();
 			}
 	    });
 		quitButton = new JButton("Quit");
 	    quitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				quit();
 			}
 	    });
 		buttonPanel.add(runButton);
 		buttonPanel.add(quitButton);
 		
 		sidePanel.add(subPanel, BorderLayout.NORTH);
 		sidePanel.add(buttonPanel, BorderLayout.SOUTH);
 		// TODO: add a JTree (for example) for the states and their data (label, x, y)
 		subPanel.add(statesPanel);
 		// TODO: add a JTree (for example) for the edges and their data (input, source, target)
 		subPanel.add(transitionsPanel);
 		subPanel.add(infoPanel);
 		
 		status = new JLabel("");
 		statusBar.add(status);	
 		
 		gui.setJMenuBar(menuBar);
 		gui.setLayout(new BorderLayout());
 		gui.add(statusBar, BorderLayout.SOUTH);
 		gui.add(sidePanel, BorderLayout.EAST);
 		gui.add(initGraph(), BorderLayout.CENTER);
 	}
 	
 	public JComponent initGraph() {
 		graph = new mxGraph();
 		graphComponent = new mxGraphComponent(graph);
 		
 		// dangling edges are bad and result in all kinds of nasty things
 		graph.setAllowDanglingEdges(false);
 		// edge source and target are the same
 		graph.setAllowLoops(true);
 		// don't need this
 		graph.setCellsResizable(false);
 		// dragging edge to empty space creates a new shape
 		graphComponent.getConnectionHandler().setCreateTarget(true);
 
 		root = graph.getDefaultParent();
 		
 		// a movement listener for any amount of selected cells
 	    graph.addListener(mxEvent.CELLS_MOVED, new mxIEventListener() {
 	        @Override
 	        public void invoke(Object sender, mxEventObject evt) {
 	            if (sender instanceof mxGraph) {
 	                for (Object cell : ((mxGraph)sender).getSelectionCells()) {
 	                	// TODO: update cell geometry attributes in appropriate list
 	                	// (if at all displayed in the list, but should if it is a JTree as proposed)
 	                }
 	            }
 	        }
 	    });
 	    
 	    // a cell add listener
 	    graph.addListener(mxEvent.CELLS_ADDED, new mxIEventListener() {
 	        @Override
 	        public void invoke(Object sender, mxEventObject evt) {
 	            if (sender instanceof mxGraph) {
 	            	// all cells concerning the add event
 	            	Object[] cells=(Object[]) evt.getProperty("cells");
 					for (Object cell : cells) {
 						if (cell instanceof mxCell) {
 							// iterate over all cells in the graph
 							Object[] allCells = graph.getChildCells(root, true, false);
 							for (Object other : allCells) {
 								// don't check the same cells
 								// don't check connectors
 								// check whether they share a name
 								if ((cell != other) && (graph.getCellStyle(cell).get("shape") != "connector") 
 										&& graph.getLabel(other).equals(graph.getLabel(cell))) {
 									// generate a new name for the new cell
 									graph.getModel().setValue(cell, new StringBuilder("S_").append(nextInt++));
 								}
 							}
 							
 							// TODO: add cells to appropriate lists
 							Object shape = graph.getCellStyle(cell).get("shape");
 							if (shape.toString().equals("initialShape")) {
 								if (hasInitialState) {
 									// change potential initial state to a normal state
 									graph.getModel().setStyle(cell, "shape=ellipse;perimeter=ellipsePerimeter");
 									// add a normal state to list
 								}
 								else {
 									// add an initial state to list
 								}
 							}
 							else if (shape.toString().equals("ellipse")) {
 								// add a normal state to list
 							}
 							else if (shape.toString().equals("doubleEllipse")) {
 								// add an end state to list
 							}
 							else if (shape.toString().equals("connector")) {
 								// add an edge to list
 							}
 						}
 	                }
 	            }
 	        }
 	    });
 	    
 	    // a cell remove listener
 	    graph.addListener(mxEvent.CELLS_REMOVED, new mxIEventListener() {
 	        @Override
 	        public void invoke(Object sender, mxEventObject evt) {
 	            if (sender instanceof mxGraph) {
 	            	Object[] cells=(Object[]) evt.getProperty("cells");
 					for (Object cell : cells) {
 						if (cell instanceof mxCell) {
 							// TODO: remove cells from appropriate lists
 							Object shape = graph.getCellStyle(cell).get("shape");
 							if (shape.toString().equals("initialShape")) {
 								hasInitialState = false;
 								// remove initial state from list
 							}
 							else if (shape.toString().equals("ellipse")) {
 								// remove normal state from list
 							}
 							else if (shape.toString().equals("doubleEllipse")) {
 								// remove end state from list
 							}
 							else if (shape.toString().equals("connector")) {
 								// remove edge from list
 							}
 						}
 	                }
 	            }
 	        }
 	    });
 
 	    // handle mouse right-click events for adding cells or changing cells
 	    graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
 	    	@Override
 	    	public void mousePressed(MouseEvent e) {
 	    		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
 	    			Object cell = graphComponent.getCellAt(e.getX(), e.getY());
 	    			if (cell != null) {
 	    				
 	    				Object shape = graph.getCellStyle(cell).get("shape");
 	    				
 	    				if (shape.toString().equals("connector")) {
 	    					popup.getComponent(0).setEnabled(false);
 	    					popup.getComponent(1).setEnabled(true);
 	    					popup.getComponent(2).setEnabled(false);
 	    					popup.getComponent(3).setEnabled(false);
 	    					popup.getComponent(4).setEnabled(false);
 	    					popup.getComponent(5).setEnabled(false);
 	    				}
 	    				else {
 	    					popup.getComponent(0).setEnabled(false);
 	    					popup.getComponent(1).setEnabled(true);
 	    					popup.getComponent(2).setEnabled(true);
 	    					if (shape.toString().equals("initialShape"))
 	    						popup.getComponent(3).setEnabled(false);
 	    					else
 	    						popup.getComponent(3).setEnabled(true);
 	    					if (shape.toString().equals("ellipse"))
 	    						popup.getComponent(4).setEnabled(false);
 	    					else
 	    						popup.getComponent(4).setEnabled(true);
 	    					if (shape.toString().equals("doubleEllipse"))
 	    						popup.getComponent(5).setEnabled(false);
 	    					else
 	    						popup.getComponent(5).setEnabled(true);
 	    				}
 	    			}
 	    			else {
 	    				popup.getComponent(0).setEnabled(true);
 	    				popup.getComponent(1).setEnabled(false);
 	    				popup.getComponent(2).setEnabled(false);
 	    				popup.getComponent(3).setEnabled(false);
 	    				popup.getComponent(4).setEnabled(false);
 	    				popup.getComponent(5).setEnabled(false);
 	    			}
 	    			popupPosition = e.getPoint();
     				popup.show(e.getComponent(), e.getX(), e.getY());
 	    		}
 	    	}
 	    });
 	    
 	    // add customized shape to list of available shapes
 	    mxGraphics2DCanvas.putShape("initialShape", new initialStateShape());
 
 		return graphComponent;
 	}
 	
 	Object addState(String name, int x, int y, int radius, int type) {
 		Object state = null;
 		graph.getModel().beginUpdate();
 		try
 		{
 			switch (type) {
 			case INITIAL_STATE:
 				state = graph.insertVertex(root, null, name, x, y, radius,
 						radius, "shape=initialShape;perimeter=ellipsePerimeter");
				hasInitialState = true;
 				break;
 			case NORMAL_STATE:
 				state = graph.insertVertex(root, null, name, x, y, radius,
 						radius, "shape=ellipse;perimeter=ellipsePerimeter");
 				break;
 			case END_STATE:
 				state = graph.insertVertex(root, null, name, x, y, radius,
 						radius, "shape=doubleEllipse;perimeter=ellipsePerimeter");
 				break;
 			}
 			nextInt++;
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 		
 		return state;
 	}
 	
 	public void addTransition(String name, Object source, Object target) {
 		graph.getModel().beginUpdate();
 		try
 		{
 			graph.insertEdge(root, null, name, source, target);
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 	}
 	
 	public void removeAllCells() {
 		graph.removeCells(graph.getChildVertices(root), true);
 		// reset next integer available
 		nextInt = 1;
 	}
 	
 	public void loadAutomata() {
 		// TODO: loading from file or scala here
 		System.out.println("some loading happens here");
 		// FIXME: remove test automata with loaded stuff
 		Object v1 = addState("first", 50, 50, CELL_RADIUS, INITIAL_STATE);
 		Object v2 = addState("second", 350, 50, CELL_RADIUS, NORMAL_STATE);
 		Object v3 = addState("third", 200, 200, CELL_RADIUS, END_STATE);
 		addTransition("E1", v1, v2);
 		addTransition("E1", v2, v3);
 		addTransition("E1", v3, v3);
 	}
 	
 	public void saveAutomata() {
 		if (!hasInitialState) {
 			JOptionPane.showMessageDialog(top, "You need an initial state to save the automata!", "Warning", JOptionPane.WARNING_MESSAGE);
 		}
 		else {
 			extractData();
 			// TODO: grab states and transitions through public methods
 			System.out.println("some saving happens here");
 		}
 	}
 	
 	public void runSimulation() {
 		if (!hasInitialState) {
 			JOptionPane.showMessageDialog(top, "You need an initial state to run the simulation!", "Warning", JOptionPane.WARNING_MESSAGE);
 		}
 		else {
 			// TODO: run the simulation
 			System.out.println("some simulation running happens here");
 		}
 	}
 	
 	void extractData() {
 		Object[] vertices = graph.getChildCells(root, true, false);
 		Object[] edges = graph.getChildCells(root, false, true);
 		
 		initialState = new HashMap<String, String>();
 		endStates = new ArrayList<HashMap<String, String>>();
 		states = new ArrayList<HashMap<String, String>>();
 		transitions = new ArrayList<HashMap<String, String>>();
 		
 		for (Object vertex : vertices) {
 			Object cell = graph.getCellStyle(vertex).get("shape");
 			if (cell.toString().equals("initialShape")) {
 				initialState.put("x", String.valueOf(graph.getCellGeometry(vertex).getX()));
 				initialState.put("y", String.valueOf(graph.getCellGeometry(vertex).getY()));
 				initialState.put("name", String.valueOf(graph.getLabel(vertex)));
 			}
 			else if (cell.toString().equals("doubleEllipse")) {
 				HashMap<String, String> endState = new HashMap<String, String>();
 				endState.put("x", String.valueOf(graph.getCellGeometry(vertex).getX()));
 				endState.put("y", String.valueOf(graph.getCellGeometry(vertex).getY()));
 				endState.put("name", String.valueOf(graph.getLabel(vertex)));
 				endStates.add(endState);
 			}
 			else if (cell.toString().equals("ellipse")) {
 				HashMap<String, String> state = new HashMap<String, String>();
 				state.put("x", String.valueOf(graph.getCellGeometry(vertex).getX()));
 				state.put("y", String.valueOf(graph.getCellGeometry(vertex).getY()));
 				state.put("name", String.valueOf(graph.getLabel(vertex)));
 				states.add(state);
 			}
 		}
 		
 		for (Object edge : edges) {
 			HashMap<String, String> transition = new HashMap<String, String>();
 			transition.put("source", String.valueOf(graph.getLabel(((mxCell)edge).getSource())));
 			transition.put("target", String.valueOf(graph.getLabel(((mxCell)edge).getTarget())));
 			transition.put("input", String.valueOf(graph.getLabel(edge)));
 			transitions.add(transition);
 		}
 	}
 	
 	HashMap<String, String> getInitialState() {
 		return initialState;
 	}
 	
 	ArrayList<HashMap<String, String>> getEndStates() {
 		return endStates;
 	}
 	
 	ArrayList<HashMap<String, String>> getStates() {
 		return states;
 	}
 	
 	ArrayList<HashMap<String, String>> getTransitions() {
 		return transitions;
 	}
 	
 	public void displayAbout() {
 		JOptionPane.showMessageDialog(top, "Some About message", "About", JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	public void quit() {
 		setVisible(false);
 		dispose();
 	}
 	
 	public void setAlphabet(String alphabet) {
 		alphabetField.setText(alphabet);
 	}
 	
 	public void setTestString(String test) {
 		testField.setText(test);
 	}
 	
 	public void setStatusMessage(String status) {
 		this.status.setText(status);
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		GUI gui = new GUI("Scalomator - Simulate finite-state machines");
 		gui.initGUI(gui);
 		
 		// FIXME: remove test load and bogus status message
 		gui.loadAutomata();
 		gui.setStatusMessage("This is some status message.");
 
 		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		gui.setSize(640, 480);
 		gui.setVisible(true);
 	}
 
 }
