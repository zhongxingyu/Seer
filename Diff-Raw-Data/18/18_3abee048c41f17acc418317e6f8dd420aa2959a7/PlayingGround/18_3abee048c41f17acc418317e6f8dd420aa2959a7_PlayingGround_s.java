 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package gamelogic;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author lenni
  */
 public class PlayingGround {
 
     public final Snake snake;
     public final PositionType[][] playingGround;
     public ArrayList items = new ArrayList();
     public Collision collision = null;
 
     public PlayingGround(int sizeX, int sizeY, Snake snake) {
         this.playingGround = new PositionType[sizeX][sizeY];
         this.snake = snake;
         this.collision = new Collision(this);
         collision.update();
     }
 
    
     
     public boolean bordercheck(){
         return true;
     }
 
     /**
      *
      * @param x xValue of Position in Array
      * @param y yValue of Position in Array
      * @param maxX lengthOfArray
      * @param maxY lengthOfArray
      * @return returns the value which should be written at the position x,y
      */
     public PositionType populateArrayAt(int x, int y, int maxX, int maxY) {
         if (x == snake.getPos().getX() && y == snake.getPos().getY()) {
             return PositionType.SNAKEHEAD;
         }
         if (x == 0 || x == maxX || y == 0 || y == maxY) {
             return PositionType.BORDER;
         }
 
         for (Position p : snake.getSnakePositions()) {
             if (p.getX() == x && p.getY() == y) {
                 return PositionType.SNAKETAIL;
             }
         }
         return PositionType.EMPTY;
     }
 
     //getter and setter
     public Snake getSnake() {
         return snake;
     }
 
     public PositionType[][] getPlayingGround() {
         return playingGround;
     }
 }
