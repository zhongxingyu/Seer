 package org.swows.producer;
 
 import org.swows.graph.events.DynamicDataset;
 import org.swows.graph.events.DynamicGraph;
 import org.swows.graph.transform.GraphTransform;
 import org.swows.graph.transform.QueryGraphTransform;
 import org.swows.graph.transform.UpdateRequestGraphTransform;
 import org.swows.spinx.QueryFactory;
 import org.swows.vocabulary.SWI;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.update.UpdateRequest;
 
 public class GraphTransformProducerFromGraph implements GraphTransformProducer {
 	
 	private Producer graphProducer;
 	private Node rootNode;
 	
 	public GraphTransformProducerFromGraph(Producer graphProducer, Node rootNode) {
 		this.graphProducer = graphProducer;
 		this.rootNode = (rootNode != null) ? rootNode : SWI.GraphRoot.asNode();
 	}
 
 	public GraphTransformProducerFromGraph(Producer graphProducer) {
 		this(graphProducer, null);
 	}
 
 	@Override
 	public GraphTransform createGraphTransform(DynamicDataset inputDataset) {
 		if (graphProducer == null)
 			return null;
 		DynamicGraph graph = graphProducer.createGraph(inputDataset);
 //		graph.getPrefixMapping()
 		if (graph == null)
 			return null;
 		Query query = QueryFactory.toQuery(graph, rootNode);
		if (query != null)
			return new QueryGraphTransform(query);
 		UpdateRequest updateRequest = QueryFactory.toUpdateRequest(graph, rootNode);
		if (updateRequest != null)
 			return new UpdateRequestGraphTransform(updateRequest);
 		return null;
 	}
 
 }
