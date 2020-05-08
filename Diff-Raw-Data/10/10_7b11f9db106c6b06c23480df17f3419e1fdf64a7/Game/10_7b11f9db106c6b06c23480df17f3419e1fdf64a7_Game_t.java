 package fr.iutvalence.java.mp.linerunner;
 
 /**
  * this class is one gameplay whose rules are in the README file.
  * 
  */
 public class Game
 {
     
     /**
      * declaration of row's number
      */
     public static final int ROWS = 5;
 
     /**
      * declaration of column's number
      */
     public static final int COLUMNS = 10;
     
     
     /**
      * an object who represents the character in the game
      * 
      */
     private Element player1;
     
     /**
      * create a variable who contain the object who can display the game
      */
     private Grid grid;
 
     /**
      * once created we've got a character on the screen and the player will jump or creep behind hurdles
      * constructors who create a new grid for each new game and make a scroll of
      * this grid
      */
     public Game()
     {
         this.player1 = new Element(ROWS-2,2);
         this.grid = new Grid();  
     }
     
     /**
      * 
      */
     public void play()
     {
        // TODO (fixed) comply with naming conventions
        boolean gameOver = false;
         System.out.print(this + "\n");
         
        while (!gameOver)
         {
           
             
             try
             {
                 Thread.sleep(1000);
             }
             catch (InterruptedException e){}
 
             
             boolean conditionCanJump = (this.player1.getPosition().getX()!=0 &&
                  (this.grid.grid[this.player1.getPosition().getY()][this.player1.getPosition().getX()-1] != this.grid.getHurdle() 
                || this.grid.grid[this.player1.getPosition().getY()][this.player1.getPosition().getX()+1]!=this.grid.getEmpty()));
             
           
             if (conditionCanJump)
             {
                 this.jump(this.player1.moveUp());
             }
             
            gameOver = this.scrolling();
             System.out.print(this + "\n");   //affichage de la grille
             
             
             System.out.println(this.grid.grid[this.player1.getPosition().getY()][this.player1.getPosition().getX()-1]);
             System.out.print(this.grid.getEmpty()+"\n");
             System.out.println(this.player1.getPosition().getX());
             System.out.print(this.grid.grid[this.player1.getPosition().getY()][this.player1.getPosition().getX()+1]+"\n");
             
             if(this.grid.grid[this.player1.getPosition().getY()][this.player1.getPosition().getX()+1] == this.grid.getEmpty())
             {
                 this.player1.setPosition(this.player1.getPosition().getX()+1,this.player1.getPosition().getY());
             }
   
             
 
         }
         System.out.println("Game over !!!");
         
     }
     
 
 
    
 
     /**
      * method that makes the grid scrolling
      * 
      * @return true if the character will die
      */
 
     private boolean scrolling()
     {   
         boolean game_over = false;
 
         for (int Y = 0; Y < COLUMNS - 1; Y++)
         {
             for (int X = 0; X < ROWS; X++)
             {
                 if(X == this.player1.getPosition().getX() && Y == this.player1.getPosition().getY()+1 && this.grid.grid[Y][X] == this.grid.getHurdle())
                 {
                     game_over = true;
                 }
                 else
                 {
                     this.grid.grid[Y][X] = this.grid.grid[Y + 1][X];
                 }
             }
             this.grid.grid[Y][ROWS - 1] = 4;
         }
         
         this.grid.obstacle();
         return game_over;
     }
     
     
     /**
      * method who manage one jump integraly
      */
     private void jump(boolean wantjump)
     {
         if(wantjump)
         {
             this.player1.setPosition(this.player1.getPosition().getX()-1, this.player1.getPosition().getY());
         }
     }
     
     
     /**
      * @see java.lang.Object#toString()
      */
     public String toString()
     {
         boolean caracters=false;
         String result = "";
 
         for (int i = 0; i < ROWS; i++)
         {
             for (int j = 0; j < COLUMNS; j++)
             {
                 
                 if (i==this.player1.getPosition().getX() && j== this.player1.getPosition().getY())
                 {
                     result += 3;
                     caracters=false;
                 }
                 else
                 {
                     result += this.grid.grid[j][i];
                 }
                 
             }
             result += "\n";
         }
 
         return result;
     }
         
 
         
 
 }
