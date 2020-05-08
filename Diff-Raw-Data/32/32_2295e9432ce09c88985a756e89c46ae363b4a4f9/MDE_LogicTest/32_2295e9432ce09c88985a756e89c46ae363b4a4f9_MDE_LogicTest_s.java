 package Testing;
 
 import javax.swing.JOptionPane;
 
 import game.*;
 
 /**
  * Miniprogramm zum Testen der Board-Funktionalitaet
  * Zwei menschliche Spieler knnen gegeneinander antreten
  * @author Michi
  *
  */
 public class MDE_LogicTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		Board my_board = new Board(7, 6);
 		
 		my_board.print();
 		
 		Player currentPlayer = Player.O;
 		
 		//solange spielen, bis ein Gewinner feststeht; Patt wird noch nicht erkannt
 		while (my_board.findWinner() == null) {
 			currentPlayer = (currentPlayer == Player.O) ? Player.X : Player.O;
 			
 			System.out.println("\n\n\n\n\n\n");
 			System.out.println("Am Zug: Spieler " + currentPlayer);
 			my_board.print();
 			
 			String input = JOptionPane.showInputDialog("Spalte: ");
 			
 			//korrekten input erzwingen
 			 while (input == null || input.length() < 1) {
 				input = JOptionPane.showInputDialog("Spalte: ");
 			}
 			
 			 //solange neu werfen lass bis ein gueltiger Zug gemacht wurde
 			while (!my_board.makeDrop(currentPlayer, Integer.valueOf(input))) {
 				JOptionPane.showMessageDialog(null, "Ungueltiger Zug!");
 				input = JOptionPane.showInputDialog("Spalte: ");
 				while (input == null || input.length() < 1) {
 					input = JOptionPane.showInputDialog("Spalte: ");
 				}
 			}
 			
 		}
 		
 		Player winner = my_board.findWinner();
 		System.out.println("\n\n\n\n\n\n");
 		my_board.print();
 		System.out.println("Spieler " + winner + " hat gewonnen!");
 	}
 
 }
