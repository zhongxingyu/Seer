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
 
 import internal.parser.containers.IStatement;
 import internal.parser.containers.Reference;
 import internal.parser.containers.Relation;
 import internal.parser.containers.Relation.InbuiltRelationEnum;
 import internal.parser.containers.pattern.IPattern;
 import internal.parser.containers.query.IQuery;
 import internal.space.Space.Direction;
 import internal.tree.IWorldTree;
 import static test.ui.UIDebugEngine.multiLine;
 
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
 	 * @return {@code String} representing the output of the {@code IStatement}
 	 */
 	public static String evaluate(IWorldTree node, IStatement statement) {
 		if(instance == null)
 			init();
 		return instance.resolve(node, statement);
 	}
 
 	/**
 	 * Resolve method that is private to {@code ResolutionEngine} and used to evaluate an {@code IStatement}
 	 * @param node {@code IWorldTree} object upon which the {@code IStatement} is to be evaluated
 	 * @param query {@code IStatement} object containing the statement to evaluate
 	 * @return {@code String} representing the output of the {@code IStatement}
 	 */
 	private String resolve(IWorldTree node, IStatement statement) {
 		Result result = new Result();
 		switch(statement.getType()) {
 		case CONSTRAINT: {
 			break;
 		}
 		case PROPERTYDEF: {
 			break;
 		}
 		case QUERY: {
 			IQuery query = (IQuery) statement;
 			Class<?> level		= query.level();
 			IPattern pattern	= query.pattern();
 			
 			List<IWorldTree> objectList = getObjects(node, level);
 			
 			while(query != null) {
 				level		= query.level();
 				pattern		= query.pattern();
 				
 				
 				String rhsColumnName 	= pattern.rhs().toString();
 				Column rhsColumn		= result.get(rhsColumnName);
 				if(rhsColumn == null)
 					rhsColumn			= new Column(rhsColumnName, objectList);
 				
 				while(pattern != null) {
 					result = resolveQuery(node, level, pattern, result, rhsColumn);
 					pattern = pattern.subPattern();
 				}
 				query = query.subQuery();
 			}
 			break;
 		}
 		default:
 			break;
 		}
 		return makeString(statement, result);
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
 	 * Helper method used to convert {@code Result} to a {@code String}
 	 * @param statement {@code IStatement} representing the statement that is being evaluated
 	 * @param result {@code Result} representing the collection that needs to be flattened
 	 * @return {@code String} representing the flattened version of the parameter <b>result</b>
 	 */
 	private String makeString(IStatement statement, Result result) {
 		StringBuffer sb = new StringBuffer(statement.toString() + "\n" + statement.debugString() + "\n\n");
 		
 		int rowIndex = 0;
 		while(rowIndex < result.get(0).size()) {
 			List<String> stringList 	= new ArrayList<String>();
 			for(Column t : result) {
 				IWorldTree obj 			= t.get(rowIndex);
 				StringBuffer visual 	= new StringBuffer(t.name + "\n" + obj.absoluteName() + "  \n");
 				List<String> stringRep	= t.get(rowIndex).getStringRepresentation();
 				for(String line : stringRep) {
 					visual.append(line + "\n");
 				}
 				stringList.add(visual.toString());
 			}
 			String multiline = multiLine(stringList);
 			sb.append(multiline + "\n\n");
 			
 			rowIndex++;
 		}
 		return sb.toString();
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
