 package internal.parser.resolve;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import internal.parser.containers.Datum;
 import internal.parser.containers.IStatement;
 import internal.parser.containers.Reference;
 import internal.parser.containers.Relation;
 import internal.parser.containers.Relation.InbuiltRelationEnum;
 import internal.parser.containers.pattern.IPattern;
 import internal.parser.containers.property.PropertyDef;
 import internal.parser.containers.query.IQuery;
 import internal.space.Space.Direction;
 import internal.tree.IWorldTree;
 
 /**
  * ResolutionEngine is a Singleton class responsible for evaluating statements issued by the user
  * @author guru
  *
  */
 public class ResolutionEngine {
 	private Map<String, Method> relationMap = new HashMap<String, Method>();
 	private static ResolutionEngine instance = null;
 	
 	protected ResolutionEngine() {
 //		Prevent initialization of ResolutionEngine
 	}
 	
 	/**
 	 * Evaluate an {@code IStatement}
 	 * @param node {@code IWorldTree} object upon which the {@code IStatement} is to be evaluated
 	 * @param query {@code IStatement} object containing the statement to evaluate
 	 * @return {@code Result} representing the output of the {@code IStatement}
 	 */
 	public static Result evaluate(IWorldTree node, IStatement statement) {
 		if(instance == null)
 			init();
 		return instance.resolve(node, statement);
 	}
 
 	/**
 	 * Resolve method that is private to {@code ResolutionEngine} and used to evaluate an {@code IStatement}
 	 * @param node {@code IWorldTree} object upon which the {@code IStatement} is to be evaluated
 	 * @param query {@code IStatement} object containing the statement to evaluate
 	 * @return {@code Result} representing the output of the {@code IStatement}
 	 */
 	private Result resolve(IWorldTree node, IStatement statement) {
 		Result result = new Result();
 		Result oldResult = null;
 		switch(statement.getType()) {
 		case CONSTRAINT: {
 			break;
 		}
 		case PROPERTYDEF: {
 			result = resolveDefinition(node, (PropertyDef) statement);
 			break;
 		}
 		case QUERY: {
 			IQuery query = (IQuery) statement;
 			Class<?> level		= query.level().HierarchyClass();
 			IPattern pattern	= query.pattern();
 			
 			List<IWorldTree> objectList = getObjects(node, level);
 			
 //			Filter objectList based on condition - if any
 			if(query.condition() != null) {
 				String property = query.condition().property().name();
 				for(IWorldTree object : getObjects(node, level)) {
 					if(!object.properties().containsKey(property))
 						objectList.remove(object);
 					else {
 						Datum conditionValue = query.condition().value();
 						Datum objPropValue = object.properties().get(property);
 						if(objPropValue.compareTo(conditionValue, query.condition().operator()) != 0)
 							objectList.remove(object);
 					}
 				}
 			}
 			while(query != null) {
 				level		= query.level().HierarchyClass();
 				pattern		= query.pattern();
 				result		= new Result();
 				while(pattern != null) {
 					String rhsColumnName	= null;
 					if(pattern.rhs() == null) {
 //						FIXME
 						result.add(new Column(pattern.lhs().toString(), objectList));
 						return result;
 					}
 					else
 						rhsColumnName 		= pattern.rhs().toString();
 					Column rhsColumn		= result.get(rhsColumnName);
 					if(rhsColumn == null)
 						rhsColumn			= new Column(rhsColumnName, objectList);
 					
 					result = resolveQuery(node, level, pattern, result, rhsColumn);
 					pattern = pattern.subPattern();
 				}
 //				Check if we need to union
 				if(oldResult != null) {
					String errMsg = "Cannot union " + result.toString() + "\n AND \n" + oldResult.toString();
 //					First check for semantics
					assert result.size() == oldResult.size() : errMsg;
 					int index = 0;
 					while(index < result.size()) {	//We have already verified that both results have same number of columns
						assert result.get(index).name.equals(oldResult.get(index).name) : errMsg;
 						index++;
 					}
 					
 //					Now do the actual merge
 					List<IWorldTree> row = null;
 					for(index = 0; index < oldResult.get(0).size(); index++) {
 						row = oldResult.getRow(index);
						if(!result.contains(row))
 							result.add(row);
 					}
 				}
 				query = query.subQuery();
 				oldResult = result;
 			}
 			break;
 		}
 		default:
 			break;
 		}
 //		return makeString(statement, result);
 		return result;
 	}
 
 	private Result resolveDefinition(IWorldTree node, PropertyDef definition) {
 		Result result = resolve(node, definition.query());
 		
 		Reference childReference 	= definition.query().pattern().lhs();
 		Column childNodes			= result.get(childReference.toString());
 		String propertyName			= definition.property().name();
 		
 		switch(definition.type()) {
 		case AGGREGATE:
 			switch(definition.aggregateExpression().type()) {
 			case COUNT:
 				node.addProperty(propertyName, new Datum.Int(childNodes.size()));
 				break;
 			case MAX:
 				float maxValue = 0;
 				for(IWorldTree childNode : childNodes) {
 					Datum datum = childNode.properties().get(propertyName);
 					if(datum != null) {
 						float value = (Float) datum.toFlt().data();
 						maxValue = maxValue > value ? maxValue : value;
 					}
 				}
 				node.addProperty(propertyName, new Datum.Flt(maxValue));	//FIXME: Should probably be same type as child datum(s)
 				break;
 			case MIN:
 				float minValue = 0;
 				for(IWorldTree childNode : childNodes) {
 					Datum datum = childNode.properties().get(propertyName);
 					if(datum != null) {
 						float value = (Float) datum.toFlt().data();
 						minValue = minValue < value ? minValue : value;
 					}
 				}
 				node.addProperty(propertyName, new Datum.Flt(minValue));	//FIXME: Should probably be same type as child datum(s)
 				break;
 			case SUM:
 				float sum = 0;
 				for(IWorldTree childNode : childNodes) {
 					Datum datum = childNode.properties().get(propertyName);
 					if(datum != null) {
 						float value = (Float) datum.toFlt().data();
 						sum += value;
 					}
 				}
 				node.addProperty(propertyName, new Datum.Flt(sum));			//FIXME: Should probably be same type as child datum(s)
 				break;
 			default:
 				break;
 			}
 			break;
 		case INHERIT:
 			break;
 		case BASIC:
 		case RANDOM:
 		default:
 			throw new IllegalStateException("Cannot handle property-definitions of type " + definition.type() + " in ResolutionEngine");
 		}
 		return result;
 	}
 
 	/**
 	 * Resolve method that is specifically designed to handle {@code IQuery}
 	 * @param node {@code IWorldTree} object upon which the {@code IQuery} is to be evaluated
 	 * @param level {@code Class<?>} representing the hierarchical level of WorldTree
 	 * @param pattern {@code IPattern} representing the pattern to search for
 	 * @param result {@code Result} object containing previous query results(if any)
 	 * @param objects {@code Column} containing the objects to iterate over while resolving this {@code IQuery}
 	 * @return {@code Result} containing tuples satisfying the {@code IQuery}
 	 */
 	private Result resolveQuery(IWorldTree node, Class<?> level, IPattern pattern, Result result, Column objects) {
 		Relation relation = pattern.relation();
 		switch(relation.type()) {
 		case CUSTOM:
 			break;
 		case INBUILT:
 			Method method = null;
 			try {
 				method = instance.relationMap.get(relation.name().toLowerCase());
 				result = (Result) method.invoke(null, pattern, result, objects);
 				if(pattern.relation().regex().equals(Relation.Regex.PLUS)) {
 //					FIXME: Remove * entries
 					Column lhs = result.get(pattern.lhs().toString());
 					Column rhs = result.get(pattern.rhs().toString());
 					int index = 0;
 					assert(lhs.size() == rhs.size());
 					while(index < lhs.size()) {
 						if(lhs.get(index).equals(rhs.get(index))) {	//FIXME
 							result.removeRow(index);
 							continue;
 						}
 						index++;
 					}
 				}
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			}
 			break;
 		default:
 			throw new IllegalStateException("Cannot have a type that does not exist in " + Relation.Type.values());
 		}
 		return result;
 	}
 	
 	/**
 	 * Obtain a collection of all objects in the specified hierarchy level from given {@code IWorldTree} instance <br>
 	 * The tree is traversed using the argument <b> node </b> as root
 	 * @param node {@code IWorldTree} 'root' of the tree
 	 * @param level {@code Class<?>} hierarchy level with which objects are filtered
 	 * @return {@code Collection<IWorldTree>} containing all nodes in the tree at <b> level </b> having <b> node </b> as root 
 	 */
 	private List<IWorldTree> getObjects(IWorldTree node, Class<?> level) {
		List<IWorldTree> nodeList	= new ArrayList<IWorldTree>();
		List<IWorldTree> objectList	= new ArrayList<IWorldTree>();
 //		Get collection of relevant objects
 		nodeList.add(node);
 		IWorldTree currentNode = null;
 		while(nodeList.size() > 0) {
 			currentNode = nodeList.get(0);
 			for(IWorldTree child : currentNode.children()) {
 				if(child.getClass().equals(level))
 					objectList.add(child);
 				else
 					nodeList.add(child);
 			}
 			nodeList.remove(currentNode);
 		}
 		return objectList;
 	}
 	
 	/**
 	 * Initialize the instance
 	 */
 	private static void init() {
 //		TODO: Figure out a nice way to add future methods similar to the way direction is being resolved
 		instance = new ResolutionEngine();
 		try {
 			for(Method m : InbuiltRelations.class.getMethods()) {
 				if(m.isAnnotationPresent(InbuiltRelations.Proxy.class)) {
 					assert(m.isAnnotationPresent(InbuiltRelations.Inbuilt.class));
 					InbuiltRelations.Proxy proxy = m.getAnnotation(InbuiltRelations.Proxy.class);
 					for(String proxyMethod : proxy.methods().split(" "))
 						instance.relationMap.put(proxyMethod, m);
 				}
 			}
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Container class used to store logic for processing built-in relations
 	 * @author guru
 	 *
 	 */
 	private static class InbuiltRelations {
 		
 		/**
 		 * Annotation to suggest that a method is used as a proxy for other methods
 		 * @author guru
 		 *
 		 */
 		@Target(ElementType.METHOD)
 		@Retention(RetentionPolicy.RUNTIME)
 		public @interface Proxy {
 			String methods() default "";
 		}
 		
 		/**
 		 * Annotation to suggest that a method is built-in
 		 * @author guru
 		 *
 		 */
 		@Target(ElementType.METHOD)
 		@Retention(RetentionPolicy.RUNTIME)
 		public @interface Inbuilt {
 		}
 		
 		/**
 		 * Built-in method to handle all direction related queries
 		 * @param pattern {@code IPattern} object specifying the pattern to test for
 		 * @param result {@code Result} containing previous results
 		 * @param objectList {@code Column} containing objects to iterate over 
 		 * @return {@code Result} containing the various valid sets
 		 */
 		@Inbuilt
 		@Proxy(methods = "toeast towest tonorth tosouth")
 		public static Result direction(IPattern pattern, Result result, Column objectList) {
 			Relation relation 	= pattern.relation();
 			Result subResult 	= Result.newCopy(result);
 			
 //			Copy over any missing columns
 			for(Reference r : pattern.references()) {
 				if(!subResult.contains(r.toString()))
 					subResult.add(new Column(r.toString()));
 			}
 			
 //			Obtain one of the columns
 			int columnIndex = subResult.indexOf(pattern.lhs().toString());
 			
 			
 			if(!relation.regex().equals(Relation.Regex.NONE)) {
 				List<IWorldTree> row = new ArrayList<IWorldTree>(subResult.size());
 				int rowIndex = 0;
 				while(rowIndex < objectList.size()) {
 					IWorldTree node = objectList.get(rowIndex);
 					
 					if(result.size() >= 2) {
 						row.addAll(result.getRow(rowIndex));
 						row.add(columnIndex, node);
 						assert subResult.size() == row.size() : "Trying to insert " + row.toString() + " into \n" + result.toString();
 					}
 					else {
 						row.add(node);
 						row.add(node);
 					}
 					
 					subResult.add(row);
 					row.clear();
 					rowIndex++;
 				}
 			}
 			
 			int objIndex = 0;
 			while(objIndex < objectList.size()) {
 				IWorldTree node = objectList.get(objIndex);
 				
 				IWorldTree dNode = null;
 				
 //				Find the neighbour..we are the LHS..so we invert the directions
 //				In A TOEAST B, LHS = A, thus B is actually to the west
 				switch(InbuiltRelationEnum.check(relation.name())) {
 				case BEGIN:
 					break;
 				case END:
 					break;
 				case TO_EAST:
 					dNode = node.neighbour(Direction.E);
 					break;
 				case TO_NORTH:
 					dNode = node.neighbour(Direction.N);
 					break;
 				case TO_SOUTH:
 					dNode = node.neighbour(Direction.S);
 					break;
 				case TO_WEST:
 					dNode = node.neighbour(Direction.W);
 					break;
 				default:
 					throw new IllegalStateException(relation.name() + " resolved to inbuilt?!\n");
 				}
 				
 				if(dNode != null) {
 					switch(relation.regex()) {
 					case NONE:
 						List<IWorldTree> row = new ArrayList<IWorldTree>(subResult.size());
 						if(result.size() >= 2) {
 							row.addAll(result.getRow(objIndex));
 							row.add(columnIndex, dNode);
 						}
 						else {
 							row.add(columnIndex, dNode);
 							row.add(node);
 						}
 						subResult.add(row);
 						break;
 					case PLUS:
 					case STAR:
 						Column recursiveList 	= new Column(objectList.name);
 						recursiveList.add(dNode);
 						Result recursiveResult 	= direction(pattern, result, recursiveList);
 						int rows = recursiveResult.get(0).size();
 						
 						row = new ArrayList<IWorldTree>(subResult.size());
 						for(int index = 0; index < rows; index++) {
 							if(result.size() >= 2) {
 								row.addAll(result.getRow(objIndex));
 								row.add(recursiveResult.getRow(index).get(columnIndex));
 							}
 							else {
 								row.add(columnIndex, recursiveResult.get(columnIndex).get(index));
 								row.add(node);
 							}
 							subResult.add(row);
 							row.clear();
 						}
 						break;
 					default:
 						break;
 					}
 				}
 				objIndex++;
 			}
 			return subResult;
 		}
 	}
 }
