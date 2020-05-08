 package org.pescuma.buildhealth.analyser.utils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.pescuma.buildhealth.core.BuildStatus;
 
 import com.google.common.base.Predicate;
 
 public class BuildHealthAnalyserUtils {
 	
 	public static <T extends TreeStats> Collection<SimpleTree<T>.Node> sort(Collection<SimpleTree<T>.Node> nodes,
 			boolean highlighProblems) {
 		
 		if (!highlighProblems)
 			return nodes;
 		else
 			return sort(nodes);
 	}
 	
 	public static <T extends TreeStats> Collection<SimpleTree<T>.Node> sort(Collection<SimpleTree<T>.Node> nodes) {
 		
 		List<SimpleTree<T>.Node> result = new ArrayList<SimpleTree<T>.Node>(nodes);
 		
 		Collections.sort(result, new Comparator<SimpleTree<T>.Node>() {
 			@Override
 			public int compare(SimpleTree<T>.Node o1, SimpleTree<T>.Node o2) {
				int cmp = precedence(o1.getData().getStatusWithChildren())
						- precedence(o2.getData().getStatusWithChildren());
 				if (cmp != 0)
 					return cmp;
 				
 				return o1.getName().compareToIgnoreCase(o2.getName());
 			}
 			
 			private int precedence(BuildStatus status) {
 				switch (status) {
 					case Good:
 						return 2;
 					case SoSo:
 						return 1;
 					case Problematic:
 						return 0;
 					default:
 						throw new IllegalStateException();
 				}
 			}
 		});
 		
 		return result;
 	}
 	
 	public static <T extends TreeStats> void removeNonSummaryNodes(SimpleTree<T> tree, boolean highlighProblems) {
 		if (!highlighProblems)
 			tree.getRoot().removeChildIf(new Predicate<SimpleTree<T>.Node>() {
 				@Override
 				public boolean apply(SimpleTree<T>.Node input) {
 					return true;
 				}
 			});
 		
 		else
 			tree.removeNodesIf(new Predicate<SimpleTree<T>.Node>() {
 				@Override
 				public boolean apply(SimpleTree<T>.Node node) {
 					T data = node.getData();
 					return data.getStatusWithChildren() == BuildStatus.Good
 							|| (!node.isRoot() && !data.hasOwnStatus() && node.getChildren().isEmpty());
 				}
 			});
 	}
 	
 	public static class TreeStats {
 		
 		private final String[] names;
 		private boolean hasOwnStatus = false;
 		private BuildStatus ownStatus = BuildStatus.Good;
 		private BuildStatus statusWithChildren = BuildStatus.Good;
 		
 		protected TreeStats(String... names) {
 			this.names = names;
 		}
 		
 		public String[] getNames() {
 			return names;
 		}
 		
 		public BuildStatus getOwnStatus() {
 			return ownStatus;
 		}
 		
 		public void setOwnStatus(BuildStatus status) {
 			if (hasOwnStatus)
 				ownStatus = ownStatus.mergeWith(status);
 			else
 				ownStatus = status;
 			
 			statusWithChildren = statusWithChildren.mergeWith(status);
 			
 			hasOwnStatus = true;
 		}
 		
 		public boolean hasOwnStatus() {
 			return hasOwnStatus;
 		}
 		
 		public BuildStatus getStatusWithChildren() {
 			return statusWithChildren;
 		}
 		
 		public void mergeChildStatus(TreeStats other) {
 			if (!hasOwnStatus)
 				ownStatus = ownStatus.mergeWith(other.ownStatus);
 			
 			statusWithChildren = statusWithChildren.mergeWith(other.statusWithChildren);
 		}
 		
 	}
 }
