 package vooga.towerdefense.action.actionlist;
 
 import vooga.towerdefense.action.TargetedAction;
 import vooga.towerdefense.factories.GameElementFactory;
import vooga.towerdefense.factories.examplesfactories.ExampleDosProjectileFactory;
 import vooga.towerdefense.gameElements.GameElement;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.util.Location;
 
 /**
  * Creates a projectile aimed at a target, target needs to be predefined by FindTarget action.
  * 
  * @author Matthew Roy
  * @author Zhen Gou
  * @author Xu Rui
  */
 public class LaunchProjectile extends TargetedAction {
 
 	private GameElementFactory myProjectileFactory;
 	private Location myStart;
 	private GameMap myMap;
	private boolean isLaunched;
 
     public LaunchProjectile (GameMap map, Location startLocation, GameElementFactory projectileFactory) {
     	setTargetTracking(true);
     	myProjectileFactory = projectileFactory;
     	myStart = startLocation;
     	myMap = map;
     }
 
     /**
      * Creates a projectile with action of shooting at targets. 
      * Needs to figure out way to pass target information (one per follow up action or all targets for all follow up actions etc.)
      * 
      * @param elapsedTime 
      */
     @Override
     public void executeAction (double elapsedTime) {
        
         for (GameElement target : getTargets()) {
             GameElement projectile = myProjectileFactory.createElement(myStart, target);
         }
         
    	
    	//System.out.println(getTargets().size());
    	GameElement projectile = myProjectileFactory.createElement(myStart);
    	if (!isLaunched){
        myMap.addGameElement(projectile);
    	}
         //System.out.println(projectile.getTargetedActions().size()+ 10);
        for (TargetedAction t: projectile.getTargetedActions()){
             t.updateTargetedFollowUpActions(getTargets());
        }
        isLaunched = true;
     }
     
 }
