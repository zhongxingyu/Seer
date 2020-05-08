 /*
  * #%L
  * Cyni Implementation (cyni-impl)
  * $Id:$
  * $HeadURL:$
  * %%
  * Copyright (C) 2006 - 2013 The Cytoscape Consortium
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.cytoscape.cyni.internal.inductionAlgorithms.K2Algorithm;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.cytoscape.cyni.*;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.CyNetworkViewFactory;
 import org.cytoscape.view.model.CyNetworkViewManager;
 import org.cytoscape.view.vizmap.VisualMappingManager;
 import org.cytoscape.work.TaskIterator;
 import org.cytoscape.work.TunableSetter;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyNetworkTableManager;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.CyNetworkFactory;
 import org.cytoscape.model.subnetwork.CyRootNetworkManager;
 
 
 /**
  * The BasicInduction provides a very simple Induction, suitable as
  * the default Induction for Cytoscape data readers.
  */
 public class K2Induction extends AbstractCyniAlgorithm {
 	
 	
 	private CyTable selectedTable;
 	/**
 	 * Creates a new BasicInduction object.
 	 */
 	public K2Induction() {
 		super("k2","Bayesian - K2", true, CyniCategory.INDUCTION);
 	
 	}
 
 	public TaskIterator createTaskIterator(Object context,CyTable table, CyNetworkFactory networkFactory, CyNetworkViewFactory networkViewFactory,
 			CyNetworkManager networkManager,CyNetworkTableManager netTableMgr, CyRootNetworkManager rootNetMgr,VisualMappingManager vmMgr,
 			CyNetworkViewManager networkViewManager, CyLayoutAlgorithmManager layoutManager, CyCyniMetricsManager metricsManager) {
 		selectedTable = table;
 		return new TaskIterator(new K2InductionTask(getName(),(K2InductionContext) context, networkFactory,networkViewFactory,
 					networkManager,netTableMgr,rootNetMgr,vmMgr,networkViewManager,layoutManager,metricsManager, selectedTable));
 	}
 	
 	public Object createCyniContext(CyTable table, CyCyniMetricsManager metricsManager, TunableSetter tunableSetter,Map<String, Object> mparams) {
 		Object context;
 		selectedTable = table;
 		List<String> listTypes = new ArrayList<String>();
 		listTypes.add(CyniMetricTypes.LOCAL_METRIC_SCORE.toString());
		metricsManager.setDefaultCyniMetric("Bayesian Metric");
 		context = new K2InductionContext(supportsSelectedOnly(), selectedTable, metricsManager.getAllCyniMetricsWithType(listTypes),metricsManager);
 		if(mparams != null && !mparams.isEmpty())
 			tunableSetter.applyTunables(context, mparams);
 		return context;
 	}
 	/**
 	 * Returns true if the layout supports only applying the layout to selected nodes.
 	 *
 	 * @return True if the layout supports only applying the layout to selected nodes.
 	 */
 	public boolean supportsSelectedOnly() {
 		return true;
 	}
 }
