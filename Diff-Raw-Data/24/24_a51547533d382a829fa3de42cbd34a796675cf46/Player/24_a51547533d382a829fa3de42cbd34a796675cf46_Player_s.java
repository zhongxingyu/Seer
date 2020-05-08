 package com.github.propra13.gruppe43;
 
 import java.awt.image.BufferedImage;
 
 public class Player extends Actor{
 	
 	
 	public Player() {
 		
 	}
 	//erhht energy, wenn ENERGY_MAX erreicht ist, wird energy verwendet um eine Aktion durchzufhren.
 	public void act() {
 		if (energy < ENERGY_MAX) {
 			energy+=energyGain;
 		}
 		else {
 			energy = ENERGY_MAX;
 			switch (currentAction) {
 			case Actor.MOVE:
				
 				if ((movex != 0 || movey != 0) &&  this.field.x+movex < this.field.level.size_x && this.field.y+movey < this.field.level.size_y && this.field.x+movex >=0 && this.field.y+movey >=0) {
 				if (this.move(this.field.level.getField(this.field.x+movex, this.field.y+movey))) energy-=100;
 				
 			}
 				break;
 			}
 		}
 		
 	}
 	
 	//bewegt den Actor aus das Zielfeld, gibt true aus, falls erfolgreich, sonst false
 	public boolean move(Field t, boolean entry) {
 		if (t.isWalkable()) {
 			return super.move(t, entry);
 					
 		}
 		return false;
 	}
 	
 	public boolean move(Field t) {
 		return this.move(t, true);
 	}
 
 }
