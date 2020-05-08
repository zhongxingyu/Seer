 package edu.illinois.gitsvn.infra.filters.blacklister;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.gitective.core.filter.commit.CommitDiffFilter;
 
 public class AllDeletionDiffFilter extends CommitDiffFilter {
	
 	@Override
 	public boolean include(RevCommit commit, Collection<DiffEntry> diffs) throws IOException {
		// TODO Auto-generated method stub
		return super.include(commit, diffs);
 	}
 }
