 import java.util.*;
 import java.io.*;
 
 public class Die implements Rollable {
 	
 	private int sides; 
 	private int lastRoll;
 	private Random r;
 	
 	public Die() {
		do {
			Scanner console = new Scanner(System.in);
			System.out.print("How many sides are on this die ( > 1)? ");
			sides = console.nextInt();
		} while (sides < 1);
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
