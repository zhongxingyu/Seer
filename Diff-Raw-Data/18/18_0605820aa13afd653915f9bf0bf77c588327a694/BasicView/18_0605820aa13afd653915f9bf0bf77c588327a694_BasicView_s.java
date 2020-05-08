 package pcpl.simplevisualizer.views;
 
 import java.awt.Frame;
 import pcpl.core.visualization.*;
 import pcpl.core.eventHandler.*;
 import org.jgraph.*;
 import org.jgraph.graph.DefaultEdge;
 import org.jgrapht.ListenableGraph;
 import org.jgrapht.ext.JGraphModelAdapter;
 import org.jgrapht.graph.ListenableDirectedGraph;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.debug.core.model.IVariable;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.awt.SWT_AWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.widgets.*;
 
 public class BasicView extends ViewPart implements IVisualizer
 						, BreakPointListener{
 	private JGraphModelAdapter m_jgAdapter;
 	private ListenableGraph g;
 	private JGraph graph;
 	private int i;
 	private String _name = null;
 	private String _id = null;
 	Composite composite;
 	Frame frame;
 	public BasicView(){
 		super();
 		_name = "jGraph";
 		_id = "pcpl.simpleVisualizer.BasicView";
 		EventCenter.getInstance().addBreakPointListener(this);
 	}
 	@Override
 	public void createPartControl(Composite parent) {
 		composite= new Composite( parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
 		frame = SWT_AWT.new_Frame(composite);
 		g = new ListenableDirectedGraph( DefaultEdge.class );
 		m_jgAdapter = new JGraphModelAdapter( g );
 		graph = new JGraph(m_jgAdapter);
 		g.addVertex( "Weclome" );
 		frame.add(graph);
 		i=0;
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 	private void createExecPath(ListenableDirectedGraph g){
 		
 	}
 		
 	@Override
 	public String getVisualizerName() {
 		return _name;
 	}
 	@Override
 	public String getVisualizerID() {
 		// TODO Auto-generated method stub
 		return _id;
 	}
 	@Override
 	public void onBreakPointTriggered(IVariable[] variables,
 			IBreakpoint breakpoint,IStackFrame[] stacks) {
 		if(EventCenter.getInstance().getModeType() == 3){
 			this.update();
 		}

 	}
 	
 	private void update(){
 		
 	}
 	private void init(){
 
 	}
 }
