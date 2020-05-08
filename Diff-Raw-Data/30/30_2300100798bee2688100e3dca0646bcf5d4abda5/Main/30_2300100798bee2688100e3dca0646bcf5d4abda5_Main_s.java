 package com.cmendenhall;
 
 import javax.swing.*;
 
 public class Main {
 
     public static void main(String[] args) {
         try {
            /*View view = new TerminalView();
             Controller controller = new GameController(view);
 
             controller.newGame();
             controller.setUp();
            controller.startGame();*/
            new SwingView();
            while (true) {}
         } catch (Exception e) {
             System.exit(0);
         }
     }
 }
