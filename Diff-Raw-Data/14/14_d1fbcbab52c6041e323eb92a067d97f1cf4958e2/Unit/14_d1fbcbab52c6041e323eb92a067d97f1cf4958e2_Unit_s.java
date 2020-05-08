 package com.me.Roguish.Model;
 
 public abstract class Unit extends Entity{
 	public Unit(int x, int y, String texture) {
 		super(x, y, texture);
		// TODO Auto-generated constructor stub
 	}
 
 	private static int baseHP;		// Determined by Class/Race combo
   	private int maxHP;				// Calculated
 	private int currentHP;
 	private static int baseHealth; 	// Determined by Class/Race combo
 	private int maxMana;
 	private int currentMana;
 	private boolean alive;			// Should be True is currentHealth <= 0
 	private int[] status;			// Most likely an array with status effects
 	private int def;				// ??? not sure yet - like AC in DCSS/NetHack (Could be based on
 									// dex levels?)
 	private int res;				// resistance to magic
 	//Attributes
 	private static int baseInt;		// Base stats
 	private static int baseDex;		// Base stats
 	private static int baseStr;		// Base stats
 
 	public void setHP(int HP){
 		this.currentHP = HP;
 	}
 	
 	public void setMana(int mana){
 		this.currentMana = mana;
 	}
 	
 	public void updateDead(boolean a){
 		this.alive = a;
 	}
 	
 	public void setStatus(int a, int b){
 		// Finish
 	}
 	
 	public int getHP(){
 		return this.currentHP;
 	}
 	
 	public int getMaxHP(){
 		return this.maxHP;
 	}
 	
 	public int getMana(){
 		return this.currentMana;
 	}
 	
 	public int getMaxMana(){
 		return this.maxMana;
 	}
 	
 	public boolean isAlive(){
 		return this.alive;
 	}
 	
 	public int getDef(){
 		return this.def;
 	}
 
 	public int getRes(){
 		return this.res;
 	}
 
 	public int getStr(){
 		return this.baseStr;
 	}
 
 	public int getDex(){
 		return this.baseStr;
 	}
 	
 	public int getInt(){
 		return this.baseStr;
 	}
 
 }
