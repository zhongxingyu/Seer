 package org.knime.knip.tracking.nodes.transition.transitiongraphbuilder;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 
 import net.imglib2.collection.KDTree;
 import net.imglib2.meta.ImgPlus;
 import net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree;
 import net.imglib2.type.NativeType;
 import net.imglib2.type.numeric.IntegerType;
 
 import org.knime.core.data.DataCell;
 import org.knime.core.data.DataColumnSpec;
 import org.knime.core.data.DataColumnSpecCreator;
 import org.knime.core.data.DataTableSpec;
 import org.knime.core.data.container.DataContainer;
 import org.knime.core.data.def.DefaultRow;
 import org.knime.core.data.def.DoubleCell;
 import org.knime.core.data.def.StringCell;
 import org.knime.core.node.BufferedDataTable;
 import org.knime.core.node.ExecutionContext;
 import org.knime.core.node.InvalidSettingsException;
 import org.knime.core.node.NodeSettingsRO;
 import org.knime.core.node.NodeSettingsWO;
 import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
 import org.knime.knip.base.data.img.ImgPlusCell;
 import org.knime.knip.base.data.img.ImgPlusCellFactory;
 import org.knime.knip.base.data.img.ImgPlusValue;
 import org.knime.knip.tracking.data.features.FeatureProvider;
 import org.knime.knip.tracking.data.graph.TrackedNode;
 import org.knime.knip.tracking.data.graph.TransitionGraph;
 import org.knime.knip.tracking.data.graph.renderer.TransitionGraphRenderer;
 import org.knime.knip.tracking.util.PartitionComparator;
 import org.knime.network.core.api.KPartiteGraphView;
 import org.knime.network.core.api.Partition;
 import org.knime.network.core.api.PersistentObject;
 import org.knime.network.core.core.PartitionType;
 import org.knime.network.core.knime.cell.GraphCellFactory;
 import org.knime.network.core.knime.node.KPartiteGraphViewAndTable2TableNodeModel;
 import org.knime.network.core.knime.port.GraphPortObjectSpec;
 
 /**
  * This is the model implementation of TransitionGraphBuilder.
  * 
  * 
  * @author Stephan Sellien
  */
 public class TransitionGraphBuilderNodeModel<T extends NativeType<T> & IntegerType<T>>
 		extends KPartiteGraphViewAndTable2TableNodeModel {
 
 	// the logger instance
 	// private static final NodeLogger logger = NodeLogger
 	// .getLogger(TransitionGraphBuilderNodeModel.class);
 
 	/**
 	 * the settings key which is used to retrieve and store the settings (from
 	 * the dialog or from a settings file) (package visibility to be usable from
 	 * the dialog).
 	 */
 	static final String CFGKEY_DISTANCE = "distance";
 
 	/** initial default distance value. */
 	static final double DEFAULT_DISTANCE = 10.0;
 
 	private final SettingsModelDouble m_distance = createDistanceSettings();
 
 	static SettingsModelDouble createDistanceSettings() {
 		return new SettingsModelDouble(
 				TransitionGraphBuilderNodeModel.CFGKEY_DISTANCE,
 				TransitionGraphBuilderNodeModel.DEFAULT_DISTANCE);
 	}
 
 	private final static String CENTROID_X = "Centroid X";
 	private final static String CENTROID_Y = "Centroid Y";
 
 	/**
 	 * Constructor for the node model.
 	 */
 	protected TransitionGraphBuilderNodeModel() {
 	}
 
 	// TODO: reenable check
 	protected void checkConfigure(GraphPortObjectSpec spec)
 			throws InvalidSettingsException {
 		String[] neededFeatures = { CENTROID_X, CENTROID_Y };
 		for (String s : neededFeatures) {
 			if (!spec.getMetaData().containsFeatureName(s))
 				throw new InvalidSettingsException("Feature " + s
 						+ " not found.");
 		}
 
 	}
 
 	private DataTableSpec createOutSpec() {
 		List<DataColumnSpec> cols = new LinkedList<DataColumnSpec>();
 		cols.add(new DataColumnSpecCreator("Image representation",
 				ImgPlusCell.TYPE).createSpec());
 		cols.add(new DataColumnSpecCreator("String representation",
 				StringCell.TYPE).createSpec());
 		cols.add(new DataColumnSpecCreator("Nodes/Partition", StringCell.TYPE)
 				.createSpec());
 		cols.add(new DataColumnSpecCreator("Transition graphs",
 				GraphCellFactory.getType()).createSpec());
 		for (String feature : FeatureProvider.getFeatureNames()) {
 			cols.add(new DataColumnSpecCreator(feature, DoubleCell.TYPE)
 					.createSpec());
 		}
 		return new DataTableSpec(cols.toArray(new DataColumnSpec[cols.size()]));
 	}
 
 	private void createTransitionGraph(KDTree<TrackedNode> tree,
 			KDTree<TrackedNode> nextTree, TransitionGraph graph, TrackedNode node,
 			double radius, Set<TrackedNode> usedSegments) {
 		// graph.addNode(node);
 		node.createCopyIn(graph);
 		RadiusNeighborSearchOnKDTree<TrackedNode> rns = new RadiusNeighborSearchOnKDTree<TrackedNode>(
 				nextTree);
 		rns.search(node, radius, false);
 		for (int obj = 0; obj < rns.numNeighbors(); obj++) {
 			TrackedNode nextNode = rns.getSampler(obj).get();
 			if (usedSegments.contains(nextNode))
 				continue;
 			usedSegments.add(nextNode);
 			createTransitionGraph(nextTree, tree, graph, nextNode, radius,
 					usedSegments);
 		}
 	}
 
 	private void addAll(Queue<TrackedNode> queue, KDTree<TrackedNode>.KDTreeCursor cursor) {
 		while (cursor.hasNext()) {
 			TrackedNode node = cursor.next();
 			queue.add(node);
 		}
 	}
 
 	@Override
 	protected void resetInternal() {
 	}
 
 	@Override
 	protected void saveSettingsTo(NodeSettingsWO settings) {
 		m_distance.saveSettingsTo(settings);
 	}
 
 	@Override
 	protected void validateSettings(NodeSettingsRO settings)
 			throws InvalidSettingsException {
 		m_distance.validateSettings(settings);
 	}
 
 	@Override
 	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
 			throws InvalidSettingsException {
 		m_distance.loadSettingsFrom(settings);
 	}
 
 	@Override
 	protected BufferedDataTable execute(ExecutionContext exec,
 			KPartiteGraphView<PersistentObject, Partition> net,
 			BufferedDataTable table) throws Exception {
 		DataContainer cont = exec.createDataContainer(createOutSpec());
 		List<Partition> partitions = new ArrayList<Partition>(
 				net.getPartitions(PartitionType.NODE));
 		Collections.sort(partitions, new PartitionComparator());
 
 		@SuppressWarnings("unchecked")
 		ImgPlus<T> baseImg = ((ImgPlusValue<T>) table.iterator().next()
 				.getCell(0)).getImgPlus();
 
 		List<KDTree<TrackedNode>> trees = new LinkedList<KDTree<TrackedNode>>();
 		// build up kdtrees
 		for (Partition partition : partitions) {
 			exec.checkCanceled();
 			List<TrackedNode> objects = new LinkedList<TrackedNode>();
 			List<TrackedNode> locations = new LinkedList<TrackedNode>();
 			for (PersistentObject pobj : net.getNodes(partition)) {
 				TrackedNode node = new TrackedNode(net, pobj);
 				objects.add(node);
 				locations.add(node);
 			}
 			trees.add(new KDTree<TrackedNode>(objects, locations));
 		}
 
 		// build up transition graphs in respect to distance
 		for (int p = 0; p < trees.size() - 1; p++) {
 			exec.checkCanceled();
 			exec.setProgress((double) p / trees.size(), (p + 1) + " / "
 					+ (trees.size() + 1));
 			// System.out.println("Partition #" + (p+1));
 
 			KDTree<TrackedNode> tree = trees.get(p);
 			KDTree<TrackedNode> nextTree = trees.get(p + 1);
 
 			List<TrackedNode> unusedNodes = new LinkedList<TrackedNode>();
 			for (TrackedNode node : nextTree) {
 				unusedNodes.add(node);
 			}
 
 			Set<TrackedNode> usedSegments = new HashSet<TrackedNode>();
 
 			KDTree<TrackedNode>.KDTreeCursor cursor = tree.localizingCursor();
 			int count = 0;
 			Queue<TrackedNode> queue = new LinkedList<TrackedNode>();
 			addAll(queue, cursor);
 			while (!queue.isEmpty()) {
 				exec.checkCanceled();
 				TrackedNode node = queue.poll();
				if (node.getID().equals("113"))
					System.out.println("----113----");
 				if (usedSegments.contains(node))
 					continue;
 				count++;
 				usedSegments.add(node);
 				// search for all connected nodes ( dist <= radius )
 				// List<Node> connectedNodes = getConnectedNodes(tree, nextTree,
 				// node, m_distance.getDoubleValue(), usedSegments);
 				// System.out.println(connectedNodes);
 				TransitionGraph tg = new TransitionGraph();
 				// add both partitions - needed for 0->1 and 1 -> 0 graphs
 				tg.addPartition(tree.firstElement().getPartition());
 				tg.addPartition(nextTree.firstElement().getPartition());
 				createTransitionGraph(tree, nextTree, tg, node,
 						m_distance.getDoubleValue(), usedSegments);
				// System.out.println(tg + " " +
				// tg.getNodes(tg.getFirstPartition()) + " " +
				// tg.getNodes(tg.getLastPartition()));
 
 				unusedNodes.removeAll(tg.getNodes(tg.getLastPartition()));
 
 				// int variantCounter = 0;
 				// for(TransitionGraph tgvariant :
 				// TransitionGraph.createAllPossibleGraphs(tg)) {
 				// no more variants for now.
 				TransitionGraph tgvariant = tg;
 				DataCell[] cells = new DataCell[cont.getTableSpec()
 						.getNumColumns()];
 				cells[0] = new ImgPlusCellFactory(exec)
 						.createCell(TransitionGraphRenderer
 								.renderTransitionGraph(tg, baseImg, tgvariant));
 				cells[1] = new StringCell(tgvariant.toString());
 				cells[2] = new StringCell(tgvariant.toNodeString());
 				cells[3] = GraphCellFactory.createCell(tgvariant.getNet());
 				double[] distVec = FeatureProvider.getFeatureVector(tgvariant);
 				for (int i = 0; i < distVec.length; i++) {
 					cells[i + 4] = new DoubleCell(distVec[i]);
 				}
 				cont.addRowToTable(new DefaultRow(p + "#" + count /*
 																 * + ";" +
 																 * variantCounter
 																 */, cells));
 				// variantCounter++;
 
 				// debug
 				// for(String partition : tgvariant.getPartitions()) {
 				// for(Node n : tgvariant.getNodes(partition)) {
 				// System.out.println(n + " out: " + n.getOutgoingEdges());
 				// }
 				// }
 				// enddebug
 				// }
 			}
 
 			Set<TrackedNode> alreadyUsedNodes = new HashSet<TrackedNode>();
 			// nodes in second partition without possible match in first, create
 			// transition
 			for (TrackedNode node : unusedNodes) {
 				exec.checkCanceled();
 				//ignore nodes already used now
 				if(alreadyUsedNodes.contains(node)) continue;
 				TransitionGraph tg = new TransitionGraph();
 				// add both partitions - needed for 0->1 and 1 -> 0 graphs
 				tg.addPartition(tree.firstElement().getPartition());
 				tg.addPartition(nextTree.firstElement().getPartition());
 				// add node
 				node.createCopyIn(tg);
 
 				// look for other nodes in distance
 				for (TrackedNode node2 : unusedNodes) {
 					if (node2.equals(node))
 						continue;
 					if (node.distanceTo(node2) < m_distance.getDoubleValue()) {
 						node2.createCopyIn(tg);
 						alreadyUsedNodes.add(node);
 					}
 				}
 				count++;
 				DataCell[] cells = new DataCell[cont.getTableSpec()
 						.getNumColumns()];
 				cells[0] = new ImgPlusCellFactory(exec)
 						.createCell(TransitionGraphRenderer
 								.renderTransitionGraph(tg, baseImg, tg));
 				cells[1] = new StringCell(tg.toString());
 				cells[2] = new StringCell(tg.toNodeString());
 				cells[3] = GraphCellFactory.createCell(tg.getNet());
 				double[] distVec = FeatureProvider.getFeatureVector(tg);
 				for (int i = 0; i < distVec.length; i++) {
 					cells[i + 4] = new DoubleCell(distVec[i]);
 				}
 				cont.addRowToTable(new DefaultRow(p + "#" + count, cells));
 			}
 
 			// System.out.println("unused nodes: " + unusedNodes);
 		}
 		cont.close();
 		return exec.createBufferedDataTable(cont.getTable(), exec);
 	}
 
 	@Override
 	protected DataTableSpec getTableSpec(GraphPortObjectSpec viewSpec,
 			DataTableSpec tableSpec) throws InvalidSettingsException {
 		return createOutSpec();
 	}
 }
