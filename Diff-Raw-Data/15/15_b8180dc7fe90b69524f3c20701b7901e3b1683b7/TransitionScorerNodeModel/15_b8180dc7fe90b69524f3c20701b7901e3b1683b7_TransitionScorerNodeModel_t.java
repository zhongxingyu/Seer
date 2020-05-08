 package org.knime.knip.tracking.nodes.transition.transitionScorer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.knime.core.data.DataCell;
 import org.knime.core.data.DataRow;
 import org.knime.core.data.DataTableSpec;
 import org.knime.core.data.container.DataContainer;
 import org.knime.core.data.def.DefaultRow;
 import org.knime.core.node.BufferedDataTable;
 import org.knime.core.node.CanceledExecutionException;
 import org.knime.core.node.ExecutionContext;
 import org.knime.core.node.ExecutionMonitor;
 import org.knime.core.node.InvalidSettingsException;
 import org.knime.core.node.NodeModel;
 import org.knime.core.node.NodeSettingsRO;
 import org.knime.core.node.NodeSettingsWO;
 import org.knime.knip.base.node.NodeUtils;
 import org.knime.knip.tracking.data.graph.TransitionGraph;
 import org.knime.network.core.knime.cell.GraphValue;
 
 /**
  * This is the model implementation of TransitionScorer. Judges the classified
  * transition results and get the optimal version of each transition graph
  * 
  * @author Stephan Sellien
  */
 public class TransitionScorerNodeModel extends NodeModel {
 
 	/**
 	 * Constructor for the node model.
 	 */
 	protected TransitionScorerNodeModel() {
 		super(1, 1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
 			final ExecutionContext exec) throws Exception {
 
 		String currentTgId = "";
 		Set<TransitionGraph> graphs = new HashSet<TransitionGraph>();
 		int tgIndex = NodeUtils.firstCompatibleColumn(
 				inData[0].getDataTableSpec(), GraphValue.class);
 
 		DataContainer cont = new DataContainer(createOutspec());
 
 		for (DataRow row : inData[0]) {
 			if (row.getKey().getString().contains(";")) {
 				System.out.println("tg: " + row.getKey());
 				String tgId = row.getKey().getString().split(";")[0];
 				if (tgId.equals(currentTgId)) {
 					graphs.add(new TransitionGraph(((GraphValue) row
 							.getCell(tgIndex)).getView()));
 				} else {
 					if (graphs.size() > 0) {
 						//TransitionGraph tg = merge(graphs);
						System.out.println("adding [regular]" + currentTgId);
 						cont.addRowToTable(new DefaultRow(currentTgId, new DataCell[0]));
 					}
 					currentTgId = tgId;
 					// start a new set
 					graphs.clear();
 					graphs.add(new TransitionGraph(((GraphValue) row
 							.getCell(tgIndex)).getView()));
 				}
 			} else {
				System.out.println("adding [else] " + row.getKey());
 				cont.addRowToTable(new DefaultRow(row.getKey(), new DataCell[0]));
 			}
 		}
 		
 		//add last tg
 		if (graphs.size() > 0) {
 //			TransitionGraph tg = merge(graphs);
			System.out.println("adding [last]" + currentTgId);
 			cont.addRowToTable(new DefaultRow(currentTgId, new DataCell[0]));
 		}
 		cont.close();
 
 		return new BufferedDataTable[] { exec.createBufferedDataTable(
 				cont.getTable(), exec) };
 
 		// ColumnRearranger rearranger = createColumnRearranger(inData[0]
 		// .getDataTableSpec());
 		//
 		// return new BufferedDataTable[] { exec.createColumnRearrangeTable(
 		// inData[0], rearranger, exec) };
 	}
 
 	private DataTableSpec createOutspec() {
 		return new DataTableSpec();
 	}
 
 //	private TransitionGraph merge(Set<TransitionGraph> graphs) {
 //		if (graphs.size() == 0)
 //			return null;
 //		else
 //			return graphs.iterator().next();
 //	}
 
 //	private ColumnRearranger createColumnRearranger(DataTableSpec dataTableSpec) {
 //		ColumnRearranger rearranger = new ColumnRearranger(dataTableSpec);
 //		rearranger.append(new SingleCellFactory(new DataColumnSpecCreator(
 //				"Transition score", DoubleCell.TYPE).createSpec()) {
 //
 //			@Override
 //			public DataCell getCell(DataRow row) {
 //				return new DoubleCell(new Random().nextDouble());
 //			}
 //		});
 //		return rearranger;
 //	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void reset() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
 			throws InvalidSettingsException {
 //		return new DataTableSpec[] { createColumnRearranger(inSpecs[0])
 //				.createSpec() };
 		return new DataTableSpec[]{createOutspec()};
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void saveSettingsTo(final NodeSettingsWO settings) {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
 			throws InvalidSettingsException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void validateSettings(final NodeSettingsRO settings)
 			throws InvalidSettingsException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void loadInternals(final File internDir,
 			final ExecutionMonitor exec) throws IOException,
 			CanceledExecutionException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void saveInternals(final File internDir,
 			final ExecutionMonitor exec) throws IOException,
 			CanceledExecutionException {
 	}
 
 }
