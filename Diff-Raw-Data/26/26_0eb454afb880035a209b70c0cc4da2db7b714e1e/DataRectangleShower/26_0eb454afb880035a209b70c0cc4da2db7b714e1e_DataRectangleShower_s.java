 package cbdt.view.analysis.tree;
 
 import cbdt.control.simulation.algorithm.dfskeeptree.NodeContentKeepTree;
 import cbdt.model.parameters.engineconfig.DFSkeepTreeEngineConfig;
 
 public class DataRectangleShower {
 
 	private TreePApplet pApplet;
 	private DFSkeepTreeEngineConfig config;
 	
 	public DataRectangleShower(TreePApplet pApplet, DFSkeepTreeEngineConfig config) {
 		this.pApplet = pApplet;
 		this.config = config;
 	}
 	
 	public void showDataRectangle(NodeContentKeepTree contentToShow){
 		pApplet.fill(255);
 		boolean showLastAction = (contentToShow.getLastActionOutcome()!=null) && config.isSaveActionNames();
 		
		int rectangleHeight = 35;
		int offsetY = 0;			
 		if(showLastAction)
			offsetY = 30;
 		
		pApplet.rect(pApplet.mouseX+10, pApplet.mouseY+10, 220, rectangleHeight + offsetY);
 		pApplet.fill(0);
 
 		if(showLastAction){
 			String lastActionName = contentToShow.getLastActionOutcome().getAction().getActionName();
 			pApplet.text("Last action: "+lastActionName, pApplet.mouseX+30, pApplet.mouseY+25);
 			double lastUtility = contentToShow.getLastActionOutcome().getUtility();
 			pApplet.text("Utility last action: "+lastUtility, pApplet.mouseX+30, pApplet.mouseY+40);
 		}
 
		double aspLevel = contentToShow.getAspirationLevel();
		pApplet.text("Asp. level: "+aspLevel, pApplet.mouseX+30, pApplet.mouseY+25 + offsetY);
 
 		double probProd = contentToShow.getProbabilityProduct();
		pApplet.text("Prob.: "+probProd, pApplet.mouseX+30, pApplet.mouseY+40 + offsetY);
 
 	}
 }
