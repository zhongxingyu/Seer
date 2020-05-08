 package info.kulhan.reversi.controller;
 
 /**
  * Game controller
  */
 public interface IController {
 
    void loadGame();
 
    void newGame();
 
    void pass();
 
    void move(int row, int column);
 
    void quitGame();
 
    void saveGame();
 
     public void endGame();
 
     public boolean isGameLoadable();
     
 }
