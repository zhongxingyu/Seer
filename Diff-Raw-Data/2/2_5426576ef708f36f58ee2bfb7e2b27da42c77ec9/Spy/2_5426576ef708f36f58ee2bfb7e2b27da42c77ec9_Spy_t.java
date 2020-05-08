 package finalBot;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bwapi.proxy.model.Game;
 import org.bwapi.proxy.model.ROUnit;
 import org.bwapi.proxy.model.TechType;
 import org.bwapi.proxy.model.TilePosition;
 import org.bwapi.proxy.model.Unit;
 import org.bwapi.proxy.model.UnitType;
 import org.bwapi.proxy.model.WeaponType;
 
 import edu.berkeley.nlp.starcraft.util.UnitUtils;
 
 public class Spy extends Overseer {
 	private Set<TilePosition> scouted; //maps areas scouted to time scouted?
 	private Set<ROUnit> enemyUnits;
 	private Unit myScout;
 	private Unit comsat;
 	private TilePosition myHome; 
 	private static final int PIXEL_SCOUT_RANGE = 500;
 	private boolean retreat;
 	private boolean firstScout;
 	private boolean noFuture;
 	
 	public Spy() {
 		scouted = new HashSet<TilePosition>();
 		enemyUnits = new HashSet<ROUnit>();
 		myHome = Game.getInstance().self().getStartLocation();
 		retreat = false;
 		firstScout = true;
 		noFuture = false;
 	}
 	
 	// grabs SCV from builder for scouting
 	private void assignScout(TilePosition tp) {
 		myScout = builder.pullWorker(tp);
 		myScout.stop();
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
 			scouted.add(tp);
 			return;
 		}
 		myScout.move(tp);
 	}
 	
 	// uses scout to find enemy
 	public void findEnemy() {
 		if(!myScout.isIdle())
 			return;
 		for(TilePosition tp : Game.getInstance().getStartLocations()) {
 			if(!scouted.contains(tp)) {
 				scan(tp);
 				return;
 			}
 		}
 	}
 	
 	// scouts enemy's nearby potential expansions locations 
 	public void scoutEnemy() {
 		if(!myScout.isIdle())
 			return;
 		for(ROUnit u : enemyUnits) {
 			if(Game.getInstance().self().canSeeUnitAtPosition(u.getType(), u.getLastKnownPosition()) && !u.isVisible() && u.getType().isBuilding()) {
 				myScout.move(u.getLastKnownPosition());
 				enemyUnits.remove(u);
 				return;
 			}
 		}
 		scoutExpansions();
 	}
 	
 	private void scoutExpansions() {
 		List<TilePosition> potentialExpansions = new LinkedList<TilePosition>();
 		for(ROUnit u : Game.getInstance().getStaticGeysers()) {
 			if(Tools.close((Unit) u, enemyBases(), PIXEL_SCOUT_RANGE))
 				potentialExpansions.add(u.getLastKnownTilePosition());
 		}
 		for(TilePosition tp : potentialExpansions) {
 			if(!scouted.contains(tp)) {
 				scan(tp);
 				return;
 			}
 		}
 	}
 
 	// remove buildings if not there anymore
 	public void updateEnemyUnits() {
 		Set<ROUnit> toRemove = new HashSet<ROUnit>();
 		for(ROUnit u : enemyUnits) {
 			if(Game.getInstance().self().canSeeUnitAtPosition(u.getType(),u.getLastKnownPosition()) && !u.isVisible())
 				toRemove.add(u);
 		}
 		enemyUnits.removeAll(toRemove);
 	}
 	
 	// adds enemy unit to set
 	public void addEnemyUnit(ROUnit u) {
 		enemyUnits.add(u);
 	}
 	
 	// removes enemy from set
 	public void removeEnemyUnit(ROUnit unit) {
 		enemyUnits.remove(unit);
 	}
 	
 	public int getNumberOf(UnitType type){
 		int count = 0;
 		for(ROUnit u : enemyUnits){
 			if(u.getType()==type)
 				count++;
 		}
 		return count;
 	}
 	
 	public int getStaticDef(){
 		int count = 0;
 		for(ROUnit u : enemyUnits){
 			if(u.getType().isBuilding() && 
 					(u.getType().canAttack() || u.getType() == UnitType.TERRAN_BUNKER))
 				count++;
 		}
 		return count;
 	}
 	
 	public Set<ROUnit> getEnemyUnits(){
 		return enemyUnits;
 	}
 	
 	// enemy armed air unit count
 	public int airForces() {
 		int airUnits = 0;
 		for(ROUnit u : enemyUnits) {
 			if(u.getType().isFlyer() && u.getType().canAttack())
 				airUnits++;
 		}
 		return airUnits;
  	}
 
 	// enemy armed ground unit count
 	public int groundForces() {
 		int groundUnits = 0;
 		for(ROUnit u : enemyUnits) {
 			if(!u.getType().isFlyer() && u.getType().canAttack())
 				groundUnits++;
 		}
 		return groundUnits;
 	}
 	
 	// enemy cloaked unit count
 	public int cloakedForces() {
 		int cloakedUnits = 0;
 		for(ROUnit u : enemyUnits) {
 			if(u.getType().isCloakable() || u.isCloaked() || u.getType().isBurrowable())
 				cloakedUnits++;
 		}
 		return cloakedUnits;
 	}
 	
 	// enemy melee attackers count
 	public int meleeForces() {
 		int meleeUnits = 0;
 		for(ROUnit u : enemyUnits) {
 			WeaponType weapon = u.getType().groundWeapon();
 			if(!weapon.equals(WeaponType.NONE) && weapon.maxRange() <= 15)
 				meleeUnits++;
 		}
 		return meleeUnits;
 	}
 	
 	// enemy ground ranged attackers count
 	public int rangedForces() {
 		int rangedUnits = 0;
 		for(ROUnit u : enemyUnits) {
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
 	
 	public void act(){
 		scouted.add(myHome);
 		updateEnemyUnits();
 		if(comsat != null) {
 			if(enemyBuildings() > 0 && comsat.getEnergy() >= 50)
 				comsat.useTech(TechType.SCANNER_SWEEP,randomEnemyBuilding().getLastKnownPosition());
 		}
 		
 		// grab new scout if scout died or have no scout
 		if(myScout==null || !myScout.isVisible()) {
 			// only get scout if have enough scv's
 			if(UnitUtils.getAllMy(UnitType.TERRAN_SCV).size() >= 11 && !noFuture) 
 				assignScout(myHome);
 			else
 				return;
 		}
 		
 		
 		// no scout received from assign
 		if(myScout==null){
 			return;
 		}
 		
 		noFuture = true;
 		/*
 		// retreat if there are attacking ground units
 		if(!attackingGroundUnits().isEmpty()) {
 			int maxAtkRange = maxAttackingGroundRange();
 			if(Tools.close(myScout, attackingGroundUnits(), maxAtkRange)) {
 				if(retreat) return;
 				retreat = true;
 				myScout.move(myHome);
 				return;
 			} else if(retreat) {
 				retreat = false;
 				myScout.stop();
 				return;
 			}
 		}
 		*/
 		// stop start location scouting if buildings found
 		if(enemyBuildings() > 0)
 			firstScout = false;
 		
 		// find enemy start location
 		if(firstScout)
 			findEnemy();
 		/*
 		// enemy found, scout nearby expansions and units
 		else if(myScout.isIdle() || myScout.isStopped())
 			scoutEnemy();
 		*/
 		// done scouting, should try moving to enemy base if nothing to do
 		else if((myScout.isIdle() || myScout.isStopped()) && !enemyBases().isEmpty()) {
 			ROUnit target = Tools.findClosest(enemyBases(),myScout.getTilePosition());
 			if(target!=null)
 				myScout.move(target.getLastKnownPosition());
 		}
 		updateEnemyUnits();
 	}
 	
 	// enemy building count
 	public int enemyBuildings() {
 		int buildings = 0;
 		for(ROUnit u : enemyUnits) {
 			if(u.getType().isBuilding());
 				buildings++;
 		}
 		return buildings;
 	}
 	
 	private ROUnit randomEnemyBuilding() {
 		List<ROUnit> enemyBuildings = new LinkedList<ROUnit>();
 		for(ROUnit u : enemyUnits) {
 			if(u.getType().isBuilding())
 				enemyBuildings.add(u);
 		}
		return enemyBuildings.get((int) (Math.random()*enemyBuildings.size()));
 	}
 	
 	// list of units on ground, includes both buildings and forces
 	public List<ROUnit> enemyGroundUnits() {
 		List<ROUnit> groundUnits = new LinkedList<ROUnit>();
 		for(ROUnit u : enemyUnits) {
 			if(!u.getType().isFlyer())
 				groundUnits.add(u);
 		}
 		return groundUnits;
 	}
 	
 	// list of units on ground, includes both buildings and forces
 	public List<ROUnit> attackingGroundUnits() {
 		List<ROUnit> groundUnits = new LinkedList<ROUnit>();
 		for(ROUnit u : enemyUnits) {
 			if(!u.getType().isFlyer() && u.isAttacking())
 				groundUnits.add(u);
 		}
 		return groundUnits;
 	}
 	
 	// max atk range of ground units
 	public int maxGroundRange() {
 		int maxAtkRange = 15;
 		for(ROUnit u : enemyGroundUnits()) {
 			WeaponType ground_weapon = u.getType().groundWeapon();
 			if(ground_weapon == null)
 				continue;
 			maxAtkRange = Math.max(maxAtkRange, ground_weapon.maxRange());
 		}
 		return maxAtkRange;
 	}
 
 	// max atk range of attacking ground units
 	public int maxAttackingGroundRange() {
 		int maxAtkRange = 15;
 		for(ROUnit u : attackingGroundUnits()) {
 			WeaponType ground_weapon = u.getType().groundWeapon();
 			if(ground_weapon == null)
 				continue;
 			maxAtkRange = Math.max(maxAtkRange, ground_weapon.maxRange());
 		}
 		return maxAtkRange;
 	}
 	
 	public List<ROUnit> enemyBases() {
 		List<ROUnit> bases = new LinkedList<ROUnit>();
 		for(ROUnit u : enemyUnits) {
 			if(u.getType().isResourceDepot())
 				bases.add(u);
 		}
 		return bases;
 	}
 	
 	public void assignComSat(Unit cs) {
 		comsat = cs;
 	}
 }
