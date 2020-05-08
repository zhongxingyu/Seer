 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * 
  * @author dmyersturnbull
  */
 package org.structnetalign;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.structnetalign.ReportGenerator.DegenerateSetEntry;
 import org.structnetalign.ReportGenerator.UpdateTableEntry;
 import org.structnetalign.cross.CrossingManager;
 import org.structnetalign.cross.SimpleCrossingManager;
 import org.structnetalign.merge.ConcurrentBronKerboschMergeManager;
 import org.structnetalign.merge.MergeManager;
 import org.structnetalign.merge.MergeUpdate;
 import org.structnetalign.util.EdgeTrimmer;
 import org.structnetalign.util.EdgeWeighter;
 import org.structnetalign.util.GraphInteractionAdaptor;
 import org.structnetalign.util.GraphMLAdaptor;
 import org.structnetalign.util.IdentifierMapping;
 import org.structnetalign.util.IdentifierMappingFactory;
 import org.structnetalign.util.InteractionUpdate;
 import org.structnetalign.util.NetworkUtils;
 import org.structnetalign.weight.SimpleWeightCreator;
 import org.structnetalign.weight.SmarterWeightManager;
 import org.structnetalign.weight.WeightCreator;
 import org.structnetalign.weight.WeightManager;
 
 import psidev.psi.mi.xml.model.EntrySet;
 import edu.uci.ics.jung.graph.UndirectedGraph;
 import edu.uci.ics.jung.graph.util.Pair;
 
 /**
  * The main class of Struct-NA. Performs 3 steps:
  * <ol>
  * <li>Weighting: identifies homologs</li>
  * <li>Crossing: updates the probabilities of interactions that are conserved across homologs</li>
  * <li>Merging: merges degenerate vertex sets into a single representative per set</li>
  * </ol>
  * Reads an input MIF25 network and generates an output MIF25 network after performing the above 3 steps.
  * 
  * @author dmyersturnbull
  * @see CLI
  */
 public class PipelineManager {
 
 	public static final int N_CORES = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
 
 	public static final double TAU = 0.5;
 	public static final int XI = 2;
 	public static final double ZETA = 0.7;
 	private static final Logger logger = LogManager.getLogger("org.structnetalign");
 
 	private CrossingManager crossingManager;
 	private MergeManager mergeManager;
 
 	private int nCores;
 	private boolean noCross;
 	private boolean noMerge;
 	private WeightCreator phi;
 	private boolean report = false;
 	private double tau = TAU;
 
 	private WeightManager weightManager;
 	private boolean writeSteps = false;
 	private Integer xi; // depends on CrossingManager
 
 	private double zeta = ZETA;
 
 	public boolean isNoCross() {
 		return noCross;
 	}
 
 	public boolean isNoMerge() {
 		return noMerge;
 	}
 
 	public boolean isReport() {
 		return report;
 	}
 
 	public boolean isWriteSteps() {
 		return writeSteps;
 	}
 
 	/**
 	 * Runs the entire pipeline.
 	 */
 	public void run(File input, File output) {
 
 		int startTime = (int) (System.currentTimeMillis() / 1000L);
 
 		init();
 
 		// handle reporting
 		// always do this even if we're not generating the report, since MergeManager etc. needs it
 		String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
 		String path = output.getParent() + File.separator + "report-" + timestamp + File.separator;
 		if (report || writeSteps) new File(path).mkdir(); // don't make a new directory if we're not reporting
 		File reportFile = new File(path + "report.html");
 		ReportGenerator.setInstance(new ReportGenerator(reportFile));
 		if (report) {
 			ReportGenerator.getInstance().put("n_cores", nCores);
 			if (phi != null) ReportGenerator.getInstance().put("phi", phi.getClass().getSimpleName());
 			ReportGenerator.getInstance().put("tau", tau);
 			ReportGenerator.getInstance().put("zeta", zeta);
 			if (xi != null) ReportGenerator.getInstance().put("xi", xi);
 		}
 
 		// make a trimmer
 		EdgeWeighter<HomologyEdge> weighter = new EdgeWeighter<HomologyEdge>() {
 			@Override
 			public double getWeight(HomologyEdge e) {
 				return e.getWeight();
 			}
 		};
 		EdgeTrimmer<Integer, HomologyEdge> trimmer = new EdgeTrimmer<>(weighter);
 
 		CleverGraph graph;
 		{
 			// build the graph
 			EntrySet entrySet = NetworkUtils.readNetwork(input);
 			UndirectedGraph<Integer, InteractionEdge> interaction = GraphInteractionAdaptor.toGraph(entrySet);
 			graph = new CleverGraph(interaction);
 
 			// assign weights
 			Map<Integer, String> uniProtIds = NetworkUtils.getUniProtIds(entrySet);
 			weightManager.assignWeights(graph, uniProtIds);
 		}
 		System.gc();
 
 		// trim with tau
 		trimmer.trim(graph.getHomology(), tau);
 
 		if (report) {
 			ReportGenerator.getInstance().saveWeighted(graph);
 		}
 		if (writeSteps) {
 			GraphMLAdaptor.writeHomologyGraph(graph.getHomology(), new File(path + "hom_weighted.graphml.xml"));
 			GraphMLAdaptor.writeInteractionGraph(graph.getInteraction(), new File(path + "int_weighted.graphml.xml"));
 		}
 
 		// cross
 		if (!noCross) {
 			crossingManager.cross(graph);
 		}
 
 		// trim with zeta
 		trimmer.trim(graph.getHomology(), zeta);
 
 		// report progress
 		if (writeSteps) {
 			GraphMLAdaptor.writeHomologyGraph(graph.getHomology(), new File(path + "hom_crossed.graphml.xml"));
 			GraphMLAdaptor.writeInteractionGraph(graph.getInteraction(), new File(path + "int_crossed.graphml.xml"));
 		}
 		if (report) {
 			ReportGenerator.getInstance().saveCrossed(graph);
 		}
 
 		// merge
 		List<MergeUpdate> merges = null;
 		if (!noMerge) {
 			merges = mergeManager.merge(graph);
 		}
 
 		// report progress
 		if (writeSteps) {
 			GraphMLAdaptor.writeHomologyGraph(graph.getHomology(), new File(path + "hom_merged.graphml.xml"));
 			GraphMLAdaptor.writeInteractionGraph(graph.getInteraction(), new File(path + "int_merged.graphml.xml"));
 		}
 		if (report) {
 			ReportGenerator.getInstance().saveMerged(graph);
 		}
 
 		// just to free up memory
 		crossingManager = null;
 		mergeManager = null;
 		trimmer = null;
 
 		// now output
 		Map<Integer, Integer> interactionsRemoved = new WeakHashMap<>();
 		Map<Integer, Integer> interactorsRemoved = new WeakHashMap<>();
		if (merges != null) {
			for (MergeUpdate update : merges) {
				for (InteractionEdge e : update.getInteractionEdges()) {
					interactionsRemoved.put(e.getId(), update.getV0());
				}
				for (int v : update.getVertices()) {
					interactorsRemoved.put(v, update.getV0());
				}
 			}
 		}
 		EntrySet entrySet = NetworkUtils.readNetwork(input);
 		List<InteractionUpdate> updates = GraphInteractionAdaptor.modifyProbabilites(entrySet, graph.getInteraction(),
 				interactionsRemoved, interactorsRemoved);
 		NetworkUtils.writeNetwork(entrySet, output);
 
 		int endTime = (int) (System.currentTimeMillis() / 1000L);
 		if (report) {
 			putUpdates(updates);
 			putMerges(merges, entrySet);
 			ReportGenerator.getInstance().put("time_taken", endTime - startTime);
 			ReportGenerator.getInstance().write();
 		}
 
 		int count = Thread.activeCount() - 1;
 		if (count > 0) {
 			logger.warn("There are " + count + " lingering threads. Exiting anyway.");
 			System.exit(0);
 		}
 	}
 
 	public void setCrossingManager(CrossingManager crossingManager) {
 		this.crossingManager = crossingManager;
 	}
 
 	public void setMergeManager(MergeManager mergeManager) {
 		this.mergeManager = mergeManager;
 	}
 
 	public void setNCores(int nCores) {
 		this.nCores = nCores;
 	}
 
 	public void setNoCross(boolean noCross) {
 		this.noCross = noCross;
 	}
 
 	public void setNoMerge(boolean noMerge) {
 		this.noMerge = noMerge;
 	}
 
 	public void setPhi(WeightCreator phi) {
 		this.phi = phi;
 	}
 
 	public void setReport(boolean report) {
 		this.report = report;
 	}
 
 	/**
 	 * @param tau
 	 *            The minimum threshold to apply to homology edges before doing crossing.
 	 */
 	public void setTau(double tau) {
 		this.tau = tau;
 	}
 
 	public void setWeightManager(WeightManager weightManager) {
 		this.weightManager = weightManager;
 	}
 
 	public void setWriteSteps(boolean writeSteps) {
 		this.writeSteps = writeSteps;
 	}
 
 	/**
 	 * @param xi
 	 *            The maximum search depth for traversal during crossing.
 	 */
 	public void setXi(int xi) {
 		this.xi = xi;
 	}
 
 	/**
 	 * @param zeta
 	 *            The minimum threshold to apply to homology edges before doing merging
 	 */
 	public void setZeta(double zeta) {
 		this.zeta = zeta;
 	}
 
 	/**
 	 * Initializes a new PipelineManager using the default parameters. Once this has been performed, setting of
 	 * variables will have no effect.
 	 */
 	private void init() {
 		if (nCores == 0) nCores = Runtime.getRuntime().availableProcessors() - 1;
 		if (weightManager == null) {
 			if (xi == null) xi = XI;
 			if (phi == null) phi = new SimpleWeightCreator();
 			SmarterWeightManager weightManager = new SmarterWeightManager(phi, nCores);
 			this.weightManager = weightManager;
 		}
 		if (crossingManager == null) {
 			crossingManager = new SimpleCrossingManager(nCores, xi);
 		}
 		if (mergeManager == null) {
 			mergeManager = new ConcurrentBronKerboschMergeManager(nCores);
 		}
 	}
 
 	private void putMerges(List<MergeUpdate> merges, EntrySet entrySet) {
 		Map<Integer, String> uniProtIds = NetworkUtils.getUniProtIds(entrySet);
 		final IdentifierMapping mapping = IdentifierMappingFactory.getMapping();
 		List<DegenerateSetEntry> entries = new ArrayList<>();
 		for (MergeUpdate update : merges) {
 			final int v0 = update.getV0();
 			DegenerateSetEntry entry = new DegenerateSetEntry();
 			entry.v0 = update.getV0();
 			entry.uniProtId0 = uniProtIds.get(v0);
 			for (int v : update.getVertices()) {
 				if (v == v0) continue; // for reporting, we only want non-representative degenerate vertices
 				entry.getIds().add(v);
 				final String uniProtId = uniProtIds.get(v);
 				entry.getUniProtIds().add(uniProtId);
 				entry.getPdbIds().add(mapping.uniProtToPdb(uniProtId));
 				entry.getScopIds().add(mapping.uniProtToScop(uniProtId));
 			}
 			for (InteractionEdge edge : update.getInteractionEdges()) {
 				entry.getInteractionIds().add(edge.getId());
 			}
 			entries.add(entry);
 		}
 		if (!entries.isEmpty()) {
 			ReportGenerator.getInstance().put("degenerate_sets", entries);
 		}
 	}
 
 	private void putUpdates(List<InteractionUpdate> updates) {
 		NumberFormat nf = new DecimalFormat(); // we do it this way because our code knows better, weirdly
 		nf.setMaximumFractionDigits(3);
 		final IdentifierMapping mapping = IdentifierMappingFactory.getMapping();
 		List<UpdateTableEntry> updated = new ArrayList<>();
 		for (InteractionUpdate update : updates) {
 			if (update.isRemoved()) continue;
 			UpdateTableEntry entry = new UpdateTableEntry();
 			entry.interaction = update.getEdge().getId();
 			entry.before = Double.parseDouble(nf.format(update.getInitialProbability()));
 			entry.after = Double.parseDouble(nf.format(update.getEdge().getWeight()));
 			entry.ids = update.getIds();
 			entry.uniProtIds = update.getUniProtIds();
 			String pdb1 = mapping.uniProtToPdb(update.getUniProtIds().getFirst());
 			pdb1 = pdb1.substring(0, pdb1.length() - 2);
 			String pdb2 = mapping.uniProtToPdb(update.getUniProtIds().getSecond());
 			pdb2 = pdb2.substring(0, pdb2.length() - 2);
 			entry.pdbIds = new Pair<String>(pdb1, pdb2);
 			String scop1 = mapping.uniProtToScop(update.getUniProtIds().getFirst());
 			String scop2 = mapping.uniProtToScop(update.getUniProtIds().getSecond());
 			entry.scopIds = new Pair<String>(scop1, scop2);
 			updated.add(entry);
 		}
 		if (!updated.isEmpty()) {
 			ReportGenerator.getInstance().put("updated", updated);
 		}
 	}
 
 }
