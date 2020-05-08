 package edu.illinois.gitsvn.infra.filters.blacklister;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import org.eclipse.jgit.diff.DiffEntry;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.gitective.core.filter.commit.CommitDiffFilter;
 
/**
 * Excludes a commit for which all Diffs are of change type DELETE.
 * 
 * @author mihai
 *
 */
 public class AllDeletionDiffFilter extends CommitDiffFilter {

 	@Override
 	public boolean include(RevCommit commit, Collection<DiffEntry> diffs) throws IOException {
		for (DiffEntry diffEntry : diffs) {
			if (!diffEntry.getChangeType().equals(DiffEntry.ChangeType.DELETE))
				return true;
		}

		return false;
 	}
 }
