package be.kuleuven.cs.gridlock.dmas.coordination.vehicle.behaviors;
 
 import be.kuleuven.cs.gridlock.dmas.coordination.vehicle.elec.behavioral.IVehicleContext;
 import be.kuleuven.cs.gridlock.simulation.api.NodeReference;
 import be.kuleuven.cs.gridlock.simulation.api.VirtualTime;
 import be.kuleuven.cs.gridlock.simulation.events.EventController;
 
 /**
  * Behavior for determining the stations that will be visited based on dmas
  * exploration.
  *
  * @author Kristof Coninx <kristof.coninx at student.kuleuven.be>
  */
 public class AdvisedEVChargingBehavior implements IElecVehicleBehavior {
      EventController eventcontroller;
     public AdvisedEVChargingBehavior( EventController eventcontroller) {
         this.eventcontroller = eventcontroller;
     }
 
     @Override
     public void executeBehavior(VirtualTime currentTime, double timeFrameDuration, IVehicleContext context) {
         if (/*!context.isOnRouteToCharge() &&*/context.isRouteStale() && context.getCurrentPosition() != null) {
             if (!context.isOnRouteToCharge()) {
                 chargingBehavior(context.getCurrentPosition(), context);
             } else {
                 reconsiderBehavior(context.getCurrentPosition(), context);
             }
             context.routeRefreshed();
         }
     }
 
     private void chargingBehavior(NodeReference position, IVehicleContext context) {
         float ar = context.getEV().getActionRadius() / 1000; //convert m to km.
         float distance = (float) context.calculateRouteReference(position).resolveDistance(context.getGraphReference());
         if (!context.getItinerary().isEmpty()) {
             NodeReference station = context.getItinerary().getFirst();
             if (distance > ar) {
                 context.scheduleChargingStop(station);
             }
         } 
 //        else {
 //            context.resetChargingStop();
 //        }
     }
 
    private void reconsiderStop(NodeReference position, NodeReference station, IVehicleContext context) {
         //TODO add stubborness factor.
         NodeReference prev = context.getChargingStop();
         float ar = context.getEV().getActionRadius() / 1000; //convert m to km.
         //float distanceBef = (float) context.calculateRouteReference(position).resolveDistance(context.getGraphReference());
         context.scheduleChargingStop(station);
         float distanceAft = (float) context.calculateRouteReference(position).resolveDistance(context.getGraphReference());
         if (distanceAft > ar) {
             context.scheduleChargingStop(prev);
         }else{
             eventcontroller.publishEvent("agent:dmas:reconsider", "vehicle",context.getEV().getVehicleEntity().getVehicleReference().getId());
         }
     }
 
     private void reconsiderBehavior(NodeReference current, IVehicleContext context) {
         if (!context.getItinerary().isEmpty()) {
             NodeReference station = context.getItinerary().getFirst();
             if (!station.equals(context.getChargingStop())) {
                 reconsiderStop(current, station, context);
             }
         }
     }
 }
