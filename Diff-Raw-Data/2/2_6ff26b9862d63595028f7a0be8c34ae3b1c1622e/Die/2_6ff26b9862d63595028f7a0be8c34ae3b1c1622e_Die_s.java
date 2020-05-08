 import java.util.*;
 import java.io.*;
 
 public class Die implements Rollable {
 	
 	private int sides; 
 	private int lastRoll;
 	private Random r;
 	
 	public Die() {
		this.sides = 0;
 		r = new Random();
 		roll();
 	}
 	
 	public Die(int sides) {
 		this.sides = sides;
 		r = new Random();
 		roll();
 	}
 	
 	
 	public int sides() {
 		return sides;
 	}
 	
 	public int criticalValue() {
 		return sides;
 	}
 	
 	public void roll() {
 		lastRoll = r.nextInt(sides) + 1;
 	}
 	
 	public int lastRoll() {
 		return lastRoll();
 	}
 
 }
