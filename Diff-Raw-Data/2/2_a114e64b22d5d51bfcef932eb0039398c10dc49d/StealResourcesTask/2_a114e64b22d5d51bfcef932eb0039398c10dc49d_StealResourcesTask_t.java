 package planning;
 
 import faction.Faction;
 import universe.World;
 
 public class StealResourcesTask extends Task {
 
 	public StealResourcesTask(World world, Task parent) {
 		super(true, "Steal Resources Task", parent);
 		this.target = world.getControllingFaction();;
 		this.world = world;
 	}
 
 	@Override
 	public int stepsToCompletion(Faction faction) {
 		return 1;
 	}
 
 	@Override
 	public Task getNextStep(Faction faction) {
 		return this;
 	}
 
 	@Override
 	public boolean isCompleted(Faction faction) {
 		return parent.didFinish;
 	}
 
 	public void perform(Faction faction) {
 		System.out.println("Doing " + name);
 		if(random.nextBoolean()) //Spy was caught
 			world.exposeSpy(faction);
 		else {
 			int resourcesToTake = Math.min(target.getNumResources(), 100);
 			target.removeResources(resourcesToTake);
 			faction.addResources(resourcesToTake/2);
 			if(parent != null) parent.reportFinished(this);
 		}
 	}
 	
 	@Override
 	public boolean canPerform(Faction faction) {
 		return world.hasSpy(faction) && target.getNumResources() > 0;
 	}
 
 	@Override
 	public double getFlavorMatch(Faction faction) 
 	{
 		double resourceAdjustCap = 2000;
 		double selfAdjustFactor = (1 - (Math.min(faction.getNumResources(), resourceAdjustCap) / resourceAdjustCap)) * 0.5;
 		double enemyAdjustFactor = (Math.min(target.getNumResources(), resourceAdjustCap) / resourceAdjustCap) * 0.5;
 		return ((enemyAdjustFactor + selfAdjustFactor) * ((faction.getAggression() * 2 + faction.getDiplomacy()) / 3.0));
 	}
 
 }
