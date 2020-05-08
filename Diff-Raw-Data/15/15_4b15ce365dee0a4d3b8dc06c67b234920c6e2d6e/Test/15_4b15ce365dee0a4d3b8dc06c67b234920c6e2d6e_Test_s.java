 /*
  * Copyright 2011 - 2012
  *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
  * 
  * This file is part of Organizations, a feature for GraphStream to manipulate
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
 package org.graphstream.organization;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.io.IOException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import org.graphstream.algorithm.Toolkit;
 import org.graphstream.graph.Graph;
 import org.graphstream.graph.implementations.AdjacencyListGraph;
 import org.graphstream.stream.file.FileSinkImages;
 import org.graphstream.stream.file.FileSourceDGS;
 import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
 import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
 import org.graphstream.stream.file.FileSinkImages.Quality;
 import org.graphstream.stream.file.FileSinkImages.Resolutions;
 import org.graphstream.ui.swingViewer.Viewer;
 
 public class Test implements OrganizationListener {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		FileSourceDGS dgs = new FileSourceDGS();
 		AdjacencyListGraph g = new AdjacencyListGraph("g");
 		OrganizationsGraph metaGraph = new OrganizationsGraph(g);
 
 		FileSinkImages images1 = new FileSinkImages();
 		images1.setQuality(Quality.HIGH);
 		images1.setOutputPolicy(OutputPolicy.BY_STEP);
 
 		FileSinkImages images2 = new FileSinkImages();
 		images2.setResolution(Resolutions.VGA);
 		images2.setQuality(Quality.HIGH);
 		images2.setOutputPolicy(OutputPolicy.BY_STEP);
 		images2.setLayoutPolicy(LayoutPolicy.COMPUTED_AT_NEW_IMAGE);
 		images2.setLayoutStepPerFrame(5);
 
 		// g.addSink(images1);
 		// metaGraph.addSink(images2);
 
 		// images1.begin("entities_");
 		// images2.begin("meta_");
 
 		metaGraph.getManager().setMetaIndexAttribute("meta.index");
 		// metaGraph.getManager().addOrganizationListener(new Test());
 
 		// g.display(false);
 		// metaGraph.display();
 		complexDisplay(g, metaGraph, metaGraph.getNodeOrganization(Toolkit
 				.randomNode(g).getId()));
 
		dgs.addSink(g);
		dgs.begin(Test.class.getResourceAsStream("test.dgs"));

		int step = 0;

		while (dgs.nextStep()) {
			Thread.sleep(100);
			//System.out.printf("step #%d\n", step++);
		}

 		//images1.end();
 		//images2.end();
 	}
 
 	protected static void complexDisplay(Graph g1, Graph g2, Graph g3) {
 		Viewer v1, v2, v3;
 		JFrame f1;
 
 		v1 = new Viewer(g1, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
 		v2 = new Viewer(g2, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
 		v3 = new Viewer(g3, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
 		// v2 = metaGraph.display();
 		// v3 = metaGraph.getNodeOrganization("A1").display();
 
 		v1.addDefaultView(false);
 		v1.getDefaultView().setPreferredSize(new Dimension(400, 400));
 		v2.addDefaultView(false);
 		v2.getDefaultView().setPreferredSize(new Dimension(200, 200));
 		v3.addDefaultView(false);
 		v3.getDefaultView().setPreferredSize(new Dimension(200, 200));
 
 		v1.enableAutoLayout();
 		v2.enableAutoLayout();
 		v3.enableAutoLayout();
 
 		f1 = new JFrame("Organization");
 		f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f1.getContentPane().setLayout(new BorderLayout());
 
 		JPanel dv1, dv2, dv3;
 		dv1 = new JPanel();
 		dv1.add(v1.getDefaultView());
 		dv1.setBackground(Color.WHITE);
 		dv2 = new JPanel();
 		dv2.add(v2.getDefaultView());
 		dv2.setBackground(Color.WHITE);
 		dv3 = new JPanel();
 		dv3.add(v3.getDefaultView());
 		dv3.setBackground(Color.WHITE);
 
 		JPanel right = new JPanel();
 		right.setLayout(new GridLayout(2, 1));
 		right.add(dv2);
 		right.add(dv3);
 
 		dv1.setBorder(BorderFactory.createLineBorder(Color.GRAY));
 		dv2.setBorder(BorderFactory.createLineBorder(Color.GRAY));
 		dv3.setBorder(BorderFactory.createLineBorder(Color.GRAY));
 
 		f1.getContentPane().add(dv1, BorderLayout.CENTER);
 		f1.getContentPane().add(right, BorderLayout.EAST);
 
 		f1.pack();
 		f1.setVisible(true);
 	}
 
 	public void connectionCreated(Object metaIndex1,
 			Object metaOrganizationIndex1, Object metaIndex2,
 			Object metaOrganizationIndex2, String connection) {
 		System.out.printf("- new connection [%s|%s] -- %s -- [%s|%s]\n",
 				metaIndex1, metaOrganizationIndex1, connection, metaIndex2,
 				metaOrganizationIndex2);
 	}
 
 	public void connectionRemoved(Object metaIndex1,
 			Object metaOrganizationIndex1, Object metaIndex2,
 			Object metaOrganizationIndex2, String connection) {
 		System.out.printf("- del connection [%s|%s] xx %s xx [%s|%s]\n",
 				metaIndex1, metaOrganizationIndex1, connection, metaIndex1,
 				metaOrganizationIndex1);
 	}
 
 	public void organizationChanged(Object metaIndex,
 			Object metaOrganizationIndex, ChangeType changeType,
 			ElementType elementType, String elementId) {
 		System.out.printf("* organization [%s|%s] changed : %s, %s, %s\n",
 				metaIndex, metaOrganizationIndex, changeType, elementType,
 				elementId);
 	}
 
 	public void organizationCreated(Object metaIndex,
 			Object metaOrganizationIndex, String rootNodeId) {
 		System.out.printf("* new organization [%s|%s] root @ %s\n", metaIndex,
 				metaOrganizationIndex, rootNodeId);
 	}
 
 	public void organizationMerged(Object metaIndex,
 			Object metaOrganizationIndex1, Object metaOrganizationIndex2,
 			String rootNodeId) {
 		System.out.printf(
 				"* organizations [%s|%s] and [%s|%s] merged, root @ %s\n",
 				metaIndex, metaOrganizationIndex1, metaIndex,
 				metaOrganizationIndex2, rootNodeId);
 	}
 
 	public void organizationRemoved(Object metaIndex,
 			Object metaOrganizationIndex) {
 		System.out.printf("* del organization [%s|%s]\n", metaIndex,
 				metaOrganizationIndex);
 	}
 
 	public void organizationRootNodeUpdated(Object metaIndex,
 			Object metaOrganizationIndex, String rootNodeId) {
 		System.out.printf("* root of [%s|%s] is %s\n", metaIndex,
 				metaOrganizationIndex, rootNodeId);
 	}
 
 	public void organizationSplited(Object metaIndex,
 			Object metaOrganizationBase, Object metaOrganizationChild) {
 		System.out.printf("* organization [%s|%s] splited, child is %s\n",
 				metaIndex, metaOrganizationBase, metaOrganizationChild);
 	}
 }
