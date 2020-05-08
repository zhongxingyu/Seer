 package simulator;
 
 public class PlantPresenter {
 
 	private Plant model;
 	
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
 		for (int i = 0; i<model.plantComponents.size(); i++) {
 			model.plantComponents.get(i).updateState();
 		}
 		for (int z = 0; z<model.beingRepaired.size(); z++) {
 			model.beingRepaired.get(i).decTimeStepsRemaning();
 			int temp = model.beingRepaired.get(i).getTimeStepsRemaining();
 			if(temp == 0) {
 				//remove from beingRepaired and add to plantComponents
 				
 			}
 		}
 			
 		// Go through all components and call updateState()
 		// This will do things in Reactor and Condenser objects etc.
 	}
 	
 	public void repairComponent(String name) { // name of component to be repaired
 		List<PlantComponents> temp = model.getFailedComponents(); 
 		for(int i = 0; i<temp.size(); i++) {
 			if(temp.getName().equals(name))
 			{
 				model.beingRepaired.add(temp.get(i));
 				model.failedComponents.remove(model.failedComponents.get(i)); 
 				break;
 			}
 		}
 	}
 	
 	public void checkFailures() {
 		List<PlantComponent> temp;		
 		temp = new ArrayList<PlantComponent>();
 		
 		for (int i = 0; i<model.plantComponents.size(); i++)
 		{
 			if(model.plantComponents.get(i).checkFailures() = true)
 				temp.add(plantComponents.get(i));
 		}
 		int NUMBER_FAILED = temp.size();
 		if(NUMBER_FAILED > 0 ) {
 			Random random = new Random();
 			int selection = random.nextInt(NUMBER_FAILED);
 			String failed = temp.get(selection).getName();
 			for (int x = 0; x<model.plantComponents.size(); x++)
 			{
 				if(model.plantComponents.get(x).getName.equals(failed)) { // code to specify element of <plantComponents>, toggle its operational state, remove it from <plantComponents> and add it to <failedComponents>
 					model.plantComponents.get(x).setOperational(false);
 					model.failedComponents.add(plantComponents.get(x));
					plantComponents.remove(plantComponents.get(x));
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
