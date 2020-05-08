 package com.Grateds.Reversi;
 
 import java.util.ArrayList;
 import com.Grateds.Reversi.AI.AI;
 import com.Grateds.Reversi.CONTROLLER.*;
 import com.Grateds.Reversi.GUI.MainWindow;
 import com.Grateds.Reversi.SAVEANDLOAD.SaveAndLoad;
 
 public class App {
 
     public static void main( String[] args ){
     	 Controller controller = new Controller();
     	 controller.start_game();
     	 controller.set_piece(2,4,1);
     	 controller.set_piece(3,4,1);
     	 controller.drawBoard();
     	 System.out.println("===============");
    	 System.out.println("Score BLACK PIECES = "+controller.getBlackScore());
    	 System.out.println("Score WHITE PIECES = "+controller.getWhiteScore());  
     	 
     	 AI a = new AI();
     	 ArrayList<Integer> list = a.findValidMove(controller.getBoard(), 1, false);
     	 int i=0;
     	 System.out.println("Possibles Valid Moves");
     	 while(i<list.size()){
     		 System.out.println("("+list.get(i)+","+list.get(i+1)+")");
     		 i = i + 2;
     	 }
    	 System.out.println(a.isValidMove(controller.getBoard(), 1, 3, 2)); // isValidMove(b, 1, 3, 2) should return: false
    	 System.out.println(a.isValidMove(controller.getBoard(), 2, 4, 5)); // isValidMove(b, 2, 4, 5) should return: true
    	 System.out.println(a.getMoveList());
    	 System.out.println(a.findValidMove(controller.getBoard(), 2, true));
    }
 }
 
 
 
 
 
 
 
 
 
