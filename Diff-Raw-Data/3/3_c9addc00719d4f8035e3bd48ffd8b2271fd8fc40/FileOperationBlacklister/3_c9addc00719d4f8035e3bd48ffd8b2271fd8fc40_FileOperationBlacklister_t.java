 package edu.illinois.gitsvn.infra.filters.blacklister;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.diff.DiffEntry.ChangeType;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.gitective.core.filter.commit.CommitDiffFilter;
 
 /**
  * Excludes a commit for which all Diffs are of change type DELETE or RENAME.
  * 
  * @author mihai
  *
  */
 public class FileOperationBlacklister extends CommitDiffFilter {
 	
 	private ChangeType changeType;
 	
 	private FileOperationBlacklister(ChangeType ct){
		this();
 		this.changeType = ct;
 	}
 	
 	private FileOperationBlacklister(){
		super(true);
 	}
 	
 	public static FileOperationBlacklister getDeleteDiffFilter(){
 		return new FileOperationBlacklister(ChangeType.DELETE);
 	}
 	
 	public static FileOperationBlacklister getRenameDiffFilter(){
 		return new FileOperationBlacklister(ChangeType.RENAME);
 	}
 
 	@Override
 	public boolean include(RevCommit commit, Collection<DiffEntry> diffs) throws IOException {
 		for (DiffEntry diffEntry : diffs) {
 			if (!diffEntry.getChangeType().equals(changeType))
 				return true;
 		}
 
 		return false;
 	}
 }
