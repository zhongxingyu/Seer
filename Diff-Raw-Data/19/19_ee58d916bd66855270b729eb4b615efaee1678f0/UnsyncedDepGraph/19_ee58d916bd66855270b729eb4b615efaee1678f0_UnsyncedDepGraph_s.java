 package org.magnolialang.util.depgraph;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class UnsyncedDepGraph<T> implements IWritableDepGraph<T> {
 	public static <T, U> boolean isSetEquals(Collection<T> a, Collection<U> b) {
 		if(a.size() != b.size())
 			return false;
 		for(Object o : a)
 			if(!b.contains(o))
 				return false;
 		return true;
 	}
 	private final IMultiMap<T, T>	depends;
 	private final IMultiMap<T, T>	dependents;
 	private final IMultiMap<T, T>	transitiveDepends;
 

 	private final IMultiMap<T, T>	transitiveDependents;
 
 
 	public UnsyncedDepGraph() {
 		this.depends = new MultiHashMap<T, T>();
 		this.dependents = new MultiHashMap<T, T>();
 		this.transitiveDepends = new MultiHashMap<T, T>();
 		this.transitiveDependents = new MultiHashMap<T, T>();
 	}
 
 
 	private UnsyncedDepGraph(IMultiMap<T, T> depends, IMultiMap<T, T> dependents, IMultiMap<T, T> transitiveDepends, IMultiMap<T, T> transitiveDependents) {
 		this.depends = depends.copy();
 		this.dependents = dependents.copy();
 		this.transitiveDepends = transitiveDepends.copy();
 		this.transitiveDependents = transitiveDependents.copy();
 	}
 
 
 	@Override
 	public void add(T node) {
 		depends.put(node);
 		dependents.put(node);
 		transitiveDepends.put(node);
 		transitiveDependents.put(node);
 	}
 
 
 	@Override
 	public void add(T element, Collection<? extends T> depends) {
 		add(element);
 		for(T d : depends)
 			add(element, d);
 	}
 
 
 	@Override
 	public void add(T dependent, T dependency) {
 		depends.put(dependent, dependency);
 		depends.put(dependency);
 		dependents.put(dependency, dependent);
 		dependents.put(dependent);
 
 		transitiveDepends.clear();
 		transitiveDependents.clear();
 
		assert dataInvariant();
 	}
 
 
 	@Override
 	public void clear() {
 		depends.clear();
 		dependents.clear();
 		transitiveDepends.clear();
 		transitiveDependents.clear();
 	}
 
 
 	@Override
 	public UnsyncedDepGraph<T> copy() {
 		return new UnsyncedDepGraph<T>(depends, dependents, transitiveDepends, transitiveDependents);
 	}
 
 
 	public boolean dataInvariant() {
 		// incoming and outgoing must be reverses of each other
 		if(depends.numKeys() != dependents.numKeys())
 			return false;
 		for(T from : depends.keySet()) {
 			if(!dependents.containsKey(from))
 				return false;
 			for(T to : depends.get(from))
 				if(!dependents.get(to).contains(from))
 					return false;
 		}
 
 		return true;
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if(this == obj)
 			return true;
 		if(obj == null)
 			return false;
 		if(getClass() != obj.getClass())
 			return false;
 		UnsyncedDepGraph<?> other = (UnsyncedDepGraph<?>) obj;
 		if(dependents == null) {
 			if(other.dependents != null)
 				return false;
 		}
 		else if(!dependents.equals(other.dependents))
 			return false;
 		if(depends == null) {
 			if(other.depends != null)
 				return false;
 		}
 		else if(!depends.equals(other.depends))
 			return false;
 		return true;
 	}
 
 
 	@Override
 	public Set<T> getDependents(T element) {
 		if(dependents.containsKey(element))
 			return Collections.unmodifiableSet(dependents.get(element));
 		else
 			return Collections.EMPTY_SET;
 	}
 
 
 	@Override
 	public Set<T> getDepends(T element) {
 		if(depends.containsKey(element))
 			return Collections.unmodifiableSet(depends.get(element));
 		else
 			return Collections.EMPTY_SET;
 	}
 
 
 	@Override
 	public Set<T> getElements() {
 		return depends.keySet();
 	}
 
 
 	@Override
 	public Set<T> getTransitiveDependents(T element) {
 		if(transitiveDependents.containsKey(element))
 			return Collections.unmodifiableSet(transitiveDependents.get(element));
 		else if(!dependents.containsKey(element))
 			return null;
 		else {
 			Set<T> deps = computeReachable(element, dependents, transitiveDependents);
 			transitiveDependents.put(element, deps);
 			return deps;
 		}
 	}
 
 
 	@Override
 	public Set<T> getTransitiveDepends(T element) {
 		if(transitiveDepends.containsKey(element))
 			return Collections.unmodifiableSet(transitiveDepends.get(element));
 		else if(!depends.containsKey(element))
 			return null;
 		else {
 			Set<T> deps = computeReachable(element, depends, transitiveDepends);
 			transitiveDepends.put(element, deps);
 			return deps;
 		}
 	}
 
 
 	@Override
 	public boolean hasCycles() {
 		throw new UnsupportedOperationException();
 	}
 
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (dependents == null ? 0 : dependents.hashCode());
 		result = prime * result + (depends == null ? 0 : depends.hashCode());
 		return result;
 	}
 
 
 	@Override
 	public void remove(T node) {
 		Set<T> deps = depends.remove(node);
 		Set<T> depts = dependents.remove(node);
 		if(deps != null)
 			for(T dep : deps)
 				dependents.remove(dep, node);
 		if(depts != null)
 			for(T dept : depts)
 				depends.remove(dept, node);
 	}
 
 
 	@Override
 	public Iterable<T> topological() {
 		return new TopologicalIterable<T>(this);
 	}
 
 
 	/**
 	 * Compute the set of all nodes reachable from 'node' in the graph
 	 * represented by 'edgeSet'.
 	 * 
 	 * The 'transitiveEdgeSet' is used as a cache.
 	 * 
 	 * @param node
 	 * @param edgeSet
 	 * @param transitiveEdgeSet
 	 * @return
 	 */
 	private Set<T> computeReachable(T node, IMultiMap<T, T> edgeSet, IMultiMap<T, T> transitiveEdgeSet) {
 		if(!edgeSet.containsKey(node))
 			throw new IllegalArgumentException();
 
 		Set<T> deps = new HashSet<T>();
 
 		List<T> todo = new ArrayList<T>(edgeSet.get(node));
 		while(!todo.isEmpty()) {
 			T n = todo.remove(0);
 			deps.add(n);
 			if(transitiveEdgeSet.containsKey(n))
 				deps.addAll(transitiveEdgeSet.get(n));
 			else
 				for(T m : edgeSet.get(n))
 					if(!m.equals(n) && !deps.contains(m))
 						todo.add(m);
 		}
 		return deps;
 	}
 
 
 	static class TopologicalIterable<T> implements Iterable<T> {
 		List<T>	sortedList	= new ArrayList<T>();
 
 
 		TopologicalIterable(UnsyncedDepGraph<T> graph) {
 			long t0 = System.currentTimeMillis();
 			IMultiMap<T, T> depends = graph.depends.copy();
 			List<T> todo = new ArrayList<T>();
 			for(T n : depends.keySet())
 				if(depends.isEmpty(n))
 					todo.add(n);
 			while(!todo.isEmpty()) {
 				T n = todo.remove(0);
 				assert depends.isEmpty(n);
 				sortedList.add(n);
 				// get all nodes that depend on n
 				for(T m : graph.dependents.get(n)) {
 					// dependents.remove(n, m);
 					depends.remove(m, n);
 					if(depends.isEmpty(m))
 						todo.add(m);
 				}
 			}
 		}
 
 
 		@Override
 		public Iterator<T> iterator() {
 			return sortedList.iterator();
 		}
 	}
 }
