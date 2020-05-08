 package org.bongomice.rotate;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.craftbukkit.block.CraftBlock;
 import org.bukkit.craftbukkit.block.CraftChest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 class RotateBlock implements Listener {
 
   @EventHandler
   public void BlockInteracted(PlayerInteractEvent event) {
       
      Block block = event.getClickedBlock();
      Player player = event.getPlayer();
      Action click = event.getAction();
     
      int toolID;
      int blockID;
      byte blockData;
      
      if (RotatePlugin.WorldGuard != null) {
          try {
             if (!RotatePlugin.WorldGuard.canBuild(player, block)) {
                 return;
             }
          } catch (NullPointerException e) {
              return;
          }
      }
      
      try {
        
         blockID = block.getTypeId();
         toolID = player.getItemInHand().getTypeId();
         blockData = block.getData();
         
      } catch(NullPointerException e){         
          return; 
      }
      
      if (RotateUtil.getUserTool(player.getPlayerListName()) != toolID) {
          return;
      }
	 player.sendMessage(String.valueOf(blockData));

      
      if (!RotateUtil.isValidTarget(blockID)) {
          return;
      }
      
      event.setCancelled(true);
      
      switch (blockID){
          
          case 44: 
          case 126:
              if (blockData < 8) { blockData += 8; } 
              else if (blockData >= 8) { blockData -= 8; } 
              else { return; }
              break;
          
          
          case 17:
              if (click == Action.LEFT_CLICK_BLOCK) {
                 
                 if (blockData < 8) { blockData += 4; }
                 else if (blockData >= 8) { blockData -= 8; }
                 else { return; }
                 
              } else if (click == Action.RIGHT_CLICK_BLOCK){
                 
                 if (blockData > 3) { blockData -= 4; }
                 else if (blockData <= 3) { blockData += 8; }
                 else { return; }
                 
              }
              break;
          
             
          case 134:
          case 135:
          case 136:
          case 114:
          case 128:
          case 53:
          case 67:
          case 108:
          case 109:
              if (click == Action.LEFT_CLICK_BLOCK) {
                 
                 if (blockData < 7) { blockData++; }
                 else if (blockData == 7) { blockData = 0; }
                 else { return; }
                 
              } else if (click == Action.RIGHT_CLICK_BLOCK){
                 
                 if (blockData > 0) { blockData--; }
                 else if (blockData == 0) { blockData = 7; }
                 else { return; }
                 
              }
              break;
 			 
 		 case 54: if("Large chest".equalsIgnoreCase(new CraftChest(block).getInventory().getName())){
 					switch(blockData){
 						case 2: blockData++;
 							break;
 						case 3: blockData--;
 							break;
 						case 4: blockData++;
 							break;
 						case 5: blockData--;
 					}
 					break;
 				 }
 		 case 130:
          case 23:
          case 61:
          case 62:
              if (click == Action.LEFT_CLICK_BLOCK) {
                 
                 if (blockData < 5) { blockData++; }
                 else if (blockData == 5) { blockData = 2; }
                 else { return; }
                 
              } else if (click == Action.RIGHT_CLICK_BLOCK){
                 
                 if (blockData > 2) { blockData--; }
                 else if (blockData == 2) { blockData = 5; }
                 else { return; }
              }
              break;
              
             
          case 29:
          case 33:
              
              if (block.isBlockPowered() || block.isBlockIndirectlyPowered()) {
                 return;
              }
              if (click == Action.LEFT_CLICK_BLOCK) {
                 
                 if (blockData < 5) { blockData++; }
                 else if (blockData == 5) { blockData = 0; }
                 else { return; }
                 
              } else if (click == Action.RIGHT_CLICK_BLOCK){
                 
                 if (blockData > 0) { blockData--; }
                 else if (blockData == 0) { blockData = 5; }
                 else { return; }
                 
              }
              break;
             
          
          case 27:
          case 28:
          case 66: 
              switch(blockData){
                 case 0: 
                     blockData = 1;
                     break;
                 case 1: 
                     blockData = 0;
                     break;
                 case 8: 
                     blockData = 9;
                     break;
                 case 9: 
                     blockData = 8;
                     break;
              }
              break;
          case 63:
          case 323:
              if (click == Action.LEFT_CLICK_BLOCK) {
                 
                 if (blockData < 15) { blockData++; }
                 else if (blockData == 15) { blockData = 0; }
                 else { return; }
                 
              } else if (click == Action.RIGHT_CLICK_BLOCK){
                 
                 if (blockData > 0) { blockData--; }
                 else if (blockData == 0) { blockData = 15; }
                 else { return; }
                 
              }
              break;
 		 case 86:
 		 case 91:
 			 if (click == Action.LEFT_CLICK_BLOCK) {
                 
                 if (blockData < 3) { blockData++; }
                 else if (blockData == 3) { blockData = 0; }
                 else { return; }
                 
              } else if (click == Action.RIGHT_CLICK_BLOCK){
                 
                 if (blockData > 0) { blockData--; }
                 else if (blockData == 0) { blockData = 5; }
                 else { return; }
                 
              }
              break;
               
      }
      
 	 
      block.setData((byte) blockData);
      
   }
 
   
 }
