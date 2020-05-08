 package tng;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Changeset;
 import models.Edge;
 import models.Method;
 import models.Network;
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
 	private DatabaseConnector 	db;
 	private GitController 		gc;
 	private UnifiedDiffParser 	udp;
 	private CallGraphGenerator 	cgg;
 	
 	private String				HEAD;
 	
 	private Network				currentNetwork;
 	
 	public NetworkBuilder(DatabaseConnector db) {
 		this.db = db;
 		gc = new GitController();
 		udp = new UnifiedDiffParser();
 		cgg = new CallGraphGenerator(db);
 		
 		HEAD = gc.getHead();
 		
 		/*
 		List<String> commits = gc.getAllCommits();
		for(String commit: commits) {
			System.out.println(commit);
 		}*/
 	}
 	
 	public void buildAllNetworksNoUpdate() {
 		System.out.println("Collecting all commit data...");
 		List<String> commits = gc.getAllCommits();
 		for(String commit: commits)
 			buildNetwork(commit, false);
 		
 		// Reset to head
 		gc.reset(HEAD);
 	}
 	
 	/**
 	 * This method reads a list of commits from a given file and
 	 * builds their networks accordingly.
 	 * @param commitFile
 	 */
 	public void buildNetworksFromCommitFile(String commitFile) {
 		try {
 			FileInputStream fstream = new FileInputStream(commitFile);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String line;
			while((line = br.readLine()) != null) {
 				buildNetwork(line, false);
 			}
 		}
 		catch(Exception e) {
 			
 		}
 		
 		// Reset to head
 		gc.reset(HEAD);
 	}
 	
 	public void buildNetwork(String commit, boolean update) {
 		System.out.println("Building network for: " + commit);
 		if(!update)
 			buildNetworkNoUpdate(commit);
 		else
 			buildNetworkUpdate(commit);
 		
 		System.out.println();
 	}
 	
 	private void buildNetworkNoUpdate(String commit) {
 		// Create new network
 		currentNetwork = new Network();
 		
 		// Clean the database
 		db.deleteCallGraph();
 		
 		// Create the new call graph
 		System.out.println("Creating the call graph");
 		cgg.createCallGraphAtCommit(commit);
 		
 		// Get the commit diff
 		System.out.println("Diffing...");
 		List<Changeset> changeset = udp.parse(gc.getCommitDiffJavaOnly(commit, gc.getAllFiles()));
 		
 		// Get author of commit
 		String author = gc.getAuthorOfCommit(commit);
 		
 		// Get list of changed methods
 		List<Pair<Method, Float>> changedMethods = getMethodsOfChangeset(changeset);
 		
 		// Create edges
 		System.out.println("Generating edges");
 		for(Pair<Method, Float> changedMethod: changedMethods) {
 			generateEdges(changedMethod, author);
 		}
 		
 		db.exportNetwork(currentNetwork);
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
 					method.setSecond(Math.min(method.getSecond() + change.getSecond(), 1.0f));
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
 			
 			currentNetwork.addEdge(edge);
 		}
 		
 		return edges;
 		
 	}
 }
