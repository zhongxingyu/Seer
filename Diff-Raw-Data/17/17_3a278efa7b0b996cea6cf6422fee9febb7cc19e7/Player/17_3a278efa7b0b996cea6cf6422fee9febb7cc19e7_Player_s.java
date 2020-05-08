 package com.github.inside;
 
 import javax.swing.JLabel;
 
 class Player extends JLabel
 {
     public String name;
     public int score = 0;
 
     public Player(String name)
     {
         this.name = name;
         this.setText("0");
 
         if (this.name.equals("left"))
         {
             this.setBounds(10,
                            Config.BOARD_HEIGHT + Config.BOARD_CONTAINER_TOP_MARGIN + 10,
                           10,
                           10);
         }
         else
         {
             this.setBounds(Config.BOARD_WIDTH + Config.BOARD_CONTAINER_LEFT_MARGIN + Config.BOARD_CONTAINER_RIGHT_MARGIN - 20,
                            Config.BOARD_HEIGHT + Config.BOARD_CONTAINER_TOP_MARGIN + 10,
                           10,
                           10);
         }
     }
 
     public void updateScore()
     {
         this.score++;
        this.setText(String.valueOf(this.score));
     }
 
     public void resetScore()
     {
         this.score = 0;
        this.setText(String.valueOf(this.score));
     }
 
     public boolean hasReachedScore()
     {
         return this.score >= Config.MAX_SCORE;
     }
 }
