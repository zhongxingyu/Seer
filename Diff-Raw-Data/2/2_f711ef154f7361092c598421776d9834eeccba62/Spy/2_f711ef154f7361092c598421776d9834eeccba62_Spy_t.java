 package finalBot;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bwapi.proxy.model.ROUnit;
 import org.bwapi.proxy.model.TilePosition;
 import org.bwapi.proxy.model.Unit;
 import org.bwapi.proxy.model.WeaponType;
 
 public class Spy {
 	Governor builder;
 	Commander attacker;
 	Map<TilePosition,Long> scouted; //maps areas scouted to time scouted?
 	Set<Unit> enemyUnits;
 	Unit myScout;
 	
 	Spy() {
 		scouted = new HashMap<TilePosition,Long>();
 		enemyUnits = new HashSet<Unit>();
 	}
 	
 	// grabs SCV from builder for scouting
 	private void assignScout(TilePosition tp) {
 		myScout = builder.pullWorker(tp);
 	}
 	
 	// returns SCV to builder or removes if scout destroyed
 	public void unassignScout() {
 		if(myScout.exists())
			builder.addWorker(myScout);
 		myScout = null;
 	}
 	
 	// scans nearby area of tp for enemies
 	public void scan(TilePosition tp) {
 		if(myScout == null)
 			assignScout(tp);
 		if(Tools.close((ROUnit) myScout, tp, myScout.getType().sightRange()/32)) {
 			return;
 		}
 		myScout.move(tp);
 	}
 	
 	// uses scout to find enemy
 	public void findEnemy() {
 	}
 	
 	//  
 	public void scoutEnemy() {
 	}
 
 	// remove buildings if not there anymore
 	public void updateEnemyUnits() {
 		Set<Unit> toRemove = new HashSet<Unit>();
 		for(Unit u : enemyUnits) {
 			if(!u.exists())
 				toRemove.add(u);
 		}
 		enemyUnits.removeAll(toRemove);
 	}
 	
 	// adds enemy unit to set
 	public void addEnemyUnit(Unit u) {
 		enemyUnits.add(u);
 	}
 	
 	// removes enemy from set
 	public void removeEnemyUnit(Unit u) {
 		enemyUnits.remove(u);
 	}
 	
 	// enemy armed air unit count
 	public int airForces() {
 		int airUnits = 0;
 		for(Unit u : enemyUnits) {
 			if(u.getType().isFlyer() && u.getType().canAttack())
 				airUnits++;
 		}
 		return airUnits;
  	}
 
 	// enemy armed ground unit count
 	public int groundForces() {
 		int groundUnits = 0;
 		for(Unit u : enemyUnits) {
 			if(!u.getType().isFlyer() && u.getType().canAttack())
 				groundUnits++;
 		}
 		return groundUnits;
 	}
 	
 	// enemy cloaked unit count
 	public int cloakedForces() {
 		int cloakedUnits = 0;
 		for(Unit u : enemyUnits) {
 			if(u.getType().isCloakable() || u.isBurrowed())
 				cloakedUnits++;
 		}
 		return cloakedUnits;
 	}
 	
 	// enemy melee attackers count
 	public int meleeForces() {
 		int meleeUnits = 0;
 		for(Unit u : enemyUnits) {
 			WeaponType weapon = u.getType().groundWeapon();
 			if(!weapon.equals(WeaponType.NONE) && weapon.maxRange() <= 15)
 				meleeUnits++;
 		}
 		return meleeUnits;
 	}
 	
 	// enemy ground ranged attackers count
 	public int rangedForces() {
 		int rangedUnits = 0;
 		for(Unit u : enemyUnits) {
 			WeaponType weapon = u.getType().groundWeapon();
 			if(!weapon.equals(WeaponType.NONE) && weapon.maxRange() > 15)
 				rangedUnits++;
 		}
 		return rangedUnits;
 	}
 	
 	// existence of a small force
 	public int smallForces() {
 		return 0;
 	}
 	
 	public int largeForces() {
 		return 0;
 	}
 	
 	public void setCommander(Commander c) {
 		attacker = c;
 	}
 	
 	public void setGovernor(Governor g) {
 		builder = g;
 	}
 	
 }
