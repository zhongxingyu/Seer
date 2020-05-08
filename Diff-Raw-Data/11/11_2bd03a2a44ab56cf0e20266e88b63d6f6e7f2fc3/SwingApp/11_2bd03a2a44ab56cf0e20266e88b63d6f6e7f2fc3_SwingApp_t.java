 package com.crudetech.tictactoe.client.swing;
 
 import javax.swing.*;
 
 public class SwingApp {
    public static void main(String[] args) throws Exception {
         setSystemLookAndFeel();
 
         final JFrame frame = new TicTacToeGameForm();
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 frame.setVisible(true);
             }
         });
     }
 
    private static void setSystemLookAndFeel() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
     }
 }
