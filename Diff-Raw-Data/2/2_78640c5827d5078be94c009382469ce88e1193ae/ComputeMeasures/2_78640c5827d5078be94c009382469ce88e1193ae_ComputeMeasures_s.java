 /*
  * Copyright 2011 - 2012
  *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
  * 
  * This file is part of organic, a feature for GraphStream to manipulate
  * organizations in a dynamic graph.
  * 
  * This program is free software distributed under the terms of two licenses, the
  * CeCILL-C license that fits European law, and the GNU Lesser General Public
  * License. You can  use, modify and/ or redistribute the software under the terms
  * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
  * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
  */
 package org.graphstream.organic.measure;
 
 import org.graphstream.algorithm.DynamicAlgorithm;
 import org.graphstream.algorithm.AlgorithmComputationTrigger;
 import org.graphstream.algorithm.AlgorithmComputationTrigger.Mode;
 import org.graphstream.algorithm.measure.ConnectivityMeasure;
 import org.graphstream.graph.Graph;
 import org.graphstream.graph.Node;
 import org.graphstream.graph.implementations.AdjacencyListGraph;
 import org.graphstream.organic.Organization;
 import org.graphstream.organic.OrganizationManager;
 import org.graphstream.organic.OrganizationManagerFactory;
 import org.graphstream.organic.OrganizationsGraph;
 import org.graphstream.organic.Validation;
 import org.graphstream.stream.Sink;
 import org.graphstream.stream.file.FileSinkDGS;
 import org.graphstream.stream.file.FileSourceDGS;
 
 public class ComputeMeasures implements DynamicAlgorithm {
 	Graph g;
 	OrganizationManager manager;
 	Sink trigger;
 
 	public ComputeMeasures() {
 		trigger = new AlgorithmComputationTrigger(Mode.BY_STEP, this);
 	}
 
 	public void init(OrganizationManager manager) {
 		init(manager.getEntitiesGraph());
 		this.manager = manager;
 
 		g.addAttribute("organic.measures", "step", "averageCentroid",
 				"averageVertexConnectivity", "averageEdgeConnectivity",
 				"averageSize");
 
 		g.addAttribute("organic.measures.averageCentroid.y-axis", 0);
 		g.addAttribute("organic.measures.averageCentroid.dim", 4);
 		g.addAttribute("organic.measures.averageCentroid.label", "step", "min",
 				"avg", "max");
 
 		g.addAttribute("organic.measures.averageVertexConnectivity.y-axis", 1);
 		g.addAttribute("organic.measures.averageVertexConnectivity.dim", 4);
 		g.addAttribute("organic.measures.averageVertexConnectivity.label",
 				"step", "min", "avg", "max");
 
 		g.addAttribute("organic.measures.averageEdgeConnectivity.y-axis", 1);
 		g.addAttribute("organic.measures.averageEdgeConnectivity.dim", 2);
 		g.addAttribute("organic.measures.averageEdgeConnectivity.label",
 				"step", "min", "avg", "max");
 
 		g.addAttribute("organic.measures.averageSize.y-axis", 2);
 		g.addAttribute("organic.measures.averageSize.dim", 4);
 		g.addAttribute("organic.measures.averageSize.label", "step", "min",
 				"avg", "max");
 	}
 
 	public void init(Graph g) {
 		this.g = g;
 		this.g.addSink(trigger);
 	}
 
 	public void terminate() {
 		g.removeSink(trigger);
 		g = null;
 	}
 
 	public void compute() {
 		g.addAttribute("organic.measures.averageCentroid.data",
 				averageCentroid());
 
 		g.addAttribute("organic.measures.averageVertexConnectivity.data",
 				averageVertexConnectivity());
 
 		g.addAttribute("organic.measures.averageEdgeConnectivity.data",
 				averageEdgeConnectivity());
 
		g.addAttribute("organic.measures.averageVertexConnectivity.data",
 				averageSize());
 	}
 
 	public double[] averageCentroid() {
 		double min, avg, max;
 
 		avg = 0;
 		min = Double.MAX_VALUE;
 		max = Double.MIN_VALUE;
 
 		if (manager.getOrganizationCount() > 0) {
 			double c;
 
 			for (Organization org : manager) {
 				c = 0;
 
 				for (Node n : org.getEachNode()) {
 					if (n.hasAttribute("centroid")
 							&& n.getAttribute("centroid").equals("true"))
 						c++;
 				}
 
 				min = Math.min(min, c);
 				max = Math.max(max, c);
 				avg = avg + c;
 			}
 
 			avg /= manager.getOrganizationCount();
 		} else {
 			min = 0;
 			max = 0;
 		}
 
 		return new double[] { g.getStep(), min, avg, max };
 	}
 
 	public double[] averageVertexConnectivity() {
 		double min, avg, max;
 
 		avg = 0;
 		min = Double.MAX_VALUE;
 		max = Double.MIN_VALUE;
 
 		if (manager.getOrganizationCount() > 0) {
 			for (Organization org : manager)
 				avg += ConnectivityMeasure.getVertexConnectivity(org);
 
 			avg /= manager.getOrganizationCount();
 		} else {
 			min = 0;
 			max = 0;
 		}
 
 		return new double[] { g.getStep(), min, avg, max };
 	}
 
 	public double[] averageEdgeConnectivity() {
 		double min, avg, max;
 
 		avg = 0;
 		min = Double.MAX_VALUE;
 		max = Double.MIN_VALUE;
 
 		if (manager.getOrganizationCount() > 0) {
 			for (Organization org : manager)
 				avg += ConnectivityMeasure.getEdgeConnectivity(org);
 
 			avg /= manager.getOrganizationCount();
 		} else {
 			min = 0;
 			max = 0;
 		}
 
 		return new double[] { g.getStep(), min, avg, max };
 	}
 
 	public double[] averageSize() {
 		double sum = 0;
 		double min, max;
 
 		min = Double.MAX_VALUE;
 		max = Double.MIN_VALUE;
 
 		if (manager.getOrganizationCount() > 0) {
 			min = Double.MAX_VALUE;
 			max = Double.MIN_VALUE;
 
 			for (Organization org : manager) {
 				sum += org.getNodeCount();
 				min = Math.min(min, org.getNodeCount());
 				max = Math.max(max, org.getNodeCount());
 			}
 
 			sum /= manager.getOrganizationCount();
 		} else {
 			min = 0;
 			max = 0;
 		}
 
 		return new double[] { g.getStep(), min, sum, max };
 	}
 
 	public static void main(String... args) throws Exception {
 		System.setProperty(Validation.PROPERTY, "none");
 		System.setProperty(OrganizationManagerFactory.PROPERTY,
 				"plugins.replay.ReplayOrganizationManager");
 
 		String what = "replayable.dgs_centroid.dgs";
 
 		FileSourceDGS dgs = new FileSourceDGS();
 		FileSinkDGS dgsOut = new FileSinkDGS();
 		AdjacencyListGraph g = new AdjacencyListGraph("g");
 		OrganizationsGraph metaGraph = new OrganizationsGraph(g);
 
 		
 		g.addSink(dgsOut);
 		dgsOut.begin(what + "_measures.dgs");
 		
 		ComputeMeasures measures = new ComputeMeasures();
 		measures.init(metaGraph.getManager());
 
 		dgs.addSink(g);
 		dgs.begin(what);
 
 		while (dgs.nextStep())
 			System.out.printf("step#%.0f\n", g.getStep());
 
 		dgs.end();
 	}
 }
