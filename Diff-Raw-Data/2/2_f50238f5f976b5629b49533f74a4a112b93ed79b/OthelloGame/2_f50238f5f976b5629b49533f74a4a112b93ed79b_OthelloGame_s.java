 package com.zoeetrope.othello.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JOptionPane;
 
 import com.zoeetrope.othello.ai.ImpossibleAI;
 import com.zoeetrope.othello.ai.OthelloAI;
 import com.zoeetrope.othello.ai.RandomAI;
 import com.zoeetrope.othello.model.Othello;
 import com.zoeetrope.othello.model.Piece;
 import com.zoeetrope.othello.model.PieceColor;
 import com.zoeetrope.othello.model.Player;
 import com.zoeetrope.othello.view.NewPlayerView;
 import com.zoeetrope.othello.view.OthelloWindow;
 
 public class OthelloGame implements ActionListener {
 
   private Othello othello;
   private OthelloWindow window;
   private ArrayList<OthelloAI> ais;
   
   public OthelloGame() {
     this.othello = new Othello();
     this.window = new OthelloWindow(this.othello, this);
     this.ais = new ArrayList<OthelloAI>();
     
     this.othello.addObserver(this.window);
   }
 
   @Override
   public void actionPerformed(ActionEvent e) {
     Pattern p1 = Pattern.compile("move:(\\d)/(\\d)");
     Pattern p2 = Pattern.compile("addPlayer:(\\s)");
     Matcher m1 = p1.matcher(e.getActionCommand());
     Matcher m2 = p2.matcher(e.getActionCommand());
     
     if(m1.find()) {
       Piece piece = othello.getNextPiece();
       piece.setLocation(Integer.parseInt(m1.group(1)), Integer.parseInt(m1.group(2)));
       
       othello.getBoard().makeMove(piece);
     } else if(e.getActionCommand().equals("newGame")) {
       // Remove any old AI's that are running.
       clearAIs();
       
       Player player1 = getNewPlayer("Player 1");
      Player player2 = getNewPlayer("Player 1");
       
       this.othello.clearGame();
       this.othello.addPlayer(player1);
       this.othello.addPlayer(player2);
       this.othello.startGame();
     } else if(e.getActionCommand().equals("quitGame")) {
       System.exit(0);
     } else if(m2.find()) {
       this.othello.addPlayer(new Player(m2.group(1)));
     }
   }
   
   private Player getNewPlayer(String title) {
     NewPlayerView dialog = new NewPlayerView();
     JOptionPane.showMessageDialog(null, dialog.getInputs(), title, 
         JOptionPane.PLAIN_MESSAGE);
     Player player = new Player(PieceColor.WHITE, dialog.getName());
     
     if(dialog.getType().equals("Random CPU")) {
       RandomAI cpu = new RandomAI(player, this.othello);
       this.ais.add(cpu);
     } else if(dialog.getType().equals("Impossible CPU")) {
       ImpossibleAI cpu = new ImpossibleAI(player, this.othello);
       this.ais.add(cpu);
     }
     
     return player;
   }
   
   private void clearAIs() {
     for(OthelloAI ai : this.ais) {
       ai.stopAI();
     }
     this.ais.clear();
   }
   
 }
