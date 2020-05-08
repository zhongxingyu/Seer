 package strategie;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import search.BreadthFirstSearch;
 import search.BreadthFirstSearchListener;
 import search.Problem;
 import search.TimeoutStopCriteria;
 import search.path.PathHelper;
 import search.path.impl.Path;
 
 public class Smell<T extends Number & Comparable<T>, U> {
 
 	private static class SearchResults<T extends Number & Comparable<T>, U> {
 		private final BreadthFirstSearch<T, U> search;
 		private final Map<U, Path<T, U>> paths = new HashMap<U, Path<T, U>>();
 
 		public SearchResults(BreadthFirstSearch<T, U> search) {
 			this.search = search;
 
 			// search for adding entries to searchResult.paths
 			BreadthFirstSearchListener<T, U> breadthFirstSearchListener = new BreadthFirstSearchListener<T, U>() {
 				@Override
 				public void addPathPerformed(Path<T, U> path) {
 					paths.put(path.getEnd(), path);
 				}
 			};
 			search.addListener(breadthFirstSearchListener);
 		}
 
 	}
 
 	private final Map<U, SearchResults<T, U>> searches = new HashMap<U, SearchResults<T, U>>();
 
 	public void removeCachedEntriesIfNotExisting(Set<U> existing) {
 		Set<U> differenceSet = new HashSet<U>(searches.keySet());
 		differenceSet.removeAll(existing);
 		for (U difference : differenceSet) {
 			searches.remove(difference);
 		}
 	}
 
 	public void createSmell(final Problem<U> problem, int searchTime, U initial) {
 		final SearchResults<T, U> searchResults;
 		if (searches.containsKey(initial)) {
 			searchResults = searches.get(initial);
 		} else {
 			searchResults = new SearchResults<T, U>(
 					new BreadthFirstSearch<T, U>(
Collections.<U> emptySet(),
							initial, problem));
 			searches.put(initial, searchResults);
 		}
 
 		searchResults.search.search(new TimeoutStopCriteria<T, U>(searchTime));
 	}
 
 	/**
 	 * Returns cached shortest path from start to nearest target. If more that
 	 * one path has the same length, then one path is picked non deterministic.
 	 * 
 	 * @param start
 	 * @return <code>null</code> means no path found
 	 */
 	public Path<T, U> getShortestPath(U start) {
 		Path<T, U> shortestPath = null;
 		for (SearchResults<T, U> entry : searches.values()) {
 			Path<T, U> path = entry.paths.get(start);
 			if (path == null) {
 				continue;
 			}
 			shortestPath = pathFound(shortestPath, path);
 		}
 		if (shortestPath != null) {
 			shortestPath = PathHelper.invert(shortestPath);
 		}
 		return shortestPath;
 	}
 
 
 
 	private Path<T, U> pathFound(Path<T, U> shortestPath, Path<T, U> path) {
 		if (shortestPath == null) {
 			return path;
 		}
 		if (path.getLength().compareTo(shortestPath.getLength()) < 0) {
 			return path;
 		}
 		return shortestPath;
 	}
 }
