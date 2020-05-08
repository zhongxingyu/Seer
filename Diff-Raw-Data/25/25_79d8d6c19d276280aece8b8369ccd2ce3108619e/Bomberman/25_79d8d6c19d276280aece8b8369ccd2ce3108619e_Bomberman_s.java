 
 package bomberman;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import javax.swing.*;
 
 public class Bomberman extends JFrame {
    JLayeredPane layeredPane;
    JPanel board;
 
     public Bomberman(){
       Dimension boardSize = new Dimension(600, 600);
       
       layeredPane = new JLayeredPane();
       getContentPane().add(layeredPane);
       layeredPane.setPreferredSize(boardSize);
       
       board = new JPanel();
       layeredPane.add(board, JLayeredPane.DEFAULT_LAYER);
      board.setLayout( new GridLayout(10, 10) );
       board.setPreferredSize( boardSize );
       board.setBounds(0, 0, boardSize.width, boardSize.height);
       
       // board, white - traversible, gray - pillars
      for (int i = 0; i < 100; i++) {
         JPanel square = new JPanel( new BorderLayout() );
         board.add( square );

        int row = (i / 10) % 2;
         if (row == 0)
            square.setBackground( i % 2 == 0 ? Color.gray : Color.white );
        else
             square.setBackground( Color.white );
       }
       
       // Randomize wall positions (green)
       Random rand = new Random();
     
       int i = 0, wall;
      while( i < 100 ){
        int row = (i / 10) % 2;
        if( row == 0 ){
            if( i % 2 != 0 ){
                 wall = rand.nextInt(3);
                 if( wall == 0 ) board.getComponent(i).setBackground(Color.green);    
             }
         }
         else{
            wall = rand.nextInt(3);
            if( wall == 0 ) board.getComponent(i).setBackground(Color.green);     
         }
         i++;
      }
     }
     
     public static void main(String[] args) {
         Bomberman b = new Bomberman();
         b.setDefaultCloseOperation(DISPOSE_ON_CLOSE );
         b.pack();
         b.setResizable(true);
         b.setLocationRelativeTo( null );
         b.setVisible(true);
         b.setVisible(true);
     }
 }
