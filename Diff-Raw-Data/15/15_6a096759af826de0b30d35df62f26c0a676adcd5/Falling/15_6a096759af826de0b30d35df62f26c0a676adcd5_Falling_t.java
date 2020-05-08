 package com.tacoid.puyopuyo.logic;
 
 public class Falling {
 	private Coord initial;
 	private Coord end;
 	private float remaining;
 	private boolean chute;
 	
 	public Falling(Coord initial, int remaining) {
 		this.chute = true;
 		this.initial = initial;
 		this.remaining = remaining;
 		this.end = new Coord(initial.l - remaining, initial.c, initial.coul);
 	}
 	
 	public void update(float delta) {
		// La chute doit prendre moins de 0.5s. Sachant qu'on a 12 lignes, ça nous donne 24.
 		if (chute) {
 			if (this.remaining > -0.5) {
				this.remaining -= 24 * delta;
 				if (this.remaining < -0.5) {
 					this.remaining = -0.5f;
 				}
 			} else {
 				chute = false;
 			}
 		} else {
 			if (this.remaining < 0) {
				this.remaining += 24 * delta;
 			} else if (this.remaining > 0) {
 				this.remaining = 0;
 			}
 		}
 	}
 
 	public Coord getInitial() {
 		return initial;
 	}
 
 	public float getRemaining() {
 		return remaining;
 	}
 	
 	public Coord getEnd() {
 		return end;
 	}
 }
