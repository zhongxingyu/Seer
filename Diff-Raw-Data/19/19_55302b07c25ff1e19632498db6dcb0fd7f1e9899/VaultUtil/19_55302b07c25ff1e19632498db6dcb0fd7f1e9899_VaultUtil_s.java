 /*
  * This file is part of ForceTools.
  *
  * © 2013 AlmuraDev <http://www.almuradev.com/>
  * ForceTools is licensed under the GNU General Public License.
  *
  * ForceTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ForceTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
/*
 * This file is part of CloudHeight.
 *
 * © 2013 AlmuraDev <http://www.almuradev.com/>
 * CloudHeight is licensed under the GNU General Public License.
 *
 * CloudHeight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CloudHeight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License. If not,
 * see <http://www.gnu.org/licenses/> for the GNU General Public License.
 */
 package com.almuradev.forcetools;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 public final class VaultUtil {
     private static final Economy economy;
     private static final Permission permission;
 
     static {
         final RegisteredServiceProvider<Economy> ersp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
         economy = ersp == null ? null : ersp.getProvider() == null ? null : ersp.getProvider();
         final RegisteredServiceProvider<Permission> prsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
         permission = prsp == null ? null : prsp.getProvider() == null ? null : prsp.getProvider();
     }
 
     public static double getBalance(String name) {
         if (!hasEconomy()) {
             throw new IllegalStateException("Tried to perform economy actions but no economy service installed!");
         }
         return economy.getBalance(name);
     }
 
     public static void add(String name, double amount) {
         if (!hasEconomy()) {
             throw new IllegalStateException("Tried to perform economy actions but no economy service installed!");
         }
         if (amount > 0) {
             economy.depositPlayer(name, amount);
         } else if (amount < 0) {
             economy.withdrawPlayer(name, Math.abs(amount));
         }
     }
 
     public static boolean hasBalance(String name, double balance) {
         if (!hasEconomy()) {
             throw new IllegalStateException("Tried to perform economy actions but no economy service installed!");
         }
         return economy.has(name, balance);
     }
 
     public static boolean hasPermission(String name, String world, String perm) {
         if (!hasPermissions()) {
             throw new IllegalStateException("Tried to perform permission actions but no permission service installed!");
         }
         return permission.has(world, name, perm);
     }
 
     public static boolean hasEconomy() {
         return economy != null;
     }
 
     public static boolean hasPermissions() {
         return permission != null;
     }
 }
