 package callgraphanalyzer;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import models.CallGraph;
 import parser.Parser;
 import parser.Resolver;
 import db.CommitsTO;
 import db.DbConnection;
 import differ.filediffer;
 import differ.filediffer.diffObjectResult;
 
 public class Comparator {
 	private DbConnection db;
 	private filediffer differ;
 	private CallGraphAnalyzer CallGraphAnalyzer;
 	private CallGraph newCallGraph;
 	private CallGraph oldCallGraph;
 	public Map<String, String> FileMap;
 	public Map<String, String> newCommitFileTree;
 	public Map<String, String> oldCommitFileTree;
 	public CommitsTO newCommit;
 	public CommitsTO oldCommit;
 	public String CurrentBranch;
 	public String CurrentBranchID;
 	
 	/**
 	 * Constructs a new Comparator class.  This class connects the FileDiffer {@link #differ} and the CallGraphAnalyzer {@link #cga}
 	 * @param branchName Name of the branch
 	 * @param db Name of the Database.
 	 * @param CommitIDOne SHA-1 Hash of the commit in question.
 	 * @param CommitIDTwo SHA-1 Hash of the second commit.
 	 */
 	public Comparator(String branchName, DbConnection db, String CommitIDOne, String CommitIDTwo, CallGraphAnalyzer cga) {
 		this.db = db;
 		
 		// Figure out which commit is newer
 		CommitsTO first = db.getCommit(CommitIDOne);
 		CommitsTO second = db.getCommit(CommitIDTwo);
 		if (first.getCommit_date().compareTo(second.getCommit_date()) > 0)
 		{
 			this.newCommit = first;
 			this.oldCommit = second;
 			this.newCommitFileTree = this.getFilesTreeForCommit(CommitIDOne);			
 			this.oldCommitFileTree = this.getFilesTreeForCommit(CommitIDTwo);			
 		}
 		else
 		{
 			this.newCommit = second;
 			this.oldCommit = first;
 			this.newCommitFileTree = this.getFilesTreeForCommit(CommitIDTwo);			
 			this.oldCommitFileTree = this.getFilesTreeForCommit(CommitIDOne);
 		}
 		this.CallGraphAnalyzer = cga;
 		this.newCallGraph = generateCallGraph();
 	}
 	
 	public CallGraph generateCallGraph() 
 	{
 		CallGraph callGraph = new CallGraph();
 		Parser parser = new Parser(callGraph);
 		
 		for (String key : this.newCommitFileTree.keySet())
 		{
 			parser.parseFileFromString(key, db.getRawFile(key, this.newCommitFileTree.get(key)));
 			
 		}
 		callGraph.print();
 		
 		System.out.println();
 		System.out.println();
 		System.out.println("Resolving the fuck out of this CallGraph");
 		
 		Resolver resolver = new Resolver(callGraph);
 		resolver.resolveMethods();
 		
 		callGraph.print();
 		return callGraph;
 	}
 
 	public boolean CompareCommits()
 	{
 		// For every file in the new commit tree
 		for (String newKey : newCommitFileTree.keySet())
 		{
 			// If the file exists in the old commit
 			if (oldCommitFileTree.containsKey(newKey))
 			{
 				// File is still present, might be modified.
 				differ = new filediffer(db.getRawFile(newKey, oldCommitFileTree.get(newKey)),
 										db.getRawFile(newKey, newCommitFileTree.get(newKey)));
 				
 				// return the change sets from the two files
 				differ.diffFilesLineMode();
 				if(differ.isModified())
 				{
 					System.out.println(newKey + " was modified.");
 					List<diffObjectResult> deleteObjects = differ.getDeleteObjects();
 					List<diffObjectResult> insertObjects = differ.getInsertObjects();
 					
 					differ.print();
 				}
 			}
 			else
 			{
 				// The file was added (+) since the old commit.
 				System.out.println("+\t" + newKey);
 			}
 		}
 		for (String oldKey : oldCommitFileTree.keySet())
 		{
 			if (!newCommitFileTree.containsKey(oldKey))
 			{
 				// The file was deleted from the old tree
 				System.out.println("-\t" + oldKey);
 			}
 		}
 		return true;
 	}
 	
 	public boolean getChangedFilesForCommit(String commitID)
 	{
 		FileMap = db.getCommitChangedFiles(commitID);
 		return true;
 	}
 	
 	/**
 	 * Recursively get the files for a commit, going down from the given commit.
 	 * adding them to @see {@link #newCommitFileTree}
 	 * @param commitID
 	 * @return true when successful
 	 */
 	public Map<String, String> getFilesTreeForCommit(String commitID)
 	{
 		Map<String, String> CommitFileTree = new HashMap<String, String>();
 		Map<String, Set<String>> prevChanges = db.getCommitsBeforeChanges(commitID);
 		Set<String> requiredFiles = db.getFileStructureFromCommit(commitID);	// First commit;
 		for (String commit : prevChanges.keySet())
 		{
 			Iterator<String> i = prevChanges.get(commit).iterator();
 			String currentChangedFile;
 			while (i.hasNext())
 			{
 				currentChangedFile = i.next();
 				System.out.println(currentChangedFile);
 				if (requiredFiles.contains(currentChangedFile) &&
 						!CommitFileTree.containsKey(currentChangedFile))
 				{
 					CommitFileTree.put(currentChangedFile, commit);
 				}
 			}
 		}
 		return CommitFileTree;
 	}
 	
 }
