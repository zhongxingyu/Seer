 package main;
 
 import java.util.*;
 
 public class TriSolitaire {
 	
 	public static void main(String args[]) {
 		Game peggy = new Game(args[0]);
 		peggy.printBoard();
 		//peggy.printValidMoves();
 		if (peggy.solve()) {
 			System.out.println("SOLVED!");
 			System.out.println("Solution path:");
 			while(!peggy.path.empty()) {
 				((Board) peggy.path.pop()).printBoard();
				System.out.println("======");
 			}
			/*if (peggy.path.empty()) {
				System.out.println("wtf...");
			}*/
 		} else {
 			System.out.println("Did not find a top single peg solution");
 		}
 	}
 }
