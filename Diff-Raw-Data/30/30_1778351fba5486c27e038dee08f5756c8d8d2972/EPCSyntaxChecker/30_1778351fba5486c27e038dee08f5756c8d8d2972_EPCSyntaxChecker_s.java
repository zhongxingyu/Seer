 package de.hpi.epc.validation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.hpi.diagram.Diagram;
 import de.hpi.diagram.DiagramEdge;
 import de.hpi.diagram.DiagramNode;
 import de.hpi.diagram.DiagramObject;
 import de.hpi.petrinet.SyntaxChecker;
 
 public class EPCSyntaxChecker implements SyntaxChecker {
 	
 	private static final String NO_SOURCE = "Each edge must have a source";
 	private static final String NO_TARGET = "Each edge must have a target";
 	private static final String NOT_CONNECTED = "Node must be connected with edges";
 	private static final String NOT_CONNECTED_2 = "Node must be connected with more edges";
 	private static final String TOO_MANY_EDGES = "Node has too many connected edges";
 	private static final String NO_CORRECT_CONNECTOR = "Node is no correct conector";
 	
 	private static final String MANY_STARTS = "There must be only one start event";
 	private static final String MANY_ENDS = "There must be only one end event";
 	
 	private static final String FUNCTION_AFTER_OR = "There must be no functions after a splitting OR/XOR";
 	private static final String FUNCTION_AFTER_FUNCTION =  "There must be no function after a function";
 	private static final String EVENT_AFTER_EVENT =  "There must be no event after an event";
 	
 	protected Diagram diagram;
 	protected Map<String,String> errors;
 	
 	public EPCSyntaxChecker(Diagram diagram) {
 		this.diagram = diagram;
 		this.errors = new HashMap<String,String>();
 	}
 
 	public boolean checkSyntax() {
 		errors.clear();
 		if (diagram == null)
 			return false;
 		checkEdges();
 		checkNodes();
 		
 		return errors.size() == 0;
 	}
 
 	public Map<String, String> getErrors() {
 		return errors;
 	}
 	
 	protected void checkEdges() {
 		for (DiagramEdge edge: diagram.getEdges()) {
 			if (edge.getSource() == null)
 				addError(edge, NO_SOURCE);
 			if (edge.getTarget() == null) 
 				addError(edge, NO_TARGET);
 		}
 	}
 	
 	protected void checkNodes() {
 		List<DiagramNode> startEvents = new ArrayList<DiagramNode>();
 		List<DiagramNode> endEvents = new ArrayList<DiagramNode>();
 		for (DiagramNode node: diagram.getNodes()) {
			int in = node.getIncomingEdges().size();
			int out = node.getOutgoingEdges().size();
			if (in == 0 && out == 0){
 				addError(node, NOT_CONNECTED);
 			}
			else if ("Event".equals(node.getType())){
 				if (in == 1 && out == 0) endEvents.add(node);
 				else if (in == 0 && out == 1) startEvents.add(node);
 				else if (in > 1 || out > 1) addError(node, TOO_MANY_EDGES);
 				for (DiagramNode next : getNextEventsOrFunctions(node.getOutgoingEdges())){
 					if ("Event".equals(next.getType())) addError(next, EVENT_AFTER_EVENT);
 				}
 			}
			else if (in == 0 || out == 0){
				addError(node, NOT_CONNECTED_2);
			}
 			else if ("Function".equals(node.getType())){
 				if (in > 1 || out > 1) addError(node, TOO_MANY_EDGES);
 				for (DiagramNode next : getNextEventsOrFunctions(node.getOutgoingEdges())){
 					if ("Function".equals(next.getType())) addError(next, FUNCTION_AFTER_FUNCTION);
 				}
 			}
 			else if ("ProcessInterface".equals(node.getType())){
 				if (in > 1 || out > 1) addError(node, TOO_MANY_EDGES);
 			}
 			else if ("XorConnector".equals(node.getType()) || "OrConnector".equals(node.getType())){
 				if (in == 1 && out == 2){
 					for (DiagramNode next : getNextEventsOrFunctions(node.getOutgoingEdges())){
 						if ("Function".equals(next.getType())){
 							addError(node, FUNCTION_AFTER_OR);
 							break;
 						}
 					}
 				} else if (in == 2 && out == 1){
 					// nothing todo
 				} else {
 					addError(node, NO_CORRECT_CONNECTOR);
 				}
 			}
 			else if ("AndConnector".equals(node.getType())){
 				if ( ! ( (in == 2 && out == 1) || (in == 1 && out == 2) ) ){
 					addError(node, NO_CORRECT_CONNECTOR);
 				}
 			}
 		}
 		if (startEvents.size() > 1){
 			for (DiagramNode n : startEvents){
 				addError(n, MANY_STARTS);
 			}
 		}
 		if (endEvents.size() > 1){
 			for (DiagramNode n : endEvents){
 				addError(n, MANY_ENDS);
 			}
 		}
 	}
 	
 	protected void addError(DiagramObject obj, String errorCode) {
 		String key = obj.getResourceId();
 		String oldErrorCode = errors.get(key);
 		if (oldErrorCode != null && oldErrorCode.startsWith("Multiple Errors: ")){
 			errors.put(obj.getResourceId(), oldErrorCode+", "+errorCode);
 		} else if (oldErrorCode != null){
 			errors.put(obj.getResourceId(), "Multiple Errors: "+oldErrorCode+", "+errorCode);
 		} else {
 			errors.put(obj.getResourceId(), errorCode);
 		}
 	}
 	
 	private List<DiagramNode>getNextEventsOrFunctions(List<DiagramEdge> edges){
 		List<DiagramEdge> newEdges = new ArrayList<DiagramEdge>();
 		List<DiagramNode> result = new ArrayList<DiagramNode>();
 		for (DiagramEdge edge : edges){
 			newEdges.add(edge);
 		}
 		return getNextEventsOrFunctions(newEdges, result);
 	}
 	
 	private List<DiagramNode>getNextEventsOrFunctions(List<DiagramEdge> edges, List<DiagramNode> result){
 		List<DiagramEdge> newEdges = new ArrayList<DiagramEdge>();
 		for (DiagramEdge edge : edges){
 			if ("ControlFlow".equals(edge.getType())){
 				DiagramNode target = edge.getTarget();
 				if ("Function".equals(target.getType()) || "Event".equals(target.getType())){
 					result.add(target);
 				} else {
 					newEdges.addAll(target.getOutgoingEdges());
 				}
 			}
 		}
 		if (newEdges.size() > 0){
 			return getNextEventsOrFunctions(newEdges, result);
 		}
 		return result;
 	}
 
 }
