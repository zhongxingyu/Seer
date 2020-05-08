 package development.hierarchical_split;
 
 import internal.parser.TokenCmpOp;
 import internal.parser.containers.Constraint;
 import internal.parser.containers.Datum;
 import internal.parser.containers.Datum.Int;
 import internal.parser.containers.property.PropertyDef;
 import internal.parser.containers.property.PropertyDef.RandomSpec;
 import internal.parser.resolve.ResolutionEngine;
 import internal.parser.resolve.Result;
 import internal.tree.IWorldTree;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import development.com.collection.range.Range;
 import development.com.collection.range.Range.BoundType;
 
 public class HierarchicalSplit {
 
 	public static Map<IWorldTree, Datum> split(IWorldTree node, Constraint constraint, PropertyDef definition) {
 		Map<IWorldTree, Range> childRanges = new HashMap<IWorldTree, Range>();
 		Result queryResult 			= ResolutionEngine.evaluate(node, definition.query());
 		String columnName 			= definition.query().pattern().lhs().toString();	//FIXME: Hard-coded! replace this with better logic
 		List<IWorldTree> children 	= queryResult.get(columnName);
 		for(IWorldTree child : children) {
 			RandomSpec bound = child.getBounds(definition);
 			childRanges.put(child, bound.range());
 		}
 		
 		Map<IWorldTree, Datum> result = new HashMap<IWorldTree, Datum>();
 		
 		Node root = buildTree(childRanges, definition);
 		
 		Datum requiredValue = constraint.condition().value();
 		root.split(result, requiredValue, definition);
 		return result;
 	}
 
 	private static Node buildTree(Map<IWorldTree, Range> childRanges, PropertyDef definition) {
 		List<IWorldTree> children = new ArrayList<IWorldTree>(childRanges.keySet());
 		
 		List<Node> nodeList = new ArrayList<Node>();
 		
 		for(IWorldTree child : children) {
 			Node node 	= new Node(definition, null);
 			Range range	= childRanges.get(child);
 			node.setObject(child, range);
 			nodeList.add(node);
 		}
 		
 		while(nodeList.size() > 1) {
 			Node node 		= new Node(definition, null);
 			
 			Node listHead	= nodeList.get(0);
 			node.insert(listHead);
 			nodeList.remove(0);
 			
 			if(nodeList.size() > 0) {
 				listHead	= nodeList.get(0);
 				node.insert(listHead);
 				nodeList.remove(0);
 			}
 			nodeList.add(node);
 		}
 		return nodeList.get(0);
 	}
 	
 	
 	private static class Node {
 		private Node parent;
 		private Node lhs;
 		private Node rhs;
 		private IWorldTree object;
 		private Range range;
 		private PropertyDef definition;
 		
 		public Node(PropertyDef definition, Node parent) {
 			this.parent	= parent;
 			this.lhs	= null;
 			this.rhs	= null;
 			this.object	= null;
 			this.range	= null;
 			this.definition	= definition;
 		}
 
 		public void setObject(IWorldTree object, Range range) {
 			this.object	= object;
 			this.range	= range;
 		}
 		
 		public void setRange(Range range) {
 			this.range	= range;
 		}
 		
 		private void setLHS(Node node) {
 			node.parent	= this;
 			this.lhs	= node;
 		}
 		
 		private void setRHS(Node node) {
 			node.parent	= this;
 			this.rhs	= node;
 		}
 		
 		public Node parent() {
 			return parent;
 		}
 		
 		public Node root() {
 			if(this.parent == null)
 				return this;
 			else
 				return parent.root();
 		}
 		
 		public PropertyDef definition() {
 			if(this.parent == null)
 				return this.definition;
 			else
 				return this.root().definition;
 		}
 		
 		public void insert(Node node) {
 			if(this.lhs == null)
 				this.setLHS(node);
 			else if(this.rhs == null)
 				this.setRHS(node);
 		}
 
 		
 		public Range range() {
 			if(lhs == null)
 				return range;
 			else if(rhs == null)
 				return lhs.range();
 			else {
 				switch(definition.type()) {
 				case AGGREGATE:
 					switch(definition.aggregateExpression().type()) {
 					case COUNT:
 					case SUM:
 						return this.lhs.range().add(this.rhs.range());	//FIXME: Potentially wrong
 					case MAX:
 					case MIN:
 						return this.lhs.range().span(this.rhs.range());
 					}
 					break;
 				default:
 					System.err.println("How can the tree have a node with 2 objects somewhere below, but not be an aggregate?");
 					break;
 				
 				}
 			}
 			throw new IllegalStateException("Shouldn't be trying to return null");
 		}
 		
 		public void split(Map<IWorldTree, Datum> values, Datum requiredValue, PropertyDef definition) {
 			Datum lhsValue 	= null;
 			Datum rhsValue	= null;
 			
 			switch(definition.type()) {
 			case AGGREGATE:
 				Range intersection 	= null;
 				if(object != null)
 					intersection	= this.range().clone();
 				else {
 					if(lhs != null)
 						intersection	= lhs.range().clone();
 					if(rhs != null)
 						intersection	= intersection.intersection(rhs.range());
 				}
 				switch(definition.aggregateExpression().type()) {
 				case COUNT:
 					lhsValue	= intersection.generateRandom();
 					rhsValue	= requiredValue.subtract(lhsValue);
 					break;
 				case MAX:
 					if(rhs == null)
 						lhsValue	= requiredValue;
 					else {
 						if(Math.random() > 0.5) {
 							lhsValue	= requiredValue;
 							intersection.setUpperBound(requiredValue);
 							rhsValue	= intersection.generateRandom();
 						}
 						else {
 							rhsValue	= requiredValue;
 							intersection.setUpperBound(requiredValue);
 							lhsValue	= intersection.generateRandom();
 						}
 					}
 					break;
 				case MIN:
 					if(rhs == null)
 						lhsValue	= requiredValue;
 					else {
 						if(Math.random() > 0.5) {
 							lhsValue	= requiredValue;
 							intersection.setLowerBound(requiredValue);
 							rhsValue	= intersection.generateRandom();
 						}
 						else {
 							rhsValue	= requiredValue;
 							intersection.setLowerBound(requiredValue);
 							lhsValue	= intersection.generateRandom();
 						}
 					}
 					break;
 				case SUM:
 					if(requiredValue.compareTo(intersection.upperBound(), TokenCmpOp.GE) == 0) {
 						intersection.setLowerBound(requiredValue.subtract(intersection.upperBound()));
 						intersection.setLowerBoundType(BoundType.OPEN);
 					}
 					else {
 						intersection.setUpperBound(requiredValue);
 					}
 					lhsValue	= intersection.generateRandom();
 					rhsValue	= requiredValue.subtract(lhsValue);
 					break;
 				}
 				break;
 			case BASIC:
 //				TODO
 				break;
 			case INHERIT:
 //				TODO
 				break;
 			case RANDOM:
 //				TODO
 				break;
 			}
 			
 			if(object != null) {
 				values.put(object, requiredValue);
 			}
 			if(lhs != null)
 				this.lhs.split(values, lhsValue, definition);
 			if(rhs != null)
 				this.rhs.split(values, rhsValue, definition);
 		}
 	}
 }
 
