 package tictactoe.applet;
 
 import tictactoe.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 /**
  * A {@code JPanel} for TicTacToe. Games must be played to completion after the "New game"
  * button is pressed. After a completed game, the winner is displayed.
  * The user may select to play a one or two player game.
  *
  * @author Todd Taomae
  */
 @SuppressWarnings("serial")
 public class TicTacToePanel extends JPanel implements ActionListener, Runnable
 {
     private BoardPanel board;
     private JLabel status;
     private JButton newGameButton;
 
     private JRadioButton onePlayerButtonA;
     private JRadioButton onePlayerButtonB;
     private JRadioButton twoPlayerButton;
 
     /**
      * Constructs a new {@code TicTacToePanel}.
      */
     public TicTacToePanel()
     {
         this.setLayout(new BorderLayout(0, 10));
         this.setPreferredSize(new Dimension(150, 250));
 
         this.board = this.board = new BoardPanel(new MousePlayer(), new AlphaBetaPlayer(10));
 
         this.status = new JLabel(" ");
         this.newGameButton = new JButton("New game");
         this.newGameButton.addActionListener(this);
 
         this.onePlayerButtonA = new JRadioButton("One Player [X]", true);
         this.onePlayerButtonB = new JRadioButton("One Player [O]", false);
         this.twoPlayerButton  = new JRadioButton("Two players", false);
         // add buttons to group
         ButtonGroup gameMode = new ButtonGroup();
         gameMode.add(this.onePlayerButtonA);
         gameMode.add(this.onePlayerButtonB);
         gameMode.add(this.twoPlayerButton);
 
         // panel with button group
         JPanel p = new JPanel();
         p.setLayout(new GridLayout(3, 1));
         p.setPreferredSize(new Dimension(150, 45));
         p.add(this.onePlayerButtonA);
         p.add(this.onePlayerButtonB);
         p.add(this.twoPlayerButton);
 
 
         this.add(this.board, BorderLayout.PAGE_START);
         this.add(this.status, BorderLayout.LINE_START);
         this.add(this.newGameButton, BorderLayout.LINE_END);
         this.add(p, BorderLayout.PAGE_END);
 
     }
 
     /**
      * When the newGameButton is clicked, disable the button then set the player types
      * based on which {@code JRadioButton} is selected.
      */
     public void actionPerformed(ActionEvent ae)
     {
         if (ae.getSource() == this.newGameButton) {
             this.newGameButton.setEnabled(false);
 
             // set player types
             if (this.onePlayerButtonA.isSelected()) {
                 this.board.newPlayers(new MousePlayer(), new AlphaBetaPlayer(10));
 
             } else if (this.onePlayerButtonB.isSelected()) {
                this.board.newPlayers(new MousePlayer(), new AlphaBetaPlayer(10));
 
             } else if (this.twoPlayerButton.isSelected()) {
                this.board.newPlayers(new MousePlayer(), new AlphaBetaPlayer(10));
             }
 
             new Thread(this.board).start();
         }
     }
 
     /**
      * Updates the status message at the start and end of each game.
      */
     public void run()
     {
         while (true) {
             try {
                 // wait until boardPanel notifies for game over or game start
                 synchronized(this.board) {
                     this.board.wait();
 
                     this.newGameButton.setEnabled(true);
 
                     switch (this.board.getWinner()) {
                         case X:
                             this.status.setText("X wins!");
                             break;
                         case O:
                             this.status.setText("O wins!");
                             break;
                         case DRAW:
                             this.status.setText("Draw!");
                             break;
                         case NONE:
                             this.status.setText(" ");
                             this.newGameButton.setEnabled(false);
                             break;
                     }
                 }
 
 
 
             } catch (InterruptedException ie) {
                 // do nothing
             }
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
 
         TicTacToePanel p = new TicTacToePanel();
         frame.add(p);
 
         frame.pack();
         frame.setVisible(true);
 
         new Thread(p).start();
     }
 }
