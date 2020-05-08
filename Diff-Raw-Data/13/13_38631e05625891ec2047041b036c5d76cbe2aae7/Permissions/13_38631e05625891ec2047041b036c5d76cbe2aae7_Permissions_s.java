 package com.nijikokun.bukkit.Permissions;
 
 import java.util.logging.Logger;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 
import to.joe.J2Plugin;
 
 public class Permissions extends JavaPlugin {
 
	private J2Plugin j2;
 	private boolean nope=false;
 	
 	@Override
 	public void onDisable() {
 
 	}
 
 	@Override
 	public void onEnable() {
		J2Plugin p = null;
 		Plugin test = this.getServer().getPluginManager().getPlugin("j2Plugin");
		if(test != null && test instanceof J2Plugin) {
			p = (J2Plugin)test;
 		}
 		if(p == null) {
 			Logger.getLogger("Minecraft").warning("Failed to find J2Plugin. Oh bother.");
 			nope=true;
 		}
 
 		j2 = p;
 		Logger.getLogger("Minecraft").info("FakePermissions is go. Found version "+j2.getDescription().getVersion());
 	}
 	
 	public boolean has(Player player, String permission) {
 		return this.permission(player, permission);
 	}
 
 	public boolean permission(Player player, String permission) {
 		if(this.j2.debug){
 			this.j2.log.info("FakePerm: Does "+player.getName()+" have "+permission+"?");
 		}
 		if(this.nope){
 			return false;
 		}
 		return j2.perms.permCheck(player.getName(), permission);
 	}
 	
 	public boolean inGroup(String name,String group){
 		if(this.j2.debug){
 			this.j2.log.info("FakePerm: Is "+name+" in "+group+"?");
 		}
 		if(this.nope){
 			return false;
 		}
 		return j2.perms.inGroup(name,group);
 	}
 	
 	public PermissionHandler getHandler(){
 		return Security;
 	}
 	
 	private final PermissionHandler Security=new PermissionHandler(this);
 	
 }
