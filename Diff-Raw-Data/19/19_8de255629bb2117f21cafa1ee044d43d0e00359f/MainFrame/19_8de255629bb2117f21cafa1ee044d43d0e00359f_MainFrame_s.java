 package niagaraGUI;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.view.mxGraph;
 import com.mxgraph.view.mxStylesheet;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.model.mxGeometry;
 import com.mxgraph.util.mxConstants;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.util.mxPoint;
 import com.mxgraph.util.mxRectangle;
 import com.mxgraph.view.mxEdgeStyle;
 
 import javax.swing.*;
 
 
 public class MainFrame extends JFrame implements ActionListener {
 	private JDesktopPane jd;
 	private mxGraph graph;
 	private Object parent;
 	private JPopupMenu popup;
 	private JMenuItem propMenuItm;
 	private QueryPlan queryPlan;
 	private Hashtable<String,OperatorTemplate> operatorTemplates;
 	private String[] operatorNames;
 	private GraphNode[] nodes;
 	private OperatorSelectorDialog opPicker;
 	
 	
 	private mxGraphComponent graphComponent;
 	private mxCell clickedCell;
 	
 	private class EdgeListener implements mxIEventListener{
 		@Override //Edge creation
 		public void invoke(Object sender, mxEventObject evt) {
 			// TODO Auto-generated method stub
 			mxCell edge = (mxCell) evt.getProperty("cell");
 			
 			GraphNode.IOPort out = (GraphNode.IOPort) edge.getSource();// gives the source cell
 			GraphNode.IOPort in = (GraphNode.IOPort) edge.getTarget();//gives the target cell
 			GraphNode sourceOp;
 			GraphNode sinkOp;
 			if (in.isInput() && out.isOutput()){//good edge
 				System.out.println("GOOD EDGE");
 			}
 			else if (in.isOutput() && out.isInput()){//backwards edge
 				System.out.println("BACKWARDS EDGE");
 				mxCell[] toRemove = {edge};
 				graphComponent.getGraph().removeCells(toRemove);
 				edge.setTerminal(out, false);
 				edge.setTerminal(in, true);
 				graphComponent.getGraph().addCell(edge);
 				GraphNode.IOPort hold = out;
 				out = in;
 				in = hold;
 			}
 			else{//bad edge
 				mxCell[] toRemove = {edge};
 				graphComponent.getGraph().removeCells(toRemove);
 				System.out.println("BAD EDGE");
 				return;
 			}
 			sourceOp = (GraphNode)out.getParent().getValue();
 			sinkOp = (GraphNode)in.getParent().getValue();
 			sinkOp.addInput(sourceOp);
 			System.out.println("New Edge " + sourceOp.getName() + " to " + sinkOp.getName());
 		}
 	}
 	
 	private class MouseListener extends MouseAdapter{
 		@Override
         public void mousePressed(MouseEvent e) {
         	if (e.getButton() == e.BUTTON3){
 	            clickedCell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
 	            System.out.println("Mouse click in graph component");
 	            popup.show(graphComponent, e.getX(), e.getY());
         	}
         }
 	
 	}
 	
 	public MainFrame(){
 		super();
 		//jd = new JDesktopPane();
 		//setContentPane(jd);
 		initComponents();
 		hw();
 	}
 	
 	private void hw(){
 		queryPlan = new QueryPlan("QP","queryplan.dtd");
 		operatorTemplates = queryPlan.getOpTemplates();
 		operatorNames = queryPlan.getOperatorNames();
 		opPicker = new OperatorSelectorDialog(operatorNames);
 
 		nodes = new GraphNode[operatorNames.length];
 		int i=0;
 		for(String op : operatorNames){
 			nodes[i++] = new GraphNode(operatorTemplates.get(op));
 		}
 		
 		
 		try
 		{
 			
 			for(int j=0; j < nodes.length; j++){
 				System.out.println(nodes[j].getName());
 				nodes[j].draw(graph, parent);
 			}
 			
 		}
 		finally
 		{
 			graph.getModel().endUpdate();
 		}
 
 		
 	}
 	private void initComponents(){
 		graph = new mxGraph();
 		parent = graph.getDefaultParent();
 
 		graph.getModel().beginUpdate();
 		graph.setCellsEditable(false);
 		graph.setAutoSizeCells(true);
 		
 		Map<String, Object> stil = new HashMap<String, Object>();
 		stil.put(mxConstants.STYLE_ROUNDED, true);
		stil.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ENTITY_RELATION); // <-- This is what you want
 		stil.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
 		stil.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
 		stil.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
 		stil.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
 		stil.put(mxConstants.STYLE_STROKECOLOR, "#6482B9");
 		stil.put(mxConstants.STYLE_FONTCOLOR, "#446299");
 
 		mxStylesheet foo = new mxStylesheet();
 		foo.setDefaultEdgeStyle(stil);
 		graph.setStylesheet(foo);
 		
 		graph.setCellStyle( "edgeStyle=mxEdgeStyle.orthoConnector" );
 		graphComponent = new mxGraphComponent(graph);
 		graphComponent.setDragEnabled(false);
 		getContentPane().add(graphComponent);
 		graphComponent.getGraphControl().addMouseListener(new MouseListener());
 		graphComponent.getConnectionHandler().addListener(mxEvent.CONNECT, new EdgeListener());
 		popup = new JPopupMenu();
 		propMenuItm = new JMenuItem("Properties");
 		propMenuItm.addActionListener(this);
 		popup.add(propMenuItm);
 		
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		MainFrame frame = new MainFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    frame.setVisible(true);
 	}
 
 	
 	//Listeners
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		if (e.getSource() == propMenuItm){
 			PropertyDialog pd = new PropertyDialog((GraphNode)clickedCell.getValue());
 			System.out.println("cell=" + graph.getLabel(clickedCell));
 		}
 	}
 	public void showPopup(){
 		
 	}
 
 
 		
 
 }
