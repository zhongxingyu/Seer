 package com.github.propra13.gruppe43;
 import java.awt.image.*;
 import java.util.*;
 public class Actor{
 	// energy wird bentigt um zu handeln
 	final int ENERGY_MAX = 1000;
 	public int energy = ENERGY_MAX;
 	public int energyGain = ENERGY_MAX / 300;
 	// Aktion, die ausgefhrt wird
 	public int currentAction = Actor.MOVE;
 	//Aktionen
 	final static int MOVE = 0;
 	
 	//Bewegungsrichtung:
 	public int movex, movey;
 	
 	//Feld auf dem sich Actor befindet
 	Field field = null;
 	
 	public Actor() {
 	}
 	//bewegt den Actor aus das Zielfeld, gibt true aus, falls erfolgreich, sonst false
 	//entry gibt an, ob die Methode onEntry des Zielfeldes ausgelst wird
 	public boolean move(Field t, boolean entry) {
 		if (!t.isOccupied() ) {
 			t.actor = this;
 			if (field != null) {
 			field.actor = null;
 			}
 			this.field = t;
 			if (entry) t.onEntry();
 			return true;
 					
 		}
 		return false;
 	}
 
 	
	public void act() {
		
	}
 	
 
 }
