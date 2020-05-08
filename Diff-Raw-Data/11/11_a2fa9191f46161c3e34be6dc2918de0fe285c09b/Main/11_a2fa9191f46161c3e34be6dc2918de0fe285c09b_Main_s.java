 package com.cmendenhall;
 
 public class Main {
 
     public static void main(String[] args) {
         try {

            View view = new SwingView();
             GameController controller = new GameController(view);
 
            view.displayMessage("Hello world");
            view.displayBoard(new GameBoard());
            while (true) {}

         } catch (Exception e) {
             System.exit(0);
         }
     }
 }
