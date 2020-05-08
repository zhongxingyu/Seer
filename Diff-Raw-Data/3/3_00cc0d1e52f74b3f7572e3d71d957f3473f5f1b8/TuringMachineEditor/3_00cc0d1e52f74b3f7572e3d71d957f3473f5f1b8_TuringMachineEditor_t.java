 package gui.turing;
 
 import java.util.ArrayList;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import machine.turing.*;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 
 import machine.turing.Edge;
 import machine.turing.State;
 import machine.turing.TuringMachine;
 
 import com.mxgraph.swing.mxGraphComponent;
 
 import com.mxgraph.view.mxGraphSelectionModel;
 import com.mxgraph.view.mxGraph;
 import com.mxgraph.model.*;
 
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventSource;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.util.mxEventObject;
 
 
 import gui.MachineEditor;
 
 public class TuringMachineEditor extends MachineEditor implements ActionListener{
 	private TuringMachine machine = null;
 	private Object selectedObject = null;
 	private StateList graphicalStates = null;
 	private ArrayList<mxCell> graphicalEdges = null;
 
 	protected JPanel jPanelLeft = null;
 	protected JPanel jPanelGraph = null;
 	protected mxGraph graph = null;
 	protected JSplitPane jSplitPaneHorizontal = null;
 	protected JPanel jPanelToolBox = null;
 	protected JPanel jPanelProperties = null;
 	protected ToolBox toolBox = new ToolBox();
 	
 	private JMenu editMenu;
 	private JMenuItem copyAction;
 	private JMenuItem cutAction;
 	private JMenuItem pasteAction;
 
 	/**
 	 * 
 	 * @author Philipp
 	 * Nested class to extend ArrayList<mxCell> to find mxCells with specified State Object
 	 */
 	class StateList extends ArrayList<mxCell>{
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
 
 	public TuringMachineEditor(TuringMachine machine) {
 		super();
 		this.machine = machine;
 		
 		this.initEditor();
 		
 		this.graphicalStates = new StateList(machine.getStates().size());
 		this.graphicalEdges = new ArrayList<mxCell>(machine.getEdges().size());
 
 		//create left panel
 		this.jPanelLeft = new JPanel();
 		this.jPanelLeft.setLayout(new BorderLayout());
 		this.jPanelToolBox = new JPanel();
 		this.jPanelProperties = new JPanel();
 		this.jPanelLeft.add(this.jPanelToolBox, BorderLayout.PAGE_START);
 		this.jPanelLeft.add(this.jPanelProperties, BorderLayout.CENTER);
 
 		//create main graph panel
 		this.jPanelGraph = new JPanel();
 		this.jPanelGraph.setLayout(new BorderLayout());
 
 		//create split pane
 		this.jSplitPaneHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				this.jPanelLeft, this.jPanelGraph);
 		this.jSplitPaneHorizontal.setOneTouchExpandable(true);
 		this.jSplitPaneHorizontal.setDividerLocation(250);
 		Dimension minimumSize = new Dimension(100, 50);
 		this.jPanelLeft.setMinimumSize(minimumSize);
 		this.jPanelGraph.setMinimumSize(minimumSize);
 		this.setLayout(new BorderLayout());
 		this.add(this.jSplitPaneHorizontal, BorderLayout.CENTER);
 		jPanelToolBox.add(toolBox);
 
 		//create the graph
 		this.graph = new mxGraph();
 		/*
 		State s1 = new State("1", "s1", true, false);
 		State s2 = new State("2", "s2", false, true);
 		s1.setHeight(5);
 		s1.setWidth(5);
 		s1.setXcoord(10);
 		s1.setYcoord(10);
 		s2.setHeight(5);
 		s2.setWidth(5);
 		s2.setXcoord(40);
 		s2.setYcoord(40);
 		ArrayList<Transition> tl = new ArrayList<Transition>();
 		ArrayList<Character> c1 = new ArrayList<Character>();
 		ArrayList<Character> c2 = new ArrayList<Character>();
 		ArrayList<Character> c3 = new ArrayList<Character>();
 		c1.add('0');
 		c2.add('0');
 		c3.add('N');
 		tl.add(new Transition("1",c1,c2,c3));
 		Edge e1 = new Edge(s1, s2, tl);
 		machine.getStates().add(s1);
 		machine.getStates().add(s2);
 		machine.getEdges().add(e1); */
 		this.drawGraph();
 		mxGraphComponent graphComponent = new mxGraphComponent(graph);
 		this.jPanelGraph.add(graphComponent, BorderLayout.CENTER);
 
 		
 
 
 //		if (this.machine != null && this.machine.getEdges().size() > 0) {
 //			this.displayProperties(this.machine.getEdges().get(0));
 //		}
 	}
 	
 	/**
 	 * Initializes the editor
 	 */
 	public void initEditor() {
 		editMenu = new JMenu("Edit");
 		copyAction = new JMenuItem("Copy");
 		cutAction = new JMenuItem("Cut");
 		pasteAction = new JMenuItem("Paste");
 		
 		editMenu.add(copyAction);
 		editMenu.add(cutAction);
 		editMenu.add(pasteAction);
 		this.getMenus().add(editMenu);
 		
 		copyAction.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		cutAction.setAccelerator(KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		pasteAction.setAccelerator(KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 	
 		copyAction.addActionListener(this);
 		cutAction.addActionListener(this);
 		pasteAction.addActionListener(this);
 	}
 
 
 	private void displayProperties(Edge edge) {
 		PropertiesEdge propertiesEdge = new PropertiesEdge(this.machine.getNumberOfTapes(), edge);
 		jPanelProperties.removeAll();
 		jPanelProperties.add(propertiesEdge);
		jPanelProperties.validate();
 	}
 
 	private void displayProperties(State state) {
 		PropertiesState propertiesState = new PropertiesState(state);
 		this.jPanelProperties.removeAll();
 		this.jPanelProperties.add(propertiesState);
		jPanelProperties.validate();
 	}
 
 	private void drawGraph(){
 		ArrayList<State> states = this.machine.getStates();
 		ArrayList<Edge> edges = this.machine.getEdges();
 
 		//load graphical states
 		graph.getModel().beginUpdate();
 		try	{
 			for (int i = 0;  i < states.size(); i++){
 				graphicalStates.add(i, (mxCell) graph.insertVertex(graph.getDefaultParent(), null, 
 						states.get(i), states.get(i).getXcoord(), states.get(i).getYcoord(), 
 						states.get(i).getWidth(), states.get(i).getHeight()));
 			}
 
 			//insert graphical Edges
 			Edge currentEdge = null;
 			Object v1 = null;
 			Object v2 = null;
 			for (int i = 0; i < edges.size(); i++){
 				currentEdge = edges.get(i);
 				v1 = graphicalStates.getMxCell(currentEdge.getFrom());
 				v2 = graphicalStates.getMxCell(currentEdge.getTo());
 				graphicalEdges.add(i,(mxCell) graph.insertEdge(graph.getDefaultParent(), null, currentEdge, v1, v2));
 
 			}
 		} finally {
 			graph.getModel().endUpdate();
 		}
 	}
 
 	/**
 	 * Responds to a clicked button
 	 * @param e ActionEvent that indicates changes
 	 */
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
 	}
 }
