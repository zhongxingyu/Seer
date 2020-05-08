 package org.morden.lightcontrol;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.tal.redstonechips.circuit.CircuitLibrary;
 
 /**
  *
  * @author Dennis Flanagan
  */
 public class LightControl extends CircuitLibrary {
 	private static List<RSLight> rsTorchLightCircuits = new ArrayList<RSLight>();
 
     @Override
     public Class[] getCircuitClasses() {
         return new Class[] { glasslight.class, pumpkinlight.class, rstorchlight.class, torchlight.class, netherlight.class };
     }
 	
 	@Override
 	public void onEnable() {
         BlockListener blockListener = new BlockListener() {
             @Override
             public void onBlockPhysics(BlockPhysicsEvent event) {
 				if (!event.isCancelled()) {
 					for (RSLight circuit : rsTorchLightCircuits)
 						circuit.onBlockPhysics(event);
 				}
             }
 			
 			@Override
             public void onBlockBreak(BlockBreakEvent event) {
                 if (!event.isCancelled()) {
                     for (RSLight circuit : rsTorchLightCircuits)
 						circuit.onBlockBreak(event);
                 }
             }
         };
 	
 		getServer().getPluginManager().registerEvent(Type.BLOCK_PHYSICS, blockListener, Priority.Highest, this);
 		getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
 	}
 	
 	static void registerRSTorchLightCircuit(RSLight circuit) {
         if (!rsTorchLightCircuits.contains(circuit))
             rsTorchLightCircuits.add(circuit);
     }
 
     static boolean deregisterRSTorchLightCircuit(RSLight circuit) {
         return rsTorchLightCircuits.remove(circuit);
     }
 }
