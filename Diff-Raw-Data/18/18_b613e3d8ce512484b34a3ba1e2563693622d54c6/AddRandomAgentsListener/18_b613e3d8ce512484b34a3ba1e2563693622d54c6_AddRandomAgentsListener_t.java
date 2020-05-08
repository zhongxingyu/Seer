 package controler.mainwindow.functionalPanels.simuEnvironment;
 
 import java.util.Random;
 
 import model.backbone.agent.Agent;
 import model.backbone.building.helpers.Point;
import model.backbone.utils.CollisionUtils;
 
 import resources.GUIResources;
 import resources.SimulationResources;
 import resources.SimulationState;
 import controler.mainwindow.functionalPanels.ClickAction;
 
 public class AddRandomAgentsListener implements ClickAction {
 
 	@Override
 	public void act() {
 
 		if (SimulationResources.simulationState == SimulationState.Stopped) {
 			GUIResources.drawDanger = false;
 			GUIResources.drawAgent = false;
 
 			int max = GUIResources.randomAgentsSliderValue;
 			for (int i = 0; i < max; i++) {
 				addRandomAgent();
 			}
 			GUIResources.mapPanel.repaint();
 			
 			GUIResources.setSuccesMessage(GUIResources.randomAgentsSliderValue + " Agents added");
 		} else {
 			GUIResources.setErrorMessage("You cannot add agents while simulation is running");
 		}
 
 	}
 
 	public void addRandomAgent() {
 		Random rand = new Random();
 		boolean success = false;
 		int x,y,f;
 		while(!success){
 			x = rand.nextInt(SimulationResources.building.getResolutionX());
 			y = rand.nextInt(SimulationResources.building.getResolutionY());
 			f = rand.nextInt(SimulationResources.building.getFloors().size());
 			
			if (CollisionUtils.canCreateAgentAtPoint(new Point(x,y), f)) {
				Agent a = new Agent(new Point(x, y), f);				
				SimulationResources.building.getAgents().add(a);	
				success = true;
			}
 			
 		}
 	}
 
 }
