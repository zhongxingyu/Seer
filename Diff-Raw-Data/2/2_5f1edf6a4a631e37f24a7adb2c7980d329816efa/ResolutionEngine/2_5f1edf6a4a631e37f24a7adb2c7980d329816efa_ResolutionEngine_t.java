 package internal.parser.resolve;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import internal.containers.Relation;
 import internal.containers.pattern.IPattern;
 import internal.containers.query.IQuery;
 import internal.space.Space.Direction;
 import internal.tree.IWorldTree;
 import static test.ui.UIDebugEngine.multiLine;
 import static test.ui.UIDebugEngine.pad;
 import static test.ui.UIDebugEngine.write;
 
 public class ResolutionEngine {
 	
 	public static String resolve(IWorldTree node, IQuery query) {
 		Class<?> level		= query.level();
 		IPattern pattern	= query.pattern();
 		Collection<Collection<IWorldTree>> result = null;
 		while(pattern != null) {
 //			TODO:Join?
 			result = resolve(node, level, pattern);
 			pattern = pattern.subPattern();
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		for(Collection<IWorldTree> collection : result) {
 			List<String> stringList = new ArrayList<String>();
 			for(IWorldTree obj : collection) {
				stringList.add(obj.absoluteName() + "  \n" + obj.toString());
 			}
 			String multiline = multiLine(stringList);
 			sb.append(multiline + "\n\n");
 		}
 		return sb.toString();
 	}
 
 	private static Collection<Collection<IWorldTree>> resolve(IWorldTree node, Class<?> level, IPattern pattern) {
 		List<IWorldTree> nodeList   = new ArrayList<IWorldTree>();
 		List<IWorldTree> objectList = new ArrayList<IWorldTree>();
 		Collection<Collection<IWorldTree>> result = null;
 		
 		nodeList.add(node);
 		
 //		Get collection of relevant objects
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
 		
 		Relation relation = pattern.relation();
 		switch(relation.type()) {
 		case CUSTOM:
 			break;
 		case INBUILT:
 			Method m = null;
 			try {
 				m = ResolutionEngine.class.getDeclaredMethod(relation.name(), Relation.class, List.class);
 				result = (Collection<Collection<IWorldTree>>) m.invoke(null, relation, objectList);
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
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
 	
 	@SuppressWarnings("unused")
 	private static Collection<Collection<IWorldTree>> toeast(Relation relation, List<IWorldTree> nodeList) {
 		Collection<Collection<IWorldTree>> result = new ArrayList<Collection<IWorldTree>>();
 		Map<IWorldTree, List<List<IWorldTree>>> map = new HashMap<IWorldTree, List<List<IWorldTree>>>();
 		
 		for(IWorldTree node : nodeList)
 			map.put(node, new ArrayList<List<IWorldTree>>());
 		
 //		TODO: Decide if A toeast B resolves as B,A or A,B...Currently resolves as B,A
 		for(IWorldTree node : nodeList) {
 			List<IWorldTree> subResult = new ArrayList<IWorldTree>();
 			subResult.add(node);
 			IWorldTree dNode = node.neighbour(Direction.E);
 //			If null, we still need to handle *
 			if(dNode == null) {
 				if(relation.regex().equals(Relation.Regex.STAR)) {
 					subResult.add(node);
 					map.get(node).add(new ArrayList<IWorldTree>(subResult));
 				}
 			}
 			else {
 				subResult.add(dNode);	//Regardless of regex type, we need to add this set to the collection 
 				map.get(node).add(new ArrayList<IWorldTree>(subResult));
 				switch(relation.regex()) {
 				case NONE:
 					continue;	//We got a match! Continue with next node
 				case PLUS:
 				case STAR:
 //					Need to recursively find all recursive sets
 					subResult.remove(0);	//Remove first element to avoid infinite recursion
 					for(IWorldTree subNode : subResult) {
 						Collection<Collection<IWorldTree>> recursiveResult = toeast(relation, subResult);
 						for(Collection<IWorldTree> col : recursiveResult) {
 							List<IWorldTree> subCollectionList = new ArrayList<IWorldTree>(col);
 							map.get(node).add(subCollectionList);
 						}
 					}
 					break;
 				}
 			}
 		}
 		
 //		The actual return logic
 		for(Map.Entry<IWorldTree, List<List<IWorldTree>>> entry : map.entrySet())
 			result.addAll(entry.getValue());
 		
 		return result;
 	}
 }
