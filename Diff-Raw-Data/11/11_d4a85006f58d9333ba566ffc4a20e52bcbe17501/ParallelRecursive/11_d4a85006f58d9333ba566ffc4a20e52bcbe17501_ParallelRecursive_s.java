 public<T> void sequentialRecursive(List<Node<T>> nodes, Collection<T> results) {
     for (Node<T> n : nodes) {
 	results.add(n.compute());
 	sequentialRecursive(n.getChildren(), results);
     }
 }
 
 public<T> void parallelRecursive(final Executor exec, List<Node<T>> nodes, final Collection<T> results) {
     for (final Node<T> n : nodes) {
 	exec.execute(new Runnable() {
 	    void run() {
 		results.add(n.compute());
 	    }
 	    });
 	parallelRecursive(exec, n.getChildren(), results);
     }
 }
 
 public<T> Collection<T> getParallelResults(List<Node<T>> nodes)
         throws InterruptedException {
     ExecutorService exec = Executors.newCachedThreadPool();
     Queue<T> resultQueue = new ConcurrentLinkedQueue();
     parallelRecursive(exec, nodes, resultQueue);
     exec.shutDown();
     exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
     return resultQueue;
 }
 
 
 	    
 interface Puzzle<P, M> {
     P initialPosition();
     boolean isGoal(P position);
     Set<M> legalMoves(P position);
     P move(P position, M mov);
 }
 
 
 
 class SequentialPuzzleSolver<P, M> {
     private final Puzzle<P, M> puzzle;
     private final Set<P> seen  = new HashSet();
     
 
     SequentialPuzzleSolver(Puzzle<P, M> p) {
 	this.puzzle = p;
     }
 
     List<M> solve() {
 	P pos = puzzle.initialPosition();
 	return search(new Node<P, M>(pos, null, null));
     }
 
     private List<M> search(Node<P, M> node) {
 	if (!seen.contains(node.pos)) {
 	    seen.add(pos);
 	    if (puzzle.isGoal(node.pos)) {
 		return node.asMoveList();
 	    }
 
 	    for (M move : puzzle.legalMoves(node.pos)) {
 		P pos = puzzle.move(node.pos, move);
 		Node<P, M> child = new Node(pos, move, node);
 		List<M> result = search(child);
 		if (result != null)
 		    return result;
 	    }
 	}
 	return null;
     }
 
     @Immutable
     static class Node<P, M> {
 	final P pos;
 	final M move;
 	final Node<P, M> prev;
 
 	Node(P p, M m, Node<P, M> prev) {
 	    this.pos = p;
 	    this.move = m;
 	    this.prev = prev;
 	}
     
 	List<M> asMoveList() {
 	    List<M> solution = new LinkedList();
 	    for (Node<P, M> n = this, n.move != null; n = n.prev)
 		solution.add(0, n.move);
 	    return solution;
 	}
     }
 }
 
 
 class ConcurrentPuzzleSolver<P, M> {
     private final Puzzle<P, M> puzzle;
     private final ExecutorService exec;
     private final ConcurrentMap<P, Boolean> seen;
    final ValueLatch<Node<P, M>> solution = new ValueLatch();
 
     ConcurrentPuzzleSolver() {
 	exec = Executors.newCachedThreadPool();
     }
 
     List<M> solve() throws InterruptedException {
 	try {
 	    P pos = puzzle.initialPosition();
	    exec.execute(newTaskp, null, null);
 	    // block until solution found
 	    Node<P, M> solnNode = solution.getValue();
 	    return (solnNode == null)? null:solnNode.asMoveList();
 	} finally {
 	    exec.shutdown();
 	}
     }
 
     protected Runnable newTask(P p, M m, Node<P, M> n) {
 	return new SolverTask(p, m, n);
     }
 
     class SolverTask extends Node<P, M> implements Runnable {
 	public void run() {
 	    if (solution.isSet() ||
 		seen.putIfAbsent(pos, true) != null)
		return;
 	    
 	    if (puzzle.isGoal(pos)) {
 		solution.setValue(this);
 	    } else {
 		for (M move : puzzle.legalMoves(pos)) {
 		    exec.execute(newTask(puzzle.move(pos, m),
 					 m,
 					 this));
 		}
 	    }
 	}
     }
 
     @Immutable
     static class Node<P, M> {
 	final P pos;
 	final M move;
 	final Node<P, M> prev;
 
 	Node(P p, M m, Node<P, M> prev) {
 	    this.pos = p;
 	    this.move = m;
 	    this.prev = prev;
 	}
     
 	List<M> asMoveList() {
 	    List<M> solution = new LinkedList();
 	    for (Node<P, M> n = this, n.move != null; n = n.prev)
 		solution.add(0, n.move);
 	    return solution;
 	}
     }
 }
 
 
 class ValueLatch<T> {
     private T value = null;
     private final CountDownLatch done = new CountDownLatch(1);
 
     public boolean isSet() {
 	return (done.getCount() == 0);
     }
 
     public synchronized void SetValue(T newValue) {
 	if (!isSet()) {
 	    value = newValue;
 	    done.countDown();
 	}
     }
 
     public T getValue() throws InterruptedException {
 	done.await();
 	synchronized(this) {
 	    return value;
 	}
     }
 }
