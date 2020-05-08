 package com.fjarverandi.tictactoe;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class GUI extends JFrame
 {
	private static final long serialVersionUID = 1L;
 	
	private iEngine ie;
     private TicTacToePanel[] panels;
 
     public GUI(iEngine ie)
     {
         this.ie = ie;
 
         final GridLayout layout = new GridLayout(3,3);
         this.setLayout(layout);
 
         panels = new TicTacToePanel[9];
         for(int i = 0; i < panels.length; i++)
         {
             panels[i] = new TicTacToePanel(this, ie, i);
             this.add(panels[i]);
         }
 
         JMenuBar menuBar = new JMenuBar();
         this.setJMenuBar(menuBar);
 
         JMenu fileMenu = new JMenu("File");
         JMenuItem newAction = new JMenuItem("New Game");
         JMenuItem quitAction = new JMenuItem("Quit");
         fileMenu.add(newAction);
         fileMenu.add(quitAction);
         menuBar.add(fileMenu);
 
         newAction.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 NewGame();
             }
         });
 
         quitAction.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 QuitGame();
             }
         });
 
         //JMenu editMenu = new JMenu("Edit");
         // items go here
         //menuBar.add(editMenu);
 
         this.setTitle("TicTacToe!");
         this.setSize(600, 600);
         this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         this.setVisible(true);
     }
 
     public void StateChanged()
     {
        switch(ie.CheckVictory())
         {
             case 0:
                 this.setTitle("TicTacToe! - Player " + ie.PlayerTurn() + "'s turn" );
                 return;
             case 1:
                 JOptionPane.showMessageDialog(this,"Player 1 won!");
                 break;
             case 2:
                 JOptionPane.showMessageDialog(this,"Player 2 won!");
                 break;
             case 3:
                 JOptionPane.showMessageDialog(this,"This game ended with a draw!");
                 break;
         }
 
         NewGame();
 
     }
 
     public void NewGame()
     {
         ie.NewGame();
 
         for (TicTacToePanel p : panels)
         {
            p.refresh();
         }
     }
 
     public void QuitGame()
     {
         if (JOptionPane.showConfirmDialog(this, "Are you sure you want to QuitGame?") == 0)
         {
             this.dispose();
         }
     }
 
 }
