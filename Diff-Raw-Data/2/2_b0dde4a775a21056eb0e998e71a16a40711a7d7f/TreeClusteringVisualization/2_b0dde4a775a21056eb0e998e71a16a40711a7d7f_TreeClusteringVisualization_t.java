 package mesquite.treecmp.clustering.TreeClusteringVisualization;
 
 import mesquite.lib.CommandChecker;
 import mesquite.lib.MesquiteModule;
 import mesquite.lib.MesquiteWindow;
 import mesquite.lib.duties.FileAssistantT;
 import mesquite.treeSetViz.TreeSetVisualization.TreeSetVisualization;
 import mesquite.treecmp.Utils;
 import mesquite.treecmp.clustering.ClusterAssignmentTreeColoring.ClusterAssignmentTreeColoring;
 
 public class TreeClusteringVisualization extends FileAssistantT {
 	@Override
 	public boolean startJob(String arguments, Object condition,
 			boolean hiredByName) {
 		//Getting tree set visualization module and posting command to it's window is rather a hack
 		//But it allows to avoid modifications in tsv code
 		if (!isTsvAvailable()) {
 			return sorry(getName() + " requires Tree Set Visualization module to be installed.");
 		}
 		final MesquiteModule visualization = Utils.hireExactImplementation(this, TreeSetVisualization.class);
 		if (visualization == null) {
 			return sorry("Could not start tree set visualization module.");
 		}
 		MesquiteWindow window = visualization.getModuleWindow();
 		final String clusterColoringModuleName = "#" + ClusterAssignmentTreeColoring.class.getName();
 		window.doCommand("setTreeScoreColorer", clusterColoringModuleName, new CommandChecker());
		return true;
 	}
 
 	@Override
 	public String getName() {
 		return "Tree Clustering Analysis";
 	}
 
 	private boolean isTsvAvailable() {
 		final String TSVClassName = "mesquite.treeSetViz.TreeSetVisualization.TreeSetVisualization";
 		try {
 			Class.forName(TSVClassName);
 		} catch (ClassNotFoundException e) {
 			return false;
 		}
 		return true;
 	}
 }
