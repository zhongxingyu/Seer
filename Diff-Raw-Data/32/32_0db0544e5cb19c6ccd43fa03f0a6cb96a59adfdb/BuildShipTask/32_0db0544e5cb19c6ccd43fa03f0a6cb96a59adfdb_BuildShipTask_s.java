 package HTNP;
 
 import Faction.Faction;
 
 public class BuildShipTask extends Task {
 	
 	private int limit;
 	public BuildShipTask(int limit) {
		super(true, "Build Ship Task " + limit);
 		this.limit = limit;
 	}
 	
 	@Override
 	public int stepsToCompletion(Faction faction) {
 		return faction.getNumArmies() - faction.getNumShips();
 	}
 
 	@Override
 	public Task getNextStep(Faction faction) {
 		return this;
 	}
 
 	@Override
 	public boolean isCompleted(Faction faction) {
 		return faction.getNumShips() >= limit;
 	}
 
 	@Override
 	public boolean canPerform(Faction faction) {
 		return faction.getNumResources() + faction.getNumShips() >= limit;
 	}
 	
 	public void perform(Faction faction) {
 		System.out.println("Doing " + name);
 		faction.setNumResources(faction.getNumResources() - 1);
 		faction.setNumShips(faction.getNumShips() + 1);
 	}
 
 	@Override
 	public double getFlavorMatch(Faction faction) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
