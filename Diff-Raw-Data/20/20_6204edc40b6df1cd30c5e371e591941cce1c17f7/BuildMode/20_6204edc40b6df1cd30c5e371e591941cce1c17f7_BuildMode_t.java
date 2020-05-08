 package vooga.towerdefense.controller.modes;
 
 import java.awt.Point;
 import vooga.towerdefense.controller.Controller;
import vooga.towerdefense.gameElements.GameElement;
 
 
 /**
  * A build mode used to aid in the placing and building of towers.
  * 
  * @author Jimmy Longley
 * @author Angelica Schwartz
  */
 public class BuildMode extends ControlMode {
 
    private GameElement myItemToBuild;
    
    public void setItemToBuild(GameElement item) {
        myItemToBuild = item;
    }
    
     public void handleMapClick (Point p, Controller controller) {
       controller.fixItemOnMap(myItemToBuild, p);
     }
 }
