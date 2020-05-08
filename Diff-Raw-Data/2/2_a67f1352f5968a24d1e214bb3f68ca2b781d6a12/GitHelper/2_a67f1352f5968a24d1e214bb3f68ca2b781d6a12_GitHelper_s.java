 package ru.uiiiii.ssearchm.searching;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.eclipse.jgit.api.BlameCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.api.errors.NoHeadException;
 import org.eclipse.jgit.blame.BlameResult;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.revwalk.RevCommit;
 
 import ru.uiiiii.ssearchm.indexing.Indexer;
 
 public class GitHelper {
 	
 	private Git git;
 	
 	private ObjectId headId;
 	
 	public GitHelper(String docsPath) throws IOException, NoHeadException, GitAPIException {
 		git = Git.open(new File(docsPath));
 		headId = git.log().call().iterator().next().getId();
 	}
 	
 	private BlameResult getBlameResult(String filePath) throws IOException, GitAPIException {
 		String docsPath = Indexer.DOCS_PATH;
 		
		String filePathInsideRepo = filePath.replace(docsPath, "").substring(1); // remove \\
 		
 		BlameCommand blame = git.blame();
 		blame.setFilePath(filePathInsideRepo);
 		blame.setStartCommit(headId);
 		blame.setFollowFileRenames(true);
 		BlameResult result = blame.call();
 		
 		return result;
 	}
 	
 	public Set<RevCommit> getCommitsFromFile(String filePath) throws IOException, GitAPIException {
 		BlameResult blameResult = getBlameResult(filePath);
 		int linesCount = blameResult.getResultContents().size();
 		TreeSet<RevCommit> commits = new TreeSet<RevCommit>();
 		
 		for	(int i = 0; i < linesCount; i++) {
 			commits.add(blameResult.getSourceCommit(i));
 		}
 		
 		return commits;
 	}
 }
