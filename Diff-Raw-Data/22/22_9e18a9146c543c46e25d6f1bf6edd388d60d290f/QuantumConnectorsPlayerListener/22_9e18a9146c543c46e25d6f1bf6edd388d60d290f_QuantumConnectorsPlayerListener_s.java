 package com.ne0nx3r0.quantum.listeners;
 
 import com.ne0nx3r0.quantum.QuantumConnectors;
 import com.ne0nx3r0.quantum.circuits.CircuitManager;
 import com.ne0nx3r0.quantum.circuits.PendingCircuit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class QuantumConnectorsPlayerListener implements Listener{
     private final QuantumConnectors plugin;
     
     public QuantumConnectorsPlayerListener(QuantumConnectors instance){
         this.plugin = instance;
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event){   
 
     //Clicked on a block that has a quantum circuit (sender) attached
         if (event.getClickedBlock() != null && CircuitManager.circuitExists(event.getClickedBlock().getLocation())) {
             Block block = event.getClickedBlock();
 
             if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.TRAP_DOOR) {
                 CircuitManager.activateCircuit(event.getClickedBlock().getLocation(), CircuitManager.getBlockCurrent(block));
             }
         }
         
     //Holding redstone, clicked a block, and has a pending circuit from /qc
         else if(event.getItem() != null
         && event.getItem().getType() == Material.REDSTONE
         && event.getClickedBlock() != null
         && CircuitManager.hasPendingCircuit(event.getPlayer())){
             Player player = event.getPlayer();
             PendingCircuit pc = CircuitManager.getPendingCircuit(player);
             Block block = event.getClickedBlock();
             Location clickedLoc = block.getLocation();
                 
         //No sender yet
             if(!pc.hasSenderLocation()){
             //Is this a valid block to act as a sender?
                 if(CircuitManager.isValidSender(block)){
                 //There is already a circuit there  
                     if(CircuitManager.circuitExists(clickedLoc)){
                         plugin.msg(player, ChatColor.YELLOW + "A circuit already sends from this location!");
                         plugin.msg(player, "Break the block to remove it.");
                     }
                 //Set the sender location
                     else{
                         pc.setSenderLocation(clickedLoc);
                         
                         plugin.msg(player, "Sender saved!");
                     }
                 }
             //Invalid sender
                 else{
                     plugin.msg(player, ChatColor.RED + "Invalid sender!");
                     plugin.msg(player, ChatColor.YELLOW + "Senders: " + ChatColor.WHITE + CircuitManager.getValidSendersString());
                 }
             }
         //Adding a receiver
             else{
             //Player clicked the sender block again
                 if(pc.getSenderLocation().toString().equals(clickedLoc.toString())) {
                     plugin.msg(player, ChatColor.YELLOW + "A block cannot be the sender AND the receiver!");
                 }
             //Player clicked a valid receiver block
                 else if(CircuitManager.isValidReceiver(block)){
                     
                 //Only allow circuits in the same world, sorry multiworld QCircuits :(
                     if(pc.getSenderLocation().getWorld().equals(clickedLoc.getWorld())){
                     //Add the receiver to our new/found circuit
                        pc.addReceiver(null);
                         
                         plugin.msg(player, "Added a receiver! (#"+pc.getCircuit().getReceiversCount() +")" +ChatColor.YELLOW + " ('/qc done', or add more receivers)");
                     }
                 //Receiver was in a different world
                     else{
                         plugin.msg(player,ChatColor.RED + "Receivers must be in the same world as their sender! Sorry :|");
                     }
                 }
             //Player clicked an invalid receiver block
                 else{
                     plugin.msg(player, ChatColor.RED + "Invalid receiver!");
                     plugin.msg(player, ChatColor.YELLOW + "Receivers: " + ChatColor.WHITE + CircuitManager.getValidReceiversString());            
                     plugin.msg(player, "('/qc done' if you are finished)");
                 }
             }
         }        
     }
 }
