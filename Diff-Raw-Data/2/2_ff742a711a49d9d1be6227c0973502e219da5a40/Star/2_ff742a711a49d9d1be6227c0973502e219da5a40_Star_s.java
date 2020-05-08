 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GameLogic;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import snakesandladders.SnakesAndLadders;
 
 /**
  *
  * @author Divyata
  */
 public class Star {
     private int affectsSquare;
     private int xPos;
     private int yPos;
     private static Image starIMG = null;
 
     /**
      *
      * @param g
      * @param parent
      *
      * @throws NullPointerException
      */
     public void draw(Graphics g, SnakesAndLadders parent) throws NullPointerException {
         if (g == null || parent == null)
             throw new NullPointerException();
         g.drawImage(starIMG,xPos,yPos, parent);  
     }
     
     /**
      *
      * @return the square it should "reside" on
      */
     public int getBoardPosition(){
         return this.affectsSquare;
     }
     
     Star(){
 	this.affectsSquare = 1 + (int)(Math.random() * (99));
         this.calcDrawXY();
         if (starIMG == null){
             try {
               starIMG = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/imgs/star.png"));
             }
             catch (Exception e) {
               System.out.printf(e.toString() + "\n");
             }
         }
     }
     
     Star(int squareNum){
         if (squareNum >= 0 && squareNum <= 100){
           this.affectsSquare = squareNum;
         } else if (squareNum < 0) { 
           this.affectsSquare = 0;
         } else {
          this.affectsSquare = 99;
         }
         this.calcDrawXY();
         if (starIMG == null){
             try {
               starIMG = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/imgs/star.png"));
             }
             catch (Exception e) {
               System.out.printf(e.toString() + "\n");
             }
         }
         
     }
     
     private void calcDrawXY(){
         int square = this.affectsSquare;
         int x, y;
         int xOffset = 16;
         int yOffset = 5;
         if (square % 20 < 11 && square % 20 != 0){
             square--;
             //board number increment left to right eg 1 2 3 .. 10
             x = ((square % 10)*80);
         } else {
             //board number increment right to left 20 19 18 .. 11
             square--;
             x = ((9 - (square % 10)) * 80);
         }
         y = 730 - ((square/10)*80);
         this.xPos = (int)((x + xOffset)*0.75);
         this.yPos = (int)((y + yOffset)*0.75);
     } 
 }
 
