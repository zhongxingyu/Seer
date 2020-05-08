 package org.housered.simul.model.actor.brain;
 
 import org.housered.simul.model.location.Position;
 import org.housered.simul.model.location.PositionImpl;
 
 public class SimpleNavigationBrainImpl implements NavigationBrain
 {
     private Position currentPosition;
     private Position target;
     
     public SimpleNavigationBrainImpl()
     {
         currentPosition = new PositionImpl();
     }
     
     @Override
     public void setTarget(Position target)
     {
         this.target = target;
     }
 
     @Override
     public boolean hasTarget()
     {
         return target != null;
     }
 
     @Override
     public Position getNextPoint()
     {
         return target;
     }
     
     @Override
     public Position getPosition()
     {
         return currentPosition;
     }
 
 }
