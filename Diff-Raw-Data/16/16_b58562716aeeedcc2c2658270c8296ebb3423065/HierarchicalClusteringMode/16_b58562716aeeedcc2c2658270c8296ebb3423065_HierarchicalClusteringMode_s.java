 package org.clustering.mode.modes;
 
 import java.util.Date;
 import java.util.HashSet;
 
 import org.clustering.classifier.HierarchicalClassifier;
 import org.clustering.data.DataUtil;
 import org.clustering.mode.AbstractMode;
 import org.clustering.model.HierarchicalCluster;
 import org.clustering.util.VisualisationUtil;
 
 public class HierarchicalClusteringMode extends AbstractMode {
 
 	public HierarchicalClusteringMode() {
 		super("-hierarchical", "HAC");
 	}
 
 	
 	public void _runCreateCluster() throws Exception {
 		DataUtil dataUtil = new DataUtil();
		dataUtil.readData(false);
 		System.out.println("Start hierarchical clustering: " + new Date());
 		HierarchicalClassifier classifier = new HierarchicalClassifier();
 		HierarchicalCluster root = classifier.createHierarchicalCluster(dataUtil.getItems());
 		System.out.println("End hierarchical clustering: " + new Date());
 
 		System.out.println("Start writing result file " + new Date());
 		System.out.println("Items in root: " + root.getItems().size());
 		System.out.println("items in first child: " + root.getFirstChild().getItems());
 		
		VisualisationUtil util = new VisualisationUtil(new HashSet<String>(), "C:/Users/Otto/Desktop/bla.png");
 		util.drawHierarchicalCluster(root);
 		
 	}
 	
 }
