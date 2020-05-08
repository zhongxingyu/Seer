 //Copyright (C) 2011-2013 Chris Price (xCP23x)
 //This software uses the GNU GPL v2 license
 //See http://github.com/xCP23x/RestockIt/blob/master/README and http://github.com/xCP23x/RestockIt/blob/master/LICENSE for details
 
 package org.cp23.restockit;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 
 class Listeners implements Listener {
     
     private static Block getRestockItChest(Block chest){
         //Return which chest has the sign (else null)
         if(RestockIt.isContainer(chest.getType())) {
             Block dc = ContUtils.getDoubleChest(chest);
             
             if(ContUtils.isRICont(chest)) return chest;
             else if(dc != null && ContUtils.isRICont(dc)) return dc;
         }
         return null;
     }
     
     private void eventTriggered(Block chest, String line2, String line3, Block sign){
         if(SignUtils.isDelayedSign(line3, SignUtils.getMaterial(line2))){
             Scheduler.startSchedule(sign, SignUtils.getPeriod(line3)); //If it's a delayed sign, start a schedule
         } else ContUtils.fillCont(chest, line2); //If not, RestockIt.
         
         //New code for delayed double chests
         Block dc = ContUtils.getDoubleChest(chest);
         if (dc!=null){
             Sign dcsign = (ContUtils.isRICont(dc)) ? ((Sign)SignUtils.getSignFromCont(dc).getState()) : null;
 			if(dcsign!=null) {
 	            String dcline2 = dcsign.getLine(2);
     	        String dcline3 = dcsign.getLine(3);
         	    if(dcline3!=null && SignUtils.isDelayedSign(dcline3, SignUtils.getMaterial(dcline2)))
             	    Scheduler.startSchedule(SignUtils.getSignFromCont(dc), SignUtils.getPeriod(dcline3));
 			}
         }
     }
     
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         RestockIt.debug("Sign Changed");
         Block sign = event.getBlock();
         Player player = event.getPlayer();
         String[] lines = event.getLines();
         String line0 = lines[0], line1 = lines[1], line2 = lines[2], line3 = lines[3]; //Get the lines
        RestockIt.debug("Line0: " + line0 + ", Line1: " + line1 + ", Line2: " + line2 + ", Line3: " + line3);
         
         Block block = ContUtils.getContFromSign(sign);
         
         
         //If the player forgot the blank line, correct it for them
         if(SignUtils.isRIsign(line0)) {
             RestockIt.debug("Fixing missing blank line0");
             event.setLine(3, line2);
             event.setLine(2, line1);
             event.setLine(1, line0);
             event.setLine(0, "");
             lines = event.getLines();
             line1 = lines[1];
             line2 = lines[2];
             line3 = lines[3]; 
         }
         
         if(SignUtils.isRIsign(line1)){
             
             if(block == null){ //There's no chest there
                 RestockIt.debug("No chest by sign");
                 SignUtils.dropSign(sign);
                 PlayerUtils.sendPlayerMessage(player, 6);
                 return;
             }
             
             RIperm perm = new RIperm(block, player, line2);
             perm.setCreated();
             
             if(!PlayerUtils.hasPermissions(perm)){ //They don't have permission
                 SignUtils.dropSign(sign);
                 PlayerUtils.sendPlayerMessage(player, 2, block.getType().name().toLowerCase());
                 return;
             }
             
             if(ContUtils.isAlreadyRICont(sign)) { //It's already a RestockIt cont
                 RestockIt.debug("It's already a RestockIt container");
                 SignUtils.dropSign(sign);
                 PlayerUtils.sendPlayerMessage(player, 1);
                 return;
             }
             
             if(SignUtils.isIncinerator(line2)) { //It's an incinerator, we can go straight to eventTriggered()
                 RestockIt.debug("It's an incinerator");
                 eventTriggered(block,line2,line3,sign);
                 return;
             }
             
             if(SignUtils.line2hasErrors(line2, player)) { //Errors were found (no need to tell the player, they've already been told)
                 SignUtils.dropSign(sign);
                 return;
             }
             
             perm.setBlacklistBypass();
             
             //Check Blacklist
             if(RestockIt.isInList(SignUtils.getMaterial(line2), RestockIt.listType.BLACKLIST) && !PlayerUtils.hasPermissions(perm)){
                 PlayerUtils.sendPlayerMessage(player, 7, SignUtils.getMaterial(line2).name());
                     SignUtils.dropSign(sign);
                     return;
             }
             
             eventTriggered(block, line2, line3, sign);
         }
     }
     
     @EventHandler
     public void onBlockDispense(BlockDispenseEvent event) { //For auto-refilling dispensers
         RestockIt.debug("Block dispensed");
         Block block = event.getBlock();
         if(RestockIt.isInList(block.getType(), RestockIt.listType.DISPENSERS)) {   //Make sure the dispensable dispensee was dispensed by a dispenser
             RestockIt.debug("... by a dispenser");
             if(ContUtils.isRICont(block)) {
                 RestockIt.debug("It's a RestockIt container");
                 Block sign = SignUtils.getSignFromCont(block);
                 String line2 = ((Sign)sign.getState()).getLine(2);
                 String line3 = ((Sign)sign.getState()).getLine(3);
                 eventTriggered(block, line2, line3, sign);
             }
         }
     }
     
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         Block block = event.getBlock();
         Material mat = block.getType();
         Player player = event.getPlayer();
         
         if(getRestockItChest(block) != null ) { //Make sure it's a RestockIt chest
             RestockIt.debug("RestockIt chest broken");
             if(ContUtils.isRICont(block)){ //Only remove the sign if we remove a main (non-auxiliary) chest
                 Block sign = SignUtils.getSignFromCont(block);
                 RIperm perm = new RIperm(block, player, sign);
                 perm.setDestroyed();
                 
                 if(!PlayerUtils.hasPermissions(perm)){
                     PlayerUtils.sendPlayerMessage(player, 9, perm.getBlockType());
                     event.setCancelled(true);
                     return;
                 }
                 
                 Scheduler.stopSchedule(sign); //Stop any schedules running for this block
                 SignUtils.dropSign(sign); //Remove the sign
             }
             Inventory inv = ContUtils.getInventory(block);
             inv.clear(); //Stops chests bursting everywhere when broken
             RestockIt.debug("Chest emptied");
         }
         else if(mat == Material.WALL_SIGN|| mat == Material.SIGN_POST) {
             Block sign = block;
             String line = ((Sign)sign.getState()).getLine(1);
             
             if(SignUtils.isRIsign(line)) {
                 RestockIt.debug("RestockIt sign broken");
                 Block chest = ContUtils.getContFromSign(sign);
                 if(chest != null){
                     RIperm perm = new RIperm(chest, player, sign);
                     perm.setDestroyed();
                     
                     if(!PlayerUtils.hasPermissions(perm)){
                         PlayerUtils.sendPlayerMessage(player, 9, perm.getBlockType());
                         event.setCancelled(true);
                         return;
                     }
                     
                     Inventory inv = ContUtils.getInventory(chest);
                     inv.clear(); //Empty the chest
                     RestockIt.debug("Chest emptied");
                     Scheduler.stopSchedule(sign); //Stop any schedules for this block
                 }
             }
         }
     }
     
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event){
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK){ //If they right click...
             Block block = event.getClickedBlock();
             Block chest = getRestockItChest(block);
             if(chest != null) {  //And it's a restockit chest...
                 RestockIt.debug("RestockIt container opened");
                 Player player = event.getPlayer();
                 
                 Block sign = SignUtils.getSignFromCont(chest);
                 String line2 = ((Sign)sign.getState()).getLine(2);
                 String line3 = ((Sign)sign.getState()).getLine(3);
                 
                 eventTriggered(chest, line2, line3, sign);
                 
                 RIperm perm = new RIperm(block, player, sign);
                 perm.setOpened();
                 if(!PlayerUtils.hasPermissions(perm)){
                     PlayerUtils.sendPlayerMessage(player, 8, perm.getBlockType());
                     event.setCancelled(true);
                 }
             }
         }
     }
 }
