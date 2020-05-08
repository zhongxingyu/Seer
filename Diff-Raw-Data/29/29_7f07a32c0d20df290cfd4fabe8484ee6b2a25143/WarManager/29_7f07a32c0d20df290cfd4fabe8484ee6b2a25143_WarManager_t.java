 package GameEngine;
 
 import java.awt.Point;
 import java.util.ArrayList;
 
 import Dispatcher.*;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - WarManager</b></br>
  * <p>The WarManager is the class responsible for the interactions between Towers on the map
  * and Units that traveled on.</br>
  * His job is to create and possess the ArmyManager and to the TowerManager and to transmit
  * all the informations from to the other manager.
  * </p> 
  * <b>Creation :</b> 22/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see ArmyManager
  * @see TowerManager
  */
 
 public class WarManager {
 
   /**
    * Constructor of the WarManager class
    */
   public WarManager() {
 	  
   }
   /**
    * Function that handle the meetings between towers and units
    * @param armyManager
    * @param towerManager
    * @see WarManager#beginEncounters(ArmyManager, TowerManager)
    * @see WarManager#terminateEncounters(ArmyManager, TowerManager)
    */
   public void war(ArmyManager armyManager, TowerManager towerManager, DispatcherManager dispatcher, Long playingTime){
 	  beginEncounters(armyManager, towerManager, dispatcher, playingTime);
 	  terminateEncounters(armyManager, towerManager);
 	  cleanUpBattlefield(armyManager, towerManager, dispatcher);
   }
   
   /**
    * Function that tell the meeting of a unit and a tower
    * @param TowerManager
    * @param ArmyManager
    * @see GameManager#timer()
    */
   public void beginEncounters(ArmyManager armyManager, TowerManager towerManager, DispatcherManager dispatcher, Long playingTime) {
 	  if(armyManager.getUnits().isEmpty() == false && towerManager.getTowers().isEmpty() == false){
       	for(Unit unit:armyManager.getUnits()){
 
      		 //Browse all towers
      		 for(Tower tower:towerManager.getTowers()){
      			 
      			 int x = unit.getPosition().x;
      			 int y = unit.getPosition().y;
      			 int centerX = tower.getPosition().x;
      			 int centerY = tower.getPosition().y;
      			 int range = tower.getRange();
      			 
      			 //The unit (x,y) is in the area of the tower(centerX, centerY) if : (x - centerX)^2 + (y - centerY)^2 < range^2
      			 if(((x - centerX)*(x - centerX) + (y - centerY)*(y - centerY)) < range*range){
      				 int currentIdCount = GameManager.idCount;
     							 
      				 //So active the tower
     				if( towerManager.activeTower(tower, unit, playingTime, currentIdCount)){
     					GameManager.idCount ++;
     					//Tell the view to create a missile  
     					System.out.println("Engine - Add missile "+currentIdCount);
     					dispatcher.addOrderToView(new AddMissileOrder(currentIdCount, tower.getPlayerType(), tower.getPosition(), tower.isAreaDamages()));
     				}
     			}
      		 }
      	 }
 	  }
   	}
 
 	  /**
 	   * Desactive Towers if no units is in there range
 	   * @param armyManager
 	   * @param towerManager
 	   * @see GameManager#timer()
 	   */
 public void terminateEncounters(ArmyManager armyManager, TowerManager towerManager){
 	if(armyManager.getUnits().isEmpty() == false && towerManager.getActivatedTowers().isEmpty() == false){
 		//Browse activated towers
       	for(Tower tower:towerManager.getActivatedTowers()){
       		//Browse all units
       		for(Unit unit:armyManager.getUnits()){
 
         			 int x = unit.getPosition().x;
         			 int y = unit.getPosition().y;
         			 int centerX = tower.getPosition().x;
         			 int centerY = tower.getPosition().y;
         			 int range = tower.getRange();
         			 
         			 //The unit (x,y) is in the area of the tower(centerX, centerY) if : (x - centerX)^2 + (y - centerY)^2 < range^2
         			 if(((x - centerX)*(x - centerX) + (y - centerY)*(y - centerY)) > range*range){
         				 
         				 //So desactive the tower
         				 towerManager.desactiveTower(tower);
         			 }
         		 }
         	 }
      		 
      	 }
   	}
 	/**
 	 * Move a missile 
 	 * @param armyManager
 	 * @param towerManager
 	 * @return true if the missile need to be move / false if the missile have reached it's target
 	 * @see Missile#searchForTarget()
 	 */
 	public boolean moveMissile(Missile missile){
 		
 		//For each pixel until the speed value, make the missile search it's target*/
 		int i=0;
 		for(i=0; i< ( missile.getSpeed() ); ++i){
 			
 			if(!missile.searchForTarget()){
 				break;
 			}
 		}
 		
 		//If the previous for was interrupted, pass the message
 		if(i!=missile.getSpeed())return false;
 		else return true;
 		
 	}
 	
 	public ArrayList<Unit> areaDamagesTarget(Missile missile, ArmyManager armyManager){
 		ArrayList<Unit> targets = new ArrayList<Unit>();
 		
 		int range = missile.getOrigin().getRange();
 		int centerX = missile.getPosition().x;
 		int centerY = missile.getPosition().y;
 		
 		 //Browse all units
 		 for(Unit unit:armyManager.getUnits()){
 			 
 			 int x = unit.getPosition().x;
 			 int y = unit.getPosition().y;
 			 
 			 //The unit (x,y) is in the targeted area if : (x - centerX)^2 + (y - centerY)^2 < range^2
 			 if(((x - centerX)*(x - centerX) + (y - centerY)*(y - centerY)) < range*range){
 				 
 				 //Store the unit as a target
 				 targets.add(unit);
 			 }
 		 }
     	 
 		return targets;
 	}
 	
 	
 	/**
 	 * Suppress units that have taken too much damages and suppress missiles that targeted suppressed units
 	 * @param towerManager
 	 * @param armyManager
 	 * @see WarManager#war(ArmyManager, TowerManager, Long)
 	 * @see GameManager#timer()
 	 */
 	public void cleanUpBattlefield(ArmyManager armyManager, TowerManager towerManager, DispatcherManager dispatcher){
 		
 		//Browse units to find those who are dead
 		for(Unit unit:armyManager.getUnits()){
 			if(unit.getAmount()<=0){
 				
 				//Suppress the missiles that targeted this unit
 				for(Missile missile:towerManager.getMissiles()){
 					if(missile.getTarget().getId() == unit.getId()){
 						
 						//Tell the view to suppress the missile
 						dispatcher.addOrderToView(new MoveMissileOrder(missile.getId(), new Point(-1, -1) ));
 						towerManager.suppressMissile(missile);
 						break;
 					}
 				}
 				//Tell the view to suppress the unit
 				dispatcher.addOrderToView(new MoveUnitOrder(unit.getId(), new Point(-1, -1)));
 				armyManager.suppressUnit(unit);
 				break;
 			}
 		}
 	}
 
 }
