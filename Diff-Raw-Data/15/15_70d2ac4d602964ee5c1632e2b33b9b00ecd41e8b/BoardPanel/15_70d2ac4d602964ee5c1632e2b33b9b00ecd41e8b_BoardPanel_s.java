 package tictactoe.applet;
 import tictactoe.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 /**
  * A {@code JPanel} representation of a TicTacToe {@code Board}. Uses {@code JButton}s to
  * represent each space.
  * <p>
  * The action command for each button is set to a string representation of the
  * index of the space it represents.
  *
  * @author Todd Taomae
  */
 @SuppressWarnings("serial")
 public class BoardPanel extends JPanel implements Runnable
 {
     private TicTacToeDriver driver;
     private Player playerX;
     private Player playerO;
     private JButton[] spaces;
 
     /**
      * Constructs a new {@code BoardPanel} with the specified types of
      *
      * @param   x   type of the X {@code Player}
      * @param   o   type of the o {@code Player}
      */
     public BoardPanel(PlayerType x, PlayerType o)
     {
         switch (x) {
             case MOUSE:
                 this.playerX = new MousePlayer();
                 break;
             case ALPHABETA:
                 this.playerX = new AlphaBetaPlayer(10);
                 break;
         }
 
         switch (o) {
             case MOUSE:
                 this.playerO = new MousePlayer();
                 break;
             case ALPHABETA:
                 this.playerO = new AlphaBetaPlayer(10);
                 break;
         }
 
         this.driver = new TicTacToeDriver(this.playerX, this.playerO);
 
         this.setLayout(new GridLayout(3, 3));
 
         // initialize buttons
         this.spaces = new JButton[9];
         for (int i = 0; i < this.spaces.length; i++) {
             JButton b = new JButton();
             this.spaces[i] = b;
 
             b.setActionCommand("" + i);
             b.setEnabled(false);
             b.setPreferredSize(new Dimension(50, 50));
             b.addActionListener(this.playerX);
             b.addActionListener(this.playerO);
             this.add(b);
 
         }
     }
 
     /**
      * Returns the winner of the current game.
      *
      * @return  the winner of the current game.
      */
     public Mark getWinner()
     {
         return this.driver.getWinner();
     }
 
     /**
      * Starts a game and updates this {@code BoardPanel} at the end of each turn.
      * <p>
      * Notifies all threads waiting on this object at the start and end of each execution.
      */
     public void run()
     {
         // notify for start of game
         synchronized(this) {
             this.notifyAll();
         }
 
         // clear and enable buttons
         for (JButton b : this.spaces) {
             b.setText("");
             b.setEnabled(true);
         }
 
         this.driver.setPlayerX(this.playerX);
         this.driver.setPlayerO(this.playerO);
         new Thread(this.driver).start();
 
         // TODO: analyze for race conditions
         while (this.driver.getWinner() == Mark.NONE) {
             try {
                 synchronized(this.driver) {
                     // wait until driver notifies after a move is played
                     this.driver.wait();
 
                     Mark[] state = this.driver.getState();
                     for (int i = 0; i < state.length; i++) {
                         switch(state[i]) {
                             case X:
                                 this.spaces[i].setText("X");
                                 this.spaces[i].setEnabled(false);
                                 break;
                             case O:
                                 this.spaces[i].setText("O");
                                 this.spaces[i].setEnabled(false);
                                 break;
                             case NONE:
                                 this.spaces[i].setText("");
                                 this.spaces[i].setEnabled(true);
                                 break;
                         }
 
                     }
                 }
             } catch (InterruptedException ie) {
                 // if thread is interruped, end game
                 System.out.println("thread interrupted");
                 break;
             }
         }
 
         for (JButton b : this.spaces) {
             b.setEnabled(false);
         }
 
         // notify for end of game
         synchronized(this) {
             this.notifyAll();
         }
     }
 
     /**
      * Creates new players for this {@code BoardPanel}.
      *
      * @param   x   player type for X player
      * @param   o   player type for O player
      */
     public void newPlayers(PlayerType x, PlayerType o)
     {
         switch (x) {
             case MOUSE:
                 this.playerX = new MousePlayer();
                 for (JButton b : spaces) {
                     b.addActionListener(this.playerX);
                 }
                 break;
             case ALPHABETA:
                 this.playerX = new AlphaBetaPlayer(10);
                 break;
         }
 
         switch (o) {
             case MOUSE:
                 this.playerO = new MousePlayer();
                 for (JButton b : spaces) {
                     b.addActionListener(this.playerO);
                 }
                 break;
             case ALPHABETA:
                 this.playerO = new AlphaBetaPlayer(10);
                 break;
         }
     }
 
 
 
     public static void main(String[] args)
     {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGui();
             }
         });
     }
 
     public static void createAndShowGui()
     {
         JFrame frame = new JFrame("TicTacToe");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         BoardPanel p = new BoardPanel(PlayerType.MOUSE, PlayerType.ALPHABETA);
         frame.add(p);
 
         frame.pack();
         frame.setVisible(true);
 
         new Thread(p).start();
     }
 }
 
 /**
  * Enumeration of possible types of players.
  *
  * @author Todd Taomae
  */
 enum PlayerType {MOUSE, ALPHABETA}
