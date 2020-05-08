 package net.eonz.bukkit.psduo.permissions;
 
 /*
  * This code is Copyright (C) 2011 Chris Bode, Some Rights Reserved.
  *
  * Copyright (C) 1999-2002 Technical Pursuit Inc., All Rights Reserved. Patent 
  * Pending, Technical Pursuit Inc.
  *
  * Unless explicitly acquired and licensed from Licensor under the Technical 
  * Pursuit License ("TPL") Version 1.0 or greater, the contents of this file are 
  * subject to the Reciprocal Public License ("RPL") Version 1.1, or subsequent 
  * versions as allowed by the RPL, and You may not copy or use this file in 
  * either source code or executable form, except in compliance with the terms and 
  * conditions of the RPL.
  *
  * You may obtain a copy of both the TPL and the RPL (the "Licenses") from 
  * Technical Pursuit Inc. at http://www.technicalpursuit.com.
  *
  * All software distributed under the Licenses is provided strictly on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TECHNICAL
  * PURSUIT INC. HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
  * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the Licenses for specific 
  * language governing rights and limitations under the Licenses. 
  */
 
 import org.bukkit.entity.Player;
 
 public class BukkitPermissionsModule implements PermissionsInterface {
 	final String pluginName;
 	private boolean groupNag = false;
 
 	public BukkitPermissionsModule(String pluginName) {
 		this.pluginName = pluginName;
 	}
  
 	public boolean has(Player player, String permission, String world) {
		String fullPermission = pluginName + '.' + permission;
		return player.hasPermission(fullPermission);
 	}
 
 	public boolean inGroup(Player player, String group, String world) {
 		if (!groupNag) {
 			System.out.println(pluginName + "-Permissions: A call to the group functionality of my permissions interface was called, but you are using default bukkit permissions. Group functionality will fail safely and silently.");
 			groupNag = true;
 		}
 		return false;
 	}
 
 }
