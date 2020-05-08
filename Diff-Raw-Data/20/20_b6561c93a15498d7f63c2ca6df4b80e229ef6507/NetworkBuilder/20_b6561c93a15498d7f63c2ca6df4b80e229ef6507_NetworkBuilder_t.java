 package callgraphanalyzer;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import models.CallGraph;
 import models.Commit;
 
 import db.CallGraphDb;
 
 public class NetworkBuilder
 {
 	private Comparator 			compare;
 	private CallGraphAnalyzer 	cga;
 	private CallGraphDb 		db;
 	
 	private String				startCommit;
 	private String				endCommit;
 
 	public NetworkBuilder(CallGraphDb db, String startCommit, String endCommit)
 	{
 		super();
 		this.db = db;
 		this.startCommit = startCommit;
 		this.endCommit = endCommit;
 		
 		// Initialize both CGs to the start commit for updating
 		compare = new Comparator(db, startCommit, startCommit);
 	}
 	
 	public void buildAllNetworks() {
 		traverseMasterCommitStream(startCommit);
 	}
 	
 	private void traverseMasterCommitStream(String parent) {
 		while(parent != null) {
 			if(parent.equals(endCommit))
 				return;
 			
 			// Get commit family here
 			List<Commit> children = db.getCommitChildren(parent);
 			
 			if(children.size() == 1) {
 				if(parent.equals("f788f2183525e2e0fe2ab7137c90a37e45ab214e")) {
 					int x = 0;
 				}
 				if(!isMergeCommit(children.get(0).getCommit_id())) {
 					compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, children.get(0).getCommit_id());
 					buildNetwork(parent, children.get(0).getCommit_id());
 					compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, children.get(0).getCommit_id());
 				}
 				parent = children.get(0).getCommit_id();
 			}
 			else if(children.size() > 1) {
 				String newParent = null;
 				for(Commit child: children) {
 					if(!isMergeCommit(child.getCommit_id())) {
 						compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, child.getCommit_id());
 						buildNetwork(parent, child.getCommit_id());
 						compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, child.getCommit_id());
 						
 						newParent = recurseCommitSubStream(child.getCommit_id());
 						
 						// This means end commit was in some substream
 						if(newParent == null) 
 							return;
 						
 						//Restore
 						compare.newCallGraph = compare.reverseUpdateCallGraph(compare.newCallGraph, parent);
 						compare.oldCallGraph = compare.reverseUpdateCallGraph(compare.oldCallGraph, parent);
 					}
 					else {
 						newParent = child.getCommit_id();
 					}
 				}
 				parent = newParent;
 				// Move call graphs up. They will probably rebuild here.
 				compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, parent);
 				compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, parent);
 			}
 		}
 	}
 	
 	private String recurseCommitSubStream(String parent) {
 		while(parent != null) {
 			if(parent.equals(endCommit))
 				return null;
 
 			// Get commit family here
 			List<Commit> children = db.getCommitChildren(parent);
 			
 			if(children.size() == 1) {
 				if(!isMergeCommit(children.get(0).getCommit_id())) {
 					compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, children.get(0).getCommit_id());
 					compare.newCallGraph.print();
 					buildNetwork(parent, children.get(0).getCommit_id());
 					compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, children.get(0).getCommit_id());
 					compare.oldCallGraph.print();
 				}
 				parent = children.get(0).getCommit_id();
 				if(isMergeCommit(parent))
 					return parent;
 			}
 			
 			else if(children.size() > 1) {
 				String newParent = null;
 				for(Commit child: children) {
 					if(!isMergeCommit(child.getCommit_id())) {
 						compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, child.getCommit_id());
 						buildNetwork(parent, child.getCommit_id());
 						compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, child.getCommit_id());
 						
 						newParent = recurseCommitSubStream(child.getCommit_id());
 						
 						//Restore
 						compare.newCallGraph = compare.reverseUpdateCallGraph(compare.newCallGraph, parent);
 						compare.oldCallGraph = compare.reverseUpdateCallGraph(compare.oldCallGraph, parent);
 					}
 					else {
 						newParent = child.getCommit_id();
 					}
 				}
 				parent = newParent;
 				// Move call graphs up. They will probably rebuild here.
 				compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, parent);
 				compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, parent);
 				if(isMergeCommit(parent))
 					return parent;
 			}
 		}
 		
 		return null;
 	}
 	
 	private boolean isMergeCommit(String commitID) {
 		List<Commit> parents = db.getCommitParents(commitID);
 		return parents.size() > 1;
 	}
 	
 	private void buildNetwork(String initial, String change) {
 		System.out.println("Generating network for: " + initial + " - " + change);
 		//compare = new Comparator(db, initial, change);
 		cga = new CallGraphAnalyzer();
 		
		// Update the comparator with the commits
		compare.updateCGVariables(initial, change);
		
 		System.out.println("Comparing Commits...");
 		compare.CompareCommits(initial, change);
 		cga.init(compare);
 		
 		System.out.println("Generating the relationships...");
 		cga.generateRelationships();
 		cga.exportRelations();
 		System.out.println();
 	}
 }
