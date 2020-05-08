 package client.controller;
 
 import client.model.Play;
 import client.views.GameView;
 import client.views.swing.gameboard.Game;
 import client.views.swing.menu.Menu;
 import java.awt.Point;
 
 /**
  *
  * @author Romain <ro.foncier@gmail.com>
  */
 public class GameController {
 
     public GameView mainView = null;
 //    public GameView secondView = null;
     private Play play = null; // Model in MVC Architecture
 
     public GameController(Play play) {
         this.play = play;
         mainView = new Game(this);
 //        secondView = new client.views.console.Game(this);
         addListenersToModel();
     }
 
     private void addListenersToModel() {
         play.addTileListener(mainView);
         play.addRackListener(mainView);
         play.addGridListener(mainView);
         play.addErrorListener(mainView);
 //        play.addTileListener(secondView);
 //        play.addRackListener(secondView);
 //        play.addGridListener(secondView);
 //        play.addErrorListener(secondView);
     }
 
     public void addMenuToView(Menu menu) {
         ((Game) mainView).setMenu(menu.getPanel());
         menu.setGame((Game) mainView);
     }
 
     public void displayViews() {
         mainView.display();
 //        secondView.display();
     }
 
     public void closeViews() {
         mainView.close();
 //        secondView.close();
     }
     
     public void notifyCreateWord(int sourcePos, int x, int y) {
         play.createWord(sourcePos, x, y);
     }
 
     public void notifyModifiedWord(int sX, int sY, int tX, int tY) {
         play.modifiedWord(sX, sY, tX, tY);
     }
 
     public void notifyRemoveLetterFromWord(int x, int y, int targetPos) {
         play.removeLetterFromWord(x, y, targetPos);
     }
 
     public void notifyOrganizeRack(int sourcePos, int targetPos) {
         play.organizeRack(sourcePos, targetPos);
     }
 
     public void notifyReArrangeRack() {
         play.reArrangeRack();
     }
 
     public void notifyExchangeTiles() {
        play.exchangeTiles();
     }
 
     public void notifyValidWord() {
         play.validateWord();
     }
 
     public void notifySetTileBlank(Point source, String letter) {
         play.setTileBlank(source.x, source.y, (letter.toUpperCase()).charAt(0));
     }
 
     public void notifyBackTileBlank(int source) {
         play.backTileBlank(source);
     }
 }
