 package main;
 
 import java.util.*;
 
 public class TriSolitaire {
 	
 	public static void main(String args[]) {
 		Game peggy = new Game(args[0]);
		System.out.println("Your original board:");
 		peggy.printBoard();
		System.out.println();
 		//peggy.printValidMoves();
		
 		if (peggy.solve()) {
 			System.out.println("SOLVED!");
 			System.out.println("Solution path:");
 			while(!peggy.path.empty()) {
 				((Board) peggy.path.pop()).printBoard();
				System.out.println("=============");
 			}
 		} else {
 			System.out.println("Did not find a top single peg solution");
 		}
 	}
 }
