 package model.dynamic_types;
 
 import graph_generators.HashRingGenerator;
 import graph_generators.HashRingNode;
 
 import java.util.ArrayList;
 
 import org.jgrapht.Graph;
 import org.jgrapht.graph.DefaultDirectedGraph;
 import org.jgrapht.graph.DefaultEdge;
 
 import model.AbstractPubSubModel;
 import desmoj.core.simulator.Model;
 import desmoj.core.simulator.TimeSpan;
 
 public class BroadcastMessageGeneratorEvent extends
 		AbstractMessasgeGeneratorEvent 
 {
 	private final static boolean fShowInTrace = true;
 	
 	public BroadcastMessageGeneratorEvent(Model owner, String name, boolean showInTrace)
 	{
 		super(owner, name, showInTrace);
 	}
 
 	@Override
 	public void eventRoutine() 
 	{
 		AbstractPubSubModel model = (AbstractPubSubModel)getModel();
 		ArrayList<HashRingNode> nodeList = model.getNodeList();
 		double publisherHashKey = nodeList.get(model.getPublisherIndex()).getHashKey();
 		if (model.networkTree == null)
 		{
 			model.networkTree = new DefaultDirectedGraph<HashRingNode, DefaultEdge>(DefaultEdge.class);
 			model.networkTree.addVertex(model.getHashRingTraverser().getNodeByKey(publisherHashKey));
 		}
 		Message message = new Message(model, publisherHashKey, publisherHashKey, true);
 		BroadcastMessageArrivalEvent messageArrivalEvent = new BroadcastMessageArrivalEvent(model, "Publisher sends new message", fShowInTrace);
 		messageArrivalEvent.schedule(message, new TimeSpan(0));
 		
		schedule(new TimeSpan(model.getMessageArrivalTime(), model.TIME_UNIT));
 		
 	}
 
 }
