 package planning;
 
 import faction.Faction;
 import settings.Globals;
 import universe.World;
 
 public class BuildShipTask extends Task {
 	
 	private World world;
 	private int limit;
 	public BuildShipTask(World world, int limit, Task parent) {
 		super(true, "Build Ship Task", parent);
 		this.limit = Math.min(limit, 100);
 		this.world = world;
 	}
 	
 	@Override
 	public int stepsToCompletion(Faction faction) {
 		return limit - faction.getNumShips();
 	}
 
 	@Override
 	public Task getNextStep(Faction faction) {
 		if(faction.getNumResources() + faction.getNumShips()*Globals.SHIP_RESOURCE_BUILD_COST >= limit*Globals.SHIP_RESOURCE_BUILD_COST)
 			return this;
 		else
 			return new GatherResourcesTask((limit - faction.getNumShips(world))*Globals.SHIP_RESOURCE_BUILD_COST - faction.getNumResources(), this);
 	}
 
 	@Override
 	public boolean isCompleted(Faction faction) {
 		return faction.getNumShips() >= limit;
 	}
 
 	@Override
 	public boolean canPerform(Faction faction) {
 		if(parent == null) 
			return faction.getNumResources() > (limit - faction.getNumShips(world))*Globals.SHIP_RESOURCE_BUILD_COST;
 		else
 			return true;
 	}
 	
 	public void perform(Faction faction) {
 		System.out.println("Doing " + name);
 		faction.removeResources(Globals.SHIP_RESOURCE_BUILD_COST);
 		faction.increaseShips(1, world);
 	}
 
 	@Override
 	public double getFlavorMatch(Faction faction) 
 	{
 		return (faction.getAggression() + faction.getDiplomacy() + faction.getScience()) / 3.0;
 	}
 
 }
