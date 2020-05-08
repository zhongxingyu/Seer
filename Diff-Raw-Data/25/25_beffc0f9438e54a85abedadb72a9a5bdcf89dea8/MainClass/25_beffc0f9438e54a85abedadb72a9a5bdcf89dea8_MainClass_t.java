 package ui;
 
 import ai.AlphaBeta;
 import datamodels.Board;
 import datamodels.Move;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Created by
  * User: jakub
  * Date: 24.05.13
  */
 public class MainClass {
 
     private static int n = -1;
     private static Board board;
     private static AlphaBeta minMax = null;
     private static AlphaBeta minMaxWhite = null;
 
     private static final int DEPTH = 4;
    private static final int WHITE_DEPTH = 4;
     private static final int WHITE_EVALUATE_OPTION = 3;
     private static final int BLACK_EVALUATE_OPTION = 3;
 
     public static void main(String[] args) {
 
         Object[] options = {"Player vs. Player",
                "Computer vs. Computer", "Player vs. Computer"};
         JFrame f = new JFrame();
         n = JOptionPane.showOptionDialog(f, "What game mode would you like to play?", "Select game mode.", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 
         switch(n){
             case 0:
                 System.out.println("Player vs Player");
                 setupPlayerVersusPlayerGame();
                 break;
             case 1:
                 System.out.println("Computer vs Computer");
                 setupComputerVersusComputerGame();
                 break;
             case 2:
                 System.out.println("Player vs Computer");
                 setupPlayerVersusComputerGame();
                 break;
         }
     }
 
     public static void setupComputerVersusComputerGame(){
         board = new Board();
         minMax = new AlphaBeta(board, true);
         minMaxWhite = new AlphaBeta(board, false);
 
         JLabel turnLabel = new JLabel("White's Turn");
         JPanel textPanel = new JPanel(new BorderLayout());
         JTextArea text = new JTextArea(5,20); //
         text.setMinimumSize(new Dimension(512, 100));
         text.setEditable(false);
         text.setText("Welcome to Chess: Player vs. Player.  Enjoy!");
 
         chessView = new ChessView(board, text, turnLabel);
 
         JScrollPane scroller = new JScrollPane(text);
         scroller.setMinimumSize(new Dimension(512,100));
 
         textPanel.add(scroller, BorderLayout.CENTER);
         textPanel.add(turnLabel, BorderLayout.PAGE_START);
 
         JFrame frame = new JFrame();
         frame.setLayout(new BorderLayout());
         frame.add(chessView, BorderLayout.CENTER);
         frame.add(textPanel, BorderLayout.PAGE_END);
         frame.pack();
         frame.setMinimumSize(new Dimension(512,612));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(false);
         frame.setVisible(true);
 
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
 
 
         System.out.println("SECOND AI making move");
         Thread t = new Thread(){
             @Override
             public void run() {
                 super.run();
                Move moveToMake = minMaxWhite.getBestMove(WHITE_DEPTH, board, WHITE_EVALUATE_OPTION);
 
                 if(moveToMake == null){
                     System.out.println("BLACK LOST !");
                 } else {
                     board.makeMove(moveToMake, false);
                     System.out.println("move ai made: " + moveToMake);
                     chessView.swapTurn();
                     chessView.repaint();
                 }
 
             }
         };
         t.start();;
     }
 
     public static void moveMade(Board boardMade){
         if(boardMade == board){
             System.out.println("Correct board made move, n: " + n);
 
             switch(n){
                 case 2:
 
 
                     if(board.getTurn() == minMax.getColor()){
                         System.out.println("AI making move");
                         Thread t = new Thread(){
                             @Override
                             public void run() {
                                 super.run();
                                 Move moveToMake = minMax.getBestMove(DEPTH, board, BLACK_EVALUATE_OPTION);
 
                                 if(moveToMake == null){
                                     System.out.println("BLACK LOST !");
                                 } else {
                                     board.makeMove(moveToMake, false);
                                     System.out.println("move ai made: " + moveToMake);
                                     chessView.swapTurn();
                                     chessView.repaint();
                                 }
 
                             }
                         };
                         t.start();
 
 
                     }
 
 
                     break;
                 case 1:
                     if(board.getTurn() == minMax.getColor()){
                         System.out.println("BLACK AI making move");
                         Thread t = new Thread(){
                             @Override
                             public void run() {
                                 super.run();
                                 Move moveToMake = minMax.getBestMove(DEPTH, board, BLACK_EVALUATE_OPTION);
 
                                 if(moveToMake == null){
                                     System.out.println("BLACK LOST !");
                                 } else {
                                     board.makeMove(moveToMake, false);
                                     System.out.println("move ai made: " + moveToMake);
                                     chessView.swapTurn();
                                     chessView.repaint();
                                 }
 
                             }
                         };
                         t.start();;
                     } else {
 
                         System.out.println("WHITE AI making move");
                         Thread t = new Thread(){
                             @Override
                             public void run() {
                                 super.run();
                                Move moveToMake = minMaxWhite.getBestMove(WHITE_DEPTH, board, WHITE_EVALUATE_OPTION);
 
                                 if(moveToMake == null){
                                     System.out.println("WHITE LOST !");
                                 } else {
                                     board.makeMove(moveToMake, false);
                                     System.out.println("move ai made: " + moveToMake);
                                     chessView.swapTurn();
                                     chessView.repaint();
                                 }
 
                             }
                         };
                         t.start();;
 
                     }
                 default:
                     break;
             }
         }
 
     }
 
     private static ChessView chessView;
 
 
     private static void setupPlayerVersusComputerGame() {
         board = new Board();
         minMax = new AlphaBeta(board, true);
 
         JLabel turnLabel = new JLabel("White's Turn");
         JPanel textPanel = new JPanel(new BorderLayout());
         JTextArea text = new JTextArea(5,20); //
         text.setMinimumSize(new Dimension(512, 100));
         text.setEditable(false);
         text.setText("Welcome to Chess: Player vs. Player.  Enjoy!");
 
         chessView = new ChessView(board, text, turnLabel);
 
         JScrollPane scroller = new JScrollPane(text);
         scroller.setMinimumSize(new Dimension(512,100));
 
         textPanel.add(scroller, BorderLayout.CENTER);
         textPanel.add(turnLabel, BorderLayout.PAGE_START);
 
         JFrame frame = new JFrame();
         frame.setLayout(new BorderLayout());
         frame.add(chessView, BorderLayout.CENTER);
         frame.add(textPanel, BorderLayout.PAGE_END);
         frame.pack();
         frame.setMinimumSize(new Dimension(512,612));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(false);
         frame.setVisible(true);
 
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
     }
 
     private static void setupPlayerVersusPlayerGame() {
         board = new Board();
 
         JLabel turnLabel = new JLabel("White's Turn");
         JPanel textPanel = new JPanel(new BorderLayout());
         JTextArea text = new JTextArea(5,20); //
         text.setMinimumSize(new Dimension(512, 100));
         text.setEditable(false);
         text.setText("Welcome to Chess: Player vs. Player.  Enjoy!");
 
         chessView = new ChessView(board, text, turnLabel);
 
         JScrollPane scroller = new JScrollPane(text);
         scroller.setMinimumSize(new Dimension(512,100));
 
         textPanel.add(scroller, BorderLayout.CENTER);
         textPanel.add(turnLabel, BorderLayout.PAGE_START);
 
         JFrame frame = new JFrame();
         frame.setLayout(new BorderLayout());
         frame.add(chessView, BorderLayout.CENTER);
         frame.add(textPanel, BorderLayout.PAGE_END);
         frame.pack();
         frame.setMinimumSize(new Dimension(512,612));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(false);
         frame.setVisible(true);
 
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
     }
 
 }
