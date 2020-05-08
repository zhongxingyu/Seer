 /**
  * Author: Andy Pacher
 * Date: May 20th, 2011
  * Assignment: House.java
  */
 
 public class House {
 	public static void main(String[] args) {
 		House.addRoof();
 		House.addBase();
 		House.addWalk();
 	}
 	/**
 	 * pre: none
 	 * post: roof will be displayed
 	 */
 	public static void addRoof() {
 		System.out.println("   /\\   ");
 		System.out.println("  /  \\  ");
 		System.out.println(" /    \\ ");
 		System.out.println("/______\\");
 	}
 	/**
 	 * pre: none
 	 * post: base will be displayed
 	 */
 	public static void addBase() {
 		System.out.println("|      |");
 		System.out.println("|      |");
 		System.out.println("|      |");
 		System.out.println("|______|");
 	}
 	/**
 	 * pre: none
 	 * post: walk will be displayed
 	 */
 	public static void addWalk() {
 		System.out.println("   **   ");
 		System.out.println("   **********");
 	}
 }
