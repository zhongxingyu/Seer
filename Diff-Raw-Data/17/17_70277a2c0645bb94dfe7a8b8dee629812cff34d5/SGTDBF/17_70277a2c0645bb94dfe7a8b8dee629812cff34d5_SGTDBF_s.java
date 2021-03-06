 package org.rascalmpl.parser.gtd;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URI;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.rascalmpl.interpreter.asserts.ImplementationError;
 import org.rascalmpl.interpreter.staticErrors.SyntaxError;
 import org.rascalmpl.interpreter.staticErrors.UndeclaredNonTerminalError;
 import org.rascalmpl.parser.gtd.result.AbstractContainerNode;
 import org.rascalmpl.parser.gtd.result.AbstractNode;
 import org.rascalmpl.parser.gtd.result.ListContainerNode;
 import org.rascalmpl.parser.gtd.result.SortContainerNode;
 import org.rascalmpl.parser.gtd.result.AbstractNode.CycleMark;
 import org.rascalmpl.parser.gtd.result.AbstractNode.FilteringTracker;
 import org.rascalmpl.parser.gtd.result.action.IActionExecutor;
 import org.rascalmpl.parser.gtd.result.action.VoidActionExecutor;
 import org.rascalmpl.parser.gtd.result.struct.Link;
 import org.rascalmpl.parser.gtd.stack.AbstractStackNode;
 import org.rascalmpl.parser.gtd.stack.IMatchableStackNode;
 import org.rascalmpl.parser.gtd.stack.NonTerminalStackNode;
 import org.rascalmpl.parser.gtd.util.ArrayList;
 import org.rascalmpl.parser.gtd.util.DoubleStack;
 import org.rascalmpl.parser.gtd.util.HashMap;
 import org.rascalmpl.parser.gtd.util.IndexedStack;
 import org.rascalmpl.parser.gtd.util.IntegerKeyedHashMap;
 import org.rascalmpl.parser.gtd.util.IntegerList;
 import org.rascalmpl.parser.gtd.util.LinearIntegerKeyedMap;
 import org.rascalmpl.parser.gtd.util.ObjectIntegerKeyedHashMap;
 import org.rascalmpl.parser.gtd.util.Stack;
 import org.rascalmpl.parser.gtd.util.specific.PositionStore;
 import org.rascalmpl.values.ValueFactoryFactory;
 
 public abstract class SGTDBF implements IGTD{
 	private final static int STREAM_READ_SEGMENT_SIZE = 8192;
 	
 	private final static int DEFAULT_TODOLIST_CAPACITY = 16;
 	
 	protected final static IValueFactory vf = ValueFactoryFactory.getValueFactory();
 	
 	private URI inputURI;
 	private char[] input;
 	private final PositionStore positionStore;
 	
 	private Stack<AbstractStackNode>[] todoLists;
 	private int queueIndex;
 	
 	private final Stack<AbstractStackNode> stacksToExpand;
 	private Stack<AbstractStackNode> stacksWithTerminalsToReduce;
 	private final DoubleStack<AbstractStackNode, AbstractNode> stacksWithNonTerminalsToReduce;
 	
 	private final ArrayList<AbstractStackNode[]> lastExpects;
 	private final LinearIntegerKeyedMap<AbstractStackNode> sharedLastExpects;
 	private final LinearIntegerKeyedMap<AbstractStackNode> sharedPrefixNext;
 	
 	private final HashMap<String, ArrayList<AbstractStackNode>> cachedEdgesForExpect;
 	
 	private final IntegerKeyedHashMap<AbstractStackNode> sharedNextNodes;
 
 	private final IntegerKeyedHashMap<ObjectIntegerKeyedHashMap<String, AbstractContainerNode>> resultStoreCache;
 	
 	private int location;
 	private boolean shiftedLevel;
 	
 	protected char lookAheadChar;
 	
 	private final HashMap<String, Method> methodCache;
 	
 	public SGTDBF(){
 		super();
 		
 		positionStore = new PositionStore();
 		
 		stacksToExpand = new Stack<AbstractStackNode>();
 		stacksWithNonTerminalsToReduce = new DoubleStack<AbstractStackNode, AbstractNode>();
 		
 		lastExpects = new ArrayList<AbstractStackNode[]>();
 		sharedLastExpects = new LinearIntegerKeyedMap<AbstractStackNode>();
 		sharedPrefixNext = new LinearIntegerKeyedMap<AbstractStackNode>();
 		cachedEdgesForExpect = new HashMap<String, ArrayList<AbstractStackNode>>();
 		
 		sharedNextNodes = new IntegerKeyedHashMap<AbstractStackNode>();
 		
 		resultStoreCache = new IntegerKeyedHashMap<ObjectIntegerKeyedHashMap<String, AbstractContainerNode>>();
 		
 		location = 0;
 		shiftedLevel = false;
 		
 		methodCache = new HashMap<String, Method>();
 	}
 	
 	protected void expect(IConstructor production, AbstractStackNode... symbolsToExpect){
 		lastExpects.add(symbolsToExpect);
 		
 		AbstractStackNode lastNode = symbolsToExpect[symbolsToExpect.length - 1];
 		lastNode.setParentProduction(production);
 	}
 	
 	protected void expect(IConstructor production, IMatchableStackNode[] followRestrictions, AbstractStackNode... symbolsToExpect){
 		lastExpects.add(symbolsToExpect);
 		
 		AbstractStackNode lastNode = symbolsToExpect[symbolsToExpect.length - 1];
 		lastNode.setParentProduction(production);
 		lastNode.setFollowRestriction(followRestrictions);
 	}
 	
 	protected void expectReject(IConstructor production, AbstractStackNode... symbolsToExpect){
 		lastExpects.add(symbolsToExpect);
 		
 		AbstractStackNode lastNode = symbolsToExpect[symbolsToExpect.length - 1];
 		lastNode.setParentProduction(production);
 		lastNode.markAsReject();
 	}
 	
 	protected void expectReject(IConstructor production, IMatchableStackNode[] followRestrictions, AbstractStackNode... symbolsToExpect){
 		lastExpects.add(symbolsToExpect);
 		
 		AbstractStackNode lastNode = symbolsToExpect[symbolsToExpect.length - 1];
 		lastNode.setParentProduction(production);
 		lastNode.setFollowRestriction(followRestrictions);
 		lastNode.markAsReject();
 	}
 	
 	protected void invokeExpects(AbstractStackNode nonTerminal){
 		String name = nonTerminal.getName();
 		Method method = methodCache.get(name);
 		if(method == null){
 			try{
 				method = getClass().getMethod(name);
 				try{
 					method.setAccessible(true); // Try to bypass the 'isAccessible' check to save time.
 				}catch(SecurityException sex){
 					// Ignore this if it happens.
 				}
 			}catch(NoSuchMethodException nsmex){
 				int errorLocation = (location == Integer.MAX_VALUE ? 0 : location);
 				int line = positionStore.findLine(errorLocation);
 				int column = positionStore.getColumn(errorLocation, line);
 				throw new UndeclaredNonTerminalError(name, vf.sourceLocation(inputURI, errorLocation, 0, line + 1, line + 1, column, column), nsmex);
 			}
 			methodCache.putUnsafe(name, method);
 		}
 		
 		try{
 			method.invoke(this);
 		}catch(IllegalAccessException iaex){
 			throw new ImplementationError(iaex.getMessage(), iaex);
 		}catch(InvocationTargetException itex){
 			throw new ImplementationError(itex.getTargetException().getMessage(), itex.getTargetException());
 		} 
 	}
 	
 	private AbstractStackNode updateNextNode(AbstractStackNode next, AbstractStackNode node, AbstractNode result){
 		AbstractStackNode alternative = sharedNextNodes.get(next.getId());
 		if(alternative != null){
 			if(alternative.isEndNode()){
 				if(result.isEmpty() && !node.isMatchable() && !next.isMatchable()){
 					if(alternative.getId() != node.getId() && !(alternative.isSeparator() || node.isSeparator())){ // (Separated) list cycle fix.
 						ObjectIntegerKeyedHashMap<String, AbstractContainerNode> levelResultStoreMap = resultStoreCache.get(location);
 						AbstractContainerNode resultStore = levelResultStoreMap.get(alternative.getName(), getResultStoreId(alternative.getId()));
 						if(resultStore != null){
 							// Encountered self recursive epsilon cycle; update the prefixes.
 							if(updatePrefixes(alternative.getParentProduction(), node, result, resultStore)) return alternative;
 						}
 					}
 				}
 			}
 			
 			alternative.updateNode(node, result);
 			
 			return alternative;
 		}
 		
 		next = next.getCleanCopy();
 		next.setStartLocation(location);
 		next.updateNode(node, result);
 		
 		sharedNextNodes.putUnsafe(next.getId(), next);
 		stacksToExpand.push(next);
 		
 		return next;
 	}
 	
 	private void updateAlternativeNextNode(AbstractStackNode next, LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edgesMap, ArrayList<Link>[] prefixesMap){
 		int id = next.getId();
 		AbstractStackNode alternative = sharedNextNodes.get(id);
 		if(alternative != null){
 			alternative.updatePrefixSharedNode(edgesMap, prefixesMap); // Prevent unnecessary overhead; share whenever possible.
 		}else{
 			next = next.getCleanCopy();
 			next.updatePrefixSharedNode(edgesMap, prefixesMap); // Prevent unnecessary overhead; share whenever possible.
 			next.setStartLocation(location);
 			
 			sharedNextNodes.putUnsafe(id, next);
 			stacksToExpand.push(next);
 		}
 	}
 	
 	private boolean updatePrefixes(IConstructor production, AbstractStackNode node, AbstractNode nodeResultStore, AbstractNode nextResultStore){
 		LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edgesMap = node.getEdges();
 		ArrayList<Link>[] prefixes = node.getPrefixesMap();
 		
 		for(int i = edgesMap.size() - 1; i >= 0; --i){
 			int startPosition = edgesMap.getKey(i);
 			ArrayList<AbstractStackNode> edgesPart = edgesMap.getValue(i);
 			
 			ObjectIntegerKeyedHashMap<String, AbstractContainerNode> levelResultStoreMap = resultStoreCache.get(startPosition);
 			if(levelResultStoreMap == null) return false;
 			Link prefix = constructPrefixesFor(edgesMap, prefixes, nodeResultStore, startPosition);
 			ArrayList<Link> edgePrefixes = new ArrayList<Link>();
 			edgePrefixes.add(prefix);
 			
 			ArrayList<String> firstTimeReductions = new ArrayList<String>();
 			for(int j = edgesPart.size() - 1; j >= 0; --j){
 				AbstractStackNode edge = edgesPart.get(0);
 				String edgeName = edge.getName();
 				int resultStoreId = getResultStoreId(edge.getId());
 				
 				if(!firstTimeReductions.contains(edgeName)){
 					firstTimeReductions.add(edgeName);
 					
 					AbstractContainerNode resultStore = levelResultStoreMap.get(edgeName, resultStoreId);
 					if(resultStore == null) return false;
 					resultStore.addAlternative(production, new Link(edgePrefixes, nextResultStore));
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	private void updateEdges(AbstractStackNode node, AbstractNode result){
 		IConstructor production = node.getParentProduction();
 		
 		LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edgesMap = node.getEdges();
 		ArrayList<Link>[] prefixesMap = node.getPrefixesMap();
 		
 		for(int i = edgesMap.size() - 1; i >= 0; --i){
 			int startLocation = edgesMap.getKey(i);
 			ArrayList<AbstractStackNode> edgeList = edgesMap.getValue(i);
 			
 			ObjectIntegerKeyedHashMap<String, AbstractContainerNode> levelResultStoreMap = resultStoreCache.get(startLocation);
 			
 			if(levelResultStoreMap == null){
 				levelResultStoreMap = new ObjectIntegerKeyedHashMap<String, AbstractContainerNode>();
 				resultStoreCache.putUnsafe(startLocation, levelResultStoreMap);
 			}
 			
 			Link resultLink = new Link((prefixesMap != null) ? prefixesMap[i] : null, result);
 			
 			IntegerList filteredParents = getFilteredParents(node.getId());
 			
 			ObjectIntegerKeyedHashMap<String, AbstractContainerNode> firstTimeReductions = new ObjectIntegerKeyedHashMap<String, AbstractContainerNode>();
 			for(int j = edgeList.size() - 1; j >= 0; --j){
 				AbstractStackNode edge = edgeList.get(j);
 				String nodeName = edge.getName();
 				int resultStoreId = getResultStoreId(edge.getId());
 				
 				AbstractContainerNode resultStore = firstTimeReductions.get(nodeName, resultStoreId);
 				if(resultStore == null){
 					resultStore = levelResultStoreMap.get(nodeName, resultStoreId);
 					
 					if(filteredParents == null || !filteredParents.contains(edge.getId())){
 						if(resultStore != null){
 							if(!resultStore.isRejected()) resultStore.addAlternative(production, resultLink);
 						}else{
 							resultStore = (!edge.isList()) ? new SortContainerNode(inputURI, startLocation, location, startLocation == location, edge.isSeparator(), edge.isLayout()) : new ListContainerNode(inputURI, startLocation, location, startLocation == location, edge.isSeparator(), edge.isLayout());
 							levelResultStoreMap.putUnsafe(nodeName, resultStoreId, resultStore);
 							resultStore.addAlternative(production, resultLink);
 							
 							stacksWithNonTerminalsToReduce.push(edge, resultStore);
 							
 							firstTimeReductions.putUnsafe(nodeName, resultStoreId, resultStore);
 						}
 					}
 				}else{
 					stacksWithNonTerminalsToReduce.push(edge, resultStore);
 				}
 			}
 		}
 	}
 	
 	private void updateRejects(AbstractStackNode node){
 		LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edgesMap = node.getEdges();
 		
 		for(int i = edgesMap.size() - 1; i >= 0; --i){
 			int startLocation = edgesMap.getKey(i);
 			ArrayList<AbstractStackNode> edgeList = edgesMap.getValue(i);
 			
 			ObjectIntegerKeyedHashMap<String, AbstractContainerNode> levelResultStoreMap = resultStoreCache.get(startLocation);
 			
 			if(levelResultStoreMap == null){
 				levelResultStoreMap = new ObjectIntegerKeyedHashMap<String, AbstractContainerNode>();
 				resultStoreCache.putUnsafe(startLocation, levelResultStoreMap);
 			}
 			
 			IntegerList filteredParents = getFilteredParents(node.getId());
 			
 			ObjectIntegerKeyedHashMap<String, AbstractContainerNode> firstTimeReductions = new ObjectIntegerKeyedHashMap<String, AbstractContainerNode>();
 			for(int j = edgeList.size() - 1; j >= 0; --j){
 				AbstractStackNode edge = edgeList.get(j);
 				String nodeName = edge.getName();
 				int resultStoreId = getResultStoreId(edge.getId());
 				
 				AbstractContainerNode resultStore = firstTimeReductions.get(nodeName, resultStoreId);
 				if(resultStore == null){
 					resultStore = levelResultStoreMap.get(nodeName, resultStoreId);
 					
 					if(filteredParents == null || !filteredParents.contains(edge.getId())){
 						if(resultStore != null){
 							resultStore.setRejected();
 						}else{
 							resultStore = (!edge.isList()) ? new SortContainerNode(inputURI, startLocation, location, startLocation == location, edge.isSeparator(), edge.isLayout()) : new ListContainerNode(inputURI, startLocation, location, startLocation == location, edge.isSeparator(), edge.isLayout());
 							levelResultStoreMap.putUnsafe(nodeName, resultStoreId, resultStore);
 							resultStore.setRejected();
 							
 							firstTimeReductions.putUnsafe(nodeName, resultStoreId, resultStore);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void move(AbstractStackNode node, AbstractNode result){
 		if(node.isEndNode()){
 			if(!result.isRejected()){
 				if(!node.isReject()){
 					updateEdges(node, result);
 				}else{
 					updateRejects(node);
 				}
 			}
 		}
 
 		if(node.hasNext()){
 			int nextDot = node.getDot() + 1;
 
 			AbstractStackNode[] prod = node.getProduction();
 			AbstractStackNode next = prod[nextDot];
 			next.setProduction(prod);
 			next = updateNextNode(next, node, result);
 			
 			ArrayList<AbstractStackNode[]> alternateProds = node.getAlternateProductions();
 			if(alternateProds != null){
 				int nextNextDot = nextDot + 1;
 				
 				// Handle alternative nexts (and prefix sharing).
 				sharedPrefixNext.dirtyClear();
 				
 				sharedPrefixNext.add(next.getId(), next);
 				
 				LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edgesMap = next.getEdges();
 				ArrayList<Link>[] prefixesMap = next.getPrefixesMap();
 				
 				for(int i = alternateProds.size() - 1; i >= 0; --i){
 					prod = alternateProds.get(i);
 					if(nextDot == prod.length) continue;
 					AbstractStackNode alternativeNext = prod[nextDot];
 					int alternativeNextId = alternativeNext.getId();
 					
 					AbstractStackNode sharedNext = sharedPrefixNext.findValue(alternativeNextId);
 					if(sharedNext == null){
 						alternativeNext.setProduction(prod);
 						updateAlternativeNextNode(alternativeNext, edgesMap, prefixesMap);
 						
 						sharedPrefixNext.add(alternativeNextId, alternativeNext);
 					}else if(nextNextDot < prod.length){
 						if(alternativeNext.isEndNode()){
 							sharedNext.markAsEndNode();
 							sharedNext.setParentProduction(alternativeNext.getParentProduction());
 							sharedNext.setFollowRestriction(alternativeNext.getFollowRestriction());
 							sharedNext.setReject(alternativeNext.isReject());
 						}
 						
 						sharedNext.addProduction(prod);
 					}
 				}
 			}
 		}
 	}
 	
 	private Link constructPrefixesFor(LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edgesMap, ArrayList<Link>[] prefixesMap, AbstractNode result, int startLocation){
 		if(prefixesMap == null){
 			return new Link(null, result);
 		}
 		
 		int index = edgesMap.findKey(startLocation);
 		return new Link(prefixesMap[index], result);
 	}
 	
 	private void reduceTerminal(AbstractStackNode terminal){
 		move(terminal, terminal.getResult());
 	}
 	
 	private void reduceNonTerminal(AbstractStackNode nonTerminal, AbstractNode result){
 		// Filtering
 		if(nonTerminal.isReductionFiltered(input, location)) return;
 		
 		move(nonTerminal, result);
 	}
 	
 	private void reduce(){
 		// Reduce terminals.
 		while(!stacksWithTerminalsToReduce.isEmpty()){
 			AbstractStackNode terminal = stacksWithTerminalsToReduce.pop();
 			reduceTerminal(terminal);
 		}
 		
 		// Reduce non-terminals.
 		while(!stacksWithNonTerminalsToReduce.isEmpty()){
 			reduceNonTerminal(stacksWithNonTerminalsToReduce.peekFirst(), stacksWithNonTerminalsToReduce.popSecond());
 		}
 	}
 	
 	private boolean findFirstStackToReduce(){
 		for(int i = 0; i < todoLists.length; ++i){
 			Stack<AbstractStackNode> terminalsTodo = todoLists[i];
 			if(!(terminalsTodo == null || terminalsTodo.isEmpty())){
 				stacksWithTerminalsToReduce = terminalsTodo;
 				
 				location += i;
 				shiftedLevel = (location != 0);
 				
 				queueIndex = i;
 				
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean findStacksToReduce(){
 		if(!stacksWithTerminalsToReduce.isEmpty()){
 			shiftedLevel = false;
 			return true;
 		}
 		
 		int queueDepth = todoLists.length;
 		for(int i = 1; i < queueDepth; ++i){
 			queueIndex = (queueIndex + 1) % queueDepth;
 			
 			Stack<AbstractStackNode> terminalsTodo = todoLists[queueIndex];
 			if(!(terminalsTodo == null || terminalsTodo.isEmpty())){
 				stacksWithTerminalsToReduce = terminalsTodo;
 				
 				shiftedLevel = true;
 				location += i;
 				
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean shareListNode(int id, AbstractStackNode stack){
 		AbstractStackNode sharedNode = sharedNextNodes.get(id);
 		if(sharedNode != null){
 			sharedNode.addEdgeWithPrefix(stack, null, location);
 			return true;
 		}
 		return false;
 	}
 	
 	private void handleExpects(AbstractStackNode stackBeingWorkedOn){
 		sharedLastExpects.dirtyClear();
 		
 		ArrayList<AbstractStackNode> cachedEdges = null;
 		
 		for(int i = lastExpects.size() - 1; i >= 0; --i){
 			AbstractStackNode[] expectedNodes = lastExpects.get(i);
 			
 			AbstractStackNode last = expectedNodes[expectedNodes.length - 1];
 			last.markAsEndNode();
 			
 			AbstractStackNode first = expectedNodes[0];
 			
 			// Handle prefix sharing.
 			int firstId = first.getId();
 			AbstractStackNode sharedNode;
 			if((sharedNode = sharedLastExpects.findValue(firstId)) != null){
 				sharedNode.addProduction(expectedNodes);
 				if(expectedNodes.length == 1){
 					sharedNode.markAsEndNode();
 					sharedNode.setParentProduction(last.getParentProduction());
 					sharedNode.setFollowRestriction(last.getFollowRestriction());
 					sharedNode.setReject(last.isReject());
 				}
 				continue;
 			}
 			
 			first = first.getCleanCopy();
 			first.setStartLocation(location);
 			first.setProduction(expectedNodes);
 			first.initEdges();
 			
 			if(cachedEdges == null){
 				cachedEdges = first.addEdge(stackBeingWorkedOn);
 			}else{
 				first.addEdges(cachedEdges, location);
 			}
 			
 			sharedLastExpects.add(firstId, first);
 			
 			stacksToExpand.push(first);
 		}
 		
 		cachedEdgesForExpect.put(stackBeingWorkedOn.getName(), cachedEdges);
 	}
 	
 	protected IntegerList getFilteredParents(int childId){
 		return null; // Default implementation; intended to be overwritten in sub-classes.
 	}
 	
 	protected int getResultStoreId(int parentId){
 		return -1; // Default implementation; intended to be overwritten in sub-classes.
 	}
 	
 	private void expandStack(AbstractStackNode stack){
 		if(stack.isMatchable()){
 			int length = stack.getLength();
 			int endLocation = location + length;
 			if(endLocation <= input.length){
 				if(stack.isLocatable()) stack.setPositionStore(positionStore); // Ugly, but necessary.
 				
 				if(!stack.match(inputURI, input)) return;
 				
 				// Filtering
 				if(stack.isReductionFiltered(input, endLocation)) return;
 				
 				int queueDepth = todoLists.length;
 				if(length >= queueDepth){
 					Stack<AbstractStackNode>[] oldTodoLists = todoLists;
 					todoLists = (Stack<AbstractStackNode>[]) new Stack[length + 1];
 					System.arraycopy(oldTodoLists, queueIndex, todoLists, 0, queueDepth - queueIndex);
 					System.arraycopy(oldTodoLists, 0, todoLists, queueDepth - queueIndex, queueIndex);
 					queueDepth = length + 1;
 					queueIndex = 0;
 				}
 				
 				int insertLocation = (queueIndex + length) % queueDepth;
 				Stack<AbstractStackNode> terminalsTodo = todoLists[insertLocation];
 				if(terminalsTodo == null){
 					terminalsTodo = new Stack<AbstractStackNode>();
 					todoLists[insertLocation] = terminalsTodo;
 				}
 				terminalsTodo.push(stack);
 			}
 			
 			return;
 		}
 		
 		if(!stack.isList()){
			ObjectIntegerKeyedHashMap<String, AbstractContainerNode> levelResultStoreMap = resultStoreCache.get(location);
			if(levelResultStoreMap != null){
				AbstractContainerNode resultStore = levelResultStoreMap.get(stack.getName(), getResultStoreId(stack.getId()));
				if(resultStore != null){ // Is nullable, add the known results.
					stacksWithNonTerminalsToReduce.push(stack, resultStore);
				}
			}
			
 			ArrayList<AbstractStackNode> cachedEdges = cachedEdgesForExpect.get(stack.getName());
 			if(cachedEdges != null){
 				cachedEdges.add(stack);
 			}else{
 				invokeExpects(stack);
 				handleExpects(stack);
 			}
 		}else{ // List
 			AbstractStackNode[] listChildren = stack.getChildren();
 			
 			AbstractStackNode child = listChildren[0];
 			int childId = child.getId();
 			if(!shareListNode(childId, stack)){
 				child = child.getCleanCopy();
 				
 				sharedNextNodes.putUnsafe(childId, child);
 				
 				child.setStartLocation(location);
 				child.initEdges();
 				child.addEdgeWithPrefix(stack, null, location);
 				
 				stacksToExpand.push(child);
 			}
 			
 			if(listChildren.length > 1){ // Star list or optional.
 				// This is always epsilon (and unique for this position); so shouldn't be shared.
 				AbstractStackNode empty = listChildren[1].getCleanCopy();
 				empty.setStartLocation(location);
 				empty.initEdges();
 				empty.addEdge(stack);
 				
 				stacksToExpand.push(empty);
 			}
 		}
 	}
 	
 	private void expand(){
 		while(!stacksToExpand.isEmpty()){
 			lastExpects.dirtyClear();
 			expandStack(stacksToExpand.pop());
 		}
 	}
 	
 	protected boolean isAtEndOfInput(){
 		return (location == input.length);
 	}
 	
 	protected boolean isInLookAhead(char[][] ranges, char[] characters){
 		if(location == input.length) return false;
 		
 		for(int i = ranges.length - 1; i >= 0; --i){
 			char[] range = ranges[i];
 			if(lookAheadChar >= range[0] && lookAheadChar <= range[1]) return true;
 		}
 		
 		for(int i = characters.length - 1; i >= 0; --i){
 			if(lookAheadChar == characters[i]) return true;
 		}
 		
 		return false;
 	}
 	
 	protected IConstructor parse(AbstractStackNode startNode, URI inputURI, char[] input){
 		return parse(startNode, inputURI, input, new VoidActionExecutor());
 	}
 	
 	protected IConstructor parse(AbstractStackNode startNode, URI inputURI, char[] input, IActionExecutor actionExecutor){
 		// Initialize.
 		this.inputURI = inputURI;
 		this.input = input;
 		positionStore.index(input);
 		
 		todoLists = (Stack<AbstractStackNode>[]) new Stack[DEFAULT_TODOLIST_CAPACITY];
 		
 		AbstractStackNode rootNode = startNode.getCleanCopy();
 		rootNode.setStartLocation(0);
 		rootNode.initEdges();
 		stacksToExpand.push(rootNode);
 		lookAheadChar = (input.length > 0) ? input[0] : 0;
 		expand();
 		
 		if(findFirstStackToReduce()){
 			do{
 				lookAheadChar = (location < input.length) ? input[location] : 0;
 				do{
 					if(shiftedLevel){ // Nullable fix.
 						sharedNextNodes.clear();
 						resultStoreCache.clear();
 						cachedEdgesForExpect.clear();
 					}
 					
 					reduce();
 					
 					expand();
 				}while(!stacksWithNonTerminalsToReduce.isEmpty());
 			}while(findStacksToReduce());
 		}
 		
 		if(location == input.length){
 			ObjectIntegerKeyedHashMap<String, AbstractContainerNode> levelResultStoreMap = resultStoreCache.get(0);
 			if(levelResultStoreMap != null){
 				AbstractContainerNode result = levelResultStoreMap.get(startNode.getName(), getResultStoreId(startNode.getId()));
 				if(result != null){
 					FilteringTracker filteringTracker = new FilteringTracker();
 					IConstructor resultTree = result.toTerm(new IndexedStack<AbstractNode>(), 0, new CycleMark(), positionStore, filteringTracker, actionExecutor);
 					if(resultTree != null){
 						return resultTree; // Success.
 					}
 					
 					// Filtering error.
 					int line = positionStore.findLine(filteringTracker.offset);
 					int column = positionStore.getColumn(filteringTracker.offset, line);
 					int endLine = positionStore.findLine(filteringTracker.endOffset);
 					int endColumn = positionStore.getColumn(filteringTracker.endOffset, endLine);
 					throw new SyntaxError("All trees were filtered.", vf.sourceLocation(inputURI, filteringTracker.offset, (filteringTracker.endOffset - filteringTracker.offset), line + 1, endLine + 1, column, endColumn));
 				}
 			}
 		}
 		
 		// Parse error.
 		int errorLocation = (location == Integer.MAX_VALUE ? 0 : location);
 		int line = positionStore.findLine(errorLocation);
 		int column = positionStore.getColumn(errorLocation, line);
 		throw new SyntaxError("Parse error.", vf.sourceLocation(inputURI, errorLocation, 0, line + 1, line + 1, column, column));
 	}
 	
 	protected IConstructor parseFromString(AbstractStackNode startNode, URI inputURI, String inputString, IActionExecutor actionExecutor){
 		return parse(startNode, inputURI, inputString.toCharArray(), actionExecutor);
 	}
 	
 	protected IConstructor parseFromFile(AbstractStackNode startNode, URI inputURI, File inputFile, IActionExecutor actionExecutor) throws IOException{
 		int inputFileLength = (int) inputFile.length();
 		char[] input = new char[inputFileLength];
 		Reader in = new BufferedReader(new FileReader(inputFile));
 		try{
 			in.read(input, 0, inputFileLength);
 		}finally{
 			in.close();
 		}
 		
 		return parse(startNode, inputURI, input, actionExecutor);
 	}
 	
 	// This is kind of ugly.
 	protected IConstructor parseFromReader(AbstractStackNode startNode, URI inputURI, Reader in, IActionExecutor actionExecutor) throws IOException{
 		ArrayList<char[]> segments = new ArrayList<char[]>();
 		
 		// Gather segments.
 		int nrOfWholeSegments = -1;
 		int bytesRead;
 		do{
 			char[] segment = new char[STREAM_READ_SEGMENT_SIZE];
 			bytesRead = in.read(segment, 0, STREAM_READ_SEGMENT_SIZE);
 			
 			segments.add(segment);
 			++nrOfWholeSegments;
 		}while(bytesRead == STREAM_READ_SEGMENT_SIZE);
 		
 		// Glue the segments together.
 		char[] segment = segments.get(nrOfWholeSegments);
 		char[] input;
 		if(bytesRead != -1){
 			input = new char[(nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE) + bytesRead];
 			System.arraycopy(segment, 0, input, (nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE), bytesRead);
 		}else{
 			input = new char[(nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE)];
 		}
 		for(int i = nrOfWholeSegments - 1; i >= 0; --i){
 			segment = segments.get(i);
 			System.arraycopy(segment, 0, input, (i * STREAM_READ_SEGMENT_SIZE), STREAM_READ_SEGMENT_SIZE);
 		}
 		
 		return parse(startNode, inputURI, input, actionExecutor);
 	}
 	
 	// With post parse filtering.
 	public IConstructor parseFromStream(AbstractStackNode startNode, URI inputURI, InputStream in, IActionExecutor actionExecutor) throws IOException{
 		return parseFromReader(startNode, inputURI, new InputStreamReader(in), actionExecutor);
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, char[] input, IActionExecutor actionExecutor){
 		return parse(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, input, actionExecutor);
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, String input, IActionExecutor actionExecutor){
 		return parseFromString(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, input, actionExecutor);
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, InputStream in, IActionExecutor actionExecutor) throws IOException{
 		return parseFromStream(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, in, actionExecutor);
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, Reader in, IActionExecutor actionExecutor) throws IOException{
 		return parseFromReader(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, in, actionExecutor);
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, File inputFile, IActionExecutor actionExecutor) throws IOException{
 		return parseFromFile(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, inputFile, actionExecutor);
 	}
 	
 	// Without post parse filtering.
 	public IConstructor parseFromStream(AbstractStackNode startNode, URI inputURI, InputStream in) throws IOException{
 		return parseFromReader(startNode, inputURI, new InputStreamReader(in), new VoidActionExecutor());
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, char[] input){
 		return parse(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, input);
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, String input){
 		return parseFromString(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, input, new VoidActionExecutor());
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, InputStream in) throws IOException{
 		return parseFromStream(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, in);
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, Reader in) throws IOException{
 		return parseFromReader(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, in, new VoidActionExecutor());
 	}
 	
 	public IConstructor parse(String nonterminal, URI inputURI, File inputFile) throws IOException{
 		return parseFromFile(new NonTerminalStackNode(AbstractStackNode.START_SYMBOL_ID, 0, nonterminal), inputURI, inputFile, new VoidActionExecutor());
 	}
 }
