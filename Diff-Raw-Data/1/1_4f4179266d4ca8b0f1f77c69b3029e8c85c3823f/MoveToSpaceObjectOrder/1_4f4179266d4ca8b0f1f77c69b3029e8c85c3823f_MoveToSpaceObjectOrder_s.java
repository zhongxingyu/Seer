 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 package com.newdawn.model.ships.orders;
 
 import com.newdawn.controllers.utils.ShipUtils;
 import com.newdawn.model.ships.Squadron;
 import com.newdawn.model.system.SpaceObject;
 
 import javafx.beans.property.ReadOnlyStringProperty;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.geometry.Point2D;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *
  * @author Pierrick Puimean-Chieze
  */
 public class MoveToSpaceObjectOrder extends MoveOrder {
 
     private SimpleStringProperty shortDescriptionProperty;
     private SimpleStringProperty longDescriptionProperty;
     private static Log LOG = LogFactory.getLog(MoveToSpaceObjectOrder.class);
 
     public MoveToSpaceObjectOrder(SpaceObject destination, Squadron squadron) {
         super(destination, squadron);
     }
 
     @Override
     public void applyOrder() {
         LOG.trace("order [" + getShortDescription() + "] applied to squadron [" + getSquadron().
                 getName() + "]");
         getSquadron().setDestination(getDestination());
         setApplied(true);
     }
 
     @Override
     public ReadOnlyStringProperty shortDescriptionProperty() {
         if (shortDescriptionProperty == null) {
             shortDescriptionProperty = new SimpleStringProperty("Move To: " + getDestination().
                     getName());
         }
         return shortDescriptionProperty;
     }
 
     @Override
     public ReadOnlyStringProperty longDescriptionProperty() {
         if (longDescriptionProperty == null) {
             longDescriptionProperty = new SimpleStringProperty("Following this order, the task group " + getSquadron().
                     getName() + " will try to move to " + getDestination().
                     getName());
         }
         return longDescriptionProperty;
     }
 
     @Override
     public boolean isOrderAccomplished() {
         return getDestination().getPositionX() == getSquadron().getPositionX() && getDestination().
                 getPositionY() == getSquadron().getPositionY();
     }
 
     @Override
     public void finalizeOrder() {
         LOG.trace("[" + getShortDescription() + "] order for squadron [" + getSquadron().
                 getName() + "] finalized");
        getSquadron().setSpeed(0);
         getSquadron().setDestination(null);
     }
 
     @Override
     public void executeOrder(long incrementSize) {
         if (getSquadron().getDestination() != null) {
             //We calculate the maximum traveled distance during the increment
             double traveledDistance = getSquadron().getSpeed() * incrementSize;
             Point2D squadronPosition = new Point2D(getSquadron().getPositionX(), getSquadron().
                     getPositionY());
             Point2D destinationPosition = new Point2D(getSquadron().
                     getDestination().
                     getPositionX(), getSquadron().getDestination().getPositionY());
 
             //We calculate the distance to the destination 
             double destinationDistance = squadronPosition.distance(destinationPosition);
             //If the maximum traveled distance is enough to reach the destination
             if (destinationDistance < traveledDistance) {
                 //We move the squadron to the destination
                 getSquadron().setPositionX(getSquadron().getDestination().
                         getPositionX());
                 getSquadron().setPositionY(getSquadron().getDestination().
                         getPositionY());
                 //We mark the order as finished
                 setFinished(true);
                 getSquadron().setDestination(null);
 
             } else {
                 Point2D newPositionForShip = ShipUtils.
                         calculateIntermediateCoordinate(squadronPosition, destinationPosition, traveledDistance);
 
 
                 getSquadron().setPositionX(newPositionForShip.getX());
                 getSquadron().setPositionY(newPositionForShip.getY());
             }
         }
     }
 }
