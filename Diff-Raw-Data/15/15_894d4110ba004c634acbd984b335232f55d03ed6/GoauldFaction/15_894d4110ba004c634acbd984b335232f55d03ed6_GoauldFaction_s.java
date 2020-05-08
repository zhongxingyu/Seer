 package faction;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import faction.Reputation.ReputationLevel;
 
 import planning.*;
 import universe.*;
 
 public class GoauldFaction extends Faction 
 {
 	public GoauldFaction()
 	{
 		factionName = "Goa'uld";
 		
 		aggression = 80;
 		diplomacy = 20;
 		science = 50;
 		
 		World hw = Universe.generateWorld();
 		hw.setName("Chulak");
 		
 		setHomeWorld(hw);
 		
 		increaseTroops(15);
 		increaseShips(5);
 		
 		tech.offensiveCapabilities = 1;
 		tech.defensiveCapabilities = 1;
 		tech.resourceEfficiency = .1;
 		//tech.hyperdriveEfficiency = .5;
 	}
 	
 	public void doTurn()
 	{
 		boolean allDefeated = true;
 		for(Faction f : getEnemies())
 		{
 			if(!f.isDefeated())
 				allDefeated = false;
 		}
 		
 		// Pick another fight.
 		if(allDefeated)
 		{
 			Faction newEnemy = null;
 			
 			for(Faction f : Universe.factions)
 			{
 				if(!isEnemy(f) && !f.isDefeated())
 				{
 					if(newEnemy == null || f.getDefenseStrength(f.getHomeWorld()) < newEnemy.getDefenseStrength(newEnemy.getHomeWorld()))
 						newEnemy = f;
 				}
 			}
 			
 			double repChange = getReputationNumber(newEnemy) - ReputationLevel.ENEMY.threshold;
 			decreaseReputation(newEnemy, repChange);
 		}
		System.out.println(homeWorld.getTroopCount(this));
 		super.doTurn();
 	}
 	
 	protected ArrayList<Task> getTaskList() {
 		ArrayList<Task> taskList = new ArrayList<Task>();
 		for(Faction enemy : getEnemies()) {
 			System.out.println(enemy);
 			for(World to : enemy.getControlledWorlds()) {
 				for(World from : getControlledWorlds()) {
 					Task attack = new AttackTask(from, to, enemy, 
 							new Random().nextInt(100) + 50, null);
 					if(attack.canPerform(this))
 						taskList.add(attack);
 				}
 			}
 		}
 		
 		for(Faction faction : Universe.factions) {
 			if(faction != this) {
 				for(World world : faction.getControlledWorlds()) {
 					Task sabotage = new SabotageTask(world, null);
 					if(sabotage.canPerform(this))
 						taskList.add(sabotage);
 				}
 			}
 		}
 		Task defend = new DefendTask(getHomeWorld(), null);
 		
 		Task research = new ResearchTask(null);
 		
 		Task raiseMorale = new RaiseMoraleTask(null);
 		
 		if(defend.canPerform(this))
 			taskList.add(defend);
 		
 		if(research.canPerform(this))
 			taskList.add(research);
 		
 		if(raiseMorale.canPerform(this))
 			taskList.add(raiseMorale);
 		
 		return taskList;
 	}
 	
 	public boolean didWin() 
 	{
 		for(Faction faction : Universe.factions) 
 		{
 			if(!faction.isDefeated())
 				return false;
 		}
 		
 		return true;
 	}
 }
