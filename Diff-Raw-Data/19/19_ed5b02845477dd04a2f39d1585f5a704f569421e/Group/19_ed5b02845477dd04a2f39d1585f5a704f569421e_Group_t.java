 package jsc_controller;
 
 import jsc_server.CantFindMachine;
 import jsc_server.Machine;
 import jsc_server.MenuItem;
 
 public class Group extends MenuItem {
 	private String gruppe_navn;
 	private MenuItem[] gruppe_innhold = new MenuItem[4];
 	private int gruppe_num = 0;
 	
 	public Group (String gruppe_navn) {
 		this.gruppe_navn = gruppe_navn;
 	}
 	
 	public void addContent (String innhold_navn) {
 		if(this.gruppe_num == this.gruppe_innhold.length) {
 			MenuItem[] tmp = new MenuItem[gruppe_innhold.length * 2];
 			for (int i = 0; i < gruppe_innhold.length; i++) {
 				tmp[i] = gruppe_innhold[i];
 			}
 			gruppe_innhold = tmp;
 		}
 		
 		try {
 			gruppe_innhold[gruppe_num] = new Machine(innhold_navn);
 			gruppe_num++;
 		} catch (CantFindMachine e) {
			System.out.println("Can't find config file for machine: " + innhold_navn);
 			gruppe_innhold[gruppe_num] = null;
 		}
 	}
 	
 	public void addContent (MenuItem maskin) {
 		if(this.gruppe_num == this.gruppe_innhold.length) {
 			MenuItem[] tmp = new MenuItem[gruppe_innhold.length * 2];
 			for (int i = 0; i < gruppe_innhold.length; i++) {
 				tmp[i] = gruppe_innhold[i];
 			}
 			gruppe_innhold = tmp;
 		}
 		
 		gruppe_innhold[gruppe_num] = maskin;
 		gruppe_num++;
 	}
 	
 	public MenuItem[] getContent () {
 		return gruppe_innhold;
 	}
 	
 	public int getContentNum () {
 		return gruppe_num;
 	}
 
 	public String toString () {
 		return gruppe_navn;
 	}
 	
 	public void wakeup () {
 		for (int i = 0; i < gruppe_num; i++) {
 			gruppe_innhold[i].wakeup();
 		}
 	}
 	
 	public void shutdown () {
 		for (int i = 0; i < gruppe_num; i++) {
 			gruppe_innhold[i].shutdown();
 		}
 	}
 	
 	public void reboot () {
 		for (int i = 0; i < gruppe_num; i++) {
 			gruppe_innhold[i].reboot();
 		}
 	}
 	
 	public String whenSelected () {
 		return "Valgt: gruppen " + this.toString();
 	}
 	
 	public String getType() {
 		return "gruppe";
 	}
 }
