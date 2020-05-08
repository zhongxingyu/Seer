 package edu.illinois.gitsvn.infra;
 
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.errors.IncorrectObjectTypeException;
 import org.eclipse.jgit.errors.MissingObjectException;
 import org.eclipse.jgit.errors.StopWalkException;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.gitective.core.filter.commit.CommitFilter;
 
 import edu.illinois.gitsvn.infra.filters.AnalysisFilter;
 
 public class PipelineCommitFilter extends CommitFilter {
 
 	private List<CommitFilter> filters = new ArrayList<CommitFilter>();
 	private List<CommitFilter> collectors = new ArrayList<CommitFilter>();
 	private AnalysisFilter dataAgregator;
 
 	public void addFilter(CommitFilter filter) {
 		filters.add(filter);
 	}
 
 	public void addDataCollector(CommitFilter collector) {
 		if (!(collector instanceof DataCollector))
 			throw new InvalidParameterException("The collector should be a DataCollector");
 		
 		collectors.add(collector);
 	}
 
 	public List<DataCollector> getAllCollectors() {
 		List<DataCollector> returnedCollectors = new ArrayList<DataCollector>();
 		for (CommitFilter collector : collectors) {
 			returnedCollectors.add((DataCollector) collector);
 		}
 
 		return returnedCollectors;
 	}
 
 	public void setDataAgregator(AnalysisFilter agregator) {
 		this.dataAgregator = agregator;
 	}
 
 	@Override
 	public boolean include(RevWalk walker, RevCommit cmit)
 			throws StopWalkException, MissingObjectException,
 			IncorrectObjectTypeException, IOException {
 
 		for (CommitFilter filter : filters) {
 			boolean result = filter.include(walker, cmit);
 			if (result == false)
				return true;
 		}
 
 		for (CommitFilter collector : collectors) {
 			collector.include(walker, cmit);
 		}
 
 		dataAgregator.include(walker, cmit);
 
 		return true;
 	}
 
 	public void setRepository(Git repo) {
 		Repository repository = repo.getRepository();
 		for (CommitFilter filter : filters) {
 			filter.setRepository(repository);
 		}
 		
 		for (CommitFilter collector: collectors) {
 			collector.setRepository(repository);
 		}
 		
 		dataAgregator.setRepository(repository);
 	}
 
 	public AnalysisFilter getAgregator() {
 		return dataAgregator;
 	}
 }
