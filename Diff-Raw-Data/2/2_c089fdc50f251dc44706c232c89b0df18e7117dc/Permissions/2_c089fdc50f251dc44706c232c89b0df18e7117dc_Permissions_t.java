 package com.github.lukevers.derp;
 
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 public class Permissions {
 	
	public Permission derp_add = new Permission("derp.add","Adds a derp to the list of derps",PermissionDefault.OP);
 	public Permission derp_reload = new Permission("derp.reload","Reloads the derp messages",PermissionDefault.OP);
 	
 	public Permissions() {
 		super();
 	}//close constructor
 
 }//close Permissions
