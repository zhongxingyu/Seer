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
 
 package de.minestar.vincicode.data;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.inventory.Inventory;
 
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.vincicode.core.VinciCodeCore;
 import de.minestar.vincicode.data.subscription.Subscription;
 
 public class LetterBox {
 
     private Inventory inventory;
     private Set<Subscription> subscriptions;
 
     // INGAME CONSTRUCTOR FOR A NEW LETTER BOX
     public LetterBox(Inventory inventory) {
         this(inventory, new HashSet<Subscription>());
     }
 
    // 
     public LetterBox(Inventory inventory, Set<Subscription> subscriptions) {
         this.inventory = inventory;
         this.subscriptions = subscriptions;
     }
 
     public void deliverSubscriptions() {
         for (Subscription subscription : subscriptions) {
             if (inventoryFull(inventory)) {
                 ConsoleUtils.printError(VinciCodeCore.NAME, "Kann Abonnement nicht ausliefern, da Inventar voll!");
                 break;
             }
             subscription.deliver(inventory);
         }
     }
 
     private boolean inventoryFull(Inventory inventory) {
         return inventory.firstEmpty() == -1;
     }
 
     public void registerSubscription(Subscription subscription) {
         this.subscriptions.add(subscription);
     }
 
     public void unregisterSubscription(Subscription subscription) {
         this.subscriptions.add(subscription);
     }
 
     public Set<Subscription> getSubscriptions() {
         return new HashSet<Subscription>(subscriptions);
     }
 }
