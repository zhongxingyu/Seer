 package GameEngine;
 
 import java.awt.Point;
 import java.util.ArrayList;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - TowerManager</b></br>
  * <p>The TowerManager is the class responsible for the Towers management in positionning
  * and in leading attacks.</br>
  * It has access to the Player's Towers and know the Unit's positions.</br>
  * It is also responsible for the creation of a new Tower according to the territoryMap,
  * and for the evolution of the Towers.
  * </p> 
  * <b>Creation :</b> 22/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see WarManager
  * @see Tower
  * 
  */
 
 public class TowerManager {
   /**
    * Associate all the Towers in game to the Players that owns them in thread safe Map. 
    */
   private ArrayList<Tower> towers;
   
   private ArrayList<Tower> activatedTowers;
   
   private ArrayList<Missile> missiles;
 
   /**
    * List all the the types of towers. There are two families of towers :
    * <ul><li>the AttackTower family, that attack your enemies</li>
    * <li>the SupportTower family, that supports your units</li></ul>
    *@see Tower
    */
   public enum TowerTypes{
 	  NOTOWER, //replace the ancient zero in the view
 	  ATTACKTOWER,
 	  SUPPORTTOWER,
 	  GUNTOWER,
 	  FROSTTOWER,
 	  BOMBTOWER,
 	  LAZERTOWER,
 	  MEDICALTOWER,
 	  SHIELDTOWER
   }
   
   /**
    * Constructor of the TowerManager class
    */
   public TowerManager() {
 	  super();
 	  towers = new ArrayList<Tower>();
 	  activatedTowers = new ArrayList<Tower>();
 	  missiles = new ArrayList<Missile>();
   }
   /**
    * Set the selected tower active because of the proximity of a enemy Unit.
    * @param toActiveTower
    * @param unit - target
    * @see WarManager
    */
  public boolean activeTower(Tower toActiveTower, Unit unit, Long date, int currentIdCount) {
 	  for(Tower tower:towers){
 		  if(tower.getId() == toActiveTower.getId()){
 			  
 			  //TODO multiple targets
 			  long currentTime = GameManager.getTime();
 			  //long diff = currentTime-date;
 
 			  if(toActiveTower.getLastShootingTime() == 0 || (currentTime-toActiveTower.getLastShootingTime())>=toActiveTower.getCadency()){
				  missiles.add(toActiveTower.shoot(unit, date, currentIdCount)); 
				  return true;
 			  }
 		  }
 	  }
	  return false;
   }
   /**
    * Set the selected Tower inactive because no Unit's in the neighborhood.
    * @param toDesactiveTower
    * @see WarManager
    */
   public void desactiveTower(Tower toDesactiveTower) {
 	  for(Tower tower:activatedTowers){
 		  if(tower.getId() == toDesactiveTower.getId()){
 			  toDesactiveTower.stop();
 		  }
 	  }
   }
  /**
   * Create a Tower of the given type
   * @param playerType
   * @param towerType
   * @param position
   */
   public void createTower(int id, Player.PlayerType playerType, TowerTypes towerType, Point position) {
 	  Tower tower = null;
 	  
 	  switch(towerType){
 	  	case ATTACKTOWER:
 	  		tower = new AttackTower(id, position, playerType);
 	  		break;
 	  	case SUPPORTTOWER :
 	  		tower = new SupportTower(id, position, playerType);
 	  		break;
 	  	case GUNTOWER :
 	  		tower = new GunTower(id, position, playerType);
 	  		break;
 	  	case FROSTTOWER :
 	  		tower = new FrostTower(id, position, playerType);
 	  		break;
 	  	case BOMBTOWER :
 	  		tower = new BombTower(id, position,playerType);
 	  		break;
 	  	case LAZERTOWER :
 	  		tower = new LazerTower(id, position, playerType);
 	  		break;
 	  	case MEDICALTOWER :
 	  		tower = new MedicalTower(id, position, playerType);
 	  		break;
 	  	case SHIELDTOWER :
 	  		tower = new ShieldTower(id, position, playerType);
 	  		break;
 	  	default :
 	  		break;
 	  }  
 	  
 	  towers.add(tower);
   }
 /**
  * Make the tower evolve if it is permitted, create the evolution and suppress the ancient tower
  * @param towerToEvolve
  * @param type
  * @param idCount
  * @see GameManager#execute()
  */
   public void evolveTower(int id, TowerTypes type, int idCount){
 	  //Browse all the towers
 	  for(Tower tower:towers){
 		  if(tower.getId() == id){
 			  //Test if the evolution type is allowed
 			  if(tower.getFirstEvolution() == type || tower.getSecondEvolution() == type){
 				  
 				  //Create a new Tower with a new ID
 				  createTower(idCount, tower.getPlayerType(), type, tower.getPosition());
 
 				  //Suppress the ancient one
 				  towers.remove(tower);
 				  break;
 			  }
 		  }
 	  }
   }
   /**
    * Remove a tower from the game
    * @param position
    * @see GameManager#execute()
    */
   public void suppressTower(int id){
 	  
 	  //Search the tower by it's position
 	  for(Tower tower: towers){
 		  if(tower.getId()==id){
 			  towers.remove(tower);
 			  break;
 		  }
 	  }
   }
   /**
    * Suppress the given missile
    * @param toSuppressMissile
    */
   public void suppressMissile(Missile toSuppressMissile){
 	  
 	  for(Missile missile:missiles){
 		  if(missile.getTarget() == toSuppressMissile.getTarget()){
 			  missiles.remove(missile);
 			  break;
 		  }
 	  }
   }
 
   /**
    * Getter - return the current tower list
    * @return
    */
   public ArrayList<Tower> getTowers(){
 	  return this.towers;
   }
   
   /**
    * Getter - return the current activated tower list
    * @return
    */
   public ArrayList<Tower> getActivatedTowers(){
 	  return this.activatedTowers;
   }
   /**
    * Getter - return the missiles list
    * @return
    */
   public ArrayList<Missile> getMissiles(){
 	  return this.missiles;
   }
 }
