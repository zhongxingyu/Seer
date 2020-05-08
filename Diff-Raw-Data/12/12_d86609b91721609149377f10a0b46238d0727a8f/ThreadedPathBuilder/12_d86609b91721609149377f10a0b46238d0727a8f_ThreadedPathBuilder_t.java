 package soot.jimple.infoflow.data.pathBuilders;
 
 import heros.solver.CountingThreadPoolExecutor;
 
 import java.util.Collections;
import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Set;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import soot.jimple.Stmt;
 import soot.jimple.infoflow.InfoflowResults;
 import soot.jimple.infoflow.data.Abstraction;
 import soot.jimple.infoflow.data.AbstractionAtSink;
 import soot.jimple.infoflow.data.SourceContextAndPath;
 import soot.jimple.infoflow.util.ConcurrentHashSet;
 import soot.util.IdentityHashSet;
 
 /**
  * Class for reconstructing abstraction paths from sinks to source
  * 
  * @author Steven Arzt
  */
 public class ThreadedPathBuilder implements IAbstractionPathBuilder {
 	
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private final InfoflowResults results = new InfoflowResults();
 	private final CountingThreadPoolExecutor executor;
 	
 	private final Set<Abstraction> roots = new ConcurrentHashSet<Abstraction>();
 	private IdentityHashMap<Abstraction, Set<Abstraction>> successors = null;
 	private IdentityHashMap<Abstraction, Set<Abstraction>> neighbors = null;
 	
 	/**
 	 * Creates a new instance of the {@link ThreadedPathBuilder} class
 	 * @param maxThreadNum The maximum number of threads to use
 	 */
 	public ThreadedPathBuilder(int maxThreadNum) {
         int numThreads = Runtime.getRuntime().availableProcessors();
 		this.executor = createExecutor(maxThreadNum == -1 ? numThreads
 				: Math.min(maxThreadNum, numThreads));
 	}
 	
 	/**
 	 * Creates a new executor object for spawning worker threads
 	 * @param numThreads The number of threads to use
 	 * @return The generated executor
 	 */
 	private CountingThreadPoolExecutor createExecutor(int numThreads) {
 		return new CountingThreadPoolExecutor
 				(numThreads, Integer.MAX_VALUE, 30, TimeUnit.SECONDS,
 				new LinkedBlockingQueue<Runnable>());
 	}
 	
 	/**
 	 * Task for only finding sources, not the paths towards them
 	 * 
 	 * @author Steven Arzt
 	 */
 	private class SourceFindingTask implements Runnable {
 		private final AbstractionAtSink flagAbs;
 		private final Abstraction abstraction;
 		
 		public SourceFindingTask(AbstractionAtSink flagAbs, Abstraction abstraction) {
 			this.flagAbs = flagAbs;
 			this.abstraction = abstraction;
 		}
 		
 		private boolean addSuccessor(Abstraction parent, Abstraction child) {
 			if (successors != null) {
 				synchronized (successors) {
 					Set<Abstraction> succs = successors.get(parent);
 					if (succs == null) {
 						succs = new IdentityHashSet<Abstraction>();
 						successors.put(parent, succs);
 					}
 					return succs.add(child);
 				}
 			}
 			return false;
 		}
 		
 		private boolean addNeighbor(Abstraction abs, Abstraction neighbor) {
 			if (neighbors != null) {
 				synchronized (neighbors) {
 					Set<Abstraction> succs = neighbors.get(abs);
 					if (succs == null) {
 						succs = new IdentityHashSet<Abstraction>();
 						neighbors.put(abs, succs);
 					}
 					return succs.add(neighbor);
 				}
 			}
 			return false;
 		}
 
 		private void addRoot(Abstraction root) {
 			if (roots != null)
 				roots.add(root);
 		}
 
 		@Override
 		public void run() {
 			if (!abstraction.registerPathFlag(flagAbs))
 				return;
 
 			if (abstraction.getPredecessor() != null)
 				addSuccessor(abstraction.getPredecessor(), abstraction);
 			if (abstraction.getNeighbors() != null)
 				for (Abstraction nb : abstraction.getNeighbors()) {
 					addNeighbor(nb, abstraction);
 					addNeighbor(abstraction, nb);
 				}
 			
 			if (abstraction.getSourceContext() != null) {
 				// Register the result
 				if (successors == null)
 					results.addResult(flagAbs.getSinkValue(),
 							flagAbs.getSinkStmt(),
 							abstraction.getSourceContext().getValue(),
 							abstraction.getSourceContext().getStmt(),
 							abstraction.getSourceContext().getUserData(),
 							Collections.<Stmt>emptyList());
 
 				SourceContextAndPath rootScap = new SourceContextAndPath
 						(abstraction.getSourceContext().getValue(),
 						abstraction.getSourceContext().getStmt(),
 						abstraction.getSourceContext().getUserData()).extendPath
 								(abstraction.getSourceContext().getStmt());
 				abstraction.getOrMakePathCache().add(rootScap);
 				
 				addRoot(abstraction);
 				
 				// Sources may not have predecessors
 				assert abstraction.getPredecessor() == null;
 			}
 			else
 				executor.execute(new SourceFindingTask(flagAbs, abstraction.getPredecessor()));
 			
 			if (abstraction.getNeighbors() != null)
 				for (Abstraction nb : abstraction.getNeighbors())
 					executor.execute(new SourceFindingTask(flagAbs, nb));
 		}
 	}
 	
 	/**
 	 * Task that extend paths down the data flow
 	 * 
 	 * @author Steven Arzt
 	 */
 	private class ExtendPathTask implements Runnable {
 		
 		private final Object flagAbs;
 		private final Abstraction parent;
 		private final boolean extendPath;
 		
 		public ExtendPathTask(Object flagAbs, Abstraction parent, boolean extendPath) {
 			this.flagAbs = flagAbs;
 			this.parent = parent;
 			this.extendPath = extendPath;
 		}
 		
 		@Override
 		public void run() {
 			// Check the paths of the parent. If we have none, we can abort
 			Set<SourceContextAndPath> parentPaths = parent.getPaths();
 			if (parentPaths == null || parentPaths.isEmpty())
 				return;
 			
 			// Copy over the paths of our neighbors
 			Set<Abstraction> nbs = neighbors.get(parent);
 			if (nbs != null)
 				for (Abstraction nb : nbs) {
 					Set<SourceContextAndPath> nbPaths = nb.getPaths();
 					if (nbPaths != null)
 						parentPaths.addAll(nbPaths);
 				}
 			
 			// Get the children. If we have none, we can abort
 			Set<Abstraction> children = successors.get(parent);
 			if (children == null || children.isEmpty())
 				return;
 			
 			for (Abstraction child : children) {
 				boolean added = false;
 				Set<SourceContextAndPath> childScaps = child.getOrMakePathCache();
 				for (SourceContextAndPath scap : parentPaths) {
 					if (extendPath && child.getCurrentStmt() != null) {
 						if (childScaps.add(scap.extendPath(child.getCurrentStmt())))
 							added = true;
 					}
 					else if (childScaps.add(scap))
 						added = true;
 				}
 				
 				// If we have added a new path, we schedule it to be propagated
 				// down to the child's children
 				if (added) {
 					executor.execute(new ExtendPathTask(flagAbs, child, true));
 					Set<Abstraction> childNbs = neighbors.get(child);
 					if (childNbs != null)
 						for (Abstraction nb : childNbs)
 							executor.execute(new ExtendPathTask(flagAbs, nb, true));
 				}
 			}
 		}
 		
 	}
 
 	@Override
 	public void computeTaintSources(final Set<AbstractionAtSink> res) {
 		if (res.isEmpty())
 			return;
 		
 		logger.debug("Running path reconstruction");
     	logger.info("Obtainted {} connections between sources and sinks", res.size());
     	
     	// Start the propagation tasks
     	int curResIdx = 0;
     	for (final AbstractionAtSink abs : res) {
     		logger.info("Building path " + ++curResIdx);
     		executor.execute(new SourceFindingTask(abs, abs.getAbstraction()));
     	}
 
     	try {
 			executor.awaitCompletion();
 		} catch (InterruptedException ex) {
 			logger.error("Could not wait for path executor completion: {0}", ex.getMessage());
 			ex.printStackTrace();
 		}
     	
     	logger.debug("Path reconstruction done.");
 	}
 	
 	@Override
 	public void computeTaintPaths(final Set<AbstractionAtSink> res) {
 		if (res.isEmpty())
 			return;
 		
 		long beforePathTracking = System.nanoTime();
 		successors = new IdentityHashMap<Abstraction, Set<Abstraction>>();
 		neighbors = new IdentityHashMap<Abstraction, Set<Abstraction>>();
 		computeTaintSources(res);
 		
     	// Start the path extensions tasks
 		logger.info("Running path extension on {} roots", roots.size());
     	for (Abstraction root : roots)
    			executor.execute(new ExtendPathTask(new Object(), root, true));
     	
     	try {
 			executor.awaitCompletion();
 		} catch (InterruptedException ex) {
 			logger.error("Could not wait for path executor completion: {0}", ex.getMessage());
 			ex.printStackTrace();
 		}
     	
     	logger.info("Path extension done.");
     	
     	// Collect the results
     	for (final AbstractionAtSink abs : res) {
    		Set<SourceContextAndPath> allScaps = new HashSet<SourceContextAndPath>();
    		if (abs.getAbstraction().getPaths() != null)
    			allScaps.addAll(abs.getAbstraction().getPaths());
    		if (abs.getAbstraction().getNeighbors() != null)
    			for (Abstraction nb : abs.getAbstraction().getNeighbors())
    				if (nb.getPaths() != null)
    					allScaps.addAll(nb.getPaths());
    		
    		for (SourceContextAndPath context : allScaps)
     			if (context.getSymbolic() == null) {
 					results.addResult(abs.getSinkValue(), abs.getSinkStmt(),
 							context.getValue(), context.getStmt(), context.getUserData(),
 							context.getPath(), abs.getSinkStmt());
     			}
     	}
     	
     	successors = null;
     	logger.info("Path proecssing took {} seconds", (System.nanoTime() - beforePathTracking) / 1E9);
 	}
 	
 	@Override
 	public void shutdown() {
     	executor.shutdown();		
 	}
 
 	@Override
 	public InfoflowResults getResults() {
 		return this.results;
 	}
 
 }
