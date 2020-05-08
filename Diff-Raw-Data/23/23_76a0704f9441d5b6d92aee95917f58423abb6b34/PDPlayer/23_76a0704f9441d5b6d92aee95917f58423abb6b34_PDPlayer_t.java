 package de.brightstorm;
 
 import org.bukkit.entity.Player;
 
 public class PDPlayer{
 	private String group;
 	private Player p;
	private boolean ignore=false;
 	
 	public PDPlayer(Player p) {
 		this.p=p;
 	}
 	
 	public void findGroup() {
		ignore=true;
 		for(String group : payday.groups) {
 			if(p.hasPermission("payday."+group)) {
 				this.group=group;
				ignore=false;
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
	public boolean ignore() {
		return ignore;
	}
 }
