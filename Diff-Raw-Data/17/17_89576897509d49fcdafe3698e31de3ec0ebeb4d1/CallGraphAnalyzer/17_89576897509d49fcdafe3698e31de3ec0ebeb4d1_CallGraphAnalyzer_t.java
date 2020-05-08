 package callgraphanalyzer;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import models.CallGraph;
 import models.CallGraph.MethodPercentage;
 import models.WeightedChange;
 import models.Change;
 import models.Method;
 import models.Relation;
 import models.User;
 import callgraphanalyzer.Comparator.CompareResult;
 import callgraphanalyzer.CallGraphResources;
 import db.CallGraphDb;
 
 public class CallGraphAnalyzer
 {
 	private CallGraphDb		db;
 	private Comparator		comparator;
 
 	private Set<Relation>	Relations;
 
 	public Comparator getComparator() {
 		return comparator;
 	}
 
 	public void setComparator(Comparator comparator) {
 		this.comparator = comparator;
 	}
 
 	public Set<Relation> getRelations() {
 		return Relations;
 	}
 
 	public void setRelations(Set<Relation> relations) {
 		Relations = relations;
 	}
 
 	public CallGraphAnalyzer()
 	{
 		this.Relations = new HashSet<Relation>();
 	}
 
 	public void init(Comparator comp)
 	{
 		this.comparator = comp;
 		this.db = comp.db;
 	}
 
 	/**
 	 * This function will return all the methods that call the given method.
 	 * 
 	 * @param method
 	 * @param callGraph
 	 * @return
 	 */
 	private List<Method> retrieveMethodCalls(String method, CallGraph callGraph)
 	{
 		Method m = callGraph.containsMethod(method);
 		return m.getCalledBy();
 	}
 
 	/**
 	 * Generates the network.
 	 * @return The Set<Relations> that make up the network. 
 	 */
 	public void generateRelationships()
 	{
 		// For each modifiedFile, for each generate an owner for the method and
 		// the % they own.
 		CompareResult compareResult = comparator.getCompareResult();
 		Set<Method> methodCalls;
 		for (String modifiedFile : compareResult.modifiedFileMethodMap.keySet())
 		{
 			System.out.println("FILE NAME :  " + modifiedFile);
 			// For each method in the new methods.
 			for (MethodPercentage newMethod : compareResult.modifiedFileMethodMap.get(modifiedFile).newMethods)
 			{
 				// get all methods this one is called by
 				Change newMethodChange = db.getLatestOwnerChange(modifiedFile, newMethod.getMethod().getstartChar(), newMethod.getMethod()
 						.getendChar(), comparator.newCommit.getCommit_date());
 				methodCalls = new HashSet<Method>();
				if(newMethodChange != null)
					recurseMethods(new User(newMethodChange.getOwnerId()), newMethod.getMethod(), 
							newMethod.getPercentage(), 0, methodCalls, comparator.newCommit.getCommit_id());
 			}
 			for (MethodPercentage oldMethod : compareResult.modifiedFileMethodMap.get(modifiedFile).oldMethods)
 			{
 				if (compareResult.modifiedFileMethodMap.get(modifiedFile).newMethods.contains(oldMethod))
 					continue;
 				// get all methods this one is called by
 				Change newMethodChange = db.getLatestOwnerChange(modifiedFile, oldMethod.getMethod().getstartChar(), oldMethod
 						.getMethod().getendChar(), comparator.oldCommit.getCommit_date());
 				methodCalls = new HashSet<Method>();
				if(newMethodChange != null)
					recurseMethods(new User(newMethodChange.getOwnerId()), oldMethod.getMethod(), 
							oldMethod.getPercentage(), 0, methodCalls, comparator.oldCommit.getCommit_id());
 			}
 		}
 		for (Relation r : this.Relations)
 			r.print();
 	}
 
 	/**
 	 * Exports our relations for the current comparison to the nodes, edges, and networks tables.
 	 */
 	public void exportRelations() 
 	{
 		// add a record in the networks table
 		int networkId = db.addNetworkRecord(this.comparator.newCommit.getCommit_id(), this.comparator.oldCommit.getCommit_id());
 		for (Relation r : this.Relations)
 		{
 			// Add the two users to our nodes.
 			db.addNode(r.getPersonOne().getUserEmail(), networkId);
 			db.addNode(r.getPersonTwo().getUserEmail(), networkId);
 			// Add the edge.
 			db.addEdge(r.getPersonOne().getUserEmail(), r.getPersonTwo().getUserEmail(), r.getWeight(), networkId);
 		}
 	}
 	
 	/**
 	 * Recursively checks what methods call each one.  Only going into each method once to avoid 
 	 * stack overflow.
 	 * @param changingUser
 	 * @param currentMethod
 	 * @param percentage
 	 * @param currentDepth
 	 * @param methodCalls
 	 */
 	public void recurseMethods(User changingUser, Method currentMethod, float percentage, int currentDepth, Set<Method> methodCalls, String commitID)
 	{
 		if (currentDepth == CallGraphResources.ANALYZER_MAX_DEPTH)
 			return;
 		List<Change> changes = db.getAllOwnersForFileAtCommit(currentMethod.getClazz().getFile().getFileName(), commitID);
 		for (Method calledMethod : currentMethod.getCalledBy())
 		{
 			if (methodCalls.contains(calledMethod))
 				continue;
 			else
 				methodCalls.add(calledMethod);
 			Set<WeightedChange> calledChanges = calledMethod.getClazz().getFile().getMethodWeights(changes, calledMethod);
 			for (WeightedChange calledMethodChange : calledChanges)
 			{
 				boolean isSelf = false;
 				if (changingUser.getUserEmail().equals(calledMethodChange.getOwnerId()))
 					isSelf = true;
 				Relation r = new Relation(new User(changingUser.getUserEmail()), 
 						new User(calledMethodChange.getOwnerId()),
 						isSelf,
 						calledMethod.getClazz().getFile().getFileName(),
 						currentMethod.getName(),
 						calledMethod.getName(),
 						percentage*(calledMethodChange.getWeight()/(currentDepth+1)));
 				this.Relations.add(r);
 			}
 			recurseMethods(changingUser, calledMethod, percentage, currentDepth + 1, methodCalls, commitID);
 		}
 	}
 }
