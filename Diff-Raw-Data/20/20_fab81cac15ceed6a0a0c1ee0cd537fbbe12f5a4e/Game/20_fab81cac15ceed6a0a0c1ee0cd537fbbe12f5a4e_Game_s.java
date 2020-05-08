 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lv.Javaguru.TicTacToe;
 
 /**
  *
  * @author Vitalij
  */
 public class Game {
    Game game =new Game();
   private  int[][] field;

    public int[][] getField() {
        return field;
    }

    public void setField(int[][] field) {
        this.field = field;
    }
    private int vert;
    private int hor;
    
   public void SetMoveOnField(int v, int h)
   {
       this.vert=v;
       this.hor=h;
              
   }
    
 }
