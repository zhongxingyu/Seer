 package com.cmendenhall;
 
 public class Main {
 
     public static void main(String[] args) {
         try {
            View view = new TerminalView();
             GameController controller = new GameController(view);
 
            controller.newGame();
            controller.setUp();
            controller.startGame();
         } catch (Exception e) {
             System.exit(0);
         }
     }
 }
