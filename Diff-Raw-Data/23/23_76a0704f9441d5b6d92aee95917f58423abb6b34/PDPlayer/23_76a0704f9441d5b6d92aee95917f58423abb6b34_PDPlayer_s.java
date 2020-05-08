 package de.brightstorm;
 
 import org.bukkit.entity.Player;
 
 public class PDPlayer{
 	private String group;
 	private Player p;
 	
 	public PDPlayer(Player p) {
 		this.p=p;
 	}
 	
 	public void findGroup() {
 		for(String group : payday.groups) {
 			if(p.hasPermission("payday."+group)) {
 				this.group=group;
 				return;
 			}
 		}
 	}
 	public String getGroup() {
 		return this.group;
 	}
 	public Player getPlayer() {
 		return p;
 	}
 }
