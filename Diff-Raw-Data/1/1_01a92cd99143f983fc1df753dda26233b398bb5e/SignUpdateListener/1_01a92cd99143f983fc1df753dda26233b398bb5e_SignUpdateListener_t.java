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
 
 import at.co.hohl.myresidence.MyResidence;
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.event.*;
 import at.co.hohl.myresidence.exceptions.ResidenceSignMissingException;
 import at.co.hohl.myresidence.storage.persistent.Residence;
 import com.sk89q.util.StringUtil;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 
 /**
  * Listens to residence events and updates the sign correct.
  *
  * @author Michael Hohl
  */
 public class SignUpdateListener extends ResidenceListener {
   private final Nation nation;
 
   private final MyResidence plugin;
 
   /**
    * Creates a new listener, which updates the signs of the residences.
    *
    * @param nation the nation.
    * @param plugin the plugin.
    */
   public SignUpdateListener(Nation nation, MyResidence plugin) {
     this.nation = nation;
     this.plugin = plugin;
   }
 
   /**
    * Called when a new residence is created.
    *
    * @param event the event itself.
    */
   @Override
   public void onResidenceCreated(ResidenceCreatedEvent event) {
     try {
       updateResidenceSign(event.getResidence());
     } catch (ResidenceSignMissingException e) {
       plugin.severe(e.getMessage());
     }
   }
 
   /**
    * Called when a residence is changed.
    *
    * @param event the event itself.
    */
   @Override
   public void onResidenceChanged(ResidenceChangedEvent event) {
     try {
       updateResidenceSign(event.getResidence());
     } catch (ResidenceSignMissingException e) {
       plugin.severe(e.getMessage());
     }
   }
 
   /**
    * Called when a residence received a lik.
    *
    * @param event the event itself.
    */
   @Override
   public void onResidenceLiked(ResidenceLikedEvent event) {
     try {
       updateResidenceSign(event.getResidence());
     } catch (ResidenceSignMissingException e) {
       plugin.severe(e.getMessage());
     }
   }
 
   /**
    * Called when a residence is removed.
    *
    * @param event the event itself.
    */
   @Override
   public void onResidenceRemoved(ResidenceRemovedEvent event) {
     try {
       Block signBlock = nation.getResidenceManager(event.getResidence()).getSign();
       Sign sign = (Sign) signBlock.getState();
 
       for (int index = 0; index < 4; ++index) {
         sign.setLine(index, "");
        sign.update();
       }
     } catch (ResidenceSignMissingException e) {
       plugin.severe("Sign not found for residence %s!", event.getResidence().getName());
     }
   }
 
   /**
    * Updates the sign linked to passed Residence.
    *
    * @param residence Residence to update.
    */
   private void updateResidenceSign(Residence residence) throws ResidenceSignMissingException {
     Block signBlock = nation.getResidenceManager(residence).getSign();
 
     if (signBlock == null) {
       throw new ResidenceSignMissingException(residence);
     }
 
     Sign sign = (Sign) signBlock.getState();
     sign.setLine(0, plugin.getConfiguration(signBlock.getWorld()).getSignTitle());
     sign.setLine(1, StringUtil.trimLength(residence.getName(), 16));
     if (residence.isForSale()) {
       sign.setLine(2, ChatColor.YELLOW +
               StringUtil.trimLength(plugin.getConfiguration(signBlock.getWorld()).getSignSaleText(), 14));
 
       sign.setLine(3, ChatColor.YELLOW + StringUtil.trimLength(plugin.format(residence.getPrice()), 14));
     } else {
       sign.setLine(2, nation.getInhabitant(residence.getOwnerId()).getName());
 
       int likes = nation.getResidenceManager(residence).getLikes().size();
       if (likes > 0) {
         sign.setLine(3, likes + " Likes");
       } else {
         sign.setLine(3, "");
       }
     }
     sign.update();
   }
 }
