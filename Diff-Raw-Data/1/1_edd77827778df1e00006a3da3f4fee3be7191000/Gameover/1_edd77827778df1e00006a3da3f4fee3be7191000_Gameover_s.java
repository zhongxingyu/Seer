 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package doodlejump.gui; 
 
 /** 
  * 
  * @author Ivan 
  */ 
 
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.*;
 import javax.swing.ImageIcon;
 import javax.swing.JTextArea;
 /**
  * Instance of the Gameover game state.
  * @author Ivan
  */
 public class Gameover { 
   
     private Image img;
     private String gameoverImageName;
     private int score;
     private String playerName;
     private Highscore highscore;
     private Graphics graphics;
     /**
      * Constructor sets up a new gameover screen with the given image.
      * @param highscore Instance of highscore class to send name and score there.
      */
     public Gameover(Highscore highscore) {
         
         this.playerName = "";
         gameoverImageName = "C:\\Users\\Ivan\\Documents\\GitHub\\OhHa\\DoodleJump\\src\\doodlejump\\gui\\images\\gameover.png";
         ImageIcon ii = new ImageIcon(gameoverImageName);
         this.img = ii.getImage();
         this.highscore = highscore;
     }
     
     /**
      * Resets the player name that gets typed in after a new high score.
      */
     public void reset(){
         this.playerName = "";
     }
     
     
     public void setScore(int score) {
         this.score = score;
     }
     
     /**
      * Adds a new letter to the player names and the draws it on the screen
      * @param letter that is added to the player name and drawn on the screen
      */
     public void writeCharToName(String letter){
         if(playerName.length() < 10){
             playerName += letter;
         }    
         this.piirra(graphics);
     }
     
     /**
      * Sends the score to the high score class/list
      */
     public void sendScoreToHighscore(){
         highscore.addScoreToTopTen(score, playerName);
     }
     /**
      * Method tells whether a new highscore is set
      * @return a boolean that tells whether the current score made the top ten
      */
     public boolean setNewHighscore(){
         return highscore.isScoreInTopTen(score);
     }
     
     /**
      * sets the Graphics to the given parameter
      * @param g the graphics parameter
      */
     public void setGraphics(Graphics g){
         this.graphics = g;
     }
     
     /**
      * Method that draws the background image and the text on the screen
      * and also calls the methods to draw different text depending on whether
      * the player made the high score list.
      * @param graphics 
      */
     public void piirra(Graphics graphics){
         graphics.drawImage(img, 0, 0, null);
         
         graphics.setFont(new Font("Serif", Font.BOLD, 25));
         String scoreString = "Your Score: ";
         scoreString += score;
         graphics.drawString(scoreString , 100, 300);
         graphics.drawString(this.playerName , 20, 450);
         
         if(highscore.isScoreInTopTen(score)){
             this.piirraIfHighscore(graphics);
         } else {
             this.piirraIfNotHighscore(graphics);
         }
     }
     
     /**
      * Draws "You Made A New Highscore!" and "Please write your name below:"
      * text if the player made a new high score
      * @param graphics for drawing the text on the screen
      */
     public void piirraIfHighscore(Graphics graphics){
         String str = "You Made A New Highscore!";
         graphics.drawString(str , 10, 350);
         String str2 = "Please write your name below:";
         graphics.drawString(str2 , 10, 400);
     }
     
     /**
      * Draws "Press 'R' to restart the game" and "You did not make a new highscore"
      * text if the player did not make a new high score.
      * The game can be restarted by pushing the letter "R" in this screen
      * @param graphics for drawing the text on the screen
      */
     public void piirraIfNotHighscore(Graphics graphics){
         String restart = "Press 'R' to restart the game";
         graphics.drawString(restart , 40, 400);
         String str = "You did not make a new highscore";
         graphics.drawString(str , 20, 350);
     }
 
     /**
      * Makes the use of Backspace possible when writing a name
      */
     public void removeCharFromName() {
         if (playerName.length() > 0) {
             playerName = playerName.substring(0, playerName.length()-1);		
 	}
         this.piirra(graphics);
     }
     
 }
