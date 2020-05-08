 package com.Grateds.Reversi.GUI;
 
 import com.Grateds.Reversi.CONTROLLER.Controller;
 import com.Grateds.Reversi.MODEL.Board;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.JFrame;
 
 public class SmallWindow extends JFrame implements Observer{
   
 	private static final long serialVersionUID = 1L;
 	private GameMenu menuBar;
 	private Controller controller;
 	private Board board;
 	private String WhiteScore;
 	private String BlackScore;
 	private GameGraphicsPanel gameGraphicsPanel1;
 
     public SmallWindow(Controller c) {
         setTitle("Reversi");
         controller = c;
         board = c.getBoard();
         board.addObserver(this);
         menuBar = new GameMenu(c);
         setJMenuBar(menuBar);
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         controller.initialization(); // fix
         GameGraphicsPanel gameGraphicsPanel1 = new GameGraphicsPanel(c);
         this.add(gameGraphicsPanel1);
         this.setSize(565, 605);
         setResizable(false);
         this.setLocationRelativeTo(getOwner());
         setVisible(true);   
         setScores(controller.getBlackScore(),controller.getWhiteScore());
     } // end MainWindow
     
     public Controller getController(){
     	return controller;
     }
     
     private void setScores(Integer BScore, Integer WScore){
     	      BlackScore=BScore.toString();
     	      WhiteScore=WScore.toString();      
     }
    
     public void update(Observable o, Object arg) {
         this.repaint();
     }
     
 }
