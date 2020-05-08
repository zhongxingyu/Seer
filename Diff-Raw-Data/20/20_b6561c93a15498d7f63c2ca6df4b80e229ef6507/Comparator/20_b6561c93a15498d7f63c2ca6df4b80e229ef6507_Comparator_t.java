 package callgraphanalyzer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import models.CallGraph;
 import models.Commit;
 import models.CallGraph.MethodPercentage;
 import models.DiffEntry;
 import models.DiffEntry.diff_types;
 import models.Pair;
 import parser.Parser;
 import parser.Resolver;
 import db.CallGraphDb;
 import differ.diff_match_patch;
 import differ.diff_match_patch.Diff;
 import differ.diffObjectResult;
 
 public class Comparator
 {
 	public class ModifiedMethod
 	{
 		public ModifiedMethod(Set<MethodPercentage> oldM, Set<MethodPercentage> newM)
 		{
 			this.oldMethods = oldM;
 			this.newMethods = newM;
 		}
 
 		public Set<MethodPercentage>	oldMethods	= new HashSet<MethodPercentage>();
 		public Set<MethodPercentage>	newMethods	= new HashSet<MethodPercentage>();
 	}
 
 	public class CompareResult
 	{
 		public CompareResult()
 		{
 		}
 
 		public void clear()
 		{
 			addedFiles.clear();
 			deletedFiles.clear();
 			modifiedFileMethodMap.clear();
 			modifiedBinaryFiles.clear();
 		}
 
 		public void print()
 		{
 			for (String file : addedFiles)
 				System.out.println("+\t" + file);
 
 			for (String file : deletedFiles)
 				System.out.println("-\t" + file);
 
 			for (String file : modifiedBinaryFiles.keySet())
 			{
 				String commitID = modifiedBinaryFiles.get(file);
 				System.out.println("+-[BIN]\t" + file + " in " + commitID);
 			}
 
 			for (String file : modifiedFileMethodMap.keySet())
 			{
 				ModifiedMethod methods = modifiedFileMethodMap.get(file);
 				System.out.println("+-\t" + file);
 				for (MethodPercentage mo : methods.oldMethods)
 					System.out
 							.println("\tModified old method: " + mo.getMethod().getName() +" "+ mo.getPercentage()+ "%");
 				for (MethodPercentage mn : methods.newMethods)
 					System.out
 							.println("\tModified new method: " + mn.getMethod().getName()+" "+ mn.getPercentage()+ "%");
 			}
 		}
 
 		public Set<String>					addedFiles				= new HashSet<String>();
 		public Set<String>					deletedFiles			= new HashSet<String>();
 		public Map<String, ModifiedMethod>	modifiedFileMethodMap	= new HashMap<String, ModifiedMethod>();
 		public Map<String, String>			modifiedBinaryFiles		= new HashMap<String, String>();
 	}
 
 	public CallGraphDb				db;
 	public CallGraph				newCallGraph;
 	public CallGraph				oldCallGraph;
 	public Map<String, String>		FileMap;
 	public Map<String, String>		newCommitFileTree;
 	public Map<String, String>		oldCommitFileTree;
 	public Map<String, String>		libraryFileTree;
 
 	private CompareResult			compareResult	= new CompareResult();
 
 	public Commit					newCommit;
 	public Commit					oldCommit;
 	public String					CurrentBranch;
 	public String					CurrentBranchID;
 
 	/**
 	 * Constructs a new Comparator class. This class connects the FileDiffer
 	 * {@link #differ} and CallGraph {@link #newCallGraph}
 	 * 
 	 * @param db
 	 *            Name of the Database.
 	 * @param CommitIDOne
 	 *            SHA-1 Hash of the commit in question.
 	 * @param CommitIDTwo
 	 *            SHA-1 Hash of the second commit.
 	 */
 	public Comparator(CallGraphDb db, String CommitIDOne, String CommitIDTwo)
 	{
 		this.db = db;
 		
 		// Figure out which commit is newer
 		Commit first = db.getCommit(CommitIDOne);
 		Commit second = db.getCommit(CommitIDTwo);
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
 
 		// check and create our owners.
 		this.newCallGraph = generateCallGraph(this.newCommitFileTree);
 		this.newCallGraph.setCommitID(CommitIDTwo);
 		this.oldCallGraph = generateCallGraph(this.oldCommitFileTree);
 		this.oldCallGraph.setCommitID(CommitIDOne);
 	}
	
	public void updateCGVariables(String oldCommit, String newCommit) {
		this.oldCommit = db.getCommit(oldCommit);
		this.newCommit = db.getCommit(newCommit);
	}
 
 	/**
 	 * Generate Callgraph from a commitFileTree
 	 * 
 	 * @param commitFileTree
 	 *            map of file name and path from a commit
 	 * @return CallGraph a resolved CallGraph
 	 */
 	public CallGraph generateCallGraph(Map<String, String> commitFileTree)
 	{	
 		CallGraph callGraph = new CallGraph();
 		Parser parser = new Parser(callGraph);
 
 		for (String key : commitFileTree.keySet())
 		{
 			if (!key.endsWith(".java")) // Currently don't care about non-java
 										// files in our callgraph
 				continue;
 			parser.parseFileFromString(key, db.getRawFileFromDiffTree(key, commitFileTree
 					.get(key)));
 		}
 		
 		// Get Java util
 		CallGraphDb libraryDB = new CallGraphDb();
 		libraryDB.connect("JavaLibraries");
 		libraryDB.setBranchName("master");
 		this.libraryFileTree = this.getFilesTreeForCommit("e436a78a73f967d47aebd02ac58677255bbec125");
 		
 		for (String key : libraryFileTree.keySet())
 		{
 			if (!key.endsWith(".java")) // Currently don't care about non-java
 				continue;
 			parser.parseFileFromString(key, libraryDB.getRawFileFromDiffTree(key, libraryFileTree
 					.get(key)));
 		}
 
 		//System.out.println("Resolving the CallGraph");
 
 		Resolver resolver = new Resolver(callGraph);
 		resolver.resolveAll();
 
 		//callGraph.print();
 		return callGraph;
 	}
 
 	public boolean CompareCommits(String oldCommitID, String newCommitID)
 	{
 		this.compareResult.clear();
 
 		// these two commit are consecutive, just got the diff and generated method
 		List<String> fileChanged = db.getFilesChanged(oldCommitID, newCommitID);
 		
 		for(String fileName : fileChanged)
 		{
 			List<DiffEntry> diffEntries = db.getDiffsFromTwoConsecutiveCommits(fileName, oldCommitID, newCommitID);
 			if(!diffEntries.isEmpty())
 			{
 				// return the change sets from the two files
 				List<diffObjectResult> deleteObjects = new ArrayList<diffObjectResult>();
 				List<diffObjectResult> insertObjects = new ArrayList<diffObjectResult>();
 				
 				for(DiffEntry entry : diffEntries)
 				{
 					if(entry.getDiff_type() == diff_types.DIFF_MODIFYDELETE)
 					{
 						diffObjectResult result = new diffObjectResult();
 						result.start 			= entry.getChar_start();
 						result.end 				= entry.getChar_end();
 						result.diffObject  		= new Diff(diff_match_patch.Operation.DELETE, entry.getDiff_text());
 						deleteObjects.add(result);
 					}
 					else if(entry.getDiff_type() == diff_types.DIFF_MODIFYINSERT)
 					{
 						diffObjectResult result = new diffObjectResult();
 						result.start 			= entry.getChar_start();
 						result.end 				= entry.getChar_end();
 						result.diffObject  		= new Diff(diff_match_patch.Operation.INSERT, entry.getDiff_text());
 						insertObjects.add(result);
 					}
 					else if(entry.getDiff_type() == diff_types.DIFF_ADD)
 					{
 						this.compareResult.addedFiles.add(fileName);
 					}
 					else if(entry.getDiff_type() == diff_types.DIFF_DELETE)
 					{
 						this.compareResult.deletedFiles.add(fileName);
 					}
 					else if(entry.getDiff_type() == diff_types.DIFF_UNKNOWN)
 					{
 						System.out.println("ERROR! Unknown operation on File: " + fileName);
 					}
 				}
 				
 				// figure out which function has changed
 				getModifiedMethodsForFile(fileName, deleteObjects, insertObjects);
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * get all methods in a file that might be changed from new commit to other
 	 * 
 	 * @param fileName
 	 *            file path
 	 * @param callgraph
 	 * @param diffs
 	 *            list of diff objects
 	 * @return
 	 */
 	public void getModifiedMethodsForFile(String fileName,
 			List<diffObjectResult> deleteDiffs,
 			List<diffObjectResult> insertDiffs)
 	{
 		Set<MethodPercentage> newMethods = new HashSet<MethodPercentage>();
 		Set<MethodPercentage> oldMethods = new HashSet<MethodPercentage>();
 
 		// methods from old file version+
 		for (diffObjectResult diff : deleteDiffs)
 		{
 			List<MethodPercentage> changedMethod = this.oldCallGraph
 					.getPercentageOfMethodUsingCharacters(fileName, diff.start, diff.end, this.oldCommit.getCommit_id());
 			for (MethodPercentage m : changedMethod)
 			{
 				// find if the method exists
 				boolean methodExist = false;
 				for(MethodPercentage oldm : oldMethods)
 				{
 					if(oldm.getMethod().equals(m))
 					{
 						methodExist = true;
 						oldm.addPercentage(m.getPercentage());
 						break;
 					}
 				}
 				
 				// add to oldMethodList
 				if(!methodExist)
 					oldMethods.add(m);
 			}
 		}
 
 		// methods from new file version
 		for (diffObjectResult diff : insertDiffs)
 		{
 			List<MethodPercentage> changedMethod = this.newCallGraph
 					.getPercentageOfMethodUsingCharacters(fileName, diff.start, diff.end, this.newCommit.getCommit_id());
 			for (MethodPercentage m : changedMethod)
 			{
 				// find if the method exists
 				boolean methodExist = false;
 				for(MethodPercentage newm : newMethods)
 				{
 					if(newm.getMethod().equals(m))
 					{
 						methodExist = true;
 						newm.addPercentage(m.getPercentage());
 						break;
 					}
 				}
 				
 				// add to oldMethodList
 				if(!methodExist)
 					newMethods.add(m);
 			}
 		}
 
 		// Insert to modifiedMethod map
 		if (!this.compareResult.modifiedFileMethodMap.containsKey(fileName))
 		{
 			ModifiedMethod mm = new ModifiedMethod(oldMethods, newMethods);
 			this.compareResult.modifiedFileMethodMap.put(fileName, mm);
 		}
 	}
 
 	/**
 	 * Recursively get the files for a commit, going down from the given commit.
 	 * adding them to @see {@link #newCommitFileTree}
 	 * 
 	 * @param commitID
 	 * @return true when successful
 	 */
 	public Map<String, String> getFilesTreeForCommit(String commitID)
 	{
 		Map<String, String> CommitFileTree = new HashMap<String, String>();
 		Map<String, Set<String>> prevChanges = db.getCommitsBeforeChanges(
 				commitID, false, false);
 		Set<String> requiredFiles = db.getFileStructureFromCommit(commitID); // First
 																				// commit;
 		Iterator<String> i = db.getCommitChangedFiles(commitID).iterator();
 		addFilesFromCommit(i, requiredFiles, CommitFileTree, commitID);
 
 		for (String commit : prevChanges.keySet())
 		{
 			Iterator<String> iter = prevChanges.get(commit).iterator();
 			addFilesFromCommit(iter, requiredFiles, CommitFileTree, commit);
 		}
 		return CommitFileTree;
 	}
 
 	public void addFilesFromCommit(Iterator<String> i,
 			Set<String> requiredFiles, Map<String, String> CommitFileTree,
 			String commit)
 	{
 		String currentChangedFile;
 		while (i.hasNext())
 		{
 			currentChangedFile = i.next();
 			//System.out.println("Commit :" + commit + " changed file "
 					//+ currentChangedFile);
 			if (requiredFiles.contains(currentChangedFile)
 					&& !CommitFileTree.containsKey(currentChangedFile))
 			{
 				CommitFileTree.put(currentChangedFile, commit);
 			}
 		}
 	}
 
 	public void print()
 	{
 		//this.compareResult.print();
 	}
 
 	public CompareResult getCompareResult()
 	{
 		return compareResult;
 	}
 	
 	public CallGraph forwardUpdateCallGraph(CallGraph cg, String newCommit) {
 		if(cg.getCommitID().equals(newCommit))
 			return cg;
 			
 		if(hasChild(cg.getCommitID(), newCommit)) {
 			List<String> files = db.getFilesChanged(cg.getCommitID(), newCommit);
 			List<Pair<String,String>> changedFiles = new ArrayList<Pair<String,String>>();
 			for(String file: files) {
 				if(file.endsWith(".java")) {
 					String rawFile = db.getRawFileFromDiffTree(file, newCommit);
 					//cg.updateCallGraphByFile(file, rawFile);
 					changedFiles.add(new Pair<String,String>(file, rawFile));
 				}
 			}
 			cg.updateCallGraphByFiles(changedFiles);
 		}
 		else {
 			cg = buildCallGraph(cg, newCommit);
 		}
 		cg.setCommitID(newCommit);
 		return cg;
 	}
 	
 	public CallGraph reverseUpdateCallGraph(CallGraph cg, String newCommit) {
 		if(cg.getCommitID().equals(newCommit))
 			return cg;
 		
 		if(hasChild(newCommit, cg.getCommitID())) {
 			List<String> files = db.getFilesChanged(newCommit, cg.getCommitID());
 			List<Pair<String,String>> changedFiles = new ArrayList<Pair<String,String>>();
 			for(String file: files) {
 				String rawFile = db.getRawFileFromDiffTree(file, newCommit);
 				//cg.updateCallGraphByFile(file, rawFile);
 				changedFiles.add(new Pair<String,String>(file, rawFile));
 			}
 			cg.updateCallGraphByFiles(changedFiles);
 		}
 		else {
 			cg = buildCallGraph(cg, newCommit);
 		}
 		cg.setCommitID(newCommit);
 		return cg;
 	}
 	
 	private boolean hasChild(String parentID, String childID) {
 		return db.parentHasChild(parentID, childID);
 	}
 	
 	private CallGraph buildCallGraph(CallGraph cg, String CommitID) {
 		Map<String, String>	 commitFileTree = this.getFilesTreeForCommit(CommitID);
 		cg = generateCallGraph(commitFileTree);
 		cg.setCommitID(CommitID);
 		return cg;
 	}
 
 }
