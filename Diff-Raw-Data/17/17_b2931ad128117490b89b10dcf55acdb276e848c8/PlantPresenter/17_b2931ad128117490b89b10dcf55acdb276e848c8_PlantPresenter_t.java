 package simulator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 public class PlantPresenter {
 
 	public Plant model;
 	
 	public PlantPresenter(Plant model)
 	{
 		this.model = model;		
 	}
 	
 	public void saveState(String filename){
 		// write serialised Plant to file?
 	}
 	
 	public void loadState(String filename) {
 		// read plant object from file
 	}
 	
 	public void updatePlant() {
 		List<PlantComponent> plantComponents = model.getPlantComponents();
 		for (PlantComponent plantComponent : plantComponents) {
 			plantComponent.updateState();
 		}
 
 		List<Repair> beingRepaired = model.getBeingRepaired();
 		for (Repair repair : beingRepaired) {
 			repair.decTimeStepsRemaining();
 			int timeStepsRemaining = repair.getTimeStepsRemaining();
 			if(timeStepsRemaining <= 0) {
 				//remove from beingRepaired and add to plantComponents
 			}
 		}
 		// Go through all components and call updateState()
 		// This will do things in Reactor and Condenser objects etc.
 	}
 	
	public void repairComponent(PlantComponent toBeRepairedComponent) { // name of component to be repaired
		List<PlantComponent> failedComponents = model.getFailedComponents(); 
		List<Repair> beingRepairedComponents = model.getBeingRepaired();
		if (failedComponents.contains(toBeRepairedComponent)) {
			Repair repair = new Repair(toBeRepairedComponent, toBeRepairedComponent.getRepairTime());
			beingRepairedComponents.add(repair);
			failedComponents.remove(toBeRepairedComponent);
 		}
 	}
 	
 	public void checkFailures() {
 		List<PlantComponent> temp;		
 		temp = new ArrayList<PlantComponent>();
 		
 		for (int i = 0; i<model.plantComponents.size(); i++)
 		{
 			if(model.plantComponents.get(i).checkFailure() == true)
 				temp.add(model.plantComponents.get(i));
 		}
 		int NUMBER_FAILED = temp.size();
 		if(NUMBER_FAILED > 0 ) {
 			Random random = new Random();
 			int selection = random.nextInt(NUMBER_FAILED);
 			String failed = temp.get(selection).toString();
 			for (int x = 0; x<model.plantComponents.size(); x++)
 			{
 				if(model.plantComponents.get(x).toString().equals(failed.toString())) { // code to specify element of <plantComponents>, toggle its operational state, remove it from <plantComponents> and add it to <failedComponents>
 					model.plantComponents.get(x).setOperational(false);
 					model.failedComponents.add(model.plantComponents.get(x));
 					model.plantComponents.remove(model.plantComponents.get(x));
 					break;			
 				}
 			}
 		}		
 	}
 	
 	public void togglePaused() {
 		
 	}
 	
 	public void calcSystemFlow() {
 		// Complex shit!
 	}
 	
 }
