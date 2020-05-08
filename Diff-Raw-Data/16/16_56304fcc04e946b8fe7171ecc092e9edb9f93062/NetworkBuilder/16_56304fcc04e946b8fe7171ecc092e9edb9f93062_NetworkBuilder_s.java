 package tng;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Changeset;
 import models.Edge;
 import models.Method;
 import models.Node;
 import models.Owner;
 import models.Pair;
 import models.Range;
 
 import ast.CallGraphGenerator;
 import git.GitController;
 import db.DatabaseConnector;
 import diff.UnifiedDiffParser;
 
 public class NetworkBuilder
 {
 	DatabaseConnector 	db;
 	GitController 		gc;
 	UnifiedDiffParser 	udp;
 	CallGraphGenerator 	cgg;
 	
 	public NetworkBuilder(DatabaseConnector db) {
 		this.db = db;
 		gc = new GitController();
 		udp = new UnifiedDiffParser();
 		cgg = new CallGraphGenerator(db);
 	}
 	
 	public void buildNetwork(String commit, boolean update) {
 		System.out.println("Building network for: " + commit);
 		if(!update)
 			buildNetworkNoUpdate(commit);
 		else
 			buildNetworkUpdate(commit);
 			
 	}
 	
 	private void buildNetworkNoUpdate(String commit) {
 		// Clean the database
 		db.deleteCallGraph();
 		
 		// Create the new call graph
 		cgg.createCallGraphAtCommit(commit);
 		
 		// Get the commit diff
 		List<Changeset> changeset = udp.parse(gc.getCommitDiff(commit));
 		
 		// Get author of commit
 		String author = gc.getAuthorOfCommit(commit);
 		
 		// Get list of changed methods
 		List<Pair<Method, Float>> changedMethods = getMethodsOfChangeset(changeset);
 		
 		// Create edges
 		for(Pair<Method, Float> changedMethod: changedMethods) {
 			generateEdges(changedMethod, author);
 		}
		
		
 	}
 	
 	private void buildNetworkUpdate(String commit) {
 		
 	}
 	
 	private List<Pair<Method, Float>> getMethodsOfChangeset(List<Changeset> changesets) {
 		List<Pair<Method, Float>> changedMethods = new ArrayList<Pair<Method, Float>>();
 		for(Changeset changeset: changesets) {
 			for(Range range: changeset.getRanges()) {
 				updateChangedMethods(changedMethods, db.getChangedMethods(changeset.getNewFile(), 
 						range.getStart(), range.getEnd()));
 			}
 		}
 		
 		return changedMethods;
 	}
 	
 	/**
 	 * This method updates the list of changed methods so that
 	 * you have no duplicate listings of methods (update their
 	 * weight instead).
 	 * @param methods
 	 * @param method
 	 */
 	private void updateChangedMethods(List<Pair<Method, Float>> changedMethods, 
 			List<Pair<Method, Float>> newChanges) {
 		for(Pair<Method, Float> change: newChanges) {
 			boolean inserted = false;
 			for(Pair<Method, Float> method: changedMethods) {
 				if(method.getFirst().getStart() == change.getFirst().getStart() && 
 						method.getFirst().getEnd() == change.getFirst().getEnd()) {
 					method.setSecond(method.getSecond() + change.getSecond());
 					inserted = true;
 					break;
 				}
 			}
 			if(!inserted)
 				changedMethods.add(change);
 		}
 	}
 	
 	private List<Method> getCallers(Pair<Method, Float> method) {
 			return db.getCallersOfMethod((Method)method.getFirst());
 	}
 	
 	private List<Pair<Method, Owner>> blameCallers(List<Method> methods) {
 		List<Pair<Method, Owner>> owners = new ArrayList<Pair<Method, Owner>>();
 		for(Method method: methods) {
 			List<Owner> ownersOfMethod = gc.getOwnersOfFileRange(method.getFile(), 
 					method.getStart(), method.getEnd());
 			for(Owner owner: ownersOfMethod) {
 				owners.add(new Pair<Method, Owner>(method, owner));
 			}
 					
 		}
 		return owners;
 	}
 	
 	private List<Edge> generateEdges(Pair<Method, Float> changedMethod, String author) {
 		List<Edge> edges = new ArrayList<Edge>();
 		
 		// Get callers
 		List<Method> callers = getCallers(changedMethod);
 		
 		// Blame callers
 		List<Pair<Method, Owner>> owners = blameCallers(callers);
 		
 		// Create edges
 		for(Pair<Method, Owner> owner: owners) {
 			Edge edge = new Edge();
 			edge.setNode1(new Node(owner.getSecond().getEmail()));
 			edge.setNode2(new Node(author));
 			edge.setWeight(owner.getSecond().getOwnership() * changedMethod.getSecond());
 			edges.add(edge);
 			
 			// Print edge
 			System.out.println("Author: " + author + 
 					" Caller: " + edge.getNode1().getEmail() + 
 					" Weight: " + edge.getWeight() +
 					" Changed Method: " + changedMethod.getFirst().toString() +
 					" Calling Method: " + owner.getFirst().toString());
 		}
 		
 		
 		return edges;
 		
 	}
 }
