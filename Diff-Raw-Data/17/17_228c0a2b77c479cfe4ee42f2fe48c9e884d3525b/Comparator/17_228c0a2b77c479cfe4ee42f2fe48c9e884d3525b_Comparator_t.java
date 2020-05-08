 package callgraphanalyzer;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import db.CommitsTO;
 import db.DbConnection;
 import differ.filediffer;
 public class Comparator {
 	private DbConnection db;
 	private filediffer differ;
 	private CallGraphAnalyzer cga;
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
 		this.cga = cga;
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
 				// TODO @triet parse that shit!
 				System.out.println(newKey + " was modified.");
				differ = new filediffer(db.getRawFile(newKey, oldCommitFileTree.get(newKey)),
						db.getRawFile(newKey, newCommitFileTree.get(newKey)));
 				differ.setDiffcontent(db.getRawFile("src/test/C.java", "ea276fbd7e46f84e02574823169cc06982542f0f"));
 				differ.getChanges();
 			}
 			else
 			{
 				// The file was added (+) since the old commit.
 				System.out.println(newKey + " was added.");
 			}
 		}
 		for (String oldKey : oldCommitFileTree.keySet())
 		{
 			if (!newCommitFileTree.containsKey(oldKey))
 			{
 				// The file was deleted from the old tree
 				System.out.println(oldKey + " was deleted.");
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
 		List<CommitsTO> commitsBefore = db.getCommitsBefore(commitID);
 		Set<String> requiredFiles = commitsBefore.get(0).getFile_structure();	// First commit;
 		while(true) {
 			if (db.isPaging) commitsBefore = db.getNextCommitsPage();
 			else commitsBefore = db.getCommitsBefore(commitID);
 			for (CommitsTO commit : commitsBefore)
 			{
 				Iterator<String> i = commit.getChanged_files().iterator();
 				String currentChangedFile;
 				while (i.hasNext())
 				{
 					currentChangedFile = i.next();
 					System.out.println(currentChangedFile);
 					if (requiredFiles.contains(currentChangedFile) &&
 							!CommitFileTree.containsKey(currentChangedFile))
 					{
 						CommitFileTree.put(currentChangedFile, commit.getCommit_id());
 					}
 				}
 			}
 			if (!db.isPaging) break;
 		}
 		return CommitFileTree;
 	}
 	
 }
