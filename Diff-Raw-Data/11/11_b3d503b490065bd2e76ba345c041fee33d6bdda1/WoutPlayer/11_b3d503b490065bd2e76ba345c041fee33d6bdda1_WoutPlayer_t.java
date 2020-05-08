 package team153;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public class WoutPlayer extends NovaPlayer {
 
     public WoutPlayer(RobotController controller) {
         super(controller);
     }
     
     public void step() {
         MapLocation location = navigation.findNearestArchon();
         int distance = location.distanceSquaredTo(controller.getLocation());
        if(energon.isEnergonLow() || controller.getFlux() > 300) {
             if(distance < 3) {
                 energon.transferFlux(location);
                 energon.requestEnergonTransfer();
                 controller.yield();
             } else {
                 navigation.moveOnceTowardsLocation(location);
             }
         } else {
            if(distance > 20 || controller.getFlux() > 300) {
                 navigation.moveOnceTowardsLocation(location);
             } else {
                 Direction dir = navigation.getMoveableDirection(controller.getDirection());
                 navigation.moveOnceInDirection(dir);
             }
         }
 
         NovaMapData square = findSquareWithFlux();
     }
 
     public boolean tileSensedCallback(NovaMapData data) {
         return super.tileSensedCallback(data);
     }
 
     public NovaMapData findSquareWithFlux() {
         return null;
     }
 }
