 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of VinciCode.
  * 
  * VinciCode is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * VinciCode is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VinciCode.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.vincicode.listener;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.minestarlibrary.bookapi.MinestarBook;
 import de.minestar.minestarlibrary.messages.Message;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 import de.minestar.vincicode.core.VinciCodeCore;
 import de.minestar.vincicode.data.MailBox;
 import de.minestar.vincicode.util.BookHelper;
 
 public class ActionListener implements Listener {
 
     @EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (VinciCodeCore.messageManger.hasNewMessage(event.getPlayer().getName())) {
             PlayerUtils.sendInfo(event.getPlayer(), VinciCodeCore.NAME, "Sie haben Post.");
         }
     }
 
     @EventHandler
     public void onItemHeldChange(PlayerItemHeldEvent event) {
         if (event.getPlayer().isSneaking()) {
             ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
             if (itemStack != null && itemStack.getType().equals(Material.WRITTEN_BOOK)) {
                 MinestarBook book = MinestarBook.loadBook(itemStack);
                 if (book.getAuthor().equalsIgnoreCase(MailBox.MAIL_BOX_HEAD)) {
                     // get mailbox
                     MailBox mailBox = VinciCodeCore.messageManger.getMailBox(event.getPlayer().getName());
                     if (mailBox == null) {
                         PlayerUtils.sendError(event.getPlayer(), VinciCodeCore.NAME, "Du hast keine Nachrichten!");
 
                         // SWAP ITEMS
                         ItemStack newSlot = event.getPlayer().getInventory().getItem(event.getNewSlot());
                         ItemStack oldSlot = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
                         if (newSlot != null && !newSlot.getType().equals(Material.AIR)) {
                             newSlot = newSlot.clone();
                         }
 
                         // finally swap
                         event.getPlayer().getInventory().setItem(event.getNewSlot(), oldSlot);
                         event.getPlayer().getInventory().setItem(event.getPreviousSlot(), newSlot);
                         return;
                     }
 
                     boolean forward = false;
                     if (event.getNewSlot() == 0 && event.getPreviousSlot() == 8) {
                         forward = true;
                     } else if (event.getNewSlot() > event.getPreviousSlot() && (event.getPreviousSlot() != 0)) {
                         forward = true;
                     }
 
                     // get changed items
                     ItemStack newSlot = event.getPlayer().getInventory().getItem(event.getNewSlot());
                     ItemStack oldSlot = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
                     if (newSlot != null && !newSlot.getType().equals(Material.AIR)) {
                         newSlot = newSlot.clone();
                     }
 
                     if (forward) {
                         if (mailBox.hasNext()) {
                             Message message = mailBox.next();
                             book.setPages(BookHelper.getPages(message));
                            PlayerUtils.sendInfo(event.getPlayer(), VinciCodeCore.NAME, "Nachricht " + mailBox.getIndex() + " von " + mailBox.getMessageCount());
                         } else {
                             PlayerUtils.sendError(event.getPlayer(), VinciCodeCore.NAME, "Keine weiteren Nachrichten.");
                         }
                     } else {
                         if (mailBox.hasPrev()) {
                             Message message = mailBox.prev();
                             book.setPages(BookHelper.getPages(message));
                            PlayerUtils.sendInfo(event.getPlayer(), VinciCodeCore.NAME, "Nachricht " + mailBox.getIndex() + " von " + mailBox.getMessageCount());
                         } else {
                             PlayerUtils.sendError(event.getPlayer(), VinciCodeCore.NAME, "Keine vorherigen Nachrichten.");
                         }
                     }
 
                     // SWAP ITEMS
                     event.getPlayer().getInventory().setItem(event.getNewSlot(), oldSlot);
                     event.getPlayer().getInventory().setItem(event.getPreviousSlot(), newSlot);
                 }
             }
         }
 
     }
 }
