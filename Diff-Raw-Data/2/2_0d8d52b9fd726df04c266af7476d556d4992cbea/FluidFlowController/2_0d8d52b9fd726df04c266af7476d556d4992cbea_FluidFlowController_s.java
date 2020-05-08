 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.DynSimulator;
 
 import eel.seprphase2.GameOverException;
 import eel.seprphase2.Simulator.CannotControlException;
 import eel.seprphase2.Simulator.CannotRepairException;
 import eel.seprphase2.Simulator.Condenser;
 import eel.seprphase2.Simulator.FailableComponent;
 import eel.seprphase2.Simulator.KeyNotFoundException;
 import eel.seprphase2.Simulator.PlantController;
 import eel.seprphase2.Simulator.Pump;
 import eel.seprphase2.Simulator.Reactor;
 import eel.seprphase2.Simulator.Turbine;
 import eel.seprphase2.Simulator.Valve;
 import eel.seprphase2.Utilities.Mass;
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import static eel.seprphase2.Utilities.Units.*;
 import eel.seprphase2.Utilities.Velocity;
 import static lamprey.seprphase3.Utilities.Units.*;
 import static lamprey.seprphase3.DynSimulator.GameConfig.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import lamprey.seprphase3.Extra.Area;
 import lamprey.seprphase3.Utilities.MassFlowRate;
 
 /**
  *
  * @author will
  */
 public class FluidFlowController implements PlantController {
 
     PlantModel plant;
     double deltaSeconds; // Time elapsed in during an update.
 
     public FluidFlowController(PlantModel plant) {
         this.plant = plant;
     }
 
     @Override
     public void step(double seconds) throws GameOverException {
         deltaSeconds = seconds;
         updateFlow(seconds);
         plant.reactor().step(seconds);
         plant.turbine().step(seconds);
         plant.condenser().step(seconds);
         for (Pump p : plant.pumps().values()) {
             p.step();
         }
         plant.increaseEnergyGenerated(joules(plant.turbine().outputPower()));
         //printFlowDebugInfo();
     }
 
     /**
      *
      * @param percent
      */
     @Override
     public void moveControlRods(Percentage percent) {
         plant.reactor().moveControlRods(percent);
     }
 
     @Override
     public void wearReactor() {
         Percentage damage = new Percentage(10);
         plant.reactor().addWear(damage);
     }
 
     @Override
     public void failCondenser() {
         plant.condenser().fail();
     }
 
     // Ruddy Eels - what on earth is this lol?!
     @Override
     public void setWornComponent(FailableComponent wornComponent) {
         if (wornComponent == null) {
             plant.setCurrentWornComponent("");
         } else {
             /*
              * Check if a Valve was worn, if so get its Key.
              */
             Iterator pumpIterator = plant.pumps().entrySet().iterator();
             while (pumpIterator.hasNext()) {
                 Map.Entry pump = (Map.Entry)pumpIterator.next();
 
                 if (((Pump)pump.getValue()).equals(wornComponent)) {
                     plant.setCurrentWornComponent("Pump " + pump.getKey());
                 }
             }
 
             /*
              * Check if the condenser was worn
              */
             if (wornComponent instanceof Condenser) {
                 plant.setCurrentWornComponent("Condenser");
             } /*
              * Check if the turbine was worn
              */ else if (wornComponent instanceof Turbine) {
                 plant.setCurrentWornComponent("Turbine");
             }
         }
 
 
     }
 
     @Override
     public void changeValveState(int valveNumber, boolean isOpen) throws KeyNotFoundException {
         if (plant.valves().containsKey(valveNumber)) {
             plant.valves().get(valveNumber).setOpen(isOpen);
         } else {
             throw new KeyNotFoundException("Valve " + valveNumber + " does not exist");
         }
     }
 
     @Override
     public void changePumpState(int pumpNumber, boolean isPumping) throws CannotControlException, KeyNotFoundException {
         if (!plant.pumps().containsKey(pumpNumber)) {
             throw new KeyNotFoundException("Pump " + pumpNumber + " does not exist");
         }
 
         if (plant.pumps().get(pumpNumber).hasFailed()) {
             throw new CannotControlException("Pump " + pumpNumber + " is failed");
         }
 
         plant.pumps().get(pumpNumber).setStatus(isPumping);
     }
 
     @Override
     public void repairPump(int pumpNumber) throws KeyNotFoundException, CannotRepairException {
         if (plant.pumps().containsKey(pumpNumber)) {
             plant.pumps().get(pumpNumber).repair();
 
 
             //These shouldn't need to be changed
             //allPumps.get(pumpNumber).setStatus(true);
             //allPumps.get(pumpNumber).setCapacity(kilograms(3));
             //allPumps.get(pumpNumber).stepWear(new Percentage(0));
 
         } else {
             throw new KeyNotFoundException("Pump " + pumpNumber + " does not exist");
         }
     }
 
     @Override
     public void repairCondenser() throws CannotRepairException {
         plant.condenser().repair();
     }
 
     @Override
     public void repairTurbine() throws CannotRepairException {
         plant.turbine().repair();
     }
 
     @Override
     public void setSoftwareFailureTimeRemaining(int failureTime) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void wearCondenser() {
         plant.condenser().wear();
     }
 
     @Override
     public void flipValveState(int valveNumber) throws KeyNotFoundException {
         if (plant.valves().containsKey(valveNumber)) {
             Valve v = plant.valves().get(valveNumber);
             v.setOpen(!v.getOpen());
         } else {
             throw new KeyNotFoundException("Valve " + valveNumber + " does not exist");
         }
     }
 
     @Override
     public void flipPumpState(int pumpNumber) throws CannotControlException, KeyNotFoundException {
         if (!plant.pumps().containsKey(pumpNumber)) {
             throw new KeyNotFoundException("Pump " + pumpNumber + " does not exist");
         }
         Pump p = plant.pumps().get(pumpNumber);
         if (p.hasFailed()) {
             throw new CannotControlException("Pump " + pumpNumber + " is failed");
         }
        p.setStatus(p.getStatus());
     }
 
   
      // ----------------------------------- CODE TO CONVERT! -----------------------------
      /**
      * Highest level method for updating flow. This method calls all other methods
      * necessary for propagating flow, as well as blockages, throughout the system.
      * In this order, we:
      * 		- Set all outputs of all Junctions to not blocked.
      * 		- Propagate blockages from all closed valves in the system back to their
      * 			first preceding Junction.
      * 		- Propagate all blockages throughout the entire system.
      * 		- Set the flow rate and temperature of all components to zero in 
      * 			preparation for flow calculation & propagation.
      * 		- Calculate and propagate the flow from the reactor forward.
      * 		- Calculate the flow due to the pumps in the system and totals them up at
      * 			the condenser output.
      * 		- Propagate the flow out of the condenser forwards.
      * 		- Propagate flow through all paths in the system.
      * 		- Transfer steam from the reactor into the condenser.
      * 		- Transfer water from the condenser into the reactor. 
      */
     private void updateFlow(double seconds) {
         setAllJunctionsUnblocked();
         blockFromValves();
         blockFromJunctions();
         resetFlowAllComponents();
 
         propagateFlowFromReactor(); // Start propagation of steam flow.
         propagateFlowFromPumpsToCondenser(); // Total up all pump flows at condenser
         propagateFlowFromCondenser();	// Start propagation of water flow.
         propagateFlowFromJunctions();
         //moveSteam(seconds);
         //moveWater(seconds);
     }
 
     // THESE NEED TO BE IMPLEMENTED! otherwise we could get water loss if condenser runs dry!
 //    /**
 //     * Moves water out of the condenser and into the reactor due to the flow in and out of the components.
 //     */
 //    private void moveWater(double seconds) {
 //        Condenser condenser = this.plant.condenser();
 //        Reactor reactor = this.plant.reactor();
 //        Mass waterInCondenser = condenser.getWaterMass();
 //        Mass massTryingToFlowOut = condenser.outputPort(null).flowRate.massFlowForTime(seconds);
 //        // Check if there's enough water in the condenser to fulfil the flow rate.
 //        Mass delta = (waterInCondenser.inKilograms() > massTryingToFlowOut.inKilograms()) ? massTryingToFlowOut : waterInCondenser;
 //        condenser.updateWaterVolume(-delta);
 //        // This should really use reactor's input's flow out but ah well.
 //        reactor.updateWaterVolume(delta);
 //    }
 //
 //    /**
 //     * Forcefully removes steam from the reactor and places it into the condenser. 
 //     */
 //    private void moveSteam(double seconds) {
 //        Reactor reactor = this.plant.reactor();
 //        Condenser condenser = this.plant.condenser();
 //        reactor.updateSteamVolume(-reactor.getFlowOut().getRate());
 //        condenser.updateSteamVolume(condenser.getInput().getFlowOut().getRate());
 //    }
 
     /**
      * Resets all Junction paths to unblocked. We do this to all Junctions at the beginning of each
      * updateFlow() before propagating the blockages since valves can change state between steps.
      */
     private void setAllJunctionsUnblocked() {
         for (Junction j : this.plant.junctions()) {
             j.resetState();
         }
     }
 
     /**
      * Iterates through all valves in the system and if they are closed we propagate the blockage through to the next
      * preceding Junction.
      */
     private void blockFromValves() {
         for (Valve v : this.plant.valves().values()) {
             if (!v.getOpen()) {
                 blockToPrecedingJunction(v);
             }
         }
     }
 
     /**
      * Iterates through all Junctions in the system and propagates the blockage, if all outputs of that
      * Junctions is blocked.
      *
      * This is done until all blocked Junctions have had their blockage propagated.
      */
     private void blockFromJunctions() {
         boolean changed = true;
         List<Junction> junctions = this.plant.junctions();
         Map<Junction, Boolean> hasBeenPropagated = new HashMap<Junction, Boolean>();
         while (changed) {
             changed = false;
             // iterate through all connector pipes and check if they're blocked up.
             for (Junction j : junctions) {
                 // If we're not already keeping track of c, add it to the hashmap
                 if (!hasBeenPropagated.containsKey(j)) {
                     hasBeenPropagated.put(j, false);
                 }
                 // If connectorPipe has all of it's outputs blocked
                 // And the blockage hasn't been propagated
                 if (isJunctionBlocking(j) && !hasBeenPropagated.get(j)) {
                     // Block the path leading into it.
                     blockPrecedingFromJunction(j);
                     hasBeenPropagated.put(j, true);
                     changed = true;
                 }
             }
         }
     }
 
     /**
      * Returns true if all outputs of a Junction are blocked.
      *
      * @return true if all outputs of a Junction are blocked.
      */
     private boolean isJunctionBlocking(Junction j) {
         return (j.numOutputs() == 0);
     }
 
     /**
      * Traces back to the first occurring Junction and blocks the path out leading to blockedComponent. We assume
      * checks have been made to ensure blockedComponent is actually blocked.
      *
      * @param blockedComponent component to start from.
      */
     private void blockToPrecedingJunction(FlowThroughComponent blockedComponent) {
         FlowThroughComponent currentComponent = blockedComponent.input;
         FlowThroughComponent prevComponent = blockedComponent;
         boolean doneBlocking = false;
         while (!doneBlocking) {
             if (currentComponent instanceof Junction) {
                 ((Junction)currentComponent).block(prevComponent);
                 doneBlocking = true;
             } else if (currentComponent instanceof Reactor) {
                 // No need to do anything here, just stop iterating.
                 doneBlocking = true;
             } else {
                 prevComponent = currentComponent;
                 currentComponent = currentComponent.input;
             }
         }
     }
 
     /**
      * Calls blockToPrecedingJunction() for all input paths into blockedConnector. We assume checks have been made to
      * ensure blockedJunction is actually blocked.
      *
      * If an input is a Junction, set the output that blockedJunction is connected to blocked.
      *
      * @param blockedJunction the blocked Junction to start from.
      */
     private void blockPrecedingFromJunction(Junction blockedJunction) {
         List<FlowThroughComponent> multipleInputs = ((Junction)blockedJunction).inputs();
         for (FlowThroughComponent c : multipleInputs) {
             if (c instanceof Junction) {
                 ((Junction)c).block(blockedJunction);
             } else {
                 if (c != null) {
                     blockToPrecedingJunction(c);
                 }
             }
         }
     }
 
     /**
      * Resets the flow of all components back ready for the flow around the system to be recalculated for the current
      * state of the plant.
      */
     private void resetFlowAllComponents() {
         for (FlowThroughComponent c : this.plant.components()) {
             // Junctions are special cases and get reset separately.
             if (!(c instanceof Junction)) { 
                 c.outputPort(null).flowRate = kilogramsPerSecond(0);
                 c.outputPort(null).temperature = kelvin(0);
             }
         }
     }
 
     /**
      * Start off propagation of the flow from the reactor to the next Junction encountered.
      */
     private void propagateFlowFromReactor() {
         Reactor reactor = this.plant.reactor();
         Condenser condenser = this.plant.condenser();
         // If there's a clear path from the reactor to the condenser then calculate
         // and start off the flow being propagated.
         if (isPathTo(reactor, condenser, true)) {
             MassFlowRate flowRate = calcReactorFlowOut();
             reactor.outputPort(null).flowRate = flowRate;
             reactor.outputPort(null).temperature = reactor.temperature();
             limitReactorFlowDueToValveMaxFlow(reactor);
             propagateFlowToNextJunction(reactor);
         } else {
             // Otherwise, all paths are blocked & don't bother.
         }
     }
     
     /**
      * Sums up the maximum flow possible through all valves that have a clear backward path to the reactor and if this
      * maximum flow is greater than the amount of steam wanting to come out of the reactor due to pressue, the rate is
      * limited.
      *
      * @param reactor the reactor to limit.
      */
     private void limitReactorFlowDueToValveMaxFlow(Reactor reactor) {
         MassFlowRate maxFlow = kilogramsPerSecond(0);
         for (Valve v : this.plant.valves().values()) {
             // If there is a path backwards from this valve to the reactor.
             // Also implying that it is actually in front of the reactor.
             if (isPathTo(v, reactor, false)) {
                 // increase the maximum flow allowed out of the reactor.
                 maxFlow = maxFlow.plus(v.maxThroughput());
             }
         }
         if (reactor.outputPort(null).flowRate.inKilogramsPerSecond() > maxFlow.inKilogramsPerSecond()) {
             reactor.outputPort(null).flowRate = maxFlow;
         }
     }
 
     /**
      * Calculate and return the flow of steam out of the reactor due to the difference in steam volume between the
      * reactor and condenser.
      *
      * This method ignores any blockages, these are dealt with when the flow is propagated around the system.
      *
      * @return rate of flow of steam out of the reactor.
      */
     private MassFlowRate calcReactorFlowOut() {
         Reactor reactor = this.plant.reactor();
         Condenser condenser = this.plant.condenser();
         Pressure pressureDiff = reactor.pressure().minus(condenser.getPressure());
         Velocity steamVelocity = FlowEquations.velocityFromPressureDiff(pressureDiff);
         MassFlowRate flowOut = FlowEquations.flowRateFromDensityVelocityArea(reactor.steamDensity(), steamVelocity, new Area(0.05));
         System.out.println("Full flow out: " + flowOut);
         return (flowOut.inKilogramsPerSecond() < 0) ? kilogramsPerSecond(0) : flowOut;
     }
 
     /**
      * Iterates through connector pipes, calculates their flow out & if it has changed, propagate this new flow forward
      * to the next connector pipe. Do this until nothing in the system changes (Inspired by bubble sort's changed
      * flag... "Good Ol' Bubble Sort!")
      */
     private void propagateFlowFromJunctions() {
         boolean changed = true;
         ArrayList<Junction> junctions = this.plant.junctions();
         MassFlowRate oldRate;
         while (changed) {
             changed = false;
             // iterate through all Junctions and update their rate.
             for (Junction j : junctions) {
                 for (FlowThroughComponent outputComponent : j.outputs()) {
                     if (!j.isBlocked(outputComponent)) {
                         oldRate = j.outputPort(outputComponent).flowRate;
                         j.updateOutputPorts();
                         if (!oldRate.equals(j.outputPort(outputComponent).flowRate)) {
                             propagateFlowFromJunction(j);
                             changed = true;
                         }
                     }
                     break;
                 }
             }
         }
     }
 
     /**
      * Propagates the flow rate and temperature to every component from startComponent until a Junction is
      * encountered.
      *
      * @param startComponent Component to start the propagation from.
      */
     private void propagateFlowToNextJunction(FlowThroughComponent startComponent) {
         FlowThroughComponent prevComponent;
         // If startComponent.isPressurised() (=> it is a reactor or condenser) start from here, not its input. 
         prevComponent = (startComponent.pressurised) ? startComponent : startComponent.input;
         FlowThroughComponent currComponent = startComponent;
         boolean donePropagating = false;
         while (!donePropagating) {
             if (currComponent instanceof Junction) {
                 donePropagating = true;
             } else if (currComponent instanceof Condenser) {
                 donePropagating = true;
             } else {
                 currComponent.outputPort(null).flowRate = prevComponent.outputPort(currComponent).flowRate;
                 currComponent.outputPort(null).temperature = prevComponent.outputPort(currComponent).temperature;
                 prevComponent = currComponent;
                 currComponent = currComponent.output;
             }
         }
     }
 
     /**
      * Propagates calls the appropriate methods for all unblocked outputs of startJunction in order to propagate
      * flow through the system.
      *
      * @param startJunction The Junction to propagate flow onward from.
      */
     private void propagateFlowFromJunction(Junction startJunction) {
         ArrayList<FlowThroughComponent> outputs = startJunction.outputs();
         for (FlowThroughComponent c : outputs) {
             // If the output is not blocked.
             if (!startJunction.isBlocked(c)) {
                 if (c instanceof Junction) {
                     propagateFlowFromJunction((Junction)c);
                 } else {
                     propagateFlowToNextJunction(c);
                 }
             }
         }
     }
 
     
 
     /**
      * Set's off the propagation from the condenser to the next Junction from it's output.
      */
     private void propagateFlowFromCondenser() {
         Condenser condenser = this.plant.condenser();
         condenser.outputPort(null).temperature = condenser.getTemperature();
         propagateFlowToNextJunction(condenser);
     }
 
     /**
      * Tracks back from a pump and if there is a clear path to the condenser adds the flow increase at this pump to the
      * flow out of the condenser.
      *
      * This method does not support multiple condensers.
      */
     private void propagateFlowFromPumpsToCondenser() {
         Condenser condenser = this.plant.condenser();
         // Hacky check to make the condenser not run dry :P
         // (You'll probably have died by then anyway ;D)
         if (condenser.getWaterMass().inKilograms() > 5) {
             // Iterate through all pumps and start tracking back through the system
             for (Pump p : this.plant.pumps().values()) {
                 // If the pump is broken, move onto the next one.
                 if (!(p.wear().points() == 100) && p.input != null) {
                     increaseCondenserFlowFromPump(p);
                 }
             }
         }
     }
 
     /**
      * Gets the flowRate due to this pump from it's current rpm. Then checks if there is a path from Pump p to the
      * connector (backwards) and if there is, we add the flow rate due to this pump to the flow rate out of the
      * condenser.
      *
      * @param p Pump to increase the flow out of the condenser due to.
      */
     private void increaseCondenserFlowFromPump(Pump p) {
         Condenser condenser = this.plant.condenser();
         // If there's a clear path to the condenser from p then add the flowRate of this pump
         // to the flowOut rate of the condenser.
         if (isPathTo(p, condenser, false)) {
             MassFlowRate condenserFlowOut = condenser.outputPort(null).flowRate;
             condenser.outputPort(null).flowRate = condenserFlowOut.plus(PUMP_INDUCEDFLOWRATE);
         }
     }
 
     /**
      * Returns true if there exists a path from start to goal that is not blocked and does not pass through a
      * pressurised component (Reactor/Condenser) in the direction that is specified.
      *
      * If forwards = true then the path with be traced using outputs, otherwise inputs.
      *
      * @param start    Component to start from.
      * @param goal     Component to attempt to reach.
      * @param forwards Direction of the path
      *
      * @return true if there exists a path from start to goal that is not blocked and does not pass through a
      *         pressurised component in the direction that is specified.
      */
     private boolean isPathTo(FlowThroughComponent start, FlowThroughComponent goal, boolean forwards) {
         List<FlowThroughComponent> possiblePaths;
         Junction junc;
 
         FlowThroughComponent current = start;
         FlowThroughComponent next = (forwards) ? start.output : start.input;
         while (!current.equals(goal)) {
             // If we're at any other component than a Junction, then advance to the next
             // component in the system in the direction we want.
             if (!(next instanceof Junction)) {
                 current = next;
                 next = (forwards) ? current.output : current.input;
             } else {
                 junc = (Junction)next;
                 if (!forwards) {
                     // If we're travelling backwards check if this path back is blocked
                     if (junc.isBlocked(current)) {
                         return false;
                     }
                 }
                 // I say, I say, we've got ourselves a Junction!
                 possiblePaths = (forwards) ? junc.outputs() : junc.inputs();
                 for (FlowThroughComponent possibleNext : possiblePaths) {
                     /* Check if we're moving forwards, check that the Junction output
                      * we're leaving from isn't blocked. If it is we don't move that way.
                      */
                     if (forwards) {
                         if (!junc.isBlocked(possibleNext)) {
                             // return isPathTo(possibleNext1, ...) || ... || isPathTo(possibleNextN,...)
                             if (isPathTo(possibleNext, goal, forwards)) {
                                 return true;
                             }
                         }
                     } else {
                         // return isPathTo(possibleNext1, ...) || ... || isPathTo(possibleNextN,...)
                         if (isPathTo(possibleNext, goal, forwards)) {
                             return true;
                         }
                     }
                 }
                 // All paths out of this connector pipe are blocked, no paths available :(
                 return false;
             }
         }
         return true;
     }
 
     // ----------------------------------- DEBUG
                                           /**
 	 * Prints debug info related to the flow of the plant to the console.
 	 */
 	private void printFlowDebugInfo() {
 		System.out.println("--------------------------");
 		for (FlowThroughComponent c : this.plant.components()) {
 			System.out.println("-----");
 			System.out.println(c.getClass().toString());
                         if (!(c instanceof Junction)) {
                             System.out.println("\tFlow Out:" + c.outputPort(null).flowRate);
                             System.out.println("\tTemp Out:" + c.outputPort(null).temperature);
                         } else {
                             for (FlowThroughComponent fc : ((Junction)c).outputs()) {
                                 System.out.println("\tFlow Out:" + c.outputPort(fc).flowRate);
                                 System.out.println("\tTemp Out:" + c.outputPort(fc).temperature);
                                 System.out.println("\t~~~~");
                             }
                         }
 		}
 			
 	}
     
 }
