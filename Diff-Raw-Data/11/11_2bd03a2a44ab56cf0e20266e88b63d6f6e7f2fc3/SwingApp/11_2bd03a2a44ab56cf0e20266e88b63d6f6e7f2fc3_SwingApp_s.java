 package com.crudetech.tictactoe.client.swing;
 
 import javax.swing.*;
 
 public class SwingApp {
    public static void main(String[] args) {
         setSystemLookAndFeel();
 
         final JFrame frame = new TicTacToeGameForm();
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 frame.setVisible(true);
             }
         });
     }
 
    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
     }
 }
