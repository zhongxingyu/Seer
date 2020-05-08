 package com.ne0nx3r0.quantum.listeners;
 
 import com.ne0nx3r0.quantum.QuantumConnectors;
 import com.ne0nx3r0.quantum.circuits.CircuitManager;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Furnace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.inventory.FurnaceBurnEvent;
 
 public class QuantumConnectorsBlockListener implements Listener {
     private static QuantumConnectors plugin;
     public static String string;
 
     public QuantumConnectorsBlockListener(final QuantumConnectors plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onBlockRedstoneChange(BlockRedstoneEvent event){
         if (CircuitManager.circuitExists(event.getBlock().getLocation())) {
             CircuitManager.activateCircuit(event.getBlock().getLocation(), event.getNewCurrent());
         }
 
        if (CircuitManager.shouldLeaveReceiverOn(event.getBlock())){
             event.setNewCurrent(15);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event) {
         Location l = event.getBlock().getLocation();
         if (CircuitManager.circuitExists(l)) { // Breaking Sender
             CircuitManager.removeCircuit(l);
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onFuranceBurn(FurnaceBurnEvent e){
         if(CircuitManager.circuitExists(e.getBlock().getLocation())){          
             if(e.isBurning()){
                 Location lFurnace = e.getBlock().getLocation();
                 
                 //SEND ON
                 CircuitManager.activateCircuit(lFurnace, 5);
                 
                 //Schedule a check to send the corresponding OFF
                 Bukkit.getScheduler().scheduleSyncDelayedTask(
                     plugin,
                     new DelayedFurnaceCoolCheck(lFurnace),
                     e.getBurnTime()
                 ); 
             }
         }
     }
     
     private static class DelayedFurnaceCoolCheck implements Runnable{
         private final Location lFurnace;
 
         DelayedFurnaceCoolCheck(Location lFurnace){
             this.lFurnace = lFurnace;
         }
         
         @Override
         public void run(){
             Block bFurnace = lFurnace.getBlock();
 
             // If it's a BURNING_FURNACE it's still on and the next 
             // FurnaceBurnEvent is responsible for dispatching a delayed task
             if(bFurnace.getType() == Material.FURNACE){
                 //Send OFF
                 if(CircuitManager.circuitExists(lFurnace)){
                     CircuitManager.activateCircuit(lFurnace, 0);
                 }
             }
         }
     }
 }
