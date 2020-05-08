 package callgraphanalyzer;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import models.Commit;
 
 import db.CallGraphDb;
 
 public class NetworkBuilder
 {
 	private Comparator 			compare;
 	private CallGraphAnalyzer 	cga;
 	private CallGraphDb 		db;
 	
 	private String				startCommit;
 	private String				endCommit;
 	
 	private List<String>		mergeStack;
 
 	public NetworkBuilder(CallGraphDb db, String startCommit, String endCommit)
 	{
 		super();
 		this.db = db;
 		this.startCommit = startCommit;
 		this.endCommit = endCommit;
 		
 		mergeStack = new LinkedList<String>();
 	}
 	
 	public void buildAllNetworks() {
 		traverseRepositoryCommits(startCommit);
 	}
 	
 	private void traverseRepositoryCommits(String parent) {
 		if(parent.equals(endCommit))
 			return;
 		
 		// Get commit family here
 		List<Commit> parents = db.getCommitParents(parent);
 		List<Commit> children = db.getCommitChildren(parent);
 		if(parents.size() > 1) {
 			// We have found a merge
 			if(mergeStack.isEmpty() || !mergeStack.get(0).equals(parent)) {
				mergeStack.add(parent);
 				return;
 			}
 			else if(!mergeStack.isEmpty() && mergeStack.get(0).equals(parent)) {
 				mergeStack.remove(0);
 			}
 		}
 		
 		if(children.size() >= 1) {
 			for(Commit child: children) {
 				if(!isMergeCommit(child.getCommit_id()))
 					buildNetwork(parent, child.getCommit_id());
 				traverseRepositoryCommits(child.getCommit_id());
 			}
 		}
 	}
 	
 	private boolean isMergeCommit(String commitID) {
 		List<Commit> parents = db.getCommitParents(commitID);
 		return parents.size() > 1;
 	}
 	
 	private void buildNetwork(String initial, String change) {
 		System.out.println("Generating network for: " + initial + " - " + change);
 		compare = new Comparator(db, initial, change);
 		cga = new CallGraphAnalyzer();
 		
 		System.out.println("Comparing Commits...");
 		compare.CompareCommits();
 		cga.init(compare);
 		
 		System.out.println("Generating the relationships...");
 		cga.generateRelationships();
 		cga.exportRelations();
		System.out.println();
 	}
 }
