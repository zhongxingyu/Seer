 /*
  * Copyright (C) 2013 Lord_Ralex
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
 package com.lordralex.totalpermissions.reflection;
 
 import com.lordralex.totalpermissions.TotalPermissions;
 import java.util.Set;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.PermissibleBase;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.permissions.PermissionAttachmentInfo;
 import org.bukkit.plugin.Plugin;
 
 /**
  * @author Lord_Ralex
  * @version 0.1
  * @since 0.1
  */
 public class TPPermissibleBase extends PermissibleBase {
 
    protected PermissibleBase initialParent;
 
    public TPPermissibleBase(PermissibleBase parent) {
         super(parent);
         initialParent = parent;
     }
 
     @Override
     public PermissionAttachment addAttachment(Plugin plugin) {
         return initialParent.addAttachment(plugin);
     }
 
     @Override
     public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
         return super.addAttachment(plugin, ticks);
     }
 
     @Override
     public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
         return super.addAttachment(plugin, name, value);
     }
 
     @Override
     public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
         return super.addAttachment(plugin, name, value, ticks);
     }
 
     @Override
     public synchronized void clearPermissions() {
         super.clearPermissions();
     }
 
     @Override
     public boolean equals(Object obj) {
         return initialParent.equals(obj);
     }
 
     @Override
     public Set<PermissionAttachmentInfo> getEffectivePermissions() {
         return super.getEffectivePermissions();
     }
 
    public PermissibleBase getInitialParent() {
         return initialParent;
     }
 
     @Override
     public boolean hasPermission(Permission perm) {
         boolean has = hasPermission(perm.getName());
         if (initialParent instanceof CommandSender) {
             CommandSender cs = (CommandSender) initialParent;
             TotalPermissions.getPlugin().getLogger().info("Checking if " + cs.getName() + " has " + perm.getName() + ": " + has);
         } else {
             TotalPermissions.getPlugin().getLogger().info("Checking for " + perm.getName() + ": " + has);
         }
         return has;
     }
 
     @Override
     public boolean hasPermission(String inName) {
         return initialParent.hasPermission(inName);
     }
 
     @Override
     public boolean isOp() {
         return initialParent.isOp();
     }
 
     @Override
     public int hashCode() {
         return initialParent.hashCode();
     }
 
     @Override
     public boolean isPermissionSet(Permission perm) {
         return initialParent.isPermissionSet(perm);
     }
 
     @Override
     public boolean isPermissionSet(String name) {
         return initialParent.isPermissionSet(name);
     }
 
     @Override
     public void recalculatePermissions() {
         initialParent.recalculatePermissions();
     }
 
     @Override
     public void removeAttachment(PermissionAttachment attachment) {
         initialParent.removeAttachment(attachment);
     }
 
     public void setInitialParent(PermissibleBase initialParent) {
         this.initialParent = initialParent;
     }
 
     @Override
     public void setOp(boolean value) {
         super.setOp(value);
     }
 
     @Override
     public String toString() {
         return super.toString();
     }
 }
