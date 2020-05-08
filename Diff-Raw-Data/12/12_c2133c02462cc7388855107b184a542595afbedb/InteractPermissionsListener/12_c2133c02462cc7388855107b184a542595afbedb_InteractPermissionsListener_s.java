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
 import at.co.hohl.myresidence.PermissionsResolver;
 import at.co.hohl.myresidence.translations.Translate;
 import org.bukkit.event.player.*;
 
 /**
  * Listens to interactions of the player to the world like opening chests, using buckets or lighters.
  *
  * @author Michael Hohl
  */
 public class InteractPermissionsListener extends PlayerListener {
   private final PermissionsResolver permissionsResolver;
 
   /**
    * Creates a new InteractPermissionsListener.
    *
    * @param permissionsResolver permissions resolver to use for retrieving permissions.
    */
   public InteractPermissionsListener(PermissionsResolver permissionsResolver) {
     this.permissionsResolver = permissionsResolver;
   }
 
   @Override
   public void onPlayerBucketFill(PlayerBucketFillEvent event) {
     if (event.isCancelled()) {
       return;
     }
 
     if (!permissionsResolver.isAllowedToInteractWithBlock(event.getPlayer(),
            event.getPlayer().getLocation().getBlock())) {
       Chat.sendMessage(event.getPlayer(), Translate.get("not_allowed_to_fill_bucket_here"));
       event.setCancelled(true);
     }
   }
 
   @Override
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
     if (event.isCancelled()) {
       return;
     }
 
     if (!permissionsResolver.isAllowedToInteractWithBlock(event.getPlayer(),
            event.getPlayer().getLocation().getBlock())) {
       Chat.sendMessage(event.getPlayer(), Translate.get("not_allowed_to_empty_bucket_here"));
       event.setCancelled(true);
     }
   }
 
   @Override
   public void onPlayerBedEnter(PlayerBedEnterEvent event) {
     if (event.isCancelled()) {
       return;
     }
 
     if (!permissionsResolver.isAllowedToInteractWithBlock(event.getPlayer(),
             event.getPlayer().getLocation().getBlock())) {
       Chat.sendMessage(event.getPlayer(), Translate.get("not_allowed_to_sleep_here"));
       event.setCancelled(true);
     }
   }
 
   @Override
   public void onPlayerFish(PlayerFishEvent event) {
     if (event.isCancelled()) {
       return;
     }
 
     if (!permissionsResolver.isAllowedToInteractWithBlock(event.getPlayer(),
             event.getPlayer().getLocation().getBlock())) {
       Chat.sendMessage(event.getPlayer(), Translate.get("not_allowed_to_fish_here"));
       event.setCancelled(true);
     }
   }
 
 }
