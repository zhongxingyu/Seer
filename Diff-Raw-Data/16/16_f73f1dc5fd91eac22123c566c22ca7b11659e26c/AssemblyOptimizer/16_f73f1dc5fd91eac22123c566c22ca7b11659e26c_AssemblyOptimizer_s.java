 package org.supremica.external.assemblyOptimizer;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Collections;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.Comparator;
 import java.lang.reflect.Method;
 import java.util.TreeMap;
 
 import static java.io.File.*;
 import static java.util.Arrays.*;
 
 import joptsimple.HelpFormatter;
 import joptsimple.OptionDescriptor;
 import joptsimple.OptionParser;
 import joptsimple.OptionSpec;
 import joptsimple.OptionSet;
 
 import org.supremica.external.assemblyOptimizer.GenericOperation;
 import org.supremica.external.assemblyOptimizer.GenericResource;
 import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Operation;
 import org.supremica.external.assemblyOptimizer.AssemblyStructureProtos.Resource;
 
 import org.supremica.external.util.Stack;
 
 /**
  * @author Zhennan, Knut
  * 
 **/
 
 
 public class AssemblyOptimizer
 {
 	// How many feasible solutions users want to get and calculate the minimal time. 
 	private int nbrFeasibleSolutions;
 	
 	// The minimal cost obtained from feasible solutions.
 	private int minCost;
 	
 	// Whether output the names of the feasible solution which has the minimal cost.
 	private boolean outputOperationSequence;
 	
 	List<Operation> operationSet;
 	private List<Resource> resourceSet;
 	
 	List<GenericOperation> genericOperationList;
 	private List<GenericResource> genericResourceList;
 	
 	private Map<String, GenericResource> nameToResourceMap;
 	private Map<String, GenericOperation> nameToOperationMap;
 	private Map<GenericOperation, Operation> genericOperationToOperation;
 		
 	private List<ConcurrentProcess> resultProcessList;
 	
 	private List<Operation> optimizedOperationList;
 	
 	public List<Operation> getOptimizedOperationList()
 	{
 		optimizedOperationList = new ArrayList<Operation>(operationSet.size()); 
 		for(ConcurrentProcess process: resultProcessList)
 		{
 			for(Map.Entry<Integer, GenericOperation> entry: process.getStartingTimeToOperation().entrySet())
 			{
 				int startingTime = entry.getKey();
 				Operation operation = genericOperationToOperation.get(entry.getValue());
 				optimizedOperationList.add(buildOperationWithStartingTime(operation, startingTime));
 			}
 		}
 		Collections.sort(optimizedOperationList, operationComparator);
 		return optimizedOperationList;
 	}
 	
 	private Operation buildOperationWithStartingTime(Operation operation, int startingTime)
 	{
 		Operation.Builder operationWithStartingTime = Operation.newBuilder();
 		
 		operationWithStartingTime.setName(operation.getName());
 		operationWithStartingTime.setGuard(operation.getGuard());
 		operationWithStartingTime.addAllEnterAction(operation.getEnterActionList());
 		operationWithStartingTime.addAllExitAction(operation.getExitActionList());
 		operationWithStartingTime.addAllUseResource(operation.getUseResourceList());
 		operationWithStartingTime.setCostTime(operation.getCostTime());
 		operationWithStartingTime.addAllStartAfterOperations(operation.getStartAfterOperationsList());
 	  	operationWithStartingTime.setTerminal(operation.getTerminal());
 		operationWithStartingTime.addAllVariable(operation.getVariableList());
 		operationWithStartingTime.setStartTime(startingTime);
 		
 		return operationWithStartingTime.build();
 	}
 	
 	private List<ConcurrentProcess> tempProcessList;
 	
 	private GenericResourceBuilder resourceBuilder;
 	private GenericOperationBuilder operationBuilder;
 	
 	
 	private int terminalCount;
 	
 	private static final Random random = new Random();
 	
 	private static final Comparator<ConcurrentProcess> processComparator 
 		= new Comparator<ConcurrentProcess>() {
 			public int compare(ConcurrentProcess p1, ConcurrentProcess p2)
 			{
 				if(p1.getPossibleStartingTimeForNextOperation() <= p2.getPossibleStartingTimeForNextOperation())
 					return -1;
 				return 1;
 			}
 		};
 	
 	private static final Comparator<Operation> operationComparator
 		= new Comparator<Operation>(){
 			public int compare(Operation op1, Operation op2)
 			{
 				if(op1.getStartTime() <= op2.getStartTime())
 					return -1;
 				return 1;
 			}
 		};
 	
 	public AssemblyOptimizer(List<Operation> operationSet, List<Resource> resourceSet)
 	{
 		this.operationSet = operationSet;
 		this.resourceSet = resourceSet;
 		
 		minCost = Integer.MAX_VALUE;
 		
 		genericResourceList = new ArrayList<GenericResource>(resourceSet.size());
 		genericOperationList = new ArrayList<GenericOperation>(operationSet.size());
 		
 		nameToResourceMap = new HashMap<String, GenericResource>(resourceSet.size());
 		nameToOperationMap = new HashMap<String, GenericOperation>(operationSet.size());
 		genericOperationToOperation = new HashMap<GenericOperation, Operation>(operationSet.size());
 		
 		resourceBuilder = new GenericResourceBuilder();
 		operationBuilder = new GenericOperationBuilder();
 	} 
 	
 	private void constructGenericCounterpart() throws Exception
 	{
 		// from the operations and resources, build corresponding generic operations and resources
 		for(Resource resource: resourceSet) {
 			GenericResource genericResource = resourceBuilder.newGenericResource(resource);
 			genericResourceList.add(genericResource);
 			// capacity could be set here.
 			nameToResourceMap.put(resource.getName(), genericResource);
 		}
 		
 		for(Operation operation: operationSet) {
 			GenericOperation genericOperation = operationBuilder.newGenericOperation(operation);
 			genericOperation.setName(operation.getName());
 			genericOperation.setCostTime(operation.getCostTime());
 			genericOperation.setTerminal(operation.getTerminal());
 			genericOperationToOperation.put(genericOperation, operation);
 			// set resources 
 			Class operationClass = genericOperation.getClass();
 			Method setMethod = null;
 			for(String useResourceName: operation.getUseResourceList()){
 				setMethod = operationClass.getMethod("setGenericResource_"+useResourceName, 
 					Class.forName("org.supremica.external.assemblyOptimizer.GenericResource"));
 				setMethod.invoke(genericOperation, nameToResourceMap.get(useResourceName));
 			}
 			
 			genericOperationList.add(genericOperation);
 			nameToOperationMap.put(operation.getName(), genericOperation);
 		}
 		
 		
 		for(Operation operation: operationSet){
 			
 			GenericOperation genericOperation = nameToOperationMap.get(operation.getName());
 			Class operationClass = genericOperation.getClass();
 			
 			Method setOperationMethod = null;
 			for(String relatedOp: operation.getStartAfterOperationsList()){
 				setOperationMethod = operationClass.getMethod("setGenericOperation_" + relatedOp,
 				 	Class.forName("org.supremica.external.assemblyOptimizer.GenericOperation"));
 				setOperationMethod.invoke(genericOperation, nameToOperationMap.get(relatedOp));
 			}
 		} 
 		
 	}
 	
 	public int getMinCost(){
 		return minCost;
 	}
 	
 	
 	private void showOperationNames()
 	{		
 		if(outputOperationSequence)
 		{
 			StringBuilder sb = new StringBuilder();
 			int processIndex = 1;
 			for(ConcurrentProcess process: resultProcessList)
 			{
 				sb.append("Process " + processIndex + ": \n");
 				for(Map.Entry<Integer, GenericOperation> entry: process.getStartingTimeToOperation().entrySet())
 				{
 					int startingTime = entry.getKey();
 					String operationName = entry.getValue().getName();
 					int endingTime = startingTime + entry.getValue().getCostTime();
 					
 					sb.append("Operation " + operationName + 
 						": [" + startingTime + ", " + endingTime +"]\n");
 				}
 				sb.append("\n");
 				processIndex ++ ;
 			}
 			
 			System.out.println("A \"good\" feasible solution is:");
 			System.out.println(sb.toString());
 		}
 	}
 	
 	public void setNbrFeasibleSolutions(int number)
 	{
 		this.nbrFeasibleSolutions = number;
 	}
 	
 	public void setOutputOperationSequence(boolean output)
 	{
 		this.outputOperationSequence = output;
 	}
 	
 	// use jopt-simple to parse the arguments
 	public void parse(String[] args) throws Exception
 	{
 		OptionParser parser = new OptionParser();
 		
 		OptionSpec<Integer> count = parser.accepts("c").withOptionalArg().ofType(Integer.class)
 		.describedAs("number of feasible solutions").defaultsTo(10000);
 		
 		OptionSpec<Boolean> outputOperationFlag = parser.accepts("o").withOptionalArg().ofType(Boolean.class)
 		.describedAs("besides the minimal cost, also print the corresponding sequences of operations and starting time")
 		.defaultsTo(false);
 		
 		OptionSpec<Void> help = parser.acceptsAll(asList("h", "?"), "show the help message");
 		
 		OptionSet options = parser.parse(args);
 		
 		if(options.has(help))
 		{
 			parser.printHelpOn(System.out);
 			System.exit(0);
 		}
 		else
 		{
 			setNbrFeasibleSolutions(options.valueOf(count));
 			setOutputOperationSequence(options.valueOf(outputOperationFlag));
 		}
 	}
 	
 	private List<GenericOperation> computeAFeasibleSolution()
 	{
 		// tempOplist: a copy of operations in genericOperationList
 		List<GenericOperation> tempOpList = new ArrayList<GenericOperation>(genericOperationList);
 		
 		// cache: during the algorithm, all operations in cache are not qualified, e.g. guards are false;
 		List<GenericOperation> cache = new ArrayList<GenericOperation>(genericOperationList.size());
 		
 		// stack; store temporary qualified operations
 		Stack<GenericOperation> stack = new Stack<GenericOperation>();
 		
 		
 		ConcreteVariables.getInstance().resetVariables();
 		// reset isFinished flag for each operation
 		// Meanwhile, count the number of terminal operation
 		terminalCount = 0;
 		for(GenericOperation operation: genericOperationList){		
 			operation.resetFinished();
 			if(operation.isTerminal())
 			{
 				terminalCount++;
 			}
 				
 		}
 		
 		for(GenericResource resource: genericResourceList){
 			int capacity = resource.getCapacity();
 			resource.setCapacity(capacity);
 		}
 		
 		stack = pathAlgo(stack, tempOpList, cache);
 		
 		List<GenericOperation> aFeasibleSolution = new ArrayList<GenericOperation>(stack.size());
 		while(!stack.isEmpty())
 			aFeasibleSolution.add(stack.pop());
 		
 		Collections.reverse(aFeasibleSolution);		
 		
 		return aFeasibleSolution;
 	}
 	
 	private Stack<GenericOperation> pathAlgo(Stack<GenericOperation> stack, List<GenericOperation> tempOplist, List<GenericOperation> cache)
 	{
 		// 1. Base case
		if(terminalCount == 0)
 			return stack;
 		else if(stack.size() >= 1 && stack.peek().evaluateTerminalCondition())
 			return stack;
 		else{
 			// At this moment, no operation left in tempOplist is qualified. We need to pop the top operation 
 			// in the stack and add it into tempOplist. As a result, all operations in tempOplist have the 
 			// chance to be qualified. So we clear the cache and call the method itself. 
 			// Since it is true that operations in cache are always in tempOplist but not vice versa,
 			// we can determin where these two lists contain the same operations 
 			// by using cache.containsAll(tempOplist).
 			if(cache.containsAll(tempOplist))
 			{
 				if(stack.peek().isTerminal())
 					terminalCount++;
 				tempOplist.add(stack.pop());
 				cache.clear();
 				return pathAlgo(stack, tempOplist, cache);
 			}
 			else // all the operations in tempOplist but not in cache might be qualified.
 			{
 				GenericOperation op = pickARandomOperation(tempOplist, cache);
 				// Yes, op is temporarily qualified. Remove it from tempOplist and push it to stack.
 				// Now, all operations in tempOplist might be qualified next. So clear cache and continue.
 				if(op.evaluateGuard())
 				{
 					op.performEnterActions();
 					op.performExitActions();
 					tempOplist.remove(op);
 					if(op.isTerminal())
 						terminalCount--;
 					stack.push(op);
 					cache.clear();
 					return pathAlgo(stack, tempOplist, cache);
 				}
 				// No, :( op is not qualified. Put it into cache and continue 
 				else
 				{
 					cache.add(op);
 					return pathAlgo(stack, tempOplist, cache);
 				}
 			}
 		}
 	}
 	
 	private GenericOperation pickARandomOperation(List<GenericOperation> tempOplist, List<GenericOperation> cache)
 	{
 		List<GenericOperation> temp = new ArrayList<GenericOperation>(tempOplist);
 		temp.removeAll(cache);
 		
 		return temp.get(random.nextInt(temp.size()));
 	}
 	
 	private int getMinimalCostToTheSolution(List<GenericOperation> aSolution)
 	{	
 		
 		// Now, every operation in the aSolution list: the field isFinished is set to true before.
 		// So, we need to reset it before find the minimal cost time. 		
 		// For the resources, all of the units have been deallocated for a well-defined operation set.
 		// Later, possibly, we also need to reset other variables before continuing.
 		
 		for(GenericOperation operation: aSolution){
 			operation.resetFinished();		
 			operation.resetRemainingTime();	
 		}
                 
         ConcreteVariables.getInstance().resetVariables();
 		
 		// TEST: FT 6*6 optimal sequence
 		/*aSolution.clear();
 		aSolution.add(genericOperationList.get(6)); // op21
 		aSolution.add(genericOperationList.get(12)); // op31
 		aSolution.add(genericOperationList.get(0)); // op11
 		aSolution.add(genericOperationList.get(13)); // op32
 		aSolution.add(genericOperationList.get(1)); // op12
 		aSolution.add(genericOperationList.get(18)); // op41
 		aSolution.add(genericOperationList.get(7)); // op22
 		aSolution.add(genericOperationList.get(14)); // op33
 		aSolution.add(genericOperationList.get(19)); // op42
 		aSolution.add(genericOperationList.get(30)); // op61
 		aSolution.add(genericOperationList.get(24)); // op51
 		aSolution.add(genericOperationList.get(8)); // op23
 		aSolution.add(genericOperationList.get(2)); // op13
 		aSolution.add(genericOperationList.get(31)); // op62
 		aSolution.add(genericOperationList.get(15)); // op34
 		aSolution.add(genericOperationList.get(32)); // op63
 		aSolution.add(genericOperationList.get(25)); // op52
 		aSolution.add(genericOperationList.get(20)); // op43
 		aSolution.add(genericOperationList.get(26)); // op53
 		aSolution.add(genericOperationList.get(16)); // op35
 		aSolution.add(genericOperationList.get(21)); // op44
 		aSolution.add(genericOperationList.get(33)); // op64
 		aSolution.add(genericOperationList.get(9)); // op24
 		aSolution.add(genericOperationList.get(3)); // op14
 		aSolution.add(genericOperationList.get(17)); // op36
 		aSolution.add(genericOperationList.get(22)); // op45
 		aSolution.add(genericOperationList.get(10)); // op25
 		aSolution.add(genericOperationList.get(27)); // op54
 		aSolution.add(genericOperationList.get(4)); // op15
 		aSolution.add(genericOperationList.get(34)); // op65
 		aSolution.add(genericOperationList.get(23)); // op46
 		aSolution.add(genericOperationList.get(28)); // op55
 		aSolution.add(genericOperationList.get(11)); // op26
 		aSolution.add(genericOperationList.get(35)); // op66
 		aSolution.add(genericOperationList.get(5)); // op16
 		aSolution.add(genericOperationList.get(29)); // op56 */
 		
 		int minimalCostTime = 0;
 		List<GenericOperation> copyOfTheSolution = new ArrayList<GenericOperation>(aSolution);
 		List<GenericOperation> temp = new ArrayList<GenericOperation>();
 		
 		LinkedList<ConcurrentProcess> processLinkedList = new LinkedList<ConcurrentProcess>();
 		
 		// Construct the inital processes of which each contains one operation
 		for(int i = 0; i < copyOfTheSolution.size(); i++)
 		{	
 			GenericOperation operation = copyOfTheSolution.get(i);
 			if(operation.evaluateGuard())
 			{
 				operation.performEnterActions();
 				
 				temp.add(operation);
 				
 				ConcurrentProcess t = new ConcurrentProcess();
 				t.addAnOperationToProcess(operation);
 				t.setPossibleStartingTimeForNextOperation(operation.getCostTime());
 				
 				t.addStartingTimeToOperation(0, operation);
 				processLinkedList.add(t);
 			}
 			else
 				break;
 		}
 			
 		// Remove the operations which are used to build processes
 		copyOfTheSolution.removeAll(temp);
 		
 		// sort this process list in a asending order
 		Collections.sort(processLinkedList, processComparator);
 		
 		// Insert the rest operations one by one to processes
 		 
 		while(!copyOfTheSolution.isEmpty())
 		{	
 			ConcurrentProcess process = processLinkedList.getFirst(); // always use the least process
 			ConcurrentProcess higherProcess = null;
 			ConcurrentProcess lowerProcess = null;
 			ListIterator<ConcurrentProcess> forwardIterator = null;
 			Iterator<ConcurrentProcess> reversedIterator = null;
 
 			// Get and remove the first operation from copyOfTheSolution
 			GenericOperation operation = copyOfTheSolution.remove(0);
 		
 			if(operation.evaluateGuard())
 			{
 				// If the last operation is not finished, a new process will be created and added into the processLinkedList
 				if(!process.getLastOperation().isFinished())
 				{
 					ConcurrentProcess newProcess = new ConcurrentProcess();
 					GenericOperation lastOperationOfProcess = process.getLastOperation();
 					int remainingTime = lastOperationOfProcess.getRemainingTime();
 					int possibleStartingTimeOfProcess = process.getPossibleStartingTimeForNextOperation();
 
 					newProcess.addAnOperationToProcess(operation);
 					operation.performEnterActions();
 
 					int startingTimeForNewProcess = possibleStartingTimeOfProcess - remainingTime;
 					newProcess.addStartingTimeToOperation(startingTimeForNewProcess, operation);
 
 					newProcess.setPossibleStartingTimeForNextOperation(startingTimeForNewProcess + operation.getCostTime());
 					processLinkedList.add(newProcess);
 				}
 				// Just append it after the last operation
 				else
 				{
 					process.addAnOperationToProcess(operation);
 					operation.performEnterActions();
 					
 					int oldPossibleStartingTimeForNextOperation = process.getPossibleStartingTimeForNextOperation();
 					process.addStartingTimeToOperation(oldPossibleStartingTimeForNextOperation, operation);
 					process.setPossibleStartingTimeForNextOperation(oldPossibleStartingTimeForNextOperation + operation.getCostTime());
 				}
 				
 				Collections.sort(processLinkedList, processComparator);
 			}
 			else
 			{			
 				do{
 					// find the lowerest process of which the last operation is unfinished.
 					forwardIterator = processLinkedList.listIterator(processLinkedList.indexOf(process)); // start to check from the lowest process
 					while(forwardIterator.hasNext())
 					{
 						process = forwardIterator.next();
 						if(!process.getLastOperation().isFinished())
 							break;
 					}
 
 					process.getLastOperation().performExitActions(); 
 					int remainingTimeOfLastOperationOfProcess = process.getLastOperation().getRemainingTime();
 					// finish the last operation by seting the remaining time 0
 					process.getLastOperation().setRemainingTime(0);
 
 					// update the remaining time of higher processes
 					reversedIterator = processLinkedList.descendingIterator(); // start from the highest 
 					while(reversedIterator.hasNext())
 					{
 						higherProcess = reversedIterator.next();
 
 						if(higherProcess == process)
 						{
 							break;
 						}
 
 						GenericOperation lastOperationOfHigherProcess = higherProcess.getLastOperation();
 						if(lastOperationOfHigherProcess.getRemainingTime() - remainingTimeOfLastOperationOfProcess == 0)
 							lastOperationOfHigherProcess.performExitActions();
 						lastOperationOfHigherProcess.setRemainingTime(
 							lastOperationOfHigherProcess.getRemainingTime() - remainingTimeOfLastOperationOfProcess);
 					}
 
 					// update the possibleStartingTimeForNextOperation of lower processes
 					forwardIterator = processLinkedList.listIterator(0);
 					while(forwardIterator.hasNext())
 					{
 						lowerProcess = forwardIterator.next();
 
 						if(lowerProcess == process)
 						{
 							break;
 						}
 
 						// Since at this moment, all of last operations of lower processes are finished,
 						// we only need to set possibleStartingTimeForNextOperation to 
 						// the possibleStartingTimeForNextOperation of this process
 						lowerProcess.setPossibleStartingTimeForNextOperation(process.getPossibleStartingTimeForNextOperation());
 					}
 				}while(!operation.evaluateGuard());				
 
 				// Insert and update the possibleStartingTimeForNextOperation of this process.
 
 				process.addAnOperationToProcess(operation);
 				operation.performEnterActions();
 				
 				int oldPossibleStartingTimeForNextOperation = process.getPossibleStartingTimeForNextOperation();
 				process.addStartingTimeToOperation(oldPossibleStartingTimeForNextOperation, operation);
 				process.setPossibleStartingTimeForNextOperation(oldPossibleStartingTimeForNextOperation + operation.getCostTime());
 
 				Collections.sort(processLinkedList, processComparator);
 			}
 		}
 		
 		// Now all of left operations have been inserted. 
 		// For the last stage, we let each last operation of each process perform the exitActions and 
 		// set the remaining time to 0
 		Iterator<ConcurrentProcess> reversedIterator = processLinkedList.descendingIterator();
 		ConcurrentProcess process = null;
 		GenericOperation last = null;
 		while(reversedIterator.hasNext())
 		{
 			process = reversedIterator.next();
 			if(process.getLastOperation().isFinished())
 				break;
 			last = process.getLastOperation();
 			last.performExitActions();
 			last.setRemainingTime(0);
 		}	
 	
 		tempProcessList = processLinkedList;
 		// In order to get the minimal cost time, we only need to get the possibleStartingTimeForNextOperation of 
 		// the highest process.
 		return processLinkedList.getLast().getPossibleStartingTimeForNextOperation();
 	}
 	
 	public void computeMinimalCost () throws Exception
 	{
 		constructGenericCounterpart();
 		
 		List<GenericOperation> bestSolution = null;
 		List<GenericOperation> tempSolution = null;
 		
 		for(int i = nbrFeasibleSolutions; i > 0; i--)
 		{
 			tempSolution = computeAFeasibleSolution();
 			int tempCost = getMinimalCostToTheSolution(tempSolution);
 			
 			if(tempCost < minCost)
 			{
 				minCost = tempCost;
 				bestSolution = tempSolution;
 				resultProcessList = tempProcessList;
 			}
 		}
 		
 		showOperationNames();
 	}
 	
 	/*************************************************************************************************************
 	 *
 	 * INTERNAL CLASS
 	 * 
 	 * 
 	************************************************************************************************************/
 	
 	class ConcurrentProcess {
 		
 		private List<GenericOperation> sequentialOperations;
 		private int possibleStartingTimeForNextOperation;
 		
 		private List<String> operationNames;	
 		
 		private GenericOperation lastOperation;
 		
 		private TreeMap<Integer, GenericOperation> startingTimeToOperation;
 		
 		public ConcurrentProcess()
 		{
 			sequentialOperations = new ArrayList<GenericOperation>();
 			operationNames = new ArrayList<String>();
 			startingTimeToOperation = new TreeMap<Integer, GenericOperation>();
 		}
 		
 		public List<GenericOperation> getSequentialOperations()
 		{
 			return sequentialOperations;
 		}
 		
 		public boolean containOperationName(String operationName)
 		{
 			return operationNames.contains(operationName);
 		}
 		
 		public int getPossibleStartingTimeForNextOperation()
 		{
 			return possibleStartingTimeForNextOperation;
 		}
 		
 		public GenericOperation getLastOperation(){
 			return lastOperation;
 		}
 		
 		public void addAnOperationToProcess(GenericOperation nextOperation)
 		{
 			operationNames.add(nextOperation.getName());
 			sequentialOperations.add(nextOperation);
 			
 			lastOperation = nextOperation;
 		}
 		
 		public void setPossibleStartingTimeForNextOperation (int time)
 		{
 			possibleStartingTimeForNextOperation = time;
 		}
 		
 		public void addStartingTimeToOperation(Integer startingTime, GenericOperation operation)
 		{
 			startingTimeToOperation.put(startingTime, operation);
 		}
 		
 		public TreeMap<Integer, GenericOperation> getStartingTimeToOperation()
 		{
 			return startingTimeToOperation;
 		}
 	}
 }
