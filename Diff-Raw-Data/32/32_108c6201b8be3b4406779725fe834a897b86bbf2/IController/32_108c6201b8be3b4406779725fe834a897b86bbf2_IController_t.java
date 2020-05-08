 package info.kulhan.reversi.controller;
 
 /**
  * Game controller
  */
 public interface IController {
 
    public void loadGame();
 
    public void newGame();
 
    public void pass();
 
    public void move(int row, int column);
 
    public void quitGame();
 
    public void saveGame();
 
     public void endGame();
 
     public boolean isGameLoadable();
     
 }
