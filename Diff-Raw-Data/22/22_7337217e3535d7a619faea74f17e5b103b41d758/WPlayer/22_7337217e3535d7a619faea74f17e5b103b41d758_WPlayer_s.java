 package com.dre.warn;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class WPlayer {
	private P p = P.p;
	
 	public static Set<WPlayer> WPlayers=new HashSet<WPlayer>();
 	
 	public String player;
 	private int Verwarnpunkt;
 	public long ResTime;
 	
 	public String Grundliste = "";
 	
 	public int reasonzahl;
 	public String[] reason = new String[0];
 	public String[] von = new String[0]; 
 	public String[] datum = new String[0];
 	public String[] position = new String[0];
 	
 	public WPlayer(){
 		WPlayers.add(this);
 	}
 	
 	public void addPunkt(){
 		this.setVerwarnpunkt(this.getVerwarnpunkt()+1);
 		this.ResTime=System.currentTimeMillis();
		p.save();
 	}
 	public void remPunkt(){
 		this.setVerwarnpunkt(this.getVerwarnpunkt()-1);
 		
 		if(this.getVerwarnpunkt() < 0){
 			this.setVerwarnpunkt(0);
 		}
		p.save();
 	}
 	
 	public void setName(String name){
 		this.player=name;
 	}
 
 	public int getVerwarnPunkte() {
 		return this.getVerwarnpunkt();
 	}
 
 	public int getVerwarnpunkt() {
 		return Verwarnpunkt;
 	}
 
 	public void setVerwarnpunkt(int verwarnpunkt) {
 		Verwarnpunkt = verwarnpunkt;
 	}
 }
