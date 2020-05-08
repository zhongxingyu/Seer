 package cbdt.control.simulation.algorithm.dfskeeptree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import cbdt.control.simulation.algorithm.SimulationAlgorithm;
 import cbdt.model.parameters.engineconfig.DFSkeepTreeEngineConfig;
 import cbdt.model.result.Result;
 import cbdt.model.result.StageResult;
 
 public class DFStreeSimulationAlgorithm extends SimulationAlgorithm {
 
 	private static final int MIN_NUMBER_OF_MONITOR_WORK_UNITS = 10;
 	private DFSkeepTreeEngineConfig config;
 
 	private int monitoredStage;
 	
 	@Override
 	public void computeResult(Result result){
 		NodeContentFactory factory = new NodeContentFactory();
 		NodeContentKeepTree rootContent = factory.getInitRootContent(parameters);
 		NodeShellKeepTree rootShell = new NodeShellKeepTree(rootContent);
 
 		NodeShellVisitor nodeShellVisitor = new NodeShellVisitor(parameters, config, result, factory);
 		
 		List<NodeShellKeepTree> monitoredStageNodeShells = computeMonitoredStageNodeShells(
 				result, rootShell, nodeShellVisitor);
 		monitor.beginTask("Computation with DFSkeepTree", monitoredStageNodeShells.size());
 		
 		for(NodeShellKeepTree nodeShell : monitoredStageNodeShells){
 			nodeShellVisitor.visitRecursively(nodeShell, monitoredStage);
 			monitor.worked(1);
 		}
 	
 		if(config.isSaveTreeStructure())
 			result.setRootNode(rootShell);
 		monitor.done();
 	}
 
 	private List<NodeShellKeepTree> computeMonitoredStageNodeShells(Result result,
 			NodeShellKeepTree rootShell, NodeShellVisitor nodeShellVisitor) {
 		List<NodeShellKeepTree> stageNodeShells = new ArrayList<NodeShellKeepTree>();
 		stageNodeShells.add(rootShell);
 		monitoredStage = 0;
 		
		while(stageNodeShells.size()<MIN_NUMBER_OF_MONITOR_WORK_UNITS 
				&& monitoredStage<config.getNumberOfRequestedExpectedUtilityValues()){
 			List<NodeShellKeepTree> childrenShells = new ArrayList<NodeShellKeepTree>();
 			StageResult childrensStageResult = result.getStageResults().get(monitoredStage);
 			for(NodeShellKeepTree parentShell : stageNodeShells){
 				nodeShellVisitor.computeChildren(parentShell, childrensStageResult);
 				childrenShells.addAll(parentShell.getChildren());
 			}
 			stageNodeShells = childrenShells;
 			monitoredStage++;
 		}
 		return stageNodeShells;
 	}
 
 	public DFSkeepTreeEngineConfig getConfig() {
 		return config;
 	}
 
 	public void setConfig(DFSkeepTreeEngineConfig config) {
 		this.config = config;
 	}
 
 }
