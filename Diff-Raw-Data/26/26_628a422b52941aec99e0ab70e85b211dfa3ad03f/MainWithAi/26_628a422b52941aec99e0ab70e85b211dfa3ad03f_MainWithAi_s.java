 package jothello;
 
 import java.awt.Point;
 
 public class MainWithAi {
 	public static void main(String[] args) {
		Ai ai_1 = new Ai(Ai.MINIMAX_ALPHA_BETA); // dark
		ai_1.setMinimaxTime(12);
 		Ai ai_2 = new Ai(Ai.MINIMAX_ALPHA_BETA); // light
		ai_2.setMinimaxTime(12);
 		Jothello jothello = new Jothello();
 		int count = 0;
 		do {	
 			jothello.printBoard();
 			byte turn = jothello.getTurn();		
 			Point p = null;
 			if(turn == State.DARK) {
 				p = ai_1.selectMove(jothello);
 			}else{
 				p = ai_2.selectMove(jothello);
 			}
 				
 			jothello.putPiece(p.y, p.x);
 			System.out.printf("P%d langkah ke [%d, %d] sukses%n", turn, p.y, p.x);
 			count++;			
 		}while(jothello.whoWin() == State.NONE);
 		System.out.printf("turns : %d , ", count);
 		System.out.println("winner : " + (jothello.whoWin() == State.DARK ? "Dark" : "Light"));				
 	}
 }
