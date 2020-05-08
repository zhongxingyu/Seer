 package com.example.zigzag.internal;
 
 import java.awt.Color;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.task.NodeViewTaskFactory;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.View;
 import org.cytoscape.view.presentation.property.BasicVisualLexicon;
 import org.cytoscape.work.AbstractTask;
 import org.cytoscape.work.Task;
 import org.cytoscape.work.TaskIterator;
 import org.cytoscape.work.TaskMonitor;
 
 class FindPathsNodeViewTaskFactory implements NodeViewTaskFactory {
 	@Override
 	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
 		return nodeView != null && networkView != null;
 	}
 
 	@Override
 	public TaskIterator createTaskIterator(View<CyNode> nodeView,
 			CyNetworkView networkView) {
 		Task task = createTask(nodeView, networkView);
 		return new TaskIterator(task);
 	}
 
 	private Task createTask(final View<CyNode> nodeView,
 			final CyNetworkView networkView) {
 		
 		// AbstractTask implements the cancel() method for us. All we need to do
 		// is poll AbstractTask.cancelled to see if we need to abort.
 		return new AbstractTask() {
 			@Override
 			public void run(TaskMonitor taskMonitor) throws Exception {
 				Set<CyEdge> edges = findPaths(nodeView, networkView);
 				taskMonitor.setProgress(0.33);
 				
 				if (cancelled) {
 					return;
 				}
 
 				resetVisualProperties(networkView);
 				taskMonitor.setProgress(0.67);
 				
 				setVisualPropertiesForPaths(networkView, edges);
 				taskMonitor.setProgress(1.0);
 				
 				networkView.updateView();
 			}
 
 			private Set<CyEdge> findPaths(View<CyNode> nodeView,
 					CyNetworkView networkView) {
 				// Use breadth-first search algorithm to find all connected
 				// nodes and edges.
 				final Set<CyNode> nodes = new HashSet<CyNode>();
 				final Set<CyEdge> edges = new HashSet<CyEdge>();
 				final LinkedList<CyNode> pending = new LinkedList<CyNode>();
 
 				final CyNetwork network = networkView.getModel();
 				final CyNode startingNode = nodeView.getModel();
 				pending.push(startingNode);
 
 				while (!pending.isEmpty()) {
 					if (cancelled) {
 						return Collections.emptySet();
 					}
 					final CyNode node = pending.pop();
 					if (nodes.contains(node))
 						continue;
 					pending.addAll(network.getNeighborList(node,
 							CyEdge.Type.OUTGOING));
 					edges.addAll(network.getAdjacentEdgeList(node,
 							CyEdge.Type.OUTGOING));
 					nodes.add(node);
 				}
 				return edges;
 			}
 		};
 	}
 
 	private static void resetVisualProperties(final CyNetworkView networkView) {
 		for (final View<CyNode> view : networkView.getNodeViews())
 			view.clearValueLock(BasicVisualLexicon.NODE_BORDER_WIDTH);
 
 		for (final View<CyEdge> view : networkView.getEdgeViews())
 			view.clearValueLock(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
 	}
 
 	private static void setVisualPropertiesForPaths(
 			final CyNetworkView networkView, final Set<CyEdge> edges) {
		for (final View<CyNode> nodeView : networkView.getNodeViews())
			nodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 9.0);

 		for (final CyEdge edge : edges) {
 			View<CyEdge> edgeView = networkView.getEdgeView(edge);
 			edgeView.setLockedValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT,
 					Color.orange);
 
 			CyNode source = edge.getSource();
 			View<CyNode> sourceView = networkView.getNodeView(source);
 			sourceView.setLockedValue(
 					BasicVisualLexicon.NODE_BORDER_WIDTH, 9.0);
 
 			CyNode target = edge.getTarget();
 			View<CyNode> targetView = networkView.getNodeView(target);
 			targetView.setLockedValue(
 					BasicVisualLexicon.NODE_BORDER_WIDTH, 9.0);
 		}
 	}
 }
