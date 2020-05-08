 package animal;
 
public class Bunny extends Animal{
 
 	private int bunnyEnergy;
 	
 	public Bunny() {
 		bunnyEnergy=10;
 	}
 	
 	public String sound() {
 		bunnyEnergy--;
 		return "Pip, pip, gnag!";
 	}
 	
 	public int eat() {
 		bunnyEnergy += 5;
 		return bunnyEnergy;
 	}
 	
 	public void bunnyJump() {
 		System.out.println("Hopp! Tjooo!");
 		bunnyEnergy -= 2;
 	}
 }
