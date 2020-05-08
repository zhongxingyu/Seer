package demo;
 
 import java.util.Scanner;
 
 public class DemoTextGame {
 
 	public static void main(String[] args) {
 		Scanner s = new Scanner(System.in);
 		System.out.println("Welcome to the GAME OF LIFE!!!!!!");
 
 		while (true) {
 			System.out.println("Are you ready to play? (y/n)");
 			String input = s.nextLine();
 			if (input.equals("y")) {
 				System.out.println("You are born! Congrats!");
 			} else if (input.equals("n")) {
 				System.out.println("Go play outside.");
 				System.exit(0);
 			} else {
 				System.out.println("Your amibiguity confuses me.");
 			}
 		}
 
 	}
 }
