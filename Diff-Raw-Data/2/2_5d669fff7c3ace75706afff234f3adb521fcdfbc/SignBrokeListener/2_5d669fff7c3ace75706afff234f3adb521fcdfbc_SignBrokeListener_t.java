 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.bukkit.listener;
 
 import at.co.hohl.mcutils.chat.Chat;
 import at.co.hohl.myresidence.MyResidence;
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.event.ResidenceChangedEvent;
 import at.co.hohl.myresidence.storage.persistent.Residence;
 import at.co.hohl.myresidence.translations.Translate;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.material.MaterialData;
 
 /**
  * Listens to the block break event, if the player broke a block which is a residence sign.
  *
  * @author Michael Hohl
  */
 public class SignBrokeListener extends BlockListener {
   private final MyResidence plugin;
 
   private final Nation nation;
 
   /**
    * Creates a new sing broke listener.
    *
    * @param plugin the plugin which holds the instance.
    * @param nation the nation.
    */
   public SignBrokeListener(MyResidence plugin, Nation nation) {
     this.nation = nation;
     this.plugin = plugin;
   }
 
   /**
    * Called when the player broken a block.
    *
    * @param event the event itself.
    */
   @Override
   public void onBlockBreak(BlockBreakEvent event) {
     if (event.isCancelled()) {
       return;
     }
 
     Block eventBlock = event.getBlock();
 
     if (eventBlock.getType().equals(Material.SIGN_POST) || eventBlock.getType().equals(Material.WALL_SIGN)) {
 
       final Sign clickedSign = (Sign) eventBlock.getState();
       if (!plugin.getConfiguration(eventBlock.getWorld()).getSignTitle().equals(clickedSign.getLine(0))) {
         return;
       }
 
       final Residence residence = nation.getResidence(clickedSign);
       if (residence == null) {
         return;
       }
 
       Chat.sendMessage(event.getPlayer(), Translate.get("cant_destroy_sign"));
       event.setCancelled(true);
 
       plugin.getEventManager().callEvent(new ResidenceChangedEvent(null, residence));
 
     } else {
       for (BlockFace blockFace :
               new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST}) {
 
         MaterialData blockAtFaceMaterial = eventBlock.getRelative(blockFace).getState().getData();
         if (blockAtFaceMaterial instanceof org.bukkit.material.Sign &&
                 ((org.bukkit.material.Sign) blockAtFaceMaterial).getAttachedFace()
                         .getOppositeFace()
                         .equals(blockFace)) {
 
           final Residence residence = nation.getResidence((Sign) eventBlock.getRelative(blockFace).getState());
           if (residence == null) {
             return;
           }
 
           Chat.sendMessage(event.getPlayer(), Translate.get("cant_destroy_sign"));
           event.setCancelled(true);
 
           plugin.getEventManager().callEvent(new ResidenceChangedEvent(null, residence));
 
         }
       }
    }
   }
 }
