 package edu.ucsf.rbvi.setsApp.internal.tasks;
 
 import java.util.List;
 
 import org.cytoscape.command.util.EdgeList;
 import org.cytoscape.command.util.NodeList;
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyTableUtil;
 import org.cytoscape.task.AbstractNetworkViewTask;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.work.AbstractTask;
 import org.cytoscape.work.ObservableTask;
 import org.cytoscape.work.TaskMonitor;
 import org.cytoscape.work.Tunable;
 
 import edu.ucsf.rbvi.setsApp.internal.model.SetsManager;
 
 public class CreateEdgeSetTask extends AbstractTask implements ObservableTask {
 	
 	@Tunable(description="Enter a name for the set:",
 	           tooltip="The set name must be unique and will appear in the 'Sets' panel",
 	           gravity=1.0)
 	public String name;
 
 	public EdgeList edgeList = new EdgeList(null);
 	@Tunable(description="Edges to be included in this set", context="nogui")
 	public EdgeList getedgeList() {
 		if (network == null) network = mgr.getCurrentNetwork();
 		edgeList.setNetwork(network);
 		return edgeList;
 	}
 	public void setedgeList(EdgeList setValue) {}
 	@Tunable(description="Network for this set", context="nogui")
 	public CyNetwork network;
 	
 	private SetsManager mgr;
 	private CyNetwork net;
 	
 	public CreateEdgeSetTask(SetsManager manager, CyNetwork net) {
 		mgr = manager;
 		this.net = net;
 	}
 	
 	@Override
 	public void run(TaskMonitor arg0) throws Exception {
 		List<CyEdge> edges;
 		if (net == null && network != null) {
 			edges = edgeList.getValue();
 			net = network;
 		} else {
 			edges = CyTableUtil.getEdgesInState(net, CyNetwork.SELECTED, true);
 		}
 		arg0.showMessage(TaskMonitor.Level.INFO,
                 "Creating set "+name+" with "+edges.size()+" nodes");
 		mgr.createSet(name, net, CyEdge.class, edges);
 		if (edges == null)
 			arg0.showMessage(TaskMonitor.Level.INFO, "Created new empty edge set: "+name);
 		else
 			arg0.showMessage(TaskMonitor.Level.INFO, 
 			                 "Created new edge set: "+name+" with "+edges.size()+" edges");
 	}
 
 	// Return the updated set
 	public Object getResults(Class expectedType) {
 		if (expectedType.equals(String.class)) {
 			return mgr.getSet(name).toString();
 		}
 		return mgr.getSet(name);
 	}
 
 }
