 /**
  * Approved for Public Release: 10-4800. Distribution Unlimited.
  * Copyright 2011 The MITRE Corporation,
  * Licensed under the Apache License,
  * Version 2.0 (the "License");
  *
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.wiredwidgets.cow.server.transform.graph.bpmn20;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.omg.spec.bpmn._20100524.model.Definitions;
 import org.omg.spec.bpmn._20100524.model.TFlowNode;
 import org.omg.spec.bpmn._20100524.model.TFormalExpression;
 import org.omg.spec.bpmn._20100524.model.TProcess;
 import org.omg.spec.bpmn._20100524.model.TSequenceFlow;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.wiredwidgets.cow.server.api.model.v2.Activity;
 import org.wiredwidgets.cow.server.api.model.v2.Process;
 import org.wiredwidgets.cow.server.api.model.v2.Variable;
 import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
 import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
 import org.wiredwidgets.cow.server.transform.graph.builder.GraphBuilder;
 import org.wiredwidgets.cow.server.transform.v2.AbstractProcessBuilder;
 
import com.sun.xml.xsom.impl.scd.Iterators.Map;

 
 /**
  *
  * @author JKRANES
  */
 @Component
 public class Bpmn20NewProcessBuilder extends AbstractProcessBuilder<Definitions> {
 	
 	public static final String VARIABLES_PROPERTY = "variables";
 	public static final String PROCESS_INSTANCE_NAME_PROPERTY = "processInstanceName";
 	public static final String PROCESS_EXIT_PROPERTY = "processExitState";
 
 	private static final String BPMN20_MODEL_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
 	
 	private static final String STRING_CLASS_NAME = String.class.getSimpleName();
 
     @Autowired
     GraphBuilder graphBuilder;
     
     @Autowired
     NodeBuilderService nodeBuilderService;
     
     @Override
     public Definitions build(Process source) {
         
         Bpmn20ProcessContext context = new Bpmn20ProcessContext(source, new TProcess());
         
         if (source.getMaxId() > 0) {
         	context.setRevised(true);
         }
         
         context.setStartId("_", source.getMaxId());
       
         // Every process has a Map for ad-hoc content
         context.addProcessVariable(VARIABLES_PROPERTY, Map.class.getCanonicalName());
         
         // Name for the process instance
         context.addProcessVariable(PROCESS_INSTANCE_NAME_PROPERTY, STRING_CLASS_NAME);
         
         // Used with Exit actions to specify which action was taken
         context.addProcessVariable(PROCESS_EXIT_PROPERTY, STRING_CLASS_NAME);
              
         // Add any additional variables defined in the workflow
         if (source.getVariables() != null) {
             for (Variable var : source.getVariables().getVariables()) {
                 context.addProcessVariable(var.getName(), STRING_CLASS_NAME);
             }       
         }        
 
         // build the activity graph
 		ActivityGraph graph = graphBuilder.buildGraph(source);
 		
 		// put vertices in a list.  We need them in a defined order.
 		List<Activity> activityVertices = new ArrayList<Activity>();
 		activityVertices.addAll(graph.vertexSet());
 		
 		// create a new graph to hold created nodes
 		NodeGraph nodeGraph = new NodeGraph();		
 		
 		// initialize a corresponding list of Nodes
 		List<Bpmn20Node> nodeVertices = new ArrayList<Bpmn20Node>();
 		
 		// build the nodes in the order defined by the list and add them to the nodeGraph
 		// in the same order
 		for (Activity activity : activityVertices) {
 			Bpmn20Node node = nodeBuilderService.buildNode(activity, context);
 			nodeVertices.add(node);
 			nodeGraph.addVertex(node);
 		}
 		
 		// add edges to connect the nodes, using the list index positions
 		// to connect the new nodes in the same structure as the activity graph
 		for (ActivityEdge edge : graph.edgeSet()) {
 			// identify the corresponding node vertices
 			int sourceIndex = activityVertices.indexOf(graph.getEdgeSource(edge));
 			int targetIndex = activityVertices.indexOf(graph.getEdgeTarget(edge));
 			
 			// create an edge for corresponding nodes
 			Bpmn20Edge nodeEdge = nodeGraph.addEdge(nodeVertices.get(sourceIndex), nodeVertices.get(targetIndex));
 			
 			// create reference back to the ActivityEdge
 			// this is currently used for Decisions to construct the edge expression
 			nodeEdge.setActivityEdge(edge);
 		}
 		
 		// Add the nodes to the BPMN20 object set
 		for (Bpmn20Node node : nodeGraph.vertexSet()) {
 			context.addNode(node);
 		}
 		
 		// Add the edes to the BPMN20 object set
 		for (Bpmn20Edge edge : nodeGraph.edgeSet()) {
 			addEdge(context, nodeGraph, edge);
 		}
 		
         source.setMaxId(context.getId("_"));
         
         return context.getDefinitions();
     }
 
 
     /**
      * Add a transition (sequenceFlow) with a FormalExpression
      * @param target
      * @param transitionName
      * @param expression
      */
     private void addEdge(Bpmn20ProcessContext context, NodeGraph graph, Bpmn20Edge edge) {
         TSequenceFlow sequenceFlow = new TSequenceFlow();
         TFlowNode sourceNode = graph.getEdgeSource(edge).getNode().getValue();
         TFlowNode targetNode = graph.getEdgeTarget(edge).getNode().getValue();
         // follow convention of concatenating source and targetIds to form the sequence ID
         String sequenceId = sourceNode.getId() + targetNode.getId();
         sequenceFlow.setId(sequenceId);
         sequenceFlow.setSourceRef(sourceNode);
         sequenceFlow.setTargetRef(targetNode);
 
         if (edge.getActivityEdge().getExpression() != null) {
         	// this edge represents a decision path
         	
         	// set the name (for display purposes)
         	sequenceFlow.setName(edge.getActivityEdge().getExpression());
         	
         	// create the expression that will be evaluated when selecting a path
         	
             // the name of the decision variable is based on the ID of the node
         	// which at this point has also been copied to the Key of the corresponding Activity
             String varName = DecisionTaskNodeBuilder.getDecisionVarName(edge.getActivityEdge().getVarSource().getKey());
             String expression = "return " + varName + " == \"" + edge.getActivityEdge().getExpression() + "\";";          
               
             TFormalExpression expr = new TFormalExpression();
             expr.getContent().add(expression);
             sequenceFlow.setConditionExpression(expr);
         }
 
         context.addEdge(sequenceFlow);
 
         // incomings and outgoings are required by igrafx       
         sourceNode.getOutgoings().add(new QName(BPMN20_MODEL_NAMESPACE, sequenceFlow.getId()));
         targetNode.getIncomings().add(new QName(BPMN20_MODEL_NAMESPACE, sequenceFlow.getId()));
     }    
     
 }
